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
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class ResizingTable extends JTable {
    
    protected ResizingTable this_object;

    protected JScrollPane theTableScroll;
 
    protected Vector<Vector<Integer>> col_widths;
    protected int default_row_height;
    protected int default_row_margin;

    public ResizingTable() {
    super();

    this_object = this;
    
    // set scroller
    theTableScroll = new JScrollPane(this);
    theTableScroll.setColumnHeaderView(getTableHeader());    
    theTableScroll.addComponentListener( new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
            if( getPreferredSize().width <= theTableScroll.getViewport().getExtentSize().width) 
            setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            else
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);            
        }
        });

    // init col widths
    getTableHeader().setReorderingAllowed(false);
    getTableHeader().setResizingAllowed(false);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
    setAutoscrolls(true);

    col_widths = new Vector<Vector<Integer>>();
    default_row_height = getRowHeight();
    default_row_margin = getRowMargin();
    initWidths();
    }        
        
    public JScrollPane getScrollPane() {
    return theTableScroll;
    }

    protected void initWidths() {

    col_widths = new Vector<Vector<Integer>>();
    col_widths.setSize(getModel().getRowCount());
    for( int i=0; i<col_widths.size(); i++ ) {
        Vector<Integer> v = new Vector<Integer>();
        v.setSize(getModel().getColumnCount());
        col_widths.setElementAt(v,i);
    }
    }

    protected void addWidths(int f, int l) {
    for( int i=f; i<=l; i++ ) {
        Vector<Integer> v = new Vector<Integer>();
        v.setSize(getModel().getColumnCount());
        col_widths.insertElementAt(v,i);
    }
    }


    protected void removeWidths(int f, int l) {
    for( int i=0; i<=(l-f); i++ ) 
        col_widths.removeElementAt(f);
    }

    protected int getColWidth(int r, int c) {
    return col_widths.elementAt(r).elementAt(c);
    }

    protected void setColWidth(int r, int c, int v) {
    col_widths.elementAt(r).setElementAt(v,c);
    }

    protected int getMaxWidth(int c) {
    int max_colwidth = 0;
    for( int r=0; r<col_widths.size(); r++ )
        max_colwidth = Math.max(getColWidth(r,c),max_colwidth);
    return max_colwidth;
    }       

    public Dimension getHeaderDimension(int colIndex) {
    TableColumn column = getColumnModel().getColumn(colIndex);
    TableCellRenderer renderer = column.getHeaderRenderer();
    if( renderer == null )
        renderer = getTableHeader().getDefaultRenderer();
    Component comp  = renderer.getTableCellRendererComponent(this,column.getHeaderValue(),false,false,-1,colIndex);
    Dimension pref_dim = comp.getPreferredSize();

    int cell_height = pref_dim.height + 2*default_row_margin;
    int cell_width  = pref_dim.width + 8;
    return new Dimension(cell_width,cell_height);    

    }

    public Dimension getCellDimension(int rowIndex, int colIndex) {
    TableCellRenderer renderer = getCellRenderer(rowIndex, colIndex);
    Component comp = prepareRenderer(renderer, rowIndex, colIndex);
    Dimension pref_dim = comp.getPreferredSize();

    int cell_height = pref_dim.height + 2*default_row_margin;
    int cell_width  = pref_dim.width + 8;
    return new Dimension(cell_width,cell_height);    
    }
   
    protected void updateDimensions(int first, int last) {    
    // update row heights
    for( int i=first; i<=last; i++ ) {     
        int row_height = default_row_height;
        for( int c=0; c<getColumnCount(); c++ ) {
        Dimension d = getCellDimension(i,c);
        row_height = Math.max(row_height, d.height); 
        setColWidth(i,c,d.width);
        }
        setRowHeight(i,row_height);
    }

    // update column widths
    updateColumnWidths();
   }
    
    protected void updateColumnWidths() {
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    for( int c=0; c<getColumnCount(); c++ ) {
        int header_width = getHeaderDimension(c).width;
        int pref_width = getMaxWidth(c);
        getColumnModel().getColumn(c).setPreferredWidth(Math.max(header_width,pref_width));
    }

    if( theTableScroll!=null && getPreferredSize().width <= theTableScroll.getViewport().getExtentSize().width )
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    }           
   
    public void tableChanged(TableModelEvent e) {
    super.tableChanged(e);
    
    if( e.getFirstRow()==TableModelEvent.HEADER_ROW ||
        e.getLastRow()==Integer.MAX_VALUE ) {
        initWidths();
        updateDimensions(0,col_widths.size()-1);
    }
    else if( e.getType()==TableModelEvent.INSERT ) {
        addWidths(e.getFirstRow(),e.getLastRow());
        updateDimensions(e.getFirstRow(),e.getLastRow());
    }
    else if( e.getType()==TableModelEvent.DELETE ) {
        removeWidths(e.getFirstRow(),e.getLastRow());
        updateColumnWidths();
    }
    else if( e.getType()==TableModelEvent.UPDATE ) {
        updateDimensions(e.getFirstRow(),e.getLastRow());
    }
    }

}