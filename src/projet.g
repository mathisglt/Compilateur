// Grammaire du langage PROJET
// CMPL L3info 
// Nathalie Girard, Veronique Masson, Laurent Perraudeau
// il convient d'y inserer les appels a {PtGen.pt(k);}
// relancer Antlr apres chaque modification et raffraichir le projet Eclipse le cas echeant

// attention l'analyse est poursuivie apres erreur si l'on supprime la clause rulecatch

grammar projet;

options {
  language=Java; k=1;
 }

@header {           
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;
} 


// partie syntaxique :  description de la grammaire //
// les non-terminaux doivent commencer par une minuscule


@members {

 
// variables globales et methodes utiles a placer ici
  
}
// la directive rulecatch permet d'interrompre l'analyse a la premiere erreur de syntaxe
@rulecatch {
catch (RecognitionException e) {reportError (e) ; throw e ; }}


unite  :   unitprog {PtGen.pt(255);} EOF
      |    unitmodule  EOF
  ;
  
unitprog
  : 'programme' ident ':'  
     declarations  
     corps { System.out.println("succes, arret de la compilation "); }
  ;
  
unitmodule
  : 'module' ident ':' 
     declarations   
  ;
  
declarations
  : partiedef? partieref? consts? vars?{PtGen.pt(200);} {PtGen.pt(48);} decprocs? {PtGen.pt(54);}
  ;
  
partiedef
  : 'def' ident  (',' ident )* ptvg
  ;
  
partieref: 'ref'  specif (',' specif)* ptvg
  ;
  
specif  : ident  ( 'fixe' '(' type  ( ',' type  )* ')' )? 
                 ( 'mod'  '(' type  ( ',' type  )* ')' )? 
  ;
  
consts  : 'const' ( ident '=' valeur {PtGen.pt(2);} ptvg  )+ 
  ;
  
vars  : 'var' ( type ident  {PtGen.pt(3);} ( ','  ident {PtGen.pt(3);} )* ptvg  )+
  ;
  
type  : 'ent'  {PtGen.pt(8);}
  |     'bool' {PtGen.pt(9);}
  ;
  
decprocs: (decproc ptvg)+
  ;
  
decproc :  'proc'  ident {PtGen.pt(49);} parfixe? parmod? {PtGen.pt(50);} consts? vars? {PtGen.pt(200);} corps {PtGen.pt(53);}
  ;
  
ptvg  : ';'
  | 
  ;
  
corps : 'debut' instructions 'fin' 
  ;
  
parfixe: 'fixe' '(' pf ( ';' pf)* ')'
  ;
  
pf  : type ident {PtGen.pt(51);} ( ',' ident {PtGen.pt(51);} )*
  ;

parmod  : 'mod' '(' pm ( ';' pm)* ')'
  ;
  
pm  : type ident {PtGen.pt(52);} ( ',' ident {PtGen.pt(52);} )*
  ;
  
instructions
  : instruction ( ';' instruction)*
  ;
  
instruction
  : inssi
  | inscond
  | boucle
  | lecture
  | ecriture
  | affouappel
  |
  ;
  
inssi : 'si' expression { PtGen.pt(31);} 'alors' instructions ('sinon' { PtGen.pt(32);} instructions )? { PtGen.pt(33);}'fsi' 
  ;
  
inscond : { PtGen.pt(39);}'cond'  expression { PtGen.pt(31);}  ':' instructions 
          (',' { PtGen.pt(36);} expression { PtGen.pt(31);}  ':' instructions)* 
          ( { PtGen.pt(37);} 'aut' instructions |  ) 
          { PtGen.pt(38);} 'fcond' 
  ;
  
boucle  : 'ttq'  { PtGen.pt(34);}  expression { PtGen.pt(31);} 'faire' instructions { PtGen.pt(35);} 'fait' 
  ;
  
lecture: 'lire' '(' ident {PtGen.pt(30);} ( ',' ident {PtGen.pt(30);} )* ')' 
  ;
  
ecriture: 'ecrire' '(' expression {PtGen.pt(25);} ( ',' expression {PtGen.pt(25);} )* ')'
   ;
  
affouappel
  : ident {PtGen.pt(28);} (    ':=' expression {PtGen.pt(29);}
            |  {PtGen.pt(70);} (effixes (effmods)?)?  {PtGen.pt(71);}
           )
  ;
  
effixes : '(' (expression {PtGen.pt(60);}  (',' expression {PtGen.pt(60);} )*)? ')'
  ;
  
effmods :'(' (ident {PtGen.pt(61);}  (',' ident {PtGen.pt(61);} )*)? ')'
  ; 
  
expression: (exp1) ('ou'  {PtGen.pt(10);} exp1{PtGen.pt(10);} {PtGen.pt(11);} )*
  ;
  
exp1  : exp2 ('et' {PtGen.pt(10);} exp2{PtGen.pt(10);}{PtGen.pt(12);}  )*
  ;
  
exp2  : 'non' exp2 {PtGen.pt(13);}
  | exp3
  ;
  
exp3  : exp4
  ({PtGen.pt(14);} '='   exp4 {PtGen.pt(14);}{PtGen.pt(15);}
  |{PtGen.pt(14);} '<>'  exp4 {PtGen.pt(14);}{PtGen.pt(16);}
  |{PtGen.pt(14);} '>'   exp4 {PtGen.pt(14);}{PtGen.pt(17);}
  |{PtGen.pt(14);} '>='  exp4 {PtGen.pt(14);}{PtGen.pt(18);}
  |{PtGen.pt(14);} '<'   exp4 {PtGen.pt(14);}{PtGen.pt(19);}
  |{PtGen.pt(14);} '<='  exp4  {PtGen.pt(14);}{PtGen.pt(20);}
  ) ?
  ;
  
exp4  : exp5 
        ('+'  exp5 {PtGen.pt(21);}
        |'-'  exp5 {PtGen.pt(22);}
        )*
  ;
  
exp5  : primaire 
        (    '*'   primaire {PtGen.pt(23);}
          | 'div'  primaire {PtGen.pt(24);}
        )*
  ;
  
primaire: valeur {PtGen.pt(26);} 
  | ident  {PtGen.pt(27);}  
  | '(' expression ')'
  ;
  
valeur  : nbentier {PtGen.pt(4);}
  | '+' nbentier {PtGen.pt(4);}
  | '-' nbentier {PtGen.pt(5);}
  | 'vrai' {PtGen.pt(6);}
  | 'faux' {PtGen.pt(7);}
  ;

// partie lexicale  : cette partie ne doit pas etre modifiee  //
// les unites lexicales de ANTLR doivent commencer par une majuscule
// Attention : ANTLR n'autorise pas certains traitements sur les unites lexicales, 
// il est alors ncessaire de passer par un non-terminal intermediaire 
// exemple : pour l'unit lexicale INT, le non-terminal nbentier a du etre introduit
 
      
nbentier  :   INT { UtilLex.valEnt = Integer.parseInt($INT.text);}; // mise a jour de valEnt

ident : ID  { UtilLex.traiterId($ID.text); } ; // mise a jour de numIdCourant
     // tous les identificateurs seront places dans la table des identificateurs, y compris le nom du programme ou module
     // (NB: la table des symboles n'est pas geree au niveau lexical mais au niveau du compilateur)
        
  
ID  :   ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ; 
     
// zone purement lexicale //

INT :   '0'..'9'+ ;
WS  :   (' '|'\t' |'\r')+ {skip();} ; // definition des "blocs d'espaces"
RC  :   ('\n') {UtilLex.incrementeLigne(); skip() ;} ; // definition d'un unique "passage a la ligne" et comptage des numeros de lignes

COMMENT
  :  '\{' (.)* '\}' {skip();}   // toute suite de caracteres entouree d'accolades est un commentaire
  |  '#' ~( '\r' | '\n' )* {skip();}  // tout ce qui suit un caractere diese sur une ligne est un commentaire
  ;

// commentaires sur plusieurs lignes
ML_COMMENT    :   '/*' (options {greedy=false;} : .)* '*/' {$channel=HIDDEN;}
    ;	   



	    