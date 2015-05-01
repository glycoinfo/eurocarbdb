/**
 * 
 */
package org.eurocarbdb.application.glycoworkbench;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;

public class GwbWindowListener implements WindowListener{
	 

		@Override
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub
			Object source=arg0.getSource();
			if(source instanceof GlycoWorkbench){
				GlycoWorkbench gwb=(GlycoWorkbench) source;
				
				
				boolean restart=gwb.restart;
				String skin=gwb.skin;
				
				if(restart){
					synchronized(GlycoWorkbench.lock){
						
						gwb.removeWindowListener(this);
						gwb.dispose();
						gwb=null;
						System.gc();
						System.runFinalization();
						//GlycoWorkbench.done=1;
					//SubstanceLookAndFeel.setSkin(skin);
					
						//SwingUtilities.invokeLater((new Runnable(){
							//public void run(){
								//GlycoWorkbench gwb1;
								//try {
								//	gwb1 = new GlycoWorkbench();
//									gwb1.setVisible(true);
//									gwb1.addWindowListener(new GwbWindowListener());
//									System.err.println("here");
//								} catch (IOException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
								
							//}
						//}));
						
					
            		System.err.println("Restart");
					}
            	}else{
            		System.err.println("Closing");
            		
            	}
			}
			
			System.err.println("Window closed");
			
		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	
	
}