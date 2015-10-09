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

final class OnChange implements Runnable {
    private final Runnable wrap;
    private final Runnable next;

    public OnChange(Runnable wrap, Runnable next) {
        this.wrap = wrap;
        this.next = next;
    }


    static Runnable add(Runnable prev, Runnable toAdd) {
        if (prev == null) {
            return toAdd;
        } else {
            return new OnChange(toAdd, prev);
        }
    }

    static void fire(Runnable chain) {
        for (;;) {
            if (chain == null) {
                break;
            }
            if (chain instanceof OnChange) {
                OnChange on = (OnChange) chain;
                on.wrap.run();
                chain = on.next;
            } else {
                ((Runnable)chain).run();
                break;
            }
        }
    }

    @Override
    public void run() {
        wrap.run();
    }
}
