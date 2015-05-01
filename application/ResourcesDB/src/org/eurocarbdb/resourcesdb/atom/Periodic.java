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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//import org.eurocarbdb.resourcesdb.util.HibernateUtil;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.io.HibernateAccess;
import org.eurocarbdb.resourcesdb.util.NumberUtils;
import org.eurocarbdb.resourcesdb.util.Utils;
//import org.hibernate.Session;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
* Enum to store / handle chemical element data
* @author Thomas Luetteke
*
*/
public enum Periodic {

    //*** num, sym, name, mass, stable, dens, boiling, melting, spec.Heat, ionisPot, maxBonds, cov.Rad, {vdwRad,} {isotopes} ***
    Xx(0, "Xx", "Unknown", 0.0, true, 0.0, 0.0, 0.0, 0.0, 0.0, -1, 0.0, 0.0),
    H(1, "H", "Hydrogen", 1.00794, true, 0.0708, -252.87, -259.34, 14.304, 13.598, 1, 0.35, 1.2),
    HE(2, "He", "Helium", 4.002602, true, 0.124901, -268.93, -272.2, 5.193, 24.587, 0, 0.28),
    LI(3, "Li", "Lithium", 6.941, true, 0.534, 1342.0, 180.5, 3.582, 5.392, 1, 1.28),
    BE(4, "Be", "Beryllium", 9.012182, true, 1.85, 2471.0, 1287.0, 1.825, 9.323, 2, 0.96),
    B(5, "B", "Boron", 10.811, true, 2.37, 4000.0, 2075.0, 1.026, 8.298, 3, 0.84),
    C(6, "C", "Carbon", 12.0107, true, 2.267, 3842.0, 4492.0, 0.709, 11.26, 4, 0.76, 1.7),
    N(7, "N", "Nitrogen", 14.0067, true, 0.807, -195.79, -210.0, 1.04, 14.534, 4, 0.71, 1.55),
    O(8, "O", "Oxygen", 15.9994, true, 1.141, -182.95, -218.79, 0.918, 13.618, 2, 0.66, 1.52),
    F(9, "F", "Fluorine", 18.9984032, true, 1.5, -188.12, -219.62, 0.824, 17.423, 1, 0.57),
    NE(10, "Ne", "Neon", 20.1797, true, 1.204, -246.08, -248.59, 1.03, 21.565, 0, 0.58),
    NA(11, "Na", "Sodium", 22.98977, true, 0.97, 883.0, 97.8, 1.228, 5.139, 1, 1.66),
    MG(12, "Mg", "Magnesium", 24.305, true, 1.74, 1090.0, 650.0, 1.023, 7.646, 2, 1.41),
    AL(13, "Al", "Aluminium", 26.981538, true, 2.7, 2519.0, 660.32, 0.897, 5.986, 6, 1.21),
    SI(14, "Si", "Silicon", 28.0855, true, 2.3296, 3265.0, 1414.0, 0.705, 8.152, 6, 1.11),
    P(15, "P", "Phosphorus", 30.973761, true, 1.82, 280.5, 44.15, 0.769, 10.487, 5, 1.07),
    S(16, "S", "Sulfur", 32.065, true, 2.067, 444.6, 115.21, 0.71, 10.36, 6, 1.05),
    CL(17, "Cl", "Chlorine", 35.453, true, 1.56, -34.04, -101.5, 0.479, 12.968, 1, 1.02),
    AR(18, "Ar", "Argon", 39.948, true, 1.396, -185.85, -189.35, 0.52, 15.76, 0, 1.06),
    K(19, "K", "Potassium", 39.0983, true, 0.89, 759.0, 63.38, 0.757, 4.341, 1, 2.03),
    CA(20, "Ca", "Calcium", 40.078, true, 1.54, 1484.0, 842.0, 0.647, 6.113, 2, 1.76),
    SC(21, "Sc", "Scandium", 44.95591, true, 2.99, 2836.0, 1541.0, 0.568, 6.561, 6, 1.70),
    TI(22, "Ti", "Titanium", 47.867, true, 4.5, 3287.0, 1668.0, 0.523, 6.828, 6, 1.60),
    V(23, "V", "Vanadium", 50.9415, true, 6.0, 3407.0, 1910.0, 0.489, 6.746, 6, 1.53),
    CR(24, "Cr", "Chromium", 51.9961, true, 7.15, 2671.0, 1907.0, 0.449, 6.767, 6, 1.39),
    MN(25, "Mn", "Manganese", 54.938049, true, 7.3, 2061.0, 1246.0, 0.479, 7.434, 8, 1.61),
    FE(26, "Fe", "Iron", 55.845, true, 7.875, 2861.0, 1538.0, 0.449, 7.902, 6, 1.52),
    CO(27, "Co", "Cobalt", 58.9332, true, 8.86, 2927.0, 1495.0, 0.421, 7.881, 6, 1.50),
    NI(28, "Ni", "Nickel", 58.6934, true, 8.912, 2913.0, 1455.0, 0.444, 7.64, 6, 1.24),
    CU(29, "Cu", "Copper", 63.546, true, 8.933, 2562.0, 1084.62, 0.385, 7.726, 6, 1.32),
    ZN(30, "Zn", "Zinc", 58.6934, true, 7.134, 907.0, 419.53, 0.388, 9.394, 6, 1.22),
    GA(31, "Ga", "Gallium", 69.7236, true, 5.91, 2204.0, 29.76, 0.371, 5.999, 3, 1.22),
    GE(32, "Ge", "Germanium", 72.64, true, 5.323, 2833.0, 938.25, 0.32, 7.9, 4, 1.20),
    AS(33, "As", "Arsenic", 74.9216, true, 5.776, 614.0, 817.0, 0.329, 9.815, 3, 1.19),
    SE(34, "Se", "Selenium", 78.96, true, 4.809, 685.0, 221.0, 0.321, 9.752, 2, 1.20),
    BR(35, "Br", "Bromine", 79.904, true, 3.11, 58.8, -7.2, 0.226, 11.814, 1, 1.20),
    KR(36, "Kr", "Krypton", 83.798, true, 2.418, -153.22, -157.36, 0.248, 14.0, 0, 1.16),
    RB(37, "Rb", "Rubidium", 85.4678, true, 1.53, 688.0, 39.31, 0.363, 4.177, 1, 2.20),
    SR(38, "Sr", "Strontium", 87.62, true, 2.64, 1382.0, 777.0, 0.301, 5.695, 2, 1.95),
    Y(39, "Y", "Yttrium", 88.90585, true, 4.47, 3345.0, 1522.0, 0.298, 6.217, 6, 1.90),
    ZR(40, "Zr", "Zirconium", 91.224, true, 6.52, 4409.0, 1855.0, 0.278, 6.634, 6, 1.75),
    NB(41, "Nb", "Niobium", 92.90638, true, 8.57, 4744.0, 2477.0, 0.265, 6.759, 6, 1.64),
    MO(42, "Mo", "Molybdenum", 95.94, true, 10.2, 4639.0, 2623.0, 0.251, 7.092, 6, 1.54),
    TC(43, "Tc", "Technetium", 98.0, false, 11.0, 4265.0, 2157.0, null, 7.28, 6, 1.47),
    RU(44, "Ru", "Ruthenium", 101.07, true, 12.1, 4150.0, 2334.0, 0.238, 7.361, 6, 1.46),
    RH(45, "Rh", "Rhodium", 102.9055, true, 12.4, 3695.0, 1964.0, 0.243, 7.459, 6, 1.42),
    PD(46, "Pd", "Palladium", 106.42, true, 12.0, 2963.0, 1554.9, 0.244, 8.337, 6, 1.39),
    AG(47, "Ag", "Silver", 107.8682, true, 10.501, 2162.0, 961.78, 0.235, 7.576, 6, 1.45),
    CD(48, "Cd", "Cadmium", 112.411, true, 8.69, 767.0, 321.07, 0.232, 8.994, 6, 1.44),
    IN(49, "In", "Indium", 114.818, true, 7.31, 2072.0, 156.6, 0.233, 5.786, 3, 1.42),
    SN(50, "Sn", "Tin", 118.71, true, 7.287, 2602.0, 231.93, 0.228, 7.344, 4, 1.39),
    SB(51, "Sb", "Antimony", 121.76, true, 6.685, 1587.0, 630.63, 0.207, 8.64, 3, 1.39),
    TE(52, "Te", "Tellurium", 127.6, true, 6.232, 988.0, 449.51, 0.202, 9.01, 2, 1.38),
    I(53, "I", "Iodine", 126.90447, true, 4.93, 184.4, 113.7, 0.145, 10.451, 1, 1.39),
    XE(54, "Xe", "Xenon", 131.293, true, 2.953, -108.04, -111.75, 0.158, 12.13, 0, 1.40),
    CS(55, "Cs", "Cesium", 132.90545, true, 1.93, 671.0, 28.44, 0.242, 3.894, 1, 2.44),
    BA(56, "Ba", "Barium", 137.327, true, 3.62, 1897.0, 727.0, 0.204, 5.212, 2, 2.15),
    LA(57, "La", "Lathanum", 138.9055, true, 6.15, 3464.0, 918.0, 0.195, 5.577, 12, 2.07),
    CE(58, "Ce", "Cerium", 140.116, true, 8.16, 3443.0, 798.0, 0.192, 5.539, 6, 2.04),
    PR(59, "Pr", "Praseodymium", 140.907648, true, 6.77, 3520.0, 931.0, 0.193, 5.464, 6, 2.03),
    ND(60, "Nd", "Neodymium", 144.24, true, 7.01, 3074.0, 1021.0, 0.19, 5.525, 6, 2.01),
    PM(61, "Pm", "Prometium", 145.0, false, 7.26, 3000.0, 1042.0, null, 5.55, 6, 1.99),
    SM(62, "Sm", "Samarium", 150.36, true, 7.52, 1794.0, 1074.0, 0.197, 5.644, 6, 1.98),
    EU(63, "Eu", "Europium", 151.964, true, 5.24, 1596.0, 822.0, 0.182, 5.67, 6, 1.98),
    GD(64, "Gd", "Gadolinium", 157.25, true, 7.9, 3273.0, 1313.0, 0.236, 6.15, 6, 1.96),
    TB(65, "Tb", "Terbium", 158.92534, true, 8.23, 3230.0, 1356.0, 0.182, 5.864, 6, 1.94),
    DY(66, "Dy", "Dysprosium", 162.5, true, 8.55, 2567.0, 1412.0, 0.173, 5.939, 6, 1.92),
    HO(67, "Ho", "Holmium", 164.93032, true, 8.8, 2700.0, 1474.0, 0.165, 6.022, 6, 1.92),
    ER(68, "Er", "Erbium", 167.259, true, 9.07, 2868.0, 1529.0, 0.168, 6.108, 6, 1.89),
    TM(69, "Tm", "Thulium", 168.93421, true, 9.32, 1950.0, 1545.0, 0.16, 6.184, 6, 1.90),
    YB(70, "Yb", "Ytterbium", 173.04, true, 6.9, 1196.0, 819.0, 0.155, 6.254, 6, 1.87),
    LU(71, "Lu", "Lutetium", 174.967, true, 9.84, 3402.0, 1663.0, 0.154, 5.426, 6, 1.87),
    HF(72, "Hf", "Hafnium", 178.49, true, 13.3, 4603.0, 2233.0, 0.144, 6.825, 6, 1.75),
    TA(73, "Ta", "Tantalum", 180.9479, true, 16.4, 5458.0, 3017.0, 0.14, 7.89, 6, 1.70),
    W(74, "W", "Tungsten", 178.49, true, 19.3, 5555.0, 3422.0, 0.132, 7.98, 6, 1.62),
    RE(75, "Re", "Rhenium", 186.207, true, 20.8, null, 3186.0, 0.137, 7.88, 6, 1.51),
    OS(76, "Os", "Osmium", 190.23, true, 22.5, 5012.0, 3033.0, 0.13, 8.7, 6, 1.44),
    IR(77, "Ir", "Iridium", 192.217, true, 22.5, null, null, 0.131, 9.1, 6, 1.41),
    PT(78, "Pt", "Platinum", 195.078, true, 21.46, 3825.0, 1768.4, 0.133, 9.0, 6, 1.36),
    AU(79, "Au", "Gold", 196.966552, true, 19.282, 2856.0, 1064.18, 0.129, 9.226, 6, 1.36),
    HG(80, "Hg", "Mercury", 200.59, true, 13.5336, 356.73, -38.83, 0.14, 10.438, 6, 1.32),
    TL(81, "Tl", "Thallium", 204.3833, true, null, null, 1473.0, 0.129, 6.108, 3, 1.45),
    PB(82, "Pb", "Lead", 207.2, true, 11.342, 1749.0, 327.46, 0.129, 7.417, 4, 1.46),
    BI(83, "Bi", "Bismuth", 208.98038, true, 9.807, 1564.0, 271.4, 0.122, 7.289, 3, 1.48),
    PO(84, "Po", "Polonium", 209.0, false, 9.32, null, 254.0, null, 8.417, 2, 1.40),
    AT(85, "At", "Astatine", 210.0, false, null, null, 302.0, null, null, 1, 1.50),
    RN(86, "Rn", "Radon", 222.0, false, 4.4, -61.7, -71.0, 0.094, 10.749, 0, 1.50),
    FR(87, "Fr", "Francium", 223.0, false, null, null, 27.0, null, null, 1, 2.60),
    RA(88, "Ra", "Radium", 226.0, false, 5.0, null, 700.0, null, 5.279, 2, 2.21),
    AC(89, "Ac", "Actinium", 227.0, false, 10.07, 3198.0, 1051.0, null, 5.17, 6, 2.15),
    TH(90, "Th", "Thorium", 232.0381, false, 11.72, 4788.0, 1750.0, 0.113, 6.08, 6, 2.06),
    PA(91, "Pa", "Protactinium", 231.03588, false, 15.37, null, 1572.0, null, 5.89, 6, 2.00),
    U(92, "U", "Uranium", 238.02891, false, 18.95, 4131.0, 1135.0, 0.116, 6.194, 6, 1.96),
    NP(93, "Np", "Neptunium", 237.0, false, 20.25, null, 644.0, null, 6.266, 6, 1.90),
    PU(94, "Pu", "Plutonium", 237.0, false, 19.84, 3228.0, 640.0, null, 6.06, 6, 1.87),
    AM(95, "Am", "Americium", 243.0, false, 3.69, 2011.0, 1176.0, null, 5.993, -1, 1.80),
    CM(96, "Cm", "Curium", 247.0, false, 13.51, null, 1345.0, null, 6.02, -1, 1.69),
    BK(97, "Bk", "Berkelium", 247.0, false, 14.0, null, 1050.0, null, 6.23, -1, null),
    CF(98, "Cf", "Californium", 251.0, false, null, null, 900.0, null, 6.3, -1, null),
    ES(99, "Es", "Einsteinium", 252.0, false, null, null, 860.0, null, 6.42, -1, null),
    FM(100, "Fm", "Fermium", 257.0, false, null, null, 1527.0, null, 6.5, -1, null),
    MD(101, "Md", "Mendelevium", 258.0, false, null, null, 827.0, null, 6.58, -1, null),
    NO(102, "No", "Nobelium", 259.0, false, null, null, 827.0, null, 6.65, -1, null),
    LR(103, "Lr", "Lawrencium", 262.0, false, null, null, 1627.0, null, null, -1, null),
    RF(104, "Rf", "Rutherfordium", 261.0, false, null, null, null, null, null, -1, null),
    DB(105, "Db", "Dubnium", 262.0, false, null, null, null, null, null, -1, null),
    SG(106, "Sg", "Seaborgium", 266.0, false, null, null, null, null, null, -1, null),
    BH(107, "Bh", "Bohrium", 264.0, false, null, null, null, null, null, -1, null),
    HS(108, "Hs", "Hassium", 277.0, false, null, null, null, null, null, -1, null),
    MT(109, "Mt", "Meitnerium", 268.0, false, null, null, null, null, null, -1, null),
    DS(110, "Ds", "Darmstadtium", 281.0, false, null, null, null, null, null, -1, null);
                
    private String symbol;
    private int periodicNumber;
    private String name;
    private Double avgMass;
    private boolean stable;
    private Double density;
    private Double boilingPoint;
    private Double meltingPoint;
    private Double specificHeat;
    private Double ionisationPotential;
    private Set<Isotope> isotopes;
    private Double vdwRadius;
    private Double covalentRadius;
    private int maxBonds;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    private Periodic() {
        init();
    }
    
    private Periodic(int periodicNum, String symbolStr, String nameStr, Double mass, boolean isStable, Double dens, Double boiling, Double melting, Double specific, Double ionisPot, int maxBond, Double covalentRad, Double vdwRad, Isotope[] isos) {
        this.setPeriodicNumber(periodicNum);
        this.setSymbol(symbolStr);
        this.setName(nameStr);
        this.setAvgMass(mass);
        this.setStable(isStable);
        this.setDensity(dens);
        this.setBoilingPoint(boiling);
        this.setMeltingPoint(melting);
        this.setSpecificHeat(specific);
        this.setIonisationPotential(ionisPot);
        if(isos != null) {
            this.setIsotopes(new HashSet<Isotope>());
            for(int i = 0; i < isos.length; i++) {
                this.getIsotopes().add(isos[i]);
            }
        } else {
            this.setIsotopes(null);
        }
    }

    private Periodic(int periodicNum, String symbolStr, String nameStr, Double mass, boolean isStable, Double dens, Double boiling, Double melting, Double specific, Double ionisPot, int maxBond, Double covalentRad, Double vdwRad) {
        this(periodicNum, symbolStr, nameStr, mass, isStable, dens, boiling, melting, specific, ionisPot, maxBond, covalentRad, vdwRad, null);
    }

    private Periodic(int periodicNum, String symbolStr, String nameStr, Double mass, boolean isStable, Double dens, Double boiling, Double melting, Double specific, Double ionisPot, int maxBond, Double covalentRad) {
        this(periodicNum, symbolStr, nameStr, mass, isStable, dens, boiling, melting, specific, ionisPot, maxBond, covalentRad, null, null);
    }

    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public Double getAvgMass() {
        return avgMass;
    }

    public void setAvgMass(double avgMass) {
        this.avgMass = new Double(avgMass);
    }

    public void setAvgMass(Double avgMass) {
        this.avgMass = avgMass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPeriodicNumber() {
        return periodicNumber;
    }

    public void setPeriodicNumber(int periodicNumber) {
        this.periodicNumber = periodicNumber;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * @return the boilingPoint
     */
    public Double getBoilingPoint() {
        return boilingPoint;
    }

    /**
     * @param boilingPoint the boilingPoint to set
     */
    public void setBoilingPoint(double boilingPoint) {
        this.boilingPoint = new Double(boilingPoint);
    }

    /**
     * @param boilingPoint the boilingPoint to set
     */
    public void setBoilingPoint(Double boilingPoint) {
        this.boilingPoint = boilingPoint;
    }

    /**
     * @return the density
     */
    public Double getDensity() {
        return density;
    }

    /**
     * @param density the density to set
     */
    public void setDensity(double density) {
        this.density = new Double(density);
    }

    /**
     * @param density the density to set
     */
    public void setDensity(Double density) {
        this.density = density;
    }

    /**
     * @return the ionisationPotential
     */
    public Double getIonisationPotential() {
        return ionisationPotential;
    }

    /**
     * @param ionisationPotential the ionisationPotential to set
     */
    public void setIonisationPotential(double ionisationPotential) {
        this.ionisationPotential = new Double(ionisationPotential);
    }

    /**
     * @param ionisationPotential the ionisationPotential to set
     */
    public void setIonisationPotential(Double ionisationPotential) {
        this.ionisationPotential = ionisationPotential;
    }

    /**
     * @return the meltingPoint
     */
    public Double getMeltingPoint() {
        return meltingPoint;
    }

    /**
     * @param meltingPoint the meltingPoint to set
     */
    public void setMeltingPoint(double meltingPoint) {
        this.meltingPoint = new Double(meltingPoint);
    }

    /**
     * @param meltingPoint the meltingPoint to set
     */
    public void setMeltingPoint(Double meltingPoint) {
        this.meltingPoint = meltingPoint;
    }

    /**
     * @return the specificHeat
     */
    public Double getSpecificHeat() {
        return specificHeat;
    }

    /**
     * @param specificHeat the specificHeat to set
     */
    public void setSpecificHeat(double specificHeat) {
        this.specificHeat = new Double(specificHeat);
    }
    
    /**
     * @param specificHeat the specificHeat to set
     */
    public void setSpecificHeat(Double specificHeat) {
        this.specificHeat = specificHeat;
    }

    /**
     * @return the stable
     */
    public boolean isStable() {
        return stable;
    }

    /**
     * @param stable the stable to set
     */
    public void setStable(boolean stable) {
        this.stable = stable;
    }

    /**
     * Get the isotopes of this element
     * @return the isotopes
     */
    public Set<Isotope> getIsotopes() {
        if(isotopes == null) {
            try {
                Periodic.setData(Config.getGlobalConfig());
            } catch(ResourcesDbException rEx) {
                if(Config.getGlobalConfig().getPrintErrorMsgLevel() > 0) {
                    System.err.println(rEx);
                }
            }
        }
        return isotopes;
    }

    /**
     * Set the isotopes of this element
     * @param isotopes the isotopes to set
     */
    public void setIsotopes(Set<Isotope> isotopes) {
        this.isotopes = isotopes;
    }
    
    /**
     * Add an isotope to this element
     * @param iso the isotope to add
     */
    public void addIsotope(Isotope iso) {
        if(this.isotopes == null) {
            this.isotopes = new HashSet<Isotope>();
        }
        this.isotopes.add(iso);
    }
    
    /**
     * Get the most abundant isotope of this element
     * If more than one isotope have identical abundance values, which are the highest among all isotopes, the one with the lower mass is returned.
     * @return the isotope, of which the getAbundance() method yields the highest value.
     */
    public Isotope getMostAbundantIsotope() {
        Isotope retIso = null;
        for(Isotope iso : getIsotopes()) {
            if(retIso == null) {
                retIso = iso;
            } else {
                if(iso.getAbundance() > retIso.getAbundance()) {
                    retIso = iso;
                } else if(iso.getAbundance() == retIso.getAbundance()) {
                    if(iso.getMass() < retIso.getMass()) {
                        retIso = iso;
                    }
                }
            }
        }
        return(retIso);
    }
    
    /**
     * Get the number of isotopes known for this element
     * @return the number of isotopes
     */
    public int getIsotopesCount() {
        if(getIsotopes() != null) {
            return getIsotopes().size();
        }
        return 0;
    }

    /**
     * Get the covalent radius of this element
     * @return the covalentRadius
     */
    public Double getCovalentRadius() {
        return this.covalentRadius;
    }

    /**
     * Set the covalent radius of this element
     * @param covRad the covalentRadius to set
     */
    public void setCovalentRadius(Double covRad) {
        this.covalentRadius = covRad;
    }

    /**
     * Set the maximum number of bonds for this element
     * @return the maxBonds
     */
    public int getMaxBonds() {
        return this.maxBonds;
    }

    /**
     * Get the maximum number of bonds for this element
     * @param maxb the maxBonds to set
     */
    public void setMaxBonds(int maxb) {
        this.maxBonds = maxb;
    }

    /**
     * Get the van der Waals radius of this element
     * @return the van der Waals radius
     */
    public Double getVdwRadius() {
        return this.vdwRadius;
    }

    /**
     * Set the van der Waals radius of this element
     * @param vdwRad the vdwRadius to set
     */
    public void setVdwRadius(Double vdwRad) {
        this.vdwRadius = vdwRad;
    }

    //*****************************************************************************
    //*** static maps / lists: ****************************************************
    //*****************************************************************************
    
    /*private static HashMap<String, Periodic> elementsBySymbolMap;
    
    private static HashMap<String, Periodic> getElementsBySymbolMap() throws ResourcesDbException {
        if(elementsBySymbolMap == null) {
            setData(Config.getGlobalConfig());
        }
        return elementsBySymbolMap;
    }

    private static void setElementsBySymbolMap(HashMap<String, Periodic> ebsMap) {
        elementsBySymbolMap = ebsMap;
    }*/

    public static Periodic getElementBySymbol(String symbol) throws ResourcesDbException {
        for(Periodic elem : Periodic.values()) {
            if(elem.getSymbol().equals(symbol)) {
                return elem;
            }
        }
        throw new ResourcesDbException("Element symbol " + symbol + " not available.");
    }
    
    public static Periodic getElementByNumber(int number) throws ResourcesDbException {
        Periodic el = null; //getElementsByNumberMap().get(new Integer(number));
        for(Periodic elem : Periodic.values()) {
            if(elem.getPeriodicNumber() == number) {
                return elem;
            }
        }
        if(el == null) {
            throw new ResourcesDbException("Element number " + number + " not available.");
        }
        return(el);
    }
    
    public static ArrayList<Periodic> getElementsList() throws ResourcesDbException {
        ArrayList<Periodic> elementsList = new ArrayList<Periodic>();
        for(Periodic elem : Periodic.values()) {
            if(elem != Periodic.Xx) {
                elementsList.add(elem);
            }
        }
        return elementsList;
    }
    
    public static boolean dataIsSet() {
            return(true);
    }
    
    //*****************************************************************************
    //*** Methods for filling static element data maps: ***************************
    //*****************************************************************************
    
    public static void setData(Config conf) throws ResourcesDbException {
        setDataFromXmlFile(conf.getElementsXmlUrl());
    }
    
    public static void setDataIfNotSet(Config conf) throws ResourcesDbException {
        if(!Periodic.dataIsSet()) {
            Periodic.setData(conf);
        }
    }
    
    public static void setDataFromXmlFile(URL xmlUrl) throws ResourcesDbException {
        //HashMap<String, Periodic> bySymbolMap = new HashMap<String, Periodic>();
        //HashMap<Integer, Periodic> byNumberMap = new HashMap<Integer, Periodic>();
        //ArrayList<Periodic> elemsList = new ArrayList<Periodic>();
        SAXBuilder parser = new SAXBuilder();
        try {
            Document doc = parser.build(xmlUrl);
            org.jdom.Element root = doc.getRootElement();
            List<?> templateList = root.getChildren();
            Iterator<?> templatesIter = templateList.iterator();
            while(templatesIter.hasNext()) {
                org.jdom.Element xmlElement = (org.jdom.Element) templatesIter.next();
                Periodic el = getElementFromXmlTree(xmlElement);
                if(el.name != null) {
                    
                }
                //bySymbolMap.put(el.getSymbol().toUpperCase(), el);
                //byNumberMap.put(new Integer(el.getPeriodicNumber()), el);
                //elemsList.add(el);
            }
        } catch (JDOMException je) {
            throw new ResourcesDbException("JDOMException: " + je.getMessage());
        } catch (IOException ie) {
            throw new ResourcesDbException("IOException: " + ie.getMessage());
        }
        //Periodic.setElementsBySymbolMap(bySymbolMap);
        //Periodic.setElementsByNumberMap(byNumberMap);
        //Periodic.setElementsList(elemsList);
    }
    
    private static Periodic getElementFromXmlTree(org.jdom.Element xmlElement) {
        Periodic e = null;
        if(xmlElement.getName().equals("element")) {
            /*e = new Periodic();
            e.setName(xmlElement.getAttributeValue("name"));
            e.setSymbol(xmlElement.getAttributeValue("symbol"));
            e.setPeriodicNumber(Integer.parseInt(xmlElement.getAttributeValue("atomic_number")));
            e.setAvgMass(NumberUtils.parseDoubleStr(xmlElement.getAttributeValue("avg_mass"), null));
            e.setStable(Utils.parseTrueFalseString(xmlElement.getAttributeValue("elem_stable"), true));
            e.setDensity(NumberUtils.parseDoubleStr(xmlElement.getAttributeValue("density"), null));
            e.setMeltingPoint(NumberUtils.parseDoubleStr(xmlElement.getAttributeValue("melting_point"), null));
            e.setBoilingPoint(NumberUtils.parseDoubleStr(xmlElement.getAttributeValue("boiling_point"), null));
            e.setIonisationPotential(NumberUtils.parseDoubleStr(xmlElement.getAttributeValue("ionisation_potential"), null));
            e.setSpecificHeat(NumberUtils.parseDoubleStr(xmlElement.getAttributeValue("specific_heat"), null));*/
            try {
                e = Periodic.getElementByNumber(Integer.parseInt(xmlElement.getAttributeValue("atomic_number")));
            } catch (NumberFormatException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (ResourcesDbException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            List<?> childList = xmlElement.getChildren("isotope");
            if(childList != null) {
                Iterator<?> isoIter = childList.iterator();
                while(isoIter.hasNext()) {
                    org.jdom.Element xmlIso = (org.jdom.Element) isoIter.next();
                    Isotope iso = getIsotopeFromXmlTree(xmlIso, e);
                    e.addIsotope(iso);
                }
            }
        }
        return(e);
    }
    
    private static Isotope getIsotopeFromXmlTree(org.jdom.Element xmlElement, Periodic e) {
        Isotope iso = null;
        if(xmlElement.getName().equalsIgnoreCase("isotope")) {
            iso = new Isotope(e, Integer.parseInt(xmlElement.getAttributeValue("neutron_count")));
            iso.setMass(NumberUtils.parseDoubleStr(xmlElement.getAttributeValue("mass"), null));
            iso.setAbundance(NumberUtils.parseDoubleStr(xmlElement.getAttributeValue("frequency"), null));
            iso.setCommonName(xmlElement.getAttributeValue("common_name"));
            iso.setHalfLife(xmlElement.getAttributeValue("half_life"));
            iso.setSpin(xmlElement.getAttributeValue("spin"));
            iso.setStable(Utils.parseTrueFalseString(xmlElement.getAttributeValue("stable"), null));
        }
        return(iso);
    }
    
    public static void setDataFromDB() throws ResourcesDbException {
        ArrayList<Periodic> elemList = null;
        try {
            //if(Class.forName(HibernateAccess.class.getName()) != null) {
                elemList = HibernateAccess.getElementListFromDB();
            //}
        } catch (Throwable e) {
            System.err.println(e);
        }
        if(elemList == null) {
            throw new ResourcesDbException("Cannot set element list from database.");
        }
        HashMap<String, Periodic> bySymbolMap = new HashMap<String, Periodic>();
        HashMap<Integer, Periodic> byNumberMap = new HashMap<Integer, Periodic>();
        for(Periodic elem : elemList) {
            bySymbolMap.put(elem.getSymbol().toUpperCase(), elem);
            byNumberMap.put(new Integer(elem.getPeriodicNumber()), elem);
        }
        /*Periodic.setElementsList(elemList);
        Periodic.setElementsBySymbolMap(bySymbolMap);
        Periodic.setElementsByNumberMap(byNumberMap);*/
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        setSymbol("");
        setPeriodicNumber(0);
        setName("");
        setAvgMass(null);
        setStable(true);
        setMeltingPoint(null);
        setBoilingPoint(null);
        setIonisationPotential(null);
        setSpecificHeat(null);
        setDensity(null);
        setIsotopes(new HashSet<Isotope>());
    }
    
    public String toString() {
        String outStr;
        outStr = "Element " + this.getPeriodicNumber() + " | symbol: " + this.getSymbol() + " | name: " + this.getName() + " | stable: " + this.isStable() + " | avg. mass: " + this.getAvgMass();
        outStr += " | density: " + this.getDensity();
        outStr += " | melting/boiling point: " + this.getMeltingPoint() + "/" + this.getBoilingPoint();
        outStr += " | specific heat: " + this.getSpecificHeat();
        outStr += " | ion. pot.: " + this.getIonisationPotential();
        return(outStr);
    }
    
    /*public boolean equals(Object anotherElem) {
        if(anotherElem.getClass().equals(this.getClass())) {
            Periodic theOtherElem = (Periodic) anotherElem;
            if(theOtherElem.getPeriodicNumber() == this.getPeriodicNumber()) {
                return(true);
            }
        }
        return(false);
    }
    
    public int hashCode() {
        return this.periodicNumber;
    }*/
}
