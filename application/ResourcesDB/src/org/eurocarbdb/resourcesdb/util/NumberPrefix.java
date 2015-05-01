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
package org.eurocarbdb.resourcesdb.util;

import java.util.ArrayList;

public enum NumberPrefix {
    DI(2, "di"),
    BIS(2, "bis"),
    TRI(3, "tri"),
    TETRA(4, "tetra"),
    PENTA(5, "penta"),
    HEXA(6, "hexa"),
    HEPTA(7, "hepta"),
    OCTA(8, "octa"),
    NONA(9, "nona"),
    DECA(10, "deca"),
    UNDECA(11, "undeca"),
    DODECA(12, "dodeca");
    
    private String prefixStr;
    private int size;
    
    private NumberPrefix(int theSize, String prefix) {
        this.setSize(theSize);
        this.setPrefixStr(prefix);
    }

    public String getPrefixStr() {
        return this.prefixStr;
    }

    private void setPrefixStr(String prefix) {
        this.prefixStr = prefix;
    }

    public int getSize() {
        return this.size;
    }

    private void setSize(int theSize) {
        this.size = theSize;
    }
    
    public static NumberPrefix forName(String nameStr) {
        for(NumberPrefix prefix : NumberPrefix.values()) {
            if(prefix.name().equalsIgnoreCase(nameStr)) {
                return prefix;
            }
            if(prefix.getPrefixStr().equalsIgnoreCase(nameStr)) {
                return prefix;
            }
        }
        return null;
    }
    
    public static NumberPrefix forSize(int theSize) {
        for(NumberPrefix prefix : NumberPrefix.values()) {
            if(prefix.getSize() == theSize) {
                return prefix;
            }
        }
        return null;
    }
    
    public static ArrayList<NumberPrefix> getPrefixListBySize(int theSize) {
        ArrayList<NumberPrefix> outlist = new ArrayList<NumberPrefix>();
        for(NumberPrefix prefix : NumberPrefix.values()) {
            if(prefix.getSize() == theSize) {
                outlist.add(prefix);
            }
        }
        return outlist;
    }
}
