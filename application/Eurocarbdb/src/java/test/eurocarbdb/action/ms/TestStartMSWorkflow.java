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
package test.eurocarbdb.action.ms;


import test.eurocarbdb.action.BaseActionTestSuite;

import java.util.HashMap;
import java.util.Map;


import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.core.*;
import org.eurocarbdb.action.ms.*;

import org.eurocarbdb.dataaccess.core.*;

import org.testng.annotations.*;

import test.eurocarbdb.action.BaseActionTestSuite;

import com.opensymphony.xwork.ActionProxy;

import org.apache.log4j.Logger;

/**
* Simple suite for testing the StartWorkflow action
* 
* @author               hirenj
* @version              $Rev: 1549 $
*/
@Test( groups = { "ecdb.action.ms.StartWorkflow" } )
public class TestStartMSWorkflow extends BaseActionTestSuite
{
    /** Logging handle. */
    static final Logger log = Logger.getLogger( TestStartMSWorkflow.class );

    @BeforeSuite
    public void initialise() throws Exception {
        actionFactory.createActionProxy("", "create_ms", null);
    }

    public void massSpecTestNoInput() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        ActionProxy action = getAction("create_ms",params);
        setContributor(action,2);

        String result = null;
        try {
            result = action.execute();
        } catch ( Exception e ) {
            log.debug("Error executing",e);
        }

        StartWorkflow startWorkflow = (StartWorkflow) action.getAction();

        assert "select_context".equals(result) : "Expected result select_context but got "+result;
    }
 
    public void massSpecTestContextIdGiven() throws Exception {
        GlycanSequenceContext randomContext = getRandomEntity(GlycanSequenceContext.class);
        
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("glycanSequenceContextId",""+randomContext.getGlycanSequenceContextId());

        ActionProxy action = getAction("create_ms",params);
        setContributor(action,2);

        String result = action.execute();

        StartWorkflow startWorkflow = (StartWorkflow) action.getAction();
        
        assert startWorkflow.getGlycanSequenceContext() != null : "Did not retrieve a GlycanSequenceContext";

        int inputId = randomContext.getGlycanSequenceContextId();
        int outputId = startWorkflow.getGlycanSequenceContext().getGlycanSequenceContextId();

        assert inputId == outputId : "IDs did not match, was expecting "+inputId+" but instead got "+outputId;

        assert "success".equals(result) : "Expected result success but got "+result;        
    }

    public void massSpecTestSequenceIdGiven() throws Exception {
        GlycanSequence randomEntity = getRandomEntity(GlycanSequence.class);
        
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("glycanSequenceId",""+randomEntity.getGlycanSequenceId());

        ActionProxy action = getAction("create_ms",params);
        setContributor(action,2);

        String result = action.execute();

        StartWorkflow startWorkflow = (StartWorkflow) action.getAction();
        
        assert startWorkflow.getGlycanSequence() != null : "Did not retrieve a GlycanSequence";

        int inputId = randomEntity.getGlycanSequenceId();
        int outputId = startWorkflow.getGlycanSequence().getGlycanSequenceId();

        assert inputId == outputId : "IDs did not match, was expecting "+inputId+" but instead got "+outputId;

        assert "select_context".equals(result) : "Expected result select_context but got "+result;                
    }
    
    public void massSpectTestBadSequenceIdGiven() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("glycanSequenceId","-1");

        ActionProxy action = getAction("create_ms",params);
        setContributor(action,2);

        String result = action.execute();

        StartWorkflow startWorkflow = (StartWorkflow) action.getAction();
        
        assert startWorkflow.getGlycanSequence() == null : "Retrieved a GlycanSequence instead of skipping it";

        assert "select_context".equals(result) : "Expected result select_context but got "+result;
    }

}