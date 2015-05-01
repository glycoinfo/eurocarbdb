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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
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

abstract public class DocumentPanel<DOCUMENTTYPE> extends JPanel implements ComponentListener, GlycanWorkspace.Listener, BaseDocument.DocumentChangeListener {

    protected static final long serialVersionUID = 0L;    
    
    // singletons    
    protected GlycoWorkbench theApplication;
    protected GlycanWorkspace theWorkspace;
    protected DOCUMENTTYPE theDocument;
    protected ActionManager theActionManager;
   
    // actions    
    protected boolean ignore_document_changes;    
    protected boolean delayed_workspace_update = false;
    protected boolean delayed_document_update = false;


    public DocumentPanel() {
    initSingletons();
    initActions();
    initComponents();
    finalSettings();
    }

    protected  void initSingletons() {
    theApplication = null;
    theWorkspace = new GlycanWorkspace();
    theDocument = getDocumentFromWorkspace(theWorkspace);
    }

    protected void initActions() {
    theActionManager = new ActionManager();    
    createActions();
    }

    abstract protected void initComponents();    

    protected void finalSettings() {
    setMinimumSize(new Dimension(0,0));
    setBackground(Color.white);
    this.setOpaque(true);
 
    ignore_document_changes = false;
    this.addComponentListener(this);                

    setWorkspace(theWorkspace);    
    }

    public void setApplication(GlycoWorkbench application) {
    theApplication = application;
    
    updateActions();
    updateView();        
    }

    public void setWorkspace(GlycanWorkspace workspace) {
    if( theWorkspace!=null ) {
        theWorkspace.removeWorkspaceListener(this);
        theWorkspace.removeDocumentChangeListener(this);
    }
    
    if( workspace==null )
        theWorkspace = new GlycanWorkspace();
    else
        theWorkspace = workspace;

    theWorkspace.addWorkspaceListener(this);
    theWorkspace.addDocumentChangeListener(this);

    setDocumentFromWorkspace(workspace);
    } 
    
    abstract protected DOCUMENTTYPE getDocumentFromWorkspace(GlycanWorkspace workspace);

    abstract protected void setDocumentFromWorkspace(GlycanWorkspace workspace);
    
    abstract protected void createActions();
    
    abstract protected void updateActions();

    abstract protected void updateData();

    abstract protected void updateView();

   
    // events

    protected void updateWorkspace() {
    if( !ignore_document_changes ) {
        if( this.isVisible() && this.getSize().width>0 && this.getSize().height>0 ) {
        theApplication.haltInteractions();
        
        setDocumentFromWorkspace(theWorkspace);
        delayed_workspace_update = false;
        delayed_document_update = false;

        theApplication.restoreInteractions();
        }
        else 
        delayed_workspace_update = true;
    }
    }    

    protected void updateDocument() {
    if( this.isVisible() && this.getSize().width>0 && this.getSize().height>0 ) {
        theApplication.haltInteractions();

        updateData();
        updateView();
        updateActions();
        delayed_document_update = false;

        theApplication.restoreInteractions();
    }
    else {
        delayed_document_update = true;
    }
    }  

    protected boolean checkForUpdates() {
    if( ignore_document_changes ) 
        return false;

    if( delayed_workspace_update ) {
        updateWorkspace();
        return true;
    }
    else if( delayed_document_update ) {
        updateDocument();
        return true;
    }
    return false;
    }

    public void documentInit(BaseDocument.DocumentChangeEvent e) {
    if( !ignore_document_changes ) {
        if( e.getSource()==theWorkspace ) 
        updateWorkspace();
        else 
        updateDocument();        
    }
    }

    public void documentChanged(BaseDocument.DocumentChangeEvent e) {
    if( !ignore_document_changes ) {
        if( e.getSource()==theWorkspace ) 
        updateWorkspace();
        else
        updateDocument();
    }
    }

    public void internalDocumentChanged(GlycanWorkspace.Event e) {
    }
    
    public void currentScanChanged(GlycanWorkspace.Event e) {
    updateWorkspace();
    }
    
    public void scanAdded(GlycanWorkspace.Event e) {
    }
    
    public void scanRemoved(GlycanWorkspace.Event e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }
    
    public void componentResized(ComponentEvent e) {    
    checkForUpdates();
    }

    public void componentShown(ComponentEvent e) {
    checkForUpdates();
    }

    public void paint(Graphics g) {
    checkForUpdates();
    super.paint(g);
    }     

}