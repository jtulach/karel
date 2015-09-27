/*
 *
 Karel blocks
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
            s += '<block type="karel_call"><field name="CALL">' + b[0] + '</field></block>\n';
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