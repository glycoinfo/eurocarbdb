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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
/**
* 
*/
package org.eurocarbdb.applications.ms.glycopeakfinder.action;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eurocarbdb.applications.ms.glycopeakfinder.io.PeakListLoader;
import org.eurocarbdb.applications.ms.glycopeakfinder.io.PeakListLoaderExeption;
import org.eurocarbdb.applications.ms.glycopeakfinder.io.PeakListLoaderFlexAnalysis;
import org.eurocarbdb.applications.ms.glycopeakfinder.io.PeakListLoaderTXT;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPOtherResidue;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPPeak;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPResult;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.MassMolecule;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.MassResidue;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.ResidueCategory;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterface;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.ErrorTextEnglish;

import com.opensymphony.xwork.Preparable;

/**
* @author rene
*
*/
public class DataCollectorAction extends GlycoPeakfinderAction implements Preparable
{
    private static final long serialVersionUID = 1L;

    // Settings for the file upload
    private File m_hFile;
    private String m_strFileType;
    private String m_strFileName;
    private String m_strExtension;
    // Settings for the page navigation
    private String m_strPageFrom = "";
    private String m_strPageTo = "";
    private String m_strMotif = "";
    private String m_strRedirect = null;
    
    public DataCollectorAction()
    {
        this.m_strPageType = "calculation";
    }
    
    public String getRedirect()
    {
        return this.m_strRedirect;
    }
    
    public void setRedirect(String a_strUrl)
    {
        this.m_strRedirect = a_strUrl;
    }
    
    public void setMotif(String a_strMotif)
    {
        this.m_strMotif = a_strMotif;
    }
    
    public String getMotif()
    {
        return this.m_strMotif;
    }
    
    public void setResidueHash(HashMap<String,String[]> a_mapParamter)
    {
        try
        {
            ArrayList<ResidueCategory> t_aCategorie = this.m_objSettings.getCategorie();
            ArrayList<MassResidue> t_aResidues;
            ResidueCategory t_objCategorie;
            MassResidue t_objResidue;
            String t_strKey;
            String[] t_aValue;
            Set<String> t_aKey = a_mapParamter.keySet();
            for ( Iterator<String> t_iterKeys = t_aKey.iterator(); t_iterKeys.hasNext();) 
            {
                t_strKey = t_iterKeys.next();
                String[] t_aKeyParts = t_strKey.split("_");
                if ( t_aKeyParts.length == 3 )
                {
                    for (Iterator<ResidueCategory> t_iterCategorie = t_aCategorie.iterator(); t_iterCategorie.hasNext();) 
                    {
                        t_objCategorie = t_iterCategorie.next();
                        if ( t_objCategorie.getId().equalsIgnoreCase(t_aKeyParts[0]) )
                        {
                            t_aResidues = t_objCategorie.getResidues();
                            for (Iterator<MassResidue> t_iterResidues = t_aResidues.iterator(); t_iterResidues.hasNext();) 
                            {
                                t_objResidue = t_iterResidues.next();
                                if ( t_objResidue.getId().equalsIgnoreCase(t_aKeyParts[1]) )
                                {
                                    t_aValue = a_mapParamter.get(t_strKey);
                                    if ( t_aValue.length > 0 )
                                    {
                                        if ( t_aKeyParts[2].equalsIgnoreCase("min") )
                                        {
                                            try 
                                            {
                                                t_objResidue.setMin(Integer.parseInt(t_aValue[0]));
                                            } 
                                            catch (Exception e) 
                                            {
                                                this.m_objSettings.addError(String.format(ErrorTextEnglish.RESIDUE_NUMBER,t_objResidue.getAbbr()));
                                            }                                            
                                        }
                                        else if (t_aKeyParts[2].equalsIgnoreCase("max")) 
                                        {
                                            try 
                                            {
                                                t_objResidue.setMax(Integer.parseInt(t_aValue[0]));
                                            } 
                                            catch (Exception e) 
                                            {
                                                this.m_objSettings.addError(String.format(ErrorTextEnglish.RESIDUE_NUMBER,t_objResidue.getAbbr()));
                                            }                                            
                                        }
                                        else if (t_aKeyParts[2].equalsIgnoreCase("ax")) 
                                        {
                                            if ( t_aValue[0].equalsIgnoreCase("true") )
                                            {
                                                t_objResidue.setUseAX(true);
                                            }
                                        }
                                        else if (t_aKeyParts[2].equalsIgnoreCase("e")) 
                                        {
                                            if ( t_aValue[0].equalsIgnoreCase("true") )
                                            {
                                                t_objResidue.setUseE(true);
                                            }
                                        }
                                        else if (t_aKeyParts[2].equalsIgnoreCase("f")) 
                                        {
                                            if ( t_aValue[0].equalsIgnoreCase("true") )
                                            {
                                                t_objResidue.setUseF(true);
                                            }
                                        }
                                        else if (t_aKeyParts[2].equalsIgnoreCase("g")) 
                                        {
                                            if ( t_aValue[0].equalsIgnoreCase("true") )
                                            {
                                                t_objResidue.setUseG(true);
                                            }
                                        }
                                        else if (t_aKeyParts[2].equalsIgnoreCase("h")) 
                                        {
                                            if ( t_aValue[0].equalsIgnoreCase("true") )
                                            {
                                                t_objResidue.setUseH(true);
                                            }
                                        }
                                    }
                                }                                
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) 
        {
            this.handleExceptions("collector","residue hash", e);
        }
    }
    
    public void setOtherResidueHash(HashMap<String,String[]> a_mapParamter)
    {
        try
        {
            GPOtherResidue t_objResidue;
            String t_strKey;
            String[] t_aValue;
            Set<String> t_aKey = a_mapParamter.keySet();
            for ( Iterator<String> t_iterKeys = t_aKey.iterator(); t_iterKeys.hasNext();) 
            {
                t_strKey = t_iterKeys.next();
                String[] t_aKeyParts = t_strKey.split("_");
                if ( t_aKeyParts.length == 2 )
                {
                    if ( t_aKeyParts[0].equalsIgnoreCase("1") )
                    {
                        t_objResidue = this.m_objSettings.getOtherResidueOne();
                    } 
                    else if (t_aKeyParts[0].equalsIgnoreCase("2")) 
                    {
                        t_objResidue = this.m_objSettings.getOtherResidueTwo();
                    }
                    else
                    {
                        t_objResidue = this.m_objSettings.getOtherResidueThree();
                    }

                    t_aValue = a_mapParamter.get(t_strKey);
                    
                    if ( t_aKeyParts[1].equalsIgnoreCase("mass") ) 
                    {
                        try 
                        {
                            t_objResidue.setMass(Double.parseDouble(t_aValue[0].replaceAll(",",".")));
                        } 
                        catch (Exception e) 
                        {
                            this.m_objSettings.addError(ErrorTextEnglish.RESIDUE_OTHER_MASS);
                        }                                            
                    }
                    else if (t_aKeyParts[1].equalsIgnoreCase("name")) 
                    {
                        t_objResidue.setName(t_aValue[0]);
                    }
                    else if (t_aKeyParts[1].equalsIgnoreCase("min")) 
                    {
                        try 
                        {
                            t_objResidue.setMin(Integer.parseInt(t_aValue[0]));
                        } 
                        catch (Exception e) 
                        {
                            this.m_objSettings.addError(String.format(ErrorTextEnglish.RESIDUE_NUMBER,t_objResidue.getName()));
                        }                                            
                    }
                    else if (t_aKeyParts[1].equalsIgnoreCase("max")) 
                    {
                        try 
                        {
                            t_objResidue.setMax(Integer.parseInt(t_aValue[0]));
                        } 
                        catch (Exception e) 
                        {
                            this.m_objSettings.addError(String.format(ErrorTextEnglish.RESIDUE_NUMBER,t_objResidue.getName()));
                        }                                            
                    }
                }
            }
        }
        catch (Exception e) 
        {
            this.handleExceptions("collector","other residue hash", e);
        }
    }

    public void setMoleculeHash(HashMap<String,String[]> a_mapParamter)
    {
        try
        {
            String t_strKey;
            Set<String> t_aKey = a_mapParamter.keySet();
            for ( Iterator<String> t_iterKeys = t_aKey.iterator(); t_iterKeys.hasNext();) 
            {
                t_strKey = t_iterKeys.next();
                String[] t_aKeyParts = t_strKey.split("_");
                if ( t_aKeyParts.length == 2 )
                {
                    for (Iterator<MassMolecule> t_iterMolecules = this.m_objSettings.getLossGainMolecules().iterator(); t_iterMolecules.hasNext();) 
                    {
                        MassMolecule t_objMolecule = t_iterMolecules.next();
                        if ( t_objMolecule.getId().equals(t_aKeyParts[0]))
                        {
                            if ( t_aKeyParts[1].equals("gain") )
                            {
                                try 
                                {
                                    t_objMolecule.setGain(Integer.parseInt(a_mapParamter.get(t_strKey)[0]));
                                }
                                catch (Exception e) 
                                {
                                    this.m_objSettings.addError(String.format(ErrorTextEnglish.MOLECULE_GAIN,t_objMolecule.getName()));
                                }
                            }
                            else if ( t_aKeyParts[1].equals("loss") )
                            {
                                try 
                                {
                                    t_objMolecule.setLoss(Integer.parseInt(a_mapParamter.get(t_strKey)[0]));
                                }
                                catch (Exception e) 
                                {
                                    this.m_objSettings.addError(String.format(ErrorTextEnglish.MOLECULE_LOSS,t_objMolecule.getName()));
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) 
        {
            this.handleExceptions("collector","molecule hash", e);
        }
    }

    public void setFileExtension(String a_strExtension)
    {
        this.m_strExtension = a_strExtension;
    }
    
    public String getFileExtension()
    {
        return this.m_strExtension;
    }
    
    public void setMyFile(File a_fFile)
    {
        this.m_hFile = a_fFile;
    }
    
    public File getMyFile()
    {
        return this.m_hFile;
    }
    
    public void setMyFileContentType(String a_strContentType)
    {
        this.m_strFileType = a_strContentType;
    }
    
    public String getMyFileContentType()
    {
        return this.m_strFileType;
    }
            
    public void setMyFileFileName(String a_strFilename)
    {
        this.m_strFileName = a_strFilename;
    }
    
    public String getMyFileFileName()
    {
        return this.m_strFileName;
    }
    
    public void setPageFrom(String a_strPage)
    {
        this.m_strPageFrom = a_strPage;
    }
    
    public String getPageFrom()
    {
        return this.m_strPageFrom;
    }
    
    public void setPageTo(String a_strPage)
    {
        this.m_strPageTo = a_strPage;
    }
    
    public String getPageTo()
    {
        return this.m_strPageTo;
    }
    
    /**
     * @see com.opensymphony.xwork.Preparable#prepare()
     */
    public void prepare() throws Exception
    {
        // delete of all values of the current form
        this.m_objSettings.resetErrors();
        if ( this.m_strPageFrom.equalsIgnoreCase("mass") || this.m_strPageFrom.equalsIgnoreCase("file") )
        {
            this.m_objSettings.resetMassPage();
        }
        if ( this.m_strPageFrom.equalsIgnoreCase("modi") )
        {
            this.m_objSettings.resetModificationPage();
        }
        if ( this.m_strPageFrom.equalsIgnoreCase("ions") )
        {
            this.m_objSettings.resetIonPage();
        }
        if ( this.m_strPageFrom.equalsIgnoreCase("resi") )
        {
            this.m_objSettings.resetResiduePage();
        }
    }

    /**
     * @see com.opensymphony.xwork.ActionSupport#execute()
     */
    @Override
    public String execute() throws Exception
    {
        if ( !this.m_objSettings.getInitialized() )
        {
            try 
            {
                DBInterface t_objDB = new DBInterface(this.m_objConfiguration);
                t_objDB.initialize(this.m_objSettings);
            } 
            catch (Exception e) 
            {
                this.handleExceptions("collector", "init", e);
                ArrayList<String> t_aError = new ArrayList<String>();
                t_aError.add(ErrorTextEnglish.DB_ERROR);
                this.m_objSettings.setErrorList( t_aError );
                return "page_error";
            }
        }
        if ( this.m_objResult.getInitialized() )
        {
            this.m_objResult = new GPResult();
        }
        // reset current page?
        if ( this.m_strPageTo.equalsIgnoreCase("reset") )
        {
            return this.resetPage();
        }
        // First we have to validate the settings
        if ( this.m_strPageFrom.equalsIgnoreCase("file") )
        {
            // doing the file handling stuff
            try 
            {
                if ( this.m_strExtension.equalsIgnoreCase("txt") )
                {
                    PeakListLoader t_objLoader = new PeakListLoaderTXT();
                    ArrayList<GPPeak> t_objPeaks = t_objLoader.loadPeaklist(this.m_hFile);
                    this.m_objSettings.setPeaks(t_objPeaks);
                }
                else if ( this.m_strExtension.equalsIgnoreCase("flexAnalysis") )
                {
                    PeakListLoader t_objLoader = new PeakListLoaderFlexAnalysis();
                    ArrayList<GPPeak> t_objPeaks = t_objLoader.loadPeaklist(this.m_hFile);
                    this.m_objSettings.setPeaks(t_objPeaks);
                }
                else
                {
                    this.m_objSettings.addError("Error loading peak file: wrong format or error in format.");
                }
                this.m_objSettings.sortPeaklist();
            } 
            catch (PeakListLoaderExeption e) 
            {
                this.m_objSettings.addError("Error loading peak file: " + e.getMessage() );
            }            
            // http://www.proteomecommons.org/archive/1132593287263/index.html
            // http://freemarker.sourceforge.net/docs/ref_builtins_number.html
        }
        if ( this.m_strPageFrom.equalsIgnoreCase("mass") )
        {
            this.m_objSettings.validateMassPage();
        }
        if ( this.m_strPageFrom.equalsIgnoreCase("modi") )
        {
            this.m_objSettings.validateModificationPage();
        }
        if ( this.m_strPageFrom.equalsIgnoreCase("ions") )
        {
            this.m_objSettings.validateIonPage();
        }
        if ( this.m_strPageTo.equalsIgnoreCase("motif") )
        {
            if ( !this.m_strMotif.equals("choose") )
            {
                try
                {
                    this.m_objSettings.resetResidueMinMax();
                    DBInterface t_objDB = new DBInterface(this.m_objConfiguration);
                    t_objDB.setMotif(this.m_objSettings,this.m_strMotif);
                    t_objDB.setDefaultResidueFragments(this.m_objSettings);
                }
                catch (Exception e) 
                {
                    this.handleExceptions("residue", "motif", e);
                    ArrayList<String> t_aError = new ArrayList<String>();
                    t_aError.add(ErrorTextEnglish.MOTIF);
                    this.m_objSettings.setErrorList( t_aError );
                    return "page_error";
                }
                this.m_objSettings.validateResiduePage();
            }
            this.m_strPageTo = "resi";
            
        }
        if ( this.m_strPageTo.equalsIgnoreCase("calc") )
        {
            this.m_objSettings.validateMassPage();
            this.m_objSettings.validateResiduePage();
            this.m_objSettings.validateIonPage();
            this.m_objSettings.validateModificationPage();
        }
        // errors?
        if ( this.m_objSettings.getErrorList().size() > 0 )
        {
            return "page_error";
        }
        // no errors go to the next page
        if ( this.m_strPageTo.equalsIgnoreCase("mass") )
        {
            return "page_mass";
        }
        if ( this.m_strPageTo.equalsIgnoreCase("modi") )
        {
            return "page_modi";
        }
        if ( this.m_strPageTo.equalsIgnoreCase("ions") )
        {
            return "page_ion";
        }
        if ( this.m_strPageTo.equalsIgnoreCase("resi") )
        {
            return "page_resi";
        }
        if ( this.m_strPageTo.equalsIgnoreCase("calc") )
        {
            return "start_calculation";
        }
        if ( this.m_strPageTo.startsWith("side_"))
        {
            this.findActionName(this.m_strPageTo);
            if ( this.m_strRedirect == null )
            {
                this.m_objSettings.addError("Error in redirect URL.");
                return "page_error";
            }
            return "redirect";
        }
        return "page_mass";
    }

    /**
     * @return
     */
    private String resetPage() 
    {
        String t_strReturn = "page_error";
        if ( this.m_strPageFrom.equalsIgnoreCase("mass") )
        {
            this.m_objSettings.resetMassPage();
            t_strReturn = "page_mass";
        }
        if ( this.m_strPageFrom.equalsIgnoreCase("modi") )
        {
            this.m_objSettings.resetModificationPage();
            return "page_modi";
        }
        if ( this.m_strPageFrom.equalsIgnoreCase("ions") )
        {
            this.m_objSettings.resetIonPage();
            return "page_ion";
        }
        if ( this.m_strPageFrom.equalsIgnoreCase("resi") )
        {
            this.m_objSettings.resetResiduePage();
            return "page_resi";
        }
        if ( this.m_strPageFrom.equalsIgnoreCase("cont") )
        {
            return "page_cont";
        }
        return t_strReturn;
    }

    
    public void findActionName(String a_strID)
    {
        if ( a_strID.equals("side_intro") )
        {
            this.m_strRedirect = "Introduction.action";
        }
        else if ( a_strID.equals("side_lsett") )
        {
            this.m_strRedirect = "LoadSettings.action";
        }
        else if ( a_strID.equals("side_ssett") )
        {
            this.m_strRedirect = "SaveSettings.action";
        }
        else if ( a_strID.equals("side_calc") )
        {
            this.m_strRedirect = "Input.action";
        }
        else if ( a_strID.equals("side_lres") )
        {
            this.m_strRedirect = "LoadResult.action";
        }
        else if ( a_strID.equals("side_sres") )
        {
            this.m_strRedirect = "SaveResult.action";
        }
        else if ( a_strID.equals("side_down") )
        {
            this.m_strRedirect = "Download.action";
        }
        else if ( a_strID.equals("side_cont") )
        {
            this.m_strRedirect = "Contact.action";
        }
        else 
        {
            this.m_strRedirect = null;
        }
    }
}
