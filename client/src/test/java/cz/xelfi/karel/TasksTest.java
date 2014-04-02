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

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import static org.testng.Assert.*;
import org.testng.SkipException;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class TasksTest {
    private static final CountDownLatch INIT = new CountDownLatch(1);
    private static Throwable T;
    private static BrwsrCtx CTX;
    private static Karel KAREL;
    
    @Factory public static Object[] initMirror() throws Throwable {
        final BrowserBuilder bb = BrowserBuilder.newBrowser().
            loadClass(TasksTest.class).
            loadPage("pages/index.html").
            invoke("ready");
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    bb.showAndWait();
                } catch (Throwable t) {
                    T = t;
                    INIT.countDown();
                }
            }
        });
        INIT.await();
        if (T != null) {
            SkipException se = new SkipException("Problems initializing the mirror!");
            se.initCause(T);
            throw se;
        }
        assertNotNull(CTX, "Not null");
        
        KAREL.changeTabTask();
        while (KAREL.getTasks().isEmpty()) {
            Thread.sleep(100);
        }

        final List<TaskInfo> tasks = KAREL.getTasks();
        final int size = tasks.size();
        Object[] arr = new Object[size];
        for (int i = 0; i < size; i++) {
            arr[i] = new TasksTest(tasks.get(i));
        }
        return arr;
    }
    
    public static void ready(String... args) throws Exception {
        CTX = BrwsrCtx.findDefault(TasksTest.class);
        KAREL = Main.onPageLoad(args);
        INIT.countDown();
    }
    private final TaskInfo ti;

    private TasksTest(TaskInfo info) {
        this.ti = info;
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
    
    @Test public void checkTest() throws Throwable {
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
        assertNotNull(ct, "Tasks loaded");

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
                assertEquals(KAREL.getCurrentTask(), ct, "Still same");
                KAREL.setSource(res);
                KarelModel.compile(KAREL);
                Command cmd = null;
                for (Command c : KAREL.getCommands()) {
                    if (name.equals(c.getName())) {
                        cmd = c;
                        break;
                    }
                }
                assertNotNull(cmd, "Command found");
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
            assertEquals(c.getState(), "ok", "Case " + c.getDescription() + " from " + ti.getUrl() + " is OK");
        }
    }
    
}
