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
*   Last commit: $Rev: 1906 $ by $Author: srikalyansswayam $ on $Date:: 2010-03-19 #$  
*/

package org.eurocarbdb.dataaccess.core;

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
 *  edited by @author srikalyan.
*/
public class Contributor 
extends BasicEurocarbObject 
implements Serializable, Comparable<Contributor> 
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Query string prefix shortcut */
    private static final String Q = "org.eurocarbdb.dataaccess.core.Contributor.";
    
    /** Singleton 'guest' contributor @see getGuestContributor() */
    private static Contributor Guest = null;
    
    /** Logging handle. */
    static final Logger log = Logger.getLogger( Contributor.class );
    
        
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

    /** OpenId Identifier */
    private String openId;

    /**
     * email address.
     */
    private String email;

    /****
     * status.
     */
    private Boolean isActivated;

    /**
     *  last login.
     */
    private Date lastLogin;

    /****
     * is Blocked.
     */

    private Boolean isBlocked;

    /** True if contributor has administrative rights */
    private Boolean isAdmin;

    /** Date on which this contributor was created in the DB. */
    private Date dateEntered;
    

    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor */
    public Contributor() 
    {
        isAdmin = false;
    }

    

    /** Constructor #2 */
    public Contributor( String unique_name ) 
    { 
        setContributorName( unique_name );
        isAdmin = false;
    }
   
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~//

    
    /*  getAllContributors  *//**************************************
    *
    *   Retrieves all contributors from the current data store as 
    *   a list.
    */
    @SuppressWarnings("unchecked")
    public static List<Contributor> getAllContributors()
    {
        log.debug("looking up all contributors");
        List contributors = getEntityManager()
                           .getQuery( Q + "ALL_CONTRIBUTORS" )
                           .list();

        return (List<Contributor>) contributors;
    }

    
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
    public static Contributor getCurrentContributor()
    {
        Contributor c = Eurocarb.getCurrentContributor();
        if ( c == null ) return getGuestContributor();
        return c;
    }
    
    
    /*  getGuestContributor  *//*************************************
    *
    *   Returns the canonical 'guest' Contributor, effectively an
    *   unregistered user.
    */
    public static Contributor getGuestContributor()
    {
        if ( Guest != null )
            return Guest;
        
        log.debug("looking up guest contributor (contributor_id=0)");
        Guest = getEntityManager().lookup( Contributor.class, 0 );
        
        if ( Guest == null )
        {
            log.debug("no guest contributor in data store, creating new guest");
            Guest = new Contributor("guest");
            Guest.setContributorId( 0 );
            try
            {
                log.debug("no guest contributor (id=0) in data store, creating it...");
                getEntityManager().store( Guest );   
            }
            catch ( RuntimeException ex )
            {
                log.warn("Caught exception while trying to save 'guest' contributor", ex ); 
                throw ex;
            }
        }
            
        return Guest;
    }
    

    /*  getMapOfGlycanSequenceCountByContributor  *//****************
    *
    *   Returns a {@link Map} of {@link Contributor}s to the number of {@link GlycanSequence}s
    *   they have contributed.
    */
    public static Map<Contributor,Integer> getMapOfGlycanSequenceCountByContributor()
    {
        log.debug("looking up map of contributors by number of glycan sequences contributed");
        List<Object[]> result = (List<Object[]>) getEntityManager()
                                .getQuery( Q + "list_contributors_by_count_glycan_sequences_contributed" )
                                .list();

        if ( result == null )
            return Collections.emptyMap();
            
        Map<Contributor,Integer> contrib_map 
            = new HashMap<Contributor,Integer>( result.size() );
            
        for ( Object[] row : result )
        {
            contrib_map.put( (Contributor) row[0], (Integer) row[1] );   
        }
        
        return contrib_map;
    }
    
    
    /*  lookupExactName  *//*****************************************
    *
    *   Retrieves a Contributor by their exact name.
    */
    public static Contributor lookupExactName( String name )
    {
        log.debug("looking up contributor by exact name");
        Object c = getEntityManager()
                  .getQuery( Q + "BY_EXACT_NAME" )
                  .setParameter("name", name )
                  .uniqueResult();

        assert c instanceof Contributor;
        
        return (Contributor) c;
    }


     /*  lookupExactEmail  *//*****************************************
    *
    *   Retrieves a Contributor by their exact Email.
    */
    public static Contributor lookupExactEmail( String email )
    {
        log.debug("looking up contributor by exact email");
        Object c = getEntityManager()
                  .getQuery( Q + "BY_EXACT_EMAIL" )
                  .setParameter("email", email )
                  .uniqueResult();

        assert c instanceof Contributor;

        return (Contributor) c;
    }

     /*  lookupExactNameNEmail  *//*****************************************
    *
    *   Retrieves a Contributor by their exact Name and Email.
    */
    public static Contributor lookupExactNameNEmail(String name, String email )
    {
        log.debug("looking up contributor by exact name and email");
        Object c = getEntityManager()
                  .getQuery( Q + "BY_EXACT_NAME_EMAIL" )
                  .setParameter("name", name )
                  .setParameter("email", email )
                  .uniqueResult();

        assert c instanceof Contributor;

        return (Contributor) c;
    }

    
 
    /** 
    *   Retrieve a contributor by looking them up by their 
    *   <a href="http://openid.net">OpenID identifier</a>.
    */
    public static Contributor lookupByIdentifier( String openId )
    {
        log.debug("looking up contributor by openId");
        Object c = getEntityManager()
                  .getQuery( Q + "BY_STRING_IDENTIFIER" )
                  .setParameter("openId", openId )
                  .uniqueResult();

        assert c instanceof Contributor;
        
        return (Contributor) c;        
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
 
    /*  @see java.lang.Comparable.compareTo */
    public int compareTo( Contributor c )
    {
        return this.getContributorName().compareTo( c.getContributorName() );
    }
    
    
    /*  isAdministrator  *//*****************************************
    *
    *   Returns true if this {@link Contributor} is an Administrator.
    */
    public boolean isAdministrator()
    {
        //  TODO
        return isAdmin;
    }
    
    
    /*  isGuest  *//*************************************************
    *
    *   Returns true if this {@link Contributor} is not a registered 
    *   user, ie: is a {@link #getGuestContributor() guest}.
    */
    public boolean isGuest()
    {
        return this == Guest
            || getContributorId() == 0 
            || getContributorName() == "guest";
    }
    
    
    /*  isLoggedIn  *//**********************************************
    *
    *   Returns true if this contributor is the active ("logged in") 
    *   Contributor for the current {@link Thread} -- Note that the 
    *   "guest" Contributor is NOT regarded as logged in, and hence 
    *   returns false from this method.
    */
    public boolean isLoggedIn()
    {
        return (this.isGuest()) 
            ?   false 
            :   this.equals( getCurrentContributor() );
    }
    
    
    /*  getContributorId  *//****************************************
    *
    *   Returns this Contributor's unique (numerical) id. 
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

    /** Returns the openId of this Contributor. */
    public String getOpenId() 
    {
        return this.openId;
    }    
    
    /** 
    *   Sets the openId of this Contributor. 
    */
    public void setOpenId( String openId ) 
    {
        this.openId = openId;
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
    public Boolean getIsAdmin() 
    {
        return this.isAdmin;
    }

    /** Set the flag indicating if the user has administrative rights
     */
    public void setIsAdmin(Boolean f)
    {
        this.isAdmin = f;
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

    /**
     *  gets the email address of the contributor.
     *
     */
    public String getEmail() {
        return email;
    }

    /***
     * sets the email address of the contributor.     *
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * gets the activation status of the contributor.     *
     */
    public Boolean getIsActivated() {
        return isActivated;
    }

    /**
     * sets the activation status of the contributor.
     * @param isActivated
     */
    public void setIsActivated(Boolean isActivated) {
        this.isActivated = isActivated;
    }

    /***
     * gets the blocked status of the contributor.
     * 
     */
    public Boolean getIsBlocked() {
        return isBlocked;
    }

    /***
     * sets the blocked status of the contributor.
     * @param isBlocked
     */
    public void setIsBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    /***
     * gets the last login date of the contributor.
     *
     */
    public Date getLastLogin() {
        return lastLogin;
    }

    /***
     * sets the last login date of the contributor.
     */
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    /***
     * gets the number of inactive unblocked contributors.
     * 
     */
    public int getNumberOfInactiveContributors()
    {
       List<Contributor> temp= getInactiveContributors();
       return temp==null?-1:temp.size();
    }

    /***
     * gets the list of inactive unblocked contributors.
     */
    public List<Contributor> getInactiveContributors()
    {        
        return isAdmin?(List<Contributor>)getEntityManager().getQuery(Q + "ALL_INACTIVE_CONTRIBUTORS").list():null;
    }    

    /***
     * gets the list of contributors who are active and are admins.
     * @return list of contributors who are admins.
     */
    public List<Contributor> getAllActiveNonAdmins()
    {
        return isAdmin?(List<Contributor>) Eurocarb.getEntityManager().getQuery(Q + "ACTIVE_CONTRIBUTORS_NON_ADMINS").list():null;
    }


    /***
     * gets a list of all active Admin except the current Admin.
     * @return
     */
    public List<Contributor> getAllActiveAdminsExceptCurrent()
    {
        //log.debug("current contributor name is "+contributorName);
        return isAdmin?(List<Contributor>) Eurocarb.getEntityManager().getQuery(Q + "ACTIVE_ADMINISTRATORS_NOT_CURRENT").setParameter("current", contributorName).list():null;
    }



    /*****
     * get the list of unblocked contributors who are not admins.
     * coz you cannot block admins. if you want demote them and unblock them.
     * @return
     */
    public List<Contributor> getAllActiveUnblockedContributors()
    {
        return isAdmin?(List<Contributor>) Eurocarb.getEntityManager().getQuery(Q + "ACTIVE_CONTRIBUTORS_NON_ADMINS").list():null;
    }

    /***
     * gets the list of the all blocked contributors.
     * @return
     */
    public List<Contributor> getAllBlockedContributors()
    {
        return isAdmin?(List<Contributor>) Eurocarb.getEntityManager().getQuery(Q + "ALL_BLOCKED_CONTRIBUTORS").list():null;
    }

    
    
    //  Query-driven methods  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public <T extends Contributed> Integer countMyContributionsOf( Class<T> c )
    {
        /*  TODO: this has to be done with Criteria query */
        
        // int id = this.getContributorId();
        Object result = getEntityManager()
                        .createQuery( c )
                        .setComment( this.getClass().getName() + ".countMyContributionsOf")
                        .add( Restrictions.eq( "contributor", this ) )
                        .setProjection( Projections.rowCount() )
                        .uniqueResult()
                        ;
        
        if ( result == null )
            return 0;
        
        assert result instanceof Integer;
        
        return (Integer) result;
    }
    
    
    /** 
    *   Convenience method for counting all the current {@link Contributor}'s 
    *   {@link GlycanSequence}s. Equivalent to calling the following: 
    *<pre>
    *       countMyContributionsOf( GlycanSequence.class )
    *</pre>.   
    */
    public Integer countMyGlycanSequences()
    {
        return countMyContributionsOf( GlycanSequence.class );   
    }
    
    
    /**
    *   Returns a sub-{@link List} of the requested object type, ordered by most recent first, 
    *   of objects that this {@link Contributor} has contributed. The size of the list 
    *   will be equal to the value of Eurocarb.getProperty("pref.show_max_recent_items"), 
    *   or 100 if not defined.
    *
    *   @see #getMyContributionsOf(Class,int,int)
    */
    public <T extends Contributed> List<T> getMyContributionsOf( Class<T> c )
    {
        int limit = Eurocarb.getProperty("pref.show_max_recent_items", Integer.class );
        return getMyContributionsOf( c, limit, 0 );
    }
    

    /**
    *   Returns a sub-{@link List} of the requested object type, ordered by most recent first, 
    *   of objects that this {@link Contributor} has contributed.
    *
    *   @see #getMyContributionsOf(Class,int,int)
    */
    public <T extends Contributed> List<T> getMyContributionsOf( Class<T> c, int limit )
    {
        return getMyContributionsOf( c, limit, 0 );
    }
    

    /**
    *   Returns a sub-{@link List} of the requested object type, ordered by most recent first, 
    *   of objects that this {@link Contributor} has contributed, limited by the given
    *   limit and index.
    *
    *   @param c 
    *   The class of the object type requested.
    *   @param limit 
    *   The max number of objects to return.
    *   @param index
    *   Index into the sub-list of the Contributor's total contributions. 
    */
    @SuppressWarnings("unchecked")
    public <T extends Contributed> List<T> getMyContributionsOf( Class<T> c, int limit, int index )
    {
        
        // int id = this.getContributorId();
        Criteria q = getEntityManager()
                    .createQuery( c )
                    .add( Restrictions.eq( "contributor", this ) )
                    .addOrder( Order.desc("dateEntered") )
                    // .setMaxResults( limit )
                    // .setFirstResult( index )
                    ;

        if ( limit > 0 )
        {
            q.setMaxResults( limit ); 
        }
         
        if ( index > 0 )
        {
            q.setFirstResult( index );   
        }
            
        Object result = q.list();
        
        if ( result == null )
            return Collections.emptyList();

        return (List<T>) result;
    }

    
    /*  getMyBiologicalContexts  *//*********************************
    *
    *   Returns all {@link BiologicalContext}s this {@link Contributor}
    *   has ever contributed.
    */
    @SuppressWarnings("unchecked")
    public List<BiologicalContext> getMyBiologicalContexts() 
    {
        int id = this.getContributorId();
        
        assert id > 0;
        
        if ( log.isDebugEnabled() )
            log.debug("looking up all BiologicalContexts for Contributor=" + id );
        
        List<BiologicalContext> list;
        list = (List<BiologicalContext>) getEntityManager()
             .getQuery( Q + "GET_ALL_CONTRIBUTED_CONTEXTS_BY_ID" )
             .setParameter("id", id )
             .list();
        
        if ( list == null )
            return emptyList();
             
        return list;
    }
    
    
    /*  getMyTaxonomies  *//*********************************
    *
    *   Returns all {@link Taxonomy}s this {@link Contributor}
    *   has ever linked to.
    */
    @SuppressWarnings("unchecked")
    public List<Taxonomy> getMyTaxonomies() 
    {
        int id = this.getContributorId();
        
        assert id > 0;
        
        if ( log.isDebugEnabled() )
            log.debug("looking up all Taxonomies for Contributor=" + id );
        
        List<Taxonomy> list;
        list = (List<Taxonomy>) getEntityManager()
             .getQuery( Q + "GET_ALL_CONTRIBUTED_TAXONOMIES_BY_ID" )
             .setParameter("id", id )
             .list();
        
        if ( list == null )
            return emptyList();
             
        return list;
    }
    
    
    /*  getMyTissueTaxonomies  *//*********************************
    *
    *   Returns all {@link TissueTaxonomy}s this {@link Contributor}
    *   has ever linked to.
    */
    @SuppressWarnings("unchecked")
    public List<TissueTaxonomy> getMyTissueTaxonomies() 
    {
        int id = this.getContributorId();
        
        assert id > 0;
        
        if ( log.isDebugEnabled() )
            log.debug("looking up all TissueTaxonomies for Contributor=" + id );
        
        List<TissueTaxonomy> list;
        list = (List<TissueTaxonomy>) getEntityManager()
             .getQuery( Q + "GET_ALL_CONTRIBUTED_TISSUETAXONOMIES_BY_ID" )
             .setParameter("id", id )
             .list();
        
        if ( list == null )
            return emptyList();
             
        return list;
    }

    
    /*  getMyDiseases  *//*********************************
    *
    *   Returns all {@link Disease}s this {@link Contributor}
    *   has ever linked to.
    */
    @SuppressWarnings("unchecked")
    public List<Disease> getMyDiseases() 
    {
        int id = this.getContributorId();
        
        assert id > 0;
        
        if ( log.isDebugEnabled() )
            log.debug("looking up all Diseases for Contributor=" + id );
        
        List<Disease> list;
        list = (List<Disease>) getEntityManager()
             .getQuery( Q + "GET_ALL_CONTRIBUTED_DISEASES_BY_ID" )
         .setParameter("id", id )
        .list();             
        
        if ( list == null )
            return emptyList();
             
        return list;
    }

    
    /*  getMyPerturbations  *//*********************************
    *
    *   Returns all {@link Perturbation}s this {@link Contributor}
    *   has ever linked to.
    */
    @SuppressWarnings("unchecked")
    public List<Perturbation> getMyPerturbations() 
    {
        int id = this.getContributorId();
        
        assert id > 0;
        
        if ( log.isDebugEnabled() )
            log.debug("looking up all Perturbations for Contributor=" + id );
        
        List<Perturbation> list;
        list = (List<Perturbation>) getEntityManager()
             .getQuery( Q + "GET_ALL_CONTRIBUTED_PERTURBATIONS_BY_ID" )
             .setParameter("id", id )
             .list();
        
        if ( list == null )
            return emptyList();
             
        return list;
    }
    
    
    /*  getMyRecentGlycanSequences  *//************************************
    *
    *   Returns the given number of {@link GlycanSequence}s that have been 
    *   contributed by this {@link Contributor}; most recent first.
    */
    @SuppressWarnings("unchecked")
    public List<GlycanSequence> getMyRecentGlycanSequences( int count ) 
    {
        return getMyContributionsOf( GlycanSequence.class, count );
    }
    
    
    /*  getMyEvidence  *//*******************************************
    *
    *   Returns all {@link Evidence} this {@link Contributor}
    *   has ever contributed.
    */
    @SuppressWarnings("unchecked")
    public List<Evidence> getMyEvidence() 
    {
        int id = this.getContributorId();
        
        assert id > 0;
        
        if ( log.isDebugEnabled() )
            log.debug("looking up all Evidence for Contributor=" + id );
        
        List<Evidence> list;
        list = (List<Evidence>) getEntityManager()
             .getQuery( Q + "GET_ALL_CONTRIBUTED_EVIDENCE_BY_ID" )
             .setParameter("id", id )
             .list();
        
        if ( list == null )
            return emptyList();
             
        return list;
    }

    
    /*  getMyExperiments  *//****************************************
    *
    *   Returns all {@link Experiment}s this {@link Contributor}
    *   has ever contributed.
    */
    @SuppressWarnings("unchecked")
    public List<Experiment> getMyExperiments() 
    {
        int id = this.getContributorId();
        
        assert id > 0;
        
        if ( log.isDebugEnabled() )
            log.debug("looking up all Experiments for Contributor=" + id );
        
        List<Experiment> list;
        list = (List<Experiment>) getEntityManager()
             .getQuery( Q + "GET_ALL_CONTRIBUTED_EXPERIMENTS_BY_ID" )
             .setParameter("id", id )
             .list();
        
        if ( list == null )
            return emptyList();
             
        return list;
    }
    

    /*  getMyRecentContributions  *//********************************
    *
    *   Returns the requested number of {@link Contributed} objects 
    *   this {@link Contributor} has contributed, ordered by most to
    *   least recent, or an empty list if this contributor has not
    *   contributed anything.
    *
    *   TODO: this method has issues due to Hibernate's brain-dead
    *   polymorphic queries not being able to accept setMaxResults
    *   so it tries to load and sort entire tables....
    */
    @SuppressWarnings("unchecked")
    public List<Contributed> getMyRecentContributions( int max_count )
    {
        int id = this.getContributorId();
        if ( id <= 0 ) 
            throw new UnsupportedOperationException(
                "Cannot execute method, contributor_id is <= 0");
        
        log.debug("looking up all Contributed objects for contributor_id=" + id );
        List<Contributed> changes = (List<Contributed>) getEntityManager()
                                    .getQuery( Q + "GET_RECENT_BY_CONTRIBUTOR")
                                    .setParameter("contributor_id", id )
                                    .setMaxResults( max_count )
                                    .list();
        if ( changes == null )
        {
            log.debug("(no rows received)");
            return Collections.emptyList();
        }
            
        return changes;
    }
    
    
    /*  getMyUniqueBiologicalContexts  *//***************************
    *
    *   Returns all unique {@link BiologicalContext}s this 
    *   {@link Contributor} has ever contributed.
    */
    @SuppressWarnings("unchecked")
    public Set<BiologicalContext> getMyUniqueBiologicalContexts() 
    {
        List<BiologicalContext> bclist = getMyBiologicalContexts();
        if ( bclist.size() == 0 ) 
            return emptySet();

        //  TODO
        
        Set<BiologicalContext> bcset 
            = new HashSet<BiologicalContext>( bclist.size() );
        
        bcset.addAll( bclist );
            
        return bcset;
    }
    
    
    /** Returns {@link getContributorName()}. */
    public String getName() {  return getContributorName();  }
    
    
    /*  hashCode  *//************************************************
    *
    *   Returns a hashCode based on {@link getContributorId() contributorId} 
    *   and {@link getContributorName() contributorName}.
    */
    public int hashCode()
    {
        return ( contributorId + ':' + contributorName ).hashCode();  
    }
    

    /*  equals  *//**************************************************
    *
    *   Compares equality by value of {@link getContributorId() contributorId} 
    *   and {@link getContributorName() contributorName}.
    */
    public boolean equals( Object x )
    {
        if ( this == x )
            return true;
        
        if ( (x == null) || ! (x instanceof Contributor) )
            return false;
        
        // objects are the same class
        Contributor c = (Contributor) x;

        return this.getContributorId() == c.getContributorId()
            && this.getContributorName() == c.getContributorName();
    }



} // end class
