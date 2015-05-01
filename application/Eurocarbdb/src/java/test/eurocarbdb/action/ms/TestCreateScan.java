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

import org.eurocarbdb.dataaccess.Eurocarb;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.core.*;
import org.eurocarbdb.action.ms.*;

import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.ms.*;

import org.testng.annotations.*;

import test.eurocarbdb.action.BaseActionTestSuite;

import com.opensymphony.xwork.ActionProxy;

import org.apache.log4j.Logger;

/**
* Simple suite for testing the CreateScan action
* 
* @author               hirenj
* @version              $Rev: 1549 $
*/
@Test( groups = { "ecdb.action.ms.CreateScan" } )
public class TestCreateScan extends BaseActionTestSuite
{
    /** Logging handle. */
    static final Logger log = Logger.getLogger( TestCreateScan.class );

    @BeforeSuite
    public void initialise() throws Exception {
        actionFactory.createActionProxy("", "create_scan", null);
    }

    public void testMassSpecCreateAScanWithoutAcquisition() throws Exception
    {
        Map<String,Object> params = new HashMap<String,Object>();

        ActionProxy action = getAction("create_scan",params);
        setContributor(action,2);

        String result = null;
        try {
            result = action.execute();
        } catch ( Exception e ) {
            log.debug("Error executing",e);
        }

        CreateScan createScan = (CreateScan) action.getAction();

        assert "input".equals(result) : "Expected result input but got "+result;        
    }

    public void testMassSpecCreateAScan() throws Exception
    {
        Map<String,Object> params = new HashMap<String,Object>();

        Acquisition randomAcquisition = getRandomEntity(Acquisition.class);        
        
        int scanCount = randomAcquisition.getScans().size();

        params.put("acquisition.acquisitionId",randomAcquisition.getAcquisitionId()+"");
        params.put("scan.startMz","100.0");
        params.put("scan.contributorQuality","1.0");

        ActionProxy action = getAction("create_scan",params);
        setContributor(action,randomAcquisition.getContributor().getContributorId());
        String result = null;
        try {
            result = action.execute();
        } catch ( Exception e ) {
            log.debug("Error executing",e);
        }

        CreateScan createScan = (CreateScan) action.getAction();


        assert "success".equals(result) : "Expected result success but got "+result;

        int newScanCount = randomAcquisition.getScans().size();

        assert (scanCount + 1) == newScanCount : "Expected scan count to increase by 1, instead went from "+scanCount+ " to "+newScanCount;

        assert randomAcquisition.getScans().contains(createScan.getScan()) : "Scan isn't added to the list of scans of the acquisition";

        assert createScan.getScan().getStartMz() == 100.0 : "The scan value has not been set properly";

        randomAcquisition.getScans().remove(createScan.getScan());        

        newScanCount = randomAcquisition.getScans().size();

        Eurocarb.getEntityManager().remove(createScan.getScan());
        
        assert scanCount == newScanCount : "Failed to delete the scan again";
        
    }
    
    public void testMassSpecParameterValidation() throws Exception 
    {
        Map<String,Object> params = new HashMap<String,Object>();

        Acquisition randomAcquisition = getRandomEntity(Acquisition.class);        
        
        int scanCount = randomAcquisition.getScans().size();

        params.put("acquisition.acquisitionId",randomAcquisition.getAcquisitionId()+"");
        params.put("scan.startMz","FOOBAR");
        params.put("scan.endMz","100.0");
        params.put("scan.contributorQuality","999.0");

        ActionProxy action = getAction("create_scan",params);
        setContributor(action,randomAcquisition.getContributor().getContributorId());
        String result = null;
        try {
            result = action.execute();
        } catch ( Exception e ) {
            log.debug("Error executing",e);
        }

        CreateScan createScan = (CreateScan) action.getAction();

        assert createScan.getFieldErrors().get("scan.startMz") != null : "No validation error for scan.startMz field";
        assert createScan.getFieldErrors().get("scan.contributorQuality") != null : "No validation error for scan.contributorQuality field";
        
        assert "input".equals(result) : "Expected result input but got "+result;

    }
        
}