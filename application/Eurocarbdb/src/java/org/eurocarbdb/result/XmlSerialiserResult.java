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

package org.eurocarbdb.result;

//  stdlib imports
import java.util.Date;
import java.io.IOException;

//  3rd party imports
import org.apache.log4j.Logger;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.webwork.views.freemarker.FreemarkerResult;
import com.opensymphony.webwork.views.freemarker.ScopesHashModel;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;

//  Eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.util.XmlSerialiser;


/**
*<p>
*   Thin wrapper class around the Webwork {@link FreemarkerResult}
*   renderer that adds {@link EurocarbAction} XML rendering support
*   using {@link XmlSerialiser}.
*</p>
*<p>
*   This class adds 2 template variables for use in Freemarker XML templates,
*   in addition to those that are added by Webwork.
*   These variables are:
*<ul>
*   <li><tt>xmlio</tt> - A reference to the {@link XmlSerialiser}</li>
*   <li><tt>dateGenerated</tt> - The {@link Date} this Result was generated (ie: right now)</li>
*</ul>
*</p>
*
* @author mjh
* @version $Rev: 1549 $
*/
public class XmlSerialiserResult extends FreemarkerResult 
{
    /** Logging handle */
    static Logger log = Logger.getLogger( XmlSerialiserResult.class );

    /**
    *   Overriden to inject extra template variables into the Freemarker
    *   data model. 
    *
    * @return 
    *   The {@link TemplateModel} returned is actually an instance of 
    *   {@link ScopesHashModel}.
    */
    @Override
    protected TemplateModel createModel() throws TemplateModelException
    {
        TemplateModel model = super.createModel();
        if ( ! (model instanceof ScopesHashModel ))
        {
            throw new UnsupportedOperationException(
                "Received TemplateModel was not a ScopesHashModel "
                + "-- this probably means Webwork has changed something on us"
            );
        }
        
        ScopesHashModel hash = (ScopesHashModel) model;
        
        XmlSerialiser xmlio = new XmlSerialiser();
        
        try 
        {  
            xmlio.setWriter( this.getWriter() );  
        }
        catch ( IOException ex )
        {
            //  can this even happen...?
            log.warn( ex );   
        }
        
        //  add essential stuff to the model that the freemarker 
        //  XML template will see. this is in addition to all the 
        //  regular stuff supplied by webwork.
        //
        hash.put( "xmlio", xmlio );
        hash.put( "dateGenerated", new Date() );
        
        return hash;
    }
    
    
    /**
    *   Overriden to log execution only.
    *   {@inheritDoc}
    */
    @Override
    public void doExecute( String location, ActionInvocation invocation )
    throws IOException, TemplateException
    {
        log.debug("rendering XML template: " + location );
        super.doExecute( location, invocation );
    }
    
    
    /**
    *   Unconditionally returns "text/xml".
    */
    @Override
    public final String getContentType() 
    {
        return "text/xml";  
    }
    
}
