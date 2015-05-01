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

import javax.mail.PasswordAuthentication;

/**
* SimpleAuthenticator is used to do simple authentication
* when the SMTP server requires it.
*/
 public class SMTPAuthenticator extends javax.mail.Authenticator
 {
    private String m_strUserName = "";
    private String m_strPass = "";

     /**
         * @param string
         * @param string2
         */
        public SMTPAuthenticator(String a_strUser, String a_strPass) 
        {
            this.m_strUserName = a_strUser;
            this.m_strPass = a_strPass;
        }

        public PasswordAuthentication getPasswordAuthentication()
     {
         return new PasswordAuthentication(this.m_strUserName, this.m_strPass);
     }
 }