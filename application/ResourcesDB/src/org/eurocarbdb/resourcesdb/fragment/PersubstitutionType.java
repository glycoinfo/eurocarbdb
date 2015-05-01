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
package org.eurocarbdb.resourcesdb.fragment;

public enum PersubstitutionType {
    NONE("none", "NONE", 0.0, 0.0),
    PM("permethylation", "PM", 0.0, 0.0),
    PAc("peracetylation", "PAc", 0.0, 0.0),
    PDM("perdeuteromethylation", "PDM", 0.0, 0.0),
    PDAc("perdeuteroacetylation", "PDAc", 0.0, 0.0);
    
    
    private String name = null;
    private String symbol = null;
    private double monoMassIncr = 0.0;
    private double avgMassIncr = 0.0;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    private PersubstitutionType(String name, String symbol, double monoIncr, double avgIncr) {
        this.setName(name);
        this.setSymbol(symbol);
        this.setMonoMassIncr(monoIncr);
        this.setAvgMassIncr(avgIncr);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public String getName() {
        return name;
    }
    
    private void setName(String name) {
        this.name = name;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    private void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getAvgMassIncr() {
        return avgMassIncr;
    }

    private void setAvgMassIncr(double avgMassIncr) {
        this.avgMassIncr = avgMassIncr;
    }

    public double getMonoMassIncr() {
        return monoMassIncr;
    }

    private void setMonoMassIncr(double monoMassIncr) {
        this.monoMassIncr = monoMassIncr;
    }
    
}
