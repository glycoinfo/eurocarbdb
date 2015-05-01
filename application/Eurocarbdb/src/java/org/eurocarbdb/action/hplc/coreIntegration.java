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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/
package org.eurocarbdb.action.hplc;

import java.util.*;
import java.lang.*;

import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.eurocarbdb.action.*;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;
import org.eurocarbdb.dataaccess.core.*;

//below class removed
//import org.eurocarbdb.dataaccess.hibernate.HibernateUtil;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.*;
import org.hibernate.cfg.*;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

//script to update glycan structures with tax, tissue, pert... contents

public class coreIntegration extends EurocarbAction implements RequiresLogin
{
    EntityManager em = Eurocarb.getEntityManager();

    protected static final Logger logger = Logger.getLogger (coreIntegration.class.getName());
    
    public String execute() throws Exception {

        logger.info("Selecting all HPLC glycan entries");

              /*  List<Glycan> HplcStructures = (List<Glycan>)getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Glycan.GLYCOBASE").list();
        for (Glycan glycanCore : HplcStructures) {
          Integer glycanIdLookup = glycanCore.getGlycanId();
          Integer glycanTranslation = glycanCore.getOgbitranslation();
        Integer translation = 629;
          GlycanSequence addTaxonomy = em.lookup (GlycanSequence.class, glycanTranslation);
          Integer glycanSequenceId = addTaxonomy.getGlycanSequenceId();
          logger.info("found glycan in core table with id: " + glycanSequenceId);
            
          now get the ncbi_id for the taxonomy
          List<GlycanTaxonomy> testrefine = em.getQuery("org.eurocarbdb.dataaccess.hplc.GlycanTaxonomy.SELECT_AND_GROUP").setParameter("glycanId", glycanIdLookup).list();
        }*/

        List<GlycanTaxonomy> taxList = em.getQuery("org.eurocarbdb.dataaccess.hplc.GlycanTaxonomy.SELECT_AND_GROUP").list();
        for (Iterator iterTax = taxList.iterator(); iterTax.hasNext();) {

        //for (GlycanTaxonomy taxCore : taxList) {
            //Integer taxTransId = taxCore.getOgbitranslation();
             //Integer taxNcbiId = taxCore.getNcbiId(); 
            Object [] taxObject = (Object []) iterTax.next();
                        Integer taxTransId = (Integer) taxObject[0];
                        Integer taxNcbiId = (Integer) taxObject[1];

            GlycanSequence addTaxInfo = em.lookup (GlycanSequence.class, taxTransId);
                        Taxonomy taxonomy = Taxonomy.lookupNcbiId(taxNcbiId);
                        if (taxonomy!=null && addTaxInfo !=null) {
                                BiologicalContext bcTax = new BiologicalContext();
                                addTaxInfo.addBiologicalContext( bcTax );
                                getEntityManager().update( addTaxInfo );
                        }
                }

        List<GlycanDisease> taxDisease = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.GlycanDisease.SELECT_AND_GROUP").list();
            for (Iterator iterDisease = taxDisease.iterator(); iterDisease.hasNext();) {
                        //for (GlycanDisease taxDisease : taxDisease) {
                        Object [] diseaseObject = (Object []) iterDisease.next();
                        //String diseaseMeshId = taxDisease.getMeshId();
                        //Integer diseaseTransId = taxDisease.getOgbitranslation();
                        String diseaseMeshId = (String) diseaseObject[1];
            Integer diseaseTransId = (Integer) diseaseObject[0];
            logger.info("the diseaseMeshId:" + diseaseMeshId);
                        logger.info("the diseaseTransId:" + diseaseTransId);

            if (diseaseMeshId !=null){
                        GlycanSequence addDiseaseInfo = em.lookup (GlycanSequence.class, diseaseTransId);
                        Disease disease= Disease.lookupByMeshId(diseaseMeshId);

                        if (disease!=null && addDiseaseInfo !=null) {
                                BiologicalContext bcDisease = new BiologicalContext();
                                addDiseaseInfo.addBiologicalContext( bcDisease );
                                getEntityManager().update( addDiseaseInfo );
                        }
            }

                 }
        
        List<GlycanPerturbation> pertInfo = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.GlycanPerturbation.SELECT_AND_GROUP")
                        .list();

                        //for (GlycanPerturbation pertCore : pertInfo ) {
                        for (Iterator iterPert = pertInfo.iterator(); iterPert.hasNext();) {
            //String pertMeshId = pertCore.getMeshId();
                        //Integer pertTransId = pertCore.getOgbitranslation();
            Object [] pertObject = (Object []) iterPert.next();
            String pertMeshId = (String) pertObject[1];
            Integer pertTransId = (Integer) pertObject[0];
                        
            if (pertMeshId !=null ) {
            GlycanSequence addPertInfo = em.lookup (GlycanSequence.class, pertTransId);
                        Perturbation perturbation= Perturbation.lookupByMeshId(pertMeshId);
                        if (perturbation!=null && addPertInfo !=null) {
                                BiologicalContext bcPert = new BiologicalContext();
                                addPertInfo.addBiologicalContext( bcPert );
                                getEntityManager().update( addPertInfo );
                        }
            }

                  }

            
           List<GlycanTissue> tissueInfo = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.GlycanTissue.SELECT_AND_GROUP").list();
                        

                        //for (GlycanTissue tissueCore : tissueInfo ) {
                        for (Iterator iterTissue = tissueInfo.iterator(); iterTissue.hasNext();) {
            Object [] tissueObject = (Object []) iterTissue.next();
            String tissueMeshId = (String) tissueObject[1];
            Integer tissueTransId = (Integer) tissueObject[0];
            //String tissueMeshId = tissueCore.getMeshId();
                        //Integer tissueTransId = tissueCore.getOgbitranslation();

            
            if (tissueMeshId !=null) {
                        GlycanSequence addTissueInfo = em.lookup (GlycanSequence.class, tissueTransId);
                        TissueTaxonomy tissue= TissueTaxonomy.lookupByMeshId(tissueMeshId);
                        if (tissue!=null && addTissueInfo !=null) {
                                BiologicalContext bcTissue = new BiologicalContext();
                                addTissueInfo.addBiologicalContext( bcTissue );
                                getEntityManager().update( addTissueInfo );
                        }
            }
                        }


        
    return SUCCESS;        
        }

    
    }



