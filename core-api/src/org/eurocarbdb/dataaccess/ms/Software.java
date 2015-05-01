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
*   Last commit: $Rev: 1981 $ by $Author: hasysf $ on $Date:: 2010-09-02 #$  
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
 *  eurocarb_devel.Software
 *  06/03/2010 20:22:50
 * 
 */
public class Software implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( Software.class );


    private Integer softwareId;
    private String name;
    private String softwareVersion;
    private Set<org.eurocarbdb.dataaccess.ms.DataProcessing> dataProcessings = new HashSet<org.eurocarbdb.dataaccess.ms.DataProcessing>();

    public Software() {
    }

    public Software(Integer softwareId, String name, String softwareVersion) {
        this.softwareId = softwareId;
        this.name = name;
        this.softwareVersion = softwareVersion;
    }

    public Software(Integer softwareId, String name, String softwareVersion, Set<org.eurocarbdb.dataaccess.ms.DataProcessing> dataProcessings) {
        this.softwareId = softwareId;
        this.name = name;
        this.softwareVersion = softwareVersion;
        this.dataProcessings = dataProcessings;
    }

    public Integer getSoftwareId() {
        return softwareId;
    }

    public void setSoftwareId(Integer softwareId) {
        this.softwareId = softwareId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public Set<org.eurocarbdb.dataaccess.ms.DataProcessing> getDataProcessings() {
        return dataProcessings;
    }

    public void setDataProcessings(Set<org.eurocarbdb.dataaccess.ms.DataProcessing> dataProcessings) {
        this.dataProcessings = dataProcessings;
    }
     public static Software getByNameAndVersion(String swName, String swVersion)
    {
    	return (Software)getEntityManager().getQuery("org.eurocarbdb.dataaccess.ms.Software.GET_BY_NAME_VERSION")
    					   .setParameter("swName",swName)
    					   .setParameter("swVersion",swVersion)
    	                                   .uniqueResult();
    }
     public static Software getByName(String swName)
     {
         return (Software)getEntityManager().getQuery("org.eurocarbdb.dataaccess.ms.Software.GET_BY_NAME")
                                            .setParameter("swName",swName)
                                            .uniqueResult();
     }

}
