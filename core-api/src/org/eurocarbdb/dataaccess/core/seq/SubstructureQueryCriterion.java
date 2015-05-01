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
*   Last commit: $Rev: 1552 $ by $Author: glycoslave $ on $Date:: 2009-07-20 #$  
*/

package org.eurocarbdb.dataaccess.core.seq;


//  stdlib imports

//  3rd party imports
import org.apache.log4j.Logger;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.engine.TypedValue;

//  eurocarb imports
import org.eurocarbdb.dataaccess.core.GlycanSequence;

//  static imports
import static org.eurocarbdb.dataaccess.HibernateEntityManager.translateHql2Sql;


/**
*<p>
*   Utility class to encapsulate this substructure query as a Hibernate 
*   {@link Criterion}, allowing it to be used in arbitrary {@link Criteria} 
*   queries. The criterion returned corresponds approximately to the following 
*   HQL WHERE clause expression:
*<pre>
*       [property_name] in ( 
*       select 
*           gs.glycanSequenceID 
*       from
*           GlycanSequence gs
*       where
*           [substructure is found] 
*       )
*</pre>
*   The value of {@code property_name} must be given at construction time or 
*   via {@link #setPropertyName}; the substructure sub-query expression is 
*   derived from the {@link SubstructureQuery} given at construction.
*</p>
*
*<h2>Usage</h2>
*<p>
*   Instances of this class can be created by from an existing {@link SubstructureQuery}
*   via {@link SubstructureQuery#getQueryCriterion()}, or constructed directly.
*</p>
*
*   @see SubstructureQuery
*   @see SubstructureQuery#getQueryCriterion()
*   @see Criteria
*
*   @author mjh
*/
public class SubstructureQueryCriterion implements Criterion
{ 
    final SubstructureQuery query;
    
    /** */
    String propertyName;
    
    
    /** 
    *   Creates a {@link SubstructureQueryCriterion} for the passed 
    *   {@link SubstructureQuery} with a default 
    *   {@link #setPropertyName query property name}.
    *
    *   @see #setPropertyName
    */
    public SubstructureQueryCriterion( SubstructureQuery q )
    {
        this.query = q;
        this.propertyName = CriteriaSpecification.ROOT_ALIAS // "this"
                          + "_.glycan_sequence_id";
    }
    

    /** 
    *   Creates a {@link SubstructureQueryCriterion} for the passed 
    *   {@link SubstructureQuery} with the given query property name.
    *
    *   @see #setPropertyName
    */
    public SubstructureQueryCriterion( SubstructureQuery q, String propertyName )
    {
        this.query = q;
        this.propertyName = propertyName;
    }

    
    /** 
    *   Returns the {@link SubstructureQuery} this {@link Criterion} 
    *   represents.
    */
    public SubstructureQuery getSubstructureQuery()
    {
        return query;   
    }
    
    
    /** 
    *   Sets the name of the alias/property that will be used in
    *   the "in sub-query" expression formed from the {@link SubstructureQuery}.
    *   This property name needs to correspond to a {@link GlycanSequence} id
    *   property/alias.
    */
    public SubstructureQueryCriterion setPropertyName( String name )
    {
        propertyName = name;
        return this;
    }
    
    
    public String toSqlString( Criteria c, CriteriaQuery q )
    {
        String initial_hql = query.getQueryString();
        
        //  default query selects whole GlycanSequence objects, which
        //  aren't needed if the Criterion we're returning is just 
        //  going to be used in a subselect.
        int i = initial_hql.indexOf("from ");
        assert i != -1 : initial_hql;
        
        //  so, narrow the select clause:
        // String hql = "select gs.id " + initial_hql.substring( i );
        String hql = "select gs.glycan_sequence_id " + initial_hql.substring( i );
        
        //  translate to SQL
        String subquery_expr_sql = hql; //translateHql2Sql( hql );

        String sql = propertyName 
                   + " in ("
                   + subquery_expr_sql
                   + ")";
                   
        return sql;
    }
    
    
    public String toSqlString()
    {
        return toSqlString( null, null );
    }
    
    
    /** Irrelevant; not used. */
    public TypedValue[] getTypedValues( Criteria c, CriteriaQuery q )
    {
        return new TypedValue[0];
    }
        
} // end class SubstructureQueryCriterion


