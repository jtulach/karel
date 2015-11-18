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

import cz.xelfi.karel.blockly.grammar.KarelBaseListener;
import cz.xelfi.karel.blockly.grammar.KarelLexer;
import cz.xelfi.karel.blockly.grammar.KarelParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

@JavaScriptResource("blockly_compressed.js")
public final class Workspace {
    static {
        init0();
    }
    private final Object js;

    private Runnable selectChange;

    private Workspace(Object js) {
        this.js = js;
        listen0(js, this);
    }

    public static Workspace create(String id) {
        Object karel = KarelBlockly.getDefault();
        Object js = create0(karel, id);
        return new Workspace(js);
    }

    public Procedure newProcedure(String commandName) {
        return new Procedure(create1(js, "karel_funkce", commandName), this, commandName, commandName);
    }

    public Procedure[] parse(String code) {
        KarelLexer lexer = new KarelLexer(new ANTLRInputStream(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        KarelParser parser = new KarelParser(tokens);
        ParseTree tree = parser.karel();
        ParseTreeWalker walker = new ParseTreeWalker();
        class KarelWalker extends KarelBaseListener {
            StringBuilder sb = new StringBuilder();
            List<String> procNames = new ArrayList<>();
            private String space = "";
            KarelWalker() {
            }
            @Override
            public void enterKarel(KarelParser.KarelContext ctx) {
                sb.append("<xml version=\"1.0\" encoding=\"UTF-8\">\n");
            }
            
            @Override
            public void enterCondition(KarelParser.ConditionContext ctx) {
                if (ctx.children.size() == 1) {
                    sb.append(space).append("<field name=\"NEG\">TRUE</field>\n");
                } else {
                    sb.append(space).append("<field name=\"NEG\">FALSE</field>\n");
                }
                sb.append(space).append("<field name=\"COND\">").append(ctx.stop.getText()).append("</field>\n");
            }

            @Override
            public void enterStatements(KarelParser.StatementsContext ctx) {
                sb.append(indent()).append("<statement name=\"IFTRUE\">\n");
            }

            @Override
            public void exitStatements(KarelParser.StatementsContext ctx) {
                sb.append(deindent()).append("</statement>\n");
            }

            @Override
            public void enterElsestatements(KarelParser.ElsestatementsContext ctx) {
                sb.append(indent()).append("<statement name=\"IFFALSE\">\n");
            }

            @Override
            public void exitElsestatements(KarelParser.ElsestatementsContext ctx) {
                sb.append(deindent()).append("</statement>\n");
            }

            @Override
            public void enterNext(KarelParser.NextContext ctx) {
                sb.append(indent()).append("<next>\n");
            }

            @Override
            public void exitNext(KarelParser.NextContext ctx) {
                sb.append(deindent()).append("</next>\n");
            }

            @Override
            public void enterKcall(KarelParser.KcallContext ctx) {
                sb.append(indent()).append("<block type=\"karel_call\">\n");
            }

            @Override
            public void enterCall(KarelParser.CallContext ctx) {
                sb.append(space).append("<field name=\"CALL\">").append(ctx.ID()).append("</field>\n");
            }

            @Override
            public void exitKcall(KarelParser.KcallContext ctx) {
                sb.append(deindent()).append("</block>\n");
            }

            private String deindent() {
                return space = space.substring(2);
            }

            private String indent() {
                String s = space;
                space += "  ";
                return s;
            }

            @Override
            public void enterKrepeat(KarelParser.KrepeatContext ctx) {
                sb.append(indent()).append("<block type=\"karel_repeat\">\n");
                sb.append(space).append("<field name=\"N\">").append(ctx.NUM().getText()).append("</field>\n");
            }

            @Override
            public void exitKrepeat(KarelParser.KrepeatContext ctx) {
                sb.append(deindent()).append("</block>\n");
            }

            @Override
            public void enterKifelse(KarelParser.KifelseContext ctx) {
                sb.append(indent()).append("<block type=\"karel_if_else\">\n");
            }

            @Override
            public void enterKif(KarelParser.KifContext ctx) {
                sb.append(indent()).append("<block type=\"karel_if\">\n");
            }

            @Override
            public void enterKwhile(KarelParser.KwhileContext ctx) {
                sb.append(indent()).append("<block type=\"karel_while\">\n");
            }

            @Override
            public void exitKwhile(KarelParser.KwhileContext ctx) {
                sb.append(deindent()).append("</block>\n");
            }

            @Override
            public void exitKif(KarelParser.KifContext ctx) {
                sb.append(deindent()).append("</block>\n");
            }

            @Override
            public void exitKifelse(KarelParser.KifelseContext ctx) {
                sb.append(deindent()).append("</block>\n");
            }
            
            @Override
            public void enterProcedure(KarelParser.ProcedureContext ctx) {
                final String procName = ctx.name().ID().getText();
                sb.append(indent()).append("<block type=\"karel_funkce\">\n");
                procNames.add(procName);
            }

            @Override
            public void enterName(KarelParser.NameContext ctx) {
                sb.append(space).append("<field name=\"NAME\">").append(ctx.ID()).append("</field>\n");
            }

            @Override
            public void exitProcedure(KarelParser.ProcedureContext ctx) {
                sb.append(deindent()).append("</block>\n");
            }

            @Override
            public void exitKarel(KarelParser.KarelContext ctx) {
                sb.append("</xml>\n");
            }
        }
        final KarelWalker karel = new KarelWalker();
        walker.walk(karel, tree);
        
        loadXML(karel.sb.toString());
//        Object procJs = stringToProcedure(js, karel.sb.toString());
//        final String name = karel.procNames.get(0);
//        return new Procedure[] {
//            new Procedure(procJs, this, name, name)
//        };
        return new Procedure[] { findProcedure(karel.procNames.get(0)) };
    }

    public List<Procedure> getProcedures() {
        Object[] blocks = list0(js);
        List<Procedure> arr = new ArrayList<>(blocks.length / 3);
        for (int i = 0; i < blocks.length; i += 3) {
            arr.add(new Procedure(blocks[i + 2], this, (String)blocks[i + 0], (String)blocks[i + 1]));
        }
        return arr;
    }

    public Procedure getSelectedProcedure() {
        Object[] jsProc = selected0(js);
        return jsProc == null ? null : new Procedure(jsProc[2], this, (String)jsProc[0], (String)jsProc[1]);
    }

    public Procedure findProcedure(String id) {
        for (Procedure p : getProcedures()) {
            if (id.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        for (Procedure p : getProcedures()) {
            if (p.rawJS() != null) {
                return false;
            }
        }
        return true;
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

    public void addSelectionChange(Runnable r) {
        selectChange = OnChange.add(selectChange, r);
    }

    void change(Object[] params) {
        if ("blocklySelectChange".equals(params[0])) {
            OnChange.fire(selectChange);
        }
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

    @JavaScriptBody(args = { "workspace", "thiz" }, wait4js = false, javacall = true, body =
        "workspace.listen(function(ev) {\n" +
        "  thiz.@cz.xelfi.karel.blockly.Workspace::change([Ljava/lang/Object;)([ev.type, ev.target]);\n" +
        "});\n"
    )
    private static native void listen0(Object workspace, Workspace thiz);

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

    @JavaScriptBody(args = { "workspace" }, body =
        "return workspace.selected();\n"
    )
    private static native Object[] selected0(Object workspace);

    @JavaScriptBody(args = { "workspace", "proc" }, body = "return workspace.procedureToString(proc);")
    static native String procedureToString(Object workspace, Object proc);

}
