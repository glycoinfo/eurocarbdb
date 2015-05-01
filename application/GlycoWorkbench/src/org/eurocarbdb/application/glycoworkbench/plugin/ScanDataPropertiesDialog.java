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

import javax.swing.*;
import java.text.*;


public class ScanDataPropertiesDialog extends EscapeDialog  implements java.awt.event.ActionListener {
    
    // data members
    private ScanData current;
    private GlycanWorkspace theWorkspace;

    private DecimalFormat double_format = new DecimalFormat("0.0000");

    /** Creates new form ScanDataPropertiesDialog */
    public ScanDataPropertiesDialog(java.awt.Frame parent, ScanData _current, GlycanWorkspace _workspace) {
        super(parent, true);

    // set data    
    current = _current;
    theWorkspace = _workspace;

    // init values
        initComponents();
    setSelections();
    setTraversal();
    setActions();
    enableItems();

    // set location
    setLocationRelativeTo(parent);
    }
    
    private void setSelections() {
    setValue(field_ms_level,current.getMSLevel());
    setValue(field_precursor_mz,current.getPrecursorMZ());
    setValue(field_precursor_charge,current.getPrecursorCharge());

    setValue(field_positive_mode,current.getPositiveMode());
    setValue(field_centroided,current.getCentroided());
    setValue(field_deisotoped,current.getDeisotoped());
    setValue(field_charge_deconvoluted,current.getChargeDeconvoluted());
    setValue(field_retention_time,current.getRetentionTime());

    setValue(field_base_peak_mz,current.getBasePeakMZ());
    setValue(field_base_peak_intensity,current.getBasePeakIntensity());
    setValue(field_start_mz,current.getStartMZ());
    setValue(field_end_mz,current.getEndMZ());
    setValue(field_low_mz,current.getLowMZ());
    setValue(field_high_mz,current.getHighMZ());
    setValue(field_total_ion_current,current.getTotalIonCurrent());
    }

    private void setValue(JTextField tf, Double v) {
    if( v!=null ) 
        tf.setText(double_format.format(v.doubleValue()));
    else
        tf.setText("");
    }

    private void setValue(JTextField tf, Integer v) {
    if( v!=null ) 
        tf.setText("" + v.intValue());
    else
        tf.setText("");
    }

    private void setValue(JCheckBox cb, Boolean v) {
    if( v!=null ) 
        cb.setSelected(v.booleanValue());
    else
        cb.setSelected(false);
    }

    private Double getDoubleValue(JTextField tf) {
    if( TextUtils.trim(tf.getText()).length()>0 ) {
        Number ret = double_format.parse(tf.getText(), new ParsePosition(0));
        if( ret==null )
        return null;
        return ret.doubleValue();
    }
    return null;
    }

    private Integer getIntegerValue(JTextField tf) {
    String text  = TextUtils.trim(tf.getText());
    if( text.length()>0 ) {
        try {
        return Integer.valueOf(text);
        }
        catch(Exception e) {
        return null;
        }
    }
    return null;
    }
        
 
    private void setTraversal() {
    CustomFocusTraversalPolicy tp = new CustomFocusTraversalPolicy();
        
    tp.addComponent(field_ms_level);
    tp.addComponent(field_precursor_mz);
    tp.addComponent(field_precursor_charge);
    tp.addComponent(field_base_peak_mz);
    tp.addComponent(field_base_peak_intensity);
    tp.addComponent(field_start_mz);
    tp.addComponent(field_end_mz);
    tp.addComponent(field_low_mz);
    tp.addComponent(field_high_mz);
    tp.addComponent(field_retention_time);
    tp.addComponent(field_total_ion_current);
    tp.addComponent(field_positive_mode);
    tp.addComponent(field_centroided);
    tp.addComponent(field_deisotoped);
    tp.addComponent(field_charge_deconvoluted);
    tp.addComponent(button_ok);
    tp.addComponent(button_cancel);

    this.setFocusTraversalPolicy(tp);

    getRootPane().setDefaultButton(button_ok); 
    }

    private void setActions() {
    button_ok.addActionListener(this);
    button_cancel.addActionListener(this);
    }

    private void enableItems() {

    }

    private void retrieveData() {
    current.setMSLevel(getIntegerValue(field_ms_level));
    current.setPrecursorMZ(getDoubleValue(field_precursor_mz));
    current.setPrecursorCharge(getIntegerValue(field_precursor_charge));

    current.setPositiveMode(field_positive_mode.isSelected());
    current.setCentroided(field_centroided.isSelected());
    current.setDeisotoped(field_deisotoped.isSelected());
    current.setChargeDeconvoluted(field_charge_deconvoluted.isSelected());
    current.setRetentionTime(getDoubleValue(field_retention_time));

    current.setBasePeakMZ(getDoubleValue(field_base_peak_mz));
    current.setBasePeakIntensity(getDoubleValue(field_base_peak_intensity));
    current.setStartMZ(getDoubleValue(field_start_mz));
    current.setEndMZ(getDoubleValue(field_end_mz));
    current.setLowMZ(getDoubleValue(field_low_mz));
    current.setHighMZ(getDoubleValue(field_high_mz));
    current.setTotalIonCurrent(getDoubleValue(field_total_ion_current));
    
    theWorkspace.fireDocumentChanged(theWorkspace);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
    String action = e.getActionCommand();
    
    if (action == "OK") {
        retrieveData();
        closeDialog();
    }
    else if (action == "Cancel"){
        closeDialog();
    }
    }        


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Application.
     */
    // <application-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        field_precursor_mz = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        button_ok = new javax.swing.JButton();
        button_cancel = new javax.swing.JButton();
        field_precursor_charge = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        field_positive_mode = new javax.swing.JCheckBox();
        field_centroided = new javax.swing.JCheckBox();
        field_deisotoped = new javax.swing.JCheckBox();
        field_charge_deconvoluted = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        field_start_mz = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        field_end_mz = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        field_base_peak_mz = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        field_base_peak_intensity = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        field_low_mz = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        field_high_mz = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        field_retention_time = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        field_total_ion_current = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        field_ms_level = new javax.swing.JTextField();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });

        jLabel2.setText("Precursor m/z");

        field_precursor_mz.setText("jTextField1");

        button_ok.setText("OK");

        button_cancel.setText("Cancel");

        field_precursor_charge.setText("jTextField1");

        jLabel4.setText("charge");

        field_positive_mode.setText("positive mode");

        field_centroided.setText("centroided");

        field_deisotoped.setText("deisotoped");

        field_charge_deconvoluted.setText("deconvoluted");

        jLabel5.setText("Start m/z");

        field_start_mz.setText("jTextField1");

        jLabel6.setText("end m/z");

        field_end_mz.setText("jTextField1");

        jLabel7.setText("Base peak m/z");

        field_base_peak_mz.setText("jTextField1");

        jLabel8.setText("intensity");

        field_base_peak_intensity.setText("jTextField1");

        jLabel9.setText("Low m/z");

        field_low_mz.setText("jTextField1");

        jLabel10.setText("high m/z");

        field_high_mz.setText("jTextField1");

        jLabel11.setText("Retention time");

        field_retention_time.setText("jTextField1");

        jLabel12.setText("TIC");

        field_total_ion_current.setText("jTextField1");

        jLabel13.setText("MS level");

        field_ms_level.setText("jTextField1");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel7)
                    .add(jLabel5)
                    .add(jLabel9)
                    .add(jLabel11)
                    .add(jLabel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(field_precursor_mz)
                            .add(field_base_peak_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(field_start_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(field_low_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(field_retention_time, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(field_positive_mode)
                            .add(field_deisotoped))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                            .add(jLabel8)
                            .add(jLabel6)
                            .add(jLabel10)
                            .add(jLabel12))
                        .add(12, 12, 12))
                    .add(field_ms_level, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(field_centroided)
                    .add(field_charge_deconvoluted)
                    .add(field_total_ion_current, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_high_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_end_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_base_peak_intensity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_precursor_charge))
                .add(12, 12, 12))
            .add(layout.createSequentialGroup()
                .add(140, 140, 140)
                .add(button_ok)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(button_cancel)
                .addContainerGap(178, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {button_cancel, button_ok}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {field_base_peak_intensity, field_end_mz, field_high_mz, field_total_ion_current}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {field_base_peak_mz, field_low_mz, field_precursor_charge, field_precursor_mz, field_retention_time, field_start_mz}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {jLabel11, jLabel2, jLabel5, jLabel7, jLabel9}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_ms_level, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel13))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(field_precursor_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4)
                    .add(field_precursor_charge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(field_base_peak_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8)
                    .add(field_base_peak_intensity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(field_start_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6)
                    .add(field_end_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(field_low_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10)
                    .add(field_high_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(field_retention_time, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel12)
                    .add(field_total_ion_current, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_positive_mode)
                    .add(field_centroided))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_deisotoped)
                    .add(field_charge_deconvoluted))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(button_cancel)
                    .add(button_ok))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }
    // </application-fold>//GEN-END:initComponents
    
    /** Closes the dialog */
    private void closeDialog() {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog       
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_cancel;
    private javax.swing.JButton button_ok;
    private javax.swing.JTextField field_base_peak_intensity;
    private javax.swing.JTextField field_base_peak_mz;
    private javax.swing.JCheckBox field_centroided;
    private javax.swing.JCheckBox field_charge_deconvoluted;
    private javax.swing.JCheckBox field_deisotoped;
    private javax.swing.JTextField field_end_mz;
    private javax.swing.JTextField field_high_mz;
    private javax.swing.JTextField field_low_mz;
    private javax.swing.JTextField field_ms_level;
    private javax.swing.JCheckBox field_positive_mode;
    private javax.swing.JTextField field_precursor_charge;
    private javax.swing.JTextField field_precursor_mz;
    private javax.swing.JTextField field_retention_time;
    private javax.swing.JTextField field_start_mz;
    private javax.swing.JTextField field_total_ion_current;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
    
}
