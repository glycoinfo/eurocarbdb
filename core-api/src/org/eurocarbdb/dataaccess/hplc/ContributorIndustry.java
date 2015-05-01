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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Criteria;
import org.hibernate.criterion.*;

//  eurocarb imports
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.Contributed;
import org.eurocarbdb.dataaccess.BasicEurocarbObject;

//  static imports
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
*   A contributor is a person, group, or institution that contributes
*   data/information to EurocarbDB.
*
*****
*<h3>Finding the current Contributor</h3>
*<pre>
*       Contributor current = Contributor.getCurrentContributor();
*</pre>
*
*****
*<h3>Getting the 'guest' Contributor</h3>
*<pre>
*       Contributor guest = Contributor.getGuestContributor();
*</pre>
*
*****
*<h3>Retrieving or counting objects for a given Contributor</h3>   
*<p>
*   The general way of doing this is as follows:
*<pre>
*       //  instantiate the appropriate Contributor, eg:
*       Contributor c = {@link Eurocarb}.getEntityManager().lookup( Contributor.class, id );
*
*       //  count objects of a certain type this Contributor has contributed
*       //  note that this method also counts all contributed *subclasses* of 
*       //  the passed class in addition to the passed class.
*       int count_my_evidence = c.countMyContributionsOf( Evidence.class ); 
*       int count_my_sequences = c.countMyContributionsOf( GlycanSequence.class );
*
*       //  retrieve all these objects, ordered by newest to oldest
*       //  note that this method also retrieves contributed *subclasses* of 
*       //  the passed class in addition to the passed class.
*       List&lt;Evidence&gt; my_evidence = c.getMyContributionsOf( Evidence.class );
*
*       //  or just retrieve a sublist of them, eg: the most recent 10 sequences
*       List&lt;GlycanSequence&gt; my_evidence = c.getMyContributionsOf( GlycanSequence.class, 10 );
*
*       //  or a pages sublist of them, eg: the 20-29th most recent 
*       List&lt;GlycanSequence&gt; my_evidence = c.getMyContributionsOf( GlycanSequence.class, 10, 20 );
*</pre>
*   Note also that these method only work with classes that implement the 
*   {@link Contributed} interface (any class that is associatable to a Contributor
*   should always implement this interface).
*</p>
*****
*
*   @see Contributed
*   @author mjh
*/
public class ContributorIndustry 
extends BasicEurocarbObject 
implements Serializable 
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    static final Logger log = Logger.getLogger( ContributorIndustry.class );
    
        
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Database-supplied id for this contributor */
    private int contributorId;
    
    /** Username of this contributor. */
    private String contributorName;

    /** Login password for this contributor. */
    private String password;

    /** Full name. */
    private String fullName;

    /** Institution */
    private String institution;

    /** True if contributor has administrative rights */
    private String email;

    /** Date on which this contributor was created in the DB. */
    private Date dateEntered;
    

    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor */
    public ContributorIndustry() 
    {
    }
    

    /** Constructor #2 */
    public ContributorIndustry( String unique_name ) 
    { 
        setContributorName( unique_name );
    }
   
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~//

    
    
    /*  getCurrentContributor  *//***********************************
    *
    *   Returns the currently active {@link Contributor}, or the 
    *   "guest" contributor ({@link #getGuestContributor()})
    *   if no Contributor is considered active in ("logged into") 
    *   the current thread.
    *
    *   @see #isGuest()
    *   @see Eurocarb.getCurrentContributor()
    */
    
    public int getContributorId() 
    {
        return this.contributorId;
    }
    
    
    /** Internal use only */
    protected void setContributorId( int contributorId ) 
    {
        this.contributorId = contributorId;
    }


    /** 
    *   Returns this Contributor's unique (user-given) name. 
    */
    public String getContributorName()
    {
        return this.contributorName;
    }
    
    
    /** 
    *   Sets this Contributor's unique (user-given) name. 
    *   @throws IllegalArgumentException if the given name 
    *   would break the uniqueness requirement of contributor names.
    */
    public void setContributorName( String name )
    throws IllegalArgumentException
    {
        //  TODO - lookup name in DB before assigning
        this.contributorName = name;
    }


    /** Returns the full name of this Contributor. */
    public String getFullName() 
    {
        return this.fullName;
    }
    
    
    /** 
    *   Sets the full name of this Contributor. 
    */
    public void setFullName( String fullName ) 
    {
        this.fullName = fullName;
    }

    /** Returns the institution of this Contributor. */
    public String getInstitution() 
    {
        return this.institution;
    }    
    
    /** 
    *   Sets the institution of this Contributor. 
    */
    public void setInstitution( String institution ) 
    {
        this.institution = institution;
    }

    /** Returns the password for this Contributor. */
    public String getPassword() 
    {
        return this.password;
    }
        
    /** 
    *   Sets the password for this Contributor. 
    */
    public void setPassword( String password ) 
    {
        this.password = password;
    }

    /** Return true if the user has administrative rights
     */
    public String getEmail() 
    {
        return this.email;
    }

    /** Set the flag indicating if the user has administrative rights
     */
    public void setEmail( String e)
    {
        this.email = e;
    }    
   
    /** Returns the {@link Date} this Contributor was created. */
    public Date getDateEntered() 
    {
        return this.dateEntered;
    }
        
    /** 
    *   Sets the {@link Date} this Contributor was created
    *   (privileged operation). 
    */
    public void setDateEntered( Date dateEntered ) 
    {
        this.dateEntered = dateEntered;
    }
    
    


} // end class
