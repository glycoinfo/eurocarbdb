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

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.io.CalcParameterXml;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationParameter;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPResult;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GlycoPeakfinderSettings;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterface;

/**
* @author rene
*
*/
public class LoadSettingsAction extends GlycoPeakfinderAction 
{
    private static final long serialVersionUID = 1L;
    private String m_strExtension = null;
    private File m_hFile;
    private String m_strFileType;
    private String m_strFileName;

    public LoadSettingsAction()
    {
        this.m_strPageType = "load_settings";
    }

    public void setLoadSettings(File a_fFile)
    {
        this.m_hFile = a_fFile;
    }

    public File getLoadSettings()
    {
        return this.m_hFile;
    }

    public void setLoadSettingsContentType(String a_strContentType)
    {
        this.m_strFileType = a_strContentType;
    }

    public String getLoadSettingsContentType()
    {
        return this.m_strFileType;
    }

    public void setLoadSettingsFileName(String a_strFilename)
    {
        this.m_strFileName = a_strFilename;
    }

    public String getLoadSettingsFileName()
    {
        return this.m_strFileName;
    }

    /**
     * @see com.opensymphony.xwork.ActionSupport#execute()
     */
     @Override
     public String execute() throws Exception
     {
         this.m_objSettings.resetErrors();
         if ( this.m_strExtension == null )
         {
             return "page_input";
         }
         if ( this.m_strExtension.equalsIgnoreCase("gpxml") )
         {
             try
             {

                 DBInterface t_objDBInterface = new DBInterface(this.m_objConfiguration);             
                 this.m_objResult = new GPResult();
                 this.m_objSettings = new GlycoPeakfinderSettings();
                 t_objDBInterface.initialize(this.m_objSettings);
                 CalcParameterXml t_objXML = new CalcParameterXml();
                 CalculationParameter t_objParam = t_objXML.importParameter(this.m_hFile);
                 this.m_objSettings.generateFromCalculationParameter(t_objParam);
                 if ( this.m_objSettings.getErrorList().size() != 0 )
                 {
                     this.m_objError.addErrors(this.m_objSettings.getErrorList());
                     this.m_objError.setText("Problems :");
                     this.m_objError.setTitle("Unable to load settings");
                     this.m_objError.setBackUrl("LoadSettings.action");
                     return "page_error";
                 }
                 return "go_start_page";
             }
             catch (Exception e) 
             {
                 this.m_objError.addErrors(this.m_objSettings.getErrorList());
                 this.m_objError.addError(e.getMessage());
                 this.m_objError.setText("Can not load result from unknown file: invalide file format.");
                 this.m_objError.setTitle("Unable to load settings");
                 this.m_objError.setBackUrl("LoadSettings.action");
                 return "page_error";
             }
         }
         else
         {
             this.m_objError.setText("Can not load result from unknown file format " + this.m_strExtension + ".");
             this.m_objError.setTitle("Unable to load settings");
             this.m_objError.setBackUrl("LoadSettings.action");
             return "page_error";
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
}
