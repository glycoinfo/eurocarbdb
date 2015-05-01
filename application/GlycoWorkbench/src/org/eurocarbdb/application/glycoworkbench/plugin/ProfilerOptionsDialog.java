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
/*
* ProfilerOptionsDialog.java
*
* Created on 22 May 2007, 14:11
*/

/**
*
* @author  aceroni
*/

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

public class ProfilerOptionsDialog extends EscapeDialog implements java.awt.event.ActionListener {
    
    private ProfilerPlugin thePlugin = null;
    private ProfilerOptions theProfilerOptions;
  
    /** Creates new form ProfilerOptionsDialog */
    public ProfilerOptionsDialog(java.awt.Frame parent, ProfilerPlugin plugin, ProfilerOptions opt) {
        super(parent, true);

    thePlugin = plugin;
    theProfilerOptions =  opt;

        initComponents();
    setTraversal();
    setData(theProfilerOptions);    
    setActions();
    enableItems();

    // set location
    setLocationRelativeTo(parent);
    }

    private void setTraversal() {
    CustomFocusTraversalPolicy tp = new CustomFocusTraversalPolicy();
    
    tp.addComponent(field_dictionaries);
    tp.addComponent(field_derivatization);
    tp.addComponent(field_reducingend);
    tp.addComponent(field_other_redend_name);
    tp.addComponent(field_other_redend_mass);
    tp.addComponent(button_ok);
    tp.addComponent(button_cancel);

    this.setFocusTraversalPolicy(tp);

    getRootPane().setDefaultButton(button_ok); 
    }

    private void setData(ProfilerOptions opt) {    
    java.util.Vector<String> dict_names = new Union<String>(thePlugin.getDictionaryNames());
    field_dictionaries.setListData(dict_names);
    for( int i=0; i<opt.DICTIONARIES.length; i++ ) {
        int ind = dict_names.indexOf(opt.DICTIONARIES[i]);
        field_dictionaries.addSelectionInterval(ind,ind);
    }        
    
    field_derivatization.setModel(new javax.swing.DefaultComboBoxModel(new String[] {MassOptions.NO_DERIVATIZATION, MassOptions.PERMETHYLATED, MassOptions.PERDMETHYLATED, MassOptions.PERACETYLATED, MassOptions.PERDACETYLATED}));
    field_derivatization.setSelectedItem(opt.DERIVATIZATION);
    
    field_reducingend.setModel(new javax.swing.DefaultComboBoxModel(new Union<String>().and("---").and(ResidueDictionary.getReducingEndsString()).and("Other...").toArray(new String[0])));
    field_reducingend.setSelectedItem(opt.REDUCING_END);

    if( opt.REDUCING_END.equals("XXX") ) {
        field_reducingend.setSelectedItem("Other...");       
        field_other_redend_name.setText(opt.OTHER_REDEND_NAME);
        field_other_redend_mass.setText("" + opt.OTHER_REDEND_MASS);
    }
    else {
        field_reducingend.setSelectedItem(opt.REDUCING_END);
        field_other_redend_name.setText("");
        field_other_redend_mass.setText("0");
    }
    }

    private void setActions() {
    field_reducingend.addActionListener(this);
    button_ok.addActionListener(this);
    button_cancel.addActionListener(this);
    }

    private void enableItems() {
    field_other_redend_name.setEnabled(field_reducingend.getSelectedItem().equals("Other..."));
    field_other_redend_mass.setEnabled(field_reducingend.getSelectedItem().equals("Other..."));
    }

    private String[] toStringArray(Object[] a) {
    if( a==null )
        return null;

    String[] ret = new String[a.length];
    for( int i=0; i<a.length; i++ )
        ret[i] =  a[i].toString();

    return ret;
    }

    private void retrieveData(ProfilerOptions opt) {    

    opt.DICTIONARIES = toStringArray(field_dictionaries.getSelectedValues());
    opt.DERIVATIZATION = (String)field_derivatization.getSelectedItem();

    if( field_reducingend.getSelectedItem().equals("Other...") ) {
        opt.REDUCING_END = "XXX";
        opt.OTHER_REDEND_NAME = TextUtils.trim(field_other_redend_name.getText());
        opt.OTHER_REDEND_MASS = Double.valueOf(field_other_redend_mass.getText());
        
        if( opt.OTHER_REDEND_NAME==null || opt.OTHER_REDEND_NAME.length()==0 )
        opt.OTHER_REDEND_NAME = "XXX";
    }
    else 
        opt.REDUCING_END = (String)field_reducingend.getSelectedItem();
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
    String action = e.getActionCommand();
    
    if (action == "OK") {
        retrieveData(theProfilerOptions);
        return_status = action;
        closeDialog();
    }
    else if (action == "Cancel"){
        return_status = action;
        closeDialog();
    }

    enableItems();
    }  

   
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
      jLabel9 = new javax.swing.JLabel();
        field_derivatization = new javax.swing.JComboBox();
        field_reducingend = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        button_ok = new javax.swing.JButton();
        button_cancel = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        field_dictionaries = new javax.swing.JList();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        field_other_redend_name = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        field_other_redend_mass = new javax.swing.JTextField();

        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });

        jLabel9.setText("Derivatization");

        field_derivatization.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        field_reducingend.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel10.setText("Reducing end");

        button_ok.setText("OK");

        button_cancel.setText("Cancel");

        jLabel1.setText("Database");

        field_dictionaries.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        field_dictionaries.setVisibleRowCount(5);
        jScrollPane1.setViewportView(field_dictionaries);

        jLabel2.setText("name");

        field_other_redend_name.setText("jTextField1");

        jLabel3.setText("mass");

        field_other_redend_mass.setText("jTextField2");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(42, 42, 42)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jLabel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jLabel3)
                                .add(jLabel2)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(field_derivatization, 0, 162, Short.MAX_VALUE)
                            .add(field_reducingend, 0, 162, Short.MAX_VALUE)
                            .add(field_other_redend_name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                            .add(field_other_redend_mass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(57, 57, 57)
                        .add(button_ok)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(button_cancel)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {button_cancel, button_ok}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_derivatization, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_reducingend, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(field_other_redend_name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(field_other_redend_mass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(button_ok)
                    .add(button_cancel))
                .addContainerGap())
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
    private javax.swing.JButton button_ok;
    private javax.swing.JComboBox field_derivatization;
    private javax.swing.JList field_dictionaries;
    private javax.swing.JTextField field_other_redend_mass;
    private javax.swing.JTextField field_other_redend_name;
    private javax.swing.JComboBox field_reducingend;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
    
}
