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

import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.ResourcesDbObject;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;

public class SubstituentAlias extends ResourcesDbObject {
    
    private GlycanNamescheme namescheme = null;
    private LinkageType linktype1 = null;
    private LinkageType linktype2 = null;
    private SubstituentTemplate primaryTemplate = null;
    private int substituentPosition1 = -1;
    private int substituentPosition2 = -1;
    private boolean isPrimary = false;
    private String residueIncludedName = null;
    private String separateDisplayName = null;
    private int dbId = 0;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public SubstituentAlias(GlycanNamescheme scheme, LinkageType linktype, String inclName, String sepDispl, boolean primary) {
        this(scheme, linktype, null, inclName, sepDispl, primary, null);
    }
    
    public SubstituentAlias(GlycanNamescheme scheme, LinkageType linktype, LinkageType linktype2, String inclName, String sepDispl, boolean primary) {
        this(scheme, linktype, linktype2, inclName, sepDispl, primary, null);
        this.setNamescheme(scheme);
        this.setLinktype1(linktype);
        this.setLinktype2(linktype2);
        this.setResidueIncludedName(inclName);
        this.setSeparateDisplayName(sepDispl);
        this.setIsPrimary(primary);
    }
    
    public SubstituentAlias(GlycanNamescheme scheme, LinkageType linktype, LinkageType linktype2, String inclName, String sepDispl, boolean primary, TemplateContainer container) {
        this.setNamescheme(scheme);
        this.setLinktype1(linktype);
        this.setLinktype2(linktype2);
        this.setResidueIncludedName(inclName);
        this.setSeparateDisplayName(sepDispl);
        this.setIsPrimary(primary);
        this.setTemplateContainer(container);
    }
    
    public SubstituentAlias(TemplateContainer container) {
        this(null, null, null, null, null, false, container);
    }
    
    public SubstituentAlias() {
        this(null, null, null, null, null, false);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    /**
     * Get the namescheme, for which this alias is defined
     * @return the namescheme
     */
    public GlycanNamescheme getNamescheme() {
        return this.namescheme;
    }
    
    /**
     * Get the name of the namescheme, for which this alias is defined
     * @return the namescheme's name
     */
    public String getNameschemeStr() {
        if(this.getNamescheme() == null) {
            return(null);
        }
        return(this.getNamescheme().name());
    }
    
    /**
     * Set the namescheme, for which this alias is defined, by its name
     * @param name the name of the namescheme to set
     */
    public void setNameschemeStr(String name) {
        if(name == null) {
            this.setNamescheme(null);
        } else {
            this.setNamescheme(GlycanNamescheme.forName(name));
        }
    }
    
    /**
     * Set the namescheme, for which this alias is defined
     * @param namescheme the namescheme to set
     */
    public void setNamescheme(GlycanNamescheme scheme) {
        this.namescheme = scheme;
    }
    
    /**
     * Get the linkageType1 of this alias
     * @return the linktype
     */
    public LinkageType getLinktype1() {
        return this.linktype1;
    }

    /**
     * Set the linkageType1 of this alias
     * @param linktype1 the linktype to set
     */
    public void setLinktype1(LinkageType link) {
        this.linktype1 = link;
    }
    
    /**
     * Get the linkageType2 of this alias
     * @return the linktype
     */
    public LinkageType getLinktype2() {
        return this.linktype2;
    }

    /**
     * Set the linkageType2 of this alias
     * @return the linktype
     */
    public void setLinktype2(LinkageType link2) {
        this.linktype2 = link2;
    }

    /**
     * Get the name of the linkageType1 of this alias
     * @return
     */
    public String getLinktype1Str() {
        if(this.getLinktype1() == null) {
            return(null);
        }
        return(this.getLinktype1().name());
    }
    
    /**
     * Set the linkageType1 of this alias using the linktype name
     * @param linktypeStr the name of the linkageType1
     */
    public void setLinktype1Str(String linktypeStr) {
        if(linktypeStr == null) {
            this.setLinktype1(null);
        } else {
            this.setLinktype1(SubstituentTemplate.getLinkageTypeByLinkageName(linktypeStr));
        }
    }

    /**
     * Get the name of the linkageType2 of this alias
     * @return
     */
    public String getLinktype2Str() {
        if(this.getLinktype2() == null) {
            return(null);
        }
        return(this.getLinktype2().name());
    }
    
    /**
     * Set the linkageType2 of this alias using the linktype name
     * @param linktypeStr the name of the linkageType2
     */
    public void setLinktypeStr2(String linktypeStr) {
        if(linktypeStr == null) {
            this.setLinktype2(null);
        } else {
            this.setLinktype2(SubstituentTemplate.getLinkageTypeByLinkageName(linktypeStr));
        }
    }

    public int getSubstituentPosition1() {
        return this.substituentPosition1;
    }

    public void setSubstituentPosition1(int substPosition1) {
        this.substituentPosition1 = substPosition1;
    }

    public int getSubstituentPosition2() {
        return this.substituentPosition2;
    }

    public void setSubstituentPosition2(int substPosition2) {
        this.substituentPosition2 = substPosition2;
    }

    /**
     * Get the SubstituentTemplate that is encoded by this alias
     * @return the primaryTemplate
     */
    public SubstituentTemplate getPrimaryTemplate() {
        return this.primaryTemplate;
    }
    
    /**
     * Set the SubstituentTemplate that is encoded by this alias
     * @param primaryTemplate the primaryTemplate to set
     */
    public void setPrimaryTemplate(SubstituentTemplate primaryTemplate) {
        this.primaryTemplate = primaryTemplate;
    }
    
    /**
     * Get name of the SubstituentTemplate that is encoded by this alias
     * @return the primaryTemplate's name
     */
    public String getPrimaryTemplateName() {
        if(this.getPrimaryTemplate() == null) {
            return(null);
        }
        return(this.getPrimaryTemplate().getName());
    }
    
    /**
     * Set the primary template by its template name
     * (needed for Hibernate mapping)
     * @param name
     */
    public void setPrimaryTemplateName(String name) throws ResourcesDbException {
        this.setPrimaryTemplate(this.getTemplateContainer().getSubstituentTemplateContainer().forName(this.getNamescheme(), name));
    }
    
    /**
     * Check, if this substituent name is the primary name for the namescheme it is associated with
     * @return true, if this is a primary name; false, if this is an alias name
     */
    public boolean isPrimary() {
        return(this.isPrimary);
    }
    
    /**
     * Check, if this substituent name is the primary name for the namescheme it is associated with.
     * This is an alias for <code>isPrimary()</code>.
     * @return true, if this is a primary name; false, if this is an alias name
     */
    public boolean getIsPrimary() {
        return(this.isPrimary());
    }
    
    public void setIsPrimary(boolean primary) {
        this.isPrimary = primary;
    }
    
    public String getResidueIncludedName() {
        return this.residueIncludedName;
    }

    public void setResidueIncludedName(String residueInclName) {
        this.residueIncludedName = residueInclName;
    }

    public String getSeparateDisplayName() {
        return this.separateDisplayName;
    }

    public void setSeparateDisplayName(String seperateDisplName) {
        this.separateDisplayName = seperateDisplName;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    /**
     * Check, whether this alias is split, i.e. whether it has both a residue included and a separately displayed part
     * @return true, if both <code>residueIncludedName</code> and <code>separateDisplayName</code> are not null
     */
    public boolean isSplit() {
        if(this.getResidueIncludedName() != null && this.getSeparateDisplayName() != null) {
            return true;
        }
        return false;
    }

    public String toString() {
        String outStr = this.getNamescheme().getNameStr() + "::" + this.getLinktype1Str() + "::" + this.getResidueIncludedName() + "/" + this.getSeparateDisplayName();
        if(this.isPrimary()) {
            outStr += " (primary)";
        }
        return(outStr);
    }
    
}
