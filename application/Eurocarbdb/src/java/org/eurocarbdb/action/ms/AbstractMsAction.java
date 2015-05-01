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
*   Last commit: $Rev: 1940 $ by $Author: khaleefah $ on $Date:: 2010-08-10 #$  
*/

package org.eurocarbdb.action.ms;

import java.util.List;
import org.eurocarbdb.action.AbstractExperimentAwareAction;
import org.eurocarbdb.dataaccess.ms.*;

public class AbstractMsAction extends AbstractExperimentAwareAction
{
    
    public List<Manufacturer> getManufacturers() 
    {
        return Manufacturer.getAllManufacturers();
    }

    public List<Device> getDevices() 
    {
        return Device.getAllDevices();
    } 
    public List<String> getPeakProcessingTypes()
    {
    	return PeakProcessing.getAllPeakProcessingTypes();
    }

}
