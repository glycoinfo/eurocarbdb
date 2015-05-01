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

import java.util.*;

import javax.swing.ImageIcon;


public class WorkspacePlugin implements Plugin{

    PluginManager theManager = null;
    GlycoWorkbench theApplication = null;
    GlycanWorkspace theWorkspace = null;

    WorkspacePanel theWorkspacePanel = null;    
    
    public WorkspacePlugin(GlycoWorkbench application) {
    	this.theApplication=application;
    theWorkspacePanel = new WorkspacePanel(application);
    
    }

    public void init() {
    }

    public void exit() {
    }

    public String getName() {
    return "Workspace";
    }

    public int getMnemonic() {
    return java.awt.event.KeyEvent.VK_W;
    }
    
    public ImageIcon getIcon() {
    return ThemeManager.getEmptyIcon(ICON_SIZE.TINY);
    }

    public int getViewPosition(String view) {
    return PluginManager.VIEW_LEFT;
    }

    public java.awt.Component getLeftComponent() {
    return theWorkspacePanel;
    }

    public java.awt.Component getRightComponent() {
    return null;
    }

    public java.awt.Component getBottomComponent() {
    return null;
    }
 
    public Collection<String> getViews() {
    return Collections.singleton("Workspace");
    }

    public Collection<GlycanAction> getActions() {
    return new Vector<GlycanAction>();
    }

    public Collection<GlycanAction> getToolbarActions() {
    return new Vector<GlycanAction>();
    }

    public Collection<GlycanAction> getObjectActions(Object prototype, java.awt.event.ActionListener al) {
    Vector<GlycanAction> actions = new Vector<GlycanAction>();
    return actions;
    }

    public void setManager(PluginManager manager) {
    theManager = manager;
    }

    public void setApplication(GlycoWorkbench application) {
    theApplication = application;
    theWorkspacePanel.setApplication(application);
    }
  
    public void setWorkspace(GlycanWorkspace workspace) {
    theWorkspace = workspace;
    theWorkspacePanel.setWorkspace(workspace);
    }
   
    public  PluginManager getManager() {
    return theManager;
    }

    public  GlycoWorkbench getApplication() {
    return theApplication;
    }

    public  GlycanWorkspace getWorkspace() {
    return theWorkspace;
    }  

    public void show(String view) throws Exception {
    if( !view.equals("Workspace") )
        throw new Exception("Invalid view: " + view);        
    }

    public boolean runAction(String action) throws Exception {
    throw new Exception("Invalid action: " + action);
    }

    public boolean runAction(String action, Object param) throws Exception {
    throw new Exception("Invalid action: " + action);
    }

    public void updateViews() {
    theWorkspacePanel.updateView();
    }

    public void updateMasses() {
    }
    
    @Override
	public void completeSetup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<JRibbonBand> getBandsForToolBar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RibbonTask getRibbonTask() {
		// TODO Auto-generated method stub
		return null;
	}
}