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

import static org.eurocarbdb.sugar.RingConformation.OpenChain;

/** 
* 
*   @author mjh
*/
public class CarbohydrateChemistry 
{
    static CarbohydrateChemistry singleton = new CarbohydrateChemistry();
    
    
    public CarbohydrateChemistry()
    {
        // nothing
    }
    
    
    public static CarbohydrateChemistry getCarbohydrateChemistry()
    {
        return singleton;
    }
    
    
    public void checkAnomer( Anomer a, Monosaccharide m )
    throws SugarChemistryException
    {
        RingConformation rc = m.getRingConformation();
        if ( a.isDefinite() && rc == OpenChain )
        {
            throw new SugarChemistryException(
                "Cannot set Anomer=" 
                + a 
                + " on a Monosaccharide with RingConformation="
                + rc
            );
        }
    }


    public void checkRingConformation( RingConformation rc, Monosaccharide m )
    throws SugarChemistryException
    {
        int size = m.getSuperclass().size();
        String error = null;
        
        switch ( rc )  
        {
            case Furanose:
                if ( size < 4 )
                    error = "size < 4";
                break;
                
            case Pyranose:
                if ( size < 5 )
                    error = "size < 5";
                break;

            case OpenChain:
            case UnknownRingConformation:
            default:
                return;
        }
        
        if ( error != null )
        {
            throw new SugarChemistryException(
                "Monosaccharide '" 
                + m 
                + "' cannot adopt ring conformation '"
                + rc.name() 
                + "'; "
                + error
            );
        }
    }
    
    
    /*    
    public void checkSubstitution( Substituent s, Monosaccharide m, int position )
    {
           
    }
    */
    
}


