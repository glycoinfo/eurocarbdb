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
package org.eurocarbdb.resourcesdb.io;

import java.util.ArrayList;

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbObject;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.monosaccharide.Substitution;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;

/** 
* Data storage class used to store substituents in the MonosaccharideExchangeObject
* @author Thomas Luetteke
*
*/
public class SubstituentExchangeObject extends ResourcesDbObject {

    private String name = null;
    private String originalName = null;
    
    private ArrayList<Integer> position1;
    private ArrayList<Integer> position2;
    private ArrayList<Integer> position3;
    
    private LinkageType linkagetype1 = null;
    private LinkageType linkagetype2 = null;
    private LinkageType linkagetype3 = null;
    
    private LinkageType originalLinkagetype1 = null;
    private LinkageType originalLinkagetype2 = null;
    private LinkageType originalLinkagetype3 = null;
    
    private ArrayList<Integer> substituentPosition1;
    private ArrayList<Integer> substituentPosition2;
    private ArrayList<Integer> substituentPosition3;
    
    private GlycanNamescheme namescheme = null;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public SubstituentExchangeObject(String name, int position1, GlycanNamescheme scheme) {
        this(name, position1, scheme, null, null);
    }
    
    public SubstituentExchangeObject(String name, int position1, GlycanNamescheme scheme, Config conf, TemplateContainer container) {
        this.setConfig(conf);
        this.setTemplateContainer(container);
        this.init();
        this.setNamescheme(scheme);
        this.setName(name);
        this.addPosition1(position1);
    }
    
    public SubstituentExchangeObject(String name, int position1, int position2, GlycanNamescheme scheme) {
        this(name, position1, position2, scheme, null, null);
    }
    
    public SubstituentExchangeObject(String name, int position1, int position2, GlycanNamescheme scheme, Config conf, TemplateContainer container) {
        this.setConfig(conf);
        this.setTemplateContainer(container);
        this.init();
        this.setNamescheme(scheme);
        this.setName(name);
        this.addPosition1(position1);
        this.addPosition2(position2);
    }
    
    public SubstituentExchangeObject(Substitution msdbSubst, GlycanNamescheme scheme) {
        this.setConfig(msdbSubst.getConfig());
        this.setTemplateContainer(msdbSubst.getTemplateContainer());
        this.init();
        this.setNamescheme(scheme);
        this.setName(msdbSubst.getName());
        this.setPosition1(msdbSubst.getPosition1());
        this.setPosition2(msdbSubst.getPosition2());
        this.setLinkagetype1(msdbSubst.getLinkagetype1());
        this.setLinkagetype2(msdbSubst.getLinkagetype2());
        this.setSubstituentPosition1(msdbSubst.getSubstituentPosition1());
        this.setSubstituentPosition2(msdbSubst.getSubstituentPosition2());
        this.setOriginalName(msdbSubst.getSourceName());
        this.setOriginalLinkagetype1(msdbSubst.getSourceLinkagetype1());
        this.setOriginalLinkagetype2(msdbSubst.getSourceLinkagetype2());
    }
    
    public SubstituentExchangeObject(GlycanNamescheme scheme) {
        this(scheme, null, null);
    }
    
    public SubstituentExchangeObject(GlycanNamescheme scheme, Config conf, TemplateContainer container) {
        this.init();
        this.setConfig(conf);
        this.setTemplateContainer(container);
        this.setNamescheme(scheme);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public void setDefaultDataFromTemplate(GlycanNamescheme scheme, SubstituentTemplate template) {
        this.setNamescheme(scheme);
        this.setLinkagetype1(template.getDefaultLinkagetype1());
        this.setLinkagetype2(template.getDefaultLinkagetype2());
        this.getSubstituentPosition1().clear();
        this.addSubstituentPosition1(template.getDefaultLinkingPosition1());
        this.getSubstituentPosition2().clear();
        this.addSubstituentPosition2(template.getDefaultLinkingPosition2());
    }
    
    /** 
     * get the name of the substituent
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /** 
     * set the name of the substituent (in Glyco-CT format)
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the substituent name that was used in the source residue
     * @return
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * Set the substituent name that was used in the source residue
     * @param originalName
     */
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    /** 
     * Get the list of possible first attachment positions at the monosaccharide
     * @return the position1
     */
    public ArrayList<Integer> getPosition1() {
        return position1;
    }
    
    /** 
     * Set the list of possible first attachment positions at the monosaccharide
     * @param position the position to set
     */
    public void setPosition1(ArrayList<Integer> position) {
        this.position1 = position;
    }
    
    /** 
     * Add a new first position to the list of possible attachment positions at the monosaccharide
     * @param pos
     */
    public void addPosition1(int pos) {
        if(getPosition1() == null) {
            setPosition1(new ArrayList<Integer>());
        }
        getPosition1().add(new Integer(pos));
    }
    
    /** 
     * Get the list of possible second attachment positions at the monosaccharide 
     * @return the position2
     */
    public ArrayList<Integer> getPosition2() {
        return position2;
    }
    
    /** 
     * Set the list of possible second attachment positions at the monosaccharide
     * @param position2 the position2 to set
     */
    public void setPosition2(ArrayList<Integer> position2) {
        this.position2 = position2;
    }
    
    /** 
     * Add a new second position to the list of possible attachment positions at the monosaccharide
     * This method may (of course) only be used with divalent substituents
     * @param pos
     */
    public void addPosition2(int pos) {
        if(getPosition2() == null) {
            setPosition2(new ArrayList<Integer>());
        }
        getPosition2().add(new Integer(pos));
    }

    /** 
     * Get the list of possible third attachment positions at the monosaccharide
     * @return the position3
     */
    public ArrayList<Integer> getPosition3() {
        return position3;
    }
    
    /** 
     * Set the list of possible third attachment positions at the monosaccharide at the monosaccharide
     * @param position the position to set
     */
    public void setPosition3(ArrayList<Integer> position) {
        this.position3 = position;
    }
    
    /** 
     * Add a new third position to the list of possible attachment positions at the monosaccharide
     * @param pos
     */
    public void addPosition3(int pos) {
        if(getPosition3() == null) {
            setPosition3(new ArrayList<Integer>());
        }
        getPosition3().add(new Integer(pos));
    }
    
    /**
     * Get the linkage type of the first attachment position
     * @return the linkagetype1
     */
    public LinkageType getLinkagetype1() {
        return this.linkagetype1;
    }

    /**
     * Set the linkage type of the first attachment position
     * @param linkagetype1 the linkagetype1 to set
     */
    public void setLinkagetype1(LinkageType linkagetype) {
        this.linkagetype1 = linkagetype;
    }

    /**
     * Get the linkage type of the second attachment position
     * @return the linkagetype2
     */
    public LinkageType getLinkagetype2() {
        return this.linkagetype2;
    }

    /**
     * Set the linkage type of the second attachment position
     * @param linkagetype2 the linkagetype2 to set
     */
    public void setLinkagetype2(LinkageType linkagetype) {
        this.linkagetype2 = linkagetype;
    }

    /**
     * Get the linkage type of the third attachment position
     * @return the linkagetype3
     */
    public LinkageType getLinkagetype3() {
        return this.linkagetype3;
    }

    /**
     * Set the linkage type of the third attachment position
     * @param linkagetype3 the linkagetype3 to set
     */
    public void setLinkagetype3(LinkageType linkagetype) {
        this.linkagetype3 = linkagetype;
    }

    public LinkageType getOriginalLinkagetype1() {
        return this.originalLinkagetype1;
    }

    public void setOriginalLinkagetype1(LinkageType origlinkagetype1) {
        this.originalLinkagetype1 = origlinkagetype1;
    }

    public LinkageType getOriginalLinkagetype2() {
        return this.originalLinkagetype2;
    }

    public void setOriginalLinkagetype2(LinkageType origlinkagetype2) {
        this.originalLinkagetype2 = origlinkagetype2;
    }

    public LinkageType getOriginalLinkagetype3() {
        return this.originalLinkagetype3;
    }

    public void setOriginalLinkagetype3(LinkageType origlinkagetype3) {
        this.originalLinkagetype3 = origlinkagetype3;
    }

    /**
     * Get the list of possible first attachment positions at the substituent 
     * @return the substituentPosition1
     */
    public ArrayList<Integer> getSubstituentPosition1() {
        if(this.substituentPosition1 == null) {
            this.substituentPosition1 = new ArrayList<Integer>();
        }
        return this.substituentPosition1;
    }

    /**
     * Set the list of possible first attachment positions at the substituent 
     * @param substituentPosition1 the substituentPosition1 to set
     */
    public void setSubstituentPosition1(ArrayList<Integer> substituentPosition) {
        this.substituentPosition1 = substituentPosition;
    }
    
    public void setSubstituentPosition1(int substPos1) {
        this.substituentPosition1 = new ArrayList<Integer>();
        this.substituentPosition1.add(substPos1);
    }
    
    /**
     * Add a new first position to the list of possible attachment positions at the substituent
     * @param position the substituent position1 to add
     */
    public void addSubstituentPosition1(int position) {
        if(getSubstituentPosition1() == null) {
            setSubstituentPosition1(new ArrayList<Integer>());
        }
        getSubstituentPosition1().add(new Integer(position));
    }

    /**
     * Get the list of possible second attachment positions at the substituent 
     * @return the substituentPosition2
     */
    public ArrayList<Integer> getSubstituentPosition2() {
        if(this.substituentPosition2 == null) {
            this.substituentPosition2 = new ArrayList<Integer>();
        }
        return this.substituentPosition2;
    }

    /**
     * Set the list of possible second attachment positions at the substituent 
     * @param substituentPosition2 the substituentPosition2 to set
     */
    public void setSubstituentPosition2(ArrayList<Integer> substituentPosition) {
        this.substituentPosition2 = substituentPosition;
    }
    
    public void setSubstituentPosition2(int substPos2) {
        this.substituentPosition2 = new ArrayList<Integer>();
        this.substituentPosition2.add(substPos2);
    }
    
    /**
     * Add a new second position to the list of possible attachment positions at the substituent
     * @param position the substituent position2 to add
     */
    public void addSubstituentPosition2(int position) {
        if(getSubstituentPosition2() == null) {
            setSubstituentPosition2(new ArrayList<Integer>());
        }
        getSubstituentPosition2().add(new Integer(position));
    }
    
    /**
     * Get the list of possible third attachment positions at the substituent 
     * @return the substituentPosition3
     */
    public ArrayList<Integer> getSubstituentPosition3() {
        if(this.substituentPosition3 == null) {
            this.substituentPosition3 = new ArrayList<Integer>();
        }
        return this.substituentPosition3;
    }

    /**
     * Set the list of possible third attachment positions at the substituent 
     * @param substituentPosition3 the substituentPosition3 to set
     */
    public void setSubstituentPosition3(ArrayList<Integer> substituentPosition) {
        this.substituentPosition3 = substituentPosition;
    }
    
    /**
     * Add a new third position to the list of possible attachment positions at the substituent
     * @param position the substituent position3 to add
     */
    public void addSubstituentPosition3(int position) {
        if(getSubstituentPosition3() == null) {
            setSubstituentPosition3(new ArrayList<Integer>());
        }
        getSubstituentPosition3().add(new Integer(position));
    }

    public GlycanNamescheme getNamescheme() {
        return namescheme;
    }

    public void setNamescheme(GlycanNamescheme namescheme) {
        this.namescheme = namescheme;
    }

    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        this.setName(null);
        this.setOriginalName(null);
        this.setPosition1(new ArrayList<Integer>());
        this.setPosition2(new ArrayList<Integer>());
        this.setPosition3(new ArrayList<Integer>());
        this.setSubstituentPosition1(new ArrayList<Integer>());
        this.setSubstituentPosition2(new ArrayList<Integer>());
        this.setSubstituentPosition3(new ArrayList<Integer>());
        this.setLinkagetype1(null);
        this.setLinkagetype2(null);
        this.setLinkagetype3(null);
    }
    
    public String toString() {
        String outStr = "";
        outStr += this.getPosition1().toString();
        outStr += ":";
        outStr += this.getSubstituentPosition1().toString();
        outStr += "(" + this.getLinkagetype1() + ")";
        if(this.getPosition2() != null && this.getPosition2().size() > 0) {
            outStr += ",";
            outStr += this.getPosition2().toString();
            outStr += ":";
            outStr += this.getSubstituentPosition2().toString();
            outStr += "(" + this.getLinkagetype2() + ")";
        }
        outStr += " ";
        outStr += this.getName();
        if(this.getOriginalName() != null) {
            outStr += " (orig: " + this.getOriginalName() + ")";
        }
        return(outStr);
    }

}
