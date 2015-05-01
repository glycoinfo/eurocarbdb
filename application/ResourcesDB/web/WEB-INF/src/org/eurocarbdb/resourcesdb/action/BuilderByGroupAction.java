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
package org.eurocarbdb.resourcesdb.action;

import java.util.ArrayList;

import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.GlycoconjugateException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.io.HibernateAccess;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.template.*;
import org.eurocarbdb.resourcesdb.util.Utils;

public class BuilderByGroupAction extends ShowMonosaccharideAction {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constant to mark unknown ring size (0)
     */
    public static final int UNKNOWN_RING   = Basetype.UNKNOWN_RING;
    
    /**
     * Constant to mark open chain residues (-1) 
     */
    public static final int OPEN_CHAIN   = Basetype.OPEN_CHAIN;
    
    public static int getOPEN_CHAIN() { //*** getter needed by freemarker to access the field ***
        return OPEN_CHAIN;
    }
    
    private TemplateContainer container;
    
    private int size = 0;
    private int ringStart = -2;
    private int ringEnd = -2;
    
    private String msname = null;
    
    private String[] positions;
    
    private String errorString;
    private ArrayList<String> positionErrors;
    private String warningString;
    
    private boolean positionsIsSet = false;
    private String extStereoString = null;
    
    private String step;
    
    public TemplateContainer getContainer() {
        if(this.container == null) {
            this.container = new TemplateContainer();
        }
        return this.container;
    }

    public void setContainer(TemplateContainer container) {
        this.container = container;
    }
    
    public SubstituentTemplateContainer getSubstTemplateContainer() {
        return this.getContainer().getSubstituentTemplateContainer();
    }

    public String getErrorString() {
        return this.errorString;
    }

    public void setErrorString(String errorMsg) {
        this.errorString = errorMsg;
    }

    public String getWarningString() {
        return warningString;
    }

    public void setWarningString(String warningString) {
        this.warningString = warningString;
    }

    public String getStep() {
        return this.step;
    }

    public void setStep(String stepStr) {
        this.step = stepStr;
    }

    public String[] getPositions() {
        return positions;
    }

    public void setPositions(String[] positionsArr) {
        this.positions = positionsArr;
    }

    public ArrayList<String> getPositionErrors() {
        return this.positionErrors;
    }

    public void setPositionErrors(ArrayList<String> positionErrors) {
        this.positionErrors = positionErrors;
    }

    public int getRingEnd() {
        return this.ringEnd;
    }

    public void setRingEnd(int ringEnd) {
        this.ringEnd = ringEnd;
    }

    public int getRingStart() {
        return this.ringStart;
    }

    public void setRingStart(int ringStart) {
        this.ringStart = ringStart;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getMsname() {
        return this.msname;
    }

    public void setMsname(String msnameStr) {
        this.msname = msnameStr;
    }

    public BasetypeBuilderGroup[] getBuilderGroups() {
        return BasetypeBuilderGroup.values();
    }
    
    private void buildMonosaccharide() throws ResourcesDbException {
        Utils.setTemplateDataIfNotSet();
        
        //*** build basetype: ***
        Basetype bt = new Basetype(null, this.getTemplateContainer());
        bt.buildByExtendedStereocode(this.getExtStereoString(), this.getSize(), this.getRingStart(), this.getRingEnd());
        Monosaccharide ms = new Monosaccharide(bt);
        
        //*** add substituents: ***
        if(this.getSubstName() != null) {
            for(int i = 0; i < this.getSubstMsPos().length; i++) {
                int pos = this.getSubstMsPosValue(i);
                if(pos > 0) {
                    String substName = this.getSubstName(i);
                    if(substName != null && substName.length() > 0) {
                        SubstituentTemplate substTmpl = this.getSubstTemplateContainer().forName(GlycanNamescheme.MONOSACCHARIDEDB, substName);
                        if(substTmpl == null) {
                            throw new ResourcesDbException("substituent name " + substName + " is unknown");
                        }
                        Substitution subst = new Substitution(substName, pos, this.getTemplateContainer());
                        String linktypeStr = this.getSubstMsLinktype(i);
                        if(linktypeStr != null && linktypeStr.length() > 0) {
                            try {
                                LinkageType linktype = LinkageType.forName(linktypeStr);
                                if(linktype != null) {
                                    subst.setLinkagetype1(linktype);
                                }
                            } catch(GlycoconjugateException ge) {
                                throw new ResourcesDbException("Invalid linktype name: " + linktypeStr, ge);
                            }
                        }
                        ms.addSubstitution(subst);
                    }
                }
            }
        }
        
        //*** process ms: ***
        ms.buildName();
        Monosaccharide dbMs = HibernateAccess.getMonosaccharideFromDB(ms.getName());
        if(dbMs != null) {
            this.setMs(dbMs);
            this.setId(dbMs.getDbId());
            this.processMonosaccharide(this.getMs());
        } else {
            MonosaccharideValidation.checkMonosaccharideConsistency(ms, this.getTemplateContainer(), false);
            this.setMs(ms);
            ms.buildRepresentations();
        }
        //this.processMonosaccharide(this.getMs());
        if(!MonosaccharideValidation.hasCorrectAlditolOrientation(this.getMs(), this.getTemplateContainer().getBasetypeTemplateContainer())) {
            this.setWarningString("Note: Orientation of open chain residue will be changed when finalized.");
        }
    }
    
    public void initFieldsByMsName(GlycanNamescheme scheme, String name) throws ResourcesDbException {
        MonosaccharideConverter converter = new MonosaccharideConverter(this.getTemplateContainer());
        Monosaccharide ms = converter.parseMsNamestr(name, scheme);
        //*** set size and ring closure: ***
        this.setSize(ms.getSize());
        this.setRingStart(ms.getRingStart());
        this.setRingEnd(ms.getRingEnd());
        //*** get building blocks from stereocode + core mods: ***
        ArrayList<BasetypeBuilderGroup> groupList = ms.getBasetype().toBuilderGroups();
        this.setPositions(new String[this.getSize()]);
        for(int i = 0; i < groupList.size(); i++) {
            this.getPositions()[i] = groupList.get(i).getExtStereoSymbolStr();
        }
        //*** set substitution data: ***
        int numOfSubst = ms.countSubstitutions();
        this.setSubstMsPos(new String[numOfSubst]);
        this.setSubstName(new String[numOfSubst]);
        this.setSubstMsLinktype(new String[numOfSubst]);
        for(int i = 0; i < numOfSubst; i++) {
            Substitution subst = ms.getSubstitutions().get(i);
            this.getSubstMsPos()[i] = Integer.toString(subst.getIntValuePosition1());
            this.getSubstName()[i] = subst.getName();
            this.getSubstMsLinktype()[i] = subst.getLinkagetypeStr1();
        }
    }
    
    public boolean getPositionsIsSet() {
        return this.positionsIsSet;
    }
    
    public void setPositionsIsSet(boolean isSet) {
        this.positionsIsSet = isSet;
    }
    
    public String getExtStereoString() {
        return extStereoString;
    }

    public void setExtStereoString(String posStr) {
        this.extStereoString = posStr;
    }

    public String execute() {
        try {
            this.setMainMenuItems();
            this.setSubMenuItems(EMenu.QUERY);
            this.setCurrentSubMenuItem(EMenu.QUERY_MONOSACC);
            if(this.getMsname() != null && this.getMsname().length() > 0) {
                GlycanNamescheme scheme = GlycanNamescheme.AUTO;
                this.initFieldsByMsName(scheme, this.getMsname());
            }
            if(this.getSize() == 0 || this.getPositions() == null || this.getPositions().length == 0) {
                if(this.getSize() > 3) {
                    if(this.getRingStart() < -1) {
                        this.setRingStart(1);
                    }
                    if(this.getRingEnd() < -1) {
                        if(this.getSize() == 4) {
                            this.setRingEnd(4);
                        } else {
                            this.setRingEnd(5);
                        }
                    }
                }
                return INPUT;
            }
            if((this.getRingStart() == OPEN_CHAIN && this.ringEnd != OPEN_CHAIN) || (this.getRingStart() != OPEN_CHAIN && this.ringEnd == OPEN_CHAIN)) {
                this.setErrorString("Ring start and ring end must be both set to a position or both be open chain");
                return INPUT;
            }
            String posStr = "";
            for(String ps : this.getPositions()) {
                posStr += ps;
            }
            this.setExtStereoString(posStr);
            /*if(!this.checkPositionInput()) {
                return INPUT;
            }*/
            try {
                buildMonosaccharide();
            } catch(ResourcesDbException ex) {
                this.setErrorString(ex.getMessage());
                return INPUT;
            }
            if(this.getStep() != null && this.getStep().equalsIgnoreCase("finish")) {
                this.setTab("residue");
                this.processMonosaccharide(this.getMs());
                if(this.getMs().getRingEnd() == Basetype.OPEN_CHAIN) {
                    this.getMs().buildName(); //*** re-build name of open chain residue to make sure that the correct name is set (current name might be wrong alditol orientation) ***
                }
                return SUCCESS;
            }
            return INPUT;
        } catch(Exception ex) {
            this.setCaughtException(ex);
            //System.err.println("ex: " + ex);
            //ex.printStackTrace();
            this.setErrorMsg(ex.getMessage());
            this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
            return ERROR;
        }
    }
}
