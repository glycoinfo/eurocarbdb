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
package org.eurocarbdb.MolecularFramework.examples;
import org.eurocarbdb.MolecularFramework.io.SugarExporterException;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.Linucs.SugarExporterLinucs;
import org.eurocarbdb.MolecularFramework.io.carbbank.SugarImporterCarbbank;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;

/**
* 
*/

/**
* @author rene
*
*/
public class CarbbankToLinucs
{

    /**
     * @param args
     * @throws SugarImporterException 
     * @throws SugarExporterException 
     */
    public static void main(String[] args) throws SugarImporterException, SugarExporterException
    {
        // linear example
        // b-D-Manp-(1-2)-Ery-ol
        String t_strStructure = "b-D-Manp-(1-2)-Ery-ol";
        // branched example
        //b-D-Galp3,4Py-(1-6)+
        //                   |
        //                b-D-Manp-(1-2)-Ery-ol
        //                   |
        //     b-D-Glcp-(1-4)+
        t_strStructure = " b-D-Galp3,4Py-(1-6)+\n                    |\n               b-D-Manp-(1-2)-Ery-ol\n                    |\n      b-D-Glcp-(1-4)+";
        // repeat
        //repeat-6)-a-D-Glcp-(1-2)-Ery-ol-(1-
        //                          |
        //            b-D-Manp-(1-2)+
        t_strStructure = "repeat-6)-a-D-Glcp-(1-2)-Ery-ol-(1-   \n                          | \n            b-D-Manp-(1-2)+";
        // inner repeat
        t_strStructure = "b-D-Galp3,4Py-(1-[6)-a-D-Manp-(1-[4)-b-D-Manf-(1]x-3)-b-D-Manp-(1]nx-2)-Ery-ol";
        // cyclic
        t_strStructure = "        a-D-Glcp-(1-6)+\n                      |\n                 a-D-Glcp-(1-[4)-a-D-Glcp-(1]n-4)-a-D-Glcp-(1-\n                      |\n             cyclic-4)+";
        SugarImporterCarbbank t_objParser = new SugarImporterCarbbank();
        Sugar t_objSugar = t_objParser.parse(t_strStructure);
        SugarExporterLinucs t_objLinucs = new SugarExporterLinucs();
        String t_strLinucs = t_objLinucs.export(t_objSugar);
        System.out.println(t_strLinucs);
    }

}
