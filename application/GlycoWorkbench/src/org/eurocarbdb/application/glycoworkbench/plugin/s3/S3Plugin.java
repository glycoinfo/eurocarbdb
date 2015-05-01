package org.eurocarbdb.application.glycoworkbench.plugin.s3;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.eurocarbdb.application.glycanbuilder.GlycanAction;
import org.eurocarbdb.application.glycanbuilder.ICON_SIZE;
import org.eurocarbdb.application.glycanbuilder.ThemeManager;
import org.eurocarbdb.application.glycoworkbench.GlycoWorkbench;
import org.eurocarbdb.application.glycoworkbench.plugin.AbstractPlugin;
import org.jets3t.service.S3ServiceException;

public class S3Plugin extends AbstractPlugin implements ActionListener{
	public static final String SHOW_S3_MANAGER="showS3Manager";
	public static final String S3_PLUGIN_NAME="S3 Plugin";
	public Cockpit s3Manager;
	public JFrame s3ManagerFrame;
	
	public S3Plugin(GlycoWorkbench application) {
		super(application);
		//for (Window window : Window.getWindows()) {
		//SwingUtilities.updateComponentTreeUI(window);
		//}
		try {
			s3ManagerFrame=new JFrame("S3Manager");
			this.s3Manager=new Cockpit(s3ManagerFrame);
			//s3ManagerFrame.setVisible(true);
		} catch (S3ServiceException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
	
	@Override
	public Collection<GlycanAction> getActions() {
		Vector<GlycanAction> actions = new Vector<GlycanAction>();
		actions.add(
				new GlycanAction(
						SHOW_S3_MANAGER,
						ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),
						"Display the S3 manager",
						KeyEvent.VK_C, "", this
				)
		);
		
		return actions;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String actionName=GlycanAction.getAction(e);
		if(actionName.equals(SHOW_S3_MANAGER)){
			this.s3ManagerFrame.setVisible(true);
			if(!this.s3Manager.isLoggedIn()){
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						s3Manager.loginEvent(null);
					}
					
				});
			}
		}
	}
	
	@Override
	public String getName(){
		return S3_PLUGIN_NAME;
	}
	
	@Override
	public void completeSetup(){
		
	}
}
