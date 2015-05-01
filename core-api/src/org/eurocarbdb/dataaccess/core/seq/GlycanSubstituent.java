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
*   Last commit: $Rev: 1258 $ by $Author: glycoslave $ on $Date:: 2009-06-26 #$  
*/

package org.eurocarbdb.dataaccess.core.seq;

//  stdlib imports

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Substituent;

import org.eurocarbdb.sugar.PositionOccupiedException;
import org.eurocarbdb.sugar.PositionNotOccupiedException;

import org.eurocarbdb.dataaccess.core.GlycanSequence;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
*   Each GlycanMonosaccharide represents a single {@link EcdbMonosaccharide} 
*   of a single {@link GlycanSequence}.
*
*   @author mjh
*/
public class GlycanSubstituent
extends GlycanResidue implements Substituent
{
    // private String name;
    
    /** Delegated substituent. */
    private Substituent substituent;
    
    /*
    public static Substituent forName( String name )
    {
        return null;
    }
    */
        
    /* needed for hibernate */
    GlycanSubstituent() 
    {
    }
    
    
    public GlycanSubstituent( Substituent s )
    {
        super();
        setResidue( s );
    }
    
    
    public boolean causesStereoloss()
    {
        return getSubstituent().causesStereoloss();   
    }
 
    
    public Residue getResidue()
    {
        return getSubstituent();
    }
    
    
    public void setResidue( Residue r )
    {
        this.substituent = (Substituent) r;
        setResidueName( r.getName() );
        super.setResidue( r );
    }
    
    /*
    public String getResidueName()
    {
        return name;
    }


    public void setResidueName( String name )
    {
        this.name = name;       
    }
    */
    
    public Substituent getSubstituent()
    {
        if ( substituent != null )
            return substituent;   
        
        log.warn("need to lookup Substituent '" + getResidueName() + "' here");
        return null;
    }
    
}
