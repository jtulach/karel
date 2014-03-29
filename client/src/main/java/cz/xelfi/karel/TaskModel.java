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

import net.java.html.json.Function;
import net.java.html.json.Model;
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
        static void reset(TaskDescription td, boolean clearState) {
            if (td == null) {
                return;
            }
            for (TaskTestCase c : td.getTests()) {
                TestCaseModel.reset(c, clearState);
            }
        }
    }
    
    @Model(className = "TaskTestCase", properties = {
        @Property(name = "description", type = String.class),
        @Property(name = "start", type = Town.class),
        @Property(name = "current", type = Town.class),
        @Property(name = "end", type = Town.class),
        @Property(name = "state", type = String.class),
        @Property(name = "showing", type = boolean.class)
    })
    static class TestCaseModel {
        @Function static void reset(TaskTestCase c) {
            reset(c, true);
        }
        
        @Function static void showHide(TaskTestCase c) {
            c.setShowing(!c.isShowing());
        }
        
        static void checkState(TaskTestCase c) {
            if (c.getCurrent() != null && c.getCurrent().equals(c.getEnd())) {
                c.setState("ok");
                c.setShowing(false);
            } else {
                c.setState("fail");
            }
        }
        
        static void reset(TaskTestCase c, boolean clearState) {
            Town cur = c.getCurrent();
            if (cur == null)  {
                cur = new Town();
            }
            TownModel.init(cur, 10, 10);
            TownModel.load(cur, c.getStart());
            c.setCurrent(cur);
            if (!"ok".equals(c.getState())) {
                c.setShowing(true);
            }
            if (clearState) {
                c.setState(null);
            }
        }
    }
}
