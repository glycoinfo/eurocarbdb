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


abstract public class TablePanel<DT> extends DocumentPanel<DT> implements TableModel, ListSelectionListener {
    
    protected GlycanTable theTable;
  
    protected Vector<TableModelListener> tm_listeners;          

    public TablePanel() {
    super();
   }

    protected  void initSingletons() {
    super.initSingletons();
    tm_listeners = new Vector<TableModelListener>(); 
    }   

    protected void initComponents() {
    setLayout(new BorderLayout());    

    theTable = new GlycanTable();
    theTable.setGlycanRenderer(theWorkspace.getGlycanRenderer());
    theTable.setShowVerticalLines(true);
    theTable.getSelectionModel().addListSelectionListener(this);
    theTable.setPopupMenu(createPopupMenu());
    theTable.setModel(this);

    add(theTable.getScrollPane(),BorderLayout.CENTER);
    }
   

    public void setWorkspace(GlycanWorkspace workspace) {
    if( theTable!=null && workspace!=null) 
        theTable.setGlycanRenderer(workspace.getGlycanRenderer());
    super.setWorkspace(workspace);
    }            

    protected JPopupMenu createPopupMenu() {
    return null;
    }

    
    // table model
    public void addTableModelListener(TableModelListener l) {
    if( l!=null )
        tm_listeners.add(l);
    }    

    public void removeTableModelListener(TableModelListener l) {
    tm_listeners.remove(l);
    }    

    public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
    }
     
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    
    }

    // events

    protected void updateView() {
    fireTableChanged();
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
    