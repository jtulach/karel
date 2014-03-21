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

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;

/** Model annotation generates class Data with 
 * one message property, boolean property and read only words property
 */
@Model(className = "Karel", properties = {
    @Property(name = "tab", type = String.class),
    @Property(name = "message", type = String.class),
    @Property(name = "town", type = Town.class),
    @Property(name = "commands", type = Command.class, array = true),
    @Property(name = "source", type = String.class),
    @Property(name = "completions", type = Completion.class, array = true),
    @Property(name = "speed", type = int.class),
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

    @Function static void changeTabTask(Karel m) {
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
            KarelCompiler frame = KarelCompiler.execute(m.getTown(), root, data.getName());
            m.animate(frame);
        } catch (SyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @ModelOperation static void animate(final Karel m, KarelCompiler frame) {
        final KarelCompiler nxt;
        try {
            nxt = frame.next();
        } catch (SyntaxException ex) {
            final Town t = m.getTown();
            t.setError(ex.getErrorCode());
            t.getErrorParams().clear();
            ex.fillParams(t.getErrorParams());
            return;
        }
        if (nxt != null) {
            int spd = m.getSpeed();
            if (spd < 0) {
                spd = 50;
            }
            if (spd > 1000) {
                spd = 1000;
            }
            KAREL.schedule(new TimerTask() {
                @Override
                public void run() {
                    m.animate(nxt);
                }
            }, spd);
        }
    }
    
    @ModelOperation @Function static void compile(Karel m) {
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
    
    @OnReceive(url = "{url}") static void loadTasks(Karel m, TaskInfo[] arr) {
        m.getTasks().addAll(Arrays.asList(arr));
    }
    
}
