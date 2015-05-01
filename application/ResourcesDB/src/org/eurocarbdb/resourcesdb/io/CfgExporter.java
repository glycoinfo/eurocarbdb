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

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.template.TrivialnameTemplate;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.*;
import org.eurocarbdb.resourcesdb.util.Utils;

/**
* Exporter (name builder) class for CFG LinearCode residue names
* @author Thomas Luetteke
*
*/
public class CfgExporter extends StandardExporter implements MonosaccharideExporter {

    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public CfgExporter() {
        this(null);
    }
    
    public CfgExporter(Config conf) {
        this(conf, null);
    }
    
    public CfgExporter(Config conf, TemplateContainer container) {
        super(GlycanNamescheme.CFG, conf, container);
    }
    
    //*****************************************************************************
    //*** export methods: *********************************************************
    //*****************************************************************************
    
    /**
     * Generate the name string of a monosaccharide.
     * If a trivial name exists that will be used only if it is the primary alias for the monosaccharide.
     * @param ms the monosaccharide the name of which is generated
     * @return the name string (or null, if no cfg name can be generated for the given monosaccharide)
     * @throws ResourcesDbException
     */
    public String export(Monosaccharide ms) throws ResourcesDbException {
        if(ms.getConfiguration() == null) {
            String stereo = ms.getStereoStrWithoutAnomeric();
            ms.setConfiguration(Stereocode.getConfigurationFromStereoString(stereo));
        }
        if(ms.getConfiguration().equals(StereoConfiguration.Unknown)) {
            return null;
        }
        if(!(ms.getRingtype().equals(Ringtype.PYRANOSE) || ms.getRingtype().equals(Ringtype.FURANOSE))) {
            return null;
        }
        String nameStr = "";
        TrivialnameTemplateContainer container = this.getTemplateContainer().getTrivialnameTemplateContainer();
        TrivialnameTemplate cfgTemplate = container.checkMsForTrivialname(GlycanNamescheme.CFG, ms);
        if(cfgTemplate == null) {
            return null;
        }
        this.setUsedTrivialnameTemplate(cfgTemplate);
        nameStr += cfgTemplate.getPrimaryName(this.getNamescheme());
        if(!ms.getConfiguration().equals(cfgTemplate.getDefaultConfiguration())) {
            if(cfgTemplate.isDefaultConfigIsCompulsory()) {
                return null;
            } else {
                if(ms.getRingEnd() == cfgTemplate.getDefaultRingend()) {
                    nameStr += "'";
                }
            }
        }
        if(ms.getRingStart() != cfgTemplate.getCarbonylPosition()) {
            return null;
        }
        if(ms.getRingEnd() != cfgTemplate.getDefaultRingend()) {
            if(ms.getConfiguration().equals(cfgTemplate.getDefaultConfiguration())) {
                nameStr += "^";
            } else {
                nameStr += "~";
            }
        }
        String substStr = "";
        for(Substitution subst : ms.getSubstitutions()) {
            if(!cfgTemplate.hasSubstitution(subst)) {
                if(substStr.length() > 0) {
                    substStr += ",";
                }
                try {
                    substStr += this.formatSubstitution(subst, cfgTemplate);
                } catch(ResourcesDbException rEx) {
                    //*** substitution cannot be displayed in cfg namescheme ***
                    //System.out.println(rEx);
                    return null;
                }
            }
        }
        if(substStr.length() > 0) {
            nameStr += "[" + substStr + "]";
        }
        nameStr += ms.getAnomer().getCarbbankSymbol();
        return nameStr;
    }
    
    private String formatSubstitution(Substitution subst, TrivialnameTemplate cfgTemplate) throws ResourcesDbException {
        String outStr = "";
        outStr += Utils.formatPositionsString(subst.getPosition1(), "/", "?");
        if(subst.hasPosition2()) {
            outStr += "," + Utils.formatPositionsString(subst.getPosition2(), "/", "?");
        }
        SubstituentTemplate substTmpl = null;
        LinkageType linktype = null;
        //*** check, if cfgTemplate already implies part of the substituent: ***
        for(Substitution cfgTmplSubst : cfgTemplate.getSubstitutions()) {
            if(cfgTmplSubst.positionsEqual(subst)) {
                SubstituentSubpartTreeNode substRoot = subst.getTemplate().getSubparts();
                if(cfgTmplSubst.getTemplate().getName().equals(substRoot.getSubstTmpl(this.getTemplateContainer().getSubstituentTemplateContainer()).getName())) {
                    if(substRoot.getChildCount() == 1) {
                        substTmpl = ((SubstituentSubpartTreeNode)substRoot.getFirstChild()).getSubstTmpl(this.getTemplateContainer().getSubstituentTemplateContainer());
                        linktype = LinkageType.H_AT_OH;
                    }
                }
            }
        }
        //*** try and add substituent name (will throw exception, if no primary alias is defined): ***
        if(substTmpl == null) {
            substTmpl = subst.getTemplate();
            linktype = subst.getLinkagetype1();
        }
        outStr += substTmpl.getPrimaryAlias(GlycanNamescheme.CFG, linktype).getResidueIncludedName();
        return outStr;
    }
    
}
