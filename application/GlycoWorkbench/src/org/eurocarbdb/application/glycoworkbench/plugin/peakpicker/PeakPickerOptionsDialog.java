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

package org.eurocarbdb.application.glycoworkbench.plugin.peakpicker;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;


public class PeakPickerOptionsDialog extends EscapeDialog  implements java.awt.event.ActionListener {
    
    private PeakPickerCWT thePeakPicker;
    private Double start_mz;
    private Double end_mz;

    /** Creates new form PeakPickerOptionsDialog */
    public PeakPickerOptionsDialog(java.awt.Frame parent, PeakPickerCWT pp) {
        super(parent, true);
    
    thePeakPicker = pp;
    start_mz = 0.;
    end_mz = 0.;

        initComponents();
    setTraversal();
    setData();    
    setActions();
    enableItems();

    // set location
    setLocationRelativeTo(parent);
    }


    public Double getStartMZ() {
    return start_mz;
    }

    public Double getEndMZ() {
    return end_mz;
    }
    
    private void setData() {

    field_start_mz.setText("");
    field_end_mz.setText("");

    field_correlation.setText("" + thePeakPicker.getPeakCorrBound());
    field_fwhm_bound.setText("" + thePeakPicker.getFwhmBound());
    field_noise_level.setText("" + thePeakPicker.getNoiseLevel());
    field_peak_bound.setText("" + thePeakPicker.getPeakBound());
    field_peak_bound_ms2.setText("" + thePeakPicker.getPeakBoundMs2Level());
    field_scale.setText("" + thePeakPicker.getWaveletScale());
    field_search_radius.setText("" + thePeakPicker.getSearchRadius());
    field_signal_to_noise.setText("" + thePeakPicker.getSignalToNoiseLevel());
    field_spacing.setText("" + thePeakPicker.getWaveletSpacing());
    field_win_len.setText("" + thePeakPicker.getWinLen());

    }

    private void setTraversal() {
    CustomFocusTraversalPolicy tp = new CustomFocusTraversalPolicy();

    tp.addComponent(field_start_mz);
    tp.addComponent(field_end_mz);

    tp.addComponent(field_signal_to_noise);
    tp.addComponent(field_win_len);
    tp.addComponent(field_peak_bound);
    tp.addComponent(field_peak_bound_ms2);
    tp.addComponent(field_fwhm_bound);
    tp.addComponent(field_correlation);
    tp.addComponent(field_noise_level);
    tp.addComponent(field_search_radius);
    tp.addComponent(field_scale);
    tp.addComponent(field_spacing);

    tp.addComponent(button_ok);
    tp.addComponent(button_cancel);
    }

    private void setActions() {
    button_ok.addActionListener(this);
    button_cancel.addActionListener(this);
    }

    private void enableItems() {
    }   

    private void retrieveData() {
    try {
        start_mz = Double.valueOf(field_start_mz.getText());
        end_mz = Double.valueOf(field_end_mz.getText());
    }
    catch(Exception e) {
        start_mz = end_mz = 0.;
    }

    thePeakPicker.setPeakCorrBound(Double.valueOf(field_correlation.getText()));
    thePeakPicker.setFwhmBound(Double.valueOf(field_fwhm_bound.getText()));
    thePeakPicker.setNoiseLevel(Double.valueOf(field_noise_level.getText()));
    thePeakPicker.setPeakBound(Double.valueOf(field_peak_bound.getText()));
    thePeakPicker.setPeakBoundMs2Level(Double.valueOf(field_peak_bound_ms2.getText()));
    thePeakPicker.setWaveletScale(Double.valueOf(field_scale.getText()));
    thePeakPicker.setSearchRadius(Integer.valueOf(field_search_radius.getText()));
    thePeakPicker.setSignalToNoiseLevel(Double.valueOf(field_signal_to_noise.getText()));
    thePeakPicker.setWaveletSpacing(Double.valueOf(field_spacing.getText()));
    thePeakPicker.setWinLen(Double.valueOf(field_win_len.getText()));
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
    String action = e.getActionCommand();
    
    if (action == "OK") {
        retrieveData();
        return_status = action;
        closeDialog();
    }
    else if (action == "Cancel"){
        return_status = action;
        closeDialog();
    }
    }  

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        field_peak_bound = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        field_peak_bound_ms2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        field_fwhm_bound = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        field_correlation = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        field_noise_level = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        field_search_radius = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        field_signal_to_noise = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        field_win_len = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        field_scale = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        field_spacing = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        button_ok = new javax.swing.JButton();
        button_cancel = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        field_end_mz = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        field_start_mz = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Min MS peak intensity");

        field_peak_bound.setText("jTextField1");

        jLabel2.setText("Min MS/MS peak intensity");

        field_peak_bound_ms2.setText("jTextField1");

        jLabel3.setText("Min peak width");

        field_fwhm_bound.setText("jTextField1");

        jLabel4.setText("Min correlation");

        field_correlation.setText("jTextField1");

        jLabel5.setText("Noise level");

        field_noise_level.setText("jTextField1");

        jLabel6.setText("Search radius");

        field_search_radius.setText("jTextField1");

        jLabel7.setText("Min signal/noise ratio");

        field_signal_to_noise.setText("jTextField1");

        jLabel8.setText("Window size for s/n estimation");

        field_win_len.setText("jTextField1");

        jLabel9.setText("Wavelet width");

        field_scale.setText("jTextField1");

        jLabel10.setText("Wavelet spacing");

        field_spacing.setText("jTextField1");

        button_ok.setText("OK");

        button_cancel.setText("Cancel");

        jLabel11.setText("End m/z");

        field_end_mz.setText("jTextField1");

        jLabel12.setText("Start m/z");

        field_start_mz.setText("jTextField1");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel12)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(field_start_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel11)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(field_end_mz, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE))
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(96, 96, 96)
                        .add(button_ok)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(button_cancel))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel7)
                            .add(jLabel8)
                            .add(jLabel2)
                            .add(jLabel1)
                            .add(jLabel3)
                            .add(jLabel4)
                            .add(jLabel5)
                            .add(jLabel6)
                            .add(jLabel9)
                            .add(jLabel10))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(field_signal_to_noise, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .add(field_win_len, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .add(field_peak_bound, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .add(field_peak_bound_ms2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .add(field_fwhm_bound, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .add(field_correlation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .add(field_noise_level, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .add(field_search_radius, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .add(field_scale, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .add(field_spacing, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {button_cancel, button_ok}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(field_start_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel11)
                    .add(field_end_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(field_signal_to_noise, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_win_len, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(field_peak_bound, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(field_peak_bound_ms2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(field_fwhm_bound, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(field_correlation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(field_noise_level, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(field_search_radius, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(field_scale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(field_spacing, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
    

    private void closeDialog() {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }   
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_cancel;
    private javax.swing.JButton button_ok;
    private javax.swing.JTextField field_correlation;
    private javax.swing.JTextField field_end_mz;
    private javax.swing.JTextField field_fwhm_bound;
    private javax.swing.JTextField field_noise_level;
    private javax.swing.JTextField field_peak_bound;
    private javax.swing.JTextField field_peak_bound_ms2;
    private javax.swing.JTextField field_scale;
    private javax.swing.JTextField field_search_radius;
    private javax.swing.JTextField field_signal_to_noise;
    private javax.swing.JTextField field_spacing;
    private javax.swing.JTextField field_start_mz;
    private javax.swing.JTextField field_win_len;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
    
}
