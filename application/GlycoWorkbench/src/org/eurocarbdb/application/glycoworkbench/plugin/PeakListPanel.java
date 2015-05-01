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

import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.text.*;
import java.awt.print.*;

public class PeakListPanel extends SortingTablePanel<PeakList> implements
		ActionListener {

	// components

	protected JToolBar theToolBarDocument;
	protected JToolBar theToolBarEdit;

	// actions
	protected String shown_mslevel = "msms";
	protected JButton mslevel_button;

	protected GlycanAction ms_action = null;
	protected GlycanAction msms_action = null;

	Double edit_mz = null;
	Double edit_int = null;

	//

	public PeakListPanel() {
		super();
	}

	protected void initComponents() {
		super.initComponents();

		// create toolbar
		JPanel theToolBarPanel = new JPanel(new BorderLayout());
		theToolBarDocument = createToolBarDocument();
		theToolBarEdit = createToolBarEdit();
		theToolBarPanel.add(theToolBarDocument, BorderLayout.NORTH);
		theToolBarPanel.add(theToolBarEdit, BorderLayout.CENTER);
		add(theToolBarPanel, BorderLayout.SOUTH);

		theTableSorter.setFixLast(true);
	}

	public PeakList getDocumentFromWorkspace(GlycanWorkspace workspace) {
		return (workspace != null) ? workspace.getPeakList() : null;
	}

	public void setDocumentFromWorkspace(GlycanWorkspace workspace) {
		if (theDocument != null)
			theDocument.removeDocumentChangeListener(this);

		theDocument = getDocumentFromWorkspace(workspace);
		if (theDocument == null)
			theDocument = new PeakList();

		theDocument.addDocumentChangeListener(this);

		updateView();
		updateActions();
	}

	protected void createActions() {
		theActionManager.add("mslevel=ms", FileUtils.defaultThemeManager.getImageIcon("msms"),
				"Change current scan level", -1, "", this);
		theActionManager.add("mslevel=msms", FileUtils.defaultThemeManager.getImageIcon("ms"),
				"Change current scan level", -1, "", this);

		// file
		theActionManager.add("new", FileUtils.defaultThemeManager.getImageIcon("new"), "New",
				KeyEvent.VK_N, "", this);
		theActionManager.add("open", FileUtils.defaultThemeManager.getImageIcon("open"), "Open",
				KeyEvent.VK_O, "", this);
		theActionManager.add("save", FileUtils.defaultThemeManager.getImageIcon("save"), "Save",
				KeyEvent.VK_S, "", this);
		theActionManager.add("saveas", FileUtils.defaultThemeManager.getImageIcon("saveas"),
				"Save as...", KeyEvent.VK_A, "", this);

		// print
		theActionManager.add("print", FileUtils.defaultThemeManager.getImageIcon("print"), "Print...",
				KeyEvent.VK_P, "", this);

		// edit
		theActionManager.add("undo", FileUtils.defaultThemeManager.getImageIcon("undo"), "Undo",
				KeyEvent.VK_U, "", this);
		theActionManager.add("redo", FileUtils.defaultThemeManager.getImageIcon("redo"), "Redo",
				KeyEvent.VK_R, "", this);

		theActionManager.add("add", FileUtils.defaultThemeManager.getImageIcon("add"), "Add peak",
				KeyEvent.VK_D, "", this);
		theActionManager.add("cut", FileUtils.defaultThemeManager.getImageIcon("cut"), "Cut",
				KeyEvent.VK_T, "", this);
		theActionManager.add("copy", FileUtils.defaultThemeManager.getImageIcon("copy"), "Copy",
				KeyEvent.VK_C, "", this);
		theActionManager.add("paste", FileUtils.defaultThemeManager.getImageIcon("paste"), "Paste",
				KeyEvent.VK_P, "", this);
		theActionManager.add("delete", FileUtils.defaultThemeManager.getImageIcon("delete"), "Delete",
				KeyEvent.VK_DELETE, "", this);
		theActionManager.add("selectall", FileUtils.defaultThemeManager.getImageIcon("selectall"),
				"Select all", KeyEvent.VK_A, "", this);

		theActionManager.add("annotatepeaks", FileUtils.defaultThemeManager.getImageIcon("annotatepeaks"),
				"Find possible annotations for selected peaks", -1, "", this);

		theActionManager.add("filterselection", FileUtils.defaultThemeManager.getImageIcon(""),
				"Show only selected peaks", -1, "", this);
		theActionManager.add("showallrows", FileUtils.defaultThemeManager.getImageIcon(""),
				"Show all peaks", -1, "", this);
	}

	private void updatePeakActions() {
		if (theApplication != null && theApplication.getPluginManager() != null) {
			if (ms_action == null
					&& theApplication.getPluginManager().getMsPeakActions()
							.size() > 0)
				ms_action = theApplication.getPluginManager()
						.getMsPeakActions().iterator().next();

			if (msms_action == null
					&& theApplication.getPluginManager().getMsMsPeakActions()
							.size() > 0)
				msms_action = theApplication.getPluginManager()
						.getMsMsPeakActions().iterator().next();
		}
	}

	protected void updateActions() {
		boolean has_selection = theTable.getSelectedRows().length > 0;

		theActionManager.get("save").setEnabled(theDocument.hasChanged());

		theActionManager.get("undo").setEnabled(
				theDocument.getUndoManager().canUndo());
		theActionManager.get("redo").setEnabled(
				theDocument.getUndoManager().canRedo());

		theActionManager.get("cut").setEnabled(has_selection);
		theActionManager.get("copy").setEnabled(has_selection);
		theActionManager.get("delete").setEnabled(has_selection);

		theActionManager.get("annotatepeaks").setEnabled(has_selection);

		theActionManager.get("filterselection").setEnabled(has_selection);
		theActionManager.get("showallrows").setEnabled(
				!theTableSorter.isAllRowsVisible());

		updateMsLevel();
	}

	public void updateMsLevel() {
		if (theWorkspace.getCurrentScan() != null)
			onSetMsLevel(
					theWorkspace.getCurrentScan().isMsMs() ? "msms" : "ms",
					false);
		else
			onSetMsLevel("msms", false);
	}

	private JToolBar createToolBarDocument() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		toolbar.add(theActionManager.get("new"));
		toolbar.add(theActionManager.get("open"));
		toolbar.add(theActionManager.get("save"));
		toolbar.add(theActionManager.get("saveas"));

		toolbar.addSeparator();

		toolbar.add(theActionManager.get("print"));

		toolbar.addSeparator();

		toolbar.add(theActionManager.get("undo"));
		toolbar.add(theActionManager.get("redo"));

		return toolbar;
	}

	private JToolBar createToolBarEdit() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		toolbar.add(theActionManager.get("add"));

		toolbar.addSeparator();

		toolbar.add(theActionManager.get("cut"));
		toolbar.add(theActionManager.get("copy"));
		toolbar.add(theActionManager.get("paste"));
		toolbar.add(theActionManager.get("delete"));

		toolbar.addSeparator();

		toolbar.add(mslevel_button = new JButton(theActionManager
				.get("mslevel=msms")));
		mslevel_button.setText(null);
		toolbar.add(theActionManager.get("annotatepeaks"));

		return toolbar;
	}

	protected JPopupMenu createPopupMenu() {
		updatePeakActions();

		JPopupMenu menu = new JPopupMenu();

		menu.add(theActionManager.get("cut"));
		menu.add(theActionManager.get("copy"));
		menu.add(theActionManager.get("paste"));
		menu.add(theActionManager.get("delete"));

		menu.addSeparator();

		menu.add(theActionManager.get("add"));

		if (theApplication != null && theApplication.getPluginManager() != null) {
			ButtonGroup group = new ButtonGroup();
			if (shown_mslevel.equals("ms")) {
				for (GlycanAction a : theApplication.getPluginManager()
						.getMsPeakActions()) {
					JRadioButtonMenuItem last = new JRadioButtonMenuItem(
							new GlycanAction(a, "annotatepeaks", -1, "", this));

					menu.add(last);
					last.setSelected(a == ms_action);
					group.add(last);
				}
			} else {
				for (GlycanAction a : theApplication.getPluginManager()
						.getMsMsPeakActions()) {
					JRadioButtonMenuItem last = new JRadioButtonMenuItem(
							new GlycanAction(a, "annotatepeaks", -1, "", this));

					menu.add(last);
					last.setSelected(a == msms_action);
					group.add(last);
				}
			}
		}

		menu.addSeparator();

		menu.add(theActionManager.get("filterselection"));
		menu.add(theActionManager.get("showallrows"));

		return menu;
	}

	// ---------------
	// table model

	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0)
			return Double.class;
		if (columnIndex == 1)
			return Double.class;
		if (columnIndex == 2)
			return Double.class;
		return Object.class;
	}

	public String getColumnName(int columnIndex) {
		if (columnIndex == 0)
			return "Mass to\ncharge";
		if (columnIndex == 1)
			return "Intensity";
		if (columnIndex == 2)
			return "Relative\nIntensity";
		if (columnIndex == 3)
			return "Charge";
		return null;
	}

	public int getColumnCount() {
		return 4;
	}

	public int getRowCount() {
		return theDocument.size() + 1;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex == theDocument.size()) {
			if (columnIndex == 0)
				return edit_mz;
			if (columnIndex == 1)
				return edit_int;
			if (columnIndex == 2)
				return null;
			if (columnIndex == 3)
				return null;
		} else {
			if (columnIndex == 0)
				return theDocument.getMZ(rowIndex);
			if (columnIndex == 1)
				return theDocument.getIntensity(rowIndex);
			if (columnIndex == 2)
				return theDocument.getRelativeIntensity(rowIndex);
			if	(columnIndex ==3)
				if(theDocument.getPeak(rowIndex).getCharge()==Integer.MIN_VALUE){
					return "";
				}else{
					return theDocument.getPeak(rowIndex).getCharge();
				}
		}
		return null;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex != 2);
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (rowIndex == theDocument.size()) {
			if (columnIndex == 0)
				edit_mz = (Double) aValue;
			else if (columnIndex == 1)
				edit_int = (Double) aValue;

			if (edit_mz != null && edit_int != null) {
				int ind = theDocument.put(edit_mz, edit_int);
				int new_ind = theTableSorter.viewIndex(ind);
				theTable.setRowSelectionInterval(new_ind, new_ind);
				edit_mz = edit_int = null;
			}
		} else {
			edit_mz = edit_int = null;
			if (columnIndex == 0){
				theDocument.setMZ(rowIndex, (Double) aValue);				
			}else if (columnIndex == 1){
				theDocument.setIntensity(rowIndex, (Double) aValue);
			}else if( columnIndex ==3){
				if(aValue==null || aValue.equals("")){
					aValue=Integer.MIN_VALUE;
				}
				
				if(aValue instanceof java.lang.String){
					theDocument.setCharge(rowIndex, Integer.valueOf((String)aValue));
				}else{
					theDocument.setCharge(rowIndex, (Integer)aValue);
				}
				
				
			}
		}
	}

	// -----------------
	// data

	public boolean checkDocumentChanges() {
		if (theDocument.hasChanged() && !theDocument.isEmpty()) {
			int ret = JOptionPane.showConfirmDialog(this,
					"Save changes to peak list?", null,
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (ret == JOptionPane.CANCEL_OPTION)
				return false;
			if (ret == JOptionPane.YES_OPTION) {
				if (!theApplication.onSaveAs(theDocument))
					return false;
			}
		}
		return true;
	}

	// -----------
	// Visualization

	// -----------------
	// actions

	public void onPrint() {
		theTable.print(theWorkspace.getPrinterJob());
	}

	public void onUndo() {
		try {
			theDocument.getUndoManager().undo();
			edit_mz = edit_int = null;
		} catch (Exception e) {
			LogUtils.report(e);
		}
	}

	public void onRedo() {
		try {
			theDocument.getUndoManager().redo();
			edit_mz = edit_int = null;
		} catch (Exception e) {
			LogUtils.report(e);
		}
	}

	public void onAdd() {
		edit_mz = edit_int = null;
		theDocument.put(0., 0.);
		theTable.setRowSelectionInterval(0, 0);
		theTable.changeSelection(0, 0, false, true);
		theTable.grabFocus();
	}

	public void cut() {
		copy();
		delete();
	}

	public void copy() {
		// get selected rows
		Data rows = new Data();

		int[] sel_ind = theTable.getSelectedRows();
		if (sel_ind.length > 0) {
			// set data header
			for (int c = 0; c < getColumnCount(); c++){
				rows.add(getColumnName(c));
			}
			rows.newRow();

			// get selection
			for (int i = 0; i < sel_ind.length; i++) {
				if(sel_ind[i]==theTable.getRowCount()-1)
					continue;

				int r = theTableSorter.modelIndex(sel_ind[i]);

				// get data
				for (int c = 0; c < getColumnCount(); c++){
					Object value=getValueAt(r, c);
					
					if(value instanceof String){
						value=Integer.MIN_VALUE;
					}
					
					rows.add(value);
				}
				rows.newRow();
			}
			ClipUtils.setContents(new GlycanSelection(rows));
		}
	}

	public void paste() {
		Transferable t = ClipUtils.getContents();
		try {
			if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String str = (String) t
						.getTransferData(DataFlavor.stringFlavor);
				
				theDocument.mergeData(PeakList.parseString(str));
			}
		} catch (Exception e) {
			LogUtils.report(e);
		}
	}

	public void delete() {
		int sel_ind[] = theTable.getSelectedRows();
	
		theDocument.remove(theTableSorter.modelIndexes(sel_ind));
	}

	public void onSetMsLevel(String mslevel, boolean changedoc) {

		shown_mslevel = mslevel;
		if (mslevel.equals("ms")) {
			mslevel_button.setAction(theActionManager.get("mslevel=msms"));
			mslevel_button.setText(null);
			if (changedoc)
				theWorkspace.setMsMs(theWorkspace.getCurrentScan(), false);
		} else {
			mslevel_button.setAction(theActionManager.get("mslevel=ms"));
			mslevel_button.setText(null);
			if (changedoc)
				theWorkspace.setMsMs(theWorkspace.getCurrentScan(), true);
		}

		theTable.setPopupMenu(createPopupMenu()); // update plugin actions
	}

	public boolean onAnnotatePeaks(String parent_action) {
		try {
			int[] sel_inds = theTable.getSelectedRows();
			if (sel_inds.length > 0) {
				int[] model_inds = theTableSorter.modelIndexes(sel_inds);
				Collection<Peak> selected_peaks = theDocument
						.extract(model_inds);
				if (shown_mslevel.equals("ms")) {
					if (parent_action != null)
						ms_action = theApplication.getPluginManager()
								.getMsPeakAction(parent_action);
					return theApplication.getPluginManager().runAction(
							ms_action, new PeakList(selected_peaks));
				}
				if (parent_action != null)
					msms_action = theApplication.getPluginManager()
							.getMsMsPeakAction(parent_action);
				return theApplication.getPluginManager().runAction(msms_action,
						new PeakList(selected_peaks));
			}
			return false;
		} catch (Exception e) {
			LogUtils.report(e);
			return false;
		}
	}

	public void filterSelection() {
		int[] sel_inds = theTable.getSelectedRows();
		if (sel_inds != null & sel_inds.length > 0)
			theTableSorter
					.setVisibleRows(theTableSorter.modelIndexes(sel_inds));
	}

	public void showAllRows() {
		theTableSorter.resetVisibleRows();
	}

	// -----------
	// listeners

	public void actionPerformed(ActionEvent e) {

		String action = GlycanAction.getAction(e);
		String param = GlycanAction.getParam(e);

		if (action.equals("new"))
			theApplication.onNew(theDocument);
		else if (action.equals("open")){
			System.err.println("Trace one");
			theApplication.onOpen(null, theDocument, true,true);
		}else if (action.equals("save"))
			theApplication.onSave(theDocument);
		else if (action.equals("saveas"))
			theApplication.onSaveAs(theDocument);

		else if (action.equals("print"))
			onPrint();

		else if (action.equals("undo"))
			onUndo();
		else if (action.equals("redo"))
			onRedo();

		else if (action.equals("add"))
			onAdd();
		else if (action.equals("cut"))
			cut();
		else if (action.equals("copy"))
			copy();
		else if (action.equals("paste"))
			paste();
		else if (action.equals("delete"))
			delete();
		else if (action.equals("mslevel"))
			onSetMsLevel(param, true);
		else if (action.equals("annotatepeaks"))
			onAnnotatePeaks(param);

		else if (action.equals("filterselection"))
			filterSelection();
		else if (action.equals("showallrows"))
			showAllRows();

		/*
		 * else if( action.equals("selectall") ) selectAll(); else if(
		 * action.equals("selectnone") ) resetSelection();
		 */

		updateActions();
	}

	public void documentInit(BaseDocument.DocumentChangeEvent e) {
		if (!ignore_document_changes) {
			if (e.getSource() == theWorkspace)
				updateWorkspace();
			else {
				theTable.setPopupMenu(createPopupMenu()); // init plugin actions
				edit_mz = edit_int = null;
				updateDocument();
			}
		}
	}

	public void documentChanged(BaseDocument.DocumentChangeEvent e) {
		updateActions();
		updateView();
	}

	protected void updateData() {

	}

	public void internalDocumentChanged(GlycanWorkspace.Event e) {
		updateMsLevel();
	}

}
