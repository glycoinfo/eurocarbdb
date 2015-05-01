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

package org.eurocarbdb.application.glycoworkbench.plugin.reporting;

import org.eurocarbdb.application.glycoworkbench.plugin.*;
import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.image.*;

public class ProfilesComparisonReportTableFrame extends JFrame implements ActionListener, TableModel, ListSelectionListener {

    // singletons
    private GlycoWorkbench theApplication;
    private ActionManager theActionManager;  
    protected ProfilesComparisonReportDocument theDocument;
    protected ProfilesComparisonReportOptions theOptions;
    
    // components
    private JMenuBar theMenuBar;
    private JToolBar theToolBar;
    private GlycanTable theTable;
    private TableSorter theTableSorter;
    
    private Vector<TableModelListener> tm_listeners = new Vector<TableModelListener>();          

    public ProfilesComparisonReportTableFrame(GlycoWorkbench application, ProfilesComparisonReportDocument doc, ProfilesComparisonReportOptions opt) {
    // set singletons
    theApplication = application;
    theActionManager = new ActionManager();        
        theDocument = doc;
        theOptions = opt;

    // initialize the action set
    createActions();

    // set layout
    getContentPane().setLayout(new BorderLayout());

    // set the MenuBar
    theMenuBar = createMenuBar();
    setJMenuBar(theMenuBar);    
    
    // set the toolbars
    UIManager.getDefaults().put("ToolTip.hideAccelerator",Boolean.TRUE);
    theToolBar = createToolBar();
    getContentPane().add(theToolBar,BorderLayout.NORTH);

        // create table
        theTable = new GlycanTable();
    theTable.setGlycanRenderer(theApplication.getWorkspace().getGlycanRenderer());
    theTable.setShowRedend(false);
        theTable.setGlycanScale(0.333);
    theTable.setShowVerticalLines(true);
    theTable.getSelectionModel().addListSelectionListener(this);
    theTable.setPopupMenu(createPopupMenu());
    
    theTableSorter = new TableSorter(this);
    theTableSorter.setTableHeader(theTable.getTableHeader());
    theTable.setModel(theTableSorter);    

        for( int i=0; i<theDocument.getNoColumns(); i++ )
            theTable.getColumn(theDocument.getNames().get(i)).setCellRenderer(new HeatMapCellRenderer(25));
    fireTableChanged();
    
    getContentPane().add(theTable.getScrollPane(),BorderLayout.CENTER);

    // finish setting up
        setSize(900,700);       
    setLocationRelativeTo(theApplication);
    
    updateActions();    
    }

    
    private void createActions() {    

    //file
    for(Map.Entry<String,String> e : SVGUtils.getExportFormats().entrySet() )
        theActionManager.add("export=" + e.getKey(),FileUtils.defaultThemeManager.getImageIcon(""),"Export to " + e.getValue() + "...",-1, "",this);
    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "ctrl P",this);
    theActionManager.add("close",FileUtils.defaultThemeManager.getImageIcon("close"),"Close",KeyEvent.VK_C, "ctrl Q",this);            

        // edit
    theActionManager.add("cut",FileUtils.defaultThemeManager.getImageIcon("cut"),"Cut",KeyEvent.VK_T, "ctrl X",this);
    theActionManager.add("copy",FileUtils.defaultThemeManager.getImageIcon("copy"),"Copy",KeyEvent.VK_C, "ctrl C",this);
    theActionManager.add("delete",FileUtils.defaultThemeManager.getImageIcon("delete"),"Delete",KeyEvent.VK_DELETE, "",this);
    theActionManager.add("screenshot",FileUtils.defaultThemeManager.getImageIcon("screenshot"),"Screenshot",KeyEvent.VK_PRINTSCREEN, "PRINTSCREEN",this);    

    theActionManager.add("filterselection",FileUtils.defaultThemeManager.getImageIcon(""),"Show only selected fragments",-1, "",this);
    theActionManager.add("showallrows",FileUtils.defaultThemeManager.getImageIcon(""),"Show all fragments",-1, "",this);
    }

    private void updateActions() {
    boolean has_selection = theTable.getSelectedRows().length>0;

    theActionManager.get("cut").setEnabled(has_selection);
    theActionManager.get("copy").setEnabled(has_selection);
    theActionManager.get("delete").setEnabled(has_selection);

        theActionManager.get("filterselection").setEnabled(has_selection);
    theActionManager.get("showallrows").setEnabled(!theTableSorter.isAllRowsVisible());
    }

    private JMenu createExportDrawingMenu() {
    
    JMenu export_menu = new JMenu("Export to graphical formats");    
    export_menu.setIcon(FileUtils.defaultThemeManager.getImageIcon("export"));

    for(Map.Entry<String,String> e : SVGUtils.getExportFormats().entrySet() )
        export_menu.add(theActionManager.get("export="+e.getKey()));

    return export_menu;
    }

    private JMenu createFileMenu() {

    JMenu file_menu = new JMenu("File");    

    file_menu.add(createExportDrawingMenu());
    
    file_menu.addSeparator();

    file_menu.add(theActionManager.get("print"));

    file_menu.addSeparator();

    file_menu.add(theActionManager.get("close"));    

    return file_menu;
    
    }

    private JMenu createEditMenu() {

    JMenu edit_menu = new JMenu("Edit");    
    edit_menu.setMnemonic(KeyEvent.VK_E);
    edit_menu.add(theActionManager.get("copy"));
    edit_menu.add(theActionManager.get("delete"));
    edit_menu.add(theActionManager.get("screenshot"));
    edit_menu.addSeparator();

    return edit_menu;
    }

    private JMenuBar createMenuBar() {
     
    JMenuBar menubar = new JMenuBar();
    
    menubar.add(createFileMenu());
        menubar.add(createEditMenu());

    return menubar;
    }

    private JToolBar createToolBar() {

    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    toolbar.add(theActionManager.get("print"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("cut"));
    toolbar.add(theActionManager.get("copy"));
    toolbar.add(theActionManager.get("delete"));
    toolbar.add(theActionManager.get("screenshot"));

    return toolbar;  
    }


    private JPopupMenu createPopupMenu() {
    JPopupMenu menu = new JPopupMenu();

    menu.add(theActionManager.get("cut"));
    menu.add(theActionManager.get("copy"));
    menu.add(theActionManager.get("delete"));

    menu.addSeparator();

    menu.add(theActionManager.get("filterselection"));       
    menu.add(theActionManager.get("showallrows"));

    return menu;
    }
  

    // table model
    public void addTableModelListener(TableModelListener l) {
    if( l!=null )
        tm_listeners.add(l);
    }    

    public void removeTableModelListener(TableModelListener l) {
    tm_listeners.remove(l);
    }    

    public Class<?> getColumnClass(int columnIndex) {
        if( columnIndex==0 )
        return Glycan.class;
    if( columnIndex==1 )
        return Double.class;
        return Double.class;
    }

    public String getColumnName(int columnIndex) {
        if( columnIndex==0 )
        return "Structure";
    if( columnIndex==1 )
        return "m/z";
        return theDocument.getNames().get(columnIndex-2);
    }

    public int getColumnCount() {
    return 2 + theDocument.getNoColumns();
    }

    public int getRowCount() {
        return theDocument.getNoRows();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        ProfilesComparisonReportDocument.Row row = theDocument.getRows().get(rowIndex);
        if( columnIndex==0 ) 
            return row.structure;
        if( columnIndex==1 ) 
            return row.mz_ratio;
        return row.getColumn(columnIndex-2);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
    }
     
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {    
    }

    // actions

    public void cut() {
    copy();
    delete();
    }
    
    public void delete() {    
    }

    public void copy() {    
    int[] sel_ind = theTable.getSelectedRows();
    if( sel_ind.length>0 ) {
            Vector<Glycan> structures = new Vector<Glycan>();
        for( int i=0; i<sel_ind.length; i++ ) {
        int r = theTableSorter.modelIndex(sel_ind[i]);
        structures.add(theDocument.getRows().get(r).structure);
        }
        ClipUtils.setContents(new GlycanSelection(theTable.getSelectedData(),theApplication.getWorkspace().getGlycanRenderer(),structures));
    }     
    }

    public void getScreenshot() {
    ClipUtils.setContents(SVGUtils.getImage(theTable));
    }

    public void onPrint() {
        theTable.print(theApplication.getWorkspace().getPrinterJob(),null);
    }

    public void onExportTo(String format) {

        // imposto la dialog per il salvataggio del file
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.addChoosableFileFilter(new ExtensionFileFilter(format));
        
        // visualizzo la dialog
        int returnVal = fileChooser.showSaveDialog(this);        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        
        // aggiunge l'estension
        String filename = fileChooser.getSelectedFile().getAbsolutePath();
        filename = FileUtils.enforceExtension(filename,format);

        // chiede conferma prima di sovrascrivere il file
        File file = new File(filename);                    
        if (file.exists()) {
        int retValue = JOptionPane.showOptionDialog(this, "File exists. Overwrite file: " + filename + "?",
                                "Salva documento", JOptionPane.YES_NO_CANCEL_OPTION, 
                                JOptionPane.QUESTION_MESSAGE, null, null, null);  
        if( retValue!=JOptionPane.YES_OPTION )
            return;
        }            
        
        // esporta il documento su file        
        try {
        SVGUtils.export(filename,theTable,format);
        }
        catch(Exception e) {
        LogUtils.report(e);
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
    
    private void updateView() {
    fireTableChanged();
    } 

    // events

    public void actionPerformed(ActionEvent e) {

    String action = GlycanAction.getAction(e);
    String param  = GlycanAction.getParam(e);    

    if( action.equals("export") )
        onExportTo(param);
    else if( action.equals("print") )
        onPrint();
    else if( action.equals("close") )
        this.setVisible(false);

    else if( action.equals("cut") )
        cut();
    else if( action.equals("copy") )
        copy();
    else if( action.equals("delete") )
        delete();
    else if( action.equals("screenshot") )
        getScreenshot();

    else if( action.equals("filterselection") )
        filterSelection();
    else if( action.equals("showallrows") )
        showAllRows();
    }

    public void valueChanged(ListSelectionEvent e) {
    updateActions();
    }    

    public void fireTableStructureChanged() {
        
    for(Iterator<TableModelListener> i=tm_listeners.iterator(); i.hasNext(); ) 
        i.next().tableChanged(new TableModelEvent(this,TableModelEvent.HEADER_ROW));
    }

    public void fireTableChanged() {
    for(Iterator<TableModelListener> i=tm_listeners.iterator(); i.hasNext(); ) {
        i.next().tableChanged(new TableModelEvent(this));
    }           
    }

    public void fireRowChanged(int row) {
    for(Iterator<TableModelListener> i=tm_listeners.iterator(); i.hasNext(); ) {
        i.next().tableChanged(new TableModelEvent(this,row));
    }
    }

    public void fireRowsChanged(int from, int to) {
    for(Iterator<TableModelListener> i=tm_listeners.iterator(); i.hasNext(); ) {
        i.next().tableChanged(new TableModelEvent(this,from,to));
    }
    }

    public void fireRowsChanged(int[] ind) {
    for( int l=0; l<ind.length; l++ ) {
        for(Iterator<TableModelListener> i=tm_listeners.iterator(); i.hasNext(); ) {
        i.next().tableChanged(new TableModelEvent(this,ind[l]));
        }
    }
    }

    public void fireRowsChanged(Collection<Integer> indexes) {
    for(Integer ind : indexes ) {
        for(Iterator<TableModelListener> i=tm_listeners.iterator(); i.hasNext(); ) {
        i.next().tableChanged(new TableModelEvent(this,ind));
        }
    }
    }

    public void fireRowDeleted(int ind) {
    for(Iterator<TableModelListener> i=tm_listeners.iterator(); i.hasNext(); ) {
        i.next().tableChanged(new TableModelEvent(this,ind,ind,TableModelEvent.ALL_COLUMNS,TableModelEvent.DELETE));        
    }
    }

    public void fireRowsDeleted(int[] ind) {
    for( int l=0; l<ind.length; l++ ) {
        for(Iterator<TableModelListener> i=tm_listeners.iterator(); i.hasNext(); ) {
        i.next().tableChanged(new TableModelEvent(this,ind[l],ind[l],TableModelEvent.ALL_COLUMNS,TableModelEvent.DELETE));
        }
    }
    }

    public void fireRowsDeleted(Collection<Integer> indexes) {
    for(Integer ind : indexes ) {
        for(Iterator<TableModelListener> i=tm_listeners.iterator(); i.hasNext(); ) {
        i.next().tableChanged(new TableModelEvent(this,ind,ind,TableModelEvent.ALL_COLUMNS,TableModelEvent.DELETE));
        }
    }
    }    
}

class HeatMapCellRenderer extends DefaultTableCellRenderer {  

    private static final long serialVersionUID = 0L;    

    private boolean fill_cell;
    private int spot_radius;
    
    public HeatMapCellRenderer() {
        fill_cell = true;
        spot_radius = 0;
    }

    public HeatMapCellRenderer(int sr) {
        fill_cell = false;
        spot_radius = sr;
    }

    
    public void setValue(Object value) {
        Color color = getColor((Double)value);        
        if( fill_cell )
            setBackground(color);
        else
            setIcon(createSpot(color,getRadius((Double)value,spot_radius)));
    }

    public ImageIcon createSpot(Color color, int radius) {
    // Create an image that supports transparent pixels
        BufferedImage img = GraphicUtils.createCompatibleImage(2*spot_radius,2*spot_radius,false);        

    // create a graphic context
    Graphics2D g2d = img.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);        
    g2d.setBackground(new Color(255,255,255,0));

        // paint the spot
    g2d.setColor(color);
        g2d.fill(new Ellipse2D.Double(spot_radius-radius,spot_radius-radius,2*radius,2*radius));    

        return new ImageIcon(img);                        
    }

    private static Color getColor(double value) {
        if( value<=-1.)
            return Color.red;
        if( value>=1. )
            return Color.green;
        if( value<0. )
            return new Color(-(float)value,0.f,0.f);
        if( value>0. )
            return new Color(0.f,(float)value,0.f);
        return Color.black;
    }

    private static int getRadius(double value, int spot_radius) {
        if( value>=1.)
            return spot_radius;
        if( value<=-1. )
            return spot_radius;
        if( value>0. )
            return (int)(value*(double)spot_radius);
        if( value<0. )
            return (int)(-value*(double)spot_radius);
        return 0;
    }

}
