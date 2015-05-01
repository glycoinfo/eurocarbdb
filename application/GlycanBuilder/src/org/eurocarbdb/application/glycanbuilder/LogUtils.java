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

package org.eurocarbdb.application.glycanbuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.awt.Frame;

import org.apache.log4j.Logger;

/**
   Utility class with function to manage the logging of errors.

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class LogUtils {

    private static boolean graphical_report = false;
    private static Frame theOwner = null;    
    private static boolean hasLastError = false;
    private static String lastError = "";
    private static String lastErrorStack = "";
  
    private LogUtils() {}

    /**
       Specify if the logger should display a dialog with a report of
       the error
     */
    static public void setGraphicalReport(boolean flag) {
    graphical_report = flag;
    }

    /**
       Return <code>true</code> if the logger should display a dialog
       with a report of the error
     */
    static public boolean getGraphicReport() {
    return graphical_report;
    }
    

    /**
       Set the frame used to display the report dialog
     */
    static public void setReportOwner(Frame owner) {
    theOwner = owner;
    }

    /**
       Return the frame used to display the report dialog
     */
    static public Frame getReportOwner() {
    return theOwner;
    }

    /**
       Clear the information relative to the last occurred error
     */
    static public void clearLastError() {
    hasLastError = false;
    lastError = "";
    lastErrorStack = "";
    }

    /**
       Return <code>true</code> if an error has been recently reported
     */
    static public boolean hasLastError() {
    return hasLastError;
    }

    /**
       Return the error message corresponding to the last error
       reported
     */
    static public String getLastError() {
    return lastError;
    }

    /**
       Return a string with the call stack corresponding to the last
       error reported
     */
    static public String getLastErrorStack() {
    return lastErrorStack;
    }

    /**
       Return the error message corresponding to a given exception
     */
    static public String getError(Exception e) {
    return e.getMessage();
    }

    /**
       Return a string with the call stack corresponding to 
       a given exception
     */
    static public String getErrorStack(Exception e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));       
    return sw.getBuffer().toString();
    }

    /**
       Report a new error taking the information from the raised
       exception. Show a report dialog if needed.
     */
    static public void report(Exception e) {
    if( e==null ) {
        clearLastError();
        return;
    }
        
    hasLastError = true;

    lastError = getError(e);
    if( lastError==null )
        lastError = "";

    lastErrorStack = getErrorStack(e);
    if( lastErrorStack==null )
        lastErrorStack = "";

    if( graphical_report ) {
        new ReportDialog(theOwner,lastErrorStack).setVisible(true);
    } else {
        Logger.getLogger( LogUtils.class ).error("Error in GlycanBuilder",e);
    }
    }
}