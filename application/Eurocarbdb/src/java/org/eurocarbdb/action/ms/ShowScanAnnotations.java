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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import org.apache.commons.io.*;
import org.apache.log4j.Logger;
import org.apache.taglibs.standard.lang.jpath.adapter.ConversionException;
import org.apache.taglibs.standard.lang.jpath.adapter.Convert;

/**
*   @author             Khalifeh Al Jadda
*   @version            $Rev: 1924 $
*/

public class ShowScanAnnotations extends AbstractMsAction implements RequiresLogin, EditingAction{
	
	private List<Object> scanAnnotations = null;
	private Object[][] items  = null;
	private int scanId = 0;
	
   protected static final Logger log = Logger.getLogger( ShowScanAnnotations.class.getName() );
	
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

	@SuppressWarnings("unchecked")
	public String execute() throws Exception{
		
		
		try
		{
		setScanAnnotations(PeakAnnotated.getScanAnnotations(scanId));
		}catch(Exception e){
			addActionError("Can't retrieve peaklists and acquisitions");
			return ERROR;
		}
		items = new Object[scanAnnotations.size()][];

		Iterator iter = scanAnnotations.iterator();
		for(int i=0 ; i< scanAnnotations.size(); i++)
		{
			items[i] = (Object[])iter.next();
//			Date d;
//			DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
//			d =  df.parse(items[i][1].toString());
//			items[i][1] = d.getTime();
//			System.out.println("d.getTime = " + d.getTime());
		}
		return SUCCESS;
				
	}
	
	public Object[][] getItems()
	{
		return items;
	}

	
	public void setScanAnnotations(List<Object> scanAnnotations) {
		this.scanAnnotations = scanAnnotations;
	}

	public List<Object> getScanAnnotations() {
		return scanAnnotations;
	}
	public int getScanId()
	{
		return scanId;
	}
	public void setScanId(int scanId)
	{
		this.scanId = scanId;
	}
	

}
