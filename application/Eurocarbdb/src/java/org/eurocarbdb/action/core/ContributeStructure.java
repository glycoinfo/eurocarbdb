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
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.SequenceFormat;

import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;
import org.eurocarbdb.application.glycanbuilder.GlycanDocument;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.FileUtils;
import org.eurocarbdb.application.glycanbuilder.LogUtils;
import org.eurocarbdb.application.glycanbuilder.TextUtils;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.hibernate.*;

/*  class ContributeStructure  *//***********************************
*
*   Action for entering/creating a glycan structure with biological
*   context
*
*   @author          hirenj
*   @version         $Rev: 1870 $
*/
@org.eurocarbdb.action.ParameterChecking( 
    whitelist = { "glycanSequence"
                , "biologicalContext"
                , "references"
                , "glycanSequenceId"
                , "biologicalContextId" ,"glycanSequenceContext", "glycanSequenceContextId"
                }
)
public class ContributeStructure extends EurocarbAction implements RequiresLogin
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    protected static final Logger log  = Logger.getLogger( ContributeStructure.class );
    
    private GlycanSequence glycanSequence = null;   
    private BiologicalContext biologicalContext = null;      
    private GlycanSequenceContext glycanSequenceContext=null;
    public GlycanSequenceContext getGlycanSequenceContext() {
		return glycanSequenceContext;
	}


	public void setGlycanSequenceContext(GlycanSequenceContext glycanSequenceContext) {
		this.glycanSequenceContext = glycanSequenceContext;
	}


	private String comment = null;
//     public String passErrorMessage = null;
    private List<Reference> references = new java.util.ArrayList<Reference>();
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    public void setGlycanSequence(GlycanSequence s) 
    {
        glycanSequence = s;
    }
    

    public GlycanSequence getGlycanSequence() 
    {
        return glycanSequence;
    }
   
    
    public void setBiologicalContext(BiologicalContext bc) 
    {
        biologicalContext = bc;
    }

    
    public BiologicalContext getBiologicalContext() 
    {
        return biologicalContext;
    }

    
    public List<Reference> getReferences() 
    {
        return references;
    }

    
    public void setReferences(List<Reference> refs) 
    {
        references = refs;
    }
    

    public void setParameters(Map params)
    {
        
        glycanSequence = getObjectFromParams(GlycanSequence.class, params);

        biologicalContext = getObjectFromParams(BiologicalContext.class, params);

        comment=(String) params.get("bc_comment");

//         passErrorMessage=(String) params.get("passErrorMessage");
        
        super.setParameters(params);
    }

  
    public String execute() throws Exception 
    {

        //*************************    
        // finalize the GlycanSequence creation and store it in the database
    
        if( glycanSequence!=null ) 
        {        
            // store sequence
            if( glycanSequence.getGlycanSequenceId() <= 0  ) 
            {
                //glycanSequence = GlycanSequence.create( getCurrentContributor(),glycanSequence.getSequenceCt());
                log.debug("saving new glycan_sequence");
                SugarSequence sseq = glycanSequence.getSugarSequence(); 
                glycanSequence = GlycanSequence.lookupOrCreateNew( sseq );
                getEntityManager().refresh( glycanSequence.getContributor() );
                try 
                {
                    getEntityManager().store( glycanSequence );
                } 
                catch (Exception e) 
                {
                    glycanSequence = null;
                    throw e;
                }
            }
                
            // add BC to sequence
            if( biologicalContext!=null ) 
            {
            	/*Not associated with this biological context*/
            	if(biologicalContext.getBiologicalContextContributor(Contributor.getCurrentContributor().getContributorId())==null){
            		biologicalContext.addContributor(this.getCurrentContributor(), this.comment);
            	}
            	
            	 try 
                 {
                     getEntityManager().store(biologicalContext);
                 } 
                 catch (Exception e) 
                 {
                     biologicalContext = null;
                     throw e;
                 }
            	
            	/*Contributor is associated with this biological context*/
            	/*Glycan associated with this biological context*/
            	boolean match=false;
            	for(GlycanSequenceContext glycanSequenceContext:glycanSequence.getGlycanSequenceContexts()){
            		if(glycanSequenceContext.getBiologicalContext().getBiologicalContextId()==biologicalContext.getBiologicalContextId() &&
            				glycanSequenceContext.getContributor().getContributorId()==Contributor.getCurrentContributor().getContributorId()
            			){
            			match=true;
            		}
            	}
            	if(match){
            		passErrorMessage="You have already associated glycan ("+glycanSequence.getGlycanSequenceId()+")"+" with this biological context";
            		return "input_bc";
            	}else{
            		/**
            		 * This should not be so ugly....
            		 */
            		glycanSequenceContext = new GlycanSequenceContext();
            		glycanSequenceContext.setBiologicalContext(biologicalContext);
            		glycanSequenceContext.setContributor(Contributor.getCurrentContributor());
            		glycanSequenceContext.setGlycanSequence(glycanSequence);
            		glycanSequenceContext.getGlycanSequenceContextId();
            		//glycanSequence.getGlycanSequenceContexts(); 
            		//glycanSequence.getGlycanSequenceContexts().add(gsc);
            		//biologicalContext.getGlycanSequenceContexts().add(gsc);
            		try{
            			
            			getEntityManager().store(glycanSequenceContext);
            		}catch(Exception e){
            			biologicalContext = null;
            			throw e;
            		}
            	}
            	
               
                //glycanSequence.addBiologicalContext(biologicalContext);
            }
            
            references = glycanSequence.getReferences( getCurrentContributor() );
            
            getEntityManager().update(glycanSequence);                  
        
            
            return SUCCESS;
        }    
                    
        return "input";
    }    

} // end class
