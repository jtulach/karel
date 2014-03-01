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

import net.java.html.js.JavaScriptBody;

/** Integration with CodeMirror editor.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class KarelMirror {
    @JavaScriptBody(args = { "id" }, body = 
"      var el = document.getElementById(id);\n" +
"      if (!el) return null;\n" + 
"      var opts = {\n" +
"        lineNumbers: true,\n" +
"        lineWrapping: true\n" +
"      };" +
"      var cm = CodeMirror.fromTextArea(el, opts);\n" +
"      cm.on('change', function() {\n" +
"        cm.save();\n" +
"        ko.utils.triggerEvent(el, 'change');\n" +
"      });\n" +
"      return cm;"
    )
    static native Object initCodeMirror(String id);

    @JavaScriptBody(args = {  }, body = 
        "var src = localStorage ? localStorage['source'] : null;\n" +
        "return src ? src : null;\n"
    )
    static native String getLocalText();
    
    @JavaScriptBody(args = { "value" }, body = 
        "if (localStorage) localStorage['source'] = value;"
    )
    static native void setLocalText(String value);
}
