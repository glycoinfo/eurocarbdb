/*
 * Copyright (c) 2010 Srikalyan C. Swayampakula. All rights reserved.
 * 
 *   Author : Srikalyan C. Swayampakula
 *   Name of the File : SendCustomMail.java
 *   Created on : Mar 3, 2010 at 11:05:34 PM
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.commons.mail.DefaultAuthenticator;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.Contributor;

/**
 *
 * @author Srikalyan C. Swayampakula
 */
public class SendCustomMail {

    public static final String ADMIN_EMAIL;
    public static final String ADMIN_EMAIL_USERNAME;
    public static final String PASSWORD;
    public static final String ADMIN_NAME;
    public static final String MAIL_SERVER;
    public static final String MAIL_SERVER_PORT;
    public static final String TLS;
    public static final String IS_SMTPS_REQUIRED;
    public static final String MESSAGE_USER_ACTIVATED;
    public static final String MESSAGE_USER_ADDED_NOTIFICATION;
    public static final String MESSAGE_USER_ACTIVATED_TO_ADMIN;
    public static final String ADDED_NOTIFICATION_SUBJECT;
    public static final String ACTIVATED_NOTIFICATION_SUBJECT;
    public static final String NOT_ACTIVATED_NOTIFICATION_SUBJECT;
    public static final String MESSAGE_USER_NOT_ACTIVATED_TO_ADMIN;
    public static final String MESSAGE_USER_NOT_ACTIVATED;
    public static final String RESET_NOTIFICATION_SUBJECT;
    public static final String MESSAGE_RESET_PASSWORD;
    public static final String BLOCKED_NOTIFICATION_SUBJECT;
    public static final String MESSAGE_USER_BLOCKED_TO_ADMIN;
    public static final String MESSAGE_USER_BLOCKED_TO_USER;
    public static final String UNBLOCKED_NOTIFICATION_SUBJECT;
    public static final String MESSAGE_USER_UNBLOCKED_TO_USER;
    public static final String MESSAGE_USER_PROMOTED_TO_USER;
    public static final String MESSAGE_USER_DEMOTED_TO_USER;
    public static final String PROMOTED_NOTIFICATION_SUBJECT;
    public static final String DEMOTED_NOTIFICATION_SUBJECT;
    private static final Logger log = Logger.getLogger(UserManipulation.class);

    public static final String ADMIN_ALLOWS_USER_TO_REGISTER;

    static {

        Properties pip = null;
        try {
            pip = getProperties();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            log.fatal("Refusing to start EurocarbDB webapp, no email settings provided");
            System.exit(1);
        }
	ADMIN_ALLOWS_USER_TO_REGISTER = pip.getProperty("adminAllowsUserToRegister");
        ADMIN_EMAIL = pip.getProperty("adminEmail");
        ADMIN_EMAIL_USERNAME = pip.getProperty("adminEmailUserName");
        PASSWORD = pip.getProperty("password");
        ADMIN_NAME = pip.getProperty("adminName");
        MAIL_SERVER = pip.getProperty("mailServer");
        MAIL_SERVER_PORT = pip.getProperty("mailServerPort");
        TLS = pip.getProperty("TLS");
        IS_SMTPS_REQUIRED = pip.getProperty("isSMTPSRequired");
        MESSAGE_USER_ACTIVATED = pip.getProperty("messageUserActivated");
        MESSAGE_USER_ADDED_NOTIFICATION = pip.getProperty("messageUserAddedNotification");
        MESSAGE_USER_ACTIVATED_TO_ADMIN = pip.getProperty("messageUserActivatedToAdmin");
        ADDED_NOTIFICATION_SUBJECT = pip.getProperty("addedNotificationSubject");
        ACTIVATED_NOTIFICATION_SUBJECT = pip.getProperty("activatedNotificationSubject");
        NOT_ACTIVATED_NOTIFICATION_SUBJECT = pip.getProperty("notActivatedNotificationSubject");
        MESSAGE_USER_NOT_ACTIVATED = pip.getProperty("messageUserNotActivated");
        MESSAGE_USER_NOT_ACTIVATED_TO_ADMIN = pip.getProperty("messageUserNotActivatedToAdmin");
        RESET_NOTIFICATION_SUBJECT = pip.getProperty("resetNotificationSubject");
        MESSAGE_RESET_PASSWORD = pip.getProperty("messageResetPassword");
        BLOCKED_NOTIFICATION_SUBJECT = pip.getProperty("blockedNotificationSubject");
        MESSAGE_USER_BLOCKED_TO_ADMIN = pip.getProperty("messageUserBlockedToAdmins");
        MESSAGE_USER_BLOCKED_TO_USER = pip.getProperty("messageUserBlockedToUser");
        UNBLOCKED_NOTIFICATION_SUBJECT = pip.getProperty("unblockedNotificationSubject");
        MESSAGE_USER_UNBLOCKED_TO_USER = pip.getProperty("messageUserUnblockedToUser");
        MESSAGE_USER_PROMOTED_TO_USER = pip.getProperty("messageUserPromotedToUser");
	MESSAGE_USER_DEMOTED_TO_USER = pip.getProperty("messageUserDemotedToUser");
    	PROMOTED_NOTIFICATION_SUBJECT = pip.getProperty("promotedNotificationSubject");
        DEMOTED_NOTIFICATION_SUBJECT = pip.getProperty("demotedNotificationSubject");
    }

    private static Properties getProperties() throws Exception {
        Properties prop = new Properties();

        InputStream in = SendCustomMail.class.getResourceAsStream("/emailConf.properties");
        if (in == null) {
            throw new Exception("Unable to access /emailConf.properties");
        }
        prop.load(in);

        return prop;
    }
    /** Query string prefix shortcut */
    private static final String Q = "org.eurocarbdb.dataaccess.core.Contributor."; 

    public static void notifyUserRegistrationToAllAdmins(Contributor c) throws Exception {
        List<Contributor> admins = getAllActiveAdmins();
        if (admins.size() == 0) { 
            log.debug("There are no admin users!");
            throw new Exception("There are no admin users!");
        }
        for (Contributor admin : admins) {
            log.debug("Sending registration email to: " + admin.getFullName());
            String message = MESSAGE_USER_ADDED_NOTIFICATION + "\n User's login name is " + c.getName() + "\n Email is " + c.getEmail() + "\n Full name is " + c.getFullName() + "\n Institution is " + c.getInstitution() + "\n";
            sendEmailToBasicSetup(admin.getEmail(), ADDED_NOTIFICATION_SUBJECT, message);
        }
    }

    public static void notifyAccountActivation(Contributor user, Contributor admin, String password) throws Exception {
	if(ADMIN_ALLOWS_USER_TO_REGISTER.equals("false")){        
		String message = MESSAGE_USER_ACTIVATED_TO_ADMIN + "(" + admin.getFullName() + ")" + ":" + "\n User's login name is " + user.getName() + ", institution is "+ user.getInstitution()+ " and email is " + user.getEmail() + ".";
		notifyToAdmins(user, ACTIVATED_NOTIFICATION_SUBJECT, message);
	}
        String message = MESSAGE_USER_ACTIVATED + "\nYou can login using the following password: " + password;
        notifyToUser(user, ACTIVATED_NOTIFICATION_SUBJECT, message);
    }

    public static void notifyAccoutBlocked(Contributor user, Contributor admin) throws Exception {
        String message=MESSAGE_USER_BLOCKED_TO_ADMIN+ "(" + admin.getFullName() + ")" + ":" + "\n User's login name is " + user.getName() + ", full name is " + user.getFullName() +" and email is " + user.getEmail() + ".";
        notifyToAdmins(user, BLOCKED_NOTIFICATION_SUBJECT, message);
        //message = MESSAGE_USER_BLOCKED_TO_USER + "\nYour login name is " + user.getName();
	message = "Your EUROCarb account (" + user.getName() +") has been blocked.";        
	notifyToUser(user, BLOCKED_NOTIFICATION_SUBJECT, message);

    }

    public static void notifyAccountPromoted(Contributor user, Contributor admin) throws Exception {
        //String message = MESSAGE_USER_PROMOTED_TO_USER + "\nYour login name is " + user.getName() + ".";       
  	String message = "Your EUROCarb account (" + user.getName() +") has been promoted.";      
	notifyToUser(user, PROMOTED_NOTIFICATION_SUBJECT, message);
    }

    public static void notifyAccountDemoted(Contributor user, Contributor admin) throws Exception {
        //String message=MESSAGE_USER_DEMOTED_TO_USER+ "\nYour login name is " + user.getName() + ".";
	String message = "your EUROCarb account (" + user.getName() + ") has been demoted.";        
	notifyToUser(user, DEMOTED_NOTIFICATION_SUBJECT, message);
    }

    public static void notifyAccoutUnblocked(Contributor user, Contributor admin) throws Exception {
        //String message = MESSAGE_USER_UNBLOCKED_TO_USER+ "\nYour login name is " + user.getName();
        String message = "Your EUROCarb account (" + user.getName() +") has been unblocked. You can use your old password to login.";
	notifyToUser(user, UNBLOCKED_NOTIFICATION_SUBJECT, message);
    }

    public static void notifyAccountNotActivated(Contributor user, Contributor admin) throws Exception {
        String message = MESSAGE_USER_NOT_ACTIVATED_TO_ADMIN + "(" + admin.getFullName() + ")" + ":" + "\n User's login name is " + user.getName() + ", institution is "+ user.getInstitution()+ " and email is " + user.getEmail() + ".";
        notifyToAdmins(user, NOT_ACTIVATED_NOTIFICATION_SUBJECT, message);
        message = MESSAGE_USER_NOT_ACTIVATED;
        notifyToUser(user, NOT_ACTIVATED_NOTIFICATION_SUBJECT, message);
    }

    public static void notifyToAdmins(Contributor user, String subject, String message) throws Exception {
        List<Contributor> admins = getAllActiveAdmins();
        if (admins.size() == 0) {
            log.debug("There are no admin users!");
            throw new Exception("There are no admin users!");
        }

        for (Contributor admin1 : admins) {
            sendEmailToBasicSetup(admin1.getEmail(), subject, message);
        }
    }

    public static void notifyResetPassword(Contributor user, String password) throws Exception {
        String message = MESSAGE_RESET_PASSWORD + "\nYour new password is " + password + "";
        notifyToUser(user, RESET_NOTIFICATION_SUBJECT, message);
    }

    public static void notifyToUser(Contributor user, String subject, String message) throws Exception {
        sendEmailToBasicSetup(user.getEmail(), subject, message);
    }

    /***
     * gets the list of all active contributors who are admins.
     */
    private static List<Contributor> getAllActiveAdmins() {
        return (List<Contributor>) Eurocarb.getEntityManager().getQuery(Q + "ACTIVE_ADMINISTRATORS").list();
    }

    private static void sendEmailToBasicSetup(String recipentEmail, String subject, String message) throws Exception {
        if (IS_SMTPS_REQUIRED != null && IS_SMTPS_REQUIRED.equals("true")) {
            sendEmailTo(recipentEmail, subject, message);
            return;
        }
        log.debug("Email settings: ");
        log.debug("Hostname: " + MAIL_SERVER);
        log.debug("Recipent: " + recipentEmail);
        log.debug("Subject: " + subject);
        log.debug("Message: " + message);

        SimpleEmail email = new SimpleEmail();
        email.setHostName(MAIL_SERVER);
        email.addTo(recipentEmail);
        email.setSubject(subject);
        email.setMsg(message);

        if (ADMIN_NAME != null) {
            log.debug("With admin name send: yes (" + ADMIN_NAME + ")");
            email.setFrom(ADMIN_EMAIL, ADMIN_NAME);
        } else {
            log.debug("With admin name send: no");
            email.setFrom(ADMIN_EMAIL);
        }
        log.debug("Admin email: " + ADMIN_EMAIL);

        if (TLS != null && TLS.equals("true")) {
            log.debug("TLS: yes");
            email.setTLS(true);
        } else {
            log.debug("TLS: no");
        }

        if (MAIL_SERVER_PORT != null) {
            log.debug("Server port explicity set: yes (" + MAIL_SERVER_PORT + ")");
            email.setSmtpPort(Integer.valueOf(MAIL_SERVER_PORT));

            if (email.isTLS()) {
                log.debug("ssl port set: yes");
                email.setSslSmtpPort(MAIL_SERVER_PORT);
            } else {
                log.debug("ssl port set: no");
            }
        }

        log.debug("Authentication email: " + ADMIN_EMAIL_USERNAME);
        log.debug("Authentication password: " + PASSWORD);
        email.setAuthentication(ADMIN_EMAIL_USERNAME, PASSWORD);
        email.send();
    }

    private static void sendEmailTo(String addToEmail, String subject, String message) throws Exception {
        SimpleEmail email = new SimpleEmail();
        if (PASSWORD != null) {
            email.setAuthenticator(new DefaultAuthenticator(ADMIN_EMAIL_USERNAME, PASSWORD));
        }

        email.setHostName(MAIL_SERVER);
        if (MAIL_SERVER_PORT != null) {

            email.setSmtpPort(Integer.parseInt(MAIL_SERVER_PORT));

        }
        if (TLS != null && TLS.equals("true")) {
            email.setTLS(true);
        }

        email.getMailSession().getProperties().put("mail.smtps.auth", "true");


        if (MAIL_SERVER_PORT != null) {
            email.getMailSession().getProperties().put("mail.smtps.port", MAIL_SERVER_PORT);
            email.getMailSession().getProperties().put("mail.smtps.socketFactory.port", MAIL_SERVER_PORT);
        } else {
            email.getMailSession().getProperties().put("mail.smtps.port", "25");
            email.getMailSession().getProperties().put("mail.smtps.socketFactory.port", "25");
        }

        email.getMailSession().getProperties().put("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        email.getMailSession().getProperties().put("mail.smtps.socketFactory.fallback", "false");
        if (TLS != null && TLS.equals("true")) {
            email.getMailSession().getProperties().put("mail.smtp.starttls.enable", "true");
        }



        email.addTo(addToEmail);
        if (ADMIN_NAME != null) {
            email.setFrom(ADMIN_EMAIL, ADMIN_NAME);
        } else {
            email.setFrom(ADMIN_EMAIL);
        }

        email.setSubject(subject);

        email.setMsg(message);

        email.send();


    }
}
