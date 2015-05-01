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
*   Last commit: $Rev: 1612 $ by $Author: magnusl@organ.su.se $ on $Date:: 2009-09-09 #$  
*/
/*------------------------------------------------------------------
*  Author: ml
*  EUROCarbDB Project
*
*------------------------------------------------------------------*/

package org.eurocarbdb.action.nmr;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.awt.image.*;

// 3rd party imports
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import com.opensymphony.webwork.ServletActionContext;

// eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.action.exception.InsufficientParams;

// static imports
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Casper
*
*
*
*  @author           ml
*
*/
public class Casper extends EurocarbAction
{
    //---------------------- STATIC FIELDS ------------------------//

    /** Logging handle. */
    //    protected static final Logger ms_log = Logger.getLogger( CreateNMRProject.class.getName() );
    
    //------------------------- FIELDS ----------------------------//

    // Parameter class containing fields that will be kept //

    public class Unit
    {
    private String residue;
    private String configuration;
    private String linkToResidue;
    private String linkToPos;
    
    public Unit()
    {
        residue="";
        configuration="";
        linkToResidue="";
        linkToPos="";
    }
    
    public String getConfiguration()
    {
        return this.configuration;
    }
    public void setConfiguration(String cfg)
    {
        this.configuration=cfg;
    }
    public String getResidue()
    {
        return this.residue;
    }
    public void setResidue(String res)
    {
        this.residue=res;
    }
    public String getLinkToResidue()
    {
        return this.linkToResidue;
    }
    public void setLinkToResidue(String lnk)
    {
        this.linkToResidue=lnk;
    }
    public String getLinkToPos()
    {
        return this.linkToPos;
    }
    public void setLinkToPos(String lnk)
    {
        this.linkToPos=lnk;
    }    
    }
    private class Casper_Parameters
    {
    private String mode;
    private String name;
    private String source;
    private int id;
    private String path;
    private String cShifts;
    private String hShifts;
    private String cHShifts;
    private String hHShifts;
    private String cCorrection;
    private String hCorrection;
    private String jHHsmall;
    private String jHHmedium;
    private String jHHlarge;
    private String jCHsmall;
    private String jCHlarge;

    private boolean graphicalStructures;
    private boolean disableCcpn;
    
    private Unit [] units;
    private Unit [] substituents;
    private int nUnits;
    private int nSubstituents;

    private String structure;

    private File project;
    private String contentType;
    private String filename;
    private String savedProject;
    }
    private Casper_Parameters pars = new Casper_Parameters();
    private String results;
    private String errorMessage;
    private float error;
    private int nrUnassigned;
    private InputStream projectStream;
    private String sequenceGWS;

    

    //------------------------ METHODS ----------------------------//

    public Casper()
    {
    this.pars.mode="";
    this.pars.name="";
    this.pars.source="";
    this.pars.id=0;
    this.pars.path="";
    this.pars.cShifts="";
    this.pars.hShifts="";
    this.pars.cHShifts="";
    this.pars.hHShifts="";
    this.pars.cCorrection="0";
    this.pars.hCorrection="0";
    this.pars.jHHsmall="0";
    this.pars.jHHmedium="0";
    this.pars.jHHlarge="0";
    this.pars.jCHsmall="0";
    this.pars.jCHlarge="0";

    this.pars.graphicalStructures=false;

    this.pars.units = new Unit[8];
    this.pars.substituents = new Unit[8];
    for(int i=0;i<8;i++)
        {
        this.pars.units[i]=new Unit();
	this.pars.substituents[i]=new Unit();
        }
    this.pars.nUnits=0;
    this.pars.nSubstituents=0;

    this.pars.structure="";

    this.pars.contentType="";
    this.pars.filename="";

    this.pars.savedProject="";

    this.results="";
    this.errorMessage="";
    this.error=0;
    this.nrUnassigned=0;
    this.sequenceGWS="";
    }

    public Casper_Parameters getCasper_Parameters()
    {
    return this.pars;
    }
    public void setCasper_Parameters(Casper_Parameters p)
    {
    this.pars=p;
    }
    public String getMode()
    {
    return this.pars.mode;
    }
    public void setMode(String m)
    {
    this.pars.mode=m;
    }
    public String getName()
    {
    return this.pars.name;
    }
    public void setName(String tl)
    {
    this.pars.name=tl;
    }
    public String getSource()
    {
    return this.pars.source;
    }
    public void setSource(String sc)
    {
    this.pars.source=sc;
    }
    public int getId()
    {
    return this.pars.id;
    }
    public void setId(int n)
    {
    this.pars.id=n;
    }
    public String getPath()
    {
    return this.pars.path;
    }
    public void setPath(String p)
    {
    this.pars.path=p;
    }
    public String getCShifts()
    {
    return this.pars.cShifts;
    }
    public void setCShifts(String shifts)
    {
    this.pars.cShifts=shifts;
    }
    public String getHShifts()
    {
    return this.pars.hShifts;
    }
    public void setHShifts(String shifts)
    {
    this.pars.hShifts=shifts;
    }
    public String getCHShifts()
    {
    return this.pars.cHShifts;
    }
    public void setCHShifts(String shifts)
    {
    this.pars.cHShifts=shifts;
    }
    public String getHHShifts()
    {
    return this.pars.hHShifts;
    }
    public void setHHShifts(String shifts)
    {
    this.pars.hHShifts=shifts;
    }
    public String getCCorrection()
    {
    return this.pars.cCorrection;
    }
    public void setCCorrection(String cCo)
    {
    this.pars.cCorrection=cCo;
    }
    public String getHCorrection()
    {
    return this.pars.hCorrection;
    }
    public void setHCorrection(String hCo)
    {
    this.pars.hCorrection=hCo;
    }
    public String getJHHsmall()
    {
    return this.pars.jHHsmall;
    }
    public void setJHHsmall(String n)
    {
    this.pars.jHHsmall=n;
    }
    public String getJHHmedium()
    {
    return this.pars.jHHmedium;
    }
    public void setJHHmedium(String n)
    {
    this.pars.jHHmedium=n;
    }
    public String getJHHlarge()
    {
    return this.pars.jHHlarge;
    }
    public void setJHHlarge(String n)
    {
    this.pars.jHHlarge=n;
    }
    public String getJCHsmall()
    {
    return this.pars.jCHsmall;
    }
    public void setJCHsmall(String n)
    {
    this.pars.jCHsmall=n;
    }
    public String getJCHlarge()
    {
    return this.pars.jCHlarge;
    }
    public void setJCHlarge(String n)
    {
    this.pars.jCHlarge=n;
    }
    public boolean getGraphicalStructures()
    {
    return this.pars.graphicalStructures;
    }
    public void setGraphicalStructures(boolean value)
    {
    this.pars.graphicalStructures=value;
    }
    public boolean getDisableCcpn()
    {
    return this.pars.disableCcpn;
    }
    public void setDisableCcpn(boolean value)
    {
    this.pars.disableCcpn=value;
    }
    public int getNUnits()
    {
    return this.pars.nUnits;
    }
    public void setNUnits(int n)
    {
    this.pars.nUnits=n;
    }
    public Unit [] getUnits()
    {
    return this.pars.units;
    }
    public Unit getUnit(int i)
    {
    return this.pars.units[i];
    }
    public int getNSubstituents()
    {
    return this.pars.nSubstituents;
    }
    public void setNSubstituents(int n)
    {
    this.pars.nSubstituents=n;
    }
    public Unit [] getSubstituents()
    {
    return this.pars.substituents;
    }
    public Unit getSubstituent(int i)
    {
    return this.pars.substituents[i];
    }
    public String getStructure()
    {
    return this.pars.structure;
    }
    public void setStructure(String str)
    {
    this.pars.structure=str;
    }
    public File getProject()
    {
    return this.pars.project;
    }
    public void setProject(File file)
    {
    this.pars.project=file;
    }
    public String getProjectContentType()
    {
    return this.pars.contentType;
    }
    public void setProjectContentType(String contentType)
    {
    this.pars.contentType=contentType;
    }
    public String getProjectFileName()
    {
    return this.pars.filename;
    }
    public void setProjectFileName(String filename)
    {
    this.pars.filename=filename;
    }
    public String getResults()
    {
    return this.results;
    }
    public void setResults(String res)
    {
    this.results=res;
    }
    public void appendResults(String res)
    {
    this.results=this.results+res+"\n";
    }
    public String getErrorMessage()
    {
    return this.errorMessage;
    }
    public void setErrorMessage(String str)
    {
    this.errorMessage=str;
    }
    public float getError()
    {
    return this.error;
    }
    public void setError(float err)
    {
    this.error=err;
    }
    public int getNrUnassigned()
    {
    return this.nrUnassigned;
    }
    public void setNrUnassigned(int n)
    {
    this.nrUnassigned=n;
    }
    public void incNrUnassigned()
    {
    this.nrUnassigned++;
    }
    public String getSavedProject()
    {
    return this.pars.savedProject;
    }
    public void setSavedProject(String file)
    {
    this.pars.savedProject=file;
    }
    public InputStream getProjectStream()
    {
    return this.projectStream;
    }
    public void setProjectStream(InputStream stream)
    {
    this.projectStream=stream;
    }
    public String getResidue1()
    {
    return this.pars.units[0].residue;
    }
    public void setResidue1(String res)
    {
    this.pars.units[0].residue=res;
    }
    public String getResidue2()
    {
    return this.pars.units[1].residue;
    }
    public void setResidue2(String res)
    {
    this.pars.units[1].residue=res;
    }
    public String getResidue3()
    {
    return this.pars.units[2].residue;
    }
    public void setResidue3(String res)
    {
    this.pars.units[2].residue=res;
    }
    public String getResidue4()
    {
    return this.pars.units[3].residue;
    }
    public void setResidue4(String res)
    {
    this.pars.units[3].residue=res;
    }
    public String getResidue5()
    {
    return this.pars.units[4].residue;
    }
    public void setResidue5(String res)
    {
    this.pars.units[4].residue=res;
    }
    public String getResidue6()
    {
    return this.pars.units[5].residue;
    }
    public void setResidue6(String res)
    {
    this.pars.units[5].residue=res;
    }
    public String getResidue7()
    {
    return this.pars.units[6].residue;
    }
    public void setResidue7(String res)
    {
    this.pars.units[6].residue=res;
    }
    public String getResidue8()
    {
    return this.pars.units[7].residue;
    }
    public void setResidue8(String res)
    {
    this.pars.units[7].residue=res;
    }
    public String getSubstituent1()
    {
    return this.pars.substituents[0].residue;
    }
    public void setSubstituent1(String res)
    {
    this.pars.substituents[0].residue=res;
    }
    public String getSubstituent2()
    {
    return this.pars.substituents[1].residue;
    }
    public void setSubstituent2(String res)
    {
    this.pars.substituents[1].residue=res;
    }
    public String getSubstituent3()
    {
    return this.pars.substituents[2].residue;
    }
    public void setSubstituent3(String res)
    {
    this.pars.substituents[2].residue=res;
    }
    public String getSubstituent4()
    {
    return this.pars.substituents[3].residue;
    }
    public void setSubstituent4(String res)
    {
    this.pars.substituents[3].residue=res;
    }
    public String getSubstituent5()
    {
    return this.pars.substituents[4].residue;
    }
    public void setSubstituent5(String res)
    {
    this.pars.substituents[4].residue=res;
    }
    public String getSubstituent6()
    {
    return this.pars.substituents[5].residue;
    }
    public void setSubstituent6(String res)
    {
    this.pars.substituents[5].residue=res;
    }
    public String getSubstituent7()
    {
    return this.pars.substituents[6].residue;
    }
    public void setSubstituent7(String res)
    {
    this.pars.substituents[6].residue=res;
    }
    public String getSubstituent8()
    {
    return this.pars.substituents[7].residue;
    }
    public void setSubstituent8(String res)
    {
    this.pars.substituents[7].residue=res;
    }
    public String getConfiguration1()
    {
    return this.pars.units[0].configuration;
    }
    public void setConfiguration1(String cfg)
    {
    this.pars.units[0].configuration=cfg;
    }
    public String getConfiguration2()
    {
    return this.pars.units[1].configuration;
    }
    public void setConfiguration2(String cfg)
    {
    this.pars.units[1].configuration=cfg;
    }
    public String getConfiguration3()
    {
    return this.pars.units[2].configuration;
    }
    public void setConfiguration3(String cfg)
    {
    this.pars.units[2].configuration=cfg;
    }
    public String getConfiguration4()
    {
    return this.pars.units[3].configuration;
    }
    public void setConfiguration4(String cfg)
    {
    this.pars.units[3].configuration=cfg;
    }
    public String getConfiguration5()
    {
    return this.pars.units[4].configuration;
    }
    public void setConfiguration5(String cfg)
    {
    this.pars.units[4].configuration=cfg;
    }
    public String getConfiguration6()
    {
    return this.pars.units[5].configuration;
    }
    public void setConfiguration6(String cfg)
    {
    this.pars.units[5].configuration=cfg;
    }
    public String getConfiguration7()
    {
    return this.pars.units[6].configuration;
    }
    public void setConfiguration7(String cfg)
    {
    this.pars.units[6].configuration=cfg;
    }
    public String getConfiguration8()
    {
    return this.pars.units[7].configuration;
    }
    public void setConfiguration8(String cfg)
    {
    this.pars.units[7].configuration=cfg;
    }
    public String getLinkToResidue1()
    {
    return this.pars.units[0].linkToResidue;
    }
    public void setLinkToResidue1(String lnk)
    {
    this.pars.units[0].linkToResidue=lnk;
    }
    public String getLinkToResidue2()
    {
    return this.pars.units[1].linkToResidue;
    }
    public void setLinkToResidue2(String lnk)
    {
    this.pars.units[1].linkToResidue=lnk;
    }
    public String getLinkToResidue3()
    {
    return this.pars.units[2].linkToResidue;
    }
    public void setLinkToResidue3(String lnk)
    {
    this.pars.units[2].linkToResidue=lnk;
    }
    public String getLinkToResidue4()
    {
    return this.pars.units[3].linkToResidue;
    }
    public void setLinkToResidue4(String lnk)
    {
    this.pars.units[3].linkToResidue=lnk;
    }
    public String getLinkToResidue5()
    {
    return this.pars.units[4].linkToResidue;
    }
    public void setLinkToResidue5(String lnk)
    {
    this.pars.units[4].linkToResidue=lnk;
    }
    public String getLinkToResidue6()
    {
    return this.pars.units[5].linkToResidue;
    }
    public void setLinkToResidue6(String lnk)
    {
    this.pars.units[5].linkToResidue=lnk;
    }
    public String getLinkToResidue7()
    {
    return this.pars.units[6].linkToResidue;
    }
    public void setLinkToResidue7(String lnk)
    {
    this.pars.units[6].linkToResidue=lnk;
    }
    public String getLinkToResidue8()
    {
    return this.pars.units[7].linkToResidue;
    }
    public void setLinkToResidue8(String lnk)
    {
    this.pars.units[7].linkToResidue=lnk;
    }
    public String getSubstituentLinkToResidue1()
    {
    return this.pars.substituents[0].linkToResidue;
    }
    public void setSubstituentLinkToResidue1(String lnk)
    {
    this.pars.substituents[0].linkToResidue=lnk;
    }
    public String getSubstituentLinkToResidue2()
    {
    return this.pars.substituents[1].linkToResidue;
    }
    public void setSubstituentLinkToResidue2(String lnk)
    {
    this.pars.substituents[1].linkToResidue=lnk;
    }
    public String getSubstituentLinkToResidue3()
    {
    return this.pars.substituents[2].linkToResidue;
    }
    public void setSubstituentLinkToResidue3(String lnk)
    {
    this.pars.substituents[2].linkToResidue=lnk;
    }
    public String getSubstituentLinkToResidue4()
    {
    return this.pars.substituents[3].linkToResidue;
    }
    public void setSubstituentLinkToResidue4(String lnk)
    {
    this.pars.substituents[3].linkToResidue=lnk;
    }
    public String getSubstituentLinkToResidue5()
    {
    return this.pars.substituents[4].linkToResidue;
    }
    public void setSubstituentLinkToResidue5(String lnk)
    {
    this.pars.substituents[4].linkToResidue=lnk;
    }
    public String getSubstituentLinkToResidue6()
    {
    return this.pars.substituents[5].linkToResidue;
    }
    public void setSubstituentLinkToResidue6(String lnk)
    {
    this.pars.substituents[5].linkToResidue=lnk;
    }
    public String getSubstituentLinkToResidue7()
    {
    return this.pars.substituents[6].linkToResidue;
    }
    public void setSubstituentLinkToResidue7(String lnk)
    {
    this.pars.substituents[6].linkToResidue=lnk;
    }
    public String getSubstituentLinkToResidue8()
    {
    return this.pars.substituents[7].linkToResidue;
    }
    public void setSubstituentLinkToResidue8(String lnk)
    {
    this.pars.substituents[7].linkToResidue=lnk;
    }
    public String getLinkToPos1()
    {
    return this.pars.units[0].linkToPos;
    }
    public void setLinkToPos1(String lnk)
    {
    this.pars.units[0].linkToPos=lnk;
    }
    public String getLinkToPos2()
    {
    return this.pars.units[1].linkToPos;
    }
    public void setLinkToPos2(String lnk)
    {
    this.pars.units[1].linkToPos=lnk;
    }
    public String getLinkToPos3()
    {
    return this.pars.units[2].linkToPos;
    }
    public void setLinkToPos3(String lnk)
    {
    this.pars.units[2].linkToPos=lnk;
    }
    public String getLinkToPos4()
    {
    return this.pars.units[3].linkToPos;
    }
    public void setLinkToPos4(String lnk)
    {
    this.pars.units[3].linkToPos=lnk;
    }
    public String getLinkToPos5()
    {
    return this.pars.units[4].linkToPos;
    }
    public void setLinkToPos5(String lnk)
    {
    this.pars.units[4].linkToPos=lnk;
    }
    public String getLinkToPos6()
    {
    return this.pars.units[5].linkToPos;
    }
    public void setLinkToPos6(String lnk)
    {
    this.pars.units[5].linkToPos=lnk;
    }
    public String getLinkToPos7()
    {
    return this.pars.units[6].linkToPos;
    }
    public void setLinkToPos7(String lnk)
    {
    this.pars.units[6].linkToPos=lnk;
    }
    public String getLinkToPos8()
    {
    return this.pars.units[7].linkToPos;
    }
    public void setLinkToPos8(String lnk)
    {
    this.pars.units[7].linkToPos=lnk;
    }
    public String getSubstituentLinkToPos1()
    {
    return this.pars.substituents[0].linkToPos;
    }
    public void setSubstituentLinkToPos1(String lnk)
    {
    this.pars.substituents[0].linkToPos=lnk;
    }
    public String getSubstituentLinkToPos2()
    {
    return this.pars.substituents[1].linkToPos;
    }
    public void setSubstituentLinkToPos2(String lnk)
    {
    this.pars.substituents[1].linkToPos=lnk;
    }
    public String getSubstituentLinkToPos3()
    {
    return this.pars.substituents[2].linkToPos;
    }
    public void setSubstituentLinkToPos3(String lnk)
    {
    this.pars.substituents[2].linkToPos=lnk;
    }
    public String getSubstituentLinkToPos4()
    {
    return this.pars.substituents[3].linkToPos;
    }
    public void setSubstituentLinkToPos4(String lnk)
    {
    this.pars.substituents[3].linkToPos=lnk;
    }
    public String getSubstituentLinkToPos5()
    {
    return this.pars.substituents[4].linkToPos;
    }
    public void setSubstituentLinkToPos5(String lnk)
    {
    this.pars.substituents[4].linkToPos=lnk;
    }
    public String getSubstituentLinkToPos6()
    {
    return this.pars.substituents[5].linkToPos;
    }
    public void setSubstituentLinkToPos6(String lnk)
    {
    this.pars.substituents[5].linkToPos=lnk;
    }
    public String getSubstituentLinkToPos7()
    {
    return this.pars.substituents[6].linkToPos;
    }
    public void setSubstituentLinkToPos7(String lnk)
    {
    this.pars.substituents[6].linkToPos=lnk;
    }
    public String getSubstituentLinkToPos8()
    {
    return this.pars.substituents[7].linkToPos;
    }
    public void setSubstituentLinkToPos8(String lnk)
    {
    this.pars.substituents[7].linkToPos=lnk;
    }
    public String getSequenceGWS()
    {
    return this.sequenceGWS;
    }
    public void setSequenceGWS(String sequence)
    {
    this.sequenceGWS=sequence;
    }

    public void findPath() throws Exception
    {
    Pattern p;
    Matcher m;
    String currentPath=System.getProperty("user.dir");


    /* Find the directory where Casper is located. Start by trying to
       find the application directory. */
    try
        {
        p = Pattern.compile("^.*?application");
        
        m=p.matcher(currentPath);
        m.lookingAt();
        this.pars.path=m.group();
        this.pars.path=this.pars.path + File.separator + "Casper";
        }
    catch(Exception e)
        {
        /* If the application directory was not found in the current
           user path try looking for the svn directory instead. */
        try
            {
            p = Pattern.compile("^.*?svn");
            
            m=p.matcher(currentPath);
            m.lookingAt();
	    this.pars.path=System.getProperty("user.dir") + File.separator +
		"application" + File.separator + "Casper";
            }
        /* If the svn directory was not found return */
        catch(Exception ee)
            {
            throw(ee);
            }
        }
    if(this.pars.path.equals("") || this.pars.path.equals(File.separator))
        {
        throw new IllegalStateException();
        }
    }

    public void idFromFile() throws Exception
    {
    FileInputStream infile;
    BufferedReader in;
    FileOutputStream outfile;
    PrintStream out;
    File errorFile, tempDir;
    String data;
    int cnt=0;


    try
        {
	this.createTempDirectory();

        infile=new FileInputStream
            (this.getPath() + File.separator + "temp" + File.separator + "count");
        
        in=new BufferedReader(new InputStreamReader(infile));
        data=in.readLine();
        StringTokenizer st = new StringTokenizer(data);
        if(st.hasMoreTokens())
            {
            data=st.nextToken();
            cnt=Integer.parseInt(data);
            }
        /*        if(this.getId()==cnt)
            {
            errorFile=new File(this.getPath() + File.separator + "temp" + File.separator + this.getId() + ".error");
            if(!errorFile.exists())
                {
                return;
                }
                }*/
        cnt++;
        in.close();
        }
    catch(EOFException eof)
        {
        }
    catch(FileNotFoundException e)
        {
        System.err.println("Exception: " + e);
        cnt=1;
        }
    catch(IOException ioe)
        {
        System.err.println("Exception: " + ioe);
        cnt=1;        
        }
    
    if(cnt<=0)
        {
        cnt=1;
        }
    try
        {
        outfile=new FileOutputStream
            (this.pars.path + File.separator + "temp" + File.separator + "count");
        out = new PrintStream(outfile);
        out.println(cnt);
        out.close();
        }
    catch(Exception e)
        {
        System.err.println("Error writing to file " + System.getProperty("user.dir") + "Exception: " + e);
        throw (e);
        }
    this.pars.id=cnt;
    }

    public String writeScript() throws Exception
    {
    return "";
    }

    /* Launching executables from java with < in the command line is troublesome.
       Therefore a script is created to launch casper. This also ensures that casper
       is started from the correct directory. */
    public String writeExecutionScript() throws Exception
    {
    FileOutputStream outfile;
    PrintStream out;

    try
        {
        outfile=new FileOutputStream
            (this.getPath() + File.separator + "temp" + 
             File.separator + this.getId() + ".runscript");
        out = new PrintStream(outfile);

        out.println("cd " + this.getPath()+ File.separator + "bin");
        out.println(this.getPath()+ File.separator + "bin" + File.separator + 
                "casper < " +  this.getPath()+ File.separator + "temp" +
                File.separator + this.getId() + ".script");
        out.close();
        }
    catch(Exception e)
        {
        System.err.println("Error writing Casper execution script file");
        throw (e);
        }

    return SUCCESS;
    }

    public String start()
    {
    Process proc;
    BufferedReader input;
    String data, path, binpath, scriptpath;

    try
        {
        if(this.getProject().toString().equals(""))
            {
            this.setProjectFileName("");
            }
        }
    catch(Exception e)
        {
        this.setProjectFileName("");
        }

    try
        {
        this.findPath();
        }
    catch (Exception e)
        {
        System.err.println("Directory pattern not found. Exception: " + e);
        return ERROR;
        }

    try
        {
        idFromFile();
        System.out.println("Id is: "+ this.getId());
        }
    catch(Exception e)
        {
        System.err.println("Exception getting Id: " + e);
        return ERROR;        
        }

    if(!this.getProjectFileName().equals(""))
        {
        if(this.getProject().exists())
            {
            System.out.println(getProjectFileName());
            if(this.getProjectFileName().endsWith(".tgz") || this.getProjectFileName().endsWith(".tar.gz"))
               {
                   try
                   {
                       this.unzipProject();
                   }
                   catch(Exception e)
                   {
                       System.err.println("Error opening/unzipping project. Exception: " +e);
                       return ERROR;
                   }
               }
            }
        else
            {
            try
                {
                this.setProject(new File(this.getProjectFileName()));
                }
            catch(Exception e)
                {
                System.err.println("Error opening already existing project. Exception: " +e);
                return ERROR;
                }
            }
        }

    try
        {
        writeScript();
        }
    catch(Exception e)
        {
        System.err.println("Exception writing script: " + e);
        return ERROR;        
        }

    scriptpath=this.getPath() + File.separator + "temp" + File.separator + this.getId() + 
        ".runscript";
    
    try
        {
        String [] execstr={"sh", scriptpath};

        System.out.println("Starting CASPER: " + scriptpath);

        proc=Runtime.getRuntime().exec(execstr);

        input = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        this.parseOutput(input);

        proc.waitFor();

        System.out.println("Starting project save.");
        this.setSavedProject(this.prepareSave());

        this.appendResults(this.getErrorMessage());
        }
    catch(Exception e)
        {
        System.err.println("Exception running Casper: " + e);
        return ERROR;
        }
    try
        {
        this.removeFiles();
        }
    catch(Exception e)
        {
        System.err.println("Exception removing files: " + e);
        return ERROR;
        }
    return SUCCESS;
    }

    public void parseOutput(BufferedReader input) throws IOException
    {
    Pattern pSimExp, pAssign, pStr;
    String data, match, type, error, structure, imgStr;
    Matcher m;
    int i, j;
    boolean list=false;
    File outDir, pic;

    pSimExp=Pattern.compile("(^sim)|(^experimental structure:)");
    pAssign=Pattern.compile("((^1H)|(^13C)|(^CH)) chemical");
    pStr=Pattern.compile("(.*?)\\(error est\\. (\\d+(\\.\\d+)?)");

    try
        {
        System.out.println("Output parsing started");
        while((data=input.readLine())!=null)
            {
	    System.out.println(data);
            if(data.equals("list start"))
                {
                list=true;
                System.out.println("Parsing structure list");
                i=0;
                while((data=input.readLine())!=null && 
                      !data.equals("list end"))
                    {
                    if(data.startsWith("sim"))
                        {
                        i++;
                        String [] split=data.split("\\s+");
                        if(split.length>1)
                            {
                            error=split[1];
                            }
                        else
                            {
                            error="dTot=0.00";
                            }
                        if((data=input.readLine())==null)
                            {
                            break;
                            }
                        structure=data.replaceAll("\\[.\\]","");
                        this.appendResults("<tr>");
                        this.appendResults("<td>"+
                                   i +
                                   ".&nbsp;&nbsp;</td>");
                        this.appendResults("<td>");
                        this.appendResults("<a href=\"javascript:vue('"+structure+"')\">");

                        structure=fixStructureOutput(structure);

                        imgStr="";
                        if(this.getGraphicalStructures())
                            {
                            System.out.println("Generating graphical output");
                            imgStr=generateGraphics(input, structure);
                            }

                        if(imgStr.length()>1)
                            {
                            this.appendResults(imgStr);
                            }
                        else
                            {
                            this.appendResults(structure);
                            }

                        this.appendResults("&nbsp;&nbsp;</a></td><td> " +
                                   error +
                                   "&nbsp;&nbsp;");

                        /* Print the score details, but hide them. These can be shown explicitly if wanted. */
                        if(split.length>2)
                            {
                            this.appendResults("</td><td class=\"hide\">");
                            for(j=2;j<split.length;j++)
                                {
                                this.appendResults(split[j] + "&nbsp;");
                                }
                            }
                        this.appendResults("</td></tr>");
                        if((data=input.readLine())==null)
                            {
                            break;
                            }
                        }
                    }
                }
            if(data!=null)
                {
                m=pSimExp.matcher(data);
                }
            else
                {
                break;
                }
            if(m.lookingAt())
                {
                System.out.println("Displaying structure");
                if(m.group(0).equals("sim"))
                    {
                    type="<h3>Simulated chemical shifts</h3>";
                    }
                else
                    {
                    type="<h3>Experimental chemical shifts</h3>";
		    data=input.readLine();
                    }

                data=input.readLine();
                data=this.fixStructureOutput(data);
                    
                imgStr="";
                if(this.getGraphicalStructures())
                    {
                    System.out.println("Generating graphical output");
                    imgStr=generateGraphics(input, data);
                    }
                
                if(imgStr.length()>1)
                    {
                    this.appendResults(type + imgStr + "<br />");
                    }
                else
                    {
                    this.appendResults(type + data + "<br />");
                    }
                data=input.readLine();
                data=input.readLine();
                System.out.println("Generating structure table");
                this.appendResults("<table width=\"100%\">");
                m=pStr.matcher(data);
                while(m.lookingAt())
                    {
                    match=m.group(1);
		    error=m.group(2);
                    this.printResidue(input, match, error);
                    if((data=input.readLine())!=null)
                        {
                        m=pStr.matcher(data);
                        }
                    else
                        {
                        break;
                        }
                    }
                this.appendResults("</table><br />");
                System.out.println("Table finished");
                }
            if(data!=null)
                {
                m=pAssign.matcher(data);
                }
            else
                {
                break;
                }
            if(m.lookingAt() && (this.getMode().equals("determine") || 
				 !this.getCShifts().equals("") || 
				 !this.getHShifts().equals("") ||
				 !this.getProjectFileName().equals("")))
                {
                if(m.group(1).equals("1H"))
                    {
                    type="<h3>Assignment of <sup>1</sup>H resonances</h3>";
                    }
                else if(m.group(1).equals("13C"))
                    {
                    type="<h3>Assignment of <sup>13</sup>C resonances</h3>";
                    }
                else
                    {
                    type="<h3>Assignment of <sup>13</sup>C<sup>1</sup>H resonances</h3>";
                    }
                this.appendResults(type);
                data=input.readLine();
                try
                    {
                    System.out.println("Printing assignments");
                    this.printAssign(input);
                    System.out.println("Assignments printed");
                    }
                catch(IOException e)
                    {
                    throw (e);
                    }
                }
            if(data==null)
                {
                break;
                }
            }

        if(!list)
            {
            if(this.getNrUnassigned()>=1)
                {
                this.appendResults("<br /><br /><a onclick=\"toggleShow();\">Show/hide unassigned chemical shifts in list of assignments</a>");
                }

            outDir=this.createTempDirectory();

            pic=new File(outDir + File.separator + "cSpectra.png");
            if(pic.exists())
                {
                this.appendResults("<br /><a href=\"/eurocarb/ww/nmr/casper_" + this.getMode() + "_picture_stream.action?pictureFileName=cSpectra.png\"><img src=\"/eurocarb/ww/nmr/casper_" + this.getMode() + "_picture_stream.action?pictureFileName=cSpectra.png\" width=\"35%\" height=\"35%\" alt=\"Cannot load 13C spectrum\" /></a>\n");
                }
            else
                {
                System.out.println("No picture of 13C spectrum.");
                }
            pic=new File(outDir + File.separator + "chSpectra.png");
            if(pic.exists())
                {
                this.appendResults("<a href=\"/eurocarb/ww/nmr/casper_" + this.getMode() + "_picture_stream.action?pictureFileName=chSpectra.png\"><img src=\"/eurocarb/ww/nmr/casper_" + this.getMode() + "_picture_stream.action?pictureFileName=chSpectra.png\" width=\"35%\" height=\"35%\" alt=\"Cannot load CH spectrum\" /></a><br />\n");
                }
            else
                {
                System.out.println("No picture of CH spectrum.");
                }
            pic=new File(outDir + File.separator + "hSpectra.png");
            if(pic.exists())
                {
                this.appendResults("<br /><a href=\"/eurocarb/ww/nmr/casper_" + this.getMode() + "_picture_stream.action?pictureFileName=hSpectra.png\">Show <sup>1</sup>H stick spectrum.</a><br />\n");
                }
            else
                {
                System.out.println("No picture of 1H spectrum.");
                }
            }
        }
    catch(IOException e)
        {
        throw(e);
        }
    System.out.println("Output parsing finished");
    }

    public void printResidue(BufferedReader input, String str, String errStr) throws IOException
    {
    String data, colourTag;
    float error;

    /* Convert the expected error string to a float */
    if(errStr.length()>0)
	{
        error=Float.valueOf(errStr.trim()).floatValue();
	}
    else
	{
        error=0;
	}
    
    /* First print the structure */
    str=this.fixStructureOutput(str);
    this.appendResults("<tr><td rowspan=\"3\" style=\"vertical-align: middle\">" + str + "<br />");

    /* Colour the expected errror if it's very high */
    colourTag="";
    if(error>=5)
	{
        colourTag="<div style=\"color:#ff0000\">";
	}
    else if(error>=2.5)
	{
        colourTag="<div style=\"color:#7f0000\">";
	}
    else if(error>=1.0)
	{
        colourTag="<div style=\"color:#3f0000\">";
	}
    /* Print the expected error */
    this.appendResults(colourTag + "(Expected Error " + error + ")");
    if(colourTag.length()>0)
	{
	this.appendResults("</div>");
	}
    this.appendResults("</td>\n");
    
    try
        {
        /* Print atom names */
        data=input.readLine();
	this.printShifts(data);
	this.appendResults("</tr><tr>\n");
	/* Print 13C shifts */
	data=input.readLine();
	this.printShifts(data);
	this.appendResults("</tr><tr>\n");
	/* Print 1H shifts */
        data=input.readLine();
	this.printShifts(data);
	this.appendResults("</tr>\n");
        data=input.readLine();
        }
    catch(IOException e)
        {
        throw(e);
        }
    }
    public void printAssign(BufferedReader input) throws IOException
    {
    String data;
    String [] split;    
    float [] value = new float[2];
    int unassigned, length, searchIndex;

    this.appendResults("<table width=\"100%\"><tr>");
    this.appendResults("<td align=\"right\">Experimental</td>");
    this.appendResults("<td align=\"right\">Simulated</td>");
    this.appendResults("<td align=\"right\">Exp-Sim</td>");
    this.appendResults("<td align=\"right\">Assignment</td></tr>");
    this.appendResults("<tr><td colspan=\"4\"><hr /></td></tr>");

    try
        {
        data=input.readLine();
	searchIndex=data.indexOf("No peaks found.");
	if(searchIndex!=-1)
	    {
	    data=data.substring(0, searchIndex);
	    }
        split=data.split("[\\s\\(\\)]+");
        }
    catch(IOException e)
        {
	System.err.println("Error in printAssign.");
        throw (e);
        }
    while(split.length>3 && !split[1].equals("ppm") && !split[2].equals("ppm") && data!=null && !data.equals(""))
        {
        /* Hide unassigned simulated shifts. These can be toggled on and off */
        if(split[1].equals("n.d.") && !split[2].equals("n.d."))
            {
            unassigned=1;
            this.incNrUnassigned();
            this.appendResults("<tr class=\"hide\">");
            }
        else
            {
            unassigned=0;
            this.appendResults("<tr>");
            }
	length=split.length;
        if(length<=8)
            {
            this.appendResults("<td align=\"right\">" + 
                       split[1]+"</td>\n");
            this.appendResults("<td align=\"right\">" + 
                       split[2]+"</td>\n");
            
            this.appendResults("<td align=\"right\">" + 
                       split[3]+ "</td>\n");
            this.appendResults("<td align=\"right\">" + 
                       fixStructureOutput(split[4] + split[5] +
                                  " - " + 
                                  split[7]));
            }
        else if(length > 9)
            {
            if(unassigned==1)
                {
                this.appendResults("<td align=\"right\">n.d.</td>\n");
                }
            else
                {
                this.appendResults("<td align=\"right\">"+ split[1] + 
                           " - " + split[3] + "</td>\n");
                }
            this.appendResults("<td align=\"right\">"+ split[4-2*unassigned] + 
                       " - " + split[6-2*unassigned] + "</td>\n");
            this.appendResults("<td align=\"right\">"+ split[7-2*unassigned] +
                       "</td>\n");    
            this.appendResults("<td align=\"right\">"+ 
                       fixStructureOutput(split[8-2*unassigned] + split[9-2*unassigned] +
                                  " - " + split[11-2*unassigned]));
            if(split.length>14)
                {
                this.appendResults(", " + 
                           fixStructureOutput(split[11-2*unassigned] + split[12-2*unassigned] +
                                      " - " +
                                      split[14-2*unassigned]));
                }
            }
        this.appendResults("</td></tr>\n");
        try
            {
            data=input.readLine();
	    searchIndex=data.indexOf("No peaks found.");
	    if(searchIndex!=-1)
		{
		data=data.substring(0, searchIndex);
		}
            split=data.split("[\\s\\(\\)]+");
            }
        catch(IOException e)
            {
            throw e;
            }
        }
    System.out.println("Table printed");
    System.out.println(data);
    this.appendResults("<tr><td colspan=\"4\"><hr /></td></tr>\n");
    this.appendResults("</table>\n");
    this.appendResults(data + "<br/>\n");
    split=data.split("[=\\s]+");
    this.setError(Float.valueOf(split[1]).floatValue());
    }
    public String fixStructureOutput(String str)
    {
    str=str.replaceAll("\\[a\\]","<sup>i</sup>");
    str=str.replaceAll("\\[b\\]","<sup>ii</sup>");
    str=str.replaceAll("\\[c\\]","<sup>iii</sup>");
    str=str.replaceAll("\\[d\\]","<sup>iv</sup>");
    str=str.replaceAll("\\[e\\]","<sup>v</sup>");
    str=str.replaceAll("\\[f\\]","<sup>vi</sup>");
    str=str.replaceAll("\\[g\\]","<sup>vii</sup>");
    str=str.replaceAll("\\[h\\]","<sup>viii</sup>");
    str=str.replaceAll("\\[i\\]","<sup>ix</sup>");
    str=str.replaceAll("\\[j\\]","<sup>x</sup>");
    str=str.replaceAll("\\[k\\]","<sup>xi</sup>");
    str=str.replaceAll("\\[l\\]","<sup>xii</sup>");
    str=str.replaceAll("\\[m\\]","<sup>xiii</sup>");
    str=str.replaceAll("\\[n\\]","<sup>xiv</sup>");
    str=str.replaceAll("\\[o\\]","<sup>xv</sup>");
    str=str.replaceAll("aD","&#945;-D-");
    str=str.replaceAll("aL","&#945;-L-");
    str=str.replaceAll("bD","&#946;-D-");
    str=str.replaceAll("bL","&#946;-L-");
    str=str.replaceAll("aA","&#945;-A");
    str=str.replaceAll("bA","&#946;-A");
    str=str.replaceAll("aC","&#945;-C");
    str=str.replaceAll("bC","&#946;-C");
    str=str.replaceAll("aP","&#945;-P");
    str=str.replaceAll("bP","&#946;-P");
    str=str.replaceAll("aT","&#945;-T");
    str=str.replaceAll("bT","&#946;-T");
    str=str.replaceAll("->","&#8594;");
    
    return (str);
    }

    public void printShifts(String data) throws IOException
    {
    Pattern pSpectrum=Pattern.compile("(\\S+)\\s*");
    Matcher m;

    /* Fix names of CO and Me */
    data=data.replaceAll("O", "CO");
    data=data.replaceAll("M", "Me");
    /* The methyl substituent was already called Me - revert its name after the
       replacement above. */
    data=data.replaceAll("Mee", "Me");

    m=pSpectrum.matcher(data);
    while(m.find())
	{
	if(m.group(1).equals("-"))
	    {
	    this.appendResults("<td></td>");
	    }
	else
	    {
	    this.appendResults("<td align=\"right\">" + 
			       m.group(1) + "</td>");
	    }
	}
    }

    public String generateGraphics(BufferedReader input, String textStructure) throws IOException
    {
    String data, structure, result, notation;

    structure="";
    try
        {
        data=input.readLine();
        while(data!=null && !data.equals("----"))
            {
            structure=structure+data;
            data=input.readLine();
            }
        }
    catch (IOException e)
        {
        return ("");
        }
    System.out.println(structure);
    structure=java.net.URLEncoder.encode(structure, "UTF-8");
    structure=structure.replaceAll("%3B","%0D%0A");

    notation=getSugarImageNotation();
    if(notation == null)
        {
        notation = "cfg";
        }
    /* Generate the html image tag including the text representation as alt (needs to be without superscript tags) */
    result="<img src=\"/eurocarb/ww/get_sugar_image.action?download=true&scale=1.0&outputType=png&showRedend=true&inputType=glycoct_condensed&tolerateUnknown=1&marginTop=2&marginBottom=2&marginLeft=10&marginRight=5&notation="
        +notation+"&sequences="+structure+"\" alt=\"" + textStructure.replaceAll("<sup>", "").replaceAll("</sup>", "") + "\"/>";

    return (result);
    }

    public void removeFiles() throws Exception
    {
    File file;
    String filenames=this.getPath() + File.separator + "temp" + File.separator + this.getId();

    try
        {
        file=new File(filenames + ".error");
        if(!file.exists() || file.length()==0)
            {
            if(file.exists())
                {
                file.delete();
                }
            file=new File(filenames + ".script");
            if(file.exists())
                {
                file.delete();
                }
            file=new File(filenames + ".runscript");        
            if(file.exists())
                {
                file.delete();
                }
            }
        else
            {
            this.setErrorMessage("<p><br />An error has occured.<br />The most common cause is linkages in the structure without data.<br />If \"unknown\" residues were used in a structure determination this error is almost never any problem.<br />If a CCPN project could not be created an error also occurs.<br />Keep in mind that results may be inaccurate.<br />This error will be logged and studied.</p>");
            }
        String [] rmCmd = {"sh", "-c", "echo 'rm -rf " + this.getPath() +
                   File.separator + "temp" +
                   File.separator + this.getId() +
                   "'| at now+15 min"};
        try
            {
            Runtime.getRuntime().exec(rmCmd);
            }
        catch (Exception e)
            {
            throw e;
            }
        }
    catch(Exception e)
        {
        throw (e);
        }
    }

    public void unzipProject() throws Exception
    {
    GZIPInputStream in;
    File outDir, files[];
    OutputStream out;
    String outFilename;
    Process proc;
    String projectFile;
    BufferedReader input;

    try
        {
        in = new GZIPInputStream(new FileInputStream(this.getProject()));

        outDir=this.createTempDirectory();

        outFilename=outDir + File.separator + "project.tar";

        out = new FileOutputStream(outFilename);

        byte[] buf = new byte[1024];
        int len;

        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        }
    catch (Exception e)
        {
        throw (e);
        }
    try
        {
        String [] untarCmd = {"sh", "-c", "cd " + 
                      outDir + 
                      "; tar -xf project.tar; rm project.tar; cd -"};

        proc=Runtime.getRuntime().exec(untarCmd);
        proc.waitFor();

        files=outDir.listFiles();

        projectFile="";
        for(int i=0;i<files.length;i++)
            {
            /* The project is bundled in a directory. Take the first directory in the .tgz as project file */
            if(files[i].isDirectory())
                {
                projectFile=(files[i].getName());
                /* If e.g. spectra are included (by mistake) their directory names are often short (numbers).
                   Ignore them for now. Then later on if no other file name was found it will be used. */
                if(projectFile.length()>2)
                    {
                    projectFile=outDir + File.separator + projectFile;
                    this.setProjectFileName(projectFile);
                    i=files.length;
                    }
                }
            }
        /* Check if a short file name has been found, but the projectFile has not been set */
        if(!projectFile.equals("") && this.getProjectFileName().equals(""))
            {
            this.setProjectFileName(projectFile);            
            }
        System.out.println("Project File: " + this.getProjectFileName());
        }
    catch (Exception e)
        {
        throw (e);
        }
    }

    public boolean deleteDirectory(File path)
    {
    if(path.exists())
        {
        File [] files = path.listFiles();
        for(int i=0;i<files.length;i++)
            {
            if(files[i].isDirectory())
                {
                deleteDirectory(files[i]);
                }
            else
                {
                files[i].delete();
                }
            }
        }
    return(path.delete());
    }

    public File createTempDirectory() throws IOException
    {
    File outDir;

    outDir=new File(this.getPath() + File.separator + "temp");

    if(outDir.exists())
	{
        if(!outDir.isDirectory())
	    {
	    throw new IOException("Cannot create CASPER temp directory.");
	    }
	}
    else
        {
        outDir.mkdir();
        }

    outDir=new File(this.getPath() + File.separator + "temp" + File.separator + this.getId());
    if(outDir.exists())
        {
        if(!outDir.isDirectory())
            {
            throw new IOException("Cannot create CASPER temp directory for this execution.");
            }
        }
    else
        {
        outDir.mkdir();
        }

    return outDir;
    }

    public String prepareSave() throws Exception
    {
    File inFile, outFile;
    String name, inDir, outDir;

    inFile=new File(this.getProjectFileName());

    name=inFile.getName();

    if(name.equals(""))
        {
        name="casper_project";
        }

    inFile=new File(this.getPath() + File.separator + "temp" +
            File.separator + this.getId() + File.separator + "save" +
            File.separator + name);
    if(!inFile.exists())
        {
        return "";
        }

    inDir=this.getPath() + File.separator + "temp" + File.separator +
        File.separator + this.getId() + File.separator + "save";

    outDir = this.getPath() + File.separator + "temp";

    String [] tarCmd = {"sh", "-c", "cd " + inDir +
                "; tar -czf " + outDir + File.separator + this.getId() + File.separator + "project.tgz " + name};
    String [] rmCmd = {"sh", "-c", "echo 'rm -f " + outDir + File.separator +
               this.getId() + File.separator + "project.tgz'| at now+15 min"};
    
    try
        {
        Runtime.getRuntime().exec(tarCmd).waitFor();
        Runtime.getRuntime().exec(rmCmd);
        }
    catch (Exception e)
        {
        throw e;
        }

    outFile=new File(outDir + File.separator + this.getId() + File.separator + "project.tgz");
    if(outFile.exists())
        {
        //        System.out.println("Outfile: " + outFile.toString() + " exists");
        return outFile.toString();
        }
    else
        {
        System.out.println("No Outfile: (" + outFile.toString() + ")");
        return "";
        }
    }

    public String executeSave()
    {
    System.out.println("Project: " + this.getSavedProject());
    if(!this.getSavedProject().equals(""))
        {
        try
            {
            projectStream=new FileInputStream(this.getSavedProject());
            }
        catch (IOException e)
            {
            return ERROR;
            }

        return SUCCESS;
        }
    else
        {
        return ERROR;
        }
    }

}

