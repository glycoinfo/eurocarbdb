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

import org.eurocarbdb.resourcesdb.*;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.GlycoconjugateException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.io.*;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.util.*;

public class ShowMonosaccharideAction extends MsdbDefaultAction {

    private static final long serialVersionUID = 1L;
    
    public static final String ACTIONNAME = "display_monosaccharide.action";
    
    private static final String HELPACTION = ShowMonosaccharideHelpAction.ACTIONNAME;
    
    private int id;
    private String name;
    private String tab;
    
    protected String[] substMsPos;
    protected String[] substName;
    protected String[] substMsLinktype;
    
    //private String carbbankName;
    private Monosaccharide ms;
    
    private int basetypeMsId = 0;
    
    public int getId() {
        return this.id;
    }
    
    public void setId(int theId) {
        this.id = theId;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String nameStr) {
        this.name = nameStr;
    }
    
    public String getTab() {
        return this.tab;
    }
    
    public void setTab(String tabName) {
        this.tab = tabName;
    }
    
    public String[] getSubstName() {
        return this.substName;
    }

    public void setSubstName(String[] substNameArr) {
        this.substName = substNameArr;
    }
    
    public String getSubstName(int i) {
        if(this.substName.length > i) {
            return this.substName[i];
        }
        return null;
    }

    public String[] getSubstMsPos() {
        return this.substMsPos;
    }

    public void setSubstMsPos(String[] posStrArr) {
        this.substMsPos = posStrArr;
    }

    public String getSubstMsPos(int i) {
        if(this.substMsPos.length > i) {
            return this.substMsPos[i];
        }
        return null;
    }
    
    public int getSubstMsPosValue(int i) {
        return NumberUtils.parseIntStr(this.getSubstMsPos(i), 0);
    }

    public String[] getSubstMsLinktype() {
        return substMsLinktype;
    }

    public void setSubstMsLinktype(String[] linktypeArr) {
        this.substMsLinktype = linktypeArr;
    }

    public String getSubstMsLinktype(int i) {
        if(this.substMsLinktype.length > i) {
            return this.substMsLinktype[i];
        }
        return null;
    }

    public void setMs(Monosaccharide mono) {
        this.ms = mono;
    }
    
    public Monosaccharide getMs() {
        return this.ms;
    }
    
    public int getBasetypeMsId() {
        return this.basetypeMsId;
    }

    public void setBasetypeMsId(int bmid) {
        this.basetypeMsId = bmid;
    }

    public String formatPositions(ArrayList<Integer> positionsList) {
        String outStr = "";
        outStr = Utils.formatPositionsString(positionsList, "/", "?");
        return outStr;
    }
    
    public boolean substNameIsSet() {
        if(this.substName.length > 0) {
            for(int i = 0; i < this.substName.length; i++) {
                if(this.substName[i] != null && this.substName[i].length() > 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    protected Monosaccharide parseNameParam() throws ResourcesDbException {
        MonosaccharideConverter converter = new MonosaccharideConverter(new Config());
        Monosaccharide mono = null;
        try {
            GlycanNamescheme sourceScheme;
            if(this.getScheme() != null) {
                sourceScheme = GlycanNamescheme.getGlycanNameschemeByNamestr(this.getScheme());
                if(sourceScheme == null) {
                    this.setErrorMsg("Unknown notation scheme: " + this.getScheme());
                    this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
                    return null;
                }
                //mono = converter.parseMsNamestr(this.getName(), sourceScheme);
            } else {
                sourceScheme = GlycanNamescheme.AUTO;
                //mono = converter.parseMsNamestr(this.getName(), GlycanNamescheme.AUTO);
            }
            mono = converter.parseMsNamestr(this.getName(), sourceScheme);
            this.addSubstituents(sourceScheme, mono);
            /*int substMsPosCount = 0;
            if(this.getSubstMsPos() != null) {
                substMsPosCount = this.getSubstMsPos().length;
            }
            int substNameCount = 0;
            if(this.getSubstName() != null) {
                substNameCount = this.getSubstName().length;
            }
            int substMsLinktypeCount = 0;
            if(this.getSubstMsLinktype() != null) {
                substMsLinktypeCount = this.getSubstMsLinktype().length;
            }
            int substCount = Math.max(Math.max(substMsPosCount, substNameCount), substMsLinktypeCount);
            for(int i = 0; i < substCount; i++) {
                Substitution subst = new Substitution();
                if(i < substMsPosCount) {
                    subst.setPosition1(this.getSubstMsPosValue(i));
                }
                SubstituentTemplate substTmpl = null;
                if(i < substNameCount) {
                    substTmpl = this.getTemplateContainer().getSubstituentTemplateContainer().forName(sourceScheme, this.getSubstName()[i]);
                    subst.setTemplate(substTmpl);
                }
                LinkageType linktype = null;
                if(i < substMsLinktypeCount) {
                    try {
                        if(this.getSubstMsLinktype()[i] != null && this.getSubstMsLinktype()[i].length() > 0) {
                            linktype = LinkageType.forName(this.getSubstMsLinktype()[i]);
                        }
                    } catch(GlycoconjugateException ge) {
                        this.setCaughtException(ge);
                        this.setErrorMsg("Error in building substitution: " + ge.getMessage());
                        this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
                        return null;
                    }
                    if(linktype == null && substTmpl != null) {
                        linktype = substTmpl.getLinkageTypeBySubstituentName(sourceScheme, this.getSubstName()[i]);
                    }
                    subst.setLinkagetype1(linktype);
                }
                subst.setSubstituentPosition1(1);
                if(subst.getTemplate() != null) {
                    mono.addSubstitution(subst);
                }
            }*/
            MonosaccharideValidation.checkMonosaccharideConsistency(mono, this.getTemplateContainer());
        } catch(ResourcesDbException me) {
            this.setCaughtException(me);
            this.setErrorMsg("Error in parsing monosaccharide name: " + me.getMessage());
            this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
            return null;
        }
        if(mono != null) {
            mono.getBasetype().buildName();
            mono.buildName();
        }
        return mono;
    }
    
    protected void addSubstituents(GlycanNamescheme sourceScheme, Monosaccharide mono) throws ResourcesDbException {
        int substMsPosCount = 0;
        if(this.getSubstMsPos() != null) {
            substMsPosCount = this.getSubstMsPos().length;
        }
        int substNameCount = 0;
        if(this.getSubstName() != null) {
            substNameCount = this.getSubstName().length;
        }
        int substMsLinktypeCount = 0;
        if(this.getSubstMsLinktype() != null) {
            substMsLinktypeCount = this.getSubstMsLinktype().length;
        }
        int substCount = Math.max(Math.max(substMsPosCount, substNameCount), substMsLinktypeCount);
        for(int i = 0; i < substCount; i++) {
            Substitution subst = new Substitution();
            if(i < substMsPosCount) {
                subst.setPosition1(this.getSubstMsPosValue(i));
            }
            SubstituentTemplate substTmpl = null;
            if(i < substNameCount) {
                substTmpl = this.getTemplateContainer().getSubstituentTemplateContainer().forName(sourceScheme, this.getSubstName()[i]);
                subst.setTemplate(substTmpl);
            }
            LinkageType linktype = null;
            if(i < substMsLinktypeCount) {
                try {
                    if(this.getSubstMsLinktype()[i] != null && this.getSubstMsLinktype()[i].length() > 0) {
                        linktype = LinkageType.forName(this.getSubstMsLinktype()[i]);
                    }
                } catch(GlycoconjugateException ge) {
                    this.setCaughtException(ge);
                    this.setErrorMsg("Error in building substitution: " + ge.getMessage());
                    this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
                    return;
                }
                if(linktype == null && substTmpl != null && sourceScheme != null && !sourceScheme.equals(GlycanNamescheme.MONOSACCHARIDEDB) && !sourceScheme.equals(GlycanNamescheme.GLYCOCT)) {
                    linktype = substTmpl.getLinkageTypeBySubstituentName(sourceScheme, this.getSubstName()[i]);
                }
                subst.setLinkagetype1(linktype);
            }
            subst.setSubstituentPosition1(1);
            if(subst.getTemplate() != null) {
                mono.addSeparateDisplaySubstitution(subst, sourceScheme, this.getTemplateContainer().getSubstituentTemplateContainer(), false);
                //mono.addSubstitution(subst);
            }
        }
    }
    
    protected void processMonosaccharide(Monosaccharide mono) throws ResourcesDbException {
        if(mono == null) {
            return;
        }
        if(mono.getName() == null || mono.getName().equals("")) {
            mono.buildName();
        }
        if(mono.getComposition() == null) {
            MonosaccharideDataBuilder.buildComposition(mono);
        }
        if(mono.getAtoms() == null) {
            try {
                MonosaccharideDataBuilder.buildAtoms(mono);
            } catch(ResourcesDbException re) {
                mono.setAtoms(null);
            }
        }
        if(mono.getConfiguration() == null) {
            mono.setConfiguration(Stereocode.getConfigurationFromStereoString(mono.getStereoStr()));
        }
        if(mono.getSynonyms() == null || mono.getSynonyms().size() == 0) {
            MonosaccharideDataBuilder.buildSynonyms(mono, this.getTemplateContainer());
        }
        if(mono.getDbId() == 0) {
            mono.buildRepresentations();
        }
        
    }
    
    public String execute() {
        this.setMainMenuItems();
        this.setSubMenuItems(EMenu.QUERY);
        this.setCurrentSubMenuItem(EMenu.QUERY_MONOSACC);
        try {
            Utils.setTemplateDataIfNotSet(getMsdbConf());
            if(this.getTab() == null) {
                this.setTab("residue");
            }
            this.setHelpAction(HELPACTION);
            this.setHelpActionArguments("#" + this.getTab());
            if(this.getId() != 0 && this.getName() != null) {
                this.setErrorMsg("Both name and id set for monosaccharide (only one of these parameters may be set).");
                this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
                return ERROR;
            }
            if(this.getId() != 0) {
                Monosaccharide dbMs = HibernateAccess.getMonosaccharideFromDB(this.getId());
                if(dbMs == null) {
                    this.setErrorMsg("Monosaccharide Id " + this.getId() + " not found.");
                    this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
                    return ERROR;
                } else {
                    this.setMs(dbMs);
                    dbMs.setTemplateContainer(this.getTemplateContainer());
                    this.processMonosaccharide(dbMs);
                }
            } else if(this.getName() != null) {
                Monosaccharide mono = this.parseNameParam();
                if(mono == null) {
                    return ERROR;
                }
                Monosaccharide dbMs = HibernateAccess.getMonosaccharideFromDB(mono.getName());
                if(dbMs != null) {
                    if(dbMs.getConfiguration() == null) {
                        String stereo = dbMs.getStereoStr();
                        if(dbMs.getRingStart() > 0) {
                            stereo = Stereocode.setPositionInStereoString(stereo, StereoConfiguration.Nonchiral, dbMs.getRingStart());
                        }
                        dbMs.setConfiguration(Stereocode.getConfigurationFromStereoString(stereo));
                    }
                    mono = dbMs;
                    this.setId(dbMs.getDbId());
                }
                this.setMs(mono);
                this.processMonosaccharide(mono);
            } else {
                return INPUT;
            }
            this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - show entry " + this.getMs().getName());
            if(!this.getMs().getName().equals(this.getMs().getBasetype().getName())) {
                Monosaccharide basetypeDbMs = HibernateAccess.getMonosaccharideFromDB(this.getMs().getBasetype().getName());
                if(basetypeDbMs != null) {
                    this.setBasetypeMsId(basetypeDbMs.getDbId());
                }
            }
            return SUCCESS;
        } catch(Exception ex) {
            this.setCaughtException(ex);
            //System.err.println("ex: " + ex);
            //ex.printStackTrace();
            this.setErrorMsg(ex.getMessage());
            this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
            return ERROR;
        }
    }
    
    /**
     * method to test the workflow of this action without tomcat in eclipse
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ShowMonosaccharideAction testAction = new ShowMonosaccharideAction();
        testAction.setId(4);
        String resultStr = testAction.execute();
        if(resultStr.equals(ERROR)) {
            System.err.println("error: " + testAction.getErrorMsg());
            System.err.println("caught exc.: " + testAction.getCaughtException());
        } else {
            System.out.println("ms: " + testAction.getMs());
        }
    }
}
