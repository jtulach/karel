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

import java.util.ArrayList;
import java.util.List;


public final class Execution {
    private final Environment env;
    private final List<Procedure> procedures;
    private Execution delegate;
    private Counter repeat;
    private Object current;

    Execution(Environment env, List<Procedure> procedures, Object current) {
        this.env = env;
        this.procedures = new ArrayList<>(procedures);
        this.current = current;
        Workspace.select(current);
    }

    public enum Condition {
        WALL, MARK, NORTH, SOUTH, WEST, EAST;
    }

    public enum State {
        RUNNING, FINISHED, ERROR_WALL, ERROR_EMPTY, ERROR_FULL, ERROR_NOT_FOUND;
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
        final int n;

        Info(Object block) {
            Object[] arr = Workspace.info(block);
            this.type = (String)arr[0];
            this.child = arr[1];
            this.next = arr[2];
            this.negCond = "TRUE".equals(arr[3]); // NOI18N
            this.cond = arr[4] == null ? null : Condition.valueOf((String)arr[4]);
            this.ifTrue = arr[5];
            this.ifFalse = arr[6];
            this.call = (String) arr[7];
            this.parent = arr[8];
            this.n = arr[9] == null ? 0 : Integer.parseInt((String) arr[9]);
        }
    }

    private static final class Counter {
        final Object repeat;
        final Counter previous;
        int counter;

        Counter(Counter previous, Object repeat, int counter) {
            this.previous = previous;
            this.repeat = repeat;
            this.counter = counter;
        }
    }

    private static final class Call {
        final Object caller;
        final Object top;

        public Call(Object caller, Object top) {
            this.caller = caller;
            this.top = top;
        }

        static Object top(Object obj) {
            if (obj instanceof Call) {
                return ((Call)obj).top;
            }
            return null;
        }
    }

    private static State buildIn(String name, Environment env) {
        if (name.equals("STEP")) {
            if (!env.step()) {
                return State.ERROR_WALL;
            }
        } else if (name.equals("LEFT")) {
            env.left();
        } else if (name.equals("PUT")) {
            if (!env.put()) {
                return State.ERROR_FULL;
            }
        } else if (name.equals("TAKE")) {
            if (!env.take()) {
                return State.ERROR_EMPTY;
            }
        } else {
            return null;
        }
        return State.FINISHED;
    }

    public State next() {
        if (current instanceof State) {
            return (State)current;
        }
        if (current instanceof String) {
            return (State) (current = buildIn((String) current, env));
        }
        AGAIN: for (;;) {
            boolean returned = false;
            if (delegate != null) {
                State exec = delegate.next();
                if (exec == State.RUNNING) {
                    return exec;
                }
                if (exec == State.FINISHED) {
                    delegate = null;
                    returned = true;
                } else {
                    return (State)(current = exec);
                }
            }
            Info info = new Info(current);
            Object next;
            switch (info.type) {
                case "karel_funkce":
                    next = info.child;
                    break;
                case "karel_while":
                    if (env.isCondition(info.cond) == info.negCond && info.child != null) {
                        next = info.child;
                        break;
                    }
                    next = info.next;
                    break;
                case "karel_repeat":
                    if (repeat != null && repeat.repeat.equals(current)) {
                        if (--repeat.counter <= 0) {
                            repeat = repeat.previous;
                            next = null;
                            break;
                        }
                    } else {
                        repeat = new Counter(repeat, current, info.n);
                    }
                    next = info.child;
                    break;
                case "karel_call":
                    if (!returned) {
                        State buildIn = buildIn(info.call, env);
                        if (buildIn == null) {
                            Procedure found = null;
                            for (Procedure p : procedures) {
                                if (p.getName().equals(info.call)) {
                                    found = p;
                                    break;
                                }
                            }
                            if (found == null) {
                                return (State)(current = State.ERROR_NOT_FOUND);
                            }
                            delegate = found.prepareExecution(env);
                            return State.RUNNING;
                        } else if (buildIn != State.FINISHED) {
                            return (State) (current = buildIn);
                        }
                    }
                    next = info.next;
                    break;
                case "karel_if":
                case "karel_if_else":
                    if (env.isCondition(info.cond) == info.negCond) {
                        next = info.ifTrue;
                    } else {
                        next = info.ifFalse;
                    }
                    if (next == null) {
                        next = info.next;
                    }
                    break;
                default:
                    throw new IllegalStateException(info.type);
            }
            if (next != null) {
                current = next;
            } else {
                END: for (;;) {
                    current = info.parent;
                    Info parentInfo = new Info(current);
                    switch (parentInfo.type) {
                        case "karel_funkce":
                            return (State) (current = State.FINISHED);
                        case "karel_if":
                        case "karel_if_else":
                            info = parentInfo;
                            continue;
                        default:
                            break END;
                    }
                }
            }
            break;
        }
        Workspace.select(current);
        return State.RUNNING;
    }

    String currentType() {
        if (delegate != null) {
            return delegate.currentType();
        }
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
        public boolean take();
    }

}
