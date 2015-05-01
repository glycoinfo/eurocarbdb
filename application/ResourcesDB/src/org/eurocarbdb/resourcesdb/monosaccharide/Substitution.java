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

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.atom.Atom;
import org.eurocarbdb.resourcesdb.fragment.PersubstitutionType;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.io.SubstituentExchangeObject;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplateContainer;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;
import org.eurocarbdb.resourcesdb.util.Utils;

public class Substitution extends Modification implements Cloneable {

    private SubstituentTemplate template = null;
    
    private LinkageType linkagetype1 = null;
    private LinkageType linkagetype2 = null;
    
    private LinkageType sourceLinkagetype1 = null;
    private LinkageType sourceLinkagetype2 = null;
    
    private ArrayList<Integer> substituentPosition1 = new ArrayList<Integer>();
    private ArrayList<Integer> substituentPosition2 = new ArrayList<Integer>();
    
    private int dbId;
    
    private boolean hasSeparateDisplayPart = false;
    
    //private SubstituentTemplateContainer substContainer = null;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /** 
     * Constructor for a monovalent substitution
     * The linkage type and substituent position are set according to the default values in the SubstituentTemplate
     * @param name Name of the substituent
     * @param position1 Linking position at the monosaccharide basetype
     * @throws ResourcesDbException in case "name" is not a valid substituent name
     */
    public Substitution(String name, int position1) throws ResourcesDbException {
        this(name, position1, null);
    }
    
    /** 
     * Constructor for a monovalent substitution
     * The linkage type and substituent position are set according to the default values in the SubstituentTemplate
     * @param name Name of the substituent
     * @param position1 Linking position at the monosaccharide basetype
     * @throws ResourcesDbException in case "name" is not a valid substituent name
     */
    public Substitution(String name, int position1, TemplateContainer cont) throws ResourcesDbException {
        this.setTemplateContainer(cont);
        this.setTemplate(this.getSubstContainer().forName(GlycanNamescheme.GLYCOCT, name));
        if(this.getTemplate() == null) {
            throw new ResourcesDbException("Unknown substituent name: " + name);
        }
        this.setName(getTemplate().getName());
        this.setPosition1(position1);
        this.setSubstituentPosition1(this.getTemplate().getDefaultLinkingPosition1());
        this.setLinkagetype1(this.getTemplate().getDefaultLinkagetype1());
    }
    
    /** 
     * Constructor for a monovalent substitution
     * @param name Name of the substituent
     * @param position1 Linking position at the monosaccharide basetype
     * @param linktype1 Linkage type, by which the substituent is linked to the basetype
     * @param substPosition1 Linking position at the substituent
     * @throws ResourcesDbException in case "name" is not a valid substituent name
     */
    public Substitution(String name, int position1, LinkageType linktype1, int substPosition1) throws ResourcesDbException {
        this(name, position1, linktype1, substPosition1, null);
    }
    
    /** 
     * Constructor for a monovalent substitution
     * @param name Name of the substituent
     * @param position1 Linking position at the monosaccharide basetype
     * @param linktype1 Linkage type, by which the substituent is linked to the basetype
     * @param substPosition1 Linking position at the substituent
     * @throws ResourcesDbException in case "name" is not a valid substituent name
     */
    public Substitution(String name, int position1, LinkageType linktype1, int substPosition1, TemplateContainer cont) throws ResourcesDbException {
        this.setTemplateContainer(cont);
        this.setTemplate(this.getSubstContainer().forName(GlycanNamescheme.GLYCOCT, name));
        if(this.getTemplate() == null) {
            throw new ResourcesDbException("Unknown substituent name: " + name);
        }
        this.setName(this.getTemplate().getName());
        this.setPosition1(position1);
        this.setSubstituentPosition1(substPosition1);
        this.setLinkagetype1(linktype1);
    }
    
    /** 
     * Constructor for a divalent substitution
     * The linkage types and substituent positions are set according to the default values in the SubstituentTemplate
     * @param name Name of the substituent
     * @param position1 Linking position 1 at the monosaccharide basetype
     * @param position2 Linking position 2 at the monosaccharide basetype
     * @throws ResourcesDbException in case "name" is not a valid substituent name or "name" marks a monovalent substituent
     */
    public Substitution(String name, int position1, int position2) throws ResourcesDbException {
        this(name, position1, position2, null);
    }
    
    /** 
     * Constructor for a divalent substitution
     * The linkage types and substituent positions are set according to the default values in the SubstituentTemplate
     * @param name Name of the substituent
     * @param position1 Linking position 1 at the monosaccharide basetype
     * @param position2 Linking position 2 at the monosaccharide basetype
     * @throws ResourcesDbException in case "name" is not a valid substituent name or "name" marks a monovalent substituent
     */
    public Substitution(String name, int position1, int position2, TemplateContainer cont) throws ResourcesDbException {
        this.setTemplateContainer(cont);
        this.setTemplate(this.getSubstContainer().forName(GlycanNamescheme.GLYCOCT, name));
        if(this.getTemplate() == null) {
            throw new MonosaccharideException("Unknown substituent name: " + name);
        }
        if(this.getTemplate().getMaxValence() != 2) {
            throw new MonosaccharideException("Cannot handle " + this.getTemplate().getName() + " as a divalent substituent");
        }
        this.setPosition1(position1);
        this.addSubstituentPosition1(this.getTemplate().getDefaultLinkingPosition1());
        this.setLinkagetype1(this.getTemplate().getDefaultLinkagetype1());
        this.setPosition2(position2);
        this.addSubstituentPosition2(this.getTemplate().getDefaultLinkingPosition2());
        this.setLinkagetype2(this.getTemplate().getDefaultLinkagetype2());
    }
    
    /** 
     * Constructor for a divalent substitution
     * @param name Name of the substituent
     * @param position1 Linking position 1 at the monosaccharide basetype
     * @param linktype1 Linkage type 1, by which the substituent is linked to the basetype
     * @param substPosition1 Linking position 1 at the substituent
     * @param position2 Linking position 2 at the monosaccharide basetype
     * @param linktype2 Linkage type 2, by which the substituent is linked to the basetype
     * @param substPosition2 Linking position 2 at the substituent
     * @throws ResourcesDbException in case "name" is not a valid substituent name
     */
    public Substitution(String name, int position1, LinkageType linktype1, int substPosition1, int position2, LinkageType linktype2, int substPosition2) throws ResourcesDbException {
        this(name, position1, linktype1, substPosition1, position2, linktype2, substPosition2, null, null);
    }
    
    /** 
     * Constructor for a divalent substitution
     * @param name Name of the substituent
     * @param position1 Linking position 1 at the monosaccharide basetype
     * @param linktype1 Linkage type 1, by which the substituent is linked to the basetype
     * @param substPosition1 Linking position 1 at the substituent
     * @param position2 Linking position 2 at the monosaccharide basetype
     * @param linktype2 Linkage type 2, by which the substituent is linked to the basetype
     * @param substPosition2 Linking position 2 at the substituent
     * @throws ResourcesDbException in case "name" is not a valid substituent name
     */
    public Substitution(String name, int position1, LinkageType linktype1, int substPosition1, int position2, LinkageType linktype2, int substPosition2, Config conf, TemplateContainer cont) throws ResourcesDbException {
        this.setConfig(conf);
        this.setTemplateContainer(cont);
        this.setTemplate(this.getSubstContainer().forName(GlycanNamescheme.GLYCOCT, name));
        if(this.getTemplate() == null) {
            throw new MonosaccharideException("Unknown substituent name: " + name);
        }
        if(this.getTemplate().getMaxValence() != 2) {
            throw new MonosaccharideException("Cannot handle " + this.getTemplate().getName() + " as a divalent substituent");
        }
        this.setName(this.getTemplate().getName());
        this.setPosition1(position1);
        this.setSubstituentPosition1(substPosition1);
        this.setLinkagetype1(linktype1);
        this.setPosition2(position2);
        this.setSubstituentPosition2(substPosition2);
        this.setLinkagetype2(linktype2);
    }
    
    /**
     * Constructor to generate a substitution object from a SubstituentExchangeObject
     * @param exchSubst
     * @throws ResourcesDbException
     */
    public Substitution(SubstituentExchangeObject exchSubst) throws ResourcesDbException {
        this(exchSubst, exchSubst.getConfig(), exchSubst.getTemplateContainer());
    }
    
    /**
     * Constructor to generate a substitution object from a SubstituentExchangeObject
     * @param exchSubst
     * @throws ResourcesDbException
     */
    public Substitution(SubstituentExchangeObject exchSubst, TemplateContainer cont) throws ResourcesDbException {
        this(exchSubst, exchSubst.getConfig(), cont);
    }
    
    public Substitution(SubstituentExchangeObject exchSubst, Config conf, TemplateContainer cont) throws ResourcesDbException {
        this(exchSubst, exchSubst.getNamescheme(), conf, cont);
    }
    
    /**
     * Constructor to generate a substitution object from a SubstituentExchangeObject
     * @param exchSubst
     * @throws ResourcesDbException
     */
    public Substitution(SubstituentExchangeObject exchSubst, GlycanNamescheme sourceScheme, Config conf, TemplateContainer cont) throws ResourcesDbException {
        this.setTemplateContainer(cont);
        this.setConfig(conf);
        this.setTemplate(this.getSubstContainer().forName(sourceScheme, exchSubst.getName()));
        if(this.getTemplate() == null) {
            throw new MonosaccharideException("cannot get template for substituent " + exchSubst.getName());
        }
        this.setName(this.getTemplate().getName());
        this.setLinkagetype1(exchSubst.getLinkagetype1());
        this.setLinkagetype2(exchSubst.getLinkagetype2());
        this.setSourceLinkagetype1(exchSubst.getLinkagetype1());
        this.setSourceLinkagetype2(exchSubst.getLinkagetype2());
        this.setSubstituentPosition1(exchSubst.getSubstituentPosition1());
        this.setSubstituentPosition2(exchSubst.getSubstituentPosition2());
        this.setPosition1(exchSubst.getPosition1());
        this.setPosition2(exchSubst.getPosition2());
        this.setSourceName(exchSubst.getName());
        if(this.getPosition2() != null && this.getPosition2().size() > 0) {
            this.setValence(2);
        } else {
            this.setValence(1);
        }
    }
    
    /**
     * Constructor to generate an empty substitution object
     */
    public Substitution() {
        this((TemplateContainer)null);
    }
    
    /**
     * Constructor to generate an empty substitution object with a TemplateContainer
     */
    public Substitution(TemplateContainer cont) {
        this.setTemplateContainer(cont);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public void setName(String name) {
        super.setName(name);
        if(name == null) {
            this.setTemplate(null);
        } else if(getTemplate() == null || !getTemplate().getName().equals(name)) {
            try {
                this.setTemplate(this.getSubstContainer().forName(GlycanNamescheme.GLYCOCT, name));
            } catch(ResourcesDbException rex) {
                if(this.getConfig().isPrintErrorMsgs()) {
                    System.err.println("exception in Substitution.setName(): " + rex);
                }
                this.setTemplate(null);
            }
        }
    }
    
    public void setNameWithoutTemplateAdjustment(String name) {
        super.setName(name);
    }
    
    /**
     * Get the substitution template
     * @return the substituent template
     */
    public SubstituentTemplate getTemplate() {
        /*if(this.template == null) {
            if(this.getName() != null) {
                SubstituentTemplate substTmpl = new SubstituentTemplateContainer().forName(GlycanNamescheme.GLYCOCT, this.getName());
                return(substTmpl);
            }
        }*/
        return this.template;
    }

    /**
     * Set the substitution template
     * Additionally, the modification name is set to the name given in the template (or to null if the template is null).
     * @param template: The template to set
     */
    public void setTemplate(SubstituentTemplate template) {
        this.template = template;
        if(template == null) {
            super.setName(null);
        } else {
            super.setName(template.getName());
        }
    }
    
    /**
     * Get the primary alias name for the substituent in a given name scheme
     * @param scheme: the name scheme
     * @return the primary alias in the namescheme
     * @throws ResourcesDbException in case no primary alias is set for the given name scheme and the linkage type of this substituent
     */
    public SubstituentAlias getName(GlycanNamescheme scheme) throws ResourcesDbException {
        return(this.template.getPrimaryAlias(scheme, this.getLinkagetype1()));
    }

    /**
     * Get the linkage type of the first linkage of the substituent to the base type
     * @return the linkage type
     */
    public LinkageType getLinkagetype1() {
        return this.linkagetype1;
    }

    public String getLinkagetypeStr1() {
        return this.getLinkagetype1().name();
    }

    /**
     * Set the linkage type of the first linkage of the substituent to the base type
     * @param linkagetype the linkagetype to set
     */
    public void setLinkagetype1(LinkageType linkagetype) {
        this.linkagetype1 = linkagetype;
    }

    public void setLinkagetypeStr1(String linktypeStr) {
        this.linkagetype1 = SubstituentTemplate.getLinkageTypeByLinkageName(linktypeStr);
    }
    
    /**
     * Get the linkage type of the second linkage of the divalent substituent to the base type
     * @return the linkagetype
     */
    public LinkageType getLinkagetype2() {
        return this.linkagetype2;
    }
    
    public String getLinkagetypeStr2() {
        if(this.getLinkagetype2() == null) {
            return(null);
        }
        return this.getLinkagetype2().name();
    }

    /**
     * Set the linkage type of the second linkage of the divalent substituent to the base type
     * @param linkagetype the linkagetype to set
     */
    public void setLinkagetype2(LinkageType linkagetype) {
        this.linkagetype2 = linkagetype;
    }
    
    public void setLinkagetypeStr2(String linktypeStr) {
        this.linkagetype2 = SubstituentTemplate.getLinkageTypeByLinkageName(linktypeStr);
    }
    
    /**
     * Get the linkage type by which the substituent is linked to the base type at a given position
     * If the position is contained in the list of possible positions for both the first and the second linkage of a divalent substituent, the first linkage type is returned.
     * @param pos: the base type position
     * @return the linkage type (or null if the substituent is not linked to the given position
     */
    public LinkageType getLinkagetypeByPosition(int pos) {
        for(Integer posInt : getPosition1()) {
            if(posInt.intValue() == pos) {
                return(getLinkagetype1());
            }
        }
        for(Integer posInt : getPosition2()) {
            if(posInt.intValue() == pos) {
                return(getLinkagetype2());
            }
        }
         return(null);
    }

    public LinkageType getSourceLinkagetype1() {
        return sourceLinkagetype1;
    }

    public void setSourceLinkagetype1(LinkageType sourceLinkagetype1) {
        this.sourceLinkagetype1 = sourceLinkagetype1;
    }

    public LinkageType getSourceLinkagetype2() {
        return sourceLinkagetype2;
    }

    public void setSourceLinkagetype2(LinkageType sourceLinkagetype2) {
        this.sourceLinkagetype2 = sourceLinkagetype2;
    }

    /**
     * @return the substituentPosition1
     */
    public ArrayList<Integer> getSubstituentPosition1() {
        return this.substituentPosition1;
    }
    
    public int getIntValueSubstituentPosition1() {
        if(getSubstituentPosition1().size() == 0) {
            return(Substitution.EMPTYPOSITIONVALUE);
        } else if(getSubstituentPosition1().size() == 1) {
            return(getSubstituentPosition1().get(0).intValue());
        } else {
            return(0);
        }
    }

    /**
     * Wrapper method, only needed for Hibernate access.
     * Use setSubstituentPosition1(int position) instead for other purposes than Hibernate.
     * @param position the substituent position1 to set
     * @throws MonosaccharideException
     */
    public void setIntValueSubstituentPosition1(int position) throws MonosaccharideException {
        setSubstituentPosition1(position);
    }
    
    /**
     * Get a clone of the substituent position 1 List
     * @return a clone of substituentPosition1
     */
    public ArrayList<Integer> getSubstituentPosition1Clone() {
        return Utils.cloneIntegerList(this.substituentPosition1);
    }

    public String getSubstituentPosition1Str(String delimiter, String unknownPositionLabel) {
        return Utils.formatPositionsString(this.getSubstituentPosition1(), delimiter, unknownPositionLabel);
    }

    /**
     * @param substituentPosition1 the substituentPosition1 to set
     */
    public void setSubstituentPosition1(ArrayList<Integer> substituentPosition1) {
        this.substituentPosition1 = substituentPosition1;
    }
    
    public void setSubstituentPosition1(int position1) {
        if(getSubstituentPosition1() == null) {
            this.setSubstituentPosition1(new ArrayList<Integer>());
        } else {
            getSubstituentPosition1().clear();
        }
        if(position1 != Modification.EMPTYPOSITIONVALUE) {
            addSubstituentPosition1(position1);
        }
    }
    
    /**
     * @param position
     */
    public void addSubstituentPosition1(int position) {
        getSubstituentPosition1().add(position);
    }

    /**
     * @return the substituentPosition2
     */
    public ArrayList<Integer> getSubstituentPosition2() {
        return this.substituentPosition2;
    }

    public int getIntValueSubstituentPosition2() {
        if(getSubstituentPosition2().size() == 0) {
            return(Substitution.EMPTYPOSITIONVALUE);
        } else if(getSubstituentPosition2().size() == 1) {
            return(getSubstituentPosition2().get(0).intValue());
        } else {
            return(0);
        }
    }

    /**
     * Wrapper method, only needed for Hibernate access.
     * Use setSubstituentPosition2(int position) instead for other purposes than Hibernate.
     * @param position the substituent position2 to set
     * @throws MonosaccharideException
     */
    public void setIntValueSubstituentPosition2(int position) throws MonosaccharideException {
        setSubstituentPosition2(position);
    }
    
    /**
     * Get a clone of the substituent position 2 List
     * @return a clone of substituentPosition2
     */
    public ArrayList<Integer> getSubstituentPosition2Clone() {
        return Utils.cloneIntegerList(this.substituentPosition2);
    }

    public String getSubstitutentPosition2Str(String delimiter, String unknownPositionLabel) {
        return Utils.formatPositionsString(this.getSubstituentPosition2(), delimiter, unknownPositionLabel);
    }

    /**
     * @param substituentPosition2 the substituentPosition2 to set
     */
    public void setSubstituentPosition2(ArrayList<Integer> substituentPosition2) {
        this.substituentPosition2 = substituentPosition2;
    }

    public void setSubstituentPosition2(int position2) {
        if(getSubstituentPosition2() == null) {
            this.setSubstituentPosition2(new ArrayList<Integer>());
        } else {
            getSubstituentPosition2().clear();
        }
        if(position2 != Modification.EMPTYPOSITIONVALUE) {
            addSubstituentPosition2(position2);
        }
    }
    
    /**
     * @param position
     */
    public void addSubstituentPosition2(int position) {
        getSubstituentPosition2().add(position);
    }
    
    /**
     * Check, if the substitution has an uncertain linkage position at the substituent.
     * @return true, if a substituent position is 0 or has more than one possible value
     */
    public boolean hasUncertainSubstituentPosition() {
        if(this.getIntValueSubstituentPosition1() == 0) {
            return(true);
        }
        if(this.hasPosition2() && this.getIntValueSubstituentPosition2() == 0) {
            return(true);
        }
        return(false);
    }
    
    /**
     * Get the substituent atom that is linked to the basetype
     * @return the linked atom
     * @throws ResourcesDbException
     */
    public Atom getLinkingAtom1() throws ResourcesDbException {
        return this.getTemplate().getLinkingAtom(this.getIntValueSubstituentPosition1());
    }

    /**
     * Get the substituent atom that is linked to the basetype in the second linkage of a divalent substitution
     * @return the linked atom
     * @throws ResourcesDbException
     */
    public Atom getLinkingAtom2() throws ResourcesDbException {
        return this.getTemplate().getLinkingAtom(this.getIntValueSubstituentPosition2());
    }
    
    public double getBondOrder1() throws ResourcesDbException {
        //try {
            return this.getTemplate().getBondOrder(this.getIntValueSubstituentPosition1());
        //} catch (ResourcesDbException e) {
        //    e.printStackTrace();
        //    return(0.0);
        //}
    }

    public double getBondOrder2() throws ResourcesDbException {
        //try {
            return this.getTemplate().getBondOrder(this.getIntValueSubstituentPosition2());
        //} catch (ResourcesDbException e) {
        //    e.printStackTrace();
        //    return(0.0);
        //}
    }
    
    public Atom getReplacedAtom1() {
        try {
            return(this.getTemplate().getReplacedAtom(this.getIntValueSubstituentPosition1()));
        } catch (ResourcesDbException e) {
            return(null);
        }
    }

    public Atom getReplacedAtom2() {
        if(!this.hasPosition2()) {
            return(null);
        }
        try {
            return(this.getTemplate().getReplacedAtom(this.getIntValueSubstituentPosition2()));
        } catch (ResourcesDbException e) {
            return(null);
        }
    }

    public void setSubstitution(SubstituentTemplate substTemplate, int position) throws MonosaccharideException {
        /*if(substTemplate == null) {
            setName(null);
            setSubstituentPosition1(new ArrayList<Integer>());
            setLinkagetype1((LinkageType)null);
        } else {
            setName(substTemplate.getName());
            setSubstituentPosition1(substTemplate.getDefaultLinkingPosition1());
            setLinkagetype1(getTemplate().getDefaultLinkagetype1());
        }
        setTemplate(substTemplate);
        setPosition1(position);
        setValence(1);*/
        if(substTemplate == null) {
            this.setSubstitution(null, position, null);
        } else {
            this.setSubstitution(substTemplate, position, substTemplate.getDefaultLinkagetype1());
        }
    }
    
    public void setSubstitution(SubstituentTemplate substTemplate, int position, LinkageType linktype) throws MonosaccharideException {
        if(substTemplate == null) {
            this.setSubstitution(null, position, linktype, 0);
        } else {
            this.setSubstitution(substTemplate, position, linktype, substTemplate.getDefaultLinkingPosition1());
        }
    }
    
    public void setSubstitution(SubstituentTemplate substTemplate, int position, LinkageType linktype, int substPosition) throws MonosaccharideException {
        if(substTemplate == null) {
            setName(null);
        } else {
            setName(substTemplate.getName());
        }
        setTemplate(substTemplate);
        setPosition1(position);
        setSubstituentPosition1(substPosition);
        setLinkagetype1(linktype);
        setValence(1);
    }
    
    public void setDivalentSubstitution(SubstituentTemplate substTemplate, int position1, int position2) throws MonosaccharideException {
        setName(substTemplate.getName());
        setTemplate(substTemplate);
        if(position1 > position2 && position2 != 0) {
            int tmpPosition = position1;
            position1 = position2;
            position2 = tmpPosition;
        }
        setPosition1(position1);
        setPosition2(position2);
        setValence(2);
        setSubstituentPosition1(substTemplate.getDefaultLinkingPosition1());
        setLinkagetype1(getTemplate().getDefaultLinkagetype1());
        setSubstituentPosition2(substTemplate.getDefaultLinkingPosition2());
        setLinkagetype2(getTemplate().getDefaultLinkagetype2());
    }
    
    public void setDivalentSubstitution(SubstituentTemplate substTemplate, int position1, LinkageType linktype1, int substPosition1, int position2, LinkageType linktype2, int substPosition2) throws MonosaccharideException {
        setName(substTemplate.getName());
        setTemplate(substTemplate);
        if(position1 > position2 && position2 != 0) {
            int tmpPosition = position1;
            position1 = position2;
            position2 = tmpPosition;
        }
        setPosition1(position1);
        setPosition2(position2);
        setValence(2);
        setSubstituentPosition1(substPosition1);
        setLinkagetype1(linktype1);
        setSubstituentPosition2(substPosition2);
        setLinkagetype2(linktype2);
    }
    
    /** 
     * change the substituent type
     * This method is used to change e.g. a substituent included in a trivial name (like the "5N" in Neu) to a different substituent type (e.g. to "NAc" in NeuAc)
     * @param substTemplate
     * @throws MonosaccharideException (in case the valence of the existing modification does not match that of the new one)
     */
    public void alterSubstituentTemplate(SubstituentTemplate substTemplate) throws MonosaccharideException {
        if(substTemplate.getMaxValence() < getValence() || substTemplate.getMinValence() > getValence()) {
            throw new MonosaccharideException("Valence mismatch when altering substituent");
        }
        setTemplate(substTemplate);
    }
    
    public String getResidueIncludedName(GlycanNamescheme scheme) throws ResourcesDbException {
        String name = this.getTemplate().getResidueIncludedName(scheme, this.getLinkagetype1());
        return(name);
    }

    public String getSeparateDisplay(GlycanNamescheme scheme) throws ResourcesDbException {
        return(this.getTemplate().getSeparateDisplay(scheme, this.getLinkagetype1()));
    }
    
    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public SubstituentTemplateContainer getSubstContainer() {
        /*if(this.substContainer == null) {
            this.substContainer = super.getTemplateContainer().getSubstituentTemplateContainer();
        }*/
        return this.getTemplateContainer().getSubstituentTemplateContainer();
    }
    
    /*public void setSubstContainer(SubstituentTemplateContainer container) {
        this.substContainer = container;
    }*/
    
    public boolean isHasSeparateDisplayPart() {
        return hasSeparateDisplayPart;
    }

    public void setHasSeparateDisplayPart(boolean hasSeparateDisplayPart) {
        this.hasSeparateDisplayPart = hasSeparateDisplayPart;
    }
    
    public void setDefaultLinkageDataFromTemplate(GlycanNamescheme scheme, SubstituentTemplate tmpl) {
        setLinkagetype1(template.getDefaultLinkagetype1());
        setLinkagetype2(template.getDefaultLinkagetype2());
        getSubstituentPosition1().clear();
        addSubstituentPosition1(template.getDefaultLinkingPosition1());
        getSubstituentPosition2().clear();
        addSubstituentPosition2(template.getDefaultLinkingPosition2());
    }

    //*****************************************************************************
    //*** mass related methods: ***************************************************
    //*****************************************************************************
    
    public double getMonoMass(PersubstitutionType persubst) {
        //TODO: determine correct value
        return 0.0;
    }
    
    public double getAvgMass(PersubstitutionType persubst) {
        //TODO: determine correct value
        return 0.0;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        super.init();
        this.setLinkagetype1(null);
        this.setLinkagetype2(null);
        this.setDbId(0);
        this.setSubstituentPosition1(new ArrayList<Integer>());
        this.setSubstituentPosition2(new ArrayList<Integer>());
        this.setTemplate(null);
    }
    
    public Substitution clone() {
        Substitution substClone = new Substitution();
        substClone.setName(this.getName());
        substClone.setTemplate(this.getTemplate());
        substClone.setValence(this.getValence());
        substClone.setPosition1(this.getPosition1Clone());
        substClone.setPosition2(this.getPosition2Clone());
        substClone.setLinkagetype1(this.getLinkagetype1());
        substClone.setLinkagetype2(this.getLinkagetype1());
        substClone.setSubstituentPosition1(this.getSubstituentPosition1Clone());
        substClone.setSubstituentPosition2(this.getSubstituentPosition2Clone());
        substClone.setTemplateContainer(this.getTemplateContainer());
        return(substClone);
    }
    
    public String toString() {
        //*** get name and position(s) from Modification.toString(), and add LinkageType info in front of terminal square bracket: ***
        String outStr = super.toString();
        outStr = outStr.substring(0, outStr.length() - 1);
        outStr += "; Linkage: " + this.getLinkagetype1() + this.getSubstituentPosition1();
        if(this.hasPosition2()) {
            outStr += "/" + this.getLinkagetype2() + this.getSubstituentPosition2();
        }
        outStr += "]";
        return(outStr);
    }
    
}
