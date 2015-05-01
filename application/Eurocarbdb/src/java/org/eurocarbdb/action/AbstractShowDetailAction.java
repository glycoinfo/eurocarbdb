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

package org.eurocarbdb.action;

//  stdlib imports
import java.util.List;
import java.util.Collections;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
// import org.eurocarbdb.dataaccess.indexes.Index;
// import org.eurocarbdb.dataaccess.indexes.Indexable;
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EurocarbObject;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*<p>
*   This (abstract) class implements a lazy man's "detail action" for
*   a single object, given an ID.
*</p>
*<p>
*   In order "to just work", this Action just needs an ID to be set, via
*   the {@link #setId} method (normally supplied by incoming CGI 
*   parameters in a web application).
*</p>
*
*<h2>Extending this class</h2>
*<p>
*   Just implement the method {@link #getGenericType} to return the 
*   correct {@link Class} of the objects you want to detail. Eg:
*<pre>
*       public ShowEvidenceAction extends AbstractShowDetailAction&lt;Evidence&gt;
*       {
*           public Class&lt;Evidence&gt; getGenericType()
*           {
*               return Evidence.class;
*           }
*
*           //  not strictly required, but strongly advised, as some 
*           //  template code assumes this setter will exist.
*           public void setEvidenceId( int evidence_id )
*           {
*               setId( evidence_id );
*           }
*       }
*</pre>
*   Then, create a config entry for this action (according to convention, 
*   this should be "show_evidence"), and then create a "success.ftl" 
*   freemarker template to display the data for the Evidence object
*   returned by {@link #getDetailedObject}.
*</p>
*<p>
*   It is <em>strongly</em> advised to also implement the additional 
*   setter method "set[class-name]Id" as above.
*<p>
*
*   @see EurocarbAction
*   @see EurocarbObject
*
*   @author mjh
*/
public abstract class AbstractShowDetailAction<T extends EurocarbObject> 
extends EurocarbAction
{
    /** logging handle */
    static Logger log = Logger.getLogger( AbstractShowDetailAction.class );
    
    /** The ID of the object to detail. */
    protected int id = -1;

    /** The object being detailed. */
    protected T detailedObject = null;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
    *   Returns the {@link Class} type of T, the generic type this class
    *   is implemented in terms of.
    */
    public abstract Class<T> getGenericType()
    ;
    
    
    public int getId()
    {
        return id;   
    }
    
    
    public void setId( int id )
    {
        if ( id > 0 )
            this.id = id;       
    }
    
    
    /** 
    *   Returns the object being detailed, obtained by looking up
    *   the {@link Class} given by {@link #getGenericType} and ID
    *   given by {@link #getId}.
    */
    public T getDetailedObject()
    {
        return detailedObject;    
    }
    
    
    /** 
    *   Looks up the detailed object by ID, returning "success"
    *   if the object was looked up without error and not-null,
    *   and returns "input" if not.   
    */
    public String execute()
    {
        if ( id <= 0 )
            return "input";
            
        Class<T> the_class = getGenericType();
        int the_id = getId();
        try
        {
            detailedObject = (T) getEntityManager().lookup( the_class, the_id );       
        }
        catch ( Exception ex )
        {
            log.warn(
                "Caught exception while looking up object of "
                + the_class
                + " with id='"
                + the_id
                + "': returning 'input' view..."
                , ex
            );   
            return "input";
        }
        
        if ( detailedObject == null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( 
                    "lookup of " 
                    + the_class.getSimpleName()
                    + " with id="
                    + the_id
                    + " returned null, returning input view..."
                );
            }
            
            return "input";
        }
        
        if ( log.isDebugEnabled() )
            log.debug( "successfully loaded " + detailedObject );
        
        return "success";
    }
 
} // end class


