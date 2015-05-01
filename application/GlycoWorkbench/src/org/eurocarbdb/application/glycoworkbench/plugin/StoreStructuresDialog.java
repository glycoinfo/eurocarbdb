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
*
* @author  aceroni
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


public class StoreStructuresDialog extends EscapeDialog implements ActionListener{
    
    private ProfilerPlugin theProfiler;

    private StructureDictionary theDictionary;
    private StructureType tomodify;
    private String dict_name;
    private String type;    
    private String source;


    /** Creates new form StoreStructuresDialog */
    public StoreStructuresDialog(java.awt.Frame parent,  ProfilerPlugin profiler, StructureDictionary dict) {
        super(parent, true);

    theProfiler = profiler;
    tomodify = null;
    theDictionary = dict;

    initComponents();
    setTraversal();
    setData();    
    setActions();
    enableItems();

    this.setLocationRelativeTo(parent);
    this.setResizable(false);      
    }

    public StoreStructuresDialog(java.awt.Frame parent, StructureType st, ProfilerPlugin profiler) {
        super(parent, true);

    theProfiler = profiler;
    tomodify = st;
    theDictionary = null;

    initComponents();
    setTraversal();
    setData();    
    setActions();
    enableItems();
    
    this.setLocationRelativeTo(parent);
    this.setResizable(false);      
    }
   
    private void setTraversal() {
    CustomFocusTraversalPolicy tp = new CustomFocusTraversalPolicy();
    
    tp.addComponent(field_dictionaries);
    //tp.addComponent(button_new);

    tp.addComponent(field_type);
    tp.addComponent(field_source);

    tp.addComponent(button_ok);
    tp.addComponent(button_cancel);

    this.setFocusTraversalPolicy(tp);

    getRootPane().setDefaultButton(button_ok);  
    }

    private void setDictionaries() {
    if( theProfiler.getUserDictionaries().size()>0 ) 
        field_dictionaries.setModel(new javax.swing.DefaultComboBoxModel(theProfiler.getUserDictionaryNames().toArray()));    
    else
        field_dictionaries.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"Create one..."}));    

    if( tomodify == null )
        field_dictionaries.setSelectedItem(theProfiler.getOptions().LAST_DICT_NAME);
    else if( theDictionary==null )
        field_dictionaries.setSelectedItem(tomodify.getDatabase());
    else
        field_dictionaries.setSelectedItem(theDictionary.getDictionaryName());
    }
    
    private void setData() {    
    
    setDictionaries();

    if( tomodify==null ) {
        field_type.setText(theProfiler.getOptions().LAST_TYPE);
        field_source.setText(theProfiler.getOptions().LAST_SOURCE);
    }
    else {
        field_type.setText(tomodify.getType());
        field_source.setText(tomodify.getSource());
    }
    }
    
    private void setActions() {
    //button_new.addActionListener(this);
    button_ok.addActionListener(this);
    button_cancel.addActionListener(this);
    }

    private void enableItems() {
    //button_new.setEnabled(tomodify==null);
    field_dictionaries.setEnabled(tomodify==null && theDictionary==null && theProfiler.getUserDictionaries().size()>0 );    
    }

    public void retrieveData() {

    dict_name = (String)field_dictionaries.getSelectedItem();
    if( dict_name!=null )
        theProfiler.getOptions().LAST_DICT_NAME = dict_name;
    
    type = field_type.getText();
    if( type==null || type.length()==0 )
        type = "unknown";
    theProfiler.getOptions().LAST_TYPE = type;

    source = field_source.getText();
    if( source==null || source.length()==0 )
        source = "unknown";    
    theProfiler.getOptions().LAST_SOURCE = source;
    }
 
    public String getDictionaryName() {
    return dict_name;
    }

    public String getType() {
    return type;
    }

    public String getSource() {
    return source;
    }
    

    public void actionPerformed(ActionEvent e) {
    
    String action = e.getActionCommand();       
    /*if( action.equals("New") ) {
        new UserDatabasesManagerDialog(theProfiler.getApplication(),theProfiler).setVisible(true);
        setDictionaries();    
        enableItems();
        }*/
    if( action.equals("OK") ) {        
        return_status = "OK";
        retrieveData();
        closeDialog();
    }
    else if( action.equals("Cancel") ) {
        return_status = "Cancel";
        closeDialog();
    }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        field_dictionaries = new javax.swing.JComboBox();
        //button_new = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        field_type = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        field_source = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        button_ok = new javax.swing.JButton();
        button_cancel = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });

        jLabel1.setText("Database");

        field_dictionaries.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        //button_new.setText("New");

        jLabel2.setText("Type");

        field_type.setText("field_type");

        jLabel3.setText("Source");

        field_source.setText("field_source");

        button_ok.setText("OK");

        button_cancel.setText("Cancel");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel1)
                                    .add(jLabel2)
                                    .add(jLabel3))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(field_source)
                     .add(layout.createSequentialGroup()
                     .add(field_dictionaries, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                      //.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                     //.add(button_new)
                      )
                                    .add(field_type)))))
                    .add(layout.createSequentialGroup()
                        .add(65, 65, 65)
                        .add(button_ok)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(button_cancel)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {button_cancel, button_ok}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(field_dictionaries, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
             //.add(button_new)
             )
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(field_type, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(field_source, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(button_ok)
                    .add(button_cancel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    /** Closes the dialog */
    private void closeDialog() {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
   
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_cancel;
    //private javax.swing.JButton button_new;
    private javax.swing.JButton button_ok;
    private javax.swing.JComboBox field_dictionaries;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField field_type;
    private javax.swing.JTextField field_source;
    // End of variables declaration//GEN-END:variables
    
}
