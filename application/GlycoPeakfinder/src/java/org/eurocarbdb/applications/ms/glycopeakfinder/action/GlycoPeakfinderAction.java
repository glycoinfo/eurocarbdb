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
package org.eurocarbdb.applications.ms.glycopeakfinder.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eurocarbdb.applications.ms.glycopeakfinder.storage.ContactInformation;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.ErrorInformation;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPResult;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GlycoPeakfinderSettings;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.Configuration;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterface;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.SMTPAuthenticator;
import org.jdom.JDOMException;

import com.opensymphony.xwork.ActionSupport;


/**
* @author Logan
*
*/
public class GlycoPeakfinderAction  extends ActionSupport
{
    private static final long serialVersionUID = 1L;
    
    protected URL m_urlConfigFile = null;
    protected Configuration m_objConfiguration = null;
    protected String m_strPageTitle = "GlycoPeakfinder";
    protected String m_strPageAuthor = "Rene Ranzinger";
    protected String m_strPageKeywords = "mass spectrometry,carbohydrate,automated analysis";
    protected String m_strPageDescription = "The Glyco-Peakfinder is a tool for fast annotation of glycan MS spectra. MS-profiles, MSn spectra with different types of ions (glycosidic cleavages and/or cross-ring cleavages) can be calculated in parallel.";
    protected String m_strPageType = "";
    protected ErrorInformation m_objError = new ErrorInformation();
    
    // User Settings Storage Variable
    protected GlycoPeakfinderSettings m_objSettings = new GlycoPeakfinderSettings();
    // calculation result
    protected GPResult m_objResult = new GPResult(); 

    public void setErrorInformation(ErrorInformation a_objError)
    {
        this.m_objError = a_objError;
    }
    
    public ErrorInformation getErrorInformation()
    {
        return this.m_objError;
    }
    
    public void setPageType(String a_strType)
    {
        this.m_strPageType = a_strType;
    }
    
    public String getPageType()
    {
        return this.m_strPageType;
    }
    
    public void setPageDescription(String a_strText)
    {
        this.m_strPageDescription = a_strText;
    }
    
    public String getPageDescription()
    {
        return this.m_strPageDescription;
    }

    public void setPageTitle(String a_strText)
    {
        this.m_strPageTitle = a_strText;
    }
    
    public String getPageTitle()
    {
        return this.m_strPageTitle;
    }

    public void setPageAuthor(String a_strText)
    {
        this.m_strPageAuthor = a_strText;
    }
    
    public String getPageAuthor()
    {
        return this.m_strPageAuthor;
    }

    public void setPageKeywords(String a_strText)
    {
        this.m_strPageKeywords = a_strText;
    }
    
    public String getPageKeywords()
    {
        return this.m_strPageKeywords;
    }

    public GlycoPeakfinderAction()
    {
        // create configuration object
        try
        {
            this.m_urlConfigFile = this.getClass().getResource("/configuration.xml");
            this.m_objConfiguration = new Configuration(this.m_urlConfigFile);
        } 
        catch (JDOMException e)
        {
            System.err.println("Error in configuration XML: " + e );
            e.printStackTrace();
        } 
        catch (IOException e)
        {
            System.err.println("Couldn't load configuration settings: " + e );
            e.printStackTrace();
        }
    }
    
    
    public void handleExceptions( String a_strPage,String a_strErrorType,Exception a_objThrow)
    {
        try
        {
            StringWriter sw = new StringWriter();
            a_objThrow.printStackTrace(new PrintWriter(sw));
            DBInterface t_objDB = new DBInterface( this.m_objConfiguration );
            t_objDB.writeError(a_strPage,a_strErrorType, a_objThrow.getMessage(), sw.toString());                    
        }
        catch (Exception e1) 
        {
            a_objThrow.printStackTrace();
            e1.printStackTrace();
        }
    }
    
    public void sendMail(DBInterface a_objDB, ContactInformation a_objContact ) throws MessagingException, SQLException
    {
        boolean debug = false;
        // mail connection information 
        Properties props = new Properties();
        props.put("mail.smtp.host", a_objDB.getSettingsProperty("smtp_server") );
        props.put("mail.smtp.auth", "true");
        Authenticator auth = new SMTPAuthenticator( a_objDB.getSettingsProperty("smtp_user"),
                a_objDB.getSettingsProperty("smtp_pass"));
        Session session = Session.getDefaultInstance(props, auth);
        session.setDebug(debug);
        // create a message
        Message msg = new MimeMessage(session);

        // Addresses
        InternetAddress addressFrom = new InternetAddress(a_objDB.getSettingsProperty("smtp_from"));
        msg.setFrom(addressFrom);
        String t_strTo = a_objDB.getSettingsProperty("smtp_to");
        String[] t_aTo = t_strTo.split("\\|");
        InternetAddress[] addressTo = new InternetAddress[t_aTo.length];
        for (int t_iCounter = 0; t_iCounter < t_aTo.length; t_iCounter++)
        {
            addressTo[ t_iCounter ] = new InternetAddress( t_aTo[ t_iCounter ] );
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);
        // create message
        String t_strMessage = "Message from " + a_objContact.getName() + " (" + a_objContact.getEmail() 
            + ") : \n\n" + a_objContact.getContent();            
        // Setting the Subject and Content Type
        msg.setSubject( "GlycoPeakfinder - " + a_objContact.getType() + " : "+ a_objContact.getSubject() );
        msg.setContent(t_strMessage, "text/plain");
        Transport.send(msg);    
    }

    public void setBaseUrl(String a_strPath)
    {}
    
    public String getBaseUrl() throws JDOMException
    {
        return this.m_objConfiguration.getBaseUrl();
    }
    
    public void setSettings(GlycoPeakfinderSettings a_objSettings)
    {
        this.m_objSettings = a_objSettings;
    }
    
    public GlycoPeakfinderSettings getSettings()
    {
        return this.m_objSettings;
    }
    
    public void setResult(GPResult a_objResult )
    {
        this.m_objResult = a_objResult;
    }
    
    public GPResult getResult()
    {
        return this.m_objResult;
    }    
}
