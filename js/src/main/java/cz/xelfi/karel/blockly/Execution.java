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

import java.util.LinkedList;


public final class Execution {
    private final Environment env;
    private final LinkedList<Object> stack;
    private Object current;

    Execution(Environment env, Object current) {
        this.env = env;
        this.current = current;
        this.stack = new LinkedList<>();
        this.stack.add(current);
        Workspace.select(current);
    }

    public enum Condition {
        WALL, MARK, NORTH, SOUTH, WEST, EAST;
    }

    public enum State {
        RUNNING, FINISHED, ERROR_WALL, ERROR_EMPTY, ERROR_FULL;
    }

    private static final class Info {
        final String type;
        final Object child;
        final Object next;
        final boolean negCond;
        final Condition cond;
        final Object ifTrue;
        final Object ifFalse;
        final String call;
        final Object parent;

        Info(Object block) {
            Object[] arr = Workspace.info(block);
            this.type = (String)arr[0];
            this.child = arr[1];
            this.next = arr[2];
            this.negCond = Boolean.TRUE.equals(arr[3]);
            this.cond = arr[4] == null ? null : Condition.valueOf((String)arr[4]);
            this.ifTrue = arr[5];
            this.ifFalse = arr[6];
            this.call = (String) arr[7];
            this.parent = arr[8];
        }
    }

    public State next() {
        if (current instanceof State) {
            return (State)current;
        }
        if (stack.isEmpty()) {
            return (State)(current = State.FINISHED);
        }
        Info info = new Info(current);
        Object next;
        switch (info.type) {
            case "karel_funkce":
                if (current.equals(stack.getLast())) {
                    next = info.child;
                } else {
                    next = null;
                }
                break;
            case "karel_while":
                if (env.isCondition(info.cond) == info.negCond && info.child != null) {
                    next = info.child;
                    break;
                }
                next = info.next;
                break;
            case "karel_call":
                if (info.call.equals("krok")) {
                    if (!env.step()) {
                        return (State) (current = State.ERROR_WALL);
                    }
                }
                next = info.next;
                break;
            default:
                throw new IllegalStateException(info.type);
        }
        if (next != null) {
            current = next;
        } else {
            current = info.parent;
            Info parentInfo = new Info(current);
            if (parentInfo.type.equals("karel_funkce")) {
                Object obj = stack.removeLast();
                assert obj.equals(current);
            }
        }
        Workspace.select(current);
        return State.RUNNING;
    }

    String currentType() {
        if (current instanceof State) {
            return null;
        }
        return new Info(current).type;
    }

    public static interface Environment {
        public boolean isCondition(Condition c);
        public void left();
        public boolean step();
        public boolean put();
        public boolean pick();
    }

}
