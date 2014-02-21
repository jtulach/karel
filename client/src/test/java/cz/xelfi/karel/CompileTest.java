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

import java.util.Iterator;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CompileTest {
    @Test public void compileTurnBack() {
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
    }
}
