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
import java.net.MalformedURLException;

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

public class DictionariesEditPanel extends TablePanel<ProfilerPlugin> implements ActionListener, ItemListener, ChangeListener, ProfilerPlugin.DictionariesChangeListener {

    // class
    
    private class DictionaryEntry {
    public DictionaryEntry(StructureType src) throws Exception {
        structure = src.generateStructure(theWorkspace.getDefaultMassOptions());
        type = src.getType();
        source = src.getSource();
        mz_ratio = structure.computeMZ();
        generator = src;
    }
    
    public Glycan structure;
    public String type;
    public String source;
    public Double mz_ratio;    
    public StructureType generator;
    }

    private static final int ROWS = 10;

    // components
    private JToolBar    theToolBarNavigation;    
    private JToolBar    theToolBarDocument;
    private JComboBox   theDictionarySelector;
    private JSlider     theDictionarySlider;
    private JLabel      theDictionaryStatus;

    // data
    private int current_page_start;
    private StructureDictionary current_dictionary;
    private Vector<DictionaryEntry> ordered_entries;
    private Vector<DictionaryEntry> shown_entries;    

    private boolean ignore_slider = false;
    private boolean ignore_selector = false;
    
    public DictionariesEditPanel(ProfilerPlugin profiler) {
    super();
    setProfiler(profiler); 
    }
     
    protected void initComponents() {
    super.initComponents(); 

    // create dictionary selector
    theDictionarySelector = new JComboBox();
    theDictionarySelector.setEditable(false);
    theDictionarySelector.addItemListener(this);

    theDictionarySlider = new JSlider();
    theDictionarySlider.addChangeListener(this);

    theDictionaryStatus = new JLabel("");

    JPanel top_left_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top_left_panel.add(new JLabel("Dictionary"));
    top_left_panel.add(theDictionarySelector);

    JPanel top_panel = new JPanel(new BorderLayout());
    top_panel.add(top_left_panel,BorderLayout.WEST);
    top_panel.add(theDictionarySlider,BorderLayout.CENTER);
    top_panel.add(theDictionaryStatus,BorderLayout.EAST);
    add(top_panel,BorderLayout.NORTH);

    // set dictionary table
    theTable.setGlycanScale(0.35);
    
    // set the toolbars
    JPanel bottom_panel = new JPanel(new BorderLayout());
    bottom_panel.add(theToolBarNavigation = createToolBarNavigation(), BorderLayout.NORTH);
    bottom_panel.add(theToolBarDocument = createToolBarDocument(), BorderLayout.CENTER);
    add(bottom_panel,BorderLayout.SOUTH);
    }

    protected ProfilerPlugin getDocumentFromWorkspace(GlycanWorkspace workspace) {
    return null;
    }

    protected void setDocumentFromWorkspace(GlycanWorkspace workspace) {
    }

  
    public void setProfiler(ProfilerPlugin profiler) {
    theDocument = profiler;
    theDocument.addDictionariesChangeListener(this);
    
    if( theDocument.getDictionaries().size()>0 )
        setDictionary(theDocument.getDictionaries().iterator().next());
    else
        setDictionary(null);
    }

    public void showDictionary(String name) {
    theDictionarySelector.setSelectedItem(name);  
    }

    private void setDictionary(StructureDictionary dictionary) {
    if( dictionary!=current_dictionary ) {
        // prevent concurrent modification on listeners list
        if( current_dictionary!=null )
        current_dictionary.removeDocumentChangeListener(this);
    
        current_dictionary = dictionary;

        if( current_dictionary!=null )
        current_dictionary.addDocumentChangeListener(this);    
    }
     
    current_page_start = 0;
    
    updateData();
    updateActions();
    updateView();
    }

    protected void createActions() {

    theActionManager.add("first",FileUtils.defaultThemeManager.getImageIcon("first"),"First page",-1, "",this);
    theActionManager.add("pprevious",FileUtils.defaultThemeManager.getImageIcon("pprevious"),"Five pages before",-1, "",this);
    theActionManager.add("previous",FileUtils.defaultThemeManager.getImageIcon("previous"),"Previous page",-1, "",this);
    theActionManager.add("next",FileUtils.defaultThemeManager.getImageIcon("next"),"Next page",-1, "",this);
    theActionManager.add("nnext",FileUtils.defaultThemeManager.getImageIcon("nnext"),"Five pages after",-1, "",this);
    theActionManager.add("last",FileUtils.defaultThemeManager.getImageIcon("last"),"Last page",-1, "",this);


    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "",this);
    
    theActionManager.add("cut",FileUtils.defaultThemeManager.getImageIcon("cut"),"Cut",-1, "",this);
    theActionManager.add("copy",FileUtils.defaultThemeManager.getImageIcon("copy"),"Copy",KeyEvent.VK_C, "",this);
    theActionManager.add("paste",FileUtils.defaultThemeManager.getImageIcon("paste"),"Paste",KeyEvent.VK_C, "",this);
    theActionManager.add("delete",FileUtils.defaultThemeManager.getImageIcon("delete"),"Delete",-1, "",this);
    
    theActionManager.add("transfer",FileUtils.defaultThemeManager.getImageIcon("transfer"),"Copy structures to canvas",KeyEvent.VK_V, "",this);

    theActionManager.add("goto",FileUtils.defaultThemeManager.getImageIcon("goto"),"Jump to a specific m/z value",-1, "",this);
    theActionManager.add("search",FileUtils.defaultThemeManager.getImageIcon("search"),"Search in the current database",-1, "",this);

    }

    protected void updateActions() {
    if( current_dictionary!=null ) {
        boolean has_selection = theTable.getSelectedRows().length>0;
        boolean writeable = current_dictionary.isOnFileSystem();

        theActionManager.get("first").setEnabled(current_page_start>0);
        theActionManager.get("pprevious").setEnabled(current_page_start>0);
        theActionManager.get("previous").setEnabled(current_page_start>0);
        if( ordered_entries!=null ) {
        theActionManager.get("next").setEnabled(current_page_start<ordered_entries.size()-ROWS);
        theActionManager.get("nnext").setEnabled(current_page_start<ordered_entries.size()-ROWS);
        theActionManager.get("last").setEnabled(current_page_start<ordered_entries.size()-ROWS);
        }
        else {
        theActionManager.get("next").setEnabled(false);
        theActionManager.get("nnext").setEnabled(false);
        theActionManager.get("last").setEnabled(false);
        }
        theActionManager.get("cut").setEnabled(writeable && has_selection);
        theActionManager.get("copy").setEnabled(has_selection);
        theActionManager.get("paste").setEnabled(writeable);
        theActionManager.get("delete").setEnabled(writeable && has_selection);

        theActionManager.get("transfer").setEnabled(has_selection);
    }
    else {
        theActionManager.get("first").setEnabled(false);
        theActionManager.get("pprevious").setEnabled(false);
        theActionManager.get("previous").setEnabled(false);
        theActionManager.get("next").setEnabled(false);
        theActionManager.get("nnext").setEnabled(false);
        theActionManager.get("last").setEnabled(false);
        
        theActionManager.get("cut").setEnabled(false);
        theActionManager.get("copy").setEnabled(false);
        theActionManager.get("paste").setEnabled(false);
        theActionManager.get("delete").setEnabled(false);

        theActionManager.get("transfer").setEnabled(false);
    }
    }
   

    private JToolBar createToolBarNavigation() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
      
    toolbar.add(theActionManager.get("first"));
    toolbar.add(theActionManager.get("pprevious"));
    toolbar.add(theActionManager.get("previous"));
    toolbar.add(theActionManager.get("next"));
    toolbar.add(theActionManager.get("nnext"));
    toolbar.add(theActionManager.get("last"));

    return toolbar;
    }


    private JToolBar createToolBarDocument() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);  

    toolbar.add(theActionManager.get("print"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("cut"));
    toolbar.add(theActionManager.get("copy"));
    toolbar.add(theActionManager.get("paste"));
    toolbar.add(theActionManager.get("delete"));
    toolbar.add(theActionManager.get("transfer"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("goto"));
    toolbar.add(theActionManager.get("search"));


    return toolbar;
    }

    protected JPopupMenu createPopupMenu() {

    JPopupMenu menu = new JPopupMenu();

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
        return String.class;
    if( columnIndex==2 )
        return String.class;
    if( columnIndex==3 )
        return Double.class;    

    return Object.class;
    }

    public String getColumnName(int columnIndex) {
    if( columnIndex==0 )
        return "Structure";
    if( columnIndex==1 )
        return "Type";
    if( columnIndex==2 )
        return "Source";
    if( columnIndex==3 )
        return "m/z";
    return null;
    }
    
    public int getColumnCount() {
    return 4;
    }

    public int getRowCount() {
    if( ordered_entries==null )
        return 0;        
    return Math.min(ROWS,ordered_entries.size()-current_page_start);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
    if( columnIndex==0 ) 
        return shown_entries.get(rowIndex).structure;
    if( columnIndex==1 ) 
        return shown_entries.get(rowIndex).type;
    if( columnIndex==2 ) 
        return shown_entries.get(rowIndex).source;
    if( columnIndex==3 ) 
        return shown_entries.get(rowIndex).mz_ratio;
    return null;
    }


    public boolean isCellEditable(int rowIndex, int columnIndex) {
    if( !current_dictionary.isOnFileSystem() )
        return false;
    return ( columnIndex==1 || columnIndex==2 );       
    }
  

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if( columnIndex==1 ) 
        current_dictionary.setType(shown_entries.get(rowIndex).generator, (String)aValue);    
    else if( columnIndex==2 ) 
        current_dictionary.setSource(shown_entries.get(rowIndex).generator, (String)aValue);

    try {
        current_dictionary.save();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    //-----------
    // Visualization

    

    public void updateData() {
    ordered_entries = new Vector<DictionaryEntry>();        
    try {
        if( current_dictionary!=null ) {
        for( StructureType st : current_dictionary.getStructureTypes() ) {
            DictionaryEntry de = new DictionaryEntry(st);
            de.structure = null; // free memory
            ordered_entries.add(de);
        }
        
        Collections.sort(ordered_entries, new Comparator<DictionaryEntry>() {
            public int compare(DictionaryEntry a, DictionaryEntry b) {
                if( a.mz_ratio<b.mz_ratio )
                return -1;
                if( a.mz_ratio>b.mz_ratio )
                return 1;
                return 0;            
            }
            });
        }
    }
    catch(Exception e) {
        LogUtils.report(e);
        ordered_entries.clear();
    }

    // check current_page_start
    current_page_start = Math.max(0,Math.min(current_page_start,ordered_entries.size() - (ordered_entries.size()%ROWS)));
    }

    public void updateShownEntries() {
    shown_entries = new Vector<DictionaryEntry>();        
    try {
        for( int i=current_page_start; i<current_page_start+ROWS && i<ordered_entries.size(); i++ )
        shown_entries.add(new DictionaryEntry(ordered_entries.get(i).generator));        
    }
    catch(Exception e) {
        LogUtils.report(e);
        shown_entries.clear();
    }    
    }

    public void updateSlider() {
    if( theDictionarySlider!=null ) {
        ignore_slider = true;
        if( ordered_entries==null ) {
        theDictionarySlider.setMinimum(0);
        theDictionarySlider.setMaximum(1);
        theDictionarySlider.setValue(0);
        }
        else {
        theDictionarySlider.setMinimum(0);
        theDictionarySlider.setMaximum(ordered_entries.size()/ROWS);
        theDictionarySlider.setValue(current_page_start/ROWS);
        }
        ignore_slider = false;
    }
    }
    
    public void updateStatus() {
    if( theDictionaryStatus!=null ) {
        String title = current_page_start + "/" + ordered_entries.size();
        theDictionaryStatus.setText(title);
    }
    } 

    public void updateView() {
    updateSlider();
    updateStatus();
    updateShownEntries();
    fireTableChanged();
    }  

    public void updateMasses() {
    updateData();
    updateActions();
    updateView();
    }
    
    public void showFirst() {
    if( current_page_start>0 ) {
        current_page_start=0;
        updateActions();
        updateView();
    }
    }

    public void showFiveBefore() {
    if( current_page_start>0 ) {
        current_page_start = Math.max(0,current_page_start-ROWS*5);
        updateActions();
        updateView();
    }
    }

    public void showPrevious() {
    if( current_page_start>0 ) {
        current_page_start = Math.max(0,current_page_start-ROWS);
        updateActions();
        updateView();
    }
    }

    public void showNext() {
    if( current_page_start<ordered_entries.size()-ROWS ) {
        current_page_start += ROWS;
        updateActions();
        updateView();
    }
    }

    public void showFiveAfter() {
    if( current_page_start<ordered_entries.size()-ROWS ) {
        current_page_start = Math.min(current_page_start+5*ROWS,ordered_entries.size() - (ordered_entries.size()%ROWS));
        updateActions();
        updateView();
    }
    }

    public void showLast() {
    if( current_page_start<ordered_entries.size()-ROWS ) {
        current_page_start = ordered_entries.size() - (ordered_entries.size()%ROWS);
        updateActions();
        updateView();
    }
    }
    
    public void onPrint() {
    theTable.print(theWorkspace.getPrinterJob(),null);    
    }
   
    public void cut() {
    copy();
    delete();
    }

    public void delete() {
    if( !current_dictionary.isOnFileSystem() )
        return;
    if( theTable.getSelectedRows().length==0 )
        return;

    if( JOptionPane.showConfirmDialog(theApplication,"Are you sure you want to permanently remove the selected structures?")!=JOptionPane.YES_OPTION ) 
        return ;

    // select which rows are removed
    Vector<StructureType> to_remove = new Vector<StructureType>();
    for( int sel_ind : theTable.getSelectedRows() )
        to_remove.add(shown_entries.get(sel_ind).generator);                    
    
    // remove structures
    current_dictionary.removeAll(to_remove);   
    current_dictionary.save();
    }

    public void copy() {    
    // get selected rows
    Vector<Glycan> structures = new Vector<Glycan>();
    Vector<StructureType> structure_types = new Vector<StructureType>();
    for( int sel_ind : theTable.getSelectedRows() ) {
        structures.add(shown_entries.get(sel_ind).structure);
        structure_types.add(shown_entries.get(sel_ind).generator);
    }
    
    if( structures.size()>0 ) {
        // get annotations
        ClipUtils.setContents(new DictionarySelection(theTable.getSelectedData(),structure_types,theWorkspace.getGlycanRenderer(),structures));
    }
    }

    public void paste() {
    if( current_dictionary==null || !current_dictionary.isOnFileSystem() )
        return;

    try {
        java.awt.datatransfer.Transferable t = ClipUtils.getContents();
        if( t!=null ) {
        if( t.isDataFlavorSupported(DictionarySelection.dictionaryFlavor) ) {
            // parse clipboard
            String content = TextUtils.consume((InputStream)t.getTransferData(DictionarySelection.dictionaryFlavor));
            Collection<StructureType> topaste = DictionarySelection.parseString(content);
            
            // add to dictionary
            current_dictionary.addAll(topaste);    
            current_dictionary.save();
        }
        else if( t.isDataFlavorSupported(DictionarySelection.glycoFlavor) ) {
            // parse clipboard
            String content = TextUtils.consume((InputStream)t.getTransferData(DictionarySelection.glycoFlavor));        
            Collection<Glycan> topaste = GlycanDocument.parseString(content,theWorkspace.getDefaultMassOptions());

            // ask for type and source            
            StoreStructuresDialog dlg = new StoreStructuresDialog(theApplication,theDocument,current_dictionary);
            dlg.setVisible(true);
            if( dlg.getReturnStatus().equals("Cancel") )
            return ;
           
            if( current_dictionary.addAll(topaste,dlg.getType(),dlg.getSource()) ) 
            current_dictionary.save();    
        }
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
    
    
    public void search() {       
    try {
		if( theDocument.search(null,current_dictionary,false) ) {
		    try {
		    theDocument.show("Search");
		    }
		    catch(Exception e){
		    }
		}
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }


    public void goToMZ() {
    String mz_str = JOptionPane.showInputDialog(this, "Insert m/z value"); 
    if( mz_str!=null ) {
        Double mz_ratio = Double.valueOf(mz_str);    

        int i=ROWS;
        for( ; i<ordered_entries.size() && mz_ratio>ordered_entries.get(i).mz_ratio; i+=ROWS );
        current_page_start = Math.max(0,i-ROWS);
        
        updateActions();
        updateView();
    }
    }    


    //-----------
    // listeners
   
    public void    itemStateChanged(ItemEvent e) {
    if( !ignore_selector && e.getStateChange()==ItemEvent.SELECTED ) 
        setDictionary(theDocument.getDictionary((String)theDictionarySelector.getSelectedItem()));
    }

    public void    stateChanged(ChangeEvent e) {
    if( !ignore_slider ) {
        current_page_start = theDictionarySlider.getValue()*ROWS;
        updateActions();
        updateView();
    }
    }
    
    public void actionPerformed(ActionEvent e) {

    String action = e.getActionCommand();       
    if( action.equals("first") )
        showFirst();
    else if( action.equals("pprevious") )
        showFiveBefore();
    else if( action.equals("previous") )
        showPrevious();
    else if( action.equals("next") )
        showNext();
    else if( action.equals("nnext") )
        showFiveAfter();
    else if( action.equals("last") )
        showLast();

    else if( action.equals("print") )
        onPrint();

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

    else if( action.equals("search") ) 
        search();
    else if( action.equals("goto") ) 
        goToMZ(); 

    updateActions();
    }

    public void dictionariesChanged(ProfilerPlugin.DictionariesChangeEvent e) {
    
    // update dictionaries list
    String to_sel = (String)theDictionarySelector.getSelectedItem();    
    theDictionarySelector.setModel(new DefaultComboBoxModel(new Vector<String>(theDocument.getDictionaryNames())));
    if( to_sel==null || ! theDocument.getDictionaryNames().contains(to_sel) ) 
        to_sel = theDocument.getDictionaryNames().iterator().next();

    ignore_selector = true;
    theDictionarySelector.setSelectedItem(to_sel);
    setDictionary(theDocument.getDictionary(to_sel));
    ignore_selector = false;
    }    
   
  
}