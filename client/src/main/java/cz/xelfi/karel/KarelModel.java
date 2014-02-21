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

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

/** Model annotation generates class Data with 
 * one message property, boolean property and read only words property
 */
@Model(className = "Karel", properties = {
    @Property(name = "message", type = String.class),
    @Property(name = "town", type = Town.class),
    @Property(name = "commands", type = Command.class, array = true),
    @Property(name = "source", type = String.class)
})
final class KarelModel {
    @Model(className = "Command", properties = {
        @Property(name = "name", type = String.class)
    })
    final static class CommandModel {
    }
    
    @Function static void invoke(Karel m, Command data) {
        if (data.getName().equalsIgnoreCase("krok")) {
            m.getTown().step();
        }
        if (data.getName().equalsIgnoreCase("vlevo-vbok")) {
            m.getTown().left();
        }
    }
    
    @Function static void compile(Karel m) {
        m.getCommands().add(new Command("novy"));
    }
}
