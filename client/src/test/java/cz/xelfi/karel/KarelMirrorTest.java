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
package cz.xelfi.karel;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import static org.testng.Assert.*;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class KarelMirrorTest {
    private static final CountDownLatch INIT = new CountDownLatch(1);
    private static Throwable T;
    private static BrwsrCtx CTX;
    
    @BeforeClass public static void initMirror() throws Throwable {
        final BrowserBuilder bb = BrowserBuilder.newBrowser().
            loadClass(KarelMirrorTest.class).
            loadPage("pages/index.html").
            invoke("ready");
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    bb.showAndWait();
                } catch (Throwable t) {
                    T = t;
                    INIT.countDown();
                }
            }
        });
        INIT.await();
        if (T != null) {
            SkipException se = new SkipException("Problems initializing the mirror!");
            se.initCause(T);
            throw se;
        }
        assertNotNull(CTX, "Not null");
    }
    
    public static void ready(String... args) throws Exception {
        CTX = BrwsrCtx.findDefault(KarelMirrorTest.class);
        INIT.countDown();
    }
    
    @Test public void firstTest() throws Throwable {
        Object ret = execJS(new Callable<Object>() { 
            @Override
            public Object call() throws Exception {
                KarelMirror.newLine("unknown");
                return null;
            }
        });
        assertNull(ret);
    }
    
    private static <T> T execJS(final Callable<T> c) throws Throwable {
        class R implements Runnable {
            CountDownLatch l = new CountDownLatch(1);
            T ret;
            Throwable ex;
            
            @Override
            public void run() {
                try {
                    ret = c.call();
                } catch (Throwable t) {
                    ex = t;
                } finally {
                    l.countDown();
                }
            }
        }
        
        R run = new R();
        CTX.execute(run);
        run.l.await();
        if (run.ex != null) {
            throw run.ex;
        }
        return run.ret;
    }
}
