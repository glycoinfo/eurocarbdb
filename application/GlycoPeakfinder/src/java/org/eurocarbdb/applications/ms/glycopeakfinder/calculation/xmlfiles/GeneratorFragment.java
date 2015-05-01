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
package org.eurocarbdb.applications.ms.glycopeakfinder.calculation.xmlfiles;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
* @author Logan
*
*/
public class GeneratorFragment 
{
    private DBInterface m_objDB = null;
    /**
     * @param t_objdb
     * @param string
     * @throws IOException 
     * @throws SQLException 
     */
    public void export(DBInterface a_objDB, String a_strFileName) throws IOException, SQLException 
    {
        this.m_objDB = a_objDB;
        // Erzeugung eines XML-Dokuments
        Document t_objDocument = new Document();
        // Erzeugung des Root-XML-Elements 
        Element t_objRoot = new Element("fragments");
        Namespace xsiNS = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");        
        t_objRoot.addNamespaceDeclaration(xsiNS);
        this.exportAX(t_objRoot);
        this.exportOthers(t_objRoot);
        // Und jetzt haengen wir noch das Root-Element an das Dokument
        t_objDocument.setRootElement(t_objRoot);
        // Damit das XML-Dokument schoen formattiert wird holen wir uns ein Format
        Format t_objFormat = Format.getPrettyFormat();
        t_objFormat.setEncoding("iso-8859-1");
        // Erzeugung eines XMLOutputters dem wir gleich unser Format mitgeben
        XMLOutputter t_objExportXML = new XMLOutputter(t_objFormat);
        // Schreiben der XML-Datei in einen String
        FileWriter t_objWriter = new FileWriter(a_strFileName);
        t_objExportXML.output(t_objDocument, t_objWriter );
    }
    
    /**
     * @param root
     * @throws SQLException 
     */
    private void exportOthers(Element a_objRoot) throws SQLException 
    {
        Element t_objFragment = null;
        Element t_objSubTag = null;
        ResultSet t_objResult = this.m_objDB.getFragmentsOther();
        while ( t_objResult.next() )
        {
            t_objFragment = new Element("fragment");
            
            t_objSubTag = new Element("fragment_id");
            t_objSubTag.setText( t_objResult.getString("fragment_other_id"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("type");
            t_objSubTag.setText( t_objResult.getString("type"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("pos");
            t_objSubTag.setText( t_objResult.getString("pos"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("pm");
            t_objSubTag.setText( t_objResult.getString("pm"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("pac");
            t_objSubTag.setText( t_objResult.getString("pac"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_mono");
            t_objSubTag.setText( t_objResult.getString("mass_mono"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_avg");
            t_objSubTag.setText( t_objResult.getString("mass_avg"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("residue_id");
            t_objSubTag.setText( t_objResult.getString("abbr").toLowerCase());
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pm_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pm_mono"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pm_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pm_avg"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pdm_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pdm_mono"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pdm_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pdm_avg"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pac_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pac_mono"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pac_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pac_avg"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pdac_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pdac_mono"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pdac_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pdac_avg"));
            t_objFragment.addContent(t_objSubTag);
            
            a_objRoot.addContent(t_objFragment);
        }    
    }

    /**
     * @param root
     * @throws SQLException 
     */
    private void exportAX(Element a_objRoot) throws SQLException 
    {
        Element t_objFragment = null;
        Element t_objSubTag = null;
        ResultSet t_objResult = this.m_objDB.getFragmentsAX();
        while ( t_objResult.next() )
        {
            t_objFragment = new Element("fragment_ax");
            
            t_objSubTag = new Element("fragment_id");
            t_objSubTag.setText( t_objResult.getString("fragment_ax_id"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("type");
            t_objSubTag.setText( t_objResult.getString("type"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("cleav_one");
            t_objSubTag.setText( t_objResult.getString("cleav_one"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("cleav_two");
            t_objSubTag.setText( t_objResult.getString("cleav_two"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("pos");
            t_objSubTag.setText( t_objResult.getString("pos"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("pm");
            t_objSubTag.setText( t_objResult.getString("pm"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("pac");
            t_objSubTag.setText( t_objResult.getString("pac"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_mono");
            t_objSubTag.setText( t_objResult.getString("mass_mono"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_avg");
            t_objSubTag.setText( t_objResult.getString("mass_avg"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("residue_id");
            t_objSubTag.setText( t_objResult.getString("abbr").toLowerCase());
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pm_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pm_mono"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pm_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pm_avg"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pdm_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pdm_mono"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pdm_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pdm_avg"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pac_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pac_mono"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pac_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pac_avg"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pdac_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pdac_mono"));
            t_objFragment.addContent(t_objSubTag);
            
            t_objSubTag = new Element("mass_pdac_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pdac_avg"));
            t_objFragment.addContent(t_objSubTag);
            
            a_objRoot.addContent(t_objFragment);
        }    
    }
}
