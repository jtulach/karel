grammar Karel;

karel:
    (procedure)* EOF;

procedure:
    'PROCEDURE' ID statements 'END';

statements:
    (statement)*;

statement:
    kwhile | kif | kifelse | krepeat | kcall;

kwhile:
    'WHILE' condition statements 'END';

kif:
    'IF' condition statements 'END';

kifelse:
    'IF' condition statements 'ELSE' statements 'END';

krepeat:
    'REPEAT' NUM 'TIMES' statements 'END';

kcall:
    ID;

condition:
    ('NOT')?
    ('WALL' | 'MARK' | 'NORTH' | 'EAST' | 'SOUTH' | 'WEST' );

NUM : [0-9]+;
ID : [A-Za-z0-9_\-]+ ;    // match lower-case identifiers
WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
