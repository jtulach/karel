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

import java.util.Locale;
import static junit.framework.Assert.assertNotNull;
import net.java.html.junit.BrowserRunner;
import net.java.html.junit.HTMLContent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Jaroslav Tulach
 */
@RunWith(BrowserRunner.class)
@HTMLContent(
    "<div id='workspace' style='height: 80vh; background-color: red'></div>"
)
public class KarelModelTest {
    
    public KarelModelTest() {
    }
    
    @BeforeClass
    public static void switchToCzech() {
        Locale.setDefault(new Locale("cs", "CZ"));
    }

    @Test
    public void animateStopsOnError() throws Exception {
        String prg = "PROCEDURE naraz\n"
                + "  LEFT\n"
                + "  LEFT\n"
                + "  STEP\n"
                + "  LEFT\n"
                + "  LEFT\n"
                + "  STEP\n"
                + "  STEP\n"
                + "END";
        Town init = new Town();
        init.clear();
        
        Town end = init.clone();
        end.left();
        end.left();
        end.setError(1);
        
        TaskDescription td = new TaskDescription("xyz", "xyz", null, 0,
            new TaskTestCase("xyz", init, null, end, prg, "current")
        );
        Karel km = new Karel().
                assignTab("town").
                assignCurrentTask(td).
                assignSource(prg).
                assignSpeed(10).
                assignTasksUrl("tasks/list.js");
        KarelModel.compileSource(km);
        
        Command cmd = null;
        for (Command c : km.getCommands()) {
            if ("naraz".equals(c.getName())) {
                cmd = c;
                break;
            }
        }
        assertNotNull("naraz found among " + km.getCommands(), cmd);

        km.setSpeed(-1);
        assertEquals("Runs all code at once", -1, km.getSpeed());
        KarelModel.invoke(km, cmd);
        
        final Town cur = km.getCurrentTask().getTests().get(0).getCurrent();
        int[] karel = TownModel.findKarel(cur);
        assertEquals("left corner", 0, karel[0]);
        assertEquals("bottom corner", 9, karel[1]);
        assertEquals("looking west", 3, karel[2]);
        assertEquals("hit the wall", 1, cur.getError());
    }

    @Test
    public void whileAndWhile() throws Exception {
        String prg = "PROCEDURE right\n"
                + "  LEFT\n"
                + "  LEFT\n"
                + "  LEFT\n"
                + "END\n"
                + "PROCEDURE rightstep\n"
                + "  right\n"
                + "  STEP\n"
                + "  LEFT\n"
                + "END\n"
                + "PROCEDURE around\n" +
                "  PUT\n" +
                "  rightstep\n" +
                "  WHILE NOT MARK\n" +
                "    WHILE WALL\n" +
                "      right\n" +
                "    END\n" +
                "    STEP\n" +
                "    LEFT\n" +
                "  END\n" +
                "END\n"
                + "";
        Town init = new Town();
        init.clear();
        init.wall(4, 5);
        init.wall(5, 5);
      //  init.wall(6, 5);
        init.wall(7, 5);
        init.wall(4, 4);
        init.wall(5, 4);
        init.wall(6, 4);
        init.wall(7, 4);
        init.wall(4, 3);
        init.wall(5, 3);
        init.wall(6, 3);
        init.wall(7, 3);
        init.step();
        init.step();
        init.step();
        init.step();
        init.step();
        init.left();
        init.step();
        init.step();
        init.step();
        
        assertEquals("no error", 0, init.getError());
        
        Town end = init.clone();
        end.put();
        
        TaskDescription td = new TaskDescription("xyz", "xyz", null, 0,
            new TaskTestCase("xyz", init, null, end, prg, "current")
        );
        Karel km = new Karel().
                assignTab("town").
                assignCurrentTask(td).
                assignSource(prg).
                assignSpeed(10).
                assignTasksUrl("tasks/list.js");
        KarelModel.compileSource(km);
        
        Command cmd = null;
        for (Command c : km.getCommands()) {
            if ("around".equals(c.getName())) {
                cmd = c;
                break;
            }
        }
        assertNotNull("around found among " + km.getCommands(), cmd);

        km.setSpeed(-1);
        assertEquals("Runs all code at once", -1, km.getSpeed());
        KarelModel.invoke(km, cmd);
        
        final Town cur = km.getCurrentTask().getTests().get(0).getCurrent();
        
        if (cur.equals(end)) {
            return;
        }
        dump(init);
        System.err.println("----");
        dump(cur);
        
        fail("Unexpected result");
    }

    private void dump(Town t) {
        for (Row r : t.getRows()) {
            for (Square s : r.getColumns()) {
                if (s.getRobot() != 0) {
                    System.err.print("" + (char)('R' + s.getMarks()));
                } else {
                    if (s.getMarks() == 111) {
                        System.err.print("W");
                    } else {
                        System.err.print(s.getMarks());
                    }
                }
            }
            System.err.println();
        }
    }
}
