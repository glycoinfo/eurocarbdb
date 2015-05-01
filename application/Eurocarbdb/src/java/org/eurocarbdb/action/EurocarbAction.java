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
*   Last commit: $Rev: 1987 $ by $Author: glycoslave $ on $Date:: 2010-09-08 #$  
*/

package org.eurocarbdb.action;

//  stdlib imports
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.MalformedURLException;
import java.util.*;

//  3rd party imports
import org.apache.log4j.Logger;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.opensymphony.webwork.interceptor.CookiesAware;

import com.opensymphony.webwork.interceptor.ParameterAware;
import com.opensymphony.webwork.config_browser.ConfigurationHelper;
import com.opensymphony.webwork.ServletActionContext;

import org.apache.commons.configuration.Configuration;

//  eurocarb imports
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.util.XmlSerialiser;

import org.eurocarbdb.gui.Navigation;

//  static imports
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.util.StringUtils.paramToInt;
import static org.eurocarbdb.util.StringUtils.lcfirst;


import static com.opensymphony.xwork.util.TextParseUtil.translateVariables;


/*  class EurocarbAction  *//****************************************
*<p>
*   Base class for Eurocarb actions.
*</p>
*<p>
*   A note on input parameters: when run in a web context, parameters
*   passed to an Action class (ie: CGI parameters) may be obtained
*   in 2 different ways: from the {@link Map} returned by 
*   {@link #getParameters}, or from the inclusion of a <tt>setAbcde</tt>
*   method in the Action, where <tt>abcde</tt> is the name of a CGI 
*   parameter. <tt>setXxxxx</tt> methods will be called implicitly
*   by the framework if there is a CGI parameter that matches a 
*   corresponding <tt>set</tt> method and the value of the CGI
*   parameter can be coerced to the argument type of the set method.
*</p>
*
*   @author mjh [glycoslave@gmail.com]
*/
public abstract class EurocarbAction extends ActionSupport
implements EurocarbObject, ParameterAware, CookiesAware
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    protected static final Logger log 
    = Logger.getLogger( EurocarbAction.class );
    
    /** Auto-incrementing counter of instantiated actions, primarily used 
    *   to identify which action is which in the logs. */
    private static int actionCounter = 0;
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Map of all parameters fed to this action, normally populated 
    *   by webwork. @see #setParameters  */
    protected Map<String,String[]> params;
    
    /** Identify which submit button in the form has been pressed */
    protected String submitAction = "";
    
    /** Specify the list of steps in a workflow */
    protected String[] progress_steps = null;
    
    /** Specify the current step in a workflow */
    protected String current_step = null;
    
    /** Cookies map */
    protected Map cookiesMap = new HashMap<String,String>();
    
    protected String sugar_image_notation = null;
    
    /** Specifies the format to output from this Action. */
    private String outputFormat = "html";
    public String passErrorMessage=null;
    
    /** Assigned at construction time to actionCounter, the index
    *   of this action relative to all other actions run since the 
    *   code/server was started. Mainly useful for tracking action
    *   executions in the tomcat logs ;-) */
    private final int actionId;
    
    
    protected EurocarbAction() 
    { 
        actionId = ++actionCounter;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~//
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~// 
    
    /*  getCurrentContributor  *//***********************************
    *
    *   Returns the currently-active (ie: "logged on", for most 
    *   intents and purposes) {@link Contributor}, if any, or the
    *   "guest" Contributor ({@link Contributor.getGuestContributor()}) 
    *   if no Contributor is active in the current {@link Thread}. 
    *   Note that only one Contributor can be active per {@link Thread} 
    *   at any one time (although the application may have many concurrent
    *   users/threads).
    *
    *   @see Contributor.getGuestContributor()
    *   @see Eurocarb.getCurrentContributor()
    */
    public Contributor getCurrentContributor()
    {
        return Contributor.getCurrentContributor();   
    }
    
    
    /*  setParameters  *//*******************************************
    *
    *   Sets a Map of input parameters to be used by this action.
    *   This method is implicitly called when run inside the 
    *   webwork/struts framework with any incoming CGI parameters.
    */
    @SuppressWarnings("unchecked")
    public void setParameters( Map params )
    {
        
        //  suppress-warnings is necessary because the webwork
        //  params hash is pre-java 1.5, and so is not typed.
        //  It does however contain String keys, String[] values
        //  so a direct unchecked cast should be ok.
        this.params = (Map<String,String[]>) params;
        if(this.params.containsKey("passErrorMessage"))
        {
            passErrorMessage=(this.params.get("passErrorMessage"))[0];
        }
        
    }
    
    
    /*  getParameters  *//*******************************************
    *<p>
    *   Gets the Map of input parameters that have been provided to 
    *   this action. The keys of the map are parameter names, the values
    *   are an array of String values for those names.
    *</p>
    *<p>
    *   Note that this method of accessing input parameters is 
    *   independent of the 
    *</p>
    */
    public Map<String,String[]> getParameters() {  return params;  }
    
    
    protected String[] parametersWhitelist() 
    {
        ParameterChecking security =
        this.getClass().getAnnotation(ParameterChecking.class);
        if (security != null) 
        {
            return security.whitelist();
        }
        return null;
    }
    
    protected String[] parametersBlacklist() 
    {
        ParameterChecking security =
        this.getClass().getAnnotation(ParameterChecking.class);
        if (security != null) 
        {
            return security.blacklist();
        }
        return null;
    }
    
    /**
    * Use a whitelist/blacklist for this action, to determine which parameters we are going to allow
    * @see ParameterChecking
    */
    public boolean acceptableParameterName(String paramName)
    {
        String[] whitelist = parametersWhitelist();
        String[] blacklist = parametersBlacklist();
        boolean paramsOk = true;
        if (whitelist != null) 
        {
            paramsOk = paramsOk && (Arrays.binarySearch(whitelist,paramName) >= 0);
        }
        if (blacklist != null) 
        {
            paramsOk = paramsOk && (Arrays.binarySearch(blacklist,paramName) < 0);            
        }
        if (! paramsOk ) 
        {
            log.info("Denying setting of parameter "+paramName);
        }
        return paramsOk;
        
    }
    
    /*  getProjectConfiguration  *//************************************
    *
    *   Returns global (project-wide) properties. These properties 
    *   are derived from a eurocarb.properties file on application 
    *   startup.
    *
    *   @see Eurocarb#getConfiguration()
    */
    //public Configuration getProjectConfiguration() {  return Eurocarb.getConfiguration();  }
    
    
    /*  getProperty  *//*********************************************
    *
    *   Returns the value for a global (project-wide) property. These properties 
    *   are derived from various .properties files on application startup,
    *   and from and user preferences if applicable.
    *
    *   @see Eurocarb#getConfiguration()
    */
    public Object getProperty( String property_name ) 
    {  
        return Eurocarb.getConfiguration().getProperty( property_name );  
    }
    
    
    /*  getAllActions  *//*******************************************
    *
    *   Returns a Set view of all action names in the current namespace 
    *   of the current application.
    *
    *   @see Eurocarb#getProperties()
    */
    public Set getAllActions()
    {    
        //  this currently gets only those actions in the ""
        //  webwork namespace i believe. this may change in the future.
        return ConfigurationHelper.getActionNames( "" );               
    }
    
    
    /*  getCurrentActionName  *//************************************
    *
    *   Returns the name of the currently-running action.
    *
    *   @see Eurocarb#getProperties()
    */
    public String getCurrentActionName()
    {
        return ActionContext.getContext().getName();   
    }
    
    
    /*  getCurrentActionNamespace  *//*******************************
    *
    *   Returns the namespace of the currently-running action.
    *
    *   @see Eurocarb#getProperties()
    */
    public String getCurrentActionNamespace()
    {
        return ActionContext.getContext().getName();   
    }
    
    
    /** 
    *   Returns the current output format for this Action. Returns 'html'
    *   by default, unless the {@link #setOutput} has been called, eg: 
    *   <code>my_action.action?output=xml</code>.
    */
    public String getOutput()
    {
        return outputFormat;    
    }
    
    
    /** 
    *   Sets the current output format for this Action. Common values are:
    *   'html' (the default), and 'xml'.
    */
    public void setOutput( String output_format )
    {
        log.debug("setting output format = " + output_format );
        this.outputFormat = output_format;    
    }
    
    
    /** 
    *   Returns true if the current action is capable of producing its results 
    *   in XML format for the (default) result code 'success', according to
    *   the current application configuration. 
    *
    *   @see #getOutput
    *   @see #setOutput
    */
    public boolean canGenerateXml()
    {
        return canGenerateXml( "success" );
    }
    
    
    /** 
    *   Returns true if the current action is capable of producing its results 
    *   in XML format for the given result code, according to the current 
    *   application configuration - basically if there is a result named
    *   <code>"[result_code]-xml"</code>, then this method assumes that this 
    *   {@link Action} is capable of generating an XML result.
    *
    *   @see #getOutput
    *   @see #setOutput
    */
    public boolean canGenerateXml( String result_code )
    {
        String action_name = this.getCurrentActionName();
        String namespace = "";
        
        ActionConfig ac = ConfigurationHelper.getActionConfig( namespace, action_name );
        if ( ac == null )
            return false;
        
        return ac.getResults().containsKey( result_code + "-xml");
    }
    
    
    /**
    *   This method does not return anything useful at the moment, as 
    *   Actions do not have {@link Version}s, per se, yet.
    */
    public String getVersion()
    {
        return "";   
    }
    
    
    
    //~~~  implementation of EurocarbObject interface methods ~~~//
    
    /** 
    *   Returns a value equivalent to the index of this Action in
    *   a list of all EurocarbActions instantiated since the code/server
    *   was started; primarily useful for indentifying which action 
    *   invocation is which in the logs.
    */
    public int getId() 
    {
        return actionId;
    }
    
    
    public <T extends EurocarbObject> Class<T> getIdentifierClass()
    {
        return (Class<T>) this.getClass();
    }
    
    
    /** 
    *   The "type" of an action is always "action". 
    *   {@inheritDoc} 
    *   @see EurocarbObject.getType()
    */
    public final String getType() 
    {
        return "action";
    }
    
    
    /** Set the value of the parameter used to identify which submit 
    *   button in the form has been pressed.
    *   @deprecated actions should manage their own state internally
    */    
    @Deprecated
    public void setSubmitAction(String s) 
    {
        submitAction = s;
    }
    
    
    /** 
    *   Get the name of the submit button in the form that has been pressed 
    *   @deprecated actions should manage their own state internally
    */
    @Deprecated
    public String getSubmitAction() 
    {
        return submitAction;
    }
    
    
    @Deprecated
    public void setProgress( String s ) 
    {
        System.out.println("setProgress " + s);
        if ( s == null ) 
        {
            progress_steps = null;
            current_step =null;
            return;
        }
        
        progress_steps = s.split(",");
        for( int i=0; i<progress_steps.length; i++ ) 
        {
            if( progress_steps[i].startsWith("#") ) 
            {
                progress_steps[i] = progress_steps[i].substring(1);
                current_step = progress_steps[i];
            }
        }
    }
    
    
    @Deprecated
    public String[] getProgressSteps() 
    {
        return progress_steps;
    }
    
    
    @Deprecated
    public String getCurrentStep() 
    {
        return current_step;
    }
    
    
    /** Called implicitly by webwork. */
    @SuppressWarnings("unchecked")
    public void setCookiesMap( Map cookies ) 
    {
        if ( cookies != null )
            cookiesMap = cookies;
        else 
            cookiesMap = new HashMap<String,String>();
        
        if ( log.isDebugEnabled() )
        {
            StringBuilder sb = new StringBuilder("cookies set:\n");
            for( Map.Entry e : (Set<Map.Entry>) cookies.entrySet() ) 
                sb.append( "\t" + e.getKey() + " = " + e.getValue() + "\n" );
            
            log.debug( sb.toString() );
        }
    }
    
    
    public String getCookieValue( String name ) 
    {
        return cookiesMap.get(name).toString();
    }
    
    
    public void setSugarImageNotation( String n ) 
    {
        sugar_image_notation = n;
    }
    
    
    public String getSugarImageNotation() 
    {
        return sugar_image_notation;
    }
    
    /**
    *   Use the entity manager to get an entity, parsing the parameter from the 
    *   parameters hash, and adding errors to the action when this object is not
    *   present
    */
    public <T extends EurocarbObject> T getObjectFromParams(Class<T> c, Map params)
    {
        String paramName = c.getName();
        paramName = lcfirst(paramName.substring(paramName.lastIndexOf(".")+1));
        paramName = paramName + "." + paramName + "Id";
        log.debug("Getting parameter "+paramName);
        
        T returnObject = getObjectFromParams(c, params, paramName);
        if (returnObject == null)
        {
            this.addActionError("Must supply a valid ID for this action");
            if (params.get(paramName) == null) 
            {
                this.addFieldError(paramName, "No identifier provided");
            } else 
            {
                this.addActionError("Required identifier "+paramName+" not valid -"+paramToInt(params.get(paramName))+"-");
                this.addFieldError(paramName, "Bad identifier provided: -"+ paramToInt(params.get(paramName))+"-");
            }
        } else 
        {
            log.debug("Retrieved object for param with id of "+((EurocarbObject) returnObject).getId());            
        }
        return returnObject;
    }
    
    /**
    *   Use the entity manager to get an entity, parsing the parameter from the 
    *   parameters hash
    */
    protected <T> T getObjectFromParams( Class<T> c, Map params, String paramName )
    {
        Object identifier;
        if ((identifier = params.get(paramName)) != null) 
        {
            return Eurocarb.getEntityManager().lookup( c , paramToInt(identifier) );
        }
        return null;
    }
    
    
} // end class

