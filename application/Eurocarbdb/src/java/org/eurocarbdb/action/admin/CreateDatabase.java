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
* $Id: CreateDatabase.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package org.eurocarbdb.action.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.Eurocarb;

import org.eurocarbdb.dataaccess.hibernate.*;
import org.hibernate.HibernateException;

/** Create the database schemas for running ECDB
* @author             hirenj
* @version                $Rev: 1549 $
*/
public class CreateDatabase extends EurocarbAction implements org.eurocarbdb.action.RequiresAdminLogin {

    /** Logging handle. */
    protected static final Log log = LogFactory.getLog( CreateDatabase.class );
    
    private String psqlBinary;
    
    public String readInputStream(InputStream stream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int i;

        while((i = stream.read()) != -1) {
            bout.write(i);
        }

        byte[] data = bout.toByteArray();
        return new String(data);
    }

    private Process runSql(String path) throws IOException {
        log.info("Running command : "+getPsqlBinary()+" -f "+path+" -U "+Eurocarb.getProperty("ecdb.db.username")+" -h "+Eurocarb.getProperty("ecdb.db.hostname")+ " "+Eurocarb.getProperty("ecdb.db.name"));
        return Runtime.getRuntime().exec(getPsqlBinary()+" -f "+path+" -U "+Eurocarb.getProperty("ecdb.db.username")+" -h "+Eurocarb.getProperty("ecdb.db.hostname")+ " "+Eurocarb.getProperty("ecdb.db.name"));        
    }
    
    public String execute() throws IOException, InterruptedException {
        if (getPsqlBinary() == null) {
            return INPUT;
        }
        Process process;
        String path = getClass().getResource("/sql/create_schema_core.sql").getPath();
        process = runSql(path);
        log.info(readInputStream(process.getErrorStream()));
        if ( process.waitFor() != 0 ) {
            log.info("Core schema creation failed!");
            return ERROR;
        } else {
            log.info("Core schema created");
        }
/*
*      For some reason this hangs!!!
*/
/*
        path = getClass().getResource("/sql/create_schema_core_comments.sql").getPath();
        process = runSql(path);
        if ( process.waitFor() != 0 ) {
            log.info("Core schema comments creation failed!");
            return ERROR;
        } else {
            log.info("Core schema comments created");
        }
*/
        path = getClass().getResource("/sql/create_schema_hplc.sql").getPath();
        process = runSql(path);
        log.info(readInputStream(process.getErrorStream()));        
        if ( process.waitFor() != 0 ) {
            log.info("HPLC schema creation failed!");
            return ERROR;
        } else {
            log.info("HPLC schema created");
        }
        path = getClass().getResource("/sql/create_schema_ms.sql").getPath();
        process = runSql(path);
        log.info(readInputStream(process.getErrorStream()));
        if ( process.waitFor() != 0 ) {
            log.info("MS schema creation failed!");
            return ERROR;
        } else {
            log.info("MS schema created");
        }
        return SUCCESS;
    }

    /**
     * @return the psqlBinary
     */
    public String getPsqlBinary() {
        return psqlBinary;
    }

    /**
     * @param psqlBinary the psqlBinary to set
     */
    public void setPsqlBinary(String psqlBinary) {
        this.psqlBinary = psqlBinary;
    }
}
