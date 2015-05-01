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

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.hplc.Glycan;

import org.apache.log4j.Logger;

import java.util.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


public class CreateHplcGu extends EurocarbAction {
  
  protected static final Logger log = Logger.getLogger( CreateHplcGu.class.getName() );
  
  private GlycanSequence glycan = null;
  private Glycan hplcGlycan = null;
  private Glycan displayHplcGlycan = null;
  private List displayRef = null;
  private List<GlycanSequence> reference = null;

  private int searchGlycanId = -1;

  public GlycanSequence getGlycan() {  return glycan;  }

  public GlycanSequence getGlycanSequence() {  return glycan;  }
  
  public Glycan getHplcGlycan() { return hplcGlycan; }

  public int getGlycanSequenceId() {  return searchGlycanId;  }

  public void setGlycanSequenceId( int search_id ) {  searchGlycanId = search_id;  }
  
  public List getReference() { return reference; }
  
 public Glycan getDisplayHplcGlycan()
  {
  return this.displayHplcGlycan;
  }

  public List getDisplayRef()
  {
  return this.displayRef;
  }


  public String execute() throws Exception {
    
  glycan = getEntityManager().lookup( GlycanSequence.class, searchGlycanId );
  log.info("i am in this script");    
  
  hplcGlycan = Glycan.lookupByGWS( searchGlycanId );
  displayHplcGlycan = hplcGlycan;

  reference = getEntityManager().getQuery("org.eurocarbdb.dataaccess.core.GlycanSequence.HPLC_REFERENCE").setParameter("sequence_id", searchGlycanId).list();

  displayRef = reference;

  return INPUT;
  }

    }      


