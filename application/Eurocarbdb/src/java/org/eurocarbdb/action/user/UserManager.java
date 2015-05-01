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
 *   Last commit: $Rev: 1926 $ by $Author: kitaemyoung $ on $Date:: 2010-07-08 #$
 */
package org.eurocarbdb.action.user;

import org.apache.log4j.Logger;

import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.action.AbstractUserAwareAction;

import com.opensymphony.webwork.interceptor.ServletRequestAware;
import java.util.GregorianCalendar;
import javax.servlet.http.HttpServletRequest;

//  static imports
import org.hibernate.Session;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

import static org.eurocarbdb.util.JavaUtils.*;

/**
 *   Handles {@link Contributor} login and logout.
 *
 *   @author mjh
 *   @author hirenj
 *  update by @author srikalyan.
 */
public class UserManager extends AbstractUserAwareAction implements ServletRequestAware {
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final Logger log = Logger.getLogger(UserManager.class);
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private String username;
    private String userpass;
    private String strMessage = "";
    private String openIdUrl;
    private String openIdIdentifier;
    private boolean logoutSuccessful;
    private HttpServletRequest request;
    public static final String FIRST_TIME_LOGIN = "first_time_login";

    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public String getMessage() {
        return strMessage;
    }

    public void setMessage(String strMessage) {
        this.strMessage = strMessage;
    }

    /**
     *  Returns the redirection URL that OpenID points the browser to when
     *  it needs to perform the authentication
     */
    public String getOpenIdUrl() {
        return openIdUrl;
    }

    public String getOpenIdIdentifier() {
        return this.openIdIdentifier;
    }

    public void setOpenIdIdentifier(String identifier) {
        this.openIdIdentifier = identifier;
    }

    /**
     *   Called implicitly by Webwork as per the specification of
     *   the {@link ServletRequestAware} interface.
     *   OpenId authentication needs to have access to the servlet request
     *   so that it can parse the query parameters on its own.
     */
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    /** 
     *   Returns the value of the current username param.
     */
    public String getUsername() {
        return username;
    }

    /** 
     *   Sets a user's name credential (prior to calling
     *   login or logout).
     */
    public void setUsername(String name) {
        checkNotNull(name);
        checkNotEmpty(name);
        username = name;
    }

    /** 
     *   Sets a user's password credential (prior to calling
     *   login or logout).
     */
    public void setUserpass(String passwd) {
        checkNotNull(passwd);
        //checkNotEmpty( passwd );
        userpass = passwd.trim();
    }

    /** 
     *   If a user was redirected from a page because they were
     *   not logged in then this method returns the URL they were
     *   originally trying to access. Calling this method also clears
     *   the URL from the Session so it will not be returned on
     *   subsequent method calls.
     */
    public String getRedirectedUrl() {
        String url = (String) retrieveFromSession("redirected_from");
        if (url != null) {
            removeFromSession("redirected_from");
            log.debug("redirect URL is " + url);
            return url;
        } else {
            return null;
        }
    }

    /** 
     *   Returns true if the passed {@link Contributor} is logged in.
     */
    public static boolean isLoggedIn(Contributor c) {
        checkNotNull(c);
        log.debug("contributor id = " + c.getContributorId());

        if (c.isGuest()) {
            return false;
        }

        Contributor cc = Eurocarb.getCurrentContributor();
        if (c.equals(cc)) {
            return true;
        }

        return false;
    }

    /** 
     *   Logs in the user given by {@link #getUsername} or {@link #getOpenIdIdentifier}, providing
     *   the given user exists and can be authenticated by the
     *   password given to {@link #setUserpass} for local logins, and that
     *   the authentication succeeds when using OpenId authentication.
     *
     *   @return
     *   <ul>
     *   <li>"login_success" if successful</li>
     *   <li>"error__username_doesnt_exist" if username not found/known</li>
     *   <li>"error__already_logged_in" if given username is already logged in</li>
     *   <li>"error__login_failed" if the backend authentication fails</li>
     *   <li>"input" if username or userpass not given</li>
     *   </ul>
     */
    public String login() {
//        if (openIdIdentifier != null)
//        {
//            return processOpenIdLogin();
//        }
        if (username != null) {
            return processLocalLogin();
        }
        return "input";
    }

    private String processLocalLogin() {
        Contributor c = getContributor();
        if (c != null && isLoggedIn(c)) {
            log.debug("user is already logged in, returning error__already_logged_in view");
            return "error__already_logged_in";
        }

        if (username == null) {
            log.debug("user name/pass not given, returning login input view");
            return "input";
        }

        if (userpass == null) {
            setMessage("Please specify a valid username and password");
            return "input";
        }


        c = Contributor.lookupExactName(username);


        if (c == null) {
            log.debug("given contributor name doesn't exist, "
                    + "returning error__username_doesnt_exist input view");
            setMessage("User name or password do not match.");
            return "input";
        }

//        if ( username.equalsIgnoreCase("guest") || c.isGuest() )
//        {
//            log.debug( "given contributor name corresponds to guest user, "
//                     + "returning login input view");
//            return "input";
//        }        


        if (!ChangePassword.compareSimple2EncryptedPassword(userpass, c.getPassword())) {
            setMessage("User name or password do not match.");
            log.debug("wrong password");
            return "input";
        }

        if (!c.getIsActivated()) {
            setMessage("Sorry, your account is not activated.");
            log.debug("not activated account");
            return "input";
        }

        if (c.getIsBlocked()) {
            setMessage("Sorry, your account is blocked.");
            log.debug("blocked account");
            return "input";
        }

        if (log.isInfoEnabled()) {
            log.info("logging in contributor '" + c.getContributorName() + "'");
        }

        storeInSession("contributor_id", c.getContributorId());

        if (c.getLastLogin() != null) {
            c.setLastLogin(GregorianCalendar.getInstance().getTime());
            Session session = Eurocarb.getHibernateSession();
            session.save(c);
        } else {

            return "login_first_time";
        }

        return "login_success";
    }

    private String processOpenIdLogin() {
        if (openIdIdentifier == null) {
            return "error__username_doesnt_exist";
        }
        try {
            OpenIdAuthenticator authenticator = new OpenIdAuthenticator();
            getSession().put("authenticator", authenticator);
            this.openIdUrl = authenticator.createAuthenticationRequest(openIdIdentifier, request);
            return "openid_redirect";
        } catch (Exception e) {
            log.error(e);
            return "error__login_failed";
        }
    }

    public String acceptOpenIdLogin() {
        String result = doAcceptOpenIdLogin();
        if (getParameters().get("popup") != null) {
            result += "_popup";
        }
        return result;
    }

    private String doAcceptOpenIdLogin() {
        String identifier;

        try {
            OpenIdAuthenticator authenticator;
            if (getSession().get("authenticator") != null) {
                authenticator = (OpenIdAuthenticator) getSession().get("authenticator");
                getSession().remove("authenticator");
            } else {
                authenticator = new OpenIdAuthenticator();
            }
            if ((identifier = authenticator.isAuthenticationValid(request)) != null) {
                log.info("Validated openid identifier is " + identifier);
                Contributor c = Contributor.lookupByIdentifier(identifier);
                if (c == null) {
                    if (authenticator.getEmail() == null) {
                        addActionError("No email address provided with OpenID login");
                        return "error__login_failed";
                    }
                    c = registerUser(identifier, authenticator.getName(), authenticator.getEmail());
                } else {
                    String name = authenticator.getName();
                    String email = authenticator.getEmail();
                    String userName = (name != null) ? name : "New user";
                    c.setFullName(userName);
                    if (email != null) {
                        c.setContributorName(email);
                    }
                    getEntityManager().store(c);
                }
                storeInSession("contributor_id", c.getContributorId());
                return "login_success";
            }
        } catch (Exception ex) {
            log.error("Caught exception while trying to authenticate OpenID", ex);
            return "error__login_failed";
        }
        return "error__login_failed";
    }

    private Contributor registerUser(String identifier, String name, String email) {
        Contributor c = getEntityManager().createNew(Contributor.class);
        c.setOpenId(identifier);
        String userName = (name != null) ? name : "New user";
        c.setFullName(userName);
        if (email != null) {
            c.setContributorName(email);
        }
        getEntityManager().store(c);
        return c;
    }

    /*
     * If there has been a log out occurring, this method will return null
     * so that the lookup for currentContributor in the ognl stack will return
     * null, instead of the contributor from the Eurocarb static method call
     */
    public Contributor getCurrentContributor() {
        if (logoutSuccessful) { 
            return null;
        } else {
            return super.getCurrentContributor();
        }
    }

    /** Logs the current contributor out */
    public String logout() {
        Contributor c = this.getContributor();

        if (c == null || !isLoggedIn(c)) {
            return "error__not_logged_in";
        }

        log.info("logging out contributor " + c);

        removeFromSession("contributor_id");
        logoutSuccessful = true;

        return "logout_success";

	
    }
} // end class

