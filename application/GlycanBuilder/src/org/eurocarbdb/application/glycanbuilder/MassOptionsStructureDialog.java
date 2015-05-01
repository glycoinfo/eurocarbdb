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

package org.eurocarbdb.application.glycanbuilder;

import java.util.Collection;

/**
   Dialog to change the mass options for a set of structures. Some of
   the options can be left unspecified and the corresponding options
   in the various structures will not be changed

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class MassOptionsStructureDialog extends EscapeDialog implements java.awt.event.ActionListener {
    
    private MassOptions common_options = new MassOptions();
    private Collection<Glycan> structures = null;

    /** 
    Creates a new dialog 
    @param parent the parent frame
    @param _structures the glycan structures whose mass options needs to be changed
    @param _default the default mass options
    */

    public MassOptionsStructureDialog(java.awt.Frame parent, Collection<Glycan> _structures, MassOptions _default) {
        super(parent, true);
    
    structures = _structures;
    setCommonOptions(_structures,_default);

        initComponents();
    initData();
    setTraversal();
    setActions();
    enableItems();

    // set location
    setLocationRelativeTo(parent);
    }       


    /**
       Return the selected mass options
     */
    public MassOptions getMassOptions() {
    return common_options;
    }

    private Object[] generateValues(int min, int max, boolean include_und) {

    if( include_und ) {
        Object[] values = new Object[1+(max-min+1)];    

        values[0] = "---";
        for( int i=min; i<=max; i++ )
        values[i-min+1] = Integer.valueOf(i);
        return values;
    }

    Object[] values = new Object[max-min+1];    
    for( int i=min; i<=max; i++ )
        values[i-min] = Integer.valueOf(i);
    return values;
    }

    private void setValue(javax.swing.JSpinner field, int value) {
    if( value==999 )
        field.setValue("---");
    else
        field.setValue(Integer.valueOf(value));
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

    private void setCommonOptions(Collection<Glycan> structures, MassOptions _default) {
    if( structures==null || structures.size()==0 ) {
        common_options = _default;
        return;
    }

    // retrieve common options
    common_options = new MassOptions(true);
    
    boolean first = true;
    for( Glycan structure : structures ) {
        MassOptions structure_options = structure.getMassOptions();
        if( first ) {
        common_options = structure_options.clone();
        first = false;
        }
        else {
        common_options.merge(structure_options);
        }
    }
    }
    
    private void initData() {
    
    // set models
    field_isotope.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"---",MassOptions.ISOTOPE_MONO,MassOptions.ISOTOPE_AVG}));

        field_derivatization.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"---",MassOptions.NO_DERIVATIZATION,MassOptions.PERMETHYLATED,MassOptions.PERDMETHYLATED,MassOptions.PERACETYLATED,MassOptions.PERDACETYLATED}));

    field_reducingend.setModel(new javax.swing.DefaultComboBoxModel(new Union<String>().and("---").and(ResidueDictionary.getReducingEndsString()).and("Other...").toArray(new String[0])));

        field_no_h_ions.setModel(new javax.swing.SpinnerListModel(generateValues(0,10,true)));
        field_no_na_ions.setModel(new javax.swing.SpinnerListModel(generateValues(0,10,true)));
        field_no_li_ions.setModel(new javax.swing.SpinnerListModel(generateValues(0,10,true)));
    field_no_k_ions.setModel(new javax.swing.SpinnerListModel(generateValues(0,10,true)));

        field_ex_na_ions.setModel(new javax.swing.SpinnerListModel(generateValues(0,50,true)));
        field_ex_li_ions.setModel(new javax.swing.SpinnerListModel(generateValues(0,50,true)));
    field_ex_k_ions.setModel(new javax.swing.SpinnerListModel(generateValues(0,50,true)));

    // set selections
    field_isotope.setSelectedItem(common_options.ISOTOPE);
    field_derivatization.setSelectedItem(common_options.DERIVATIZATION);

    if( common_options.REDUCING_END_TYPE==null ) {
        field_reducingend.setSelectedItem("---");
        field_other_name.setText("");
        field_other_mass.setText("0");
    }
    else if( common_options.REDUCING_END_TYPE.isCustomType() ) {
        field_reducingend.setSelectedItem("Other...");
        field_other_name.setText(common_options.REDUCING_END_TYPE.getResidueName());
        field_other_mass.setText("" + common_options.REDUCING_END_TYPE.getResidueMassMain());
    }
    else {
        field_reducingend.setSelectedItem(common_options.REDUCING_END_TYPE.getName());
        field_other_name.setText("");
        field_other_mass.setText("0");
    }
    
    field_negative_mode.setSelected(common_options.ION_CLOUD.isNegative());

    setValue(field_no_h_ions,Math.abs(common_options.ION_CLOUD.get(MassOptions.ION_H)));
    setValue(field_no_na_ions,Math.abs(common_options.ION_CLOUD.get(MassOptions.ION_NA)));
    setValue(field_no_li_ions,Math.abs(common_options.ION_CLOUD.get(MassOptions.ION_LI)));
    setValue(field_no_k_ions,Math.abs(common_options.ION_CLOUD.get(MassOptions.ION_K)));

    setValue(field_ex_na_ions,common_options.NEUTRAL_EXCHANGES.get(MassOptions.ION_NA));
    setValue(field_ex_li_ions,common_options.NEUTRAL_EXCHANGES.get(MassOptions.ION_LI));
    setValue(field_ex_k_ions,common_options.NEUTRAL_EXCHANGES.get(MassOptions.ION_K));    
    }


    private void setTraversal() {
    CustomFocusTraversalPolicy tp = new CustomFocusTraversalPolicy();
    tp.addComponent(field_isotope);
    tp.addComponent(field_derivatization);
    tp.addComponent(field_reducingend);
    tp.addComponent(field_other_name);
    tp.addComponent(field_other_mass);
    tp.addComponent(field_negative_mode);
    tp.addComponent(field_no_h_ions);
    tp.addComponent(field_no_na_ions);
    tp.addComponent(field_no_li_ions);
    tp.addComponent(field_no_k_ions);
    tp.addComponent(field_ex_na_ions);
    tp.addComponent(field_ex_li_ions);
    tp.addComponent(field_ex_k_ions);
    tp.addComponent(button_ok);
    tp.addComponent(button_cancel);
    this.setFocusTraversalPolicy(tp);
    
    getRootPane().setDefaultButton(button_ok);  
    }
    
    private void setActions() {
    button_ok.addActionListener(this);
    button_cancel.addActionListener(this);    
    field_reducingend.addActionListener(this);
    }

    private void enableItems() {
    //field_isotope.setEnabled(structures==null || structures.size()==0);
    field_isotope.setEnabled(false);
    field_other_name.setEnabled(field_reducingend.getSelectedItem().equals("Other..."));
    field_other_mass.setEnabled(field_reducingend.getSelectedItem().equals("Other..."));
    }

    private boolean retrieveData() {

    // retrieve common options
    if( field_reducingend.getSelectedItem().equals("Other...") ) {
        ResidueType red_end_type = ResidueType.createOtherReducingEnd(field_other_name.getText(),Double.valueOf(field_other_mass.getText()));
        if( ResidueDictionary.findResidueType(red_end_type.getResidueName())!=null ) {
        javax.swing.JOptionPane.showMessageDialog(this,"The name specified for the reducing end is already existing.", "Duplicate name", javax.swing.JOptionPane.ERROR_MESSAGE);
        return false;
        }
        common_options.REDUCING_END_TYPE = red_end_type;
    }
    else
        common_options.REDUCING_END_TYPE = ResidueDictionary.findResidueType((String)field_reducingend.getSelectedItem());

    common_options.ISOTOPE = (String)field_isotope.getSelectedItem();
    common_options.DERIVATIZATION = (String)field_derivatization.getSelectedItem();
        
    int multiplier = (field_negative_mode.isSelected()) ?-1 :1;
    common_options.ION_CLOUD.set(MassOptions.ION_H,multiplier*getValue(field_no_h_ions));
    common_options.ION_CLOUD.set(MassOptions.ION_NA,multiplier*getValue(field_no_na_ions));
    common_options.ION_CLOUD.set(MassOptions.ION_LI,multiplier*getValue(field_no_li_ions));
    common_options.ION_CLOUD.set(MassOptions.ION_K,multiplier*getValue(field_no_k_ions));
    
    common_options.NEUTRAL_EXCHANGES.set(MassOptions.ION_H,-getValue(field_ex_na_ions)-getValue(field_ex_li_ions)-getValue(field_ex_k_ions));
    common_options.NEUTRAL_EXCHANGES.set(MassOptions.ION_NA,getValue(field_ex_na_ions));
    common_options.NEUTRAL_EXCHANGES.set(MassOptions.ION_LI,getValue(field_ex_li_ions));
    common_options.NEUTRAL_EXCHANGES.set(MassOptions.ION_K,getValue(field_ex_k_ions));
    return true;
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
    String action = e.getActionCommand();
    
    if (action == "OK") {
        return_status = action;
        if( retrieveData() )
        closeDialog();
    }
    else if (action == "Cancel"){
        return_status = action;
        closeDialog();
    }
    else
        enableItems();
    
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {

    jLabel2 = new javax.swing.JLabel();
        field_isotope = new javax.swing.JComboBox();
        field_derivatization = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        button_ok = new javax.swing.JButton();
        button_cancel = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        field_no_h_ions = new javax.swing.JSpinner();
        field_no_na_ions = new javax.swing.JSpinner();
        field_no_li_ions = new javax.swing.JSpinner();
        field_no_k_ions = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        field_ex_na_ions = new javax.swing.JSpinner();
        field_ex_li_ions = new javax.swing.JSpinner();
        field_ex_k_ions = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        field_negative_mode = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        field_reducingend = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        field_other_name = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        field_other_mass = new javax.swing.JTextField();

        setResizable(false);
        setTitle("Mass options");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });

        jLabel2.setText("Isotope");

        field_isotope.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        field_derivatization.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel3.setText("Derivatization");

        jLabel4.setText("# H ions");

        button_ok.setText("OK");

        button_cancel.setText("Cancel");

        jLabel5.setText("# Na ions");

        jLabel7.setText("# Li ions");

        jLabel8.setText("# K ions");

        jLabel6.setText("ex. Na ions");

        jLabel9.setText("ex. Li ions");

        jLabel10.setText("ex. K ions");

        jLabel11.setText("Negative mode");

        field_negative_mode.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        field_negative_mode.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel12.setText("Reducing end");

        field_reducingend.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("name");

        field_other_name.setText("jTextField1");

        jLabel13.setText("mass");

        field_other_mass.setText("jTextField1");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel2)
                            .add(jLabel12))
                        .add(21, 21, 21)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(field_reducingend, 0, 80, Short.MAX_VALUE)
                                    .add(jLabel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(field_other_mass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, field_other_name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, field_isotope, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, field_derivatization, 0, 80, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 172, Short.MAX_VALUE))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(107, 107, 107)
                        .add(button_ok)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(button_cancel))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel11)
                            .add(jLabel8)
                            .add(jLabel7)
                            .add(jLabel5)
                            .add(jLabel4))
                        .add(16, 16, 16)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(field_no_na_ions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                                    .add(field_no_h_ions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                                    .add(field_no_li_ions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                                    .add(field_no_k_ions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                                .add(12, 12, 12))
                            .add(layout.createSequentialGroup()
                                .add(field_negative_mode)
                                .add(79, 79, 79)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(jLabel10)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 19, Short.MAX_VALUE)
                                .add(field_ex_k_ions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel6)
                                    .add(jLabel9))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(field_ex_na_ions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, field_ex_li_ions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {button_cancel, button_ok}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_isotope, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_derivatization, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(field_reducingend, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(field_other_name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(field_other_mass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(field_negative_mode))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(field_no_h_ions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(30, 30, 30)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel7)
                            .add(field_ex_li_ions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(field_no_li_ions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel9)))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel5)
                        .add(field_ex_na_ions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(field_no_na_ions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel6)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel8)
                        .add(field_ex_k_ions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(field_no_k_ions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel10)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(button_ok)
                    .add(button_cancel))
                .addContainerGap())
        );
        pack();
    }// </editor-fold>
       
    /** Closes the dialog */
    private void closeDialog() {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog        
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_cancel;
    private javax.swing.JButton button_ok;
    private javax.swing.JComboBox field_derivatization;
    private javax.swing.JSpinner field_ex_k_ions;
    private javax.swing.JSpinner field_ex_li_ions;
    private javax.swing.JSpinner field_ex_na_ions;
    private javax.swing.JComboBox field_isotope;
    private javax.swing.JCheckBox field_negative_mode;
    private javax.swing.JSpinner field_no_h_ions;
    private javax.swing.JSpinner field_no_k_ions;
    private javax.swing.JSpinner field_no_li_ions;
    private javax.swing.JSpinner field_no_na_ions;
    private javax.swing.JTextField field_other_mass;
    private javax.swing.JTextField field_other_name;
    private javax.swing.JComboBox field_reducingend;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    // End of variables declaration//GEN-END:variables
    
}
