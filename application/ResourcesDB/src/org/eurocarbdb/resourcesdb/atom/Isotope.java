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

import org.eurocarbdb.resourcesdb.ResourcesDbException;

/**
* Class to store isotope data
* @author Thomas Lütteke
*
*/
public class Isotope {
    private int id;
    private Periodic element;
    private String periodicSymbol;
    private Integer neutrons;
    private Double mass;
    private Double abundance;
    private String spin;
    private Boolean stable;
    private String halfLife;
    private String commonName;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public Isotope() {
        this.init();
    }
    
    public Isotope(Periodic elem, Integer neutronCount) {
        this.init();
        this.setElement(elem);
        this.setNeutrons(neutronCount);
    }
    
    public Isotope(Periodic elem, String periodicSym, Integer neutr, Double massDbl, Double abund, String spinStr, Boolean isStable, String halfLifeStr, String commonNameStr) {
        this.init();
        this.setElement(elem);
        this.setPeriodicSymbol(periodicSym);
        this.setNeutrons(neutr);
        this.setMass(massDbl);
        this.setAbundance(abund);
        this.setSpin(spinStr);
        this.setStable(isStable);
        this.setHalfLife(halfLifeStr);
        this.setCommonName(commonNameStr);
    }
    
    public Isotope(String periodicSym, Integer neutr, Double massDbl, Double abund, String spinStr, Boolean isStable, String halfLifeStr, String commonNameStr) {
        this.init();
        try {
            this.setElement(Periodic.getElementBySymbol(periodicSym));
        } catch(ResourcesDbException rEx) {
            
        }
        this.setPeriodicSymbol(periodicSym);
        this.setNeutrons(neutr);
        this.setMass(massDbl);
        this.setAbundance(abund);
        this.setSpin(spinStr);
        this.setStable(isStable);
        this.setHalfLife(halfLifeStr);
        this.setCommonName(commonNameStr);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    /**
     * @return the abundance
     */
    public Double getAbundance() {
        return abundance;
    }
    
    /**
     * @param abundance the abundance to set
     */
    public void setAbundance(Double abundance) {
        this.abundance = abundance;
    }
    
    /**
     * @param abundance the abundance to set
     */
    public void setAbundance(double abundance) {
        this.abundance = new Double(abundance);
    }
    
    /**
     * @return the commonName
     */
    public String getCommonName() {
        return commonName;
    }
    
    /**
     * @param commonName the commonName to set
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
    
    /**
     * @return the element
     */
    public Periodic getElement() {
        return element;
    }
    
    /**
     * @param element the element to set
     */
    public void setElement(Periodic element) {
        this.element = element;
    }
    
    /**
     * @return the halfLife
     */
    public String getHalfLife() {
        return halfLife;
    }
    
    /**
     * @param halfLife the halfLife to set
     */
    public void setHalfLife(String halfLife) {
        this.halfLife = halfLife;
    }
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * @return the mass
     */
    public Double getMass() {
        return mass;
    }
    
    /**
     * @param mass the mass to set
     */
    public void setMass(Double mass) {
        this.mass = mass;
    }
    
    /**
     * @param mass the mass to set
     */
    public void setMass(double mass) {
        this.mass = new Double(mass);
    }
    
    /**
     * @return the neutrons
     */
    public Integer getNeutrons() {
        return neutrons;
    }
    
    /**
     * @param neutrons the neutrons to set
     */
    public void setNeutrons(Integer neutrons) {
        this.neutrons = neutrons;
    }
    
    /**
     * @return the spin
     */
    public String getSpin() {
        return spin;
    }
    
    /**
     * @param spin the spin to set
     */
    public void setSpin(String spin) {
        this.spin = spin;
    }
    
    /**
     * @return the stable
     */
    public boolean isStable() {
        if(this.getStable() == null) {
            return(false);
        }
        return this.getStable().booleanValue();
    }
    
    /**
     * @return the stable
     */
    public Boolean getStable() {
        return stable;
    }

    /**
     * @param stable the stable to set
     */
    public void setStable(boolean stable) {
        this.stable = new Boolean(stable);
    }
    
    /**
     * @param stable the stable to set
     */
    public void setStable(Boolean stable) {
        this.stable = stable;
    }
    
    /**
     * @return the periodicSymbol
     */
    public String getPeriodicSymbol() {
        return periodicSymbol;
    }

    /**
     * @param periodicSymbol the periodicSymbol to set
     */
    public void setPeriodicSymbol(String periodicSymbol) {
        this.periodicSymbol = periodicSymbol;
    }

    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        this.setAbundance(null);
        this.setCommonName(null);
        this.setElement(null);
        this.setHalfLife(null);
        this.setId(0);
        this.setMass(null);
        this.setNeutrons(null);
        this.setSpin(null);
        this.setStable(null);
        this.setPeriodicSymbol(null);
    }
    
    public String toString() {
        String outStr = "Isotope: ";
        outStr += this.getElement().getSymbol() + " " + this.getMass();
        outStr += " | neutrons " + this.getNeutrons();
        outStr += " | spin " + this.getSpin();
        outStr += " | stable " + this.isStable();
        return(outStr);
    }
    
    public String getName() {
        return this.getNeutrons() + this.getPeriodicSymbol();
    }
    
    public int hashCode() {
        return this.getName().hashCode();
    }
}
