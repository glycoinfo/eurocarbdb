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
 *  eurocarb_devel.SumAverageRelationship
 *  06/03/2010 20:22:49
 * 
 */
public class SumAverageRelationship implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( SumAverageRelationship.class );

    private Integer sumAverageRelationshipId;
    private org.eurocarbdb.dataaccess.ms.Scan scanByScanId;
    private MethodOfCombination methodOfCombination;
    private org.eurocarbdb.dataaccess.ms.Scan scanBySubsetScanId;

    public SumAverageRelationship() {
    }

    public SumAverageRelationship(Integer sumAverageRelationshipId) {
        this.sumAverageRelationshipId = sumAverageRelationshipId;
    }

    public SumAverageRelationship(Integer sumAverageRelationshipId, org.eurocarbdb.dataaccess.ms.Scan scanByScanId, MethodOfCombination methodOfCombination, org.eurocarbdb.dataaccess.ms.Scan scanBySubsetScanId) {
        this.sumAverageRelationshipId = sumAverageRelationshipId;
        this.scanByScanId = scanByScanId;
        this.methodOfCombination = methodOfCombination;
        this.scanBySubsetScanId = scanBySubsetScanId;
    }

    public Integer getSumAverageRelationshipId() {
        return sumAverageRelationshipId;
    }

    public void setSumAverageRelationshipId(Integer sumAverageRelationshipId) {
        this.sumAverageRelationshipId = sumAverageRelationshipId;
    }

    public org.eurocarbdb.dataaccess.ms.Scan getScanByScanId() {
        return scanByScanId;
    }

    public void setScanByScanId(org.eurocarbdb.dataaccess.ms.Scan scanByScanId) {
        this.scanByScanId = scanByScanId;
    }

    public MethodOfCombination getMethodOfCombination() {
        return methodOfCombination;
    }

    public void setMethodOfCombination(MethodOfCombination methodOfCombination) {
        this.methodOfCombination = methodOfCombination;
    }

    public org.eurocarbdb.dataaccess.ms.Scan getScanBySubsetScanId() {
        return scanBySubsetScanId;
    }

    public void setScanBySubsetScanId(org.eurocarbdb.dataaccess.ms.Scan scanBySubsetScanId) {
        this.scanBySubsetScanId = scanBySubsetScanId;
    }

}
