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
// Generated Apr 16, 2007 10:40:27 AM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.core;

//  stdlib imports
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

//  eurocarb imports
import org.eurocarbdb.dataaccess.core.CompositionId;
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

// 3rd party imports
import org.hibernate.Query;
import org.apache.log4j.Logger;



/*  class Composition  *//**********************************************
*
*
*/ 
public class Composition extends BasicEurocarbObject implements Serializable 
{

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    protected static final Logger log = Logger.getLogger( Composition.class.getName() );

    private static final String DELETE_BY_GLYCANSEQUENCE_ID     = "org.eurocarbdb.dataaccess.core.Composition.DELETE_BY_GLYCANSEQUENCE_ID";
    private static final String QUERY_COMPONENTS                 = "org.eurocarbdb.dataaccess.core.Composition.QUERY_COMPONENTS";
    private static final String QUERY_BY_COMPOSITION_EQUALS    = "org.eurocarbdb.dataaccess.core.Composition.QUERY_BY_COMPOSITION_EQUALS";
    private static final String QUERY_BY_COMPOSITION_GREATER    = "org.eurocarbdb.dataaccess.core.Composition.QUERY_BY_COMPOSITION_GREATER";
    private static final String QUERY_BY_COMPOSITION_LESS        = "org.eurocarbdb.dataaccess.core.Composition.QUERY_BY_COMPOSITION_LESS";


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private CompositionId m_compId;      
    private int m_nOccurances;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Composition() {}

    
    /** full constructor */
    public Composition(CompositionId compId, int nOccurances)
    {
        m_compId = compId;
        m_nOccurances = nOccurances;
    }
   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    public static void deleteByGlycanSequenceId(int nGlycanSequenceId)
    {
        Query q = getEntityManager().getQuery(DELETE_BY_GLYCANSEQUENCE_ID);
        q.setParameter("glycan_sequence_id", nGlycanSequenceId);
        
        q.executeUpdate();
    }
    
    
    @SuppressWarnings("unchecked")
    public static List<String> getComponents()
    {
        Query q = getEntityManager().getQuery(QUERY_COMPONENTS);
        return q.list();
    }
    
    
    //  mjh says to ss: List of what? pls fix
    @SuppressWarnings("unchecked")
    public static List search(Vector vecCriteria)
    {
        Vector vecCriterion;
        String strOperator;
        Query q;
        
        List lResult = null;
        List lTemp;
    
        for (int i = 0; i < vecCriteria.size(); i++)
        {            
            vecCriterion = (Vector)(vecCriteria.get(i));
            strOperator = (String)(vecCriterion.get(1));

            if (strOperator.equals("="))
            {
                q = getEntityManager().getQuery(QUERY_BY_COMPOSITION_EQUALS);
            }
            else if (strOperator.equals("<"))
            {
                q = getEntityManager().getQuery(QUERY_BY_COMPOSITION_LESS);
            }
            else
            {
                q = getEntityManager().getQuery(QUERY_BY_COMPOSITION_GREATER);    
            }        
        
            q.setParameter("component", (String)(vecCriterion.get(0)));
            q.setParameter("occurances", (Integer)(vecCriterion.get(2)));

            lTemp = q.list();
            
            log.warn("searching for " + vecCriterion.get(0) + " " + strOperator + " " + vecCriterion.get(2) + " => hits " + lTemp.size());
            
            if (lResult == null)            
            {
                lResult = lTemp;
            }
            else
            {
                lResult.retainAll(lTemp);
            }
        }
        
        return lResult;
        
    }


    /*  getId  *//******************************** 
    *
    */ 
    public CompositionId getCompositionId() 
    {
        return this.m_compId;
    }
    
    
    /*  setId  *//******************************** 
    *
    */
    public void setCompositionId(CompositionId compId)
    {
        this.m_compId = compId;
    }

    /*  getOccurances  *//******************************** 
    *
    */ 
    public int getOccurances() 
    {
        return this.m_nOccurances;
    }
    
    
    /*  setOccurances  *//******************************** 
    *
    */
    public void setOccurances( int nOccurances ) 
    {
        this.m_nOccurances = nOccurances;
    }

} // end class
