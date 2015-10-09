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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
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
        assertEquals(filterProcedures(w).size(), 0, "No top blocks yet");
        Procedure p = w.newProcedure("vpravo-vbok");
        assertNotNull(p);
        final List<Procedure> p2 = filterProcedures(w);
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

        assertTrue(w.isEmpty(), "Empty now");

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
"            <field name=\"CALL\">STEP</field>\n" +
"          </block>\n" +
"        </statement>\n" +
"      </block>\n" +
"    </statement>\n" +
"  </block>\n" +
"</xml>"
        );

        List<Procedure> arr = filterProcedures(w);
        assertEquals(arr.size(), 1, "One proc: " + arr);
        assertFalse(w.isEmpty(), "Not empty now");
        arr.get(0).setCollapsed(true);

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
        assertEquals(exec.next(), State.FINISHED);
    }


    @Test
    public void testTurnRight() throws Throwable {
        doTest("doTurnRight");
    }

    private void doTurnRight() throws Exception {
        Workspace w = Workspace.create("any");
        w.clear();

        w.loadXML(
"<xml xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
"  <block type=\"karel_funkce\" x=\"36\" y=\"115\">\n" +
"    <field name=\"NAME\">vpravo-vbok</field>\n" +
"    <statement name=\"IFTRUE\">\n" +
"      <block type=\"karel_repeat\">\n" +
"        <field name=\"N\">3</field>\n" +
"        <statement name=\"IFTRUE\">\n" +
"          <block type=\"karel_call\">\n" +
"            <field name=\"CALL\">LEFT</field>\n" +
"          </block>\n" +
"        </statement>\n" +
"      </block>\n" +
"    </statement>\n" +
"  </block>\n" +
"</xml>"
        );

        List<Procedure> arr = filterProcedures(w);
        assertEquals(arr.size(), 1, "One proc: " + arr);

        class FewTurns implements Execution.Environment {
            int cnt;

            public FewTurns() {
            }

            @Override
            public boolean isCondition(Execution.Condition c) {
                return false;
            }

            @Override
            public void left() {
                cnt++;
            }

            @Override
            public boolean step() {
                return true;
            }

            @Override
            public boolean put() {
                return false;
            }

            @Override
            public boolean take() {
                return false;
            }
        }

        FewTurns env = new FewTurns();

        Execution exec = arr.get(0).prepareExecution(env);
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_repeat");
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_call");
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_repeat");
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_call");
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_repeat");
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_call");
        assertEquals(exec.next(), State.RUNNING, "OK, running");
        assertEquals(exec.currentType(), "karel_repeat");
        assertEquals(exec.next(), State.FINISHED);

        assertEquals(env.cnt, 3, "Three turns left");
    }


    @Test
    public void testIfWhile() throws Throwable {
        doTest("doIfWhile");
    }

    private void doIfWhile() throws Exception {
        Workspace w = Workspace.create("any");
        w.clear();

        w.loadXML(
"<xml xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
"  <block type=\"karel_funkce\" x=\"133\" y=\"147\">\n" +
"    <field name=\"NAME\">step-north</field>\n" +
"    <statement name=\"IFTRUE\">\n" +
"      <block type=\"karel_if_else\">\n" +
"        <field name=\"NEG\">TRUE</field>\n" +
"        <field name=\"COND\">NORTH</field>\n" +
"        <statement name=\"IFTRUE\">\n" +
"          <block type=\"karel_call\">\n" +
"            <field name=\"CALL\">STEP</field>\n" +
"          </block>\n" +
"        </statement>\n" +
"        <statement name=\"IFFALSE\">\n" +
"          <block type=\"karel_call\">\n" +
"            <field name=\"CALL\">LEFT</field>\n" +
"          </block>\n" +
"        </statement>\n" +
"      </block>\n" +
"    </statement>\n" +
"  </block>\n" +
"</xml>"
        );

        List<Procedure> arr = filterProcedures(w);
        assertEquals(arr.size(), 1, "One proc: " + arr);

        class StepNorth implements Execution.Environment {
            int direction;

            public StepNorth(int initDir) {
                direction = initDir;
            }

            @Override
            public boolean isCondition(Execution.Condition c) {
                if (c == Execution.Condition.NORTH) {
                    return direction == 0;
                }
                return false;
            }

            @Override
            public void left() {
                if (--direction < 0) {
                    direction = 4;
                }
            }

            @Override
            public boolean step() {
                return false;
            }

            @Override
            public boolean put() {
                return false;
            }

            @Override
            public boolean take() {
                return false;
            }
        }

        StepNorth env = new StepNorth(1);

        {
            Execution exec = arr.get(0).prepareExecution(env);
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_if_else");
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_call");
            assertEquals(exec.next(), State.FINISHED);

            assertEquals(env.direction, 0, "Heading north");
        }

        {
            Execution exec = arr.get(0).prepareExecution(env);
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_if_else");
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_call");
            assertEquals(exec.next(), State.ERROR_WALL);

            assertEquals(env.direction, 0, "Still Heading north");
        }
    }

    @Test
    public void testIfOnly() throws Throwable {
        doTest("doIfOnly");
    }

    private void doIfOnly() throws Exception {
        Workspace w = Workspace.create("any");
        w.clear();

        w.loadXML(
"<xml xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
"  <block type=\"karel_funkce\" x=\"133\" y=\"147\">\n" +
"    <field name=\"NAME\">turn-north</field>\n" +
"    <statement name=\"IFTRUE\">\n" +
"      <block type=\"karel_if\">\n" +
"        <field name=\"NEG\">FALSE</field>\n" +
"        <field name=\"COND\">NORTH</field>\n" +
"        <statement name=\"IFTRUE\">\n" +
"          <block type=\"karel_call\">\n" +
"            <field name=\"CALL\">LEFT</field>\n" +
"          </block>\n" +
"        </statement>\n" +
"      </block>\n" +
"    </statement>\n" +
"  </block>\n" +
"</xml>"
        );

        List<Procedure> arr = filterProcedures(w);
        assertEquals(arr.size(), 1, "One proc: " + arr);

        class StepNorth implements Execution.Environment {
            int direction;

            public StepNorth(int initDir) {
                direction = initDir;
            }

            @Override
            public boolean isCondition(Execution.Condition c) {
                if (c == Execution.Condition.NORTH) {
                    return direction == 0;
                }
                return false;
            }

            @Override
            public void left() {
                if (--direction < 0) {
                    direction = 4;
                }
            }

            @Override
            public boolean step() {
                return false;
            }

            @Override
            public boolean put() {
                return false;
            }

            @Override
            public boolean take() {
                return false;
            }
        }

        StepNorth env = new StepNorth(2);

        {
            Execution exec = arr.get(0).prepareExecution(env);
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_if");
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_call");
            assertEquals(exec.next(), State.FINISHED);

            assertNotEquals(env.direction, 0, "Not Heading north");
        }

        {
            Execution exec = arr.get(0).prepareExecution(env);
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_if");
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_call");
            assertEquals(exec.next(), State.FINISHED);

            assertEquals(env.direction, 0, "Now Heading north");
        }

        {
            Execution exec = arr.get(0).prepareExecution(env);
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_if");
            assertEquals(exec.next(), State.FINISHED);

            assertEquals(env.direction, 0, "Still Heading north");
        }
    }

    @Test
    public void testPrimitiveCall() throws Throwable {
        doTest("doPrimitiveCall");
    }

    private void doPrimitiveCall() throws Exception {
        Workspace w = Workspace.create("any");
        w.clear();

        FewSteps env = new FewSteps(1);

        Procedure step = w.findProcedure("STEP");
        assertNotNull(step, "Step is always present");

        Execution exec = step.prepareExecution(env);
        assertEquals(exec.next(), State.FINISHED, "one step is OK");

        Execution exec2 = step.prepareExecution(env);
        assertEquals(exec2.next(), State.ERROR_WALL, "second isn't");
    }

    @Test
    public void testProcedureCall() throws Throwable {
        doTest("doProcedureCall");
    }

    private void doProcedureCall() throws Exception {
        Workspace w = Workspace.create("any");
        w.clear();

        w.loadXML(
"<xml xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
"  <block type=\"karel_funkce\" x=\"38\" y=\"45\">\n" +
"    <field name=\"NAME\">safe-step</field>\n" +
"    <statement name=\"IFTRUE\">\n" +
"      <block type=\"karel_if\">\n" +
"        <field name=\"NEG\">FALSE</field>\n" +
"        <field name=\"COND\">WALL</field>\n" +
"        <statement name=\"IFTRUE\">\n" +
"          <block type=\"karel_call\">\n" +
"            <field name=\"CALL\">STEP</field>\n" +
"          </block>\n" +
"        </statement>\n" +
"      </block>\n" +
"    </statement>\n" +
"  </block>\n" +
"  <block type=\"karel_funkce\" x=\"259\" y=\"44\">\n" +
"    <field name=\"NAME\">ten-safe-steps</field>\n" +
"    <statement name=\"IFTRUE\">\n" +
"      <block type=\"karel_repeat\">\n" +
"        <field name=\"N\">10</field>\n" +
"        <statement name=\"IFTRUE\">\n" +
"          <block type=\"karel_call\">\n" +
"            <field name=\"CALL\">safe-step</field>\n" +
"          </block>\n" +
"        </statement>\n" +
"      </block>\n" +
"    </statement>\n" +
"  </block>\n" +
"</xml>"
        );

        List<Procedure> arr = filterProcedures(w);
        assertEquals(arr.size(), 2, "Two procs: " + arr);

        FewSteps env = new FewSteps(10);

        Procedure tenSafeSteps = w.findProcedure("ten-safe-steps");
        Procedure safeStep = w.findProcedure("safe-step");

        assertNull(w.getSelectedProcedure(), "no selected procedure yet");
        {
            Execution exec = tenSafeSteps.prepareExecution(env);
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_repeat");
            assertEquals(w.getSelectedProcedure(), tenSafeSteps, "ten steps is selected");
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_call");
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_funkce");
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_if");
            assertEquals(w.getSelectedProcedure(), safeStep, "one safe step is selected");
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_call");
            assertEquals(exec.next(), State.RUNNING, "OK, running");
            assertEquals(exec.currentType(), "karel_repeat");

            State state;
            for (;;) {
                state = exec.next();
                if (state != State.RUNNING) {
                    break;
                }
            }
            assertEquals(exec.next(), State.FINISHED, "At the end reached successful state");
        }
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

    private List<Procedure> filterProcedures(Workspace w) {
        List<Procedure> arr = w.getProcedures();
        assertEquals(arr.get(0).getId(), "STEP");
        assertEquals(arr.get(1).getId(), "LEFT");
        assertEquals(arr.get(2).getId(), "PUT");
        assertEquals(arr.get(3).getId(), "TAKE");
        return arr.subList(4, arr.size());
    }
    
    static class FewSteps implements Execution.Environment {

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
        public boolean take() {
            return false;
        }
    }

}
