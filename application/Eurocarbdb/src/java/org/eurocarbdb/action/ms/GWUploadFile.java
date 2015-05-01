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
*   Last commit: $Rev: 1924 $ by $Author: khaleefah $ on $Date:: 2010-06-20 #$  
*/

package org.eurocarbdb.action.ms;

import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.tranche.*;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.action.*;
import org.eurocarbdb.action.exception.*;

import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.hibernate.*;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

import org.hibernate.*; 
import org.hibernate.criterion.*; 
import org.eurocarbdb.dataaccess.hibernate.*;

import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import org.apache.commons.io.*;
import org.apache.log4j.Logger;
// import org.apache.taglibs.standard.lang.jpath.adapter.ConversionException;
// import org.apache.taglibs.standard.lang.jpath.adapter.Convert;

/**
*   @author             Khalifeh Al Jadda
*   @version            $Rev: 1924 $
*/
@SuppressWarnings("unchecked")
public class GWUploadFile extends AbstractMsAction implements RequiresLogin, EditingAction
{
	
	//List<String> peakProcessingTypes;
	protected int acquisitionId = -1;
	private File gFile = null;
	private String gFileFileName = null;
	private String gFileContentType = null;
	private Acquisition acquisition = null;
	Vector<org.eurocarbdb.application.glycoworkbench.Scan> scans;
	private ArrayList<PeakList> peaklist = new ArrayList();
	private PeakAnnotated peakAnnotated = null;
	private PeakLabeled peakLabeled = null;
	private PeakProcessing peakprocessing = null;
	private PeakListToDataProcessing peakListToDataProcessing= null;
	private boolean ChargeDeconvoluted = true;
	private boolean Deisotoped = true;
	private String peakProcessingType;
	private Double peakListContributorQuality = 5d;
	private Double peakAnnotatedContributorQuality = 5d;
	private GlycanWorkspace gwSpace = new GlycanWorkspace();
	private PeakList pl = null;
	private MsMsRelationship r = null;
	private Map gSequenceToPeakAnnotated = new HashMap();
	private Map ScanToGSequenceMap = new HashMap();
	private Map peakToParentGlycoCTId = new HashMap();
	private Map structures = new HashMap();
	private ArrayList<Scan> allMsScans = new ArrayList<Scan>();
	private Map ionsMap = new HashMap();
	private Date dateEntered = new Date();
	private boolean withRoot = true;
	
	protected static final Logger log = Logger.getLogger( GWUploadFile.class.getName() );
	
	// output message
    private String strMessage = "";

    public String getMessage()
    {
        return strMessage;
    }

    public void setMessage(String strMessage)
    {
        this.strMessage = strMessage;
    }

	@Override
	public void checkPermissions() throws InsufficientPermissions {
		// TODO Auto-generated method stub
		
	}
	
	//getters and setters
	public void setGFile(File gwFile)
	{
		this.gFile = gwFile;
	}
	public File getGFile()
	{
		return gFile;
	}
	public void setGFileFileName(String filename)
	{
		this.gFileFileName = filename;
	}
	public String getGFileFileName()
	{
		return gFileFileName;
	}
	public void setGFileContentType(String contentType)
	{
		this.gFileContentType = contentType;
	}
	public String getGFileContentType()
	{
		return gFileContentType;
	}
	
	
	public void setAcquisition(Acquisition acquisition)
	{
		
		this.acquisition = acquisition;
	}
	public Acquisition getAcquisition()
	{
		return acquisition;
	}
	public void setPeakList(ArrayList<PeakList> peakList)
	{
		this.peaklist = peakList;
	}
	public ArrayList<PeakList> getPeakList()
	{
		return this.peaklist;
	}
	public void setPeakAnnotated(PeakAnnotated peakannotated)
	{
		this.peakAnnotated = peakannotated;
	}
	public PeakAnnotated getPeakAnnotated()
	{
		return this.peakAnnotated;
	}
	public void setAcquisitionId(int acquisitionId)
	{
		if(Acquisition.lookupById(acquisitionId) != null)
		this.acquisitionId = acquisitionId;
		else
			this.acquisitionId = -1;
	}
	public int getAcquisitionId()
	{
		return acquisitionId;
	}
	
	
	
//	public void setPeakProcessingTypes(List<String> peakProcessingTypes)
//	{
//		this.peakProcessingTypes = peakProcessingTypes; 
//	}
//	public List<String> getPeakProcessingTypes()
//	{
//		return this.peakProcessingTypes;
//	}
//	public boolean openGWFile()
//	{
//		if( gwSpace.open(gwFile,true, true))
//		{
//		  //peaklist.
//		}
//			
//	}
	
	@SuppressWarnings("deprecation")
	public String execute() throws Exception 
    {   
		
		if(gFile == null )
		{
//			setPeakProcessingTypes(PeakProcessing.getAllPeakProcessingTypes());
			addActionError("Please select a file");
			return ERROR;	
		}
////		if(gFileContentType!="gwp") 
////		{
////			addActionError("Please selet a glycoworkhbench file with file type gwp");
////			return ERROR;
////		}
////		Open the GW file and extract it's content to glycanWorkspace object
////		FileReader reader=new FileReader(gFile);
////		BufferedReader br=new BufferedReader(reader);
//		
//		
//		FileInputStream fstream = new FileInputStream(gFile);
//	  //   Get the object of DataInputStream
//	  //  BufferedInputStream bis = new BufferedInputStream(fstream);
//	    DataInputStream in = new DataInputStream(fstream);
//	 //   FileReader fr = new FileReader(gFile);
//	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
//	    StringBuilder ret = new StringBuilder();
//	    int ch;
//	    while( (ch = br.read())!= Integer.parseInt("\n") ) 
//	        ret.appendCodePoint(ch);
//	    String strLine = ret.toString();
//	    //Read File Line By Line
//	   // strLine = br.readLine();
//	   // strLine = br.readLine();
//	    if(strLine != "<GlycanWorkspace>")
//	    {
//	    	addActionError("**Can't open the file please check your file and try again** " + gFileFileName + " test");
//	    	//fstream.close();
//			return ERROR;
//	    }
		if(!gFileFileName.endsWith(".gwp"))
		{
			addActionError("Can't open the file please select gwp file");
			return ERROR;
		}
		if( ! gwSpace.open(gFile,true, true))
		{
			addActionError("Can't open the file please check your file and try again");
			return ERROR;
		}
//		// Open the file that is the first 
//	    // command line parameter
	    
		Eurocarb.getEntityManager().endUnitOfWork();
		Eurocarb.getEntityManager().beginUnitOfWork();
		if(!fillIonHashMap())
		{
			addActionError("Error in retrieving Ions");
			Eurocarb.getEntityManager().abortUnitOfWork();
		    Eurocarb.getEntityManager().beginUnitOfWork();
			return ERROR;
		}
		if(fillPeakList())
		{
			//Eurocarb.getEntityManager();
		//	for(PeakList peak : peaklist)
				Eurocarb.getEntityManager().endUnitOfWork();
				Eurocarb.getEntityManager().beginUnitOfWork();
				
			return SUCCESS;
		}
			
		else
		{
			addActionError("Erro in filling peaklist");
		    Eurocarb.getEntityManager().abortUnitOfWork();
		    Eurocarb.getEntityManager().beginUnitOfWork();
			return ERROR;
		}
		
//		fillPeakLabeled();
//		fillPeakAnnotated();
//		fillPeakAnnotatedToIon();
//		fillFragmentation();
			
		
		//setOwnedAcquisitions(Acquisition.ownedAcquisitions());
		//setOthersAcquisitions(Acquisition.othersAcquisitions());
		
		
    }
	private boolean fillIonHashMap()
	{
		
		String key = null;
	    List<Ion> allIons = Ion.getAll();
	    if(allIons != null)
	    for(Ion i : allIons)
	    {
	    	key = i.getIonType() + i.getCharge().toString() + i.getPositive().toString();
	    	ionsMap.put(key,i);
	    }
	    else
	    {
	    	addActionError("No ions retrieved");
	    	return false;
	    }
	    
	    return true;
	    	
	    	
	}

private boolean fillFragmentation(String type) {
	//scans.get(1).getAnnotatedPeakList().getAnnotations(1, 0).get(0).getFragmentEntry().getName();
	//String type = "^{1,4}A_{GlcNAc}YB^{12,5}X_{Man}C";
	Fragmentation fragmentation = null;
	if(type.contains("X") || type.contains("A"))
	{
		try
		{
		String[] parts = Pattern.compile("\\^").split(type);
		//Matcher m = Pattern.compile("\\^").matcher(type);
		//System.out.println(m.group());
		for(int i=0;i<parts.length;i++)
		    if(parts[i].startsWith("{"))
		    	processAX(parts[i]);
		    else
		    	processBCYZ(parts[i]);
	
		    }catch(Exception e){addActionError("Can't fill fragmentation table");
		     return false;}
	}
	else
		processBCYZ(type);
	
	return true;
		// TODO Auto-generated method stub
		
	}
public  void processBCYZ(String s)
{
	Fragmentation fragmentation = null;

	for(int j=0;j<s.length();j++)
	{
		String temp;
		// try {
			
		// temp = Convert.toString(s.charAt(j));
		temp = "" + s.charAt(j);
		fragmentation = new Fragmentation();
		fragmentation.setPeakAnnotated(peakAnnotated);
		fragmentation.setCleavageOne(null);
		fragmentation.setCleavageTwo(null);
		fragmentation.setFragmentPosition(null);
		fragmentation.setFragmentType(temp);
		fragmentation.setFragmentDc(temp);
		fragmentation.setFragmentAlt(temp);
		Eurocarb.getEntityManager().store(fragmentation);
		// } catch (ConversionException e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }
	}
}
public void processAX(String s)
{
	Fragmentation fragmentation = null;
	try {
		
		fragmentation = new Fragmentation();
		fragmentation.setPeakAnnotated(peakAnnotated);
		fragmentation.setCleavageOne(Integer.parseInt(s.substring(s.indexOf("{")+1, s.indexOf(","))));
		fragmentation.setCleavageTwo(Integer.parseInt(s.substring(s.indexOf(",") + 1, s.indexOf("}"))));
		fragmentation.setFragmentPosition(null);
		fragmentation.setFragmentType(s.substring(s.indexOf("}") + 1, s.indexOf("_")));
		fragmentation.setFragmentDc("^" + s.substring(s.indexOf("{"), s.lastIndexOf("}")+1));
		fragmentation.setFragmentAlt("^" + s.substring(s.indexOf("{"), s.lastIndexOf("}")+1));
		Eurocarb.getEntityManager().store(fragmentation);
		//if the second part is BCYZ
		String[] temp = Pattern.compile("_").split(s);
		String[] others = Pattern.compile("}").split(temp[1]);
		if(others.length>1)
			processBCYZ(others[1]);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}

private boolean fillPeakAnnotatedToIon(String fragment) {
	
	int number=1;
	String ionType = null;
	Ion ion = null;
    PeakAnnotatedToIon peakAnnotatedToIon = null;
    boolean flagTest = false;
    String[] ions = Pattern.compile(",").split(peakAnnotated.getSequenceGws());
    String key = null;
	//extract and check ions
    try{
    	
    	//processing of part1
    	String[] part1 =Pattern.compile("\\+|-").split(ions[ions.length-3]);
    	boolean positive = true;
    	if(ions[ions.length-3].indexOf("-")!=-1)
    		positive = false;
    	
    	for(int i = 0; i < part1.length ; i++)
    	{
    		String[] num = Pattern.compile("[A-Z][a-z]*").split(part1[i]);
    		number = 1;
    		ionType = part1[i];
    		if(num.length > 0)
    		{
    			 number = Integer.parseInt(num[0]); 
    			 ionType = part1[i].substring(num[0].length(), part1[i].length());
    			
    		}
    		//check if this ion exists in DB
    		
    		key = ionType + number + positive;
    		ion = (Ion)ionsMap.get(key);
    		
    		if(ion == null)
    		{
    			//insert a new ion into ion table
    			ion = new Ion();
    			ion.setAtomer(true);
    			ion.setCharge(number);
    			ion.setIonType(ionType);
    			ion.setPositive(positive);
    			Eurocarb.getEntityManager().store(ion);
    			//add this Ion to the ions hashmap
    			key = ionType + number + positive;
    			ionsMap.put(key,ion);
    		}
    		peakAnnotatedToIon = new PeakAnnotatedToIon();
    		peakAnnotatedToIon.setPeakAnnotated(peakAnnotated);
    		peakAnnotatedToIon.setIon(ion);
    		peakAnnotatedToIon.setNumber(number);
    		peakAnnotatedToIon.setGain(true);
    		Eurocarb.getEntityManager().store(peakAnnotatedToIon);
    	}
    	//processing of part 2
    	if(!ions[ions.length-2].equals("0"))
    	{
    	String[] part2 =Pattern.compile("\\+|-").split(ions[ions.length-2]);
    	flagTest = true;
    	boolean p2Positive = true, lastIonPositive = true, gain = true, lastGain=true;
    	if(ions[ions.length-2].indexOf("-")== 0)
    		p2Positive = false;
    	if(ions[ions.length-2].substring(ions[ions.length-2].indexOf(part2[part2.length-1])-1, ions[ions.length-2].indexOf(part2[part2.length-1])).equals("-"))
    		{
    		   lastIonPositive= false;
    		   lastGain = false; 
    		}
    	
    	for(int i = 0; i < part2.length ; i++)
    	{
    		String[] num = Pattern.compile("[A-Z][a-z]*").split(part2[i]);
    		number = 1;
    		ionType = part2[i];
    		//if this is the last ion then it might has different sign
    		if(i == part2.length-1)
    			{
    			   p2Positive = lastIonPositive;
    			   gain = lastGain;
    			}
    		
    		if(num.length > 0)
    		{
    			number= Integer.parseInt(num[0]);
    			ionType = part2[i].substring(num[0].length(), part2[i].length());
    			
    		}
    		//check if this ion exists in DB
    		key = ionType+number+p2Positive;
    		ion = (Ion)ionsMap.get(key);
    		
    		if(ion == null)
    		{
    			//insert a new ion into ion table
    			ion = new Ion();
    			ion.setAtomer(true);
    			ion.setCharge(number);
    			ion.setIonType(ionType);
    			ion.setPositive(p2Positive);
    			Eurocarb.getEntityManager().store(ion);
    		}
    		peakAnnotatedToIon = new PeakAnnotatedToIon();
    		peakAnnotatedToIon.setPeakAnnotated(peakAnnotated);
    		peakAnnotatedToIon.setIon(ion);
    		peakAnnotatedToIon.setNumber(number);
    		peakAnnotatedToIon.setGain(gain);
    		Eurocarb.getEntityManager().store(peakAnnotatedToIon);
    	
    		
    	}
    	}
    	
    }catch(Exception e){
    	addActionError("Can't fill Peak_Annotated_To_Ion");
    	addActionError("Ion = " + ion.getIonType() + " IonId = " + ion.getIonId()+" charge: " + ion.getCharge());
    	addActionError("peakAnnotatedToIon: " + peakAnnotatedToIon.getPeakAnnotatedToIonId());
    	addActionError(peakAnnotated.getSequenceGws());
    	addActionError("Fragment: " + fragment);
    	addActionError("flagTest: " + flagTest);
    	
    	return false;
    }
    return true;
	
	
//	System.out.println("Positive = " + p2Positive);
//	System.out.println("lastPositive = " + ions[8].substring(ions[8].indexOf(part2[part2.length-1])-1, ions[8].indexOf(part2[part2.length-1])));
//	System.out.println(lastIonPositive);
	
		// TODO Auto-generated method stub
		
	}
public boolean extractStructures(int peakListIndex)
{
	int size = scans.get(peakListIndex).getAnnotatedPeakList().getStructures().size();
	int fragmentSize = 0;
	String structure = null;
	GlycanSequence g = null;
	SugarSequence ss = null;
	//Date date = new Date();
	String abbr = null;
	Glycan fragment = null;
	ReducingEnd reducingEnd = null;
	Persubstitution persub = null;
	GlycanSequenceEvidence gste = null;
	int glycoCtId = 0;
	int i = 0,j = 0;
	List peakListStructures = new ArrayList();
	
	
	
	try
	{
		for(i=0;i<size;i++)
		{
			
			//check if the structure into the DB or not, if not add it
			structure = scans.get(peakListIndex).getStructures().getStructure(i).toString();
			//check if the structure is a fragment of previous structure
			if(peakToParentGlycoCTId.containsKey(structure))
				glycoCtId = (Integer) peakToParentGlycoCTId.get(structure);

			else
			{
				structure = scans.get(peakListIndex).getStructures().getStructure(i).toGlycoCTCondensed();
				ss = new SugarSequence(structure);
				//structure.getglyc
				g = GlycanSequence.lookupByExactSequenceString(structure);
				if(g == null)
				{
					g = GlycanSequence.lookupOrCreateNew(ss);
					Eurocarb.getEntityManager().store(g);
					
					gste = new GlycanSequenceEvidence(g,acquisition);
					gste.setContributor(getCurrentContributor());
					gste.setDateEntered(dateEntered);
					gste.setQuantitationByPercent(1.0);
					Eurocarb.getEntityManager().store(gste);
					
				}
				
				glycoCtId = g.getGlycanSequenceId();

				
			}
			
			peakListStructures.add(glycoCtId);
		}
		structures.put(peakListIndex, peakListStructures);
	}catch(Exception e){
		addActionError("Can't extract structure for peaklist number: " + peakListIndex);
		return false;
	}
			//****************************************************************************************
//			fragmentSize = scans.get(peakListIndex).getAnnotatedPeakList().getFragments(peakIndex, i).size();
//			//fragment = scans.get(peakListIndex).getFragments().getFragments(i).elementAt(peakIndex).fragment.toString();
//			if(fragmentSize>0)
//			{
//				for(j=0;j<fragmentSize;j++)
//				{
//					fragment = scans.get(peakListIndex).getAnnotatedPeakList().getFragments(peakIndex, i).get(j);
//					
//					//abbr = fragment.toString().substring(fragment.toString().lastIndexOf(",")+1, fragment.toString().length());
//					//Extracting and processing ReducingEnd
//					abbr = fragment.getMassOptions().getReducingEndTypeString();
//					reducingEnd = ReducingEnd.getReducingEndByAbbr(abbr);
//					if(reducingEnd==null)
//					{
//						reducingEnd = new ReducingEnd();
//						reducingEnd.setAbbreviation(abbr);
//						reducingEnd.setName(abbr);
//						reducingEnd.setUri(null);
//						Eurocarb.getEntityManager().store(reducingEnd);
//						
//					}
//					//extract the persubstitution abbreviation and then check if it's in the Database
//					persub = Persubstitution.getByAbbreviation(fragment.getDerivatization());
//					if(persub == null)
//					{
//						persub = new Persubstitution();
//						persub.setAbbreviation(fragment.getDerivatization());
//						persub.setName(fragment.getDerivatization());
//						Eurocarb.getEntityManager().store(persub);
//					}
	
	return true;
}
@SuppressWarnings("null")
private boolean fillPeakAnnotated(PeakLabeled peakLabeled,int peakListIndex, int peakIndex, List _structures) {
	//int size = scans.get(peakListIndex).getAnnotatedPeakList().getStructures().size();
	int size = 0;
	int fragmentSize = 0;
	String structure = null;
	GlycanSequence g = null;
	SugarSequence ss = null;
//	Date date = new Date();
	String abbr = null;
	Glycan fragment = null;
	ReducingEnd reducingEnd = null;
	Persubstitution persub = null;
	int glycoCtId = 0;
	int i = 0,j = 0;
	
	try
	{

//			
		   for(i = 0 ; i < _structures.size() ; i ++)
		   {
			fragmentSize = scans.get(peakListIndex).getAnnotatedPeakList().getFragments(peakIndex, i).size();
			//fragment = scans.get(peakListIndex).getFragments().getFragments(i).elementAt(peakIndex).fragment.toString();
			if(fragmentSize>0)
			{
				for(j=0;j<fragmentSize;j++)
				{
					fragment = scans.get(peakListIndex).getAnnotatedPeakList().getFragments(peakIndex, i).get(j);
					
					//abbr = fragment.toString().substring(fragment.toString().lastIndexOf(",")+1, fragment.toString().length());
					//Extracting and processing ReducingEnd
					abbr = fragment.getMassOptions().getReducingEndTypeString();
					reducingEnd = ReducingEnd.getReducingEndByAbbr(abbr);
					if(reducingEnd==null)
					{
						reducingEnd = new ReducingEnd();
						reducingEnd.setAbbreviation(abbr);
						reducingEnd.setName(abbr);
						reducingEnd.setUri(null);
						Eurocarb.getEntityManager().store(reducingEnd);
						
					}
					//extract the persubstitution abbreviation and then check if it's in the Database
					persub = Persubstitution.getByAbbreviation(fragment.getDerivatization());
					if(persub == null)
					{
						persub = new Persubstitution();
						persub.setAbbreviation(fragment.getDerivatization());
						persub.setName(fragment.getDerivatization());
						Eurocarb.getEntityManager().store(persub);
					}
					peakAnnotated = new PeakAnnotated();
					peakAnnotated.setGlycoCtId((Integer)_structures.get(i));
					peakAnnotated.setSequenceGws(fragment.toString());
					peakAnnotated.setFormula(null);
					peakAnnotated.setCalculatedMass(scans.get(peakListIndex).getAnnotatedPeakList().getAnnotations(peakIndex, i).get(j).getFragmentEntry().getMass());
					peakAnnotated.setContributorQuality(peakAnnotatedContributorQuality);
					peakAnnotated.setContributorId(Eurocarb.getCurrentContributor().getContributorId());
					peakAnnotated.setDateEntered(dateEntered);
					peakAnnotated.setReducingEnd(reducingEnd);
					peakAnnotated.setPersubstitution(persub);
					peakAnnotated.setPeakLabeled(peakLabeled);
					Eurocarb.getEntityManager().store(peakAnnotated);
					
					if(! gSequenceToPeakAnnotated.containsKey(fragment))
					gSequenceToPeakAnnotated.put(fragment.toString(), peakAnnotated.getGlycoCtId());
					
					if(!fillFragmentation(scans.get(peakListIndex).getAnnotatedPeakList().getAnnotations(peakIndex, i).get(j).getFragmentEntry().getName()))
					{
							addActionError("Can't fill the fragmentation table");
							return false;
							
					}
					if(!fillPeakAnnotatedToIon(fragment.toString()))
						return false;
				}
				
				
			}
		} 
		
		
		
	}catch(Exception e){
	//	addActionError("persub result:" + persub.getAbbreviation());
//		addActionError("Structure: "+g.getGlycanSequenceId());
//		addActionError("reducingEnd: " + reducingEnd.getAbbreviation());
//		addActionError("Persub: " +persub.getAbbreviation());
//		addActionError("Mass: " + scans.get(peakListIndex).getAnnotatedPeakList().getAnnotations(peakIndex, i).get(j).getFragmentEntry().getMass());
		addActionError("Structure: " + scans.get(peakListIndex).getStructures().getStructure(i).toString());
		addActionError("Structures size: " + scans.get(peakListIndex).getStructures().size());
		//addActionError("HashMap: " + temp.get(structure));
		addActionError("peakToParentGlycoCTId: "+peakToParentGlycoCTId.size());
		addActionError("gSequenceToPeakAnnotated: " + gSequenceToPeakAnnotated.size());
		addActionError("Formula: " + peakAnnotated.getFormula());
		addActionError("GWS: " + peakAnnotated.getSequenceGws());
		addActionError("Mass: " + peakAnnotated.getCalculatedMass());
		addActionError("Contributor: " + peakAnnotated.getContributorId());
		addActionError("Quality: " + peakAnnotated.getContributorQuality());
		addActionError("GlycoCT: " + peakAnnotated.getGlycoCtId());
		addActionError("Date: " + peakAnnotated.getDateEntered());
		addActionError("Persub: " + peakAnnotated.getPersubstitution().getAbbreviation());
		addActionError("Reducing End: " + peakAnnotated.getReducingEnd().getAbbreviation());
		addActionError("Peak Labeled: " + peakAnnotated.getPeakLabeled().getPeakLabeledId());
		
		return false;
	}
	return true;
	
		// TODO Auto-generated method stub
		
	}

private boolean fillPeakLabeled(PeakList pl,int index) {
	 boolean monoisotopic;
	 List peakListStructures = new ArrayList();
	 peakListStructures = (ArrayList)structures.get(index);
	 int numberOfPeaks = scans.get(index).getPeakList().getPeaks().size();
	 if(gwSpace.getDefaultMassOptions().ISOTOPE == "MONO")
		  monoisotopic = true;
	 else
		 monoisotopic = false;
	 try
	 {
		 for(int i=0;i<numberOfPeaks;i++)
		 {
			 peakLabeled = new PeakLabeled();
			 peakLabeled.setPeakList(pl);
			 peakLabeled.setMonoisotopic(monoisotopic);
			 peakLabeled.setMzValue(scans.get(index).getPeakList().getPeak(i).getMZ());
			 peakLabeled.setIntensityValue(scans.get(index).getPeakList().getPeak(i).getIntensity());
			 peakLabeled.setFwhm(null);
			 peakLabeled.setChargeCount(null);
			 peakLabeled.setSignalToNoise(0.0);
			 
			 Eurocarb.getEntityManager().store(peakLabeled);
			 if(!fillPeakAnnotated(peakLabeled,index,i,peakListStructures))
			 {
				 addActionError("Error in fillPeakAnnotated for peaklist: " + index);
				 return false;
			 }
			 ScanToGSequenceMap.put(scans.get(index),this.gSequenceToPeakAnnotated);	
			 
		 }
		 
	 }
	 catch(Exception e)
	 {
		 addActionError("Can't fill peaklabeled for peakList: " + pl.getPeakListId());
		 return false;
	 }
	 
	
	return true;
	
		// TODO Auto-generated method stub
		
	}
private boolean fillPeakListToDataProcessing(PeakList pl)
{
	Software software = null;
	SoftwareType softwareType = null;
	DataProcessing dataProcessing = null;
	try
	{
		software = Software.getByName("GlycoWorkbench");
	    softwareType = SoftwareType.getByType("Annotation");
		
		//check if the database has software name GlycoWorkbench
		//if not then add one to the software table
		
		if(software == null)
		{
			software = new Software();
			software.setName("GlycoWorkbench");
			software.setSoftwareVersion("1.1");
			Eurocarb.getEntityManager().store(software);
		}
		//check if the software type "Annotation" is there
		//if not add one to software type table
		if(softwareType == null)
		{
			softwareType = new SoftwareType();
			softwareType.setSoftwareType("Annotation");
			Eurocarb.getEntityManager().store(softwareType);
		}
		
		dataProcessing = DataProcessing.getBySoftwareTypeIdAndSoftwareId(softwareType.getSoftwareTypeId(), software.getSoftwareId());
		//check if the dataprocessing table has a row for both software and software type that we want
		//if not add one to the table data processing
		if(dataProcessing == null)
		{
			
				
			
			dataProcessing = new DataProcessing();
			dataProcessing.setIntensityCutoff(0.0);
			dataProcessing.setSoftware(software);
			dataProcessing.setSoftwareType(softwareType);
			dataProcessing.setFormat("gwp");
			
			Eurocarb.getEntityManager().store(dataProcessing);
			
			
		}
		
			
			
			
		//Fill PeakListToDataProcessing based on the former information
		
		peakListToDataProcessing = new PeakListToDataProcessing();
		peakListToDataProcessing.setPeakList(pl);
		peakListToDataProcessing.setDataProcessing(dataProcessing);
		peakListToDataProcessing.setSoftwareOrder(0);
		Eurocarb.getEntityManager().store(peakListToDataProcessing);
	}
	catch(Exception e){
	    addActionError("peakListId: " + pl.getPeakListId());
		addActionError("PeakListToDataId: " + dataProcessing.getDataProcessingId());
		addActionError("softwareId: " + software.getSoftwareId());
		addActionError("softwareType:" + softwareType.getSoftwareTypeId() );
		e.printStackTrace();
		
		//addActionError("Can't fill Peak List Data Processing Table");
		return false;
	}
	return true;
}
private boolean checkRootScan()
{
	Scan temp=null;
	
	int Index = 1;
	ReducingEnd reducingEnd = null;
	Persubstitution persub = null;
	SimpleScan scan = new SimpleScan(gwSpace);
	org.eurocarbdb.application.glycoworkbench.PeakList peaklist = new org.eurocarbdb.application.glycoworkbench.PeakList();
	org.eurocarbdb.application.glycoworkbench.PeakAnnotation peak = new org.eurocarbdb.application.glycoworkbench.PeakAnnotation();
	org.eurocarbdb.application.glycoworkbench.AnnotatedPeakList apl = new org.eurocarbdb.application.glycoworkbench.AnnotatedPeakList();
	//apl.
	
	//GlycanWorkspace gwspace = new GlycanWorkspace();
	//gwspace.
	//allMsScans.add(temp);
	if(scans.get(1).getPrecursorMZ() != null)
		{
			r = MsMsRelationship.getParentByScanId(Scan.getScanByOriginalId(Integer.parseInt(scans.get(1).getName()),acquisitionId).getScanId());
			withRoot = false;
		}
	if(r == null)
		return false;
	else
	{
		try{
		temp = r.getScanByParentId();
		//fill peaklist
//		peak.setMZ(r.getPrecursorMz());
//		peak.setIntensity(r.getPrecursorIntensity());
//		peaklist.add(peak);
//		//fill Scan object
//		scan.setMsMs(true);
//		scan.setName(temp.getOriginalScanId().toString());
//		scan.setParent(null);
//		scan.setPrecursorMZ(null);
//		//i need to add the peaklist to this scan
//		scan.setPeakList(peaklist);
		//to continue here
//		scans.get(1).setParent(scan);
//		allMsScans.add(temp);
		//fill peaklist
		PeakList pl = new PeakList();
		pl.setScan(temp);
		pl.setDateEntered(dateEntered);
		pl.setDeisotoped(this.Deisotoped);
		pl.setChargeDeconvoluted(this.ChargeDeconvoluted);
		pl.setBasePeakMz(null);
		pl.setBasePeakIntensity(null);
		pl.setLowMz(r.getPrecursorMz());
		pl.setHighMz(r.getPrecursorMz());
		pl.setContributorId(Contributor.getCurrentContributor().getId());
		pl.setPeakProcessing(PeakProcessing.getPeakProcessingByType(this.peakProcessingType));
		pl.setContributorQuality(this.peakListContributorQuality);
		Eurocarb.getEntityManager().store(pl);
		if(! fillPeakListToDataProcessing(pl))
	       {
	    	   addActionError("Can't fill PeakListToDataProcessing");
	    	   return false;
	       }
		//fill peakLabeled
		PeakLabeled pLabeled = new PeakLabeled();
		pLabeled.setPeakList(pl);
		pLabeled.setMzValue(r.getPrecursorMz());
		pLabeled.setChargeCount(null);
		pLabeled.setFwhm(null);
		pLabeled.setIntensityValue(r.getPrecursorIntensity());
		pLabeled.setSignalToNoise(0.0);
		pLabeled.setMonoisotopic(gwSpace.getDefaultMassOptions().ISOTOPE == "MONO");
		Eurocarb.getEntityManager().store(pLabeled);
		//fill peakAnnotated
		
		//call extract structures method to fill Glyco_Ct_id
		extractStructures(1);
		List peakListStructures = null;
		peakListStructures = (ArrayList)structures.get(1);
		if(peakListStructures == null || peakListStructures.size()>1)
		{
			addActionError("Only one structure is expected ");
			return false;
				
		}
		
		//create a glycan object
		Glycan fragment = Glycan.fromString(scans.get(1).getStructures().getStructure(0).toString());
		//Extracting and processing ReducingEnd
		String abbr = fragment.getMassOptions().getReducingEndTypeString();
		reducingEnd = ReducingEnd.getReducingEndByAbbr(abbr);
		if(reducingEnd==null)
		{
			reducingEnd = new ReducingEnd();
			reducingEnd.setAbbreviation(abbr);
			reducingEnd.setName(abbr);
			reducingEnd.setUri(null);
			Eurocarb.getEntityManager().store(reducingEnd);
			
		}
		//extract the persubstitution abbreviation and then check if it's in the Database
		persub = Persubstitution.getByAbbreviation(fragment.getDerivatization());
		if(persub == null)
		{
			persub = new Persubstitution();
			persub.setAbbreviation(fragment.getDerivatization());
			persub.setName(fragment.getDerivatization());
			Eurocarb.getEntityManager().store(persub);
		}
		peakAnnotated = new PeakAnnotated();
		peakAnnotated.setPeakLabeled(pLabeled);
		peakAnnotated.setGlycoCtId((Integer)peakListStructures.get(0));
		peakAnnotated.setSequenceGws(scans.get(1).getStructures().getStructure(0).toString());
		peakAnnotated.setFormula(null);
		peakAnnotated.setContributorQuality(peakAnnotatedContributorQuality);
		peakAnnotated.setDateEntered(dateEntered);
		peakAnnotated.setContributorId(getCurrentContributor().getId());
		peakAnnotated.setReducingEnd(reducingEnd);
		peakAnnotated.setPersubstitution(persub);
		peakAnnotated.setCalculatedMass(fragment.computeMass());
		Eurocarb.getEntityManager().store(peakAnnotated);
		if(!fillPeakAnnotatedToIon(fragment.toString()))
			return false;		
	}
	catch(Exception e)
	{
		addActionError("An error occured while processing the root scan " + e.getMessage());
		return false;
	}
	}
	return true;
	
}

private boolean fillPeakList() {
	//get scans from GW file and check the correctness of the file
//	Date date = new Date();
	boolean flag = true;
	int scansInFile =0;
	//create hashmap for MS scans
	List<Scan> tempList = Scan.getAllScans(acquisitionId);
	Map scansMap = new HashMap();
	for(Scan s : tempList)
		scansMap.put(s.getOriginalScanId(), s);
	//retrieve all scans from GWP file
	scans = gwSpace.getAllScans();
	if(scans.get(1).getPrecursorMZ() == null)
		scansInFile = scans.size() -1;
	else
		scansInFile = scans.size();
	
	if(scansInFile != scansMap.size())
	{
		addActionError("The selected file is not compatible with the selected acquisition, scans in file = " + scansInFile +  " while scans in DB= " + scansMap.size());
		return false;
	}
	
		
	Scan temp = null;

	//this is to fill a null object at index 0 like the vectore that generated by workbench
	allMsScans.add(temp);
	
	for(int i=1;i<scans.size();i++)
	{
		//use try catch
		try
		{
			temp = null;
			temp = (Scan)scansMap.get(Integer.parseInt(scans.get(i).getName()));
//	      add error message that show the name of the scan 		
			if(temp  == null)
				{
				 
				addActionError("Can't find scan (" + scans.get(i).getName() + ") in our database");
				return false;
				}
			else
				allMsScans.add(temp);
		}
		catch(Exception e){
			addActionError("Can't get scan based on this scan name:" + scans.get(i).getName());
			return false;
		}
		
	}

	//process the file and fill the peaklist table
	for(int i=1;i<allMsScans.size();i++)
	{
		try
		{
			pl = new PeakList();
			pl.setScan(allMsScans.get(i));
			pl.setDateEntered(dateEntered);
			pl.setDeisotoped(this.getDeisotoped());
			pl.setChargeDeconvoluted(this.getChargeDeconvoluted());
			pl.setContributorQuality(peakListContributorQuality);
			if(scans.get(i).getPrecursorMZ()==null)
				if(scans.get(i).getParent()!=null)
				{
					addActionError("can't find a precursorMZ for a scan");
					return false;
				}
				else
					{
						pl.setBasePeakMz(null);
						pl.setBasePeakIntensity(null);
					}
			else
			{
				if(scans.get(i).getParent() != null)
				{
				pl.setBasePeakMz(scans.get(i).getPrecursorMZ());
				Double intensity = findBaseIntensity(scans,i);
				if(intensity == null)
				{
					addActionError("Can't find precursor:" +  scans.get(i).getPrecursorMZ() + " please check your file and try again");
					return false;
				}
				pl.setBasePeakIntensity(intensity);
				}
				else
					if(checkRootScan())
					{
						pl.setBasePeakMz(scans.get(i).getPrecursorMZ());
						pl.setBasePeakIntensity(r.getPrecursorIntensity());
					}
					else
					{
						addActionError("Can't handle the root scan of this file!");
						return false;
					}
			}
			pl.setLowMz(scans.get(i).getPeakList().getMinMZ());
			pl.setHighMz(scans.get(i).getPeakList().getMaxMZ());
			pl.setContributorId(Contributor.getCurrentContributor().getId());
			pl.setPeakProcessing(PeakProcessing.getPeakProcessingByType(this.peakProcessingType));
			

			//fillPeakListToDataProcessing(pl);
	        Eurocarb.getEntityManager().store(pl);
	        //clean this map to allow new peak list fill it's fragments
	        if(i < allMsScans.size()-1)
	        if(scans.get(i+1).getParent().getName().equals(scans.get(i).getName()))
	           gSequenceToPeakAnnotated.clear();
	        if(! extractStructures(i))
	        {
	        	addActionError("Can't extract the structures for peaklist: " + i);
	        	return false;
	        }
	       if(! fillPeakListToDataProcessing(pl))
	       {
	    	   addActionError("Can't fill PeakListToDataProcessing");
	    	   return false;
	       }
	       if(! fillPeakLabeled(pl,i))
	       {
	    	   addActionError("Can't fill PeakLabeled");
	    	   return false;
	       }
	       //empty the sequence to allow new peak list fill it
	       peakToParentGlycoCTId.clear();
	      
			
		}
		catch(Exception e){
			addActionError("Scan size= " + allMsScans.size());
			addActionError("PeakList scanId " + pl.getScan().getScanId());
			addActionError("PeakList date entered " + pl.getDateEntered());
			addActionError("PeakList deisotoped " + pl.getDeisotoped());
			addActionError("PeakList charge " + pl.getChargeDeconvoluted());
			addActionError("PeakList base peakMZ " + pl.getBasePeakMz());
			addActionError("PeakList base peak intensity " + pl.getBasePeakIntensity());
			addActionError("PeakList low MZ " + pl.getLowMz());
			addActionError("PeakList high MZ " + pl.getHighMz());
			addActionError("PeakList contributor " + pl.getContributorId());
			addActionError("PeakList peak processing " + pl.getPeakProcessing().getPeakProcessingId());
			addActionError("Can't fill the peak list object for this scan: " + scans.get(i).getName());
			
			return false;
		}
				
	}
	
	
		
	return true;
	
}
private Double findBaseIntensity(Vector<org.eurocarbdb.application.glycoworkbench.Scan> scans,int index)
{
	
	Double precursorMZ = scans.get(index).getPrecursorMZ();
	for(int i=0;i<scans.get(index).getParent().getPeakList().size();i++)
		if(precursorMZ == scans.get(index).getParent().getPeakList().getMZ(i))
			{
				//check if the parent hash map has a fragment equal to the structure of this scan
			int size = scans.get(index).getAnnotatedPeakList().getStructures().size();
			
			for(int j=0;j<size;j++)
			{
				String structure = scans.get(index).getStructures().getStructure(j).toString();
			
				if(gSequenceToPeakAnnotated.containsKey(structure))
					peakToParentGlycoCTId.put(structure, (Integer)gSequenceToPeakAnnotated.get(structure));
			
			}		
				
				return scans.get(index).getParent().getPeakList().getIntensity(i);
			}
	//if(checkParentInDB(scans,index))
	return null;
}


public void setChargeDeconvoluted(boolean chargeDeconvoluted) {
	ChargeDeconvoluted = chargeDeconvoluted;
}

public boolean getChargeDeconvoluted() {
	return ChargeDeconvoluted;
}

public void setPeakProcessingType(String peakProcessingType) {
	this.peakProcessingType = peakProcessingType;
}

public String getPeakProcessingType() {
	return peakProcessingType;
}

public void setDeisotoped(boolean deisotoped) {
	Deisotoped = deisotoped;
}

public boolean getDeisotoped() {
	return Deisotoped;
}

public void setPeakprocessing(PeakProcessing peakprocessing) {
	this.peakprocessing = peakprocessing;
}

public PeakProcessing getPeakprocessing() {
	return peakprocessing;
}

public void setPeakLabeled(PeakLabeled peakLabeled) {
	this.peakLabeled = peakLabeled;
}

public PeakLabeled getPeakLabeled() {
	return peakLabeled;
}
public void setPeakAnnotatedContributorQuality(Double peakAnnotatedContributorQuality)
{
	this.peakAnnotatedContributorQuality = peakAnnotatedContributorQuality;
}
public Double getPeakAnnotatedContributorQuality()
{
	return peakAnnotatedContributorQuality;
}

public void setPeakListContributorQuality(Double peakListContributorQuality) {
	this.peakListContributorQuality = peakListContributorQuality;
}

public Double getPeakListContributorQuality() {
	return peakListContributorQuality;
}

public void setPl(PeakList pl) {
	this.pl = pl;
}

public PeakList getPl() {
	return pl;
}

public void setPeakListToDataProcessing(PeakListToDataProcessing peakListToDataProcessing) {
	this.peakListToDataProcessing = peakListToDataProcessing;
}

public PeakListToDataProcessing getPeakListToDataProcessing() {
	return peakListToDataProcessing;
}

public void setDateEntered(Date dateEntered) {
	this.dateEntered = dateEntered;
}

public Date getDateEntered() {
	return dateEntered;
}
        
} // end class
