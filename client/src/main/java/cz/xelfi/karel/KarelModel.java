/**
 * Karel
 * Copyright (C) 2014-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import cz.xelfi.karel.blockly.Execution.State;
import cz.xelfi.karel.blockly.Procedure;
import cz.xelfi.karel.blockly.Workspace;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
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
    @Property(name = "selectedCommand", type = Command.class),
    @Property(name = "source", type = String.class),
    @Property(name = "speed", type = int.class),
    @Property(name = "running", type = boolean.class),
    @Property(name = "tasksUrl", type = String.class),
    @Property(name = "tasks", type = TaskInfo.class, array = true)
})
final class KarelModel {
    private static Karel karel;
    private static final Timer KAREL = new Timer("Karel Moves");
    private static Workspace workspace;

    static Karel onPageLoad(String... args) throws Exception {
        String src = Storage.getDefault().get("source", "\n\n");
        final Scratch s = new Scratch();
        s.getTown().clear();

        karel = new Karel("home", "msg", null, null, s, null, src, 300, false, "tasks/list.js");
        KarelModel.compile(karel, false);
        karel.applyBindings();

        final Workspace workArea = KarelModel.findWorkspace(karel);
        workArea.addSelectionChange(new Runnable() {
            @Override
            public void run() {
                Procedure proc = workArea.getSelectedProcedure();
                if (proc == null) {
                    karel.setSelectedCommand(null);
                } else {
                    karel.setSelectedCommand(new Command(proc.getId(), proc.getName()));
                }
            }
        });

        return karel;
    }

    @ComputedProperty
    static boolean canStartScratch(Command selectedCommand, boolean running) {
        return !running && selectedCommand != null;
    }

    @Function
    static void collapse(Karel model) {
        Workspace w = findWorkspace(model);
        Procedure proc = w.getSelectedProcedure();
        if (proc != null) {
            proc.setCollapsed(true);
        }
    }

    @Function
    static void expand(Karel model) {
        Workspace w = findWorkspace(model);
        Procedure proc = w.getSelectedProcedure();
        if (proc != null) {
            proc.setCollapsed(false);
        }
    }

    @Function
    static void useTestForScratch(Karel model, TaskTestCase data) {
        data.getStart().copyTo(model.getScratch().getTown());
    }

    @Model(className = "Command", properties = {
        @Property(name = "id", type = String.class),
        @Property(name = "name", type = String.class)
    })
    final static class CommandModel {
    }

    @Function static void changeTabTown(Karel m) {
        m.setTab("town");
    }

    @ModelOperation @Function static void changeTabTask(Karel m) throws URISyntaxException {
        String tasks = m.getTasksUrl();
        m.loadTasks(tasks, new URI(tasks));
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
            refreshCommands(m);
        }
    }

    static Workspace findWorkspace(Karel model) {
        if (workspace == null) {
            workspace = Workspace.create("workspace");
        }
        return workspace;
    }

    @Function
    static void loadWorkspace(Karel m) {
        String xml = Storage.getDefault().get("workspace", null);
        if (xml != null) {
            findWorkspace(m).loadXML(xml);
        }
    }

    private static void refreshCommands(Karel m) {
        if (findWorkspace(m).isEmpty()) {
            return;
        }
        List<Command> arr = new ArrayList<>();
        for (Procedure p : findWorkspace(m).getProcedures()) {
            arr.add(new Command(p.getId(), p.getName()));
        }
        Storage.getDefault().put("workspace", findWorkspace(m).toString());
        m.getCommands().clear();
        m.getCommands().addAll(arr);
    }

    @Function static void invokeScratch(Karel m) {
        Command data = m.getSelectedCommand();
        if (data == null) {
            return;
        }
        Procedure procedure = findWorkspace(m).findProcedure(data.getId());
        if (procedure == null) {
            refreshCommands(m);
            return;
        }
        List<KarelCompiler> comps = new ArrayList<>();
        KarelCompiler frame = KarelCompiler.execute(m.getScratch().getTown(), procedure, data.getName());
        comps.add(frame);
        m.animate(comps);
    }

    @Function static void invoke(Karel m, Command data) {
        Procedure procedure = findWorkspace(m).findProcedure(data.getId());
        if (procedure == null) {
            refreshCommands(m);
            return;
        }
        List<TaskTestCase> arr = m.getCurrentTask().getTests();
        TaskTestCase showing = null;
        List<KarelCompiler> comps = new ArrayList<>();
        for (TaskTestCase c : arr) {
            TaskModel.TestCaseModel.reset(c, false, null);
            if (c.getShowing() != null) {
                showing = c;
            }
            KarelCompiler frame = KarelCompiler.execute(c.getCurrent(), procedure, data.getName());
            comps.add(frame);
        }
        if (showing == null && arr.size() > 0) {
            arr.get(0).setShowing("current");
        }
        m.animate(comps);
    }

    @Function static void edit(Karel m) {
        String cmd = m.getCurrentTask().getCommand();
        Workspace w = findWorkspace(m);
        Procedure proc = w.findProcedure(cmd);
        if (proc == null) {
            proc = w.newProcedure(cmd);
        }
        proc.select();
        m.setTab("edit");
    }

    @ModelOperation static void animate(final Karel model, List<KarelCompiler> frames) {
        final List<KarelCompiler> next = animateOne(model, frames);
        if (!next.isEmpty()) {
            model.setRunning(true);
            int spd = model.getSpeed();
            if (spd < 0) {
                animate(model, next);
            } else {
                if (spd < 50) {
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
            }
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

    private static List<KarelCompiler> animateOne(final Karel model, List<KarelCompiler> frames) {
        final List<KarelCompiler> next = new ArrayList<>();
        for (KarelCompiler frame : frames) {
            State nxt = frame.exec.next();
            switch (nxt) {
                case RUNNING:
                    next.add(frame);
                    continue;
                case FINISHED:
                    continue;
                default:
                    final Town t = frame.town;
                    int e = 0;
                    switch (nxt) {
                        case ERROR_EMPTY:
                            e = 2;
                            break;
                        case ERROR_FULL:
                            e = 3;
                            break;
                        case ERROR_WALL:
                            e = 1;
                            break;
                        case ERROR_NOT_FOUND:
                            e = 4;
                            break;
                    }
                    t.setError(e);
                    t.getErrorParams().clear();
                    //ex.fillParams(t.getErrorParams());
            }
        }
        return next;
    }

    @ModelOperation @Function static void compile(Karel m) {
        TaskModel.DescriptionModel.reset(m.getCurrentTask(), false);
        compile(m, true);
    }
    static void compile(Karel m, boolean switchToTown) {
        refreshCommands(m);
        if (switchToTown) {
            m.setTab("town");
        }
    }

    @Function
    static void loadSource(Karel m) {
        StringBuilder sb = new StringBuilder();
        Workspace w = findWorkspace(m);
        for (Procedure p : w.getProcedures()) {
            sb.append("\n").append(p.getCode()).append("\n");
        }
        m.setSource(sb.toString());
    }

    @Function
    static void compileSource(Karel m) {
        Workspace w = findWorkspace(m);
        w.parse(m.getSource());
        refreshCommands(m);
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
    static void loadTasks(Karel m, TaskInfo[] arr, URI baseUrl) {
        for (TaskInfo ti : arr) {
            URI url = baseUrl.resolve(ti.getUrl());
            if (!containsURL(m.getTasks(), url.toString())) {
                ti.setUrl(url.toString());
                m.getTasks().add(ti);
            }
        }

        int gathered = 0;
        for (TaskInfo ti : m.getTasks()) {
            gathered += ti.getAwarded();
        }
        int persistGathered = Storage.getDefault().getInt("gathered", 0);
        if (persistGathered > gathered) {
            gathered = persistGathered;
        } else {
            Storage.getDefault().putInt("gathered", gathered);
        }
        for (TaskInfo ti : m.getTasks()) {
            ti.setDisabled(ti.getRequired() > gathered);
        }
    }

    @ModelOperation @Function static void chooseTask(Karel m, TaskInfo data) {
        m.setCurrentTask(null);
        m.setCurrentInfo(data);
        if (data.getDescription() == null) {
            m.loadTaskDescription(data.getUrl(), data);
        } else {
            loadTaskDescription(m, data.getDescription(), data);
        }
    }

    @OnReceive(url = "{url}", onError = "errorLoadingTask")
    static void loadTaskDescription(Karel m, TaskDescription td, TaskInfo data) {
        data.setDescription(td);
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
