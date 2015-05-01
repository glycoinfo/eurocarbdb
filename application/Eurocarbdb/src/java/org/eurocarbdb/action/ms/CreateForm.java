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
 *   Last commit: $Rev: 1870 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-02-22 #$  
 */

package org.eurocarbdb.action.ms;


import org.eurocarbdb.tranche.*;
import org.eurocarbdb.action.*;
import org.eurocarbdb.action.exception.*;

import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;

import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.core.Evidence.Type;
import org.eurocarbdb.dataaccess.hibernate.*;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

import org.hibernate.*; 
import org.hibernate.criterion.*; 
import org.systemsbiology.jrap.DataProcessingInfo;
import org.systemsbiology.jrap.MSInstrumentInfo;
import org.systemsbiology.jrap.MSXMLParser;
import org.systemsbiology.jrap.SoftwareInfo;
import org.systemsbiology.jrap.Scan;
import org.eurocarbdb.dataaccess.hibernate.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import org.apache.commons.io.*;
import org.apache.log4j.Logger;
import java.io.File;
import org.eurocarbdb.action.ms.CreateAcquisition;


/**
 *   @author             Haseeb Yousaf
 *   @version            $Rev: 1955 $
 * */
public class CreateForm extends AbstractMsAction implements RequiresLogin, EditingAction
{
	protected static final Logger log = Logger.getLogger( CreateForm.class.getName() );

	//private static final long serialVersionUID = 1L;
	// private static final String TMP_DIR_PATH = "/webapps/eurocarb/temp";
	// private File tmpDir;
	// private static final String DESTINATION_DIR_PATH ="/webapps/eurocarb/files";
	// private File destinationDir;

	private File temp_file = null;
	private String acquisitionFileContentType = null;
	private String acquisitionFileFileName=null;  
	private String strMessage;
	private String msManufacturer;
	private String msurl;
	private String msModel;
	private String msIonization;
	private String msDetector;
	private String msMAnalyzer;
	private String mssoftware_types;
	private String mssoftware_name;
	private String mssoftware_version;
	private String dsoftware_types;
	private String dsoftware_name;
	private String dsoftware_version;
	private double dsoftware_cutoff;
	private String temp;
	private static boolean flag=false;
	private String fpath;
	private int mz;
	private int count;
	private int a = 0;
	private int persubstitutionId;
	private Acquisition acquisition= null;
	private AcquisitionToPersubstitution acquisition_to_persubstitution=null;
	private Evidence evidence= null;
	Software software = null;
	SoftwareType softwaretype = null;
	DataProcessing dp = null;
	private Double seq;
	private org.eurocarbdb.dataaccess.ms.Scan addscan;
	private org.eurocarbdb.dataaccess.ms.Scan parentscan;
	private MSXMLParser parse;
	private HashMap<Integer,Boolean> hash_scansid;
	private HashMap<Integer, org.eurocarbdb.dataaccess.ms.Scan> scan_map;
	private HashMap<Integer,org.systemsbiology.jrap.Scan> parentscan_map;
	private ScanToDataProcessing scantodp= null;
	private MsMsRelationship mmr = null;
	private String spot;
	private String filepath;
	private String state;
	private Properties contributer_prop;
	private String fdate;
	private Date cdate = new Date();
	public List<Acquisition> ownedAcquisitions = null;
	public String scansid = null;
	void setOwnedAcquisitions(List<Acquisition> ownedAcquisitions)
	{
		if(ownedAcquisitions == null)
			this.ownedAcquisitions = Collections.emptyList();
		else

			this.ownedAcquisitions = ownedAcquisitions;
	}
	List<Acquisition> getOwnedAcquisitions()
	{
		return ownedAcquisitions;

	}
	public void setCdate(Date cdate)
	{
		this.cdate = cdate;
	}
	public Date getCdate()
	{
		return this.cdate;
	}

	public void setFdate(String fdate)
	{
		this.fdate = fdate;
	}

	public String getFdate()
	{
		return this.fdate;
	}

	public void setState(String state)
	{
		this.state =state;
	}

	public String getState()
	{
		return this.state;
	}

	public void setScansid(String scansid)
	{
		this.scansid =scansid;
	}

	public String getScansid()
	{
		return this.scansid;
	}
	public String getSpot()
	{
		return this.spot;
	}

	public void setSpot(String spot)
	{
		this.spot = spot;
	}

	public String getFilepath()
	{
		return this.filepath;
	}

	public void setFilepath(String filepath)
	{
		this.filepath = filepath;
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
	public int getPersubstitutionId()
	{
		return this.persubstitutionId;

	}

	public void setPersubstitutionId(int persubstitutionId)
	{
		this.persubstitutionId=persubstitutionId;
	}

	public int getMz()
	{
		return this.mz;

	}

	public void setMz(int mz)
	{
		this.mz=mz;
	}

	public Double getSeq()
	{
		return seq;
	}

	public void setSeq(Double seq)
	{
		this.seq = seq;
	}

	public String getMessage()
	{
		return strMessage;
	}

	public void setMessage(String strMessage)
	{
		this.strMessage = strMessage;
	}

	public void setFpath(String fpath)
	{
		this.fpath=fpath;
	}

	public String getFpath()
	{
		return this.fpath;
	}

	public String getMsManufacturer() {
		return msManufacturer;
	}

	public void setMsManufacturer(String msManufacturer) {
		this.msManufacturer = msManufacturer ;
	}

	public String getMsurl() {
		return msurl;
	}

	public void setMsurl(String msurl) {
		this.msurl = msurl;
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
	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp= temp;
	}

	public boolean getFlag()
	{
		return CreateForm.flag;
	}
	public void setFlag(boolean flag)
	{
		CreateForm.flag = flag;
	}

	public int getCount()
	{
		return this.count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public int getA()
	{
		return a;
	}

	public void setA(int a)
	{
		this.a = a;
	}
	public void checkPermissions() throws InsufficientPermissions {
		//        if (glycanSequenceContext != null && ! getGlycanSequenceContext().getBiologicalContext().getContributor().equals(getContributor())) {
		//            throw new InsufficientPermissions(this,"Biological context does not belong to logged in user");
		//        }
		// throw new InsufficientPermissions(this,"Biological context does not belong to logged in user");
	}
	public String execute() throws Exception 
	{      
		contributer_prop = new Properties();
		contributer_prop.load(CreateForm.class.getResourceAsStream("/contributer.properties"));
		
		String ms_temp_path= contributer_prop.getProperty("ms_temp_upload");
	
		File ms_temp = new File(ms_temp_path + this.getFpath());
		parse= new MSXMLParser(ms_temp.getAbsolutePath());
	    
		Manufacturer manufacture = null;
		manufacture = Manufacturer.getByName(this.msManufacturer);
		if (!(manufacture == null)){
			//System.out.println("URI:::"+manufacture.getUrl());}
			if (!(manufacture.getUrl() == null))
			{
				this.setMsurl(manufacture.getUrl());
			}

		}
		System.out.println("Check constraints applying:"+getMsurl());

		if (state.equalsIgnoreCase("no") & count == 0)
		{
			
			this.setCount(parse.getScanCount());	
			return "review";
		}
		else if(state.equals("yes")) 
		{
			setState("no");
			return INPUT;
		}
//		else if (this.dsoftware_name == null && this.dsoftware_version==null){
//		    addActionError("Data Processing Software name and version cannot be null");	
//		    setState("no");
//		    return INPUT;
//		}    
//		//Manufacturer manufacture = null;
//		else if (this.dsoftware_name.equalsIgnoreCase(null) && state.equals("yes"))
//		{
//			if (dsoftware_name.trim().length() == 0)
//	    	setState("no");
//	    	addActionError("Please input the Data Processing Name");
//		    return INPUT;
//			
//		}
		else if (state.equalsIgnoreCase("no")& count != 0)
		{ 
			setState("no");


			if(! addAcquisition(manufacture))
			{
				addActionError("Error in handling acquisition");
				return ERROR;
			}

			acquisitionToPersubstitution();
			setOwnedAcquisitions(Acquisition.ownedAcquisitions());
		}


		// create a new folder


		String ms_final_path = contributer_prop.getProperty("ms_final_upload");
		
		//temp_file = new File(fpath);
		boolean status = new File(ms_final_path+getCurrentContributor().getContributorId()).mkdir();
		File ms_final = new File(ms_final_path+getCurrentContributor().getContributorId()+File.separator +acquisition.getAcquisitionId());
		ms_final.createNewFile();
		FileUtils.copyFile(ms_temp, ms_final);
		boolean del_temp = ms_temp.delete();
		setState("yes");
		setCdate(new Date());
		return "form";

	}

	// adding a new acquisition 
	private boolean addAcquisition(Manufacturer manufacture)
	{
	
		if (manufacture == null)
		{
			manufacture = new Manufacturer();
			manufacture.setName(this.msManufacturer);
			manufacture.setUrl(this.msurl);
			Eurocarb.getEntityManager().store(manufacture);
		}// end if
		Device device = null;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try
		{

			device = Device.getByModelAndManufacturer(this.msModel,manufacture.getManufacturerId());

			if (device == null)
			{
				device = new Device();
				device.setModel(msModel);
				device.setManufacturer(manufacture);
				device.setIonisationType(this.msIonization);
				Eurocarb.getEntityManager().store(device);
			}


			acquisition = new Acquisition();
			acquisition.setTechnique((Technique)Technique.lookupAbbrev("ms"));
			acquisition.setContributor(getCurrentContributor());
			acquisition.setEvidenceType(Type.MS);
			acquisition.setDateEntered(new Date());
			acquisition.setDevice(device);
			acquisition.setFilename(acquisitionFileFileName.replaceAll(".mzXML", ""));
			acquisition.setFiletype(temp);
			acquisition.setDateObtained(df.parse(this.getFdate()));
			acquisition.setContributorQuality(this.seq);
			Eurocarb.getEntityManager().store(acquisition);

			//    if(!AcquistionToPersubstitution())
			//    {
			//    	   addActionError("Error in Adding Acquisition To Persubstitution Table");
			//    	   return false;
			//    }
			if(!addScan(parse))
			{
				addActionError("Error in handling acquisition");
				return false;
			}
		}catch(Exception e)
		{
			addActionError(e.getMessage());

			return false;
		}
		return true;
	}

	// Adding a new Scan
	private boolean addScan(MSXMLParser parser)
	{ 
		float peak_intensity;
		try
		{
			boolean x= true;
			float [][] peakI =null;
			int cnt = parser.getScanCount(); 
			org.systemsbiology.jrap.Scan p_scan;
			scan_map = new HashMap<Integer, org.eurocarbdb.dataaccess.ms.Scan>();
			parentscan_map = new HashMap<Integer,org.systemsbiology.jrap.Scan>();
			for (int i=1;i <=cnt;i++)
			{               
				addscan = new org.eurocarbdb.dataaccess.ms.Scan();
				org.systemsbiology.jrap.Scan _scan = parser.rap(i);
				//System.out.println(1);
				if (_scan.getMsLevel()==1)
				{ 
					System.out.println(_scan.getPrecursorMz());
					addscan.setAcquisition(acquisition);
					addscan.setMsExponent(_scan.getMsLevel());
					String a = _scan.getPolarity();
					if (a .equals("+") )
					{
						x = true;
					}
					else
					{
						x = false;
					}



					addscan.setPolarity(x);
					addscan.setStartMz((double)_scan.getLowMz());
					addscan.setEndMz((double)_scan.getHighMz());
					addscan.setContributorQuality(this.seq);
					addscan.setOriginalScanId(_scan.getNum());
					scan_map.put(_scan.getMsLevel(), addscan);
					parentscan_map.put(_scan.getMsLevel(),_scan);
					Eurocarb.getEntityManager().store(addscan);
					addStoDP();
				}


				else 
				{
					p_scan = parentscan_map.get(_scan.getMsLevel()-1);
					parentscan = scan_map.get(_scan.getMsLevel()-1);
					addscan.setAcquisition(acquisition);
					addscan.setMsExponent(_scan.getMsLevel());
					System.out.println(p_scan.getNum());
					System.out.println(_scan.getNum());
					peakI = _scan.getMassIntensityList();
					float preMZ=_scan.getPrecursorMz();
					//     System.out.println("PijRE:::"+preMZ);


					peak_intensity = getIntensity(peakI,preMZ);					   
					String a = _scan.getPolarity();
					if (a .equals("+") )
					{
						x = true;
						addscan.setPolarity(x);
					}
					else
					{
						x = false;
						addscan.setPolarity(x);
					}

					addscan.setPolarity(x);
					addscan.setStartMz((double)_scan.getLowMz()); 
					addscan.setEndMz((double)_scan.getHighMz());
					addscan.setContributorQuality(this.seq);
					addscan.setOriginalScanId(_scan.getNum());
					scan_map.put(_scan.getMsLevel(), addscan);
					parentscan_map.put(_scan.getMsLevel(),_scan);

					//System.out.println("HHH:::"+addscan);
					Eurocarb.getEntityManager().store(addscan);
					if (!addStoDP())
					{
						addActionError("Error in Scan to Data Processing Table");
						return false;
					}// end if
					if (!addMMR(_scan,peak_intensity))
					{
						addActionError("Error in MsMsRelation table");
						return false;

					}


				} //end else      
			} //end for
		}// end try

		catch(Exception e)	
		{
			//addActionError("Error in filling scan: " + addscan.getAcquisition().getAcquisitionId());
			addActionError("Error in filling scan: " + e.getMessage());
			return false;
		}
		return true;
	}
	private boolean addStoDP()
	{
		Software d_software =null;
		SoftwareType d_softwaretype=null;
		DataProcessing d_p = null;
		ScanToDataProcessing d_scantodp = null;
		boolean spot_int = true; 
		//   Software d_software= null;
		try
		{
			software = Software.getByNameAndVersion(this.mssoftware_name,this.mssoftware_version);
			softwaretype = SoftwareType.getByType(this.mssoftware_types);
			d_software = Software.getByNameAndVersion(this.dsoftware_name,this.dsoftware_version);
			d_softwaretype = SoftwareType.getByType(this.dsoftware_types);
			// if the software name doesnt exist create a new software
			if (software == null)
			{
				software = new Software();
				software.setName(this.mssoftware_name);
				software.setSoftwareVersion(this.mssoftware_version);
				Eurocarb.getEntityManager().store(software);

			}

			d_software = Software.getByNameAndVersion(this.dsoftware_name,this.dsoftware_version);
			if (d_software == null)
			{
				d_software = new Software();
				d_software.setName(this.dsoftware_name);
				d_software.setSoftwareVersion(this.dsoftware_version);
				Eurocarb.getEntityManager().store(d_software);

			}
			//if softwaretype does not exist create a new SoftwareType object
			if (softwaretype == null)
			{

				softwaretype = new SoftwareType();
				softwaretype.setSoftwareType(mssoftware_types);
				Eurocarb.getEntityManager().store(softwaretype);

			}
			d_softwaretype = SoftwareType.getByType(this.dsoftware_types);
			if (d_softwaretype == null)
			{

				d_softwaretype = new SoftwareType();
				d_softwaretype.setSoftwareType(dsoftware_types);
				Eurocarb.getEntityManager().store(d_softwaretype);
			}
			dp = DataProcessing.getBySoftwareTypeIdAndSoftwareId(this.softwaretype.getSoftwareTypeId(), software.getSoftwareId());

			// if DataProcessing is empty then create a new Dataprocessing object
			if(dp == null)
			{


				// New Data Processng Insertion
				dp = new DataProcessing();
				dp.setIntensityCutoff(0.0);
				dp.setSoftware(software);
				dp.setSoftwareType(softwaretype);
				dp.setFormat(temp);

				Eurocarb.getEntityManager().store(dp);


			}

			d_p = DataProcessing.getBySoftwareTypeIdAndSoftwareId(d_softwaretype.getSoftwareTypeId(), d_software.getSoftwareId());

			// if DataProcessing is empty then create a new Dataprocessing object
			if(d_p == null)
			{


				// New Data Processng Insertion
				d_p = new DataProcessing();
				d_p.setIntensityCutoff(0.0);
				d_p.setSoftware(d_software);
				d_p.setSoftwareType(d_softwaretype);
				d_p.setFormat(temp);

				Eurocarb.getEntityManager().store(d_p);


			}

			// Scan to Data Processing table      
			scantodp = new ScanToDataProcessing();
			scantodp.setScan(addscan);
			scantodp.setDataProcessing(dp);
			scantodp.setSoftwareOrder(1);
			if (spot.equals("true"))
			{
				spot_int = true;
			}
			else{
				spot_int= false;
			}
			scantodp.setSpotIntegration(spot_int);
			Eurocarb.getEntityManager().store(scantodp);

			//Second scan to data processing
			d_scantodp = new ScanToDataProcessing();
			d_scantodp.setScan(addscan);
			d_scantodp.setDataProcessing(d_p);
			d_scantodp.setSoftwareOrder(2);
			if (spot.equals("true"))
			{
				spot_int = true;
			}
			else{
				spot_int= false;
			}
			d_scantodp.setSpotIntegration(spot_int);
			Eurocarb.getEntityManager().store(d_scantodp);
		}//end try
		catch(Exception e)
		{
			addActionError("The Data Processing to Scan table cannot be made "+ e.getMessage());
			return false;
		}
		return true;
	}
	// adding MS MS table
	private boolean addMMR(org.systemsbiology.jrap.Scan _scan, float pre_mz)
	{
		try 		{
			mmr = new MsMsRelationship();
			mmr.setScanByParentId(parentscan);
			mmr.setScanByScanId(addscan);
			mmr.setPrecursorMz((double)_scan.getPrecursorMz());
			mmr.setPrecursorIntensity((double)pre_mz);
			mmr.setMsMsMethode("CID");
			mmr.setPrecursorCharge(_scan.getPrecursorCharge());
			Eurocarb.getEntityManager().store(mmr);
		}
		catch(Exception e)
		{
			addActionError("Unable to add Ms Ms Relation Table "+e.getMessage());
			return false;
		}
		return true;
	}// end method

	// method for adding acquisition to persubstitution table
	private void acquisitionToPersubstitution()
	{

		acquisition_to_persubstitution = new AcquisitionToPersubstitution();
		acquisition_to_persubstitution.setAcquisition(this.acquisition);
		acquisition_to_persubstitution.setPersubstitution(Persubstitution.lookupPid(persubstitutionId));
		Eurocarb.getEntityManager().store(acquisition_to_persubstitution);


	}// end method

	// get the closest Intensity

	private float getIntensity(float[][] peak, float preMz)
	{
		float closest_intensity= peak[1][0];

		try
		{
			float closestMz = Math.abs(peak[0][0] - preMz);
			for (int i = 1; i <peak[0].length-1; i++) 
			{
				float currentMz= Math.abs(peak[0][i]- preMz);

				if (currentMz < closestMz)
				{
					closest_intensity = peak[1][i];
					closestMz=currentMz;
				} // end if
				else 
				{
					return closest_intensity;  
				} // end else if
			}// end for
			return closest_intensity ;
		}// end try

		catch (Exception e)
		{
			addActionError(" Unable to get the Current Mz");
		}
		return closest_intensity;
	}

} // end class


