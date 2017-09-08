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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;

@JavaScriptResource("karel.js")
final class KarelBlockly {
    private static Object js;

    static Object getDefault() {
        if (js == null) {
            js = init0();
            try {
                loadMsgs(Locale.getDefault());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return js;
    }


    @JavaScriptBody(args = {}, body = "return Blockly['karel']")
    private static native Object init0();

    private static void loadMsgs(Locale l) throws IOException {
        String suffix = l.getLanguage();
        StringBuilder sb;
        try (Reader is = new InputStreamReader(findLocalizedMsgs(suffix), "UTF-8")) {
            sb = new StringBuilder();
            for (;;) {
                int ch = is.read();
                if (ch == -1) {
                    break;
                }
                sb.append((char) ch);
            }
        }
        registerMsg(sb.toString());
    }

    private static InputStream findLocalizedMsgs(String suffix) {
        InputStream is = KarelBlockly.class.getResourceAsStream("msg/json/" + suffix + ".json");
        if (is == null) {
            KarelBlockly.class.getResourceAsStream("msg/json/en.json");
        }
        return is;
    }

    @JavaScriptBody(args = { "json" }, body =
        "var obj = JSON.parse(json);\n" +
        "Blockly.Msg = obj;\n"
    )
    private native static void registerMsg(String json);
}
