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

(function() {
function injectKarel(id, keys, loc) {
    var workspace;

    var l10n = {};
    for (var i = 0; i < keys.length; i++) {
        l10n[keys[i].toString()] = loc[i].toString();
    }

    var negDropdown = [
        [l10n["TRUE"], "TRUE"], [l10n["FALSE"], "FALSE"]
    ];

    var condDropdown = [
        [l10n["WALL"], "WALL"],
        [l10n["MARK"], "MARK"],
        [l10n["NORTH"], "NORTH"],
        [l10n["SOUTH"], "SOUTH"],
        [l10n["WEST"], "WEST"],
        [l10n["EAST"], "EAST"]
    ];

    var ifColor = 210;
    
    function procedures() {
        var arr = [];
        arr.push([l10n["STEP"], 'STEP', null]);
        arr.push([l10n["LEFT"], 'LEFT', null]);
        arr.push([l10n["PUT"], 'PUT', null]);
        arr.push([l10n["TAKE"], 'TAKE', null]);
        if (workspace) {
            workspace.getTopBlocks().forEach(function (b) {
                var n = b.getFieldValue("NAME");
                if (n) {
                    arr.push([ n, n, b ]);
                }
            });
        }
        return arr;
    }

    var onSelectListener = null;

    function toolbox(modify) {
        if (onSelectListener && modify && 'selected' === modify.element) {
            onSelectListener(['blocklySelectChange', modify.oldValue, modify.newValue]);
            return;
        }
        var s = '<xml>\n' +
            '<block type="karel_funkce"></block>\n' +
            '<block type="karel_if"></block>\n' +
            '<block type="karel_if_else"></block>\n' +
            '<block type="karel_while"></block>\n' +
            '<block type="karel_repeat"></block>\n' +
            '<block type="karel_call"><field name="CALL">' + l10n["NOCMD"] + '</field></block>\n';

        s += '</xml>\n';

        if (modify) {
            workspace.updateToolbox(s);
        }
        return s;
    };    
var Blockly = window['Blockly'];
if (Object.defineProperty) {
    var currentlySelected = null;
    Object.defineProperty(Blockly, 'selected', {
        get: function () {
            return currentlySelected;
        },

        set: function (value) {
            var old = currentlySelected;
            currentlySelected = value;
            if (onSelectListener) {
                onSelectListener(['blocklySelectChange', old, value]);
            }
        }
    });
}
Blockly.Blocks['karel_funkce'] = {
  init: function() {
    this.appendDummyInput()
        .appendField(new Blockly.FieldTextInput(l10n["NEWCMD"]), "NAME");
    this.appendStatementInput("IFTRUE");
    this.appendDummyInput()
        .appendField(l10n["END"]);
    this.setColour(65);
    this.setTooltip('');
    this.setHelpUrl('help.html');
  }
};

Blockly.Blocks['karel_if'] = {
  init: function() {
    this.appendDummyInput()
        .appendField(l10n["IF"])
        .appendField(new Blockly.FieldDropdown(negDropdown), "NEG")
        .appendField(new Blockly.FieldDropdown(condDropdown), "COND");
    this.appendStatementInput("IFTRUE");
    this.appendDummyInput()
        .appendField(l10n["END"]);
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
        .appendField(l10n["IF"])
        .appendField(new Blockly.FieldDropdown(negDropdown), "NEG")
        .appendField(new Blockly.FieldDropdown(condDropdown), "COND");
    this.appendStatementInput("IFTRUE");
    this.appendDummyInput()
        .appendField(l10n["ELSE"]);
    this.appendStatementInput("IFFALSE");
    this.appendDummyInput()
        .appendField(l10n["END"]);
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
        .appendField(l10n["WHILE"])
        .appendField(new Blockly.FieldDropdown(negDropdown), "NEG")
        .appendField(new Blockly.FieldDropdown(condDropdown), "COND");
    this.appendStatementInput("IFTRUE");
    this.appendDummyInput()
        .appendField(l10n["END"]);
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
        .appendField(l10n["REPEAT"])
        .appendField(new Blockly.FieldTextInput("2"), "N")
        .appendField(l10n["TIMES"]);
    this.appendStatementInput("IFTRUE");
    this.appendDummyInput()
        .appendField(l10n["END"]);
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
        .appendField("     ");
    this.appendDummyInput()
        .appendField(new Blockly.FieldDropdown(procedures), "CALL");
    this.appendDummyInput()
        .setAlign(Blockly.ALIGN_RIGHT)
        .appendField("     ");
    this.setInputsInline(true);
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
    workspace.options.collapse = true;

    function addListeners(callback) {
        onSelectListener = callback;
    }

    function loadXml(xml) {
        Blockly.Xml.domToWorkspace(Blockly.Xml.textToDom(xml), workspace);
    }
    
    function toXml() {
        return Blockly.Xml.domToPrettyText(Blockly.Xml.workspaceToDom(workspace));
    }

    function newBlock(type, commandName) {
        var b = workspace.newBlock(type);
        b.initSvg();
        b.setFieldValue(commandName, 'NAME');
        b.render();
        return b;
    }

    function clear() {
        return workspace.clear();
    }

    function flatProcedures() {
        var arr = procedures();
        var res = [];
        for (var i = 0; i < arr.length; i++) {
            var nameIdRef = arr[i];
            res.push(nameIdRef[0]);
            res.push(nameIdRef[1]);
            res.push(nameIdRef[2]);
        }
        return res;
    }

    function selectedProcedure() {
        var element = Blockly.selected;
        if (!element) {
            return null;
        }
        for (;;) {
            var parent = element.getSurroundParent();
            if (!parent) {
                break;
            }
            element = parent;
        }
        var ret = null;
        workspace.getTopBlocks().forEach(function (b) {
            if (element !== b) {
                return;
            }
            var n = b.getFieldValue("NAME");
            if (n) {
                ret = [ n, n, b ];
            }
        });
        return ret;
    }

    function dumpCondition(command, proc) {
        var str = command;
        if (proc.getFieldValue("NEG") === 'FALSE') {
            str += " NOT";
        }
        str += ' ' + proc.getFieldValue("COND") + '\n';
        return str;
    }

    function dump(proc, indent) {
        if (!indent) {
            indent = "";
        }
        var str = indent;
        var middle = '';
        switch (proc.type) {
            case 'karel_funkce':
                str += "PROCEDURE " + proc.getFieldValue("NAME") + '\n';
                break;
            case 'karel_while':
                str += dumpCondition('WHILE', proc);
                break;
            case 'karel_if_else':
                middle = indent + 'ELSE\n';
                // fall through
            case 'karel_if':
                str += dumpCondition('IF', proc);
                break;
            case 'karel_repeat':
                str += 'REPEAT ' + proc.getFieldValue("N") + " TIMES\n";
                break;
            case 'karel_call':
                str += proc.getFieldValue("CALL") + "\n";
                break;
            default:
                str += proc.type + ' call ' + proc.getFieldValue("CALL") + ' name ' + proc.getFieldValue("NAME") + '\n';
                break;
        }
        var arr = proc.getChildren();
        var cnt = 1;
        for (var i = 0; i < arr.length; i++) {
            var ch = arr[i];
            if (ch.getSurroundParent() === proc) {
                if (cnt == 0) {
                    str += middle;
                    middle = '';
                }
                str += dump(ch, "  " + indent);
                cnt--;
            }
            for (;;) {
                ch = ch.getNextBlock();
                if (ch === null) {
                    break;
                }
                if (ch.getSurroundParent() === proc) {
                    str += dump(ch, "  " + indent);
                    if (ch.getNextBlock() === null) {
                        str += middle;
                        middle = '';
                    }
                    cnt--;
                }
            }
        }
        switch (proc.type) {
            case 'karel_funkce':
            case 'karel_while':
            case 'karel_repeat':
            case 'karel_if':
            case 'karel_if_else':
                str += indent + "END\n";
                break;
        }
        return str;
    }

    return {
        'clear' : clear,
        'loadXml' : loadXml,
        'toXml' : toXml,
        'procedures': flatProcedures,
        'newBlock': newBlock,
        'listen': addListeners,
        'selected': selectedProcedure,
        'procedureToString': dump
    };
}
Blockly['karel'] = injectKarel;
})();