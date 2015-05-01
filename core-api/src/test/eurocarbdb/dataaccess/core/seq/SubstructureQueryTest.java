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

package test.eurocarbdb.dataaccess.core.seq;

//  stdlib imports
import java.util.List;

//  3rd party imports
import org.testng.annotations.*;

import org.hibernate.Criteria;
// import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.MatchMode;

//  eurocarb imports
import org.eurocarbdb.util.graph.Graph;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.GlycosidicLinkage;

import org.eurocarbdb.sugar.impl.SimpleMonosaccharide;

import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;

import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.seq.SubstructureQuery;
import org.eurocarbdb.dataaccess.core.seq.SubstructureQueryResult;
import org.eurocarbdb.dataaccess.core.seq.SubstructureQuery.Option;
import org.eurocarbdb.dataaccess.core.seq.SubstructureQueryCriterion;

import test.eurocarbdb.dataaccess.CoreApplicationTest;

//  static imports
import static java.lang.System.out;


/**
*   Tests {@link SubstructureQuery}.
*
*   @author mjh
*   @version $Rev: 1552 $
*/
@Test
(   
    groups={"sugar.search.substructure"} 
    //, dependsOnGroups={"ecdb.db.populated"}
    , sequential=true
)
public class SubstructureQueryTest extends CoreApplicationTest
{
    
    /*~~~~~~~~~~~~~~~~~~~~~~~~~~ TESTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
 
    /**
    *   search struct s1:
    *<pre>
    *               Man1
    *              /    \
    *           a1-3    a1-6
    *          /          \
    *        Man2         Man3
    *</pre>
    */
    @Test
    public void substructureQuery1() throws Exception
    {
        System.out.println("--- query 1 ---");
        setup();  
        
        Sugar s1 = new Sugar();
        
        Monosaccharide 
            r1 = monosac("Man"), 
            r2 = monosac("Man"), 
            r3 = monosac("Man");
         
        s1.addRootResidue( r1 );
        s1.addResidue( r1, linkage('a', 3, 1), r2 );
        s1.addResidue( r1, linkage('a', 6, 1), r3 );
        
        assert s1.countResidues() == 3;
        
        SubstructureQuery q1 = new SubstructureQuery( s1 );
        
        q1.execute();
        
        reportResults( q1 );
        
        teardown();
    }

    
    /**
    *   search struct:
    *<pre>
    *               Man1
    *              /    \
    *           a1-3    a1-6
    *          /          \
    *        Man2         Man3
    *                      |
    *                    GlcNAc4
    *                      |
    *                     Gal5
    *
    *</pre>
    */
    @Test
    public void substructureQuery2() throws Exception
    {
        System.out.println("--- query 2 ---");
        setup();
        
        Sugar s2 = new Sugar();
        
        Monosaccharide 
            r1 = monosac("Man"), 
            r2 = monosac("Man"), 
            r3 = monosac("Man"),
            r4 = monosac("GlcNAc"),
            r5 = monosac("Gal");
         
        s2.addRootResidue( r1 );
        s2.addResidue( r1, linkage('a', 3, 1), r2 );
        s2.addResidue( r1, linkage('a', 6, 1), r3 );
        s2.addResidue( r3, linkage(), r4 );
        s2.addResidue( r4, linkage(), r5 );
   
        assert s2.countResidues() == 5;
        
        SubstructureQuery q2 = new SubstructureQuery( s2 );
        
        q2.execute();
        
        reportResults( q2 );
        
        teardown();
    }

    
    /** find all structures with a non-reducing-terminal Man-Glc-Gal.<br/> 
    *
    *   search struct:
    *<pre>
    *               Man1 
    *                |
    *              GlcNAc2
    *                |
    *               Gal3 (leaf)
    *
    *</pre>
    */
    @Test
    public void substructureQuery3() throws Exception
    {
        System.out.println("--- query 3 ---");
        setup();
        
        Sugar s3 = new Sugar();
        s3.addRootResidue( monosac("Man") );
        s3.addResidue( s3.lastResidue(), linkage(), monosac("Glc") );
        s3.addResidue( s3.lastResidue(), linkage(), monosac("Gal") );
        
        assert s3.countResidues() == 3;    
        
        SubstructureQuery q3 = new SubstructureQuery( s3 );
        
        q3.setOption( Option.Must_Include_All_Non_Reducing_Terminii );
        
        q3.execute();
        
        reportResults( q3 );
        
        teardown();
    }


    /** 
    *   find all structures with a non-reducing-terminal Gal-Gal.<br/> 
    *   search struct:
    *<pre>
    *               Gal1
    *                |
    *               Gal2 (leaf)
    *
    *</pre>
    */
    @Test
    public void substructureQuery4() throws Exception
    {
        System.out.println("--- query 4 ---");
        setup();
        
        Sugar s4 = new Sugar();
        s4.addRootResidue( monosac("Gal") );
        s4.addResidue( s4.lastResidue(), linkage(), monosac("Gal") );
        
        assert s4.countResidues() == 2;    
        
        SubstructureQuery q4 = new SubstructureQuery( s4 );
        
        q4.setOption( Option.Must_Include_All_Non_Reducing_Terminii );
        
        q4.execute();
        
        reportResults( q4 );
        
        teardown();
    }

    
    @Test
    public void substructureQuerySql()
    {
        setup();
        Sugar s2 = new Sugar();
        
        Monosaccharide 
            r1 = monosac("Man"), 
            r2 = monosac("Man"), 
            r3 = monosac("Man"),
            r4 = monosac("GlcNAc"),
            r5 = monosac("Gal");
         
        s2.addRootResidue( r1 );
        s2.addResidue( r1, linkage('a', 3, 1), r2 );
        s2.addResidue( r1, linkage('a', 6, 1), r3 );
        s2.addResidue( r3, linkage(), r4 );
        s2.addResidue( r4, linkage(), r5 );
   
        assert s2.countResidues() == 5;
        
        
        SubstructureQuery q = new SubstructureQuery( s2 );

        out.println("---");
        out.println( "SQL:" );
        out.println( q.getQueryString() );
                
        out.println();
        
        SubstructureQueryCriterion substruct_crit = q.getQueryCriterion();

        out.println("---");
        out.println( "criterion:" );
        out.println( substruct_crit.toSqlString() );
        
        out.println();
        
        /*  count of substruct results only  */
        out.println("---");
        out.println("Criteria-based rowCount: " );
        EntityManager em = Eurocarb.getEntityManager();
        Object x = em.createQuery( GlycanSequence.class )
                     .setProjection( Projections.rowCount() )
                     .add( substruct_crit )
                     .uniqueResult()
        ;
        out.println("    " + x );
        
        out.println();
        
        /*  BC lookup + substruct lookup  */
        out.println("---");
        out.println("Criteria-based substruct query + biological context search: " );
        
        List<GlycanSequence> results = (List<GlycanSequence>) 
                                      em.createQuery( GlycanSequence.class )
                                        .add( substruct_crit )
                                        .createAlias("glycanContexts", "gc")
                                        .createAlias("gc.biologicalContext", "bc")
                                        .createAlias("bc.taxonomy", "t")
                                        .add( Restrictions.like( "t.taxon", "xenopus", MatchMode.START ) )
                                        .setMaxResults( 10 )
                                        .list()
        ;

        if ( results != null && results.size() > 0 )
        {
            for ( GlycanSequence gs : results )
            {
                out.println("   " + gs );
            }
        }
        else
        {
            out.println("   (query successful but no results)");
        }
        
        teardown();
    }

    
    /* helper methods */
    
    static Monosaccharide monosac( String name )
    {
        // return lib.getInstanceOf( Monosaccharide.class, name );           
        return SimpleMonosaccharide.forName( name );           
    }
    
    
    static GlycosidicLinkage linkage( char anomer, int parent_term, int child_term )
    {
        return new GlycosidicLinkage( 
            Anomer.forName(anomer), parent_term, child_term );
    }
    
    
    static void reportResults( SubstructureQuery q )
    {
        List<SubstructureQueryResult> sequences = q.getResults();
        int count_results = sequences.size();
        
        if ( count_results > 0 )
        {
            int first_ten = (count_results > 10) ? 10 : sequences.size();
            StringBuilder sb = new StringBuilder();                

            out.println(
                "listing first " 
                + first_ten 
                + " of " 
                + count_results
                + " total substructure result(s) only:"
            );
            
            for ( int i = 0; i < first_ten; i++ )
            {
                GlycanSequence gs = sequences.get(i).getMatchedGlycanSequence();
                out.println(
                    "substructure result " 
                    + ( i + 1 ) 
                    + " "
                    + gs.toString()
                    /*
                    + ( VERBOSE_LOGGING 
                        ? ": \n"
                        + gs.getSugarSequence().getSugar().getGraph()
                        : "" )
                    */
                );
            }            
        }
        else
        {
            out.println( "(no results for query)" );   
        }
        
    }
    
    
    static GlycosidicLinkage linkage()
    {
        return new GlycosidicLinkage(); 
    }
    
 
    /*
    public static void main( String[] args ) throws Exception
    {
        SubstructureQueryTest q = new SubstructureQueryTest(); 

        q.substructureQuery1();
        
        q.substructureQuery2();
        
        q.substructureQuery3();
        
        q.substructureQuery4();
    }
    */
 
} // end class



