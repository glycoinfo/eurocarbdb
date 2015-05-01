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

import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCT;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.Linucs.SugarImporterLinucs;
import org.eurocarbdb.MolecularFramework.io.OGBI.SugarImporterOgbi;
import org.eurocarbdb.MolecularFramework.io.bcsdb.SugarImporterBCSDB;
import org.eurocarbdb.MolecularFramework.io.carbbank.SugarImporterCarbbank;
import org.eurocarbdb.MolecularFramework.io.cfg.SugarImporterCFG;
import org.eurocarbdb.MolecularFramework.io.iupac.SugarImporterIupacCondenced;
import org.eurocarbdb.MolecularFramework.io.iupac.SugarImporterIupacShortV1;
import org.eurocarbdb.MolecularFramework.io.iupac.SugarImporterIupacShortV2;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCT;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;

/**
* @author Logan
*
*/
public class SugarImporterFactory 
{
    public static SugarImporter getImporter(CarbohydrateSequenceEncoding a_enumEncoding) throws Exception
    {
        SugarImporter t_objImporter;
        if ( a_enumEncoding == CarbohydrateSequenceEncoding.carbbank )
        {
            t_objImporter = new SugarImporterCarbbank();
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.linucs )
        {
            t_objImporter = new SugarImporterLinucs();
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.ogbi )
        {
            t_objImporter = new SugarImporterOgbi();
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.glycoct_xml )
        {
            t_objImporter = new SugarImporterGlycoCT();
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.glycoct_condensed )
        {
            t_objImporter = new SugarImporterGlycoCTCondensed();
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.bcsdb )
        {
            t_objImporter = new SugarImporterBCSDB();
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.cfg )
        {
            t_objImporter = new SugarImporterCFG();
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.iupac_condenced )
        {
            t_objImporter = new SugarImporterIupacCondenced();
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.iupac_short_v1 )
        {
            t_objImporter = new SugarImporterIupacShortV1();
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.iupac_short_v2 )
        {
            t_objImporter = new SugarImporterIupacShortV2();
        }
        else
        {
            throw new Exception("Invalide CarbohydrateSequenceEncoding for Importer.");
        }
        return t_objImporter;
    }
    
    public static ArrayList<CarbohydrateSequenceEncoding> getSupportedEncodings()
    {
        ArrayList<CarbohydrateSequenceEncoding> t_aList = new ArrayList<CarbohydrateSequenceEncoding>();
        t_aList.add(CarbohydrateSequenceEncoding.carbbank);
        t_aList.add(CarbohydrateSequenceEncoding.linucs);
        t_aList.add(CarbohydrateSequenceEncoding.ogbi);
        t_aList.add(CarbohydrateSequenceEncoding.bcsdb);
        t_aList.add(CarbohydrateSequenceEncoding.cfg);
        t_aList.add(CarbohydrateSequenceEncoding.iupac_condenced);
        t_aList.add(CarbohydrateSequenceEncoding.iupac_short_v1);
        t_aList.add(CarbohydrateSequenceEncoding.iupac_short_v2);
        t_aList.add(CarbohydrateSequenceEncoding.glycoct_xml);
        t_aList.add(CarbohydrateSequenceEncoding.glycoct_condensed);        
        return t_aList;
    }
    
    public static Sugar importSugar(String a_strSequence,CarbohydrateSequenceEncoding a_enumEncoding) throws Exception
    {
        SugarImporter t_objImporter = SugarImporterFactory.getImporter(a_enumEncoding);
        Sugar t_objSugar = t_objImporter.parse(a_strSequence); 
        if ( a_enumEncoding == CarbohydrateSequenceEncoding.carbbank )
        {
            // translate to validated sugar
            Config t_objConf = new Config();
            MonosaccharideConverter t_objTrans = new MonosaccharideConverter(t_objConf);
            GlycoVisitorToGlycoCT t_objTo = new GlycoVisitorToGlycoCT(t_objTrans);
            t_objTo.setNameScheme(GlycanNamescheme.CARBBANK);
            t_objTo.start(t_objSugar);
            t_objSugar = t_objTo.getNormalizedSugar();            
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.linucs )
        {
            // translate to validated sugar
            Config t_objConf = new Config();
            MonosaccharideConverter t_objTrans = new MonosaccharideConverter(t_objConf);
            GlycoVisitorToGlycoCT t_objTo = new GlycoVisitorToGlycoCT(t_objTrans);
            t_objTo.setNameScheme(GlycanNamescheme.GLYCOSCIENCES);
            t_objTo.start(t_objSugar);
            t_objSugar = t_objTo.getNormalizedSugar();            
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.bcsdb )
        {
            // translate to validated sugar
            Config t_objConf = new Config();
            MonosaccharideConverter t_objTrans = new MonosaccharideConverter(t_objConf);
            GlycoVisitorToGlycoCT t_objTo = new GlycoVisitorToGlycoCT(t_objTrans);
            t_objTo.setNameScheme(GlycanNamescheme.BCSDB);
            t_objTo.start(t_objSugar);
            t_objSugar = t_objTo.getNormalizedSugar();            
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.cfg )
        {
            // translate to validated sugar
            Config t_objConf = new Config();
            MonosaccharideConverter t_objTrans = new MonosaccharideConverter(t_objConf);
            GlycoVisitorToGlycoCT t_objTo = new GlycoVisitorToGlycoCT(t_objTrans);
            t_objTo.setNameScheme(GlycanNamescheme.CFG);
            t_objTo.start(t_objSugar);
            t_objSugar = t_objTo.getNormalizedSugar();            
        }
        else if ( a_enumEncoding == CarbohydrateSequenceEncoding.iupac_condenced ||  
                a_enumEncoding == CarbohydrateSequenceEncoding.iupac_short_v1 ||
                a_enumEncoding == CarbohydrateSequenceEncoding.iupac_short_v2 )
        {
            // translate to validated sugar
            Config t_objConf = new Config();
            MonosaccharideConverter t_objTrans = new MonosaccharideConverter(t_objConf);
            GlycoVisitorToGlycoCT t_objTo = new GlycoVisitorToGlycoCT(t_objTrans);
            t_objTo.setNameScheme(GlycanNamescheme.CARBBANK);
            t_objTo.start(t_objSugar);
            t_objSugar = t_objTo.getNormalizedSugar();            
        }
        return t_objSugar;
    }
}
