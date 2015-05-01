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
*   Last commit: $Rev$ by $Author$ on $Date::             $  
*/

package org.eurocarbdb.action.core;

import org.eurocarbdb.action.BrowseAction;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.core.GlycanSequenceContext;

import org.eurocarbdb.action.ParameterChecking;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class BrowseContributorStructures
*
*   descr here
*
*/
@ParameterChecking( whitelist = { "contributorId" })
public class BrowseContributorContexts extends BrowseAction<GlycanSequenceContext>
{
    private Contributor contributor = Contributor.getCurrentContributor();
    
    public void setContributorId(int id) 
    {
        contributor = getEntityManager().lookup( Contributor.class, id );
        if( contributor == null ) 
            contributor = Contributor.getGuestContributor();
    }

    public int getContributorId() 
    {
        return contributor.getContributorId();
    }


    public void setContributor(Contributor c) 
    { 
        contributor = c; 
        if( contributor == null ) 
            contributor = Contributor.getGuestContributor();
    }

    public Contributor getContributor() 
    {
        return contributor;
    } 
    

    public String execute()
    {
        setAllResults(contributor.getMyContributionsOf( GlycanSequenceContext.class,-1,0 ));
        return SUCCESS;
    }
    
    
    public final Class<GlycanSequenceContext> getIndexableType() 
    {
        return GlycanSequenceContext.class;
    }
    
   
}

