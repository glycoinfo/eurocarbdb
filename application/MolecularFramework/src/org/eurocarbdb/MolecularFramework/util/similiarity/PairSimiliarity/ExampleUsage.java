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
package org.eurocarbdb.MolecularFramework.util.similiarity.PairSimiliarity;

import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.Linucs.SugarImporterLinucs;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCT;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
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
        String t_strCode = "[][D-GAL]{[(4+1)][B-D-GLCP]{[(3+1)][A-D-MANP]{[(3+1)][A-D-MANP]{}}}}";
        MonosaccharideConverter t_objTrans = new MonosaccharideConverter(t_objConf);
        Sugar g1 = t_objImporter.parse(t_strCode);
        GlycoVisitorToGlycoCT t_objTo = new GlycoVisitorToGlycoCT(t_objTrans);
        t_objTo.start(g1);
        g1 = t_objTo.getNormalizedSugar();
        
        //g2
        t_strCode = "[][D-GLC]{[(4+1)][B-D-GLCP]{[(3+1)][A-D-MANP]{[(3+1)][A-D-GULP]{}}}}";
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
        PairSimiliarity t_oComp = new PairSimiliarity (g1,g2);
        
        System.out.println("Score: \t" + t_oComp.getScore());
        System.out.println("Normalized Score: " + t_oComp.getNormalizedScore());
        Integer counter =0;
        
        for (PairData p : t_oComp.getPairs()){
            counter ++;
            Sugar t_oSug = new Sugar ();
            try {                
                t_oSug.addNode(p.getParent());
                t_oSug.addNode(p.getChild());
                t_oSug.addEdge(p.getParent(),p.getChild(),p.getEdge());
                
                SugarExporterGlycoCTCondensed exp = new SugarExporterGlycoCTCondensed ();
                //exp.LineBreakOff();
                exp.start(t_oSug);
                
                System.out.print("PAIR "+counter+":  \n"+exp.getHashCode()+"\n");
                
            } catch (GlycoconjugateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }    
        }        
    }
    
}
