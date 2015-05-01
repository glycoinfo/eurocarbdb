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

public class FragmentEditorPanel extends JPanel implements ActionListener, BaseDocument.DocumentChangeListener, FragmentCanvas.SelectionChangeListener, GlycanWorkspace.Listener  {

    private static final long serialVersionUID = 0L;    

    // components
    protected GlycoWorkbench theApplication;
    protected GlycanWorkspace theWorkspace;
    protected GlycanDocument  theDocument;
   
    protected FragmentCanvas theCanvas;
    protected JToolBar       theToolBar;

    // data
    protected int    current_ind;
    protected Glycan current_structure;
   
    // actions     
    protected ActionManager theActionManager;


    //

    public FragmentEditorPanel()  {

    super(new BorderLayout());
    
    theApplication    = null;
    theWorkspace = new GlycanWorkspace();
    theDocument  = theWorkspace.getStructures();
    theDocument.addDocumentChangeListener(this);
    
    // init actions
    theActionManager = new ActionManager();
    createActions();

    // init data
    current_ind = 0;
    current_structure = null;
    
    // create fragment canvas
    theCanvas = new FragmentCanvas();
    theCanvas.setGlycanRenderer(theWorkspace.getGlycanRenderer());

    JScrollPane sp = new JScrollPane(theCanvas);
    theCanvas.setScrollPane(sp);
    add(sp,BorderLayout.CENTER);

    // create toolbar
    theToolBar = createToolBar(); 
    add(theToolBar,BorderLayout.SOUTH);    

    // final settings        
    theWorkspace.addDocumentChangeListener(this);
    theCanvas.addSelectionChangeListener(this);

    setMinimumSize(new Dimension(0,0));
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

    theCanvas.setGlycanRenderer(theWorkspace.getGlycanRenderer());

    setDocument(workspace.getStructures());
    }

    private void setDocument(GlycanDocument document) {
    // reset old
    theDocument.removeDocumentChangeListener(this);
    
    // set new
    theDocument  = document;
    theDocument.addDocumentChangeListener(this);

    // reset current_ind
    current_ind = 0;

    updateActions();
    updateStructure();
    updateView();
    }

    private void createActions() {
    theActionManager.add("previous",FileUtils.defaultThemeManager.getImageIcon("previous"),"Previous structure",KeyEvent.VK_L, "",this);
    theActionManager.add("next",FileUtils.defaultThemeManager.getImageIcon("next"),"Next structure",KeyEvent.VK_N, "",this);
    
    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "",this);

    theActionManager.add("copy",FileUtils.defaultThemeManager.getImageIcon("copy"),"Copy",KeyEvent.VK_C, "",this);
    theActionManager.add("transfer",FileUtils.defaultThemeManager.getImageIcon("transfer"),"Copy fragments into canvas",KeyEvent.VK_V, "",this);

    }

    private void updateActions() {
    theActionManager.get("previous").setEnabled(current_ind>0);
    theActionManager.get("next").setEnabled(current_ind<(theDocument.getNoStructures()-1));

    boolean has_selection = theCanvas.hasSelection();
    theActionManager.get("copy").setEnabled(has_selection);
    theActionManager.get("transfer").setEnabled(has_selection);
    }

    private JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
      
    toolbar.add(theActionManager.get("previous"));    
    toolbar.add(theActionManager.get("next"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("print"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("copy"));
    toolbar.add(theActionManager.get("transfer"));

    return toolbar;
    }
   

    //----------------
    // data


    public boolean setStructure(Glycan structure) {
    int ind = theDocument.indexOf(structure);
    if( ind!=-1 ) {
        current_ind = ind;
        updateStructure();
        updateActions();
        return true;
    }
    return false;
    }

    public boolean setStructure(Glycan structure, Residue frag_at) {
    int ind = theDocument.indexOf(structure);
    if( ind!=-1 ) {
        current_ind = ind;
        updateStructure(frag_at);
        updateActions();
        return true;
    }
    return false;
    }


    public boolean setStructure(Glycan structure, Linkage frag_at) {
    int ind = theDocument.indexOf(structure);
    if( ind!=-1 ) {
        current_ind = ind;
        updateStructure(frag_at);
        updateActions();
        return true;
    }
    return false;
    }

    private void updateStructure() {
    if( theDocument.getNoStructures()==0 )
        current_structure = null;
    else
        current_structure = theDocument.getStructure(current_ind).clone();

    theCanvas.setStructure(current_structure);
    }


    private void updateStructure(Residue frag_at) {
    if( theDocument.getNoStructures()==0 )
        current_structure = null;
    else
        current_structure = theDocument.getStructure(current_ind).clone();

    theCanvas.setStructure(current_structure,frag_at);
    }

    private void updateStructure(Linkage frag_at) {
    if( theDocument.getNoStructures()==0 )
        current_structure = null;
    else
        current_structure = theDocument.getStructure(current_ind).clone();

    theCanvas.setStructure(current_structure,frag_at);
    }

    
    //---------------------------
    // actions

    public void updateView() {
    theCanvas.repaint();
    }

    public void showPrevious() {
    if( !theDocument.isEmpty() && current_ind>0 ) {
        current_ind--;
        updateStructure();
    }
    }
    
    public void showNext() {
    if( !theDocument.isEmpty() && current_ind<(theDocument.getNoStructures()-1) ) {
        current_ind++;
        updateStructure();
    }
    }

    public void onPrint() {
    try {
        PrinterJob pj = theApplication.getWorkspace().getPrinterJob();
        if( pj!=null ) {
        pj.setPrintable(theCanvas);
        if( pj.printDialog() ) 
            theCanvas.print(pj);
        }

        //theCanvas.print(theApplication.getWorkspace().getPrinterJob());    
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }
   
    public void copy() {        
    ClipUtils.setContents(new GlycanSelection(theWorkspace.getGlycanRenderer(),theCanvas.getSelectedFragments()));
    }

    public void transfer() {
    GlycanCanvas theCanvas = theApplication.getCanvas();
    
    this.copy();    
    theCanvas.resetSelection();
    theCanvas.paste();
    }             

    public void actionPerformed(ActionEvent e) {
    
    String action = e.getActionCommand();       
    if( action.equals("previous") )
        showPrevious();
    else if( action.equals("next") )
        showNext();

    else if( action.equals("print") )
        onPrint();

    else if( action.equals("copy") ) 
        copy();    
    else if( action.equals("transfer") ) 
        transfer(); 

    updateActions();
    }

    //---------------------------
    // events

    public void documentInit(BaseDocument.DocumentChangeEvent e) {    
    if( e.getSource()==theWorkspace ) 
        setDocument(theWorkspace.getStructures());    
    else    
        documentChanged(e);
    }

    public void documentChanged(BaseDocument.DocumentChangeEvent e) {
    
    // update current_ind
    if( current_ind>=theDocument.getNoStructures() ) {
        current_ind = 0;            
        updateStructure();
    }
    else if( !theDocument.getStructure(current_ind).equalsStructure(current_structure) ) {
        updateStructure();
    }
    else if( !theDocument.getStructure(current_ind).getMassOptions().equals(current_structure.getMassOptions()) ) {
        updateStructure();
    }
        
    updateActions();            
    }
    

    public void selectionChanged(FragmentCanvas.SelectionChangeEvent e) {
    updateActions();
    }   

    public void internalDocumentChanged(GlycanWorkspace.Event e) {
    }

    public void currentScanChanged(GlycanWorkspace.Event e) {
    setDocument(theWorkspace.getStructures());
    }
    
    public void scanAdded(GlycanWorkspace.Event e) {
    }
    
    public void scanRemoved(GlycanWorkspace.Event e) {
    }
}
