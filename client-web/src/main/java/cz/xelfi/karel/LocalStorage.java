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
