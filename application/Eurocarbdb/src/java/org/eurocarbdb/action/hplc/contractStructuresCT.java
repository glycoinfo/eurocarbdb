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

public class contractStructuresCT extends EurocarbAction implements RequiresLogin
{
   
    protected static final Logger logger = Logger.getLogger (contractStructuresCT.class.getName());
    

    public String execute() throws Exception 
    {

                SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session =sessionFactory.openSession();
        int countId = 0;
        //GlycanSequence gsStore = null;
        EntityManager em = Eurocarb.getEntityManager();
                                          
                logger.info("selecting all structures from contracts");

        List<StructuresGlycoct> StoredCt = (List<StructuresGlycoct>)getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.StructuresGlycoct.SELECT_ALL").list();
        for ( StructuresGlycoct storedCTSeq : StoredCt) {
        
            Integer glycanId = storedCTSeq.getGlycanId();
            String ctStructure = storedCTSeq.getSeqCt();
            logger.info("condensed ct" + ctStructure);            
            SugarSequence seq = new SugarSequence(ctStructure);
            GlycanSequence gs = GlycanSequence.lookupOrCreateNew(seq);
            logger.info("looking at glycan id" + glycanId);
            int id = gs.getGlycanSequenceId();
            logger.info("returned glycanseqid" + id);

            //if (id == 0) {
                logger.info("ended up here");

                /*Transaction tx = session.beginTransaction();  
                String insertStatement = "insert into core.glycan_sequence  (sequence_ct, sequence_ct_condensed, sequence_gws, sequence_iupac) select t.sequence_ct, t.sequence_ct_condensed, t.sequence_gws, t.sequence_iupac from temp.glycan_sequence t"; 
                session.createQuery( insertStatement ).executeUpdate();  
                tx.commit();  
                session.close(); 
                */
/*                GlycanSequence gsStore = null;
                gsStore.setSequenceIupac(ctStructure);
                gsStore.setSequenceCt(ctStructure);
                gsStore.setSequenceCtCondensed(ctStructure);
                gsStore.setSequenceGWS(ctStructure);
                gsStore.setContributor(Contributor.getCurrentContributor());
                //gsStore.setResidueCount(countId);
                Eurocarb.getEntityManager().store(gsStore); */
            //}
            int numberUpdates = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.ContractGlycans.UPDATE_STORED_CT")
                .setParameter("id", id)
                        .setParameter("glycanId", glycanId)
                .executeUpdate(); 

            int numberUpdatesStore = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.StructuresGlycoct.UPDATE_SEQ_ID")
            .setParameter("id", id)
                        .setParameter("glycanId", glycanId)
                .executeUpdate(); 
            }
             
             List<MultistructuresGlycoct> StoredMultiCt = (List<MultistructuresGlycoct>)getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.MultistructuresGlycoct.SELECT_CONTRACT_ALL").list();

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
