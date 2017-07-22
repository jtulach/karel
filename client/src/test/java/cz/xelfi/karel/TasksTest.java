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

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import net.java.html.junit.BrowserRunner;
import net.java.html.junit.HTMLContent;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@RunWith(BrowserRunner.class)
@HTMLContent(
    "<div id='workspace' style='height: 80vh; background-color: red'></div>"
)
public class TasksTest {
    private Karel KAREL;
    
    @Before
    public void initKarel() throws Throwable {
        Locale.setDefault(new Locale("cs", "CZ"));
    }
    
    public TasksTest() {
    }

    public String stepSolution() {
        return "PROCEDURE udelej-krok\n"
                + "  STEP\n"
                + "END\n";
    }
    public String turnbackSolution() {
        return "PROCEDURE celem\n"
                + "  LEFT\n"
                + "  LEFT\n"
                + "END\n";
    }

    public String twostepsSolution() {
        return "PROCEDURE two\n"
                + "  STEP\n"
                + "  STEP\n"
                + "END\n";
    }

    public String turnrightSolution() {
        return "PROCEDURE right\n"
                + "  LEFT\n"
                + "  LEFT\n"
                + "  LEFT\n"
                + "END\n";
    }
    
    public String sixstepsSolution() {
        return "PROCEDURE six\n"
            + "  REPEAT 6\n"
            + "    STEP\n"
            + "  END\n"
            + "END\n";
    }

    public String wallSolution() {
        return "PROCEDURE towall\n"
            + "  WHILE NOT WALL\n"
            + "    STEP\n"
            + "  END\n"
            + "END\n";
    }
    
    public String safestepSolution() {
        return "PROCEDURE safestep\n"
            + "  IF NOT WALL\n"
            + "    STEP\n"
            + "  END\n"
            + "END\n"
            + "";
    }
    
    public String addremoveSolution() {
        return "PROCEDURE addremove\n"
            + "  IF MARK\n"
            + "    TAKE\n"
            + "  ELSE\n"
            + "    PUT\n"
            + "  END\n"
            + "END\n";
    }

    public String pickupallSolution() {
        return "PROCEDURE pickup\n"
            + "  WHILE MARK\n"
            + "    TAKE\n"
            + "  END\n"
            + "END\n";
    }

    public String homeSolution() {
        return "PROCEDURE domu\n"
            + "  WHILE NOT WEST\n"
            + "    LEFT\n"
            + "  END\n"
            + "  REPEAT 2\n"
            + "    towall\n"
            + "    LEFT\n"
            + "  END\n"
            + "END\n"
            + wallSolution();
    }
    
    @Test
    public Runnable[] allTests() throws Throwable {
        if (KAREL == null) {
            KAREL = Main.onPageLoad();
            File browserDir = new File(System.getProperty("browser.rootdir"));
            assertTrue(browserDir.isDirectory());
            File tasks = new File(new File(new File(browserDir, "pages"), "tasks"), "list.js");
            assertTrue("File " + tasks + " found", tasks.isFile());
            KAREL.setTasksUrl(tasks.toURI().toString());
            KAREL.changeTabTask();
        }

        return new Runnable[] {
            new LoadListOfTasks(),
            new ChooseTasks(),
            new RunTests(),
        };
    }

    private void checkTest(TaskInfo ti) throws Throwable {
        KAREL.chooseTask(ti);
        final TaskDescription ct = KAREL.getCurrentTask();
        String s = null;
        try {
            assertNotNull("Tasks loaded", ct);

            int dash = ti.getUrl().indexOf('-');
            int end = ti.getUrl().indexOf(".js");

            s = ti.getUrl().substring(dash + 1, end);

            Method m = getClass().getMethod(s + "Solution");
            final String res = (String) m.invoke(this);

            int newLine = res.indexOf('\n');
        
            final String name = res.substring("PROCEDURE ".length(), newLine);
            assertEquals("Still same", KAREL.getCurrentTask(), ct);
            KAREL.setSource(res);
            KarelModel.compileSource(KAREL);
            Command cmd = null;
            for (Command c : KAREL.getCommands()) {
                if (name.equals(c.getName())) {
                    cmd = c;
                    break;
                }
            }
            assertNotNull("Command found", cmd);
            KAREL.setSpeed(-1);
            assertEquals("Run all test at once", -1, KAREL.getSpeed());
            KarelModel.invoke(KAREL, cmd);

            for (TaskTestCase c : ct.getTests()) {
                assertEquals("Case " + c.getDescription() + " from " + ti.getUrl() + " is OK", c.getState(), "ok");
            }
        } catch (Throwable t) {
            throw new AssertionError("Error running " + s, t);
        }
    }
    
    private static <E extends Throwable> E raise(Class<E> type, Throwable ex) throws E {
        throw (E)ex;
    }

    private class LoadListOfTasks implements Runnable {
        @Override
        public void run() {
            final List<TaskInfo> tasks = KAREL.getTasks();
        }
    }

    private class ChooseTasks implements Runnable {
        @Override
        public void run() {
            final List<TaskInfo> tasks = KAREL.getTasks();
            for (TaskInfo ti : tasks) {
                KAREL.chooseTask(ti);
            }
        }
    }

    private class RunTests implements Runnable {
        @Override
        public void run() {
            final List<TaskInfo> tasks = KAREL.getTasks();
            final int size = tasks.size();
            for (int i = 0; i < size; i++) {
                try {
                    checkTest(tasks.get(i));
                } catch (Throwable ex) {
                    throw raise(RuntimeException.class, ex);
                }
            }
        }
    }
}
