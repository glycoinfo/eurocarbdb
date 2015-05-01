package org.eurocarbdb.util.glycomedb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class Configuration {
	
	private String[] m_databases = {"bcsdb","carbbank","cfg","eurocarbdb(nibrt)","glycobase(dublin)","glycobase(lille)","GlycomeDB","glycosciences.de","kegg","pdb"};
	private String username = "";
	private String userEmailAddress = "";
	private String url = "";
	private String ignoreDatabase = "";
	private String userFullName="";
	private String currentFolder="";
	private String outXMLFilename="";
	private HashMap<String, String> m_hashDatabaseNames = new HashMap<String, String>();
	private HashMap<String, String> m_hashDatabaseURL = new HashMap<String, String>();
	
	/*get a database name from HashMap
	*/
	public String getDatabaseName(String a_key)
	{
		String t_result = this.m_hashDatabaseNames.get(a_key);
		if ( t_result == null )
		{
			return a_key;
		}
		return t_result;
	}
	
	/*get a database url from HashMap
	*/
	public String getDatabaseUrl(String a_key)
	{
		return this.m_hashDatabaseURL.get(a_key);
	}
	
	/*constructor
	 * loading up all information from ini file and save databseName and URL into hashMap
	 */
    public Configuration(String a_file ) throws FileNotFoundException, IOException {
    	Properties p = new Properties();
        p.load(new FileInputStream(a_file));
    	setUsername(p.getProperty("DBuser"));
    	setUserFullName(p.getProperty("DBuserFullName"));
    	setUserEmailAddress(p.getProperty("DBuserEmailAddress"));
    	setUrl(p.getProperty("DBurl"));
    	setCurrentFolder(p.getProperty("CurrentFolder"));
    	setOutXMLFilename(p.getProperty("outXMLFilename"));
    	setIgnoreDatabase(p.getProperty("ignoredatabase"));
    	
    	for (String t_name : this.m_databases) 
    	{
    		String[] t_properties = p.getProperty(t_name).split("\\|");
    		this.m_hashDatabaseNames.put(t_name, t_properties[0]);
    		if(t_properties.length == 2){
    			this.m_hashDatabaseURL.put(t_name, t_properties[1]);
        	}
        }
    }

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUserEmailAddress(String userEmailAddress) {
		this.userEmailAddress = userEmailAddress;
	}

	public String getUserEmailAddress() {
		return userEmailAddress;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setIgnoreDatabase(String ignoreDatabase) {
		this.ignoreDatabase = ignoreDatabase;
	}

	public String getIgnoreDatabase() {
		return ignoreDatabase;
	}

	public boolean isIgnoreDatabase(String db) {
		if(db.equals(getIgnoreDatabase())){
			return true;
		}
		return false;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public String getOutXMLFilename() {
		return outXMLFilename;
	}

	public void setOutXMLFilename(String outXMLFilename) {
		this.outXMLFilename = outXMLFilename;
	}

	public void setCurrentFolder(String currentFolder) {
		this.currentFolder = currentFolder;
	}

	public String getCurrentFolder() {
		return currentFolder;
	}
} 
