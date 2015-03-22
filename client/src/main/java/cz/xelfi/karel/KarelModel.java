/**
 * Karel
 * Copyright (C) 2014 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
 */
package cz.xelfi.karel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;

/** Model annotation generates class Data with 
 * one message property, boolean property and read only words property
 */
@Model(className = "Karel", targetId = "", properties = {
    @Property(name = "tab", type = String.class),
    @Property(name = "message", type = String.class),
    @Property(name = "currentTask", type = TaskDescription.class),
    @Property(name = "currentInfo", type = TaskInfo.class),
    @Property(name = "scratch", type = Scratch.class),
    @Property(name = "commands", type = Command.class, array = true),
    @Property(name = "source", type = String.class),
    @Property(name = "completions", type = Completion.class, array = true),
    @Property(name = "speed", type = int.class),
    @Property(name = "running", type = boolean.class),
    @Property(name = "tasks", type = TaskInfo.class, array = true)
})
final class KarelModel {
    private static final Timer KAREL = new Timer("Karel Moves");
    @Model(className = "Command", properties = {
        @Property(name = "name", type = String.class)
    })
    final static class CommandModel {
    }
    
    @Function static void changeTabTown(Karel m) {
        m.setTab("town");
    }

    @ModelOperation @Function static void changeTabTask(Karel m) {
        m.loadTasks("tasks/list.js");
        m.setTab("task");
    }

    @Function static void changeTabEdit(Karel m) {
        m.setTab("edit");
    }

    @Function static void changeTabHome(Karel m) {
        m.setTab("home");
    }
    
    @Function static void changeTabAbout(Karel m) {
        m.setTab("about");
    }
    
    @Function static void templateShown(Karel m) {
        if ("edit".equals(m.getTab())) {
            KarelMirror.initialize();
            Object cm = KarelMirror.initCodeMirror(m, "editor");
        }
    }
    
    @Function static void invoke(Karel m, Command data) {   
        try {
            KarelCompiler.Root root = (KarelCompiler.Root) KarelCompiler.toAST(m.getSource());
            List<TaskTestCase> arr = m.getCurrentTask().getTests();
            List<KarelCompiler> comps = new ArrayList<KarelCompiler>(arr.size());
            TaskTestCase showing = null;
            for (TaskTestCase c : arr) {
                TaskModel.TestCaseModel.reset(c, false, null);
                if (c.getShowing() != null) {
                    showing = c;
                }
                KarelCompiler frame = KarelCompiler.execute(c.getCurrent(), root, data.getName());
                comps.add(frame);
            }
            if (showing == null && arr.size() > 0) {
                arr.get(0).setShowing("current");
            }
            m.animate(comps);
        } catch (SyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @Function static void edit(Karel m) {
        String cmd = m.getCurrentTask().getCommand();
        if (!m.getSource().contains(cmd)) {
            m.setSource(m.getSource() + "\n\n" + cmd +"\n  \n" + KarelToken.END.text() + "\n");
        }
        m.setTab("edit");
    }
    
    @ModelOperation static void animate(final Karel model, List<KarelCompiler> frames) {
        final List<KarelCompiler> next = new ArrayList<KarelCompiler>(frames.size());
        for (KarelCompiler frame : frames) {
            try {
                KarelCompiler nxt = frame.next();
                if (nxt == null) {
                    continue;
                }
                next.add(nxt);
            } catch (SyntaxException ex) {
                final Town t = frame.getTown();
                t.setError(ex.getErrorCode());
                t.getErrorParams().clear();
                ex.fillParams(t.getErrorParams());
            }
        }
        if (!next.isEmpty()) {
            model.setRunning(true);
            int spd = model.getSpeed();
            if (spd < 0) {
                spd = 50;
            }
            if (spd > 1000) {
                spd = 1000;
            }
            KAREL.schedule(new TimerTask() {
                @Override
                public void run() {
                    model.animate(next);
                }
            }, spd);
        } else {
            model.setRunning(false);
            boolean ok = true;
            for (TaskTestCase c : model.getCurrentTask().getTests()) {
                ok &= TaskModel.TestCaseModel.checkState(c);
            }
            int award = 1;
            if (ok && model.getCurrentInfo() != null && model.getCurrentInfo().getAwarded() < award) {
                model.getCurrentInfo().setAwarded(award);
            }
            model.getCurrentTask().setAwarded(1);
        }
    }
    
    @ModelOperation @Function static void compile(Karel m) {
        TaskModel.DescriptionModel.reset(m.getCurrentTask(), false);
        compile(m, true);
    }
    static void compile(Karel m, boolean switchToTown) {
        try {
            KarelCompiler.Root root = (KarelCompiler.Root) KarelCompiler.toAST(m.getSource());
            List<Command> lst = m.getCommands();
            lst.clear();
            for (KarelCompiler.AST ast : root.children) {
                if (ast instanceof KarelCompiler.Define) {
                    KarelCompiler.Define d = (KarelCompiler.Define) ast;
                    m.getCommands().add(new Command(d.token.text().toString()));
                }
            }
            if (switchToTown) {
                m.setTab("town");
            }
        } catch (SyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @OnPropertyChange("source") static void storeSource(Karel m) {
        KarelMirror.setLocalText(m.getSource());
    }
    
    @ModelOperation static void updateCompletions(Karel m, List<Completion> compl) {
        m.getCompletions().clear();
        m.getCompletions().addAll(compl);
    }
    
    @Function static void complete(Karel m, Completion data) {
        KarelMirror.complete("editor", data.getWord(), data.getThen(), data.getLine(), data.getStart(), data.getEnd());
    }
    
    @Function static void newLine(Karel m) {
        KarelMirror.newLine("editor");
    }
    
    @Model(className="Completion", properties = {
        @Property(name="name", type = String.class),
        @Property(name="word", type = String.class),
        @Property(name="line", type = int.class),
        @Property(name="start", type = int.class),
        @Property(name="end", type = int.class),
        @Property(name="then", type = String.class)
    })
    static class CompletionModel {
    }
    
    private static boolean containsURL(List<TaskInfo> arr, String url) {
        for (TaskInfo ti : arr) {
            if (url.equals(ti.getUrl())) {
                return true;
            }
        }
        return false;
    }
    
    @OnReceive(url = "{url}", onError = "errorLoadingTask") 
    static void loadTasks(Karel m, TaskInfo[] arr) {
        for (TaskInfo ti : arr) {
            if (!containsURL(m.getTasks(), ti.getUrl())) {
                m.getTasks().add(ti);
            }
        }
            
        int gathered = 0;
        for (TaskInfo ti : m.getTasks()) {
            gathered += ti.getAwarded();
        }
        for (TaskInfo ti : m.getTasks()) {
            ti.setDisabled(ti.getRequired() > gathered);
        }
    }

    @ModelOperation @Function static void chooseTask(Karel m, TaskInfo data) {
        m.setCurrentTask(null);
        m.setCurrentInfo(data);
        m.loadTaskDescription(data.getUrl());
    }
    
    @OnReceive(url = "{url}", onError = "errorLoadingTask") 
    static void loadTaskDescription(Karel m, TaskDescription td) {
        for (TaskTestCase c : td.getTests()) {
            Town e = new Town();
            TownModel.load(e, c.getEnd());
            c.setEnd(e);
            Town s = new Town();
            TownModel.load(s, c.getStart());
            c.setStart(s);
            TaskModel.TestCaseModel.reset(c, true, "");
        }
        m.setCurrentTask(td);
        m.setTab("town");
    }
    
    static void errorLoadingTask(Karel m, Exception ex) {
        TaskDescription td = new TaskDescription("Error", "Cannot load task: " + ex.getLocalizedMessage(), null, 0);
        m.setCurrentTask(td);
    }
}
