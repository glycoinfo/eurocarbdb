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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/

package org.eurocarbdb.dataaccess.core.seq;


//  stdlib imports
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.EnumSet;
import java.util.ArrayList;

//  3rd party imports
import org.apache.log4j.Logger;
import com.google.common.collect.Multimap;
import com.google.common.collect.ArrayListMultimap;

//  eurocarb imports
import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.Vertex;
import org.eurocarbdb.util.graph.DepthFirstGraphVisitor;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.GlycosidicLinkage;

import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.seq.GlycanResidue;

//  static imports
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.sugar.Basetypes.getBasetypeId;
import static org.eurocarbdb.dataaccess.core.seq.SubstructureQuery.Option.*;


/** 
*   Implementation of {@link SubstructureQueryGenerator} that favours
*   putting all predicates into joins.
*
*   @see SubstructureQuery
*   @see SubstructureQueryResult
*   @see SubstructureQuery.Option
*   @see <a href="http://docs.jboss.org/hibernate/stable/core/reference/en/html/">hibernate docs</a>
*   @author mjh
*/
public class SubstructureQueryGeneratorImpl2 
extends DepthFirstGraphVisitor<Linkage,Residue>
implements SubstructureQueryGenerator
{
    /** logging handle */
    static final Logger log = SubstructureQuery.log;
    
    /** tuneable query optimisation parameter: this is the min number of 
    *   residues in the search graph at which we add an optimisation
    *   predicate to exclude searching graphs smaller than this number.
    *   currently, this does not appear to have a huge effect on performance
    *   but since it is very cheap to generate & include at both the code & sql
    *   level, the default is to always add it. */
    private static final int MIN_DESCENDANTS_B4_OPTIMISATION_APPLIES = 0;
    
    //~~~~~~~~~~~~~~~~~~~~~~~ PROPERTIES ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** counter for table aliases to track which residue is which, with
    *   the first (root) residue in the depth-first traversal being 1,
    *   and incrementing for each subsequent Residue encountered. */
    private int counter = 1;

    /** List of String aliases to table names, 1 table per residue. */
    private List<String> ids;    
    
    /** list of table joins, declaring table aliases */
    private List<String> joins;
    
    /** multimap of table join predicates: key=table alias ({@link #ids})
    *   and value is a {@link List} of join predicates for that table. */
    ArrayListMultimap<String,String> joinPredicates;
    
    // /** list of WHERE predicates for residues */
    // List<String> residuePredicates;
    
    // /** list of WHERE predicates for linkages */
    // List<String> linkagePredicates;
    
    /** list of WHERE predicates for other stuff */
    List<String> otherPredicates;
    
    /** the source of the substruct query; specifies various options 
    *   & meta-data that may add additional predicates to query */
    private final SubstructureQuery query;
    
    /** the graph of the search substructure */
    private final Graph<Linkage,Residue> graph;
    
    /** maps a residue in the search graph to its table alias */
    private Map<Residue,String> tableAliasMap;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public SubstructureQueryGeneratorImpl2( SubstructureQuery q )
    {
        this.query = q;
        this.graph = q.getGraph();
        
        //  SELECT clause: one id for each *vertex* in graph
        this.ids = new ArrayList<String>( graph.countVertices() );
        this.tableAliasMap = new HashMap<Residue,String>( graph.countVertices() );

        //  FROM clause: one join for each *edge* in graph
        this.joins = new ArrayList<String>( graph.countEdges() );
        
        //  WHERE clause: any number of where predicates
        //  where predicates are broken into different types because
        //  order of predicate inclusion DOES have a large effect on 
        //  query performance. 
        this.joinPredicates  = ArrayListMultimap.create( joins.size(), 4 );
        this.otherPredicates = new ArrayList<String>(2);
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
    *   Returns the substructure {@link Graph} for which we're 
    *   generating a query. 
    */
    public Graph<Linkage,Residue> getSearchGraph()
    {
        return graph;
    }
    
    
    /** Returns HQL query string representing given search sub-structure. */
    public String getQueryString()
    {       
        visit( graph );  
        
        processOptions();
        
        //  assemble all WHERE predicates 
        //  note: order DOES have a significant influence on query performance.

        int size = otherPredicates.size();

        String glyseq_join_predicate 
            = ids.get(0) 
            + "." 
            + GLYCAN_SEQUENCE_ID
            + " = gs."
            + GLYCAN_SEQUENCE_ID
            ;
        // joinPredicates.put( ids.get(0), ids.get(0) + ".glycanSequence = gs" );
        joinPredicates.put( ids.get(0), glyseq_join_predicate );
        
        List<String> from_clause = new ArrayList<String>( joins.size() );
        for ( int i = 0; i < ids.size(); i++ )
        {
            List<String> join_predicates = this.joinPredicates.get( ids.get(i) );
            String join = joins.get(i)
                        + "\n    on "
                        + join( "\n    and ", join_predicates);
            from_clause.add( join );
        }
        
            
        //  assemble final query
        StringBuilder sql = new StringBuilder();
        
        //  SELECT clause
        sql.append(
            query.getOption( Distinct ) 
                ?   "select distinct gs.* "
                :   "select gs.* " 
        );
        
        //  FROM clause
        sql.append(
            "\nfrom \n    "
            + GLYCAN_SEQUENCE_TABLE 
            + " as gs "
            + "\ninner join\n    "
            + join("\ninner join\n    ", from_clause )
        );
         
        //  WHERE clause
        if ( otherPredicates.size() > 0 )
        {
            sql.append(
                "\nwhere "
                + join("\nand ", otherPredicates )
            );
        }
        
        if ( log.isDebugEnabled() )
            log.debug("generated substructure query:\n" + sql );
            
        return sql.toString();
    }
    

    /** 
    *   Returns the String table alias for the table representing
    *   the given Residue. Creates new aliases as needed. 
    */        
    public String getTableAliasFor( Residue r )
    {
        if ( tableAliasMap.containsKey( r ) )
            return tableAliasMap.get( r );
        
        String alias = "r" + counter;
        
        counter++;
        ids.add( alias );
        tableAliasMap.put( r, alias );
        // joins.add( "GlycanResidue " + alias );
        joins.add( GLYCAN_RESIDUE_TABLE + " as " + alias );
        
        return alias;
    }

    
    /**
    *   {@inheritDoc}
    *
    *   Supports: Distinct
    *           , Ignore_Linkages  
    *           , Ignore_Monosac_Substituents
    *           , Must_Include_Reducing_Terminus
    *           , Must_Include_All_Non_Reducing_Terminii
    */
    public Set<SubstructureQuery.Option> getSupportedOptions()
    {
        return EnumSet.of( Distinct
                         , Ignore_Linkages  
                         , Ignore_Monosac_Substituents
                         , Must_Include_Reducing_Terminus
                         , Must_Include_All_Non_Reducing_Terminii
                         );    
    }
    
    
    @Override
    public void accept( Graph<Linkage,Residue> g )
    {
        //  ensure no sub-graphs for the moment
        if ( this.graph != g )
            throw new UnsupportedOperationException(
                "Substructure queries with sub-graphs are not yet supported");
            
        Residue root = g.getRootVertex().getValue();
        int min_size = g.countVertices() - 1;
        addSizePredicate( root, min_size );
            
        super.accept( g );
    }
    

    @Override
    public void accept( Edge<Linkage,Residue> edge )
    {
        if ( counter + 1 >= SubstructureQuery.MAX_SUBSTRUCTURE_RESIDUES )
            return;

        Linkage link   = edge.getValue();
        Residue parent = edge.getParent().getValue();
        Residue child  = edge.getChild().getValue();
        
        addJoinPredicate( parent, child );
        
        if ( ! query.getOption( Ignore_Linkages ) )
            addLinkagePredicates( parent, child, link );
        
        super.accept( edge );
    }
    

    @Override
    public void accept( Vertex<Linkage,Residue> vertex )
    {
        if ( counter + 1 >= SubstructureQuery.MAX_SUBSTRUCTURE_RESIDUES )
            return;
        
        Residue r = vertex.getValue();

        addResiduePredicate( r );
        
        super.accept( vertex );
    }
    
    
    /** Adds a {@link String} WHERE predicate */
    public void addPredicate( String predicate )
    {
        otherPredicates.add( predicate );   
    }
    
    
    /** 
    *   Add a table join predicate for the link between the given 
    *   parent and child {@link Residue}s. 
    */
    public void addJoinPredicate( Residue parent, Residue child )
    {
        String parent_id = getTableAliasFor( parent );
        String child_id  = getTableAliasFor( child );
        
        //  if root, should contain at least the size predicate, 
        //  else should already be added by this method as a child.
        assert joinPredicates.containsKey( parent_id );
        
        // String join_fragment = parent_id + " = " + child_id + ".parent";
        String join_fragment 
            = parent_id 
            + "."
            + GLYCAN_RESIDUE_ID
            + " = " 
            + child_id 
            + "."
            + GLYCAN_RESIDUE_PARENT_ID
            ;
            
        joinPredicates.put( child_id, join_fragment );
    }
    
    
    /**
    *   Add WHERE predicates for the given {@link Linkage} between the 
    *   given parent/child {@link Residue}s.
    *
    *   @see Option#Ignore_Linkages
    */
    public void addLinkagePredicates( Residue parent, Residue child, Linkage link )
    {
        // addLinkagePredicate( child, "linkage.parentTerminus", link.getParentTerminus() );
        // addLinkagePredicate( child, "linkage.childTerminus", link.getChildTerminus() );
        addLinkagePredicate( child, "linkage_parent", link.getParentTerminus() );
        addLinkagePredicate( child, "linkage_child", link.getChildTerminus() );
        
        // if ( child instanceof Monosaccharide )
        // {
        //     Anomer a = ((Monosaccharide) child).getAnomer();
        //     if ( a == null && (link instanceof GlycosidicLinkage) )
        //         a = ((GlycosidicLinkage) link).getAnomer();
            
        //     if ( a != null )
        //         addAnomerPredicate( child, "anomer", a );
        // }
    }
    
    
    /** Add predicate(s) that specify the identity of the given {@link Residue}. */
    public void addResiduePredicate( Residue r )
    {
        Monosaccharide m = (r instanceof Monosaccharide) 
                         ? (Monosaccharide) r 
                         : null;
                         
        String alias = getTableAliasFor( r ); 
        String predicate;
        
        if ( m != null && query.getOption( Ignore_Monosac_Substituents ) )
        {
            predicate = alias
                      + ".basetype_id = "
                      + getBasetypeId( m.getBasetype() )
                      ;
            
            if ( log.isDebugEnabled() )
                log.debug("adding basetype stereochemistry predicate: " + predicate );
        }
        else
        {
            predicate = alias
                      + ".residue_name = '"
                      + r.getName()
                      + "'";
            
            if ( log.isDebugEnabled() )
                log.debug("adding residue identity predicate: " + predicate );
        }
        
        joinPredicates.put( alias, predicate );

        //  add anomer predicate, if applicable.
        if ( m != null )
        {
            Anomer a = m.getAnomer();
            if ( a != null )
                addAnomerPredicate( r, "anomer", a );
        }
        
        addSiblingDisambiguationPredicateIfNeeded( r );  
    }
    

    /**    
    *   if given residue has 2 or more children, then there needs to 
    *   be a predicate added to ensure that we don't match the
    *   same child residue twice. this will be a factorial-style
    *   expansion of negated equality of sibling child residues of 
    *   the given residue.
    */
    private void addSiblingDisambiguationPredicateIfNeeded( Residue r )
    {
        List<Edge<Linkage,Residue>> children 
            = graph.getVertex( r ).getOutgoingEdges();
        
        if ( children.size() > 1 )
        {
            for ( int i = 0; i < children.size(); i++ )
            {
                for ( int j = i + 1; j < children.size(); j++ )
                {
                    Residue r1 = children.get( i ).getChild().getValue();
                    Residue r2 = children.get( j ).getChild().getValue();
                    assert r1 != r2;
                    
                    String disambiguation_predicate 
                        = getTableAliasFor( r1 )
                        + "."
                        + GLYCAN_RESIDUE_ID
                        + " != "
                        + getTableAliasFor( r2 )
                        + "."
                        + GLYCAN_RESIDUE_ID
                        ;
                    
                    log.debug(
                        "adding predicate to disambiguate sibling residues: "
                        + disambiguation_predicate
                    );
                    
                    //  trying to add to join conditions doesn't work due to
                    //  traversal order not being strictly followed, so we
                    //  add to WHERE clause instead.
                    addPredicate( disambiguation_predicate );
                }
            }
        }
    }
    
    
    /** 
    *   Adds a query optimisation predicate which ensures that only 
    *   candidate structures with at least the given number of descendants
    *   descending from the given {@link Residue} are searched.
    */
    protected void addSizePredicate( Residue r, int min_children )
    {
        //  size predicates do cut the time down a decent amount for
        //  this generator implementation as it narrows the join condition
        //  considerably for large search structures.
        
        // if ( min_children <= MIN_DESCENDANTS_B4_OPTIMISATION_APPLIES ) 
        //     return;
        
        String table = getTableAliasFor( r );
        
        //  makes use of nested set left/right values --
        //  number of descendants of any node in a tree is
        //  equal to ((right - left - 1) / 2), so (right - left)
        //  should be equal to or greater than 2x descendants + 1    
        int limit = (min_children * 2) + 1; 
        
        String size_predicate 
            = table
            // + ".rightIndex"
            + ".right_index"
            + " - "
            + table
            // + ".leftIndex"
            + ".left_index"
            + " >= "
            + limit
            ;
          
        if ( log.isDebugEnabled() )
        {
            log.debug(
                "adding min descendants predicate: "
                + size_predicate
            );
        }
        
        // addPredicate( size_predicate );
        joinPredicates.put( table, size_predicate );
    }
    
    
    /** 
    *   Add a predicate for the given {@link Residue}, using the given linkage
    *   column, to match the given linkage position (ie: r.column == position). 
    */
    protected void addLinkagePredicate( Residue r, String column, int position )
    {
        if ( position == 1 )
        {
            log.debug("*** NOTE: glycanbuilder gives default position == 1"
                + " so NOT adding a predicate for it ***");
            return;
        }
            
        if ( position > 0 )
        {
            String alias = getTableAliasFor( r ); 
            String predicate 
                = alias 
                + '.'
                + column
                + " = "
                + position ;   
            
            // if ( log.isDebugEnabled() )
            //     log.debug("adding WHERE clause linkage predicate: " + predicate );

            // linkagePredicates.add( predicate ); 
            joinPredicates.put( alias, predicate ); 
        }
    }
    
    
    /** 
    *   Add a predicate for the given {@link Anomer} of the given {@link Residue}
    *   using the given column (ie: r.column == a). 
    */
    protected void addAnomerPredicate( Residue r, String column, Anomer a )
    {
        // log.warn("discarding anomer predicate cause DB column empty for the moment...");
        
        // if ( false /* fixme */ && a != null && a.isDefinite() )
        if ( a != null && a.isDefinite() )
        {
            String alias = getTableAliasFor( r ); 
            String predicate 
                = alias
                + '.'
                + column
                + " = '"
                + a.toChar()
                + "'" ;   
            
            // if ( log.isDebugEnabled() )
            //     log.debug("adding WHERE clause anomer predicate: " + predicate );

            // linkagePredicates.add( predicate );
            joinPredicates.put( alias, predicate );
        }
    }
    
    
    /** 
    *   Modifies the query being generated by the {@link Set} of {@link Option}s 
    *   contained in the current {@link SubstructureQuery}.
    */
    protected void processOptions()
    {
        if ( query.options.size() == 0 )
            return;
        
        for ( SubstructureQuery.Option option : query.options )
        {
            log.debug("processing option " + option );
            option.modifyQuery( this, query.options );
        }   
    }
    
} // end class

