package test.eurocarbdb.action.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eurocarbdb.action.core.Autocompleter;
import org.eurocarbdb.action.core.Autocompleter.AutocompleteResult;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import test.eurocarbdb.action.BaseActionTestSuite;

import com.opensymphony.xwork.ActionProxy;

@Test( groups = { "ecdb.action.core.CreateReferences" } )
public class TestAutoCompletor 
	extends BaseActionTestSuite {

	    /** Logging handle. */
	    static final Logger log = Logger.getLogger( TestAutoCompletor.class );

	    @BeforeSuite
	    public void initialise() throws Exception {
	        actionFactory.createActionProxy("", "user_autocompleter", null);
	        log.setLevel(Level.OFF);
	    }

	    public void testHomoSapQueryString() throws Exception {
	    	
	    	HashMap<String,String> queryToResult=new HashMap<String,String>();
	    	queryToResult.put("Homo", "Homo sapiens");
	    	queryToResult.put("9606", "Homo sapiens");
	    	queryToResult.put("Mus", "Mus musculus");
	    	queryToResult.put("10090","Mus musculus");
	    	
	    	for(String query:queryToResult.keySet()){
	    		Map<String,Object> params = new HashMap<String,Object>();

	    		params.put("queryString", query);
	    		params.put("queryType", "taxonomy_name");

	    		ActionProxy action = getAction("user_autocompleter",params);

	    		String result = action.execute();

	    		Autocompleter autoObj=(Autocompleter) action.getAction();

	    		Set<AutocompleteResult> resultSet=autoObj.getResults();
	    		Iterator<AutocompleteResult> resultSetIterator=resultSet.iterator();
	    		String topResult=resultSetIterator.next().description;
	    		log.debug("Top result: "+topResult);
	    		assert (topResult.equals(queryToResult.get(query))) : "Expecting top result of "+queryToResult.get(query)+" but got: "+topResult+" instead. For query: "+query;
	    	}
	    }	    
	    
	    public void testDiseaseQueryString() throws Exception {
	    	HashMap<String,String> queryToResult=new HashMap<String,String>();
	    	queryToResult.put("Canc", "Cancer");
	    	
	    	for(String query:queryToResult.keySet()){
	    		Map<String,Object> params = new HashMap<String,Object>();

	    		params.put("queryString", query);
	    		params.put("queryType", "disease_name");

	    		ActionProxy action = getAction("user_autocompleter",params);

	    		String result = action.execute();

	    		Autocompleter autoObj=(Autocompleter) action.getAction();

	    		Set<AutocompleteResult> resultSet=autoObj.getResults();
	    		Iterator<AutocompleteResult> resultSetIterator=resultSet.iterator();
	    		String topResult=resultSetIterator.next().description;
	    		log.debug("Top result: "+topResult);
	    		assert (topResult.equals(queryToResult.get(query))) : "Expecting top result of "+queryToResult.get(query)+" but got: "+topResult+" instead. For query: "+query;
	    	}
	    }
}
