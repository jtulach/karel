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
package cz.xelfi.karel.blockly;

import java.util.ArrayList;
import java.util.List;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;

@JavaScriptResource("blockly_compressed.js")
public final class Workspace {
    static {
        init0();
        KarelBlockly.init0();
    }
    private final Object js;

    private Workspace(Object js) {
        this.js = js;
    }

    public static Workspace create(String id) {
        Object js = create0(id);
        return new Workspace(js);
    }



    public List<Procedure> getProcedures() {
        Object[] blocks = list0(js);
        List<Procedure> arr = new ArrayList<>(blocks.length);
        for (Object js : blocks) {
            arr.add(new Procedure(js));
        }
        return arr;
    }

    Object rawJS() {
        return js;
    }

    @JavaScriptBody(args = {}, body = "", wait4js = false)
    private static native void init0();

    @JavaScriptBody(args = { "id" }, wait4js = true, body =
        "return Blockly.karel.inject(id);"
    )
    private static native Object create0(String id);

    @JavaScriptBody(args = { "workspace" }, wait4js = true, body =
        "var arr = [];\n" +
        "var blocks = workspace.getTopBlocks();\n" +
        "for (var i = 0; i < blocks.length; i++) {\n" +
        "  if (blocks[i].type == 'karel_funkce') {\n" +
        "    arr.push(blocks[i]);\n" +
        "  }\n" +
        "}\n" +
        "return arr;\n" +
        ""
    )
    private static native Object[] list0(Object js);


    @JavaScriptBody(args = { "id" }, body =
        "if (!id) return false;\n" +
        "var e = document.createElement('div');\n" +
        "e.id = id;\n" +
        "document.body.appendChild(e);\n" +
        "return e == document.getElementById(id);"
    )
    static native boolean defineElement(String id);

    @JavaScriptBody(args = {}, body =
        "window.XML = {};\n" +
        "window.XMLList = function XMLList() { return {}; }\n"
            + ""
            + ""
            + "(function(DOMParser) {\n" +
"	\"use strict\";\n" +
"\n" +
"	var\n" +
"	  DOMParser_proto = DOMParser.prototype\n" +
"	, real_parseFromString = DOMParser_proto.parseFromString\n" +
"	;\n" +
"\n" +
"	// Firefox/Opera/IE throw errors on unsupported types\n" +
"	try {\n" +
"		// WebKit returns null on unsupported types\n" +
"		if ((new DOMParser).parseFromString(\"\", \"text/html\")) {\n" +
"			// text/html parsing is natively supported\n" +
"			return;\n" +
"		}\n" +
"	} catch (ex) {}\n" +
"\n" +
"	DOMParser_proto.parseFromString = function(markup, type) {\n" +
"		if (/^\\s*text\\/html\\s*(?:;|$)/i.test(type)) {\n" +
"			var\n" +
"			  doc = document.implementation.createHTMLDocument(\"\")\n" +
"			;\n" +
"	      		if (markup.toLowerCase().indexOf('<!doctype') > -1) {\n" +
"        			doc.documentElement.innerHTML = markup;\n" +
"      			}\n" +
"      			else {\n" +
"        			doc.body.innerHTML = markup;\n" +
"      			}\n" +
"			return doc;\n" +
"		} else {\n" +
"			return real_parseFromString.apply(this, arguments);\n" +
"		}\n" +
"	};\n" +
"}(DOMParser));"
    )
    static native void defineXML();
}
