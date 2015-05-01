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

public class QueryStartAction extends MsdbDefaultAction {

    private static final long serialVersionUID = 1L;
    
    public static final String ACTIONNAME = "query.action";
    
    public static final String ITEM_TOP = "top";
    public static final String ITEM_MONOSACCHARIDE = "monosaccharide";
    public static final String ITEM_SUBSTITUENT = "substituent";
    public static final String ITEM_AGLYCON = "aglycon";
    
    private String item;
    
    public void setItem(String itemName) {
        this.item = itemName;
    }
    
    public String getItem() {
        return this.item;
    }
    
    public String execute() throws Exception {
        this.setMainMenuItems();
        if(this.item == null || this.item.length() == 0) {
            this.item = ITEM_TOP;
        }
        this.setSubMenuItems(EMenu.QUERY);
        if(this.item.equals(ITEM_MONOSACCHARIDE)) {
            this.setCurrentSubMenuItem(EMenu.QUERY_MONOSACC);
            return SUCCESS + "_" + ITEM_MONOSACCHARIDE;
        }
        if(this.item.equals(ITEM_SUBSTITUENT)) {
            this.setCurrentSubMenuItem(EMenu.QUERY_SUBST);
            return SUCCESS + "_" + ITEM_SUBSTITUENT;
        }
        /*if(this.item.equals(ITEM_AGLYCON)) {
            this.setCurrentSubMenuItem(EMenu.QUERY_AGLYCA);
            return SUCCESS + "_" + ITEM_AGLYCON;
        }*/
        //*** default: ***
        this.setCurrentSubMenuItem(EMenu.QUERY_START);
        return SUCCESS;
    }
}
