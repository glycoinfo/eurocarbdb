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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/
/**
* $Id: TestShowTissueTaxonomy.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package test.eurocarbdb.action.core;

import java.util.HashMap;
import java.util.Map;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.core.ShowTissueTaxonomy;
import org.testng.annotations.*;

import test.eurocarbdb.action.BaseActionTestSuite;

import com.opensymphony.xwork.ActionProxy;

/**
* Simple suite for testing the ShowTissueTaxonomy action
* 
* @author             hirenj
* @version                $Rev: 1549 $
*/

@Test( groups = { "ecdb.action.core.ShowTissueTaxonomy" } )
public class TestShowTissueTaxonomy extends BaseActionTestSuite {

    @BeforeSuite
    public void initialise() throws Exception {
        actionFactory.createActionProxy("", "show_tissue_taxonomy", null);
    }

    @Test
    public void testSimpleRetrieve() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();
        
        params.put("tissueTaxonomyId", "18");
        
        ActionProxy action = getAction("show_tissue_taxonomy",params);

        String result = action.execute();

        ShowTissueTaxonomy tissTax = (ShowTissueTaxonomy) action.getAction();
        
        assert EurocarbAction.SUCCESS.equals(result) : "Expected for result "+EurocarbAction.INPUT+" but got "+result;
        
        assert new Integer(18).equals(tissTax.getTissueTaxonomy().getTissueTaxonomyId()) : "Expected 18 for tissueTaxonomyId, got "+tissTax.getTissueTaxonomyId();
    }
    
    @Test
    public void testWorkingRetrieveAgainstBadData() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();
        
        params.put("tissueTaxonomyId", "18");
        
        ActionProxy action = getAction("show_tissue_taxonomy",params);
        
        String result = action.execute();
        
        ShowTissueTaxonomy tissTax = (ShowTissueTaxonomy) action.getAction();
        
        assert ! new Integer(20).equals(tissTax.getTissueTaxonomy().getTissueTaxonomyId()) : "Did not expect to get 20 for tissueTaxonomyId, should be 18";
        assert EurocarbAction.SUCCESS.equals(result) : "Expected for result "+EurocarbAction.INPUT+" but got "+result;
    }
    
    @Test
    public void testRetrieveWithBadId() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("tissueTaxonomyId", "-1");
        ActionProxy action = getAction("show_tissue_taxonomy",params);
        String result = action.execute();
        //ShowTissueTaxonomy tissTax = (ShowTissueTaxonomy) action.getAction();
        
        assert EurocarbAction.ERROR.equals(result) : "Expected for result "+EurocarbAction.INPUT+" but got "+result;

    }
    
    
}
