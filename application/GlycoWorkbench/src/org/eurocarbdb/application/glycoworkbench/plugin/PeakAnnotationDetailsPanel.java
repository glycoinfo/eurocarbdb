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

public class PeakAnnotationDetailsPanel extends SortingTablePanel<AnnotatedPeakList> implements ActionListener {    

    // components

    protected JLabel      theStructure;
    protected JToolBar    theToolBarDocument;
    protected JToolBar    theToolBarEdit;
   
    // data
    protected int current_ind = 0;
   
    //

    public PeakAnnotationDetailsPanel() {
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
    theActionManager.add("paste",FileUtils.defaultThemeManager.getImageIcon("paste"),"Paste",KeyEvent.VK_P, "",this);
    theActionManager.add("delete",FileUtils.defaultThemeManager.getImageIcon("delete"),"Delete",-1, "",this);

    theActionManager.add("transfer",FileUtils.defaultThemeManager.getImageIcon("transfer"),"Copy fragments into canvas",KeyEvent.VK_V, "",this);

    theActionManager.add("goto",FileUtils.defaultThemeManager.getImageIcon("goto"),"Jump to a specific m/z value",-1, "",this);

    theActionManager.add("selectsimilar",FileUtils.defaultThemeManager.getImageIcon(""),"Select annotations with equal structures",-1, "",this);
    theActionManager.add("filterselection",FileUtils.defaultThemeManager.getImageIcon(""),"Show only selected annotations",-1, "",this);
    theActionManager.add("showallrows",FileUtils.defaultThemeManager.getImageIcon(""),"Show all annotations",-1, "",this);
    theActionManager.add("addIsotopeCurves",FileUtils.defaultThemeManager.getImageIcon(""),"Show isotopic distributions",-1, "",this);
    }

    protected void updateActions() {
    theActionManager.get("close").setEnabled(theDocument.getNoStructures()!=0);
    theActionManager.get("previous").setEnabled(current_ind>0);
    theActionManager.get("next").setEnabled(current_ind<(theDocument.getNoStructures()-1));
    
    theActionManager.get("save").setEnabled(theDocument.hasChanged());

    //theActionManager.get("undo").setEnabled(theDocument.getUndoManager().canUndo());
    //theActionManager.get("redo").setEnabled(theDocument.getUndoManager().canRedo());

    boolean has_selection = theTable.getSelectedRows().length>0;
    theActionManager.get("cut").setEnabled(has_selection);
    theActionManager.get("copy").setEnabled(has_selection);
    theActionManager.get("delete").setEnabled(has_selection);

    theActionManager.get("transfer").setEnabled(has_selection);

    theActionManager.get("goto").setEnabled(getRowCount()>0);
    theActionManager.get("selectsimilar").setEnabled(has_selection);
    theActionManager.get("filterselection").setEnabled(has_selection);
    theActionManager.get("showallrows").setEnabled(!theTableSorter.isAllRowsVisible());
    theActionManager.get("addIsotopeCurves").setEnabled(has_selection);
 
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
    toolbar.add(theActionManager.get("paste"));
    toolbar.add(theActionManager.get("delete"));
    toolbar.add(theActionManager.get("transfer"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("goto"));

    return toolbar;
    }

    protected JPopupMenu createPopupMenu() {

    JPopupMenu menu = new JPopupMenu();

    menu.add(theActionManager.get("cut"));
    menu.add(theActionManager.get("copy"));
    menu.add(theActionManager.get("paste"));
    menu.add(theActionManager.get("delete"));
    menu.add(theActionManager.get("transfer"));
    
    menu.addSeparator();

    menu.add(theActionManager.get("selectsimilar"));
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
    if( columnIndex==3 )
        return Glycan.class;
    if( columnIndex==4 )
        return String.class;
    if( columnIndex==5 )
        return Double.class;
    if( columnIndex==6 )
        return Double.class;
    if( columnIndex==7 )
        return Double.class;
    if( columnIndex==8 )
        return Double.class;    
    if( columnIndex==9 )
        return IonCloud.class;
    if( columnIndex==10 )
        return IonCloud.class;

    return Object.class;
    }
        
    public String getColumnName(int columnIndex) {
    if( columnIndex==0 )
        return "Mass to\ncharge";
    if( columnIndex==1 )
        return "Intensity";
    if( columnIndex==2 )
        return "Relative\nIntensity";
    if( columnIndex==3 )
        return "Ion";
    if( columnIndex==4 )
        return "Type";
    if( columnIndex==5 )
        return "Score";
    if( columnIndex==6 )
        return "Accuracy";
    if( columnIndex==7 )
        return "Accuracy PPM";
    if( columnIndex==8 )
        return "Ion m/z";
    if( columnIndex==9 )
        return "Charges";
    if( columnIndex==10 )
        return "Neutral\nExchanges";
    return null;
    }
        
    public int getColumnCount() {
    return 11;
    }

   
    public int getRowCount() {
    if( theDocument.getNoStructures()>0 ) 
        return theDocument.getPeakAnnotationCollection(current_ind).size();    
    return 0;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
    PeakAnnotationCollection pac = theDocument.getPeakAnnotationCollection(current_ind);
    if( columnIndex==0 ) 
        return pac.getMZ(rowIndex);
    if( columnIndex==1 ) 
        return pac.getIntensity(rowIndex);
    if( columnIndex==2 ) 
        return pac.getRelativeIntensity(rowIndex);
    if( columnIndex==3 ) 
        return pac.getFragment(rowIndex);
    if( columnIndex==4 ) 
        return pac.getFragmentType(rowIndex);
    if( columnIndex==5 ) 
        return pac.getFragmentScore(rowIndex);
    if( columnIndex==6 ) 
        return pac.getAccuracy(rowIndex);
    if( columnIndex==7 ) 
        return pac.getAccuracyPPM(rowIndex);
    if( columnIndex==8 ) 
        return pac.getAnnotationMZ(rowIndex);
    if( columnIndex==9 ) 
        return pac.getIons(rowIndex);
    if( columnIndex==10 ) 
        return pac.getNeutralExchanges(rowIndex);
    return null;
    }
   

    //-----------------
    // data

    public void clear() {
    current_ind = 0;
    theDocument.clear();
    }                            

    //-----------
    // Visualization

    protected void updateView() {
    if( theStructure!=null ) {
        if( theDocument.getNoStructures()>0 )
        theStructure.setIcon(new ImageIcon(theWorkspace.getGlycanRenderer().getImage(theDocument.getStructure(current_ind),false,true,true,0.667)));
        else
        theStructure.setIcon(null);
    }

    fireTableChanged();
    }   
    
    
    protected void updateData() {
    current_ind = Math.min(current_ind,theDocument.getNoStructures()-1);    
    current_ind = Math.max(current_ind,0);
    }

    //-----------------
    // actions  

    public void closeCurrent() {
    if( theDocument.getNoStructures()>0 ) {
        int old_ind = current_ind;
        current_ind = Math.min(current_ind,theDocument.getNoStructures()-2);        
        current_ind = Math.max(current_ind,0);

        // long action
        theApplication.haltInteractions();
        theDocument.removePeakAnnotationsAt(old_ind);
        theApplication.restoreInteractions();
    }
    }

    public void showPrevious() {
    if( theDocument.getNoStructures()>0 && current_ind>0 ) {
        current_ind--;
        updateView();
    }
    }
    
    public void showNext() {
    if( theDocument.getNoStructures()>0 && current_ind<(theDocument.getNoStructures()-1) ) {
        current_ind++;
        updateView();
    }
    }      

    public void showStructure(int s_ind) {
    if( s_ind>=0 && s_ind<theDocument.getNoStructures() ) {
        current_ind = s_ind;
        updateView();
    }
    }
    
    public void onNew() {
    clear();
    }

    public void onPrint() {
    theTable.print(theWorkspace.getPrinterJob(),theDocument.getStructure(current_ind));    
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
    // suppress automatic update
    ignore_document_changes = true;
    
    int[] sel_ind = theTable.getSelectedRows();
    PeakAnnotationCollection pac = theDocument.getPeakAnnotationCollection(current_ind);
    
    // select which rows are removed
    Vector<PeakAnnotation> to_remove = new Vector<PeakAnnotation>();
    for( int i=0; i<sel_ind.length; i++ ) {
        PeakAnnotation pa = pac.getPeakAnnotation(theTableSorter.modelIndex(sel_ind[i]));
        //if( pa.getAnnotation()!=null && pa.getFragment()!=null ) 
        to_remove.add(pa);                    
    }
    
    // remove annotations
    for( PeakAnnotation pa : to_remove ) {
        int old_size = pac.size();
        int ind = theDocument.getPeakAnnotationCollection(current_ind).indexOf(pa);        
        if( pa.isAnnotated() ) 
        theDocument.removePeakAnnotation(current_ind,pa);
        else if( !theDocument.isAnnotated(pa.getPeak()) ) 
        theDocument.removePeak(pa.getPeak());        
        else
        ind = -1;
        
        // update table
        if( ind!=-1 ) {
        if( pac.size()!=old_size )
            fireRowDeleted(theTableSorter.viewIndex(ind));
        else
            fireRowChanged(theTableSorter.viewIndex(ind));
        }
    }
    
    // restore automatic update
    ignore_document_changes = false;
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
        Glycan fragment = theDocument.getPeakAnnotationCollection(current_ind).getFragment(mod_inds[i]);
        if( fragment!=null )
            structures.add(fragment);        
        }

        // get annotations
        ClipUtils.setContents(new AnnotationSelection(theTable.getSelectedData(),theDocument.extractAnnotations(current_ind,mod_inds),theWorkspace.getGlycanRenderer(),structures));
    }     
    }

    public void paste() {
    try {
        java.awt.datatransfer.Transferable t = ClipUtils.getContents();
        if( t!=null && t.isDataFlavorSupported(AnnotationSelection.annotationFlavor) ) {
        String content = TextUtils.consume((InputStream)t.getTransferData(AnnotationSelection.annotationFlavor));
        
        // parse clipboard
        theDocument.fromString(content,true,true);        
            }        
        } 
    catch (Exception e) {
        LogUtils.report(e);
    }
    }


    public void transfer() {
    GlycanCanvas theCanvas = theApplication.getCanvas();
    
    this.copy();    
    theCanvas.resetSelection();
    theCanvas.paste();
    }  

    public void goToMZ() {
    String m_z = JOptionPane.showInputDialog(theApplication, "Insert m/z value"); 
    if( m_z!=null ) {
        int mod_ind = theDocument.getPeakAnnotationCollection(current_ind).indexOf(Double.valueOf(m_z));
        if( mod_ind!=-1 ) {
        int sel_ind = theTableSorter.viewIndex(mod_ind);
        theTable.setRowSelectionInterval(sel_ind,sel_ind);
        theTable.scrollRectToVisible(theTable.getCellRect(sel_ind,0,false));
        }
    }
    }

    public void selectSimilarAnnotations() {
    PeakAnnotationCollection pac = theDocument.getPeakAnnotationCollection(current_ind);

    // get selected structures
    Vector<Glycan> structures = new Vector<Glycan>();
    for( int sel_row : theTable.getSelectedRows() ) {
        Glycan fragment = pac.getFragment(theTableSorter.modelIndex(sel_row));
        if( fragment!=null && !fragment.isEmpty() ) 
        structures.add(fragment);                
    }

    // select similar structures
    theTable.clearSelection();
    for( int i=0; i<pac.size(); i++ ) {
        for( Glycan g : structures ) {
        if( g.compareToIgnoreCharges(pac.getFragment(i))==0 ) {
            int sel_row = theTableSorter.viewIndex(i);
            theTable.addRowSelectionInterval(sel_row,sel_row);
        }
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
    PeakAnnotationCollection pac = theDocument.getPeakAnnotationCollection(current_ind);

    TreeMap<Peak,Collection<Annotation>> map = new TreeMap<Peak,Collection<Annotation>>();
    for( int sel_row : theTable.getSelectedRows() ) {
        PeakAnnotation pa = pac.getPeakAnnotation(theTableSorter.modelIndex(sel_row));
        if( map.get(pa.getPeak())==null )
        map.put(pa.getPeak(),new Vector<Annotation>());
        map.get(pa.getPeak()).add(pa.getAnnotation());            
    }
        
    try {
        theApplication.getPluginManager().runAction("Spectra","addIsotopeCurves",map);
        theApplication.getPluginManager().runAction("PeakList","addIsotopeCurves",map);
    }    
    catch(Exception e) {
        LogUtils.report(e);
    }
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

    else if( action.equals("new") )
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
    else if( action.equals("paste") ) 
        paste();    
    else if( action.equals("delete") ) 
        delete();    
    else if( action.equals("transfer") ) 
        transfer(); 

    else if( action.equals("goto") ) 
        goToMZ(); 
    else if( action.equals("selectsimilar") ) 
        selectSimilarAnnotations();
    else if( action.equals("filterselection") )
        filterSelection();
    else if( action.equals("showallrows") )
        showAllRows();
    else if( action.equals("addIsotopeCurves") )
        addIsotopeCurves();

    updateActions();
    }
     

}
