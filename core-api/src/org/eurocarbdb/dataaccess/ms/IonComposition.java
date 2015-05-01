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
*   Last commit: $Rev: 1916 $ by $Author: Khalifeh $ on $Date:: 2010-06-16 #$  
*/


/**
 *  eurocarb_devel.AcquisitionToPersubstitution
 *  06/10/2010 00:59:00
 * 
 */
package org.eurocarbdb.dataaccess.ms;

import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
 *  eurocarb_devel.IonComposition
 *  06/03/2010 20:22:50
 * 
 */
public class IonComposition implements Serializable {
   /** Logging handle. */
    static final Logger log = Logger.getLogger( IonComposition.class );

    private Integer ionCompositionId;
    private Ion ion;
    private String atomType;
    private Integer number;

    public IonComposition() {
    }

    public IonComposition(Integer ionCompositionId, String atomType, Integer number) {
        this.ionCompositionId = ionCompositionId;
        this.atomType = atomType;
        this.number = number;
    }

    public IonComposition(Integer ionCompositionId, Ion ion, String atomType, Integer number) {
        this.ionCompositionId = ionCompositionId;
        this.ion = ion;
        this.atomType = atomType;
        this.number = number;
    }

    public Integer getIonCompositionId() {
        return ionCompositionId;
    }

    public void setIonCompositionId(Integer ionCompositionId) {
        this.ionCompositionId = ionCompositionId;
    }

    public Ion getIon() {
        return ion;
    }

    public void setIon(Ion ion) {
        this.ion = ion;
    }

    public String getAtomType() {
        return atomType;
    }

    public void setAtomType(String atomType) {
        this.atomType = atomType;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

}
