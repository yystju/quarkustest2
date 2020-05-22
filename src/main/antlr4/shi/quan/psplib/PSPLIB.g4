grammar PSPLIB;

//@header {
//    package shi.quan.psplib;
//}

model :	(line NEWLINE)* ;

line : comment | resource | resourceParam | projectInformation | precedenceRelations | requestsDurations | resourceAvailabilities | prop | headers | data;

resource : 'RESOURCES' ;

resourceParam: WS '-' WS TEXT WS ':' WS TEXT;

projectInformation: 'PROJECT INFORMATION:' ;

precedenceRelations: 'PRECEDENCE RELATIONS:' ;

requestsDurations: 'REQUESTS/DURATIONS:' ;

resourceAvailabilities: 'RESOURCEAVAILABILITIES:' ;

headers: HEADER_TEXT (WS TEXT)* ;

data: NUMBER (WS NUMBER)* ;

prop: key WS* ':' WS* value ;

key:  TEXT (WS TEXT)* ;
value: TEXT (WS TEXT)* ;

HEADER_TEXT : [a-zA-Z]+ ;
WS     : [ \t]+ ;
LETTER : [a-zA-Z]+ ;
NUMBER : [0-9]+;
TEXT   : [a-zA-Z0-9.(/)_#]+ ;

comment : '*' '*'+ ;

NEWLINE : [\r\n]+ ;
