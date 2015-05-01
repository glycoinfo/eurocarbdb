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
*   Last commit: $Rev: 1581 $ by $Author: hirenj $ on $Date:: 2009-07-28 #$  
*/

package org.eurocarbdb.action.core;

import java.util.List;
import java.util.ArrayList;

//  3rd party imports 
import org.apache.log4j.Logger;
import org.hibernate.*; 
import org.hibernate.criterion.*; 
import com.opensymphony.xwork.ValidationAware;

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.RequiresLogin;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.Eurocarb;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.hibernate.*;


@org.eurocarbdb.action.ParameterChecking(blacklist={"reference.referenceId","journalReference.referenceId"})
public class CreateReferences extends EurocarbAction implements RequiresLogin, ValidationAware
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging handle. */
    protected static final Logger log  = Logger.getLogger( CreateReferences.class.getName() );
       
    /** Resultant data structure */
    private List<Reference> references = new ArrayList<Reference>();

    /** Input data structure */
    private Reference reference;

    private int deleteReference;

    /** Input data structure */
    private JournalReference journalReference;
        
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//  

    public List<Reference> getReferences() 
    {
        return references;
    }

    public void setReferences(List<Reference> refs) 
    {
        references = refs;
    }

    public Reference getReference() 
    {
        return reference;
    }

    public void setReference(Reference ref) 
    {
        reference = ref;
    }
    
    public JournalReference getJournalReference() 
    {
        return journalReference;
    }

    public void setJournalReference(JournalReference ref) 
    {
        journalReference = ref;
    }

    public void setDeleteReference(int refId)
    {
        this.deleteReference = refId;
    }

    public int getDeleteReference()
    {
        return this.deleteReference;
    }

    public String execute() throws Exception 
    {    
        
        if (this.references != null && this.deleteReference > 0 && this.references.size() > (this.deleteReference-1)) {
            this.references.remove(this.deleteReference-1);
            return "success";
        }
        
        if (journalReference == null && reference == null) {
            return "input";
        }
        
        Reference toSave = null;
        
        if (journalReference != null) {
            
            if (journalReference.getPubmedId() != 0) {
                toSave = JournalReference.createFromPubmedId(journalReference.getPubmedId());
            } else {
                toSave = journalReference;
            }
        }
        
        if (reference != null) {
            toSave = reference;
        }

        if (references == null) {
            references = new ArrayList<Reference>();
        }

        if (toSave != null && ! references.contains(toSave)) {
            if (toSave instanceof JournalReference && ((JournalReference) toSave).getJournal().getJournalId() <= 0) {                
                JournalReference jref = (JournalReference) toSave;
                jref.setJournal(jref.getJournal().storeOrLookup());
            }
            if (toSave.getReferenceId() <= 0 ) {
                toSave.setContributor(this.getCurrentContributor());
                getEntityManager().store( toSave );
                toSave = getEntityManager().lookup(Reference.class,toSave.getReferenceId());
            }
            references.add(toSave);
        }
        
        reference = toSave;
        
        return "success";
    }


}
 