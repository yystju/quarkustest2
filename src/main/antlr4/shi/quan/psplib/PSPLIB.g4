grammar PSPLIB;

model :	(line NEWLINE)* ;

line : comment | property | resource | projectInformation | precedenceRelations | requestsDurations | resourceAvailableilities | header | row | others;

comment : ('*' '*'+) | ('-' '-'*) ;

property : key WS* ':' WS* value;

key : (WS* '-' WS*)* TEXT (WS TEXT)* ;

value : (TEXT | NUMBER) (WS (TEXT | NUMBER))* ;

others : TEXT (WS TEXT)*;

resource : 'RESOURCES' ;

projectInformation: 'PROJECT INFORMATION:' ;

precedenceRelations: 'PRECEDENCE RELATIONS:' ;

requestsDurations: 'REQUESTS/DURATIONS:' ;

resourceAvailableilities: 'RESOURCEAVAILABILITIES:' ;

header : WS* TEXT (WS (TEXT | NUMBER))* ;

row : WS* NUMBER (WS NUMBER)* WS*;

TEXT   : [a-zA-Z#()][a-zA-Z0-9.(/)_#\-]* ;
LETTER : [a-zA-Z]+ ;
NUMBER : [0-9]+;
WS     : [ \t]+ ;
NEWLINE : '\r\n' | '\n' ;
