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

import cz.xelfi.karel.blockly.Execution;
import net.java.html.junit.BrowserRunner;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@RunWith(BrowserRunner.class)
public class TownModelTest {

    public TownModelTest() {
    }

    @Test
    public void testStepLeftFourTimes() {
        Town t = new Town();
        t.clear();
        assertLocation(t, 0, 9, 1, "Initial position");
        
        t.step();
        t.left();
        assertLocation(t, 1, 9, 4, "One step heading north");
        
        t.step();
        t.left();
        assertLocation(t, 1, 8, 3, "Another and heading to west");
        
        t.step();
        t.left();
        assertLocation(t, 0, 8, 2, "Back on first column, heading south");
        
        t.step();
        t.left();
        assertLocation(t, 0, 9, 1, "Back");
    }
    
    @Test public void crashIntoTheWall() {
        Town t = new Town();
        t.clear();
        t.left();
        t.left();
        
        assertLocation(t, 0, 9, 3, "Rotated back");
        
        t.step();
        
        assertLocation(t, 0, 9, 3, "Same position remains");
        assertEquals("Hit the wall", 1, t.getError());
    }

    @Test public void crashIntoMiddleWall() {
        Town t = new Town();
        t.clear();
        t.wall(1, 9);
        
        t.step();
        
        assertLocation(t, 0, 9, 1, "Same position remains");
        assertEquals("Hit the wall", 1, t.getError());
        
        boolean isWall = TownModel.isCondition(t, Execution.Condition.WALL);
        assertTrue("Yes, heading towards the wall", isWall);
    }
    
    @Test public void compressTown() throws Exception {
        Town t = new Town();
        t.clear();
        String s = TownModel.toJSON(t);
        
        int first = s.indexOf("\"robot\"");
        assertNotEquals("Found one", first, -1);
        int second = s.indexOf("\"robot\"", first + 1);
        assertEquals("Only one robot in the town: " + s, -1, second);

        assertEquals("Don't dump info about errors: " + s, -1, s.indexOf("error"));
    }

    @Test public void compressTownWithAStep() throws Exception {
        Town t = new Town();
        t.clear();
        t.step();
        t.step();
        
        Town cl = t.clone();
        TownModel.simplify(cl);

        String s = cl.toString();
        int first = s.indexOf("\"robot\":1");
        assertNotEquals("Found one", first, -1);
        int second = s.indexOf("\"robot\"", first + 1);
        assertEquals("Only one robot in the town: " + s, -1, second);
        
        Town t2 = new Town();
        TownModel.load(t2, cl);
        
        assertEquals("Towns are the same", t2, t);
    }
 
    private static void assertLocation(Town t, int x, int y, int d, String msg) {
        int[] xyd = TownModel.findKarel(t);
        assertNotNull("Location of Karel found: " + t, xyd);
        assertEquals("X: " + msg, x, xyd[0]);
        assertEquals("Y: " + msg, y, xyd[1]);
        assertEquals("Direction: " + msg, d, xyd[2]);
    }
}
