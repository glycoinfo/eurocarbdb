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

import java.util.List;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;

import org.hibernate.*;
import org.hibernate.cfg.*;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.core.Taxonomy;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.HibernateEntityManager;
import org.eurocarbdb.dataaccess.hplc.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

public class showTax extends EurocarbAction  
{
    static final Logger logger = Logger.getLogger( showTax.class );
    
    private List<RefTaxLink> displayGlycobase;
    private List<RefTaxLink> displayGlycobaseList;
    private RefTaxLink refTaxLink;
          
    private Taxonomy t;
    private Taxonomy tlist;
    
    private List<Taxonomy> matchingTaxonomies = null;
    private List<Taxonomy> matchingTaxonomies2 = null;
    private List<Glycan> glycanSearch = null;    
    private List<Glycan> displayGlycanSearch = null;
    private List<Ref> displayRefs;
    private List<Ref> refs;
    
    private int refId;
    private int glycanId;
    private int orderId;
    private int speciesId;
    private String diseaseName;
    private String tissueName;
    private String perturbationName;
    private String glycanName;
    private String imageStyle;
    
    
    public String execute() throws Exception 
    {
        //  no!!!!
        // SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        // Session session = sessionFactory.openSession();
    
        HibernateEntityManager hem = (HibernateEntityManager) getEntityManager();
        Session session = hem.getHibernateSession();
        
        logger.info("check ref id" + refId);
        
        String queryRefineComplete = "select taxOrderId, taxSpeciesId, pertName, diseaseName, tissueName from RefTaxLink where ogbiLinkId = " + refId ;
        
        logger.info("entire" + queryRefineComplete);
        
        Query query = session.createQuery(queryRefineComplete);
        
        List displayGlycobase = query.list();
        
        //List displayGlycobase = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.TAX_SEARCH")
        //.setParameter ("refId", refId)
        //.list();
        
        
        Iterator iter = displayGlycobase.iterator();
        logger.info(displayGlycobase.size());
        
        //for ( int count = 0; count < displayGlycobase.size(); count++ )
        //           System.out.printf( "hhh%s ", displayGlycobase.get( count ) );
        
        
        /*
        while (iter.hasNext()) {
            RefTaxLink [] temp = (RefTaxLink []) iter.next();
            logger.info("listtt" + temp);
            Integer orderId = (Integer) temp[0];
            Integer speciesId = (Integer) temp[1];
            String perturbationName = (String) temp[2];
            String diseaseName = (String) temp[3];
            String tissueName = (String) temp[4];
        }
        */
        logger.info("int" + orderId + speciesId + "diseasename" + diseaseName + "pername" + perturbationName + "tissuename" + tissueName);
        
        
        displayGlycobaseList = displayGlycobase;
        
        refs = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.PUBS")
                 .setParameter("glycan_id", glycanId)
                 .list();
        
                 displayRefs = refs;
        
        
        /*
        
        Taxonomy t = Taxonomy.lookupNcbiId( refId );
        
        this.matchingTaxonomies = new ArrayList<Taxonomy>( 2 );
        this.matchingTaxonomies.add( t );
        
        matchingTaxonomies2 = matchingTaxonomies;
        
        logger.info("taxttt:" + t);
        
        //tlist = t;
        
        String ddd = "hhh";
        List<Disease> d = Disease.lookupNameOrSynonym(diseaseName);
        
        List<Perturbation> p = Perturbation.lookupNameOrSynonym(perturbationName);
        
        */
        
        return SUCCESS;
    }
        

    public List getDisplayGlycobase()
    {
        return this.displayGlycobase;
    }
   
    public List getDisplayGlycobaseList()
    {
        return this.displayGlycobaseList;
    }


    public List getGlycanSearch()
    {
        return this.glycanSearch;
    }

    public List getDisplayGlycanSearch()
    {
        return this.displayGlycanSearch;
    }


    public Taxonomy getT()
    {
        return this.t;
    }

    public Taxonomy getTlist()
    {
        return this.tlist;
    }

    public List getMatchingTaxonomies()
    {
        return this.matchingTaxonomies;
    }
     
    public List getMatchingTaxonomies2()
    {
        return this.matchingTaxonomies2;
    }
     

    public int getRefId() { return this.refId; }
    
    public void setRefId( int search_ref_id) {this.refId = search_ref_id; }

    public int getGlycanId() {return this.glycanId;}
    public void setGlycanId ( int search_glycan_id) {this.glycanId = search_glycan_id;}

    public String getGlycanName() {return this.glycanName;}
    public void setGlycanName ( String search_glycan_name) {this.glycanName = search_glycan_name;}

    public String getImageStyle() { return this.imageStyle;}
    public void setImageStyle( String pic_image_style) {this.imageStyle = pic_image_style;}

    public List getDisplayRefs()
    {
            return displayRefs;
    }

    /*  wtf?
    public List<Ref> getEntityManager() {
        return refs;
    }
    */

}
