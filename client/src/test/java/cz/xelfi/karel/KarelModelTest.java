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
 * @author Jaroslav Tulach
 */
public class KarelModelTest {
    
    public KarelModelTest() {
    }
    
    @BeforeClass public static void switchToCzech() {
        Locale.setDefault(new Locale("cs", "CZ"));
    }

    @Test public void animateStopsOnError() throws Exception {
        String prg = "naraz\n"
                + "  vlevo-vbok\n"
                + "  vlevo-vbok\n"
                + "  krok\n"
                + "  vlevo-vbok\n"
                + "  vlevo-vbok\n"
                + "  krok\n"
                + "  krok\n"
                + "konec";
        Town init = new Town();
        init.clear();
        
        Town end = init.clone();
        end.left();
        end.left();
        end.setError(1);
        
        TaskDescription td = new TaskDescription("xyz", "xyz", null, 
            new TaskTestCase("xyz", init, null, end, prg, "current")
        );
        Karel km = new Karel("town", null, td, null, null, prg, 10, false);
        km.compile();
        
        Command cmd = null;
        for (Command c : km.getCommands()) {
            if ("naraz".equals(c.getName())) {
                cmd = c;
                break;
            }
        }
        assertNotNull(cmd, "naraz found among " + km.getCommands());
        
        KarelModel.invoke(km, cmd);
        
        while (km.isRunning()) {
            Thread.sleep(100);
        }
        
        final Town cur = km.getCurrentTask().getTests().get(0).getCurrent();
        int[] karel = TownModel.findKarel(cur);
        assertEquals(karel[0], 0, "left corner");
        assertEquals(karel[1], 9, "bottom corner");
        assertEquals(karel[2], 3, "looking west");
        assertEquals(cur.getError(), 1, "hit the wall");
    }

    
}
