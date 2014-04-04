/*
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
[{
    "description" : "Karlovi patří celé město, ale jeho skutečným domovem " +
            "je levý dolní roh města, v němž je otočen směrem na východ. " +
            "Nauč Karla najít cestu domů z jakéhokoli místa ve městě (bez vnitřních zdí).",
    "command" : "domů",
    "tests" : [
        {
            "description" : "Otoč se a jdi domů.",
            "start" : {"rows":[null,null,null,null,null,null,null,{"columns":[null,null,null,{"robot":4,"marks":0},null,null,null,null,null,null]},null,null]},
            "end" : {"rows":[null,null,null,null,null,null,null,null,null,{"columns":[{"robot":1,"marks":0},null,null,,null,null,null,null,null,null]}]}
        },
        {
            "description" : "Značky nech na původním místě.",
            "start" : {"rows":[null,null,
                {"columns":[null,null,null,null,null,null,null,{"robot":3,"marks":1},null,null]},
                null,null,null,null,null,null,null,
            ]},
            "end" : {"rows":[null,null,
                    {"columns":[null,null,null,null,null,null,null,{"robot":0,"marks":1},null,null]},
                    null,null,null,null,null,null,{"columns":[{"robot":1,"marks":0},null,null,,null,null,null,null,null,null]}]}
        },
        {
            "description" : "A odsuď trefíš?",
            "start" : {"rows":[
                {"columns":[null,null,null,null,null,null,null,null,null,{"robot":2,"marks":0}]},
                null,null,null,null,null,null,null,null,null,
            ]},
            "end" : {"rows":[null,null,null,null,null,null,null,null,null,{"columns":[{"robot":1,"marks":0},null,null,,null,null,null,null,null,null]}]}
        }
    ]
}]
