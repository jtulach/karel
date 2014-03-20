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
        registerSyntax(KarelToken.keywords());
    }
    
    static Object initCodeMirror(Karel k, String id) {
        Object cm = initCodeMirrorImpl(k, id);
        registerCodeCompletion(cm);
        return cm;
    }
    
    static Object[] listCompletions(cz.xelfi.karel.Karel karel, String line, String word, int lineNo, int offset, int end) {
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
        List<String> touch = new ArrayList<String>();
        switch (type) {
            case 1:
                appendWord(arr, touch, word, KarelToken.IS.text(), KarelToken.NOT.text());
                break;
            case 2:
                appendWord(arr, touch, word, "1", "2", "3", "4", "5", "6", "7", "8");
                break;
            case 3:
                appendWord(arr, touch, word, 
                    KarelToken.WALL.text(),
                    KarelToken.SIGN.text(),
                    KarelToken.EAST.text(), 
                    KarelToken.SOUTH.text(),
                    KarelToken.WEST.text(),
                    KarelToken.NORTH.text()
                );
                break;
            default:
                appendWord(arr, touch, word, 
                    KarelToken.IF.text(),
                    KarelToken.WHILE.text(),
                    KarelToken.REPEAT.text(),
                    KarelToken.END.text()
                );
                for (Command command : karel.getCommands()) {
                    appendWord(arr, touch, word, command.getName());
                }
                break;
        }
        if (karel != null) {
            int max = Math.min(10, touch.size());
            List<Completion> cmpl = new ArrayList<Completion>(max);
            if (word.isEmpty()) {
                for (int i = 0; i < max; i++) {
                    String w = touch.get(i);
                    String then = null;
                    if (KarelToken.IF.sameText(w)) {
                        w = KarelToken.IF.text() + " " +
                            KarelToken.IS.text() + " " +
                            KarelToken.SIGN.text() + "\n";
                        then = "\n" + KarelToken.ELSE.text() + "\n\n" + KarelToken.END.text() + "\n";
                    } else if (KarelToken.WHILE.sameText(w)) {
                        w = KarelToken.WHILE.text() + " " +
                            KarelToken.IS.text() + " " +
                            KarelToken.SIGN.text() + "\n";
                        then = "\n\n" + KarelToken.END.text() + "\n";
                    } else if (KarelToken.REPEAT.sameText(w)) {
                        w = KarelToken.REPEAT.text() + " 8\n";
                        then = "\n\n" + KarelToken.END.text() + "\n";
                    } else {
                        w += "\n";
                        then = "";
                    }
                    cmpl.add(new Completion(touch.get(i), w, lineNo, offset, end, then));
                }
                
            } else {
                // replace
                for (int i = 0; i < max; i++) {
                    final String w = touch.get(i);
                    cmpl.add(new Completion(w, w, lineNo, offset, end, ""));
                }
            }
            karel.updateCompletions(cmpl);
        }
        return arr.toArray();
    }
    
    private static void appendWord(List<String> arr, List<String> all, String prefix, CharSequence... values) {
        for (CharSequence cs : values) {
            all.add(cs.toString());
            if (prefix.length() > cs.length()) {
                continue;
            }
            if (!cs.subSequence(0, prefix.length()).equals(prefix)) {
                continue;
            }
            arr.add(cs.toString());
        }
    }
 
    @JavaScriptBody(args = { "kw" }, body = 
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
"        if (kw.indexOf(w.toString().toLowerCase()) >= 0) {\n" +
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
    private static native void registerSyntax(String... keywords);
    

    @JavaScriptBody(args = { "cm" }, javacall = true, body = 
"  var WORD = /[\\S$]+/;\n" +
"  function cmptCmpl(editor) {\n" +
"    var cur = editor.getCursor();\n" +
"    var offset = editor.getDoc().indexFromPos(editor.getCursor());\n" +
"    var curLine = editor.getLine(cur.line);\n" +
"    var start = cur.ch;\n" +
"    var end = start;\n" +
"    while (end < curLine.length && WORD.test(curLine.charAt(end))) ++end;\n" +
"    while (start && WORD.test(curLine.charAt(start - 1))) --start;\n" +
"    var curWord = curLine.slice(start, end);\n" +
"    var list = @cz.xelfi.karel.KarelMirror::listCompletions(Lcz/xelfi/karel/Karel;Ljava/lang/String;Ljava/lang/String;III)(\n" +
"        editor['karel'],\n" +
"        curLine,\n" +
"        curWord,\n" +
"        cur.line,\n" +
"        start,\n" +
"        end\n" +
"    );\n" +
"    return {list: list, from: CodeMirror.Pos(cur.line, start), to: CodeMirror.Pos(cur.line, end)};\n" +
"  }\n" +
"  CodeMirror.registerHelper('hint', 'karel', cmptCmpl);\n" +
"  cm.getDoc().on('cursorActivity', function() {\n" +
"    cmptCmpl(cm);\n" +
"  });\n" +
"\n"
    )
    private static native void registerCodeCompletion(Object cm);

    @JavaScriptBody(args = { "id", "word", "then", "line", "start", "end" }, body = 
"      var el = document.getElementById(id);\n" +
"      if (!el) return null;\n" + 
"      var cm = el['cm'];\n" + 
"      var doc = cm.getDoc();\n" +            
"      if (then === null) { word += ' '; }\n" +            
"      doc.replaceRange(word, CodeMirror.Pos(line, start), CodeMirror.Pos(line, end));\n" +            
"      var cur = doc.getCursor();\n" +            
"      if (then !== null) doc.replaceRange(then, cur);\n" +            
"      cm.setCursor(cur);\n" +            
"      cm.focus();\n" +            
""
    )
    static native void complete(String id, String word, String then, int line, int start, int end);
    
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
"      el['cm'] = cm;\n" +
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
