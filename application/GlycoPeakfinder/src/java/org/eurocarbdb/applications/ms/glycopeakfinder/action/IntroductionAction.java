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

import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterface;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.ErrorTextEnglish;

/**
* @author rene
*
*/
public class IntroductionAction extends GlycoPeakfinderAction
{
    private static final long serialVersionUID = 1L;
    
    public IntroductionAction() 
    {
        this.m_strPageType = "introduction";
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
                this.handleExceptions("intro", "init", e);
                this.m_objError.setText(ErrorTextEnglish.DB_ERROR);
                this.m_objError.setTitle("Unable to initialise data objects");
                this.m_objError.setBackUrl("Introduction.action");
                return "page_error";
            }
        }
        return "page_info";
    }
}
