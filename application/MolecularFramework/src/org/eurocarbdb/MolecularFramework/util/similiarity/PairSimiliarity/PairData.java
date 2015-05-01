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

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;

/**
* @author sherget
*
*/
public class PairData {
    
    GlycoNode parent=null;
    GlycoNode child=null;
    GlycoEdge edge=null;
    
    public void clear (){
        this.parent=null;
        this.child=null;
        this.edge=null;
    }
    
    public GlycoNode getChild() {
        return child;
    }
    public void setChild(GlycoNode child) {
        this.child = child;
    }
    public GlycoEdge getEdge() {
        return edge;
    }
    public void setEdge(GlycoEdge edge) {
        this.edge = edge;
    }
    public GlycoNode getParent() {
        return parent;
    }
    public void setParent(GlycoNode parent) {
        this.parent = parent;
    }

}
