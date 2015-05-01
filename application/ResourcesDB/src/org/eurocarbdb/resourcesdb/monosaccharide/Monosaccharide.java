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
import java.util.List;

import org.eurocarbdb.resourcesdb.*;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.io.*;
//import org.openscience.cdk.Molecule;
//import org.openscience.cdk.smiles.SmilesGenerator;
import org.eurocarbdb.resourcesdb.representation.*;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplateContainer;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;
import org.eurocarbdb.resourcesdb.template.TrivialnameTemplate;

/**
* Monosaccharide object, stores and handles the properties of a monosaccharide residue (basetype + substituents).
* 
* @author Thomas Luetteke
*/
public class Monosaccharide extends MolecularEntity {
    private Basetype basetype;
    private List<Substitution> substitutions;
//    private Molecule cdkMolecule;
    private List<MonosaccharideSynonym> synonyms;
    private List<MonosaccharideLinkingPosition> possibleLinkingPositions;

    private boolean fuzzy;
    private boolean orientationChanged = false;
    
    private boolean checkPositionsOnTheFly = false;
    
    private int modificationIndex = 0;
    
    private int dbId;
    
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /** 
     * Constructor
     */
    public Monosaccharide() {
        this((Config)null, (TemplateContainer)null);
    }
    
    /** 
     * Constructor
     */
    public Monosaccharide(TemplateContainer container) {
        this((Config)null, container);
    }
    
    /** 
     * Constructor
     */
    public Monosaccharide(Config conf, TemplateContainer container) {
        this.setConfig(conf);
        this.setTemplateContainer(container);
        this.init();
    }
    
    /** 
     * Constructor, initializes the monosaccharide with a base type
     * @param bt the base type of the monosaccharide
     */
    public Monosaccharide(Basetype bt) {
        this.setConfig(bt.getConfig());
        this.setTemplateContainer(bt.getTemplateContainer());
        this.init(bt);
    }
    
    /**
     * Constructor, initializes the monosaccharide using its name
     * @param scheme the notation scheme of the name
     * @param name the monosaccharide name
     */
    public Monosaccharide(GlycanNamescheme scheme, String name) throws ResourcesDbException {
        this(scheme, name, null);
    }
    
    /**
     * Constructor, initializes the monosaccharide using its name
     * @param scheme the notation scheme of the name
     * @param name the monosaccharide name
     * @param container a TemplateContainer to be used for parsing the name
     */
    public Monosaccharide(GlycanNamescheme scheme, String name, TemplateContainer container) throws ResourcesDbException {
        this(scheme, name, container, null);
    }
    
    /**
     * Constructor, initializes the monosaccharide using its name
     * @param scheme the notation scheme of the name
     * @param name the monosaccharide name
     * @param container a TemplateContainer to be used for parsing the name
     * @param conf a configuration object to be stored in this monosaccharide
     */
    public Monosaccharide(GlycanNamescheme scheme, String name, TemplateContainer container, Config conf) throws ResourcesDbException {
        this.setTemplateContainer(container);
        this.setConfig(conf);
        if(GlycanNamescheme.CARBBANK.equals(scheme.getBaseScheme())) {
            CarbbankImporter parser;
            parser = new CarbbankImporter(scheme, this.getConfig(), this.getTemplateContainer());
            parser.parseMsString(name, this);
        } else if(GlycanNamescheme.GLYCOCT.equals(scheme.getBaseScheme())) {
            GlycoCTImporter parser;
            parser = new GlycoCTImporter(scheme, this.getConfig(), this.getTemplateContainer());
            parser.parseMsString(name, this);
        } else if(GlycanNamescheme.BCSDB.equals(scheme.getBaseScheme())) {
            BcsdbImporter parser;
            parser = new BcsdbImporter(this.getConfig(), this.getTemplateContainer());
            parser.parseMsString(name, this);
        } else if(GlycanNamescheme.CFG.equals(scheme.getBaseScheme())) {
            CfgImporter parser;
            parser = new CfgImporter(this.getConfig(), this.getTemplateContainer());
            parser.parseMsString(name, this);
        } else {
            throw new ResourcesDbException("GlycanNamescheme " + scheme.getNameStr() + " not supported in monosaccharide constructor");
        }
    }
    
    //*****************************************************************************
    //*** getters/setters of basetype properties: *********************************
    //*****************************************************************************
    
    /**
     * Get the carbonyl position of the monosaccharide.
     * @return the carbonylPosition
     */
    public int getRingStart() {
        return this.getBasetype().getRingStart();
    }

    /**
     * Set the ring start (carbonyl position) of the monosaccharide.
     * In case the ring oxygen position is defined, it is adjusted to the new carbonyl position, i.e. the ring type is conserved
     * @param carbonylPosition the carbonylPosition to set
     * @throws MonosaccharideException in case the carbonyl position is smaller than -1 or larger than the ring size
     */
    public void setRingStart(int position) throws MonosaccharideException {
        if(isCheckPositionsOnTheFly()) {
            if(position < -1 || position > this.getSize()) {
                throw new MonosaccharideException("Ring start out of range: " + position);
            }
        }
        this.getBasetype().setRingStart(position);
    }
    
    /**
     * Set the ring start (carbonyl position) of the monosaccharide.
     * In contrast to the <code>setRingStart()</code> method, the ring oxygen is not touched here.
     * @param carbonylposition the carbonylPosition to set
     * @throws MonosaccharideException in case the carbonyl position is smaller than -1 or larger than the ring size
     */
    public void setRingStartNoAdjustment(int carbonylposition) throws MonosaccharideException {
        if(isCheckPositionsOnTheFly()) {
            if(carbonylposition < -1 || carbonylposition > this.getSize()) {
                throw new MonosaccharideException("Ring start out of range: " + carbonylposition);
            }
        }
        this.getBasetype().setRingStartNoAdjustment(carbonylposition);
    }
    
    /**
     * Get the ring end of the monosaccharide
     * @return the ring end
     */
    public int getRingEnd() {
        return this.getBasetype().getRingEnd();
    }

    /**
     * Set the ring end of the monosaccharide
     * @param ringEndPos the ring end position to set
     * @throws Monosaccharide Exception in case the ringEndPos is smaller than -1 or largen than ms size
     */
    public void setRingEnd(int ringEndPos) throws MonosaccharideException {
        if(isCheckPositionsOnTheFly()) {
            if(ringEndPos < -1) {
                throw new MonosaccharideException("Ring oxygen may not be smaller than -1");
            }
            if(ringEndPos > getSize()) {
                throw new MonosaccharideException("Ring oxygen out of range: " + ringEndPos);
            }
        }
        this.getBasetype().setRingEnd(ringEndPos);
    }
    
    public void setDefaultCarbonylPosition(int position) {
        this.getBasetype().setDefaultCarbonylPosition(position);
    }
    
    public int getDefaultCarbonylPosition() {
        return this.getBasetype().getDefaultCarbonylPosition();
    }

    /**
     * Get the size of the monosaccharide
     * @return the size
     */
    public int getSize() {
        return this.getBasetype().getSize();
    }

    /**
     * Set the size of the monosaccharide
     * @param size the size to set
     */
    public void setSize(int size) {
        this.getBasetype().setSize(size);
    }

    /**
     * Get the anomer of the monosaccharide
     * @return the anomer
     */
    public Anomer getAnomer() {
        return this.getBasetype().getAnomer();
    }

    /**
     * Set the anomer of the monosaccharide
     * @param anomer the anomer to set
     */
    public void setAnomer(Anomer anomer) {
        this.getBasetype().setAnomer(anomer);
    }
    
    /**
     * Set the anomer of the monosaccharide by the anomer namestring
     * @param anomerStr the string representation of the anomer to set
     * @throws MonosaccharideException in case anomerStr cannot be translated into a valid anomer
     */
    public void setAnomer(String anomerStr) throws MonosaccharideException {
        this.getBasetype().setAnomer(Anomer.forNameOrSymbol(anomerStr));
    }

    /**
     * Get the configuration of the monosaccharide
     * @return the configuration
     */
    public StereoConfiguration getConfiguration() {
        return this.getBasetype().getConfiguration();
    }

    /**
     * Set the configuration of the monosaccharide
     * @param configuration the configuration to set
     */
    public void setConfiguration(StereoConfiguration configuration) {
        this.getBasetype().setConfiguration(configuration);
    }
    
    /**
     * Set the configuration of the monosaccharide by a configuration string
     * @param configStr the string representation of the configuration to set
     * @throws MonosaccharideException in case configStr does not encode a valid configuration
     */
    public void setConfiguration(String configStr) throws MonosaccharideException {
        this.getBasetype().setConfiguration(StereoConfiguration.forNameOrSymbol(configStr));
    }

    public void setRingtype(Ringtype type) throws MonosaccharideException {
        this.getBasetype().setRingtype(type);
    }
    
    public Ringtype getRingtype() {
        return(this.getBasetype().getRingtype());
    }
    
    public String getRingtypeSymbol() {
        return(this.getBasetype().getRingtypeSymbol());
    }
    
    /**
     * Check, if this monosaccharide is an alditol
     * @return true, if the basetype is an alditol
     */
    public boolean isAlditol() {
        return this.getBasetype().isAlditol();
    }

    /**
     * Set / unset an alditol modification for this monosaccharide's basetype.
     * Setting the alditol includes setting of ring start / end to open chain and adding the aldi core modification,
     * unsetting includes deletion of the aldi core modification (ring closure is not touched).
     * @param alditol flag to indicate whether to set (true) or unset (false) the alditol modification
     * @throws MonosaccharideException in case the ring oxygen is > 0 (i.e. monosaccharide is ring form, which excludes alditol)
     */
    public void setAlditol(boolean alditol) throws MonosaccharideException {
        this.getBasetype().setAlditol(alditol);
    }

    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public int getNextModificationId() {
        return(++this.modificationIndex);
    }
    
    public Basetype getBasetype() {
        return(this.basetype);
    }
    
    public void setBasetype(Basetype bt) {
        this.basetype = bt;
    }
    
    /**
     * @return the ignoreRingoxygenPositionCheck
     */
    public boolean isCheckPositionsOnTheFly() {
        return this.checkPositionsOnTheFly;
    }

    /**
     * @param ignoreRingoxygenPositionCheck the ignoreRingoxygenPositionCheck to set
     */
    public void setCheckPositionsOnTheFly(boolean ignoreRingoxygenPositionCheck) {
        this.checkPositionsOnTheFly = ignoreRingoxygenPositionCheck;
    }

    /**
     * Check if the orientation of the monosaccharide was changed during consistency checks (might only happen with residues where no clear C1 is defined, like in alditols)
     * @return the value of the orientationChanged flag
     */
    public boolean isOrientationChanged() {
        return this.orientationChanged;
    }

    /**
     * Set the value of the orientationChanged flag to mark if the orientation of the monosaccharide was changed during consistency checks (might only happen with residues where no clear C1 is defined, like in alditols)
     * @param orientationChanged the orientationChanged to set
     */
    private void setOrientationChanged(boolean orientationChanged) {
        this.orientationChanged = orientationChanged;
    }
    
    /**
     * Invert the value of the orientationChanged flag
     * (set it to true if it's false and vice versa)
     */
    private void invertOrientationChanged() {
        this.orientationChanged = !this.orientationChanged;
    }

    /**
     * @return the fuzzy
     */
    public boolean isFuzzy() {
        return this.fuzzy;
    }

    /**
     * @param fuzzy the fuzzy to set
     */
    public void setFuzzy(boolean fuzzy) {
        this.fuzzy = fuzzy;
    }

    /**
     * Get the database id of the monosaccharide
     * (needed for Hibernate connection)
     * @return the database id
     */
    public int getDbId() {
        return this.dbId;
    }

    /**
     * Set the database id of the monosaccharide
     * (needed for Hibernate connection)
     * @param dbId the database id to set
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    //*****************************************************************************
    //*** stereocode related methods: *********************************************
    //*****************************************************************************
    
    /**
     * Get the stereocode of the monosaccharide
     * @return the stereocode
     */
    public Stereocode getStereocode() {
        return this.getBasetype().getStereocode();
    }

    /**
     * Set the stereocode of the monosaccharide
     * @param stereocode the stereocode to set
     */
    public void setStereocode(Stereocode stereocode) {
        this.getBasetype().setStereocode(stereocode);
    }
    
    /**
     * Get the stereocode string of the monosaccharide
     * @return the string value of the stereocode
     */
    public String getStereoStr() {
        return this.getBasetype().getStereoStr();
    }
    
    /**
     * Set the string value of the stereocode of the monosaccharide
     * @param stereoStr the stereo string to set
     */
    public void setStereoStr(String stereoStr) {
        this.getBasetype().setStereoStr(stereoStr);
    }
    
    public String getStereoStrWithoutAnomeric() throws ResourcesDbException {
        return this.getBasetype().getStereoStrWithoutAnomeric();
    }

    /** 
     * Set the stereochemistry resulting from the anomer in the stereocode
     */
    public void setAnomerInStereocode() throws ResourcesDbException {
        this.getBasetype().setAnomerInStereocode();
    }
    
    /**
     * Get the configuration that to which the anomeric center has to be compared to decide whether a monosaccharide is alpha or beta
     * @return the configuration of the anomeric reference atom
     * @throws ResourcesDbException
     */
    public StereoConfiguration getAnomericReferenceConfiguration() throws ResourcesDbException {
        return this.getBasetype().getAnomericReferenceConfiguration();
    }
    
    public String getExtendedStereocodeStr() throws ResourcesDbException {
        if(this.hasUncertainCoremodificationPosition()) {
            throw new MonosaccharideException("Cannot generate extended stereocode for monosaccharide with uncertain core modification positions.");
        }
        String extStereo = this.getStereoStr();
        for(int pos = 1; pos <= extStereo.length(); pos++) {
            if(extStereo.substring(pos - 1, pos).equals(Stereocode.StereoN)) {
                ArrayList<CoreModificationTemplate> coreModList = this.getCoreModificationTemplatesByPosition(pos);
                if(coreModList.contains(CoreModificationTemplate.ACID)) {
                    Stereocode.setPositionInStereoString(extStereo, Stereocode.ExtStereoAcid, pos);
                } else if(coreModList.contains(CoreModificationTemplate.SP2)) {
                    Stereocode.setPositionInStereoString(extStereo, Stereocode.ExtStereoSp2, pos);
                } else if(coreModList.contains(CoreModificationTemplate.YN)) {
                    Stereocode.setPositionInStereoString(extStereo, Stereocode.ExtStereoYn, pos);
                } else if(coreModList.contains(CoreModificationTemplate.EN) || coreModList.contains(CoreModificationTemplate.ENX)) {
                    if(coreModList.contains(CoreModificationTemplate.DEOXY)) {
                        Stereocode.setPositionInStereoString(extStereo, Stereocode.ExtStereoEnDeoxy, pos);
                    } else if(coreModList.contains(CoreModificationTemplate.EN) || MonosaccharideValidation.enDeoxyStatusPositionConfident(this, pos)) {
                        Stereocode.setPositionInStereoString(extStereo, Stereocode.ExtStereoEnOH, pos);
                    } else {
                        Stereocode.setPositionInStereoString(extStereo, Stereocode.ExtStereoEnX, pos);
                    }
                } else if(coreModList.contains(CoreModificationTemplate.DEOXY)) {
                    Stereocode.setPositionInStereoString(extStereo, Stereocode.ExtStereoDeoxy, pos);
                } else if(pos == this.getRingStart() || coreModList.contains(CoreModificationTemplate.KETO)) {
                    Stereocode.setPositionInStereoString(extStereo, Stereocode.ExtStereoCarbonyl, pos);
                } else if(pos == 1 || pos == this.getSize()) {
                    Stereocode.setPositionInStereoString(extStereo, Stereocode.ExtStereoCH2OH, pos);
                } else {
                    throw new MonosaccharideException("extended stereocode builder: cannot resolve source of loss of stereochemistry at position " + pos);
                }
            }
        }
        return extStereo;
    }
    
    //*****************************************************************************
    //*** modification related methods: *******************************************
    //*****************************************************************************
    
    public List<CoreModification> getCoreModifications() {
        return this.getBasetype().getCoreModifications();
    } 

    /** Check, if the position of a modification is valid 
     * @param mod The modification to be checked
     * @return true, if modification is at valid position(s); false, if modification is already present
     * @throws MonosaccharideException (in case the modification is at an invalid position)
     */
    public boolean checkModificationPosition(CoreModification mod) throws MonosaccharideException {
        return this.getBasetype().checkModificationPosition(mod);
    }
    
    /** Add a modification to this monosaccharide
     * @param mod The modification to be added
     * @return true, if modification was added; false, if modification is already present
     * @throws MonosaccharideException (in case the modification is at an invalid position)
     */
    public boolean addCoreModification(CoreModification mod) throws MonosaccharideException {
        //*** test, if modification is at a valid position: ***
        boolean valid = true;
        if(isCheckPositionsOnTheFly()) {
            valid = checkModificationPosition(mod);
        }
        if(valid) {
            this.getBasetype().addCoreModification(mod);
        }
        return valid;
    }
    
    public void deleteCoreModification(String name, int position) throws MonosaccharideException {
        this.getBasetype().deleteCoreModification(name, position);
    }
    
    public void deleteCoreModification(CoreModification mod) throws MonosaccharideException {
        this.getBasetype().deleteCoreModification(mod);
    }
    
    public void initCoreModifications() {
        this.getBasetype().initCoreModifications();
    }
    
    /**
     * Get a list of all substitutions that are attached to the monosaccharide
     * @return the substitutions
     */
    public List<Substitution> getSubstitutions() {
        return this.substitutions;
    }
    
    public void setSubstitutions(List<Substitution> substList) {
        this.substitutions = substList;
    }

    /**
     * Add a substitution to the monosaccharide (specified by name and position)
     * @param name the name of the substitution
     * @param position the position of the substitution
     * @throws ResourcesDbException in case the substitution contains a position that is not substitutable (only if the checkPositionsOnTheFly flag is set)
     *         or the name does not encode a valid substituent
     */
    public void addSubstitution(String name, int position) throws ResourcesDbException {
        Substitution subst = new Substitution(name, position, this.getTemplateContainer());
        this.addSubstitution(subst);
    }
    
    /**
     * Add a divalent substitution to the monosaccharide (specified by name and positions)
     * @param name the name of the substitution
     * @param position the position of the substitution
     * @throws ResourcesDbException in case the substitution contains a position that is not substitutable (only if the checkPositionsOnTheFly flag is set)
     *         or the name does not encode a valid substituent
     */
    public void addSubstitution(String name, int position1, int position2) throws ResourcesDbException {
        Substitution subst = new Substitution(name, position1, position2, this.getTemplateContainer());
        this.addSubstitution(subst);
    }
    
    /**
     * Add a substitution to the monosaccharide
     * @param subst the substitution to add
     * @throws MonosaccharideException in case the substitution contains a position that is not substitutable (only if the checkPositionsOnTheFly flag is set)
     */
    public void addSubstitution(Substitution subst) throws MonosaccharideException {
        if(isCheckPositionsOnTheFly()) {
            for(Integer posInt : subst.getPositions()) {
                if(!MonosaccharideValidation.isSubstitutable(this, posInt.intValue(), subst.getLinkagetype1())) {
                    if(posInt.intValue() == this.getRingEnd() && subst.getTemplate().isCanReplaceRingOxygen()) {
                        if(this.getSubstitutionsByPosition(posInt.intValue()).size() > 0) {
                            throw new MonosaccharideException("Cannot add substitution " + posInt.intValue() + subst.getName() + ": position not subsitutable");
                        }
                    } else {
                        throw new MonosaccharideException("Cannot add substitution " + posInt.intValue() + subst.getName() + ": position not subsitutable");
                    }
                }
            }
        }
        subst.setModificationId(this.getNextModificationId());
        this.getSubstitutions().add(subst);
        this.sortSubstitutions();
    }
    
    /**
     * Add a substitution that in the source notation is not part of the residue name but a separate residue to the monosaccharide.
     * If there is already a substitution present at the given position, this method, in contrast to <code>addSubstitution(Substitution)</code>,
     * checks if the present substituent and the one that is to be added can be merged,
     * e.g. an acetyl added to a present amino group results in an n-acetyl substituent.
     * @param subst the substitution to add
     * @param scheme the name scheme to be used
     * @param stContainer a SubstituentTemplateContainer
     * @param nameBasedMerge if this parameter is set to 'true', substituents are merged on the basis of residueIncludedName and seperateDisplayName, otherwise substituent subParts are used
     * @throws MonosaccharideException
     */
    public void addSeparateDisplaySubstitution(Substitution subst, GlycanNamescheme scheme, SubstituentTemplateContainer stContainer, boolean nameBasedMerge) throws ResourcesDbException {
        boolean substMerged = false;
        if(subst.getIntValuePosition1() == Modification.EMPTYPOSITIONVALUE) {
            if(subst.getPosition1().size() > 1) {
                for(Integer posInt : subst.getPosition1()) {
                    if(this.getSubstitution(null, posInt.intValue(), subst.getLinkagetype1()) != null) {
                        throw new MonosaccharideException("Cannot add separate substitutent with uncertain position to a residue, which is already substituted");
                    }
                }
            }
        } else {
            Integer posInt = subst.getPosition1().get(0);
            Substitution presentSubst = this.getSubstitution(null, posInt.intValue(), subst.getLinkagetype1());
            if(presentSubst == null) {
                LinkageType linktype = null;
                if(subst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                    linktype = LinkageType.DEOXY;
                }
                if(linktype != null) {
                    presentSubst = this.getSubstitution(null, posInt.intValue(), linktype);
                }
            }
            if(presentSubst != null) {
                if(presentSubst != null && presentSubst.isHasSeparateDisplayPart()) {
                    throw new MonosaccharideException("Cannot add separate display substituent at a position, which holds already a separate display substituent");
                }
                //*** substitution already present at this position, so check, if there is a template...
                if(nameBasedMerge) {
                    //*** ...that has the name of the present substitution as residue included name and the name of the subst. to be added as separate display name ***
                    String presentSubstName = presentSubst.getSourceName();
                    String separateSubstName = subst.getSourceName();
                    if(!scheme.isCaseSensitive()) {
                        presentSubstName = presentSubstName.toLowerCase();
                        separateSubstName = separateSubstName.toLowerCase();
                    }
                    for(SubstituentTemplate substTmpl : stContainer.getTemplateList()) {
                        for(SubstituentAlias substAlias : substTmpl.getAliasList(scheme, presentSubst.getLinkagetype1())) {
                            String aliasIncludedName = substAlias.getResidueIncludedName();
                            if(aliasIncludedName == null) {
                                continue;
                            }
                            String aliasSeparateName = substAlias.getSeparateDisplayName();
                            if(aliasSeparateName == null) {
                                continue;
                            }
                            if(!scheme.isCaseSensitive()) {
                                aliasIncludedName = aliasIncludedName.toLowerCase();
                                aliasSeparateName = aliasSeparateName.toLowerCase();
                            }
                            if(aliasIncludedName.equals(presentSubstName)) {
                                if(aliasSeparateName.equals(separateSubstName)) {
                                    presentSubst.setTemplate(substAlias.getPrimaryTemplate());
                                    presentSubst.setHasSeparateDisplayPart(true);
                                    substMerged = true;
                                    break;
                                }
                            }
                        }
                        if(substMerged) {
                            break;
                        }
                    }
                } else {
                    //*** ...that has the existing substituent and the one to be added as substituent subparts ***
                    for(SubstituentTemplate substTmpl : stContainer.getTemplateList()) {
                        if(substTmpl.getSubparts() != null) {
                            SubstituentSubpartTreeNode subpartsRoot = substTmpl.getSubparts();
                            SubstituentTemplate rootTmpl = subpartsRoot.getSubstTmpl(stContainer);
                            if(rootTmpl == null) {
                                throw new ResourcesDbException("Error in subparts of substTemplate " + substTmpl.getName() + ": cannot assign template for subpart " + subpartsRoot.getName());
                            }
                            if(rootTmpl.equals(presentSubst.getTemplate())) {
                                if(subpartsRoot.getChildCount() == 1) {
                                    SubstituentSubpartTreeNode subpartsNode = (SubstituentSubpartTreeNode) subpartsRoot.getFirstChild();
                                    SubstituentTemplate nodeTmpl = subpartsNode.getSubstTmpl(stContainer);
                                    if(nodeTmpl == null) {
                                        throw new ResourcesDbException("Error in subparts of substTemplate " + substTmpl.getName() + ": cannot assign template for subpart " + subpartsNode.getName());
                                    }
                                    if(nodeTmpl.equals(subst.getTemplate())) {
                                        if(subpartsNode.getChildCount() == 0) {
                                            presentSubst.setTemplate(substTmpl);
                                            presentSubst.setHasSeparateDisplayPart(true);
                                            substMerged = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(!substMerged) {
                    throw new MonosaccharideException("Cannot add separate display substitution: position already substituted and substituents cannot be merged");
                }
            } 
        }
        if(!substMerged) {
            subst.setHasSeparateDisplayPart(true);
            this.addSubstitution(subst);
        }
        this.sortSubstitutions();
    }
    
    public Modification getModificationById(int modId) throws MonosaccharideException {
        for(CoreModification mod : getCoreModifications()) {
            if(mod.getModificationId() == modId) {
                return(mod);
            }
        }
        for(Substitution subst : getSubstitutions()) {
            if(subst.getModificationId() == modId) {
                return(subst);
            }
        }
        throw new MonosaccharideException("modification id " + modId + " not found.");
    }
    
    public void sortModifications() {
        this.sortCoreModifications();
        this.sortSubstitutions();
    }

    public void sortSubstitutions() {
        List<Substitution> substList = this.getSubstitutions();
        for(int i = 0; i < substList.size(); i++) {
            for(int j = substList.size() - 1; j > 0; j--) {
                if(substList.get(j).makeCmpString().compareTo(substList.get(j - 1).makeCmpString()) < 0) {
                    Substitution tmpMod = substList.get(j - 1);
                    substList.set(j - 1, substList.get(j));
                    substList.set(j, tmpMod);
                }
            }
        }
    }
    
    public void sortCoreModifications() {
        if(this.getBasetype() != null) {
            this.getBasetype().sortCoreModifications();
        }
    }
    
    public void setUronic() throws MonosaccharideException {
        CoreModification mod = new CoreModification();
        mod.setModification(CoreModificationTemplate.ACID, getSize());
        this.addCoreModification(mod);
    }
    
    public boolean isUronic() {
        return this.getBasetype().isUronic();
    }
    
    public void setAldonic() throws MonosaccharideException {
        this.getBasetype().setAldonic();
    }
    
    public boolean isAldonic()  {
        return this.getBasetype().isAldonic();
    }
    
    public void setAldaric() throws MonosaccharideException {
        this.getBasetype().setAldaric();
    }
    
    public boolean isAldaric() {
        return this.getBasetype().isAldaric();    
    }
    
    /**
     * Check, if this monosaccharide's basetype has a double bond at a given position
     * @param pos the position to check
     * @return true, if there is an EN or ENX core modification present at the given position
     */
    public boolean hasDoubleBond(int pos) {
        return this.getBasetype().hasDoubleBond(pos);
    }
    
    /**
     * Get the number of substitutions
     * @return the substitution count
     */
    public int countSubstitutions() {
        return(getSubstitutions().size());
    }
    
    /**
     * Get the number of core modifications
     * @return the core modification count
     */
    public int countCoreModifications() {
        return this.getBasetype().countCoreModifications();
    }
    
    /**
     * Get the number of core modifications of a given type (identified by mod. name)
     * @param name the name of the core modification
     * @return the core modification count
     */
    public int countCoreModifications(String name) {
        return this.getBasetype().countCoreModifications(name);
    }
    
    /**
     * Get the number of core modifications of a given type (identified by mod. template)
     * @param tmpl the core modification template
     * @return the core modification count
     */
    public int countCoreModifications(CoreModificationTemplate tmpl) {
        return this.getBasetype().countCoreModifications(tmpl);
    }
    
    /**
     * Get the number of substitutions of a given type
     * @param name the name of the substitution
     * @return the substitution count
     */
    public int countSubstitutions(String name) {
        int count = 0;
        for(Substitution subst: getSubstitutions()) {
            if(subst.getName().equals(name)) {
                count ++;
            }
        }
        return(count);
    }
    
    /**
     * Get a list of all positions that have a modification which results in a loss of stereochemistry
     * @return list of achiral positions
     */
    public ArrayList<Integer> getStereolossPositions() {
        List<CoreModification> modifications = getCoreModifications();
        ArrayList<Integer> positions = new ArrayList<Integer>();
        for(int i = 0; i < modifications.size(); i++) {
            CoreModification mod = modifications.get(i);
            if(mod.getTemplate().isStereoLoss()) {
                if(mod.getTemplate().equals(CoreModificationTemplate.DEOXY)) {
                    //TODO: check influence of r/s_config substiuents
                    if(this.getSubstitution(null, mod.getIntValuePosition1(), LinkageType.H_LOSE) != null) {
                        continue;
                    }
                }
                ArrayList<Integer> modPositions = mod.getPositions();
                for(int p = 0; p < modPositions.size(); p++) {
                    Integer pos = modPositions.get(p);
                    if(!positions.contains(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        Collections.sort(positions);
        return(positions);
    }
    
    /**
     * Check, if there is a loss of stereochemistry at a given position (e.g. due to deoxygenation, double bonds, etc.)
     * @param pos the position to check
     * @return true, if the position has a loss of stereochemistry, otherwise false
     */
    public boolean isStereolossPosition(int pos) {
        return this.getBasetype().isStereolossPosition(pos);
    }
    
    /**
     * Check, if there is a loss of stereochemistry at a given position (e.g. due to deoxygenation, double bonds, etc.)
     * that is not caused by the modification type specified in the "ignore" parameter.
     * @param pos the position to check
     * @param ignore the modification type to ignore
     * @return true, if the position has a loss of stereochemistry (not caused by the ignored modification type), otherwise false
     */
    public boolean isStereolossPositionWithIgnoreType(int pos, CoreModificationTemplate ignoreTemplate) {
        return this.getBasetype().isStereolossPositionWithIgnoreType(pos, ignoreTemplate);
    }
    
    /**
     * Get a list of all substitutions that are present at a given position
     * @param position the position for which the substitutions are listed
     * @return list of substitutions
     */
    public ArrayList<Substitution> getSubstitutionsByPosition(int position) {
        List<Substitution> substitutions = getSubstitutions();
        ArrayList<Substitution> substOut = new ArrayList<Substitution>();
        if(substitutions != null) {
            Integer positionInt = new Integer(position);
            for(Substitution subst : substitutions) {
                if(subst.getPositions().contains(positionInt)) {
                    substOut.add(subst);
                }
            }
        }
        return(substOut);
    }
    
    /**
     * Get a substitution identified by name, position and linkage type.
     * @param name the name of the substitution (or null to match any name)
     * @param position the position of the substitution
     * @param linktype the linkage type of the substitution (or null to match any linkage type)
     * @return the first substitution matching the given properties, or null if no such substitution is present
     */
    public Substitution getSubstitution(String name, int position, LinkageType linktype) {
        for(Substitution subst : getSubstitutions()) {
            if(name == null || subst.getName().equals(name)) {
                if(subst.containsPosition1(position)) {
                    if(linktype == null || subst.getLinkagetype1().equals(linktype)) {
                        return(subst);
                    }
                }
                if(subst.containsPosition2(position)) {
                    if(linktype == null || subst.getLinkagetype2().equals(linktype)) {
                        return(subst);
                    }
                }
            }
        }
        return(null);
    }
    
    /**
     * Get a list of all core modifications that are present at a given position
     * @param position the position for which the core modifications are listed
     * @return list of core modifications
     */
    public ArrayList<CoreModification> getCoreModificationsByPosition(int position) {
        return this.getBasetype().getCoreModificationsByPosition(position);
    }
    
    /**
     * Get a list of all core modification types that are present at a given position
     * @param position the position for which the core modification types are listed
     * @return list of core modification templates
     */
    public ArrayList<CoreModificationTemplate> getCoreModificationTemplatesByPosition(int position) {
        return this.getBasetype().getCoreModificationTemplatesByPosition(position);
    }
    
    /**
     * Get a list of all modifications that are present at a given position
     * @param position the position for which the modifications are listed
     * @return list of modifications (core modifications + substituents)
     */
    public ArrayList<Modification> getModifications(int position) {
        ArrayList<Modification> outList = new ArrayList<Modification>();
        ArrayList<Substitution> substList = getSubstitutionsByPosition(position);
        for(Substitution subst : substList) {
            outList.add(subst);
        }
        ArrayList<CoreModification> coremodList = getCoreModificationsByPosition(position);
        for(CoreModification mod: coremodList) {
            outList.add(mod);
        }
        return(outList);
    }
    
    /**
     * Get a list of all core modifications of a given type (identified by type name)
     * @param name the name of the core modification
     * @return list of core modifications
     */
    public ArrayList<CoreModification> getCoreModifications(String name) {
        return this.getBasetype().getCoreModifications(name);
    }
    
    /**
     * Get a list of all core modifications of a given type (identified by type template)
     * @param tmpl the core modification template
     * @return list of core modifications
     */
    public ArrayList<CoreModification> getCoreModifications(CoreModificationTemplate tmpl) {
        return this.getBasetype().getCoreModifications(tmpl);
    }
    
    /**
     * Get a core modification specified by name and position
     * Note: If more than one modification matches the given criteria (e.g. due to uncertain positions) the first match is returned.
     * @param name the name of the core modification
     * @param position the position of the core modification
     * @return the core modification or null if no modification matching the given criteria is present
     */
    public CoreModification getCoreModification(String name, int position) {
        return this.getBasetype().getCoreModification(name, position);
    }
    
    /**
     * Get a core modification specified by template and position
     * Note: If more than one modification matches the given criteria (e.g. due to uncertain positions) the first match is returned.
     * @param name the name of the core modification
     * @param position the position of the core modification
     * @return the core modification or null if no modification matching the given criteria is present
     */
    public CoreModification getCoreModification(CoreModificationTemplate tmpl, int position) {
        return this.getBasetype().getCoreModification(tmpl, position);
    }
    
    /**
     * Get a list of EN/ENX core modifications present in this monosaccharide's basetype
     * @return
     */
    public ArrayList<CoreModification> getEnModifications() {
        return this.getBasetype().getEnModifications();
    }
    
    /**
     * Test, if a given core modification is present in the monosaccharide.
     * @param mod: The core modification to be checked for.
     * @return true, if the core modification is present, otherwise false.
     */
    public boolean hasCoreModification(CoreModification mod) {
        return this.getBasetype().hasCoreModification(mod);
    }
    
    /**
     * Test, if a given core modification is present in the monosaccharide.
     * @param tmpl: the core modification template
     * @param position: the position of the core modification
     * @return true, if the core modification is present, otherwise false.
     */
    public boolean hasCoreModification(CoreModificationTemplate tmpl, int position) {
        return this.getBasetype().hasCoreModification(tmpl, position);
    }
    
    public boolean hasCoreModification(CoreModificationTemplate tmpl) {
        return this.getBasetype().hasCoreModification(tmpl);
    }
    
    /**
     * Test, if a given substitution is present in the monosaccharide.
     * @param subst: The substitution to be checked for.
     * @return true, if the substitution is present, otherwise false.
     */
    public boolean hasSubstitution(Substitution subst) {
        for(Substitution presentSubst : getSubstitutions()) {
            if(presentSubst.equals(subst)) {
                return(true);
            }
        }
        return(false);
    }
    
    /**
     * Check, whether the monosaccharide contains a modification (core modification or substitution) that is located at an uncertain position
     * @return
     */
    public boolean hasUncertainModificationPosition() {
        return(hasUncertainCoremodificationPosition() || hasUncertainSubstitutionPosition());
    }
    
    /**
     * Check, whether the monosaccharide contains a core modification that is located at an uncertain position
     * @return
     */
    public boolean hasUncertainCoremodificationPosition() {
        return this.getBasetype().hasUncertainCoremodificationPosition();
    }
    
    /**
     * Check, whether the monosaccharide contains a substitution that is located at an uncertain position
     * @return
     */
    public boolean hasUncertainSubstitutionPosition() {
        for(Substitution subst : getSubstitutions()) {
            if(subst.hasUncertainLinkagePosition()) {
                return(true);
            }
        }
        return(false);
    }
    
    /** 
     * Adjust the positions of core modifications resulting from a rotation of the monosaccharide by 180 degrees
     * Such a rotation might occurr with alditol residues or aldaric acids.
     */
    public void mirrorCoreModificationPositions() {
        this.getBasetype().mirrorCoreModificationPositions();
    }
    
    /** 
     * Adjust the positions of substitutions resulting from a rotation of the monosaccharide by 180 degrees
     * Such a rotation might occurr with alditol residues or aldaric acids.
     */
    public void mirrorSubstitutionPositions() {
        for(Substitution subst: getSubstitutions()) {
            ArrayList<Integer> positions1 = subst.getPosition1();
            for(int i = 0; i < positions1.size(); i++) {
                int pos = positions1.get(i);
                if(pos > 0) {
                    pos = getSize() + 1 - pos;
                    positions1.set(i, new Integer(pos));
                }
            }
            //Collections.sort(positions1);  //*** this sorting is now included in the sortPositions() call below ***
            
            ArrayList<Integer> positions2 = subst.getPosition2();
            for(int i = 0; i < positions2.size(); i++) {
                int pos = positions2.get(i);
                if(pos > 0) {
                    pos = getSize() + 1 - pos;
                    positions2.set(i, new Integer(pos));
                }
            }
            //Collections.sort(positions2);
            
            subst.setPosition1(positions1);
            subst.setPosition2(positions2);
            subst.sortPositions();
        }
    }
    
    /**
     * Get a list of the positions with "free" hydroxyl groups, i.e. the positions to which substituents or other monosaccharides can be added. 
     * @return
     */
    public ArrayList<Integer> getExtendablePositions() {
        ArrayList<Integer> extPosList = new ArrayList<Integer>();
        for(int i = 1; i <= this.getSize(); i++) {
            if(MonosaccharideValidation.isSubstitutable(this,i, LinkageType.H_AT_OH)) {
                extPosList.add(new Integer(i));
            }
        }
        return(extPosList);
    }
    
    /**
     * Rotate an alditol residue by 180 degrees.
     * The rotation affects the stereocode, the positions of core modifications and substitutions and (via the stereocode) potentially the abs. configuration.
     */
    public void rotateAlditol() {
        getStereocode().rotate();
        mirrorCoreModificationPositions();
        mirrorSubstitutionPositions();
        this.setConfiguration(Stereocode.getConfigurationFromStereoString(this.getStereoStr()));
        this.invertOrientationChanged();
    }
    
    /**
     * Check, if this monosaccharide represents a superclass (something like "HexNAc")
     * @return
     */
    public boolean isSuperclass() {
        try {
            for(StereoConfiguration stereoConf : this.getStereocode().toConfigurationList()) {
                if(stereoConf != StereoConfiguration.Unknown && stereoConf != StereoConfiguration.Nonchiral) {
                    return false;
                }
            }
        } catch(ResourcesDbException rex) {
            return false;
        }
        if(!this.getAnomer().equals(Anomer.UNKNOWN)) {
            return false;
        }
        if(this.getRingStart() != Basetype.UNKNOWN_RING || this.getRingEnd() != Basetype.UNKNOWN_RING) {
            return false;
        }
        return true;
    }
    
    public void setPossibleLinkingPositions(List<MonosaccharideLinkingPosition> posList) {
        this.possibleLinkingPositions = posList;
    }
    
    public List<MonosaccharideLinkingPosition> getPossibleLinkingPositions() {
        if(this.possibleLinkingPositions == null) {
            this.setPossibleLinkingPositions(MonosaccharideDataBuilder.buildPossibleLinkagePositions(this));
        }
        return this.possibleLinkingPositions;
    }
    
    //*****************************************************************************
    //*** notation related methods: ***********************************************
    //*****************************************************************************
    
    public List<MonosaccharideSynonym> getSynonyms() {
        return this.synonyms;
    }

    public void setSynonyms(List<MonosaccharideSynonym> synonymsList) {
        this.synonyms = synonymsList;
    }
    
    public boolean addSynonym(MonosaccharideSynonym alias) {
        return this.addSynonym(alias, false);
    }
    
    public boolean addSynonym(MonosaccharideSynonym alias, boolean replacePrimary) {
        if(getSynonyms() == null) {
            setSynonyms(new ArrayList<MonosaccharideSynonym>());
        }
        for(int i = 0; i < this.getSynonyms().size(); i++) {
            MonosaccharideSynonym exstAlias = this.getSynonyms().get(i);
            if(exstAlias.getNamescheme().equals(alias.getNamescheme())) {
                if(exstAlias.equals(alias)) {
                    System.out.println("Monosaccharide.addSynonym(): Alias already present.");
                    return false; //*** do not add an already existing alias name ***
                }
                if(alias.isPrimary()) {
                    if(exstAlias.isPrimary()) {
                        if(replacePrimary) {
                            this.getSynonyms().set(i, alias);
                            return true;
                        } else {
                            System.out.println("Warning: Could not add primary alias for name scheme " + alias.getNameschemeStr() + " to " + this.getName() + " - primary alias already present (1).");
                            System.out.println("  exst: " + exstAlias.toString());
                            System.out.println("  new:  " + alias.toString());
                            return false;
                        }
                    } else {
                        if(alias.equalsIgnoreBooleans(exstAlias)) {
                            if(this.hasPrimaryAlias(alias.getNamescheme())) {
                                if(replacePrimary) {
                                    for(int k = i + 1; k < this.getSynonyms().size(); k++) {
                                        if(this.getSynonyms().get(k).isPrimary()) {
                                            this.getSynonyms().get(k).setIsPrimary(false);
                                        }
                                        this.getSynonyms().set(i, alias);
                                        return true;
                                    }
                                } else {
                                    System.out.println("Warning: Could not add primary alias for name scheme " + alias.getNameschemeStr() + " to " + this.getName() + " - primary alias already present (2).");
                                    return false;
                                }
                            } else {
                                this.getSynonyms().set(i, alias);
                                return true;
                            }
                        }
                    }
                } else {
                    continue;
                }
            }
        }
        getSynonyms().add(alias);
        return true;
    }
    
    public void addSynonyms(List<MonosaccharideSynonym> aliasList, boolean replacePrimary) {
        if(aliasList != null) {
            for(MonosaccharideSynonym alias : aliasList) {
                if(alias != null) {
                    this.addSynonym(alias, replacePrimary);
                }
            }
        }
    }
    
    public boolean hasPrimaryAlias(GlycanNamescheme scheme) {
        if(this.getSynonyms() != null) {
            for(MonosaccharideSynonym alias : this.getSynonyms()) {
                if(alias.getNamescheme().equals(scheme) && alias.isPrimary()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate the MonosaccharideDB name of this monosaccharide object
     * @throws ResourcesDbException
     */
    public void buildName() throws ResourcesDbException {
        this.getBasetype().buildName();
        GlycoCTExporter msdbexp = new GlycoCTExporter(GlycanNamescheme.MONOSACCHARIDEDB, this.getConfig(), this.getTemplateContainer());
        this.setName(msdbexp.export(this));
    }
    
    public void initSynonyms() {
        if(this.getSynonyms() == null) {
            this.setSynonyms(new ArrayList<MonosaccharideSynonym>());
        } else {
            this.getSynonyms().clear();
        }
    }
    
    public String getPrimaryAliasName(GlycanNamescheme scheme) throws ResourcesDbException {
        return this.getPrimaryAliasObject(scheme).getName();
    }
    
    public MonosaccharideSynonym getPrimaryAliasObject(GlycanNamescheme scheme) throws ResourcesDbException {
        if(this.getSynonyms() == null) {
            MonosaccharideDataBuilder.buildSynonyms(this, this.getTemplateContainer());
        }
        if(this.getSynonyms() == null) {
            throw new MonosaccharideException("Cannot build monosaccharide synonyms.");
        }
        for(MonosaccharideSynonym alias : this.getSynonyms()) {
            if(alias.getNamescheme().equals(scheme)) {
                if(alias.isPrimary()) {
                    return alias;
                }
            }
        }
        MonosaccharideDataBuilder.buildSynonyms(this, this.getTemplateContainer(), scheme);
        for(MonosaccharideSynonym alias : this.getSynonyms()) {
            if(alias.getNamescheme().equals(scheme)) {
                if(alias.isPrimary()) {
                    return alias;
                }
            }
        }
        throw new MonosaccharideException("No primary monosaccharide alias defined for notation scheme " + scheme.getNameStr());
    }
    
    public MonosaccharideSynonym getCarbbankAlias() {
        try {
            return getPrimaryAliasObject(GlycanNamescheme.CARBBANK);
        } catch(ResourcesDbException me) {
            return null;
        }
    }
    
    public boolean hasAliasWithResidueExcludedSubst() {
        if(this.getSynonyms() != null) {
            for(MonosaccharideSynonym alias : this.getSynonyms()) {
                if(alias.getExternalSubstList().size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void buildSynonyms() {
        MonosaccharideDataBuilder.buildSynonyms(this, this.getTemplateContainer());
    }

    //*****************************************************************************
    //*** atom related methods: ***************************************************
    //*****************************************************************************
    
    public void calculateMassesFromComposition() throws ResourcesDbException {
        if(this.getComposition() == null) {
            MonosaccharideDataBuilder.buildComposition(this);
        }
        this.setMonoMass(this.getComposition().getMonoMass());
        this.setAvgMass(this.getComposition().getAvgMass());
    }
    
    //*****************************************************************************
    //*** representation related methods: *****************************************
    //*****************************************************************************
    
    public void buildRepresentations() {
        if(this.getRingtype().equals(Ringtype.OPEN)) {
            MonosaccharideDataBuilder.addFischerRepresentations(this);
        } else {
            MonosaccharideDataBuilder.addHaworthRepresentations(this);
        }
    }
    
    public void updateRepresentations() {
        if(this.getRingtype().equals(Ringtype.OPEN)) {
            MonosaccharideDataBuilder.updateFischerRepresentations(this);
        } else {
            MonosaccharideDataBuilder.updateHaworthRepresentations(this);
        }
    }
    
    public int getHaworthImageId(ResidueRepresentationFormat format) {
        ResidueRepresentation monoRep = null;
        monoRep = this.getRepresentation(ResidueRepresentationType.HAWORTH, format);
        if(monoRep != null) {
            return monoRep.getDbId();
        }
        return 0;
    }
    
    public boolean hasHaworth(ResidueRepresentationFormat format) {
        ResidueRepresentation monoRep = this.getRepresentation(ResidueRepresentationType.HAWORTH, format);
        return(monoRep != null);
    }
    
    public boolean hasHaworth() {
        return hasHaworth(null);
    }
    
    public int getFischerImageId(ResidueRepresentationFormat format) {
        ResidueRepresentation monoRep = null;
        monoRep = this.getRepresentation(ResidueRepresentationType.FISCHER, format);
        if(monoRep != null) {
            return monoRep.getDbId();
        }
        return 0;
    }
    
    public boolean hasFischer(ResidueRepresentationFormat format) {
        ResidueRepresentation monoRep = this.getRepresentation(ResidueRepresentationType.FISCHER, format);
        return(monoRep != null);
    }
    
    public boolean hasFischer() {
        return hasFischer(null);
    }
    
    public int getCfgImageId(ResidueRepresentationFormat format) {
        ResidueRepresentation monoRep = null;
        monoRep = this.getRepresentation(ResidueRepresentationType.CFG_SYMBOL, format);
        if(monoRep != null) {
            return monoRep.getDbId();
        }
        return 0;
    }
    
    public int getCfgBwImageId(ResidueRepresentationFormat format) {
        ResidueRepresentation monoRep = null;
        monoRep = this.getRepresentation(ResidueRepresentationType.CFG_SYMBOL_BW, format);
        if(monoRep != null) {
            return monoRep.getDbId();
        }
        return 0;
    }
    
    public int getOxfordImageId(ResidueRepresentationFormat format) {
        ResidueRepresentation monoRep = null;
        monoRep = this.getRepresentation(ResidueRepresentationType.OXFORD_SYMBOL, format);
        if(monoRep != null) {
            return monoRep.getDbId();
        }
        return 0;
    }
    
    public boolean hasRepresentation(ResidueRepresentationType type, ResidueRepresentationFormat format) {
        for(ResidueRepresentation monoRep : this.getRepresentations()) {
            if(type == null || type.equals(monoRep.getType())) {
                if(format == null || format.equals(monoRep.getFormat())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public int getImageId(ResidueRepresentationFormat format) {
        int imgId = 0;
        imgId = this.getHaworthImageId(format);
        if(imgId != 0) {
            return imgId;
        }
        imgId = this.getFischerImageId(format);
        if(imgId != 0) {
            return imgId;
        }
        imgId = this.getOxfordImageId(format);
        if(imgId != 0) {
            return imgId;
        }
        imgId = this.getCfgImageId(format);
        if(imgId != 0) {
            return imgId;
        }
        imgId = this.getCfgBwImageId(format);
        if(imgId != 0) {
            return imgId;
        }
        for(ResidueRepresentation rep : this.getRepresentations()) {
            if(rep.getType().getFormatType().equals(ResidueRepresentationFormat.FORMAT_TYPE_GRAPHICS)) {
                if(format == null || format.equals(rep.getFormat())) {
                    return rep.getDbId();
                }
            }
        }
        return imgId;
    }
    
    //*****************************************************************************
    //*** methods related to ms/nmr data: *****************************************
    //*****************************************************************************
    
    public boolean hasFragmentData() {
        return false;
    }
    
    public boolean hasNmrData() {
        return false;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    /** 
     * Initialize Monosaccharide properties with given Basetype
     */
    public void init(Basetype bt) {
        super.init();
        this.setBasetype(bt);
        this.setSubstitutions(new ArrayList<Substitution>());
        this.setFuzzy(false);
        this.setOrientationChanged(false);
    }
    
    /** 
     * Initialize Monosaccharide properties
     */
    public void init() {
        this.init(new Basetype(this.getConfig(), this.getTemplateContainer()));
    }
    
    /**
     * Initialize Monosaccharide properties with a given TrivialnameTemplate
     * (size, defaultCarbonylPosition, CoreModifications, Substitutions)
     * @param trivTmpl the template providing the data to initialize the Monosaccharide
     * @throws MonosaccharideException
     */
    public void init(TrivialnameTemplate trivTmpl) throws MonosaccharideException {
        super.init();
        Anomer tmpAnom = this.getAnomer();
        List<CoreModification> tmpCoreMods = this.getCoreModifications();
        this.setBasetype(new Basetype(this.getConfig(), this.getTemplateContainer()));
        this.setSize(trivTmpl.getSize());
        this.setDefaultCarbonylPosition(trivTmpl.getCarbonylPosition());
        for(CoreModification coremod : trivTmpl.getCoreModifications()) {
            this.addCoreModification(coremod.clone());
        }
        this.setAnomer(tmpAnom);
        for(CoreModification mod : tmpCoreMods) {
            if(!this.hasCoreModification(mod)) {
                this.addCoreModification(mod);
            }
        }
        for(Substitution subst : trivTmpl.getSubstitutions()) {
            this.addSubstitution(subst.clone());
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String outStr = "";
        outStr += "[Name: " + this.getName() + "; ";
        String basetypeStr = "Basetype: null";
        if(this.getBasetype() != null) {
            basetypeStr = this.getBasetype().toString();
        }
        String substStr = "";
        if(this.getSubstitutions() != null) {
            for(Substitution subst : this.getSubstitutions()) {
                substStr += subst.toString();
            }
        }
        outStr += "" + basetypeStr + "; ";
        outStr += "Substitutions: [" + substStr + "]";
        outStr += "]";
        return(outStr);
    }
    
    public boolean equals(Object anotherMs) {
        if(anotherMs == null) {
            return false;
        }
        if(!anotherMs.getClass().equals(Monosaccharide.class)) {
            return false;
        }
        Monosaccharide compareMs = (Monosaccharide) anotherMs;
        try {
            this.buildName();
            compareMs.buildName();
            if(!this.getName().equals(compareMs.getName())) {
                return false;
            }
        } catch(ResourcesDbException rEx) {
            return false;
        }
        return true;
    }

}
