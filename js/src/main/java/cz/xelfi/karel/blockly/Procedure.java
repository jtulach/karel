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

public final class Procedure {
    private final Object js;
    private final Workspace ws;
    private final String id;
    private final String name;
    
    Procedure(Object js, Workspace ws, String name, String id) {
        this.js = js;
        this.ws = ws;
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    /** Universtal, non-tranlatable name of the procedure.
     * @return values like <code>STEP</code>, <code>LEFT</code>, etc.
     */
    public String getId() {
        return id;
    }

    public Execution prepareExecution(Execution.Environment env) {
        return new Execution(env, ws.getProcedures(), js == null ? id : js);
    }

    Object rawJS() {
        return js;
    }

    public void select() {
        if (js != null) {
            Workspace.select(js);
        }
    }

    public void setCollapsed(boolean b) {
        if (js != null) {
            Workspace.setCollapsed(js, b);
        }
    }

    @Override
    public String toString() {
        return "Procedure[" + id + "]";
    }

    /**
     * Converts the procedure into textual representation. The code is
     * written in English mimics the standard Karel's way of writing its
     * code.
     * 
     * @return human readable code of the procedure
     */
    public String getCode() {
        return Workspace.procedureToString(ws.rawJS(), js);
    }

    @Override
    public int hashCode() {
        return id.hashCode() * 7 + 5432543;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Procedure) {
            Procedure p = (Procedure) obj;
            return js.equals(p.js);
        }
        return false;
    }
}
