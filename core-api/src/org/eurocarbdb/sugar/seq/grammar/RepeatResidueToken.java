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
*   Last commit: $Rev: 1231 $ by $Author: glycoslave $ on $Date:: 2009-06-19 #$  
*/

package org.eurocarbdb.sugar.seq.grammar;

import java.util.Set;
import java.util.HashSet;

import org.apache.log4j.Logger;
import antlr.Token;

import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.SugarRepeat;
import org.eurocarbdb.sugar.SugarRepeatAnnotation;

import org.eurocarbdb.util.graph.Graph;


/*  class RepeatResidueToken  *//************************************
*
*
*   @author mjh 
*/
public class RepeatResidueToken extends ResidueToken
{
    // protected Graph<LinkageToken,ResidueToken> graph 
    //     = new Graph<LinkageToken,ResidueToken>();

    /** the number (index) of the current repeat */
    private int repeatIndex;
    
    /** the reducing terminal side ResidueToken */
    protected ResidueToken repeatRootToken;
    
    /** the non-reducing terminal side ResidueToken */
    protected ResidueToken repeatLeafToken;
    
    // /** the linkage between the repeats.  */
    // protected LinkageToken linkageBetweenRepeats;
        
    // /** collection of Residues that form this repeat */
    // protected Set<ResidueToken> repeatResidueTokens = new HashSet<ResidueToken>();
    
    protected SugarRepeatAnnotation repeat = new SugarRepeatAnnotation();
    
    
    /** 
    *   Constructor. 
    *   @param parser   The parser generating this token
    *   @param tok      The ANTLR token being wrapped.
    */
    public RepeatResidueToken( ParserAdaptor parser, Token tok, Residue r )
    {
        super( parser, tok, r );
    }
    
    
    // /** 
    // *   Constructor. 
    // *   @param parser   The parser generating this token
    // *   @param tok      The ANTLR token being wrapped.
    // *   @param r        A pre-constructed {@link Residue}
    // */
    // public RepeatResidueToken( ParserAdaptor parser, Token tok, Residue r )
    // {
    //     super( parser, tok, r );
    // }

    
    // public Graph<LinkageToken,ResidueToken> getGraph()
    // {
    //     return graph;    
    // }
     
    
    /** Adds a {@link ResidueToken} to this repeat. */
    public void addResidueToken( ResidueToken rt )
    {
        assert rt != null;
        Residue r = rt.getResidue();
        
        repeat.addRepeatResidue( r );
        
        if ( repeat.getRepeatRootResidue() == null )
            repeat.setRepeatRootResidue( r );
    }
    
    
    public SugarRepeatAnnotation getRepeatAnnotation()
    {
        return repeat;    
    }
    
    
    public void setLinkageBetweenRepeats( LinkageToken lt )
    {
        // linkageBetweenRepeats = lt;
        repeat.setLinkageBetweenRepeats( lt.getLinkage() );
    }
    
    
    public void setRepeatRange( int lower, int upper )
    {
        repeat.setMinRepeatCount( lower );
        repeat.setMaxRepeatCount( upper );
    }
 

    public ResidueToken getLeafResidueToken() {  return repeatLeafToken;  }
    
    /** 
    *   Sets the leaf {@link ResidueToken} of this repeat.
    */
    public void setLeafResidueToken( ResidueToken rt )
    {
        repeatLeafToken = rt;
        repeat.setRepeatLeafResidue( rt.getResidue() );
    }

    
    public ResidueToken getRootResidueToken() {  return repeatRootToken;  }

    /** 
    *   Sets the root {@link ResidueToken} of this repeat.
    */
    public void setRootResidueToken( ResidueToken rt )
    {
        repeatRootToken = rt;
        repeat.setRepeatRootResidue( rt.getResidue() );
    }
    
    
    @Override
    public String toString()
    {
        return "(virtual repeat residue " + repeatIndex + ")";
    }
    
} // end class
