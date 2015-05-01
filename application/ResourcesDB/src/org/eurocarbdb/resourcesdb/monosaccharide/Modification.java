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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eurocarbdb.resourcesdb.ResourcesDbObject;
import org.eurocarbdb.resourcesdb.util.Utils;

/**
* The Modification class is the basis for both the Substitution and the CoreModification classes.
* It contains the fields and methods that are common to both types of modifications, esp. the methods for handling the positions of the monosaccharide at which the modification is present.
*
* @author Thomas Luetteke
*/
public class Modification extends ResourcesDbObject implements Comparator<Object>, Cloneable {

    private String name;
    private int valence;
    private ArrayList<Integer> position1;
    private ArrayList<Integer> position2;
    private String sourceName;
    private int modificationId = 0;
    
    public static final int EMPTYPOSITIONVALUE = -1;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public Modification() {
        this.init();
    }

    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValence() {
        return valence;
    }

    public void setValence(int valence) {
        this.valence = valence;
    }

    /**
     * Get the original modification name as used in the parsed source residue
     * @return
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Set the original modification name as used in the parsed source residue
     * @param sourceName
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * @return the modificationId
     */
    public int getModificationId() {
        return modificationId;
    }

    /**
     * @param modificationId the modificationId to set
     */
    public void setModificationId(int modificationId) {
        this.modificationId = modificationId;
    }
    
    //*****************************************************************************
    //*** position related methods: ***********************************************
    //*****************************************************************************
    
    public ArrayList<Integer> getPosition1() {
        if(this.position1 == null) {
            this.position1 = new ArrayList<Integer>();
        }
        return this.position1;
    }
    
    public String getPosition1Str(String delimiter, String unknownPositionLabel) {
        return Utils.formatPositionsString(this.getPosition1(), delimiter, unknownPositionLabel);
    }
    
    public ArrayList<Integer> getPosition1Clone() {
        return(Utils.cloneIntegerList(this.getPosition1()));
    }
    
    public int getIntValuePosition1() {
        if(getPosition1().size() == 0) {
            return(Modification.EMPTYPOSITIONVALUE);
        } else if(getPosition1().size() == 1) {
            return(getPosition1().get(0).intValue());
        } else {
            return(0);
        }
    }
    
    public void setIntValuePosition1(int position) throws MonosaccharideException {
        setPosition1(position);
    }
    
    public void setPosition1(ArrayList<Integer> positions) {
        this.position1 = positions;
    }
    
    public void setPosition1(int position) throws MonosaccharideException {
        if(getPosition1() == null) {
            setPosition1(new ArrayList<Integer>());
        } else {
            getPosition1().clear();
        }
        if(position != Modification.EMPTYPOSITIONVALUE) {
            addPosition1(position);
        }
    }
    
    public void addPosition1(int position) throws MonosaccharideException {
        if(position < 0) {
            throw new MonosaccharideException("Modification position must not be negative.");
        }
        if(!containsPosition1(position)) {
            getPosition1().add(new Integer(position));
            Collections.sort(getPosition1());
        }
    }

    public boolean containsPosition1(int position) {
        for(int i = 0; i < getPosition1().size(); i++) {
            int usedPosition = getPosition1().get(i).intValue();
            if(usedPosition == position) {
                return(true);
            }
        }
        return(false);
    }
    
    /**
     * Check, if the modification position 1 is identical with a given position
     * @param position: the position to be checked
     * @return true, if position1 contains exactly one value, which equals the position parameter
     */
    public boolean position1equals(int position) {
        return(getPosition1().size() == 1 && getIntValuePosition1() == position);
    }
    
    public ArrayList<Integer> getPosition2() {
        if(this.position2 == null) {
            this.position2 = new ArrayList<Integer>();
        }
        return this.position2;
    }
    
    public String getPosition2Str(String delimiter, String unknownPositionLabel) {
        return Utils.formatPositionsString(this.getPosition2(), delimiter, unknownPositionLabel);
    }
    
    public ArrayList<Integer> getPosition2Clone() {
        return(Utils.cloneIntegerList(this.getPosition2()));
    }

    public int getIntValuePosition2() {
        if(getPosition2().size() == 0) {
            return(Modification.EMPTYPOSITIONVALUE);
        } else if(getPosition2().size() == 1) {
            return(getPosition2().get(0).intValue());
        } else {
            return(0);
        }
    }
    
    public void setIntValuePosition2(int position) throws MonosaccharideException {
        setPosition2(position);
    }
    
    public void setPosition2(ArrayList<Integer> positions) {
        this.position2 = positions;
    }
    
    public void setPosition2(int position) throws MonosaccharideException {
        if(getPosition2() == null) {
            setPosition2(new ArrayList<Integer>());
        } else {
            getPosition2().clear();
        }
        if(position != Modification.EMPTYPOSITIONVALUE) {
            addPosition2(position);
        }
    }
    
    public void addPosition2(int position) throws MonosaccharideException {
        if(position < 0) {
            throw new MonosaccharideException("Modification position must not be negative.");
        }
        if(!containsPosition2(position)) {
            getPosition2().add(new Integer(position));
            Collections.sort(getPosition2());
        }
    }

    public boolean containsPosition2(int position) {
        for(int i = 0; i < getPosition2().size(); i++) {
            int usedPosition = getPosition2().get(i).intValue();
            if(usedPosition == position) {
                return(true);
            }
        }
        return(false);
    }
    
    /**
     * Check, if the modification position 2 is identical with a given position
     * @param position: the position to be checked
     * @return true, if position2 contains exactly one value, which equals the position parameter
     */
    public boolean position2equals(int position) {
        return(getPosition2().size() == 1 && getIntValuePosition2() == position);
    }
    
    /**
     * Check, if a given position is contained in the positions of the modification
     * @param position: the position to be checked
     * @return true, if the given position is present, otherwise false
     */
    public boolean containsPosition(int position) {
        return(containsPosition1(position) || containsPosition2(position));
    }
    
    /**
     * Check, if the modification is divalent, i.e. if a position2 is set for the modification.
     * @return true, if the modification is divalent, otherwise false
     */
    public boolean hasPosition2() {
        if(getPosition2().size() > 0) {
            return(true);
        }
        return(false);
    }
    
    /**
     * Get a list of all positions the modification is involved in.
     * @return
     */
    public ArrayList<Integer> getPositions() {
        ArrayList<Integer> posList = this.getPosition1Clone();
        if(this.hasPosition2()) {
            //posList.addAll(this.getPosition2());
            for(Integer pos2value : this.getPosition2()) {
                if(!this.containsPosition1(pos2value.intValue())) { //*** avoid adding double values, in case both position1 and position2 contain the same value (due to uncertain positions or a substituent which is twice linked to the same backbone atom like pyruvate) ***
                    posList.add(pos2value);
                }
            }
        }
        return(posList);
    }
    
    /**
     * Check, if this modification has an uncertain linkage position, i.e. if it has a linkage position for which more than one value or the value "0" is set
     * @return
     */
    public boolean hasUncertainLinkagePosition() {
        if(getPosition1().size() > 1) {
            return(true);
        }
        if(containsPosition1(0)) {
            return(true);
        }
        if(hasPosition2()) {
            if(getPosition2().size() > 1) {
                return(true);
            }
            if(containsPosition2(0)) {
                return(true);
            }
        }
        return(false);
    }

    public void sortPositions() {
        Collections.sort(this.getPosition1());
        if(this.hasPosition2()) {
            Collections.sort(this.getPosition2());
            String pos1Str = Utils.formatPositionsString(this.getPosition1(), ",", "X");
            String pos2Str = Utils.formatPositionsString(this.getPosition2(), ",", "X");
            if(pos1Str.compareTo(pos2Str) > 0) {
                ArrayList<Integer> tmpPosList = this.getPosition1();
                this.setPosition1(this.getPosition2());
                this.setPosition2(tmpPosList);
            }
        }
    }
    
    /**
     * Check, if the positions of this modification equal those of another modification.
     * The check is independant of the order of uncertain positions.
     * @param otherMod the Modification to compare to this one
     * @return true, if position1 and position2 of both modifications contain the same values
     */
    public boolean positionsEqual(Modification otherMod) {
        if(otherMod == null) {
            return false;
        }
        if(this.getPosition1().size() != otherMod.getPosition1().size()) {
            return false;
        }
        for(Integer posInt : this.getPosition1()) {
            if(!otherMod.containsPosition1(posInt.intValue())) {
                return false;
            }
        }
        if(this.getPosition2().size() != otherMod.getPosition2().size()) {
            return false;
        }
        for(Integer posInt : this.getPosition2()) {
            if(!otherMod.containsPosition2(posInt.intValue())) {
                return false;
            }
        }
        return true;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public Modification clone() {
        Modification modClone = new Modification();
        modClone.setName(this.getName());
        modClone.setValence(this.getValence());
        modClone.setPosition1(this.getPosition1Clone());
        modClone.setPosition2(this.getPosition2Clone());
        return(modClone);
    }
    
    public int compare(Object o1, Object o2) {
        ArrayList<Integer> positions1 = ((Modification) o1).getPosition1();
        ArrayList<Integer> positions2 = ((Modification) o2).getPosition1();
        for(int i = 0; i < Math.min(positions1.size(), positions2.size()); i++) {
            if(positions1.get(i).intValue() < positions2.get(i).intValue() && positions1.get(i).intValue() != 0) {
                return(-1);
            }
            if(positions1.get(i).intValue() > positions2.get(i).intValue()) {
                return(1);
            }
        }
        if(positions1.size() > positions2.size()) {
            return(-1);
        }
        if(positions1.size() < positions2.size()) {
            return(1);
        }
        positions1 = ((Modification) o1).getPosition2();
        positions2 = ((Modification) o2).getPosition2();
        for(int i = 0; i < Math.min(positions1.size(), positions2.size()); i++) {
            if(positions1.get(i).intValue() < positions2.get(i).intValue()) {
                return(-1);
            }
            if(positions1.get(i).intValue() > positions2.get(i).intValue()) {
            return(1);
            }
        }
        if(positions1.size() > positions2.size()) {
            return(-1);
        }
        if(positions1.size() < positions2.size()) {
            return(1);
        }
        return(((Modification) o1).getName().compareTo(((Modification) o2).getName()));
    }
    
    public boolean equals(Modification mod2) {
        return(compare(this, mod2) == 0);
    }
    
    public void init() {
        this.setName(null);
        this.setPosition1(new ArrayList<Integer>());
        this.setPosition2(new ArrayList<Integer>());
        this.setValence(0);
        this.setSourceName(null);
        this.setModificationId(0);
    }
    
    public String makeCmpString() {
        String cmpStr;
        cmpStr = Utils.formatPositionsString(this.getPosition1(), "/", "0");
        if(this.hasPosition2()) {
            cmpStr += "|" + Utils.formatPositionsString(this.getPosition2(), "/", "0");
        }
        cmpStr += "|" + this.getName();
        return cmpStr;
    }
    
    public String toString() {
        String str = "[";
        str += "name: " + this.getName() + "; ";
        str += "position: " + this.getPosition1().toString();
        if(getPosition2() != null && getPosition2().size() > 0) {
            str += "/" + getPosition2().toString();
        }
        //str += ";";
        str += "]";
        return(str);
    }
}
