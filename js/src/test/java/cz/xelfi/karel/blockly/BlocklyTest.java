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
package cz.xelfi.karel.blockly;

import cz.xelfi.karel.blockly.Execution.State;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BlocklyTest {
    private static BrwsrCtx CTX;

    public BlocklyTest() {
    }

    @BeforeClass
    public static void initializePresenter() throws InterruptedException {
        final CountDownLatch cdl = new CountDownLatch(1);
        final BrowserBuilder builder = BrowserBuilder.newBrowser().
            loadPage("test.html").
            loadFinished(new Runnable() {
                @Override
                public void run() {
                    CTX = BrwsrCtx.findDefault(BlocklyTest.class);
                    cdl.countDown();
                }
            });
        new Thread(new Runnable() {
            @Override
            public void run() {
                builder.showAndWait();
            }
        }, "Launcher").start();
        cdl.await();
        Assert.assertNotNull(CTX, "Context is ready");
    }

    @Test
    public void testWorkingWithWorkspace() throws Throwable {
        doTest("doWorkingWithWorkspace");
    }
    
    private void doWorkingWithWorkspace() throws Exception {
        Workspace w = Workspace.create("any");
        assertEquals(w.getProcedures().size(), 0, "No top blocks yet");
        Procedure p = w.newProcedure("vpravo-vbok");
        assertNotNull(p);
        final List<Procedure> p2 = w.getProcedures();
        assertEquals(p2.size(), 1, "One top block now");
        assertEquals(p2.get(0).getName(), "vpravo-vbok");
    }

    @Test
    public void testReachTheWall() throws Throwable {
        doTest("doReachTheWall");
    }

    private void doReachTheWall() throws Exception {
        Workspace w = Workspace.create("any");
        w.clear();

        w.loadXML(
"<xml xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
"  <block type=\"karel_funkce\" x=\"36\" y=\"115\">\n" +
"    <field name=\"NAME\">ke-zdi</field>\n" +
"    <statement name=\"IFTRUE\">\n" +
"      <block type=\"karel_while\">\n" +
"        <field name=\"NEG\">FALSE</field>\n" +
"        <field name=\"COND\">WALL</field>\n" +
"        <statement name=\"IFTRUE\">\n" +
"          <block type=\"karel_call\">\n" +
"            <field name=\"CALL\">krok</field>\n" +
"          </block>\n" +
"        </statement>\n" +
"      </block>\n" +
"    </statement>\n" +
"  </block>\n" +
"</xml>"
        );

        List<Procedure> arr = w.getProcedures();
        assertEquals(arr.size(), 1, "One proc: " + arr);

        class FewSteps implements Execution.Environment {
            int steps;

            public FewSteps(int steps) {
                this.steps = steps;
            }

            @Override
            public boolean isCondition(Execution.Condition c) {
                if (c == Execution.Condition.WALL) {
                    return steps == 0;
                }
                return false;
            }

            @Override
            public void left() {
            }

            @Override
            public boolean step() {
                if (steps == 0) {
                    return false;
                }
                steps--;
                return true;
            }

            @Override
            public boolean put() {
                return false;
            }

            @Override
            public boolean pick() {
                return false;
            }
        }

        FewSteps env = new FewSteps(2);

        Execution exec = arr.get(0).prepareExecution(env);
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_while");
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_call");
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_while");
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_call");
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_while");
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_funkce");
        assertEquals(exec.next(), State.FINISHED);
    }

    private void doTest(String method) throws Throwable {
        final Method m = this.getClass().getDeclaredMethod(method);
        m.setAccessible(true);
        final Exception[] arr = { null };
        final CountDownLatch cdl = new CountDownLatch(1);
        CTX.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    m.invoke(BlocklyTest.this);
                } catch (Exception ex) {
                    arr[0] = ex;
                } finally {
                    cdl.countDown();
                }
            }
        });
        cdl.await();
        if (arr[0] != null) {
            if (arr[0] instanceof InvocationTargetException) {
                InvocationTargetException ite = (InvocationTargetException) arr[0];
                throw ite.getTargetException();
            }
            throw arr[0];
        }
    }

}
