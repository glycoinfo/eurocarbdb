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

import java.lang.*;
import java.util.*;
import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;

import org.apache.log4j.Logger;

import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.core.Technique;
import org.eurocarbdb.dataaccess.hplc.Column;
import org.eurocarbdb.dataaccess.hplc.Detector;
import org.eurocarbdb.dataaccess.hplc.Instrument;
import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.criterion.*;

import org.eurocarbdb.action.BrowseAction;
import org.eurocarbdb.dataaccess.indexes.Index;
import org.eurocarbdb.dataaccess.indexes.Indexable;
import org.eurocarbdb.dataaccess.indexes.IndexByMostEvidence;
import org.eurocarbdb.dataaccess.indexes.IndexByContributedDate;
import org.eurocarbdb.dataaccess.indexes.IndexByContributorName;





//  eurocarb party imports
/*import org.eurocarbdb.action.BrowseAction;
import org.eurocarbdb.dataaccess.indexes.Index;
import org.eurocarbdb.dataaccess.indexes.Indexable;
import org.eurocarbdb.dataaccess.indexes.IndexByMostEvidence;
import org.eurocarbdb.dataaccess.indexes.IndexByContributedDate;
import org.eurocarbdb.dataaccess.indexes.IndexByContributorName;
*/

public class showGlycobaseRefine extends BrowseAction<Glycan> 
{

    private Profile parent = null;
    private Instrument instrument = null;
    String imageStyle = "uoxf";    
    private int instrument_id;
    
    short classType = 1;
    int leaveOut = 0;
    short refineClass = 0;
    private String refineAssignment;
    short replaceSearch = 100;
    short IgG;
    short serum;
    
    int a1s;
    int a1f;
    int a1b;
    int a1bgal;
    int a1agal;
    int a1galnac;
    int a1polylac;
    int a1fouterarm;
    int a1hybrid;
    int a1mannose;
    
    int a2s;
    int a2f;
    int a2b;
    int a2bgal;
    int a2agal;
    int a2galnac;
    int a2polylac;
    int a2fouterarm;
    int a2hybrid;
    int a2mannose;
    
    int a3s;
    int a3f;
    int a3b;
    int a3bgal;
    int a3agal;
    int a3galnac;
    int a3polylac;
    int a3fouterarm;
    int a3hybrid;
    int a3mannose;
    
    int a4s;
    int a4f;
    int a4b;
    int a4bgal;
    int a4agal;
    int a4galnac;
    int a4polylac;
    int a4fouterarm;
    int a4hybrid;
    int a4mannose;
    
    short classA1;
    String A1S;
    short assignA1S;
    short assigna1s = 1;
    String A1F;
    short assignA1F;
    short assigna1f =1;
    String A1B;
    short assignA1B;
    short assigna1b =1;
    String A1BGAL;
    short assignA1BGAL;
    short assigna1bgal;
    String A1AGAL;
    short assignA1AGAL;
    short assigna1agal =1;
    String A1GALNAC;
    short assignA1GALNAC;
    short assigna1galnac =1;
    String A1POLYLAC;
    short assignA1POLYLAC;
    short assigna1polylac =1;
    String A1FOUTERARM;
    short assignA1FOUTERARM;
    short assigna1fouterarm =1;
    String A1HYBRID;
    short assignA1HYBRID;
    short assigna1hybrid =1;
    String A1MANNOSE;
    short assignA1MANNOSE;
    short assigna1mannose =1;
    

    short classA2;
    String A2S;
    short assignA2S;
    short assigna2s = 1;
    String A2F;
    short assignA2F;
    short assigna2f =1;
    String A2B;
    short assignA2B;
    short assigna2b =1;
    String A2BGAL;
    short assignA2BGAL;
    short assigna2bgal;
    String A2AGAL;
    short assignA2AGAL;
    short assigna2agal =1;
    String A2GALNAC;
    short assignA2GALNAC;
    short assigna2galnac =1;
    String A2POLYLAC;
    short assignA2POLYLAC;
    short assigna2polylac =1;
    String A2FOUTERARM;
    short assignA2FOUTERARM;
    short assigna2fouterarm =1;
    String A2HYBRID;
    short assignA2HYBRID;
    short assigna2hybrid =1;
    String A2MANNOSE;
    short assignA2MANNOSE;
    short assigna2mannose =1;
    
    short classA3;
    String A3S;
    short assignA3S;
    short assigna3s = 1;
    String A3F;
    short assignA3F;
    short assigna3f =1;
    String A3B;
    short assignA3B;
    short assigna3b =1;
    String A3BGAL;
    short assignA3BGAL;
    short assigna3bgal;
    String A3AGAL;
    short assignA3AGAL;
    short assigna3agal =1;
    String A3GALNAC;
    short assignA3GALNAC;
    short assigna3galnac =1;
    String A3POLYLAC;
    short assignA3POLYLAC;
    short assigna3polylac =1;
    String A3FOUTERARM;
    short assignA3FOUTERARM;
    short assigna3fouterarm =1;
    String A3HYBRID;
    short assignA3HYBRID;
    short assigna3hybrid =1;
    String A3MANNOSE;
    short assignA3MANNOSE;
    short assigna3mannose =1;;

    short classA4;
    String A4S;
    short assignA4S;
    short assigna4s = 1;
    String A4F;
    short assignA4F;
    short assigna4f =1;
    String A4B;
    short assignA4B;
    short assigna4b =1;
    String A4BGAL;
    short assignA4BGAL;
    short assigna4bgal;
    String A4AGAL;
    short assignA4AGAL;
    short assigna4agal =1;
    String A4GALNAC;
    short assignA4GALNAC;
    short assigna4galnac =1;
    String A4POLYLAC;
    short assignA4POLYLAC;
    short assigna4polylac =1;
    String A4FOUTERARM;
    short assignA4FOUTERARM;
    short assigna4fouterarm =1;
    String A4HYBRID;
    short assignA4HYBRID;
    short assigna4hybrid =1;
    String A4MANNOSE;
    short assignA4MANNOSE;
    short assigna4mannose =1;

    private Glycan glycan = null;
    protected static final Logger logger = Logger.getLogger ( showGlycobaseRefine.class );
  
    public Glycan getGlycan() {
        return glycan;
    }

    @Override
    public int getTotalResults()
    {
    if ( totalResults <= 0 )
    {
            totalResults = getEntityManager().countAll( Glycan.class );
            logger.debug("calculated totalResults = " + totalResults );
    }

      return totalResults;
    }
    
    public String getQueryRefine() {
    return queryRefine;
    }

    public void setQueryRefine(String s) {
    queryRefine = s;
    }

    String queryRefine;

    public String execute() throws Exception {

    if (classA1 == 0) {
        classA1 = replaceSearch;
    }

    if (classA2 == 0) {
        classA2 = replaceSearch;
    }

    if (classA3 == 0) {
        classA3 = replaceSearch;
    }

    if (classA4 == 0) {
        classA4 = replaceSearch;
    }
    
    
    EntityManager em = getEntityManager();
    HibernateEntityManager hem = (HibernateEntityManager) getEntityManager();
        Session session = hem.getHibernateSession();

    Criteria criteria = session.createCriteria(Glycan.class);
    Disjunction disjunction = Restrictions.disjunction();
    ProjectionList proList = Projections.projectionList();
    proList.add(Projections.property("name"));
    proList.add(Projections.property("ogbitranslation"));
    proList.add(Projections.property("gu"));
    proList.add(Projections.property("glycanId"));

    criteria.setProjection(proList);
    criteria.addOrder( Order.asc("gu") );
    
    if(assignA3S==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a3", classA3),
                    Expression.eq("s", assignA3S)
                    ));
    }
    if(assignA3F ==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a3", classA3),
                    Expression.eq("f6", assignA3F)
                    ));
    
    }
    
    if(assignA3FOUTERARM ==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a3", classA3),
                    Expression.eq("fouterarm", assignA3FOUTERARM)
                    ));
    }
    
    if(assignA3B ==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a3", classA3),
                    Expression.eq("b", assignA3B)
                    ));
    
    }
    
    if(assignA3BGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a3", classA3),
                    Expression.eq("bgal", assignA3BGAL)
                    ));
    }
    
    if(assignA3AGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a3", classA3),
                    Expression.eq("agal", assignA3AGAL)
                    ));
    }
    if(assignA3GALNAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a3", classA3),
                    Expression.eq("galnac", assignA3GALNAC)
                    ));
    }
    
    if(assignA3POLYLAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a3", classA3),
                    Expression.eq("polylac", assignA3POLYLAC)
                    ));
    }
    if(assignA3HYBRID==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a3", classA3),
                    Expression.eq("hybrid", assignA3HYBRID)
                    ));
    }
    if(assignA3MANNOSE==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a3", classA3),
                    Expression.eq("mannose", assignA3MANNOSE)
                    ));
    }
    if(assignA2S==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a2", classA2),
                    Expression.eq("s", assignA2S)
                    ));
    }
    if(assignA2F==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a2", classA2),
                    Expression.eq("f6", assignA2F)
                    ));
    }
    if(assignA2FOUTERARM==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a2", classA2),
                    Expression.eq("fouterarm", assignA2FOUTERARM)
                    ));
    }
    if(assignA2B==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a2", classA2),
                    Expression.eq("b", assignA2B)
                    ));
    }
    if(assignA2BGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a2", classA2),
                    Expression.eq("bgal", assignA2BGAL)
                    ));
    }
    if(assignA2AGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a2", classA2),
                    Expression.eq("agal", assignA2AGAL)
                    ));
    }
    if(assignA2GALNAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a2", classA2),
                    Expression.eq("galnac", assignA2GALNAC)
                    ));
    }
    if(assignA2POLYLAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a2", classA2),
                    Expression.eq("polylac", assignA2POLYLAC)
                    ));
    }
    if(assignA2HYBRID==1){
    disjunction.add(Restrictions.and(
                    Expression.eq("a2", classA2),
                    Expression.eq("hybrid", assignA2HYBRID)
                    ));
    }
    if(assignA2MANNOSE==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a2", classA2),
                    Expression.eq("mannose", assignA2MANNOSE)
                    ));
    }
    
    if(assignA1S==1){
    disjunction.add(Restrictions.and(
                    Expression.eq("a1", classA1),
                    Expression.eq("s", assignA1S)
                    ));
    }
    if(assignA1F==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a1", classA1),
                    Expression.eq("f6", assignA1F)
                    ));
    }
    if(assignA1FOUTERARM==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a1", classA1),
                    Expression.eq("fouterarm", assignA1FOUTERARM)
                    ));
    }
    if(assignA1B==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a1", classA1),
                    Expression.eq("b", assignA1B)
                    ));
    }
    if(assignA1BGAL==1){
    disjunction.add(Restrictions.and(
                    Expression.eq("a1", classA1),
                    Expression.eq("bgal", assignA1BGAL)
                    ));
    }
    if(assignA1AGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a1", classA1),
                    Expression.eq("agal", assignA1AGAL)
                    ));
    }
    if(assignA1GALNAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a1", classA1),
                    Expression.eq("galnac", assignA1GALNAC)
                    ));
    }
    if(assignA1POLYLAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a1", classA1),
                    Expression.eq("polylac", assignA1POLYLAC)
                    ));
    }
    if(assignA1HYBRID==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a1", classA1),
                    Expression.eq("hybrid", assignA1HYBRID)
                    ));
    }
    if(assignA1MANNOSE==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a1", classA1),
                    Expression.eq("mannose", assignA1MANNOSE)
                    ));
    }
    if(assignA4S==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a4", classA4),
                    Expression.eq("s", assignA4S)
                    ));
    }
    if(assignA4F==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a4", classA4),
                    Expression.eq("f6", assignA4F)
                    ));
    }
    if(assignA4FOUTERARM==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a4", classA4),
                    Expression.eq("fouterarm", assignA4FOUTERARM)
                    ));
    }
    if(assignA4B==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a4", classA4),
                    Expression.eq("b", assignA4B)
                    ));
    }
    if(assignA4BGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a4", classA4),
                    Expression.eq("bgal", assignA4BGAL)
                    ));
    }
    if(assignA4AGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a4", classA4),
                    Expression.eq("agal", assignA4AGAL)
                    ));
    }
    if(assignA4GALNAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a4", classA4),
                    Expression.eq("galnac", assignA4GALNAC)
                    ));
    }
    if(assignA4POLYLAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a4", classA4),
                    Expression.eq("polylac", assignA4POLYLAC)
                    ));
    }
    if(assignA4HYBRID==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a4", classA4),
                    Expression.eq("hybrid", assignA4HYBRID)
                    ));
    }
    if(assignA4MANNOSE==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("a4", classA4),
                    Expression.eq("mannose", assignA4MANNOSE)
                    ));
    }

    if(serum == 1 && IgG == 1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("serum", serum),
                    Expression.eq("normalIgG", IgG)
                    ));
    }
    
    if(serum ==1 && IgG == 0) {
    criteria.add(Expression.eq("serum", serum));
    }
    
    if(serum == 0 && IgG ==1) {
    criteria.add(Expression.eq("normalIgg", IgG));
    }
    
    criteria.add(disjunction);
    

    List displayCriteria = criteria.list();
    
    int listSize = displayCriteria.size();
    
    if ( listSize > 0) {
    setAllResults(displayCriteria);

    return SUCCESS;
    }
    else { return ERROR;}
    
    }
    
    public void setHumanIgG(Short igg) {
        if (igg ==1) {
        this.IgG = igg;
        }
        else { this.IgG = refineClass;}
    }
    
    public Short getHumanIgG() { return this.IgG;}
    
    public void setSerum(Short serumSearch) {
        if ( serum ==1) {
        this.serum = serumSearch;
        }
        else { this.serum = refineClass;}
    }
    
    public Short getSerum() { return this.serum;}
        
    
    public void setA1s(Integer tempa1s) {
        if (tempa1s == 1){
        //this.A1S = a1s;
        this.a1s = tempa1s;
        this. assignA1S = assigna1s;
        this.classA1 = classType;
        }
        else { this.a1s = leaveOut;}
    }
    
    public Integer getA1s() { return this.a1s;}
    
    public void setA1f(Integer tempa1f) {
        if (tempa1f == 1) {
        //this.A1F = a1f;
        this.a1f = tempa1f;
        this. assignA1F = assigna1f;
        this.classA1 = classType;
        }
        else { this.a1f = leaveOut;}
    }
    
    public Integer getA1f() { return this.a1f;}
    
    public void setA1b(Integer tempa1b) {
        if (tempa1b == 1) {
        //this.A1B = a1b;
        this.a1b = tempa1b;
        this. assignA1B = assigna1b;
        this.classA1 = classType;
        }
        else { this.a1b = leaveOut;}
    }
    
    public Integer getA1b() { return this.a1b;}
    
    public void setA1bgal(Integer tempa1bgal) {
        if (tempa1bgal == 1) {
        //this.A1BGAL = a1bgal;
        this.a1bgal = tempa1bgal;
        this. assignA1BGAL = assigna1bgal;
        this.classA1 = classType;
        }
        else { this.a1bgal = leaveOut;}
    }
    
    public Integer getA1bgal() { return this.a1bgal;}
    
    public void setA1agal(Integer tempa1agal) {
        if (tempa1agal == 1) {
        //this.A1AGAL = a1agal;
        this.a1agal = tempa1agal;
        this. assignA1AGAL = assigna1agal;
        this.classA1 = classType;
        }
        else { this.a1agal = leaveOut;}
    }
    
    public Integer getA1agal() { return this.a1agal;}
    
    public void setA1galnac(Integer tempa1galnac) {
        if (tempa1galnac == 1) {
        //this.A1GALNAC = a1galnac;
        this.a1galnac = tempa1galnac;
        this. assignA1GALNAC = assigna1galnac;
        this.classA1 = classType;
        }
        else { this.a1galnac = leaveOut;}
    }
    
    public Integer getA1galnac() { return this.a1galnac;}
    
    
    public void setA1polylac(Integer tempa1polylac) {
        if (tempa1polylac == 1) {
        //this.A1POLYLAC = a1polylac;
        this.a1polylac = tempa1polylac;
        this. assignA1POLYLAC = assigna1polylac;
        this.classA1 = classType;
        }
        else { this.a1polylac = leaveOut;}
    }
    
    public Integer getA1polylac() {return this.a1polylac;}
    
    public void setA1fouterarm(Integer tempa1fouterarm) {
        if (tempa1fouterarm == 1) {
        //this.A1FOUTERARM = a1fouterarm;
        this.a1fouterarm = tempa1fouterarm;
        this. assignA1FOUTERARM = assigna1fouterarm;
        this.classA1 = classType;
        }
        else { this.a1fouterarm = leaveOut;}
    }
    
    public Integer getA1fouterarm() { return this.a1fouterarm; }
    
    public void setA1hybrid(Integer tempa1hybrid) {
        if (tempa1hybrid == 1) {
        //this.A1HYBRID = a1hybrid;
        this.a1hybrid = tempa1hybrid;
        this. assignA1HYBRID = assigna1hybrid;
        this.classA1 = classType;
        }
        else { this.a1hybrid = leaveOut;}
    }
    
    public Integer getA1hybrid() { return this.a1hybrid; }
    
    public void setA1mannose(Integer tempa1mannose) {
        if (tempa1mannose == 1) {
        //this.A1MANNOSE = a1mannose;
        this.a1mannose = tempa1mannose;
        this. assignA1MANNOSE = assigna1mannose;
        this.classA1 = classType;
        }
        else { this.a1mannose = leaveOut;}
    }
    
    public Integer getA1mannose() { return this.a1mannose; }
    
    
    public void setA2s(Integer tempa2s) {
        if (tempa2s == 1) {
        //this.A2S = a2s;
        this.a2s = tempa2s;
        this. assignA2S = assigna2s;
        this.classA2 = classType;
        }
        else { this.a2s = leaveOut;}
    }
    
    public Integer getA2s() { return this.a2s;}
    
    public void setA2f(Integer tempa2f) {
        if (tempa2f == 1) {
        //this.A2F = a2f;
        this.a2f = tempa2f;
        this. assignA2F = assigna2f;
        this.classA2 = classType;
        }
        else { this.a2f = leaveOut;}
    }
    
    public Integer getA2f() { return this.a2f;}
    
    public void setA2b(Integer tempa2b) {
        if (tempa2b == 1) {
        //this.A2B = a2b;
        this.a2b = tempa2b;
        this. assignA2B = assigna2b;
        this.classA2 = classType;
        }
        else { this.a2b = leaveOut;}
    }
    
    public Integer getA2b() { return this.a2b;}
    
    public void setA2bgal(Integer tempa2bgal) {
        if (tempa2bgal == 1) {
        //this.A2BGAL = a2bgal;
        this.a2bgal = tempa2bgal;
        this. assignA2BGAL = assigna2bgal;
        this.classA2 = classType;
        }
        else { this.a2bgal = leaveOut;}
    }
    
    public Integer getA2bgal() { return this.a2bgal;}
    
    public void setA2agal(Integer tempa2agal) {
        if (tempa2agal == 1) {
        //this.A2AGAL = a2agal;
        this.a2agal = tempa2agal;
        this. assignA2AGAL = assigna2agal;
        this.classA2 = classType;
        }
        else { this.a2agal = leaveOut;}
    }
    
    public Integer getA2agal() { return this.a2agal;}
    
    public void setA2galnac(Integer tempa2galnac) {
        if (tempa2galnac == 1) {
        //this.A2GALNAC = a2galnac;
        this.a2galnac = tempa2galnac;
        this. assignA2GALNAC = assigna2galnac;
        this.classA2 = classType;
        }
        else { this.a2galnac = leaveOut;}
    }
    
    public Integer getA2galnac() { return this.a2galnac;}
    
    public void setA2polylac(Integer tempa2polylac) {
        if ( tempa2polylac == 1) {
        //this.A2POLYLAC = a2polylac;
        this.a2polylac = tempa2polylac;
        this. assignA2POLYLAC = assigna2polylac;
        this.classA2 = classType;
        }
        else { this.a2polylac = leaveOut;}
    }
    
    public Integer getA2polylac() { return this.a2polylac;}
    
    public void setA2fouterarm(Integer tempa2fouterarm) {
        if (tempa2fouterarm == 1) {
        //this.A2FOUTERARM = a2fouterarm;
        this.a2fouterarm = tempa2fouterarm;
        this. assignA2FOUTERARM = assigna2fouterarm;
        this.classA2 = classType;
        }
        else { this.a2fouterarm = leaveOut;}
    }
    
    public Integer getA2fouterarm() { return this.a2fouterarm;}
    
    public void setA2hybrid(Integer tempa2hybrid) {
        if (tempa2hybrid == 1) {
        //this.A2HYBRID = a2hybrid;
        this.a2hybrid = tempa2hybrid;
        this. assignA2HYBRID = assigna2hybrid;
        this.classA2 = classType;
        }
        else { this.a2hybrid = leaveOut;}
    }
    
    public Integer getA2hybrid() { return this.a2hybrid;}
    
    public void setA2mannose(Integer tempa2mannose) {
        if (tempa2mannose == 1) {
        //this.A2MANNOSE = a2mannose;
        this.a2mannose = tempa2mannose;
        this. assignA2MANNOSE = assigna2mannose;
        this.classA2 = classType;
        }
        else { this.a2mannose = leaveOut;}
    }
    
    public Integer getA2mannose() { return this.a2mannose;}
    
    public void setA3s(Integer tempa3s) {
        if (tempa3s == 1) {
        //this.A3S = a3s;
        this.a3s = tempa3s;
        this. assignA3S = assigna3s;
        this.classA3 = classType;
        }
        else { this.a3s = leaveOut;}
    }
    
    public Integer getA3s() { return this.a3s;}
    
    public void setA3f(Integer tempa3f) {
        if (tempa3f == 1) {
        //this.A3F = a3f;
        this.a3f = tempa3f;
        this. assignA3F = assigna3f;
        this.classA3 = classType;
        }
        else { this.a3f = leaveOut;}
    }
    
    public Integer getA3f() {return this.a3f;}
    
    public void setA3b(Integer tempa3b) {
        if (tempa3b == 1) {
        //this.A3B = a3b;
        this.a3b = tempa3b;
        this. assignA3B = assigna3b;
        this.classA3 = classType;
        }
        else { this.a3b = leaveOut;}
    }
    
    public Integer getA3b() { return this.a3b;}
    
    public void setA3bgal(Integer tempa3bgal) {
        if (tempa3bgal == 1) {
        //this.A3BGAL = a3bgal;
        this.a3bgal = tempa3bgal;
        this. assignA3BGAL = assigna3bgal;
        this.classA3 = classType;
        }
        else { this.a3bgal = leaveOut;}
    }
    
    public Integer getA3bgal() {return this.a3bgal;}
    
    public void setA3agal(Integer tempa3agal) {
        if (tempa3agal == 1) {
        //this.A3AGAL = a3agal;
        this.a3agal = tempa3agal;
        this. assignA3AGAL = assigna3agal;
        this.classA3 = classType;
        }
        else { this.a3agal = leaveOut;}
    }
    
    public Integer getA3agal() { return this.a3agal;}
    
    public void setA3galnac(Integer tempa3galnac) {
        if ( tempa3galnac == 1) {
        //this.A3GALNAC = a3galnac;
        this.a3galnac = tempa3galnac;
        this.assignA3GALNAC = assigna3galnac;
        this.classA3 = classType;
        }
        else { this.a3galnac = leaveOut;}
    }
    
    public Integer getA3galnac() { return this.a3galnac;}
    
    public void setA3polylac(Integer tempa3polylac) {
        if ( tempa3polylac == 1) {
        //this.A3POLYLAC = a3polylac;
        this.a3polylac = tempa3polylac;
        this. assignA3POLYLAC = assigna3polylac;
        this.classA3 = classType;
        }
        else { this.a3polylac = leaveOut;}
    }
    
    public Integer getA3polylac() { return this.a3polylac;}
    
    public void setA3fouterarm(Integer tempa3fouterarm) {
        if ( tempa3fouterarm == 1) {
        //this.A3FOUTERARM = a3fouterarm;
        this.a3fouterarm = tempa3fouterarm;
        this. assignA3FOUTERARM = assigna3fouterarm;
        this.classA3 = classType;
        }
        else { this.a3fouterarm = leaveOut;}
    }
    
    public Integer getA3fouterarm() { return this.a3fouterarm;}
    
    public void setA3hybrid(Integer tempa3hybrid) {
        if ( tempa3hybrid == 1) {
        //this.A3HYBRID = a3hybrid;
        this.a3hybrid = tempa3hybrid;
        this. assignA3HYBRID = assigna3hybrid;
        this.classA3 = classType;
        }
        else { this.a3hybrid = leaveOut;}
    }
    
    public Integer getA3hybrid() { return this.a3hybrid;}
    
    public void setA3mannose(Integer tempa3mannose) {
        if ( tempa3mannose == 1) {
        //this.A3MANNOSE = a3mannose;
        this.a3mannose = tempa3mannose;
        this.assignA3MANNOSE = assigna3mannose;
        this.classA3 = classType;
        }
        else { this.a3mannose = leaveOut;}
    }
    
    public Integer getA3mannose() { return this.a3mannose;}
    
    
    
    public void setA4s(Integer tempa4s) {
        if (tempa4s == 1) {
        //this.A4S = a4s;
        this.a4s = tempa4s;
        this.assignA4S = assigna4s;
        this.classA4 = classType;
        }
        else { this.a4s = leaveOut;}
    }
    
    public Integer getA4s() { return this.a4s;}
    
    public void setA4f(Integer tempa4f) {
        if (tempa4f == 1) {
        //this.A4F = a4f;
        this.a4f = tempa4f;
        this. assignA4F = assigna4f;
        this.classA4 = classType;
        }
        else { this.a4f = leaveOut;}
    }
    
    public Integer getA4f() { return this.a4f;}
    
    public void setA4b(Integer tempa4b) {
        if (tempa4b == 1) {
        //this.A4B = a4b;
        this.a4b = tempa4b;
        this. assignA4B = assigna4b;
        this.classA4 = classType;
        }
        else { this.a4b = leaveOut;}
    }
    
    public Integer getA4b() { return this.a4b;}
    
    public void setA4bgal(Integer tempa4bgal) {
        if (tempa4bgal == 1) {
        //this.A4BGAL = a4bgal;
        this.a4bgal = tempa4bgal;
        this. assignA4BGAL = assigna4bgal;
        this.classA4 = classType;
        }
        else { this.a4bgal = leaveOut;}
    }
    
    public Integer getA4bgal() { return this.a4bgal;}
    
    public void setA4agal(Integer tempa4agal) {
        if (tempa4agal == 1) { 
        //this.A4AGAL = a4agal;
        this.a4agal = tempa4agal;
        this. assignA4AGAL = assigna4agal;
        this.classA4 = classType;
        }
        else { this.a4agal = leaveOut;}
    }
    
    public Integer getA4agal() { return this.a4agal;}
    
    public void setA4galnac(Integer tempa4galnac) {
        if (tempa4galnac == 1) {
        //this.A4GALNAC = a4galnac;
        this.a4galnac = tempa4galnac;
        this. assignA4GALNAC = assigna4galnac;
        this.classA4 = classType;
        }
        else { this.a4galnac = leaveOut;}
    }
    
    public Integer getA4galnac() { return this.a4galnac;}
    
    public void setA4polylac(Integer tempa4polylac) {
        if (tempa4polylac == 1) {
        //this.A4POLYLAC = a4polylac;
        this.a4polylac = tempa4polylac;
        this. assignA4POLYLAC = assigna4polylac;
        this.classA4 = classType;
        }
        else { this.a4polylac = leaveOut;}
    }
    
    public Integer getA4polylac() { return this.a4polylac;}
    
    public void setA4fouterarm(Integer tempa4fouterarm) {
        if (tempa4fouterarm == 1) {
        //this.A4FOUTERARM = a4fouterarm;
        this.a4fouterarm = tempa4fouterarm;
        this. assignA4FOUTERARM = assigna2fouterarm;
        this.classA4 = classType;
        }
        else { this.a4fouterarm = leaveOut;}
    }
    
    public Integer getA4fouterarm() { return this.a4fouterarm;}
    
    public void setA4hybrid(Integer tempa4hybrid) {
        if (tempa4hybrid == 1) {
        //this.A4HYBRID = a4hybrid;
        this.a4hybrid = tempa4hybrid;
        this. assignA4HYBRID = assigna4hybrid;
        this.classA4 = classType;
        }
        else { this.a4hybrid = leaveOut;}
    }
    
    public Integer getA4hybrid() {return this.a4hybrid;}
    
    public void setA4mannose(Integer tempa4mannose) {
        if (tempa4mannose == 1) {
        //this.A4MANNOSE = a4mannose;
        this.a4mannose = tempa4mannose;
        this. assignA2MANNOSE = assigna2mannose;
        this.classA4 = classType;
        }
        else { this.a4mannose = leaveOut;}
    }
    
    public Integer getA4mannose() { return this.a4mannose;}
    
    
    public final Class<Glycan> getIndexableType() 
    {
        return Glycan.class;
    }
 
}
