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
import org.eurocarbdb.dataaccess.hplc.*;

import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;
import org.eurocarbdb.application.glycanbuilder.GlycanDocument;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.FileUtils;
import org.eurocarbdb.application.glycanbuilder.LogUtils;
import org.eurocarbdb.application.glycanbuilder.TextUtils;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.hibernate.*;

public class CreateHplcBiologicalContext extends EurocarbAction implements RequiresLogin
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
   
    /** Logging handle. */
    protected static final Logger log  = Logger.getLogger( CreateHplcBiologicalContext.class.getName() );
       
    private Taxonomy taxonomy = null;
    private TissueTaxonomy tissueTaxonomy = null;    
    private Set<Disease> diseases = null;
    private Set<Perturbation> perturbations = null;
    private BiologicalContext biologicalContext = null; 
    private int bcId;
    private int storedBcId;
    private int displayBcId;
    private int profileId;
    private int displayProfileId;
    
    // output message
    private String strMessage = "";

    private String submitAction = null;
        
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
   
    public String getMessage()
    {
        return strMessage;
    }
    
    public void setMessage(String strMessage)
    {
        this.strMessage = strMessage;
    }   

    public void setSubmitAction(String s) 
    {
        submitAction = s;
    }   

    public void setBiologicalContext(BiologicalContext bc) 
    {
        biologicalContext = bc;
    }

    public BiologicalContext getBiologicalContext() 
    {
        return biologicalContext;
    }

    
    public int getBcId() { return this.storedBcId; }
    public void setBcId( int store_bc_id) {this.storedBcId = store_bc_id; }
 
    public int getDisplayBcId() {return this.displayBcId; }

    public int getProfileId() { return this.profileId;}
    public void setProfileId ( int profile_id) {this.profileId = profile_id;}
    public int getDisplayProfileId() { return this.displayProfileId; }

    public void setTaxonomySearch(String text) 
    {
    
        // exact search by taxon
        if( text!=null && text.length()>0 ) 
        {
            Criteria crit = getEntityManager().createQuery(Taxonomy.class).add(Restrictions.eq("taxon",text));    
            java.util.Collection<Taxonomy> list = crit.list();
            if( list!=null && list.size()>0 ) 
            {
                taxonomy =  list.iterator().next();
                return;
            }
        }
        taxonomy = null;    
    }
    

    public void setTissueTaxonomySearch(String text) 
    {

        // exact search by taxon
        if( text!=null && text.length()>0 ) 
        {
            Criteria crit = getEntityManager().createQuery(TissueTaxonomy.class).add(Restrictions.eq("tissueTaxon",text));    
            java.util.Collection<TissueTaxonomy> list = crit.list();
            if( list!=null && list.size()>0 ) 
            {
                tissueTaxonomy =  list.iterator().next();
                return;
            }
        }
        tissueTaxonomy = null;    
    }

    public void setDiseaseSearch(String[] values) 
    {
    
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
        BiologicalContext bc = new BiologicalContext();
        
        //  set taxonomy & tissue taxonomy
        bc.setTaxonomy( taxonomy );
        bc.setTissueTaxonomy( tissueTaxonomy );
        
        //  add disease associations, if any
        if( diseases!=null ) 
        {
            Set<DiseaseContext> dcSet = new HashSet<DiseaseContext>(); //bc.getDiseaseContexts();
            for ( Disease d : diseases )
        dcSet.add( new DiseaseContext( bc, d ) );               
            
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

        return bc;        
    }
   
    public String execute() throws Exception 
    {

    
        //*************************    
        // cancel action

        if( submitAction!=null && submitAction.equals("Skip"))
        {
            return "skip";
        }
    
        if( submitAction!=null && submitAction.equals("Cancel") ) 
        {
            return "cancel";
        }

    if( submitAction!=null && (submitAction.equals("Next") || submitAction.equals("Store")) )
        {
        try { 
        
        //Profile profile = getEntityManager().lookup( Profile.class, profileId);
        Profile p = Profile.lookupById(profileId);
        int t = p.getEvidenceId(); 
        //int evidenceId = profile.getEvidenceId();
        log.info("corresponding eviden" + t);
        
        biologicalContext = createBiologicalContext();
        getEntityManager().store( biologicalContext ); //this works
        int bcId = biologicalContext.getBiologicalContextId();
        log.info("stored bc" + bcId);
        displayBcId = bcId;
        
        Evidence e = getEntityManager().lookup( Evidence.class, t);
        
        EvidenceContext ev = new EvidenceContext();
        //ev.addEvidence ( e) ;
        
        e.addBiologicalContext(biologicalContext);
        getEntityManager().store(e);
    
        return "success";
        }
        catch(Exception e) {
        setMessage(e.getMessage());
        return "input";
        }                                
        }                                     

    return "input";

    }  

} // end class
