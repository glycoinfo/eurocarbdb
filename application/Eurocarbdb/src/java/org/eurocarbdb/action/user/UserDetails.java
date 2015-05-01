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
package org.eurocarbdb.action.user;

import org.apache.log4j.Logger;

import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.action.AbstractUserAwareAction;
import org.eurocarbdb.action.RequiresLogin;
import org.eurocarbdb.action.ParameterChecking;

import com.opensymphony.xwork.interceptor.ParameterNameAware;

import java.util.Arrays;



//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
* Action for updating user details and preferences
* @author hirenj
*/
@ParameterChecking( whitelist = { "contributor.contributorName", "contributor.fullName", "contributor.institution" })
public class UserDetails extends AbstractUserAwareAction implements RequiresLogin, ParameterNameAware
{
    private static final Logger log 
        = Logger.getLogger( UserDetails.class );
    
    /*
     * Set the user details for the current contributor
     */
    public String updateDetails() {
        getEntityManager().update(getContributor());
        return "success";
    }
}
