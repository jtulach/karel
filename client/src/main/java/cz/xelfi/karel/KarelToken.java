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
import java.util.ResourceBundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class KarelToken {
    private static final List<KarelToken> ALL = new ArrayList<KarelToken>();
    final String[] text;
    final int begin;
    final int end;
    
    static final KarelToken NORTH = create("NORTH"); // NOI18N
    static final KarelToken EAST = create("EAST"); // NOI18N
    static final KarelToken SOUTH = create("SOUTH"); // NOI18N
    static final KarelToken WEST = create("WEST"); // NOI18N
    static final KarelToken WALL = create("WALL"); // NOI18N
    static final KarelToken SIGN = create("SIGN"); // NOI18N
    
    static final KarelToken IF = create("IF"); // NOI18N
    static final KarelToken ELSE = create("ELSE"); // NOI18N
    static final KarelToken WHILE = create("WHILE"); // NOI18N
    static final KarelToken REPEAT = create("REPEAT"); // NOI18N
    static final KarelToken END = create("END"); // NOI18N
    static final KarelToken IS = create("IS"); // NOI18N
    static final KarelToken NOT = create("NOT"); // NOI18N
    
    static final KarelToken EOF = new KarelToken("", 0, 0); // NOI18N

    private KarelToken(String text, int begin, int end) {
        this.begin = begin;
        this.end = end;
        this.text = new String[] { text };
    }
    
    private KarelToken(String[] arr) {
        this.text = arr;
        this.begin = -1;
        this.end = -1;
    }

    private static KarelToken create(String key) {
        ResourceBundle rb = ResourceBundle.getBundle("cz/xelfi/karel/Bundle");
        String[] arr = rb.getString(key).split(",");
        KarelToken kt = new KarelToken(arr);
        ALL.add(kt);
        return kt;
    }

    CharSequence text() {
        if (begin == -1 || end == -1) {
            return text[0];
        }
        return text[0].subSequence(begin, end);
    }
    
    boolean isIdentifier() {
        return begin != -1;
    }

    static Iterator<KarelToken> tokenize(String text) {
        List<KarelToken> tokens = new ArrayList<KarelToken>();
        int begin = -1;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                if (begin == -1) {
                    continue;
                } else {
                    KarelToken t = new KarelToken(text, begin, i);
                    for (KarelToken k : ALL) {
                        if (k.sameText(t.text())) {
                            t = k;
                        }
                    }
                    tokens.add(t);
                    begin = -1;
                }
            } else {
                if (begin == -1) {
                    begin = i;
                }
            }
        }
        tokens.add(KarelToken.EOF);
        return tokens.iterator();
    }

    @Override
    public String toString() {
        return "Token{" + "text=" + text() + '}';
    }

    boolean trueFalse() throws SyntaxException {
        if (KarelToken.IS.equals(this)) {
            return true;
        }
        if (KarelToken.NOT.equals(this)) {
            return false;
        }
        throw new SyntaxException(this);
    }

    final boolean sameText(CharSequence text) {
        if (begin == -1 && end == -1) {
            for (String t : this.text) {
                if (t.contentEquals(text)) {
                    return true;
                }
            }
            return false;
        } else {
            return text().equals(text);
        }
    }
    
}
