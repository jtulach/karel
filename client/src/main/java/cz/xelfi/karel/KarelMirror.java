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
import net.java.html.js.JavaScriptBody;

/** Integration with CodeMirror editor.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class KarelMirror {
    static {
        registerSyntax();
    }
    
    static Object initCodeMirror(String id) {
        Object cm = initCodeMirrorImpl(id);
        return cm;
    }
    
    static boolean isKeyword(String text) {
        Iterator<KarelToken> it = KarelToken.tokenize(text);
        if (it.hasNext()) {
            KarelToken kt = it.next();
            if (kt.isIdentifier()) {
                return false;
            }
            return true;
        }
        return false;
    }
 
    @JavaScriptBody(args = {}, javacall = true, body = 
"CodeMirror.defineMode(\"karel\", function() {\n" +
"  var numbers = /^([0-9]+)/i;\n" +
"\n" +
"  return {\n" +
"    startState: function() {\n" +
"      return {context: 0};\n" +
"    },\n" +
"    token: function(stream, state) {\n" +
"      if (stream.eatSpace())\n" +
"        return null;\n" +
"\n" +
"      var w;\n" +
"\n" +
"      if (stream.eatWhile(/\\S/)) {\n" +
"        w = stream.current();\n" +
"\n" +
"        if (@cz.xelfi.karel.KarelMirror::isKeyword(Ljava/lang/String;)(w)) {\n" +
"          return 'keyword';\n" +
"        } else if (numbers.test(w)) {\n" +
"          return 'number';\n" +
"        } else {\n" +
"          return null;\n" +
"        }\n" +
"      } else if (stream.eat('#')) {\n" +
"        stream.skipToEnd();\n" +
"        return 'comment';\n" +
"      } else {\n" +
"        stream.next();\n" +
"      }\n" +
"      return null;\n" +
"    }\n" +
"  }; \n" +
"});\n" +
"")
    private static native void registerSyntax();
    
    @JavaScriptBody(args = { "id" }, body = 
"      var el = document.getElementById(id);\n" +
"      if (!el) return null;\n" + 
"      var opts = {\n" +
"        model: 'karel',\n" +
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
    private static native Object initCodeMirrorImpl(String id);

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
