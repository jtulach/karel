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

import java.util.Locale;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProgramTest {
    
    public ProgramTest() {
    }
    
    @BeforeClass public static void czechLocale() {
        Locale.setDefault(new Locale("cs", "CZ"));
    }

    @Test
    public void testTurnBack() throws SyntaxException {
        Town t = new Town();
        t.clear();
        
        KarelCompiler.AST root = KarelCompiler.toAST(
            "čelem-vzad\n"
                + "  vlevo-vbok\n"
                + "  vlevo-vbok\n"
                + "konec\n"
                + "");
        
        KarelCompiler inst = KarelCompiler.execute(t, (KarelCompiler.Root)root, "čelem-vzad");
        assertNotNull(inst, "Instruction created");

        inst = inst.next();
        assertLocation(t, 0, 9, 4, "One step heading north");
        
        inst = inst.next();
        assertLocation(t, 0, 9, 3, "Another and heading to west");

        assertNotNull(inst, "Waiting for another instruction");
        inst = inst.next();
        
        assertNull(inst, "No next instruction");
    }

    @Test public void advance() throws Exception {
        KarelCompiler.AST root = KarelCompiler.toAST(
              "čelem-vzad\n"
            + "  vlevo-vbok\n"
            + "  vlevo-vbok\n"
            + "konec\n"
            + "\n"
            + "postup\n"
            + "  kdyz není zed\n"
            + "    krok\n"
            + "  jinak\n"
            + "    čelem-vzad\n"
            + "  konec\n"
            + "konec\n"
        );
        
        Town t = new Town();
        t.clear();
        t.getRows().get(9).getColumns().get(0).setRobot(0);
        t.getRows().get(9).getColumns().get(2).setRobot(3);
        assertLocation(t, 2, 9, 3, "Heading to home");
        
        KarelCompiler inst = KarelCompiler.execute(t, (KarelCompiler.Root)root, "postup");
        assertNotNull(inst, "Instruction created");

        assertLocation(t, 2, 9, 3, "Still heading to home");
        {
            KarelCompiler one = inst.next();
            assertLocation(t, 1, 9, 3, "Step was made");
            assertNotNull(one);
            assertNull(one.next());
        }
        
        inst = KarelCompiler.execute(t, (KarelCompiler.Root)root, "postup");
        {
            KarelCompiler one = inst.next();
            assertLocation(t, 0, 9, 3, "Step was made");
            assertNotNull(one);
            assertNull(one.next());
        }

        inst = KarelCompiler.execute(t, (KarelCompiler.Root)root, "postup");
        {
            KarelCompiler one = inst.next();
            assertLocation(t, 0, 9, 2, "Rotated once");
            assertNotNull(one);
            KarelCompiler two = one.next();
            assertNotNull(two);
            assertLocation(t, 0, 9, 1, "Rotated 2nd");
            assertNull(two.next(), "No further movements");
        }
    }

    @Test public void pickupAllSigns() throws Exception {
        KarelCompiler.AST root = KarelCompiler.toAST(
              "seber\n"
            + "  dokud je značka\n"
            + "    zvedni\n"
            + "  konec\n"
            + "konec\n"
        );
        
        Town t = new Town();
        t.clear();
        
        int[] xyd = TownModel.findKarel(t);
        final Square square = t.getRows().get(xyd[1]).getColumns().get(xyd[0]);
        square.setSign(3);
        
        KarelCompiler inst = KarelCompiler.execute(t, (KarelCompiler.Root)root, "seber");
        
        {
            KarelCompiler one = inst.next();
            assertNotNull(one);
            assertEquals(square.getSign(), 2, "One sign less");
            KarelCompiler two = one.next();
            assertNotNull(two);
            assertEquals(square.getSign(), 1, "One sign remaining");
            KarelCompiler three = two.next();
            assertNotNull(three);
            assertEquals(square.getSign(), 0, "Empty");
            assertNull(three.next(), "Execution is over");
        }
    }

    @Test public void approachTheWall() throws Exception {
        KarelCompiler.AST root = KarelCompiler.toAST(
              "jdi\n"
            + "  dokud neni zed\n"
            + "    krok\n"
            + "  konec\n"
            + "konec\n"
        );
        
        Town t = new Town();
        t.clear();
        
        int[] xyd = TownModel.findKarel(t);
        final Square square = t.getRows().get(xyd[1]).getColumns().get(xyd[0]);
        square.setSign(3);
        
        KarelCompiler inst = KarelCompiler.execute(t, (KarelCompiler.Root)root, "jdi");
        
        for (;;) {
            KarelCompiler next = inst.next();
            if (next == null) {
                break;
            }
            inst = next;
        }
        
        assertLocation(t, 9, 9, 1, "Right bottom corner");
    }
    
    @Test public void threeSteps() throws Exception {
        KarelCompiler.AST root = KarelCompiler.toAST(
              "tri-kroky\n"
            + "  opakuj 3\n"
            + "    krok\n"
            + "  konec\n"
            + "konec\n"
        );
        
        Town t = new Town();
        t.clear();
        
        KarelCompiler inst = KarelCompiler.execute(t, (KarelCompiler.Root)root, "tri-kroky");
        
        {
            KarelCompiler one = inst.next();
            assertNotNull(one);
            KarelCompiler two = one.next();
            assertNotNull(two);
            KarelCompiler three = two.next();
            assertNotNull(three);
            assertNull(three.next(), "Execution is over");
        }
        
        assertLocation(t, 3, 9, 1, "Three steps");
    }
    
    private static void assertLocation(Town t, int x, int y, int d, String msg) {
        int[] xyd = TownModel.findKarel(t);
        assertNotNull(xyd, "Location of Karel found: " + t);
        assertEquals(xyd[0], x, "X: " + msg);
        assertEquals(xyd[1], y, "Y: " + msg);
        assertEquals(xyd[2], d, "Direction: " + msg);
        
    }
}
