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
    private KarelCompiler() {
    }
    
    
    public static AST toAST(String text) throws SyntaxException {
        Iterator<KarelToken> it = KarelToken.tokenize(text);
        Stack<AST> stack = new Stack<AST>();
        while (it.hasNext()) {
            final KarelToken t = it.next();
            if (t == KarelToken.END) {
                if (stack.isEmpty()) {
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
                
            }
        }
        return null;
    }

    private static abstract class AST {
        final AST[] onTrue;
        final AST[] onFalse;

        protected AST(AST[] onTrue, AST[] onFalse) {
            this.onTrue = onTrue;
            this.onFalse = onFalse;
        }
        
        abstract boolean isCondition(Town town);
        abstract int repeat(int counter);
    }
    
    private static final class Call extends AST {
        private final String function;
        
        public Call(String function) {
            super(null, null);
            this.function = function;
        }

        @Override
        boolean isCondition(Town town) {
            return true;
        }

        @Override
        int repeat(int counter) {
            return 0;
        }
        
    }
    
    private static final class If extends AST {

        public If(AST[] onTrue, AST[] onFalse) {
            super(onTrue, onFalse);
        }
        @Override
        boolean isCondition(Town town) {
            return true;
        }

        @Override
        int repeat(int counter) {
            return 0;
        }

        final void switchElse() {
            
        }
        
    }
}
