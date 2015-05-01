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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/

package test.eurocarbdb.sugar.seq;

import org.testng.annotations.*;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.SequenceFormatException;


/*  class IupacSequenceFormatTest  *//*******************************
*
*   Unit test class for Iupac sequence parsing.
*/
// @Test( groups={"sugar.parsing.iupac"}, dependsOnGroups={"sugar.lib"} )         
@Test( groups={"sugar.lib"} )         
public class IupacSequenceFormatTest extends SequenceFormatTest
{
    @Test
    public SequenceFormat getParser() 
    {
        assert SequenceFormat.Iupac != null;
        return SequenceFormat.Iupac;  
    }

    
    @Test
    public void iupacSequences()
    {
        testParsing( correct_sequences );   
    }

    
    @Test
    public void iupacSequenceExceptions()
    {
        testParsingExceptions( incorrect_sequences );   
    }

    
    @Test
    public void iupacSequenceGeneration()
    {
        testSequenceGeneration( correct_sequences, SequenceFormat.Iupac );   
    }
    

    //  syntactically correct sequences
    public static String[] correct_sequences =  
    { 
        //  "regular" sequences
        "Man"
        ,   "Man(a1-4)Man"
        ,   "GlcNAc(a1-4)Man"
        ,   "Glc(a1-3)Glc(a1-3)Glc"
        ,   "Man(a1-6)[Man(a1-3)]Man(a1-3)GlcNAc(b1-3)GlcNAc"
        ,   "Man(a1-6)[Man(a1-4)][Man(a1-3)]GlcNAc(b1-3)GlcNAc"
        ,   "GlcNAc(b1-4)[Man(a1-6)[Man(a1-4)[Man(a1-3)]Man(a1-4)][Man(a1-3)]GlcNAc(b1-3)]GlcNAc"
        
        //  sequences with substituents
        ,   "Glc(a1-)P(-4)Glc"
        ,   "Glc(a1-)S(-4)Glc"
        ,   "Glc(a1-)[Glc(a1-)]P(-4)Glc"
        ,   "Glc(a1-6)[Glc(a1-)P(-3)]Glc(a1-)P(-4)Glc"
        // ,   "Glc(a1-)P(-)P(-4)Glc" <- todo
        
        
        //  structures with unknowns
        ,   "Man(a1-?)Man"
        ,   "Man(u1-3)Man"
        ,   "NeuAc(a2-?)Gal(b1-4)GlcNAc(b1-2)Man(a1-6)[NeuAc(a2-?)Gal(b1-4)GlcNAc(b1-2)Man(a1-3)]Man(b1-3)GlcNAc(b1-3)GlcNAc"
        
        
        //  structures with linkage alternates
        ,   "Man(a1-4|6)Man"
        ,   "Man(a1-3|4|6)Man"
        ,   "Man(a1-3|6)[Man(a1-3|4)]GlcNAc(b1-3)GlcNAc"
        ,   "Man(a1-?)[Man(a1-3|4)]GlcNAc(b1-3)GlcNAc"
        ,   "Glc(a1-)P(-3|4)Glc"
        
        
        
        //  sequences with repeats
        ,   "{Glc(a1-4)Glc}(a1-4)Glc"
        ,   "Glc(a1-6){Glc(a1-4)Glc}(a1-4)Glc"
        ,   "Man(b1-4){Glc(a1-4)Glc}(a1-4)Glc"
        ,   "Man(b1-4){5:Glc(a1-4)Glc}(a1-4)Glc"
        ,   "Man(b1-4){15:Glc(a1-4)Glc}(a1-4)Glc"
        ,   "Man(b1-4){151:Glc(a1-4)Glc}(a1-4)Glc"
        ,   "Man(b1-4){1515:Glc(a1-4)Glc}(a1-4)Glc"
        ,   "Man(b1-4){10-21:Glc(a1-4)Glc}(a1-4)Glc"                
        ,   "Man(b1-4){1-20:Glc(a1-4)Glc}(a1-4)Glc"                
        ,   "Man(b1-4){Glc(a1-4)[Glc(a1-3)]Glc}(a1-4)Glc"
        ,   "Man(b1-4){3-6:Glc(a1-4)[{1-5:Glc(a1-3)Glc}(a1-3)]Glc}(a1-4)Glc"

        //  sequences with multiconnections
        ,   "NeuAc(a2-8,1-9)NeuAc"
        ,   "NeuAc(a2-4,8-8,9-9)NeuAc"
    };
    
    
    //  syntactically incorrect sequences
    public static String[] incorrect_sequences =  
    {                 
        //  structures with residue name syntax errors
            " "
        ,   ")Man"
        ,   "Man(a1-4Man"
        ,   "Man(a1(-4Man"
        ,   "Mana1-4Man"
        ,   "a1-4Man"
        ,   "Man(a14)Man"
        ,   "Man(a1-4Man-"
        ,   "Man(a1-4)"
        ,   "Man(a1-4)Glc&NAc"     
        ,   "Man(a1-4)(GlcNAc)"    
//             ,   "Man(a1-4)Mannose"     // residue name too long
//             ,   "Ma"                   // residue name too short
        ,   "lcNAc(b1-3)GlcNAc"    
            
        //  structures with linkage syntax errors
        ,   "Man(1-4)GlcNAc"            // missing anomer
        ,   "Man(x1-4)GlcNAc"           // invalid anomer
        ,   "GlcNAc(a1-4))Man"          // unmatched/extra parenthesis
        ,   "Man(a1-4Man"               // unmatched/missing parenthesis
        ,   "Man(a14)GlcNAc"            // missing internal delimiter
            
            
        //  structures with branching syntax errors
        ,   "Glc(a1-3)]Glc(a1-3)Glc"    //  unmatched opening branch
        ,   "Glc(a1-3)[Glc(a1-3)Glc"    //  unmatched closing branch
        ,   "Glc(a1-3)[]Glc(a1-3)Glc"   //  empty branch
        ,   "Man(a1-6)[[Man(a1-4)]Man(a1-3)]GlcNAc(b1-3)GlcNAc" // doubled branch delim
        ,   "Man(a1-6)[[Man(a1-4)]]Man(a1-3)GlcNAc(b1-3)GlcNAc" // doubled branch delims
        ,   "Glc(a1-3)[Glc(a1-3)]"      //  missing root residue
            
        ,   "Man(b1-4){5-1:Glc(a1-4)Glc}(a1-4)Glc"  //  repeat bounds in wrong order
        ,   "Man(b1-4){1:Glc(a1-4)Glc}(a1-4)Glc"    //  repeat bound too low
            
    };    
 
} // end class
