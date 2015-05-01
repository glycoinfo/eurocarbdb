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
* $Id: ShowColumn.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/

package org.eurocarbdb.action.hplc;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.Detector;
import org.eurocarbdb.dataaccess.hibernate.*;

import org.hibernate.*;
import org.hibernate.criterion.*;
import java.util.*;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;
import java.util.List;
import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.Column;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.Query;
import org.hibernate.*;
import org.hibernate.cfg.*;

import org.apache.log4j.Logger;

/**
* @author              matthew    
* @version                $Rev: 1549 $
*/
public class ShowColumn extends EurocarbAction {
    
    private Column db_column_list;
    private Column column;

    protected static final Logger logger = Logger.getLogger( Column.class );

    public String execute() throws Exception {
        return INPUT;
    }
 
    
     public Collection<Column> getColumns() {
    logger.info("getDetectors");
    Criteria crit = getEntityManager().createQuery(Column.class);
    logger.info("getDetectors " + crit.list());
    return crit.list();
    } 
    
    public Column getColumn() {
        return column;
    }



    public void setColumn (Column column) {
        this.column = column;
    }
    

}
