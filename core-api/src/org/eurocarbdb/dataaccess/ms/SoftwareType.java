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
*   Last commit: $Rev: 1940 $ by $Author: khaleefah $ on $Date:: 2010-08-10 #$  
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
 *  eurocarb_devel.SoftwareType
 *  06/03/2010 20:22:49
 * 
 */
public class SoftwareType implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( SoftwareType.class );

    private Integer softwareTypeId;
    private String softwareType;
    private Set<org.eurocarbdb.dataaccess.ms.DataProcessing> dataProcessings = new HashSet<org.eurocarbdb.dataaccess.ms.DataProcessing>();

    public SoftwareType() {
    }

    public SoftwareType(Integer softwareTypeId, String softwareType) {
        this.softwareTypeId = softwareTypeId;
        this.softwareType = softwareType;
    }

    public SoftwareType(Integer softwareTypeId, String softwareType, Set<org.eurocarbdb.dataaccess.ms.DataProcessing> dataProcessings) {
        this.softwareTypeId = softwareTypeId;
        this.softwareType = softwareType;
        this.dataProcessings = dataProcessings;
    }

    public Integer getSoftwareTypeId() {
        return softwareTypeId;
    }

    public void setSoftwareTypeId(Integer softwareTypeId) {
        this.softwareTypeId = softwareTypeId;
    }

    public String getSoftwareType() {
        return softwareType;
    }

    public void setSoftwareType(String softwareType) {
        this.softwareType = softwareType;
    }

    public Set<org.eurocarbdb.dataaccess.ms.DataProcessing> getDataProcessings() {
        return dataProcessings;
    }

    public void setDataProcessings(Set<org.eurocarbdb.dataaccess.ms.DataProcessing> dataProcessings) {
        this.dataProcessings = dataProcessings;
    }
    //Quiries
    public static SoftwareType getByType(String softwareType)
    {
    	return (SoftwareType)getEntityManager().getQuery("org.eurocarbdb.dataaccess.ms.SoftwareType.GET_BY_TYPE")
		   .setParameter("softwareType",softwareType)
           .uniqueResult();
    }

}
