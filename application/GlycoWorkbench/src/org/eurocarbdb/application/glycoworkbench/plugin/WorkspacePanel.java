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

import org.eurocarbdb.application.glycoworkbench.plugin.reporting.*;
import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycoworkbench.Annotation;
import org.eurocarbdb.application.glycanbuilder.*;

import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.text.*;
import java.awt.print.*;

public class WorkspacePanel extends JPanel implements TreeModel,
		ActionListener, TreeSelectionListener,
		BaseDocument.DocumentChangeListener, GlycanWorkspace.Listener,
		MouseListener {

	class WorkspaceTreeCellRenderer extends DefaultTreeCellRenderer {

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, "", sel, expanded, leaf,
					row, hasFocus);
			
			setFont(getFont().deriveFont(Font.PLAIN));

			
			
			if (value instanceof BaseDocument) {
				JLabel master;
				
				BaseDocument doc = (BaseDocument) value;
				if(doc.getRegisteredComponent("treeView")!=null){
					master=(JLabel)doc.getRegisteredComponent("treeView");
					master.setBackground(this.getBackground());
					master.setForeground(this.getForeground());
				}else{
					master=new JLabel();
					// set icon
					if (leaf)
						master.setIcon(doc.getIcon());
				}
				
				
				// set text
				String text = doc.getName();
				if (doc.wasSaved())
					text += " - "
							+ FileHistory.getAbbreviatedName(doc.getFileName());
				if (doc.hasChanged())
					text += "*";
				master.setText(text);

				
				doc.registerComponent("treeView",master);
				
				return master;
				
				
			} else if (value instanceof Scan) {
				Scan scan = (Scan) value;

				String text = (scan.getName() != null && scan.getName()
						.length() > 0) ? scan.getName() : "Scan";
				if (scan.getPrecursorMZ() != null)
					text += " [precursor m/z= "
							+ new DecimalFormat("0.0000").format(scan
									.getPrecursorMZ().doubleValue()) + " Da]";
				setText(text);

				if (theWorkspace.isCurrent(scan))
					setFont(getFont().deriveFont(Font.BOLD));
			}

			
			return this;
		}

	}

	private static final long serialVersionUID = 0L;

	// components
	protected GlycoWorkbench theApplication;

	protected JTree theTree;
	protected JScrollPane theScrollPane;

	protected JToolBar theToolBarDocument;
	protected JToolBar theToolBarEdit;

	// data
	protected GlycanWorkspace theWorkspace;

	// actions
	protected ActionManager theActionManager;
	protected Vector<TreeModelListener> tm_listeners;

	Double edit_mz = null;
	Double edit_int = null;

	//

	public WorkspacePanel(GlycoWorkbench application) {
		

		super(new BorderLayout());
		
		this.theApplication=application;

		// init data
		//theApplication = null;
		theWorkspace = new GlycanWorkspace();

		// init actions
		theActionManager = new ActionManager();
		createActions();
		tm_listeners = new Vector<TreeModelListener>();

		// create workspace viewer
		theTree = new JTree();
		theTree.setEditable(false);
		theTree.setDragEnabled(false);
		theTree.setRootVisible(true);
		theTree.setToggleClickCount(3);
		theTree.setCellRenderer(new WorkspaceTreeCellRenderer());
		theTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		theTree.setModel(this);

		theScrollPane = new JScrollPane(theTree);
		add(theScrollPane, BorderLayout.CENTER);

		// create toolbar
		JPanel theToolBarPanel = new JPanel(new BorderLayout());
		theToolBarDocument = createToolBarDocument();
		theToolBarEdit = createToolBarEdit();
		theToolBarPanel.add(theToolBarDocument, BorderLayout.NORTH);
		theToolBarPanel.add(theToolBarEdit, BorderLayout.CENTER);
		add(theToolBarPanel, BorderLayout.SOUTH);

		// final settings
		theWorkspace.addDocumentChangeListener(this);
		theWorkspace.addWorkspaceListener(this);

		theTree.addTreeSelectionListener(this);
		theTree.addMouseListener(this);

		expandAll();

		setMinimumSize(new Dimension(0, 0));
		setBackground(Color.white);
		this.setOpaque(true);

	}

	public void setApplication(GlycoWorkbench application) {
		theApplication = application;

		updateActions();
		updateView();
	}

	public void setWorkspace(GlycanWorkspace workspace) {
		theWorkspace = workspace;
		theWorkspace.addWorkspaceListener(this);
		theWorkspace.addDocumentChangeListener(this);

		updateActions();
		updateView();
	}

	private void createActions() {
		// file
		theActionManager.add("new", this.theApplication.getThemeManager().getResizableIcon(STOCK_ICON.DOCUMENT_NEW,ICON_SIZE.SMALL),
				"Clear selected document", KeyEvent.VK_N, "", this);
		theActionManager.add("open",this.theApplication.getThemeManager().getResizableIcon(STOCK_ICON.DOCUMENT_OPEN,ICON_SIZE.SMALL),
				"Open selected document", KeyEvent.VK_O, "", this);
		theActionManager.add("save",this.theApplication.getThemeManager().getResizableIcon(STOCK_ICON.DOCUMENT_SAVE,ICON_SIZE.SMALL),
				"Save selected document", KeyEvent.VK_S, "", this);
		theActionManager.add("saveas", this.theApplication.getThemeManager().getResizableIcon(STOCK_ICON.DOCUMENT_SAVE_AS,ICON_SIZE.SMALL),
				"Save selected document as...", KeyEvent.VK_A, "", this);

		theActionManager.add("newall", this.theApplication.getThemeManager().getResizableIcon("newall",ICON_SIZE.SMALL),
				"Clear the workspace", -1, "", this);
		theActionManager.add("openall", this.theApplication.getThemeManager().getResizableIcon("openall",ICON_SIZE.SMALL),
				"Open a workspace", -1, "", this);
		theActionManager.add("saveall", this.theApplication.getThemeManager().getResizableIcon("saveall",ICON_SIZE.SMALL),
				"Save the workspace", -1, "", this);
		theActionManager.add("saveallas", this.theApplication.getThemeManager().getResizableIcon("saveallas",ICON_SIZE.SMALL),
				"Save the workspace as...", -1, "", this);

		theActionManager.add("add", this.theApplication.getThemeManager().getResizableIcon("add",ICON_SIZE.SMALL),
				"Attach new scan", KeyEvent.VK_D, "", this);
		theActionManager.add("delete", this.theApplication.getThemeManager().getResizableIcon("delete",ICON_SIZE.SMALL), "Delete",
				KeyEvent.VK_E, "", this);
		theActionManager.add("select", this.theApplication.getThemeManager().getResizableIcon("select",ICON_SIZE.SMALL), "Activate",
				KeyEvent.VK_L, "", this);

		theActionManager.add("sync",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Sync",-1, "",this);
		
		theActionManager.add("properties",this.theApplication.getThemeManager().getResizableIcon("properties",ICON_SIZE.SMALL),
				"Properties", KeyEvent.VK_P, "", this);
	}

	private void updateActions() {

		theActionManager.get("new").setEnabled(
				hasSelectedDocument() && !hasSelectedAnnotationReport());
		theActionManager.get("open").setEnabled(hasSelectedDocument());
		theActionManager.get("save").setEnabled(
				hasSelectedDocument() && !hasSelectedSpectra());
		theActionManager.get("saveas").setEnabled(
				hasSelectedDocument() && !hasSelectedSpectra());

		// theActionManager.get("undo").setEnabled(theWorkspace.getUndoManager().canUndo());
		// theActionManager.get("redo").setEnabled(theWorkspace.getUndoManager().canRedo());

		theActionManager.get("delete").setEnabled(
				hasSelectedScan() || hasSelectedAnnotationReport());
		theActionManager.get("select").setEnabled(hasSelection());
		theActionManager.get("properties").setEnabled(hasSelectedScan());
		theActionManager.get("sync").setEnabled(hasSelectedScan());
	}

	private JToolBar createToolBarDocument() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		toolbar.add(theActionManager.get("newall"));
		toolbar.add(theActionManager.get("openall"));
		toolbar.add(theActionManager.get("saveall"));
		toolbar.add(theActionManager.get("saveallas"));

		toolbar.addSeparator();

		toolbar.add(theActionManager.get("new"));
		toolbar.add(theActionManager.get("open"));
		toolbar.add(theActionManager.get("save"));
		toolbar.add(theActionManager.get("saveas"));

		// toolbar.addSeparator();

		// toolbar.add(theActionManager.get("undo"));
		// toolbar.add(theActionManager.get("redo"));

		return toolbar;
	}

	private JToolBar createToolBarEdit() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		toolbar.add(theActionManager.get("add"));
		toolbar.add(theActionManager.get("delete"));
		toolbar.add(theActionManager.get("select"));

		toolbar.addSeparator();

		toolbar.add(theActionManager.get("properties"));

		return toolbar;
	}

	private JPopupMenu createPopupMenu() {

		JPopupMenu menu = new JPopupMenu();

		menu.add(theActionManager.get("open"));
		menu.add(theActionManager.get("save"));
		menu.add(theActionManager.get("saveas"));

		menu.addSeparator();

		menu.add(theActionManager.get("add"));
		menu.add(theActionManager.get("delete"));
		menu.add(theActionManager.get("select"));

		menu.addSeparator();

		menu.add(theActionManager.get("sync"));
		menu.add(theActionManager.get("properties"));

		return menu;
	}

	private void showPopup(MouseEvent e) {
		theTree.setSelectionPath(theTree.getClosestPathForLocation(e.getX(), e
				.getY()));
		createPopupMenu().show(this, e.getX(), e.getY());
	}

	// -------------
	// selection

	public boolean hasSelection() {
		return theTree.getSelectionPath() != null;
	}

	public boolean hasSelectedWorkspace() {
		return (getSelectedObject() instanceof GlycanWorkspace);
	}

	public boolean hasSelectedSpectra() {
		return (getSelectedObject() instanceof SpectraDocument);
	}

	public boolean hasSelectedScan() {
		return (getSelectedObject() instanceof Scan);
	}

	public boolean hasSelectedAnnotationReport() {
		return (getSelectedObject() instanceof AnnotationReportDocument);
	}

	public boolean hasSelectedDocument() {
		return (getSelectedObject() instanceof BaseDocument);
	}

	public Object getSelectedObject() {
		TreePath sel = theTree.getSelectionPath();
		if (sel != null)
			return sel.getLastPathComponent();
		return null;
	}

	public Scan getSelectedScan() {
		Object node = getSelectedObject();
		if (node != null && (node instanceof Scan))
			return (Scan) node;
		return null;
	}

	public AnnotationReportDocument getSelectedAnnotationReport() {
		Object node = getSelectedObject();
		if (node != null && (node instanceof AnnotationReportDocument))
			return (AnnotationReportDocument) node;
		return null;
	}

	public BaseDocument getSelectedDocument() {
		Object node = getSelectedObject();
		if (node != null && (node instanceof BaseDocument))
			return (BaseDocument) node;
		return null;
	}

	public Scan getSelectedObjectOrParentScan() {
		TreePath sel = theTree.getSelectionPath();
		
		
		
		if (sel != null) {
			Object node = sel.getLastPathComponent();
			if (node instanceof Scan)
				return (Scan) node;
			if ((node instanceof GlycanDocument)
					|| (node instanceof FragmentDocument)
					|| (node instanceof SpectraDocument)
					|| (node instanceof PeakList)
					|| (node instanceof AnnotatedPeakList)
					|| (node instanceof NotesDocument)
					|| (node instanceof AnnotationReportDocument))
				return (Scan) sel.getParentPath().getLastPathComponent();
		}
		return null;
	}

	// ---------------
	// table model

	public void expandAll() {
		for (Scan s : theWorkspace.getScanList())
			expandSubTree(s);
	}

	public void expandSubTree(Scan s) {
		expand(s);
		for (Scan c : s.getChildren())
			expandSubTree(c);
	}

	public void expand(Object node) {
		theTree.expandPath(getTreePath(node));
	}

	public Object getRoot() {
		return theWorkspace;
	}

	public Object getChild(Object parent, int index) {
		if (parent == theWorkspace)
			return theWorkspace.scanAt(index);
		else if (parent instanceof Scan) {
			if (index == 0)
				return ((Scan) parent).getStructures();
			if (index == 1)
				return ((Scan) parent).getFragments();
			if (index == 2)
				return ((Scan) parent).getSpectra();
			if (index == 3)
				return ((Scan) parent).getPeakList();
			if (index == 4)
				return ((Scan) parent).getAnnotatedPeakList();
			if (index == 5)
				return ((Scan) parent).getNotes();
			if (index >= 6
					&& index <= 5 + ((Scan) parent).getAnnotationReports()
							.size())
				return ((Scan) parent).getAnnotationReports().get(index - 6);
			else
				return ((Scan) parent).childAt(index
						- ((Scan) parent).getAnnotationReports().size() - 6);
		}
		return null;
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent == theWorkspace)
			return theWorkspace.indexOf((Scan) child);
		else if (parent instanceof Scan) {
			if (child == ((Scan) parent).getStructures())
				return 0;
			if (child == ((Scan) parent).getFragments())
				return 1;
			if (child == ((Scan) parent).getSpectra())
				return 2;
			if (child == ((Scan) parent).getPeakList())
				return 3;
			if (child == ((Scan) parent).getAnnotatedPeakList())
				return 4;
			if (child == ((Scan) parent).getNotes())
				return 5;
			if (((Scan) parent).getAnnotationReports().contains(child))
				return 6 + ((Scan) parent).getAnnotationReports()
						.indexOf(child);
			if (child instanceof Scan)
				return 6 + ((Scan) parent).getAnnotationReports().size()
						+ ((Scan) parent).indexOf((Scan) child);

		}
		return -1;
	}

	public TreePath getTreePath(Object node) {
		if (node == theWorkspace)
			return new TreePath(theWorkspace);

		for (Scan s : theWorkspace.getScanList()) {
			TreePath ret = getTreePath(new Union(theWorkspace), s, node);
			if (ret != null)
				return ret;
		}
		return null;
	}

	private TreePath getTreePath(Union<Object> path, Scan cur_node, Object node) {
		if (cur_node == node)
			return new TreePath(path.and(cur_node).toArray());
		if (node == cur_node.getStructures())
			return new TreePath(path.and(cur_node).and(node).toArray());
		if (node == cur_node.getFragments())
			return new TreePath(path.and(cur_node).and(node).toArray());
		if (node == cur_node.getSpectra())
			return new TreePath(path.and(cur_node).and(node).toArray());
		if (node == cur_node.getPeakList())
			return new TreePath(path.and(cur_node).and(node).toArray());
		if (node == cur_node.getAnnotatedPeakList())
			return new TreePath(path.and(cur_node).and(node).toArray());
		if (node == cur_node.getNotes())
			return new TreePath(path.and(cur_node).and(node).toArray());
		if (cur_node.getAnnotationReports().contains(node))
			return new TreePath(path.and(cur_node).and(node).toArray());

		for (Scan s : cur_node.getChildren()) {
			TreePath ret = getTreePath(path.and(cur_node), s, node);
			if (ret != null)
				return ret;
		}
		return null;
	}

	public int getChildCount(Object parent) {
		if (parent == theWorkspace)
			return theWorkspace.getNoScans();
		else if (parent instanceof Scan)
			return 6 + ((Scan) parent).getAnnotationReports().size()
					+ ((Scan) parent).getNoChildren();
		return 0;
	}

	public boolean isLeaf(Object node) {
		return (node != theWorkspace && !(node instanceof Scan));
	}

	public void valueForPathChanged(TreePath path, Object newValue) {

	}

	public void addTreeModelListener(TreeModelListener l) {
		if (l != null)
			tm_listeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		tm_listeners.remove(l);
	}

	// -----------------
	// data

	public boolean checkDocumentChanges() {
		/*
		 * if( theWorkspace.hasChanged() && !theWorkspace.isEmpty() ) { int ret
		 * = JOptionPane.showConfirmDialog(this,"Save changes to peak list?",
		 * null, JOptionPane.YES_NO_CANCEL_OPTION,
		 * JOptionPane.QUESTION_MESSAGE); if( ret == JOptionPane.CANCEL_OPTION )
		 * return false; if( ret == JOptionPane.YES_OPTION ) { if(
		 * !theApplication.onSaveAs(theWorkspace) ) return false; } } return
		 * true;
		 */
		return true;
	}

	// -----------
	// Visualization

	public void updateView() {
		fireTreeStructureChanged(theWorkspace);
		expandAll();
	}

	// -----------------
	// actions

	public void onPrint() {
	}

	/*
	 * public void onUndo() { try { theWorkspace.getUndoManager().undo(); }
	 * catch(Exception e) { LogUtils.report(e); } }
	 * 
	 * public void onRedo() { try { theWorkspace.getUndoManager().redo(); }
	 * catch(Exception e) { LogUtils.report(e); } }
	 */

	public void cut() {
		copy();
		delete();
	}

	public void copy() {
	}

	public void paste() {
	}
	
	public boolean sync(){
		System.err.println("Sync has been called");
		if(hasSelectedScan()){
			Scan scan=getSelectedScan();
			if(scan!=null){
				return theWorkspace.syncScan(scan);
			}
		}
		
		return false;
	}

	public void delete() {
		if (hasSelectedScan()) {
			Scan s = getSelectedScan();
			if (s != null
					&& JOptionPane
							.showConfirmDialog(
									this,
									"Are you sure to remove the selected scan, its children, and all the data associated?") == JOptionPane.YES_OPTION)
				theWorkspace.removeScan(s.getParent(), s);
		} else {
			AnnotationReportDocument ard = getSelectedAnnotationReport();
			if (ard != null
					&& JOptionPane
							.showConfirmDialog(this,
									"Are you sure to remove the selected annotation report?") == JOptionPane.YES_OPTION)
				theWorkspace.removeAnnotationReport(ard);
		}
	}

	public void onAdd() {
		Scan s = getSelectedObjectOrParentScan();
		theWorkspace.addScan(s, new Scan(theWorkspace));
	}

	public void onSelect() {

		try {
			Object sel = getSelectedObject();
			if (sel instanceof Scan)
				select((Scan) sel);
			else if (sel instanceof GlycanDocument) {
				select(getSelectedObjectOrParentScan());
				// theApplication.getPluginManager().show("Structures","Structures");
			} else if (sel instanceof FragmentDocument) {
				select(getSelectedObjectOrParentScan());
				theApplication.getPluginManager().show("Fragments", "Summary");
			} else if (sel instanceof SpectraDocument) {
				select(getSelectedObjectOrParentScan());
				theApplication.getPluginManager().show("Spectra", "Spectra");
			} else if (sel instanceof PeakList) {
				select(getSelectedObjectOrParentScan());
				theApplication.getPluginManager().show("PeakList", "PeakList");
			} else if (sel instanceof AnnotatedPeakList) {
				select(getSelectedObjectOrParentScan());
				theApplication.getPluginManager().show("Annotation", "Summary");
			} else if (sel instanceof NotesDocument) {
				select(getSelectedObjectOrParentScan());
				theApplication.getPluginManager().show("Notes", "Notes");
			} else if (sel instanceof AnnotationReportDocument) {
				select(getSelectedObjectOrParentScan());
				((ReportingPlugin) theApplication.getPluginManager().get(
						"Reporting")).showAnnotationsReport(
						(AnnotationReportDocument) sel, false);
			}
		} catch (Exception ex) {
			LogUtils.report(ex);
		}
	}

	public void select(Scan s) {
		if (s != null)
			theWorkspace.setCurrentScan(s);
	}

	public void onProperties() {
		Scan s = getSelectedScan();
		if (s != null) {
			/*
			 * String m_z = JOptionPane.showInputDialog(this,
			 * "Insert m/z value of the precursor ion", s.getPrecursorMZ()); if(
			 * m_z!=null ) theWorkspace.setPrecursorMZ(s,Double.valueOf(m_z));
			 */
			new ScanPropertiesDialog(theApplication, s, theWorkspace)
					.setVisible(true);
		}
	}

	// -----------
	// listeners

	public void actionPerformed(ActionEvent e) {

		String action = e.getActionCommand();

		if (action.equals("newall"))
			theApplication.onNew(theWorkspace);
		else if (action.equals("openall"))
			theApplication.onOpen(null, theWorkspace, false);
		else if (action.equals("saveall"))
			theApplication.onSave(theWorkspace);
		else if (action.equals("saveallas"))
			theApplication.onSaveAs(theWorkspace);

		else if (action.equals("new"))
			theApplication.onNew(getSelectedDocument());
		else if (action.equals("open")) {
			if (theApplication.onOpen(null, getSelectedDocument(), false))
				onSelect();
		} else if (action.equals("save"))
			theApplication.onSave(getSelectedDocument());
		else if (action.equals("saveas"))
			theApplication.onSaveAs(getSelectedDocument());

		/*
		 * else if( action.equals("undo") ) onUndo(); else if(
		 * action.equals("redo") ) onRedo();
		 */
		/*
		 * else if( action.equals("cut") ) cut(); else if( action.equals("copy")
		 * ) copy(); else if( action.equals("paste") ) paste();
		 */

		if (action.equals("add"))
			onAdd();
		else if (action.equals("delete"))
			delete();
		else if (action.equals("select"))
			onSelect();
		else if (action.equals("properties"))
			onProperties();
		else if (action.equals("sync"))
			sync();

		updateActions();
	}

	public void valueChanged(TreeSelectionEvent e) {
		updateActions();
	}

	public void currentScanChanged(GlycanWorkspace.Event e) {
		fireTreeNodesChanged(e.getChildScan());
		fireTreeNodesChanged(e.getCurrentScan());
		updateActions();
	}

	public void scanAdded(GlycanWorkspace.Event e) {
		fireTreeNodesInserted(e.getParentScan(), e.getChildScan(), e.getIndex());
		expand(e.getChildScan());
		updateActions();
	}

	public void scanRemoved(GlycanWorkspace.Event e) {
		fireTreeNodesRemoved(e.getParentScan(), e.getChildScan(), e.getIndex());
		updateActions();
	}

	public void internalDocumentChanged(GlycanWorkspace.Event e) {
		fireTreeNodesChanged(e.getSource());
		updateActions();
	}

	public void documentInit(BaseDocument.DocumentChangeEvent e) {
		fireTreeStructureChanged(e.getSource());
		updateActions();
	}

	public void documentChanged(BaseDocument.DocumentChangeEvent e) {
		if (e.getSource() == theWorkspace)
			fireTreeStructureChanged(e.getSource());
		else
			fireTreeNodesChanged(e.getSource());
		updateActions();
	}

	public void mousePressed(MouseEvent e) {
		if (MouseUtils.isPopupTrigger(e))
			showPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		if (MouseUtils.isPopupTrigger(e))
			showPopup(e);
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
		if (MouseUtils.isSelectTrigger(e))
			onSelect();
	}

	public void fireTreeNodesChanged(Object node) {
		for (TreeModelListener tml : tm_listeners)
			tml.treeNodesChanged(new TreeModelEvent(this, getTreePath(node)));
	}

	public void fireTreeNodesInserted(Object parent, Object child, int index) {
		TreePath par_path = (parent == null) ? getTreePath(theWorkspace)
				: getTreePath(parent);
		for (TreeModelListener tml : tm_listeners)
			tml.treeNodesInserted(new TreeModelEvent(this, par_path,
					new int[] { index }, new Object[] { child }));
	}

	public void fireTreeNodesRemoved(Object parent, Object child, int index) {
		TreePath par_path = (parent == null) ? getTreePath(theWorkspace)
				: getTreePath(parent);
		for (TreeModelListener tml : tm_listeners)
			tml.treeNodesRemoved(new TreeModelEvent(this, par_path,
					new int[] { index }, new Object[] { child }));
	}

	public void fireTreeStructureChanged(Object node) {
		TreePath path = (node == null) ? getTreePath(theWorkspace)
				: getTreePath(node);
		for (TreeModelListener tml : tm_listeners)
			tml.treeStructureChanged(new TreeModelEvent(this, path));
		expandAll();
	}

}
