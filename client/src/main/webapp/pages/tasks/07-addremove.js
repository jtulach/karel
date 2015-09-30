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
    "description" : "Karel umí pokládat na políčka ve městě značky a otestovat " + 
        "zda-li na jeho současném políčku již značka je. Pojďme jej tedy nyní " +
        "naučit měnit značky: Je-li na jeho políčku značka, ať ji zvedne; " +
        "je-li políčko bez značky, ať jednu položí.",
    "command" : "vyměň-značku",
    "tests" : [
        {
            "description" : "Polož značku, když je políčko prázdné.",
            "start" : {"rows":[null,null,null,null,null,null,null,{"columns":[null,null,null,{"robot":1,"marks":0},null,null,null,null,null,null]},null,null]},
            "end" : {"rows":[null,null,null,null,null,null,null,{"columns":[null,null,null,{"robot":1,"marks":1},null,null,null,null,null,null]},null,null]}
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
            "description" : "Uber jednu, je-li jich tam více.",
            "start" : {"rows":[null,null,null,null,
                {"columns":[null,null,null,{"robot":3,"marks":4},null,null,null,null,null,null]},
                null,null,null,null,null
            ]},
            "end" : {"rows":[null,null,null,null,
                {"columns":[null,null,null,{"robot":3,"marks":3},null,null,null,null,null,null]},
                null,null,null,null,null
            ]}
        }
    ]
}]
