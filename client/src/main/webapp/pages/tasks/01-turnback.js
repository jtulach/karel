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
    "description" : "Další základní příkaz, který robot Karel umí, je otočit se doleva. " + 
        "Avšak nejdůležitější je, že se Karel umí učit nové příkazy a skládat je dohromady. " + 
        "Prosím nauč jej otočit se čelem vzad. Vyber si 'definuj čelem-vzad' " +
        "a v editoru vlož mezi 'čelem-vzad' a 'konec' příkazy, které se mají vykonat, " +
        "vyvolá-li někdo 'čelem-vzad'. Pak zadej 'Přeložit' a spusť nově vytvořený " +
        "příkaz. Je-li správně nadefinován, vyřeší všechny tři připravené situace."
        ,
    "command" : "čelem-vzad",
    "tests" : [
        {
            "description" : "Kouká na sever. Otočí se na jih.",
            "start" : {"rows":[null,null,null,null,null,null,null,{"columns":[null,null,null,{"robot":4,"marks":0},null,null,null,null,null,null]},null,null]},
            "end" : {"rows":[null,null,null,null,null,null,null,{"columns":[null,null,null,{"robot":2,"marks":0},null,null,null,null,null,null]},null,null]}
        },
        {
            "description" : "Je-li na západ, bude na východ.",
            "start" : {"rows":[null,null,null,null,null,null,null,null,{"columns":[null,{"robot":3,"marks":0},null,null,null,null,null,null,null,null]},null]},
            "end" : {"rows":[null,null,null,null,null,null,null,null,{"columns":[null,{"robot":1,"marks":0},null,null,null,null,null,null,null,null]},null]}
        },
        {
            "description" : "Nevadí, že je u zdi a jsou tam značky.",
            "start" : {"rows":[null,null,null,null,null,null,null,null,null,{"columns":[null,{"robot":2,"marks":3},null,null,null,null,null,null,null,null]}]},
            "end" : {"rows":[null,null,null,null,null,null,null,null,null,{"columns":[null,{"robot":4,"marks":3},null,null,null,null,null,null,null,null]}]}
        }
    ]
}]

