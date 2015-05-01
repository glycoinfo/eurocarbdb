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
*   Last commit: $Rev: 1922 $ by $Author: khaleefah $ on $Date:: 2010-06-18 #$  
*/
package org.eurocarbdb.dataaccess.ms;

import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
 *  eurocarb_devel.SmallMoleculeComposition
 *  06/03/2010 20:22:49
 * 
 */
public class SmallMoleculeComposition implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( SmallMoleculeComposition.class );



    private Integer smallMoleculeCompositionId;
    private SmallMolecule smallMolecule;
    private String atomType;
    private Integer number;

    public SmallMoleculeComposition() {
    }

    public SmallMoleculeComposition(Integer smallMoleculeCompositionId, String atomType, Integer number) {
        this.smallMoleculeCompositionId = smallMoleculeCompositionId;
        this.atomType = atomType;
        this.number = number;
    }

    public SmallMoleculeComposition(Integer smallMoleculeCompositionId, SmallMolecule smallMolecule, String atomType, Integer number) {
        this.smallMoleculeCompositionId = smallMoleculeCompositionId;
        this.smallMolecule = smallMolecule;
        this.atomType = atomType;
        this.number = number;
    }

    public Integer getSmallMoleculeCompositionId() {
        return smallMoleculeCompositionId;
    }

    public void setSmallMoleculeCompositionId(Integer smallMoleculeCompositionId) {
        this.smallMoleculeCompositionId = smallMoleculeCompositionId;
    }

    public SmallMolecule getSmallMolecule() {
        return smallMolecule;
    }

    public void setSmallMolecule(SmallMolecule smallMolecule) {
        this.smallMolecule = smallMolecule;
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
