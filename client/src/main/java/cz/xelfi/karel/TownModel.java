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

import java.util.ArrayList;
import java.util.List;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className = "Town", properties = {
    @Property(name = "error", type = int.class),
    @Property(name = "rows", type = Row.class, array = true)
})
class TownModel {
    @Model(className = "Row", properties = {
        @Property(name = "columns", type = Square.class, array = true)
    })
    static class RowModel {
    }
    
    static enum Orientation {
        NORTH, EAST, SOUTH, WEST;
    }

    static boolean isCondition(Town town, KarelToken cond) {
        int[] xyd = findKarel(town);
        if (KarelToken.EAST == cond) {
            return xyd[2] == 1;
        }
        if (KarelToken.SOUTH == cond) {
            return xyd[2] == 2;
        }
        if (KarelToken.WEST == cond) {
            return xyd[2] == 3;
        }
        if (KarelToken.NORTH == cond) {
            return xyd[2] == 4;
        }
        if (KarelToken.SIGN == cond) {
            return town.getRows().get(xyd[1]).getColumns().get(xyd[0]).getSign() > 0;
        }
        if (KarelToken.WALL != cond) {
            throw new IllegalStateException("" + cond);
        }
        int[] next = stepInDirection(xyd);
        try {
            town.getRows().get(next[1]).getColumns().get(next[0]);
            return false;
        } catch (IndexOutOfBoundsException ex) {
            return true;
        }
    }
    
    
    @Model(className = "Square", properties = {
        @Property(name = "robot", type = int.class),
        @Property(name = "sign", type = int.class),
    })
    static class SquareModel {
        @ComputedProperty static String html(int robot, int sign) {
            if (robot != 0) switch (robot) {
                case 1: return "&rarr;";
                case 2: return "&darr;";
                case 3: return "&larr;";
                case 4: return "&uarr;";
            }
            return "&nbsp;";
        }
    }
    
    @ModelOperation static void clear(Town m) {
        List<Row> rows = new ArrayList<Row>();
        for (int y = 0; y < 10; y++) {
            Square[] arr = new Square[10];
            for (int x = 0; x < 10; x++) {
                arr[x] = new Square(0, 0);
            }
            rows.add(new Row(arr));
        }
        m.getRows().clear();
        m.getRows().addAll(rows);
        m.getRows().get(9).getColumns().get(0).setRobot(1);
    }
    
    static int[] findKarel(Town t) {
        for (int y = 0; y < t.getRows().size(); y++) {
            Row r = t.getRows().get(y);
            for (int x = 0; x < r.getColumns().size(); x++) {
                Square square = r.getColumns().get(x);
                if (square.getRobot() != 0) {
                    return new int[] { x, y, square.getRobot() };
                }
            }
        }
        return null;
    }
    
    @ModelOperation static void step(Town t) {
        t.setError(0);
        int[] xyd = findKarel(t);
        if (xyd != null) {
            int[] next = stepInDirection(xyd);
            try {
                t.getRows().get(next[1]).getColumns().get(next[0]).setRobot(next[2]);
            } catch (IndexOutOfBoundsException ex) {
                t.setError(1);
                return;
            }
            t.getRows().get(xyd[1]).getColumns().get(xyd[0]).setRobot(0);
        }
    }

    private static int[] stepInDirection(int[] xyd) {
        int[] old = xyd.clone();
        switch (xyd[2]) {
            case 1: old[0]++; break;
            case 2: old[1]++; break;
            case 3: old[0]--; break;
            case 4: old[1]--; break;
        }
        return old;
    }
    
    @ModelOperation static void left(Town t) {
        t.setError(0);
        int[] xyd = findKarel(t);
        if (xyd != null) {
            if (--xyd[2] == 0) {
                xyd[2] = 4;
            }
            t.getRows().get(xyd[1]).getColumns().get(xyd[0]).setRobot(xyd[2]);
        }
    }
    
    @ModelOperation static void put(Town t) {
        int[] xyd = findKarel(t);
        Square sq = t.getRows().get(xyd[1]).getColumns().get(xyd[0]);
        if (sq.getSign() >= 5) {
            t.setError(3);
            return;
        }
        sq.setSign(sq.getSign() + 1);
    }

    @ModelOperation static void take(Town t) {
        int[] xyd = findKarel(t);
        Square sq = t.getRows().get(xyd[1]).getColumns().get(xyd[0]);
        if (sq.getSign() <= 0) {
            t.setError(2);
            return;
        }
        sq.setSign(sq.getSign() - 1);
    }
}
