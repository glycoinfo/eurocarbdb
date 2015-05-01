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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
// Generated Apr 18, 2007 5:02:27 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.core;

//  stdlib imports
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import java.io.Serializable;

import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Technique  *//**********************************************
*
*
*/ 
public class Technique extends BasicEurocarbObject implements Serializable 
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( Technique.class.getName() );


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int techniqueId;
      
    private String techniqueAbbrev;
      
    private String techniqueName;
      
    private Set<Evidence> evidences = new HashSet<Evidence>(0);
      
    private Set<ExperimentStep> experimentSteps = new HashSet<ExperimentStep>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Technique() {}

    /** Minimal constructor */
    public Technique( String techniqueAbbrev, String techniqueName ) 
    {
        this.techniqueAbbrev = techniqueAbbrev;
        this.techniqueName = techniqueName;
    }
    
    /** full constructor */
    public Technique( String techniqueAbbrev, 
                      String techniqueName, 
                      Set<Evidence> evidences, 
                      Set<ExperimentStep> experimentSteps ) 
    {
        this.techniqueAbbrev = techniqueAbbrev;
        this.techniqueName = techniqueName;
        this.evidences = evidences;
        this.experimentSteps = experimentSteps;
    }
    

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~//

    /*  getAllTechniques  *//****************************************
    *
    *   Returns the list of all techniques defined in the data store.
    */
    @SuppressWarnings("unchecked")
    public static List<Technique> getAllTechniques()
    {
        log.debug( "looking up all techniques" );
            
        
        List result = getEntityManager()
                     .getQuery( "org.eurocarbdb.dataaccess.core.Technique.ALL_TECHNIQUES" )
                     .list();
                                            
        return (List<Technique>) result;
    }

    public static Technique lookupAbbrev(String abbrev) {
        Object t = getEntityManager()
                  .getQuery( "org.eurocarbdb.dataaccess.core.Technique.BY_ABBREV" )
                  .setParameter("abbrev", abbrev )
                  .uniqueResult();

        assert t instanceof Technique;
        
        return (Technique) t;
    }
        
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    public boolean isMS() {
    return this.techniqueAbbrev.equals("ms");
    }

    public boolean isHPLC() {
    return this.techniqueAbbrev.equals("hplc");
    }

    public boolean isNMR() {
    return this.techniqueAbbrev.equals("nmr");
    } 

    /*  getTechniqueId  *//****************************************** 
    *
    */ 
    public int getTechniqueId() 
    {
        return this.techniqueId;
    }
    
    
    /*  setTechniqueId  *//****************************************** 
    *
    */
    public void setTechniqueId( int techniqueId ) 
    {
        this.techniqueId = techniqueId;
    }
    

    /*  getTechniqueAbbrev  *//************************************** 
    *   
    *   Gets the abbreviated (common) name of this technique. 
    */ 
    public String getTechniqueAbbrev() 
    {
        return this.techniqueAbbrev;
    }
    
    
    /*  setTechniqueAbbrev  *//************************************** 
    *
    *   Sets the abbreviated (common) name of this technique. 
    */
    public void setTechniqueAbbrev( String techniqueAbbrev ) 
    {
        this.techniqueAbbrev = techniqueAbbrev;
    }
    

    /*  getTechniqueName  *//**************************************** 
    *
    */ 
    public String getTechniqueName() 
    {
        return this.techniqueName;
    }
    
    
    /*  setTechniqueName  *//****************************************
    *
    */
    public void setTechniqueName( String techniqueName ) 
    {
        this.techniqueName = techniqueName;
    }
    

    /*  getEvidences  *//******************************************** 
    *
    *   Returns all {@link Evidence}s that employ this Technique.
    */ 
    public Set<Evidence> getEvidences() 
    {
        return this.evidences;
    }
    
    
    /*  setEvidences  *//********************************************
    *
    *   Sets the set of all {@link Evidence}s that employ this Technique.
    */
    public void setEvidences( Set<Evidence> evidences ) 
    {
        this.evidences = evidences;
    }
    

    /*  getExperimentSteps  *//**************************************
    *
    *   Returns all {@link ExperimentStep}s that employ this Technique.
    */ 
    public Set<ExperimentStep> getExperimentSteps() 
    {
        return this.experimentSteps;
    }
    
    
    /*  setExperimentSteps  *//************************************** 
    *
    *   Sets the set of all {@link ExperimentStep}s that employ this Technique.
    */
    public void setExperimentSteps( Set<ExperimentStep> experimentSteps ) 
    {
        this.experimentSteps = experimentSteps;
    }
    
       

} // end class
