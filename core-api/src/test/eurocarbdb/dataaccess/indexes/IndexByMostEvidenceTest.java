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

package test.eurocarbdb.dataaccess.indexes;

import java.util.List;

import org.testng.annotations.*;
import org.hibernate.Criteria;

import test.eurocarbdb.dataaccess.CoreApplicationTest;

import org.eurocarbdb.dataaccess.indexes.Index;
import org.eurocarbdb.dataaccess.indexes.IndexByMostEvidence;
import org.eurocarbdb.dataaccess.core.GlycanSequence;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*   Test class for ensuring that the index {@link IndexByMostEvidence}
*   actually works.
*
*   @author mjh
*   @version $Rev: 1147 $
*/
public class IndexByMostEvidenceTest extends CoreApplicationTest
{

    
    @Test
    (   
        groups={"ecdb.indexes"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void indexGlycanSequenceByMostEvidence1() 
    {
        super.setup();
        
        Criteria q = getEntityManager()
                    .createQuery( GlycanSequence.class )
                    .setMaxResults( 20 );
        
        List<GlycanSequence> seqlist1 = (List<GlycanSequence>) q.list();                    
                    
        Index<GlycanSequence> by_evidence = new IndexByMostEvidence<GlycanSequence>();
        by_evidence.apply( q );
        
        List<GlycanSequence> seqlist2 = (List<GlycanSequence>) q.list();
        
        System.out.println();
        System.out.println("first list: unordered");
        System.out.println("sequences should appear in a random order");
        
        for ( int i = 0; i < seqlist1.size(); i++ )
        {
            GlycanSequence gs = seqlist1.get( i );
            System.out.println(
                (i + 1)
                + ": id = "
                + gs.getGlycanSequenceId()
                + " evidence count = "
                + gs.getEvidenceCount()
            );
        }

        System.out.println();
        System.out.println("second list: ordered by most evidence");
        System.out.println("sequences should be sorted, with seqs having the most evidence coming first");

        for ( int i = 0; i < seqlist1.size(); i++ )
        {
            GlycanSequence gs = seqlist2.get( i );
            System.out.println(
                (i + 1)
                + ": id = "
                + gs.getGlycanSequenceId()
                + " evidence count = "
                + gs.getEvidenceCount()
            );
        }
        
        /* TODO */
        // System.out.println();
        // System.out.println("verification:");
        
        
        super.teardown();
    }

}
