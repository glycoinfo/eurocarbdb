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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/

package org.eurocarbdb.action.admin;

//  stdlib imports

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.net.*;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;

import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.Contributed;


//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

import org.eurocarbdb.dataaccess.core.Contributor;


/*  class SendBugReport  *//*************************************
*
*   
*
*   @author   ac [a.ceroni@imperial.ac.uk]
*   @version  $Rev: 1549 $
*/
public class SendBugReport extends EurocarbAction
{

    /** Logging handle. */
    private static final Logger log = Logger.getLogger( SendBugReport.class );

    private String exceptionMessage = null;
    private String exceptionType = null;
    private String exceptionStack = null;


    public void setExceptionMessage(String s) {
    exceptionMessage = s;
    }

    public String setExceptionMessage() {
    return exceptionMessage;
    }

    public void setExceptionType(String s) {
    exceptionType = s;
    }

    public String setExceptionType() {
    return exceptionType;
    }

    public void setExceptionStack(String s) {
    exceptionStack = s;
    }

    public String setExceptionStack() {
    return exceptionStack;
    }

    /*  execute  *//*************************************************
    *
    */
    public String execute()
    {        
    try {
        if( exceptionMessage==null || exceptionType==null || exceptionStack==null )
        return "empty";
        
        // retrieve configuration
        String smtp_address = Eurocarb.getProperty("ecdb.ww.smtp_address");
        String mail_address = Eurocarb.getProperty("ecdb.ww.mail_address");
        String buglist_address = Eurocarb.getProperty("ecdb.ww.buglist_address");
        if( smtp_address==null || mail_address==null || buglist_address==null )
        return ERROR;

        // create session
        Properties mailServerConfig = new Properties();
        mailServerConfig.setProperty("mail.host",smtp_address);

        Session session = Session.getDefaultInstance( mailServerConfig, null );
        
        // create text
        String mbody = "";        
        mbody += "Host: " + InetAddress.getLocalHost() + "\n";
        mbody += "Contributor: " + Contributor.getCurrentContributor().getName() + "\n";
        mbody += "Exception type: " + exceptionType + "\n";
        mbody += "Exception message: " + exceptionMessage + "\n";
        mbody += "Exception Stack:\n" + exceptionStack + "\n";
        
        // create message
        MimeMessage message = new MimeMessage( session );
        message.setFrom( new InternetAddress(mail_address) );
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(buglist_address));      
        message.setSubject( "User exception report" );
        message.setText( mbody );

        // send message
        Transport.send( message );

        return SUCCESS;
    }
    catch(Exception e) {
        return ERROR;
    }

    }

} // end class

