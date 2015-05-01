/*
 * Copyright (c) 2010 Srikalyan C. Swayampakula. All rights reserved.
 * 
 *   Author : Srikalyan C. Swayampakula
 *   Name of the File : UserManipulation.java
 *   Created on : Mar 4, 2010 at 10:16:06 PM
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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.eurocarbdb.action.AbstractUserAwareAction;
import org.eurocarbdb.action.RequiresAdminLogin;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.hibernate.Session;

/**
 *
 * @author Srikalyan C. Swayampakula
 */
public class UserManipulation extends AbstractUserAwareAction implements ServletRequestAware, RequiresAdminLogin {

    private String message = "";
    //please don't change the value of title as it will effect the back button.
    private String title="";
    //private String redirectedFrom=this.getCookieValue("redirected_from");
    private String loginName;
    private HttpServletRequest request;
    private List<Contributor> contributors;
    private final String DISPLAY = "display";
    private final String ACTIVATE_STATUS = "activate_status";
    private final String DEACTIVATE_STATUS = "deactivate_status";
    private final String PROMOTE_STATUS = "promote_status";
    private final String DEMOTE_STATUS="demote_status";
    private final String BLOCK_STATUS = "block_status";
    private final String UNBLOCK_STATUS="unblock_status";
    private static final Logger log = Logger.getLogger(UserManipulation.class);

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public List<Contributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<Contributor> contributors) {
        this.contributors = contributors;
    }

    public String showRequests() {
        //if(Eurocarb.getCurrentContributor()==null || !Eurocarb.getCurrentContributor().getIs)
        contributors = (Eurocarb.getCurrentContributor()).getInactiveContributors();
        if (contributors != null && contributors.size() == 0) {
            message = "Sorry, There are no activation requests";
        }
        return DISPLAY;
    }

    public String activate() {
        Contributor admin = Eurocarb.getCurrentContributor();
        if (!admin.getIsAdmin()) {
            message = "You cannot activate as you are not an admin";
        } else if (loginName == null || loginName.length() == 0) {
            message = "Oops! A network error occured please try again.";
        } else {
            Contributor c = Contributor.lookupExactName(loginName);
            if (!c.getIsActivated()) {
                String password = RandomPassword.generateRandomPassword();
                c.setPassword(ChangePassword.getBasicEncryptedPassword(password));
                c.setIsActivated(true);
                Session session = Eurocarb.getHibernateSession();
                session.update(c);
                try {
                    SendCustomMail.notifyAccountActivation(c, admin, password);
                } catch (Exception e) {
                    log.debug("Whilst attempting a user attempted to activate their account, and email send failure has occured\n", e);
                    addActionError("An internal network error has occured\nPlease try again later");
                    return "error";
                }
                message = "Contributor account has been activated";
            } else {
                message = "Contributor account has already been activated";
            }
        }
        title="Activation Status";
        return ACTIVATE_STATUS;
    }

    public String deactivate() {
        Contributor admin = Eurocarb.getCurrentContributor();
        if (!admin.getIsAdmin()) {
            message = "You cannot deactivate as you are not an admin";
        } else if (loginName == null || loginName.length() == 0) {
            message = "Oops! A network error occured please try again.";
        } else {
            Contributor c = Contributor.lookupExactName(loginName);
            if (!c.getIsActivated()) {
                try {
                    SendCustomMail.notifyAccountNotActivated(c, admin);
                } catch (Exception ex) {
                    log.debug("Whilst attempting a user attempted to de-activate their account, and email send failure has occured\n", ex);
                }
                Session session = Eurocarb.getHibernateSession();
                session.delete(c);
                message = "Contributor account has been deactivated";
            } else {
                message = "Contributor account has already been activated";
            }
        }
        title="Deactivation Status";
        return DEACTIVATE_STATUS;
    }

    public String showPromotableContributors() {
        contributors = (Eurocarb.getCurrentContributor()).getAllActiveNonAdmins();
        if (contributors != null && contributors.size() == 0) {
            message = "Sorry, There are no promotable contributors";
        }
        return DISPLAY;
    }

    public String promote() {
        Contributor admin = Eurocarb.getCurrentContributor();
        if (!admin.getIsAdmin()) {
            message = "You cannot promote as you are not an admin";
        } else if (loginName == null || loginName.length() == 0) {
            message = "Oops! A network error occured please try again.";
        } else {
            Contributor c = Contributor.lookupExactName(loginName);
            if (!c.getIsActivated()) {
                message = "Contributor account has not been activated";
            } else if (c.getIsAdmin()) {
                message = "Contributor is already an admin";
            } else {
                c.setIsAdmin(Boolean.TRUE);
                Session session = Eurocarb.getHibernateSession();
                session.update(c);
                message = "Contributor is promoted successfully";
              try {
                    SendCustomMail.notifyAccountPromoted(c, admin);
                } catch (Exception e) {
                    log.debug("Whilst attempting a user attempted to promote their account, and email send failure has occured\n", e);
                    return "error";
                }
	     }
        }
        title="Promotion Status";
        return PROMOTE_STATUS;
    }

    public String showDemotableContributors() {
        contributors = (Eurocarb.getCurrentContributor()).getAllActiveAdminsExceptCurrent();
        if (contributors != null && contributors.size() == 0) {
            message = "Sorry, there are no demotable contributors";
        }
        return DISPLAY;
    }

    public String demote() {
        Contributor admin = Eurocarb.getCurrentContributor();
        if (!admin.getIsAdmin()) {
            message = "You cannot demote as you are not an admin";
        } else if (loginName == null || loginName.length() == 0) {
            message = "Oops! A network error occured please try again.";
        } else if (loginName.equalsIgnoreCase(admin.getContributorName())) {
            message = "Sorry, you cannot demote yourselves.";
        } else {
            Contributor c = Contributor.lookupExactName(loginName);
            if (!c.getIsActivated()) {
                message = "Contributor account has not been activated";
            } else if (!c.getIsAdmin()) {
                message = "Contributor is not an admin";
            } else {
                c.setIsAdmin(Boolean.FALSE);
                Session session = Eurocarb.getHibernateSession();
                session.update(c);
                message = "Contributor is demoted successfully";
                try {
                    SendCustomMail.notifyAccountDemoted(c, admin);
                } catch (Exception e) {
                    log.debug("Whilst attempting a user attempted to demote their account, and email send failure has occured\n", e);
                    return "error";
                }
	      }
        }
        title="Demotion Status";
        return DEMOTE_STATUS;
    }

    public String showUnblockedContributors() {
        contributors = (Eurocarb.getCurrentContributor()).getAllActiveUnblockedContributors();
        if (contributors != null && contributors.size() == 0) {
            message = "Sorry, there are no unblocked contributors";
        }
        return DISPLAY;
    }

    public String block() {
        Contributor admin = Eurocarb.getCurrentContributor();
        if (!admin.getIsAdmin()) {
            message = "You cannot block as you are not an admin";
        } else if (loginName == null || loginName.length() == 0) {
            message = "Oops! A network error occured please try again.";
        } else if (loginName.equalsIgnoreCase(admin.getContributorName())) {
            message = "Sorry, you cannot block yourselves.";
        } else {
            Contributor c = Contributor.lookupExactName(loginName);
            if (!c.getIsActivated()) {
                message = "Contributor account has not been activated";
            } else if (c.getIsBlocked()) {
                message = "Contributor is already blocked";
            } else if (c.getIsAdmin()) {
                message = "Sorry, you cannot block an admin. Please demote the Contributor to block the Contributor.";
            } else {
                c.setIsBlocked(Boolean.TRUE);
                Session session = Eurocarb.getHibernateSession();
                session.update(c);
                message = "Contributor is blocked successfully";
                try {
                    SendCustomMail.notifyAccoutBlocked(c, admin);
                } catch (Exception e) {
                    log.debug("Whilst attempting a user attempted to block their account, and email send failure has occured\n", e);
                    //addActionError("An internal network error has occured\nPlease try again later");
                    return "error";
                }
            }
        }

        title="Block Status";
        return BLOCK_STATUS;
    }

    public String showBlockedContributors() {
        contributors = (Eurocarb.getCurrentContributor()).getAllBlockedContributors();
        if (contributors != null && contributors.size() == 0) {
            message = "Sorry, there are no blocked contributors";
        }
        return DISPLAY;
    }

    public String unblock() {
        Contributor admin = Eurocarb.getCurrentContributor();
        if (!admin.getIsAdmin()) {
            message = "You cannot unblock as you are not an admin";
        } else if (loginName == null || loginName.length() == 0) {
            message = "Oops! A network error occured please try again.";
        } else {
            Contributor c = Contributor.lookupExactName(loginName);
            if (!c.getIsActivated()) {
                message = "Contributor account has not been activated";
            } else if (!c.getIsBlocked()) {
                message = "Contributor is not blocked";
            } else {
                c.setIsBlocked(Boolean.FALSE);
                Session session = Eurocarb.getHibernateSession();
                session.update(c);
                message = "Contributor is unblocked successfully";
                try {
                    SendCustomMail.notifyAccoutUnblocked(c, admin);
                } catch (Exception e) {
                    log.debug("Whilst attempting a user attempted to unblock their account, and email send failure has occured\n", e);
                    //addActionError("An internal network error has occured\nPlease try again later");
                    return "error";
                }
            }
        }
        title="Unblock Status";
        return UNBLOCK_STATUS;
    }
}
