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

import java.util.List;
import net.java.html.json.Model;
import net.java.html.json.OnReceive;
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
}
