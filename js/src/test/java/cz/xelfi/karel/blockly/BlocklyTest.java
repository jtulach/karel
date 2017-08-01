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
import java.util.List;
import net.java.html.junit.BrowserRunner;
import net.java.html.junit.HTMLContent;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BrowserRunner.class)
@HTMLContent("<div id = 'any'>Blockly</div>")
public class BlocklyTest {
    public BlocklyTest() {
    }

    @Test
    public void testWorkingWithWorkspace() throws Throwable {
        Workspace w = Workspace.create("any");
        assertEquals("No top blocks yet", 0, filterProcedures(w).size());
        Procedure p = w.newProcedure("vpravo-vbok");
        assertNotNull(p);
        final List<Procedure> p2 = filterProcedures(w);
        assertEquals("One top block now", 1, p2.size());
        assertEquals("vpravo-vbok", p2.get(0).getName());
    }

    @Test
    public void testReachTheWall() throws Throwable {
        Workspace w = Workspace.create("any");
        w.clear();

        assertTrue("Empty now", w.isEmpty());

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
        assertEquals("One proc: " + arr, 1, arr.size());
        Assert.assertFalse("Not empty now", w.isEmpty());
        arr.get(0).setCollapsed(true);

        FewSteps env = new FewSteps(2);

        Execution exec = arr.get(0).prepareExecution(env);
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_while", exec.currentType());
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_call", exec.currentType());
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_while", exec.currentType());
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_call", exec.currentType());
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_while", exec.currentType());
        assertEquals(State.FINISHED, exec.next());

        final String code = arr.get(0).getCode();
        assertTrue(code, code.contains("WHILE NOT WALL"));
    }


    @Test
    public void testTurnRight() throws Throwable {
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
        assertEquals("One proc: " + arr, 1, arr.size());

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
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_repeat", exec.currentType());
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_call", exec.currentType());
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_repeat", exec.currentType());
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_call", exec.currentType());
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_repeat", exec.currentType());
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_call", exec.currentType());
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_repeat", exec.currentType());
        assertEquals(State.FINISHED, exec.next());

        assertEquals("Three turns left", 3, env.cnt);

        String code = arr.get(0).getCode();
        assertTrue(code, code.contains("REPEAT 3 TIMES"));
    }

    @Test
    public void testInfiniteRepeat() throws Throwable {
        Workspace w = Workspace.create("any");
        w.clear();

        w.loadXML(
"<xml xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
"  <block type=\"karel_funkce\" x=\"76\" y=\"59\">\n" +
"    <field name=\"NAME\">zmatek</field>\n" +
"    <statement name=\"IFTRUE\">\n" +
"      <block type=\"karel_while\">\n" +
"        <field name=\"NEG\">FALSE</field>\n" +
"        <field name=\"COND\">NORTH</field>\n" +
"        <statement name=\"IFTRUE\">\n" +
"          <block type=\"karel_if\">\n" +
"            <field name=\"NEG\">FALSE</field>\n" +
"            <field name=\"COND\">WALL</field>\n" +
"            <statement name=\"IFTRUE\">\n" +
"              <block type=\"karel_call\">\n" +
"                <field name=\"CALL\">LEFT</field>\n" +
"              </block>\n" +
"            </statement>\n" +
"          </block>\n" +
"        </statement>\n" +
"      </block>\n" +
"    </statement>\n" +
"  </block>\n" +
"</xml>"
        );

        List<Procedure> arr = filterProcedures(w);
        assertEquals("One proc: " + arr, 1, arr.size());

        class NorthNoWall implements Execution.Environment {
            int cnt;

            public NorthNoWall() {
            }

            @Override
            public boolean isCondition(Execution.Condition c) {
                if (c == Execution.Condition.NORTH) {
                    return cnt % 4 == 3;
                }
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

        NorthNoWall env = new NorthNoWall();

        Execution exec = arr.get(0).prepareExecution(env);
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_while", exec.currentType());
        for (int i = 0; i < 3; i++) {
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_if", exec.currentType());
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_call", exec.currentType());
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_while", exec.currentType());
        }
        assertEquals(State.FINISHED, exec.next());
    }


    @Test
    public void testIfWhile() throws Throwable {
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
        assertEquals("One proc: " + arr, 1, arr.size());

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
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_if_else", exec.currentType());
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_call", exec.currentType());
            assertEquals(State.FINISHED, exec.next());

            assertEquals("Heading north", 0, env.direction);
        }

        {
            Execution exec = arr.get(0).prepareExecution(env);
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_if_else", exec.currentType());
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_call", exec.currentType());
            assertEquals(State.ERROR_WALL, exec.next());

            assertEquals("Still Heading north", 0, env.direction);
        }

        String code = arr.get(0).getCode();
        assertTrue(code, code.contains("ELSE"));
    }

    @Test
    public void testIfOnly() throws Throwable {
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
        assertEquals("One proc: " + arr, 1, arr.size());

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
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_if", exec.currentType());
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_call", exec.currentType());
            assertEquals(State.FINISHED, exec.next());

            assertNotEquals("Not Heading north", 0, env.direction);
        }

        {
            Execution exec = arr.get(0).prepareExecution(env);
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_if", exec.currentType());
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_call", exec.currentType());
            assertEquals(State.FINISHED, exec.next());

            assertEquals("Now Heading north", 0, env.direction);
        }

        {
            Execution exec = arr.get(0).prepareExecution(env);
            assertEquals("OK, running", State.RUNNING, exec.next());
            assertEquals("karel_if", exec.currentType());
            assertEquals(State.FINISHED, exec.next());

            assertEquals("Still Heading north", 0, env.direction);
        }

        String code = arr.get(0).getCode();
        assertTrue(code, code.contains("IF NOT NORTH"));
    }

    @Test
    public void testPrimitiveCall() throws Throwable {
        Workspace w = Workspace.create("any");
        w.clear();

        FewSteps env = new FewSteps(1);

        Procedure step = w.findProcedure("STEP");
        assertNotNull("Step is always present", step);

        Execution exec = step.prepareExecution(env);
        assertEquals("one step is OK", State.FINISHED, exec.next());

        Execution exec2 = step.prepareExecution(env);
        assertEquals("second isn't", State.ERROR_WALL, exec2.next());
    }

    @Test
    public Runnable[] testProcedureCall() throws Throwable {
        final Workspace w = Workspace.create("any");
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
        assertEquals("Two procs: " + arr, 2, arr.size());

        FewSteps env = new FewSteps(10);

        Procedure tenSafeSteps = w.findProcedure("ten-safe-steps");
        final Procedure safeStep = w.findProcedure("safe-step");

        class SelectionChanged implements Runnable {
            boolean changed;

            @Override
            public void run() {
                changed = true;
            }

            void assertChange() {
                assertTrue("Message delivered", changed);
                changed = false;
            }
        }
        final SelectionChanged selectionChanged = new SelectionChanged();
        w.addSelectionChange(selectionChanged);
        Assert.assertNull("no selected procedure yet", w.getSelectedProcedure());

        final Execution exec = tenSafeSteps.prepareExecution(env);
        assertEquals("OK, running", State.RUNNING, exec.next());
        assertEquals("karel_repeat", exec.currentType());
        assertEquals("ten steps is selected", tenSafeSteps, w.getSelectedProcedure());

        return new Runnable[] {
            () -> {},
            () -> {},
            () -> {},
            () -> {},
            () -> {},
            () -> {
                selectionChanged.assertChange();
                assertEquals("OK, running", State.RUNNING, exec.next());
                assertEquals("karel_call", exec.currentType());
                assertEquals("OK, running", State.RUNNING, exec.next());
                assertEquals("karel_funkce", exec.currentType());
                assertEquals("OK, running", State.RUNNING, exec.next());
                assertEquals("karel_if", exec.currentType());
                assertEquals("one safe step is selected", safeStep, w.getSelectedProcedure());
            },
            () -> {
                selectionChanged.assertChange();
                assertEquals("OK, running", State.RUNNING, exec.next());
                assertEquals("karel_call", exec.currentType());
                assertEquals("OK, running", State.RUNNING, exec.next());
                assertEquals("karel_repeat", exec.currentType());

                State state;
                for (;;) {
                    state = exec.next();
                    if (state != State.RUNNING) {
                        break;
                    }
                }
                assertEquals("At the end reached successful state", State.FINISHED, exec.next());
            }
        };
    }


    static List<Procedure> filterProcedures(Workspace w) {
        List<Procedure> arr = w.getProcedures();
        assertEquals("STEP", arr.get(0).getId());
        assertEquals("LEFT", arr.get(1).getId());
        assertEquals("PUT", arr.get(2).getId());
        assertEquals("TAKE", arr.get(3).getId());
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

    static class FewMarks implements Execution.Environment {

        int steps;

        public FewMarks(int steps) {
            this.steps = steps;
        }

        @Override
        public boolean isCondition(Execution.Condition c) {
            if (c == Execution.Condition.MARK) {
                return steps > 0;
            }
            return false;
        }

        @Override
        public void left() {
        }

        @Override
        public boolean step() {
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
