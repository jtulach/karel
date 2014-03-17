
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
