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
*   Last commit: $Rev: 1930 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-07-29 #$  
*/
/**
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.application.glycanbuilder.*;

import org.eurocarbdb.application.glycoworkbench.*;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

import java.util.Collection;
import java.util.List;
import java.awt.Component;

import javax.swing.ImageIcon;

public interface Plugin {
	/**
	 * Not currently used...
	 */
	public void completeSetup();

    public void init();

    public void exit();

    public String getName();

    public int getMnemonic();
    
    public ImageIcon getIcon();

    public Component getLeftComponent();    

    public Component getRightComponent();    

    public Component getBottomComponent();    

    public int getViewPosition(String view);
    
    public Collection<String> getViews();

    public Collection<GlycanAction> getActions();

    public Collection<GlycanAction> getToolbarActions();
    
    public Collection<GlycanAction> getObjectActions(Object prototype, java.awt.event.ActionListener al);
    
    public void setManager(PluginManager manager);    

    public void setApplication(GlycoWorkbench application);
    
    public void setWorkspace(GlycanWorkspace workspace);
    
    public PluginManager getManager();

    public GlycoWorkbench getApplication();

    public GlycanWorkspace getWorkspace();
    
    public void show(String view) throws Exception;

    public boolean runAction(String action) throws Exception;

    public boolean runAction(String action, Object params) throws Exception;

    public void updateViews();

    public void updateMasses();
    
    /**
     * Return a set of JRibbonBand's for your plugin - to be placed on the Tools RibbonTask
     * @return
     */
    public List<JRibbonBand> getBandsForToolBar();

    /**
     * Return a RibbonTask for your plugin - RibbonTask's are placed in the order your plugins are loaded.
     * TODO:  Allow plugins to specify their "preferred" position
     * @return
     */
    public RibbonTask getRibbonTask();
}