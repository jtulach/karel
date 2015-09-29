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
package cz.xelfi.karel.blockly;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BlocklyTest {
    private static BrwsrCtx CTX;

    public BlocklyTest() {
    }

    @BeforeClass
    public static void initializePresenter() throws InterruptedException {
        final CountDownLatch cdl = new CountDownLatch(1);
        final BrowserBuilder builder = BrowserBuilder.newBrowser().
            loadPage("test.html").
            loadFinished(new Runnable() {
                @Override
                public void run() {
                    CTX = BrwsrCtx.findDefault(BlocklyTest.class);
                    cdl.countDown();
                }
            });
        new Thread(new Runnable() {
            @Override
            public void run() {
                builder.showAndWait();
            }
        }, "Launcher").start();
        cdl.await();
        Assert.assertNotNull(CTX, "Context is ready");
    }

    @Test public void testWorkingWithWorkspace() throws Exception {
        doTest("doWorkingWithWorkspace");
    }
    
    private void doWorkingWithWorkspace() throws Exception {
        Workspace w = Workspace.create("any");
        assertEquals(w.getProcedures().size(), 0, "No top blocks yet");
    }

    private void doTest(String method) throws Exception {
        final Method m = this.getClass().getDeclaredMethod(method);
        m.setAccessible(true);
        final Exception[] arr = { null };
        final CountDownLatch cdl = new CountDownLatch(1);
        CTX.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    m.invoke(BlocklyTest.this);
                } catch (Exception ex) {
                    arr[0] = ex;
                } finally {
                    cdl.countDown();
                }
            }
        });
        cdl.await();
        if (arr[0] != null) {
            throw arr[0];
        }
    }

}
