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

public class DictionariesManagerPanel extends SortingTablePanel<ProfilerPlugin> implements ActionListener, ProfilerPlugin.DictionariesChangeListener {    
     
    // components 
    protected JToolBar theToolBar;

    public DictionariesManagerPanel(ProfilerPlugin profiler) {
    super();
    setProfiler(profiler);
    }

    protected void initComponents() {
    super.initComponents();

    // create toolbar
    theToolBar = createToolBar(); 
    add(theToolBar,BorderLayout.SOUTH);

    // create table
    theTable.setShowVerticalLines(false);
    theTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    theTable.setUseStyledText(false);
    }

    protected ProfilerPlugin getDocumentFromWorkspace(GlycanWorkspace workspace) {
    return null;
    }

    protected void setDocumentFromWorkspace(GlycanWorkspace workspace) {
    }

  
    public void setProfiler(ProfilerPlugin _theDocument) {
    // reset old list
    if( theDocument!=null )
        theDocument.removeDictionariesChangeListener(this);

    // set new list
    if( _theDocument!=null )
        theDocument = _theDocument;
    else
        theDocument = new ProfilerPlugin(this.theApplication);

    theDocument.addDictionariesChangeListener(this);

    // update view
    updateView();
    updateActions();
    }

    protected void createActions() {
    theActionManager.add("add",FileUtils.defaultThemeManager.getImageIcon("add"),"Add new dictionary",KeyEvent.VK_N, "",this);
    theActionManager.add("open",FileUtils.defaultThemeManager.getImageIcon("open"),"Open existing dictionary",KeyEvent.VK_O, "",this);
    theActionManager.add("delete",FileUtils.defaultThemeManager.getImageIcon("delete"),"Delete selected dictionary",KeyEvent.VK_DELETE, "",this);
    theActionManager.add("edit",FileUtils.defaultThemeManager.getImageIcon("edit"),"Edit selected dictionary",KeyEvent.VK_E, "",this);
    }

    protected void updateActions() {    
    boolean has_selection = theTable.getSelectedRows().length>0;
    boolean writeable = has_selection && getSelectedDictionary().isOnFileSystem();

    theActionManager.get("delete").setEnabled(writeable);
    theActionManager.get("edit").setEnabled(has_selection);
    }  

    private JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
      
    toolbar.add(theActionManager.get("add"));
    toolbar.add(theActionManager.get("open"));    

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("delete"));
    toolbar.add(theActionManager.get("edit"));
    
    return toolbar;
    }

    protected JPopupMenu createPopupMenu() {

    JPopupMenu menu = new JPopupMenu();

    menu.add(theActionManager.get("delete"));
    menu.add(theActionManager.get("edit"));

    return menu;
    }

    // table model

    public Class<?> getColumnClass(int columnIndex) {
    if( columnIndex==0 )
        return String.class;
    if( columnIndex==1 )
        return Integer.class;
    if( columnIndex==2 )
        return String.class;
    return Object.class;
    }
        
    public String getColumnName(int columnIndex) {
    if( columnIndex==0 )
        return "Name";
    if( columnIndex==1 )
        return "Size";
    if( columnIndex==2 )
        return "Path";
    return null;
    }
        
    public int getColumnCount() {
    return 3;
    }

    public int getRowCount() {
    if( theDocument==null )
        return 0;
    return theDocument.getDictionaries().size();
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
    StructureDictionary sd = new ArrayList<StructureDictionary>(theDocument.getDictionaries()).get(rowIndex);

    if( columnIndex==0 ) 
        return sd.getDictionaryName();
    if( columnIndex==1 ) 
        return sd.size();
    if( columnIndex==2 ) 
        return (sd.isOnFileSystem()) ?sd.getFileName() :"";        

    return null;
    }       
    
    // Actions

    public StructureDictionary getSelectedDictionary() {
    if( theTable.getSelectedRow()>=0 )        
        return new ArrayList<StructureDictionary>(theDocument.getDictionaries()).get(theTable.getSelectedRow());
    return null;
    }
   
    public boolean createUserDatabase() {
    try {
        // ask name
        String name = JOptionPane.showInputDialog(this, "Specify a database name"); 
        if( name==null || name.length()==0 )
        return false;
    
        // specify a file        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(name + ".gwd"));
        fileChooser.setDialogTitle("Specify a file to store the database" );
        fileChooser.addChoosableFileFilter(new ExtensionFileFilter("gwd", "GlycoWorkbench dictionary file"));
        fileChooser.setCurrentDirectory(theDocument.getWorkspace().getFileHistory().getRecentFolder());
        
        // visualizzo la dialog
        if( fileChooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION ) 
        return false;
        
        // aggiunge l'estension        
        String filename = fileChooser.getSelectedFile().getAbsolutePath();
        javax.swing.filechooser.FileFilter ff = fileChooser.getFileFilter();
        if( ff!=fileChooser.getAcceptAllFileFilter() && (ff instanceof ExtensionFileFilter) ) 
        filename = FileUtils.enforceExtension(filename,((ExtensionFileFilter)ff).getDefaultExtension());
        
        // chiede conferma prima di sovrascrivere il file
        File file = new File(filename);                    
        if( file.exists() ) {
        int retValue = JOptionPane.showOptionDialog(this, "File exists. Overwrite file: " + filename + "?",
                                "Salva documento", JOptionPane.YES_NO_CANCEL_OPTION, 
                                JOptionPane.QUESTION_MESSAGE, null, null, null);  
        if( retValue!=JOptionPane.YES_OPTION )
            return false;
        }            
        
        // create dictionary and save
        StructureDictionary toadd = new StructureDictionary(name);
        toadd.save(filename);
        theDocument.addUserDictionary(toadd);
        
        fireTableChanged();
        return true;
    }
    catch(Exception e) {
        LogUtils.report(e);
        return false;
    }
    }

    public void loadUserDatabase() {
    try { 
        // imposto la dialog per l'apertura del file      
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a database file");
        fileChooser.addChoosableFileFilter(new ExtensionFileFilter("gwd", "GlycoWorkbench dictionary file"));
        fileChooser.setCurrentDirectory(theDocument.getWorkspace().getFileHistory().getRecentFolder());

        // visualizzo la dialog
        if( fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION ) 
        return;
        
        // retrieve file path
        String filename = fileChooser.getSelectedFile().getAbsolutePath();
        if( !FileUtils.exists(filename) ) 
        return;
    
        // load dictionary
        StructureDictionary toadd = new StructureDictionary(filename,true,null);
        if( theDocument.containsDictionary(toadd) ) {
        JOptionPane.showMessageDialog(this,"The database selected is already loaded.", "Duplicate database", JOptionPane.ERROR_MESSAGE);
        return; 
        }
        
        // add dictionary
        theDocument.addUserDictionary(toadd);
        fireTableChanged();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    public void removeSelectedDatabase() {
    if( theTable.getSelectedRow()==-1 )
        return;

    int retValue = JOptionPane.showOptionDialog(this, "Are you sure you want to permanently remove the user database?",
                            "", JOptionPane.YES_NO_OPTION, 
                            JOptionPane.QUESTION_MESSAGE, null, null, null);  
    if( retValue!=JOptionPane.YES_OPTION )
        return;

    theDocument.removeDictionary(getSelectedDictionary());
    fireTableChanged();
    }


    public void editSelectedDatabase() {
    try {
        StructureDictionary selected = getSelectedDictionary();
        if( selected!=null ) {
        theDocument.getDictionariesEditPanel().showDictionary(selected.getDictionaryName());
        theDocument.show("Structures");
        }
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    //-----------
    // listeners


    public void actionPerformed(ActionEvent e) {

    String action = e.getActionCommand();       

    if( action.equals("add") ) 
        createUserDatabase();    
    else if( action.equals("open") ) 
        loadUserDatabase();    
    else if( action.equals("delete") ) 
        removeSelectedDatabase();    
    else if( action.equals("edit") ) 
        editSelectedDatabase();        
    
    updateActions();
     }  

    public void dictionariesChanged(ProfilerPlugin.DictionariesChangeEvent e) {
    updateView();
    updateActions();
    }

    public void updateData() {
    }

}
