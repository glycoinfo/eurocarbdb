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
/*  class Casper_simulate
*
*
*
*  @author           ml
*
*/
package org.eurocarbdb.action.nmr;

import java.util.Collection;
import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;

// 3rd party imports
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import com.opensymphony.webwork.ServletActionContext;

// eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.action.exception.InsufficientParams;
import org.eurocarbdb.application.glycanbuilder.ConvertGWS;

// static imports
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

public class Casper_simulate extends Casper
{
    //------------------------- FIELDS ----------------------------//

    public class residueData
    {
    private String name;
    private String value;
    
    public residueData()
    {
        name="";
        value="";
    }
    
    public residueData(String inName, String inValue)
    {
        name=inName;
        value=inValue;
    }

    public String getName()
    {
        return this.name;
    }
    public void setName(String str)
    {
        this.name=str;
    }
    public String getValue()
    {
        return this.value;
    }
    public void setValue(String str)
    {
        this.value=str;
    }
    }
    private residueData [] residueList;
    private residueData [] substituentList;

    //------------------------ METHODS ----------------------------//

    public Casper_simulate()
    {
	/* Set values of the enabled residues */
    String [] Names= {"D-Galp","D-Glcp","D-Manp","D-GlcpNAc","D-GalpNAc",
              "D-ManpNAc","D-GalpA","D-GlcpA","D-ManpA","L-Fucp",
              "D-Fucp","L-Rhap","D-Rhap","D-Quip","L-FucpNAc",
              "L-RhapNAc","D-QuipNAc","D-GalpANAc","D-ManpANAc",
              "Abep","Colp","Parp","Tyvp","Ascp","MurpNAc",
              "Aspargine","Serine","Threonine"};
    String [] Values= {"DGal.12346","DGlc.12346","DMan.12346",
               "DGlcNAc.1346","DGalNAc.1346","DManNAc.1346",
               "DGalA.1234","DGlcA.1234","DManA.1234","LFuc.1234",
               "DFuc.1234","LRha.1234","DRha.1234","DQui.1234",
               "LFucNAc.134","LRhaNAc.134","DQuiNAc.134",
               "DGalANAc.134","DManANAc.134",
               "Abe.124","Col.124","Par.124","Tyv.124","Asc.124",
               "MurNAc.146","Asn.4","Ser.3","Thr.3"};
    
    residueList = new residueData[Names.length];

    for(int i=0;i<Names.length;i++)
        {
        residueList[i] = new residueData(Names[i],Values[i]);
        }

	/* Set values of the enabled substituents */
    String [] SubstNames= {"Methyl", "O-Acetyl", "Phosphate"};
    String [] SubstValues= {"Me", "Ac", "P"};
    
    substituentList = new residueData[SubstNames.length];

    for(int i=0;i<SubstNames.length;i++)
        {
        substituentList[i] = new residueData(SubstNames[i],SubstValues[i]);
        }

    this.setMode("simulate");
    }

    public residueData [] getResidueList()
    {
    return this.residueList;
    }
    public void setResidueList(residueData [] res)
    {
    this.residueList=res;
    }
    public residueData getResidue(int i)
    {
    return this.residueList[i];
    }
    public residueData [] getSubstituentList()
    {
    return this.substituentList;
    }
    public void setSubstituentList(residueData [] sub)
    {
    this.substituentList=sub;
    }

    /* Generates the script that will be used by Casper for this run */
    public String writeScript() throws Exception
    {
    Unit unit;
    FileOutputStream outfile;
    PrintStream out;
    String res, toRes, line, name;
    String experimental="exp";
    boolean manualres=true;
    File outDir, outFile;

    try
        {
        outfile=new FileOutputStream
            (this.getPath() + File.separator + "temp" + 
             File.separator + this.getId() + ".script");
        out = new PrintStream(outfile);

        outDir=this.createTempDirectory();

        out.println("set error '" + this.getPath() + File.separator + "temp" +
                File.separator + this.getId() + ".error'");


	/* Will print structure in CT format. Can be used for graphical output. */
        if(this.getGraphicalStructures())
            {
            out.println("set printct 1");
            }

	/* Removes all CCPN functionality and increases speed. */
	if(this.getDisableCcpn())
	    {
		out.println("disableccpn");
		this.setProjectFileName("");
	    }

        if(!this.getProjectFileName().equals(""))
            {
            out.println("ccpnload '"+ this.getProjectFileName() + "'");
            experimental="/";
	    /*            out.println("ldexp c /");
			  out.println("ldexp h /");*/
            out.println("ldexp ch /");

            String [] split=this.getUnit(0).getResidue().split("\\.");
            res=split[0];
            if(res.equals(""))
                {
                out.println("build sim /");
                manualres=false;
                }
            }
        if(manualres==true)
            {
            out.println("build sim {");
            /* If there is a structure from GlycanBuilder convert
               it to GlycoCT format and use it as CASPER input */
            if(!this.getSequenceGWS().equals(""))
                {
                out.println("glycoinput *");
		out.println(ConvertGWS.toFormat("glycoct_condensed", this.getSequenceGWS()));
                out.println("*");
                }
            /* Otherwise use the specified structure */
            else
                {
		    /* Add the specified residues */
                for(int i=0;i<8;i++)
                    {
                    unit=this.getUnit(i);
                    String [] split=unit.getResidue().split("\\.");
                    res=split[0];
                    if(!res.equals(""))
                        {
                        if(unit.getLinkToPos().equals("m"))
                            {
                            res=res+"OMe";
                            }
                        if(res.equals("Asn") || res.equals("Ser") || res.equals("Thr"))
                            {
                            unit.setConfiguration("");
                            }
                        out.println("unit " + (char)('a'+i) + " " +
                                unit.getConfiguration() +
                                res);
                        }
                    }
		    /* Add the specified substituents */
                for(int i=0;i<8;i++)
                    {
                    unit=this.getSubstituent(i);
                    res=unit.getResidue();
                    if(!res.equals(""))
                        {
		        out.println("unit sub" + (i+1) + " " + res);
                        }
                    }
		/* Add the residue linkages */
                for(int i=0;i<8;i++)
                    {
                    unit=this.getUnit(i);
                    res=unit.getResidue();
                    if(!res.equals(""))
                        {
                        toRes=unit.getLinkToResidue();
                        if(!toRes.equals("") && !unit.getLinkToPos().equals("m"))
                            {
                            out.println("link '" + (char)('a'+i) + "(->" +
                                    unit.getLinkToPos() + ")" +
                                    toRes + "'");
                            }
                        }
                    }
		/* Add the substituent linkages */
                for(int i=0;i<8;i++)
                    {
                    unit=this.getSubstituent(i);
                    res=unit.getResidue();
                    if(!res.equals(""))
                        {
			out.println("link 'sub" + (i+1) + "(->" +
                                    unit.getLinkToPos() + ")" +
                                    unit.getLinkToResidue() + "'");
                        }
                    }
                }
	    out.println("}");
            }
        out.println("show str sim");

        if(this.getProjectFileName().equals(""))
            {
            if(!this.getCShifts().equals(""))
                {
                out.println("ldexp c exp *");
                out.println(this.getCShifts());
                out.println("*");
                }
            if(!this.getHShifts().equals(""))
                {
                out.println("ldexp h exp *");
                out.println(this.getHShifts());
                out.println("*");
                }
	    }
        if(!this.getCCorrection().equals("") ||
           !this.getHCorrection().equals(""))
	    {
            out.println("correct "+ experimental + " " +
                        this.getCCorrection() + " " +
                        this.getHCorrection());
	    }

	/* Just to keep it to reset experimental if it is temporarily set to
	   "none" */
	name=experimental;

	if(!this.getDisableCcpn() && this.getProjectFileName().equals("") &&
	   this.getCShifts().equals("") && this.getHShifts().equals(""))
	    {
	    out.println("ldassign sim " + experimental);
	    /*	    out.println("migrateassign sim " + experimental);*/
	    experimental="none";
	    }

        /* Save spectra as pictures to be able to show them */
        out.println("gnuplot c sim " + experimental +
                " '" + outDir +
                File.separator + "cSpectra.png'");
        out.println("gnuplot c sim " + experimental +
                " '" + outDir +
                File.separator + "cSpectra.eps'");
        out.println("gnuplot h sim " + experimental +
                " '" + outDir +
                File.separator + "hSpectra.png'");
        out.println("gnuplot h sim " + experimental +
                " '" + outDir +
                File.separator + "hSpectra.eps'");
        out.println("gnuplot ch sim " + experimental +
                " '" + outDir +
                File.separator + "chSpectra.png'");
        out.println("gnuplot ch sim " + experimental +
                " '" + outDir +
                File.separator + "chSpectra.eps'");
	if(experimental.equals("none"))
	    {
	    experimental=name;
	    }
        
	if(!this.getCShifts().equals("") || !this.getHShifts().equals("") ||
	   !this.getProjectFileName().equals(""))
            {
            out.println("assign sim "+ experimental);
	    out.println("echo 'experimental structure:'");
            out.println("show str "+ experimental);
            }
	else if(!this.getDisableCcpn())
	    {
	    out.println("assign sim " + experimental);
	    }

        if(!this.getDisableCcpn())
	    {
	    outFile=new File(this.getProjectFileName());
        
            name=outFile.getName();
        
            if(name.equals(""))
                {
                name="casper_project";
                }

	        out.println("ccpnsave '" + outDir +
			    File.separator + "save" +
			    File.separator + 
			    name +"'");
	    }

        out.println("quit");
        out.close();
        }
    catch(Exception e)
        {
        System.err.println("Error writing parameters script file");
        throw (e);
        }
    try
        {
        this.writeExecutionScript();
        }
    catch(Exception e)
        {
        throw (e);
        }

    return SUCCESS;
    }

}
