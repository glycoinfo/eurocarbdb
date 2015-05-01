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

import java.util.ArrayList;
import java.util.List;

import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideException;
import org.eurocarbdb.resourcesdb.util.NumberUtils;

/**
* Class to store atom properties
* @author Thomas Lütteke
*
*/
public class Atom implements Cloneable {
    
    private int id = 0;
    private int dbId = 0;
    private AtomTemplate template = null;
    private Periodic element = null;
    private Isotope isotope = null;
    private String name = null;
    private String mol2Type = null;
    private Double charge = null;
    private List<AtomConnection> connectionList = null;
    private boolean deletedInLinkage = false;
    
    //*** coordinates: ***
    private Double x = null;
    private Double y = null;
    private Double z = null;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public Atom() {
        this.init();
    }
    
    public Atom(Periodic el) {
        this.init();
        this.setElement(el);
    }
    
    public Atom(String elSym) throws ResourcesDbException {
        this.init();
        this.setElement(Periodic.getElementBySymbol(elSym));
    }
    
    public Atom(AtomTemplate tmpl) {
        this.init();
        this.setTemplate(tmpl);
        this.setElement(tmpl.getElement());
        this.setMol2Type(tmpl.getMol2Type());
    }
    
    public Atom(int id, Periodic el, String name) {
        this.init();
        this.setId(id);
        this.setElement(el);
        this.setName(name);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    /**
     * Get the atom id
     * @return the id
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * Set the atom id
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get the atom template
     * @return the template
     */
    public AtomTemplate getTemplate() {
        return template;
    }

    /**
     * Set the atom template
     * @param tmpl the template to set
     */
    public void setTemplate(AtomTemplate tmpl) {
        this.template = tmpl;
    }
    
    public void setTemplateAndInit(AtomTemplate tmpl) {
        this.template = tmpl;
        this.setElement(tmpl.getElement());
        this.setName(tmpl.getElement().getSymbol());
        this.setMol2Type(tmpl.getMol2Type());
    }

    /**
     * Get the atom name
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Set the atom name
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get this atom's mol2 type
     * @return the mol2 type
     */
    public String getMol2Type() {
        if(this.mol2Type == null) {
            this.setMol2Type(Mol2Type.getMol2Type(this));
        }
        return this.mol2Type;
    }

    /**
     * Set this atom's type for use in mol2 files
     * @param type the mol2 atom type to set
     */
    public void setMol2Type(String type) {
        this.mol2Type = type;
    }

    /**
     * Get the atom's element
     * @return the element
     */
    public Periodic getElement() {
        return this.element;
    }
    
    /**
     * Set the atom's element
     * @param element the element to set
     */
    public void setElement(Periodic element) {
        this.element = element;
    }
    
    /**
     * Get the periodic symbol of the element of this atom
     * @return the periodic symbol
     */
    public String getElementSymbol() {
        if(this.getElement() == null) {
            return(null);
        } else {
            return(this.getElement().getSymbol());
        }
    }
    
    /**
     * Set the periodic element of this atom specified by its periodic symbol
     * (This setter was needed for Hibernate mapping)
     * @param symbol the periodic symbol of the element
     * @throws MonosaccharideException in case the symbol parameter is not a valid periodic symbol
     */
    public void setElementSymbol(String symbol) throws ResourcesDbException {
        this.setElement(Periodic.getElementBySymbol(symbol));
    }
    
    /**
     * Get the atom's charge (Double Object)
     * @return the charge
     */
    public Double getCharge() {
        return this.charge;
    }
    
    /**
     * Get the atom's charge (simple double value)
     * @return the charge
     */
    public double getChargeValue() {
        if(this.getCharge() == null) {
            return(0.0);
        }
        return(this.getCharge().doubleValue());
    }
    
    /**
     * Set the atom's charge using a Double Object
     * @param charge the charge to set
     */
    public void setCharge(Double charge) {
        this.charge = charge;
    }
    
    /**
     * Set the atom's charge using a simple double value
     * @param charge the charge to set
     */
    public void setCharge(double charge) {
        this.charge = new Double(charge);
    }
    
    /**
     * Get the list of connections to other atoms
     * @return the connections
     */
    public List<AtomConnection> getConnections() {
        return this.connectionList;
    }
    
    /**
     * Set the list of connections to other atoms
     * @param connections the connections to set
     */
    public void setConnections(List<AtomConnection> connections) {
        this.connectionList = connections;
    }
    
    /**
     * Add a connection to another atom
     * @param to the atom to which this atom is connected
     * @param bo the bond order of the connection
     */
    public void addConnection(Atom to, double bo) {
        if(!this.hasConnection(to)) {
            this.addConnection(new AtomConnection(this, to, bo));
        }
        if(!to.hasConnection(this)) {
            to.addConnection(this, bo);
        }
    }
    
    /**
     * Add a connection to another atom
     * @param conect the connection object
     */
    public void addConnection(AtomConnection conect) {
        this.getConnections().add(conect);
    }
    
    /**
     * The first connection to a specified atom is removed from the connections list.
     * @param to the Atom the connection to which shall be removed
     * @return true, if such a connection was present
     */
    public boolean removeConnection(Atom to) {
        for(AtomConnection ac : this.getConnections()) {
            if(ac.getToAtom() == to) {
                this.getConnections().remove(ac);
                return(true);
            }
        }
        return(false);
    }
    
    public boolean hasConnection(Atom to) {
        for(AtomConnection ac : this.getConnections()) {
            if(ac.getToAtom().equals(to)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get a clone of the connections list.
     * @return a clone of the connections list
     */
    public List<AtomConnection> getConnectionsClone() {
        List<AtomConnection> retList = new ArrayList<AtomConnection>();
        for(AtomConnection ac : this.getConnections()) {
            retList.add(ac.clone());
        }
        return(retList);
    }
    
    /**
     * Get the isotope of this atom
     * @return the isotope
     */
    public Isotope getIsotope() {
        return this.isotope;
    }

    /**
     * Set the isotope for this atom
     * @param iso the isotope to set
     */
    public void setIsotope(Isotope iso) {
        this.isotope = iso;
    }

    /**
     * Get the database Id of this atom
     * @return the database Id
     */
    public int getDbId() {
        return this.dbId;
    }

    /**
     * Set the database Id of this atom
     * @param dbId the database Id to set
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }
    
    /**
     * Get the value of the flag that marks atoms, which are deleted in a linkage
     * (e.g. atoms of a basetype OH-group that is replaced by a DEOXY-linked substituent)
     * @return the deletedInLinkage flag
     */
    public boolean isDeletedInLinkage() {
        return this.deletedInLinkage;
    }

    /**
     * Set the deletedInLinkage flag
     * @param flag
     */
    public void setDeletedInLinkage(boolean flag) {
        this.deletedInLinkage = flag;
    }

    /**
     * Get the X coordinate of this atom
     * @return the X coordinate
     */
    public Double getX() {
        return this.x;
    }

    /**
     * Set the X coordinate of this atom
     * @param xD the X cooridinate to set
     */
    public void setX(Double xD) {
        this.x = xD;
    }

    /**
     * Get the Y coordinate of this atom
     * @return the Y coordinate
     */
    public Double getY() {
        return this.y;
    }

    /**
     * Set the Y coordinate of this atom
     * @param yD the Y cooridinate to set
     */
    public void setY(Double yD) {
        this.y = yD;
    }

    /**
     * Get the Z coordinate of this atom
     * @return the Z coordinate
     */
    public Double getZ() {
        return this.z;
    }

    /**
     * Set the Z coordinate of this atom
     * @param zD the Z cooridinate to set
     */
    public void setZ(Double zD) {
        this.z = zD;
    }
    
    /**
     * Set the coordinates of this atom
     * @param xD the X cooridinate to set
     * @param yD the Y cooridinate to set
     * @param zD the Z cooridinate to set
     */
    public void setCoordinates(Double xD, Double yD, Double zD) {
        this.setX(xD);
        this.setY(yD);
        this.setZ(zD);
    }

    //*****************************************************************************
    //*** connection related methods: *********************************************
    //*****************************************************************************
    
    /**
     * Get the sum of the bond orders of this atom's connections
     * @return the bond order sum
     */
    public double getBondSum() {
        double bs = 0;
        for(AtomConnection ac : this.getConnections()) {
            bs += ac.getBondOrder().doubleValue();
        }
        return(bs);
    }
    
    public int countBonds() {
        if(this.getConnections() == null) {
            return 0;
        }
        return this.getConnections().size();
    }
    
    public int countBondsByBondorder(double bo) {
        int count = 0;
        if(this.getConnections() != null) {
            for(AtomConnection con : this.getConnections()) {
                if(con.getBondOrder() == bo) {
                    count ++;
                }
            }
        }
        return count;
    }
    
    public int countSingleBonds() {
        return this.countBondsByBondorder(1.0);
    }
    
    public int countDoubleBonds() {
        return this.countBondsByBondorder(2.0);
    }
    
    public int countAromaticBonds() {
        return this.countBondsByBondorder(1.5);
    }
    
    public int countTripleBonds() {
        return this.countBondsByBondorder(3.0);
    }
    
    public Atom getConnectedAtom(String elemSymbol, int index) {
        if(this.getConnections() != null) {
            int count = 0;
            for(AtomConnection acon : this.getConnections()) {
                Atom toAtom = acon.getToAtom();
                if(toAtom.equals(this)) {
                    toAtom = acon.getFromAtom();
                }
                if(toAtom.getElementSymbol() != null && toAtom.getElementSymbol().equals(elemSymbol)) {
                    count ++;
                    if(count == index) {
                        return toAtom;
                    }
                }
            }
        }
        return null;
    }
    
    public int countConnectedAtoms(String elemSymbol) {
        int count = 0;
        if(this.getConnections() != null) {
            for(AtomConnection acon : this.getConnections()) {
                Atom toAtom = acon.getToAtom();
                if(toAtom.equals(this)) {
                    toAtom = acon.getFromAtom();
                }
                if(toAtom.getElementSymbol() != null && toAtom.getElementSymbol().equals(elemSymbol)) {
                    count ++;
                }
            }
        }
        return count;
    }

    //*****************************************************************************
    //*** xml parsing methods: ****************************************************
    //*****************************************************************************
    
    public static Atom parseXmlAtomTag(org.jdom.Element atomtag) throws ResourcesDbException {
        Atom a = null;
        if(atomtag.getName().equalsIgnoreCase("atom")) {
            a = new Atom();
            a.setId(Integer.parseInt(atomtag.getAttributeValue("id")));
            a.setName(atomtag.getAttributeValue("name"));
            a.setElement(Periodic.getElementBySymbol(atomtag.getAttributeValue("element")));
            a.setX(NumberUtils.parseDoubleStr(atomtag.getAttributeValue("x"), null));
            a.setY(NumberUtils.parseDoubleStr(atomtag.getAttributeValue("y"), null));
            a.setZ(NumberUtils.parseDoubleStr(atomtag.getAttributeValue("z"), null));
        }
        return(a);
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    /**
     * Initialize the atom properties
     */
    public void init() {
        this.setId(0);
        this.setDbId(0);
        this.setName(null);
        this.setElement(null);
        this.setIsotope(null);
        this.setCharge(null);
        this.setConnections(new ArrayList<AtomConnection>());
    }
    
    /** 
     * Check, if this atom equals another atom
     * @param o another atom
     * @return true, if the name, element and isotope of o equal those of this atom
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if(o instanceof Atom) {
            Atom a = (Atom) o;
            if((a.getName() == null && this.getName() == null) || (a.getName() != null && a.getName().equals(this.getName()))) {
                if((a.getElement() == null && this.getElement() == null) || (a.getElement() != null && a.getElement().equals(this.getElement()))) {
                    if((a.getIsotope() == null && this.getIsotope() == null) || (a.getIsotope() != null && a.getIsotope().equals(this.getIsotope()))) {
                        return(true);
                    }
                }
            }
        }
        return(false);
    }
    
    public String toString() {
        String outStr;
        outStr = "Atom [Id: " + this.getId() + " Element: " + this.getElement().getSymbol() + " Name: " + this.getName() + " Charge: " + this.getCharge();
        if(this.isDeletedInLinkage()) {
            outStr += " (deleted)";
        }
        outStr += "]"; 
        return(outStr);
    }
    
    public Atom clone() {
        Atom a = new Atom(this.getElement());
        a.setName(this.getName());
        a.setIsotope(this.getIsotope());
        a.setCharge(this.getCharge());
        a.setConnections(this.getConnectionsClone());
        a.setTemplate(this.getTemplate());
        a.setMol2Type(this.getMol2Type());
        a.setX(this.getX());
        a.setY(this.getY());
        a.setZ(this.getZ());
        return(a);
    }
    
    /**
     * Remove an atom from a list.
     * The given atom is removed from the list. Connections from other atoms to this one are removed as well. 
     * @param atomList a list of atoms
     * @param a the atom to be removed
     * @param removeHydrogens flag to indicate if hydrogens that are attached to the removed atom are to be removed as well
     */
    public static void removeAtomFromList(List<Atom> atomList, Atom a, boolean removeHydrogens) {
        Atom aClone = null;
        for(Atom listAtom : atomList) {
            if(listAtom.equals(a)) {
                aClone = listAtom;
                break;
            }
        }
        atomList.remove(aClone);
        for(AtomConnection con : aClone.getConnections()) {
            Atom toAtom = null;
            if(con.getToAtom().equals(aClone)) {
                toAtom = con.getFromAtom();
            } else {
                toAtom = con.getToAtom();
            }
            if(removeHydrogens) { //*** remove attached hydrogens ***
                if(toAtom.getElementSymbol().equals("H")) {
                    atomList.remove(con.getToAtom());
                    continue;
                }
            }
            //*** remove back-connection to removed atom: ***
            ArrayList<AtomConnection> removeCons = new ArrayList<AtomConnection>();
            for(AtomConnection backCon : toAtom.getConnections()) {
                if(backCon.getToAtom().equals(aClone)) {
                    removeCons.add(backCon);
                    //toAtom.getConnections().remove(backCon);
                }
            }
            for(AtomConnection remCon : removeCons) {
                toAtom.getConnections().remove(remCon);
            }
        }
    }
    
    public static void printAtoms(List<Atom> atomList) {
        for(Atom a : atomList) {
            System.out.println(a.toString());
            for(AtomConnection con : a.getConnections()) {
                System.out.println("   " + con.toString());
            }
        }
    }
}
