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
*   Last commit: $Rev: 1208 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/

package org.eurocarbdb.sugar;

/*  enum Element  *//************************************************
*
*   Enumeration of chemical elements of the periodic table. 
*
*<p>
*   Data obtained from http://www.science.co.il/PTelements.asp 
*   and parsed into usefulness with the following Perl script:
*</p>
*<pre>

#!/usr/bin/perl -n

@a = split /\t/; 

$var   = uc( "$a[3](" );
$fname = qq!"$a[3]"!;
$name  = qq!"$a[4]"!;
$mass  = $a[2];
$amass = $a[2];
                                     
format STDOUT =
    @<<<<<<<<<<<<<< @<<<<<<<<<<<<<<, @<<<<, @###.#####, @###.#####  ),
    $var            $fname           $name  $mass       $amass       
.  

write();

*</pre>
*
*     @author mjh
*/
public enum Element implements Molecule
{
    //~~~ ENUM VALUES ~~~//
    
    NULL(           "NULL"         , ""   ,    0.0,        0.0      ),
    HYDROGEN(       "Hydrogen"     , "H"  ,    1.00790,    1.00790  ),
    HELIUM(         "Helium"       , "He" ,    4.00260,    4.00260  ),
    LITHIUM(        "Lithium"      , "Li" ,    6.94100,    6.94100  ),
    BERYLLIUM(      "Beryllium"    , "Be" ,    9.01220,    9.01220  ),
    BORON(          "Boron"        , "B"  ,   10.81100,   10.81100  ),
    CARBON(         "Carbon"       , "C"  ,   12.01070,   12.01070  ),
    NITROGEN(       "Nitrogen"     , "N"  ,   14.00670,   14.00670  ),
    OXYGEN(         "Oxygen"       , "O"  ,   15.99940,   15.99940  ),
    FLUORINE(       "Fluorine"     , "F"  ,   18.99840,   18.99840  ),
    NEON(           "Neon"         , "Ne" ,   20.17970,   20.17970  ),
    SODIUM(         "Sodium"       , "Na" ,   22.98970,   22.98970  ),
    MAGNESIUM(      "Magnesium"    , "Mg" ,   24.30500,   24.30500  ),
    ALUMINUM(       "Aluminum"     , "Al" ,   26.98150,   26.98150  ),
    SILICON(        "Silicon"      , "Si" ,   28.08550,   28.08550  ),
    PHOSPHORUS(     "Phosphorus"   , "P"  ,   30.97380,   30.97380  ),
    SULFUR(         "Sulfur"       , "S"  ,   32.06500,   32.06500  ),
    CHLORINE(       "Chlorine"     , "Cl" ,   35.45300,   35.45300  ),
    ARGON(          "Argon"        , "Ar" ,   39.94800,   39.94800  ),
    POTASSIUM(      "Potassium"    , "K"  ,   39.09830,   39.09830  ),
    CALCIUM(        "Calcium"      , "Ca" ,   40.07800,   40.07800  ),
    SCANDIUM(       "Scandium"     , "Sc" ,   44.95590,   44.95590  ),
    TITANIUM(       "Titanium"     , "Ti" ,   47.86700,   47.86700  ),
    VANADIUM(       "Vanadium"     , "V"  ,   50.94150,   50.94150  ),
    CHROMIUM(       "Chromium"     , "Cr" ,   51.99610,   51.99610  ),
    MANGANESE(      "Manganese"    , "Mn" ,   54.93800,   54.93800  ),
    IRON(           "Iron"         , "Fe" ,   55.84500,   55.84500  ),
    COBALT(         "Cobalt"       , "Co" ,   58.93320,   58.93320  ),
    NICKEL(         "Nickel"       , "Ni" ,   58.69340,   58.69340  ),
    COPPER(         "Copper"       , "Cu" ,   63.54600,   63.54600  ),
    ZINC(           "Zinc"         , "Zn" ,   65.39000,   65.39000  ),
    GALLIUM(        "Gallium"      , "Ga" ,   69.72300,   69.72300  ),
    GERMANIUM(      "Germanium"    , "Ge" ,   72.64000,   72.64000  ),
    ARSENIC(        "Arsenic"      , "As" ,   74.92160,   74.92160  ),
    SELENIUM(       "Selenium"     , "Se" ,   78.96000,   78.96000  ),
    BROMINE(        "Bromine"      , "Br" ,   79.90400,   79.90400  ),
    KRYPTON(        "Krypton"      , "Kr" ,   83.80000,   83.80000  ),
    RUBIDIUM(       "Rubidium"     , "Rb" ,   85.46780,   85.46780  ),
    STRONTIUM(      "Strontium"    , "Sr" ,   87.62000,   87.62000  ),
    YTTRIUM(        "Yttrium"      , "Y"  ,   88.90590,   88.90590  ),
    ZIRCONIUM(      "Zirconium"    , "Zr" ,   91.22400,   91.22400  ),
    NIOBIUM(        "Niobium"      , "Nb" ,   92.90640,   92.90640  ),
    MOLYBDENUM(     "Molybdenum"   , "Mo" ,   95.94000,   95.94000  ),
    TECHNETIUM(     "Technetium"   , "Tc" ,   98.00000,   98.00000  ),
    RUTHENIUM(      "Ruthenium"    , "Ru" ,  101.07000,  101.07000  ),
    RHODIUM(        "Rhodium"      , "Rh" ,  102.90550,  102.90550  ),
    PALLADIUM(      "Palladium"    , "Pd" ,  106.42000,  106.42000  ),
    SILVER(         "Silver"       , "Ag" ,  107.86820,  107.86820  ),
    CADMIUM(        "Cadmium"      , "Cd" ,  112.41100,  112.41100  ),
    INDIUM(         "Indium"       , "In" ,  114.81800,  114.81800  ),
    TIN(            "Tin"          , "Sn" ,  118.71000,  118.71000  ),
    ANTIMONY(       "Antimony"     , "Sb" ,  121.76000,  121.76000  ),
    TELLURIUM(      "Tellurium"    , "Te" ,  127.60000,  127.60000  ),
    IODINE(         "Iodine"       , "I"  ,  126.90450,  126.90450  ),
    XENON(          "Xenon"        , "Xe" ,  131.29300,  131.29300  ),
    CESIUM(         "Cesium"       , "Cs" ,  132.90550,  132.90550  ),
    BARIUM(         "Barium"       , "Ba" ,  137.32700,  137.32700  ),
    LANTHANUM(      "Lanthanum"    , "La" ,  138.90550,  138.90550  ),
    CERIUM(         "Cerium"       , "Ce" ,  140.11600,  140.11600  ),
    PRASEODYMIUM(   "Praseodymium" , "Pr" ,  140.90770,  140.90770  ),
    NEODYMIUM(      "Neodymium"    , "Nd" ,  144.24000,  144.24000  ),
    PROMETHIUM(     "Promethium"   , "Pm" ,  145.00000,  145.00000  ),
    SAMARIUM(       "Samarium"     , "Sm" ,  150.36000,  150.36000  ),
    EUROPIUM(       "Europium"     , "Eu" ,  151.96400,  151.96400  ),
    GADOLINIUM(     "Gadolinium"   , "Gd" ,  157.25000,  157.25000  ),
    TERBIUM(        "Terbium"      , "Tb" ,  158.92530,  158.92530  ),
    DYSPROSIUM(     "Dysprosium"   , "Dy" ,  162.50000,  162.50000  ),
    HOLMIUM(        "Holmium"      , "Ho" ,  164.93030,  164.93030  ),
    ERBIUM(         "Erbium"       , "Er" ,  167.25900,  167.25900  ),
    THULIUM(        "Thulium"      , "Tm" ,  168.93420,  168.93420  ),
    YTTERBIUM(      "Ytterbium"    , "Yb" ,  173.04000,  173.04000  ),
    LUTETIUM(       "Lutetium"     , "Lu" ,  174.96700,  174.96700  ),
    HAFNIUM(        "Hafnium"      , "Hf" ,  178.49000,  178.49000  ),
    TANTALUM(       "Tantalum"     , "Ta" ,  180.94790,  180.94790  ),
    TUNGSTEN(       "Tungsten"     , "W"  ,  183.84000,  183.84000  ),
    RHENIUM(        "Rhenium"      , "Re" ,  186.20700,  186.20700  ),
    OSMIUM(         "Osmium"       , "Os" ,  190.23000,  190.23000  ),
    IRIDIUM(        "Iridium"      , "Ir" ,  192.21700,  192.21700  ),
    PLATINUM(       "Platinum"     , "Pt" ,  195.07800,  195.07800  ),
    GOLD(           "Gold"         , "Au" ,  196.96650,  196.96650  ),
    MERCURY(        "Mercury"      , "Hg" ,  200.59000,  200.59000  ),
    THALLIUM(       "Thallium"     , "Tl" ,  204.38330,  204.38330  ),
    LEAD(           "Lead"         , "Pb" ,  207.20000,  207.20000  ),
    BISMUTH(        "Bismuth"      , "Bi" ,  208.98040,  208.98040  ),
    POLONIUM(       "Polonium"     , "Po" ,  209.00000,  209.00000  ),
    ASTATINE(       "Astatine"     , "At" ,  210.00000,  210.00000  ),
    RADON(          "Radon"        , "Rn" ,  222.00000,  222.00000  ),
    FRANCIUM(       "Francium"     , "Fr" ,  223.00000,  223.00000  ),
    RADIUM(         "Radium"       , "Ra" ,  226.00000,  226.00000  ),
    ACTINIUM(       "Actinium"     , "Ac" ,  227.00000,  227.00000  ),
    THORIUM(        "Thorium"      , "Th" ,  232.03810,  232.03810  ),
    PROTACTINIUM(   "Protactinium" , "Pa" ,  231.03590,  231.03590  ),
    URANIUM(        "Uranium"      , "U"  ,  238.02890,  238.02890  ),
    NEPTUNIUM(      "Neptunium"    , "Np" ,  237.00000,  237.00000  ),
    PLUTONIUM(      "Plutonium"    , "Pu" ,  244.00000,  244.00000  ),
    AMERICIUM(      "Americium"    , "Am" ,  243.00000,  243.00000  ),
    CURIUM(         "Curium"       , "Cm" ,  247.00000,  247.00000  ),
    BERKELIUM(      "Berkelium"    , "Bk" ,  247.00000,  247.00000  ),
    CALIFORNIUM(    "Californium"  , "Cf" ,  251.00000,  251.00000  ),
    EINSTEINIUM(    "Einsteinium"  , "Es" ,  252.00000,  252.00000  ),
    FERMIUM(        "Fermium"      , "Fm" ,  257.00000,  257.00000  ),
    MENDELEVIUM(    "Mendelevium"  , "Md" ,  258.00000,  258.00000  ),
    NOBELIUM(       "Nobelium"     , "No" ,  259.00000,  259.00000  ),
    LAWRENCIUM(     "Lawrencium"   , "Lr" ,  262.00000,  262.00000  ),
    RUTHERFORDIUM(  "Rutherfordium", "Rf" ,  261.00000,  261.00000  ),
    DUBNIUM(        "Dubnium"      , "Db" ,  262.00000,  262.00000  ),
    SEABORGIUM(     "Seaborgium"   , "Sg" ,  266.00000,  266.00000  ),
    BOHRIUM(        "Bohrium"      , "Bh" ,  264.00000,  264.00000  ),
    HASSIUM(        "Hassium"      , "Hs" ,  277.00000,  277.00000  ),
    MEITNERIUM(     "Meitnerium"   , "Mt" ,  268.00000,  268.00000  ),
    ;
    

    //~~~ FIELDS ~~~//

    /** Fullname */
    final String fullname;
    
    /** Common, or abbreviated name/symbol */
    final String name;
    
    /** Monoisotopic mass */
    final double mass;
    
    /** Average mass */
    final double avgmass; 
    
    
    //~~~ CONSTRUCTORS ~~~//

    /** Constructor */
    Element( String fullname, String name, double mass, double avgmass )
    {
        this.fullname   = fullname;
        this.name       = name;
        this.mass       = mass;
        this.avgmass    = avgmass;
    }
    
    
    //~~~ METHODS ~~~//
    
    /*     @see org.eurocarbdb.Molecule#getAvgMass()  */
    public final double getAvgMass()
    {
        return avgmass;
    }

    /*     @see org.eurocarbdb.Molecule#getComposition()  */
    public final Composition getComposition()
    {
        return null;
    }

    /*     @see org.eurocarbdb.Molecule#getElementalComposition()  */
    public final Composition getElementalComposition()
    {
        return null;
    }

    /*     @see org.eurocarbdb.Molecule#getFullName()  */
    public final String getFullName()
    {
        return fullname;
    }

    /*     @see org.eurocarbdb.Molecule#getMass()  */
    public final double getMass()
    {
        return mass;
    }

    /*     @see org.eurocarbdb.Molecule#getName()  */
    public final String getName()
    {
        return name;
    }

    /*     @see org.eurocarbdb.Molecule#getType()  */
    public final String getType()
    {
        return "element";
    }

} // end enum Element
