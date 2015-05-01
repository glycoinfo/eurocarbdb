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

package org.eurocarbdb.interceptor;

//  stdlib imports

//  3rd party imports 
import org.apache.log4j.Logger;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.Result;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.opensymphony.xwork.interceptor.PreResultListener;
import com.opensymphony.xwork.interceptor.ExceptionMappingInterceptor;
import com.opensymphony.xwork.interceptor.ExceptionHolder;

import org.hibernate.HibernateException;
import org.hibernate.FlushMode;

//  eurocarb imports
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.HibernateEntityManager;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
*<p>
*   Interceptor that manages the current persistence context.
*   This interceptor performs 3 significant functions:
*<ul>
*   <li>Catches any/all {@link Exception}s thrown by {@link Action}s 
*       and nested {@link Interceptor}s. See 'Transaction' and 'Exception' 
*       sections below.</li>
*   <li>Executes a manual {@link EntityManager.flush()} of the 
*       persistence context <em>after</em> the Action is executed, but 
*       <em>before</em> the View is executed or rendered.</li>
*   <li>Sets the persitence context as read-only for the rendering of the View.</li>
*</ul>
*</p>
*
*<h2>Lifecycle</h2>
*<p>
*<ol>
*   <li>Request is received by client.</li>
*   <li>{@link #intercept} is called.</li>
*   <li>{@link #handlePreExecutePhase} is called.</li>
*   <li>{@link Action} is executed (by <code>super.intercept</code>)</li>
*   <li>If no Exceptions were thrown, {@link #handlePostExecutePreViewPhase} 
*       is called, which flushes the persistence context. If there was an 
*       Exception, see 'Exceptions' section below.</li>
*   <li>{@link #handlePostExecutePostViewPhase} is called, which 
*       sets the persistence context to be <em>read-only</em></li>
*   <li>Control is allowed to pass to the View, and view is rendered.</li>
*   <li>Request to sent to client.</li>
*</ol>
*</p>
*
*<h2>Transactions</h2>
*<p>
*   Transactions are not opened and closed by this class (see 
*   {@link org.eurocarbdb.servlet.filter.HibernateSessionRequestFilter}), 
*   unless an Exception is caught, in which case the current transaction 
*   is aborted by calling {@link EntityManager.abortUnitOfWork()}.
*<p>
*
*<h2>Exceptions</h2>
*<p>
*   If an {@link Exception} is caught during Action execution (this includes
*   Exceptions thrown by other {@link Interceptor}s), the webwork base class
*   {@link ExceptionMappingInterceptor} compares the class of the Exception
*   to the list of configured exception-mappings in the application config
*   (conf/xwork.xml). If mapped, the Exception is consumed (and logged) and
*   the mapped result name returned as the result code for the Action. See
*   <a href="http://www.opensymphony.com/webwork/wikidocs/Exception%20Handling.html">
*   the online docs</a> or {@link ExceptionMappingInterceptor} for further info. 
*</p>
*<p>
*   If an Exception class is <em>not</em> mapped, the Exception is allowed 
*   to propagate (and probably return a Exception trace to the browser/user/console).
*   This is for ease of debugging. Obviously, for production we want all 
*   Exceptions to be mapped.
*</p>
*
*   @see org.eurocarbdb.dataaccess.EntityManager
*   @see org.eurocarbdb.dataaccess.HibernateEntityManager
*   @see ActionInvocation
*
*   @version $Rev: 1549 $
*   @author mjh
*/
public class PersistenceLifecycleInterceptor extends ExceptionMappingInterceptor 
implements Interceptor, PreResultListener
{
    /** Logging handle. */
    private static final Logger log = Logger.getLogger( PersistenceLifecycleInterceptor.class );
    
    
    private EntityManager em; 
    
    private boolean exceptionWasThrown = false;

    private boolean exceptionIsHandled = false;
    
    private boolean unitOfWorkAborted = false;
    
    private long start = System.currentTimeMillis();
    
    private long elapsed = 0;

    
    /** Called implicitly by webwork when class is first used */
    public void init() 
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( 
                this.getClass().getName() 
                + " loaded" 
            );
        }
        super.init();
    }

    
    /** Called implicitly by webwork on application shutdown */
    public void destroy() 
    {
        super.destroy();
    }
    
    
    /** 
    *   Calls {@link HibernateEntityManager.flush()} for every 
    *   {@link ActionInvocation} that completes without throwing 
    *   an Exception. 
    *
    *   Also logs the result string returned by the underlying {@link Action}.
    */
    public String intercept( ActionInvocation ai )
    throws Exception
    {
        //  add flush handler: "preResult" means that the 'beforeResult'
        //  method will be called *after* the Action gets executed, but 
        //  *before* control has been passed to the View.
        ai.addPreResultListener( this ); 
        
        this.em = getEntityManager();
        
        handlePreExecutePhase();
        
        try
        {
            //*** action executes here ***
            String result_code = super.intercept( ai );
            
            // any code from here on executes *after* the view has been rendered     
            log.info("Action returned result code: " + result_code );
            
            return result_code;
        }
        catch ( Exception ex )
        {
            this.exceptionWasThrown = true;
            this.exceptionIsHandled = false;
            handlePostExecuteException( ex );
            throw ex;
        }
        finally
        {
            handlePostExecutePostViewPhase();
        }
    }
    
    
    /** 
    *   This method calls {@link #handlePostExecutePreViewPhase()} 
    *   after the {@link Action} is run, but before the View phase 
    *   has rendered the {@link Result}. It is hooked into
    *   the action lifecycle by {@link ActionInvocation.addPreResultListener}
    *   called during the call to {@link intercept}.
    */
    public void beforeResult( ActionInvocation ai, String resultCode ) 
    {
        handlePostExecutePreViewPhase();
    }
    
    
    
    /** 
    *   Manually flushes the Hibernate session, if there is one, thereby
    *   committing unsaved data to the database in the form of inserts/updates. 
    */
    public void flush() 
    {
        EntityManager em = getEntityManager();
        if ( em instanceof HibernateEntityManager )
        {
            log.debug("flushing Hibernate session");
            HibernateEntityManager hem = (HibernateEntityManager) em;
            // try 
            // {
            hem.flush(); // allow flush exceptions to be thrown & caught
            // } 
            // catch ( Exception ex ) 
            // {
            //     log.warn("Caught exception while trying to flush data store", ex );
            //     this.exceptionWasThrown = true;
            //     throw ex;
            // } 
            // finally 
            // {
            //     //  change the FlushMode to effectively read-only to render
                //  view to improve Hibernate performance
            log.debug("setting Hibernate flush_mode to MANUAL (ie: read-only)");
            hem.getHibernateSession().setFlushMode( FlushMode.MANUAL );
            // }
        }
        else log.debug("(not a hibernate entity manager)");
    }

    
    /**
    *   Called before Action or View have been executed. Current 
    *   implementation sets the Hibernate Session to be 
    *   {@link FlushMode.AUTO} (which it probably is already..).
    */
    protected void handlePreExecutePhase()
    {
        //  reset Hibernate FlushMode to AUTO (just in case it 
        //  wasn't already). AUTO means DB operations will be performed
        //  asynchronously, batched together near the end of the transaction
        //  to improve performance & concurrency.
        if ( em instanceof HibernateEntityManager )
        {   
            HibernateEntityManager hem = (HibernateEntityManager) em;
            log.trace("resetting Hibernate flush_mode to AUTO");
            hem.getHibernateSession().setFlushMode( FlushMode.AUTO );
        }
        
        log.info("time elapsed since init: " + elapsed() + "millsecs");
    }
    
    
    /** 
    *   Called *after* action has been executed but *before* View has 
    *   been executed or rendered; current implementation calls 
    *   {@link EntityManager.flush()}.
    */
    protected void handlePostExecutePreViewPhase()
    {
        elapsed();
        flush();
        log.debug("flush took " + elapsed() + "millsecs");
    }
    
    
    /** 
    *   Called *after* action and view have both been executed.
    *   Current implementation does nothing.
    */
    protected void handlePostExecutePostViewPhase()
    {
        log.info("rendering of view took " + elapsed() + "millsecs");
        
        /*
        if ( hem != null && hem.getHibernateSession().getTransaction().isActive() )
        {
            log.debug("force clearing session");
            hem.getHibernateSession().clear();   
        }
        */
    }
    

    /**
    *   Called if any {@link Exception} was thrown by the {@link Action} 
    *   or nested {@link Interceptor}s. Current implementation does the
    *   following:
    *<ol>
    *   <li>If the Exception is configured to be handleable by the application,
    *       then the method logs the exception and returns.</li>
    *   <li>If the Exception is not configured as handleable, then the 
    *       Exception is allowed to propagate.
    *</ol>
    *   In both cases, the current transaction is aborted.
    *
    *   @see EntityManager.abortUnitOfWork()
    */
    protected void handlePostExecuteException( Exception ex )
    {
        if ( unitOfWorkAborted )
            return;
        
        assert exceptionWasThrown == true;
        
        if ( exceptionIsHandled )
        {
            log.warn( 
                "Caught handled exception transaction will be aborted "
                + "and an exception result will be rendered", 
                ex 
            );
        }
        else
        {
            log.fatal( 
                "Caught unhandled exception (" 
                + ex.getClass().getSimpleName() 
                + "), transaction will be aborted "
                + "and exception rethrown"
            );
        }
        
        getEntityManager().abortUnitOfWork();
        
        unitOfWorkAborted = true;
        
        return;
    }
    
    
    @Override
    protected void handleLogging( Exception ex )
    {
        this.exceptionWasThrown = true;
        this.exceptionIsHandled = true;
        handlePostExecuteException( ex );
        super.handleLogging( ex );
    }
    
    
    /** 
    *   This method is ONLY ever called if 1) an {@link Exception} was 
    *   thrown by application code, and 2) the Exception thrown has been
    *   mapped in the app config to be mapped to a result code 
    *   (the 'exception-mappings' element).
    *
    *   Exceptions that are NOT mapped will not cause this method to be 
    *   called but are simply handled as a regular exception.
    */
    @Override
    protected void publishException( ActionInvocation inv, ExceptionHolder exh ) 
    {    
        this.exceptionWasThrown = true;
        this.exceptionIsHandled = true;
        handlePostExecuteException( exh.getException() );
        if (inv.getAction() instanceof ActionSupport ) {
            ((ActionSupport) inv.getAction()).addActionError("An error occurred generating this page");
        }
        super.publishException( inv, exh );
    }
    
    
    private final int elapsed()
    {
        long now = System.currentTimeMillis();
        elapsed = now - start; 
        start = now;
        
        return (int) elapsed;
    }
    
} // end class PersistenceLifecycleInterceptor
