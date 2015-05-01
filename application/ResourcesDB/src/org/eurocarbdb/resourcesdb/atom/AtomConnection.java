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

import org.eurocarbdb.resourcesdb.MolecularEntity;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideException;

/**
* Class to store connections between atoms
* @author Thomas Lütteke
*
*/
public class AtomConnection implements Cloneable {
    
    private Atom fromAtom = null;
    private Atom toAtom = null;
    private Double bondOrder = null;
    
    private int dbId;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public AtomConnection(Atom from, Atom to, Double bo) {
        this.setFromAtom(from);
        this.setToAtom(to);
        this.setBondOrder(bo);
    }
    
    public AtomConnection() {
        this.init();
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    /**
     * @return the fromAtom
     */
    public Atom getFromAtom() {
        return fromAtom;
    }
    
    /**
     * @param fromAtom the fromAtom to set
     */
    public void setFromAtom(Atom fromAtom) {
        this.fromAtom = fromAtom;
    }
    
    /**
     * @return the toAtom
     */
    public Atom getToAtom() {
        return toAtom;
    }
    
    /**
     * @param toAtom the toAtom to set
     */
    public void setToAtom(Atom toAtom) {
        this.toAtom = toAtom;
    }
    
    /**
     * @return the bondOrder
     */
    public Double getBondOrder() {
        return bondOrder;
    }
    
    /**
     * @param bondOrder the bondOrder to set
     */
    public void setBondOrder(Double bondOrder) {
        this.bondOrder = bondOrder;
    }
    
    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    //*****************************************************************************
    //*** xml parsing methods: ****************************************************
    //*****************************************************************************
    
    public static void parseXmlAtomConnectionTag(org.jdom.Element acTag, MolecularEntity molec) throws MonosaccharideException {
        if(acTag.getName().equalsIgnoreCase("connection")) {
            Double bo = null;
            Atom fromAtom = null;
            Atom toAtom = null;
            
            String toAtomIdStr = acTag.getAttributeValue("to");
            String fromAtomIdStr = acTag.getAttributeValue("from");
            try {
                int toAtomId = Integer.parseInt(toAtomIdStr);
                toAtom = molec.getAtomById(toAtomId);
                
                int fromAtomId = Integer.parseInt(fromAtomIdStr);
                fromAtom = molec.getAtomById(fromAtomId);
                
                String boStr = acTag.getAttributeValue("bond_order");
                if(boStr != null && boStr.length() > 0) {
                    bo = Double.parseDouble(boStr);
                }
            } catch(NumberFormatException ne) {
                MonosaccharideException me = new MonosaccharideException("XmlAtomConnection: Exception occurred when parsing numerical parameter.");
                me.initCause(ne);
                throw me;
            }
            
            if(toAtom == null || fromAtom == null) {
                throw new MonosaccharideException("XmlAtomConnection: could not get atom from xml tag (IDs: from " + fromAtomIdStr + " / to " + toAtomIdStr + ").");
            }
            
            fromAtom.addConnection(toAtom, bo);
            toAtom.addConnection(fromAtom, bo);
        }
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        this.setBondOrder(null);
        this.setFromAtom(null);
        this.setToAtom(null);
    }
    
    public String toString() {
        String outStr;
        outStr = "AtomConnection from " + this.getFromAtom().getId() + " (" + this.getFromAtom().getName() + ") to " + this.getToAtom().getId() + " (" + this.getToAtom().getName() + ") BondOrder " + this.getBondOrder(); 
        return(outStr);
    }
    
    public boolean equals(Object obj) {
        if(obj == null) {
            return(false);
        }
        if(! (obj instanceof AtomConnection)) {
            return(false);
        }
        AtomConnection conect = (AtomConnection) obj;
        if(conect.getFromAtom() == this.getFromAtom() && conect.getToAtom() == this.getToAtom() && conect.getBondOrder() == this.getBondOrder()) {
            return(true);
        }
        return(false);
    }
    
    public AtomConnection clone() {
        return new AtomConnection(this.getFromAtom(), this.getToAtom(), this.getBondOrder());
    }
}
