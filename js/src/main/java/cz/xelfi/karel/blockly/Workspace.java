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
package cz.xelfi.karel.blockly;

import java.util.ArrayList;
import java.util.List;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;

@JavaScriptResource("blockly_compressed.js")
public final class Workspace {
    static {
        init0();
    }
    private final Object js;

    private Workspace(Object js) {
        this.js = js;
    }

    public static Workspace create(String id) {
        Object karel = KarelBlockly.getDefault();
        Object js = create0(karel, id);
        return new Workspace(js);
    }

    public Procedure newProcedure(String commandName) {
        return new Procedure(create1(js, "karel_funkce", commandName), this, commandName, commandName);
    }


    public List<Procedure> getProcedures() {
        Object[] blocks = list0(js);
        List<Procedure> arr = new ArrayList<>(blocks.length / 3);
        for (int i = 0; i < blocks.length; i += 3) {
            arr.add(new Procedure(blocks[i + 2], this, (String)blocks[i + 0], (String)blocks[i + 1]));
        }
        return arr;
    }

    public Procedure findProcedure(String id) {
        for (Procedure p : getProcedures()) {
            if (id.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }


    public void clear() {
        clear0(js);
    }

    public void loadXML(String xml) {
        load0(js, xml);
    }

    @Override
    public String toString() {
        return toString0(js);
    }

    Object rawJS() {
        return js;
    }

    @JavaScriptBody(args = {}, body = "", wait4js = false)
    private static native void init0();

    @JavaScriptBody(args = { "karel", "id" }, wait4js = true, body =
        "return karel(id);"
    )
    private static native Object create0(Object karel, String id);

    @JavaScriptBody(args = { "workspace" }, wait4js = true, body =
        "return workspace.procedures();"
    )
    private static native Object[] list0(Object workspace);


    @JavaScriptBody(args = { "workspace", "type", "commandName" }, body =
        "return workspace.newBlock(type, commandName);"
    )
    private static native Object create1(Object workspace, String type, String commandName);

    @JavaScriptBody(args = { "workspace" }, body = "workspace.clear();", wait4js = false)
    private static native void clear0(Object workspace);

    @JavaScriptBody(args = { "workspace", "xml" }, body =
        "workspace.loadXml(xml);"
    )
    private static native void load0(Object workspace, String xml);

    @JavaScriptBody(args = { "workspace" }, body =
        "return workspace.toXml();"
    )
    private static native String toString0(Object workspace);
    @JavaScriptBody(args = { "js" }, body = "if (js.select) js.select();", wait4js = false)
    static native void select(Object js);

    @JavaScriptBody(args = { "js", "b" }, body = "js.setCollapsed(b);", wait4js = false)
    static native void setCollapsed(Object js, boolean b);

    @JavaScriptBody(args = { "js" }, body =
        "return [\n"
      + "  js.type,\n"
      + "  js.getChildren()[0],\n"
      + "  js.getNextBlock(),\n"
      + "  js.getFieldValue('NEG'),\n"
      + "  js.getFieldValue('COND'),\n"
      + "  js.getInputTargetBlock('IFTRUE'),\n"
      + "  js.getInputTargetBlock('IFFALSE'),\n"
      + "  js.getFieldValue('CALL'),\n"
      + "  js.getSurroundParent(),\n"
      + "  js.getFieldValue('N'),\n"
      + "  ''\n"
      + "];\n"
    )
    static native Object[] info(Object js);
}
