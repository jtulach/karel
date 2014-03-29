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
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className = "TaskInfo", properties = {
    @Property(name = "name", type = String.class),
    @Property(name = "url", type = String.class)
})
class TaskModel {
    @Model(className = "TaskDescription", properties = {
        @Property(name = "name", type = String.class),
        @Property(name = "description", type = String.class),
        @Property(name = "tests", array = true, type = TaskTestCase.class)
    })
    static class DescriptionModel {
    }
    
    @Model(className = "TaskTestCase", properties = {
        @Property(name = "description", type = String.class),
        @Property(name = "start", type = Town.class),
        @Property(name = "current", type = Town.class),
        @Property(name = "end", type = Town.class),
        @Property(name = "state", type = String.class)
    })
    static class TestCaseModel {
        static void checkState(TaskTestCase c) {
            if (c.getCurrent() != null && c.getCurrent().equals(c.getEnd())) {
                c.setState("ok");
            } else {
                c.setState("fail");
            }
        }
        
        static void reset(TaskTestCase c) {
            Town cur = c.getCurrent();
            if (cur == null)  {
                cur = new Town();
            }
            TownModel.init(cur, 10, 10);
            TownModel.load(cur, c.getStart());
            c.setCurrent(cur);
            c.setState(null);
        }
    }
}
