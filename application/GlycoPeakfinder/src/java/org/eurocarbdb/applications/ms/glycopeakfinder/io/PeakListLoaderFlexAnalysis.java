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
package org.eurocarbdb.applications.ms.glycopeakfinder.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eurocarbdb.applications.ms.glycopeakfinder.io.PeakListLoader;
import org.eurocarbdb.applications.ms.glycopeakfinder.io.PeakListLoaderExeption;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPPeak;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class PeakListLoaderFlexAnalysis implements PeakListLoader 
{
    ArrayList<GPPeak> m_aPeaks = new ArrayList<GPPeak>();
    
    public ArrayList<GPPeak> loadPeaklist(File a_hFile) throws PeakListLoaderExeption 
    {
        boolean t_bMass;
        boolean t_bIntsensity;
        this.m_aPeaks.clear();
        Document t_objDocument;
        try 
        {
            t_objDocument = new SAXBuilder().build(a_hFile);
            Element t_objRoot = t_objDocument.getRootElement();
            for (Iterator t_iterPK = t_objRoot.getChildren().iterator(); t_iterPK.hasNext();) 
            {
                Element t_objSubElement = (Element) t_iterPK.next();
                GPPeak t_objPeak = new GPPeak();
                t_bMass = false;
                t_bIntsensity = false;
                for (Iterator t_iterSubs = t_objSubElement.getChildren().iterator(); t_iterSubs.hasNext();) 
                {
                    Element t_objElement = (Element) t_iterSubs.next();
                    if ( t_objElement.getName().equals("area") )
                    {
                        t_objPeak.setIntensity( Double.parseDouble(t_objElement.getText()) );
                        t_bIntsensity = true;
                    }
                    else if ( t_objElement.getName().equals("mass") )
                    {
                        t_objPeak.setMZ( Double.parseDouble(t_objElement.getText()) );
                        t_bMass = true;
                    }
                }
                if ( !(t_bMass && t_bIntsensity) )
                {
                    throw new PeakListLoaderExeption("Mass or Intensity value missing in file.");
                }
                this.m_aPeaks.add(t_objPeak);
            }
        } 
        catch (JDOMException e) 
        {
            throw new PeakListLoaderExeption(e.getMessage());
        } 
        catch (IOException e) 
        {
            throw new PeakListLoaderExeption(e.getMessage());
        }            
        return this.m_aPeaks;
    }
}
