/*
 *
 Karel blocks
 */

(function() {
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

Blockly.Blocks['karel_funkce'] = {
  init: function() {
    this.appendDummyInput()
        .appendField(new Blockly.FieldTextInput("nový příkaz"), "NAME");
    this.appendStatementInput("NAME");
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

Blockly.Blocks['karel_vlevo'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("vlevo-vbok");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(330);
    this.setTooltip('');
    this.setHelpUrl('help.html');
  }
};
Blockly.Blocks['karel_krok'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("krok");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setColour(330);
    this.setTooltip('');
    this.setHelpUrl('help.html');
  }
};

})();