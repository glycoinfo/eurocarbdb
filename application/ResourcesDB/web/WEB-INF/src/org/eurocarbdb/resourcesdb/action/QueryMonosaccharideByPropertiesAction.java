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
import java.util.List;

import org.eurocarbdb.resourcesdb.io.*;

public class QueryMonosaccharideByPropertiesAction extends MsdbDefaultAction {

    private static final long serialVersionUID = 1L;
    
    public static final String ACTIONNAME = "query_monosaccharide_by_properties.action";
    
    private int id = 0;
    private int basetypeId = 0;
    private String substName = null;
    private int size = 0;
    private String parentname = null;
    private String anomer = null;
    private String configuration = null;
    private String stereocode = null;
    private ArrayList<Integer> corePosList = new ArrayList<Integer>();
    private ArrayList<String> coreModList = new ArrayList<String>();
    private ArrayList<Integer> substPosList = new ArrayList<Integer>();
    private ArrayList<String> substNameList = new ArrayList<String>();
    
    private String logicalOperator = "and";
    
    private List msResultList = null;
    private String hqlStr = null;
    private int usedParameters = 0;
    
    public String getAnomer() {
        return this.anomer;
    }

    public void setAnomer(String anomerStr) {
        this.anomer = anomerStr.toLowerCase();
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configurationStr) {
        this.configuration = configurationStr.toLowerCase();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int theId) {
        this.id = theId;
    }

    public int getBasetypeId() {
        return basetypeId;
    }

    public void setBasetypeId(int theId) {
        this.basetypeId = theId;
    }

    public String getSubstName() {
        return substName;
    }

    public void setSubstName(String nameStr) {
        this.substName = nameStr;
    }

    public List getMsResultList() {
        return this.msResultList;
    }

    public void setMsResultList(List msResult) {
        this.msResultList = msResult;
    }

    public String getParentname() {
        return this.parentname;
    }

    public void setParentname(String nameStr) {
        this.parentname = nameStr.toLowerCase();
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int theSize) {
        this.size = theSize;
    }

    public String getStereocode() {
        return this.stereocode;
    }

    public void setStereocode(String stereocodeStr) {
        this.stereocode = stereocodeStr;
    }
    
    public String getCoreMod1() {
        if(this.coreModList.size() < 1) {
            return null;
        }
        return this.coreModList.get(0);
    }

    public void setCoreMod1(String coreModStr) {
        while(this.coreModList.size() < 1) {
            this.coreModList.add((String)null);
        }
        this.coreModList.set(0, coreModStr);
    }

    public String getCoreMod2() {
        if(this.coreModList.size() < 2) {
            return null;
        }
        return this.coreModList.get(1);
    }

    public void setCoreMod2(String coreModStr) {
        while(this.coreModList.size() < 2) {
            this.coreModList.add((String)null);
        }
        this.coreModList.set(1, coreModStr);
    }

    public String getCoreMod3() {
        if(this.coreModList.size() < 3) {
            return null;
        }
        return this.coreModList.get(2);
    }

    public void setCoreMod3(String coreModStr) {
        while(this.coreModList.size() < 3) {
            this.coreModList.add((String)null);
        }
        this.coreModList.set(2, coreModStr);
    }
    
    public int getCorePos1() {
        if(this.corePosList.size() < 1) {
            return(0);
        }
        return this.corePosList.get(0).intValue();
    }
    
    public void setCorePos1(int pos1) {
        while(this.corePosList.size() < 1) {
            this.corePosList.add((Integer)null);
        }
        this.corePosList.set(0, new Integer(pos1));
    }

    public int getCorePos2() {
        if(this.corePosList.size() < 2) {
            return(0);
        }
        return this.corePosList.get(1).intValue();
    }
    
    public void setCorePos2(int pos2) {
        while(this.corePosList.size() < 2) {
            this.corePosList.add((Integer)null);
        }
        this.corePosList.set(1, new Integer(pos2));
    }

    public int getCorePos3() {
        if(this.corePosList.size() < 3) {
            return(0);
        }
        return this.corePosList.get(2).intValue();
    }
    
    public void setCorePos3(int pos3) {
        while(this.corePosList.size() < 3) {
            this.corePosList.add((Integer)null);
        }
        this.corePosList.set(2, new Integer(pos3));
    }

    public String getSubstName1() {
        if(this.substNameList.size() < 1) {
            return null;
        }
        return this.substNameList.get(0);
    }

    public void setSubstName1(String substStr) {
        while(this.substNameList.size() < 1) {
            this.substNameList.add((String)null);
        }
        this.substNameList.set(0, substStr);
    }

    public String getSubstName2() {
        if(this.substNameList.size() < 2) {
            return null;
        }
        return this.substNameList.get(1);
    }

    public void setSubstName2(String substStr) {
        while(this.substNameList.size() < 2) {
            this.substNameList.add((String)null);
        }
        this.substNameList.set(1, substStr);
    }

    public String getSubstName3() {
        if(this.substNameList.size() < 3) {
            return null;
        }
        return this.substNameList.get(2);
    }

    public void setSubstName3(String substStr) {
        while(this.substNameList.size() < 3) {
            this.substNameList.add((String)null);
        }
        this.substNameList.set(2, substStr);
    }

    public int getSubstPos1() {
        if(this.substPosList.size() < 1) {
            return(0);
        }
        return this.substPosList.get(0).intValue();
    }
    
    public void setSubstPos1(int pos1) {
        while(this.substPosList.size() < 1) {
            this.substPosList.add((Integer)null);
        }
        this.substPosList.set(0, new Integer(pos1));
    }

    public int getSubstPos2() {
        if(this.substPosList.size() < 2) {
            return(0);
        }
        return this.substPosList.get(1).intValue();
    }
    
    public void setSubstPos2(int pos2) {
        while(this.substPosList.size() < 2) {
            this.substPosList.add((Integer)null);
        }
        this.substPosList.set(1, new Integer(pos2));
    }

    public int getSubstPos3() {
        if(this.substPosList.size() < 3) {
            return(0);
        }
        return this.substPosList.get(2).intValue();
    }
    
    public void setSubstPos3(int pos3) {
        while(this.substPosList.size() < 3) {
            this.substPosList.add((Integer)null);
        }
        this.substPosList.set(2, new Integer(pos3));
    }

    public String getHqlStr() {
        return this.hqlStr;
    }

    public void setHqlStr(String theHqlStr) {
        this.hqlStr = theHqlStr;
    }

    public String getLogicalOperator() {
        return this.logicalOperator;
    }

    public void setLogicalOperator(String logicalOperatorStr) {
        this.logicalOperator = logicalOperatorStr;
    }

    private void addHqlParam(String param) {
        if(this.usedParameters == 0) {
            this.hqlStr += " where ";
        } else {
            this.hqlStr += " " + this.getLogicalOperator() + " ";
        }
        this.hqlStr += param;
    }
    
    private List substituentTemplateNamesList = null;
    
    public List getSubstituentTemplateNamesList() {
        if(this.substituentTemplateNamesList == null) {
            String substHql;
            substHql = "select name from Substitution group by name order by count(*) desc, name asc";
            List substList = null;
            try {
                substList = HibernateAccess.getObjectList(substHql);
            } catch(Exception ex) {
                System.err.println("Exception: " + ex);
            }
            if(substList == null) {
                substList = new ArrayList();
            }
            this.substituentTemplateNamesList = substList;
        }
        return this.substituentTemplateNamesList;
    }
    
    public String execute() throws Exception {
        this.setMainMenuItems();
        this.setSubMenuItems(EMenu.QUERY);
        this.setCurrentSubMenuItem(EMenu.QUERY_MONOSACC);
        if(this.getLogicalOperator() == null || !(this.getLogicalOperator().equalsIgnoreCase("and") || this.getLogicalOperator().equalsIgnoreCase("or"))) {
            this.setLogicalOperator("and");
        }
        try {
            this.hqlStr = "select distinct ms from Monosaccharide ms";
            
            //*** add core modification restrictions: ***
            if(this.coreModList != null && this.coreModList.size() > 0) {
                this.hqlStr += " inner join ms.basetype msBasetype";
                for(int i = 0; i < this.coreModList.size(); i++) {
                    String modName = this.coreModList.get(i);
                    if(modName != null && modName.length() > 0) {
                        this.hqlStr += " inner join msBasetype.coreModifications mod" + i + " with mod" + i + ".name='" + modName + "'";
                        if(this.corePosList != null && this.corePosList.size() > i) {
                            Integer pos = this.corePosList.get(i);
                            if(pos != null && pos.intValue() > 0) {
                                this.hqlStr += " and mod" + i + ".intValuePosition1=" + pos.intValue();
                            }
                        }
                    }
                }
            }
            //*** add substitution restrictions: ***
            if(this.substNameList != null && this.substNameList.size() > 0) {
                for(int i = 0; i < this.substNameList.size(); i++) {
                    String substName = this.substNameList.get(i);
                    if(substName != null && substName.length() > 0) {
                        this.hqlStr += " inner join ms.substitutions subst" + i + " with subst" + i + ".name='" + substName + "'";
                        if(this.substPosList != null && this.substPosList.size() > i) {
                            Integer pos = this.substPosList.get(i);
                            this.hqlStr += " and subst" + i + ".intValuePosition1=" + pos.intValue(); 
                        }
                    }
                }
            }
            
            //*** add basetype restrictions: ***
            if(this.getBasetypeId() != 0) {
                this.addHqlParam("ms.basetype.id=" + this.getBasetypeId());
                this.usedParameters ++;
            }
            if(this.getSize() != 0) {
                this.addHqlParam("ms.basetype.size=" + this.getSize());
                this.usedParameters ++;
            }
            if(this.getAnomer() != null && !this.getAnomer().equals("") && !this.getAnomer().equals("x")) {
                this.addHqlParam("ms.basetype.anomerSymbol='" + this.getAnomer() + "'");
                this.usedParameters ++;
            }
            if(this.getConfiguration() != null && !this.getConfiguration().equals("") && !this.getConfiguration().equals("x")) {
                this.addHqlParam("ms.basetype.configurationSymbol='" + this.getConfiguration().toUpperCase() + "'");
                this.usedParameters ++;
            }
            if(this.getSubstName() != null) {
                this.addHqlParam("ms.substitutions.name like '" + this.getSubstName() + "'");
                this.usedParameters ++;
            }
            if(this.getStereocode() != null && this.getStereocode().length() > 0) {
                this.addHqlParam("ms.basetype.stereocode like '" + this.getStereocode() + "'");
                this.usedParameters ++;
            }
            if(this.getParentname() != null && this.getParentname().length() > 0) {
                //TODO: change criteria for trivial names and search carbbank names
                if(this.getParentname().equals("fuc")) {
                    this.addHqlParam("ms.synonyms.name like'%Fuc%'");
                } else if(this.getParentname().equals("rha")) {                    
                    this.addHqlParam("ms.synonyms.name like'%Rha%'");
                } else if(this.getParentname().equals("neu")) {
                    this.addHqlParam("ms.synonyms.name like'%Neu%'");
                } else {
                    this.addHqlParam("ms.basetype.name like '%" + this.getParentname() + "%'");
                }
                this.usedParameters ++;
            }
            if(this.coreModList != null && this.coreModList.size() > 0) {
                this.usedParameters += this.coreModList.size();
            }
            if(this.substNameList != null && this.substNameList.size() > 0) {
                this.usedParameters += this.substNameList.size();
            }
            
            List msResults = HibernateAccess.getObjectList(this.hqlStr);
            this.setMsResultList(msResults);
            
            if(this.usedParameters == 0) {
                this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - query monosaccharide");
                return INPUT;
            }
        } catch(Exception ex) {
            this.setCaughtException(ex);
            //System.err.println("ex: " + ex);
            //ex.printStackTrace();
            this.setErrorMsg(ex.getMessage());
            this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
            return ERROR;
        }
        this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - query monosaccharide results");
        return SUCCESS;
    }
}
