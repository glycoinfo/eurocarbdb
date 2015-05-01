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
* $Id: ShowGlycobase.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
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

//  eurocarb party imports
import org.eurocarbdb.action.BrowseAction;
import org.eurocarbdb.dataaccess.indexes.Index;
import org.eurocarbdb.dataaccess.indexes.Indexable;
import org.eurocarbdb.dataaccess.indexes.IndexByMostEvidence;
import org.eurocarbdb.dataaccess.indexes.IndexByContributedDate;
import org.eurocarbdb.dataaccess.indexes.IndexByContributorName;


/**
* @author              matthew    
* @version                $Rev: 1549 $
*/
public class ShowGlycobase extends BrowseAction<Glycan> 
{

    protected static final Logger log = Logger.getLogger( ShowGlycobase.class );

    private Glycan glycan = null;
    private List<Glycan> displayGlycobase;
    private List<Glycan> displayGlycobaseList;
    String imageStyle = "uoxf";
    private int searchGlycanId = 0;
 //   private String searchGlycanName = null;

     public Glycan getGlycan() {
        return glycan;
    }

      @Override
      public int getTotalResults()
      {
      if ( totalResults <= 0 )
      {
            totalResults = getEntityManager().countAll( Glycan.class );
            log.debug("calculated totalResults = " + totalResults );
        }

      return totalResults;
        }

    public String execute() {

         EntityManager em = getEntityManager();
     
     log.info("getting main page to glycobase");
     
     List<Glycan> displayGlycobase = (List<Glycan>)getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Glycan.GLYCOBASE").list();
     
     displayGlycobaseList = displayGlycobase;
     setAllResults(displayGlycobase);
     
     return SUCCESS;
}

//getters

public List getDisplayGlycobase()
{
    return this.displayGlycobase;
}

public List getDisplayGlycobaseList()
{
    return this.displayGlycobaseList;
}

 public String getImageStyle() { return this.imageStyle;}
    public void setImageStyle( String pic_image_style) {this.imageStyle = pic_image_style;}


    public final Class<Glycan> getIndexableType() 
    {
        return Glycan.class;
    }
    
}



