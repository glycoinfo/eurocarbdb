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

import java.util.HashMap;
import java.util.List;

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideException;
import org.eurocarbdb.resourcesdb.util.StringUtils;

/**
* Class to store / handle element compositions (element counts).
* @author Thomas Lütteke
* 
*/
public class Composition implements java.lang.Cloneable {

    private HashMap<String, Integer> compositionMap;
    
    /**
     * Constructor to create an empty composition
     */
    public Composition() {
        this.setCompositionMap(new HashMap<String, Integer>());
    }
    
    /**
     * Constructor to create a new composition and initialize it using a chemical formula
     * @param formula a chemical formula like C6H12O6 or CH2OHCOCHOHCH2Cl
     * @throws ResourcesDbException in case the formula cannot be parsed properly
     */
    public Composition(String formula) throws ResourcesDbException {
        this.parseFormula(formula);
    }
    
    /**
     * Constructor to create a new composition and initialize it using an already existing composition
     * @param compo a Composition object
     */
    public Composition(Composition compo) {
        this.addComposition(compo);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public HashMap<String, Integer> getCompositionMap() {
        return this.compositionMap;
    }
    
    private void setCompositionMap(HashMap<String, Integer> composMap) {
        this.compositionMap = composMap;
    }
    
    public int getElementCount(Periodic el) {
        if(el == null) {
            return 0;
        }
        return getElementCount(el.getSymbol());
    }
    
    public int getElementCount(String elSymbol) {
        if(getCompositionMap().get(elSymbol) == null) {
            return 0;
        }
        return getCompositionMap().get(elSymbol).intValue();
    }
    
    public void setElementCount(Periodic el, int value) {
        getCompositionMap().put(el.getSymbol(), new Integer(value));
    }
    
    public void setElementCount(String elSymbol, int value) {
        getCompositionMap().put(elSymbol, new Integer(value));
    }
    
    //*****************************************************************************
    //*** methods to change composition: ******************************************
    //*****************************************************************************
    
    /**
     * Increase the number of atoms of a given element type by a given number
     * @param el the element type
     * @param step the value to be added to the element count
     */
    public void increaseCount(Periodic el, int step) {
        Integer countObj = getCompositionMap().get(el.getSymbol());
        if(countObj == null) {
            countObj = new Integer(step);
        } else {
            countObj += step;
        }
        getCompositionMap().put(el.getSymbol(), countObj);
    }
    
    /**
     * Increase the number of atoms of a given element type by 1
     * @param el the element type
     */
    public void increaseCount(Periodic el) {
        increaseCount(el, 1);
    }
    
    public void increaseCount(String elSymbol, int step) {
        Integer countObj = getCompositionMap().get(elSymbol);
        if(countObj == null) {
            countObj = new Integer(step);
        } else {
            countObj += step;
        }
        getCompositionMap().put(elSymbol, countObj);
    }
    
    /**
     * Decrease the number of atoms of a given element type by a given number
     * @param el the element type
     * @param step the value to be substracted from the element count
     */
    public void decreaseCount(Periodic el, int step) {
        Integer countObj = getCompositionMap().get(el.getSymbol());
        if(countObj == null) {
            countObj = new Integer(-1 * step);
        } else {
            countObj -= step;
        }
        getCompositionMap().put(el.getSymbol(), countObj);
    }
    
    /**
     * Decrease the number of atoms of a given element type by 1
     * @param el the element type
     */
    public void decreaseCount(Periodic el) {
        decreaseCount(el, 1);
    }
    
    /**
     * Set the composition using a chemical formula.
     * This method initializes the composition, existing element counts are deleted.
     * To preserve the existing counts and just add the ones from the formula use the addFormula(Str) method instead.
     * @param formula a chemical formula like C6H12O6 or CH2OHCOCHOHCH2Cl
     * @throws ResourcesDbException in case the formula cannot be parsed properly
     */
    public void parseFormula(String formula) throws ResourcesDbException {
        //*** empty composition map: ***
        if(this.getCompositionMap() != null) {
            this.getCompositionMap().clear();
        }
        //*** get elements and counts from formula: ***
        this.addFormula(formula);
    }
    
    /**
     * Add a chemical formula to this composition.
     * Element counts yielded from the formula are added to ones that are already present in this composition.
     * @param formula a chemical formula like C6H12O6 or CH2OHCOCHOHCH2Cl
     * @throws ResourcesDbException in case the formula cannot be parsed properly
     */
    public void addFormula(String formula) throws ResourcesDbException {
        //*** set composition map if not done yet (avoid null pointer exceptions): ***
        if(this.getCompositionMap() == null) {
            this.setCompositionMap(new HashMap<String, Integer>());
        }
        //*** get elements and counts from formula: ***
        String subformula;
        if(formula != null) {
            subformula = formula;
        } else {
            subformula = "";
        }
        while(subformula.length() > 0) {
            if(subformula.startsWith(" ")) { //*** ignore blanks ***
                subformula = subformula.substring(1);
                continue;
            }
            if(subformula.startsWith("(")) { //*** parse subcomposition ***
                int closeBracketPos = StringUtils.findClosingBracketPosition(subformula);
                if(closeBracketPos > 0) {
                    Composition subCompo = new Composition(subformula.substring(1, closeBracketPos));
                    subformula = subformula.substring(closeBracketPos + 1);
                    int count;
                    if(subformula.length() == 0 || subformula.matches("^[A-Z()].*")) {
                        count = 1;
                    } else {
                        count = 0;
                        while(subformula.matches("^[0-9].*")) {
                            count = 10 * count + Integer.parseInt(subformula.substring(0, 1));
                            subformula = subformula.substring(1);
                        }
                    }
                    if(count != 1) {
                        for(String elSymbol : subCompo.getCompositionMap().keySet()) {
                            subCompo.getCompositionMap().put(elSymbol, subCompo.getElementCount(elSymbol) * count);
                        }
                    }
                    this.addComposition(subCompo);
                    continue;
                }
            }
            boolean foundElement = false;
            //for(Periodic el : Periodic.getElementsList()) {
            for(Periodic el : Periodic.values()) {
                String symbol = el.getSymbol();
                if(subformula.matches("^" + symbol + "[0-9A-Z()].*") || subformula.equals(symbol)) {
                    subformula = subformula.substring(symbol.length());
                    foundElement = true;
                    int count;
                    if(subformula.length() == 0 || subformula.matches("^[A-Z()].*")) {
                        count = 1;
                    } else {
                        count = 0;
                        while(subformula.matches("^[0-9].*")) {
                            count = 10 * count + Integer.parseInt(subformula.substring(0, 1));
                            subformula = subformula.substring(1);
                        }
                    }
                    this.increaseCount(el, count);
                    break;
                }
            }
            if(!foundElement) {
                throw new MonosaccharideException("Cannot parse formula " + formula);
            }
        }
    }
    
    /**
     * Add another composition to this composition.
     * The element counts of the other composition are added to the ones of this composition.
     * @param compo the composition to add
     */
    public void addComposition(Composition compo) {
        //*** set composition map if not done yet (avoid null pointer exceptions): ***
        if(this.getCompositionMap() == null) {
            this.setCompositionMap(new HashMap<String, Integer>());
        }
        //*** add element counts: ***
        for(String elSymbol : compo.getCompositionMap().keySet()) {
            Integer elCount = compo.getElementCount(elSymbol);
            if(elCount == null || elCount.intValue() == 0) {
                continue;
            }
            this.increaseCount(elSymbol, elCount.intValue());
        }
    }
    
    /**
     * Add a list of atoms to this composition
     * @param atomList the atoms to add
     */
    public void addAtoms(List<Atom> atomList) {
        for(Atom a : atomList) {
            this.addAtom(a);
        }
    }
    
    /**
     * Add a single atom to this composition
     * @param a the atom to add
     */
    public void addAtom(Atom a) {
        this.increaseCount(a.getElement());
    }
    
    //*****************************************************************************
    //*** mass related methods: ***************************************************
    //*****************************************************************************
    
    /**
     * Calculate the average mass of the atoms contained in this composition
     * @return the average mass
     */
    public double getAvgMass() {
        double mass = 0.0;
        for(String elSymbol : this.getCompositionMap().keySet()) {
            try {
                Periodic el = Periodic.getElementBySymbol(elSymbol);
                Integer elCount = this.getElementCount(el);
                if(elCount != null) {
                    mass += elCount.intValue() * el.getAvgMass().doubleValue();
                }
            } catch(ResourcesDbException me) {
                if(Config.getGlobalConfig().isPrintErrorMsgs()) {
                    System.err.println("Exception: " + me);
                    me.printStackTrace();
                }
            }
        }
        return(mass);
    }
    
    /**
     * Calculate the monoisotopic mass of the atoms contained in this composition
     * @return the monoisotopic mass
     */
    public double getMonoMass() {
        double mass = 0.0;
        for(String elSymbol : this.getCompositionMap().keySet()) {
            try {
                Periodic el = Periodic.getElementBySymbol(elSymbol);
                int elCount = this.getElementCount(el);
                if(elCount != 0) {
                    mass += elCount * el.getMostAbundantIsotope().getMass().doubleValue();
                }
            } catch(ResourcesDbException me) {
                if(Config.getGlobalConfig().isPrintErrorMsgs()) {
                    System.err.println("Exception: " + me);
                    me.printStackTrace();
                }
            }
        }
        return(mass);
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    /**
     * Convert the composition into a chemical formula string
     * @return the chemical formula representing this composition
     */
    public String toFormula() {
        String outStr = "";
        /*for(Periodic el : Periodic.getElementsList()) {
            Integer elCount = this.getElementCount(el);
            if(elCount != null && elCount.intValue() != 0) {
                outStr += el.getSymbol() + elCount.intValue();
            }
        }*/
        for(String elemSymbol : this.getCompositionMap().keySet()) {
            Integer elCount = this.getElementCount(elemSymbol);
            if(elCount != null && elCount.intValue() != 0) {
                outStr += elemSymbol + elCount.intValue();
            }
        }
        return(outStr);
    }
    
    public String toWebFormula() {
        String outStr = "";
        /*for(Periodic el : Periodic.getElementsList()) {
            Integer elCount = this.getElementCount(el);
            if(elCount != null && elCount.intValue() != 0) {
                outStr += el.getSymbol() + "<sub>" + elCount.intValue() + "</sub>";
            }
        }*/
        for(String elemSymbol : this.getCompositionMap().keySet()) {
            Integer elCount = this.getElementCount(elemSymbol);
            if(elCount != null && elCount.intValue() != 0) {
                outStr += elemSymbol + "<sub>" + elCount.intValue() + "</sub>";
            }
        }
        return outStr;
    }
    
    public String toString() {
        String outStr = "Composition:";
        /*for(Periodic el : Periodic.getElementsList()) {
            Integer elCount = this.getElementCount(el);
            if(elCount != null && elCount.intValue() != 0) {
                outStr += " " + el.getSymbol() + elCount.intValue();
            }
        }*/
        for(String elemSymbol : this.getCompositionMap().keySet()) {
            Integer elCount = this.getElementCount(elemSymbol);
            if(elCount != null && elCount.intValue() != 0) {
                outStr += " " + elemSymbol + elCount.intValue();
            }
        }
        return(outStr);
    }
    
    public Composition clone() {
        Composition retComp = new Composition(this);
        return(retComp);
    }
    
}
