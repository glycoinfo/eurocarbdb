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

package org.eurocarbdb.action.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.BiologicalContext;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.GlycanSequenceContext;

/**
*   Action to gather a set of info about a {@link BiologicalContext},
*   once supplied with a BiologicalContext id.
*
*   @author         mjh
*   @author         hirenj
*   @version        $Rev: 2570$
*/
public class ShowBiologicalContext extends EurocarbAction 
{
    private int biological_context_id = -1;
    private BiologicalContext biologicalContext = null; 
    
    private String strMessage = "";
    
    public String getMessage()
    {
        return strMessage;
    }
    
    public void setMessage(String strMessage)
    {
        this.strMessage = strMessage;
    }   
    
    
    public String execute() 
    {
        log.debug("attempting to show detail for BC=" + biological_context_id );
        
        if ( biological_context_id > 0 ) 
        {
            this.setBiologicalContext( 
                Eurocarb.getEntityManager().lookup(
                    BiologicalContext.class, biological_context_id ) );
            
            if( biologicalContext == null ) 
            {
                setMessage("Invalid biological context id: " + biological_context_id);
                return INPUT;
            }
            return SUCCESS;
        }
        return INPUT;
    }
    
    public void setBiologicalContextId(int id) 
    {
        biological_context_id = id;
    }
    
    public BiologicalContext getBiologicalContext() 
    {
        return biologicalContext;
    }
    
    public void setBiologicalContext(BiologicalContext biologicalContext) 
    {
        this.biologicalContext = biologicalContext;
    }
    
    /**
    *   @return     the glycanSequenceContexts associated with the 
    *               current {@link BiologicalContext}.
    */
    public Set<GlycanSequenceContext> getGlycanSequenceContexts() 
    {
        return this.getBiologicalContext().getGlycanSequenceContexts();
    }
    
    /**
    * @param glycanSequences the glycanSequences to set
    */
    public void setGlycanSequenceContexts(Set<GlycanSequenceContext> glycanSequenceContexts) 
    {
        this.getBiologicalContext().setGlycanSequenceContexts(glycanSequenceContexts);
    }
    
}
