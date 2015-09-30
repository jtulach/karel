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

import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className = "TaskInfo", properties = {
    @Property(name = "name", type = String.class),
    @Property(name = "url", type = String.class),
    @Property(name = "required", type = int.class),
    @Property(name = "awarded", type = int.class),
    @Property(name = "disabled", type = boolean.class),
})
class TaskModel {
    @Model(className = "TaskDescription", properties = {
        @Property(name = "name", type = String.class),
        @Property(name = "description", type = String.class),
        @Property(name = "command", type = String.class),
        @Property(name = "awarded", type = int.class),
        @Property(name = "tests", array = true, type = TaskTestCase.class)
    })
    static class DescriptionModel {
        static void reset(TaskDescription td, boolean clearState) {
            if (td == null) {
                return;
            }
            for (TaskTestCase c : td.getTests()) {
                TestCaseModel.reset(c, clearState, "");
            }
        }
        @Function
        static void showHide(TaskDescription td, TaskTestCase data) {
            for (TaskTestCase c : td.getTests()) {
                if (c != data) {
                    c.setShowing(null);
                }
            }
            if (data.getShowing() == null || "".equals(data.getShowing())) {
                data.setShowing("current");
            } else {
                data.setShowing(null);
            }
        }
        @Function static void reset(TaskDescription td) {
            td.setAwarded(0);
            for (TaskTestCase c : td.getTests()) {
                TestCaseModel.reset(c, true, null);
            }
        }
    }
    
    @Model(className = "TaskTestCase", properties = {
        @Property(name = "description", type = String.class),
        @Property(name = "start", type = Town.class),
        @Property(name = "current", type = Town.class),
        @Property(name = "end", type = Town.class),
        @Property(name = "state", type = String.class),
        @Property(name = "showing", type = String.class)
    })
    static class TestCaseModel {
        @Function static void reset(TaskTestCase c) {
            reset(c, true, "start");
        }
        
        @Function static void begin(TaskTestCase c) {
            c.setShowing("start");
        }
        
        @Function static void now(TaskTestCase c) {
            c.setShowing("current");
        }

        @Function static void finish(TaskTestCase c) {
            c.setShowing("end");
        }
        
        static boolean checkState(TaskTestCase c) {
            if (c.getCurrent() != null && c.getCurrent().equals(c.getEnd())) {
                c.setState("ok");
                c.setShowing(null);
                return true;
            } else {
                c.setState("fail");
                return false;
            }
        }
        
        static void reset(TaskTestCase c, boolean clearState, String showing) {
            Town cur = c.getCurrent();
            if (cur == null)  {
                cur = new Town();
            }
            TownModel.init(cur, 10, 10);
            TownModel.load(cur, c.getStart());
            c.setCurrent(cur);
            if (showing != null) {
                c.setShowing(showing);
            }
            if (clearState) {
                c.setState(null);
            }
        }
    }
}
