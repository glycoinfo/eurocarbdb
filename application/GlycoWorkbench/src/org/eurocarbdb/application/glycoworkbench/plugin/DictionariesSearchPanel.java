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

public class DictionariesSearchPanel extends TablePanel<ProfilerPlugin> implements ActionListener, ChangeListener {

    // class
    
    private class DictionaryEntry {
    public DictionaryEntry(StructureType src) throws Exception {
        structure = src.generateStructure(theWorkspace.getDefaultMassOptions());
        type = src.getType();
        source = src.getSource();
        database = src.getDatabase();
        mz_ratio = structure.computeMZ();
        generator = src;
    }
    
    public Glycan structure;
    public String type;
    public String source;
    public String database;
    public Double mz_ratio;    
    public StructureType generator;
    }

    private static final int ROWS = 10;

    // components
    private JToolBar    theToolBarNavigation;    
    private JToolBar    theToolBarDocument;
    private JSlider     theDictionarySlider;
    private JLabel      theDictionaryStatus;

    // data
    private int current_page_start;
    private Collection<StructureType> entries;
    private Vector<DictionaryEntry> ordered_entries;
    private Vector<DictionaryEntry> shown_entries;    

    private boolean ignore_slider = false;
    private boolean ignore_selector = false;
    
    public DictionariesSearchPanel(ProfilerPlugin profiler) {
    super();
    setProfiler(profiler); 
    setData(new Vector<StructureType>());
    }
     
    protected void initComponents() {
    super.initComponents(); 

    // create dictionary selector
    theDictionarySlider = new JSlider();
    theDictionarySlider.addChangeListener(this);
    theDictionaryStatus = new JLabel("");

    JPanel top_panel = new JPanel(new BorderLayout());
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
    }

    
    public void setData(Collection<StructureType> _entries) {         
    entries = _entries;
    current_page_start = 0;
    
    updateData();
    updateActions();
    updateView();
    }

    public Collection<StructureType> getData() {
    return entries;
    } 

    protected void createActions() {

    theActionManager.add("first",FileUtils.defaultThemeManager.getImageIcon("first"),"First page",-1, "",this);
    theActionManager.add("pprevious",FileUtils.defaultThemeManager.getImageIcon("pprevious"),"Five pages before",-1, "",this);
    theActionManager.add("previous",FileUtils.defaultThemeManager.getImageIcon("previous"),"Previous page",-1, "",this);
    theActionManager.add("next",FileUtils.defaultThemeManager.getImageIcon("next"),"Next page",-1, "",this);
    theActionManager.add("nnext",FileUtils.defaultThemeManager.getImageIcon("nnext"),"Five pages after",-1, "",this);
    theActionManager.add("last",FileUtils.defaultThemeManager.getImageIcon("last"),"Last page",-1, "",this);


    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "",this);
    
    theActionManager.add("copy",FileUtils.defaultThemeManager.getImageIcon("copy"),"Copy",KeyEvent.VK_C, "",this);    
    theActionManager.add("transfer",FileUtils.defaultThemeManager.getImageIcon("transfer"),"Copy structures to canvas",KeyEvent.VK_V, "",this);

    theActionManager.add("goto",FileUtils.defaultThemeManager.getImageIcon("goto"),"Jump to a specific m/z value",-1, "",this);
    theActionManager.add("search",FileUtils.defaultThemeManager.getImageIcon("search"),"Search the dictionary",-1, "",this);
    theActionManager.add("searchagain",FileUtils.defaultThemeManager.getImageIcon("searchagain"),"Search in the current results",-1, "",this);
    theActionManager.add("clearsearch",FileUtils.defaultThemeManager.getImageIcon("clearsearch"),"Clear search",-1, "",this);
    }

    protected void updateActions() {
    boolean has_selection = theTable.getSelectedRows().length>0;
      
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

    theActionManager.get("copy").setEnabled(has_selection);
    theActionManager.get("transfer").setEnabled(has_selection);    
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

    toolbar.add(theActionManager.get("copy"));
    toolbar.add(theActionManager.get("transfer"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("goto"));
    toolbar.add(theActionManager.get("search"));
    toolbar.add(theActionManager.get("searchagain"));
    toolbar.add(theActionManager.get("clearsearch"));

    return toolbar;
    }

    protected JPopupMenu createPopupMenu() {

    JPopupMenu menu = new JPopupMenu();

    menu.add(theActionManager.get("copy"));
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
        return String.class;
    if( columnIndex==4 )
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
        return "Database";
    if( columnIndex==4 )
        return "m/z";
    return null;
    }
    
    public int getColumnCount() {
    return 5;
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
    if( columnIndex==2 ) 
        return shown_entries.get(rowIndex).database;
    if( columnIndex==4 ) 
        return shown_entries.get(rowIndex).mz_ratio;
    return null;
    }


    public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
    }
  

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    //-----------
    // Visualization

    

    public void updateData() {
    ordered_entries = new Vector<DictionaryEntry>();        
    try {
        if( entries!=null ) {
        for( StructureType st : entries ) {
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

    System.out.println(ordered_entries.size());

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

    public void transfer() {
    GlycanCanvas theCanvas = theApplication.getCanvas();
    
    this.copy();    
    theCanvas.resetSelection();
    theCanvas.paste();
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

    else if( action.equals("copy") ) 
        copy();    
    else if( action.equals("transfer") ) 
        transfer();    

    else if( action.equals("search") )
		try {
			theDocument.search(null,null,false);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	else if( action.equals("searchagain") )
		try {
			theDocument.search(null,null,true);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	else if( action.equals("clearsearch") )  
        setData(new Vector<StructureType>());    
    else if( action.equals("goto") ) 
        goToMZ(); 

    updateActions();
    }
    
  
}