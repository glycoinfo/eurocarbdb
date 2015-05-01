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

public enum EMenu implements IMenuItem {
    
    HOME("home", "start.action", null),
    NOTATION("notation", "notation.action", null),
    QUERY("query", "query.action", null),
    HOME_START("introduction", "start.action", EMenu.HOME),
    HOME_CONTACT("contact", "contact.action", EMenu.HOME),
    HOME_ABOUT("about", "about.action", EMenu.HOME),
    NOTATION_START("introduction", "notation.action", EMenu.NOTATION),
    NOTATION_MONOSACC("monosaccharides", "notation.action?topic=" + NotationAction.TOPIC_MONOSACC, EMenu.NOTATION),
    NOTATION_BASETYPE("basetypes", "notation.action?topic=" + NotationAction.TOPIC_BASETYPE, EMenu.NOTATION),
    NOTATION_SUBST("substituents", "notation.action?topic=" + NotationAction.TOPIC_SUBST, EMenu.NOTATION),
    //NOTATION_AGLYCA("aglyca", "notation.action?topic=" + NotationAction.TOPIC_AGLYCA, EMenu.NOTATION),
    NOTATION_SCHEMES("notation schemes", "notation.action?topic=" + NotationAction.TOPIC_SCHEMES, EMenu.NOTATION),
    QUERY_START("overview", "query.action", EMenu.QUERY),
    QUERY_MONOSACC("Monosaccharide", "query.action?item=" + QueryStartAction.ITEM_MONOSACCHARIDE, EMenu.QUERY),
    QUERY_SUBST("Substituent", "query.action?item=" + QueryStartAction.ITEM_SUBSTITUENT, EMenu.QUERY),
    //QUERY_AGLYCA("Aglycon", "query.action?item=" + QueryStartAction.ITEM_AGLYCON, EMenu.QUERY),
    QUERY_ELEMENT("Element", "query_element.action", EMenu.QUERY);
    
    public String label = null;
    public IMenuItem parent = null;
    public String actionName = null;

    public String getLabel() {
        return this.label;
    }
    
    public void setLabel(String theName) {
        this.label = theName;
    }
    
    public IMenuItem getParent() {
        return this.parent;
    }
    
    public void setParent(IMenuItem parentMenu) {
        this.parent = parentMenu;
    }
    
    public boolean isSubmenu() {
        return(this.parent != null);
    }
    
    public String getActionName() {
        return this.actionName;
    }
    
    public void setActionName(String actionStr) {
        this.actionName = actionStr;
    }
    
    public static EMenu forName(String nameStr) {
        for(EMenu menu: EMenu.values()) {
            if(menu.name().equalsIgnoreCase(nameStr)) {
                return menu;
            }
        }
        return null;
    }
    
    private EMenu(String itemName, String action, IMenuItem parentMenu) {
        this.setLabel(itemName);
        this.setActionName(action);
        this.setParent(parentMenu);
    }

}
