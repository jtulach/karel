grammar Karel;

karel:
    (procedure)* EOF;

procedure:
    'PROCEDURE' name statements 'END';

name:
    ID;

statements:
    statement;

next:
    statement;

statement:
    kwhile | kif | kifelse | krepeat | kcall;

kwhile:
    'WHILE' condition statements 'END' next?;

kif:
    'IF' condition statements 'END' next?;

kifelse:
    'IF' condition statements 'ELSE' statements 'END' next?;

krepeat:
    'REPEAT' NUM 'TIMES' statements 'END' next?;

kcall:
    call next?;

call:
    ID;

condition:
    ('NOT')?
    ('WALL' | 'MARK' | 'NORTH' | 'EAST' | 'SOUTH' | 'WEST' );

NUM : [0-9]+;
ID : [A-Za-z0-9_\-]+ ;    // match lower-case identifiers
WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
