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
/**
* $Id: TestShowTissueTaxonomy.java 1147 2009-06-04 12:02:50Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package test.eurocarbdb.action.ms;

import java.util.HashMap;
import java.util.Map;

import org.eurocarbdb.action.ms.*;
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.ms.*;

import org.testng.annotations.*;

import test.eurocarbdb.action.BaseActionTestSuite;

import com.opensymphony.xwork.ActionProxy;

import org.apache.log4j.Logger;

/**
* Simple suite for testing the EditScan action
* 
* @author             hirenj
* @version                $Rev: 1147 $
*/
@Test( groups = { "ecdb.action.ms.EditScan" } )
public class TestEditScan extends BaseActionTestSuite {
    /** Logging handle. */
    static final Logger log = Logger.getLogger( TestEditScan.class );

    @BeforeSuite
    public void initialise() throws Exception
    {
        actionFactory.createActionProxy("", "edit_scan", null);
    }

    public void massSpecTestEditScanInput() throws Exception
    {
        Map<String,Object> params = new HashMap<String,Object>();
        
        Scan someScan = getRandomEntity(Scan.class);
        ActionProxy action = getAction("edit_scan!input",params);
        setContributor(action,someScan.getAcquisition().getContributor().getContributorId());

        String result = action.execute();

        EditScan editScans = (EditScan) action.getAction();
        
        assert "input".equals(result) : "Expecting result input for operation. but got "+result+ " instead.";        
        assert hasAnError(editScans);
    }

    public void massSpecTestEditScanInputWithScanId() throws Exception
    {
        Map<String,Object> params = new HashMap<String,Object>();
        
        Scan someScan = getRandomEntity(Scan.class);
        
        params.put("scan.scanId", ""+someScan.getScanId());
        
        ActionProxy action = getAction("edit_scan!input",params);
        setContributor(action,someScan.getAcquisition().getContributor().getContributorId());

        String result = action.execute();

        EditScan editScans = (EditScan) action.getAction();
        
        assert "input".equals(result) : "Expecting result input for operation. but got "+result+ " instead.";        
    }


    public void massSpecTestEditingScan() throws Exception
    {
        Map<String,Object> params = new HashMap<String,Object>();
        
        Scan someScan = getRandomEntity(Scan.class);
        
        params.put("scan.scanId", ""+someScan.getScanId());
        
        ActionProxy action = getAction("edit_scan",params);
        setContributor(action,someScan.getAcquisition().getContributor().getContributorId());

        String result = action.execute();

        EditScan editScans = (EditScan) action.getAction();
        
        assert "success".equals(result) : "Expecting result success for operation. but got "+result+ " instead.";
                
    } 

    public void massSpecTestBadIdentifier() throws Exception
    {
        Map<String,Object> params = new HashMap<String,Object>();
        
        params.put("scan.scanId", "-1");
        
        ActionProxy action = getAction("edit_scan",params);

        String result = action.execute();

        EditScan editScans = (EditScan) action.getAction();
        
        assert "input".equals(result) : "Expecting result input for operation. but got "+result+ " instead.";
        assert hasAnError(editScans, "scan.scanId") : "Expecting a field error for scan.scanId";
        
    }
    
    public void massSpecTestDeleteUnknownScan() throws Exception
    {
        Map<String,Object> params = new HashMap<String,Object>();
        
        params.put("scan.scanId", "-1");
        
        ActionProxy action = getAction("delete_scan",params);

        String result = action.execute();

        EditScan editScans = (EditScan) action.getAction();
        
        assert "input".equals(result) : "Expecting result input for operation. but got "+result+ " instead.";
        assert hasAnError(editScans, "scan.scanId") : "Expecting a field error for scan.scanId";        
    }
    
    public void massSpecTestChangeScanDetails() throws Exception
    {
        Map<String,Object> params = new HashMap<String,Object>();
        
        Acquisition someAcquisition = getRandomEntity(Acquisition.class);
        
        params.put("acquisitionId", ""+someAcquisition.getAcquisitionId());
        params.put("scan.startMz","200.0");
        
        ActionProxy action = getAction("create_scan",params);
        setContributor(action,someAcquisition.getContributor().getContributorId());

        String result = action.execute();

        assert "success".equals(result) : "Expecting result success for operation. but got "+result+ " instead.";

        Scan someScan = ((CreateScan) action.getAction()).getScan();
        
        assert 200.0 == someScan.getStartMz() : "Expecting 200.0 for startMz, got "+someScan.getStartMz()+" instead";
        
        params.clear();
        
        params.put("scan.scanId",""+someScan.getScanId());
        params.put("scan.startMz","100.0");
        
        action = getAction("edit_scan",params);
        setContributor(action,someAcquisition.getContributor().getContributorId());

        result = action.execute();
        
        EditScan editScans = (EditScan) action.getAction();

        assert "success".equals(result) : "Expecting result success for operation. but got "+result+ " instead.";
        
        assert 100.0 == editScans.getScan().getStartMz() : "Expecting startMz to be set to 100.0 but instead was "+editScans.getScan().getStartMz();

        params.clear();
        
        params.put("scan.scanId",""+someScan.getScanId());
        
        action = getAction("delete_scan",params);
        setContributor(action,someAcquisition.getContributor().getContributorId());

        result = action.execute();
        
        editScans = (EditScan) action.getAction();

        assert "success".equals(result) : "Expecting result success for operation. but got "+result+ " instead.";
                
    }

}