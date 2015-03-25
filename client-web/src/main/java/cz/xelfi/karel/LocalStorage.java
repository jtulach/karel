package cz.xelfi.karel;

import net.java.html.js.JavaScriptBody;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service = Storage.class)
public class LocalStorage extends Storage {
    @JavaScriptBody(args = { "key" }, body = 
        "var v = localStorage[key];\n"
      + "return v ? v : null;"
    )
    private static native Object get(String key);

    @Override
    @JavaScriptBody(args = { "key", "value" }, body = 
        "localStorage[key] = value;"
    )
    public native void put(String key, String value);

    @Override
    public String get(String key, String defaultValue) {
        Object v = get(key);
        return v instanceof String ? (String)v : defaultValue;
    }

    @JavaScriptBody(args = { "key", "value" }, body = 
        "localStorage[key] = value;"
    )
    @Override
    public native void putInt(String key, int value);

    @Override
    public int getInt(String key, int defaultValue) {
        Object v = get(key);
        return v instanceof Number ? ((Number)v).intValue() : defaultValue;
    }
}
