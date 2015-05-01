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

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class StructureSelectorDialog extends EscapeDialog implements ListSelectionListener, ActionListener {

    // components
    private GlycanRenderer theGlycanRenderer;
    private JList theSelector;
    private JLabel theMessage;
    private JButton ok_button;
    private JButton cancel_button;

    //
    public StructureSelectorDialog(JFrame parent, String title, String message, Vector<Glycan> structures, boolean multiple_sel, GlycanRenderer gr) {
    super(parent,title,true);
    
    theGlycanRenderer = gr;

    // add components
    this.getContentPane().setLayout(new BorderLayout());
    
    theMessage = new JLabel(message);
    theMessage.setBorder(new EmptyBorder(10,10,10,10));
    this.getContentPane().add(theMessage,BorderLayout.NORTH);

    theSelector = createStructureSelector(structures,multiple_sel);
    theSelector.setBorder(new BevelBorder(BevelBorder.LOWERED));
    this.getContentPane().add(new JScrollPane(theSelector),BorderLayout.CENTER);

    JPanel buttons_panel = new JPanel(new FlowLayout());

    ok_button = new JButton(new GlycanAction("OK",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"OK",-1,"",this));
    cancel_button = new JButton(new GlycanAction("Cancel",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Cancel",-1,"",this));
    buttons_panel.add(ok_button);
    buttons_panel.add(cancel_button);
    this.getContentPane().add(buttons_panel,BorderLayout.SOUTH);

    getRootPane().setDefaultButton(ok_button);
  
    // set dialog    
    theSelector.addListSelectionListener(this);
    updateActions();

    //pack();
    setSize(new Dimension(400,300));
    setResizable(false);    
    setLocationRelativeTo(parent);
    }

    private JList createStructureSelector(Vector<Glycan> structures, boolean multiple_sel) {
    
    JList ret = new JList();

    ret.setCellRenderer( new javax.swing.DefaultListCellRenderer()  {
        private static final long serialVersionUID = 0L;    
        public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);

            Glycan s = (Glycan)value;
            if( s==null ) {
            setIcon(null);
            setText("null");
            }
            else if( s.isEmpty() ) {
            setIcon(null);
            setText("profile " + (index+1));
            }
            else {
            setIcon(new javax.swing.ImageIcon(theGlycanRenderer.getImage((Glycan)value,false,false,true,0.333)));                
            setText("");
            }
            return this;
        }
        });
    
    if( multiple_sel ) 
        ret.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    else
        ret.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

    ret.setListData(structures);
    if( structures.size()>0 )
        ret.setSelectedIndex(0);

    return ret;
    }

    public boolean isCanceled() {
    return return_status.equals("Cancel");
    }

    public Glycan[] getSelectedStructures() {    
    return (Glycan[])theSelector.getSelectedValues();
    }    

    public int[] getSelectedIndices() {    
    return theSelector.getSelectedIndices();
    }    

    public Glycan getSelectedStructure() {    
    return (Glycan)theSelector.getSelectedValue();
    }    

    public int getSelectedIndex() {    
    return theSelector.getSelectedIndex();
    }    

    

    private void updateActions() {       
    ok_button.getAction().setEnabled(!theSelector.isSelectionEmpty());
    }    

    public void valueChanged(ListSelectionEvent e) {
    updateActions();
    }

    public void actionPerformed(ActionEvent e) {

    String action = GlycanAction.getAction(e);

    if( action.equals("OK") ) {
        return_status = action;
        setVisible(false);
    }
    else if( action.equals("Cancel") ) {
        return_status = action;
        setVisible(false);
    }

    }
}