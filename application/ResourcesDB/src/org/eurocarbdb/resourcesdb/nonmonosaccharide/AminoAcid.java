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
package org.eurocarbdb.resourcesdb.nonmonosaccharide;

import org.eurocarbdb.resourcesdb.MolecularEntity;

public class AminoAcid extends MolecularEntity {
    
    private String abbreviation3 = null;
    private String abbreviation1 = null;
    private Double monoIncr = null;
    private Double avgIncr = null;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public AminoAcid() {
        this.init();
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************

    public String getAbbreviation1() {
        return abbreviation1;
    }
    
    public void setAbbreviation1(String abbreviation1) {
        this.abbreviation1 = abbreviation1;
    }
    
    public String getAbbreviation3() {
        return abbreviation3;
    }
    
    public void setAbbreviation3(String abbreviation3) {
        this.abbreviation3 = abbreviation3;
    }
    
    public Double getAvgIncr() {
        return avgIncr;
    }
    
    public void setAvgIncr(Double avgIncr) {
        this.avgIncr = avgIncr;
    }
    
    public Double getMonoIncr() {
        return monoIncr;
    }
    
    public void setMonoIncr(Double monoIncr) {
        this.monoIncr = monoIncr;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        super.init();
        this.setAbbreviation1(null);
        this.setAbbreviation3(null);
        this.setMonoIncr(null);
        this.setAvgIncr(null);
    }
}
