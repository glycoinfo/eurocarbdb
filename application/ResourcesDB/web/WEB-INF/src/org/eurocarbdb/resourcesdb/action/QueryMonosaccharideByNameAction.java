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

import java.util.List;

import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.io.*;
import org.eurocarbdb.resourcesdb.monosaccharide.*;

public class QueryMonosaccharideByNameAction extends ShowMonosaccharideAction {

    private static final long serialVersionUID = 1L;
    
    public static final String ACTIONNAME = "query_monosaccharide_by_name.action";
    
    private List<Monosaccharide> msResultList;
    
    private Monosaccharide uncertainMs = null;

    public List<Monosaccharide> getMsResultList() {
        return this.msResultList;
    }

    public void setMsResultList(List<Monosaccharide> msResult) {
        this.msResultList = msResult;
    }
    
    /**
     * If the ms name used in this query denotes a monosaccharide that is not fully defined, this monosaccharide will be stored in the uncertainMs field.
     * @return the value of the uncertainMs field
     */
    public Monosaccharide getUncertainMs() {
        return this.uncertainMs;
    }
    
    public void setUncertainMs(Monosaccharide uncMs) {
        this.uncertainMs = uncMs;
    }
    
    private boolean nameIsSet() {
        return (this.getName() != null && this.getName().length() > 0);
    }

    public String execute() {
        this.setMainMenuItems();
        this.setSubMenuItems(EMenu.QUERY);
        this.setCurrentSubMenuItem(EMenu.QUERY_MONOSACC);
        try {
            if(this.nameIsSet()) {
                if(GlycanNamescheme.PDB.equals(this.getSchemeObj())) {
                    List<Monosaccharide> msList = HibernateAccess.getMonosaccharideListByAliasName(this.getName(), GlycanNamescheme.PDB);
                    if(msList.size() == 1) {
                        Monosaccharide mono = msList.get(0);
                        this.processMonosaccharide(mono);
                        this.setMs(mono);
                        return SUCCESS_SINGLE;
                    } else {
                        this.setMsResultList(msList);
                        return SUCCESS_MULTIPLE;
                    }
                } else {
                    Monosaccharide mono = null;
                    try {
                        //*** parse monosaccharide name: ***
                        mono = this.parseNameParam();
                        if(mono == null) {
                            return ERROR;
                        }
                        
                        //*** check for uncertainties: ***
                        if(MonosaccharideValidation.checkFuzziness(mono)) { 
                            //*** monosaccharide has uncertain/fuzzy properties, set uncertainMs field and build hibernate query to find matching database entries: ***
                            this.setUncertainMs(mono);
                            this.processMonosaccharide(mono);
                            List<Monosaccharide> resultList;
                            resultList = HibernateAccess.getMonosaccharideListByFuzzyMonosaccharide(mono, true, true);
                            this.setMsResultList(resultList);
                            if(resultList.size() == 0) { //*** no entries match the query, thus directly display the uncertain residue ***
                                this.setMs(mono);
                                if(this.getTab() == null) {
                                    this.setTab("residue");
                                }
                                return SUCCESS_SINGLE;
                            } 
                            return SUCCESS_MULTIPLE;
                        } else {
                            //*** monosaccharide is fully defined, display single entry: ***
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
                            if(this.getTab() == null) {
                                this.setTab("residue");
                            }
                            return SUCCESS_SINGLE;
                        }
                    } catch(MonosaccharideException me) {
                        this.setCaughtException(me);
                        //System.err.println("ex: " + ex);
                        //ex.printStackTrace();
                        this.setErrorMsg("Error in parsing monosaccharide name: " + me.getMessage());
                        this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
                        return ERROR;
                    }
                }
            } else if(this.substNameIsSet()) {
                Monosaccharide ms = new Monosaccharide();
                this.addSubstituents(this.getSchemeObj(), ms);
                List<Monosaccharide> resultList = HibernateAccess.getMonosaccharideListByFuzzyMonosaccharide(ms, false, false);
                this.setMsResultList(resultList);
                return SUCCESS_MULTIPLE;
            }
            return INPUT;
        } catch(Exception ex) {
            this.setCaughtException(ex);
            this.setErrorMsg(ex.getMessage());
            this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
            return ERROR;
        }
    }    
    
}
