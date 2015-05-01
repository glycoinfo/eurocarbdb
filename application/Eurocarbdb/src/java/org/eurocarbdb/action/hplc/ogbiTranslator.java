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

import java.io.IOException;
import java.lang.*;
import java.util.*;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCT;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCT;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;

import org.eurocarbdb.MolecularFramework.io.OGBI.SugarImporterOgbi;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;


import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
/*import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.GlycanParserFactory;
import org.eurocarbdb.application.glycanbuilder.MassOptions;
import org.eurocarbdb.application.glycanbuilder.GlycanParser;
*/
import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.eurocarbdb.action.*;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;
import org.eurocarbdb.dataaccess.core.*;

//Hibernate Class removed
//import org.eurocarbdb.dataaccess.hibernate.HibernateUtil;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.*;
import org.hibernate.cfg.*;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
* @author matthew using renes translation code!
*
*/
public class ogbiTranslator extends EurocarbAction implements RequiresLogin
{
    private List<Glycan> HplcStructures;
    private List<GlycanSourceLink> GlycanSourceInfo;
 

    protected static final Logger logger = Logger.getLogger (ogbiTranslator.class.getName());

    // private GlycanSequence glycoCT;
    //private GlycanSequence glyseq = new GlycanSequence();
      

    public String execute() throws Exception 
    {

                SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session =sessionFactory.openSession();

        EntityManager em = Eurocarb.getEntityManager();
        em.beginUnitOfWork();
        //below two lines removed due to loss of HibernateUtil
        //Session s = HibernateUtil.getSession();
        //Transaction tx = s.beginTransaction();

            //query all structures to be translated
                           
                logger.info("selecting all HPLC glycan entries");

        List<StructuresGlycoct> StoredCt = (List<StructuresGlycoct>)getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.StructuresGlycoct.SELECT_ALL").list();
        for ( StructuresGlycoct storedCTSeq : StoredCt) {
        
            Integer glycanId = storedCTSeq.getGlycanId();
            String ctStructure = storedCTSeq.getSeqCt();
            //i dont think any translation is required with sugar objects lets try
            SugarSequence seq = new SugarSequence(ctStructure);
            GlycanSequence gs = GlycanSequence.lookupOrCreateNew(seq);
            
            int id = gs.getGlycanSequenceId();
            logger.info("for the hell of it tell me the id:" + id);
            int numberUpdates = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Glycan.UPDATE_STORED_CT")
                .setParameter("id", id)
                        .setParameter("glycanId", glycanId)
                .executeUpdate(); 

            int numberUpdatesStore = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.StructuresGlycoct.UPDATE_SEQ_ID")
            .setParameter("id", id)
                        .setParameter("glycanId", glycanId)
                .executeUpdate(); 
            }

        List<MultistructuresGlycoct> StoredMultiCt = (List<MultistructuresGlycoct>)getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.MultistructuresGlycoct.SELECT_ALL").list();

        for ( MultistructuresGlycoct storedMultiCt : StoredMultiCt) {
            Integer glycanMultiId = storedMultiCt.getGlycanId();
            String ctMultiStructure = storedMultiCt.getSeqCt();
            SugarSequence seqMulti = new SugarSequence(ctMultiStructure);
            GlycanSequence gsMulti = GlycanSequence.lookupOrCreateNew(seqMulti);
            int idMulti = gsMulti.getGlycanSequenceId();
            logger.info("and tell me again:" + idMulti + "for glycanId" + glycanMultiId);
            int numberMultiUpdates = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.MultistructuresGlycoct.UPDATE_SEQ_ID")
            .setParameter("idMulti", idMulti)
                        .setParameter("glycanMultiId", glycanMultiId)
                .executeUpdate(); 

        }
                        
                        
        return SUCCESS;
         
 
 }
}
