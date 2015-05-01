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
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.text.*;

public class MultiLineHeaderRenderer extends JList implements TableCellRenderer {
    protected GlycanRenderer theGlycanRenderer;

    public MultiLineHeaderRenderer(GlycanRenderer gr) {
    super();
    
    theGlycanRenderer = gr;

    setOpaque(true);
    setForeground(UIManager.getColor("TableHeader.foreground"));
    setBackground(UIManager.getColor("TableHeader.background"));

    Border border = UIManager.getBorder("TableHeader.cellBorder");
    Border margin = new EmptyBorder(2,2,2,2);
    setBorder(new CompoundBorder(border,margin));

    setCellRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
            setHorizontalAlignment(JLabel.LEFT);
            setVerticalAlignment(JLabel.CENTER);

            if( value instanceof Glycan ) {
            setIcon(new ImageIcon(theGlycanRenderer.getImage((Glycan)value,false,false,false,0.4)));
            setText(null);
            }
            else {
            setIcon(null);
            setText(value.toString());
            }        
            return this;
        }
        });
        
    //ListCellRenderer renderer = getCellRenderer();
    //((JLabel)renderer).setHorizontalAlignment(JLabel.LEFT);
    //setCellRenderer(renderer);
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    setFont(table.getFont());
    
    if( value == null ) 
        setListData(new Vector<Object>());    
    else if( value instanceof Vector ) 
        setListData((Vector)value);    
    else if( value instanceof Glycan ) {       
        Vector<Glycan> v = new Vector<Glycan>();
        v.add((Glycan)value);
        setListData(v);
    }
    else         
        setListData(TextUtils.splitLines(value.toString()));    

    return this;
    }
}


