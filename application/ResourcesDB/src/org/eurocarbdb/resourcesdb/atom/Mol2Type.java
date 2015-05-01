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
package org.eurocarbdb.resourcesdb.atom;

/**
* Methods to assign the correct mol2 atom type to an atom.
* The assignment follows the rules given at http://www.scl.kyoto-u.ac.jp/scl/appli/appli_manual/CSD-5.21/pluto/atom_types.html#C
*
* @author Thomas Lütteke
*/
public class Mol2Type {

    public static final String C_1 = "C.1";
    public static final String C_2 = "C.2";
    public static final String C_3 = "C.3";
    public static final String C_AR = "C.ar";
    public static final String C_CAT = "C.cat";
    public static final String O_CO2 = "O.co2";
    public static final String O_2 = "O.2";
    public static final String O_3 = "O.3";
    public static final String N_1 = "N.1";
    public static final String N_2 = "N.2";
    public static final String N_3 = "N.3";
    public static final String N_PL3 = "N.pl3";
    public static final String N_4 = "N.4";
    public static final String N_AR = "N.ar";
    public static final String N_AM = "N.am";
    public static final String S_O = "S.o";
    public static final String S_O2 = "S.o2";
    public static final String S_2 = "S.2";
    public static final String S_3 = "S.3";
    public static final String H = "H";
    public static final String P_3 = "P.3";
    public static final String CO_OH = "Co.oh";
    public static final String RU_OH = "Ru.oh";
    public static final String TI_TH = "Ti.th";
    public static final String TI_OH = "Ti.oh";
    public static final String CR_TH = "Cr.th";
    public static final String CR_OH = "Cr.oh";
    public static final String DUMMY = "Du";
    
    private static String[] nonMetalAtoms = {"H", "C", "O", "N", "F", "Si", "P", "S", "Cl", "As", "Se", "Br", "Te", "I", "At", "He", "Ne", "Ar", "Kr", "Xe", "Rn", "D", "B"};
    
    public static boolean isNonMetalAtom(Atom a) {
        for(String nonMetalSymbol : Mol2Type.nonMetalAtoms) {
            if(nonMetalSymbol.equals(a.getElementSymbol())) {
                return true;
            }
        }
        return false;
    }
    
    public static int countNonmetalBonds(Atom a) {
        int bondcount = 0;
        if(a.getConnections() != null) {
            for(AtomConnection acon : a.getConnections()) {
                Atom toAtom = acon.getToAtom();
                if(toAtom.equals(a)) {
                    toAtom = acon.getFromAtom();
                }
                if(isNonMetalAtom(toAtom)) {
                    bondcount ++;
                }
            }
        }
        return bondcount;
    }
    
    public static int countNonmetalSingleBonds(Atom a) {
        int bondcount = 0;
        if(a.getConnections() != null) {
            for(AtomConnection acon : a.getConnections()) {
                if(acon.getBondOrder() != 1.0) {
                    continue;
                }
                Atom toAtom = acon.getToAtom();
                if(toAtom.equals(a)) {
                    toAtom = acon.getFromAtom();
                }
                if(isNonMetalAtom(toAtom)) {
                    bondcount ++;
                }
            }
        }
        return bondcount;
    }
    
    public static int countNonmetalDoubleBonds(Atom a) {
        int bondcount = 0;
        if(a.getConnections() != null) {
            for(AtomConnection acon : a.getConnections()) {
                if(acon.getBondOrder() != 2.0) {
                    continue;
                }
                Atom toAtom = acon.getToAtom();
                if(toAtom.equals(a)) {
                    toAtom = acon.getFromAtom();
                }
                if(isNonMetalAtom(toAtom)) {
                    bondcount ++;
                }
            }
        }
        return bondcount;
    }
    
    public static int countNonmetalTripleBonds(Atom a) {
        int bondcount = 0;
        if(a.getConnections() != null) {
            for(AtomConnection acon : a.getConnections()) {
                if(acon.getBondOrder() != 3.0) {
                    continue;
                }
                Atom toAtom = acon.getToAtom();
                if(toAtom.equals(a)) {
                    toAtom = acon.getFromAtom();
                }
                if(isNonMetalAtom(toAtom)) {
                    bondcount ++;
                }
            }
        }
        return bondcount;
    }
    
    public static Atom getConnectedNonmetalAtom(Atom a, int index) {
        int count = 0;
        if(a.getConnections() != null) {
            for(AtomConnection acon : a.getConnections()) {
                Atom toAtom = acon.getToAtom();
                if(toAtom.equals(a)) {
                    toAtom = acon.getFromAtom();
                }
                if(isNonMetalAtom(toAtom)) {
                    count ++;
                    if(count == index) {
                        return toAtom;
                    }
                }
            }
        }
        return null;
    }
    
    public static String getMol2Type(Atom a) {
        String elementSymbol = a.getElementSymbol();
        if(elementSymbol != null) {
            if(elementSymbol.equals("P")) {
                return Mol2Type.P_3;
            }
            if(elementSymbol.equals("Co")) {
                return Mol2Type.CO_OH;
            }
            if(elementSymbol.equals("Ru")) {
                return Mol2Type.RU_OH;
            }
            if(elementSymbol.equals("C")) {
                if(a.countBonds() >= 4 && a.countSingleBonds() == a.countBonds()) {
                    return Mol2Type.C_3;
                }
                //TODO: implement checks for Mol2Type.C_CAT
                if(a.countBonds() >= 2 && a.countAromaticBonds() == 2) {
                    return Mol2Type.C_AR;
                }
                if(a.countBonds() <=2 && a.countTripleBonds() == 1) {
                    return Mol2Type.C_1;
                }
                return Mol2Type.C_2;
            }
            if(elementSymbol.equals("O")) {
                if(countNonmetalBonds(a) == 1) {
                    Atom toAtom = getConnectedNonmetalAtom(a, 1);
                    if(toAtom.getElementSymbol().equals("C")) {
                        if(toAtom.countBonds() == 3) {
                            Atom o = toAtom.getConnectedAtom("O", 1);
                            if(o != null && countNonmetalBonds(o) == 1) {
                                o = toAtom.getConnectedAtom("O", 2);
                                if(o != null && countNonmetalBonds(o) == 1) {
                                    return Mol2Type.O_CO2;
                                }
                            }
                        }
                    }
                    //TODO: implement phosphorus checks for Mol2Type.O_CO2
                }
                if(a.countBonds() >= 2 && a.countBonds() == a.countSingleBonds()) {
                    return Mol2Type.O_3;
                }
                return Mol2Type.O_2;
            }
            if(elementSymbol.equals("N")) {
                if(countNonmetalBonds(a) == 4 && countNonmetalSingleBonds(a) == 4) {
                    return Mol2Type.N_4;
                }
                if(a.countBonds() >= 2 && a.countAromaticBonds() == 2) {
                    return Mol2Type.N_AR;
                }
                if(countNonmetalBonds(a) == 1 && countNonmetalTripleBonds(a) == 1) {
                    return Mol2Type.N_1;
                }
                if(countNonmetalBonds(a) == 2 && (countNonmetalDoubleBonds(a) == 2 || (countNonmetalSingleBonds(a) ==1 && countNonmetalTripleBonds(a) == 1))) {
                    return Mol2Type.N_1;
                }
                if(countNonmetalBonds(a) == 3) {
                    for(int i = 1; i <= 3; i++) {
                        Atom c = a.getConnectedAtom("C", i);
                        if(c != null) {
                            for(AtomConnection acon : c.getConnections()) {
                                if(acon.getBondOrder() == 2) {
                                    if(acon.getToAtom().getElementSymbol().equals("O")) {
                                        return Mol2Type.N_AM;
                                    }
                                    if(acon.getToAtom().getElementSymbol().equals("S")) {
                                        return Mol2Type.N_AM;
                                    }
                                }
                            }
                        }
                    }
                    if(countNonmetalSingleBonds(a) == 2) {
                        return Mol2Type.N_PL3;
                    }
                    return Mol2Type.N_3;
                }
                return Mol2Type.N_2;
            }
            if(elementSymbol.equals("S")) {
                if(countNonmetalBonds(a) == 3) {
                    int oCount = 0;
                    for(int i = 1; i <= 3; i++) {
                        Atom o = a.getConnectedAtom("O", i);
                        if(o != null) {
                            if(countNonmetalBonds(o) == 1) {
                                oCount ++;
                            }
                        }
                    }
                    if(oCount == 1) {
                        return Mol2Type.S_O;
                    }
                }
                if(countNonmetalBonds(a) == 4) {
                    int oCount = 0;
                    for(int i = 1; i <= 4; i++) {
                        Atom o = a.getConnectedAtom("O", i);
                        if(o != null) {
                            if(countNonmetalBonds(o) == 1) {
                                oCount ++;
                            }
                        }
                    }
                    if(oCount == 2) {
                        return Mol2Type.S_O2;
                    }
                }
                if(a.countBonds() >= 2 && (a.countBonds() == a.countSingleBonds())) {
                    return Mol2Type.S_3;
                }
                return Mol2Type.S_2;
            }
            if(elementSymbol.equals("Ti")) {
                if(a.countBonds() <= 4) {
                    return Mol2Type.TI_TH;
                }
                return Mol2Type.TI_OH;
            }
            if(elementSymbol.equals("Cr")) {
                if(a.countBonds() <= 4) {
                    return Mol2Type.CR_TH;
                }
                return Mol2Type.CR_OH;
            }
            return elementSymbol;
        }
        return Mol2Type.DUMMY;
    }
}
