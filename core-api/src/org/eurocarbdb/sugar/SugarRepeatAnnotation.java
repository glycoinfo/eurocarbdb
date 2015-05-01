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
*   Last commit: $Rev: 1561 $ by $Author: glycoslave $ on $Date:: 2009-07-21 #$  
*/

package org.eurocarbdb.sugar;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import org.apache.log4j.Logger;

public class SugarRepeatAnnotation implements SugarAnnotation, PotentiallyIndefinite
{
    /** Int value indicating the number of repeats is unknown */
    public static final int UNKNOWN_NUMBER_OF_REPEATS = -1;

    /** The {@link Sugar} to which this annotation applies */    
    private Sugar annotatedSugar = null;
    
    /** the residues that comprise the repeat sub-tree */
    private Set<Residue> annotatedResidues = new HashSet<Residue>( 8 );
    
    /** root of repeat sub-tree */
    private Residue repeatRootResidue = null;
    
    /** connected leaf of repeat sub-tree */
    private Residue repeatLeafResidue = null;
    
    /** linkage between root & leaf */
    private Linkage repeatInternalLinkage = null;
    
    /** min number of copies of repeat sub-tree */
    private int minRepeatCount = UNKNOWN_NUMBER_OF_REPEATS;
    
    /** max number of copies of repeat sub-tree */
    private int maxRepeatCount = UNKNOWN_NUMBER_OF_REPEATS;
    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~ 
    
    public SugarRepeatAnnotation()
    {
    }
    
    
    // public SugarRepeatAnnotation( Sugar s )
    // {
    //     annotatedSugar = s;
    // }
    

    // public SugarRepeatAnnotation( 
    //         Sugar s, 
    //         Set<Residue> annotated,
    //         Residue root,
    //         Residue leaf,
    //         Linkage between 
    // )
    // {
    //     annotatedSugar = s;
    //     annotatedResidues = annotated;
    //     repeatRootResidue = root;
    //     repeatLeafResidue = leaf;
    //     repeatInternalLinkage = between;
    // }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
    
    public void addRepeatResidue( Residue r )
    {
        annotatedResidues.add( r );                   
    }
    
        
    /*  SugarAnnotation.getAnnotatedResidues  */
    public Set<Residue> getAnnotatedResidues()
    {
        if ( annotatedResidues == null )
            return Collections.emptySet();            
        
        return Collections.unmodifiableSet( annotatedResidues );
    }

    
    /*  SugarAnnotation.getAnnotatedSugar  */
    public Sugar getAnnotatedSugar()
    {
        return annotatedSugar;   
    }
    
    
    public void setAnnotatedSugar( Sugar s )
    {
        if ( annotatedSugar != s )
        {
            annotatedSugar = s;    
            repeatLeafResidue = null;
            repeatRootResidue = null;
            annotatedResidues.clear();
        }
    }
    
    
    /** 
    *   Returns the {@link Residue} that is closest to the reducing terminus
    *   ("most parent") in the {@link Set} of residues that form this repeat
    *   that is also involved in the joining of repeat units together. 
    */
    public Residue getRepeatRootResidue()
    {
        return repeatRootResidue;
    }
    
    
    public void setRepeatRootResidue( Residue r )
    {
        assert annotatedResidues.contains( r );
        repeatRootResidue = r;
    }
    
    
    /** 
    *   Returns the {@link Residue} that is closest to the non-reducing terminii
    *   ("most child/leaf") in the {@link Set} of residues that form this repeat
    *   that is also involved in the joining of repeat units together. 
    */
    public Residue getRepeatLeafResidue()
    {
        return repeatLeafResidue;
    }
    
    
    public void setRepeatLeafResidue( Residue r )
    {
        assert annotatedResidues.contains( r );
        repeatLeafResidue = r;
    }

    
    /** 
    *   Returns the {@link Linkage} between that joins individual repeat units together. 
    *   This is also the linkage between the {@link Residues} given by 
    *   {@link #getRepeatRootResidue()} and {@link #getRepeatLeafResidue()}.
    */
    public Linkage getLinkageBetweenRepeats()
    {
        return repeatInternalLinkage;
    }
    

    public void setLinkageBetweenRepeats( Linkage l )
    {
        assert l != null;
        repeatInternalLinkage = l;
    }

    
    /**
    *   Returns the minimum number of repeats of this repeat region;  
    *   {@link #UNKNOWN_NUMBER_OF_REPEATS} for unknown.
    */
    public int getMinRepeatCount()
    {
        return minRepeatCount;
    }
    
    
    /**
    *   Returns the maximum number of repeats of this repeat region; -
    *   {@link #UNKNOWN_NUMBER_OF_REPEATS} for unknown.
    */
    public int getMaxRepeatCount()
    {
        return maxRepeatCount;
    }
    
    
    /**
    *   Sets the minimum number of repeats of this repeat region; 
    *   {@link #UNKNOWN_NUMBER_OF_REPEATS} for unknown.
    */
    public void setMinRepeatCount( int min )
    {
        if ( min < 0 )
            min = UNKNOWN_NUMBER_OF_REPEATS;
        
        minRepeatCount = min;
    }
    
    
    /**
    *   Sets the maximum number of repeats of this repeat region; 
    *   {@link #UNKNOWN_NUMBER_OF_REPEATS} for unknown.
    */
    public void setMaxRepeatCount( int max )
    {
        if ( max < 0 )
            max = UNKNOWN_NUMBER_OF_REPEATS;
        
        maxRepeatCount = max;
    }
    
    
    public boolean isDefinite()
    {
        return minRepeatCount != -1
            && maxRepeatCount != -1
            && repeatInternalLinkage.isDefinite();
    }

    
    public String toString()
    {
        return "[repeat: range="
            + ((minRepeatCount != -1) ? minRepeatCount : '?')
            + '-'
            + ((maxRepeatCount != -1) ? maxRepeatCount : '?')
            + "; linkage="
            + repeatInternalLinkage
            + "; from="
            + repeatRootResidue
            + "; to="
            + repeatLeafResidue
            + "; residues="
            + annotatedResidues
            + "]"
        ;    
    }
    
} // end interface


