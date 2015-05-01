package org.eurocarbdb.application.glycoworkbench.plugin;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;

import org.eurocarbdb.application.glycanbuilder.GlycanAction;
import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;
import org.eurocarbdb.application.glycoworkbench.GlycoWorkbench;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

public class AbstractPlugin implements Plugin {
	PluginManager theManager = null;
	GlycoWorkbench theApplication = null;
	GlycanWorkspace theWorkspace = null;
	WorkspacePanel theWorkspacePanel = null;

	public AbstractPlugin(GlycoWorkbench application) {
		this.theApplication = application;
		theWorkspacePanel = new WorkspacePanel(application);
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

	public PluginManager getManager() {
		return theManager;
	}

	public GlycoWorkbench getApplication() {
		return theApplication;
	}

	public GlycanWorkspace getWorkspace() {
		return theWorkspace;
	}
	
	public void completeSetup() {
		
	}

	public List<JRibbonBand> getBandsForToolBar() {
		
		return null;
	}

	public RibbonTask getRibbonTask() {
		
		return null;
	}

	@Override
	public void exit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<GlycanAction> getActions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Component getBottomComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Component getLeftComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMnemonic() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<GlycanAction> getObjectActions(Object prototype,
			ActionListener al) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Component getRightComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<GlycanAction> getToolbarActions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getViewPosition(String view) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<String> getViews() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean runAction(String action) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean runAction(String action, Object params) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void show(String view) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMasses() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateViews() {
		// TODO Auto-generated method stub
		
	}
}
