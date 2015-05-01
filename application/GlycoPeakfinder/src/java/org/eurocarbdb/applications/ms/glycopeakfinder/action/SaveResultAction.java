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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.io.CalcParameterXml;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.io.XLSExporter;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationParameter;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterface;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterfaceMasses;

/**
* @author rene
*
*/
public class SaveResultAction extends GlycoPeakfinderAction
{
    private static final long serialVersionUID = 1L;
    private String m_strExtension = null;
    private String m_strXML = "";
    private byte[] m_aBytes = null;
    
    public SaveResultAction()
    {
        this.m_strPageType = "save_result";
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
        if ( !this.m_objSettings.getInitialized() || !this.m_objResult.getInitialized() )
        {
            this.m_objError.setText("No settings or results available.");
            this.m_objError.setTitle("Unable to save results");
            this.m_objError.setBackUrl("SaveResult.action");
            return "page_error";
        }
        if ( this.m_strExtension.equalsIgnoreCase("gpxml") )
        {
            try
            {
                DBInterface t_objDBInterface = new DBInterface(this.m_objConfiguration);
                DBInterfaceMasses t_objDBMasses = new DBInterfaceMasses(this.m_objConfiguration);
                CalculationParameter t_objParameter = this.m_objSettings.generateCalculationParameters(t_objDBMasses,t_objDBInterface);
                this.m_objResult.fillParameter(t_objParameter);
                CalcParameterXml t_objXML = new CalcParameterXml();
                this.m_strXML = t_objXML.exportParameter(t_objParameter);
                return "gpxml";
            }
            catch (Exception e) 
            {
                this.handleExceptions("save result", e.getMessage(), e);
                this.m_objError.addError(e.getMessage());
                this.m_objError.setText("Problems :");
                this.m_objError.setTitle("Unable to save results");
                this.m_objError.setBackUrl("SaveResult.action");
                return "page_error";
            }
        }
        else if ( this.m_strExtension.equalsIgnoreCase("gpxls") )
        {
            try
            {
                DBInterface t_objDBInterface = new DBInterface(this.m_objConfiguration);
                DBInterfaceMasses t_objDBMasses = new DBInterfaceMasses(this.m_objConfiguration);
                CalculationParameter t_objParameter = this.m_objSettings.generateCalculationParameters(t_objDBMasses,t_objDBInterface);
                this.m_objResult.fillParameter(t_objParameter);
                ByteArrayOutputStream t_outStream = new ByteArrayOutputStream();
                XLSExporter t_objXLS = new XLSExporter();
                t_objXLS.export(t_objParameter,t_outStream);
                this.m_aBytes = t_outStream.toByteArray();
                return "gpxls";
            }
            catch (Exception e) 
            {
                this.handleExceptions("save result", e.getMessage(), e);
                this.m_objError.addError(e.getMessage());
                this.m_objError.setText("Problems :");
                this.m_objError.setTitle("Unable to save results");
                this.m_objError.setBackUrl("SaveResult.action");
                return "page_error";
            }
        }
        else
        {
            this.m_objError.setText("Can not store settings into unknown file format " + this.m_strExtension + ".");
            this.m_objError.setTitle("Unable to save results");
            this.m_objError.setBackUrl("SaveResult.action");
            return "page_error";
        }
    }
    
    public InputStream getGpXmlStream() 
    {  
        ByteArrayInputStream t_streamString = new ByteArrayInputStream(this.m_strXML.getBytes());
        return t_streamString;
    }
    
    public InputStream getGpXlsStream() 
    {  
        ByteArrayInputStream t_streamString = new ByteArrayInputStream(this.m_aBytes);
        return t_streamString;
    }
    
    public void setFileExtension(String a_strExtension)
    {
        this.m_strExtension = a_strExtension;
    }
    
    public String getFileExtension()
    {
        return this.m_strExtension;
    }

    public String getGpFilename()
    {
        Date t_objDate = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern( "yyyy.MM.dd-hh.mm" );
        return "gp_results-" + sdf.format(t_objDate);
    }
}
