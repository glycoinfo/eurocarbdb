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
/**
* $Id: searchGlycobase.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/

package org.eurocarbdb.action.hplc;


import java.util.*;
import java.lang.*;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;


import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.*;
import org.hibernate.cfg.*;

import org.apache.log4j.Logger;

/**
* @author              matthew    
* @version                $Rev: 1549 $
*/
public class searchGlycobase extends EurocarbAction 
{

        protected static final Logger logger = Logger.getLogger( ShowGlycan.class );

    private Glycan glycan = null;
        private int searchGlycanId = 0;
        private double searchGlycanGu = 0;
        private String searchGlycanName;
        List <Glycan> displayGlycobaseList;
        List <Glycan> displayGlycobase;
        private String imageStyle;

         public Glycan getGlycan() {
        return glycan;
        }
    

 
        public int getSearchGlycanId() { return this.searchGlycanId; }
        public void setSearchGlycanId( int search_glycan_id) {this.searchGlycanId = search_glycan_id; }

        public String getImageStyle() { return this.imageStyle;}
        public void setImageStyle( String pic_image_style) {this.imageStyle = pic_image_style;}

        public double getSearchGlycanGu() { return this.searchGlycanGu; }
        public void setSearchGlycanGu( double search_glycan_gu) {this.searchGlycanGu = search_glycan_gu; }

        public String getSearchGlycanName() { return this.searchGlycanName; }
        public void setSearchGlycanName( String search_glycan_name) {this.searchGlycanName = search_glycan_name; }



        public String execute() {

    logger.info("idcheck:" + searchGlycanId);


    if (searchGlycanGu > 0) {

        displayGlycobase =Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Glycan.GLYCOBASE_SEARCH")
        
              .setParameter("gu", searchGlycanGu)
              .list();
    displayGlycobaseList = displayGlycobase;

    return SUCCESS;
    }

    if (searchGlycanGu < 0) {
         String upperGlycanName = searchGlycanName.toUpperCase();
        
        List displayGlycobase = getEntityManager()
                                .getQuery("org.eurocarbdb.dataaccess.hplc.Glycan.GLYCOBASE_NAME_SEARCH" )
                                .setParameter("name", "%" + upperGlycanName + "%" )
                                .list();

    displayGlycobaseList = displayGlycobase;

    return SUCCESS;
    }


    return INPUT;
    }

    public List getDisplayGlycobase()
    {
        return this.displayGlycobase;
        }

    public List getDisplayGlycobaseList()
        {
        return this.displayGlycobaseList;
        }

}
