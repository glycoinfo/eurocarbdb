/*
*   EuroCarbDB, a framework for carbohydrate bioinformatics
*
*   Copyright (c) 2006-2009, Eurocarb project, or third-party contributors as
*   indicated by the @author tags or express copyright attribution
*   statements applied by the authors.  
*
*   This copyrighted material is made available to anyone wishing to use, modify,
*   copy, or redistribute it subject to the terms and conditions of the GNU
*   Lesser General Public License, as published by the Free Software Foundation.
*   A copy of this license accompanies this distribution in the file LICENSE.txt.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*   or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
*   for more details.
*
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package org.eurocarbdb.sugar.seq.grammar;

import java.io.*;
import java.util.*;

import antlr.collections.AST;
import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.TokenStreamRecognitionException;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.SequenceFormatException;

import org.eurocarbdb.sugar.seq.grammar.GlycoctLexer;
import org.eurocarbdb.sugar.seq.grammar.GlycoctParser;

//import antlr.debug.DebuggingParser;

//import antlr.debug.misc.ASTFrame;

/*  class GlycoctTest  *//*******************************************
*
*   Unit test class for GlycoCT parsing.
*
*   To run:
*<pre>  
*   java org.eurocarbdb.sugar.seq.grammar.GlycoctTest
*</pre>
*
*   for an interactive shell: 
*
*<pre>
*   java org.eurocarbdb.sugar.seq.grammar.SequenceTestHarness\$Shell Glycoct
*</pre>
*
*   Don't forget to set your classpath to include the ANTLR, common logging,
*   and log4j jars.
*
*/
public class GlycoctTest extends SequenceTestHarness
{
    
    public static void main( String[] args ) throws Exception
    {
        new GlycoctTest().testParsing( correct_sequences, incorrect_sequences );
    }

    //  syntactically correct sequences
    public static String[] correct_sequences =  
    { 
        //  seq 1
        //  a-d-fructose-f
            "RES 1b:a-dara-hex-2:5|2:keto;" 
            
        //  seq 2 - a deoxy sugar: 
        //  2,6-dideoxy-3-O-methyl-a-D-arabino-hexopyranose
        ,   "RES 1b:a-dara-hex-1:5|2:d|6:d;" 
        
        //  seq 3 - a uronic acid:
        //  D-Glucopyranosyluronic acid, b-d-GlcU-p
        ,   "RES 1b:b-dglc-hex-1:5|6:a;"    
        
        //  seq 4 - an aldonic acid:
        //  D-Gluconic acid
        ,   "RES 1b:o-dglc-hex-0:0|1:a;"
        
        //  seq 5 - an amino sugar:
        //  2,6-diamino-2,3,6-trideoxy-a-D-ribo-hexopyranose
        ,   "RES 1b:a-drib-hex-1:5|3:d;2s:n;3s:n;" 
        +   "LIN 1:1d(2-1)2n;2:1d(6-1)3n;" 
        
        //  seq 6 - a thiol sugar:
        //  3-amino-3,4-dideoxy-4-thio-a-D-galactopyranose
        ,   "RES 1b:a-dgal-hex-1:5;2s:n;4s:thiol;" 
        +   "LIN 1:1d(3-1)2n;2:1d(4-1)3n;"
        
        //  seq 7 - an alditol:
        //  D-Arabinitol
        ,   "RES 1b:o-dara-pen-0:0|1:aldi;"
        
        //  seq 8 - an intramolecular anhydride:
        //  3,6-anhydro-a-D-glucopyranose
        ,   "RES 1b:a-dglc-hex-1:5;2s:lactone;" 
        +   "LIN 1:1d(3-6)2o;"
        
        //  seq 9 - an unsaturated monosac:
        //  2,3-dideoxy-a-D-erythro-hex-2-en-pyranose
        //,   "RES 1b:a-dery-hex-1:5|2:d|2,3:en|3d;" // error in manual '3d'                
        ,   "RES 1b:a-dery-hex-1:5|2:d|2,3:en|3:d;"                
        
        //  seq 10 - a lactone:
        //  L-xylo-hex-2-ulosono-1,4-lactone (Vitamin C isomer)
        ,   "RES 1b:o-lxyl-hex-0:0|1:a|2:keto;"    
        +   "LIN 1:1d(1-4)1o;"
        
        //  seq 11 - a sialic acid: NeuGc
        //  CHECK: I think this sequence is wrong in the manual -- there should be 
        //  a linkage type id at the end of the sequence before the final semicolon.
        ,   "RES 1b:a-dgro-dgal-non-2:6|1:a|2:keto|3:d;1s:n-glycolyl;" 
        +   "LIN 1:1o(5-1)2d;"
                        
        
        //~~~  larger examples  ~~~//
        
        //  seq 12 - Lewis X
        ,   "RES"
        +   "1b:b-dgal-hex-1:5;" 
        +   "2s:n-acetyl;"
        +   "3b:a-lgal-hex-1:5|6:d;"
        +   "4b:b-dgal-hex-1:5;"
        +   "LIN"
        +   "1:1d(2-1)2n;"
        +   "2:1o(3-1)3d;"
        +   "3:1o(4-1)4d;"
        
        
        //  structures with unknowns
        
        //  seq 13 - 
        ,   "RES"
        +   "1b:a-dglc-hex-1:5;"
        +   "2s:n-acetyl;"
        +   "3b:a-dery-hex-1:5|2:d;" // error in manual '2d'
        +   "4b:a-dgro-dgal-non-2:6|1:a|2:keto|3:d;"
        +   "5s:n;"
        +   "6b:a-dtal-hex-1:5|6:d;" // error in manual '6d'
        +   "7s:n;"
        +   "LIN"
        +   "1:1d(?-1)2n;"
        +   "2:4d(?-1)5n;"
        +   "3:6d(?-1)7n;"
        
        //  structures with linkage alternates
        
        
        //  sequences with repeats


        //  sequences with multiconnections

    };
    
        
    //  syntactically incorrect sequences
    public static String[] incorrect_sequences =  
    {         
        //  basic sequence errors
            ""                              // 1: empty string 
        ,   " "                             // 2: space only
        ,   "1b:a-dara-hex-2:5|2:keto;"     // 3: no RES section id
        ,   "RES 1b:a-dara-hex-2:5|2:keto"  // 4: missing semicolon at end
        ,   "RES 1b:a-dara-hex-2:5|2:keto|" // 5: invalid end char
        ,   "RES 1b:a-dara-hex-2:5|2:keto;;"// 6: double semicolon at end
        ,   "RES 1:a-dara-hex-2:5|2:keto;"  // 7: missing residue type 'b'
        ,   "RES b:a-dara-hex-2:5|2:keto;"  // 8: missing residue numbering
        ,   "RES :a-dara-hex-2:5|2:keto;"   // 9: missing residue number/type
        ,   "RES 1b:a-dara-2:5|2:keto;"     // 10: missing ring config
        
        //  ring closure errors 
        ,   "RES 1b:a-dara-hex|2:keto;"     // 11: missing ring closure '-2:5'
        ,   "RES 1b:a-dara-hex-2:0|2:keto;" // 12: invalid ring closure position
        ,   "RES 1b:a-dara-hex-2:2|2:keto;" // 13: duplicate ring closure position
        ,   "RES 1b:a-dara-hex-2:5:2:keto;" // 14: '|' mistyped as ':'    
        
        //  modification errors
        ,   "RES 1b:a-dara-hex-2:5|0:en;"   // 15: invalid modification terminus position
        ,   "RES 1b:a-dara-hex-2:5|2,2:en;" // 16: duplicate modification terminus position
        ,   "RES 1b:a-dara-hex-2:5|2:zzzzz;"// 17: invalid mod name 
            
        //  structures with linkage syntax errors
                        
        //  structures with branching syntax errors
            
    };
    

    public ParserAdaptor getParserFor( String seq )
    {
        GlycoctLexer   lexer = new GlycoctLexer( new StringReader( seq ) );
        GlycoctParser parser = new GlycoctParser( lexer );
        parser.setSequence( seq );
        return parser;
    }
    
 
} // end class
