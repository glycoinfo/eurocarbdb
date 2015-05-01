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

import java.util.ArrayList;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.io.CalcParameterXml;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationParameter;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPResult;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GlycoPeakfinderSettings;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterface;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.ErrorTextEnglish;

/**
* @author rene
*
*/
public class ExampleLoadAction extends GlycoPeakfinderAction
{
    private static final long serialVersionUID = 1L;

    // User Settings Storage Variable
    private String m_strExample = "";
    
    public void setExample(String a_strExample)
    {
        this.m_strExample = a_strExample;
    }
    
    public String getExample()
    {
        return this.m_strExample;
    }
    
    /**
     * @see com.opensymphony.xwork.ActionSupport#execute()
     */
    @Override
    public String execute() throws Exception
    {
        try 
        {
            DBInterface t_objDB = new DBInterface(this.m_objConfiguration);
            this.m_objResult = new GPResult();
            this.m_objSettings = new GlycoPeakfinderSettings();
            t_objDB.initialize(this.m_objSettings);
            String t_strXML = t_objDB.getExample(this.m_strExample);
            CalcParameterXml t_objParser = new CalcParameterXml();
            CalculationParameter t_objCalcParam = t_objParser.importParameter(t_strXML);
            ArrayList<String> t_aErrors = this.m_objSettings.generateFromCalculationParameter(t_objCalcParam);
            if ( t_aErrors.size() > 0 )
            {
                this.m_objSettings = new GlycoPeakfinderSettings();
                t_objDB.initialize(this.m_objSettings);
                this.m_objError.addErrors(t_aErrors);
                this.m_objError.setBackUrl("Introduction.action");
                this.m_objError.setText("Problems :");
                this.m_objError.setTitle("Unable to load Example");
                return "page_error";    
            }
        } 
        catch (Exception e) 
        {
            this.handleExceptions("example", "load", e);
            this.m_objError.setBackUrl("Introduction.action");
            this.m_objError.setText(ErrorTextEnglish.DB_ERROR);
            this.m_objError.setTitle("Unable to load Example");
            return "page_error";
        }
        return "go_start_page";
    }

}