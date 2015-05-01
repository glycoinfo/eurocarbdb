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
*   Last commit: $Rev: 1239 $ by $Author: hirenj $ on $Date:: 2009-06-23 #$  
*/

package org.eurocarbdb.action.core;

//  3rd party imports 
import org.apache.log4j.Logger;
import org.hibernate.*; 
import org.hibernate.criterion.*; 
import com.opensymphony.xwork.ValidationAware;

import java.util.Map;

//  eurocarb imports
import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.hibernate.*;


import org.eurocarbdb.action.exception.*;


//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

@org.eurocarbdb.action.ParameterChecking(whitelist={"reference.referenceId","glycanSequence.glycanSequenceId"})
public class EditGlycanSequence extends EurocarbAction implements RequiresLogin, EditingAction
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging handle. */
    protected static final Logger log  = Logger.getLogger( EditGlycanSequence.class.getName() );
       
    /** Input data structure */
    private Reference reference;
        
    private GlycanSequence glycanSequence = null;

        
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//  

    /**
     *  Get accessor for glycanSequence
     */
    public GlycanSequence getGlycanSequence()
    {
        return this.glycanSequence;
    }

    /**
     *  Set accessor for glycanSequence
     *  @param glycanSequence Data to set
     */
    public void setGlycanSequence(GlycanSequence glycanSequence)
    {
        this.glycanSequence = glycanSequence;
    }


    public Reference getReference() 
    {
        return reference;
    }

    public void setReference(Reference ref) 
    {
        reference = ref;
    }


    /**
     * No permissions to check
     */
    public void checkPermissions() throws InsufficientPermissions
    {
    }

    public void setParameters(Map params)
    {
        glycanSequence = getObjectFromParams(GlycanSequence.class, params);

        super.setParameters(params);

    }

    public String addReference() throws Exception
    {
        reference = getObjectFromParams(Reference.class, getParameters());            

        if ( reference == null ) {
            return "input";
        }

        glycanSequence.addReference(reference);

        Eurocarb.getEntityManager().store(glycanSequence);

        return "success";
    }

    public String deleteReference() throws Exception
    {

        reference = getObjectFromParams(Reference.class, getParameters());            

        if ( reference == null ) {
            return "input";
        }
        if (! getCurrentContributor().equals(glycanSequence.deleteReference(reference).getContributor())) {
            throw new InsufficientPermissions(this,"Reference to glycan sequence relationship not owned by current logged in user");
        }
        Eurocarb.getEntityManager().store(glycanSequence);
        Eurocarb.getEntityManager().store(reference);
        return "success";
    }

    public String execute() throws Exception 
    {    
        return "success";
    }


}
