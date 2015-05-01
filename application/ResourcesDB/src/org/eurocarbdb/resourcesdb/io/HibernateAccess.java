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
package org.eurocarbdb.resourcesdb.io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eurocarbdb.resourcesdb.*;
import org.eurocarbdb.resourcesdb.atom.*;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.nonmonosaccharide.*;
import org.eurocarbdb.resourcesdb.representation.ResidueRepresentation;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.*;
import org.eurocarbdb.resourcesdb.util.HibernateUtil;
import org.hibernate.*;
import org.hibernate.criterion.*;

/**
* This class stores the methods to access the database via hibernate.
* It was introduced to separate the database access from the actual residue objects for easy generation of "hibernate-free" standalone versions for use in Applets.
* 
* @author Thomas Lütteke
*/
public class HibernateAccess {

//    *****************************************************************************
//    *** Elements data: **********************************************************
//    *****************************************************************************

    public static boolean writeElementsToDB() throws ResourcesDbException {
        //*** store elements in db: ***
        Session hbSession = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = hbSession.beginTransaction();
        try {
            for(Periodic elem : Periodic.values()) {
                System.out.println("write element " + elem.getSymbol() + "...");
                hbSession.save(elem);
                for(Isotope iso : elem.getIsotopes()) {
                    System.out.println("   write isotope " + iso.getNeutrons() + "...");
                    hbSession.save(iso);
                }
            }
            hbSession.getTransaction().commit();
        } catch(Exception e) {
            System.err.println(e);
            tx.rollback();
            return(false);
        }
        return(true);
    }
    
    @SuppressWarnings("unchecked")
    public static ArrayList<Periodic> getElementListFromDB() {
        ArrayList<Periodic> resultList = null;
        Session hbSession = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = hbSession.beginTransaction();
        try {
            resultList = (ArrayList<Periodic>) hbSession.createQuery("from Element").list();
        } catch(Exception e) {
            tx.rollback();
        }
        return resultList;
    }

//    *****************************************************************************
//    *** Basetype data: **********************************************************
//    *****************************************************************************

    public static boolean writeBasetypeToDB(Basetype bt, BasetypeTemplateContainer container) {
        return writeBasetypeToDB(bt, container, true);
    }
    
    public static boolean writeBasetypeToDB(Basetype bt, BasetypeTemplateContainer container, boolean commit) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            if(bt.getName() == null) {
                bt.buildName();
            }
            if(bt.getSuperclass() == null) {
                bt.setSuperclass(container.getSuperclassTemplateBySize(bt.getSize()).getBaseName());
            }
            if(bt.getIsSuperclassFlag() == null) {
                bt.checkIsSuperclass();
            }
            if(bt.getComposition() == null) {
                bt.buildComposition();
            }
            session.save(bt);
            for(CoreModification mod : bt.getCoreModifications()) {
                session.save(mod);
            }
            if(bt.getAtoms() == null) {
                bt.buildAtoms();
            }
            for(Atom a : bt.getAtoms()) {
                session.save("BasetypeAtom", a);
            }
            for(Atom a : bt.getAtoms()) {
                for(AtomConnection ac : a.getConnections()) {
                    session.save("BasetypeAtomConnection", ac);
                }
            }
            if(commit) {
                tx.commit();
            }
        } catch(Exception e) {
            tx.rollback();
            System.err.println("Exception in HibernateAccess.writeBasetypeToDB(bt): " + e);
            return(false);
        }
        return(true);
    }
    
    public static Basetype getBasetypeFromDB(String name) {
        Basetype bt = null;
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            bt = (Basetype) session.createQuery("from Basetype where name='" + name + "'").uniqueResult();
        } catch(Exception e) {
            tx.rollback();
        }
        return(bt);
    }
    
    public static Basetype getBasetypeFromDB(int id) {
        Basetype bt = null;
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            bt = (Basetype) session.createQuery("from Basetype where id=" + id).uniqueResult();
        } catch(Exception e) {
            tx.rollback();
        }
        return(bt);
    }
    
    public static Basetype storeOrUpdateBasetype(Basetype bt, BasetypeTemplateContainer container) throws ResourcesDbException {
        return storeOrUpdateBasetype(bt, container, true);
    }
    
    public static Basetype storeOrUpdateBasetype(Basetype bt, BasetypeTemplateContainer container, boolean commit) throws ResourcesDbException {
        if(bt.getName() == null) {
            bt.buildName();
        }
        Basetype dbBasetype = HibernateAccess.getBasetypeFromDB(bt.getName());
        if(dbBasetype == null) { //*** basetype not yet present in DB ***
            HibernateAccess.writeBasetypeToDB(bt, container, commit);
            return(bt);
        }
        return(dbBasetype);
    }
    
//    *****************************************************************************
//    *** Monosaccharide data: ****************************************************
//    *****************************************************************************

    /**
     * Get a monosaccharide from the database identified by name
     * @param name the name of the monosaccharide
     * @return the monosaccharide
     */
    public static Monosaccharide getMonosaccharideFromDB(String name) {
        Monosaccharide ms = null;
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            ms = (Monosaccharide) session.createQuery("from Monosaccharide where name='" + name + "'").uniqueResult();
        } catch(Exception e) {
            tx.rollback();
        }
        return(ms);
    }
    
    /**
     * Get a monosaccharide from the database identified by the database id
     * @param id the database id of the monosaccharide
     * @return the monosaccharide
     */
    public static Monosaccharide getMonosaccharideFromDB(int id) {
        Monosaccharide ms = null;
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            ms = (Monosaccharide) session.createQuery("from Monosaccharide where id=" + id).uniqueResult();
        } catch(Exception e) {
            tx.rollback();
        }
        return(ms);
    }
    
    /**
     * Get a list of monosaccharides from the database that match the properties of a given monosaccharide.
     * If the given monosaccharide contains unknown properties, they are ignored in the search. 
     * @param mono the monosaccharide that defines the search parameters
     * @return a list of monosaccharides matching the given one
     */
    public static ArrayList<Monosaccharide> getMonosaccharideListByFuzzyMonosaccharide(Monosaccharide mono) {
        return getMonosaccharideListByFuzzyMonosaccharide(mono, true, true);
    }
    
    public static ArrayList<Monosaccharide> getMonosaccharideListByFuzzyMonosaccharide(Monosaccharide mono, boolean matchCoremodCount, boolean matchSubstCount) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
        //*** prepare Criteria object: ***
        Criteria query = session.createCriteria(Monosaccharide.class);
        query.createAlias("basetype", "bt");
        if(mono.countCoreModifications() > 0) {
            query.createAlias("bt.coreModifications", "mod");
        }
        if(mono.countSubstitutions() > 0) {
            query.createAlias("substitutions", "subst", Criteria.LEFT_JOIN);
        }
        
        //*** add restrictions: ***
        
        Conjunction monoJunc    = Restrictions.conjunction();
        query.add(monoJunc);

        //*** size restriction: ***
        if(mono.getSize() > 0) {
            monoJunc.add(Restrictions.eq("bt.size", mono.getSize()));
        }
        
        //*** stereocode restrictions: ***
        if(mono.getStereoStr() != null && mono.getStereoStr().length() > 0) {
            String stereo1 = null;
            String stereo2 = null;
            if(mono.getStereocode().hasRelativePosition()) {
                try {
                    stereo1 = Stereocode.relativeToAbsolute(mono.getStereoStr());
                    stereo2 = Stereocode.relativeToAbsolute(Stereocode.changeRelativeDLinStereoString(mono.getStereoStr()));
                    stereo2 = stereo2.replaceAll("" + Stereocode.StereoX, "_");
                } catch(ResourcesDbException me) {
                    //*** stereocode causes problems, doesn't make sense to use it for search then, therefore nothing needs to be done here ***
                    if(Config.getGlobalConfig().isPrintErrorMsgs()) {
                        System.err.println(me);
                        me.printStackTrace();
                    }
                }
            } else {
                stereo1 = mono.getStereoStr();
            }
            if(stereo1 != null) {
                stereo1 = stereo1.replaceAll("" + Stereocode.StereoX, "_");
                if(stereo2 != null) {
                    monoJunc.add(Restrictions.disjunction().add(Restrictions.like("bt.stereoStr", stereo1)).add(Restrictions.like("bt.stereoStr", stereo2)));
                } else {
                    monoJunc.add(Restrictions.like("bt.stereoStr", stereo1));
                }
            }
        }
        
        //*** anomeric restriction: ***
        if(mono.getAnomer() != null && !mono.getAnomer().equals(Anomer.UNKNOWN)) {
            monoJunc.add(Restrictions.eq("bt.anomerSymbol", mono.getAnomer().getSymbol()));
        }
        
        //*** ring restrictions: ***
        if(mono.getRingStart() != Basetype.UNKNOWN_RING) {
            monoJunc.add(Restrictions.eq("bt.ringStart", mono.getRingStart()));
        }
        if(mono.getRingEnd() != Basetype.UNKNOWN_RING) {
            monoJunc.add(Restrictions.eq("bt.ringEnd", mono.getRingEnd()));
        }
        
        //*** core modification restrictions: ***
        if(mono.countCoreModifications() > 0) {
            Conjunction modListConjunct = Restrictions.conjunction();
            for(CoreModification mod : mono.getCoreModifications()) {
                Conjunction modConjunct = Restrictions.conjunction();
                modConjunct.add(Restrictions.eq("mod.name", mod.getName()));
                if(mod.getPosition1().size() > 1) {
                    Disjunction modPos1Disjunct = Restrictions.disjunction();
                    for(Integer pos1 : mod.getPosition1()) {
                        if(pos1.intValue() != 0) {
                            modPos1Disjunct.add(Restrictions.eq("mod.intValuePosition1", pos1));
                        }
                    }
                    modConjunct.add(modPos1Disjunct);
                } else if(mod.getIntValuePosition1() > 0) {
                    modConjunct.add(Restrictions.eq("mod.intValuePosition1", mod.getIntValuePosition1()));
                }
                if(mod.hasPosition2()) {
                    if(mod.getPosition2().size() > 1) {
                        Disjunction modPos2Disjunct = Restrictions.disjunction();
                        for(Integer pos2 : mod.getPosition2()) {
                            if(pos2.intValue() != 0) {
                                modPos2Disjunct.add(Restrictions.eq("mod.intValuePosition2", pos2));
                            }
                        }
                        modConjunct.add(modPos2Disjunct);
                    } else if(mod.getIntValuePosition2() > 0) {
                        modConjunct.add(Restrictions.eq("mod.intValuePosition2", mod.getIntValuePosition2()));
                    }
                }
                modListConjunct.add(modConjunct);
            }
            monoJunc.add(modListConjunct);
        }
        
        //*** substitution restrictions: ***
        if(mono.countSubstitutions() > 0) {
            Conjunction substListConjunct = Restrictions.conjunction();
            for(Substitution subst : mono.getSubstitutions()) {
                Conjunction substConjunct = Restrictions.conjunction();
                substConjunct.add(Restrictions.eq("subst.name", subst.getName()));
                if(subst.getPosition1().size() > 1) {
                    Disjunction substPos1Disjunct = Restrictions.disjunction();
                    for(Integer pos1 : subst.getPosition1()) {
                        if(pos1.intValue() != 0) {
                            substPos1Disjunct.add(Restrictions.eq("subst.intValuePosition1", pos1));
                        }
                    }
                    substConjunct.add(substPos1Disjunct);
                } else if(subst.getIntValuePosition1() > 0) {
                    substConjunct.add(Restrictions.eq("subst.intValuePosition1", subst.getIntValuePosition1()));
                }
                if(subst.getLinkagetype1() != null) {
                    substConjunct.add(Restrictions.eq("subst.linkagetypeStr1", subst.getLinkagetypeStr1()));
                }
                if(subst.hasPosition2()) {
                    if(subst.getPosition2().size() > 1) {
                        Disjunction substPos2Disjunct = Restrictions.disjunction();
                        for(Integer pos2 : subst.getPosition2()) {
                            if(pos2.intValue() != 0) {
                                substPos2Disjunct.add(Restrictions.eq("subst.intValuePosition2", pos2));
                            }
                        }
                        substConjunct.add(substPos2Disjunct);
                    } else if(subst.getIntValuePosition2() > 0) {
                        substConjunct.add(Restrictions.eq("subst.intValuePosition2", subst.getIntValuePosition2()));
                    }
                    if(subst.getLinkagetype2() != null) {
                        substConjunct.add(Restrictions.eq("subst.linkagetypeStr2", subst.getLinkagetypeStr2()));
                    }
                }
                substListConjunct.add(substConjunct);
            }
            monoJunc.add(substListConjunct);
        }
        
        
        //*** add "distinct": ***
        query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        //System.out.println("query: " + query.toString());
        
        //*** perform query: ***
        List<?> queryList = query.list();
        
        //*** prepare result List: ***
        ArrayList<Monosaccharide> resultList = new ArrayList<Monosaccharide>();
        Iterator<?> listIter = queryList.iterator();
        while(listIter.hasNext()) {
            Monosaccharide resultMs = (Monosaccharide) listIter.next();
            if(resultMs != null) {
                //TODO: include modification counts into criteria
                if(!matchCoremodCount || resultMs.countCoreModifications() == mono.countCoreModifications()) {
                    if(!matchSubstCount || resultMs.countSubstitutions() == mono.countSubstitutions()) {
                        resultList.add(resultMs);
                    }
                }
            }
        }
        return resultList;
    }
    
    @SuppressWarnings("unchecked")
    public static List<Monosaccharide> getMonosaccharideListByAliasName(String aliasname, GlycanNamescheme scheme) {
        List<Monosaccharide> msList = null;
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            //*** prepare query: ***
            Criteria query = session.createCriteria(Monosaccharide.class);
            query.createAlias("synonyms", "alias");
            //*** add restrictions: ***
            Conjunction monoJunc    = Restrictions.conjunction();
            query.add(monoJunc);
            if(scheme == null || scheme.isCaseSensitive()) {
                monoJunc.add(Restrictions.like("alias.name", aliasname));
            } else {
                monoJunc.add(Restrictions.ilike("alias.name", aliasname));
            }
            if(scheme != null && !scheme.equals(GlycanNamescheme.AUTO)) {
                monoJunc.add(Restrictions.eq("alias.nameschemeStr", scheme.name()));
            }
            //*** add "distinct": ***
            query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            //*** perform query: ***
            msList = (List<Monosaccharide>) query.list();
        } catch(Exception e) {
            tx.rollback();
        }
        return(msList);
    }
    
    public static boolean writeMonosaccharideToDB(Monosaccharide ms, TemplateContainer container) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            //*** make sure monosaccharide and basetype names are set: ***
            if(ms.getName() == null) {
                ms.buildName();
            }
            Basetype bt = ms.getBasetype();
            if(bt.getName() == null) {
                bt.buildName();
            }
            if(ms.getSynonyms() == null || ms.getSynonyms().size() == 0) {
                MonosaccharideDataBuilder.buildSynonyms(ms, container);
            }
            if(ms.getComposition() == null) {
                MonosaccharideDataBuilder.buildComposition(ms);
            }
            if(ms.getAtoms() == null || ms.getAtoms().size() == 0) {
                MonosaccharideDataBuilder.buildAtoms(ms, Config.getGlobalConfig());
            }
            if(ms.getAtoms() == null || ms.getAtoms().size() == 0) {
                System.err.println("no atoms set in ms " + ms.toString());
                return(false);
            }
            if(ms.getRepresentations() == null || ms.getRepresentations().size() == 0) {
                ms.buildRepresentations();
            }
            
            //*** link basetype to db: ***
            ms.setBasetype(HibernateAccess.storeOrUpdateBasetype(bt, container.getBasetypeTemplateContainer(), false));
            
            //*** save monosaccharide: ***
            System.out.println("store monosaccharide " + ms.getName());
            session.save(ms);
            for(Substitution subst : ms.getSubstitutions()) {
                session.save("Substitution",subst);
            }
            for(Atom a : ms.getAtoms()) {
                session.save("MonosaccharideAtom", a);
            }
            for(Atom a : ms.getAtoms()) {
                for(AtomConnection ac : a.getConnections()) {
                    session.save("MonosaccharideAtomConnection", ac);
                }
            }
            for(MonosaccharideSynonym alias : ms.getSynonyms()) {
                //System.out.println("   alias " + alias.toString());
                if(alias.getName() == null) {
                    continue;
                }
                session.save(alias);
                for(Substitution extSubst : alias.getExternalSubstList()) {
                    session.save("MonosaccharideSynonymExternalSubstituent", extSubst);
                }
            }
            for(ResidueRepresentation monoRep : ms.getRepresentations()) {
                //System.out.println(" save representation " + monoRep.toString());
                session.save("MonosaccharideRepresentation", monoRep);
            }
            for(MonosaccharideLinkingPosition linkPos : ms.getPossibleLinkingPositions()) {
                session.save(linkPos);
            }
            
            tx.commit();
            Monosaccharide dbMs = HibernateAccess.getMonosaccharideFromDB(ms.getDbId());
            System.out.println("stored ms with id " + dbMs.getDbId());
        } catch(Exception e) {
            if(Config.getGlobalConfig().isPrintErrorMsgs()) {
                System.err.println("Exception in WriteMonosaccharideToDB(): " + e);
                e.printStackTrace();
            }
            tx.rollback();
            return(false);
        }
        return(true);
    }
    
    public static boolean updateMonosaccharide(Monosaccharide dbMs) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            session.update(dbMs);
        } catch(Exception e) {
            if(Config.getGlobalConfig().isPrintErrorMsgs()) {
                System.err.println("Exception in HibernateAccess.updateMonosaccharide(): " + e);
                e.printStackTrace();
            }
            tx.rollback();
            return(false);
        }
        return true;
    }
    
    public static Monosaccharide storeOrUpdateMonosaccharide(Monosaccharide ms, TemplateContainer container) throws ResourcesDbException {
        if(ms.getName() == null || ms.getName().equals("")) {
            ms.buildName();
        }
        Monosaccharide dbMs = HibernateAccess.getMonosaccharideFromDB(ms.getName());
        if(dbMs == null) { //*** monosaccharide not yet present in DB ***
            HibernateAccess.writeMonosaccharideToDB(ms, container);
            return(ms);
        } else {
            //TODO: update ms in database with e.g. additional synynoms, if present in ms
            for(ResidueRepresentation msRep : ms.getRepresentations()) {
                if(!dbMs.hasRepresentation(msRep.getType(), msRep.getFormat())) {
                    dbMs.addRepresentation(msRep);
                    System.out.println("store representation " + msRep.toString());
                    HibernateAccess.storeOrUpdateMonosaccharideRepresentation(msRep);
                }
            }
        }
        return(dbMs);
    }
    
    public static MonosaccharideSynonym storeOrUpdateMonosaccharideSynonym(MonosaccharideSynonym msAlias) throws ResourcesDbException {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            session.saveOrUpdate(msAlias);
            for(Substitution extSubst : msAlias.getExternalSubstList()) {
                session.saveOrUpdate("MonosaccharideSynonymExternalSubstituent", extSubst);
            }
            tx.commit();
        } catch(Exception e) {
            tx.rollback();
            throw new ResourcesDbException("Error in storing or updating monosaccharide alias", e);
        }
        return msAlias;
    }
    
    public static ResidueRepresentation storeOrUpdateMonosaccharideRepresentation(ResidueRepresentation monoRep) throws ResourcesDbException {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            session.saveOrUpdate("MonosaccharideRepresentation", monoRep);
            tx.commit();
        } catch(Exception e) {
            tx.rollback();
            MonosaccharideException me = new MonosaccharideException("Error in storing or updating monosaccharide representation");
            me.initCause(e);
            throw me;
        }
        return monoRep;
    }
    
//    *****************************************************************************
//    *** Substituent Templates: **************************************************
//    *****************************************************************************

    private static void writeOrUpdateSingleSubstituentTemplateToDB(SubstituentTemplate substTmpl, Session openSession) {
        SubstituentTemplate dbTmpl = (SubstituentTemplate) openSession.createQuery("from SubstituentTemplate where name='" + substTmpl.getName() + "'").uniqueResult();
        if(dbTmpl == null) {
            openSession.save(substTmpl);
            for(SubstituentAlias alias : substTmpl.getAliasList()) {
                openSession.save(alias);
            }
            for(Atom a : substTmpl.getAtoms()) {
                openSession.save("SubstituentAtom", a);
            }
            for(Atom a : substTmpl.getAtoms()) {
                for(AtomConnection ac : a.getConnections()) {
                    openSession.save("SubstituentAtomConnection", ac);
                }
            }
            try {
                for(NonBasetypeLinkingPosition nlp : substTmpl.getValidLinkingPositions()) {
                    openSession.save("ValidSubstituentLinkage", nlp);
                }
            } catch(ResourcesDbException me) {
                //*** exception is thrown when no valid linkage positions are given, nothing to do here in that case ***
            }
        } else {
            System.out.println("subst already present: " + dbTmpl.toString());
        }
    }
    
    public static void writeSubstituentTemplatesToDB(SubstituentTemplateContainer container) throws ResourcesDbException {
        ArrayList<String> substTmplList = container.getResidueIncludedNameList(GlycanNamescheme.MONOSACCHARIDEDB);
        if(substTmplList == null) {
            System.err.println("subst template list is null");
            return;
        }
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        for(String tmplName : container.getResidueIncludedNameList(GlycanNamescheme.MONOSACCHARIDEDB)) {
            SubstituentTemplate substTmpl = container.forName(GlycanNamescheme.MONOSACCHARIDEDB, tmplName);
            System.out.println("write substituent template " + substTmpl.getName());
            writeOrUpdateSingleSubstituentTemplateToDB(substTmpl, session);
        }
        session.getTransaction().commit();
    }
    
    public static SubstituentTemplate getSubstituentTemplateFromDB(String name) {
        SubstituentTemplate dbTmpl = null;
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            dbTmpl = (SubstituentTemplate) session.createQuery("from SubstituentTemplate where name='" + name + "'").uniqueResult();
        } catch(Exception e) {
            tx.rollback();
        }
        return dbTmpl;
    }
    
//    *****************************************************************************
//    *** Aglycon Templates: ******************************************************
//    *****************************************************************************

    private static void writeOrUpdateSingleAglyconTemplateToDB(AglyconTemplate aglTmpl, Session openSession) {
        AglyconTemplate dbTmpl = (AglyconTemplate) openSession.createQuery("from AglyconTemplate where name='" + aglTmpl.getName() + "'").uniqueResult();
        if(dbTmpl == null) {
            openSession.save(aglTmpl);
            for(AglyconAlias alias : aglTmpl.getAliasList()) {
                openSession.save(alias);
            }
            for(Atom a : aglTmpl.getAtoms()) {
                openSession.save("AglyconAtom", a);
            }
            for(Atom a : aglTmpl.getAtoms()) {
                for(AtomConnection ac : a.getConnections()) {
                    openSession.save("AglyconAtomConnection", ac);
                }
            }
            try {
                for(NonBasetypeLinkingPosition nlp : aglTmpl.getValidLinkingPositions()) {
                    openSession.save("ValidAglyconLinkage", nlp);
                }
            } catch(ResourcesDbException me) {
                //*** exception is thrown when no valid linkage positions are given, nothing to do here in that case ***
            }
        } else {
            System.out.println("aglycon already present: " + dbTmpl.toString());
        }
    }
    
    public static void writeAglyconTemplatesToDB() throws ResourcesDbException {
        AglyconTemplateContainer container = new AglyconTemplateContainer();
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        for(String tmplName : container.getTemplateNameList(GlycanNamescheme.GLYCOCT)) {
            AglyconTemplate aglTmpl = container.getAglyconTemplateByName(GlycanNamescheme.GLYCOCT, tmplName);
            System.out.println("write aglycon template " + aglTmpl.getName());
            writeOrUpdateSingleAglyconTemplateToDB(aglTmpl, session);
        }
        session.getTransaction().commit();
    }
    
//    *****************************************************************************
//    *** Representations: ********************************************************
//    *****************************************************************************

    public static ResidueRepresentation getMonosaccharideRepresentation(int repId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        ResidueRepresentation dbRep = null;
        try {
            dbRep = (ResidueRepresentation) session.createQuery("from MonosaccharideRepresentation where id=" + repId).uniqueResult();
        } catch(Exception e) {
            tx.rollback();
        }
        return dbRep;
    }
    
    public static ResidueRepresentation getMonosaccharideRepresentation(int monoId, String formatStr, String typeStr) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        ResidueRepresentation dbRep = null;
        try {
            dbRep = (ResidueRepresentation) session.createQuery("from MonosaccharideRepresentation where monosaccharide_id=" + monoId + " and representation_format='" + formatStr + "' and representation_type='" + typeStr + "'").uniqueResult();
        } catch(Exception e) {
            tx.rollback();
        }
        return dbRep;
    }
    
    public static void updateMonosaccharideRepresentations(Monosaccharide ms) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            if(ms != null) {
                System.out.println("check monosaccharide " + ms.toString() + "\n");
                ms.updateRepresentations();
                System.out.println("  updated representations...");
                if(ms.getRepresentations() != null) {
                    System.out.println("   representations: " + ms.getRepresentations().size());
                    for(ResidueRepresentation monoRep : ms.getRepresentations()) {
                        System.out.println("  save or update rep. " + monoRep.toString());
                        session.saveOrUpdate("MonosaccharideRepresentation", monoRep);
                    }
                } else {
                    System.out.println("  getRepresentations() is null");
                }
            }
        } catch(Exception e) {
            System.err.println("Exception: " + e);
            tx.rollback();
        }
        tx.commit();
    }
    
    public static void updateMonosaccharideRepresentations(int msId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            Monosaccharide ms = HibernateAccess.getMonosaccharideFromDB(msId);
            if(ms != null) {
                updateMonosaccharideRepresentations(ms);
                /*System.out.println("check monosaccharide " + ms.toString() + "\n");
                ms.updateRepresentations();
                System.out.println("  updated representations...");
                if(ms.getRepresentations() != null) {
                    System.out.println("   representations: " + ms.getRepresentations().size());
                    for(ResidueRepresentation monoRep : ms.getRepresentations()) {
                        System.out.println("  save or update rep. " + monoRep.toString());
                        session.saveOrUpdate("MonosaccharideRepresentation", monoRep);
                    }
                } else {
                    System.out.println("  getRepresentations() is null");
                }*/
            }
        } catch(Exception e) {
            System.err.println("Exception: " + e);
            tx.rollback();
        }
        //tx.commit();
    }
    
    public static void updateMonosaccharideRepresentations(int firstId, int lastId) {
        for(int i = firstId; i <= lastId; i++) {
            HibernateAccess.updateMonosaccharideRepresentations(i);
        }
    }
    
//    *****************************************************************************
//    *** Linking Positions: ******************************************************
//    *****************************************************************************

    public static void updateMonosaccharideLinkingPositions(Monosaccharide ms) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            if(ms != null) {
                System.out.println("check monosaccharide " + ms.toString() + "\n");
                if(ms.getPossibleLinkingPositions().size() == 0) {
                    ms.setPossibleLinkingPositions(MonosaccharideDataBuilder.buildPossibleLinkagePositions(ms));
                }
                for(MonosaccharideLinkingPosition linkpos : ms.getPossibleLinkingPositions()) {
                    System.out.println("  save or update linkpos. " + linkpos.toString());
                    session.saveOrUpdate(linkpos);
                }
            }
        } catch(Exception e) {
            System.err.println("Exception: " + e);
            tx.rollback();
        }
        tx.commit();
    }
    
    public static void updateMonosaccharideLinkingPositions(int msId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            Monosaccharide ms = HibernateAccess.getMonosaccharideFromDB(msId);
            if(ms != null) {
                updateMonosaccharideLinkingPositions(ms);
            }
        } catch(Exception e) {
            System.err.println("Exception: " + e);
            tx.rollback();
        }
        //tx.commit();
    }
    
    public static void updateMonosaccharideLinkingPositions(int firstId, int lastId) {
        for(int i = firstId; i <= lastId; i++) {
            HibernateAccess.updateMonosaccharideLinkingPositions(i);
        }
    }
    
//    *****************************************************************************
//    *** General Queries: ********************************************************
//    *****************************************************************************

    /**
     * Query the database by an HQL string and get results as a list.
     * @param hqlStr
     * @return a list of objects resulting from the query
     */
    public static List<?> getObjectList(String hqlStr) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Query q = session.createQuery(hqlStr);
        List<?> resultList = null;
        if(q != null) {
            resultList = q.list();
        }
        return resultList;
    }
    
    public static List<?> getObjectList(DetachedCriteria queryCriteria) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        List<?> resultList = queryCriteria.getExecutableCriteria(session).list();
        return resultList;
    }
    
//    *****************************************************************************
//    *** Other methods: **********************************************************
//    *****************************************************************************

    public static void writeTemplateDataToDB(TemplateContainer container) throws ResourcesDbException {
        HibernateAccess.writeElementsToDB();
        HibernateAccess.writeSubstituentTemplatesToDB(container.getSubstituentTemplateContainer());
        HibernateAccess.writeAglyconTemplatesToDB();
    }
}
