[{
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
    "description" : "Každý úkol je popsán slovně a k tomu je doplněn i " + 
        "několika názornými situacemi. V každé situaci je znázorněna výchozí  " + 
        "pozice a také cílová pozice. Je třeba vybrat ten Karlův příkaz, který " +
        "jej ve všech situacích dostane z výchozí pozice do cílové. V tomto " +
        "úkolu je Karlových cílem udělat krok, což je zároveň jeden ze základních " +
        "příkazů, který Karel umí. Zvol jej a ověř, že ve všech situacích Karla " +
        "posune do cílové pozice.",
    "tests" : [
        {
            "description" : "Udělá krok na jih.",
            "start" : {"rows":[null,null,null,null,null,null,null,{"columns":[null,null,null,{"robot":2,"marks":0},null,null,null,null,null,null]},null,null]},
            "end" : {"rows":[null,null,null,null,null,null,null,null,{"columns":[null,null,null,{"robot":2,"marks":0},null,null,null,null,null,null]},null]}
        },
        {
            "description" : "Je-li na západ, udělá krok na západ. Je jedno, že pod ním je značka.",
            "start" : {"rows":[null,null,null,null,null,null,null,null,
                    {"columns":[null,{"robot":3,"marks":2},null,null,null,null,null,null,null,null]},
                    null
                ]},
            "end" : {"rows":[null,null,null,null,null,null,null,null,
                    {"columns":[{"robot":3,"marks":0},{"robot":0,"marks":2},null,null,null,null,null,null,null,null]},
                    null
                ]}
        },
        {
            "description" : "Je-li u zdi, narazí.",
            "start" : {"rows":[null,null,null,null,null,null,null,null,null,{"columns":[{"robot":3,"marks":0},null,null,null,null,null,null,null,null,null]}]},
            "end" : {"error": 1, "rows":[null,null,null,null,null,null,null,null,null,{"columns":[{"robot":3,"marks":0},null,null,null,null,null,null,null,null,null]}]}
        }
    ]
}]

