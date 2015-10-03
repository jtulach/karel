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

/* global Blockly */

(function() {
function injectKarel(id) {
    var workspace;

    var negDropdown = [
        ["je", "TRUE"], ["není", "FALSE"]
    ];

    var condDropdown = [
        ["zeď", "WALL"],
        ["značka", "STAMP"],
        ["sever", "NORTH"],
        ["jih", "SOUTH"],
        ["západ", "WEST"],
        ["východ", "EAST"]
    ];

    var ifColor = 210;
    
    function procedures() {
        var arr = [];
        arr.push(['krok', 'STEP']);
        arr.push(['vlevo-vbok', 'LEFT']);
        arr.push(['polož', 'PUT']);
        arr.push(['zvedni', 'TAKE']);
        if (workspace) {
            workspace.getTopBlocks().forEach(function (b) {
                var n = b.getFieldValue("NAME");
                if (n) {
                    arr.push([ n, n ]);
                }
            });
        }
        return arr;
    }

    function toolbox(modify) {
        var s = '<xml>\n' +
            '<block type="karel_funkce"></block>\n' +
            '<block type="karel_if"></block>\n' +
            '<block type="karel_if_else"></block>\n' +
            '<block type="karel_while"></block>\n' +
            '<block type="karel_repeat"></block>\n';

        procedures().forEach(function (b) {
            s += '<block type="karel_call"><field name="CALL">' + b[1] + '</field></block>\n';
        });

        s += '</xml>\n';

        if (modify) {
            workspace.updateToolbox(s);
        }
        return s;
    };

Blockly.Blocks['karel_funkce'] = {
  init: function() {
    this.appendDummyInput()
        .appendField(new Blockly.FieldTextInput("nový příkaz"), "NAME");
    this.appendStatementInput("IFTRUE");
    this.appendDummyInput()
        .appendField("konec");
    this.setColour(65);
    this.setTooltip('');
    this.setHelpUrl('help.html');
  }
};

Blockly.Blocks['karel_if'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Když")
        .appendField(new Blockly.FieldDropdown(negDropdown), "NEG")
        .appendField(new Blockly.FieldDropdown(condDropdown), "COND");
    this.appendStatementInput("IFTRUE");
    this.appendDummyInput()
        .appendField("konec");
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(ifColor);
    this.setTooltip('');
    this.setHelpUrl('help.html');
  }
};

Blockly.Blocks['karel_if_else'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Když")
        .appendField(new Blockly.FieldDropdown(negDropdown), "NEG")
        .appendField(new Blockly.FieldDropdown(condDropdown), "COND");
    this.appendStatementInput("IFTRUE");
    this.appendDummyInput()
        .appendField("jinak");
    this.appendStatementInput("IFFALSE");
    this.appendDummyInput()
        .appendField("konec");
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(ifColor);
    this.setTooltip('');
    this.setHelpUrl('help.html');
  }
};

Blockly.Blocks['karel_while'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("dokud")
        .appendField(new Blockly.FieldDropdown(negDropdown), "NEG")
        .appendField(new Blockly.FieldDropdown(condDropdown), "COND");
    this.appendStatementInput("IFTRUE");
    this.appendDummyInput()
        .appendField("konec");
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(135);
    this.setTooltip('');
    this.setHelpUrl('help.html');
  }
};

Blockly.Blocks['karel_repeat'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("opakuj")
        .appendField(new Blockly.FieldTextInput("2"), "N")
        .appendField("krát");
    this.appendStatementInput("IFTRUE");
    this.appendDummyInput()
        .appendField("konec");
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(135);
    this.setTooltip('');
    this.setHelpUrl('help.html');
  }
};

Blockly.Blocks['karel_call'] = {
  init: function() {
    this.appendDummyInput()
        .appendField(new Blockly.FieldDropdown(procedures), "CALL");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(330);
    this.setTooltip('');
    this.setHelpUrl('help.html');
  }
};
    workspace = Blockly.inject(id, {
        'media': 'media/',
        'toolbox': toolbox()
    });
    workspace.addChangeListener(toolbox);

    return workspace;
}

Blockly.karel = {
  'inject' : injectKarel
};
})();