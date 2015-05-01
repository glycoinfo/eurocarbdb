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

package org.eurocarbdb.application.glycoworkbench;

import org.eurocarbdb.application.glycanbuilder.*;

public class AnnotationOptionsDialog extends EscapeDialog implements
		java.awt.event.ActionListener, java.awt.event.ItemListener,
		javax.swing.event.ChangeListener {

	private FragmentOptions theFragmentOptions;
	private AnnotationOptions theAnnotationOptions;
	private boolean annotate_fragments = true;
	private boolean derive_options = true;

	/**
	 * Creates new form FragmentOptionsDialog1
	 */
	public AnnotationOptionsDialog(java.awt.Frame parent,
			FragmentOptions frag_opt, AnnotationOptions ann_opt,
			boolean ann_fragments, boolean der_options) {
		super(parent, true);

		theFragmentOptions = (frag_opt != null) ? frag_opt
				: new FragmentOptions();
		theAnnotationOptions = (ann_opt != null) ? ann_opt
				: new AnnotationOptions();
		annotate_fragments = ann_fragments;
		derive_options = der_options;

		initComponents();
		fillComponents();
		setSelections();
		setTraversal();
		setActions();
		enableItems();

		// set location
		setLocationRelativeTo(parent);
		
		//New for Substance
		pack();
	}

	private void setValue(javax.swing.JSpinner field, int value) {
		if (value == 999)
			field.setValue("---");
		else
			field.setValue(value);
	}

	private int getValue(javax.swing.JSpinner field) {
		if (field.getValue().equals("---"))
			return 999;
		return (Integer) field.getValue();
	}

	private Object[] generateValues(int min, int max, boolean include_und) {

		if (include_und) {
			Object[] values = new Object[1 + (max - min + 1)];

			values[0] = "---";
			for (int i = min; i <= max; i++)
				values[i - min + 1] = Integer.valueOf(i);
			return values;
		}

		Object[] values = new Object[max - min + 1];
		for (int i = min; i <= max; i++)
			values[i - min] = Integer.valueOf(i);
		return values;
	}

	private void fillComponents() {

		field_no_cleavages.setModel(new javax.swing.SpinnerNumberModel(1, 1, 5,
				1));
		field_no_crossrings.setModel(new javax.swing.SpinnerNumberModel(0, 0,
				5, 1));

		field_accuracy_unit.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] {}));
		field_accuracy_unit.addItem(AnnotationOptions.MASS_ACCURACY_DA);
		field_accuracy_unit.addItem(AnnotationOptions.MASS_ACCURACY_PPM);

		field_max_no_h_ions.setModel(new javax.swing.SpinnerNumberModel(0, 0,
				10, 1));
		field_max_no_na_ions.setModel(new javax.swing.SpinnerNumberModel(0, 0,
				10, 1));
		field_max_no_li_ions.setModel(new javax.swing.SpinnerNumberModel(0, 0,
				10, 1));
		field_max_no_k_ions.setModel(new javax.swing.SpinnerNumberModel(0, 0,
				10, 1));
		field_max_no_charges.setModel(new javax.swing.SpinnerNumberModel(0, 0,
				10, 1));

		field_max_ex_na_ions.setModel(new javax.swing.SpinnerListModel(
				generateValues(0, 50, true)));
		field_max_ex_li_ions.setModel(new javax.swing.SpinnerListModel(
				generateValues(0, 50, true)));
		field_max_ex_k_ions.setModel(new javax.swing.SpinnerListModel(
				generateValues(0, 50, true)));

		field_accuracy.setText("1.0");
	}

	private void setSelections() {
		field_afragments.setSelected(theFragmentOptions.ADD_AFRAGMENTS);
		field_bfragments.setSelected(theFragmentOptions.ADD_BFRAGMENTS);
		field_cfragments.setSelected(theFragmentOptions.ADD_CFRAGMENTS);
		field_xfragments.setSelected(theFragmentOptions.ADD_XFRAGMENTS);
		field_yfragments.setSelected(theFragmentOptions.ADD_YFRAGMENTS);
		field_zfragments.setSelected(theFragmentOptions.ADD_ZFRAGMENTS);

		field_internal_fragments
				.setSelected(theFragmentOptions.INTERNAL_FRAGMENTS);

		field_no_cleavages.setValue(theFragmentOptions.MAX_NO_CLEAVAGES);
		field_no_crossrings.setValue(theFragmentOptions.MAX_NO_CROSSRINGS);

		field_negative_mode.setSelected(theAnnotationOptions.NEGATIVE_MODE);
		field_max_no_h_ions.setValue(theAnnotationOptions.MAX_NO_H_IONS);
		field_max_no_na_ions.setValue(theAnnotationOptions.MAX_NO_NA_IONS);
		field_max_no_li_ions.setValue(theAnnotationOptions.MAX_NO_LI_IONS);
		field_max_no_k_ions.setValue(theAnnotationOptions.MAX_NO_K_IONS);
		field_max_no_charges.setValue(theAnnotationOptions.MAX_NO_CHARGES);

		field_compute_exchanges
				.setSelected(theAnnotationOptions.COMPUTE_EXCHANGES);
		setValue(field_max_ex_na_ions, theAnnotationOptions.MAX_EX_NA_IONS);
		setValue(field_max_ex_li_ions, theAnnotationOptions.MAX_EX_LI_IONS);
		setValue(field_max_ex_k_ions, theAnnotationOptions.MAX_EX_K_IONS);

		field_derive_from_parent
				.setSelected(theAnnotationOptions.DERIVE_FROM_PARENT
						&& derive_options);

		field_accuracy.setText(Double
				.toString(theAnnotationOptions.MASS_ACCURACY));
		field_accuracy_unit
				.setSelectedItem(theAnnotationOptions.MASS_ACCURACY_UNIT);
	}

	private void setTraversal() {
		CustomFocusTraversalPolicy tp = new CustomFocusTraversalPolicy();
		tp.addComponent(field_bfragments);
		tp.addComponent(field_cfragments);
		tp.addComponent(field_yfragments);
		tp.addComponent(field_zfragments);
		tp.addComponent(field_afragments);
		tp.addComponent(field_xfragments);

		tp.addComponent(field_internal_fragments);

		tp.addComponent(field_no_cleavages);
		tp.addComponent(field_no_crossrings);

		tp.addComponent(field_negative_mode);
		tp.addComponent(field_max_no_h_ions);
		tp.addComponent(field_max_no_na_ions);
		tp.addComponent(field_max_no_li_ions);
		tp.addComponent(field_max_no_k_ions);
		tp.addComponent(field_max_no_charges);

		tp.addComponent(field_compute_exchanges);
		tp.addComponent(field_max_ex_na_ions);
		tp.addComponent(field_max_ex_li_ions);
		tp.addComponent(field_max_ex_k_ions);

		tp.addComponent(field_derive_from_parent);

		tp.addComponent(field_accuracy);
		tp.addComponent(field_accuracy_unit);

		tp.addComponent(button_ok);
		tp.addComponent(button_cancel);

		this.setFocusTraversalPolicy(tp);

		getRootPane().setDefaultButton(button_ok);
	}

	private void setActions() {
		field_negative_mode.addItemListener(this);
		field_compute_exchanges.addItemListener(this);

		field_max_no_h_ions.addChangeListener(this);
		field_max_no_na_ions.addChangeListener(this);
		field_max_no_li_ions.addChangeListener(this);
		field_max_no_k_ions.addChangeListener(this);
		field_max_no_charges.addChangeListener(this);

		field_max_ex_na_ions.addChangeListener(this);
		field_max_ex_li_ions.addChangeListener(this);
		field_max_ex_k_ions.addChangeListener(this);

		field_derive_from_parent.addItemListener(this);

		button_ok.addActionListener(this);
		button_cancel.addActionListener(this);
	}

	private void enableItems() {
		field_afragments.setEnabled(annotate_fragments);
		field_bfragments.setEnabled(annotate_fragments);
		field_cfragments.setEnabled(annotate_fragments);
		field_xfragments.setEnabled(annotate_fragments);
		field_yfragments.setEnabled(annotate_fragments);
		field_zfragments.setEnabled(annotate_fragments);

		field_internal_fragments.setEnabled(annotate_fragments);

		field_no_cleavages.setEnabled(annotate_fragments);
		field_no_crossrings.setEnabled(annotate_fragments);

		field_negative_mode.setEnabled(!field_derive_from_parent.isSelected());
		field_max_no_h_ions.setEnabled(!field_derive_from_parent.isSelected());
		field_max_no_na_ions.setEnabled(!field_negative_mode.isSelected()
				&& !field_derive_from_parent.isSelected());
		field_max_no_li_ions.setEnabled(!field_negative_mode.isSelected()
				&& !field_derive_from_parent.isSelected());
		field_max_no_k_ions.setEnabled(!field_negative_mode.isSelected()
				&& !field_derive_from_parent.isSelected());
		field_max_no_charges.setEnabled(!field_derive_from_parent.isSelected());

		field_compute_exchanges.setEnabled(!field_derive_from_parent
				.isSelected());
		field_max_ex_na_ions.setEnabled(field_compute_exchanges.isSelected()
				&& !field_derive_from_parent.isSelected());
		field_max_ex_li_ions.setEnabled(field_compute_exchanges.isSelected()
				&& !field_derive_from_parent.isSelected());
		field_max_ex_k_ions.setEnabled(field_compute_exchanges.isSelected()
				&& !field_derive_from_parent.isSelected());

		field_derive_from_parent.setEnabled(derive_options);
	}

	private void retrieveData() {
		theFragmentOptions.ADD_AFRAGMENTS = field_afragments.isSelected();
		theFragmentOptions.ADD_BFRAGMENTS = field_bfragments.isSelected();
		theFragmentOptions.ADD_CFRAGMENTS = field_cfragments.isSelected();
		theFragmentOptions.ADD_XFRAGMENTS = field_xfragments.isSelected();
		theFragmentOptions.ADD_YFRAGMENTS = field_yfragments.isSelected();
		theFragmentOptions.ADD_ZFRAGMENTS = field_zfragments.isSelected();

		theFragmentOptions.INTERNAL_FRAGMENTS = field_internal_fragments
				.isSelected();

		theFragmentOptions.MAX_NO_CLEAVAGES = (Integer) field_no_cleavages
				.getValue();
		theFragmentOptions.MAX_NO_CROSSRINGS = (Integer) field_no_crossrings
				.getValue();

		theAnnotationOptions.NEGATIVE_MODE = field_negative_mode.isSelected();
		theAnnotationOptions.MAX_NO_H_IONS = (Integer) field_max_no_h_ions
				.getValue();
		theAnnotationOptions.MAX_NO_NA_IONS = (Integer) field_max_no_na_ions
				.getValue();
		theAnnotationOptions.MAX_NO_LI_IONS = (Integer) field_max_no_li_ions
				.getValue();
		theAnnotationOptions.MAX_NO_K_IONS = (Integer) field_max_no_k_ions
				.getValue();
		theAnnotationOptions.MAX_NO_CHARGES = (Integer) field_max_no_charges
				.getValue();

		theAnnotationOptions.COMPUTE_EXCHANGES = field_compute_exchanges
				.isSelected();
		theAnnotationOptions.MAX_EX_NA_IONS = getValue(field_max_ex_na_ions);
		theAnnotationOptions.MAX_EX_LI_IONS = getValue(field_max_ex_li_ions);
		theAnnotationOptions.MAX_EX_K_IONS = getValue(field_max_ex_k_ions);

		if (derive_options)
			theAnnotationOptions.DERIVE_FROM_PARENT = field_derive_from_parent
					.isSelected();

		theAnnotationOptions.MASS_ACCURACY = Double.valueOf(field_accuracy
				.getText());
		theAnnotationOptions.MASS_ACCURACY_UNIT = (String) field_accuracy_unit
				.getSelectedItem();
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		String action = e.getActionCommand();

		if (action == "OK") {
			retrieveData();
			return_status = action;
			closeDialog();
		} else if (action == "Cancel") {
			return_status = action;
			closeDialog();
		}
	}

	public void itemStateChanged(java.awt.event.ItemEvent e) {
		enableItems();
	}

	public void stateChanged(javax.swing.event.ChangeEvent e) {
		Object source = e.getSource();

		int max_no_charges_ions = 0;
		max_no_charges_ions = Math.max(max_no_charges_ions,
				(Integer) field_max_no_h_ions.getValue());
		if (!field_negative_mode.isSelected()) {
			max_no_charges_ions = Math.max(max_no_charges_ions,
					(Integer) field_max_no_na_ions.getValue());
			max_no_charges_ions = Math.max(max_no_charges_ions,
					(Integer) field_max_no_li_ions.getValue());
			max_no_charges_ions = Math.max(max_no_charges_ions,
					(Integer) field_max_no_k_ions.getValue());
		}

		if (source == field_max_no_charges) {
			int max_no_charges_new = (Integer) field_max_no_charges.getValue();
			enforceValue(field_max_no_h_ions, max_no_charges_ions,
					max_no_charges_new);
			enforceValue(field_max_no_na_ions, max_no_charges_ions,
					max_no_charges_new);
			enforceValue(field_max_no_li_ions, max_no_charges_ions,
					max_no_charges_new);
			enforceValue(field_max_no_k_ions, max_no_charges_ions,
					max_no_charges_new);
		}
		if (source == field_max_no_h_ions || source == field_max_no_na_ions
				|| source == field_max_no_li_ions
				|| source == field_max_no_k_ions)
			field_max_no_charges.setValue(max_no_charges_ions);
	}

	public void enforceValue(javax.swing.JSpinner field, int old_max_value,
			int new_max_value) {
		int value = (Integer) field.getValue();
		if (value > new_max_value || value == old_max_value)
			field.setValue(new_max_value);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
	private void initComponents() {
		field_bfragments = new javax.swing.JCheckBox();
		field_cfragments = new javax.swing.JCheckBox();
		field_yfragments = new javax.swing.JCheckBox();
		field_zfragments = new javax.swing.JCheckBox();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		button_ok = new javax.swing.JButton();
		button_cancel = new javax.swing.JButton();
		jLabel3 = new javax.swing.JLabel();
		field_afragments = new javax.swing.JCheckBox();
		field_xfragments = new javax.swing.JCheckBox();
		jLabel14 = new javax.swing.JLabel();
		jLabel15 = new javax.swing.JLabel();
		jLabel16 = new javax.swing.JLabel();
		jLabel17 = new javax.swing.JLabel();
		field_accuracy = new javax.swing.JTextField();
		field_accuracy_unit = new javax.swing.JComboBox();
		jLabel18 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		jLabel8 = new javax.swing.JLabel();
		field_compute_exchanges = new javax.swing.JCheckBox();
		jLabel19 = new javax.swing.JLabel();
		field_negative_mode = new javax.swing.JCheckBox();
		jLabel9 = new javax.swing.JLabel();
		field_no_cleavages = new javax.swing.JSpinner();
		field_no_crossrings = new javax.swing.JSpinner();
		field_max_no_charges = new javax.swing.JSpinner();
		field_max_no_h_ions = new javax.swing.JSpinner();
		field_max_no_na_ions = new javax.swing.JSpinner();
		field_max_no_li_ions = new javax.swing.JSpinner();
		field_max_no_k_ions = new javax.swing.JSpinner();
		jLabel20 = new javax.swing.JLabel();
		jLabel21 = new javax.swing.JLabel();
		jLabel22 = new javax.swing.JLabel();
		field_max_ex_na_ions = new javax.swing.JSpinner();
		field_max_ex_li_ions = new javax.swing.JSpinner();
		field_max_ex_k_ions = new javax.swing.JSpinner();
		field_derive_from_parent = new javax.swing.JCheckBox();
		field_internal_fragments = new javax.swing.JCheckBox();
		jSeparator2 = new javax.swing.JSeparator();
		jSeparator1 = new javax.swing.JSeparator();

		setTitle("Fragment options");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog();
			}
		});

		field_bfragments.setText("B fragments");
		field_bfragments.setBorder(javax.swing.BorderFactory.createEmptyBorder(
				0, 0, 0, 0));
		field_bfragments.setMargin(new java.awt.Insets(0, 0, 0, 0));

		field_cfragments.setText("C fragments");
		field_cfragments.setBorder(javax.swing.BorderFactory.createEmptyBorder(
				0, 0, 0, 0));
		field_cfragments.setMargin(new java.awt.Insets(0, 0, 0, 0));

		field_yfragments.setText("Y fragments");
		field_yfragments.setBorder(javax.swing.BorderFactory.createEmptyBorder(
				0, 0, 0, 0));
		field_yfragments.setMargin(new java.awt.Insets(0, 0, 0, 0));

		field_zfragments.setText("Z fragments");
		field_zfragments.setBorder(javax.swing.BorderFactory.createEmptyBorder(
				0, 0, 0, 0));
		field_zfragments.setMargin(new java.awt.Insets(0, 0, 0, 0));

		jLabel1.setText("Fragment types");

		jLabel2.setText("Max n.o. cleavages");

		button_ok.setText("OK");

		button_cancel.setText("Cancel");

		jLabel3.setText("Cross ring fragments");

		field_afragments.setText("A fragments");
		field_afragments.setBorder(javax.swing.BorderFactory.createEmptyBorder(
				0, 0, 0, 0));
		field_afragments.setMargin(new java.awt.Insets(0, 0, 0, 0));

		field_xfragments.setText("X fragments");
		field_xfragments.setBorder(javax.swing.BorderFactory.createEmptyBorder(
				0, 0, 0, 0));
		field_xfragments.setMargin(new java.awt.Insets(0, 0, 0, 0));

		jLabel14.setText("Max # H ions");

		jLabel15.setText("Max # Na ions");

		jLabel16.setText("Max # Li ions");

		jLabel17.setText("Accuracy");

		field_accuracy.setText("9999.9999");

		field_accuracy_unit.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { "ppm", "da" }));

		jLabel18.setText("Max # K ions");

		jLabel4.setFont(new java.awt.Font("Dialog", 2, 12));
		jLabel4.setText("Fragment options");

		jLabel5.setFont(new java.awt.Font("Dialog", 2, 12));
		jLabel5.setText("Mass options");

		jLabel6.setText("Max # charges");

		jLabel8.setText("Max n.o. cross rings");

		field_compute_exchanges.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		field_compute_exchanges.setMargin(new java.awt.Insets(0, 0, 0, 0));

		jLabel19.setText("Neutral exchanges");

		field_negative_mode.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		field_negative_mode.setMargin(new java.awt.Insets(0, 0, 0, 0));

		jLabel9.setText("Negative mode");

		jLabel20.setText("Max ex. Na ions");

		jLabel21.setText("Max ex. Li ions");

		jLabel22.setText("Max ex K ions");

		field_derive_from_parent.setText("Derive options from parent ion");
		field_derive_from_parent.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		field_derive_from_parent.setMargin(new java.awt.Insets(0, 0, 0, 0));

		field_internal_fragments.setText("Internal fragments");
		field_internal_fragments.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		field_internal_fragments.setMargin(new java.awt.Insets(0, 0, 0, 0));

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				this.getContentPane());
		this.getContentPane().setLayout(layout);
		layout
				.setHorizontalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								layout
										.createSequentialGroup()
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																layout
																		.createSequentialGroup()
																		.addContainerGap()
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(
																								jSeparator1,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								362,
																								Short.MAX_VALUE)
																						.add(
																								jLabel4)
																						.add(
																								layout
																										.createSequentialGroup()
																										.add(
																												12,
																												12,
																												12)
																										.add(
																												layout
																														.createParallelGroup(
																																org.jdesktop.layout.GroupLayout.LEADING)
																														.add(
																																jLabel1)
																														.add(
																																jLabel3)
																														.add(
																																jLabel2)
																														.add(
																																jLabel8))
																										.addPreferredGap(
																												org.jdesktop.layout.LayoutStyle.RELATED)
																										.add(
																												layout
																														.createParallelGroup(
																																org.jdesktop.layout.GroupLayout.LEADING)
																														.add(
																																field_bfragments)
																														.add(
																																layout
																																		.createParallelGroup(
																																				org.jdesktop.layout.GroupLayout.TRAILING,
																																				false)
																																		.add(
																																				org.jdesktop.layout.GroupLayout.LEADING,
																																				field_xfragments,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				Short.MAX_VALUE)
																																		.add(
																																				org.jdesktop.layout.GroupLayout.LEADING,
																																				field_afragments,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				Short.MAX_VALUE)
																																		.add(
																																				org.jdesktop.layout.GroupLayout.LEADING,
																																				field_zfragments,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				Short.MAX_VALUE)
																																		.add(
																																				org.jdesktop.layout.GroupLayout.LEADING,
																																				field_yfragments,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				Short.MAX_VALUE)
																																		.add(
																																				org.jdesktop.layout.GroupLayout.LEADING,
																																				field_cfragments,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				Short.MAX_VALUE))
																														.add(
																																field_internal_fragments)
																														.add(
																																layout
																																		.createParallelGroup(
																																				org.jdesktop.layout.GroupLayout.TRAILING,
																																				false)
																																		.add(
																																				org.jdesktop.layout.GroupLayout.LEADING,
																																				field_no_crossrings)
																																		.add(
																																				org.jdesktop.layout.GroupLayout.LEADING,
																																				field_no_cleavages,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				50,
																																				Short.MAX_VALUE))))
																						.add(
																								jLabel5)
																						.add(
																								layout
																										.createParallelGroup(
																												org.jdesktop.layout.GroupLayout.TRAILING,
																												false)
																										.add(
																												org.jdesktop.layout.GroupLayout.LEADING,
																												jSeparator2)
																										.add(
																												org.jdesktop.layout.GroupLayout.LEADING,
																												layout
																														.createSequentialGroup()
																														.add(
																																12,
																																12,
																																12)
																														.add(
																																layout
																																		.createParallelGroup(
																																				org.jdesktop.layout.GroupLayout.LEADING)
																																		.add(
																																				jLabel9)
																																		.add(
																																				jLabel14)
																																		.add(
																																				jLabel15)
																																		.add(
																																				jLabel16)
																																		.add(
																																				jLabel18)
																																		.add(
																																				jLabel6)
																																		.add(
																																				jLabel17))
																														.addPreferredGap(
																																org.jdesktop.layout.LayoutStyle.RELATED)
																														.add(
																																layout
																																		.createParallelGroup(
																																				org.jdesktop.layout.GroupLayout.LEADING)
																																		.add(
																																				layout
																																						.createSequentialGroup()
																																						.add(
																																								layout
																																										.createParallelGroup(
																																												org.jdesktop.layout.GroupLayout.LEADING,
																																												false)
																																										.add(
																																												field_max_no_k_ions)
																																										.add(
																																												field_max_no_li_ions)
																																										.add(
																																												field_max_no_na_ions)
																																										.add(
																																												field_max_no_charges)
																																										.add(
																																												field_negative_mode)
																																										.add(
																																												field_max_no_h_ions,
																																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																												51,
																																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
																																						.addPreferredGap(
																																								org.jdesktop.layout.LayoutStyle.RELATED)
																																						.add(
																																								layout
																																										.createParallelGroup(
																																												org.jdesktop.layout.GroupLayout.LEADING)
																																										.add(
																																												layout
																																														.createSequentialGroup()
																																														.add(
																																																layout
																																																		.createParallelGroup(
																																																				org.jdesktop.layout.GroupLayout.LEADING)
																																																		.add(
																																																				jLabel20)
																																																		.add(
																																																				jLabel21)
																																																		.add(
																																																				jLabel22))
																																														.add(
																																																29,
																																																29,
																																																29)
																																														.add(
																																																layout
																																																		.createParallelGroup(
																																																				org.jdesktop.layout.GroupLayout.LEADING)
																																																		.add(
																																																				field_max_ex_na_ions,
																																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																																				56,
																																																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																																																		.add(
																																																				layout
																																																						.createSequentialGroup()
																																																						.addPreferredGap(
																																																								org.jdesktop.layout.LayoutStyle.RELATED)
																																																						.add(
																																																								layout
																																																										.createParallelGroup(
																																																												org.jdesktop.layout.GroupLayout.LEADING)
																																																										.add(
																																																												field_max_ex_k_ions,
																																																												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																																												56,
																																																												Short.MAX_VALUE)
																																																										.add(
																																																												field_max_ex_li_ions,
																																																												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																																												56,
																																																												Short.MAX_VALUE)))))
																																										.add(
																																												layout
																																														.createSequentialGroup()
																																														.add(
																																																jLabel19)
																																														.addPreferredGap(
																																																org.jdesktop.layout.LayoutStyle.RELATED)
																																														.add(
																																																field_compute_exchanges))))
																																		.add(
																																				layout
																																						.createParallelGroup(
																																								org.jdesktop.layout.GroupLayout.TRAILING,
																																								false)
																																						.add(
																																								layout
																																										.createSequentialGroup()
																																										.add(
																																												field_accuracy)
																																										.addPreferredGap(
																																												org.jdesktop.layout.LayoutStyle.RELATED)
																																										.add(
																																												field_accuracy_unit,
																																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																																												60,
																																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
																																						.add(
																																								org.jdesktop.layout.GroupLayout.LEADING,
																																								field_derive_from_parent)))))))
														.add(
																layout
																		.createSequentialGroup()
																		.add(
																				100,
																				100,
																				100)
																		.add(
																				button_ok)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				button_cancel)))
										.addContainerGap()));

		layout.linkSize(new java.awt.Component[] { button_cancel, button_ok },
				org.jdesktop.layout.GroupLayout.HORIZONTAL);

		layout
				.setVerticalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								layout
										.createSequentialGroup()
										.addContainerGap()
										.add(jLabel4)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel1)
														.add(field_bfragments))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(field_cfragments)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(field_yfragments)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(field_zfragments)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel3)
														.add(
																field_afragments,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																15,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(field_xfragments)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(field_internal_fragments)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel2)
														.add(
																field_no_cleavages,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel8)
														.add(
																field_no_crossrings,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(jLabel5)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jSeparator1,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												10,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel9)
														.add(
																field_negative_mode)
														.add(jLabel19)
														.add(
																field_compute_exchanges))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel14)
														.add(
																field_max_no_h_ions,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel15)
														.add(
																field_max_no_na_ions,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(jLabel20)
														.add(
																field_max_ex_na_ions,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel16)
														.add(
																field_max_no_li_ions,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(jLabel21)
														.add(
																field_max_ex_li_ions,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel18)
														.add(
																field_max_no_k_ions,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(jLabel22)
														.add(
																field_max_ex_k_ions,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel6)
														.add(
																field_max_no_charges,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												field_derive_from_parent,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												15,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel17)
														.add(
																field_accuracy_unit,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																field_accuracy,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																27,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												jSeparator2,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												10,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(button_ok).add(
																button_cancel))
										.addContainerGap()));
		
		pack();
	}// </editor-fold>

	/** Closes the dialog */
	private void closeDialog() {// GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
	}// GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton button_cancel;
	private javax.swing.JButton button_ok;
	private javax.swing.JTextField field_accuracy;
	private javax.swing.JComboBox field_accuracy_unit;
	private javax.swing.JCheckBox field_afragments;
	private javax.swing.JCheckBox field_bfragments;
	private javax.swing.JCheckBox field_cfragments;
	private javax.swing.JCheckBox field_compute_exchanges;
	private javax.swing.JCheckBox field_derive_from_parent;
	private javax.swing.JCheckBox field_internal_fragments;
	private javax.swing.JSpinner field_max_ex_k_ions;
	private javax.swing.JSpinner field_max_ex_li_ions;
	private javax.swing.JSpinner field_max_ex_na_ions;
	private javax.swing.JSpinner field_max_no_charges;
	private javax.swing.JSpinner field_max_no_h_ions;
	private javax.swing.JSpinner field_max_no_k_ions;
	private javax.swing.JSpinner field_max_no_li_ions;
	private javax.swing.JSpinner field_max_no_na_ions;
	private javax.swing.JCheckBox field_negative_mode;
	private javax.swing.JSpinner field_no_cleavages;
	private javax.swing.JSpinner field_no_crossrings;
	private javax.swing.JCheckBox field_xfragments;
	private javax.swing.JCheckBox field_yfragments;
	private javax.swing.JCheckBox field_zfragments;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel14;
	private javax.swing.JLabel jLabel15;
	private javax.swing.JLabel jLabel16;
	private javax.swing.JLabel jLabel17;
	private javax.swing.JLabel jLabel18;
	private javax.swing.JLabel jLabel19;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel20;
	private javax.swing.JLabel jLabel21;
	private javax.swing.JLabel jLabel22;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JLabel jLabel9;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSeparator jSeparator2;
	// End of variables declaration//GEN-END:variables

}
