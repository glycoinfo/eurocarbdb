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
* $Id: TestCreateReferences.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package test.eurocarbdb.action.core;

import java.util.HashMap;
import java.util.Map;


import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.core.CreateReferences;
import org.eurocarbdb.dataaccess.core.*;
import org.testng.annotations.*;

import test.eurocarbdb.action.BaseActionTestSuite;

import com.opensymphony.xwork.ActionProxy;

import org.apache.log4j.Logger;

/**
* Simple suite for testing the CreateReferences action
* 
* @author             hirenj
* @version                $Rev: 1549 $
*/
@Test( groups = { "ecdb.action.core.CreateReferences" } )
public class TestCreateReferences extends BaseActionTestSuite {

    /** Logging handle. */
    static final Logger log = Logger.getLogger( TestCreateReferences.class );

    @BeforeSuite
    public void initialise() throws Exception {
        actionFactory.createActionProxy("", "create_references", null);
    }

    public void createReferencesTestPubmedAddition() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();
        
        params.put("journalReference.pubmedId", "12345");
        
        ActionProxy action = getAction("create_references",params);

        String result = action.execute();

        CreateReferences createRefs = (CreateReferences) action.getAction();

        assert EurocarbAction.SUCCESS.equals(result) : "Expected result "+EurocarbAction.INPUT+" but got "+result;
        
        assert (1 == createRefs.getReferences().size()) : "Expected 1 result reference from pubmed retrieve but got "+createRefs.getReferences().size();
        
        assert 12345 == ((JournalReference) createRefs.getReferences().get(0)).getPubmedId() : "Expected to retrieve pubmed with id of 12345, but got "+((JournalReference) createRefs.getReferences().get(0)).getPubmedId();

        assert ("Rubinstein MH").equals(((JournalReference) createRefs.getReferences().get(0)).getAuthors()) : "Expected to retrieve article by Rubinstein MH but got "+((JournalReference) createRefs.getReferences().get(0)).getAuthors();
        
    }    

    public void createReferencesTestBadPubmedAddition() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("journalReference.pubmedId", "99999999");

        ActionProxy action = getAction("create_references",params);

        String result = action.execute();

        CreateReferences createRefs = (CreateReferences) action.getAction();

        assert EurocarbAction.SUCCESS.equals(result) : "Expected result "+EurocarbAction.INPUT+" but got "+result;

        assert (0 == createRefs.getReferences().size()) : "Expected 0 result reference from pubmed retrieve but got "+createRefs.getReferences().size();                
    }
    
    public void createReferencesTestPubmedAlreadyDownloaded() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("journalReference.pubmedId", "7737180");

        ActionProxy action = getAction("create_references",params);

        String result = action.execute();

        CreateReferences createRefs = (CreateReferences) action.getAction();

        assert EurocarbAction.SUCCESS.equals(result) : "Expected result "+EurocarbAction.INPUT+" but got "+result;

        assert (1 == createRefs.getReferences().size()) : "Expected 0 result reference from pubmed retrieve but got "+createRefs.getReferences().size();

        assert 7737180 == ((JournalReference) createRefs.getReferences().get(0)).getPubmedId() : "Expected to retrieve pubmed with id of 7737180, but got "+((JournalReference) createRefs.getReferences().get(0)).getPubmedId();

        assert ("Lochnit G; Geyer R").equals(((JournalReference) createRefs.getReferences().get(0)).getAuthors()) : "Expected to retrieve article by Rubinstein MH but got "+((JournalReference) createRefs.getReferences().get(0)).getAuthors();

        assert 0 != createRefs.getReferences().get(0).getReferenceId() : "Expected a non-zero reference ID for an existing entry, but got 0";
    }

    public void createReferencesTestAddingToExistingList() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("journalReference.pubmedId", "7737180");

        ActionProxy action = getAction("create_references",params);

        CreateReferences createRefs = (CreateReferences) action.getAction();

        createRefs.getReferences().add(JournalReference.createFromPubmedId(12520065));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(17202164));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(15174134));

        assert 3 == createRefs.getReferences().size();

        String result = action.execute();

        assert 4 == createRefs.getReferences().size() : "Expected 4 references in the list, instead got "+createRefs.getReferences().size();        
    }


    public void createReferencesTestAddDuplicateReference() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("journalReference.pubmedId", "12520065");

        ActionProxy action = getAction("create_references",params);

        CreateReferences createRefs = (CreateReferences) action.getAction();

        createRefs.getReferences().add(JournalReference.createFromPubmedId(12520065));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(17202164));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(15174134));

        assert 3 == createRefs.getReferences().size();

        String result = action.execute();

        log.debug("Errors are");
        for (Object error: createRefs.getActionErrors()) {
            log.debug((String) error);
        }
        
        log.debug(createRefs.getFieldErrors().size());

        assert "success".equals(result) : "Expected success for result, instead got "+result;

        assert 3 == createRefs.getReferences().size() : "Expected 3 references in the list, instead got "+createRefs.getReferences().size();
    }

    public void createReferencesTestAddJournalReference() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("journalReference.authors", "Hiren");
        params.put("journalReference.journal.journalTitle", "Hirens journal of articles");
        params.put("journalReference.journal.journalAbbrev", "H.J.A");

        params.put("journalReference.title","Hiren's big adventure with unit testing");
        params.put("journalReference.publicationYear","2009");
        params.put("journalReference.journalVolume","12");
        params.put("journalReference.firstPage","1");
        params.put("journalReference.lastPage","2");
        params.put("journalReference.referenceType","journal");
        
        ActionProxy action = getAction("create_references",params);

        CreateReferences createRefs = (CreateReferences) action.getAction();

        assert 0 == createRefs.getReferences().size();

        String result = action.execute();

        assert "success".equals(result) : "Expected success result, instead got "+result;

        assert 1 == createRefs.getReferences().size() : "Expected 1 reference in the list, instead got "+createRefs.getReferences().size();
        
        assert "Hiren".equals(((JournalReference) createRefs.getReferences().get(0)).getAuthors()) : "Expected Authors to be 'Hiren' but, instead got "+((JournalReference) createRefs.getReferences().get(0)).getAuthors();
        
    }

    public void createReferencesTestAddJournalReferenceWithMissingFields() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("journalReference.authors", "Hiren");
        params.put("journalReference.referenceType","journal");
        
        ActionProxy action = getAction("create_references",params);

        CreateReferences createRefs = (CreateReferences) action.getAction();

        assert 0 == createRefs.getReferences().size();

        String result = action.execute();

        assert "input".equals(result) : "Expected input result, instead got "+result;

        assert 0 == createRefs.getReferences().size() : "Expected 0 references in the list, instead got "+createRefs.getReferences().size();        
    }

    public void createReferencesTestAddReferenceToListOfJournals() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("journalReference.authors", "Hiren");
        params.put("journalReference.journal.journalTitle", "Hirens journal of articles");
        params.put("journalReference.title","Hiren's big adventure with unit testing");
        params.put("journalReference.journal.journalAbbrev", "H.J.A");
        params.put("journalReference.publicationYear","2009");
        params.put("journalReference.journalVolume","12");
        params.put("journalReference.firstPage","1");
        params.put("journalReference.lastPage","2");
        params.put("journalReference.referenceType","journal");
        
        ActionProxy action = getAction("create_references",params);

        CreateReferences createRefs = (CreateReferences) action.getAction();

        createRefs.getReferences().add(JournalReference.createFromPubmedId(12520065));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(17202164));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(15174134));

        assert 3 == createRefs.getReferences().size();

        String result = action.execute();

        assert "success".equals(result) : "Expected success result, instead got "+result;

        assert 4 == createRefs.getReferences().size() : "Expected 4 references in the list, instead got "+createRefs.getReferences().size();
        
        assert "Hiren".equals(((JournalReference) createRefs.getReferences().get(3)).getAuthors()) : "Expected Authors to be 'Hiren' but, instead got "+((JournalReference) createRefs.getReferences().get(3)).getAuthors();
    }

    public void createReferencesTestDoNothing() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        ActionProxy action = getAction("create_references",params);

        CreateReferences createRefs = (CreateReferences) action.getAction();

        String result = action.execute();

        assert 0 == createRefs.getReferences().size() : "Expected 0 references in the list, instead got "+createRefs.getReferences().size();
        assert "input".equals(result) : "Expected result of input but got "+result;
    }

    public void createReferencesTestDoNothingWhenThereIsAnExistingSession() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        ActionProxy action = getAction("create_references",params);

        CreateReferences createRefs = (CreateReferences) action.getAction();

        createRefs.getReferences().add(JournalReference.createFromPubmedId(12520065));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(17202164));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(15174134));

        assert 3 == createRefs.getReferences().size();

        String result = action.execute();

        assert 3 == createRefs.getReferences().size() : "Expected 3 references in the list, instead got "+createRefs.getReferences().size();
        
        assert "input".equals(result) : "Expected result of input but got "+result;
        
    }

    public void createReferencesTestCreateWebsiteReference() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("reference.url", "http://eurocarbdb.org");
        params.put("reference.referenceType","website");
        params.put("reference.externalReferenceId","999");
        params.put("reference.externalReferenceName","CarbBank");
        
        ActionProxy action = getAction("create_references",params);

        CreateReferences createRefs = (CreateReferences) action.getAction();

        createRefs.getReferences().add(JournalReference.createFromPubmedId(12520065));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(17202164));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(15174134));

        assert 3 == createRefs.getReferences().size();

        String result = action.execute();

        log.debug("Errors are");
        for (Object error: createRefs.getActionErrors()) {
            log.debug((String) error);
        }

        assert "success".equals(result) : "Expected success result, instead got "+result;

        assert 4 == createRefs.getReferences().size() : "Expected 4 references in the list, instead got "+createRefs.getReferences().size();
        
    }



    public void createReferencesTestCreateDatabaseReference() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("reference.url", "http://eurocarbdb.org");
        params.put("reference.externalReferenceId", "12");
        params.put("reference.referenceType","database");
        params.put("reference.externalReferenceName", "eurocarbdb");
        
        ActionProxy action = getAction("create_references",params);

        CreateReferences createRefs = (CreateReferences) action.getAction();

        createRefs.getReferences().add(JournalReference.createFromPubmedId(12520065));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(17202164));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(15174134));

        assert 3 == createRefs.getReferences().size();

        String result = action.execute();

        log.debug("Errors are");
        for (Object error: createRefs.getActionErrors()) {
            log.debug((String) error);
        }


        assert "success".equals(result) : "Expected success result, instead got "+result;

        assert 4 == createRefs.getReferences().size() : "Expected 4 references in the list, instead got "+createRefs.getReferences().size();
        
    }

    public void createReferencesTestIncompleteCreateDatabaseReference() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("reference.url", "http://eurocarbdb.org");
        params.put("reference.referenceType","database");
        
        ActionProxy action = getAction("create_references",params);

        CreateReferences createRefs = (CreateReferences) action.getAction();

        createRefs.getReferences().add(JournalReference.createFromPubmedId(12520065));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(17202164));
        createRefs.getReferences().add(JournalReference.createFromPubmedId(15174134));

        assert 3 == createRefs.getReferences().size();

        String result = action.execute();

        log.debug("Errors are");
        for (Object error: createRefs.getActionErrors()) {
            log.debug((String) error);
        }


        assert "input".equals(result) : "Expected input result, instead got "+result;

        assert 3 == createRefs.getReferences().size() : "Expected 4 references in the list, instead got "+createRefs.getReferences().size();
        
    }

    public void createReferencesTestConflictingParameterResolution() throws Exception {
        Map<String,Object> params = new HashMap<String,Object>();

        params.put("reference.url", "http://eurocarbdb.org");
        params.put("journalReference.pubmedId", "12520065");
        
        ActionProxy action = getAction("create_references",params);

        CreateReferences createRefs = (CreateReferences) action.getAction();

        assert 0 == createRefs.getReferences().size();

        String result = action.execute();

        log.debug("Errors are");
        for (Object error: createRefs.getActionErrors()) {
            log.debug((String) error);
        }

        assert "input".equals(result) : "Expected input result, instead got "+result;

        assert 0 == createRefs.getReferences().size() : "Expected 1 references in the list, instead got "+createRefs.getReferences().size();
        
    }

}
