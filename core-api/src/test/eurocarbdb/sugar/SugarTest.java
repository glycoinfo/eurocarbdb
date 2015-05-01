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
*   Last commit: $Rev: 1193 $ by $Author: glycoslave $ on $Date:: 2009-06-11 #$  
*/

package test.eurocarbdb.sugar;

import org.testng.annotations.*;

import test.eurocarbdb.dataaccess.CoreApplicationTest;

import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.sugar.*;
import org.eurocarbdb.sugar.impl.*;

import static java.lang.System.out;
import static org.eurocarbdb.sugar.Anomer.*;
import static org.eurocarbdb.sugar.CommonBasetype.*;


/**
*   Tests programmatic building of {@link Sugar}s.
*
*   @author mjh
*/
@Test( 
    groups={ "sugar.lib" } ) //, 
    // dependsOnGroups={"util.graphs", "sugar.lib.basetype", "sugar.lib.superclass"} )    
public class SugarTest extends CoreApplicationTest
{
    /** The core N-linked GlcNAc2Man3 pentasaccharide. */
    Sugar nLinkedCore;
    
    /** A typical N-linked bi-antennary complex glycan. */
    Sugar nLinkedComplex;
    
    Monosaccharide Man13, Man16;
    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TESTS ~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** 
    *   Build up the N-linked core pentasaccharide programmatically. 
    */
    @Test
    public void createNlinkedCore()
    {
        // setup();
        
        //  create the N-linked core sugar programmatically 
        nLinkedCore = new Sugar();
        
        //  reducing terminal GlcNAc
        Monosaccharide GlcNAc1 = new SimpleMonosaccharide( GlcNAc );
        nLinkedCore.addRootResidue( GlcNAc1 );
        
        //  the 2nd GlcNAc
        Monosaccharide GlcNAc2 = new SimpleMonosaccharide( GlcNAc );
        nLinkedCore.addResidue( GlcNAc1, linkage( Alpha, 1, 4 ), GlcNAc2 );

        //  the bisecting Man
        Monosaccharide Man1 = new SimpleMonosaccharide( Man );
        nLinkedCore.addResidue( GlcNAc2, linkage( Beta, 1, 4 ), Man1 );
        
        //  the core 3' mannose branch
        Man13 = new SimpleMonosaccharide( Man );
        nLinkedCore.addResidue( Man1, linkage( Beta, 1, 3 ), Man13 );

        //  the core 6' mannose branch
        Man16 = new SimpleMonosaccharide( Man );
        nLinkedCore.addResidue( Man1, linkage( Beta, 1, 6 ), Man16 );
     
        out.println( "The core N-linked pentasaccharide:" );
        out.println( nLinkedCore.getGraph().toString() );
        
        assert nLinkedCore.countResidues() == 5 
            : "expected 5 residues, got " 
            + nLinkedCore.countResidues();
        
        assert nLinkedCore.getRootResidue() == GlcNAc1
            : "Mismatched root residue, expected " 
            + GlcNAc1 
            + ", got " 
            + nLinkedCore.getRootResidue();

        // teardown();
        
        return;
    }
        
    
    @Test( dependsOnMethods = {"createNlinkedCore"} )
    public void copySugar()
    {
        assert nLinkedCore != null;
        assert nLinkedCore.countResidues() == 5;
        
        out.println( "Attempting to clone the N-linked core oligosaccharide:" );        
        nLinkedComplex = (Sugar) nLinkedCore.clone();
        out.println( "clone is:" );        
        out.println( nLinkedComplex.getGraph().toString() );        
        
        assert nLinkedComplex != null;
        assert nLinkedComplex != nLinkedCore;
        assert nLinkedComplex.countResidues() == 5;    
    }
    
        
    /** 
    *   Build up an N-linked complex glycan from the N-linked core 
    *   programmatically. 
    */
    @Test( dependsOnMethods = {"copySugar"} )
    public void createNlinkedComplex()
    {
        // setup();
        
        assert nLinkedComplex != null;
        assert nLinkedComplex.countResidues() == 5;
        
        out.println( "Building an N-linked complex glycan from the core oligosaccharide" );        
        
        //  elaborate the core 3' mannose branch
        Monosaccharide GlcNAc13 = new SimpleMonosaccharide( GlcNAc );
        nLinkedComplex.addResidue( Man13, linkage( Beta, 1, 2 ), GlcNAc13 );
        
        Monosaccharide Gal13 = new SimpleMonosaccharide( Gal );
        nLinkedComplex.addResidue( GlcNAc13, linkage( Beta, 1, 4 ), Gal13 );
        
        Monosaccharide NeuAc13 = new SimpleMonosaccharide( NeuAc );
        nLinkedComplex.addResidue( Gal13, linkage( Alpha, 2, 6 ), NeuAc13 );

        //  elaborate the core 6' mannose branch
        Monosaccharide GlcNAc16 = new SimpleMonosaccharide( GlcNAc );
        nLinkedComplex.addResidue( Man16, linkage( Beta, 1, 2 ), GlcNAc16 );
        
        Monosaccharide Gal16 = new SimpleMonosaccharide( Gal );
        nLinkedComplex.addResidue( GlcNAc13, linkage( Beta, 1, 4 ), Gal16 );
        
        Monosaccharide NeuAc16 = new SimpleMonosaccharide( NeuAc );
        nLinkedComplex.addResidue( Gal13, linkage( Alpha, 2, 6 ), NeuAc16 );

        out.println( "N-linked complex glycan is:" );        
        out.println( nLinkedComplex.getGraph().toString() );
        
        //  new sugar == core + 3 residues on 2 antennae == 11 residues
        assert  nLinkedComplex.countResidues() == 11
            :   "expected freshly-built complex oligosaccharide to have "
            +   "11 residues, observed " 
            +   nLinkedComplex.countResidues();
       
        //  make sure we haven't changed core. should still be 5.
        assert  nLinkedCore.countResidues() == 5
            :   "expected core oligosaccharide to only have its original "
            +   "5 residues, observed " 
            +   nLinkedCore.countResidues();
       
        assert nLinkedComplex.isDefinite();
            
        /*        
        //  add a couple of multiconnections, just for fun
        sugar.addLinkage( sugar.getRootResidue(), linkage("3-4"), sugar.lastResidue() );
        sugar.addLinkage( sugar.getRootResidue(), linkage("6-6"), sugar.lastResidue() );
        
        log.info( sugar.toString() );
        */
        
        // teardown();
    }
    
    
    /** java macro to create a monosaccharide, for testing purposes only */ 
    private static final GlycosidicLinkage linkage( Anomer a, int child_pos, int parent_pos )
    {
        return new GlycosidicLinkage( a, parent_pos, child_pos );
    }
    
}

