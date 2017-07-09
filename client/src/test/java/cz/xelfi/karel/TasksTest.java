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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import net.java.html.BrwsrCtx;
import net.java.html.junit.BrowserRunner;
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
public class TasksTest {
    private BrwsrCtx CTX;
    private Karel KAREL;
    
    @Before
    public void initKarel() throws Throwable {
        Locale.setDefault(new Locale("cs", "CZ"));
    }
    
    public TasksTest() {
    }

    public String stepSolution() {
        return "udelej-krok\n"
                + "  krok\n"
                + "konec\n";
    }
    public String turnbackSolution() {
        return "celem\n"
                + "  vlevo-vbok\n"
                + "  vlevo-vbok\n"
                + "konec\n";
    }

    public String twostepsSolution() {
        return "two\n"
                + "  krok\n"
                + "  krok\n"
                + "konec\n";
    }

    public String turnrightSolution() {
        return "right\n"
                + "  vlevo-vbok\n"
                + "  vlevo-vbok\n"
                + "  vlevo-vbok\n"
                + "konec\n";
    }
    
    public String sixstepsSolution() {
        return "six\n" 
            + "  opakuj 6\n"
            + "    krok\n"
            + "  konec\n"
            + "konec\n";
    }

    public String wallSolution() {
        return "towall\n" 
            + "  dokud neni zed\n"
            + "    krok\n"
            + "  konec\n"
            + "konec\n";
    }
    
    public String safestepSolution() {
        return "safestep\n"
            + "  kdyz neni zed\n"
            + "    krok\n"
            + "  konec\n"
            + "konec\n"
            + "";
    }
    
    public String addremoveSolution() {
        return "addremove\n"
            + "  kdyz je znacka\n"
            + "    zvedni\n"
            + "  jinak\n"
            + "    poloz\n"
            + "  konec\n"
            + "konec\n";
    }

    public String pickupallSolution() {
        return "pickup\n"
            + "  dokud je znacka\n"
            + "    zvedni\n"
            + "  konec\n"
            + "konec\n";
    }

    public String homeSolution() {
        return "domu\n"
            + "  dokud neni zapad\n"
            + "    vlevo-vbok\n"
            + "  konec\n"
            + "  opakuj 2\n"
            + "    towall\n"
            + "    vlevo-vbok\n"
            + "  konec\n"
            + "konec\n" 
            + wallSolution();
    }
    
    @Test
    public void allTests() throws Throwable {
        if (KAREL == null) {
            KAREL = Main.onPageLoad();
            KAREL.changeTabTask();
        }
        final List<TaskInfo> tasks = KAREL.getTasks();
        final int size = tasks.size();
        for (int i = 0; i < size; i++) {
            checkTest(tasks.get(i));
        }
    }

    private void checkTest(TaskInfo ti) throws Throwable {
        KAREL.chooseTask(ti);
        class Wait implements Runnable {
            CountDownLatch down;
            TaskDescription ct;
            @Override
            public void run() {
                ct = KAREL.getCurrentTask();
                down.countDown();
            }
            
            TaskDescription get() throws Exception {
                down = new CountDownLatch(1);
                CTX.execute(this);
                down.await();
                return ct;
            }
        }
        Wait w = new Wait();
        for (int i = 0; i < 50; i++) {
            if (w.get() != null) {
                break;
            }
            Thread.sleep(100);
        }
        final TaskDescription ct = w.get();
        assertNotNull("Tasks loaded", ct);

        int dash = ti.getUrl().indexOf('-');
        int end = ti.getUrl().indexOf(".js");

        String s = ti.getUrl().substring(dash + 1, end);
        
        Method m = getClass().getMethod(s + "Solution");
        final String res = (String) m.invoke(this);
        
        int newLine = res.indexOf('\n');
        final String name = res.substring(0, newLine);
        
        class DoTest implements Runnable {
            CountDownLatch down = new CountDownLatch(1);
            Throwable ex;
            
            @Override
            public void run() {
                try {
                    doTest();
                } catch (Throwable t) {
                    ex = t;
                } finally {
                    down.countDown();
                }
            }
            
            private void doTest() throws Exception {
                assertEquals("Still same", KAREL.getCurrentTask(), ct);
                KAREL.setSource(res);
                KarelModel.compile(KAREL);
                Command cmd = null;
                for (Command c : KAREL.getCommands()) {
                    if (name.equals(c.getName())) {
                        cmd = c;
                        break;
                    }
                }
                assertNotNull("Command found", cmd);
                KarelModel.invoke(KAREL, cmd);
            }
        }
        DoTest run = new DoTest();
        CTX.execute(run);
        run.down.await();
        if (run.ex != null) {
            throw run.ex;
        }
        while (KAREL.isRunning()) {
            Thread.sleep(100);
        }
        
        for (TaskTestCase c : ct.getTests()) {
            assertEquals("Case " + c.getDescription() + " from " + ti.getUrl() + " is OK", c.getState(), "ok");
        }
    }
    
}
