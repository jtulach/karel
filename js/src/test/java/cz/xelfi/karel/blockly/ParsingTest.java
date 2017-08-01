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

import java.util.List;
import net.java.html.BrwsrCtx;
import net.java.html.junit.BrowserRunner;
import net.java.html.junit.HTMLContent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BrowserRunner.class)
@HTMLContent("<div id = 'any'>Blockly</div>")
public class ParsingTest {
    private static BrwsrCtx CTX;

    @Test
    public void printOutCodeOfProcedure() throws Exception {
        final Workspace w = Workspace.create("any");
        w.clear();

        w.loadXML(
            "<xml xmlns=\"http://www.w3.org/1999/xhtml\">\n"
            + "  <block type=\"karel_funkce\" x=\"38\" y=\"45\">\n"
            + "    <field name=\"NAME\">safe-step</field>\n"
            + "    <statement name=\"IFTRUE\">\n"
            + "      <block type=\"karel_if\">\n"
            + "        <field name=\"NEG\">FALSE</field>\n"
            + "        <field name=\"COND\">WALL</field>\n"
            + "        <statement name=\"IFTRUE\">\n"
            + "          <block type=\"karel_call\">\n"
            + "            <field name=\"CALL\">STEP</field>\n"
            + "          </block>\n"
            + "        </statement>\n"
            + "      </block>\n"
            + "    </statement>\n"
            + "  </block>\n"
            + "  <block type=\"karel_funkce\" x=\"259\" y=\"44\">\n"
            + "    <field name=\"NAME\">ten-safe-steps</field>\n"
            + "    <statement name=\"IFTRUE\">\n"
            + "      <block type=\"karel_repeat\">\n"
            + "        <field name=\"N\">10</field>\n"
            + "        <statement name=\"IFTRUE\">\n"
            + "          <block type=\"karel_call\">\n"
            + "            <field name=\"CALL\">safe-step</field>\n"
            + "          </block>\n"
            + "        </statement>\n"
            + "      </block>\n"
            + "    </statement>\n"
            + "  </block>\n"
            + "</xml>"
        );

        List<Procedure> arr = BlocklyTest.filterProcedures(w);
        assertEquals("Two procs: " + arr, 2, arr.size());

        Procedure tenSafeSteps = w.findProcedure("ten-safe-steps");
        final Procedure safeStep = w.findProcedure("safe-step");

        assertNotNull(tenSafeSteps);
        assertNotNull(safeStep);

        String p = safeStep.getCode();
        assertTrue(p, p.contains("PROCEDURE safe-step"));
        assertTrue(p, p.contains("IF NOT WALL"));
        assertTrue(p, p.contains("STEP"));
    }

    @Test
    public void testIf() throws Exception {
        doType("IF MARK", 1);
    }

    @Test
    public void testIfIfIf() throws Exception {
        doType("IF NOT WALL", 3);
    }

    @Test
    public void testWhile22() throws Exception {
        doType("WHILE NORTH", 22);
    }

    @Test
    public void testRepeat() throws Exception {
        doType("REPEAT 10 TIMES", 1);
    }

    private void doType(String type, int cnt) throws Exception {
        final Workspace w = Workspace.create("any");
        w.clear();

        final StringBuilder sb = new StringBuilder();
        sb.append("PROCEDURE safe-step\n" + "  ").
            append(type).append("\n");
        while (cnt-- > 0) {
            sb.append(
            "    STEP\n"
            );
        }

        sb.append(
            "  END\n" +
            "END\n");

        final String CODE = sb.toString();

        final Procedure[] procs = w.parse(CODE);

        assertEquals("One procedure has been defined", 1, procs.length);
        assertEquals("right name", "safe-step", procs[0].getName());
        assertEquals("right id", "safe-step", procs[0].getId());

        assertEquals("Generated code of the procedure is the same, with\n" + w.toString(), CODE, procs[0].getCode());
    }

    @Test
    public void testIfIfIfElseElseElse() throws Exception {
        final Workspace w = Workspace.create("any");
        w.clear();

        final StringBuilder sb = new StringBuilder();
        sb.append(
            "PROCEDURE safe-step\n" +
            "  IF NOT WALL\n");
        for (int i = 0; i < 3; i++) {
            sb.append(
            "    STEP\n"
            );
        }
        sb.append("  ELSE\n");
        for (int i = 0; i < 3; i++) {
            sb.append(
            "    PUT\n"
            );
        }

        sb.append(
            "  END\n" +
            "END\n");

        final String CODE = sb.toString();

        final Procedure[] procs = w.parse(CODE);

        assertEquals("One procedure has been defined", 1, procs.length);
        assertEquals("right name", "safe-step", procs[0].getName());
        assertEquals("right id", "safe-step", procs[0].getId());

        assertEquals("Generated code of the procedure is the same, with\n" + w.toString(), CODE, procs[0].getCode());
    }

    @Test
    public void twoSteps() throws Exception {
        final Workspace w = Workspace.create("any");
        w.clear();

        final String CODE =
            "PROCEDURE two-steps\n" +
            "  REPEAT 2 TIMES\n" +
            "    STEP\n" +
            "  END\n" +
            "END\n";

        final Procedure[] procs = w.parse(CODE);

        assertEquals("One procedure has been defined", 1, procs.length);
        assertEquals("right name", "two-steps", procs[0].getName());

        Execution e = procs[0].prepareExecution(new BlocklyTest.FewSteps(10));
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_repeat", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_call", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_repeat", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_call", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_repeat", e.currentType());
        assertEquals(Execution.State.FINISHED, e.next());
        assertNull(e.currentType());
    }

    @Test
    public void towall() throws Exception {
        final Workspace w = Workspace.create("any");
        w.clear();

        final String CODE =
            "PROCEDURE towall\n" +
            "  IF NOT WALL\n" +
            "    STEP\n" +
            "  END\n" +
            "  towall\n" +
            "END\n";

        final Procedure[] procs = w.parse(CODE);
        assertEquals("One procedure has been defined", 1, procs.length);
        assertEquals("right name", "towall", procs[0].getName());

        Execution e = procs[0].prepareExecution(new BlocklyTest.FewSteps(10));
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_if", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_call", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_call", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_funkce", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_if", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_call", e.currentType());
    }

    @Test
    public void around() throws Exception {
        final Workspace w = Workspace.create("any");
        w.clear();

        final String CODE =
            "PROCEDURE okolo\n" +
            "  IF WALL\n" +
            "    LEFT\n" +
            "  ELSE\n" +
            "    STEP\n" +
            "  END\n" +
            "  okolo\n" +
            "END\n";

        final Procedure[] procs = w.parse(CODE);
        
        assertEquals("One procedure has been defined", 1, procs.length);
        assertEquals("right name", "okolo", procs[0].getName());

        Execution e = procs[0].prepareExecution(new BlocklyTest.FewSteps(10));
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_if_else", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_call", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_call", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_funkce", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_if_else", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_call", e.currentType());
    }
    @Test
    public void whileMark() throws Exception {
        final Workspace w = Workspace.create("any");
        w.clear();

        final String CODE =
            "PROCEDURE znacky\n" +
            "  WHILE MARK\n" +
            "    STEP\n" +
            "  END\n" +
            "END\n";

        final Procedure[] procs = w.parse(CODE);

        assertEquals("One procedure has been defined", 1, procs.length);
        assertEquals("right name", "znacky", procs[0].getName());

        Execution e = procs[0].prepareExecution(new BlocklyTest.FewMarks(3));
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_while", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_call", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_while", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_call", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_while", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_call", e.currentType());
        assertEquals(Execution.State.RUNNING, e.next());
        assertEquals("karel_while", e.currentType());
        assertEquals(Execution.State.FINISHED, e.next());
        assertNull(e.currentType());
    }
}
