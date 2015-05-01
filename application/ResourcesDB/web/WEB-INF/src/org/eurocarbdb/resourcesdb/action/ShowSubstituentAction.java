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

import org.eurocarbdb.resourcesdb.*;
import org.eurocarbdb.resourcesdb.io.HibernateAccess;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;

public class ShowSubstituentAction extends MsdbDefaultAction {

    private static final long serialVersionUID = 1L;
    
    private String name;
    private String scheme;
    private String tab;

    private SubstituentTemplate substTmpl = null;

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
    
    public String getTab() {
        return this.tab;
    }
    
    public void setTab(String tabName) {
        this.tab = tabName;
    }
    
    public SubstituentTemplate getSubstTmpl() {
        return substTmpl;
    }

    public void setSubstTmpl(SubstituentTemplate substTmpl) {
        this.substTmpl = substTmpl;
    }
    
    public List getSubstituentLinktypeList() {
        String substHql;
        substHql = "select linkagetypeStr1 from Substitution where name='" + this.getName() + "' group by linkagetype1 order by count(*) desc, linkagetype1 asc";
        List linktypeList = null;
        try {
            linktypeList = HibernateAccess.getObjectList(substHql);
        } catch(Exception ex) {
            System.err.println("Exception: " + ex);
        }
        if(linktypeList == null) {
            linktypeList = new ArrayList();
        }
        return linktypeList;
}

    public String execute() throws Exception {
        this.setMainMenuItems();
        this.setSubMenuItems(EMenu.QUERY);
        this.setCurrentSubMenuItem(EMenu.QUERY_SUBST);
        if(this.getTab() == null) {
            this.setTab("residue");
        }
        try {
            this.setMainMenuItems();
            GlycanNamescheme sourceScheme = null;
            if(this.getName() != null) {
                if(this.getScheme() != null) {
                    sourceScheme = GlycanNamescheme.getGlycanNameschemeByNamestr(this.getScheme());
                    if(sourceScheme == null) {
                        this.setErrorMsg("Unknown notation scheme: " + this.getScheme());
                        this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
                        return ERROR;
                    }
                } else {
                    sourceScheme = GlycanNamescheme.GLYCOCT;
                }
                //SubstituentTemplate.setDataIfNotSet(new Config());
                SubstituentTemplate tmpl = this.getTemplateContainer().getSubstituentTemplateContainer().forName(sourceScheme, this.getName());
                if(tmpl == null) {
                    this.setErrorMsg("Cannot find substituent " + this.getName());
                    return ERROR;
                }
                this.setSubstTmpl(tmpl);
                this.setName(tmpl.getName());
            } else {
                return INPUT;
            }
            return SUCCESS;
        } catch(Exception ex) {
            this.setCaughtException(ex);
            System.err.println("ex: " + ex);
            ex.printStackTrace();
            this.setErrorMsg(ex.getMessage());
            this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
            return ERROR;
        }
    }
    

}
