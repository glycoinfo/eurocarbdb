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

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.table.*;

class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
    private static final long serialVersionUID = 0L;    

    protected Color unselectedForeground; 
    protected Color unselectedBackground; 
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1); 

    public MultiLineCellRenderer() {
    super();
    setOpaque(true);
        setBorder(getNoFocusBorder());
    setAlignmentX(1.f);
    setAlignmentY(1.f);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    
    // set colors
    
    if (isSelected) {
       super.setForeground(table.getSelectionForeground());
       super.setBackground(table.getSelectionBackground());
    }
    else {
        super.setForeground((unselectedForeground != null) ? unselectedForeground 
                                                           : table.getForeground());
        super.setBackground((unselectedBackground != null) ? unselectedBackground 
                                                           : table.getBackground());
    }
    
    setFont(table.getFont());

    // set borders

    if (hasFocus) {
            Border border = null;
            if (isSelected) {
                border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = UIManager.getBorder("Table.focusCellHighlightBorder");
            }
            setBorder(border);

        if (!isSelected && table.isCellEditable(row, column)) {
                Color col;
                col = UIManager.getColor("Table.focusCellForeground");
                if (col != null) {
                    super.setForeground(col);
                }
                col = UIManager.getColor("Table.focusCellBackground");
                if (col != null) {
                    super.setBackground(col);
                }
        }
    } else {
            setBorder(getNoFocusBorder());
    }
                       
    // set text
    setText(value.toString());

    return this;
    }
    

    private static Border getNoFocusBorder() {
    return noFocusBorder;
    }

    public void setForeground(Color c) {
        super.setForeground(c); 
        unselectedForeground = c; 
    }

    public void setBackground(Color c) {
        super.setBackground(c); 
        unselectedBackground = c; 
    }

    public void updateUI() {
        super.updateUI(); 
    setForeground(null);
    setBackground(null);
    }
    
    
    
    // The following methods are overridden as a performance measure to 
    // to prune code-paths are often called in the case of renders
    // but which we know are unnecessary.  Great care should be taken
    // when writing your own renderer to weigh the benefits and 
    // drawbacks of overriding methods like these.

    public boolean isOpaque() { 
    Color back = getBackground();
    Component p = getParent(); 
    if (p != null) { 
        p = p.getParent(); 
    }
    // p should now be the JTable. 
    boolean colorMatch = (back != null) && (p != null) && 
        back.equals(p.getBackground()) && 
            p.isOpaque();
    return !colorMatch && super.isOpaque(); 
    }

    public void invalidate() {}

    public void validate() {}

    public void revalidate() {}

    public void repaint(long tm, int x, int y, int width, int height) {}

    public void repaint(Rectangle r) { }

    public void repaint() { }
     
}