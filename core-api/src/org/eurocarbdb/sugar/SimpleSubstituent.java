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
*   Last commit: $Rev: 1259 $ by $Author: glycoslave $ on $Date:: 2009-06-26 #$  
*/

package org.eurocarbdb.sugar;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;


/**
*
*   @author mjh
*/
public class SimpleSubstituent extends BasicMolecule implements Substituent
{
    
    public SimpleSubstituent( Molecule m )
    {
        this(
            m.getFullName(),
            m.getName(),
            null, // type
            null, // m.getComposition(),
            m.getMass(),
            m.getAvgMass()
        );   
    }
    
    
    public SimpleSubstituent( String full_name
                            , String name
                            , String type
                            , Composition c
                            , double mass
                            , double avg_mass )
    {
        super( full_name, name, type, c, mass, avg_mass );
    }
    
    
    public SimpleSubstituent clone()
    {
        return new SimpleSubstituent(
            this.full_name,
            this.name,
            this.type, 
            this.composition,
            this.mass,
            this.avg_mass
        );    
    }

    
    public boolean causesStereoloss()
    {
        return false;   
    }
    
    
    public String toString()
    {
        return "[" + getClass().getSimpleName() + "=" + name + "]";
    }
    
}
