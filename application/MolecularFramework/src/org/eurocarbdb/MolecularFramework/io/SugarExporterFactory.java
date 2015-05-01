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
package org.eurocarbdb.MolecularFramework.io;

import java.util.ArrayList;

import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCT;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.Glyde.SugarExporterGlydeII;
import org.eurocarbdb.MolecularFramework.io.Linucs.SugarExporterLinucs;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorFromGlycoCT;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;


/**
* @author Logan
*
*/
public class SugarExporterFactory 
{
    public static ArrayList<CarbohydrateSequenceEncoding> getSupportedEncodings()
    {
        ArrayList<CarbohydrateSequenceEncoding> t_aList = new ArrayList<CarbohydrateSequenceEncoding>();
        t_aList.add(CarbohydrateSequenceEncoding.glyde);
        t_aList.add(CarbohydrateSequenceEncoding.linucs);
        t_aList.add(CarbohydrateSequenceEncoding.glycoct_xml);
        t_aList.add(CarbohydrateSequenceEncoding.glycoct_condensed);        
        return t_aList;
    }
    
    public static String exportSugar(Sugar a_objSugar,CarbohydrateSequenceEncoding a_enumEncoding) throws Exception
    {
        String t_strResult = "";
        if ( a_enumEncoding == CarbohydrateSequenceEncoding.glyde )
        {
            SugarExporterGlydeII t_objExporter = new SugarExporterGlydeII();
            t_objExporter.start(a_objSugar);
            t_strResult = t_objExporter.getXMLCode();
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.linucs )
        {
            Config t_objConf = new Config();
            MonosaccharideConverter t_objTrans = new MonosaccharideConverter(t_objConf);
            GlycoVisitorFromGlycoCT t_objFrom = new GlycoVisitorFromGlycoCT(t_objTrans);
            t_objFrom.start(a_objSugar);
            Sugar t_objSugar = t_objFrom.getNormalizedSugar();
            SugarExporterLinucs t_objExporter = new SugarExporterLinucs();
            t_strResult = t_objExporter.export(t_objSugar);            
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.glycoct_xml )
        {
            SugarExporterGlycoCT t_objExporter = new SugarExporterGlycoCT();
            t_objExporter.start(a_objSugar);
            t_strResult = t_objExporter.getXMLCode();
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.glycoct_condensed )
        {
            SugarExporterGlycoCTCondensed t_objExporter = new SugarExporterGlycoCTCondensed();
            t_objExporter.start(a_objSugar);
            t_strResult = t_objExporter.getHashCode();
        }
        else
        {
            throw new Exception("Invalide CarbohydrateSequenceEncoding for Exporter.");
        }
        return t_strResult;
    }
}