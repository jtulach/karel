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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.html.BrwsrCtx;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Models;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;

/** Model annotation generates class Data with
 * one message property, boolean property and read only words property
 */
@Model(className = "Karel", targetId = "", instance = true, builder = "assign", properties = {
    @Property(name = "tab", type = String.class),
    @Property(name = "message", type = String.class),
    @Property(name = "currentTask", type = TaskDescription.class),
    @Property(name = "currentInfo", type = TaskInfo.class),
    @Property(name = "scratch", type = Scratch.class),
    @Property(name = "commands", type = Command.class, array = true),
    @Property(name = "selectedCommand", type = Command.class),
    @Property(name = "source", type = String.class),
    @Property(name = "speed", type = int.class),
    @Property(name = "paused", type = boolean.class),
    @Property(name = "running", type = boolean.class),
    @Property(name = "tasksUrl", type = String.class),
    @Property(name = "tasks", type = TaskInfo.class, array = true)
})
final class KarelModel {
    /** @guardedby(this) */
    private List<List<KarelCompiler>> pausedFrames;
    private static Karel karel;
    private static final Timer KAREL = new Timer("Karel Moves");
    private static Workspace workspace;

    static Karel onPageLoad(String... args) throws Exception {
        String src = Storage.getDefault().get("source", "\n\n");
        final Scratch s = new Scratch();
        s.getTown().clear();

        karel = new Karel().
                assignTab("home").
                assignSpeed(50).
                assignScratch(s).
                assignSource(src).
                assignCurrentTask(null).
                assignCurrentInfo(null).
                assignSelectedCommand(null).
                assignTasksUrl("tasks/list.js");
        KarelModel.compile(karel, false);
        karel.applyBindings();

        final Workspace workArea = KarelModel.findWorkspace(karel);
        workArea.addSelectionChange(new Runnable() {
            @Override
            public void run() {
                refreshCommands(karel, true);
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
    static void showCode(Karel model) {
        Workspace w = findWorkspace(model);
        Procedure proc = w.getSelectedProcedure();
        if (proc != null) {
            model.setSource(proc.getCode());
            model.setTab("edit");
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
        refreshCommands(m, true);
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
            refreshCommands(m, false);
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
            final Workspace w = findWorkspace(m);
            w.clear();
            w.loadXML(xml);
        }
        String json = Storage.getDefault().get("town", null);
        if (json != null) {
            ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes());
            try {
                Town town = Models.parse(BrwsrCtx.findDefault(KarelModel.class), Town.class, is);
                TownModel.load(m.getScratch().getTown(), town);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Function
    static void storeWorkspace(Karel m) {
        Storage.getDefault().put("workspace", findWorkspace(m).toString());
        Storage.getDefault().put("town", TownModel.toJSON(m.getScratch().getTown()));
    }

    private static void refreshCommands(Karel m, boolean select) {
        Procedure selectedProc = findWorkspace(m).getSelectedProcedure();
        Command selectedCommand = null;

        List<Command> arr = new ArrayList<>(m.getCommands());
        int index = 0;
        for (Procedure p : findWorkspace(m).getProcedures()) {
            Command current = index < arr.size() ? arr.get(0) : null;
            if (current != null && current.getId().equals(p.getId())) {
                current.setName(p.getName());
            } else {
                arr.add(index, new Command(p.getId(), p.getName()));
            }
            if (selectedProc != null && selectedProc.getId().equals(p.getId())) {
                selectedCommand = arr.get(index);
            }
            index++;
        }
        if (index < arr.size()) {
            arr.subList(index, arr.size()).clear();
        }
        m.assignCommands(arr.toArray(new Command[0]));
        if (select) {
            m.setSelectedCommand(selectedCommand);
        }
    }

    @Function static void invokeScratch(Karel m) {
        Command data = m.getSelectedCommand();
        if (data == null) {
            return;
        }
        Procedure procedure = findWorkspace(m).findProcedure(data.getId());
        if (procedure == null) {
            refreshCommands(m, false);
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
            refreshCommands(m, false);
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
        m.setTab("town");
    }

    @Function static void stop(Karel m) {
        m.setRunning(false);
    }

    @Function
    void pause(Karel m) {
        List<List<KarelCompiler>> wakeUp = Collections.emptyList();
        synchronized (this) {
            if (this.pausedFrames == null) {
                this.pausedFrames = new ArrayList<>();
                m.setPaused(true);
            } else {
                wakeUp = this.pausedFrames;
                this.pausedFrames = null;
                m.setPaused(false);
            }
        }
        for (List<KarelCompiler> frames : wakeUp) {
            animate(m, frames);
        }
    }

    @Function
    void step(Karel m) {
        synchronized (this) {
            if (this.pausedFrames == null) {
                return;
            }
            ListIterator<List<KarelCompiler>> it = pausedFrames.listIterator();
            while (it.hasNext()) {
                List<KarelCompiler> frames = it.next();
                List<KarelCompiler> newFrames = animateOne(m, frames);
                it.set(newFrames);
            }
        }
    }

    @ModelOperation void animate(final Karel model, List<KarelCompiler> frames) {
        final List<KarelCompiler> next = animateOne(model, frames);
        if (!next.isEmpty()) {
            model.setRunning(true);
            int spd = 1000 / model.getSpeed();
            if (spd < 0) {
                animate(model, next);
            } else {
                synchronized (this) {
                    if (this.pausedFrames != null) {
                        this.pausedFrames.add(next);
                        return;
                    }
                }
                if (spd < 3) {
                    spd = 3;
                }
                if (spd > 1000) {
                    spd = 1000;
                }
                KAREL.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!model.isRunning()) {
                            return;
                        }
                        model.animate(next);
                    }
                }, spd);
            }
        } else {
            model.setRunning(false);
            boolean ok = true;
            final TaskDescription currentTask = model.getCurrentTask();
            if (currentTask != null) {
                for (TaskTestCase c : currentTask.getTests()) {
                    ok &= TaskModel.TestCaseModel.checkState(c);
                }
                int award = 1;
                if (ok && model.getCurrentInfo() != null && model.getCurrentInfo().getAwarded() < award) {
                    model.getCurrentInfo().setAwarded(award);
                }
                currentTask.setAwarded(1);
            }
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
        refreshCommands(m, false);
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
        refreshCommands(m, true);
        m.setTab("town");
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
