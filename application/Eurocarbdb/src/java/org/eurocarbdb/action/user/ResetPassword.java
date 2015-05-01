/*
 * Copyright (c) 2010 Srikalyan C. Swayampakula. All rights reserved.
 * 
 *   Author : Srikalyan C. Swayampakula
 *   Name of the File : ResetPassword.java
 *   Created on : Mar 19, 2010 at 1:22:20 AM
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 * 
 *  1. Redistributions of source code must retain the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer.
 *  2. Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *  3. Neither the name of the University of Georgia nor the names
 *     of its contributors may be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 *  CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.eurocarbdb.action.user;

import com.opensymphony.webwork.interceptor.ServletRequestAware;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
import org.eurocarbdb.action.AbstractUserAwareAction;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.hibernate.Session;

/**
 *
 * @author Srikalyan C. Swayampakula
 */
public class ResetPassword extends AbstractUserAwareAction implements ServletRequestAware {

    private String message = "";
    private String loginName;
    private String email;
    private HttpServletRequest request;
    private final String RESET_NONE = "reset_none";
    private final String RESET_STATUS = "reset_return";
    private static final Logger log = Logger.getLogger(ResetPassword.class);

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String validateReset() {
        if (allFieldsNull()) {
            return RESET_NONE;
        }
        if (!validateAllFields()) {
            return RESET_STATUS;
        }
        return fetchNReset();

    }

    private boolean allFieldsNull() {
        if (loginName == null && email == null) {
            return true;
        }
        if (email != null && email.trim().length() == 0) {
            message = "Email should not be empty.";
            return true;
        }
        if (loginName != null && loginName.trim().length() == 0) {
            message = "Login name should not be empty.";
            return true;
        }
        loginName = loginName.trim();
        email = email.trim();
        return false;
    }

    private boolean validateAllFields() {

        if (email.indexOf('@') == -1 || email.indexOf('.') == -1) {
            message = "Please enter a proper email address.";
            return false;
        }
        if (!checkIfLoginNameExists()) {
            message = "Sorry, login Name does not exist.";
            return false;
        }
        if(!checkIfEmailExists())
        {
            message="Sorry, this Email does not exist.";
            return false;
        } 
        Contributor c = Contributor.lookupExactNameNEmail(loginName, email);
        if (c == null) {
            message = "Your Email and LoginName does not match. Please make sure that you have entered right details.";
            return false;
        }
        return true;
    }

    private boolean checkIfEmailExists() {
        Contributor c = Contributor.lookupExactEmail(email);
        if (c == null) {
            return false;
        }
        return true;
    }

    private boolean checkIfLoginNameExists() {
        Contributor c = Contributor.lookupExactName(loginName);
        if (c == null) {
            return false;
        }
        return true;
    }

    private String fetchNReset() {
        Contributor c = Contributor.lookupExactNameNEmail(loginName, email);
        if (c == null) {
            message = "Cannot find your details. Please make sure that you have entered right details.";
            return RESET_STATUS;
        }
        if(!c.getIsActivated())
        {
            message = "Your account is not activated. Please wait for the admin to respond.";
            return RESET_STATUS;
        }
        if(c.getIsBlocked())
        {
            message="Your account is blocked. So, you cannot reset your password.";
            return RESET_STATUS;
        }
        resetPassword(c);
        message="Your new password is emailed to you. Please check you email.";
        return RESET_STATUS;
    }

    private void resetPassword(Contributor c) {
        String password = RandomPassword.generateRandomPassword();
        c.setPassword(ChangePassword.getBasicEncryptedPassword(password));
        Session session = Eurocarb.getHibernateSession();
        session.update(c);
        try {
            SendCustomMail.notifyResetPassword(c, password);
        } catch (Exception e) {
            log.debug("Whilst attempting a user attempted to reset their password, and email send failure has occured\n", e);
            
        }
    }
}
