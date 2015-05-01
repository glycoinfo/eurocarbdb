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
/*
 * SearchDialog.java
 *
 * Created on 07 August 2008, 16:40
 */

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.application.glycanbuilder.*;
import org.eurocarbdb.application.glycoworkbench.*;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import java.net.MalformedURLException;
import java.text.*;
import java.awt.print.*;

/**
 * 
 * @author aceroni
 */
public class SearchDialog extends JRibbonFrame implements ActionListener,
		MouseListener {

	private ProfilerPlugin theProfiler;
	private BuilderWorkspace theWorkspace;
	private GlycanCanvas theCanvas;

	private StructureDictionary return_database = null;
	private Glycan return_structure = null;
	private String return_type = "";
	private String return_source = "";
	private boolean include_redend = false;
	private boolean include_all_leafs = false;
	protected String return_status;
	protected Runnable run;
	
	public void setRunnable(Runnable _run){
		this.run=_run;
	}
	
	
	public String getReturnStatus(){
		return return_status;
	}

	/**
	 * Creates new form SearchDialog
	 * 
	 * @throws MalformedURLException
	 */
	public SearchDialog(java.awt.Frame parent, ProfilerPlugin profiler,
			boolean search_again, GraphicOptions gopt)
			throws MalformedURLException {
		super();

		// create environment
		theWorkspace = new BuilderWorkspace(null, false);
		theWorkspace.getGraphicOptions().copy(gopt);
		theWorkspace.getGraphicOptions().SHOW_MASSES_CANVAS = false;
		theWorkspace.getGraphicOptions().SHOW_REDEND_CANVAS = false;

		theProfiler = profiler;

		// init components
		initComponents();
		initCanvas();
		initData(search_again);
		setTraversal();
		setActions();

		this.setLocationRelativeTo(parent);
		this.setResizable(false);
	}

	public boolean getIncludeRedEnd() {
		return include_redend;
	}

	public boolean getIncludeAllLeafs() {
		return include_all_leafs;
	}

	public StructureDictionary getDatabase() {
		return return_database;
	}

	public void setDatabase(StructureDictionary database) {
		if (database != null && field_database.isEnabled())
			field_database.setSelectedItem(database.getDictionaryName());
		return_database = database;
	}

	public Glycan getStructure() {
		return return_structure;
	}

	public void setStructure(Glycan s) {
		if (s != null && !s.isEmpty()) {
			theWorkspace.getStructures().clear();
			theWorkspace.getStructures().addStructure(s);
		}
		return_structure = s;
	}

	public String getType() {
		return return_type;
	}

	public String getSource() {
		return return_source;
	}

	private void initCanvas() throws MalformedURLException {

		// set layout
		panel_canvas.setLayout(new BorderLayout());

		// create canvas
		theCanvas = new GlycanCanvas(null, theWorkspace, new ThemeManager(
				"/icons", this.getClass()));

		// set toolbars
		JPanel northTbPanel = new javax.swing.JPanel();
		northTbPanel.setLayout(new BorderLayout());

		for (Component component : theCanvas.getToolBarStructure()
				.getComponents()) {
			component.setEnabled(true);
			component.setVisible(true);
		}
		theCanvas.getToolBarStructure().setEnabled(true);
		theCanvas.getToolBarStructure().setVisible(true);
		northTbPanel.add(theCanvas.getToolBarStructure(), BorderLayout.NORTH);
		northTbPanel.add(theCanvas.getToolBarProperties(), BorderLayout.CENTER);
		panel_canvas.add(northTbPanel, BorderLayout.NORTH);

		// set canvas
		JScrollPane sp = new JScrollPane(theCanvas);
		theCanvas.setScrollPane(sp);
		panel_canvas.add(sp, BorderLayout.CENTER);

		// listeners
		theCanvas.addMouseListener(this);
	}

	private void initData(boolean search_again) {

		field_database.setModel(new javax.swing.DefaultComboBoxModel(
				theProfiler.getDictionaryNames().toArray(new String[0])));
		field_database.setEnabled(!search_again);

		field_type.setText("");
		field_source.setText("");
	}

	private void setTraversal() {
		CustomFocusTraversalPolicy tp = new CustomFocusTraversalPolicy();

		tp.addComponent(field_database);
		tp.addComponent(field_source);
		tp.addComponent(field_type);

		tp.addComponent(button_search);
		tp.addComponent(button_search_core);
		tp.addComponent(button_search_terminal);
		tp.addComponent(button_cancel);

		this.setFocusTraversalPolicy(tp);

		getRootPane().setDefaultButton(button_search);
	}

	private void setActions() {
		button_search.addActionListener(this);
		button_search_core.addActionListener(this);
		button_search_terminal.addActionListener(this);
		button_cancel.addActionListener(this);
	}

	// Listeners

	private void retrieveData() {
		if (field_database.isEnabled())
			return_database = theProfiler.getDictionary((String) field_database
					.getSelectedItem());
		else
			return_database = null;

		return_structure = theWorkspace.getStructures().getFirstStructure();
		if (return_structure == null)
			return_structure = new Glycan();

		return_type = TextUtils.trim(field_type.getText());
		return_source = TextUtils.trim(field_source.getText());
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("Search")) {
			retrieveData();
			include_redend = false;
			include_all_leafs = false;
			return_status = "OK";
			//closeDialog();
		} else if (action.equals("Search core")) {
			retrieveData();
			include_redend = true;
			include_all_leafs = false;
			return_status = "OK";
			//closeDialog();
		} else if (action.equals("Search terminal")) {
			retrieveData();
			include_redend = false;
			include_all_leafs = true;
			return_status = "OK";
			//closeDialog();
		} else if (action.equals("Cancel")) {
			return_status = "Cancel";
			//closeDialog();
		}
		
		run.run();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (MouseUtils.isPopupTrigger(e)) {
			theCanvas.enforceSelection(e.getPoint());
			theCanvas.createPopupMenu(false)
					.show(theCanvas, e.getX(), e.getY());
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (MouseUtils.isPopupTrigger(e)) {
			theCanvas.enforceSelection(e.getPoint());
			theCanvas.createPopupMenu(false)
					.show(theCanvas, e.getX(), e.getY());
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		panel_canvas = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		field_source = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		field_type = new javax.swing.JTextField();
		jSeparator1 = new javax.swing.JSeparator();
		button_search_core = new javax.swing.JButton();
		button_search_terminal = new javax.swing.JButton();
		button_search = new javax.swing.JButton();
		button_cancel = new javax.swing.JButton();
		jLabel3 = new javax.swing.JLabel();
		field_database = new javax.swing.JComboBox();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		panel_canvas.setBackground(java.awt.Color.white);
		panel_canvas.setPreferredSize(new java.awt.Dimension(600, 400));
		panel_canvas.setRequestFocusEnabled(false);

		org.jdesktop.layout.GroupLayout panel_canvasLayout = new org.jdesktop.layout.GroupLayout(
				panel_canvas);
		panel_canvas.setLayout(panel_canvasLayout);
		panel_canvasLayout.setHorizontalGroup(panel_canvasLayout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(0, 693, Short.MAX_VALUE));
		panel_canvasLayout.setVerticalGroup(panel_canvasLayout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(0, 400, Short.MAX_VALUE));

		jLabel1.setText("Source");

		field_source.setText("jTextField1");

		jLabel2.setText("Type");

		field_type.setText("jTextField1");

		button_search_core.setText("Search core");

		button_search_terminal.setText("Search terminal");

		button_search.setText("Search");

		button_cancel.setText("Cancel");

		jLabel3.setText("Database");

		field_database.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout
				.setHorizontalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								layout
										.createSequentialGroup()
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																layout
																		.createSequentialGroup()
																		.addContainerGap()
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(
																								layout
																										.createSequentialGroup()
																										.add(
																												jLabel2)
																										.addPreferredGap(
																												org.jdesktop.layout.LayoutStyle.RELATED)
																										.add(
																												field_type,
																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																												233,
																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												org.jdesktop.layout.LayoutStyle.RELATED)
																										.add(
																												jLabel1)
																										.addPreferredGap(
																												org.jdesktop.layout.LayoutStyle.RELATED)
																										.add(
																												field_source,
																												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																												349,
																												Short.MAX_VALUE))
																						.add(
																								jSeparator1,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								693,
																								Short.MAX_VALUE)))
														.add(
																layout
																		.createSequentialGroup()
																		.add(
																				139,
																				139,
																				139)
																		.add(
																				button_search)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				button_search_core)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				button_search_terminal)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				button_cancel))
														.add(
																layout
																		.createSequentialGroup()
																		.addContainerGap()
																		.add(
																				jLabel3)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				field_database,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																				216,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
														.add(
																org.jdesktop.layout.GroupLayout.TRAILING,
																layout
																		.createSequentialGroup()
																		.addContainerGap()
																		.add(
																				panel_canvas,
																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																				693,
																				Short.MAX_VALUE)))
										.addContainerGap()));
		layout
				.setVerticalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								layout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel3)
														.add(
																field_database,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.UNRELATED)
										.add(
												panel_canvas,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.add(12, 12, 12)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel2)
														.add(jLabel1)
														.add(
																field_type,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																field_source,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.UNRELATED)
										.add(
												jSeparator1,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												10,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.UNRELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.CENTER)
														.add(button_search_core)
														.add(
																button_search_terminal)
														.add(button_search)
														.add(button_cancel))
										.addContainerGap(
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	/** Closes the dialog */
	private void closeDialog() {// GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
	}// GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton button_cancel;
	private javax.swing.JButton button_search;
	private javax.swing.JButton button_search_core;
	private javax.swing.JButton button_search_terminal;
	private javax.swing.JComboBox field_database;
	private javax.swing.JTextField field_source;
	private javax.swing.JTextField field_type;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JPanel panel_canvas;
	// End of variables declaration//GEN-END:variables

}
