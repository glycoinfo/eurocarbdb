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
package org.eurocarbdb.resourcesdb.monosaccharide;

public enum Ringtype {
    PYRANOSE(6, "pyranose", "p", "p"),
    FURANOSE(5, "furanose", "f", "f"),
    OPEN(0, "open chain", "", "a"),
    UNKNOWN(-1, "unknown", "", "?");

    private int size;
    private String name;
    private String carbbankSymbol;
    private String bcsdbSymbol;
    
    private Ringtype(int rtSize, String rtName, String rtCarbbankSymbol, String rtBcsdbSymbol) {
        this.setSize(rtSize);
        this.setName(rtName);
        this.setCarbbankSymbol(rtCarbbankSymbol);
        this.setBcsdbSymbol(rtBcsdbSymbol);
    }
    
    public String getCarbbankSymbol() {
        return carbbankSymbol;
    }
    
    private void setCarbbankSymbol(String carbbankSymbol) {
        this.carbbankSymbol = carbbankSymbol;
    }
    
    public String getBcsdbSymbol() {
        return bcsdbSymbol;
    }

    public void setBcsdbSymbol(String bcsdbSymbol) {
        this.bcsdbSymbol = bcsdbSymbol;
    }

    public String getName() {
        return name;
    }
    
    private void setName(String name) {
        this.name = name;
    }
    
    public int getSize() {
        return size;
    }
    
    private void setSize(int size) {
        this.size = size;
    }
    
    public static Ringtype forSize(int size) {
        for(Ringtype r : Ringtype.values()) {
            if(r.getSize() == size) {
                return r;
            }
        }
        return null;
    }
    
    public static Ringtype forCarbbankSymbol(String sym) {
        for(Ringtype r : Ringtype.values()) {
            if(r.getCarbbankSymbol().equals(sym)) {
                return r;
            }
        }
        return null;
    }
    
    public static Ringtype forBcsdbSymbol(String sym) {
        for(Ringtype r : Ringtype.values()) {
            if(r.getBcsdbSymbol().equals(sym)) {
                return r;
            }
        }
        return null;
    }
    
}
