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
 *   Last commit: $Rev: 1996 $ by $Author: hasysf@gmail.com $ on $Date:: 2010-11-06 #$  
 */

package org.eurocarbdb.action.ms;

import org.eurocarbdb.tranche.*;
import org.eurocarbdb.action.*;
import org.eurocarbdb.action.exception.*;

import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.hibernate.*;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

import org.hibernate.*; 
import org.hibernate.criterion.*; 
import org.eurocarbdb.dataaccess.hibernate.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

import org.apache.commons.io.*;
import org.apache.log4j.Logger;

import  org.systemsbiology.jrap.*;
import org.systemsbiology.jrap.Scan;

/**
 *   @author             Haseeb Yousaf
 *   @version            $Rev: 1996 $
 */
public class CreateAcquisition extends AbstractMsAction implements RequiresLogin, EditingAction
{

	protected static final Logger log = Logger.getLogger( CreateAcquisition.class.getName() );


	private File acquisitionFile = null;
	private String acquisitionFileContentType = null;
	private String acquisitionFileFileName= null;  
	private Acquisition acquisition = null;       
	private ExperimentStep experiment_step = null;
	private GlycanSequenceContext glycanSequenceContext = null;
	private String msManufacturer;
	private String msurl;
	private String msModel;
	private String msIonization;
	private String msDetector;
	private String msMAnalyzer;
	private String mssoftware_types;
	private String mssoftware_name;
	private String mssoftware_version;
	private String dsoftware_types;;
	private String dsoftware_name;
	private String dsoftware_version;
	private double dsoftware_cutoff;
	private String scannum;
	private String scanlevel;
	private String peakscount;
	private String polarity;
	private float lowmz;
	private float highmz;
	private String precursorIntensity;
	private String precursorChange;
	private String activationMethod;
	private String precursorval;
	private Manufacturer manufacture = null;
	private Device device = null;
	private File tempFile = null;
	private boolean val = false;
	private String fpath;
	private File tfile;
	private int persubstitutionId;
	private List<Persubstitution> persubstitutions = null;
	private String filepath;
	private Properties prop;
	private int scans;
	private String submit;
	private String state = "yes";
	private Date date_obtained;
	private String fdate;

	public void setFdate(String fdate)
	{
		this.fdate = fdate;
	}

	public String getFdate()
	{
		return this.fdate;
	}

	public List<Persubstitution> getPersubstitutions()
	{
		return Persubstitution.getPersubstitution();
	}

	public void setState(String state)
	{
		this.state =state;
	}

	public String getState()
	{
		return this.state;
	}

	public int getPersubstitutionId()
	{
		return this.persubstitutionId;  
	}

	public void setPersubstitutionId(int persubstitutionId)
	{
		this.persubstitutionId=persubstitutionId;
	}

	public void setScans(int scans)
	{
		this.scans = scans;
	}

	public int getScans()
	{
		return this.scans;
	}

	public String getMsManufacturer() {
		return msManufacturer;
	}

	public void setMsManufacturer(String msManufacturer) {
		this.msManufacturer = msManufacturer;
	}

	public String getMsurl() {
		return msurl;
	}

	public void setMsurl(String msurl) {
		this.msurl = msurl;
	}

	public String getSubmit() {
		return submit;
	}

	public void setSubmit(String submit) {
		this.submit = submit;
	}
	public String getMsModel() {
		return msModel;
	}

	public void setMsModel(String msModel) {
		this.msModel = msModel;
	}

	public String getMsIonization() {
		return msIonization;
	}

	public void setMsIonization(String msIonization) {
		this.msIonization = msIonization;
	}

	public String getMssoftware_types() {
		return mssoftware_types;
	}

	public void setMssoftware_types(String mssoftware_types) {
		this.mssoftware_types = mssoftware_types;
	}

	public String getMssoftware_name() {
		return mssoftware_name;
	}

	public void setMssoftware_name(String mssoftware_name) {
		this.mssoftware_name = mssoftware_name;
	}

	public String getMssoftware_version() {
		return mssoftware_version;
	}

	public void setMssoftware_version(String mssoftware_version) {
		this.mssoftware_version = mssoftware_version;
	}

	public String getDsoftware_types() {
		return dsoftware_types;
	}

	public void setDsoftware_types(String dsoftware_types) {
		this.dsoftware_types = dsoftware_types;
	}

	public String getDsoftware_name() {
		return dsoftware_name;
	}

	public void setDsoftware_name(String dsoftware_name) {
		this.dsoftware_name = dsoftware_name;
	}

	public String getDsoftware_version() {
		return dsoftware_version;
	}

	public void setDsoftware_cutoff(double dsoftware_cutoff) {
		this.dsoftware_cutoff = dsoftware_cutoff;
	}

	public double getDsoftware_cutoff() {
		return dsoftware_cutoff;
	}

	public void setDsoftware_version(String dsoftware_version) {
		this.dsoftware_version = dsoftware_version;
	}

	public String getScannum() {
		return scannum;
	}

	public void setScannum(String scannum) {
		this.scannum = scannum;
	}

	public String getScanlevel() {
		return scanlevel;
	}

	public void setScanlevel(String scanlevel) {
		this.scanlevel = scanlevel;
	}

	public String getPeakscount() {
		return peakscount;
	}

	public void setPeakscount(String peakscount) {
		this.peakscount = peakscount;
	}

	public String getPolarity() {
		return polarity;
	}

	public void setPolarity(String polarity) {
		this.polarity = polarity;
	}

	public float getLowmz() {
		return lowmz;
	}

	public void setLowmz(float lowmz) {
		this.lowmz = lowmz;
	}

	public float getHighmz() {
		return highmz;
	}

	public void setHighmz(float highmz) {
		this.highmz = highmz;
	}

	public String getPrecursorIntensity() {
		return precursorIntensity;
	}

	public void setPrecursorIntensity(String precursorIntensity) {
		this.precursorIntensity = precursorIntensity;
	}

	public String getPrecursorChange() {
		return precursorChange;
	}

	public void setPrecursorChange(String precursorChange) {
		this.precursorChange = precursorChange;
	}

	public String getActivationMethod() {
		return activationMethod;
	}

	public void setActivationMethod(String activationMethod) {
		this.activationMethod = activationMethod;
	}

	public String getPrecursorval() {
		return precursorval;
	}

	public void setPrecursorval(String precursorval) {
		this.precursorval = precursorval;
	}

	//  private List<Persubstitution> persubstitution = null; // this is from the persubstitution in the hibernate class 
	// output message
	private String strMessage ="helo";

	//  private String strMessage = "";


	public File getAcquisitionFile() 
	{
		return this.acquisitionFile;
	}

	public void setAcquisitionFile(File file) 
	{
		this.acquisitionFile = file;
	}

	public String getAcquisitionFileContentType() 
	{
		return this.acquisitionFileContentType;
	}

	public void setAcquisitionFileContentType(String contentType) 
	{
		this.acquisitionFileContentType = contentType;
	}

	public String getAcquisitionFileFileName() 
	{
		return this.acquisitionFileFileName;
	}

	public void setAcquisitionFileFileName(String filename) 
	{
		this.acquisitionFileFileName = filename;
	}        
	public String getMessage()
	{
		return strMessage;
	}

	public void setMessage(String strMessage)
	{
		this.strMessage = strMessage;
	}

	public void setExperimentStep(ExperimentStep exp_step) {
		experiment_step = exp_step;
	}

	public ExperimentStep getExperimentStep() {
		return experiment_step;
	}   

	public void setFpath(String fpath)
	{
		this.fpath=fpath;
	}

	public void setTfile(File tfile)
	{
		this.tfile=tfile;
	}

	public File getTfile()
	{
		return this.tfile;
	}

	public String getFpath()
	{
		return this.fpath;
	}

	public String getFilepath()
	{
		return this.filepath;
	}

	public void setFilepath(String filepath)
	{
		this.filepath = filepath;
	}

	/**
	 *  Get accessor for glycanSequenceContext
	 *  Description
	 */



	/**
	 * Check permissions for the parent biological context
	 * Why did we need a bc to belong to a contributor, assuming this has something to do with retrieval methods
	 */
	public void checkPermissions() throws InsufficientPermissions {
		//        if (glycanSequenceContext != null && ! getGlycanSequenceContext().getBiologicalContext().getContributor().equals(getContributor())) {
		//            throw new InsufficientPermissions(this,"Biological context does not belong to logged in user");
		//        }
		// throw new InsufficientPermissions(this,"Biological context does not belong to logged in user");
	}



	public String execute() throws Exception 
	{       
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		//    this.setPersubstitution(Persubstitution.getPersubstitution());
		if ( acquisitionFile ==null)
		{
			addActionError("Please Select a File");
			return INPUT;
		}
		if (!this.acquisitionFileFileName.endsWith(".mzXML"))
		{
			addActionError("Please Upload the File of type mzXML");
			return INPUT;

		}
		this.setFdate(df.format(this.getDate_obtained()));


		this.setState(this.getState());	
		//CreateAcquisition.class.getClassLoader().
		prop= new Properties();
		prop.load(CreateAcquisition.class.getResourceAsStream("/contributer.properties")); 

		//	tfile = copyFiles(acquisitionFile,prop);
		Random rand = new Random();
		int randomInt = rand.nextInt(1000000000);

		String path = prop.getProperty("ms_temp_upload");
		//   FileUtils t_file = new FileUtils();

		File ms_temp = new File(path+File.separator +"temp"+randomInt+".tmp");
		this.setFpath(ms_temp.getName());

		if(!ms_temp.getParentFile().exists())
		{
			addActionError("file is not exist "+ ms_temp.getName()+" path: " + path);
			return ERROR;
		}
		FileUtils.copyFile(acquisitionFile, ms_temp);
		MSXMLParser parser = new MSXMLParser(acquisitionFile.getAbsolutePath());

		//    	System.out.println("ACQ:::"+acquisitionFile.getParent()+"/"+acquisitionFileFileName); 
		//        this.setFilepath(acquisitionFile.getParent()+"/"+acquisitionFileFileName);
		//     System.out.println("PROP:::"+prop.getProperty("ms_temp_upload"));


		// read a scan from the file
		MSInstrumentInfo ins= parser.getHeaderInfo().getInstrumentInfo(); // get the acquisition file info
		DataProcessingInfo dp = parser.getHeaderInfo().getDataProcessing(); // get file generator info
		SoftwareInfo soft = ins.getSoftwareInfo(); // get software info
		SoftwareInfo[] dsoft = dp.getSoftwareUsed();  // get file software info
		// parseScans(parser);


		// this.setLowmz(scans.getLowMz());
		//this.setHighmz(sca                       ns.getHighMz());
		this.setMsModel(ins.getModel());
		this.setMsManufacturer(ins.getManufacturer());
		this.setMsIonization(ins.getIonization());
		this.setMssoftware_name(soft.name);
		this.setMssoftware_types(soft.type);
		this.setMssoftware_version(soft.version);
		this.setDsoftware_cutoff(dp.getIntensityCutoff());

		for ( SoftwareInfo so : dsoft)
		{
			this.setDsoftware_name(so.name);
			this.setDsoftware_types(so.type);
			this.setDsoftware_version(so.version);

		}

		//          
		return SUCCESS;

	}

	public void setDate_obtained(Date date_obtained) {
		//this.date_obtained = date_obtained;
		this.date_obtained= date_obtained;
	}

	public Date getDate_obtained() {
		return date_obtained;
	}      


	
} // end class
