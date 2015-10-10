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

import android.content.SharedPreferences;

final class AndroidStorage extends Storage {
    private final SharedPreferences prefs;

    public AndroidStorage(SharedPreferences aprefs) {
        prefs = aprefs;
    }

    @Override
    public void put(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    @Override
    public String get(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    @Override
    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }
}
