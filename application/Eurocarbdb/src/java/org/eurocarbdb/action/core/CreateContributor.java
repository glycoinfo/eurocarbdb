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

//  stdlib imports

//  3rd party imports 
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.RequiresAdminLogin;
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.Contributor;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class CreateContributor  *//*************************************
*
*   Action for entering/creating a new Eurocarb data contributor.
*
*   This action is not yet intended to work, due to the fact we're
*   not attempting to solve the user management/permissions component
*   just yet.
*
*   @author  mjh
*   @version $Rev: 2113$
*/
public class CreateContributor extends EurocarbAction implements RequiresAdminLogin
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( CreateContributor.class.getName() );
    
    
    /** The contributor we're creating. */
    public Contributor contributor;
    
    private String strMessage = "";
    
    private String repeatedPassword = "";

    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    public String getMessage()
    {
        return strMessage;
    }
    
    public void setMessage(String strMessage)
    {
        this.strMessage = strMessage;
    }

    public String getRepeatedPassword()
    {
        return repeatedPassword;
    }
    
    public void setRepeatedPassword(String repeatedPassword)
    {
        this.repeatedPassword = repeatedPassword;
    }

    /** Accesses the Contributor that is being created & manipulated. */
    public Contributor getContributor() {
    if ( contributor == null ) 
        contributor = getEntityManager().createNew( Contributor.class );        
    
    return contributor;
    }
    

    /**
    *<ol>   
    *   <li>check current user is not logged in</li>
    *   <li>validate new user name & params</li>
    *   <li>create new user in data store</li>
    *</ol>
    */
    public String execute()   {
        //  check that noone is currently logged in first
        /*Contributor c = this.getCurrentContributor();
        
        if ( c != null && c.isLoggedIn() )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "contributor is already logged in as '"
                         + c.getContributorName() 
                         + "', returning 'error__already_logged_in' view"
                         );   
            }
            return "error__already_logged_in";
        }
    */  
        
    Contributor c = this.getContributor();
        String name = c.getContributorName();
        
        //  check name exists
    if ( name == null || name.length() == 0 ) 
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "contributor name not given or "
                         + "zero length, returning 'input' view"
                         );
            }
            return "input";
        }
        
        //  check name length is >= 3 chars
        if ( name.length() < 3 )
        {
            log.debug( "contributor name too short, returning 'input' view" );
            addFieldError("contributor.contributorName", "Name too short");
            return "input";
        }
        
        //  check name is unique
        if ( Contributor.lookupExactName( name ) != null )
        {
            String msg = "Contributor name '" + name + "' already exists";
            log.debug( msg + ", returning input view");   
            addFieldError("contributor.contributorName", msg );
            return "input";
        }
            
        //  add any other validation here...
        
    if( !c.getPassword().equals(repeatedPassword) ) {
        addFieldError("contributor.password", "password mismatch" );
            return "input";
    }
        
        //  all fine, time to save
        try 
        {
            log.debug("attempting to save new Contributor");   
            getEntityManager().store( c );  
            log.debug("Contributor saved");   
        }
        catch ( Exception bzzt ) 
        {
            log.warn( "Caught " 
                    + bzzt.getClass().getName() 
                    + " while saving new Contributor '" 
                    + name 
                    + "'"
                    , bzzt 
                    );
            
            addActionError( bzzt.getMessage() );
            return "input";  
        }
        
        return "success";
    }

} // end class
