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

import java.util.ArrayList;

import org.eurocarbdb.applications.ms.glycopeakfinder.storage.ContactInformation;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterface;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.ErrorTextEnglish;

/**
* @author rene
*
*/
public class ContactAction extends GlycoPeakfinderAction
{
    private static final long serialVersionUID = 1L;
    // contact
    private ContactInformation m_objContact = new ContactInformation();

    public ContactAction()
    {
        this.m_strPageType = "contact";
    }
    
    public void setContact(ContactInformation a_objContact)
    {
        this.m_objContact = a_objContact;
    }

    public ContactInformation getContact()
    {
        return this.m_objContact;
    }

    /**
     * @see com.opensymphony.xwork.ActionSupport#execute()
     */
    @Override
    public String execute() throws Exception
    {
        if ( this.m_objContact.getContent() == null || this.m_objContact.getSubject() == null )
        {
            this.m_objContact.setContent("");
            this.m_objContact.setSubject("");
            return "page_input";
        }
        if ( this.m_objContact.getContent().trim().length() != 0 && this.m_objContact.getSubject().trim().length() != 0 )
        {
            try 
            {
                DBInterface t_objDB = new DBInterface(this.m_objConfiguration);
                t_objDB.writeContact(this.m_objContact);
                try
                {
                    this.sendMail(t_objDB, this.m_objContact);
                }
                catch (Exception e) 
                {
                    this.handleExceptions("contact", "email", e);
                }
                return "page_finish";
            } 
            catch (Exception e) 
            {
                this.handleExceptions("contact", "email", e);
                ArrayList<String> t_aError = new ArrayList<String>();
                t_aError.add(e.getMessage());
                this.m_objError.setTitle("Unable to send email");
                this.m_objError.setText(ErrorTextEnglish.DB_ERROR);
                this.m_objError.setErrors( t_aError );
                this.m_objError.setBackUrl("Contact.action");
                return "page_error";
            }               
        }
        if ( this.m_objContact.getUsed() )
        {
            if ( this.m_objContact.getContent().trim().length() == 0 )
            {
                this.m_objContact.setMissContent(true);
            }
            if ( this.m_objContact.getSubject().trim().length() == 0 )
            {
                this.m_objContact.setMissSubject(true);
            }
        }
        return "page_input";
    }

}
