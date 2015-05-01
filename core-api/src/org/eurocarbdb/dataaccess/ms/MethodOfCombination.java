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


/**
 *  eurocarb_devel.AcquisitionToPersubstitution
 *  06/10/2010 00:59:00
 * 
 */
package org.eurocarbdb.dataaccess.ms;

import java.util.HashSet;
import java.util.Set;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
 *  eurocarb_devel.MethodOfCombination
 *  06/03/2010 20:22:49
 * 
 */
public class MethodOfCombination implements Serializable{

  /** Logging handle. */
    static final Logger log = Logger.getLogger( MethodOfCombination.class );


    private Integer methodOfCombinationId;
    private String methodOfCombination;
    private Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> sumAverageRelationships = new HashSet<org.eurocarbdb.dataaccess.ms.SumAverageRelationship>();

    public MethodOfCombination() {
    }

    public MethodOfCombination(Integer methodOfCombinationId, String methodOfCombination) {
        this.methodOfCombinationId = methodOfCombinationId;
        this.methodOfCombination = methodOfCombination;
    }

    public MethodOfCombination(Integer methodOfCombinationId, String methodOfCombination, Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> sumAverageRelationships) {
        this.methodOfCombinationId = methodOfCombinationId;
        this.methodOfCombination = methodOfCombination;
        this.sumAverageRelationships = sumAverageRelationships;
    }

    public Integer getMethodOfCombinationId() {
        return methodOfCombinationId;
    }

    public void setMethodOfCombinationId(Integer methodOfCombinationId) {
        this.methodOfCombinationId = methodOfCombinationId;
    }

    public String getMethodOfCombination() {
        return methodOfCombination;
    }

    public void setMethodOfCombination(String methodOfCombination) {
        this.methodOfCombination = methodOfCombination;
    }

    public Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> getSumAverageRelationships() {
        return sumAverageRelationships;
    }

    public void setSumAverageRelationships(Set<org.eurocarbdb.dataaccess.ms.SumAverageRelationship> sumAverageRelationships) {
        this.sumAverageRelationships = sumAverageRelationships;
    }

}
