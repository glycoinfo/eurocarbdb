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

public class FragmentDetailsPanel extends SortingTablePanel<FragmentDocument> implements ActionListener {    

    // components   
    protected JLabel      theStructure;
    protected JToolBar    theToolBarDocument;
    protected JToolBar    theToolBarEdit;

    // data
    protected int current_ind = 0;     

    public FragmentDetailsPanel()  {
    super();
    }
       
    protected void initComponents() {
    super.initComponents();

    // create structure viewer
    theStructure = new JLabel();
    theStructure.setBorder(new BevelBorder(BevelBorder.RAISED));
    add(theStructure,BorderLayout.NORTH);    
        
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
    if( theDocument!=null ) 
        theDocument.removeDocumentChangeListener(this);    

    theDocument = getDocumentFromWorkspace(workspace);
    if( theDocument==null )
        theDocument = new FragmentDocument();
    
    theDocument.addDocumentChangeListener(this);    

    current_ind = 0;
    updateView();
    updateActions();
    }      

    protected void createActions() {
    theActionManager.add("new",FileUtils.defaultThemeManager.getImageIcon("new"),"Clear",KeyEvent.VK_N, "",this);
    theActionManager.add("open",FileUtils.defaultThemeManager.getImageIcon("open"),"Open",KeyEvent.VK_O, "",this);
    theActionManager.add("save",FileUtils.defaultThemeManager.getImageIcon("save"),"Save",KeyEvent.VK_S, "",this);
    theActionManager.add("saveas",FileUtils.defaultThemeManager.getImageIcon("saveas"),"Save as...",KeyEvent.VK_A, "",this);

    theActionManager.add("close",FileUtils.defaultThemeManager.getImageIcon("close"),"Close structure",KeyEvent.VK_S, "",this);
    theActionManager.add("previous",FileUtils.defaultThemeManager.getImageIcon("previous"),"Previous structure",KeyEvent.VK_L, "",this);
    theActionManager.add("next",FileUtils.defaultThemeManager.getImageIcon("next"),"Next structure",KeyEvent.VK_N, "",this);
    
    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "",this);

    // edit
    //theActionManager.add("undo",FileUtils.defaultThemeManager.getImageIcon("undo"),"Undo",KeyEvent.VK_U, "",this);
    //theActionManager.add("redo",FileUtils.defaultThemeManager.getImageIcon("redo"),"Redo", KeyEvent.VK_R, "",this);

    theActionManager.add("cut",FileUtils.defaultThemeManager.getImageIcon("cut"),"Cut",-1, "",this);
    theActionManager.add("copy",FileUtils.defaultThemeManager.getImageIcon("copy"),"Copy",KeyEvent.VK_C, "",this);
    theActionManager.add("delete",FileUtils.defaultThemeManager.getImageIcon("delete"),"Delete",-1, "",this);
    theActionManager.add("transfer",FileUtils.defaultThemeManager.getImageIcon("transfer"),"Copy fragments into canvas",KeyEvent.VK_V, "",this);

    theActionManager.add("charges",FileUtils.defaultThemeManager.getImageIcon("charges"),"Compute multiple charges and exchanges",-1, "",this);

    theActionManager.add("filterselection",FileUtils.defaultThemeManager.getImageIcon(""),"Show only selected fragments",-1, "",this);
    theActionManager.add("showallrows",FileUtils.defaultThemeManager.getImageIcon(""),"Show all fragments",-1, "",this);
    }

    protected void updateActions() {
    theActionManager.get("previous").setEnabled(current_ind>0);
    theActionManager.get("next").setEnabled(current_ind<(theDocument.getNoStructures()-1));
    theActionManager.get("close").setEnabled(!theDocument.isEmpty());

    theActionManager.get("save").setEnabled(theDocument.hasChanged());

    //theActionManager.get("undo").setEnabled(theDocument.getUndoManager().canUndo());
    //theActionManager.get("redo").setEnabled(theDocument.getUndoManager().canRedo());        

    boolean has_selection = theTable.getSelectedRows().length>0;
    theActionManager.get("cut").setEnabled(has_selection);
    theActionManager.get("copy").setEnabled(has_selection);
    theActionManager.get("delete").setEnabled(has_selection);
    theActionManager.get("transfer").setEnabled(has_selection);

    theActionManager.get("charges").setEnabled(has_selection);

    theActionManager.get("filterselection").setEnabled(has_selection);
    theActionManager.get("showallrows").setEnabled(!theTableSorter.isAllRowsVisible());
    }
   

    private JToolBar createToolBarDocument() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
      
    toolbar.add(theActionManager.get("previous"));    
    toolbar.add(theActionManager.get("close"));
    toolbar.add(theActionManager.get("next"));

    toolbar.addSeparator();
    
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

    menu.add(theActionManager.get("charges"));

    menu.addSeparator();

    menu.add(theActionManager.get("filterselection"));       
    menu.add(theActionManager.get("showallrows"));

    return menu;
    }
       
    //---------------
    // table model

    public int getNoStructures() {
    return theDocument.getNoStructures();
    }

    public Class<?> getColumnClass(int columnIndex) {
    if( columnIndex==0 )
        return Glycan.class;
    if( columnIndex==1 )
        return String.class;
    if( columnIndex==2 )
        return Double.class;
    if( columnIndex==3 )
        return IonCloud.class;
    if( columnIndex==4 )
        return IonCloud.class;
    if( columnIndex==5 )
        return Double.class;
    return Object.class;
    }
        
    public String getColumnName(int columnIndex) {
    if( columnIndex==0 )
        return "Fragment";
    if( columnIndex==1 )
        return "Type";
    if( columnIndex==2 )
        return "m/z";
    if( columnIndex==3 )
        return "Ions";
    if( columnIndex==4 )
        return "Neutral\nExchanges";
    if( columnIndex==5 )
        return "Fragment\nMass";
    return null;
    }
        
    public int getColumnCount() {
    return 6;
    }

    public int getRowCount() {
    return (theDocument.isEmpty()) ?0 :getCurrentFragments().size();
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
    if( !theDocument.isEmpty() ) {
        FragmentEntry f = getFragmentEntry(rowIndex);
        if( columnIndex==0 ) 
        return f.fragment;
        if( columnIndex==1 ) 
        return f.name;
        if( columnIndex==2 ) 
        return f.mz_ratio;
        if( columnIndex==3 ) 
        return f.getCharges();            
        if( columnIndex==4 ) 
        return f.getNeutralExchanges();            
        if( columnIndex==5 ) 
        return f.mass;
    }
    return null;
    }       
  
    //-----------------
    // data

    public void clear() {
    theDocument.init();
    }
    
  
    public Glycan getCurrentStructure() {
    return (theDocument.isEmpty()) ?null :theDocument.getStructure(current_ind);
    }
    

    public FragmentCollection getCurrentFragments() {
    return (theDocument.isEmpty()) ?null :theDocument.getFragments(current_ind);
    }

    public FragmentEntry getFragmentEntry(int l) {
    return (theDocument.isEmpty()) ?null :theDocument.getFragments(current_ind).elementAt(l);
    }

    public Collection<FragmentEntry> getSelectedFragments() {
    Vector<FragmentEntry> fragments = new Vector<FragmentEntry>();        

    int[] sel_ind = theTable.getSelectedRows();
    for( int i=0; i<sel_ind.length; i++ ) {
        int r = theTableSorter.modelIndex(sel_ind[i]);
        fragments.add(getFragmentEntry(r));
    }    
    
    return fragments;
    }

    //-----------
    // Visualization

    public void updateView() {
    GlycanRenderer gr = theWorkspace.getGlycanRenderer();

    theStructure.setIcon(theDocument.isEmpty() ?null :new ImageIcon(gr.getImage(getCurrentStructure(),false,true,true,0.667)));    

    fireTableChanged();
    }   
       
    
    //-----------------
    // actions   

    public void showPrevious() {
    if( !theDocument.isEmpty() && current_ind>0 ) {
        current_ind--;
        updateView();
    }
    }
    
    public void showNext() {
    if( !theDocument.isEmpty() && current_ind<(theDocument.getNoStructures()-1) ) {
        current_ind++;
        updateView();
    }
    }

    public void closeCurrent() {
    if( !theDocument.isEmpty() ) 
        theDocument.removeFragments(current_ind);        
    }

    public void onPrint() {
    theTable.print(theWorkspace.getPrinterJob(),getCurrentStructure());    
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
    }*/
   
    public void cut() {
    copy();
    delete();
    }
    
    public void delete() {

    // collect selected fragments
    Vector<FragmentEntry> to_remove = new Vector<FragmentEntry>();
        
    int[] sel_ind = theTable.getSelectedRows();
    for( int i=0; i<sel_ind.length; i++ ) 
        to_remove.add(getFragmentEntry(theTableSorter.modelIndex(sel_ind[i])));    

    // remove fragments
    theDocument.removeFragments(current_ind,to_remove);
    }

    public void copy() {    
    // get selected rows
    Vector<Glycan> structures = new Vector<Glycan>();

    int[] sel_ind = theTable.getSelectedRows();
    if( sel_ind.length>0 ) {
        // get selection
        for( int i=0; i<sel_ind.length; i++ ) {
        int r = theTableSorter.modelIndex(sel_ind[i]);
        
        // get structure
        structures.add(getFragmentEntry(r).fragment);
        }
        ClipUtils.setContents(new GlycanSelection(theTable.getSelectedData(),theWorkspace.getGlycanRenderer(),structures));
    }     
    }

    public void transfer() {
    GlycanCanvas theCanvas = theApplication.getCanvas();
    
    this.copy();    
    theCanvas.resetSelection();
    theCanvas.paste();
    }                 

    public void onComputeCharges() {
    
    // show annotation dialog
    FragmentOptions frag_opt = theWorkspace.getFragmentOptions();
    AnnotationOptions ann_opt = theWorkspace.getAnnotationOptions();
    AnnotationOptionsDialog dlg = new AnnotationOptionsDialog(theApplication,frag_opt,ann_opt,false,true);

    dlg.setVisible(true);     
    if( !dlg.getReturnStatus().equals("OK") )
        return;    
    
    // compute charges and exchanges    
    Collection<FragmentEntry> fragments = getSelectedFragments();
    Collection<FragmentEntry> fragments_charges = AnnotationThread.computeChargesAndExchanges(getCurrentStructure(),fragments,ann_opt);
    
    // add fragments
    theDocument.addFragments(current_ind,fragments_charges);
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
    if( action.equals("previous") )
        showPrevious();
    else if( action.equals("next") )
        showNext();
    else if( action.equals("close") )
        closeCurrent();

    else if( action.equals("print") )
        onPrint();

    else if( action.equals("new") )
        theApplication.onNew(theDocument);
    else if( action.equals("open") )
        theApplication.onOpen(null,theDocument,false);
    else if( action.equals("save") )
        theApplication.onSave(theDocument);
    else if( action.equals("saveas") )
        theApplication.onSaveAs(theDocument);


    /*else if( action.equals("undo") )
        onUndo();
    else if( action.equals("redo") )
        onRedo();
    */

    else if( action.equals("cut") ) 
        cut();    
    else if( action.equals("copy") ) 
        copy();    
    else if( action.equals("delete") ) 
        delete();    

    else if( action.equals("transfer") ) 
        transfer(); 

    else if( action.equals("charges") ) 
        onComputeCharges(); 

    else if( action.equals("filterselection") )
        filterSelection();
    else if( action.equals("showallrows") )
        showAllRows();

    updateActions();
    }    
   

    protected void updateData() {
    current_ind = Math.min(current_ind,theDocument.getNoStructures()-1);    
    current_ind = Math.max(current_ind,0);
    }   

   
}

