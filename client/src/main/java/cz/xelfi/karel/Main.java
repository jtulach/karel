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

import net.java.html.boot.BrowserBuilder;


/** Bootstrap and initialization. */
public final class Main {
    private Main() {
    }
    
    /** Launches the browser */
    public static void main(String... args) throws Exception {
        BrowserBuilder.newBrowser().
            loadPage("pages/index.html").
            loadClass(Main.class).
            invoke("onPageLoad", args).
            showAndWait();
        System.exit(0);
    }
    
    /** Called when page is ready */
    public static void onPageLoad(String... args) throws Exception {
        Town t = new Town();
        t.clear();
        
        String src = KarelMirror.getLocalText();
        if (src == null) {
            src = "čelem-vzad\n"
          + "  vlevo-vbok\n"
          + "  vlevo-vbok\n"
          + "konec\n";
        }
        
        Karel d = new Karel("home", "msg", null, t, null, src, 300);
        d.applyBindings();
        d.loadTasks("tasks/list.js");
    }
}
