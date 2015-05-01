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
import java.util.List;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
 *  eurocarb_devel.Persubstitution
 *  06/10/2010 00:59:00
 * 
 */
public class Persubstitution implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( Persubstitution.class );

    private Integer persubstitutionId;
    private String abbreviation;
    private String name;
    private Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> peakAnnotateds = new HashSet<org.eurocarbdb.dataaccess.ms.PeakAnnotated>();
    private Set<org.eurocarbdb.dataaccess.ms.AcquisitionToPersubstitution> AcquisitionToPersubstitutions = new HashSet<org.eurocarbdb.dataaccess.ms.AcquisitionToPersubstitution>();

    public Persubstitution() {
    }

    public Persubstitution(Integer persubstitutionId, String abbreviation, String name) {
        this.persubstitutionId = persubstitutionId;
        this.abbreviation = abbreviation;
        this.name = name;
    }

    public Persubstitution(Integer persubstitutionId, String abbreviation, String name, Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> peakAnnotateds, Set<org.eurocarbdb.dataaccess.ms.AcquisitionToPersubstitution> AcquisitionToPersubstitutions) {
        this.persubstitutionId = persubstitutionId;
        this.abbreviation = abbreviation;
        this.name = name;
        this.peakAnnotateds = peakAnnotateds;
        this.AcquisitionToPersubstitutions = AcquisitionToPersubstitutions;
    }

    public Integer getPersubstitutionId() {
        return persubstitutionId;
    }

    public void setPersubstitutionId(Integer persubstitutionId) {
        this.persubstitutionId = persubstitutionId;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> getPeakAnnotateds() {
        return peakAnnotateds;
    }

    public void setPeakAnnotateds(Set<org.eurocarbdb.dataaccess.ms.PeakAnnotated> peakAnnotateds) {
        this.peakAnnotateds = peakAnnotateds;
    }

    public Set<org.eurocarbdb.dataaccess.ms.AcquisitionToPersubstitution> getAcquisitionToPersubstitutions() {
        return AcquisitionToPersubstitutions;
    }

    public void setAcquisitionToPersubstitutions(Set<org.eurocarbdb.dataaccess.ms.AcquisitionToPersubstitution> AcquisitionToPersubstitutions) {
        this.AcquisitionToPersubstitutions = AcquisitionToPersubstitutions;
    }
     //////////////////////////////////////////////////////////////////////////// Static Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    @SuppressWarnings("unchecked")
	public static List<Persubstitution> getPersubstitution()
{
     return (List<Persubstitution>) getEntityManager()
            .getQuery("org.eurocarbdb.dataaccess.ms.Persubstitution.GET_ALL")
            .list();
}

    public static Persubstitution lookupPid(int pid) {
        Object p_id = getEntityManager()
                  .getQuery( "org.eurocarbdb.dataaccess.ms.Persubstitution.BY_ID" )
                  .setParameter("pid", pid )
                  .uniqueResult();

        assert p_id instanceof Persubstitution;
        
        return (Persubstitution) p_id;
    }

    public static Persubstitution getByAbbreviation(String abbr)
    {
    	return (Persubstitution) getEntityManager().getQuery("org.eurocarbdb.dataaccess.ms.Persubstitution.BY_ABBREVIATION")
    	                                           .setParameter("abbreviation", abbr)
    	                                           .uniqueResult();
    	
    }

}
