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

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.io.HibernateAccess;
import org.eurocarbdb.resourcesdb.io.NameParsingException;
import org.eurocarbdb.resourcesdb.representation.ResidueRepresentationFormat;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;

import com.opensymphony.xwork.ActionSupport;

/**
* The default action class for MonoSaccharideDB actions.
* This class is used by actions that only display static content.
* Furthermore, it holds some fields and methods that are used by various other actions, e.g. for menu selections and error handling.
* @author Thomas Lütteke
*
*/
public class MsdbDefaultAction extends ActionSupport {

    private static final long serialVersionUID = 1L;

    public static final String SUCCESS_SINGLE = SUCCESS + "_single";
    public static final String SUCCESS_MULTIPLE = SUCCESS + "_multiple";
    
    public static final String MSDB_TITLE = "MonoSaccharideDB";
    
    private String title = MSDB_TITLE;
    private String subTitle = null;
    
    public static final String ACTIONNAME = "start.action";
    
    private String errorMsg = null;
    private Exception caughtException = null;
    
    private String helpAction = null;
    private String helpActionArguments = null;
    
    private IMenuItem currentMainMenuItem = null;
    private IMenuItem currentSubMenuItem = null;
    
    private ArrayList<IMenuItem> mainMenuItems = null;
    private ArrayList<IMenuItem> subMenuItems = null;
    private String subMenu = null;
    
    private Config msdbConf = Config.getGlobalConfig();
    private TemplateContainer templateContainer;
    
    private String scheme;
    private GlycanNamescheme schemeObj;

    private ResidueRepresentationFormat defaultGraphicsFormat = ResidueRepresentationFormat.PNG;
    
    public void setTitle(String theTitle) {
        this.title = theTitle;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setSubTitle(String theTitle) {
        this.subTitle = theTitle;
    }
    
    public String getSubTitle() {
        return this.subTitle;
    }
    
    public void setErrorMsg(String theError) {
        this.errorMsg = theError;
    }
    
    public String getErrorMsg() {
        return this.errorMsg;
    }
    
    public Exception getCaughtException() {
        return this.caughtException;
    }

    public void setCaughtException(Exception caughtExc) {
        this.caughtException = caughtExc;
        System.err.println("\nMsdbWeb caught exception: " + caughtExc);
        if(caughtExc.getClass().equals(NameParsingException.class)) {
            System.err.println(((NameParsingException) caughtExc).buildExplanationString());
        }
        caughtExc.printStackTrace();
    }

    public String getHelpAction() {
        return this.helpAction;
    }

    public void setHelpAction(String ha) {
        this.helpAction = ha;
    }

    public String getHelpActionArguments() {
        return this.helpActionArguments;
    }

    public void setHelpActionArguments(String haa) {
        this.helpActionArguments = haa;
    }

    public Config getMsdbConf() {
        return this.msdbConf;
    }

    public void setMsdbConf(Config conf) {
        this.msdbConf = conf;
    }
    
    public Config getConfig() {
        return this.msdbConf;
    }

    public void setConfig(Config conf) {
        this.msdbConf = conf;
    }
    
    public TemplateContainer getTemplateContainer() {
        if(this.templateContainer == null) {
            this.templateContainer = new TemplateContainer(this.getConfig());
        }
        return this.templateContainer;
    }
    
    public void setTemplateContainer(TemplateContainer container) {
        this.templateContainer = container;
    }
    
    /**
     * Get the name scheme parameter
     * @return
     */
    public String getScheme() {
        return this.scheme;
    }
    
    /**
     * Set the namescheme parameter.
    * @param schemeStr the namescheme string
     */
    public void setScheme(String schemeStr) {
        this.scheme = schemeStr;
    }
    
    /**
     * Set the namescheme parameter.
     * This method is an alias for <code>setScheme(String)</code>, so that both "scheme" and "namescheme" can be used as html parameters for the actions.
     * @param schemeStr the namescheme string
     */
    public void setNamescheme(String schemeStr) {
        this.scheme = schemeStr;
    }
    
    /**
     * Get the GlycanNamescheme Object.
     * If this object is null and the GlycanNamescheme String is not null, the Object is set using the scheme String before it is returned.
     * @return the GlycanNamescheme
     */
    public GlycanNamescheme getSchemeObj() {
        if(this.schemeObj == null) {
            if(this.getScheme() != null) {
                this.setSchemeObj(GlycanNamescheme.forName(this.getScheme()));
            }
        }
        return this.schemeObj;
    }

    /**
     * Set the GlycanNamescheme Object
     * @param schemeObject the GlycanNamescheme to set
     */
    public void setSchemeObj(GlycanNamescheme schemeObject) {
        this.schemeObj = schemeObject;
    }

    /**
     * Get the default graphics format.
     * @return the default graphics format
     */
    public ResidueRepresentationFormat getDefaultGraphicsFormat() {
        return this.defaultGraphicsFormat;
    }

    /**
     * Set the defaults graphics format.
     * @param dgf the graphics format to be used as default
     */
    public void setDefaultGraphicsFormat(ResidueRepresentationFormat dgf) {
        this.defaultGraphicsFormat = dgf;
    }
    
    //********* Menu Items: ****************************************************

    /**
     * Get the current main menu item (the one to be highlighted in the display).
     * @return the current main menu item
     */
    public IMenuItem getCurrentMainMenuItem() {
        return this.currentMainMenuItem;
    }

    /**
     * Set the current main menu item (the one to be highlighted in the display).
     * @param mainMenuItemName the main menu item to set
     */
    public void setCurrentMainMenuItem(IMenuItem mainMenuItem) {
        this.currentMainMenuItem = mainMenuItem;
    }

    /**
     * Set the current main menu item (the one to be highlighted in the display) by its name.
     * @param mainMenuItemName the name of the main menu item to set
     */
    public void setCurrentMainMenuItemStr(String mainMenuItemName) {
        IMenuItem mainMenuItem = EMenu.forName(mainMenuItemName);
        this.setCurrentMainMenuItem(mainMenuItem);
    }

    /**
     * Get the current sub menu item (the one to be highlighted in the display).
     * @return the current sub menu item
     */
    public IMenuItem getCurrentSubMenuItem() {
        return this.currentSubMenuItem;
    }

    /**
     * Set the current sub menu item (the one to be highlighted in the display).
     * @param subMenuItemName the sub menu item to set
     */
    public void setCurrentSubMenuItem(IMenuItem subMenuItem) {
        this.currentSubMenuItem = subMenuItem;
        if(subMenuItem != null && subMenuItem.getParent() != null) {
            this.setCurrentMainMenuItem(subMenuItem.getParent());
        }
    }
    
    /**
     * Set the current sub menu item (the one to be highlighted in the display) by its name.
     * @param subMenuItemName the name of the sub menu item to set
     */
    public void setCurrentSubMenuItem(String subMenuItemName) {
        IMenuItem subMenuItem = EMenu.forName(subMenuItemName);
        this.setCurrentSubMenuItem(subMenuItem);
    }

    /**
     * Get the main menu items.
     * If the field holding these items is not yet set, this is done before returning the items.
     * @return a list of main menu items
     */
    public ArrayList<IMenuItem> getMainMenuItems() {
        if(this.mainMenuItems == null) {
            this.setMainMenuItems();
        }
        return this.mainMenuItems;
    }
    
    /**
     * Fill the field holding the main menu items.
     */
    public void setMainMenuItems() {
        ArrayList<IMenuItem> menuList = new ArrayList<IMenuItem>();
        for(IMenuItem item : EMenu.values()) {
            if(item.getParent() == null) {
                menuList.add(item);
            }
        }
        this.mainMenuItems = menuList;
    }
    
    /**
     * Get a list of sub menu items to display.
     * @return a list of sub menu items
     */
    public ArrayList<IMenuItem> getSubMenuItems() {
        if((this.subMenuItems == null || this.subMenuItems.size() == 0) && this.subMenu != null) {
            this.setSubMenu(this.subMenu);
        }
        return this.subMenuItems;
    }
    
    /**
     * Set the subMenu.
     * @param parent the main menu item, to which the submenu corresponds
     */
    public void setSubMenuItems(IMenuItem parent) {
        this.setCurrentMainMenuItem(parent);
        ArrayList<IMenuItem> menuList = new ArrayList<IMenuItem>();
        for(IMenuItem item : EMenu.values()) {
            if(item.getParent() != null && item.getParent().equals(parent)) {
                menuList.add(item);
            }
        }
        this.subMenuItems = menuList;
    }
    
    /**
     * Set the subMenu field.
     * Using this name, the currentSubMenuItem and currentMainMenuItem are set as well.
     * @param parentName the name of the main menu item, to which the submenu corresponds
     */
    public void setSubMenu(String submenuName) {
        this.subMenu = submenuName;
        IMenuItem subMenuItem = EMenu.forName(submenuName);
        if(subMenuItem != null) {
            this.setCurrentSubMenuItem(subMenuItem);
            this.setCurrentMainMenuItem(subMenuItem.getParent());
        }
    }
    
    private int entriesCount = -1;
    
    /**
     * Get the number of monosaccharide entries that are present in the database
     * @return the number of entries
     */
    public int getEntriesCount() {
        if(this.entriesCount < 0) {
            try {
                String hqlString = "select count(*) as num from Monosaccharide";
                List<?> result = HibernateAccess.getObjectList(hqlString);
                this.entriesCount = ((Integer) result.get(0)).intValue();
            } catch(Exception ex) {
                System.err.println(ex);
                ex.printStackTrace();
                return 0;
            }
        }
        return this.entriesCount;
    }
    
    /**
     * Set the field that holds the number of monosaccharide entries that are present in the database.
     * @param count
     */
    public void setEntriesCount(int count) {
        this.entriesCount = count;
    }
    
    public String execute() throws Exception {
        this.setMainMenuItems();
        if(this.getSubMenuItems() == null) {
            this.setSubMenuItems(EMenu.HOME);
        }
        if(this.getCurrentSubMenuItem() == null) {
            this.setCurrentSubMenuItem(EMenu.HOME_START);
        }
        if(this.getSubTitle() != null) {
            this.setTitle(this.getTitle() + " - " + this.getSubTitle());
        }
        return SUCCESS;
    }
    
}
