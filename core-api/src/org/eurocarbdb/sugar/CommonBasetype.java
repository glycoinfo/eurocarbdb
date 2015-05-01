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
*   Last commit: $Rev: 1271 $ by $Author: glycoslave $ on $Date:: 2009-06-26 #$  
*/

package org.eurocarbdb.sugar;


//  stdlib imports
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.util.BitSet;
import org.eurocarbdb.sugar.Monosaccharide;

//  static imports
import static org.eurocarbdb.sugar.Superclass.*;
import static org.eurocarbdb.sugar.StereoConfig.*;
import static org.eurocarbdb.sugar.RingConformation.*;
import static org.eurocarbdb.sugar.CommonSubstituent.*;
import static org.eurocarbdb.sugar.Basetypes.UnknownBasetype;
import static org.eurocarbdb.util.StringUtils.join;


/** 
*<p>
*   Immutable, {@link Enum}-based implementation of {@link Monosaccharide} 
*   interface specifically for "common" basetype monosaccharides, ie: those 
*   monosaccharides that Eurocarb defines as being the fundamental 
*   monosaccharide building blocks of all other permissible monosaccharides.
*</p>
*<p>
*   These basetypes are intended (and expected) to be used declaratively, ie:
*<pre>
*       import static org.eurocarbdb.sugar.CommonBasetype.*;
*       
*       public class Xxx 
*       {
*           public static void main( String[] args )
*           {
*               {@link Monosaccharide} m1 = new {@link SimpleMonosaccharide}( Man );
*               {@link Monosaccharide} m2 = new {@link SimpleMonosaccharide}( GlcNAc );
*               ...
*           }
*       }
*</pre>
*</p>
*
*<h2>Basetype monosaccharides</h2>
*<p>   
*   The following table comes direct from the <a href="http://en.wikipedia.org/wiki/Monosaccharide">
*   wikipedia entry on monosaccharides</a>.
*
*********************************************************************
*<style>
*    table.wikitable     {  border-collapse: collapse; background: #f2f2f2;  } 
*    table.wikitable a   {  display: block;  } 
*    table.wikitable td  {  text-align: center; border: thin solid #ccc; padding: 0.2em;  }
*</style>
*
*<h3>Aldoses</h3>
*<table class="wikitable">
*<tbody><tr>
*<td>Aldotriose</td>
*<td colspan="8"><a href="http://en.wikipedia.org/wiki/File:DGlyceraldehyde_Fischer.svg" class="image" title=""><img alt="D-Glyceraldehyde" src="http://upload.wikimedia.org/wikipedia/commons/thumb/0/02/DGlyceraldehyde_Fischer.svg/73px-DGlyceraldehyde_Fischer.svg.png" border="0" height="78" width="73"></a><br>
*
*<a href="http://en.wikipedia.org/wiki/Glyceraldehyde" title=""><small>D</small>-Glyceraldehyde</a></td>
*</tr>
*<tr>
*<td>Aldotetroses</td>
*<td colspan="4"><a href="http://en.wikipedia.org/wiki/File:DErythrose_Fischer.svg" class="image" title=""><img alt="D-Erythrose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/DErythrose_Fischer.svg/72px-DErythrose_Fischer.svg.png" border="0" height="98" width="72"></a><br>
*<a href="http://en.wikipedia.org/wiki/Erythrose" title=""><small>D</small>-Erythrose</a></td>
*<td colspan="4"><a href="http://en.wikipedia.org/wiki/File:DThreose_Fischer.svg" class="image" title=""><img alt="D-Threose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/DThreose_Fischer.svg/83px-DThreose_Fischer.svg.png" border="0" height="98" width="83"></a><br>
*<a href="http://en.wikipedia.org/wiki/Threose" title="Threose"><small>D</small>-Threose</a></td>
*</tr>
*<tr>
*
*<td>Aldopentoses</td>
*<td colspan="2"><a href="http://en.wikipedia.org/wiki/File:DRibose_Fischer.svg" class="image" title=""><img alt="D-Ribose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/DRibose_Fischer.svg/72px-DRibose_Fischer.svg.png" border="0" height="120" width="72"></a><br>
*<a href="http://en.wikipedia.org/wiki/Ribose" title=""><small>D</small>-Ribose</a></td>
*<td colspan="2"><a href="http://en.wikipedia.org/wiki/File:DArabinose_Fischer.svg" class="image" title=""><img alt="D-Arabinose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/e/ec/DArabinose_Fischer.svg/83px-DArabinose_Fischer.svg.png" border="0" height="120" width="83"></a><br>
*<a href="http://en.wikipedia.org/wiki/Arabinose" title=""><small>D</small>-Arabinose</a></td>
*<td colspan="2"><a href="http://en.wikipedia.org/wiki/File:DXylose_Fischer.svg" class="image" title=""><img alt="D-xylose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/2/29/DXylose_Fischer.svg/83px-DXylose_Fischer.svg.png" border="0" height="120" width="83"></a><br>
*<a href="http://en.wikipedia.org/wiki/Xylose" title=""><small>D</small>-Xylose</a></td>
*<td colspan="2"><a href="http://en.wikipedia.org/wiki/File:DLyxose_Fischer.svg" class="image" title=""><img alt="D-lyxose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/4/45/DLyxose_Fischer.svg/83px-DLyxose_Fischer.svg.png" border="0" height="120" width="83"></a><br>
*<a href="http://en.wikipedia.org/wiki/Lyxose" title=""><small>D</small>-Lyxose</a></td>
*
*</tr>
*<tr>
*<td>Aldohexoses</td>
*<td><a href="http://en.wikipedia.org/wiki/File:DAllose_Fischer.svg" class="image" title=""><img alt="D-allose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/DAllose_Fischer.svg/72px-DAllose_Fischer.svg.png" border="0" height="140" width="72"></a><br>
*<a href="http://en.wikipedia.org/wiki/Allose" title="Allose"><small>D</small>-Allose</a></td>
*<td><a href="http://en.wikipedia.org/wiki/File:DAltrose_Fischer.svg" class="image" title=""><img alt="D-Altrose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/7/78/DAltrose_Fischer.svg/83px-DAltrose_Fischer.svg.png" border="0" height="140" width="83"></a><br>
*<a href="http://en.wikipedia.org/wiki/Altrose" title=""><small>D</small>-Altrose</a></td>
*<td><a href="http://en.wikipedia.org/wiki/File:DGlucose_Fischer.svg" class="image" title=""><img alt="D-Glucose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/1/14/DGlucose_Fischer.svg/83px-DGlucose_Fischer.svg.png" border="0" height="140" width="83"></a><br>
*<a href="http://en.wikipedia.org/wiki/Glucose" title=""><small>D</small>-Glucose</a></td>
*<td><a href="http://en.wikipedia.org/wiki/File:Mannose.svg" class="image" title=""><img alt="D-mannose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/4/45/Mannose.svg/83px-Mannose.svg.png" border="0" height="140" width="83"></a><br>
*
*<a href="http://en.wikipedia.org/wiki/Mannose" title=""><small>D</small>-Mannose</a></td>
*<td><a href="http://en.wikipedia.org/wiki/File:DGulose_Fischer.svg" class="image" title=""><img alt="D-Gulose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/8/80/DGulose_Fischer.svg/83px-DGulose_Fischer.svg.png" border="0" height="140" width="83"></a><br>
*<a href="http://en.wikipedia.org/wiki/Gulose" title=""><small>D</small>-Gulose</a></td>
*<td><a href="http://en.wikipedia.org/wiki/File:DIdose_Fischer.svg" class="image" title=""><img alt="D-Idose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/4/45/DIdose_Fischer.svg/83px-DIdose_Fischer.svg.png" border="0" height="140" width="83"></a><br>
*<a href="http://en.wikipedia.org/wiki/Idose" title=""><small>D</small>-Idose</a></td>
*<td><a href="http://en.wikipedia.org/wiki/File:DGalactose_Fischer.svg" class="image" title=""><img alt="D-Galactose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/DGalactose_Fischer.svg/83px-DGalactose_Fischer.svg.png" border="0" height="140" width="83"></a><br>
*<a href="http://en.wikipedia.org/wiki/Galactose" title=""><small>D</small>-Galactose</a></td>
*<td><a href="http://en.wikipedia.org/wiki/File:DTalose_Fischer.svg" class="image" title=""><img alt="D-Talose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/8/8b/DTalose_Fischer.svg/83px-DTalose_Fischer.svg.png" border="0" height="140" width="83"></a><br>
*<a href="http://en.wikipedia.org/wiki/Talose" title=""><small>D</small>-Talose</a></td>
*
*</tr>
*</tbody></table>
*
*<h3>Ketoses</h3>
*<table class="wikitable">
*<tbody><tr>
*<td>Ketotriose</td>
*<td colspan="4"><a href="http://en.wikipedia.org/wiki/File:Dihydroxyacetone_Fischer.svg" class="image" title=""><img alt="Dihydroxyacetone" src="http://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/Dihydroxyacetone_Fischer.svg/41px-Dihydroxyacetone_Fischer.svg.png" border="0" height="72" width="41"></a><br>
*<a href="http://en.wikipedia.org/wiki/Dihydroxyacetone" title="">Dihydroxyacetone</a></td>
*</tr>
*<tr>
*<td>ketotetrose</td>
*<td colspan="4"><a href="http://en.wikipedia.org/wiki/File:DErythrulose_Fischer.svg" class="image" title=""><img alt="D-Eerythrulose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/b/be/DErythrulose_Fischer.svg/70px-DErythrulose_Fischer.svg.png" border="0" height="94" width="70"></a><br>
*<a href="http://en.wikipedia.org/wiki/Erythrulose" title=""><small>D</small>-Erythrulose</a></td>
*
*</tr>
*<tr>
*<td>ketopentoses</td>
*<td colspan="2"><a href="http://en.wikipedia.org/wiki/File:DRibulose_Fischer.svg" class="image" title=""><img alt="D-Ribulose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/DRibulose_Fischer.svg/70px-DRibulose_Fischer.svg.png" border="0" height="115" width="70"></a><br>
*<a href="http://en.wikipedia.org/wiki/Ribulose" title="Ribulose"><small>D</small>-Ribulose</a></td>
*<td colspan="2"><a href="http://en.wikipedia.org/wiki/File:DXylulose_Fischer.svg" class="image" title=""><img alt="D-Xylulose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/DXylulose_Fischer.svg/80px-DXylulose_Fischer.svg.png" border="0" height="115" width="80"></a><br>
*<a href="http://en.wikipedia.org/wiki/Xylulose" title="Xylulose"><small>D</small>-Xylulose</a></td>
*</tr>
*<tr>
*<td>ketohexoses</td>
*<td><a href="http://en.wikipedia.org/wiki/File:DPsicose_Fischer.svg" class="image" title=""><img alt="D-Psicose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/7/7c/DPsicose_Fischer.svg/70px-DPsicose_Fischer.svg.png" border="0" height="136" width="70"></a><br>
*
*<a href="http://en.wikipedia.org/wiki/Psicose" title="Psicose"><small>D</small>-Psicose</a></td>
*<td><a href="http://en.wikipedia.org/wiki/File:DFructose_Fischer.svg" class="image" title=""><img alt="D-Fructose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/0/04/DFructose_Fischer.svg/80px-DFructose_Fischer.svg.png" border="0" height="136" width="80"></a><br>
*<a href="http://en.wikipedia.org/wiki/Fructose" title="Fructose"><small>D</small>-Fructose</a></td>
*<td><a href="http://en.wikipedia.org/wiki/File:DSorbose_Fischer.svg" class="image" title=""><img alt="D-Sorbose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/d/d6/DSorbose_Fischer.svg/80px-DSorbose_Fischer.svg.png" border="0" height="136" width="80"></a><br>
*<a href="http://en.wikipedia.org/wiki/Sorbose" title="Sorbose"><small>D</small>-Sorbose</a></td>
*<td><a href="http://en.wikipedia.org/wiki/File:DTagatose_Fischer.svg" class="image" title=""><img alt="D-Tagatose" src="http://upload.wikimedia.org/wikipedia/commons/thumb/6/6c/DTagatose_Fischer.svg/80px-DTagatose_Fischer.svg.png" border="0" height="136" width="80"></a><br>
*<a href="http://en.wikipedia.org/wiki/Tagatose" title="Tagatose"><small>D</small>-Tagatose</a></td>
*</tr>
*</tbody></table>
*</p>
*********************************************************************
*
*   @see <a href="http://en.wikipedia.org/wiki/Monosaccharide">Common monosaccharides</a> 
*   @see <a href="http://www.cem.msu.edu/~reusch/VirtualText/carbhyd.htm">An introduction to carbohydrates</a>
*   @author mjh
*/
public enum CommonBasetype implements Basetype
{
    // /** Constant indicating an unknown basetype. */
    // UnknownBasetype,
        
    //-------------------------------------------------------------------------
    // name     _______________________________________________________________
    //          | stereo configuration (dextro/laevo)
    //          |   ___________________________________________________________
    //          |   | superclass (pentose, hexose, etc)
    //          |   |       ___________________________________________________
    //          |   |       | stereo conf of OHs (fischer: 1 == right, 0 == left); 
    //          |   |       | in a haworth proj, 0 == up, 1 == down 
    //          |   |       |           _______________________________________
    //          |   |       |           | position of carbonyl & permissable ring closure positions
    //          |   |       |           |           ___________________________
    //          |   |       |           |           | fullname [, synonyms]
    //          |   |       |           |           |
    
    /*~~~~~~~~~~  trioses  ~~~~~~~~~~*/
    /** Glyceraldehyde</a>, an aldotriose. 
    *   <a href="http://en.wikipedia.org/wiki/Glyceraldehyde">
    *   <img src="http://en.wikipedia.org/wiki/File:DRibose_Fischer.svg" /></a>
    */
    Gly( 
        D, Triose, bitsetOf("010"), bitsetOf("010"), 
        funcGroups( OH, OH, Carbonyl ), synonyms( "Glyceraldehyde", "Gro" ) 
    ), 
    
    /*~~~~~~~~~~  aldotetroses  ~~~~~~~~~~*/
    /** <a href="http://en.wikipedia.org/wiki/Erythrose">Erythrose</a>, an aldotetrose */
    Ery( 
        D, Tetrose, bitsetOf("0110"), bitsetOf("0110"), 
        funcGroups( OH, OH, OH, Carbonyl ), synonyms("Erythrose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Threose">Threose</a>, an aldotetrose */
    Thr( 
        D, Tetrose, Ery.chiralPositions, bitsetOf("0100"), 
        Ery.functionalGroups, synonyms("Threose") 
    ),
    
    /*~~~~~~~~~~  ketotetrose  ~~~~~~~~~~*/
    /** <a href="http://en.wikipedia.org/wiki/Erythrulose">Erythrulose</a>, a ketotetrose */
    Eul( 
        D, Tetrose, bitsetOf("0100"), bitsetOf("0100"), 
        funcGroups( OH, OH, Carbonyl, OH ), synonyms("Erythrulose") 
    ),
    
    /*~~~~~~~~~~  aldopentoses  ~~~~~~~~~~*/
    /** <a href="http://en.wikipedia.org/wiki/Ribose">Ribose</a>, an aldopentose. */
    Rib( 
        D, Pentose, bitsetOf("01110"), bitsetOf("01110"), 
        funcGroups( OH, OH, OH, OH, Carbonyl ), synonyms("Ribose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Arabinose">Arabinose</a>, an aldopentose. */
    Ara( 
        D, Pentose, Rib.chiralPositions, bitsetOf("01100"), 
        Rib.functionalGroups, synonyms("Arabinose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Xylose">Xylose</a>, an aldopentose. */
    Xyl( 
        D, Pentose, Rib.chiralPositions, bitsetOf("01010"), 
        Rib.functionalGroups, synonyms("Xylose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Lyxose">Lyxose</a>, an aldopentose. */
    Lyx( 
        D, Pentose, Rib.chiralPositions, bitsetOf("01000"), 
        Rib.functionalGroups, synonyms("Lyxose") 
    ),
    
    /*~~~~~~~~~~  ketopentoses  ~~~~~~~~~~*/
    /** <a href="http://en.wikipedia.org/wiki/Ribulose">Ribulose</a>, a ketopentose. */
    Rul( 
        D, Pentose, bitsetOf("01100"), bitsetOf("01100"), 
        funcGroups( OH, OH, OH, Carbonyl, OH ), synonyms("Ribulose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Xylulose">Xylulose</a>, a ketopentose. */
    Xul( 
        D, Pentose, Rul.chiralPositions, bitsetOf("01000"), 
        Rul.functionalGroups, synonyms("Xylulose") 
    ),
    
    /*~~~~~~~~~~  aldohexoses  ~~~~~~~~~~*/
    /** <a href="http://en.wikipedia.org/wiki/Allose">Allose</a>, an aldohexose. */
    All( 
        D, Hexose, bitsetOf("011110"), bitsetOf("011110"), 
        funcGroups( OH, OH, OH, OH, OH, Carbonyl ), synonyms("Allose")            
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Altrose">Altrose</a>, an aldohexose. */
    Alt( 
        D, Hexose, All.chiralPositions, bitsetOf("011100"), 
        All.functionalGroups, synonyms("Altrose") ),
    
    /** <a href="http://en.wikipedia.org/wiki/Glucose">Glucose</a>, an aldohexose. */
    Glc( 
        D, Hexose, All.chiralPositions, bitsetOf("011010"), 
        All.functionalGroups, synonyms("Glucose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Mannose">Mannose</a>, an aldohexose. */
    Man( 
        D, Hexose, All.chiralPositions, bitsetOf("011000"), 
        All.functionalGroups, synonyms("Mannose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Gulose">Gulose</a>, an aldohexose. */
    Gul( 
        D, Hexose, All.chiralPositions, bitsetOf("010110"), 
        All.functionalGroups, synonyms("Gulose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Idose">Idose</a>, an aldohexose. */
    Ido( 
        D, Hexose, All.chiralPositions, bitsetOf("010100"), 
        All.functionalGroups, synonyms("Idose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Galactose">Galactose</a>, an aldohexose. */
    Gal( 
        D, Hexose, All.chiralPositions, bitsetOf("010010"), 
        All.functionalGroups, synonyms("Galactose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Talose">Talose</a>, an aldohexose. */
    Tal( 
        D, Hexose, All.chiralPositions, bitsetOf("010000"), 
        All.functionalGroups, synonyms("Talose") 
    ),
    
    /*~~~~~~~~~~  ketohexoses  ~~~~~~~~~~*/
    /** <a href="http://en.wikipedia.org/wiki/Psicose">Psicose</a>, a ketohexose. */
    Psi( 
        D, Hexose, bitsetOf("011100"), bitsetOf("011100"), 
        funcGroups( OH, OH, OH, OH, Carbonyl, OH ), synonyms("Psicose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Fructose">Fructose</a>, a ketohexose. */
    Fru( 
        D, Hexose, Psi.chiralPositions, bitsetOf("011000"), 
        Psi.functionalGroups, synonyms("Fructose", "Levulose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Sorbose">Sorbose</a>, a ketohexose. */
    Sor( 
        D, Hexose, Psi.chiralPositions, bitsetOf("010100"), 
        Psi.functionalGroups, synonyms("Sorbose") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/Tagatose">Tagatose</a>, a ketohexose. */
    Tag( 
        D, Hexose, Psi.chiralPositions, bitsetOf("010000"), 
        Psi.functionalGroups, synonyms("Tagatose") 
    ),
    
    /*~~~~~~~~~~  common NAc derivatives  ~~~~~~~~~~*/
    /** <a href="http://en.wikipedia.org/wiki/GlcNAc">N-acetylglucosamine</a> */
    GlcNAc( 
        D, Glc, Glc.chiralPositions, 
        funcGroups( OH, OH, OH, OH, NAc, Carbonyl ), synonyms("N-acetylglucosamine")           
    ),

    /** <a href="http://en.wikipedia.org/wiki/ManNAc">N-acetylmannosamine</a> */
    ManNAc( 
        D, Man, Man.chiralPositions, 
        GlcNAc.functionalGroups, synonyms("N-acetylmannosamine") 
    ),

    /** <a href="http://en.wikipedia.org/wiki/GalNAc">N-acetylgalactosamine</a> */
    GalNAc( 
        D, Gal, Gal.chiralPositions, 
        GlcNAc.functionalGroups, synonyms("N-acetylgalactosamine") 
    ),
    
    /*~~~~~~~~~~  common amino-sugars  ~~~~~~~~~~*/
    /** <a href="http://en.wikipedia.org/wiki/GlcNAc">N-acetylglucosamine</a> */
    GlcN( 
        D, Glc, Glc.chiralPositions, 
        funcGroups( OH, OH, OH, OH, NH2, Carbonyl ), synonyms("glucosamine")
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/ManNAc">N-acetylmannosamine</a> */
    ManN( 
        D, Man, Man.chiralPositions, 
        GlcN.functionalGroups, synonyms("mannosamine") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/GalNAc">N-acetylgalactosamine</a> */
    GalN( 
        D, Gal, Gal.chiralPositions, 
        GlcN.functionalGroups, synonyms("galactosamine") 
    ),
    
    /*~~~~~~~~~~  common deoxy-sugars  ~~~~~~~~~~*/
    /** <a href="http://en.wikipedia.org/wiki/Fucose">Fucose</a> */
    Fuc( 
        L, Gal, Gal.chiralPositions,
        funcGroups( Deoxy, OH, OH, OH, OH, Carbonyl ), synonyms("Fucose", "6-deoxy-L-Galactose") 
    ),

    /** <a href="http://en.wikipedia.org/wiki/Rhamnose">Rhamnose</a> */
    Rha( 
        D, Man, Man.chiralPositions, 
        funcGroups( Deoxy, OH, OH, OH, OH, Carbonyl ), synonyms("Rhamnose", "6-deoxy-D-Mannose") 
    ),
    
    /*~~~~~~~~~~  common acidic-sugars  ~~~~~~~~~~*/
    /** <a href="http://en.wikipedia.org/wiki/Glucuronic_acid">Glucuronic acid</a> */
    GlcU(
       D, Glc, Glc.chiralPositions,
        funcGroups( Carboxyl, OH, OH, OH, OH, Carbonyl ), synonyms("Glucuronic acid") 
    ),
    
    
    /*~~~~~~~~~~  sialic acids  ~~~~~~~~~~*/
    Kdn( 
        D, Nonose, bitsetOf("011111000"), bitsetOf("011001000"), 
        funcGroups( OH, OH, OH, OH, OH, OH, Deoxy, Carbonyl, Carboxyl ),
        synonyms("2-keto-3-deoxynonic acid") 
    ),

    /** <a href="http://en.wikipedia.org/wiki/Neuraminic_acid">Neuraminic acid</a> */
    Neu( 
        D, Kdn, Kdn.chiralPositions, 
        funcGroups( OH, OH, OH, OH, NH2, OH, Deoxy, Carbonyl, Carboxyl ),
        synonyms("neuraminic acid")
    ),

    /** <a href="http://en.wikipedia.org/wiki/N-Acetylneuraminic_acid">N-acetylneuraminic (Sialic) acid</a> */
    NeuAc( 
        D, Kdn, Kdn.chiralPositions,
        funcGroups( OH, OH, OH, OH, NAc, OH, Deoxy, Carbonyl, Carboxyl ),
        synonyms("N-acetylneuraminic acid", "NANA", "Neu5Ac") 
    ),
    
    /** <a href="http://en.wikipedia.org/wiki/N-Glycolylneuraminic_acid">N-Glycolyl-neuraminic acid</a> */
    NeuGc(
        D, Kdn, Kdn.chiralPositions,
        funcGroups( OH, OH, OH, OH, NGlycolyl, OH, Deoxy, Carbonyl, Carboxyl ),
        synonyms("N-glycolylneuraminic acid", "NGNA", "Neu5Gc") 
    ),
    
    /*~~~~~~~~~~  others  ~~~~~~~~~~*/
    /** <a href="http://en.wikipedia.org/wiki/Muramic_acid">Muramic acid</a> */
    Mur( 
        D, GlcNAc, Glc.chiralPositions, 
        funcGroups( OH, OH, OH, Lactate, NH2, Carbonyl ), synonyms("Muramic acid") 
        )
    ;
    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ PROPERTIES ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** logging handle */
    static Logger log = Logger.getLogger( CommonBasetype.class );
    
    static boolean verbose = false;
    
    /** Common (IUPAC) name. */
    private final String fullname;
    
    /** {@link Superclass} - number of CHOs in backbone, Hexose=6, Pentose=5, etc. */
    private final Superclass superclass;
    
    /** The {@link StereoConfig} this basetype has been defined as. */
    private final StereoConfig stereoConfig;
    
    /** Bitmask indicating stereochemistry of OHs; 1 == right side of Fischer projection. 
    *   Bit at position == 0 indicates D or L -- false == D, true == L. 
    *   Note bitset size is +1 to superclass size. */
    private final BitSet stereochemistry;
    
    /** Bitmask indicating which positions in the basetype are chiral. */
    private final BitSet chiralPositions;
    
    /** Array of functional groups for this basetype, */
    private final Substituent[] functionalGroups;
    
    // /** true if this basetype is defined in terms of another, parent basetype. */
    // private final Basetype parent;
    
    private final String[] synonyms;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Constructor for "pure" basetypes */
    CommonBasetype( 
        StereoConfig stereoConfig,
        Superclass superclass,
        BitSet chiralPositions,
        BitSet stereochemistry, 
        Substituent[] functionalGroups,
        String[] synonyms
    )
    {
        _check_equal_length( 
            this, 
            superclass.size(), 
            chiralPositions.length(), 
            functionalGroups.length,
            stereochemistry.length()
        );
        
        this.stereoConfig     = stereoConfig;
        this.superclass       = superclass;
        this.chiralPositions  = chiralPositions;
        this.stereochemistry  = stereochemistry;
        this.functionalGroups = functionalGroups;
        this.fullname         = synonyms[0];
        this.synonyms         = synonyms;
        
        // assert stereoConfig == Basetypes.determineStereoConfig( this );
    }
    
    
    /** Constructor for "derived" basetypes */
    CommonBasetype( 
        StereoConfig stereoConfig,
        Basetype archetype, 
        BitSet chiralPositions,
        Substituent[] functionalGroups,
        String[] synonyms
    )
    {
        this(
            stereoConfig,
            archetype.getSuperclass(),
            chiralPositions,
            ((stereoConfig != archetype.getStereoConfig()) 
                ? archetype.getStereochemistry().bitwiseXorEquals( 
                    archetype.getChiralPositions() ) 
                : archetype.getStereochemistry() ),
            functionalGroups,
            synonyms
        );
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~
   
    static final Map<String,CommonBasetype> definedBasetypes;
    static 
    {
        int presize = CommonBasetype.values().length * 4;
        definedBasetypes = new HashMap<String,CommonBasetype>( presize );
        
        for ( CommonBasetype b : CommonBasetype.values() )
        {
            definedBasetypes.put( b.name().toLowerCase(), b ); 
            definedBasetypes.put( b.toString().toLowerCase(), b ); 
            for ( String s : b.getSynonyms() )
                definedBasetypes.put( s.toLowerCase(), b ); 
        }
    }

    
    
    /**
    *   Returns a CommonBasetype corresponding to the given string
    *   name, or null if not found.
    *
    *   @throws IllegalArgumentException if the name is null or zero-length.
    */
    public static CommonBasetype forName( String name )
    {
        if ( name == null || name.length() == 0 )
            throw new IllegalArgumentException(
                "Basetype name can't be null or zero-length");
            
        String n = name.toLowerCase();
        
        CommonBasetype b = definedBasetypes.get( n );

        /*
        if ( b == null )
        {
            throw new IllegalArgumentException(
                "Unknown basetype '" 
                + name 
                + "'"
            );
        }
        */
        
        return b;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public final boolean isDefinite()
    {
        return this != UnknownBasetype && this.stereoConfig != null;   
    }
    
    
    public boolean isAldose()
    {
        return functionalGroups[0] == Carbonyl; 
    }
    
    
    /** Returns names of the form "D-Man". */
    public String getName() 
    {
        if ( this == UnknownBasetype )
            return /*UnknownStereoConfig + "-" +*/ name();
        
        return getStereoConfig()
            + "-"
            + name()
        ;
    }
    
    
    public String getFullName()
    {
        return fullname;    
    }
    

    public double getMass() 
    {
        return 0;
    }    
    
    
    public double getAvgMass() 
    {
        return 0;
    }
    
    
    /** 
    *   Returns the default (preferred) {@link RingConformation} this basetype
    *   predominantly adopts in aqueous solution. This method is not intended to be 
    *   100% chemically accurate (yet); return value is based on simple heuristics.
    */
    public RingConformation getDefaultRingConformation()
    {
        boolean has_carbonyl = false;
        for ( Substituent s : functionalGroups )
        {
            if ( s == Carbonyl )
            {
                if ( /* already */ has_carbonyl )
                    return UnknownRingConformation;
                
                has_carbonyl = true;
            }
        }
        
        if ( ! has_carbonyl || superclass.size() < 5 )
            return OpenChain;            
            
        if ( superclass.size() == 5 )
            return Furanose;
        
        return Pyranose;
    }
    
    
    public BitSet getChiralPositions()
    {
        return chiralPositions;   
    }

    
    public List<Substituent> getFunctionalGroups()
    {
        return Arrays.asList( functionalGroups );   
    }
    
    
    public final StereoConfig getStereoConfig()
    {
        return stereoConfig;
    }
    
    
    public BitSet getStereochemistry()
    {
        return stereochemistry;   
    }
    
    
    public Superclass getSuperclass()
    {
        return superclass;    
    }
    
    
    String[] getSynonyms()
    {
        if ( synonyms == null )
            return new String[] {};
            
        return synonyms;    
    }
    

    public String toString()
    {
        return getName();
    }
    

    //~~~~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~
    
    
    private static final Substituent[] funcGroups( Substituent... array ) 
    {  
        // return array; 
        Substituent[] reversed = new Substituent[ array.length ];
        for ( int i = 0; i < array.length; i++ )
            reversed[ array.length - i - 1 ] = array[i];
        return reversed;  
    }
    
    
    private static final BitSet bitsetOf( String bitstring ) 
    {  
        return BitSet.forString( bitstring );  
    }
    

    private static final String[] synonyms( String... synonym_list ) 
    {
        return synonym_list;
    }
    
    
    private static final void _check_equal_length( CommonBasetype b, int su, int ch, int fg, int st )
    {
        assert su == ch : b + " - expected to be equal: superclass=" + su + ", chiralpos=" + ch + ", func_groups=" + fg + ", stereochem=" + st;
        assert su == fg : b + " - expected to be equal: superclass=" + su + ", chiralpos=" + ch + ", func_groups=" + fg + ", stereochem=" + st;
        assert su == st : b + " - expected to be equal: superclass=" + su + ", chiralpos=" + ch + ", func_groups=" + fg + ", stereochem=" + st;
    }
    
    
    
}

