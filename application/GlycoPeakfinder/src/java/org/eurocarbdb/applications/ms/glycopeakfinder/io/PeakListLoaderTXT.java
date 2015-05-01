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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPPeak;

import java.io.LineNumberReader;
/**
* @author Logan
*
*/
public class PeakListLoaderTXT implements PeakListLoader 
{

    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.util.PeakListLoader#loadPeaklist(java.io.File)
     */
    public ArrayList<GPPeak> loadPeaklist(File a_hFile) throws IOException,PeakListLoaderExeption
    {
        ArrayList<GPPeak> t_aPeaklist = new ArrayList<GPPeak>();
        GPPeak t_objPeak;
        String t_text = "";
        try 
        {
            LineNumberReader in = new LineNumberReader(new FileReader(a_hFile));
            while ((t_text = in.readLine()) != null) 
            {
                t_objPeak = this.parseLine(t_text);
                if ( t_objPeak != null )
                {
                    t_aPeaklist.add(t_objPeak);
                }
            }
        }
        catch (Exception e) 
        {
            throw new PeakListLoaderExeption("Error in file Format.");
        }        
        return t_aPeaklist;
    }

    public GPPeak parseLine(String a_strLine) 
    {
        GPPeak t_objPeak = new GPPeak();
        String t_strText = a_strLine.replace('\t',' ');
        t_strText = t_strText.trim();
        t_strText = t_strText.replace(',','.');
        if ( t_strText.trim().length() == 0 )
        {
            return null;
        }
        // MZ
        int t_iIndex = t_strText.indexOf(" ");
        if ( t_iIndex == -1 )
        {
            t_objPeak.setMZ(Double.valueOf(t_strText));
            return t_objPeak;
        }
        String t_strSubtext = t_strText.substring(0, t_iIndex);
        t_objPeak.setMZ(Double.valueOf(t_strSubtext.trim()));
        if ( t_strText.length() > t_iIndex )
        {
            t_strText = t_strText.substring(t_iIndex+1).trim();
            // Intensity
            t_iIndex = t_strText.indexOf(" ");
            if ( t_iIndex == -1 )
            {
                t_objPeak.setIntensity(Double.valueOf(t_strText));
                return t_objPeak;
            }
            t_strSubtext = t_strText.substring(0, t_iIndex);
            t_objPeak.setIntensity(Double.valueOf(t_strSubtext.trim()));
            if ( t_strText.length() > t_iIndex )
            {
                t_strText = t_strText.substring(t_iIndex+1).trim();
                t_objPeak.setCharge(Integer.parseInt(t_strText));
            }
        }
        return t_objPeak;
    }
}
