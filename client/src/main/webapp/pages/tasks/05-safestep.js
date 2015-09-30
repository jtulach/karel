/*
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
[{
    "description" : "Pokud Karel stojí u zdi, ať nedělá nic. Pokud je ode zdi " + 
        "vzdálen, ať udělá jeden krok. Využij příkaz 'když'.",
    "command" : "opatrný-krok",
    "tests" : [
        {
            "description" : "Krok na sever.",
            "start" : {"rows":[null,null,null,null,null,null,null,{"columns":[null,null,null,{"robot":4,"marks":0},null,null,null,null,null,null]},null,null]},
            "end" : {"rows":[null,null,null,null,null,null,{"columns":[null,null,null,{"robot":4,"marks":0},null,null,null,null,null,null,null]},null,null]},
        },
        {
            "description" : "Nedělej nic, ať nenarazíš.",
            "start" : {"rows":[null,null,null,null,null,null,null,{"columns":[{"robot":3,"marks":0},null,null,null,null,null,null,null,null,null]},null,null]},
            "end" : {"rows":[null,null,null,null,null,null,null,{"columns":[{"robot":3,"marks":0},null,null,null,null,null,null,null,null,null]},null,null]},
        }
    ]
}]
