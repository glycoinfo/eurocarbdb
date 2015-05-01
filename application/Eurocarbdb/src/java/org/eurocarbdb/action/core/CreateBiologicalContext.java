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
*   Last commit: $Rev: 1870 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-02-23 #$  
*/

package org.eurocarbdb.action.core;

//  stdlib imports
import java.util.*;

import java.io.*;
import java.net.*;

//  3rd party imports 
import org.apache.log4j.Logger;
import org.hibernate.*; 
import org.hibernate.criterion.*; 

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.RequiresLogin;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.*;

import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;
import org.eurocarbdb.application.glycanbuilder.GlycanDocument;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.FileUtils;
import org.eurocarbdb.application.glycanbuilder.LogUtils;
import org.eurocarbdb.application.glycanbuilder.TextUtils;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.dataaccess.Eurocarb.getHqlQuery;
import org.eurocarbdb.dataaccess.hibernate.*;

/*  class ContributeStructure ***********************************
*
*   Action for entering/creating a glycan structure with biological
*   context
*
*   @author          ac [a.ceroni@imperial.ac.uk]
*   @version         $Rev: 1870 $

*/
@org.eurocarbdb.action.ParameterChecking(whitelist={"taxonomySearch","tissueTaxonomySearch","diseaseSearch","perturbationSearch"}, blacklist={"biologicalContextId"})
public class CreateBiologicalContext extends EurocarbAction implements RequiresLogin
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
   
    /** Logging handle. */
    protected static final Logger log  = Logger.getLogger( ContributeStructure.class.getName() );
       
    private Taxonomy taxonomy = null;
    private TissueTaxonomy tissueTaxonomy = null;    
    private Set<Disease> diseases = null;
    private Set<Perturbation> perturbations = null;
    private BiologicalContext biologicalContext = null;    
    private String comment = null;
            
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    public void setParameters(Map params)
    {
        
        comment=(String) params.get("bc_comment");

        super.setParameters(params);
    }
   
    public void setBiologicalContext(BiologicalContext bc) 
    {
        biologicalContext = bc;
    }

    public BiologicalContext getBiologicalContext() 
    {
        return biologicalContext;
    }

    public void setTaxonomySearch(String text) 
    {
        text = text.toLowerCase();
        log.debug("Searching for taxonomy with name "+text);
        // exact search by taxon
        if( text!=null && text.length()>0 ) 
        {
        	log.debug("In first!");
            //Criteria crit = getEntityManager().createQuery(Taxonomy.class).add(Restrictions.eq("taxon",text));
        	Query hqlQuery=getHqlQuery("SELECT t FROM Taxonomy t WHERE lower(t.taxon)=?");
        	hqlQuery.setString(0, text);
            java.util.Collection<Taxonomy> list = hqlQuery.list();
            if( list!=null && list.size()>0 ) 
            {
            	log.debug("In second!");
                taxonomy =  list.iterator().next();
                log.debug("Found a taxonomy "+taxonomy.getTaxonomyId());
                return;
            }
        }
        log.debug("In third!");
        taxonomy = null;    
    }
    

    public void setTissueTaxonomySearch(String text) 
    {
        log.debug("Searching for tissue with name "+text);
        // exact search by taxon
        if( text!=null && text.length()>0 ) 
        {
            Criteria crit = getEntityManager().createQuery(TissueTaxonomy.class).add(Restrictions.eq("tissueTaxon",text));    
            java.util.Collection<TissueTaxonomy> list = crit.list();
            if( list!=null && list.size()>0 ) 
            {
                tissueTaxonomy =  list.iterator().next();
                log.debug("Found a tissueTaxonomy "+tissueTaxonomy.getId());
                return;
            }
        }
        tissueTaxonomy = null;    
    }

    public void setDiseaseSearch(String[] values) 
    {
        log.debug("Searching for disease with names in array size of "+values.length);
        diseases = new HashSet<Disease>();
        if( values!=null ) 
        {
            for( int i=0; i<values.length; i++ ) 
            {
                String text = values[i];
                
                // exact search by disease name
                if( text!=null && text.length()>0 ) 
                {
                    Criteria crit = getEntityManager().createQuery(Disease.class).add(Restrictions.eq("diseaseName",text));    
                    java.util.Collection<Disease> list = crit.list();
                    if( list!=null && list.size()>0 ) 
                    {
                        log.debug("Found a disease");
                        diseases.add(list.iterator().next());
                    }
                }
            }
        }
    }
    
    public void setPerturbationSearch(String[] values) 
    {
        perturbations = new HashSet<Perturbation>();
        if( values!=null ) 
        {
            for( int i=0; i<values.length; i++ ) 
            {
                String text = values[i];
                
                // exact search by disease name
                if( text!=null && text.length()>0 ) 
                {
                    Criteria crit = getEntityManager().createQuery(Perturbation.class).add(Restrictions.eq("perturbationName",text));    
                    java.util.Collection<Perturbation> list = crit.list();
                    if( list!=null && list.size()>0 ) 
                    {
                        perturbations.add(list.iterator().next());
                    }
                }
            }
        }
    }
      

    protected BiologicalContext createBiologicalContext() throws Exception
    {
        BiologicalContext bc=null;
        log.debug("--FIND ME--");
//        log.debug("Tissue taxonomy id is: "+tissueTaxonomy.getTissueTaxonomyId());
        Query checkForExistingBiologicalContext=getHqlQuery("SELECT bc FROM BiologicalContext bc where bc.taxonomy.taxonomyId=?");
        checkForExistingBiologicalContext.setInteger(0, taxonomy.getTaxonomyId());
        List<BiologicalContext> bcList=checkForExistingBiologicalContext.list();
        if(bcList.size()!=0){
        	for(BiologicalContext bcItem:bcList){
        		log.debug("Matched contexts based on taxonomyId");
        		if(tissueTaxonomy==null){
        			if(bcItem.getTissueTaxonomy().getTissueTaxonomyId()==1){
        				log.debug("Found matching bc");
            			bc=bcItem;
            			break;
        			}
        		}else if(bcItem.getTissueTaxonomy().getTissueTaxonomyId()==tissueTaxonomy.getTissueTaxonomyId()){
        			log.debug("Found matching bc");
        			bc=bcItem;
        			break;
        		}
        	}
        	
        }

        if(bc==null){
        	bc=new BiologicalContext();
        	// set taxonomy & tissue taxonomy
            bc.setTaxonomy( taxonomy );
            bc.setTissueTaxonomy( tissueTaxonomy );
            
            //  add disease associations, if any
            if( diseases!=null ) 
            {
                Set<DiseaseContext> dcSet = new HashSet<DiseaseContext>(); //bc.getDiseaseContexts();
                for ( Disease d : diseases ) {
                    dcSet.add( new DiseaseContext( bc, d ) );               
                }
                bc.setDiseaseContexts( dcSet );
            }
                
                //  add perturbation associations, if any
            if( perturbations!=null ) 
            {
                Set<PerturbationContext> pcSet = new HashSet<PerturbationContext>(); //bc.getDiseaseContexts();
                for ( Perturbation p : perturbations )
                	pcSet.add( new PerturbationContext( bc, p ) );    
                
                bc.setPerturbationContexts( pcSet );
            }      	
        }//else{
        	//bc=null;
        //}
      
        
        return bc;        
    }
   
    public String execute() throws Exception 
    {
    	log.debug("---Creating biological context---");
        if ( taxonomy == null ) {
        	log.debug("Taxonomy is null!");
            return "input";
        }
        try { 
            biologicalContext = createBiologicalContext();
            if( biologicalContext !=null){
            	//if(biologicalContext.getBiologicalContextContributor(Contributor.getCurrentContributor().getContributorId())==null){
            		//biologicalContext.addContributor(Contributor.getCurrentContributor(), comment);
            	//}else{
//            		addActionError("You have already contributed this association...");
  //          		return "input";
            //	}
            	getEntityManager().store( biologicalContext );
            	return "success";
            }else{
            	addActionError("An unexpected error has occured");
        		return "input";
            }
        }
        catch(Exception e) {
        	StringWriter sw = new StringWriter();
        	e.printStackTrace(new PrintWriter(sw));
        	String stacktrace = sw.toString();
        	log.debug("Exception storing new bc: "+stacktrace);
            return "input";
        }                             
    }  

} // end class
