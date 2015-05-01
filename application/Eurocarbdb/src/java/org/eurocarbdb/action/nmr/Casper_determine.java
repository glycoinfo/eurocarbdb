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
*   Last commit: $Rev: 1595 $ by $Author: magnusl@organ.su.se $ on $Date:: 2009-08-18 #$  
*/
/*  class Casper_determine
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
import org.apache.log4j.FileAppender;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.Logger;

import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import com.opensymphony.webwork.ServletActionContext;

// static imports
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

public class Casper_determine extends Casper
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

    //------------------------ METHODS ----------------------------//

    public Casper_determine()
    {
    String [] Names= {"D-Galp","D-Glcp","D-Manp","D-GlcpNAc","D-GalpNAc",
              "D-ManpNAc","D-GalpA","D-GlcpA","D-ManpA","L-Fucp",
              "D-Fucp","L-Rhap","D-Rhap","D-Quip","L-FucpNAc",
              "L-RhapNAc","D-QuipNAc","D-GalpANAc","D-ManpANAc",
              "Abep","Colp","Parp","Tyvp","Ascp","MurpNAc",
              "D-GalpOMe", "D-GlcpOMe", "D-ManpOMe", "D-GalpNAcOMe",
              "D-GlcpNAcOMe", "D-ManpNAcOMe", "D-GalpAOMe",
              "D-GlcpAOMe", "D-ManpAOMe", "L-RhapOMe", "L-FucpOMe",
              "Asn", "Ser", "Thr",
              "Unknown", "Unknown Hex", "Unknown HexOMe",
              "Unknown HexNAc", "Unknown HexNAcOMe",
	      "Unknown 6dHex", "Unknown HexA",
	      "Unknown Pen", "Unknown PenOMe",
              "Unknown PenNAc", "Unknown PenNAcOMe"};
    String [] Values= {"DGal.12346.6.7","DGlc.12346.6.7","DMan.12346.6.7",
               "DGlcNAc.1346.8.8","DGalNAc.1346.8.8",
               "DManNAc.1346.8.8","DGalA.1234.6.5",
               "DGlcA.1234.6.5","DManA.1234.6.5","LFuc.1234.6.6",
               "DFuc.1234.6.6","LRha.1234.6.6","DRha.1234.6.6",
               "DQui.1234.6.6", "LFucNAc.134.8.7","LRhaNAc.134.8.7",
               "DQuiNAc.134.8.7","DGalANAc.134.8.7",
               "DManANAc.134.8.7","Abe.124.6.7","Col.124.6.7",
               "Par.124.6.7","Tyv.124.6.7","Asc.124.6.7",
               "MurNAc.146.11.10","DGalOMe.2346.7.8",
               "DGlcOMe.2346.7.8","DManOMe.2346.7.8",
               "DGalNAcOMe.346.9.9","DGlcNAcOMe.346.9.9",
               "DManNAcOMe.346.9.9","DGalAOMe.234.7.6",
               "DGlcAOMe.234.7.6","DManAOMe.234.7.6",
               "LRhaOMe.234.7.7","LFucOMe.234.7.7",
               "Asn.4.4.3", "Ser.3.3.3", "Thr.3.4.3",
               "unknown.123456.12.12", "unknownhex.12346.6.7",
               "unknownhexome.2346.7.8", "unknownhexnac.1346.8.8",
               "unknownhexnacome.346.9.9", "unknown6dhex.1234.6.6",
	       "unknownhexa.1234.6.5",
               "unknownpen.1234.5.6", "unknownpenome.234.6.7",
               "unknownpennac.134.7.7", "unknownpennacome.34.8.8"};
    
    residueList = new residueData[Names.length];

    for(int i=0;i<Names.length;i++)
        {
        residueList[i] = new residueData(Names[i],Values[i]);
        }

    this.setMode("determine");
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


    /* Generates the script that will be used by Casper for this run */
    public String writeScript() throws Exception
    {
    Unit unit;
    FileOutputStream outfile;
    PrintStream out;
    String res, toRes, name;
    String experimental="exp";
    boolean manualres=true;
    File outDir, outFile;
    int lowNrKept=15, highNrKept=50;


    try
        {
        outfile=new FileOutputStream
            (this.getPath() + File.separator + "temp" + 
             File.separator + this.getId() + ".script");
        out = new PrintStream(outfile);

        outDir=this.createTempDirectory();

        out.println("set error '" + this.getPath() + File.separator + "temp" +
                File.separator + this.getId() + ".error'");

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

            if(!this.getCCorrection().equals("") || 
               this.getHCorrection().equals(""))
                {
                out.println("correct "+ experimental + " " +
                        this.getCCorrection() + " " +
                        this.getHCorrection());
                }
            
            if(this.getStructure().equals(""))
                {
                out.println("generate sim / {");
                }
            }
        else
            {
            if(this.getCShifts().equals("")&&
               this.getHShifts().equals("")&&
               this.getCHShifts().equals(""))
                {
                out.println("quit");
                return ERROR;
                }
            if(!this.getCShifts().equals(""))
                {
                out.println("ldexp c exp *");
                out.println(getCShifts());
                out.println("*");
                }
            if(!this.getHShifts().equals(""))
                {
                out.println("ldexp h exp *");
                out.println(getHShifts());
                out.println("*");
                }
            if(!this.getCHShifts().equals(""))
                {
                out.println("ldexp ch exp *");
                out.println(getCHShifts());
                out.println("*");
                }
            if(!this.getCCorrection().equals("") || 
               this.getHCorrection().equals(""))
                {
                out.println("correct "+ experimental + " " +
                        this.getCCorrection() + " " +
                        this.getHCorrection());
                }
            if(this.getStructure().equals(""))
                {
                out.println("generate sim exp {");
                }
            }
        if(this.getStructure().equals(""))
            {
            if(!this.getCHShifts().equals(""))
                {
                out.println("purge " + lowNrKept +" " + 
                        highNrKept + " CH");
                }
            else if(!this.getCShifts().equals(""))
                {
                out.println("purge " + lowNrKept +" " + 
                        highNrKept + " C");
                }
            else if(!this.getHShifts().equals(""))
                {
                out.println("purge " + lowNrKept +" " + 
                        highNrKept + " H");
                }
            /* Default to use CH and CASPER will select a purge
               type. This is important for e.g. CCPN projects
               since they use empty shift inputs. */
            else
                {
                out.println("purge " + lowNrKept +" " + 
                        highNrKept + " CH");
                }
            out.println("jhh " + this.getJHHsmall() + " " +
                    this.getJHHmedium() + " " + this.getJHHlarge());
            out.println("jch " + this.getJCHsmall() + " 0 " +
                    this.getJCHlarge());
            
            for(int i=0;i<8;i++)
                {
                unit=this.getUnit(i);
                String [] split=unit.getResidue().split("\\.");
                res=split[0];
                if(!res.equals(""))
                    {
                    out.println("unit " + (char)('a' + i) +
                            " {");
                    if(res.startsWith("unknown") || res.equals("Asn") || 
                       res.equals("Ser") || res.equals("Thr"))
                        {
                        out.println("residue " +
                                res + " " +
                                unit.getLinkToPos().replaceAll(",","").replaceAll("([\\d\\*]+)","'$1'"));
                        }
                    else
                        {
                        out.println("residue a" +
                                res + " " +
                                unit.getLinkToPos().replaceAll(",","").replaceAll("([\\d\\*]+)","'$1'"));
                        out.println("residue b" +
                                res + " " +
                                unit.getLinkToPos().replaceAll(",","").replaceAll("([\\d\\*]+)","'$1'"));                
                        }
                    out.println("}");
                    }
                }
            out.println("}");
            out.println("purgelist 10 10 T");
            out.println("echo 'list start'");
            out.println("list str");
            out.println("echo 'list end'");
            }
        else
            {
		out.println("build sim {");
		out.println("ln '"+this.getStructure()+"'");
		out.println("}");
		out.println("show str sim");
        
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

            out.println("assign sim " + experimental);
	    out.println("echo 'experimental structure:'");
            out.println("show str " + experimental);
            
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
