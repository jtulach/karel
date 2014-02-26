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
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class KarelCompiler {
    private final Town town;
    private final Root root;
    private final KarelCompiler prev;
    private final AST owner;
    private final List<AST> instructions;
    private int pc;
    private int count;
    
    private KarelCompiler(KarelCompiler p, AST fn, List<AST> inst) {
        root = p.root;
        town = p.town;
        prev = p;
        owner = fn;
        instructions = inst;
    }
    
    private KarelCompiler(Town t, Root r, AST fn, List<AST> inst) {
        root = r;
        town = t;
        prev = null;
        owner = fn;
        instructions = inst;
    }
    
    public static KarelCompiler execute(Town town, Root r, String function) {
        for (AST ast : r.children) {
            if (ast instanceof Define) {
                final Define fn = (Define)ast;
                if (fn.token.sameText(function)) {
                    return new KarelCompiler(town, r, fn, fn.children);
                }
            }
        }
        throw new IllegalArgumentException();
    }
    
    public KarelCompiler next() {
        if (instructions.size() <= pc) {
            int cnt = owner.repeatFrame(this, count);
            count = cnt;
            if (cnt > 0) {
                pc = 0;
                return this;
            } else {
                return prev;
            }
        } else {
            AST inst = instructions.get(pc++);
            KarelCompiler inner = inst.exec(this);
            if (inner != null) {
                return inner;
            } else {
                return this;
            }
        }
    }
    
    
    public static AST toAST(String text) throws SyntaxException {
        Iterator<KarelToken> it = KarelToken.tokenize(text);
        Stack<AST> stack = new Stack<AST>();
        final Root root = new Root();
        stack.push(root);
        while (it.hasNext()) {
            final KarelToken t = it.next();
            final AST top = stack.peek();
            if (t == KarelToken.EOF) {
                break;
            }
            if (t == KarelToken.END) {
                if (top instanceof Root) {
                    throw new SyntaxException(t);
                }
                stack.pop();
                continue;
            }
            if (t == KarelToken.ELSE) {
                if (stack.peek() instanceof If) {
                    ((If)stack.peek()).switchElse();
                } else {
                    throw new SyntaxException(t);
                }
                continue;
            }
            if (t == KarelToken.IF) {
                boolean is = it.next().trueFalse();
                If cond = new If(t, is, it.next());
                top.addNode(cond);
                stack.push(cond);
                continue;
            }
            if (t == KarelToken.WHILE) {
                boolean is = it.next().trueFalse();
                While w = new While(t, is, it.next());
                top.addNode(w);
                stack.push(w);
                continue;
            }
            if (t == KarelToken.REPEAT) {
                Repeat r = new Repeat(t, it.next());
                top.addNode(r);
                stack.push(r);
                continue;
            }
            if (t.isIdentifier()) {
                AST d;
                if (top instanceof Root) {
                    d = new Define(t);
                    stack.push(d);
                } else {
                    d = new Call(t);
                }
                top.addNode(d);
                continue;
            }
        }
        return root;
    }

    static abstract class AST {
        final KarelToken token;

        public AST(KarelToken token) {
            this.token = token;
        }
        
        abstract void addNode(AST child) throws SyntaxException;
        
        abstract KarelCompiler exec(KarelCompiler frame);
        int repeatFrame(KarelCompiler frame, int counter) {
            return 0;
        }
    }
    
    static final class Root extends AST {
        final List<AST> children = new ArrayList<AST>();

        public Root() {
            super(KarelToken.EOF);
        }
        
        @Override
        void addNode(AST child) throws SyntaxException {
            children.add(child);
        }

        @Override
        KarelCompiler exec(KarelCompiler frame) {
            throw new IllegalStateException();
        }
    }
    
    static final class Define extends AST {
        final List<AST> children;
        public Define(KarelToken id) {
            super(id);
            children = new ArrayList<AST>();
        }

        @Override
        void addNode(AST child) {
            children.add(child);
        }

        @Override
        KarelCompiler exec(KarelCompiler frame) {
            return new KarelCompiler(frame, this, children);
        }
    }
    
    static final class Call extends AST {
        public Call(KarelToken id) {
            super(id);
        }

        @Override
        void addNode(AST child) throws SyntaxException {
            throw new SyntaxException(child.token);
        }

        @Override
        KarelCompiler exec(KarelCompiler frame) {
            for (AST a : frame.root.children) {
                if (a instanceof Define) {
                    Define d = (Define)a;
                    if (d.token.sameText(this.token.text())) {
                        return new KarelCompiler(frame, d, d.children);
                    }
                }
            }
            throw new IllegalStateException("Cannot find " + this.token.text());
        }
    }
    
    static final class If extends AST {
        private final KarelToken cond;
        List<AST> yes;
        List<AST> no;

        public If(KarelToken t, boolean yes, KarelToken cond) {
            super(t);
            this.cond = cond;
        }

        @Override
        KarelCompiler exec(KarelCompiler frame) {
            if (TownModel.isCondition(frame.town, this.cond)) {
                return new KarelCompiler(frame, this, yes);
            } else {
                return no == null ? null : new KarelCompiler(frame, this, no);
            }
        }

        final void switchElse() {
            no = new ArrayList<AST>();
        }

        @Override
        void addNode(AST child) throws SyntaxException {
            if (no != null) {
                no.add(child);
            } else {
                if (yes == null) {
                    yes = new ArrayList<AST>();
                }
                yes.add(child);
            }
        }
        
    }
    
    static final class While extends AST {
        private final List<AST> block;
        private final KarelToken cond;

        public While(KarelToken t, boolean yes, KarelToken cond) {
            super(t);
            this.block = new ArrayList<AST>();
            this.cond = cond;
        }

        @Override
        void addNode(AST child) throws SyntaxException {
            block.add(child);
        }

        @Override
        KarelCompiler exec(KarelCompiler frame) {
            if (TownModel.isCondition(frame.town, cond)) {
                return new KarelCompiler(frame, this, block);
            } else {
                return null;
            }
        }

        @Override
        int repeatFrame(KarelCompiler frame, int counter) {
            if (TownModel.isCondition(frame.town, cond)) {
                return 1;
            }
            return 0;
        }
    }
    
    static final class Repeat extends AST {
        private final List<AST> block;
        private final int count;

        public Repeat(KarelToken t, KarelToken count) {
            super(t);
            this.block = new ArrayList<AST>();
            this.count = Integer.parseInt(count.text().toString());
        }

        @Override
        void addNode(AST child) throws SyntaxException {
            block.add(child);
        }

        @Override
        KarelCompiler exec(KarelCompiler frame) {
            KarelCompiler my = new KarelCompiler(frame, this, block);
            my.count = count;
            return my;
        }
        
        @Override
        int repeatFrame(KarelCompiler frame, int counter) {
            return counter - 1;
        }
    }
}
