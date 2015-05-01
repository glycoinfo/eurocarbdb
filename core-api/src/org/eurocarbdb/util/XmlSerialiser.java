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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package org.eurocarbdb.util;

//  stdlib imports
import java.util.Map;
import java.util.HashMap;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;

//  3rd party imports
import org.apache.log4j.Logger;
import freemarker.template.Template;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

//  eurocarb imports
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.core.GlycanSequence;

//  static imports
import static java.util.Collections.synchronizedMap;
import static org.eurocarbdb.util.JavaUtils.checkNotNull;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*   Serialises Eurocarb objects to and from XML. 
*
*   This implementation uses Freemarker templates as the primary
*   driver of XML structure and content. Properties and associations
*   of serialised objects are processed recursively in depth-first 
*   order.
*
*<h2>Typical usage</h2>
*<ol>   
*<li>Serialise to an output {@link Writer} stream:
*<pre>
*       GlycanSequence gs = ...;
*       XmlSerialiser xmlio = new XmlSerialiser();
*
*       //  note: defaults to System.out unless specified anyway... 
*       xmlio.setWriter( System.out );
*   
*       xmlio.serialise( gs );
*</pre>
*</li>
*<li>Serialise to a {@link String}, using a {@link StringWriter}:
*<pre>
*       GlycanSequence gs = ...;
*       XmlSerialiser xmlio = new XmlSerialiser();
*
*       //  note: be aware that the xml produced may be *large*
*       xmlio.setWriter( new StringWriter() );
*
*       xmlio.serialise( gs );
*
*       String xml = xmlio.getWriter().toString();
*</pre>
*</li>
*</ol>
*
*<h2>Freemarker templates</h2>
*<p>
*   The templates for processed objects are expected to be named 
*   after the class of the object being serialised, plus ".xml.ftl". 
*   For example, serialising a {@link org.eurocarbdb.dataaccess.core.GlycanSequence} 
*   object will look for a template "org/eurocarbdb/dataaccess/core/GlycanSequence.xml.ftl"
*   in the classpath. If this template isn't found, templates corresponding to
*   each of the superclasses of the serialised object will be looked for.
*</p>
*<p>
*   Templates are provided with the following Freemarker variables:
*   <ul>
*       <li>x - reference to the object currently being serialised</li>
*       <li>xmlio - reference to the XmlSerialiser object itself</li>
*       <li>show_detail - boolean indicating whether to produce brief or detailed XML for 
*           the current object</li>
*   </ul>
*</p>
*<p>
*   Regarding the <tt>show_detail</tt>flag: "brief" XML is intended to provide
*   a minimal XML representation of an object, enough to establish a link to 
*   that object, but little actual object data. It must at least contain the 
*   object's main id as an attribute, eg:
*   <pre>
*       &lt;glycan id="1234" /&gt;
*       &lt;reference id="1234" type="journal" ref-name="pubmed" ref-id="9999" /&gt;
*   </pre>
*   "detailed" XML is intended to contain all the information shown in the brief
*   representation, plus all/most object data.
*</p>
*
*   @author mjh
*/
public class XmlSerialiser 
{
    /** logging handle */
    static Logger log = Logger.getLogger( XmlSerialiser.class );    
    
    /** Freemarker {@link Template} factory. */
    static Configuration freemarkerConfig = null;

    /** Hash of Class to template name for Class. */
    private static Map<Class,Template> cache 
        = synchronizedMap( new HashMap<Class,Template>() );
    
    /** Output Writer for XML. */
    private Writer out;

    
    /** Returns the Writer for output. Defaults to {@link System#out}. */
    public Writer getWriter() 
    {  
        if ( out == null )
            out = new OutputStreamWriter( System.out );
        
        return out;  
    }
    
    
    /** Sets the Writer for output. */
    public void setWriter( Writer out )
    {
        this.out = out;    
    }
    
    
    /**
    *   Outputs detailed XML for the given object to the {@link Writer}
    *   given by {@link #getWriter()}.
    */
    public void serialise( EurocarbObject x ) throws Exception
    {
        serialise( x, true );
    }
    
    
    /**
    *   Outputs brief or detailed XML for the given object to the {@link Writer}
    *   given by {@link #getWriter()}.
    */
    public void serialise( EurocarbObject x, boolean showDetail ) throws Exception
    {
        Template template = getTemplate( x.getClass() );
        if ( template == null )
        {
            log.warn("No XML template for object of class " + x.getClass() );
            out.write("<!-- no xml written to handle objects of " 
                + x.getClass() 
                + " -->");
            return;
        }
            
        optimise( x );
        
        Map<String,Object> data = new HashMap<String,Object>();
        
        data.put("x", x );
        data.put("xml", this );
        data.put("show_detail", showDetail );
        
        template.process( data, this.getWriter() );
        this.getWriter().flush();
    }


    /** 
    *   Returns the Freemarker {@link Template} for the given {@link Class}. 
    */
    protected Template getTemplate( Class<?> c )
    throws IOException
    {
        if ( cache.containsKey( c ) )
            return cache.get( c );
        
        Template t = findTemplate( c );
        
        //  even if it's null, we cache it to save looking it up next time.
        cache.put( c, t );
        
        return t;
    }
    
    
    private Template findTemplate( Class<?> c )
    throws IOException
    {
        checkNotNull( c );
        if ( freemarkerConfig == null )
        {
            freemarkerConfig = new Configuration();
            freemarkerConfig.setObjectWrapper( new DefaultObjectWrapper() );
        }
    
        String template_name = getTemplateName( c );
        log.debug("trying to load template: " + template_name );
        Template t = null;
        try
        {
            t = freemarkerConfig.getTemplate( template_name );
            if ( t != null ) 
            {
                return t;
            }
        }
        catch ( FileNotFoundException ex )
        {
            log.debug("couldn't find a template for " + template_name );
            c = c.getSuperclass();
            return ( c != null ) 
                ? getTemplate( c )
                : null;
        }
        
        return t;
    }
    
    
    /** 
    *   Returns the pathname of the Freemarker template file for the 
    *   given {@link Class}. Current implementation uses the full name
    *   of the passed class plus ".xml.ftl".
    */
    protected String getTemplateName( Class<?> c )
    {
        freemarkerConfig.setClassForTemplateLoading( c, "" );
        return c.getSimpleName() + ".xml.ftl";
    }
    
    
    protected void optimise( Object x )
    {
        getEntityManager().populate( x );   
    }
    
} // end class XmlSerialiser 




