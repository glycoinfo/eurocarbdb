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
package org.eurocarbdb.action.admin;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.Taxonomy;
import org.eurocarbdb.dataaccess.core.BiologicalContext;
import org.eurocarbdb.dataaccess.core.Reference;
import org.eurocarbdb.dataaccess.core.ExternalDatabaseReference;
import org.eurocarbdb.sugar.SugarSequence;


import com.opensymphony.xwork.Action;
import au.com.bytecode.opencsv.CSVReader;

import java.util.*;
import java.io.*;
import org.apache.commons.io.*;
import org.apache.log4j.Logger;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


public class ImportGlycomeDB extends EurocarbAction implements org.eurocarbdb.action.RequiresAdminLogin  
{

    static final Logger log = Logger.getLogger( ImportGlycomeDB.class );
 
    private File glycomedbFile = null;
    private String glycomedbFileContentType = null;
    private String glycomedbFileFileName = null;  

    private int from_line = -1;
    private int to_line = -1;

    public File getGlycomedbFile() 
    {
        return this.glycomedbFile;
    }

    public void setGlycomedbFile(File file) 
    {
        this.glycomedbFile = file;
    }

    public String getGlycomedbFileContentType() 
    {
        return this.glycomedbFileContentType;
    }

    public void setGlycomedbFileContentType(String contentType) 
    {
        this.glycomedbFileContentType = contentType;
    }

    public String getGlycomedbFileFileName() 
    {
        return this.glycomedbFileFileName;
    }

    public void setGlycomedbFileFileName(String filename) 
    {
        this.glycomedbFileFileName = filename;
    }        

    public void setFromLine(int l) {
        from_line = l;
    }

    public int getFromLine() {
        return from_line;
    }

    public void setToLine(int l) {
        to_line = l;
    }

    public int getToLine() {
        return to_line;
    }

    public String execute() throws Exception 
    {       
        if( glycomedbFile==null )  {
            this.addFieldError( "glycomedbFile", "You must specify the file containing the glycomedb exported as CSV" );
            return "error";
        }
        
        try 
    {
            getEntityManager().beginUnitOfWork();

            // parse CSV
            CSVReader parser = new CSVReader(new FileReader(glycomedbFile),',','\"');
        
            int line_num = 0;
            int count = 0;
            String[] line;
            while( (line = parser.readNext())!=null ) {

                // check format
                if( line.length!=4 )
                    throw new Exception("Invalid number of fields in record " + count);            

                // check from/to
                line_num++;
                if( from_line>=0 && line_num<from_line )
                    continue;
                if( to_line>=0 && line_num>to_line )
                    break;
                log.debug("line " + line_num);
                
                // commit if necessary
                if( count>0 && (count%100)==0 ) {
                    getEntityManager().endUnitOfWork();
                    getEntityManager().beginUnitOfWork();
                }

                // create glycan sequence
                GlycanSequence glycanSequence = null;
                try {
                    SugarSequence seq = new SugarSequence( line[0] );
                    glycanSequence = GlycanSequence.lookupOrCreateNew( seq );
                    glycanSequence.validate();
                    log.info("sequence validated");
                }
                catch(Exception ex) {
                    log.error(ex.getMessage());
                    continue;
                }                

                if( glycanSequence.getGlycanSequenceId()<=0 ) {
                    log.debug("storing sequence");
                    Eurocarb.getEntityManager().store(glycanSequence);
                }
                else
                    log.debug("sequence exists " + glycanSequence.getGlycanSequenceId());
                
                // add biological context
                if( line[1]!=null && line[1].length()>0 ) {
                    Taxonomy tax = Taxonomy.lookupNcbiId( Integer.valueOf(line[1]) );
                    if( tax!=null ) {
                        Eurocarb.getEntityManager().refresh(tax);
                        BiologicalContext bc = new BiologicalContext();
                        bc.setTaxonomy(tax);
                        Eurocarb.getEntityManager().store(bc);
                        glycanSequence.addBiologicalContext(bc);
                    }            
                }

                // add reference
                if( line[2]!=null && line[2].length()>0 ) {        
                    if( line[2].equals("carbbank") )
                        line[2] = "Carbbank";                    
                    Reference ref = ExternalDatabaseReference.lookupOrCreateNew(line[2],line[3]);
                    if( ref.getReferenceId()<=0 ) 
                        Eurocarb.getEntityManager().store(ref);
                    glycanSequence.addReference(ref);
                }

                // update
                Eurocarb.getEntityManager().update(glycanSequence);
                count++;            
            }

            // final commit
            getEntityManager().endUnitOfWork();
        }
        catch(Exception e) {
            // rollback on errors
            getEntityManager().abortUnitOfWork();
            throw e;
        }
        
        return "success";
    }

    public static int main(String[] args) throws Exception {
        if( args.length==0 ) {
            System.err.println("You must specify the file containing the glycomedb exported as CSV" );
            return(-1);
        }
        
        ImportGlycomeDB action = new ImportGlycomeDB();
        action.setGlycomedbFile(new File(args[0]));
        if( args.length>1 )
            action.setFromLine(Integer.valueOf(args[1]));
        action.execute();        
        
        return 0;
    }

}