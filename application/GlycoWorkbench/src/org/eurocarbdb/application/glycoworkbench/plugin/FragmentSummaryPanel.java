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
import java.util.*;
import java.text.*;
import java.awt.print.*;

public class FragmentSummaryPanel extends SortingTablePanel<FragmentDocument> implements ActionListener, FragmentDocument.StructuresChangeListener {    

    // components
    protected JToolBar    theToolBarDocument;
    protected JToolBar    theToolBarEdit;
    
    // actions
    protected boolean update_header = false;
    
    //


    public FragmentSummaryPanel() {
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
    add(theToolBarPanel,BorderLayout.SOUTH);
    }

    protected FragmentDocument getDocumentFromWorkspace(GlycanWorkspace workspace) {
    return (workspace!=null) ?workspace.getFragments() :null;
    }

    public void setDocumentFromWorkspace(GlycanWorkspace workspace) {
    if( theDocument!=null ) {
        theDocument.removeDocumentChangeListener(this);    
        theDocument.removeStructuresChangeListener(this);
    }

    theDocument = getDocumentFromWorkspace(workspace);
    if( theDocument==null )
        theDocument = new FragmentDocument();
    
    theDocument.addDocumentChangeListener(this);    
    theDocument.addStructuresChangeListener(this);

    update_header = true;
    updateView();
    updateActions();
    }             

    protected void createActions() {
    theActionManager.add("new",FileUtils.defaultThemeManager.getImageIcon("new"),"Clear all",KeyEvent.VK_N, "",this);
    theActionManager.add("open",FileUtils.defaultThemeManager.getImageIcon("open"),"Open",KeyEvent.VK_O, "",this);
    theActionManager.add("save",FileUtils.defaultThemeManager.getImageIcon("save"),"Save",KeyEvent.VK_S, "",this);
    theActionManager.add("saveas",FileUtils.defaultThemeManager.getImageIcon("saveas"),"Save as...",KeyEvent.VK_A, "",this);

    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "",this);

    // edit
    //theActionManager.add("undo",FileUtils.defaultThemeManager.getImageIcon("undo"),"Undo",KeyEvent.VK_U, "",this);
    //theActionManager.add("redo",FileUtils.defaultThemeManager.getImageIcon("redo"),"Redo", KeyEvent.VK_R, "",this);

    theActionManager.add("cut",FileUtils.defaultThemeManager.getImageIcon("cut"),"Cut",-1, "",this);
    theActionManager.add("copy",FileUtils.defaultThemeManager.getImageIcon("copy"),"Copy",KeyEvent.VK_C, "",this);
    theActionManager.add("delete",FileUtils.defaultThemeManager.getImageIcon("delete"),"Delete",-1, "",this);
    theActionManager.add("transfer",FileUtils.defaultThemeManager.getImageIcon("transfer"),"Copy fragments into canvas",KeyEvent.VK_V, "",this);

    theActionManager.add("filterselection",FileUtils.defaultThemeManager.getImageIcon(""),"Show only selected annotations",-1, "",this);
    theActionManager.add("showallrows",FileUtils.defaultThemeManager.getImageIcon(""),"Show all annotations",-1, "",this);  
    }

    protected void updateActions() {    

    theActionManager.get("save").setEnabled(theDocument.hasChanged());
    //theActionManager.get("undo").setEnabled(theDocument.getUndoManager().canUndo());
    //theActionManager.get("redo").setEnabled(theDocument.getUndoManager().canRedo());

    boolean has_selection = theTable.getSelectedRows().length>0;
    theActionManager.get("cut").setEnabled(has_selection);
    theActionManager.get("copy").setEnabled(has_selection);
    theActionManager.get("delete").setEnabled(has_selection);
    theActionManager.get("transfer").setEnabled(has_selection);

    theActionManager.get("filterselection").setEnabled(has_selection);
    theActionManager.get("showallrows").setEnabled(!theTableSorter.isAllRowsVisible());
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

    //toolbar.addSeparator();
    //toolbar.add(theActionManager.get("undo"));
    //toolbar.add(theActionManager.get("redo"));
    
    return toolbar;
    }

    private JToolBar createToolBarEdit() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    toolbar.add(theActionManager.get("cut"));
    toolbar.add(theActionManager.get("copy"));
    toolbar.add(theActionManager.get("delete"));
    toolbar.add(theActionManager.get("transfer"));

    return toolbar;
    }  
       

    protected JPopupMenu createPopupMenu() {

    JPopupMenu menu = new JPopupMenu();

    menu.add(theActionManager.get("cut"));
    menu.add(theActionManager.get("copy"));
    menu.add(theActionManager.get("delete"));
    menu.add(theActionManager.get("transfer"));

    menu.addSeparator();

    menu.add(theActionManager.get("filterselection"));       
    menu.add(theActionManager.get("showallrows"));

    return menu;
    }


    //---------------
    // table model
 
    public Class<?> getColumnClass(int columnIndex) {
    if( columnIndex==0 )
        return Double.class;
    return Vector.class;
    }
        
    public String getColumnName(int columnIndex) {
    if( columnIndex==0 )
        return "Mass to\ncharge";    
    return "Structure" + (columnIndex-1);
    }
        
    public int getColumnCount() {
    return 1 + theDocument.getNoStructures();
    }
  
    public int getRowCount() {
    return theDocument.getNoPeaks();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
    if( columnIndex==0 ) 
        return theDocument.getFragmentMZ(rowIndex);
    return theDocument.getFragments(rowIndex,columnIndex-1);
    }   

    //-----------------
    // data   

    public void clear() {
    theDocument.init();
    }                 

    //-----------
    // Visualization

    public void updateView() {
    if( update_header ) {
        // change number of columns
        fireTableStructureChanged();

        // set structures in header
        for( int i=0; i<theDocument.getNoStructures(); i++ )
        theTable.getColumn("Structure"+i).setHeaderValue(theDocument.getStructure(i));

        update_header = false;
    }

    super.updateView();
    } 
    
    //-----------------
    // actions

    public void onNew() {
    clear();
    }
  
    public void onPrint() {
    theTable.print(theWorkspace.getPrinterJob());    
    }

    /*public void onUndo()  {
    try {
        theDocument.getUndoManager().undo();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    public void onRedo()  {
    try {
        theDocument.getUndoManager().redo();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    } 
    */         

    public void cut() {
    copy();
    delete();
    }

    public void delete() {

     if( theDocument.getNoStructures()>0 ) {
        // suppress automatic update
        int[] sel_inds = theTable.getSelectedRows();
      
        // clear annotations                
        theDocument.clearFragmentsFor(theTableSorter.modelIndexes(sel_inds));        
    
        // update table
        fireRowsChanged(sel_inds);
    }
    }


    public void copy() {    
    // get selected rows
    int[] sel_inds = theTable.getSelectedRows();
    if( sel_inds.length>0 ) {
        // get selection       
        int focusRow = theTable.getSelectionModel().getLeadSelectionIndex();
        int focusColumn = theTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();
        
        Collection<Glycan> structures = null;
        if( focusRow!=-1 && focusColumn!=-1 ) {
        Object v = getValueAt(focusRow, focusColumn);
        if( v instanceof Collection ) {
            Collection cv = (Collection)v;
            if( cv.size()>0 && (cv.iterator().next() instanceof Glycan) ) 
            structures = cv;
        }
        }

        ClipUtils.setContents(new AnnotationSelection(theTable.getSelectedData(),theWorkspace.getGlycanRenderer(),structures));
    }     
    }  

    public void transfer() {
    GlycanCanvas theCanvas = theApplication.getCanvas();
    
    this.copy();    
    theCanvas.resetSelection();
    theCanvas.paste();
    }   
       

    public void filterSelection() {
    int[] sel_inds = theTable.getSelectedRows();
    if( sel_inds!=null & sel_inds.length>0 )
        theTableSorter.setVisibleRows(theTableSorter.modelIndexes(sel_inds));
    }

    public void showAllRows() {
    theTableSorter.resetVisibleRows();
    }

    //-----------
    // listeners

    public void actionPerformed(ActionEvent e) {

    String action = e.getActionCommand();       

    if( action.equals("new") )
        theApplication.onNew(theDocument);
    else if( action.equals("open") )
        theApplication.onOpen(null,theDocument,false);
    else if( action.equals("save") )
        theApplication.onSave(theDocument);
    else if( action.equals("saveas") )
        theApplication.onSaveAs(theDocument);
    else if( action.equals("print") )
        onPrint();

    //else if( action.equals("undo") )
    //onUndo();
    //else if( action.equals("redo") )
    //onRedo();

    else if( action.equals("cut") ) 
        cut();    
    else if( action.equals("copy") ) 
        copy();    
    else if( action.equals("delete") ) 
        cut();    

    else if( action.equals("transfer") ) 
        transfer(); 

    else if( action.equals("filterselection") )
        filterSelection();
    else if( action.equals("showallrows") )
        showAllRows();

    updateActions();
    }   

    public void structuresChanged(FragmentDocument.StructuresChangeEvent e) {
    if( !ignore_document_changes ) {
        update_header = true;    
        updateDocument();       
    }
    }  


    public void documentInit(BaseDocument.DocumentChangeEvent e) {
    if( !ignore_document_changes ) {
        update_header = true;
        super.documentInit(e);
    }
    }

    public void documentChanged(BaseDocument.DocumentChangeEvent e) {
    if( !ignore_document_changes ) {
        update_header = true;
        super.documentChanged(e);
    }
    }      
    
    protected void updateData() {
    }
}
