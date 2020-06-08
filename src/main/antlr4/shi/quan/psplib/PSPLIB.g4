grammar PSPLIB;

model : file_header
        header_info
        project_information
        precedence_relations
        requests_durations
        resource_availabilities
        ;

file_header : comment property+ comment ;

header_info : property+ 'RESOURCES' NEWLINE renewable nonrenewable doublyconstrained comment ;

renewable : WS+ '- renewable' WS+ ':' WS+ renewable_number WS+ 'R' WS* NEWLINE ;
nonrenewable : WS+ '- nonrenewable' WS+ ':' WS+ nonrenewable_number WS+ 'N' WS* NEWLINE ;
doublyconstrained : WS+ '- doubly constrained' WS+ ':' WS+ doublyconstrained_number WS+ 'D' WS* NEWLINE ;

renewable_number : NUMBER ;
nonrenewable_number : NUMBER ;
doublyconstrained_number : NUMBER ;

project_information : 'PROJECT INFORMATION:' NEWLINE 'pronr.  #jobs rel.date duedate tardcost  MPM-Time' NEWLINE general_information+ comment ;

general_information : WS+ proNumber WS+ jobId WS+ realDate WS+ dueDate WS+ tardCost WS+ mpmTime NEWLINE ;

precedence_relations : 'PRECEDENCE RELATIONS:' NEWLINE 'jobnr.    #modes  #successors   successors' NEWLINE relationships+ comment ;

relationships : WS+ jobId WS+ modes WS+ successorNumber WS* (WS+ successor)* NEWLINE ;

requests_durations : 'REQUESTS/DURATIONS:' NEWLINE 'jobnr. mode duration' (WS+ RESOURCE_FLAG WS+ resourceNumber)* NEWLINE comment resources+ comment ;

resources : WS* jobId WS+ modes WS+ duration (WS+ resource)+ NEWLINE ;

resource_availabilities : 'RESOURCEAVAILABILITIES:' NEWLINE (WS+ RESOURCE_FLAG WS+ resourceNumber)* NEWLINE availablities+ comment ;

availablities : (WS+ availability)+ NEWLINE ;

availability : NUMBER ;

proNumber : NUMBER ;
jobId : NUMBER ;
realDate : NUMBER ;
dueDate : NUMBER ;
tardCost : NUMBER ;
mpmTime : NUMBER ;

modes : NUMBER ;
successorNumber : NUMBER ;
successor : NUMBER ;

resourceNumber : NUMBER ;
duration : NUMBER ;

resource : NUMBER ;

property : key WS* ':' WS* value WS* NEWLINE ;

comment : (('*' '*'+) | ('-' '-'+)) NEWLINE ;

key : (WS* '-' WS*)* TEXT (WS TEXT)* ;
value : (TEXT | NUMBER) (WS+ (TEXT | NUMBER))* ;

RESOURCE_FLAG : 'R' ; //TODO: This should be 'R', 'U', 'D'...
TEXT   : [a-zA-Z#()][a-zA-Z0-9.(/)_#\-]* ;
LETTER : [a-zA-Z]+ ;
NUMBER : [0-9]+;
WS     : [ \t]+ ;
NEWLINE : '\r\n' | '\n' ;
