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
    "description" : "Karel umí značky na políčka pokládat a také je zvedat. " +
            "Nauč jej vysbírat všechny značky na políčku na němž stojí."
    ,
    "command" : "seber-značky",
    "tests" : [
        {
            "description" : "Nedělej nic, když je políčko prázdné.",
            "start" : {"rows":[null,null,null,null,null,null,null,{"columns":[null,null,null,{"robot":1,"marks":0},null,null,null,null,null,null]},null,null]},
            "end" : {"rows":[null,null,null,null,null,null,null,{"columns":[null,null,null,{"robot":1,"marks":0},null,null,null,null,null,null]},null,null]}
        },
        {
            "description" : "Zvedni značku, je-li tam jedna.",
            "start" : {"rows":[null,null,null,null,null,null,null,
                {"columns":[null,null,null,{"robot":3,"marks":1},null,null,null,null,null,null]},
                null,null
            ]},
            "end" : {"rows":[null,null,null,null,null,null,null,
                {"columns":[null,null,null,{"robot":3,"marks":0},null,null,null,null,null,null]},
                null,null
            ]}
        },
        {
            "description" : "Uber všechny, je-li jich tam více.",
            "start" : {"rows":[null,null,null,null,
                {"columns":[null,null,null,{"robot":3,"marks":4},null,null,null,null,null,null]},
                null,null,null,null,null
            ]},
            "end" : {"rows":[null,null,null,null,
                {"columns":[null,null,null,{"robot":3,"marks":0},null,null,null,null,null,null]},
                null,null,null,null,null
            ]}
        }
    ]
}]
