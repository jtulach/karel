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
    "description" : "Robot Karel se umí udělat jeden krok. " + 
        "Ale také umí skládat příkazy dohromady. " + 
        "Prosím nauč jej nový příkaz, kterým udělá dva kroky.",
    "command" : "dva-kroky",
    "tests" : [
        {
            "description" : "Kouká na sever. Pokročí.",
            "start" : {"rows":[null,null,null,null,null,null,null,{"columns":[null,null,null,{"robot":4,"marks":0},null,null,null,null,null,null]},null,null]},
            "end" : {"rows":[null,null,null,null,null,{"columns":[null,null,null,{"robot":4,"marks":0},null,null,null,null,null,null]},null,null,null,null]}
        },
        {
            "description" : "Je-li blízko zdi, narazí.",
            "start" : {"rows":[null,null,null,null,null,null,null,null,{"columns":[null,{"robot":3,"marks":0},null,null,null,null,null,null,null,null]},null]},
            "end" : {"error":1,"rows":[null,null,null,null,null,null,null,null,{"columns":[{"robot":3,"marks":0},null,null,null,null,null,null,null,null,null]},null]}
        },
        {
            "description" : "Nevadí, že je u zdi a jsou tam značky.",
            "start" : {"rows":[null,null,null,null,null,null,null,null,null,{"columns":[null,{"robot":1,"marks":3},null,null,null,null,null,null,null,null]}]},
            "end" : {"rows":[null,null,null,null,null,null,null,null,null,{"columns":[null,{"robot":0,"marks":3},null,{"robot":1,"marks":0},null,null,null,null,null,null]}]}
        }
    ]
}]

