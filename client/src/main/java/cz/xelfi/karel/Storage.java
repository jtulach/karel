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

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

/**
 *
 * @author Jaroslav Tulach
 */
public abstract class Storage {
    private static final Storage DEFAULT;
    static {
        Iterator<Storage> it = ServiceLoader.load(Storage.class).iterator();
        if (it.hasNext()) {
            DEFAULT = it.next();
        } else {
            DEFAULT = new Impl();
        }
    }
    
    public static Storage getDefault() {
        return DEFAULT;
    }
    
    public abstract void put(String key, String value);
    public abstract String get(String key, String defaultValue);
    public abstract void putInt(String key, int value);
    public abstract int getInt(String key, int defaultValue);

    private static class Impl extends Storage {
        private final Preferences node;
        public Impl() {
            node = Preferences.userNodeForPackage(Storage.class);
        }

        @Override
        public void put(String key, String value) {
            node.put(key, value);
        }

        @Override
        public String get(String key, String defaultValue) {
            return node.get(key, defaultValue);
        }

        @Override
        public void putInt(String key, int value) {
            node.putInt(key, value);
        }

        @Override
        public int getInt(String key, int defaultValue) {
            return node.getInt(key, defaultValue);
        }
    }
}
