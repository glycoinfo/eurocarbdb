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
import java.awt.print.*;
import java.text.DecimalFormat;
import java.text.MessageFormat;

public class GlycanTable extends ResizingTable implements SVGUtils.Renderable, Printable, MouseListener {
    
    protected JPopupMenu thePopupMenu;

    protected GlycanRenderer theGlycanRenderer;
    protected DecimalFormat  df_fourdigits;
    protected DecimalFormat  df_twodigits;

    protected double glycan_scale = 0.5;
    protected double glycans_vector_scale = 0.4;
    protected boolean use_styled_text = true;
    protected boolean show_redend = true;

    protected boolean is_printing;
    protected HashSet<Glycan> magnified;

    protected Vector<ActionListener> action_listeners = new Vector<ActionListener>();      

    // printing
    
    Glycan    header_structure = null;
    Printable table_printable = null;

    public GlycanTable() {
    super();
    
    // init members
    thePopupMenu = null;
        
    theGlycanRenderer = new GlycanRenderer();
    df_fourdigits  = new DecimalFormat("0.0000");
    df_twodigits  = new DecimalFormat("0.00");

    is_printing = false;
    magnified = new HashSet<Glycan>();

    // set aspect
    setRowSelectionAllowed(true);
    setColumnSelectionAllowed(false);
    setShowVerticalLines(false);

    // set renderers
    getTableHeader().setDefaultRenderer(new MultiLineHeaderRenderer(theGlycanRenderer));    

    setDefaultRenderer(Vector.class, new DefaultTableCellRenderer() {  
        private static final long serialVersionUID = 0L;    
        public void setValue(Object value) {
            setIcon(new ImageIcon(theGlycanRenderer.getImage((Vector<Glycan>)value,false,false,show_redend,glycans_vector_scale)));            
        }
        });
    
    setDefaultRenderer(Glycan.class, new DefaultTableCellRenderer() {
        private static final long serialVersionUID = 0L;    
        public void setValue(Object value) {
            if( (magnified.contains((Glycan)value) && !is_printing) )
            setIcon(new ImageIcon(theGlycanRenderer.getImage((Glycan)value,false,false,show_redend,1.)));
            else
            setIcon(new ImageIcon(theGlycanRenderer.getImage((Glycan)value,false,false,show_redend,glycan_scale)));                
        }
        });

    if( use_styled_text )
        setDefaultRenderer(String.class, new StyledTextCellRenderer());       
    else
        setDefaultRenderer(String.class, new DefaultTableCellRenderer());       

    setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
        private static final long serialVersionUID = 0L;    
        public void setValue(Object value) {
            if( value!=null )
            setText(((Integer)value).toString());
            else
            setText("");
        }
        });

    setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
        private static final long serialVersionUID = 0L;    
        public void setValue(Object value) {
            if( value!=null ) 
            setText(df_fourdigits.format(((Double)value).doubleValue()));            
            else
            setText("");
        }    
        });               

    // add listeners
    addMouseListener( this ); 
    }   

    public void setShowRedend(boolean f) {
    show_redend = f;
    }

    public boolean getShowRedend() {
    return show_redend;
    }

    public void setGlycanScale(double val) {
    glycan_scale = val;
    }

    public double getGlycanScale() {
    return glycan_scale;
    }

    public void setGlycansVectorScale(double val) {
    glycans_vector_scale = val;
    }

    public double getGlycansVectorScale() {
    return glycans_vector_scale;
    }

    
    public void setUseStyledText(boolean f) {
    use_styled_text = f;                 
    if( use_styled_text )
        setDefaultRenderer(String.class, new StyledTextCellRenderer());       
    else
        setDefaultRenderer(String.class, new DefaultTableCellRenderer());       
    }

    public boolean getUseStyledText() {
    return use_styled_text;
    }

    public void setPopupMenu(JPopupMenu menu) {
    thePopupMenu = menu;
    }

    public JPopupMenu getPopupMenu() {
    return thePopupMenu;
    }

    public void setGlycanRenderer(GlycanRenderer r) {
    theGlycanRenderer = r;
    getTableHeader().setDefaultRenderer(new MultiLineHeaderRenderer(theGlycanRenderer));
    }

    public GlycanRenderer getGlycanRenderer() {
    return theGlycanRenderer;
    }
    

    public void beforeRendering() {
    is_printing = true;
    }

    public void afterRendering() {
    is_printing = false;
    }

    public Dimension getRenderableSize() {
    return new Dimension(this.getWidth(),this.getHeight()+this.getTableHeader().getHeight());
    }
    
    public void paintRenderable(Graphics2D g2d) {
    JTableHeader tableHeader = getTableHeader();
        tableHeader.paint(g2d);
        
        g2d.translate(0, tableHeader.getHeight());
    super.paint(g2d);
    }

    public void print(PrinterJob pj) {
    print(pj,null);
    }


    public void print(PrinterJob pj, Glycan _header_structure ) {
    if( pj==null )
        return;

    try {        
        header_structure = _header_structure;
        table_printable = getPrintable(JTable.PrintMode.FIT_WIDTH,null,new MessageFormat("{0}"));    

        pj.setPrintable(this);
        if( pj.printDialog() ) {
        is_printing = true;
        pj.print();
        is_printing = false;
        }
    }
    catch(Exception e) {
        LogUtils.report(e);
    }    
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
    if( pageIndex==0 ) {
        // print structure

        Image struct_img = theGlycanRenderer.getImage(header_structure,false,true,true,1.);

        Graphics2D g2d = (Graphics2D)graphics;

        java.awt.geom.AffineTransform old_transform = g2d.getTransform();
        g2d.setBackground(Color.white);
        
        //Dimension td = theStructure.getPreferredSize();
        Dimension td = new Dimension(struct_img.getWidth(null),struct_img.getHeight(null));

        double s_width  = td.width;
        double s_height = td.height;
        double sx = pageFormat.getImageableWidth()/td.width;
        double sy = pageFormat.getImageableHeight()/td.height;
        double s = Math.min(sx,sy);
        if( s<1. ) {
        s_width *= s;
        s_height *= s;
        struct_img = theGlycanRenderer.getImage(header_structure,false,true,true,s);
        }

        int left_margin = (int)((pageFormat.getImageableWidth() - s_width)/2);
        g2d.translate(pageFormat.getImageableX()+left_margin, pageFormat.getImageableY());

        g2d.drawImage(struct_img,null,null);
        
        g2d.setTransform(old_transform);        
        
        // print table after structure
        return table_printable.print(graphics,new OffsetPageFormat(pageFormat,(int)(s_height+20)),pageIndex);
    }
    else
        return table_printable.print(graphics,pageFormat,pageIndex);
    }

    private void showPopup(MouseEvent e) {
    if( thePopupMenu!=null ) {
        int row = rowAtPoint(e.getPoint());
        int column = columnAtPoint(e.getPoint());
        
        if( !isRowSelected(row) )
        setRowSelectionInterval(row,row);
        else
        addRowSelectionInterval(row,row);
        addColumnSelectionInterval(column,column);
    
        thePopupMenu.show(this, e.getX(), e.getY());   
    
    }
    }
    
    public void mousePressed(MouseEvent e) {
    if( MouseUtils.isPopupTrigger(e) )
        showPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
    if( MouseUtils.isPopupTrigger(e) )
        showPopup(e);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {      
    
    if( MouseUtils.isActionTrigger(e) ) {
        
        // zoom structure if any
        int row = rowAtPoint(e.getPoint());
        int col = columnAtPoint(e.getPoint());
        Object value = getValueAt(row,col);

        if( value instanceof Glycan ) {
        // zoom toggle
        if( magnified.contains((Glycan)value) )
            magnified.remove((Glycan)value);
        else
            magnified.add((Glycan)value);

        // update view
        updateDimensions(row,row);

        // reselect row
        addRowSelectionInterval(row,row);
        }
        else {
        for( ActionListener al : action_listeners )
            al.actionPerformed(new ActionEvent(this,0,"tableaction"));
        }
    }
    }


    public void tableChanged(TableModelEvent e) {
    super.tableChanged(e);

    if( (e.getFirstRow()==TableModelEvent.HEADER_ROW || e.getLastRow()==Integer.MAX_VALUE) && magnified!=null)         
        magnified.clear();        

    if( e.getFirstRow()==TableModelEvent.HEADER_ROW ) {
        // the renderer has to be set for each column otherwise
        // the size is not correctly computed
        Enumeration<TableColumn> columns = getColumnModel().getColumns();
        while( columns.hasMoreElements() )
        columns.nextElement().setHeaderRenderer(getTableHeader().getDefaultRenderer());
    }
    }

    public void addActionListener(ActionListener al) {
    if( al!=null )
        action_listeners.add(al);
    }

    public void removeActionListener(ActionListener al) {
    if( al!=null && action_listeners.contains(al) )
        action_listeners.remove(al);
    }



    // -------

    public Data getSelectedData() {
    Data rows = new Data();
    
    // set data header
    for( int c = 0; c<getColumnCount(); c++ ) 
        rows.add(getColumnName(c));
    rows.newRow();
    
    // set data rows
    int[] sel_ind = getSelectedRows();
    for( int i=0; i<sel_ind.length; i++ ) {
        int r = sel_ind[i];     
        for( int c = 0; c<getColumnCount(); c++ ) {
        Object o =getValueAt(r,c);
        if( o instanceof Glycan ) 
            rows.add(theGlycanRenderer.makeCompositionText((Glycan)o,false));            
        else if( o instanceof Vector ) {
            StringBuilder sb = new StringBuilder();
            for( Glycan g : (Vector<Glycan>)o ) {
            if( sb.length()>0 )
                sb.append(",");
            sb.append(theGlycanRenderer.makeCompositionText(g,false));            
            }
            rows.add(sb.toString());
        }
        else 
            rows.add(o);            
        }
        rows.newRow();    
    }
     
    return rows;
    }
    
}


class OffsetPageFormat extends PageFormat {

    protected int offset = 0;
    protected PageFormat base_object = null;

    public OffsetPageFormat(PageFormat bo, int off) {
    base_object = bo;
    offset = off;
    }

    public Object clone() {    
    return new OffsetPageFormat((PageFormat)base_object.clone(),offset);
    }

    public double getHeight() {
    return base_object.getHeight();
    }

    public double getImageableHeight() {
    return base_object.getImageableHeight()-offset;
    }

    public double getImageableWidth() {
    return base_object.getImageableWidth();
    }

    public double getImageableX() {
    return base_object.getImageableX();
    }

    public double getImageableY() {
    return base_object.getImageableY() + offset;
    }


    public double[] getMatrix() {
    return base_object.getMatrix();
    }

    public int getOrientation() {
    return base_object.getOrientation();
    }

    public Paper getPaper() {
    return base_object.getPaper();
    }

    public double getWidth() {
    return base_object.getWidth();
    }
    
    public void setOrientation(int orientation) {
    base_object.setOrientation(orientation);
    }

    public void setPaper(Paper paper) {
    base_object.setPaper(paper);
    }

}
