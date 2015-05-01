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
*   Last commit: $Rev: 1574 $ by $Author: glycoslave $ on $Date:: 2009-07-24 #$  
*/

package org.eurocarbdb.dataaccess.core.seq;


//  stdlib imports
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.ArrayList;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.Vertex;
import org.eurocarbdb.util.graph.DepthFirstGraphVisitor;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.GlycosidicLinkage;

import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.seq.GlycanResidue;

//  static imports
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.dataaccess.core.seq.SubstructureQuery.Option.Distinct;

/** 
*<p>
*   Implements translation of a search {@link Graph} of {@link Linkage}s
*   and {@link Residue}s (encapsulated as a {@link SubstructureQuery} object) 
*   to an SQL/HQL query string, using the Visitor pattern. 
*</p>
*<p>
*   This class is not used directly, it is normally called from 
*   {@link SubstructureQuery}. That said, the generation process consists of:
*<ol>
*   <li>creation of generator object (this class) from a {@link SubstructureQuery}</li>
*   <li>traversal of the search sugar graph (see {@link DepthFirstGraphVisitor})</li>
*   <li>translation of {@link Residue}s and {@link Linkage}s into SQL WHERE
*       predicates and table joins respectively, plus addition of other predicates
*       as given by the structure query, and query options</li>
*   <li>generation of a <a href="http://docs.jboss.org/hibernate/stable/core/reference/en/html/queryhql.html">HQL</a>
*       query string by concatenation</li>
*</ol>   
*</p>
*
*   @see SubstructureQuery
*   @see SubstructureQueryResult
*   @see SubstructureQuery.Option
*   @see <a href="http://docs.jboss.org/hibernate/stable/core/reference/en/html/">hibernate docs</a>
*   @author mjh
*/
public interface SubstructureQueryGenerator
{
    static final String GLYCAN_RESIDUE_TABLE = "seq.glycan_residue";
    
    static final String GLYCAN_RESIDUE_ID = "glycan_residue_id";
    
    static final String GLYCAN_RESIDUE_PARENT_ID = "parent_id";
    
    static final String GLYCAN_SEQUENCE_TABLE = "core.glycan_sequence";
    
    static final String GLYCAN_SEQUENCE_ID = "glycan_sequence_id";
    
    /* 
    -- most common linkages
    SELECT parent.residue_name AS parent_residue_name
        , child.residue_name AS child_residue_name
        , child.anomer AS child_anomer
        , child.linkage_child
        , child.linkage_parent
        , count(parent.glycan_residue_id) AS count
    FROM seq.glycan_residue parent
        , seq.glycan_residue child
    WHERE parent.glycan_residue_id = child.parent_id
    GROUP BY parent.residue_name
        , child.residue_name
        , child.anomer
        , child.linkage_parent
        , child.linkage_child
    order by count desc

    */

    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
    *   Returns the substructure {@link Graph} for which we're 
    *   generating a query. 
    */
    public Graph<Linkage,Residue> getSearchGraph()
    ;
    
    
    /** 
    *   Returns an SQL query string representing the search sub-structure. 
    *   given by {@link #getSearchGraph}.
    */
    public String getQueryString()
    ;
    
    
    /** Adds a {@link String} WHERE predicate */
    public void addPredicate( String predicate )
    ;
    
    
    /** 
    *   Returns the {@link Set} of query options supported by this 
    *   query engine. 
    *
    *   @see EnumSet
    */
    public Set<SubstructureQuery.Option> getSupportedOptions()
    ;
 
    
    /** 
    *   Returns the String table alias for the table representing
    *   the given Residue. Creates new aliases as needed. 
    */        
    public String getTableAliasFor( Residue r )
    ;
    
} // end class

