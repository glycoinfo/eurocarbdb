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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
package org.eurocarbdb.dataaccess.hplc;
// Generated 21-Jan-2008 14:42:44 by Hibernate Tools 3.2.0.b9

//  stdlib imports
import java.util.HashSet;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.hplc.Profile;
import org.eurocarbdb.dataaccess.core.Evidence;

import org.eurocarbdb.dataaccess.BasicEurocarbObject;

//public class Content extends Profile implements Serializable {
public class Content extends BasicEurocarbObject implements java.io.Serializable {

     private int contentId;
     private int taxonomyId;
     private int tissueId;
     private int diseaseId;
     private int perturbationId;
     private int taxonomyNcbiId;
     private String tissueMeshId;
     private String diseaseMeshId;
     private String perturbationMeshId;
    private  Profile profile;
    private int contributorId;
    private int parentProfileId;

    public Content() {
    }

    
    public Content(int parentProfileId, int contributorId, int contentId, int taxonomyId, int tissueId, int diseaseId, int perturbationId, int taxonomyNcbiId, String tissueMeshId, String diseaseMeshId, String perturbationMeshId) {
        this.contentId = contentId;
        this.taxonomyId = taxonomyId;
        this.tissueId = tissueId;
        this.diseaseId = diseaseId;
        this.perturbationId = perturbationId;
        this.taxonomyNcbiId = taxonomyNcbiId;
        this.tissueMeshId = tissueMeshId;
        this.diseaseMeshId = diseaseMeshId;
        this.perturbationMeshId = perturbationMeshId;
        this.contributorId = contributorId;
        this.parentProfileId = parentProfileId;
    }

    public int getContentId() {
        return this.contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public int getParentProfileId() {
        return this.parentProfileId;
    }

    public void setParentProfileId(int parentProfileId) {
        this.parentProfileId = parentProfileId;
    }
    
    public int getContributorId() {
        return this.contributorId;
    }

    public void setContributorId(int contributorId) {
        this.contributorId = contributorId;
    }
    
    public int getTaxonomyId() {
        return this.taxonomyId;
    }
    
    public void setTaxonomyId(int taxonomyId) {
        this.taxonomyId = taxonomyId;
    }

    public int getTissueId() {
        return this.tissueId;
    }
    
    public void setTissueId(int tissueId) {
        this.tissueId = tissueId;
    }

    public int getDiseaseId() {
        return this.diseaseId;
    }
    
    public void setDiseaseId(int diseaseId) {
        this.diseaseId = diseaseId;
    }

    public int getPerturbationId() {
        return this.perturbationId;
    }
    
    public void setPerturbationId(int perturbationId) {
        this.perturbationId = perturbationId;
    }

    public int getTaxonomyNcbiId() {
        return this.taxonomyNcbiId;
    }
    
    public void setTaxonomyNcbiId(int taxonomyNcbiId) {
        this.taxonomyNcbiId = taxonomyNcbiId;
    }

    public String getTissueMeshId() {
        return this.tissueMeshId;
    }
    
    public void setTissueMeshId(String tissueMeshId) {
        this.tissueMeshId = tissueMeshId;
    }

    public String getDiseaseMeshId() {
        return this.diseaseMeshId;
    }
    
    public void setDiseaseMeshId(String diseaseMeshId) {
        this.diseaseMeshId = diseaseMeshId;
    }

    public String getPerturbationMeshId() {
        return this.perturbationMeshId;
    }
    
    public void setPerturbationMeshId(String perturbationMeshId) {
        this.perturbationMeshId = perturbationMeshId;
    }

    public Profile getProfile() {
    return profile;
    }

    public void setProfile(Profile profile) {
    this.profile = profile;
    }

    

}


