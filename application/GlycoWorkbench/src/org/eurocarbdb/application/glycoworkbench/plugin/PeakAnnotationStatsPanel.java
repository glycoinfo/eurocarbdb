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

public class PeakAnnotationStatsPanel extends SortingTablePanel<AnnotatedPeakList> implements ActionListener {

    // components    
    protected JToolBar    theToolBarDocument;
    protected JToolBar    theToolBarEdit;

    // 

    public PeakAnnotationStatsPanel() {
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

    // create table
    theTable.setShowVerticalLines(false);
    }
     
    public AnnotatedPeakList getDocumentFromWorkspace(GlycanWorkspace workspace) {
    return ( workspace!=null ) ?workspace.getAnnotatedPeakList() :null;
    }
 
    public void setDocumentFromWorkspace(GlycanWorkspace workspace) {
    if( theDocument!=null )
        theDocument.removeDocumentChangeListener(this);

    theDocument = getDocumentFromWorkspace(workspace);
    if( theDocument==null )
        theDocument = new AnnotatedPeakList();

    theDocument.addDocumentChangeListener(this);

    updateView();
    updateActions();
    }      

    protected void createActions() {
    // print
    theActionManager.add("new",FileUtils.defaultThemeManager.getImageIcon("new"),"Clear",KeyEvent.VK_N, "",this);

    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "",this);
    theActionManager.add("open",FileUtils.defaultThemeManager.getImageIcon("open"),"Open",KeyEvent.VK_O, "",this);
    theActionManager.add("save",FileUtils.defaultThemeManager.getImageIcon("save"),"Save",KeyEvent.VK_S, "",this);
    theActionManager.add("saveas",FileUtils.defaultThemeManager.getImageIcon("saveas"),"Save as...",KeyEvent.VK_A, "",this);

    // edit
    //theActionManager.add("undo",FileUtils.defaultThemeManager.getImageIcon("undo"),"Undo",KeyEvent.VK_U, "",this);
    //theActionManager.add("redo",FileUtils.defaultThemeManager.getImageIcon("redo"),"Redo", KeyEvent.VK_R, "",this);

    theActionManager.add("cut",FileUtils.defaultThemeManager.getImageIcon("cut"),"Cut",KeyEvent.VK_T, "",this);
    theActionManager.add("copy",FileUtils.defaultThemeManager.getImageIcon("copy"),"Copy",KeyEvent.VK_C, "",this);
    theActionManager.add("paste",FileUtils.defaultThemeManager.getImageIcon("paste"),"Paste",KeyEvent.VK_P, "",this);
    theActionManager.add("delete",FileUtils.defaultThemeManager.getImageIcon("delete"),"Delete",KeyEvent.VK_DELETE, "",this);
    theActionManager.add("transfer",FileUtils.defaultThemeManager.getImageIcon("transfer"),"Copy structures into canvas",KeyEvent.VK_V, "",this);

    theActionManager.add("showdetails",FileUtils.defaultThemeManager.getImageIcon("showdetails"),"Show annotation details",KeyEvent.VK_D, "",this);
    }

    protected void updateActions() {    
    
    theActionManager.get("save").setEnabled(theDocument.hasChanged());

    boolean has_selection = theTable.getSelectedRows().length>0;

    //theActionManager.get("undo").setEnabled(theDocument.getUndoManager().canUndo());
    //theActionManager.get("redo").setEnabled(theDocument.getUndoManager().canRedo());

    theActionManager.get("cut").setEnabled(has_selection);
    theActionManager.get("copy").setEnabled(has_selection);
    theActionManager.get("delete").setEnabled(has_selection);
    theActionManager.get("transfer").setEnabled(has_selection);
    theActionManager.get("showdetails").setEnabled(has_selection);
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
    
    toolbar.add(theActionManager.get("showdetails"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("cut"));
    toolbar.add(theActionManager.get("copy"));
    toolbar.add(theActionManager.get("paste"));
    toolbar.add(theActionManager.get("delete"));      
    toolbar.add(theActionManager.get("transfer"));

    return toolbar;
    }  

    protected JPopupMenu createPopupMenu() {

    JPopupMenu menu = new JPopupMenu();

    menu.add(theActionManager.get("showdetails"));

    menu.addSeparator();

    menu.add(theActionManager.get("cut"));
    menu.add(theActionManager.get("copy"));
    menu.add(theActionManager.get("paste"));
    menu.add(theActionManager.get("delete"));
    menu.add(theActionManager.get("transfer"));

    return menu;
    }
       
    //---------------
    // table model

    public Class<?> getColumnClass(int columnIndex) {
    if( columnIndex==0 )
        return Glycan.class;
    if( columnIndex==1 )
        return Double.class;
    if( columnIndex==2 )
        return Double.class;
    if( columnIndex==3 )
        return Double.class;
    if( columnIndex==4 )
        return IntPair.class;
    if( columnIndex==5 )
        return IntPair.class;
    if( columnIndex==6 )
        return IntPair.class;


    return Object.class;
    }
        
    public String getColumnName(int columnIndex) {
    if( columnIndex==0 )
        return "Structure";
    if( columnIndex==1 )
        return "Coverage";
    if( columnIndex==2 )
        return "RMSD";
    if( columnIndex==3 )
        return "RMSD PPM";
    if( columnIndex==4 )
        return "Assigned";
    if( columnIndex==5 )
        return ">10% assigned";
    if( columnIndex==6 )
        return ">5% assigned";

    return null;
    }
        
    public int getColumnCount() {
    return 7;
    }  

    public int getRowCount() {
    return theDocument.getNoStructures();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
    if( columnIndex==0 ) 
        return theDocument.getStructure(rowIndex);        
    if( columnIndex==1 ) 
        return theDocument.getCoverage(rowIndex);
    if( columnIndex==2 ) 
        return theDocument.getRMSD(rowIndex);
    if( columnIndex==3 ) 
        return theDocument.getRMSD_PPM(rowIndex);
    if( columnIndex==4 ) 
        return new IntPair(theDocument.getNoAnnotatedPeaks(rowIndex),theDocument.getNoPeaks());
    if( columnIndex==5 ) 
        return new IntPair(theDocument.getNoAnnotatedPeaks(rowIndex,10.),theDocument.getNoPeaks(10.));
    if( columnIndex==6 ) 
        return new IntPair(theDocument.getNoAnnotatedPeaks(rowIndex,5.),theDocument.getNoPeaks(5.));
    return null;
    }
   

    //-----------------
    // data

    public void updateData() {
    }
    
    public void clear() {
    theDocument.clear();
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

    public void onShowDetails() {
    try {
        int s_ind = theTable.getSelectedRow();
        if( s_ind!=-1 ) {
        PluginManager pm = theApplication.getPluginManager();
        pm.show("Annotation","Details");
        ((AnnotationPlugin)pm.get("Annotation")).getPeakAnnotationDetailsPanel().showStructure(theTableSorter.modelIndex(s_ind));        
        }    
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    public void cut() {    
    copy();
    delete();
    }

    public void copy() {    
    // get selected rows
    Vector<Glycan> structures = new Vector<Glycan>();

    int[] sel_inds = theTable.getSelectedRows();
    if( sel_inds.length>0 ) {
        // get selection
        
        int[] mod_inds = theTableSorter.modelIndexes(sel_inds);
        for( int i=0; i<mod_inds.length; i++ ) {
        // get structure
        Glycan fragment = theDocument.getStructure(mod_inds[i]);
        if( fragment!=null )
            structures.add(fragment);        
        }
        ClipUtils.setContents(new AnnotationSelection(theTable.getSelectedData(),theDocument.extractCollections(mod_inds),theWorkspace.getGlycanRenderer(),structures));
    }     
    }

    public void paste() {
    try {
        java.awt.datatransfer.Transferable t = ClipUtils.getContents();
        if( t!=null && t.isDataFlavorSupported(AnnotationSelection.annotationFlavor) ) {
        String content = TextUtils.consume((InputStream)t.getTransferData(AnnotationSelection.annotationFlavor));
        theDocument.fromString(content,true,true);
            }        
        } 
    catch (Exception e) {
        LogUtils.report(e);
    }
    }
    
    public void delete() {
    theDocument.removePeakAnnotationsAt(theTableSorter.modelIndexes(theTable.getSelectedRows()));
    }

    public void transfer() {
    GlycanCanvas theCanvas = theApplication.getCanvas();
    
    this.copy();    
    theCanvas.resetSelection();
    theCanvas.paste();
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

    //if( action.equals("new") )
    //onNew();
    else if( action.equals("print") )
        onPrint();
    
    //else if( action.equals("undo") )
    //onUndo();
    //else if( action.equals("redo") )
    //onRedo();

    else if( action.equals("showdetails") ) 
        onShowDetails();
    else if( action.equals("cut") )
        cut();
    else if( action.equals("copy") ) 
        copy();    
    else if( action.equals("paste") ) 
        paste();    
    else if( action.equals("delete") )
        delete();
    else if( action.equals("transfer") ) 
        transfer(); 

    updateActions();
    }
    
}

