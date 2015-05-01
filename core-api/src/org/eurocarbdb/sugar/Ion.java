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
package org.eurocarbdb.sugar;

public interface Ion extends Molecule
{
    
    /*  getCharge  *//****************************************
    *
    *   Retrieves the current charge state. The returned value will
    *   be positive for a positively charged ion, and negative for
    *   a negatively-charged ion. Returns 0 if this species is currently
    *   uncharged (meaning it's not really an ion, but this is a necessary
    *   evil for code consistency).
    *
    *   @return 
    *   Current charge state.
    */
    public int getCharge();
    

    /*  addChargedComponent  *//****************************************
    *
    */
    public void addChargedComponent( Ion adduct );
    
    
    /*  getChargedComponents  *//****************************************
    *
    *   @return
    */
    public Ion[] getChargedComponents();
    
    
    /*  setChargedComponents  *//****************************************
    *
    *   @param ions
    */
    public void setChargedComponents( Ion[] adducts );
        
    
    /*  getMZ  *//***************************************************
    *   
    *   Retrieves the mass/charge (face) value of this ion.
    */
    public double getMZ();
 
    
}
