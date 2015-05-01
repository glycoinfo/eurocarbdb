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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/

package org.eurocarbdb.action.admin;

//  stdlib imports
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.InputStream;
import java.io.IOException;

//  3rd party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

//  eurocarb imports
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class LoadTaxonomy  *//******************************************
*
*   Parses taxonomy information from a given input stream, and loads
*   the resultant taxonomy data as Taxonomy objects into the data model 
*   backed by a given EntityManager.
*
*   @author   mjh <glycoslave@gmail.com>
*   @version  $Rev: 1932 $
*/
public class LoadTaxonomy extends EurocarbAction implements org.eurocarbdb.action.RequiresAdminLogin 
{

    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    protected static final Log log = LogFactory.getLog( LoadTaxonomy.class );
    
    /** EntityManager to which taxonomy objects parsed from the given 
    *   input stream will be stored.  */
    private EntityManager em;
    
    /** Input stream from which to read & parse taxonomy data. */
    private InputStream instreamNames, instreamNodes;
    

    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//        
    
    
    /** Gets the EntityManager to which taxonomy objects parsed from the given 
    *   input stream will be stored.
    */
    public EntityManager getEntityManager() {  return em;  }
    
    
    /** Sets the EntityManager to which taxonomy objects parsed from the given 
    *   input stream will be stored. 
    */
    public void setEntityManager( EntityManager em ) {  this.em = em;  }
    
    
    /** Gets the input stream from which to read & parse taxonomy name data. */
    public InputStream getNamesInputStream() throws IOException
    {
        if ( instreamNames == null ) initDefaultInputStreams();
        return instreamNames;
    }
    
    
    /** Gets the input stream from which to read & parse taxonomy nodes data. */
    public InputStream getNodesInputStream() throws IOException
    {
        if ( instreamNodes == null ) 
            initDefaultInputStreams();
        
        return instreamNodes;
    }


    /*  setNamesInputStream  *//************************************* 
    *
    *   Sets the input stream from which to read & parse taxonomy names
    *   (ie: the NCBI taxonomy "names.dmp" file).
    *
    *   Setting to null forces re-initialisation of default streams --
    *   see the initDefaultInputStreams method.
    *
    *   @see #initDefaultInputStreams
    */
    public void setNamesInputStream( InputStream in ) {  instreamNames = in;  }


    /*  setNodesInputStream  *//************************************* 
    *
    *   Sets the input stream from which to read & parse taxonomy nodes
    *   (ie: the NCBI taxonomy "nodes.dmp" file).
    *
    *   Setting to null forces re-initialisation of default streams --
    *   see the initDefaultInputStreams method.
    *
    *   @see #initDefaultInputStreams
    */
    public void setNodesInputStream( InputStream in ) {  instreamNodes = in;  }


    /*  initDefaultInputStreams  *//********************************* 
    *
    *   Open zip file from local file, extract names & nodes files from the 
    *   zip stream, assign to local fields for later parsing.
    */
    protected void initDefaultInputStreams()
    throws IOException
    {
        log.debug("initialising default input streams for NCBI names/nodes files");
        String ncbi_zipfile_name = Eurocarb.getProperty("ncbi.taxonomy.localfile");
        ZipFile ncbi_zipfile = new ZipFile( ncbi_zipfile_name );  
    
        ZipEntry names_file = ncbi_zipfile.getEntry("names.dmp");
        ZipEntry nodes_file = ncbi_zipfile.getEntry("nodes.dmp");

        this.instreamNames = ncbi_zipfile.getInputStream( names_file );
        this.instreamNodes = ncbi_zipfile.getInputStream( nodes_file );
    }


    /*  execute  *//*************************************************
    *
    */
    public String execute()
    {
        return SUCCESS;
    }


} // end class

