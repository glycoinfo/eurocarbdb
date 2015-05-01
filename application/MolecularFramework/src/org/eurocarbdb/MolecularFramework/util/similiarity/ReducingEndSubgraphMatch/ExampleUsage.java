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
package org.eurocarbdb.MolecularFramework.util.similiarity.ReducingEndSubgraphMatch;

import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.Linucs.SugarImporterLinucs;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCT;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;

/**
* @author sherget
*
*/
public class ExampleUsage {

    /**
     * @param args
     * @throws MonosaccharideException 
     * @throws SugarImporterException 
     * @throws GlycoVisitorException 
     */
    public static void main(String[] args) throws ResourcesDbException, SugarImporterException, GlycoVisitorException {
        SugarImporter t_objImporter = new SugarImporterLinucs();
        Config t_objConf = new Config();
        //g1
        String t_strCode = "[][A-D-MANP]{[(4+1)][A-D-MANP]{[(4+1)][A-D-MANP]{[(3+1)][A-D-MANP]{}[(6+1)][A-D-MANP]{}}}}";
        MonosaccharideConverter t_objTrans = new MonosaccharideConverter(t_objConf);
        Sugar g1 = t_objImporter.parse(t_strCode);
        GlycoVisitorToGlycoCT t_objTo = new GlycoVisitorToGlycoCT(t_objTrans);
        t_objTo.start(g1);
        g1 = t_objTo.getNormalizedSugar();
        
        //g2
        t_strCode = "[][A-D-MANP]{[(5+1)][A-D-MANP]{[(2+1)][A-D-MANP]{[(3+1)][A-D-MANP]{}[(6+1)][A-D-MANP]{}}}}";
        SugarImporter t_objImporter2 = new SugarImporterLinucs();
        Config t_objConf2 = new Config();
        MonosaccharideConverter t_objTrans2 = new MonosaccharideConverter(t_objConf2);
        t_objTrans2 = new MonosaccharideConverter(t_objConf2);
        Sugar g2 = t_objImporter2.parse(t_strCode);
        GlycoVisitorToGlycoCT t_objTo2 = new GlycoVisitorToGlycoCT(t_objTrans2);
        t_objTo2.start(g1);
        g1 = t_objTo2.getNormalizedSugar();
        t_objTo2.start(g2);
        g2 = t_objTo2.getNormalizedSugar();
        
        
        // compare
        RESMFuzzy t_oComp = new RESMFuzzy (g1,g2);
        
        t_oComp.setAnomerSensitivity(true);
        t_oComp.setExactLinkageMatch(true);
        t_oComp.setRingsizeSensitivity(true);
        t_oComp.setStereochemistrySensitivity(true);
        t_oComp.setOnlyTopology(true);
        t_oComp.setModificationSensitivity(true);
        
        t_oComp.plotMatrix();
        
            if (t_oComp.isContained()){
                SugarExporterGlycoCTCondensed t_exporter = new SugarExporterGlycoCTCondensed ();
            t_exporter.start(g2);
            
            System.out.println("\n"+t_exporter.getHashCode()+"\n Query structure is contained at reducing end: "+String.valueOf(t_oComp.isContained()));
            }
            else {
                System.out.println("No common subgraph");
            }
        
    }

}
