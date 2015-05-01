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
*   Last commit: $Rev: 1541 $ by $Author: glycoslave $ on $Date:: 2009-07-17 #$  
*/

package org.eurocarbdb.dataaccess.indexes;


//  stdlib imports
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

//  3rd party imports

//  eurocarb imports
import org.eurocarbdb.dataaccess.Contributed;
import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.GlycanSequenceEvidence;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.transform.PassThroughResultTransformer;

//  static imports
import static org.hibernate.criterion.CriteriaSpecification.LEFT_JOIN;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.dataaccess.hibernate.HibernateUtils.RETURN_FIRST_COLUMN_ONLY;


/**
*   Allows the sorting and comparison of {@link GlycanSequence}s by least/most
*   items of associated {@link Evidence}.
*
*   @see GlycanSequence.getEvidenceCount
*   @see GlycanSequenceEvidence
*
*   @author mjh
*   @version $Rev: 1541 $
*/
public class IndexByMostEvidence<T extends GlycanSequence> implements Index<T>
{

    public void apply( Criteria query )
    {
        /*  
        *   the query for this index requires a horrible hibernate hack.
        *   basically, the incoming Criteria query depends on an SQL 
        *   'group by', which, due to Hibernate limitations, screws over 
        *   the select clause. So, we attach a results transformer to the
        *   query that post-processes the result set from a list of 
        *   GlycanSequence ids into GlycanSequence objects through a
        *   separate query.
        */
        query
            .createAlias("glycanEvidence", "gs2ev", LEFT_JOIN )  
            .setProjection( 
                Projections.projectionList()
                    .add( Projections.groupProperty("glycanSequenceId") )
                    .add( Projections.count( "gs2ev.evidence" ).as( "ev" ) )
                )
            .addOrder( Order.desc("ev") )
            .setResultTransformer( 
                new PassThroughResultTransformer()
                {
                    public List transformList( List results )
                    {
                        if ( results == null || results.size() == 0 )
                            return results;
                        
                        List<Object[]> rows = (List<Object[]>) results;
                        
                        //  the lookup hash
                        Map<Integer,GlycanSequence> hash 
                            = new HashMap<Integer,GlycanSequence>( results.size() );
                            
                        //  gather the sequence ids 
                        List<Integer> ids = new ArrayList<Integer>( results.size() );
                        for ( Object[] columns : rows )
                            ids.add( (Integer) columns[0] );
                            
                        //  look them up
                        List<GlycanSequence> seqs = (List<GlycanSequence>) getEntityManager()
                            .createQuery( GlycanSequence.class )
                            .add( Restrictions.in( "glycanSequenceId", ids ) )
                            .setFetchSize( ids.size() )
                            // .setCacheable( true )
                            .list()
                            ;
                            
                        for ( GlycanSequence seq : seqs )
                            hash.put( seq.getGlycanSequenceId(), seq );
                     
                        seqs.clear();
                        
                        for ( Integer id : ids )
                            seqs.add( hash.get( id ) );
                        
                        return seqs;
                    }
/*
                    public Object transformTuple( Object[] tuple, String[] aliases )
                    {
                        int id = (Integer) tuple[0];
                        return getEntityManager().lookup( GlycanSequence.class, id );
                    }
*/                        
                }
            ) 
        ;

/* // attempt 2
        query
            .createAlias("glycanEvidence", "gs2ev", LEFT_JOIN ) 
            .addOrder( 
                new Order( "irrelevant", false ) 
                {
                    public final String toSqlString( Criteria c, CriteriaQuery q )
                    {
                        // return "count( gs2ev.evidence_id ) desc";       
                        return "count( "
                            + "gs2ev1_"//q.getSQLAlias( query, "glycanEvidence" )
                            + ".evidence_id ) desc";       
                    }
                }
            )
        ;
*/
/* // attempt 3
        query
            .createAlias("glycanEvidence", "gs2ev", LEFT_JOIN )  
            .setProjection( 
                Projections.groupProperty("glycanSequenceId").as("glycanSequenceId")
            )
            .addOrder( 
                new Order( "irrelevant", false ) 
                {
                    public final String toSqlString( Criteria c, CriteriaQuery q )
                    {
                        // return "count( gs2ev.evidence_id ) desc";       
                        return "count( "
                            + "gs2ev1_"//q.getSQLAlias( query, "glycanEvidence" )
                            + ".evidence_id ) desc";       
                    }
                }
            )
            .setResultTransformer( 
                org.hibernate.transform.Transformers.aliasToBean( 
                    GlycanSequence.class ) ) 
        ;
*/        
    }
    
    
    public void apply( List<T> results )
    {
        Collections.sort( results, this );
    }    
    
    
    public int compare( T seq1, T seq2 ) 
    {
        int i1 = seq1.getEvidenceCount();
        int i2 = seq2.getEvidenceCount();
        return (i1 > i2) ? 1 : (i1 < i2) ? -1 : 0; 
    }
    
    
    public Class<GlycanSequence> getIndexableType()
    {
        return GlycanSequence.class;
    }

    
    /** Returns "evidence". */
    public String getName()
    {
        return "evidence";
    }
    
    
    /** Returns "Amount of evidence". */
    public String getTitle()
    {
        return "Amount of evidence";
    }
    
    
    public String getDescription()
    {
        return "Orders results by most to least items of evidence";
    }
    
    
    
}



