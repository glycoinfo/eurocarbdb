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
package test.eurocarbdb.dataaccess;

import java.util.*;

import org.apache.log4j.Logger;
import org.testng.annotations.*;

import test.eurocarbdb.CommandLineTest;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*   @author mjh
*   @version $Rev: 1147 $
*/
public class CoreApplicationTest extends CommandLineTest
{
    /** logging handle */
    static Logger log = Logger.getLogger( CoreApplicationTest.class );    
    
    public void setup()
    {
        log.info("starting Eurocarb unit of work");
        getEntityManager().beginUnitOfWork();
    }
    
    
    public void teardown()
    {
        log.info("ending Eurocarb unit of work");
        getEntityManager().endUnitOfWork();
    }

        
}
