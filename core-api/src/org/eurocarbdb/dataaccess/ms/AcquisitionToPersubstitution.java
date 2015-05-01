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

public class AcquisitionToPersubstitution implements Serializable{
 /** Logging handle. */
    static final Logger log = Logger.getLogger( AcquisitionToPersubstitution.class );


    private Integer AcquisitiontoPersubstitutionId;
    private Acquisition acquisition;
    private Persubstitution persubstitution;

    public AcquisitionToPersubstitution() {
    }

    public AcquisitionToPersubstitution(Integer AcquisitiontoPersubstitutionId) {
        this.AcquisitiontoPersubstitutionId = AcquisitiontoPersubstitutionId;
    }

    public AcquisitionToPersubstitution(Integer AcquisitiontoPersubstitutionId, Acquisition acquisition, Persubstitution persubstitution) {
        this.AcquisitiontoPersubstitutionId = AcquisitiontoPersubstitutionId;
        this.acquisition = acquisition;
        this.persubstitution = persubstitution;
    }

    public Integer getAcquisitiontoPersubstitutionId() {
        return AcquisitiontoPersubstitutionId;
    }

    public void setAcquisitiontoPersubstitutionId(Integer AcquisitiontoPersubstitutionId) {
        this.AcquisitiontoPersubstitutionId = AcquisitiontoPersubstitutionId;
    }

    public Acquisition getAcquisition() {
        return acquisition;
    }

    public void setAcquisition(Acquisition acquisition) {
        this.acquisition = acquisition;
    }

    public Persubstitution getPersubstitution() {
        return persubstitution;
    }

    public void setPersubstitution(Persubstitution persubstitution) {
        this.persubstitution = persubstitution;
    }

}
