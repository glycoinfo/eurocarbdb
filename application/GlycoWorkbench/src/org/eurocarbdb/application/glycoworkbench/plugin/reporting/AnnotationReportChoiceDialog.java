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
* AnnotationReportChoiceDialog.java
*
* Created on 25 January 2008, 16:32
*/

package org.eurocarbdb.application.glycoworkbench.plugin.reporting;

import org.eurocarbdb.application.glycoworkbench.plugin.*;
import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;



/**
*
* @author  aceroni
*/
public class AnnotationReportChoiceDialog extends EscapeDialog implements java.awt.event.ActionListener {

    private GlycanWorkspace theWorkspace;
    private AnnotatedPeakList theAnnotations;
    private SpectraDocument theSpectra;
    private GlycanRenderer theGlycanRenderer;
    private AnnotationReportOptions theOptions;
    
    private double                   return_start_mz;
    private double                   return_end_mz;
    private Glycan                   return_parent;
    private PeakAnnotationCollection return_pac;
    private PeakData                 return_pd;
    
    /** Creates new form AnnotationReportChoiceDialog */
    public AnnotationReportChoiceDialog(java.awt.Frame parent, GlycanWorkspace workspace, AnnotationReportOptions opt) {
        super(parent, true);

    theWorkspace = workspace;
    theAnnotations = theWorkspace.getAnnotatedPeakList();
    theSpectra = theWorkspace.getSpectra();
    theGlycanRenderer = theWorkspace.getGlycanRenderer();
    theOptions = opt;

        initComponents();
    setTraversal();
    setData();    
    setActions();
    enableItems();
    
    setLocationRelativeTo(parent);
    }

    public Double getStartMZ() {
    return return_start_mz;
    }

    public Double getEndMZ() {
    return return_end_mz;
    }

    public Glycan getParentStructure() {
    return return_parent;
    }

    public PeakAnnotationCollection getPeakAnnotationCollection() {
    return return_pac;
    }

    public PeakData getPeakData() {
    return return_pd;
    }
    
    private void setTraversal() {
    CustomFocusTraversalPolicy tp = new CustomFocusTraversalPolicy();
    
    tp.addComponent(field_start_mz);
    tp.addComponent(field_end_mz);
    tp.addComponent(field_structures_list);
    tp.addComponent(field_spectra_list);
    tp.addComponent(field_show_raw_spectrum);
    tp.addComponent(field_show_rel_int);
    tp.addComponent(field_show_empty_ann);
    tp.addComponent(field_show_max_int);
    tp.addComponent(button_ok);
    tp.addComponent(button_cancel);

    this.setFocusTraversalPolicy(tp);

    getRootPane().setDefaultButton(button_ok);
    }
    
    private void setData() {    
    
    field_start_mz.setText("");
    field_end_mz.setText("");

    field_structures_list.setCellRenderer( new javax.swing.DefaultListCellRenderer()  {
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
    
    field_structures_list.setListData(theAnnotations.getStructures());
    field_structures_list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    if( theAnnotations.size()>0 )
        field_structures_list.setSelectedIndex(0);

    field_spectra_list.setListData(theSpectra.getScans());
    field_spectra_list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    if( theSpectra.size()>0 )
        field_spectra_list.setSelectedIndex(0);

    if( theSpectra.size()>0 )
        field_show_raw_spectrum.setSelected(theOptions.SHOW_RAW_SPECTRUM);
    field_show_rel_int.setSelected(theOptions.SHOW_RELATIVE_INTENSITIES);
    field_show_empty_ann.setSelected(theOptions.SHOW_EMPTY_ANNOTATIONS);
    field_show_max_int.setSelected(theOptions.SHOW_MAX_INTENSITY);

    this.pack();
    }

    private void setActions() {
    field_show_raw_spectrum.addActionListener(this);
    field_show_rel_int.addActionListener(this);
    button_ok.addActionListener(this);
    button_cancel.addActionListener(this);
    }

    private void enableItems() {
    field_spectra_list.setEnabled( (theSpectra.size()>0) && field_show_raw_spectrum.isSelected() );
    field_show_raw_spectrum.setEnabled(theSpectra.size()>0 );
    field_show_max_int.setEnabled(field_show_rel_int.isSelected());
    }
   
    private void retrieveData() {
    try {
        return_start_mz = Double.valueOf(field_start_mz.getText());
        return_end_mz = Double.valueOf(field_end_mz.getText());
    }
    catch(Exception e) {
        return_start_mz = return_end_mz = 0.;
    }
     
    // get annotations
    if( field_structures_list.getSelectedIndex()>=0 ) {
        return_parent = theAnnotations.getStructure(field_structures_list.getSelectedIndex());
        return_pac = theAnnotations.getPeakAnnotationCollection(field_structures_list.getSelectedIndex());
    }
    else if( theAnnotations.size()>0 ) {
        return_parent = theAnnotations.getStructure(0);
        return_pac = theAnnotations.getPeakAnnotationCollection(0);
    }
    else {
        return_parent = null;
        return_pac = null;
    }
    
    // get peak data
    if( field_spectra_list.getSelectedIndex()>=0 )
        return_pd = theSpectra.getPeakDataAt(field_spectra_list.getSelectedIndex());
    else if( theSpectra.size()>0 )
        return_pd = theSpectra.getPeakDataAt(0);
    else
        return_pd = null;
    
    theOptions.SHOW_RAW_SPECTRUM = field_show_raw_spectrum.isSelected();
    theOptions.SHOW_RELATIVE_INTENSITIES = field_show_rel_int.isSelected();
    theOptions.SHOW_EMPTY_ANNOTATIONS = field_show_empty_ann.isSelected();
    theOptions.SHOW_MAX_INTENSITY = field_show_max_int.isSelected();
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
    else
        enableItems();
    }  

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
    jLabel1 = new javax.swing.JLabel();
        field_start_mz = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        field_end_mz = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        button_ok = new javax.swing.JButton();
        button_cancel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        field_structures_list = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        field_spectra_list = new javax.swing.JList();
        field_show_rel_int = new javax.swing.JCheckBox();
        field_show_raw_spectrum = new javax.swing.JCheckBox();
        field_show_empty_ann = new javax.swing.JCheckBox();
        field_show_max_int = new javax.swing.JCheckBox();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });

        jLabel1.setText("Start m/z");

        field_start_mz.setText("jTextField1");

        jLabel2.setText("End m/z");

        field_end_mz.setText("jTextField2");

        jLabel3.setText("Choose a structure");

        jLabel4.setText("Choose a spectrum");

        button_ok.setText("OK");

        button_cancel.setText("Cancel");

        field_structures_list.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(field_structures_list);

        field_spectra_list.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(field_spectra_list);

        field_show_rel_int.setText("Show relative intensities");
        field_show_rel_int.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        field_show_raw_spectrum.setText("Show raw spectrum");
        field_show_raw_spectrum.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        field_show_empty_ann.setText("Show empty annotations");
        field_show_empty_ann.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        field_show_max_int.setText("Show max intensity");
        field_show_max_int.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(jScrollPane2)
                                .add(layout.createSequentialGroup()
                                    .add(jLabel1)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(field_start_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(jLabel2)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(field_end_mz, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))
                                .add(jLabel3)
                                .add(jLabel4))
                            .add(field_show_rel_int)
                            .add(field_show_raw_spectrum)
                            .add(field_show_empty_ann)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(field_show_max_int))
                    .add(layout.createSequentialGroup()
                        .add(96, 96, 96)
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(field_start_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2)
                    .add(field_end_mz, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(field_show_raw_spectrum)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(field_show_rel_int)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(field_show_empty_ann)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(field_show_max_int)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(button_ok)
                    .add(button_cancel))
                .addContainerGap())
        );

        pack();

    }// </editor-fold>
    
    /** Closes the dialog */
    private void closeDialog() {                             
        setVisible(false);
        dispose();
    }                            
       
    // Variables declaration - do not modify
    private javax.swing.JButton button_cancel;
    private javax.swing.JButton button_ok;
    private javax.swing.JTextField field_end_mz;
    private javax.swing.JCheckBox field_show_empty_ann;
    private javax.swing.JCheckBox field_show_max_int;
    private javax.swing.JCheckBox field_show_raw_spectrum;
    private javax.swing.JCheckBox field_show_rel_int;
    private javax.swing.JList field_spectra_list;
    private javax.swing.JTextField field_start_mz;
    private javax.swing.JList field_structures_list;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration
        
}

