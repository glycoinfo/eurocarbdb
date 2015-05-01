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
/*  class Casper_curate
*
*
*
*  @author           ml
*
*/
package org.eurocarbdb.action.nmr;

import java.util.Collection;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Enumeration;
import java.util.Iterator;

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

public class Casper_curate extends Casper
{
    //------------------------- FIELDS ----------------------------//


    //------------------------ METHODS ----------------------------//


    /* Generates the script that will be used by Casper for this run */
    public String writeScript() throws Exception
    {
    FileOutputStream outfile;
    PrintStream out;
    File outDir, outFile;

    try
        {
        if(this.getProjectFileName().equals(""))
            {
            throw new Exception("In order to curate a project a CCPN project file name must be specified.");
            }

        outfile=new FileOutputStream
            (this.getPath() + File.separator + "temp" + 
             File.separator + this.getId() + ".script");
        out = new PrintStream(outfile);

        outDir=this.createTempDirectory();

        out.println("set error '" + this.getPath() + File.separator + "temp" +
                File.separator + this.getId() + ".error'");

        out.println("ccpnload '"+ this.getProjectFileName() + "'");

        out.println("multibuild sim /");
        out.println("multildassign sim /");
        out.println("multicorrect /");
	/*        out.println("multimigrateassign sim /");*/
        out.println("disablecppn");
        out.println("multiassign sim /");
	/* Save the project with a _CASPER tag to separate from the original project.
	   FIXME: Check that this is a good place to save it. */
	out.println("ccpnsave '"+ this.getProjectFileName() +"_CASPER'");
        
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

    if(this.getProjectFileName().equals(""))
        {
        System.err.println("In order to curate a project a CCPN project file name must be specified.");
        return ERROR;
        }
    if(this.getProject().exists())
        {
        System.out.println(getProjectFileName());
        if(!this.getProjectFileName().endsWith(".xml"))
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
        System.err.println("Project does not exist.");
        return ERROR;
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

	/*        input = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		  this.parseOutput(input);*/

        proc.waitFor();
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

    /* This should not be needed anymore. All data should be available in the CCPN project */
    /*    public void parseOutput(BufferedReader input) throws IOException
    {
    Pattern pRms;
    String data;
    Matcher m;

    pRms=Pattern.compile("Error=.* RMS error=([\\d\\.]+) ppm, Systematic");

    try
        {
        System.out.println("Output parsing started");
        while((data=input.readLine())!=null)
            {
            if(data.equals("1H chemical shifts"))
                {
                while((data=input.readLine())!=null)
                    {
                    m=pRms.matcher(data);
                    if(m.lookingAt())
                        {
    *//* This should be able to handle more structures.
                           Should there be an RMS array instead?
                           Should the RMS errors be summed up? *//*
                        this.setHRms(Float.valueOf(m.group(1)).floatValue());
                        break;
                        }
                    }
                }
            if(data.equals("13C chemical shifts"))
                {
                while((data=input.readLine())!=null)
                    {
                    m=pRms.matcher(data);
                    if(m.lookingAt())
                        {
                        this.setCRms(Float.valueOf(m.group(1)).floatValue());
                        break;
                        }
                    }
                }
            }
        }
    catch(IOException e)
        {
        throw(e);
        }
    System.out.println("Output parsing finished");
    }*/
}
