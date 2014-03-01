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

import cz.xelfi.karel.KarelCompiler.AST;
import cz.xelfi.karel.KarelCompiler.Call;
import cz.xelfi.karel.KarelCompiler.Define;
import cz.xelfi.karel.KarelCompiler.If;
import cz.xelfi.karel.KarelCompiler.Root;
import java.util.Iterator;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CompileTest {
    @Test public void tokenize() {
        Iterator<KarelToken> it = KarelToken.tokenize(
              "čelem-vzad\n"
            + "  vlevo.\n"
            + "  vl.\n"
            + "konec\n"
            + "");
        
        final KarelToken i1 = it.next();
        assertTrue(i1.isIdentifier(), "Identifier");
        assertEquals(i1.text(), "čelem-vzad");

        KarelToken i2 = it.next();
        assertTrue(i2.isIdentifier(), "Identifier");
        assertEquals(i2.text(), "vlevo.");
        
        KarelToken i3 = it.next();
        assertTrue(i3.isIdentifier(), "Identifier");
        assertEquals(i3.text(), "vl.");
        
        assertEquals(it.next(), KarelToken.END);
        assertEquals(it.next(), KarelToken.EOF);
        assertFalse(it.hasNext());
    }
    
    @Test public void singleWord() throws SyntaxException {
        Iterator<KarelToken> it = KarelToken.tokenize("konec");
        assertTrue(it.hasNext());
        KarelToken kt = it.next();
        assertEquals(kt, KarelToken.END);
        assertTrue(it.hasNext());
        assertEquals(it.next(), KarelToken.EOF);
        assertFalse(it.hasNext());
    }

    @Test public void astize() throws SyntaxException {
        AST root = KarelCompiler.toAST(
            "čelem-vzad\n"
                + "  vlevo-vbok\n"
                + "  vlevo-vbok\n"
                + "Konec\n"
                + "");
        List<AST> arr = ((Root)root).children;
        assertEquals(arr.size(), 5, "five definitions: " + arr);
        arr = ((Define)arr.get(4)).children;
        
        assertEquals(arr.size(), 2, "Two calls: " + arr);
        assertTrue(arr.get(0) instanceof Call);
        assertTrue(arr.get(1) instanceof Call);
        
        assertEquals(arr.get(0).token.text(), "vlevo-vbok");
        assertEquals(arr.get(1).token.text(), "vlevo-vbok");
    }
    
    @Test public void stepWithCare() throws SyntaxException {
        AST root = KarelCompiler.toAST(
            "step-care\n"
          + "  kdyz není zed\n"
          + "    krok\n"
          + "  KONec\n"
          + "konec\n"
        );
        List<AST> arr = ((Root)root).children;
        assertEquals(arr.size(), 5, "five definitions: " + arr);
        arr = ((Define)arr.get(4)).children;

        assertEquals(arr.size(), 1, "One if: " + arr);
        assertTrue(arr.get(0) instanceof If);
        
        If f = (If)arr.get(0);
        assertNull(f.no, "Only one branch");
        
        assertNotNull(f.yes, "One branch present");
        assertEquals(f.yes.size(), 1, "One call in the branch");
        assertTrue(f.yes.get(0) instanceof Call, "The call is here");
    }
}
