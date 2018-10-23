// https://www.w3.org/TR/CSS21/grammar.html used as a reference

grammar Parser;
options { language=Java; }

@parser::header {
    import nodes.*;
}

// Productions

stylesheet : (mixinDeclaration | variableDeclaration | ruleset)* ;

statement : variableDeclaration
          | useDeclaration
          ;

variable : '$' IDENT ;

params : param (',' param)* ;

param : variable ;

mixinDeclaration : '@mixin' name=IDENT '(' args=params? ')' block ;

variableDeclaration : variable '=' expression ';' ;

useDeclaration : '@use' name=IDENT '(' args=values? ')' ';' ;

ruleset : selectors block ;

selectors : selector ;

selector
  : simple_selector ( COMBINATOR? selector )?
  ;
simple_selector : element_name ( HASH | cssclass | attrib | pseudo )* | ( HASH | cssclass | attrib | pseudo )+ ;
cssclass : '.' IDENT ;
element_name : IDENT | '*' ;
attrib : '[' IDENT ( ( '=' | '~=' | '|=' ) ( IDENT | STRING ) )? ']' ;
pseudo : ':' ( IDENT | IDENT '(' IDENT? ')' ) ;

block : '{' (property ';' | statement)* property? '}' ;

property : name=IDENT ':' values ;

values : expression+ (',' expression+)* ;

expression : expr ;

expr : left=expr op=('*' | '/') right=expr   # ExprMulDiv
     | left=expr op=('+' | '-') right=expr   # ExprAddSub
     | '(' expr ')'                          # ExprParens
     | factor                                # ExprFactor
     ;

/*term (('+' | '-') term)* ;
term : factor (('*' | '/') factor) * ;
*/

factor returns [Factor value]
       : num=NUMBER unit=UNIT? {$value = new Factor(Factor.Type.Number, $num.text, $unit.text); }
       | color                 {$value = new Factor(Factor.Type.Color, $color.text);            }
       | IDENT                 {$value = new Factor(Factor.Type.Ident, $IDENT.text);            }
       | string                {$value = new Factor(Factor.Type.String, $string.text);          }
       | variable              {$value = new Factor(Factor.Type.Variable, $variable.text);      }
       ;

string : STRING ;

// Has the restriction that there are 3 or 6 hex characters, and the characters in the hash are [0-9a-f]
color : HASH ;

// Lexer

fragment STRING1 :	'"' ('\t'|' '|'!'|'#'|'$'|'%'|'&'|'\''|'.'|'-'|'_'|[a-zA-Z0-9])* '"' ;
fragment STRING2 :	'\'' ('\t'|' '|'!'|'#'|'$'|'%'|'&'|'.'|'-'|'_'|[a-zA-Z0-9])* '\'' ;

fragment NUM   : '-'? [0-9]+ | [0-9]* '.' [0-9]+ ;

// Units (reserved keywords)
fragment EMS             : E M ;
fragment EXS             : E X ;
fragment LENGTH          : P X | C M | M M | I N | P T | P C ;
fragment ANGLE           : (D E G | R A D | G R A D) ;
fragment TIME            : (M S | S ) ;
fragment FREQ            : (H Z | K H Z) ;
fragment PERCENTAGE      : '%' ;

UNIT : EMS | EXS | LENGTH | ANGLE | TIME | FREQ | PERCENTAGE ;
NUMBER : NUM ;


IDENT : '-'? NMSTART NMCHAR* ;
HASH : '#' NMCHAR+ ;
fragment NMSTART: ('_' | 'a'..'z' | 'A'..'Z') ;
fragment NMCHAR: ('_' | '-' | 'a'..'z' | 'A'..'Z' | '0'..'9');

COMBINATOR: '+' | '>' ;

COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;

 // COMMENT : '/*' [^*]* [*]+  '/' -> skip ; \/\*[^*]*\*+([^/*][^*]*\*+)*\/


// MISSING URL

WS : (' '|'\t'|'\r'|'\n'|'\f')+ -> skip ;


HEX : '#' [0-9a-f]+ ;

STRING          : STRING1 | STRING2 ;
URI             : 'url(' STRING ')' /* | 'url(' URL ')' */ ;
//FUNCTION        : IDENT '(' ;

fragment A : 'A' | 'a' ;
fragment C : 'C' | 'c' ;
fragment D : 'D' | 'd' ;
fragment E : 'E' | 'e' ;
fragment G : 'G' | 'g' ;
fragment H : 'H' | 'h' ;
fragment I : 'I' | 'i' ;
fragment K : 'K' | 'k' ;
fragment M : 'M' | 'm' ;
fragment N : 'N' | 'n' ;
fragment O : 'O' | 'o' ;
fragment P : 'P' | 'p' ;
fragment R : 'R' | 'r' ;
fragment S : 'S' | 's' ;
fragment T : 'T' | 't' ;
fragment X : 'X' | 'x' ;
fragment Z : 'Z' | 'z' ;

