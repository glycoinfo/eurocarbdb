/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eurocarbdb.action.user;

import com.opensymphony.webwork.interceptor.ServletRequestAware;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.eurocarbdb.action.AbstractUserAwareAction;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.hibernate.Session;
import java.util.Properties;
import java.io.InputStream;

/**
 *
 * @author srikalyan
 */
public class UserRegistration extends AbstractUserAwareAction implements ServletRequestAware {

    private String message = "";
    private String fullName;
    private String institution;
    private String loginName;
    private String email;
    private HttpServletRequest request;
    private final String REGISTRATION_NONE = "registration_none";
    private final String REGISTRATION_SUCCESS = "registration_success";
    private final String REGISTRATION_FAILURE = "registration_failure";
    private static final Logger log = Logger.getLogger(UserRegistration.class);

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
	
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
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

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    public String validateRegistration() {
        if (allFieldsNull()) {
            return REGISTRATION_NONE;
        }
        if (!validateAllFields()) {
            return REGISTRATION_FAILURE;
        }
        Contributor c = new Contributor();
        c.setFullName(fullName);
        c.setContributorName(loginName);
        c.setInstitution(institution);
        c.setEmail(email);
	
	if(ADMIN_ALLOWS_USER_TO_REGISTER.equals("true"))
	{        
		Contributor admin = Eurocarb.getCurrentContributor();
		String password = RandomPassword.generateRandomPassword();
		c.setPassword(ChangePassword.getBasicEncryptedPassword(password));
		c.setIsActivated(true);
		try {
		    SendCustomMail.notifyAccountActivation(c, admin, password);
		} catch (Exception e) {
		    log.debug("Whilst attempting a user attempted to activate their account, and email send failure has occured\n", e);
		    addActionError("An internal network error has occured\nPlease try again later");
		    return "error";
		}
		log.debug("unable to send user registration notification to all admins");
	}
	else
	{
		c.setIsActivated(false);
	}	
	c.setIsBlocked(false);
        Session session = Eurocarb.getHibernateSession();
	session.save(c);      	
	if(ADMIN_ALLOWS_USER_TO_REGISTER.equals("false")){        
		try {
		    SendCustomMail.notifyUserRegistrationToAllAdmins(c);
		} catch (Exception ex) {
		    log.debug("unable to send user registration notification to all admins");
		}	
	}
	//message = "We have got your details. Please wait for admin to respond.";
        return REGISTRATION_SUCCESS;
    }

    private boolean allFieldsNull() {
        if (loginName == null && fullName == null && email == null && institution == null) {
            return true;
        }
        if (fullName != null && fullName.trim().length() == 0) {
            message = "Full Name should not be empty.";
            return true;
        }
        if (institution != null && institution.trim().length() == 0) {
            message = "Institution should not be empty.";
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
        fullName = fullName.trim();
        loginName = loginName.trim();
        institution = institution.trim();
        email = email.trim();
        return false;
    }

    private boolean validateAllFields() {

        if (email.indexOf('@') == -1 || email.indexOf('.') == -1) {
            message = "Please enter a proper email address.";
            return false;
        }

        if(checkIfEmailExists())
        {
            message="Sorry, an account is already registered with this email.";
            return false;
        }

        if (checkIfLoginNameExists()) {
            message = "Sorry, login Name is already taken.";
            return false;
        }
        System.out.println(message);
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
}
