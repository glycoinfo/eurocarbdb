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

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.GlycoPeakfinder;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationParameter;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterface;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterfaceMasses;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.ErrorTextEnglish;

/**
* @author rene
*
*/
public class CalculationAction extends GlycoPeakfinderAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see com.opensymphony.xwork.ActionSupport#execute()
     */
     @Override
     public String execute() throws Exception
     {        
         try 
         {
             DBInterface t_objDBInterface = new DBInterface(this.m_objConfiguration);
             DBInterfaceMasses t_objDBMasses = new DBInterfaceMasses(this.m_objConfiguration);
             GlycoPeakfinder t_objCalculator = new GlycoPeakfinder();
             t_objCalculator.setStoreOnlyLowestDeviation(true);
             long t_iStartzeit = System.currentTimeMillis();
             CalculationParameter t_objParameter = this.m_objSettings.generateCalculationParameters(t_objDBMasses,t_objDBInterface);
             // start calculation
             int t_iCalcID = t_objDBInterface.insertCalculation("",t_objParameter,t_iStartzeit);
             long t_iCalcZeit = System.currentTimeMillis();
             t_objParameter = t_objCalculator.calculate(t_objParameter);
             t_iCalcZeit = System.currentTimeMillis() - t_iCalcZeit;
             // finish calculation
             this.m_objResult.createFromParameter(t_objParameter,this.m_objSettings.getDerivatisation(),this.m_objSettings.getOtherModificationName(),this.m_objSettings);
             this.m_objResult.setInitialized(true); 
             long t_iTime = System.currentTimeMillis() - t_iStartzeit;
             t_objDBInterface.updateCalculation(t_iCalcID,t_objParameter,t_iCalcZeit,t_iTime);
             return "finished_calculation";
         } 
         catch (Exception e) 
         {
             this.handleExceptions("calculation", "all", e);
             ArrayList<String> t_aError = new ArrayList<String>();
             t_aError.add(ErrorTextEnglish.CALCULATION);
             this.m_objSettings.setErrorList( t_aError );
             e.printStackTrace();
             return "page_error";
         }
     }

     /**
      * @param parameter
      */

}
