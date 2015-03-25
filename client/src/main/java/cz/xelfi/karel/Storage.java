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
