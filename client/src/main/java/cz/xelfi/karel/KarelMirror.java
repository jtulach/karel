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
import net.java.html.js.JavaScriptBody;

/** Integration with CodeMirror editor.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class KarelMirror {
    static void initialize() {
        registerSyntax();
        registerCodeCompletion();
    }
    
    static Object initCodeMirror(Karel k, String id) {
        Object cm = initCodeMirrorImpl(k, id);
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
    
    static Object[] listCompletions(cz.xelfi.karel.Karel karel, String line, String word, int offset) {
        Iterator<KarelToken> it = KarelToken.tokenize(line.substring(0, offset));
        int type = 0;
        while (it.hasNext()) {
            KarelToken kt = it.next();
            if (kt == KarelToken.WHILE || kt == KarelToken.IF) {
                type = 1;
                continue;
            }
            if (kt == KarelToken.REPEAT) {
                type = 2;
                continue;
            }
            if (kt == KarelToken.IS || kt == KarelToken.NOT) {
                type = 3;
                continue;
            }
            if (kt == KarelToken.EOF) {
                break;
            }
            type = 0;
        }
        
        List<String> arr = new ArrayList<String>();
        switch (type) {
            case 1:
                appendWord(arr, word, KarelToken.IS.text(), KarelToken.NOT.text());
                break;
            case 2:
                appendWord(arr, word, "1", "2", "3", "4", "5", "6", "7", "8");
                break;
            case 3:
                appendWord(arr, word, 
                    KarelToken.WALL.text(),
                    KarelToken.SIGN.text(),
                    KarelToken.EAST.text(), 
                    KarelToken.SOUTH.text(),
                    KarelToken.WEST.text(),
                    KarelToken.NORTH.text()
                );
                break;
            default:
                appendWord(arr, word, 
                    KarelToken.IF.text(),
                    KarelToken.WHILE.text(),
                    KarelToken.REPEAT.text(),
                    KarelToken.END.text()
                );
                for (Command command : karel.getCommands()) {
                    appendWord(arr, word, command.getName());
                }
                break;
        }
        return arr.toArray();
    }
    
    private static void appendWord(List<String> arr, String prefix, CharSequence... values) {
        for (CharSequence cs : values) {
            if (prefix.length() > cs.length()) {
                continue;
            }
            if (!cs.subSequence(0, prefix.length()).equals(prefix)) {
                continue;
            }
            arr.add(cs.toString());
        }
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
    

    @JavaScriptBody(args = {}, javacall = true, body = 
"  var WORD = /[\\S$]+/;\n" +
"\n" +
"  CodeMirror.registerHelper('hint', 'karel', function(editor) {\n" +
"    var cur = editor.getCursor();\n" +
"    var offset = editor.getDoc().indexFromPos(editor.getCursor());\n" +
"    var curLine = editor.getLine(cur.line);\n" +
"    var start = cur.ch;\n" +
"    var end = start;\n" +
"    while (end < curLine.length && WORD.test(curLine.charAt(end))) ++end;\n" +
"    while (start && WORD.test(curLine.charAt(start - 1))) --start;\n" +
"    var curWord = curLine.slice(start, end);\n" +
"\n" +
"    var list = @cz.xelfi.karel.KarelMirror::listCompletions(Lcz/xelfi/karel/Karel;Ljava/lang/String;Ljava/lang/String;I)(\n" +
"      editor['karel'],\n" +
"      curLine,\n" +
"      curWord,\n" +
"      start\n" +
"    );\n" +
"    \n" +
"    return {list: list, from: CodeMirror.Pos(cur.line, start), to: CodeMirror.Pos(cur.line, end)};\n" +
"  });\n" +
""            
    )
    private static native void registerCodeCompletion();
    
    @JavaScriptBody(args = { "k", "id" }, body = 
"      var el = document.getElementById(id);\n" +
"      if (!el) return null;\n" + 
"      function AutoComplete(cm) {\n" +
"        CodeMirror.showHint(cm, CodeMirror.hint.anyword);\n" +
"      }\n" +
"      function Compile(cm) {\n" +
"        cm['karel'].compile();\n" +
"      }\n" +
"      var opts = {\n" +
"        model: 'karel',\n" +
"        lineNumbers: true,\n" +
"        lineWrapping: true,\n" +
"        extraKeys: {\n" +
"          'Ctrl-Space': AutoComplete,\n" + 
"          'Tab': AutoComplete,\n" + 
"          'F9': Compile\n" + 
"        }\n" +
"      };" +
"      var cm = CodeMirror.fromTextArea(el, opts);\n" + 
"      cm.on('change', function() {\n" +
"        cm.save();\n" +
"        ko.utils.triggerEvent(el, 'change');\n" +
"      });\n" +
"      cm['karel'] = k;\n" +
"      return cm;"
    )
    private static native Object initCodeMirrorImpl(Karel k, String id);

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
