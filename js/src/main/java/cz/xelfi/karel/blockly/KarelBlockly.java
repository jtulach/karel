
package cz.xelfi.karel.blockly;

import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;

@JavaScriptResource("js/blocks/karel.js")
final class KarelBlockly {
    @JavaScriptBody(args = {}, body = "", wait4js = false)
    static native void init0();
}
