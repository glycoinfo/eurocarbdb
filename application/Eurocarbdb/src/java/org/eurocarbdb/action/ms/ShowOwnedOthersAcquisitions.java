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

import org.eurocarbdb.tranche.*;
import org.eurocarbdb.action.*;
import org.eurocarbdb.action.exception.*;

import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.hibernate.*;
import org.eurocarbdb.application.glycoworkbench.*;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

import org.hibernate.*; 
import org.hibernate.criterion.*; 
import org.eurocarbdb.dataaccess.hibernate.*;

import java.util.*;
import java.io.*;
import org.apache.commons.io.*;
import org.apache.log4j.Logger;

/**
*   @author             Khalifeh
*   @version            $Rev: 1924 $
*/
public class ShowOwnedOthersAcquisitions extends AbstractMsAction implements RequiresLogin, EditingAction
{
	public List<Acquisition> ownedAcquisitions = null;
	public List<Acquisition> othersAcquisitions = null;
	
	protected static final Logger log = Logger.getLogger( CreateAcquisition.class.getName() );
	
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
	void setOthersAcquisitions(List<Acquisition> othersAcquisitions)
	{
		if(othersAcquisitions == null)
			this.othersAcquisitions = Collections.emptyList();
		else
			
		   this.othersAcquisitions = othersAcquisitions;
	}
	List<Acquisition> getOthersAcquisitions()
	{
		return ownedAcquisitions;
		
	}
	
	public String execute() throws Exception 
    {   
		setOwnedAcquisitions(Acquisition.ownedAcquisitions());
		setOthersAcquisitions(Acquisition.othersAcquisitions());
		
		return SUCCESS;
    }

        
} // end class
