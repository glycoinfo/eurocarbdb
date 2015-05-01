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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/
/**
* $Id: BaseActionTestSuite.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package test.eurocarbdb.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

import org.eurocarbdb.servlet.init.EurocarbApplicationContextHandler;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.action.EurocarbAction;

import org.testng.annotations.*;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.ActionProxyFactory;

import org.eurocarbdb.servlet.init.*;

import org.apache.log4j.Logger;

/**
* Base class for all Action-based test suites. Inheriting from this class will
* initialise the database and webwork configuration
*  
* @author             hirenj
* @version                $Rev: 1549 $
*/
@Test( groups = { "ecdb" } ) 
public class BaseActionTestSuite 
{
    /** Logging handle. */
    static final Logger log = Logger.getLogger( BaseActionTestSuite.class );

    protected static ActionProxyFactory actionFactory;
    
    static 
    {
        actionFactory = ActionProxyFactory.getFactory();        
    }

    @BeforeSuite( groups = { "ecdb.action" } )    
    protected void initialiseEnvironment() throws Exception 
    {
        new EurocarbApplicationContextHandler().contextInitialized(null);
    }
    
    @BeforeMethod( groups = { "ecdb.action" } )    
    protected void initialiseTransaction() throws Exception
    {
        log.debug("Initialising the transaction");
        //  start a hibernate transaction
        Eurocarb.getEntityManager().beginUnitOfWork();        
    }
    
    @AfterMethod( groups = { "ecdb.action" } )    
    protected void closeTransaction() throws Exception
    {
        log.debug("Ending the transaction");
        //  main application logic has been run, commit and cleanup
        //  as appropriate.
        Eurocarb.getEntityManager().endUnitOfWork();        
    }
    
    protected <T> T getRandomEntity(Class<T> clazz)
    {
        return (T) Eurocarb.getEntityManager().createQuery(clazz).setMaxResults(1).list().get(0);
    }
    
    protected void setContributor(ActionProxy proxy, int contributorId) {
        ActionContext context = proxy.getInvocation().getInvocationContext();
        context.getSession().put("contributor_id",contributorId);
    }
    
    protected ActionProxy getAction(String namespace, String actionName, Map<String,Object> params,String methodName) throws Exception
    {
        ActionProxy action = getAction(namespace,actionName,params);
        action.setMethod(methodName);
        return action;
    }
    
    /**
     * Get an action from the webwork configuration. The action returned will also execute
     * the interceptors associated with the action
     * @param namespace     Namespace to find the action in
     * @param actionName    Name of action to run (e.g. 'show_tissue_taxonomy') 
     * @param params        Map of parameters to fill the action with
     * @return              Proxy of action to execute
     * @throws Exception
     */
    protected ActionProxy getAction(String namespace, String actionName, Map<String,Object> params) throws Exception 
    {
        HashMap<String, Map<String, Object>> extraContext = new HashMap<String, Map<String, Object>>();
        extraContext.put(ActionContext.PARAMETERS, params);
        ActionProxy actionProxy = null;
        actionProxy = actionFactory.createActionProxy(namespace, actionName, extraContext);
        
        // Only execute the action, but don't render a result
        
        actionProxy.setExecuteResult(false);
        
        ActionContext context = actionProxy.getInvocation().getInvocationContext();

        context.setSession(new HashMap<String,Object>());

        return actionProxy;
    }    
    
    protected ActionProxy getAction(String actionName, Map<String,Object> params) throws Exception 
    {
        String[] actionMethod = actionName.split("!");
        if (actionMethod.length > 1) {
            return getAction("",actionMethod[0],params,actionMethod[1]);
        }
        return getAction("",actionName,params);
    }
    
    protected boolean hasAnError(EurocarbAction action)
    {
        if (action.getActionErrors() == null ) {
            return false;
        }
        return (action.getActionErrors().size() > 0);
    }
    
    protected boolean hasAnError(EurocarbAction action, String fieldName)
    {
        if (action.getFieldErrors() == null ) {
            return false;            
        }
        if ( action.getFieldErrors().get(fieldName) == null ) {
            return false;
        }
        return ((Collection<String>) action.getFieldErrors().get(fieldName)).size() > 0;
    }
}
