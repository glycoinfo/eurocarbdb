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
public class GeneratorDefault 
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
        Element t_objRoot = new Element("defaults");
        Namespace xsiNS = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");        
        t_objRoot.addNamespaceDeclaration(xsiNS);
        this.exportResidues(t_objRoot);
        this.exportPersubstitutions(t_objRoot);
        this.exportIons(t_objRoot);
        this.exportDericatisation(t_objRoot);
        this.exportMolecules(t_objRoot);
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
    
    private void exportResidues(Element a_objRoot) throws SQLException
    {
        Element t_objResidues = new Element("residues");
        Element t_objResidue = null;
        Element t_objSubTag = null;
        
        ResultSet t_objResult = this.m_objDB.getResidues();
        while ( t_objResult.next() )
        {
            t_objResidue = new Element("residue");
            
            t_objSubTag = new Element("name");
            t_objSubTag.setText( t_objResult.getString("name"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("abbr");
            t_objSubTag.setText( t_objResult.getString("abbr").toLowerCase());
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("pos");
            t_objSubTag.setText( t_objResult.getString("pos"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("pm");
            t_objSubTag.setText( t_objResult.getString("pm"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("pac");
            t_objSubTag.setText( t_objResult.getString("pac"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_mono");
            t_objSubTag.setText( t_objResult.getString("mass_mono"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_avg");
            t_objSubTag.setText( t_objResult.getString("mass_avg"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pm_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pm_mono"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pm_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pm_avg"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pdm_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pdm_mono"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pdm_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pdm_avg"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pac_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pac_mono"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pac_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pac_avg"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pdac_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pdac_mono"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pdac_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pdac_avg"));
            t_objResidue.addContent(t_objSubTag);

            t_objSubTag = new Element("increment");
            if ( t_objResult.getBoolean("increment") )
            {
                t_objSubTag.setText( "1" );
            }
            else
            {
                t_objSubTag.setText( "0" );
            }
            t_objResidue.addContent(t_objSubTag);
            
            t_objResidues.addContent(t_objResidue);
        }        
        a_objRoot.addContent(t_objResidues);
    }
    
    private void exportPersubstitutions(Element a_objRoot) throws SQLException
    {
        Element t_objPers = new Element("persubstitutions");
        Element t_objPer = null;
        Element t_objSubTag = null;
        
        ResultSet t_objResult = this.m_objDB.getPersubstitution();
        while ( t_objResult.next() )
        {
            t_objPer = new Element("persubstitution");
            
            t_objSubTag = new Element("name");
            t_objSubTag.setText( t_objResult.getString("name"));
            t_objPer.addContent(t_objSubTag);

            t_objSubTag = new Element("mono");
            t_objSubTag.setText( t_objResult.getString("mono"));
            t_objPer.addContent(t_objSubTag);

            t_objSubTag = new Element("avg");
            t_objSubTag.setText( t_objResult.getString("avg"));
            t_objPer.addContent(t_objSubTag);

            t_objSubTag = new Element("ergaenzung_nonred_mono");
            t_objSubTag.setText( t_objResult.getString("ergaenzung_nonred_mono"));
            t_objPer.addContent(t_objSubTag);

            t_objSubTag = new Element("ergaenzung_nonred_avg");
            t_objSubTag.setText( t_objResult.getString("ergaenzung_nonred_avg"));
            t_objPer.addContent(t_objSubTag);

            t_objSubTag = new Element("ergaenzung_red_mono");
            t_objSubTag.setText( t_objResult.getString("ergaenzung_red_mono"));
            t_objPer.addContent(t_objSubTag);

            t_objSubTag = new Element("ergaenzung_red_avg");
            t_objSubTag.setText( t_objResult.getString("ergaenzung_red_avg"));
            t_objPer.addContent(t_objSubTag);

            t_objSubTag = new Element("increment_mono");
            t_objSubTag.setText( t_objResult.getString("increment_mono"));
            t_objPer.addContent(t_objSubTag);

            t_objSubTag = new Element("increment_avg");
            t_objSubTag.setText( t_objResult.getString("increment_avg"));
            t_objPer.addContent(t_objSubTag);

            t_objPers.addContent(t_objPer);
        }        
        a_objRoot.addContent(t_objPers);
    }

    private void exportIons(Element a_objRoot) throws SQLException
    {
        Element t_objIons = new Element("ions");
        Element t_objIon = null;
        Element t_objSubTag = null;
        
        ResultSet t_objResult = this.m_objDB.getIons();
        while ( t_objResult.next() )
        {
            t_objIon = new Element("ion");
            
            t_objSubTag = new Element("name");
            t_objSubTag.setText( t_objResult.getString("name"));
            t_objIon.addContent(t_objSubTag);

            t_objSubTag = new Element("formula");
            t_objSubTag.setText( t_objResult.getString("formula").toLowerCase());
            t_objIon.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_mono");
            t_objSubTag.setText( t_objResult.getString("mass_mono"));
            t_objIon.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_avg");
            t_objSubTag.setText( t_objResult.getString("mass_avg"));
            t_objIon.addContent(t_objSubTag);

            t_objSubTag = new Element("charge");
            t_objSubTag.setText( t_objResult.getString("charge"));
            t_objIon.addContent(t_objSubTag);

            t_objIons.addContent(t_objIon);
        }        
        a_objRoot.addContent(t_objIons);
    }

    private void exportDericatisation(Element a_objRoot) throws SQLException
    {
        Element t_objDeris = new Element("dericatisation");
        Element t_objDeri = null;
        Element t_objSubTag = null;
        
        ResultSet t_objResult = this.m_objDB.getDerivatisations();
        while ( t_objResult.next() )
        {
            t_objDeri = new Element("derivate");
            
            t_objDeri.setAttribute("name", t_objResult.getString("name") );
            t_objDeri.setAttribute("abbr", t_objResult.getString("abbr").toLowerCase() );
            
            t_objSubTag = new Element("mass_mono");
            t_objSubTag.setText( t_objResult.getString("mass_mono"));
            t_objDeri.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_avg");
            t_objSubTag.setText( t_objResult.getString("mass_avg"));
            t_objDeri.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pm_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pm_mono"));
            t_objDeri.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pm_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pm_avg"));
            t_objDeri.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pdm_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pdm_mono"));
            t_objDeri.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pdm_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pdm_avg"));
            t_objDeri.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pac_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pac_mono"));
            t_objDeri.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pac_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pac_avg"));
            t_objDeri.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pdac_mono");
            t_objSubTag.setText( t_objResult.getString("mass_pdac_mono"));
            t_objDeri.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_pdac_avg");
            t_objSubTag.setText( t_objResult.getString("mass_pdac_avg"));
            t_objDeri.addContent(t_objSubTag);

            t_objDeris.addContent(t_objDeri);
        }        
        a_objRoot.addContent(t_objDeris);
    }

    private void exportMolecules(Element a_objRoot) throws SQLException
    {
        Element t_objMols = new Element("molecules");
        Element t_objMol = null;
        Element t_objSubTag = null;
        
        ResultSet t_objResult = this.m_objDB.getMolecules();
        while ( t_objResult.next() )
        {
            t_objMol = new Element("small_molecules");
            
            t_objSubTag = new Element("name");
            String t_String = t_objResult.getString("formula");
            t_String = t_String.replaceAll("<sub>", "");
            t_String = t_String.replaceAll("</sub>", "");
            t_objSubTag.setText( t_String.toLowerCase() );
            t_objMol.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_mono");
            t_objSubTag.setText( t_objResult.getString("mass_mono"));
            t_objMol.addContent(t_objSubTag);

            t_objSubTag = new Element("mass_avg");
            t_objSubTag.setText( t_objResult.getString("mass_avg"));
            t_objMol.addContent(t_objSubTag);

            t_objMols.addContent(t_objMol);
        }        
        a_objRoot.addContent(t_objMols);
    }
}
