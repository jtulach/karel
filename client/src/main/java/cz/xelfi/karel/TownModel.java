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

import java.util.List;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className = "Town", properties = {
    @Property(name = "error", type = int.class),
    @Property(name = "errorParams", type = String.class, array = true),
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
            final int m = town.getRows().get(xyd[1]).getColumns().get(xyd[0]).getMarks();
            return m > 0 && m < 100;
        }
        if (KarelToken.WALL != cond) {
            throw new IllegalStateException("" + cond);
        }
        int[] next = stepInDirection(xyd);
        try {
            Square sq = town.getRows().get(next[1]).getColumns().get(next[0]);
            return sq.getMarks() == 111;
        } catch (IndexOutOfBoundsException ex) {
            return true;
        }
    }
    
    
    @Model(className = "Square", properties = {
        @Property(name = "robot", type = int.class),
        @Property(name = "marks", type = int.class),
    })
    static class SquareModel {
        static boolean isEmpty(Square sq) {
            return sq.getRobot() == 0 && sq.getMarks() == 0;
        }
    }
    
    @ModelOperation static void clear(Town m) {
        init(m, 10, 10);
        m.getRows().get(9).getColumns().get(0).setRobot(1);
    }

    static void init(Town m, int columns, int rows) {
        List<Row> r = m.getRows();
        for (int y = 0; y < rows; y++) {
            List<Square> cl;
            if (r.size() > y) {
                cl = r.get(y).getColumns();
            } else {
                Row nr = new Row();
                cl = nr.getColumns();
                r.add(nr);
                
            }
            for (int x = 0; x < columns; x++) {
                Square sq;
                if (cl.size() > x && (sq = cl.get(x)) != null) {
                    sq.setMarks(0);
                    sq.setRobot(0);
                } else {
                    cl.add(new Square(0, 0));
                }
            }
            while (cl.size() > columns) {
                cl.remove(columns);
            }
        }
        while (r.size() > rows) {
            r.remove(rows);
        }
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
                final Square sq = t.getRows().get(next[1]).getColumns().get(next[0]);
                if (sq.getMarks()== 111) {
                    t.setError(1);
                    return;
                }
                sq.setRobot(next[2]);
            } catch (IndexOutOfBoundsException ex) {
                t.setError(1);
                return;
            }
            t.getRows().get(xyd[1]).getColumns().get(xyd[0]).setRobot(0);
        }
    }
    
    @ModelOperation static void wall(Town t, int x, int y) {
        final Square sq = t.getRows().get(y).getColumns().get(x);
        sq.setMarks(111);
        sq.setRobot(0);
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
        if (sq.getMarks()>= 5) {
            t.setError(3);
            return;
        }
        sq.setMarks(sq.getMarks()+ 1);
    }

    @ModelOperation static void take(Town t) {
        int[] xyd = findKarel(t);
        Square sq = t.getRows().get(xyd[1]).getColumns().get(xyd[0]);
        if (sq.getMarks()<= 0) {
            t.setError(2);
            return;
        }
        sq.setMarks(sq.getMarks()- 1);
    }
    
    static String toJSON(Town t) {
        final Town cl = t.clone();
        simplify(cl);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"rows\":[");
        String sep = "";
        for (Row row : cl.getRows()) {
            sb.append(sep).append(row);
            sep = ",";
        }
        sb.append("]}");
        return sb.toString();
    }
    
    static void simplify(Town t) {
        for (int i = 0; i < t.getRows().size(); i++) {
            Row r = t.getRows().get(i);
            int empty = 0;
            final List<Square> cols = r.getColumns();
            for (int j = 0; j < cols.size(); j++) {
                Square sq = cols.get(j);
                if (SquareModel.isEmpty(sq)) {
                    r.getColumns().set(j, null);
                    empty++;
                }
            }
            if (empty == cols.size()) {
                t.getRows().set(i, null);
            }
        }
    }

    static void load(Town real, Town simple) {
        init(real, 10, 10);
        for (int i = 0; i < real.getRows().size(); i++) {
            Row r = simple.getRows().get(i);
            if (r == null) {
                continue;
            }
            final List<Square> cols = r.getColumns();
            for (int j = 0; j < cols.size(); j++) {
                Square sq = cols.get(j);
                if (sq != null) {
                    real.getRows().get(i).getColumns().set(j, sq.clone());
                }
            }
        }
        real.setError(simple.getError());
    }
}
