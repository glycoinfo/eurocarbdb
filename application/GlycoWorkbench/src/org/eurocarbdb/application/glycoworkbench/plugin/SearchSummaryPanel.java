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
import java.awt.print.*;

public class SearchSummaryPanel extends SortingTablePanel<AnnotatedPeakList> implements ActionListener, AnnotatedPeakList.AnnotationChangeListener {    

    // components
    protected JToolBar    theToolBar;
 
    protected boolean update_header;

    //
    public SearchSummaryPanel() {
    super();
    }
 
    protected void initComponents() {
    super.initComponents();

    // create toolbar
    theToolBar = createToolBar(); 
    add(theToolBar,BorderLayout.SOUTH);
    }
     
    
    public AnnotatedPeakList getDocumentFromWorkspace(GlycanWorkspace workspace) {
    return ( workspace!=null ) ?workspace.getSearchResults() :null;
    }
    
    public void setDocumentFromWorkspace(GlycanWorkspace workspace) {
    if( theDocument!=null )
        theDocument.removeAnnotationChangeListener(this);
    
    theDocument = getDocumentFromWorkspace(workspace);
    if( theDocument==null )
        theDocument = new AnnotatedPeakList();
    
    theDocument.addAnnotationChangeListener(this);

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
    theActionManager.add("annotate",FileUtils.defaultThemeManager.getImageIcon("annotate"),"Add to the annotated peak list",KeyEvent.VK_V, "",this);

    theActionManager.add("goto",FileUtils.defaultThemeManager.getImageIcon("goto"),"Jump to a specific m/z value",-1, "",this);

    theActionManager.add("filterselection",FileUtils.defaultThemeManager.getImageIcon(""),"Show only selected annotations",-1, "",this);
    theActionManager.add("showallrows",FileUtils.defaultThemeManager.getImageIcon(""),"Show all annotations",-1, "",this);
    theActionManager.add("addIsotopeCurves",FileUtils.defaultThemeManager.getImageIcon(""),"Show isotopic distributions",-1, "",this);
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
    theActionManager.get("annotate").setEnabled(has_selection);

    theActionManager.get("goto").setEnabled(getRowCount()>0);

    theActionManager.get("filterselection").setEnabled(has_selection);
    theActionManager.get("showallrows").setEnabled(!theTableSorter.isAllRowsVisible());
    theActionManager.get("addIsotopeCurves").setEnabled(has_selection); 
    }    

    private JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
      
    toolbar.add(theActionManager.get("print"));

    toolbar.addSeparator();
    
    toolbar.add(theActionManager.get("cut"));
    toolbar.add(theActionManager.get("copy"));
    toolbar.add(theActionManager.get("delete"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("transfer"));
    toolbar.add(theActionManager.get("annotate"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("goto"));

    return toolbar;
    }  

    protected JPopupMenu createPopupMenu() {

    JPopupMenu menu = new JPopupMenu();

    menu.add(theActionManager.get("cut"));
    menu.add(theActionManager.get("copy"));
    menu.add(theActionManager.get("delete"));

    menu.addSeparator();

    menu.add(theActionManager.get("transfer"));
    menu.add(theActionManager.get("annotate"));

    menu.addSeparator();

    menu.add(theActionManager.get("filterselection"));       
    menu.add(theActionManager.get("showallrows"));
    menu.add(theActionManager.get("addIsotopeCurves"));

    return menu;
    }

       
    //---------------
    // table model
 
    public Class<?> getColumnClass(int columnIndex) {
    if( columnIndex==0 )
        return Double.class;
    if( columnIndex==1 )
        return Double.class;
    if( columnIndex==2 )
        return Double.class;
    return Vector.class;
    }
        
    public String getColumnName(int columnIndex) {
    if( columnIndex==0 )
        return "Mass to\ncharge";
    if( columnIndex==1 )
        return "Intensity";
    if( columnIndex==2 )
        return "Relative\nintensity";
    return "Structure" + (columnIndex-3);
    }
        
    public int getColumnCount() {
    return 3 + theDocument.getNoStructures();
    }  

    public int getRowCount() {
    return theDocument.getNoPeaks();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
    if( columnIndex==0 ) 
        return theDocument.getMZ(rowIndex);
    if( columnIndex==1 ) 
        return theDocument.getIntensity(rowIndex);
    if( columnIndex==2 ) 
        return theDocument.getRelativeIntensity(rowIndex);
    return theDocument.getFragments(rowIndex,columnIndex-3);
    }   

    //-----------------
    // data   

    public void clear() {
    theDocument.clear();
    }                    

    //-----------
    // Visualization
    
    public void updateData() {
    }

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
        ignore_document_changes = true;

        // collect selected peaks
        Vector<Peak> peaks = new Vector<Peak>();

        int[] sel_ind = theTable.getSelectedRows();
        for( int i=0; i<sel_ind.length; i++ ) 
        peaks.add(theDocument.getPeak(theTableSorter.modelIndex(sel_ind[i])));

        // clear annotations                
        theDocument.clearAnnotationsFor(peaks);        
    
        // update table
        fireRowsChanged(sel_ind);

        // restore automatic update
        ignore_document_changes = false;
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

        int[] mod_inds = theTableSorter.modelIndexes(sel_inds);
        if( structures!=null )
        ClipUtils.setContents(new AnnotationSelection(theTable.getSelectedData(),theDocument.extractAnnotations(mod_inds),theWorkspace.getGlycanRenderer(),structures));
        else
        ClipUtils.setContents(new AnnotationSelection(theTable.getSelectedData(),theDocument.extractAnnotations(mod_inds)));
    }     
    }

    public void transfer() {
    GlycanCanvas theCanvas = theApplication.getCanvas();
    
    this.copy();    
    theCanvas.resetSelection();
    theCanvas.paste();
    }   

    public void annotate() {    

    int[] sel_ind = theTable.getSelectedRows();
    if( sel_ind.length>0 ) {
        Vector<Peak> p_toadd = new Vector<Peak>();
        AnnotatedPeakList apl_toadd = new AnnotatedPeakList();
        for( int l=0; l<theDocument.getNoStructures(); l++ ) {
        Glycan structure = theDocument.getStructure(l);
        PeakAnnotationCollection pac = theDocument.getPeakAnnotationCollection(l);
        for( int i=0; i<sel_ind.length; i++ ) {
            int r = theTableSorter.modelIndex(sel_ind[i]);
            apl_toadd.addPeakAnnotation(structure,pac.getPeakAnnotation(r),true);
            p_toadd.add(pac.getPeak(r));
        }
        }

        theWorkspace.getAnnotatedPeakList().merge(apl_toadd);
        theWorkspace.getPeakList().addAll(p_toadd);
    }     
    }

    
    public void goToMZ() {
    String m_z = JOptionPane.showInputDialog(theApplication, "Insert m/z value"); 
    if( m_z!=null ) {
        int mod_ind = theDocument.nearestTo(Double.valueOf(m_z));
        if( mod_ind!=-1 ) {
        int sel_ind = theTableSorter.viewIndex(mod_ind);
        theTable.setRowSelectionInterval(sel_ind,sel_ind);
        theTable.scrollRectToVisible(theTable.getCellRect(sel_ind,0,false));
        }
    }
    }

    public void filterSelection() {
    int[] sel_inds = theTable.getSelectedRows();
    if( sel_inds!=null & sel_inds.length>0 )
        theTableSorter.setVisibleRows(theTableSorter.modelIndexes(sel_inds));
    }

    public void showAllRows() {
    theTableSorter.resetVisibleRows();
    }

    public void addIsotopeCurves() {
    int focusRow = theTable.getSelectionModel().getLeadSelectionIndex();
    int focusColumn = theTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();          
    if( focusRow==-1 || focusColumn==-1 )
        return;

    PeakAnnotationMultiple pam = theDocument.getAnnotations(theTableSorter.modelIndex(focusRow));

    TreeMap<Peak,Collection<Annotation>> annotations = new TreeMap<Peak,Collection<Annotation>>();
    annotations.put(pam.getPeak(),pam.getAnnotations(focusColumn-3));
        
    try {
        theApplication.getPluginManager().runAction("Spectra","addIsotopeCurves",annotations);
        theApplication.getPluginManager().runAction("PeakList","addIsotopeCurves",annotations);
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    //-----------
    // listeners

    public void actionPerformed(ActionEvent e) {

    String action = e.getActionCommand();       

    if( action.equals("print") )
        onPrint();

    else if( action.equals("cut") ) 
        cut();    
    else if( action.equals("copy") ) 
        copy();    
    else if( action.equals("delete") ) 
        cut();    

    else if( action.equals("transfer") ) 
        transfer(); 
    else if( action.equals("annotate") ) 
        annotate(); 

    else if( action.equals("goto") ) 
        goToMZ(); 
    else if( action.equals("filterselection") )
        filterSelection();
    else if( action.equals("showallrows") )
        showAllRows();
    else if( action.equals("addIsotopeCurves") )
        addIsotopeCurves();

    updateActions();
    }          
  

    public void structuresChanged(AnnotatedPeakList.AnnotationChangeEvent e) {
    if( !ignore_document_changes ) {
        update_header = true;
        updateDocument();
    }
    }  

    public void annotationsChanged(AnnotatedPeakList.AnnotationChangeEvent e) {
    if( !ignore_document_changes ) 
        updateDocument();
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

    
}
