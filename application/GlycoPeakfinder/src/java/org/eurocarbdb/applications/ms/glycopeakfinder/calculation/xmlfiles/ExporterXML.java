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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
/**
* 
*/
package org.eurocarbdb.applications.ms.glycopeakfinder.calculation.xmlfiles;

import java.io.IOException;
import java.sql.SQLException;

/**
* @author Logan
*
*/
public class ExporterXML 
{

    /**
     * @param args
     * @throws IOException 
     * @throws SQLException 
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException 
    {
        DBInterface t_objDB = new DBInterface();
        GeneratorFragment t_objFragments = new GeneratorFragment();
//        t_objFragments.export(t_objDB,args[0]);
        t_objFragments.export(t_objDB,"c:/residue_fragments.xml");
        GeneratorDefault t_objDefault = new GeneratorDefault();
//        t_objDefault.export(t_objDB,args[1]);
        t_objDefault.export(t_objDB,"c:/default_masses.xml");
    }

}
