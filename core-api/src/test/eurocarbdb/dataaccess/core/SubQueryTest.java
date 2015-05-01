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

package test.eurocarbdb.dataaccess.core;

import java.util.*;

import org.testng.annotations.*;


import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.HibernateEntityManager;

import org.eurocarbdb.dataaccess.core.*;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.*;

import test.eurocarbdb.dataaccess.CoreApplicationTest;

/**
*   @author mjh
*   @version $Rev: 1210 $
*/
@Test
(   groups={"ecdb.db.core.hibernate"}
,   dependsOnGroups={"ecdb.db.connection"} 
)
public class SubQueryTest extends CoreApplicationTest
{
    EntityManager em = Eurocarb.getEntityManager();
    
    // private GlycanSequence gs1;
   
    public void canSubQuery()
    {
      super.setup();
      String taxonomyName = "homo sapiens";
      
      Criteria q = em.createQuery( GlycanSequence.class ).setMaxResults( 20 );
      
      List<GlycanSequence> seqlist1 = (List<GlycanSequence>) q.list();
      
      System.out.println(seqlist1.size());

      Criteria bc_criteria;
      Criteria tax_criteria;

      Criteria criteria = em.createQuery( GlycanSequence.class ).setMaxResults( 20 );
      bc_criteria = criteria.createCriteria("glycanContexts")
                      .createCriteria("biologicalContext", "bc");    



        // add taxonomy criteria        
        if( taxonomyName!=null ) {
        tax_criteria = bc_criteria.createCriteria("taxonomy", "taxa")
                            .createCriteria("taxonomySupertypes", "supertax")
                            .add( Restrictions.ilike( "taxon", taxonomyName, MatchMode.EXACT ) );            
        }
        
        System.out.println(criteria.list().size());
      
      super.teardown();
    }

    public void canSimpleDetachedSubQuery() {
      super.setup();

      //Criteria subcriteria = em.createQuery( GlycanSequence.class ).setMaxResults( 20 );
      DetachedCriteria subcriteria = DetachedCriteria.forClass(GlycanSequence.class);

      DetachedCriteria criteria;

      criteria = DetachedCriteria.forClass( GlycanSequence.class );        
        
//        criteria.add( Restrictions.lt( "glycanSequenceId", 100));
        DetachedCriteria bc_crit = criteria.createCriteria("glycanContexts");
        
        //DetachedCriteria bc_crit = DetachedCriteria.forClass( GlycanSequenceContext.class );
        
        //criteria.createCriteria("glycanContexts");
        
        bc_crit.add( Restrictions.gt( "glycanSequenceContextId", 0));
        bc_crit.setProjection( Projections.property("glycanSequenceContextId"));
        
        criteria.add( Subqueries.propertyIn("glycanSequenceContextId",bc_crit) );
        
      criteria.setProjection(
          Projections.distinct(
              Projections.property("glycanSequenceId")));


      subcriteria.add( Subqueries.propertyIn("glycanSequenceId",criteria));
      
      //System.out.println(subcriteria.list().size());
      //subcriteria.getExecutableCriteria(((HibernateEntityManager) em).getHibernateSession()).setMaxResults(20).list();
      
      super.teardown();
    }


    public void canDetachedSubQuery() {
      super.setup();
      String taxonomyName = "homo sapiens";

      //Criteria subcriteria = em.createQuery( GlycanSequence.class ).setMaxResults( 20 );
      DetachedCriteria subcriteria = DetachedCriteria.forClass(GlycanSequence.class);


      DetachedCriteria criteria;

      criteria = DetachedCriteria.forClass( GlycanSequence.class );        
        
        DetachedCriteria bc_criteria;
        DetachedCriteria tax_criteria;

      bc_criteria = criteria.createCriteria("glycanContexts")
                      .createCriteria("biologicalContext", "bc");    



        // add taxonomy criteria        
        if( taxonomyName!=null ) {
        tax_criteria = bc_criteria.createCriteria("taxonomy", "taxa")
                            .createCriteria("taxonomySupertypes", "supertax")
                            .add( Restrictions.ilike( "taxon", taxonomyName, MatchMode.EXACT ) );            
        }

      criteria.setProjection(
          Projections.distinct(
              Projections.property("glycanSequenceId")));


      subcriteria.add( Subqueries.propertyIn("glycanSequenceId",criteria));
      
      //System.out.println(subcriteria.list().size());
      subcriteria.getExecutableCriteria(((HibernateEntityManager) em).getHibernateSession()).list();
      
      super.teardown();
    }

} // end class



