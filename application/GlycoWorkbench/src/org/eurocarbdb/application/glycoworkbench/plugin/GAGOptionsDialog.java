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

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

public class GAGOptionsDialog extends EscapeDialog implements java.awt.event.ActionListener, javax.swing.event.ListSelectionListener, javax.swing.event.ChangeListener, java.awt.event.ItemListener  {
    
    private boolean       specify_subs;
    private GAGDictionary theGAGDictionary;
    private GAGOptions    theGAGOptions;
    
    private boolean       allow_events = true;

    /** Creates new form GAGOptionsDialog */
    public GAGOptionsDialog(java.awt.Frame parent, GAGDictionary dict, GAGOptions opt) {
        super(parent, true);
    
    theGAGDictionary = dict;
    theGAGOptions =  opt;

    initComponents();
    setTraversal();
    setData(theGAGOptions);    
    setActions();
    enableItems();

    // set location
    setLocationRelativeTo(parent);
    pack();
    }

    private Object[] generateValues(int min, int max, boolean include_und) {

    if( include_und ) {
        Object[] values = new Object[1+(max-min+1)];    

        values[0] = "---";
        for( int i=min; i<=max; i++ )
        values[i-min+1] = i;
        return values;
    }

    Object[] values = new Object[max-min+1];    
    for( int i=min; i<=max; i++ )
        values[i-min] = i;
    return values;
    }    

    private void setValue(javax.swing.JSpinner field, int value) {
    if( value==999 )
        field.setValue("---");
    else
        field.setValue(value);
    }


    private int getValue(javax.swing.JSpinner field) {
    if( field.getValue().equals("---") )
        return 999;
    return (Integer)field.getValue();        
    }

    private int limitValue(int v, int l) {
    if( v==999 )
        return 999;
    return Math.min(v,l);
    }


    private javax.swing.ListModel createModel(java.util.Collection<String> values) {
    javax.swing.DefaultListModel ret = new javax.swing.DefaultListModel();

    // add elements
    for(String v : values) 
        ret.addElement(v);
    
    return ret;
    }

    private javax.swing.ListModel createModel(String[] values) {
    javax.swing.DefaultListModel ret = new javax.swing.DefaultListModel();

    // add elements
    for(int i=0; i<values.length; i++) 
        ret.addElement(values[i]);
    
    return ret;
    }

    private void selectValues(javax.swing.JList field, String[] values) {
    javax.swing.DefaultListModel dlm = (javax.swing.DefaultListModel)field.getModel();
    for( int i=0; i<values.length; i++ ) {
        int ind = dlm.indexOf(values[i]);
        field.addSelectionInterval(ind,ind);
    }
    }

    private int getMaxNoAcetyls(GAGOptions gag_opt) {
    int ret = 0;
    for( int i=0; i<gag_opt.GAG_FAMILIES.length; i++ ) {
        GAGType cur_type = theGAGDictionary.getType(gag_opt.GAG_FAMILIES[i],gag_opt);
        ret = Math.max(ret,cur_type.getMaxNoAcetyls(gag_opt.MAX_NO_UNITS));
    }
    return ret;
    }

    private int getMaxNoSulfates(GAGOptions gag_opt) {
    int ret = 0;
    for( int i=0; i<gag_opt.GAG_FAMILIES.length; i++ ) {
        GAGType cur_type = theGAGDictionary.getType(gag_opt.GAG_FAMILIES[i],gag_opt);
        ret = Math.max(ret,cur_type.getMaxNoSulfates(gag_opt.MAX_NO_UNITS,0));
    }
    return ret;
    }

    private void setData(GAGOptions gag_opt) {

    field_gag_families.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);                   
    field_gag_families.setModel(createModel(theGAGDictionary.getFamilies()));
    selectValues(field_gag_families,gag_opt.GAG_FAMILIES);
    
    field_min_no_units.setModel(new javax.swing.SpinnerListModel(generateValues(1,10,false)));
    field_max_no_units.setModel(new javax.swing.SpinnerListModel(generateValues(1,10,false)));
    setValue(field_min_no_units,gag_opt.MIN_NO_UNITS);
    setValue(field_max_no_units,gag_opt.MAX_NO_UNITS);

    int max_no_acetyls = getMaxNoAcetyls(gag_opt);
    field_min_no_acetyls.setModel(new javax.swing.SpinnerListModel(generateValues(0,max_no_acetyls,false)));
    field_max_no_acetyls.setModel(new javax.swing.SpinnerListModel(generateValues(0,max_no_acetyls,true)));
    setValue(field_min_no_acetyls,limitValue(gag_opt.MIN_NO_ACETYLS,max_no_acetyls));
    setValue(field_max_no_acetyls,limitValue(gag_opt.MAX_NO_ACETYLS,max_no_acetyls));

    int max_no_sulfates = getMaxNoSulfates(gag_opt);
    field_min_no_sulfates.setModel(new javax.swing.SpinnerListModel(generateValues(0,max_no_sulfates,false)));
    field_max_no_sulfates.setModel(new javax.swing.SpinnerListModel(generateValues(0,max_no_sulfates,true)));
    setValue(field_min_no_sulfates,limitValue(gag_opt.MIN_NO_SULFATES,max_no_sulfates));
    setValue(field_max_no_sulfates,limitValue(gag_opt.MAX_NO_SULFATES,max_no_sulfates));
    
    field_reduced.setSelected(gag_opt.IS_REDUCED);
    field_unsaturated.setSelected(gag_opt.IS_UNSATURATED);

    field_derivatization.setModel(new javax.swing.DefaultComboBoxModel(new String[] {MassOptions.NO_DERIVATIZATION, MassOptions.PERMETHYLATED, MassOptions.PERDMETHYLATED, MassOptions.PERACETYLATED, MassOptions.PERDACETYLATED}));
    field_derivatization.setSelectedItem(gag_opt.DERIVATIZATION);

    field_modifications.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);                   
    field_modifications.setModel(createModel(GAGOptions.ALL_MODIFICATIONS));
    selectValues(field_modifications,gag_opt.MODIFICATIONS);

    field_allow_unlikely_acetylation.setSelected(gag_opt.ALLOW_UNLIKELY_ACETYLATION);
    field_allow_redend_loss.setSelected(gag_opt.ALLOW_REDEND_LOSS);
    }

    private void setTraversal() {
    CustomFocusTraversalPolicy tp = new CustomFocusTraversalPolicy();
    
    tp.addComponent(field_gag_families);
    tp.addComponent(field_min_no_units);
    tp.addComponent(field_max_no_units);
    tp.addComponent(field_min_no_acetyls);
    tp.addComponent(field_max_no_acetyls);
    tp.addComponent(field_min_no_sulfates);
    tp.addComponent(field_max_no_sulfates);
    tp.addComponent(field_reduced);
    tp.addComponent(field_unsaturated);
    tp.addComponent(field_derivatization);
    tp.addComponent(field_modifications);
    tp.addComponent(field_allow_unlikely_acetylation);
    tp.addComponent(field_allow_redend_loss);
    tp.addComponent(button_ok);
    tp.addComponent(button_cancel);

    this.setFocusTraversalPolicy(tp);

    getRootPane().setDefaultButton(button_ok); 
    }

    private void setActions() {
    field_gag_families.addListSelectionListener(this);

    field_min_no_units.addChangeListener(this);
    field_max_no_units.addChangeListener(this);

    field_min_no_acetyls.addChangeListener(this);
    field_max_no_acetyls.addChangeListener(this);

    field_min_no_sulfates.addChangeListener(this);
    field_max_no_sulfates.addChangeListener(this);
    
    field_modifications.addListSelectionListener(this);
    field_allow_unlikely_acetylation.addItemListener(this);

    button_ok.addActionListener(this);
    button_cancel.addActionListener(this);
    }
    
    private void enableItems() {
    }   

    private String[] getValues(javax.swing.JList field) {
    Object[] sel = field.getSelectedValues();
    String[] ret = new String[sel.length];
    for( int i=0; i<sel.length; i++ )
        ret[i] = (String)sel[i];
    return ret;
    }

    private void retrieveData(GAGOptions gag_opt) {
    gag_opt.GAG_FAMILIES = getValues(field_gag_families);
    gag_opt.MIN_NO_UNITS = getValue(field_min_no_units);
    gag_opt.MAX_NO_UNITS = getValue(field_max_no_units);
    gag_opt.MIN_NO_ACETYLS = getValue(field_min_no_acetyls);
    gag_opt.MAX_NO_ACETYLS = getValue(field_max_no_acetyls);
    gag_opt.MIN_NO_SULFATES = getValue(field_min_no_sulfates);
    gag_opt.MAX_NO_SULFATES = getValue(field_max_no_sulfates);
    gag_opt.IS_REDUCED = field_reduced.isSelected();
    gag_opt.IS_UNSATURATED = field_unsaturated.isSelected();
    gag_opt.DERIVATIZATION = (String)field_derivatization.getSelectedItem();
    gag_opt.MODIFICATIONS = getValues(field_modifications);
    gag_opt.ALLOW_UNLIKELY_ACETYLATION = field_allow_unlikely_acetylation.isSelected();
    gag_opt.ALLOW_REDEND_LOSS = field_allow_redend_loss.isSelected();
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
    String action = e.getActionCommand();
    
    if (action == "OK") {
        retrieveData(theGAGOptions);
        return_status = action;
        closeDialog();
    }
    else if (action == "Cancel"){
        return_status = action;
        closeDialog();
    }
    }  

    public void    valueChanged(javax.swing.event.ListSelectionEvent e) {
    if( allow_events )
        stateChanged(e.getSource());
    }

    public void itemStateChanged(java.awt.event.ItemEvent e) {
    if( allow_events )
        stateChanged(e.getSource());
    }

    public void stateChanged(javax.swing.event.ChangeEvent e) {
    if( allow_events )
        stateChanged(e.getSource());
    }

    public void stateChanged(Object source) {
    allow_events = false;
    
    // enforce min<=max
    if( source == field_max_no_units ) 
        setValue(field_min_no_units, Math.min(getValue(field_min_no_units),getValue(field_max_no_units)));
    else if( source==field_min_no_units ) 
        setValue(field_max_no_units, Math.max(getValue(field_min_no_units),getValue(field_max_no_units)));    
    else if( source==field_max_no_acetyls ) 
        setValue(field_min_no_acetyls, Math.min(getValue(field_min_no_acetyls),getValue(field_max_no_acetyls)));    
    else if( source==field_min_no_acetyls ) 
        setValue(field_max_no_acetyls, Math.max(getValue(field_min_no_acetyls),getValue(field_max_no_acetyls)));    
    else if( source==field_max_no_sulfates ) 
        setValue(field_min_no_sulfates, Math.min(getValue(field_min_no_sulfates),getValue(field_max_no_sulfates)));    
    else if( source==field_min_no_sulfates ) 
        setValue(field_max_no_sulfates, Math.max(getValue(field_min_no_sulfates),getValue(field_max_no_sulfates)));

     // regenerate data
    GAGOptions buffer = new GAGOptions(); 
    retrieveData(buffer);
    setData(buffer);

    allow_events = true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        field_reduced = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        field_unsaturated = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        button_ok = new javax.swing.JButton();
        button_cancel = new javax.swing.JButton();
        field_min_no_units = new javax.swing.JSpinner();
        field_max_no_units = new javax.swing.JSpinner();
        field_min_no_acetyls = new javax.swing.JSpinner();
        field_min_no_sulfates = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        field_max_no_acetyls = new javax.swing.JSpinner();
        field_max_no_sulfates = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        field_derivatization = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        field_gag_families = new javax.swing.JList();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        field_modifications = new javax.swing.JList();
        field_allow_unlikely_acetylation = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        field_allow_redend_loss = new javax.swing.JCheckBox();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });

        jLabel1.setText("GAG families");

        jLabel2.setText("# units");

        jLabel3.setText("Min");

        jLabel4.setText("# acetyls");

        jLabel5.setText("# sulfates");

        field_reduced.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        field_reduced.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel6.setText("reduced");

        field_unsaturated.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        field_unsaturated.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel7.setText("unsaturated");

        button_ok.setText("OK");

        button_cancel.setText("Cancel");

        jLabel8.setText("Max");

        jLabel9.setText("derivatized");

        field_derivatization.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        field_gag_families.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        field_gag_families.setVisibleRowCount(5);
        jScrollPane1.setViewportView(field_gag_families);

        jLabel10.setText("modifications");

        field_modifications.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        field_modifications.setVisibleRowCount(4);
        jScrollPane2.setViewportView(field_modifications);

        field_allow_unlikely_acetylation.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        field_allow_unlikely_acetylation.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel12.setText("Allow unlikely");

        jLabel13.setText("acetylation");

        jLabel14.setText("Allow loss of");

        jLabel15.setText("reducing end");

        field_allow_redend_loss.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        field_allow_redend_loss.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(68, 68, 68)
                        .add(button_ok)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(button_cancel))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .add(21, 21, 21)
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel13))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel15))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel10)
                            .add(jLabel12)
                            .add(jLabel9)
                            .add(jLabel7)
                            .add(jLabel6)
                            .add(jLabel5)
                            .add(jLabel4)
                            .add(jLabel2)
                            .add(jLabel14))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, field_allow_redend_loss)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel3)
                                        .add(60, 60, 60))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, field_min_no_sulfates, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                                            .add(field_min_no_acetyls, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, field_reduced)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, field_unsaturated)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, field_derivatization, 0, 91, Short.MAX_VALUE)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, field_min_no_units, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel8)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, field_max_no_units)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, field_max_no_acetyls, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                                        .add(field_max_no_sulfates)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, field_allow_unlikely_acetylation)
                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {field_max_no_units, field_min_no_units}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {button_cancel, button_ok}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_max_no_units, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_min_no_units, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_min_no_acetyls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_no_acetyls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_min_no_sulfates, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_no_sulfates, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_reduced)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_unsaturated)
                    .add(jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_derivatization, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel10)
                        .add(62, 62, 62))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_allow_unlikely_acetylation)
                    .add(jLabel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel13)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_allow_redend_loss))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel15)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
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
    private javax.swing.JCheckBox field_allow_redend_loss;
    private javax.swing.JComboBox field_derivatization;
    private javax.swing.JList field_gag_families;
    private javax.swing.JSpinner field_max_no_acetyls;
    private javax.swing.JSpinner field_max_no_sulfates;
    private javax.swing.JSpinner field_max_no_units;
    private javax.swing.JSpinner field_min_no_acetyls;
    private javax.swing.JSpinner field_min_no_sulfates;
    private javax.swing.JSpinner field_min_no_units;
    private javax.swing.JList field_modifications;
    private javax.swing.JCheckBox field_reduced;
    private javax.swing.JCheckBox field_unsaturated;
    private javax.swing.JCheckBox field_allow_unlikely_acetylation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
    
}
