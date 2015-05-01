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

import java.util.HashSet;
import java.util.Set;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
 *  eurocarb_devel.SmallMolecule
 *  06/03/2010 20:22:49
 * 
 */
public class SmallMolecule implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( SmallMolecule.class );

    private Integer smallMoleculeId;
    private String name;
    private Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToSmallMolecule> peakAnnotatedToSmallMolecules = new HashSet<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToSmallMolecule>();
    private Set<org.eurocarbdb.dataaccess.ms.SmallMoleculeComposition> smallMoleculeCompositions = new HashSet<org.eurocarbdb.dataaccess.ms.SmallMoleculeComposition>();

    public SmallMolecule() {
    }

    public SmallMolecule(Integer smallMoleculeId, String name) {
        this.smallMoleculeId = smallMoleculeId;
        this.name = name;
    }

    public SmallMolecule(Integer smallMoleculeId, String name, Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToSmallMolecule> peakAnnotatedToSmallMolecules, Set<org.eurocarbdb.dataaccess.ms.SmallMoleculeComposition> smallMoleculeCompositions) {
        this.smallMoleculeId = smallMoleculeId;
        this.name = name;
        this.peakAnnotatedToSmallMolecules = peakAnnotatedToSmallMolecules;
        this.smallMoleculeCompositions = smallMoleculeCompositions;
    }

    public Integer getSmallMoleculeId() {
        return smallMoleculeId;
    }

    public void setSmallMoleculeId(Integer smallMoleculeId) {
        this.smallMoleculeId = smallMoleculeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToSmallMolecule> getPeakAnnotatedToSmallMolecules() {
        return peakAnnotatedToSmallMolecules;
    }

    public void setPeakAnnotatedToSmallMolecules(Set<org.eurocarbdb.dataaccess.ms.PeakAnnotatedToSmallMolecule> peakAnnotatedToSmallMolecules) {
        this.peakAnnotatedToSmallMolecules = peakAnnotatedToSmallMolecules;
    }

    public Set<org.eurocarbdb.dataaccess.ms.SmallMoleculeComposition> getSmallMoleculeCompositions() {
        return smallMoleculeCompositions;
    }

    public void setSmallMoleculeCompositions(Set<org.eurocarbdb.dataaccess.ms.SmallMoleculeComposition> smallMoleculeCompositions) {
        this.smallMoleculeCompositions = smallMoleculeCompositions;
    }

}
