package org.eurocarbdb.util.glycomedb;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.eurocarbdb.util.glycomedb.data.DataExport;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

public class GlycomeDBUpdateUtil {
	private String url;
	DataExport m_export = null;
	// new XML file name
	private String outXMLFilename="";
	//XML file with Full Path
	private String outXMLFileFullName="";
	//current working folder
	private String currentFolder = "";
	File folder = new File(currentFolder+".");
	String currentDir = new File(".").getAbsolutePath();
	
	/*
     *initial checking point if reading XML works or not
     */
	public GlycomeDBUpdateUtil(Configuration t_config) {
		currentFolder = t_config.getCurrentFolder();
		outXMLFilename = t_config.getOutXMLFilename();
		url = t_config.getUrl();
		outXMLFileFullName = currentFolder + t_config.getOutXMLFilename();
	}
    /*
     *download GZ and decompress xml and then parse xml 
     */
	public void performUpdate() throws JiBXException, FileNotFoundException{
		fileExistTest();
		downloadAndDecompress();
		parseXML();
	}
	/*
     *check if previous file exist or not, if it is, then delete old XML file. 
     */
	public void fileExistTest(){
		File[] listOfFiles = folder.listFiles();
	    for (int i = 0; i < listOfFiles.length; i++) {
	    	//delete old file in the same name as current file
	    	if(listOfFiles[i].getName().contains(outXMLFilename)){
	    		listOfFiles[i].delete();
	    	}
	    }
	}
	/*
     *download GZ and decompress xml 
     */
	public void downloadAndDecompress(){
		try {
			BufferedInputStream in = new java.io.BufferedInputStream(new URL(url).openStream());
			byte[] buf = new byte[4*1024]; //read 4 bytes
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int bytesRead;
			while ((bytesRead = in.read(buf)) != -1) {
				out.write(buf, 0, bytesRead);
			}
			in.close();
			out.close(); 
			
			byte[] byteArray= out.toByteArray();
			ByteArrayInputStream Byteop= new ByteArrayInputStream(byteArray); 
			GZIPInputStream gzipInputStream = new GZIPInputStream(Byteop);
			OutputStream out2 = new FileOutputStream(outXMLFileFullName);
		    byte[] buf2 = new byte[4*1024];  
			int bytesRead2;    
		    while ((bytesRead2 = gzipInputStream.read(buf2)) > 0) {
				          out2.write(buf2, 0, bytesRead2);
			}
		    gzipInputStream.close();
			out2.close();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
     *parse xml using jbix
     */
	public void parseXML() throws JiBXException{
		 IBindingFactory bfact = BindingDirectory.getFactory(DataExport.class);
		 IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
		 try {
			 m_export = (DataExport)uctx.unmarshalDocument (new FileInputStream(outXMLFileFullName), null);
		 } catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
     * getting export data object from parsed XML usgin jbix 
     */
	public DataExport getDataExport(){
		return m_export;
	}
}	
		
			
