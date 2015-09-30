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
package cz.xelfi.karel;

import net.java.html.boot.BrowserBuilder;

public final class Main {
    private static Karel karel;
    private Main() {
    }
    
    public static void main(String... args) throws Exception {
        BrowserBuilder.newBrowser().
            loadPage("pages/index.html").
            loadClass(Main.class).
            invoke("onPageLoad", args).
            showAndWait();
        System.exit(0);
    }

    /** Called when page is ready */
    public static Karel onPageLoad(String... args) throws Exception {
        String src = Storage.getDefault().get("source", "\n\n");
        final Scratch s = new Scratch();
        s.getTown().clear();
    
        karel = new Karel("home", "msg", null, null, s, src, 300, false);
        KarelModel.compile(karel, false);
        karel.applyBindings();
        
        return karel;
    }
}
