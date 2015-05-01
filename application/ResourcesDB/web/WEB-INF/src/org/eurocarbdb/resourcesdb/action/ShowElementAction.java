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

import org.eurocarbdb.resourcesdb.atom.*;

public class ShowElementAction extends MsdbDefaultAction {

    public static final String ACTIONNAME = "show_element.action";
    
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String symbol;
    
    private Periodic element;
    
    public int getId() {
        return this.id;
    }
    
    public void setId(int theId) {
        this.id = theId;
    }
    
    public String getSymbol() {
        return this.symbol;
    }
    
    public void setSymbol(String symbolStr) {
        this.symbol = symbolStr;
    }
    
    public Periodic getElement() {
        return this.element;
    }
    
    public void setElement(Periodic elem) {
        this.element = elem;
    }
    public String execute() {
        this.setMainMenuItems();
        this.setSubMenuItems(EMenu.QUERY);
        this.setCurrentSubMenuItem(EMenu.QUERY_ELEMENT);
        if(this.getId() != 0 && this.getSymbol() != null) {
            this.setErrorMsg("Both symbol and periodic number set for element (only one of these parameters may be set)");
            this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
            return ERROR;
        }
        try {
            Periodic.setDataIfNotSet(getMsdbConf());
            if(this.getId() != 0) {
                this.setElement(Periodic.getElementByNumber(this.getId()));
                if(this.getElement() == null) {
                    this.setErrorMsg(this.getId() + "is not a valid element id.");
                }
                return SUCCESS;
            } else if(this.getSymbol() != null) {
                this.setElement(Periodic.getElementBySymbol(this.getSymbol()));
                if(this.getElement() == null) {
                    this.setErrorMsg(this.getSymbol() + "is not a valid element symbol.");
                }
                return SUCCESS;
            } else {
                return INPUT;
            }
        } catch(Exception ex) {
            this.setCaughtException(ex);
            this.setErrorMsg(ex.getMessage());
            this.setTitle(MsdbDefaultAction.MSDB_TITLE + " - error");
            return ERROR;
        }
    }
    
}
