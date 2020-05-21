grammar PSPLIB;

//@header {
//    package shi.quan.psplib;
//}

model:	(line NEWLINE)* ;

line: comment | prop ;

prop: key WS* ':' WS* value ;

key:  TEXT (WS TEXT)* ;
value: TEXT (WS TEXT)* ;

TEXT : [a-zA-Z0-9]+ ;
WS: [ \t]+ ;

comment : '*' '*'+ ;

NEWLINE : [\r\n]+ ;
