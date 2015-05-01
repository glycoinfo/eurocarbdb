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
package org.eurocarbdb.resourcesdb.template;

import java.util.ArrayList;
import java.util.List;

import org.eurocarbdb.resourcesdb.MolecularEntity;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.atom.Atom;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.nonmonosaccharide.NonBasetypeLinkingPosition;

/**
* Class to store properties that are present in SubstituentTemplates and AglyconTemplates
* @author Thomas Lütteke
*
*/
public abstract class NonBasetypeTemplate extends MolecularEntity {
    private int minValence;
    private int maxValence;
    private int defaultLinkingPosition1;
    private int defaultLinkingPosition2;
    private List<NonBasetypeLinkingPosition> validLinkingPositionsList;
    private String comments;
    private boolean fuzzy;

    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /**
     * Constructor, initializes template
     */
    public NonBasetypeTemplate() {
        init();
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************

    /**
     * Set the valence (= number of possible linkages of this template).
     * Both the min and the max valence are set to the same value
     * @param valence the valence
     */
    public void setValence(int valence) {
        this.minValence = valence;
        this.maxValence = valence;
    }
    
    /**
     * Get the max valence (= maximum number of linkages of this template)
     * @return the valence
     */
    public int getMaxValence() {
        return maxValence;
    }

    /**
     * Set the max valence (= maximum number of linkages of this template).
     * @param valence the max valence
     */
    public void setMaxValence(int valence) {
        this.maxValence = valence;
    }

    /**
     * Get the min valence (= minimum number of linkages of this template)
     * @return the valence
     */
    public int getMinValence() {
        return minValence;
    }

    /**
     * Set the min valence (= minimum number of linkages of this template).
     * @param valence the min valence
     */
    public void setMinValence(int valence) {
        this.minValence = valence;
    }

    /**
     * @return the defaultLinkingPosition1
     */
    public int getDefaultLinkingPosition1() {
        return this.defaultLinkingPosition1;
    }

    /**
     * @param defaultLinkingPosition1 the defaultLinkingPosition1 to set
     */
    public void setDefaultLinkingPosition1(int defaultLinkingPosition1) {
        this.defaultLinkingPosition1 = defaultLinkingPosition1;
    }

    /**
     * @return the defaultLinkingPosition2
     */
    public int getDefaultLinkingPosition2() {
        return this.defaultLinkingPosition2;
    }

    /**
     * @param defaultLinkingPosition2 the defaultLinkingPosition2 to set
     */
    public void setDefaultLinkingPosition2(int defaultLinkingPosition2) {
        this.defaultLinkingPosition2 = defaultLinkingPosition2;
    }

    /**
     * @return the bondOrder1
     */
    public double getDefaultBondOrder1() {
        NonBasetypeLinkingPosition defaultPos1 = this.getValidLinkingPosition(this.getDefaultLinkingPosition1());
        if(defaultPos1 == null) {
            return 0.0;
        } else {
            return defaultPos1.getBondOrder();
        }
        /*try {
            return this.getValidLinkingPosition(this.getDefaultLinkingPosition1()).getBondOrder().doubleValue();
        } catch(ResourcesDbException me) {
            return(0.0);
        }*/
    }

    /**
     * @return the bondOrder2
     */
    public double getDefaultBondOrder2() {
        NonBasetypeLinkingPosition defaultPos2 = this.getValidLinkingPosition(this.getDefaultLinkingPosition2());
        if(defaultPos2 == null) {
            return 0.0;
        } else {
            return defaultPos2.getBondOrder();
        }
        /*try {
            return this.getValidLinkingPosition(this.getDefaultLinkingPosition2()).getBondOrder().doubleValue();
        } catch(ResourcesDbException me) {
            return(0.0);
        }*/
    }

    /**
     * Get a list of the valid linking positions on the nonmonosaccharide residue side
     * @return the validLinkingPositions
     * @throws MonosaccharideException in case the validLinkingPositionsList is null
     */
    public List<NonBasetypeLinkingPosition> getValidLinkingPositions() throws ResourcesDbException {
        if(this.validLinkingPositionsList == null) {
            throw new ResourcesDbException("No valid linking positions set for " + this.getName() + ".");
        }
        return this.validLinkingPositionsList;
    }

    /**
     * @param validLinkingPositions the validLinkingPositions to set
     */
    public void setValidLinkingPositions(List<NonBasetypeLinkingPosition> validLinkingPositions) {
        this.validLinkingPositionsList = validLinkingPositions;
    }
    
    public void addValidLinkingPosition(int position, Atom linkAtom, Atom replacedAtom, Double bo, LinkageType defaultLinktype) {
        if(this.validLinkingPositionsList == null) {
            this.validLinkingPositionsList = new ArrayList<NonBasetypeLinkingPosition>();
        }
        this.validLinkingPositionsList.add(new NonBasetypeLinkingPosition(position, linkAtom, replacedAtom, bo, defaultLinktype));
    }
    
    public boolean isValidLinkingPosition(int position) throws ResourcesDbException {
        return(this.getValidLinkingPosition(position) != null);
    }
    
    public NonBasetypeLinkingPosition getValidLinkingPosition(int position) {
        try {
            for(NonBasetypeLinkingPosition posObj : this.getValidLinkingPositions()) {
                if(posObj.getPosition() == position) {
                    return(posObj);
                }
            }
        } catch (ResourcesDbException e) {
            return null;
        }
        return null;
    }
    
    public Atom getLinkingAtom(int position) throws ResourcesDbException {
        try {
            NonBasetypeLinkingPosition linkpos = this.getValidLinkingPosition(position);
            if(linkpos == null) {
                throw new ResourcesDbException(position + " is not a valid linking position for " + this.getName());
            }
            Atom a = linkpos.getLinkedAtom();
            return(a);
        } catch(NullPointerException ne) {
            ResourcesDbException me = new ResourcesDbException("cannot get linking atom " + position + " for " + this.getName());
            me.initCause(ne);
            throw me;
        }
    }

    public Atom getDefaultLinkingAtom1() throws ResourcesDbException {
        return this.getLinkingAtom(this.getDefaultLinkingPosition1());
    }

    /**
     * @return the linkingAtom2
     */
    public Atom getDefaultLinkingAtom2() throws ResourcesDbException {
        return this.getLinkingAtom(this.getDefaultLinkingPosition2());
    }
    
    public Atom getReplacedAtom(int position) throws ResourcesDbException {
        try {
            NonBasetypeLinkingPosition linkpos = this.getValidLinkingPosition(position);
            if(linkpos == null) {
                throw new ResourcesDbException(position + " is not a valid linking position for " + this.getName());
            }
            Atom a = linkpos.getReplacedAtom();
            return(a);
        } catch(NullPointerException ne) {
            ResourcesDbException me = new ResourcesDbException("cannot get linkage replaced atom " + position + " for " + this.getName());
            me.initCause(ne);
            throw me;
        }
    }
    
    public double getBondOrder(int position) throws ResourcesDbException {
        try {
            NonBasetypeLinkingPosition linkpos = this.getValidLinkingPosition(position);
            if(linkpos == null) {
                throw new ResourcesDbException(position + " is not a valid linking position for " + this.getName());
            }
            return(this.getValidLinkingPosition(position).getBondOrder().doubleValue());
        } catch(NullPointerException ne) {
            throw new ResourcesDbException("cannot get bond order of linkage position " + position + " for " + this.getName());
        }
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public boolean isFuzzy() {
        return this.fuzzy;
    }

    public void setFuzzy(boolean fuzzyflag) {
        this.fuzzy = fuzzyflag;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        super.init();
        this.setDefaultLinkingPosition1(0);
        this.setDefaultLinkingPosition2(0);
        this.setValence(0);
        this.setValidLinkingPositions(new ArrayList<NonBasetypeLinkingPosition>());
        this.setComments(null);
        this.setFuzzy(false);
    }
    
    public String toString() {
        String outStr = "";
        outStr += "Name: " + this.getName() + "\n";
        outStr += "Valence: min " + this.getMinValence() + " max " + this.getMaxValence() + "\n";
        try {
            outStr += "Linking Atom: " + this.getDefaultLinkingAtom1() + " [bond order " + this.getDefaultBondOrder1() + ", default linkage position: " + this.getDefaultLinkingPosition1() + "]\n";
        } catch (ResourcesDbException me) {
            outStr += "Linking Atom: not defined";
        }
        if(this.getMaxValence() == 2) {
            try {
                outStr += "Linking Atom2: " + this.getDefaultLinkingAtom2() + " [bond order " + this.getDefaultBondOrder2() + ", default linkage position: " + this.getDefaultLinkingPosition2() + "]\n";
            } catch (ResourcesDbException me) {
                outStr += "Linking Atom2: not defined";
            }
        }
        return(outStr);
    }

}
