/*
 * Copyright (c) 2010 Srikalyan C. Swayampakula. All rights reserved.
 * 
 *   Author : Srikalyan C. Swayampakula
 *   Name of the File : ChangePassword.java
 *   Created on : Mar 8, 2010 at 10:47:14 PM
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
import java.util.GregorianCalendar;
import javax.servlet.http.HttpServletRequest;
import org.eurocarbdb.action.AbstractUserAwareAction;
import org.eurocarbdb.action.RequiresLogin;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.hibernate.Session;
import org.jasypt.util.password.BasicPasswordEncryptor;

/**
 *
 * @author Srikalyan C. Swayampakula
 */
public class ChangePassword extends AbstractUserAwareAction implements ServletRequestAware, RequiresLogin {

    private String newPassword;
    private String currentPassword;
    private String confirmPassword;
    private HttpServletRequest request;
    private String message = "";
    public static final String CHG_PASSD_INPUT = "input";
    public static final String CHG_PWD_SUCCESS = "change_password_success";

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static String getBasicEncryptedPassword(String textPassword) {
        BasicPasswordEncryptor encryptor = new BasicPasswordEncryptor();
        return encryptor.encryptPassword(textPassword);
    }

    public static boolean compareSimple2EncryptedPassword(String plainPassword, String encryptedPassword) {
        if (plainPassword == null || encryptedPassword == null) {
            return false;
        }
        if (plainPassword.trim().length() == 0 || encryptedPassword.trim().length() == 0) {
            return false;
        }
        BasicPasswordEncryptor encryptor = new BasicPasswordEncryptor();
        return encryptor.checkPassword(plainPassword, encryptedPassword);
    }

    public String changePassword() {
        if (currentPassword == null && newPassword == null && confirmPassword == null) {
            if (Eurocarb.getCurrentContributor().getLastLogin() == null) {
                message = "Please change your password. (mandatory for first time logging)";
            }
            return CHG_PASSD_INPUT;
        }
        if (currentPassword == null || currentPassword.trim().length() == 0) {
            message = "Current password cannot be null";
            return CHG_PASSD_INPUT;
        }
        if (currentPassword.length() < 6 || currentPassword.length() > 15) {
            message = "Current password should be 6-15 characters long";
            return CHG_PASSD_INPUT;
        }
        if (newPassword == null || newPassword.trim().length() == 0) {
            message = "New password cannot be null";
            return CHG_PASSD_INPUT;
        }
        if (newPassword.length() < 6 || newPassword.length() > 15) {
            message = "New password should be 6-15 characters long";
            return CHG_PASSD_INPUT;
        }
        if (confirmPassword == null || confirmPassword.trim().length() == 0) {
            message = "Confirm password cannot be empty";
            return CHG_PASSD_INPUT;
        }
        if (confirmPassword.length() < 6 || confirmPassword.length() > 15) {
            message = "Confirm password should be 6-15 characters long";
            return CHG_PASSD_INPUT;
        }
        if (!newPassword.equals(confirmPassword)) {
            message = "New password and confirm password do not match";
            return CHG_PASSD_INPUT;
        }
        if (!compareSimple2EncryptedPassword(currentPassword, Eurocarb.getCurrentContributor().getPassword())) {
            message = "Current password does not match with our records";
            return CHG_PASSD_INPUT;
        }
        String encryptedPassword = getBasicEncryptedPassword(newPassword);
        Contributor c = Eurocarb.getCurrentContributor();
        c.setPassword(encryptedPassword);
        Session session = Eurocarb.getHibernateSession();
        if (c.getLastLogin() == null) {
            c.setLastLogin(GregorianCalendar.getInstance().getTime());
        }
        session.save(c);
//copy the code..remove session
//change template file to go to login page.
        return CHG_PWD_SUCCESS;

    }
}
