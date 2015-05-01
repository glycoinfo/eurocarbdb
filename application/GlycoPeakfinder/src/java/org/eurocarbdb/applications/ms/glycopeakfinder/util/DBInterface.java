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
package org.eurocarbdb.applications.ms.glycopeakfinder.util;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.ParameterException;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.io.CalcParameterXml;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationFragment;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationParameter;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.util.MassValueStorage;
import org.eurocarbdb.applications.ms.glycopeakfinder.glycosciences.DatabaseResult;
import org.eurocarbdb.applications.ms.glycopeakfinder.glycosciences.GlycoSciencesEntry;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.Compound;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.ContactInformation;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPAnnotation;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPMolecule;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPResidue;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GlycoPeakfinderSettings;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.LimitValues;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.MassMolecule;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.MassResidue;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.ResidueCategory;
import org.jdom.JDOMException;


/**
* @author Logan
*
*/
public class DBInterface 
{
    private GlycoPeakfinderSettings m_objSettings;
    private Connection m_objDB;
    private String m_strSchema = "";
    
    public DBInterface(Configuration a_objConfig) throws ClassNotFoundException, SQLException, JDOMException
    {
        String t_strIP = a_objConfig.resultXpathSingleAttribute(
                "/configuration", "database_ip");
        String t_strUser = a_objConfig.resultXpathSingleAttribute(
                "/configuration", "database_user");
        String t_strPassword = a_objConfig.resultXpathSingleAttribute(
                "/configuration", "database_pw");
        String t_strDatabase = a_objConfig.resultXpathSingleAttribute(
                "/configuration", "database_name");
        String t_strPort = a_objConfig.resultXpathSingleAttribute(
                "/configuration", "database_port");

        Class.forName("org.postgresql.Driver");
        
        String t_strURL = "jdbc:postgresql://" + t_strIP + ":"
        + t_strPort + "/" + t_strDatabase;
        // open database connection
        this.m_objDB = DriverManager.getConnection(t_strURL, t_strUser,
                t_strPassword);
        
        this.m_strSchema = a_objConfig.resultXpathSingleAttribute(
                "/configuration", "schema_name");
    }
    
    /**
     * @param settings
     * @throws SQLException 
     */
    public void initialize(GlycoPeakfinderSettings a_objSettings) throws SQLException 
    {
        this.m_objSettings = a_objSettings;
        this.initMass();
        this.initLipids();
        this.initIonSettings();
        this.initResidues();
        this.initExamples();
        this.initLimits();
        this.m_objSettings.setInitialized(true);
    }
    
    private void initMass() throws SQLException
    {
        this.m_objSettings.setAnnotationsPerPeakValue(25);
    }
    
    /**
     * 
     */
    private void initLimits() throws SQLException
    {
        LimitValues t_objLimits = new LimitValues();
        String t_strQuery = "SELECT * FROM " + this.m_strSchema + ".settings WHERE keyword like 'limit%'";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            String t_strKey = t_objResult.getString("keyword");
            if ( t_strKey.equals("limit_accuracy_ppm") )
            {
                t_objLimits.setMaxAccuracyPPM(Double.parseDouble(t_objResult.getString("value")));
            }
            else if ( t_strKey.equals("limit_accuracy_u") )
            {
                t_objLimits.setMaxAccuracyU(Double.parseDouble(t_objResult.getString("value")));
            }
            else if ( t_strKey.equals("limit_max_gain_loss") )
            {
                t_objLimits.setMaxLossNumber(Integer.parseInt(t_objResult.getString("value")));
            }
            else if ( t_strKey.equals("limit_max_mz") )
            {
                t_objLimits.setMaxMZ(Double.parseDouble(t_objResult.getString("value")));
            }
            else if ( t_strKey.equals("limit_max_peaks") )
            {
                t_objLimits.setMaxPeakCount(Integer.parseInt(t_objResult.getString("value")));
            }
            else if ( t_strKey.equals("limit_max_glycosciences_results") )
            {
                t_objLimits.setMaxGlycosciencesResults(Integer.parseInt(t_objResult.getString("value")));
            }            
            else if ( t_strKey.equals("limit_max_annotation_per_peak") )
            {
                t_objLimits.setMaxAnnotationPerPeak(Integer.parseInt(t_objResult.getString("value")));
            }
        }
        this.m_objSettings.setLimits(t_objLimits);
    }

    private void initLipids() throws SQLException
    {
        // Fatty Acid
        ArrayList<Compound> t_aCompound = new ArrayList<Compound>();
        Compound t_objCompound;
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".lipid_fatty_acid;";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            t_objCompound = new Compound();
            t_objCompound.setId(t_objResult.getString("fatty_acid_id"));
            t_objCompound.setName(t_objResult.getString("name_eng"));
            t_objCompound.setAbbr(t_objResult.getString("abbr"));
            t_objCompound.setUsed(false);
            t_aCompound.add(t_objCompound);
        }
        this.m_objSettings.setFattyAcidList(t_aCompound);
        // Sphingosin
        t_aCompound = new ArrayList<Compound>();
        t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".lipid_sphingosin;";
        t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            t_objCompound = new Compound();
            t_objCompound.setId(t_objResult.getString("sphingosin_id"));
            t_objCompound.setName(t_objResult.getString("name"));
            t_objCompound.setUsed(false);
            t_aCompound.add(t_objCompound);
        }
        this.m_objSettings.setSphingosinList(t_aCompound);
    }
    
    /**
     * @throws SQLException 
     * 
     */
    private void initIonSettings() throws SQLException 
    {
        ArrayList<Compound> t_aCompound = new ArrayList<Compound>(); 
        Compound t_objElement;
        String t_strValue = "";
        // charge state 
        for (int t_iCounter = 1; t_iCounter < 5; t_iCounter++) 
        {
            t_strValue = String.format("%d",t_iCounter);
            t_objElement = new Compound();
            t_objElement.setAbbr(t_strValue);
            t_objElement.setName(t_strValue);
            t_objElement.setId(t_strValue);
            t_objElement.setUsed(false);
            t_aCompound.add(t_objElement);
        }
        t_aCompound.get(0).setUsed(true);
        this.m_objSettings.setChargeList(t_aCompound);
        // multi fragmentation 
        t_aCompound = new ArrayList<Compound>();
        for (int t_iCounter = 1; t_iCounter < 5; t_iCounter++) 
        {
            t_strValue = String.format("%d",t_iCounter);
            t_objElement = new Compound();
            t_objElement.setAbbr(t_strValue);
            t_objElement.setName(t_strValue);
            t_objElement.setId(t_strValue);
            t_objElement.setUsed(false);
            t_aCompound.add(t_objElement);
        }
        t_aCompound.get(0).setUsed(true);
        this.m_objSettings.setMultiFragmentationList(t_aCompound);
        // ion exchange
        t_aCompound = new ArrayList<Compound>();
        for (int t_iCounter = 1; t_iCounter < 4; t_iCounter++) 
        {
            t_strValue = String.format("%d",t_iCounter);
            t_objElement = new Compound();
            t_objElement.setAbbr(t_strValue);
            t_objElement.setName(t_strValue);
            t_objElement.setId(t_strValue);
            t_objElement.setUsed(false);
            t_aCompound.add(t_objElement);
        }
        this.m_objSettings.setIonExchangeCountList(t_aCompound);
        // fragment type
        t_aCompound = new ArrayList<Compound>();
        t_objElement = new Compound();
        t_objElement.setAbbr("A");
        t_objElement.setName("A");
        t_objElement.setId("A");
        t_objElement.setUsed(false);
        t_aCompound.add(t_objElement);
        t_objElement = new Compound();
        t_objElement.setAbbr("B");
        t_objElement.setName("B");
        t_objElement.setId("B");
        t_objElement.setUsed(false);
        t_aCompound.add(t_objElement);
        t_objElement = new Compound();
        t_objElement.setAbbr("C");
        t_objElement.setName("C");
        t_objElement.setId("C");
        t_objElement.setUsed(true);
        t_aCompound.add(t_objElement);
        t_objElement = new Compound();
        t_objElement.setAbbr("X");
        t_objElement.setName("X");
        t_objElement.setId("X");
        t_objElement.setUsed(false);
        t_aCompound.add(t_objElement);
        t_objElement = new Compound();
        t_objElement.setAbbr("Y");
        t_objElement.setName("Y");
        t_objElement.setId("Y");
        t_objElement.setUsed(true);
        t_aCompound.add(t_objElement);
        t_objElement = new Compound();
        t_objElement.setAbbr("Z");
        t_objElement.setName("Z");
        t_objElement.setId("Z");
        t_objElement.setUsed(false);
        t_aCompound.add(t_objElement);
        // TODO Fragmenttype adding
        t_objElement = new Compound();
        t_objElement.setAbbr("E");
        t_objElement.setName("E");
        t_objElement.setId("E");
        t_objElement.setUsed(false);
        t_aCompound.add(t_objElement);
        
        t_objElement = new Compound();
        t_objElement.setAbbr("F");
        t_objElement.setName("F");
        t_objElement.setId("F");
        t_objElement.setUsed(false);
        t_aCompound.add(t_objElement);
        
        t_objElement = new Compound();
        t_objElement.setAbbr("G");
        t_objElement.setName("G");
        t_objElement.setId("G");
        t_objElement.setUsed(false);
        t_aCompound.add(t_objElement);
        
        t_objElement = new Compound();
        t_objElement.setAbbr("H");
        t_objElement.setName("H");
        t_objElement.setId("H");
        t_objElement.setUsed(false);
        t_aCompound.add(t_objElement);

        this.m_objSettings.setFragmentTypeList(t_aCompound);
        // ions
        t_aCompound = new ArrayList<Compound>();
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".ions ORDER BY \"order\";";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            t_objElement = new Compound();
            t_objElement.setAbbr( t_objResult.getString("formula") );
            t_objElement.setName( t_objResult.getString("formula") );
            t_objElement.setId( t_objResult.getString("formula") );
            if ( t_objResult.getBoolean("default_usage") )
            {
                t_objElement.setUsed(true);
            }
            else
            {
                t_objElement.setUsed(false);
            }
            t_aCompound.add(t_objElement);
        }
        this.m_objSettings.setIonList(t_aCompound);    
        // ion exchange ions
        t_aCompound = new ArrayList<Compound>();
        t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".ions WHERE exchange=true ORDER BY \"order\";";
        t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            t_objElement = new Compound();
            t_objElement.setAbbr( t_objResult.getString("formula") );
            t_objElement.setName( t_objResult.getString("formula") );
            t_objElement.setId( t_objResult.getString("formula") );
            t_objElement.setUsed(false);
            t_aCompound.add(t_objElement);
        }
        this.m_objSettings.setIonExchangeIonList(t_aCompound);    
        // loss / gain molecules
        ArrayList<MassMolecule> t_aMolecules = new ArrayList<MassMolecule>();
        MassMolecule t_objMolecule;
        t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".small_molecules ORDER BY \"order\";";
        t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            t_objMolecule = new MassMolecule();
            t_objMolecule.setAbbr(t_objResult.getString("formula"));
            t_objMolecule.setName(t_objResult.getString("name"));
            t_objMolecule.setMassAvg(t_objResult.getDouble("mass_avg"));
            t_objMolecule.setMassMono(t_objResult.getDouble("mass_mono"));
            t_objMolecule.setId(t_objResult.getString("small_molecules_id"));
            t_objMolecule.setGain(t_objResult.getInt("gain"));
            t_objMolecule.setLoss(t_objResult.getInt("loss"));
            t_aMolecules.add(t_objMolecule);
        }
        this.m_objSettings.setLossGainMolecules(t_aMolecules);
        GPMolecule t_objMoleculeOther = new GPMolecule();
        t_objMoleculeOther.setAbbr("OM");
        t_objMoleculeOther.setName("OM");
        this.m_objSettings.setOtherLossGainMolecule(t_objMoleculeOther);
        //
        this.m_objSettings.setIonBool("true");
    }
    
    /**
     * @throws SQLException 
     * 
     */
    private void initResidues() throws SQLException 
    {
        ArrayList<ResidueCategory> t_aCategory = new ArrayList<ResidueCategory>();
        ResidueCategory t_objCategory;
        ArrayList<MassResidue> t_aResidues;
        // get categories
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".categorie ORDER BY \"order\";";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            t_objCategory = new ResidueCategory();
            t_objCategory.setName(t_objResult.getString("name"));
            t_objCategory.setId(t_objResult.getString("categorie_id"));
            t_aResidues = this.getResidues(t_objResult.getString("categorie_id"));
            if ( t_aResidues.size() > 0 )
            {
                t_objCategory.setResidues(t_aResidues);
                t_aCategory.add(t_objCategory);
            }
        }
        this.m_objSettings.setCategorie(t_aCategory);
        // motifs
        t_strQuery = "SELECT * FROM " + this.m_strSchema + ".residue_motif ORDER BY \"order\"";
        t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objResult = t_objStatement.executeQuery();
        ArrayList<Compound> t_aMotifs = new ArrayList<Compound>();
        Compound t_objCompound = null;
        Integer t_iDefault = null;
        while ( t_objResult.next() )
        {
            t_objCompound = new Compound();
            t_objCompound.setId( t_objResult.getString("residue_motif_id"));
            t_objCompound.setName( t_objResult.getString("name"));
            t_objCompound.setAbbr( t_objResult.getString("name") );
            if ( t_objResult.getBoolean("profile") )
            {
                t_objCompound.setUsed(true);
            }
            else
            {
                t_objCompound.setUsed(false);
            }
            if ( t_objResult.getBoolean("default") )
            {
                t_iDefault = t_objResult.getInt("residue_motif_id");                
            }
            t_aMotifs.add(t_objCompound);
        }
        this.m_objSettings.setMotifs(t_aMotifs);
        // default motif
        if ( t_iDefault != null )
        {
            t_strQuery = "SELECT * FROM " + this.m_strSchema + ".residue_motif as rr LEFT JOIN " + this.m_strSchema
                + ".residue_to_residue_motif as rm ON rr.residue_motif_id=rm.residue_motif_id WHERE rr.\"default\"=true ";
            if ( this.m_objSettings.getSpectraType().equals("profile") )
            {
                t_strQuery += " AND profile=true"; 
            }
            else
            {
                t_strQuery += " AND profile=false";
            }
            t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
            t_objResult = t_objStatement.executeQuery();
            while ( t_objResult.next() )
            {
                String t_strResidueID = t_objResult.getString("residue_id");
                for (Iterator<ResidueCategory> t_iterCategorie = this.m_objSettings.getCategorie().iterator(); t_iterCategorie.hasNext();) 
                {
                    for (Iterator<MassResidue> t_iterResidue = t_iterCategorie.next().getResidues().iterator(); t_iterResidue.hasNext();) 
                    {
                        MassResidue t_objResidue = t_iterResidue.next();
                        if ( t_objResidue.getId().equals(t_strResidueID) )
                        {
                            t_objResidue.setMin( t_objResult.getInt("min"));
                            t_objResidue.setMax( t_objResult.getInt("max"));
                        }
                    }                    
                }
            }
        }
    }
    
    /**
     * @param string
     * @return
     * @throws SQLException 
     */
    private ArrayList<MassResidue> getResidues(String a_strCategorie) throws SQLException 
    {
        Double t_dIncrementNoneMono = 0.0;
        Double t_dIncrementNoneAvg = 0.0;
        Double t_dIncrementPmeMono = 0.0;
        Double t_dIncrementPmeAvg = 0.0;
        Double t_dIncrementPdmeMono = 0.0;
        Double t_dIncrementPdmeAvg = 0.0;
        Double t_dIncrementPacMono = 0.0;
        Double t_dIncrementPacAvg = 0.0;
        Double t_dIncrementPdacMono = 0.0;
        Double t_dIncrementPdacAvg = 0.0;
        // get all incrementmasses
        String t_strQuery = "SELECT * FROM " + this.m_strSchema + ".persubstitution"; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            if ( t_objResult.getString("name").equalsIgnoreCase("none") )
            {
                t_dIncrementNoneMono    = t_objResult.getDouble("increment_mono");
                t_dIncrementNoneAvg        = t_objResult.getDouble("increment_avg");
            }
            else if (t_objResult.getString("name").equalsIgnoreCase("pme")) 
            {
                t_dIncrementPmeMono    = t_objResult.getDouble("increment_mono");
                t_dIncrementPmeAvg        = t_objResult.getDouble("increment_avg");
            }
            else if (t_objResult.getString("name").equalsIgnoreCase("pdme")) 
            {
                t_dIncrementPdmeMono    = t_objResult.getDouble("increment_mono");
                t_dIncrementPdmeAvg        = t_objResult.getDouble("increment_avg");
            }
            else if (t_objResult.getString("name").equalsIgnoreCase("pac")) 
            {
                t_dIncrementPacMono    = t_objResult.getDouble("increment_mono");
                t_dIncrementPacAvg        = t_objResult.getDouble("increment_avg");
            }
            else if (t_objResult.getString("name").equalsIgnoreCase("pdac")) 
            {
                t_dIncrementPdacMono    = t_objResult.getDouble("increment_mono");
                t_dIncrementPdacAvg        = t_objResult.getDouble("increment_avg");
            }
        }
        ArrayList<MassResidue> t_aResidues = new ArrayList<MassResidue>();
        MassResidue t_objResidue;
        t_strQuery = "SELECT * FROM " + this.m_strSchema + ".residue_to_categorie as rtc, " + 
        this.m_strSchema + ".residue as r WHERE rtc.residue_id = r.residue_id AND categorie_id = ? ORDER BY \"order\"";
        t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setInt(1, Integer.parseInt(a_strCategorie));
        t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            t_objResidue = new MassResidue();
            if ( t_objResult.getBoolean("ax") )
            {
                t_objResidue.setHasAx(true);
                t_objResidue.setListAx(this.getAX( t_objResult.getString("residue_id"),t_objResult.getString("abbr") ));
            }
            else
            {
                t_objResidue.setHasAx(false);
                t_objResidue.setListAx(new ArrayList<Compound>());
            }
            if ( t_objResult.getBoolean("e") )
            {
                t_objResidue.setHasE(true);
            }
            else
            {
                t_objResidue.setHasE(false);
            }
            if ( t_objResult.getBoolean("f") )
            {
                t_objResidue.setHasF(true);
            }
            else
            {
                t_objResidue.setHasF(false);
            }
            if ( t_objResult.getBoolean("g") )
            {
                t_objResidue.setHasG(true);
            }
            else
            {
                t_objResidue.setHasG(false);
            }
            if ( t_objResult.getBoolean("h") )
            {
                t_objResidue.setHasH(true);
            }
            else
            {
                t_objResidue.setHasH(false);
            }
            t_objResidue.setId(t_objResult.getString("residue_id"));
            t_objResidue.setMin(0);
            t_objResidue.setMax(0);
            t_objResidue.setName(t_objResult.getString("name"));
            t_objResidue.setAbbr(t_objResult.getString("abbr"));
            t_objResidue.setUseAX(true);
            t_objResidue.setUseE(true);
            t_objResidue.setUseF(true);
            t_objResidue.setUseG(true);
            t_objResidue.setUseH(true);
            // fill masses
            // TODO: Zu statisch
            HashMap<String,Double> t_hashMasses = new HashMap<String,Double>(); 
            if ( !t_objResult.getBoolean("increment") )
            {
                t_hashMasses.put("mass_none_mono",     t_objResult.getDouble("mass_mono")         - t_dIncrementNoneMono );
                t_hashMasses.put("mass_none_avg",     t_objResult.getDouble("mass_avg")         - t_dIncrementNoneAvg );
                t_hashMasses.put("mass_pme_mono",     t_objResult.getDouble("mass_pm_mono")     - t_dIncrementPmeMono );
                t_hashMasses.put("mass_pme_avg",     t_objResult.getDouble("mass_pm_avg")     - t_dIncrementPmeAvg );
                t_hashMasses.put("mass_pdme_mono",     t_objResult.getDouble("mass_pdm_mono")     - t_dIncrementPdmeMono );
                t_hashMasses.put("mass_pdme_avg",     t_objResult.getDouble("mass_pdm_avg")     - t_dIncrementPdmeAvg );
                t_hashMasses.put("mass_pac_mono",     t_objResult.getDouble("mass_pac_mono")     - t_dIncrementPacMono );
                t_hashMasses.put("mass_pac_avg",     t_objResult.getDouble("mass_pac_avg")     - t_dIncrementPacAvg );
                t_hashMasses.put("mass_pdac_mono",     t_objResult.getDouble("mass_pdac_mono") - t_dIncrementPdacMono );
                t_hashMasses.put("mass_pdac_avg",     t_objResult.getDouble("mass_pdac_avg")     - t_dIncrementPdacAvg );
            }
            else
            {
                t_hashMasses.put("mass_none_mono",     t_objResult.getDouble("mass_mono") );
                t_hashMasses.put("mass_none_avg",     t_objResult.getDouble("mass_avg") );
                t_hashMasses.put("mass_pme_mono",     t_objResult.getDouble("mass_pm_mono") );
                t_hashMasses.put("mass_pme_avg",     t_objResult.getDouble("mass_pm_avg") );
                t_hashMasses.put("mass_pdme_mono",     t_objResult.getDouble("mass_pdm_mono") );
                t_hashMasses.put("mass_pdme_avg",     t_objResult.getDouble("mass_pdm_avg") );
                t_hashMasses.put("mass_pac_mono",     t_objResult.getDouble("mass_pac_mono") );
                t_hashMasses.put("mass_pac_avg",     t_objResult.getDouble("mass_pac_avg") );
                t_hashMasses.put("mass_pdac_mono",     t_objResult.getDouble("mass_pdac_mono") );
                t_hashMasses.put("mass_pdac_avg",     t_objResult.getDouble("mass_pdac_avg") );
            }
            t_objResidue.setResidueMasses(t_hashMasses);
            t_aResidues.add(t_objResidue);
        }        
        return t_aResidues;
    }
    
    /**
     * @param string
     * @return
     * @throws SQLException 
     */
    private ArrayList<Compound> getAX(String a_strResidueID, String a_strResName) throws SQLException 
    {
        ArrayList<Compound> t_aAX = new ArrayList<Compound>();
        Compound t_objElement;
        String t_strQuery = "SELECT * FROM " + this.m_strSchema + 
        ".fragment_ax as fax WHERE fax.residue_id = ? ORDER BY fax.mass_mono ";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setInt(1, Integer.parseInt(a_strResidueID));
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            t_objElement = new Compound();
            String t_strFragment = t_objResult.getString("type") + 
            t_objResult.getString("cleav_one") + "," + t_objResult.getString("cleav_two"); 
            t_objElement.setAbbr(t_strFragment);
            t_objElement.setName(a_strResName);
            t_objElement.setUsed(true);
            t_objElement.setId(t_strFragment);
            t_aAX.add(t_objElement);
        }
        return t_aAX;
    }
    
    /**
     * @param information
     * @throws SQLException 
     */
    public void writeContact(ContactInformation a_objInformation) throws SQLException 
    {
        String t_sqlQuery = "INSERT INTO " + this.m_strSchema+ ".contact ( name , email , \"type\" , content , date, subject ) ";
        t_sqlQuery += " VALUES ( ? , ? , ? , ? , now() , ? )";
        
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_sqlQuery) ;
        t_objStatement.setString( 1 , a_objInformation.getName() );
        t_objStatement.setString( 2 , a_objInformation.getEmail() );
        t_objStatement.setString( 3 , a_objInformation.getType() );
        t_objStatement.setString( 4 , a_objInformation.getContent() );
        t_objStatement.setString( 5 , a_objInformation.getSubject() );
        t_objStatement.executeUpdate();
    }

    public DatabaseResult performeQueryGlycosciences(GPAnnotation a_objResidues, int a_iMaxIds) throws SQLException, GlycoPeakfinderException
    {
        String t_sqlQuery = "SELECT \"LinucsID\" FROM " + this.m_strSchema + ".glycosciences_composition_search WHERE \"NeuAc\"=? AND \"NeuGc\"=? AND \"Hex\"=? AND \"HexMe\"=? AND \"dHex\"=? AND \"HexA\"=? AND \"HexNAc\"=? AND \"Pent\"=? AND \"P\"=? AND \"KDO\"=? ORDER BY \"LinucsID\""; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_sqlQuery) ;
        DatabaseResult t_objResult = new DatabaseResult();
        t_objResult.setComposition(a_objResidues.getComposition());
        t_objResult.setMass(a_objResidues.getMass());
        int t_iNeuAc = 0;
        int t_iNeuGc = 0;
        int t_iHex = 0;
        int t_iHexMe = 0;
        int t_iDHex = 0;
        int t_iHexA = 0;
        int t_iHexNac = 0;
        int t_iPen = 0;
        int t_iP = 0;
        int t_iKDO = 0;
        ArrayList<GPResidue> t_aResidues = a_objResidues.getResidues();
        String t_strResidue = "";
        GPResidue t_objResidue;
        if ( t_aResidues == null )
        {
            throw new GlycoPeakfinderException("Critical error. No composition available.");
        }
        for (Iterator<GPResidue> t_iterResidue = t_aResidues.iterator(); t_iterResidue.hasNext();) 
        {
        t_objResidue = t_iterResidue.next();
        t_strResidue = t_objResidue.getName();
            if ( t_strResidue.equalsIgnoreCase("Neu5AC") )
            {
                t_iNeuAc = t_objResidue.getMin();
            }
            else if (t_strResidue.equalsIgnoreCase("Neu5GC")) 
            {
                t_iNeuGc = t_objResidue.getMin();
            }
            else if (t_strResidue.equalsIgnoreCase("Hex")) 
            {
                t_iHex = t_objResidue.getMin();
            }
            else if (t_strResidue.equalsIgnoreCase("HexA")) 
            {
                t_iHexA = t_objResidue.getMin();
            }
            else if (t_strResidue.equalsIgnoreCase("MeHex")) 
            {
                t_iHexMe = t_objResidue.getMin();
            }
            else if (t_strResidue.equalsIgnoreCase("dHex")) 
            {
                t_iDHex = t_objResidue.getMin();
            }
            else if (t_strResidue.equalsIgnoreCase("HexNAc")) 
            {
                t_iHexNac = t_objResidue.getMin();
            }
            else if (t_strResidue.equalsIgnoreCase("Pen")) 
            {
                t_iPen = t_objResidue.getMin();
            }
            else if (t_strResidue.equalsIgnoreCase("P")) 
            {
                t_iP = t_objResidue.getMin();
            }
            else if (t_strResidue.equalsIgnoreCase("KDO")) 
            {
                t_iKDO = t_objResidue.getMin();
            }
            else
            {
                t_objResult.setError( String.format( ErrorTextEnglish.WRONG_RESIDUE_FOR_DB ,t_strResidue) );
                return t_objResult;
            }
        }
        t_objStatement.setInt( 1 , t_iNeuAc );
        t_objStatement.setInt( 2 , t_iNeuGc );
        t_objStatement.setInt( 3 , t_iHex );            
        t_objStatement.setInt( 4 , t_iHexMe );
        t_objStatement.setInt( 5 , t_iDHex );
        t_objStatement.setInt( 6 , t_iHexA );
        t_objStatement.setInt( 7 , t_iHexNac );
        t_objStatement.setInt( 8 , t_iPen );
        t_objStatement.setInt( 9 , t_iP );
        t_objStatement.setInt( 10 , t_iKDO );            


        ResultSet t_objDBResult = t_objStatement.executeQuery();
        ArrayList<Integer> t_aResult = new ArrayList<Integer>();
        while ( t_objDBResult.next() )
        {
            t_aResult.add( t_objDBResult.getInt("LinucsID") );
        }
        t_objResult.setIds(t_aResult);
        double t_dPages = t_aResult.size() / ((double) a_iMaxIds );
        t_aResult = new ArrayList<Integer>();
        int t_iCounter = 1;
        if ( t_dPages > 0 )
        {
            for (t_iCounter = 1; t_iCounter <= t_dPages; t_iCounter++) 
            {
                t_aResult.add(t_iCounter);
            }
            t_aResult.add(t_iCounter);
        }
        t_objResult.setPages(t_aResult);
        t_objResult.setLastPage(t_iCounter);
        t_objResult.setCurrentPage(1);
        t_objResult.setDatabase("glycosciences");
        return t_objResult;
    }
    
    /**
     * @param string
     * @return
     * @throws SQLException 
     */
    public String getLinucs(String a_strID) throws SQLException
    {
        String t_strResult = "";
        String t_sqlQuery = "SELECT * FROM " + this.m_strSchema + ".glycosciences_composition_search WHERE \"LinucsID\"=?";; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_sqlQuery) ;
        t_objStatement.setInt(1, Integer.parseInt(a_strID));
        ResultSet t_objDBResult = t_objStatement.executeQuery();
        if (t_objDBResult.next() )
        {
            t_strResult = t_objDBResult.getString("LinucsCode");
        }
        return t_strResult;
    }

    /**
     * @param string
     * @return
     * @throws SQLException 
     */
    public String getIupac(String a_strID) throws SQLException
    {
        String t_strResult = "";
        String t_sqlQuery = "SELECT * FROM " + this.m_strSchema + ".glycosciences_composition_search WHERE \"LinucsID\"=?"; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_sqlQuery) ;
        t_objStatement.setInt( 1 , Integer.parseInt(a_strID) );
        ResultSet t_objDBResult = t_objStatement.executeQuery();
        if (t_objDBResult.next() )
        {
            t_strResult = t_objDBResult.getString("Structure");
        }
        return t_strResult;
    }

    /**
     * @throws SQLException 
     * 
     */
    private void initExamples() throws SQLException 
    {
        ArrayList<Compound> t_aCompound = new ArrayList<Compound>();
        Compound t_objCompound;
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".examples ORDER BY example_id";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            t_objCompound = new Compound();
            t_objCompound.setId(t_objResult.getString("example_id"));
            t_objCompound.setName(t_objResult.getString("name"));
            t_aCompound.add(t_objCompound);
        }
        this.m_objSettings.setExamples(t_aCompound);
    }

    /**
     * @param string
     * @return
     * @throws SQLException 
     */
    public String getExample(String a_strID) throws SQLException 
    {
        String t_strResult = "";
        String t_sqlQuery = "SELECT * FROM " + this.m_strSchema + ".examples WHERE example_id=?";; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_sqlQuery) ;
        t_objStatement.setInt( 1 , Integer.parseInt(a_strID) );
        ResultSet t_objDBResult = t_objStatement.executeQuery();
        if (t_objDBResult.next() )
        {
            t_strResult = t_objDBResult.getString("values");
        }
        return t_strResult;
    }

    public double getAsSequenceMass(String a_strSequence, boolean a_bMonoIsotopic) throws ParameterException,SQLException 
    {
        int t_iLength = a_strSequence.length();
        double t_dMass = 0;
        String t_strQuery = "SELECT * FROM " + this.m_strSchema + ".amino_acids as d WHERE d.amino_abbr_1=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        for (int t_iCounter = 0; t_iCounter < t_iLength; t_iCounter++) 
        {
            t_objStatement.setString( 1 , a_strSequence.substring(t_iCounter,t_iCounter+1) );
            ResultSet t_objResult = t_objStatement.executeQuery();
            if ( t_objResult.next() )
            {
                if ( a_bMonoIsotopic )
                {
                    t_dMass += t_objResult.getDouble("amino_incr_mono");
                }
                else
                {
                    t_dMass += t_objResult.getDouble("amino_incr_avg");
                }
            }
            else
            {
                throw new ParameterException("Unknown amino acid: " + a_strSequence.substring(t_iCounter,t_iCounter+1));
            }
        }
        return t_dMass;
    }

    public double getSphingosin(String a_strName, boolean a_bMonoIsotopic) throws SQLException, ParameterException 
    {
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".lipid_sphingosin as s WHERE s.name=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString(1, a_strName);
        ResultSet t_objResult = t_objStatement.executeQuery();
        if ( t_objResult.next() )
        {
            if ( a_bMonoIsotopic )
            {
                return t_objResult.getDouble("mass_mono");
            }
            else
            {
                return t_objResult.getDouble("mass_avg");
            }
        }
        throw new ParameterException("Unknown sphingosin : " + a_strName);
    }

    /**
     * @param t_strid
     * @param monoIsotopic
     * @return
     * @throws SQLException 
     * @throws GlycoPeakfinderException 
     */
    public double getFattyAcid(String a_strName, boolean a_bMonoIsotopic) throws SQLException, ParameterException 
    {
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".lipid_fatty_acid as s WHERE s.name_eng=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString(1, a_strName);
        ResultSet t_objResult = t_objStatement.executeQuery();
        if ( t_objResult.next() )
        {
            if ( a_bMonoIsotopic )
            {
                return t_objResult.getDouble("mass_mono");
            }
            else
            {
                return t_objResult.getDouble("mass_avg");
            }
        }
        throw new ParameterException("Unknown lipid : " + a_strName);
    }

    public void addA(MassResidue a_objResidue, ArrayList<CalculationFragment> a_aFragments,Persubstitution a_enumPersubst, boolean a_bMonoisotopic,double a_dDiff) throws SQLException 
    {
        String t_strKey = "mass_";
        if ( a_enumPersubst == Persubstitution.Me )
        {
            t_strKey += "pm_";
        }
        else if ( a_enumPersubst == Persubstitution.DMe ) 
        {
            t_strKey += "pdm_";
            
        }
        else if ( a_enumPersubst == Persubstitution.Ac ) 
        {
            t_strKey += "pac_";
            
        }
        else if ( a_enumPersubst == Persubstitution.DAc ) 
        {
            t_strKey += "pdac_";
            
        }            
        if ( a_bMonoisotopic )
        {
            t_strKey += "mono";
        }
        else
        {
            t_strKey += "avg";
        }
        String t_strQuery = "SELECT f.* FROM " + this.m_strSchema + ".residue as r , " 
        + this.m_strSchema + ".fragment_ax as f WHERE r.abbr=? AND r.residue_id=f.residue_id AND f.type='A'"; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString( 1 , a_objResidue.getAbbr() );
        ResultSet t_objResult = t_objStatement.executeQuery();
        CalculationFragment t_objFragment = null;
        while ( t_objResult.next() )
        {
            t_objFragment = new CalculationFragment();
            t_objFragment.setId( String.format("<sup>%s,%s</sup>A<sub>%s</sub>", 
                    t_objResult.getString("cleav_one"),
                    t_objResult.getString("cleav_two"),
                    a_objResidue.getAbbr()) );
            t_objFragment.setFragmentType("A");
            t_objFragment.setResidueId(a_objResidue.getAbbr());
            t_objFragment.setMass(t_objResult.getDouble(t_strKey) + a_dDiff);
            a_aFragments.add(t_objFragment);
        }        
    }
    
    public void addX(MassResidue a_objResidue, ArrayList<CalculationFragment> a_aFragments,Persubstitution a_enumPersubst, boolean a_bMonoisotopic,double a_dDiff) throws SQLException 
    {
        String t_strKey = "mass_";
        if ( a_enumPersubst == Persubstitution.Me )
        {
            t_strKey += "pm_";
        }
        else if ( a_enumPersubst == Persubstitution.DMe ) 
        {
            t_strKey += "pdm_";
            
        }
        else if ( a_enumPersubst == Persubstitution.Ac ) 
        {
            t_strKey += "pac_";
            
        }
        else if ( a_enumPersubst == Persubstitution.DAc ) 
        {
            t_strKey += "pdac_";
            
        }            
        if ( a_bMonoisotopic )
        {
            t_strKey += "mono";
        }
        else
        {
            t_strKey += "avg";
        }
        String t_strQuery = "SELECT f.* FROM " + this.m_strSchema + ".residue as r , " 
        + this.m_strSchema + ".fragment_ax as f WHERE r.abbr=? AND r.residue_id=f.residue_id AND f.type='X'"; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString( 1 , a_objResidue.getAbbr() );
        ResultSet t_objResult = t_objStatement.executeQuery();
        CalculationFragment t_objFragment = null;
        while ( t_objResult.next() )
        {
            t_objFragment = new CalculationFragment();
            t_objFragment.setId( String.format("<sup>%s,%s</sup>X<sub>%s</sub>", 
                    t_objResult.getString("cleav_one"),
                    t_objResult.getString("cleav_two"),
                    a_objResidue.getAbbr()) );
            t_objFragment.setFragmentType("X");
            t_objFragment.setResidueId(a_objResidue.getAbbr());
            t_objFragment.setMass(t_objResult.getDouble(t_strKey) + a_dDiff);
            a_aFragments.add(t_objFragment);
        }        
    }

    public void addE(MassResidue a_objResidue, ArrayList<CalculationFragment> a_aFragments,Persubstitution a_enumPersubst, boolean a_bMonoisotopic,MassValueStorage a_objMasses) throws ParameterException, Exception 
    {
        String t_strKey = "mass_";
        if ( a_enumPersubst == Persubstitution.Me )
        {
            t_strKey += "pm_";
        }
        else if ( a_enumPersubst == Persubstitution.DMe ) 
        {
            t_strKey += "pdm_";
            
        }
        else if ( a_enumPersubst == Persubstitution.Ac ) 
        {
            t_strKey += "pac_";
            
        }
        else if ( a_enumPersubst == Persubstitution.DAc ) 
        {
            t_strKey += "pdac_";
            
        }            
        if ( a_bMonoisotopic )
        {
            t_strKey += "mono";
        }
        else
        {
            t_strKey += "avg";
        }
        String t_strQuery = "SELECT f.* FROM " + this.m_strSchema + ".residue as r , " 
        + this.m_strSchema + ".fragment_other as f WHERE r.abbr=? AND r.residue_id=f.residue_id AND f.type='E'"; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString( 1 , a_objResidue.getAbbr() );
        ResultSet t_objResult = t_objStatement.executeQuery();
        CalculationFragment t_objFragment = null;
        while ( t_objResult.next() )
        {
            t_objFragment = new CalculationFragment();
            t_objFragment.setId( String.format("E<sub>%s</sub>", 
                    a_objResidue.getAbbr()) );
            t_objFragment.setFragmentType("E");
            t_objFragment.setResidueId(a_objResidue.getAbbr());
            t_objFragment.setMass(a_objMasses.getResidueFragmentMass("E", a_enumPersubst, a_bMonoisotopic, a_objResidue.getAbbr()));
            a_aFragments.add(t_objFragment);
        }        
    }
    
    public void addF(MassResidue a_objResidue, ArrayList<CalculationFragment> a_aFragments,Persubstitution a_enumPersubst, boolean a_bMonoisotopic,MassValueStorage a_objMasses) throws ParameterException, Exception 
    {
        String t_strKey = "mass_";
        if ( a_enumPersubst == Persubstitution.Me )
        {
            t_strKey += "pm_";
        }
        else if ( a_enumPersubst == Persubstitution.DMe ) 
        {
            t_strKey += "pdm_";
            
        }
        else if ( a_enumPersubst == Persubstitution.Ac ) 
        {
            t_strKey += "pac_";
            
        }
        else if ( a_enumPersubst == Persubstitution.DAc ) 
        {
            t_strKey += "pdac_";
            
        }            
        if ( a_bMonoisotopic )
        {
            t_strKey += "mono";
        }
        else
        {
            t_strKey += "avg";
        }
        String t_strQuery = "SELECT f.* FROM " + this.m_strSchema + ".residue as r , " 
        + this.m_strSchema + ".fragment_other as f WHERE r.abbr=? AND r.residue_id=f.residue_id AND f.type='F'"; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString( 1 , a_objResidue.getAbbr() );
        ResultSet t_objResult = t_objStatement.executeQuery();
        CalculationFragment t_objFragment = null;
        while ( t_objResult.next() )
        {
            t_objFragment = new CalculationFragment();
            t_objFragment.setId( String.format("F<sub>%s</sub>", 
                    a_objResidue.getAbbr()) );
            t_objFragment.setFragmentType("F");
            t_objFragment.setResidueId(a_objResidue.getAbbr());
            t_objFragment.setMass(a_objMasses.getResidueFragmentMass("F", a_enumPersubst, a_bMonoisotopic, a_objResidue.getAbbr()));
            a_aFragments.add(t_objFragment);
        }        
    }

    public void addG(MassResidue a_objResidue, ArrayList<CalculationFragment> a_aFragments,Persubstitution a_enumPersubst, boolean a_bMonoisotopic,MassValueStorage a_objMasses) throws ParameterException, Exception 
    {
        String t_strKey = "mass_";
        if ( a_enumPersubst == Persubstitution.Me )
        {
            t_strKey += "pm_";
        }
        else if ( a_enumPersubst == Persubstitution.DMe ) 
        {
            t_strKey += "pdm_";
            
        }
        else if ( a_enumPersubst == Persubstitution.Ac ) 
        {
            t_strKey += "pac_";
            
        }
        else if ( a_enumPersubst == Persubstitution.DAc ) 
        {
            t_strKey += "pdac_";
            
        }            
        if ( a_bMonoisotopic )
        {
            t_strKey += "mono";
        }
        else
        {
            t_strKey += "avg";
        }
        String t_strQuery = "SELECT f.* FROM " + this.m_strSchema + ".residue as r , " 
        + this.m_strSchema + ".fragment_other as f WHERE r.abbr=? AND r.residue_id=f.residue_id AND f.type='G'"; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString( 1 , a_objResidue.getAbbr() );
        ResultSet t_objResult = t_objStatement.executeQuery();
        CalculationFragment t_objFragment = null;
        while ( t_objResult.next() )
        {
            t_objFragment = new CalculationFragment();
            t_objFragment.setId( String.format("G<sub>%s</sub>", 
                    a_objResidue.getAbbr()) );
            t_objFragment.setFragmentType("G");
            t_objFragment.setResidueId(a_objResidue.getAbbr());
            t_objFragment.setMass(a_objMasses.getResidueFragmentMass("G", a_enumPersubst, a_bMonoisotopic, a_objResidue.getAbbr()));
            a_aFragments.add(t_objFragment);
        }        
    }

    public void addH(MassResidue a_objResidue, ArrayList<CalculationFragment> a_aFragments,Persubstitution a_enumPersubst, boolean a_bMonoisotopic,MassValueStorage a_objMasses) throws ParameterException, Exception 
    {
        String t_strKey = "mass_";
        if ( a_enumPersubst == Persubstitution.Me )
        {
            t_strKey += "pm_";
        }
        else if ( a_enumPersubst == Persubstitution.DMe ) 
        {
            t_strKey += "pdm_";
            
        }
        else if ( a_enumPersubst == Persubstitution.Ac ) 
        {
            t_strKey += "pac_";
            
        }
        else if ( a_enumPersubst == Persubstitution.DAc ) 
        {
            t_strKey += "pdac_";
            
        }            
        if ( a_bMonoisotopic )
        {
            t_strKey += "mono";
        }
        else
        {
            t_strKey += "avg";
        }
        String t_strQuery = "SELECT f.* FROM " + this.m_strSchema + ".residue as r , " 
        + this.m_strSchema + ".fragment_other as f WHERE r.abbr=? AND r.residue_id=f.residue_id AND f.type='H'"; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString( 1 , a_objResidue.getAbbr() );
        ResultSet t_objResult = t_objStatement.executeQuery();
        CalculationFragment t_objFragment = null;
        while ( t_objResult.next() )
        {
            t_objFragment = new CalculationFragment();
            t_objFragment.setId( String.format("H<sub>%s</sub>", 
                    a_objResidue.getAbbr()) );
            t_objFragment.setFragmentType("H");
            t_objFragment.setResidueId(a_objResidue.getAbbr());
            t_objFragment.setMass(a_objMasses.getResidueFragmentMass("H", a_enumPersubst, a_bMonoisotopic, a_objResidue.getAbbr()));
            a_aFragments.add(t_objFragment);
        }        
    }

    public int insertCalculation(String a_strThread, CalculationParameter a_objParamter,long a_iStartzeit) throws SQLException, IOException 
    {
        String t_strQuery = "INSERT INTO " + this.m_strSchema+ ".calculation ( thread_id , parameter , time_calculation_start , progress , parameter_length ) ";
        t_strQuery += " VALUES ( ? , ? , ?, 0 , ?)";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery ) ;
        t_objStatement.setString( 1 , a_strThread );
        CalcParameterXml t_objXML = new CalcParameterXml();
        String t_strParameter = t_objXML.exportParameter(a_objParamter);
        t_objStatement.setString( 2 , t_strParameter );
        t_objStatement.setTimestamp(3 , new Timestamp(a_iStartzeit) );
        t_objStatement.setInt(4 , t_strParameter.length() );
        t_objStatement.executeUpdate();
        t_strQuery = "SELECT calculation_id FROM " + this.m_strSchema + ".calculation WHERE time_calculation_start=? AND parameter=?";
        t_objStatement = this.m_objDB.prepareStatement( t_strQuery ) ;
        t_objStatement.setTimestamp(1 , new Timestamp(a_iStartzeit) );
        t_objStatement.setString(2, t_strParameter);
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            return t_objResult.getInt("calculation_id");
        }
        throw new SQLException("Data was not inserted.");
    }

    public void updateCalculation(int a_iID,CalculationParameter a_objResult,long a_iTime, long a_iOperationTime) throws SQLException, IOException
    {
        String t_strQuery = "UPDATE " + this.m_strSchema + ".calculation SET time_calculation_end=now() , progress=100 , result_length=? , time_calculation=? , time_operation = ? WHERE calculation_id=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery ) ;
        CalcParameterXml t_objXML = new CalcParameterXml();
        String t_strResult = t_objXML.exportParameter(a_objResult);
        t_objStatement.setInt( 1, t_strResult.length() );
        t_objStatement.setLong( 2, a_iTime );
        t_objStatement.setLong( 3, a_iOperationTime );
        t_objStatement.setInt( 4, a_iID );
        t_objStatement.executeUpdate();
    }
    
    public void writeError( String a_strPage,String a_strErrorType, String a_strText, String a_strComment) throws SQLException 
    {
        String t_sqlQuery = "INSERT INTO " + this.m_strSchema 
            + ".error( page,error_type, error_text, \"comment\", date) "
            + "VALUES (?,?, ?, ?, now())";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_sqlQuery) ;
        t_objStatement.setString( 1 , a_strPage );
        t_objStatement.setString( 2 , a_strErrorType );
        t_objStatement.setString( 3 , a_strErrorType );
        t_objStatement.setString( 4 , a_strComment );
        t_objStatement.executeUpdate();        
    }

    /**
     * @param string
     * @return
     * @throws SQLException 
     */
    public String getSettingsProperty(String a_strKey) throws SQLException 
    {
        String t_strQuery = "SELECT * FROM " + this.m_strSchema + ".settings WHERE keyword=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString( 1 , a_strKey );
        ResultSet t_objResult = t_objStatement.executeQuery();
        if ( t_objResult.next() )
        {
            return t_objResult.getString("value");
        }
        return null;        
    }

    /**
     * @param settings
     * @param motif
     */
    public void setMotif(GlycoPeakfinderSettings a_objSettings, String a_strMotifID) throws SQLException 
    {
        String t_strQuery = "SELECT * FROM " + this.m_strSchema + ".residue_to_residue_motif WHERE residue_motif_id=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setInt(1, Integer.parseInt(a_strMotifID));
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            String t_strResidueID = t_objResult.getString("residue_id");
            for (Iterator<ResidueCategory> t_iterCategorie = a_objSettings.getCategorie().iterator(); t_iterCategorie.hasNext();) 
            {
                for (Iterator<MassResidue> t_iterResidue = t_iterCategorie.next().getResidues().iterator(); t_iterResidue.hasNext();) 
                {
                    MassResidue t_objResidue = t_iterResidue.next();
                    if ( t_objResidue.getId().equals(t_strResidueID) )
                    {
                        t_objResidue.setMin( t_objResult.getInt("min"));
                        t_objResidue.setMax( t_objResult.getInt("max"));
                    }
                }                    
            }
        }        
    }

    /**
     * @param databaseResult
     * @param maxGlycosciencesResults
     * @param page
     * @throws SQLException 
     */
    public void pageGlycosciences(DatabaseResult a_objDBResult, int a_iMaxResults, Integer a_iPage) throws SQLException 
    {
        GlycoSciencesEntry t_objEntry;
        ArrayList<GlycoSciencesEntry> t_aResult = new ArrayList<GlycoSciencesEntry>();
        String t_sqlQuery = "SELECT \"LinucsID\",\"LinucsCode\",\"Structure\" FROM " + this.m_strSchema + ".glycosciences_composition_search WHERE \"LinucsID\"=?"; 

        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_sqlQuery) ;
        int t_iStart = (a_iPage-1)*a_iMaxResults;
        int t_iCounter = 0;
        int t_iCounter2 = 0;
        for (Iterator<Integer> t_iterIds = a_objDBResult.getIds().iterator(); t_iterIds.hasNext();) 
        {
            Integer t_iID = t_iterIds.next();
            if ( t_iCounter >= t_iStart )
            {
                if ( t_iCounter2 < a_iMaxResults )
                {
                    t_objStatement.setInt( 1 , t_iID );
                    ResultSet t_objDBResult = t_objStatement.executeQuery();
                    if ( t_objDBResult.next() )
                    {
                    t_objEntry = new GlycoSciencesEntry();
                        t_objEntry.setLinucs(t_objDBResult.getString("LinucsID") );
                        t_objEntry.setLinucsCode(t_objDBResult.getString("LinucsCode") );
                        String t_strString = t_objDBResult.getString("Structure");
                        t_objEntry.setIupac(t_strString );
                        String[] t_aParts = t_strString.split("\n");
                        t_objEntry.setHeight( (t_aParts.length * 16 ) + 32);
                        t_aResult.add(t_objEntry);
                    }
                    t_iCounter2++;
                }
                else
                {
                    a_objDBResult.setCurrentPage(a_iPage);
                    a_objDBResult.setEntry(t_aResult);
                    return;
                }
            }            
            t_iCounter++;
        }   
        a_objDBResult.setCurrentPage(a_iPage);
        a_objDBResult.setEntry(t_aResult);
    }

    /**
     * @param settings
     */
    public void setDefaultResidueFragments(GlycoPeakfinderSettings a_objSettings) throws SQLException
    {
//        String t_strQuery = "SELECT * FROM " + this.m_strSchema + ".residue WHERE abbr=?";
//        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
//        ResultSet t_objResult;
        for (Iterator<ResidueCategory> t_iterCategorie = a_objSettings.getCategorie().iterator(); t_iterCategorie.hasNext();) 
        {
            for (Iterator<MassResidue> t_iterResidue = t_iterCategorie.next().getResidues().iterator(); t_iterResidue.hasNext();) 
            {
                MassResidue t_objResidue = t_iterResidue.next();
//                t_objStatement.setInt(1, Integer.parseInt(t_objResidue.getAbbr()));
//                t_objResult = t_objStatement.executeQuery();
//                if ( t_objResult.next() )
//                {
                t_objResidue.setUseAX( true );
                t_objResidue.setUseE( true );
                t_objResidue.setUseF( true );
                t_objResidue.setUseG( true );
                t_objResidue.setUseH( true );
//                }
            }                    
        }
    }
}