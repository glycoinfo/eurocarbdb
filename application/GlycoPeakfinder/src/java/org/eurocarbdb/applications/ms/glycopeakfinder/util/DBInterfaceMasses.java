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
package org.eurocarbdb.applications.ms.glycopeakfinder.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.ParameterException;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.util.MassValueStorage;
import org.jdom.JDOMException;

/**
* Object that gives default values for masses 
* 
* @author Logan
*/
public class DBInterfaceMasses implements MassValueStorage 
{
    private Connection m_objDB;
    private String m_strSchema = "";

    public DBInterfaceMasses(Configuration a_objConfig) throws Exception,ParameterException 
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

    private double getSpecialMass(String a_strValue, boolean a_bMonoIsotopic) throws Exception, ParameterException 
    {
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".standard_masses as s WHERE s.name=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString(1, a_strValue);
        ResultSet t_objResult = t_objStatement.executeQuery();
        if ( t_objResult.next() )
        {
            if ( a_bMonoIsotopic )
            {
                return t_objResult.getDouble("mono");
            }
            else
            {
                return t_objResult.getDouble("avg");
            }
        }
        throw new ParameterException("Unknown chemical : " + a_strValue);
    }

    /**
     * @throws GlycoPeakfinderException 
     * @throws SQLException 
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMassH(boolean)
     */
    public double getMassH(boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        return this.getSpecialMass("H", a_bMonoisotopic);
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMassOH(boolean)
     */
    public double getMassOH(boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        return this.getSpecialMass("OH", a_bMonoisotopic);
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMassO(boolean)
     */
    public double getMassO(boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        return this.getSpecialMass("O", a_bMonoisotopic);
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMassH2O(boolean)
     */
    public double getMassH2O(boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        return this.getSpecialMass("H2O", a_bMonoisotopic);
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getDerivatisationMass(java.lang.String, org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution, boolean)
     */
    public double getDerivatisationMass(String a_strType, Persubstitution a_enumPersubst, boolean a_bMonoisotopic) throws Exception, ParameterException 
    {
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".derivates as d WHERE d.abbr=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString(1, a_strType);
        ResultSet t_objResult = t_objStatement.executeQuery();
        if ( t_objResult.next() )
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
            return t_objResult.getDouble(t_strKey);
        }
        throw new ParameterException("Unknown dericatisation : " + a_strType );
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMoleculeMass(java.lang.String, boolean)
     */
    public double getMoleculeMass(String a_strType, boolean a_bMonoisotopic) throws Exception, ParameterException 
    {
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".small_molecules WHERE name=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString(1, a_strType);
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            if ( a_bMonoisotopic ) 
            {
                return t_objResult.getDouble("mass_mono");
            }
            else
            {
                return t_objResult.getDouble("mass_avg");
            }
        }
        throw new ParameterException("Unknown small molecule.");
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getCompletionMass(java.lang.String, org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution, boolean)
     */
    public double getCompletionMass(String a_strType, Persubstitution a_objPerSubst, boolean a_bMonoisotopic) throws ParameterException, Exception 
    {
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".persubstitution as p WHERE p.name=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        if ( a_objPerSubst == Persubstitution.None )
        {
            t_objStatement.setString(1, "none");
        }
        else if ( a_objPerSubst == Persubstitution.Ac )
        {
            t_objStatement.setString(1, "pac");
        }
        else if ( a_objPerSubst == Persubstitution.DAc )
        {
            t_objStatement.setString(1, "pdac");
        }
        else if ( a_objPerSubst == Persubstitution.Me )
        {
            t_objStatement.setString(1, "pme");
        }
        else if ( a_objPerSubst == Persubstitution.DMe )
        {
            t_objStatement.setString(1, "pdme");
        }
        else
        {
            t_objStatement.setString(1, "");
        }
        ResultSet t_objResult = t_objStatement.executeQuery();
        String t_strMass = null;
        if ( t_objResult.next() )
        {
            if ( a_bMonoisotopic ) 
            {
                if ( a_strType.equals("red") )
                {
                    t_strMass = t_objResult.getString("ergaenzung_red_mono");                        
                }
                else if ( a_strType.equals("nonred") )
                {
                    t_strMass = t_objResult.getString("ergaenzung_nonred_mono");
                }
                else if ( a_strType.equals("profile") )
                {
                    t_strMass = t_objResult.getString("mono");
                }                    
            }
            else
            {
                if ( a_strType.equals("red") )
                {
                    t_strMass = t_objResult.getString("ergaenzung_red_avg");                        
                }
                else if ( a_strType.equals("nonred") )
                {
                    t_strMass = t_objResult.getString("ergaenzung_nonred_avg");
                }
                else if ( a_strType.equals("profile") )
                {
                    t_strMass = t_objResult.getString("avg");
                }                    
            }
            if ( t_strMass == null )
            {
                throw new ParameterException("Unknown completion type.");
            }
            if ( a_strType.equals("profile") )
            {
                return ( Double.parseDouble(t_strMass.trim()) - this.getMassH(a_bMonoisotopic));
            }
            else
            {
                return Double.parseDouble(t_strMass.trim());
            }
        }
        throw new ParameterException("Unknown persubstitution type.");
    }


    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getIonMass(java.lang.String, boolean)
     */
    public double getIonMass(String a_strIon, boolean a_bMonoisotopic) throws ParameterException, Exception 
    {
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".ions WHERE formula=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString(1, a_strIon);
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            if ( a_bMonoisotopic ) 
            {
                if ( t_objResult.getBoolean("signum") )
                {
                    return t_objResult.getDouble("mass_mono");
                }
                else
                {
                    return (0-t_objResult.getDouble("mass_mono"));
                }
            }
            else
            {
                if ( t_objResult.getBoolean("signum") )
                {
                    return t_objResult.getDouble("mass_avg");
                }
                else
                {
                    return (0-t_objResult.getDouble("mass_mono"));
                }
            }
        }
        throw new ParameterException("Unknown ion.");
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getGlycosidicFragmentMass(java.lang.String, boolean)
     */
    public double getGlycosidicFragmentMass(String a_strType, boolean a_bMonoisotopic) throws ParameterException , Exception
    {
        if ( a_strType.equalsIgnoreCase("y"))
        {
            return this.getMassH(a_bMonoisotopic);
        }
        if ( a_strType.equalsIgnoreCase("z"))
        {
            return 0 - this.getMassOH(a_bMonoisotopic);
        }
        if ( a_strType.equalsIgnoreCase("c"))
        {
            return this.getMassOH(a_bMonoisotopic);
        }
        if ( a_strType.equalsIgnoreCase("b"))
        {
            return 0 - this.getMassH(a_bMonoisotopic);
        }    
        throw new ParameterException("Unknown fragment type.");
    }

    /**
     * Gives the mass of the increment 
     * 
     * @param a_strPerSub type of persubstitution
     * @param a_bMonoIsotopic true if monoisotopic mass
     * @return
     * @throws JDOMException
     * @throws ParameterException thrown if type of persubstitution is unknown
     */
    public double getIncrementMass(Persubstitution a_strPerSub , boolean a_bMonoIsotopic) throws Exception, ParameterException  
    {
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".persubstitution as p WHERE p.name=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        if ( a_strPerSub == Persubstitution.None )
        {
            t_objStatement.setString(1, "none");
        }
        else if ( a_strPerSub == Persubstitution.Ac )
        {
            t_objStatement.setString(1, "pac");
        }
        else if ( a_strPerSub == Persubstitution.DAc )
        {
            t_objStatement.setString(1, "pdac");
        }
        else if ( a_strPerSub == Persubstitution.Me )
        {
            t_objStatement.setString(1, "pme");
        }
        else if ( a_strPerSub == Persubstitution.DMe )
        {
            t_objStatement.setString(1, "pdme");
        }
        else
        {
            t_objStatement.setString(1, "");
        }
        ResultSet t_objResult = t_objStatement.executeQuery();
        if ( t_objResult.next() )
        {
            if ( a_bMonoIsotopic )
            {
                return t_objResult.getDouble("increment_mono");
            }
            else
            {
                return t_objResult.getDouble("increment_avg");
            }
        }
        throw new GlycoPeakfinderException("Unknown persubstitution : " + a_strPerSub);
    }

    /**
     * Gives the mass of the increment for A/X fragments 
     * 
     * @param a_strPerSub type of persubstitution
     * @param a_bMonoIsotopic true if monoisotopic mass
     * @return
     * @throws JDOMException
     * @throws ParameterException thrown if type of persubstitution is unknown
     */
    public double getIncrementMassAX(Persubstitution a_strPerSub , boolean a_bMonoIsotopic) throws Exception, ParameterException  
    {
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".persubstitution as p WHERE p.name=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        if ( a_strPerSub == Persubstitution.None )
        {
            t_objStatement.setString(1, "none");
        }
        else if ( a_strPerSub == Persubstitution.Ac )
        {
            t_objStatement.setString(1, "pac");
        }
        else if ( a_strPerSub == Persubstitution.DAc )
        {
            t_objStatement.setString(1, "pdac");
        }
        else if ( a_strPerSub == Persubstitution.Me )
        {
            t_objStatement.setString(1, "pme");
        }
        else if ( a_strPerSub == Persubstitution.DMe )
        {
            t_objStatement.setString(1, "pdme");
        }
        else 
        {
            t_objStatement.setString(1, "");
        }
        ResultSet t_objResult = t_objStatement.executeQuery();
        if ( t_objResult.next() )
        {
            if ( a_bMonoIsotopic )
            {
                return t_objResult.getDouble("increment_mono") - t_objResult.getDouble("mono");
            }
            else
            {
                return t_objResult.getDouble("increment_avg") - t_objResult.getDouble("avg");
            }
        }
        throw new GlycoPeakfinderException("Unknown persubstitution : " + a_strPerSub);
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getResidueMass(java.lang.String, org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution, boolean)
     */
    public double getResidueMass(String a_strResidue, Persubstitution a_enumPersubst, boolean a_bMonoisotopic) throws Exception, ParameterException 
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
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".residue as d WHERE d.abbr=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString( 1 , a_strResidue );
        ResultSet t_objResult = t_objStatement.executeQuery();
        if ( t_objResult.next() )
        {
            if ( !t_objResult.getBoolean("increment") )
            {
                return ( t_objResult.getDouble(t_strKey) - this.getIncrementMass(a_enumPersubst, a_bMonoisotopic) );
            }
            else
            {
                return t_objResult.getDouble(t_strKey);
            }
        }
        else
        {
            throw new GlycoPeakfinderException("Unknown residue : " + a_strResidue );
        }
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getCrossringFragmentMass(java.lang.String, org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution, boolean, java.lang.String, int, int)
     */
    public double getCrossringFragmentMass(String a_strType, Persubstitution a_enumPersubst, boolean a_bMonoisotopic, String a_strResidue, int a_iPosOne, int a_iPosTwo ) throws Exception, ParameterException 
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
        + this.m_strSchema + ".fragment_ax as f WHERE r.abbr=? AND r.residue_id=f.residue_id AND f.type=? AND cleav_one= ? AND cleav_two=?"; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString( 1 , a_strResidue );
        t_objStatement.setString( 2 , a_strType );
        t_objStatement.setInt( 3 , a_iPosOne );
        t_objStatement.setInt( 4 , a_iPosTwo );
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            if ( a_strType.equals("A") )
            {
                return ( t_objResult.getDouble(t_strKey) 
                        - this.getIncrementMassAX(a_enumPersubst, a_bMonoisotopic) 
                        + this.getMassOH(a_bMonoisotopic) );
            }
            else
            {
                return ( t_objResult.getDouble(t_strKey) 
                        - this.getIncrementMassAX(a_enumPersubst, a_bMonoisotopic) 
                        + this.getMassH(a_bMonoisotopic) );
            }
        }
        throw new ParameterException("Unknown fragment.");
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getExchangeIonMass(boolean)
     */
    public double getExchangeIonMass(boolean a_bMonoisotopic) throws ParameterException,Exception
    {
        return this.getMassH(a_bMonoisotopic);
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getNonReducingDifference(org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution, boolean)
     */
    public double getNonReducingDifference(Persubstitution a_objPersub, boolean a_bMonoisotopic) throws ParameterException, Exception 
    {
        String t_strQuery = "SELECT * FROM "+ this.m_strSchema + ".persubstitution as p WHERE p.name=?";
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        if ( a_objPersub == Persubstitution.None )
        {
            t_objStatement.setString(1, "none");
        }
        else if ( a_objPersub == Persubstitution.Ac )
        {
            t_objStatement.setString(1, "pac");
        }
        else if ( a_objPersub == Persubstitution.DAc )
        {
            t_objStatement.setString(1, "pdac");
        }
        else if ( a_objPersub == Persubstitution.Me )
        {
            t_objStatement.setString(1, "pme");
        }
        else if ( a_objPersub == Persubstitution.DMe )
        {
            t_objStatement.setString(1, "pdme");
        }
        else 
        {
            t_objStatement.setString(1, "");
        }
        ResultSet t_objResult = t_objStatement.executeQuery();
        if ( t_objResult.next() )
        {
            if ( a_bMonoisotopic )
            {
                return t_objResult.getDouble("ergaenzung_nonred_mono");
            }
            else
            {
                return t_objResult.getDouble("ergaenzung_nonred_avg");
            }
        }
        throw new GlycoPeakfinderException("Unknown persubstitution : " + a_objPersub.getName());
    }

    /**
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMassE(boolean)
     */
    public double getMassE(boolean a_bMonoisotopic) throws ParameterException,Exception
    {
        return this.getSpecialMass("e", a_bMonoisotopic);
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.util.MassValueStorage#getResidueFragmentMass(java.lang.String, org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution, boolean, java.lang.String)
     */
    public double getResidueFragmentMass(String a_strType, Persubstitution a_enumPersubst,
            boolean a_bMonoisotopic, String a_strResidue) throws ParameterException, Exception 
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
        + this.m_strSchema + ".fragment_other as f WHERE r.abbr=? AND r.residue_id=f.residue_id AND f.type=?"; 
        PreparedStatement t_objStatement = this.m_objDB.prepareStatement( t_strQuery) ;
        t_objStatement.setString( 1 , a_strResidue );
        t_objStatement.setString( 2 , a_strType );
        ResultSet t_objResult = t_objStatement.executeQuery();
        while ( t_objResult.next() )
        {
            if ( a_strType.equals("E") )
            {
                return ( t_objResult.getDouble(t_strKey) 
                        - this.getIncrementMassAX(a_enumPersubst, a_bMonoisotopic) 
                        + this.getMassOH(a_bMonoisotopic) );
            }
            else
            {
                return ( t_objResult.getDouble(t_strKey) 
                        - this.getIncrementMassAX(a_enumPersubst, a_bMonoisotopic) 
                        + this.getMassH(a_bMonoisotopic) );
            }
        }
        throw new ParameterException("Unknown fragment.");
    }    
}