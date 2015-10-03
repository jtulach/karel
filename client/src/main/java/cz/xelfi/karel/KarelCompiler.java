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

import cz.xelfi.karel.blockly.Execution;
import cz.xelfi.karel.blockly.Procedure;

final class KarelCompiler implements Execution.Environment {
    final String name;
    final Town town;
    final Execution exec;

    public KarelCompiler(String name, Town town, Procedure procedure) {
        this.name = name;
        this.town = town;
        this.exec = procedure.prepareExecution(this);
    }


    static KarelCompiler execute(Town current, Procedure procedure, String name) {
        return new KarelCompiler(name, current, procedure);
    }

    @Override
    public boolean isCondition(Execution.Condition c) {
        return TownModel.isCondition(town, c);
    }

    @Override
    public void left() {
        town.left();
    }

    @Override
    public boolean step() {
        town.step();
        return town.getError() == 0;
    }

    @Override
    public boolean put() {
        town.put();
        return town.getError() == 0;
    }

    @Override
    public boolean pick() {
        town.take();
        return town.getError() == 0;
    }

}
