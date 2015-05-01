// $ANTLR 2.7.6 (2005-12-22): "iupac_grammar.g" -> "IupacParser.java"$
   
package org.eurocarbdb.sugar.seq.grammar; 

import org.eurocarbdb.sugar.seq.grammar.IupacParserAdaptor;
import org.eurocarbdb.sugar.seq.grammar.ParserAdaptor;
import org.eurocarbdb.sugar.seq.grammar.ResidueToken;
import org.eurocarbdb.sugar.seq.grammar.LinkageToken;


public interface IupacParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int NEWLINE = 4;
	int OPENING_REPEAT_DELIM = 5;
	int CLOSING_REPEAT_DELIM = 6;
	int NUMBER = 7;
	int INTERNAL_DELIM = 8;
	int REPEAT_RANGE_DELIM = 9;
	int OPENING_BRANCH_DELIM = 10;
	int CLOSING_BRANCH_DELIM = 11;
	int OPENING_LINKAGE_DELIM = 12;
	int CLOSING_LINKAGE_DELIM = 13;
	int COMMA = 14;
	int RESIDUE = 15;
	int ANOMER = 16;
	int UNKNOWN_ANOMER = 17;
	int UNKNOWN_TERMINUS = 18;
	int LOGICAL_OR = 19;
}
