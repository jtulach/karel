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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import net.java.html.BrwsrCtx;
import net.java.html.boot.BrowserBuilder;
import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ParsingTest {
    private static BrwsrCtx CTX;

    @BeforeClass
    public static void initializePresenter() throws InterruptedException {
        final CountDownLatch cdl = new CountDownLatch(1);
        final BrowserBuilder builder = BrowserBuilder.newBrowser().
            loadPage("test.html").
            loadFinished(new Runnable() {
                @Override
                public void run() {
                    CTX = BrwsrCtx.findDefault(ParsingTest.class);
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

    @Test
    public void printOutCodeOfProcedure() throws Exception {
        String parsed = new Later<String>() {
            @Override
            String work() throws Exception {
                final Workspace w = Workspace.create("any");
                w.clear();

                w.loadXML(
                    "<xml xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                    + "  <block type=\"karel_funkce\" x=\"38\" y=\"45\">\n"
                    + "    <field name=\"NAME\">safe-step</field>\n"
                    + "    <statement name=\"IFTRUE\">\n"
                    + "      <block type=\"karel_if\">\n"
                    + "        <field name=\"NEG\">FALSE</field>\n"
                    + "        <field name=\"COND\">WALL</field>\n"
                    + "        <statement name=\"IFTRUE\">\n"
                    + "          <block type=\"karel_call\">\n"
                    + "            <field name=\"CALL\">STEP</field>\n"
                    + "          </block>\n"
                    + "        </statement>\n"
                    + "      </block>\n"
                    + "    </statement>\n"
                    + "  </block>\n"
                    + "  <block type=\"karel_funkce\" x=\"259\" y=\"44\">\n"
                    + "    <field name=\"NAME\">ten-safe-steps</field>\n"
                    + "    <statement name=\"IFTRUE\">\n"
                    + "      <block type=\"karel_repeat\">\n"
                    + "        <field name=\"N\">10</field>\n"
                    + "        <statement name=\"IFTRUE\">\n"
                    + "          <block type=\"karel_call\">\n"
                    + "            <field name=\"CALL\">safe-step</field>\n"
                    + "          </block>\n"
                    + "        </statement>\n"
                    + "      </block>\n"
                    + "    </statement>\n"
                    + "  </block>\n"
                    + "</xml>"
                );

                List<Procedure> arr = BlocklyTest.filterProcedures(w);
                assertEquals(arr.size(), 2, "Two procs: " + arr);

                Procedure tenSafeSteps = w.findProcedure("ten-safe-steps");
                final Procedure safeStep = w.findProcedure("safe-step");

                assertNotNull(tenSafeSteps);
                assertNotNull(safeStep);

                return safeStep.getCode();
            }
        }.get();

        assertTrue(parsed.contains("PROCEDURE safe-step"), parsed);
        assertTrue(parsed.contains("IF NOT WALL"), parsed);
        assertTrue(parsed.contains("STEP"), parsed);
    }

    private static abstract class Later<R> implements Runnable {
        private static final LinkedList<Later> PENDING = new LinkedList<>();
        private boolean executed;
        private R result;
        private Exception ex;

        protected Later() {
            PENDING.add(this);
        }

        abstract R work() throws Exception;

        @Override
        public final void run() {
            if (executed) {
                return;
            }
            try {
                result = work();
            } catch (Exception ex) {
                this.ex = ex;
            } finally {
                synchronized (this) {
                    executed = true;
                    notifyAll();
                }
            }
        }

        public final R get() throws Exception {
            for (Later run : PENDING.toArray(new Later[0])) {
                PENDING.clear();
                CTX.execute(run);
            }
            synchronized (this) {
                while (!executed) {
                    wait();
                }
            }
            if (ex != null) {
                throw ex;
            }
            return result;
        }
    }
}
