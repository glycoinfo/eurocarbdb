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
*
* @author  aceroni
*/

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

public class PeakFinderOptionsDialog extends EscapeDialog implements ActionListener, ChangeListener {
    
    private PeakFinderOptions theOptions;
  
    public PeakFinderOptionsDialog(java.awt.Frame parent, PeakFinderOptions opt) {
        super(parent, true);

    theOptions =  opt;

        initComponents();
    setTraversal();
    setData();    
    setActions();
    enableItems();

    // set location
    setLocationRelativeTo(parent);
    	pack();
    }

    private void setTraversal() {
    CustomFocusTraversalPolicy tp = new CustomFocusTraversalPolicy();
    
    tp.addComponent(field_derivatization);
    tp.addComponent(field_reducingend);
    tp.addComponent(field_other_redend_name);
    tp.addComponent(field_other_redend_mass);

    tp.addComponent(field_min_pen);
    tp.addComponent(field_max_pen);
    tp.addComponent(field_min_hex);
    tp.addComponent(field_max_hex);
    tp.addComponent(field_min_hep);
    tp.addComponent(field_max_hep);
    tp.addComponent(field_min_hexn);
    tp.addComponent(field_max_hexn);
    tp.addComponent(field_min_hexnac);
    tp.addComponent(field_max_hexnac);
    tp.addComponent(field_min_dpen);
    tp.addComponent(field_max_dpen);
    tp.addComponent(field_min_dhex);
    tp.addComponent(field_max_dhex);
    tp.addComponent(field_min_ddhex);
    tp.addComponent(field_max_ddhex);
    tp.addComponent(field_min_mehex);
    tp.addComponent(field_max_mehex);

    tp.addComponent(field_min_or1);
    tp.addComponent(field_max_or1);
    tp.addComponent(field_min_or2);
    tp.addComponent(field_max_or2);
    tp.addComponent(field_min_or3);
    tp.addComponent(field_max_or3);
    tp.addComponent(field_or1_name);
    tp.addComponent(field_or1_mass);    
    tp.addComponent(field_or2_name);
    tp.addComponent(field_or2_mass);
    tp.addComponent(field_or3_name);
    tp.addComponent(field_or3_mass);

    tp.addComponent(field_min_hexa);
    tp.addComponent(field_max_hexa);
    tp.addComponent(field_min_dhexa);
    tp.addComponent(field_max_dhexa);
    tp.addComponent(field_min_neu5gc);
    tp.addComponent(field_max_neu5gc);
    tp.addComponent(field_min_neu5ac);
    tp.addComponent(field_max_neu5ac);
    tp.addComponent(field_min_neu5gclac);
    tp.addComponent(field_max_neu5gclac);
    tp.addComponent(field_min_neu5aclac);
    tp.addComponent(field_max_neu5aclac);
    tp.addComponent(field_min_kdo);
    tp.addComponent(field_max_kdo);
    tp.addComponent(field_min_kdn);
    tp.addComponent(field_max_kdn);
    tp.addComponent(field_min_mur);
    tp.addComponent(field_max_mur);

    tp.addComponent(field_min_s);
    tp.addComponent(field_max_s);
    tp.addComponent(field_min_p);
    tp.addComponent(field_max_p);
    tp.addComponent(field_min_ac);
    tp.addComponent(field_max_ac);
    tp.addComponent(field_min_pyr);
    tp.addComponent(field_max_pyr);
    tp.addComponent(field_min_pc);
    tp.addComponent(field_max_pc);

    tp.addComponent(button_ok);
    tp.addComponent(button_cancel);

    this.setFocusTraversalPolicy(tp);

    getRootPane().setDefaultButton(button_ok); 
    }

    private void setData() {    
    
    field_derivatization.setModel(new DefaultComboBoxModel(new String[] {"Und", "perMe", "perAc", "perDMe", "perDAc"}));
    field_derivatization.setSelectedItem(theOptions.DERIVATIZATION);
    
    field_reducingend.setModel(new javax.swing.DefaultComboBoxModel(new Union<String>().and("---").and(ResidueDictionary.getReducingEndsString()).and("Other...").toArray(new String[0])));
    field_reducingend.setSelectedItem(theOptions.REDUCING_END);

    if( theOptions.REDUCING_END.equals("XXX") ) {
        field_reducingend.setSelectedItem("Other...");       
        field_other_redend_name.setText(theOptions.OTHER_REDEND_NAME);
        field_other_redend_mass.setText("" + theOptions.OTHER_REDEND_MASS);
    }
    else {
        field_reducingend.setSelectedItem(theOptions.REDUCING_END);
        field_other_redend_name.setText("");
        field_other_redend_mass.setText("0");
    }

    field_min_pen.setModel(new SpinnerNumberModel(theOptions.MIN_PEN,0,99,1));
    field_max_pen.setModel(new SpinnerNumberModel(theOptions.MAX_PEN,0,99,1));
    field_min_hex.setModel(new SpinnerNumberModel(theOptions.MIN_HEX,0,99,1));
    field_max_hex.setModel(new SpinnerNumberModel(theOptions.MAX_HEX,0,99,1));
    field_min_hep.setModel(new SpinnerNumberModel(theOptions.MIN_HEP,0,99,1));
    field_max_hep.setModel(new SpinnerNumberModel(theOptions.MAX_HEP,0,99,1));
    field_min_hexn.setModel(new SpinnerNumberModel(theOptions.MIN_HEXN,0,99,1));
    field_max_hexn.setModel(new SpinnerNumberModel(theOptions.MAX_HEXN,0,99,1));
    field_min_hexnac.setModel(new SpinnerNumberModel(theOptions.MIN_HEXNAC,0,99,1));
    field_max_hexnac.setModel(new SpinnerNumberModel(theOptions.MAX_HEXNAC,0,99,1));
    field_min_dpen.setModel(new SpinnerNumberModel(theOptions.MIN_DPEN,0,99,1));
    field_max_dpen.setModel(new SpinnerNumberModel(theOptions.MAX_DPEN,0,99,1));
    field_min_dhex.setModel(new SpinnerNumberModel(theOptions.MIN_DHEX,0,99,1));
    field_max_dhex.setModel(new SpinnerNumberModel(theOptions.MAX_DHEX,0,99,1));
    field_min_ddhex.setModel(new SpinnerNumberModel(theOptions.MIN_DDHEX,0,99,1));
    field_max_ddhex.setModel(new SpinnerNumberModel(theOptions.MAX_DDHEX,0,99,1));
    field_min_mehex.setModel(new SpinnerNumberModel(theOptions.MIN_MEHEX,0,99,1));
    field_max_mehex.setModel(new SpinnerNumberModel(theOptions.MAX_MEHEX,0,99,1));

    field_min_or1.setModel(new SpinnerNumberModel(theOptions.MIN_OR1,0,99,1));
    field_max_or1.setModel(new SpinnerNumberModel(theOptions.MAX_OR1,0,99,1));
    field_min_or2.setModel(new SpinnerNumberModel(theOptions.MIN_OR2,0,99,1));
    field_max_or2.setModel(new SpinnerNumberModel(theOptions.MAX_OR2,0,99,1));
    field_min_or3.setModel(new SpinnerNumberModel(theOptions.MIN_OR3,0,99,1));
    field_max_or3.setModel(new SpinnerNumberModel(theOptions.MAX_OR3,0,99,1));
    field_or1_name.setText("" + theOptions.OR1_NAME);
    field_or2_name.setText("" + theOptions.OR2_NAME);
    field_or3_name.setText("" + theOptions.OR3_NAME);
    field_or1_mass.setText("" + theOptions.OR1_MASS);
    field_or2_mass.setText("" + theOptions.OR2_MASS);
    field_or3_mass.setText("" + theOptions.OR3_MASS);

    field_min_hexa.setModel(new SpinnerNumberModel(theOptions.MIN_HEXA,0,99,1));
    field_max_hexa.setModel(new SpinnerNumberModel(theOptions.MAX_HEXA,0,99,1));
    field_min_dhexa.setModel(new SpinnerNumberModel(theOptions.MIN_DHEXA,0,99,1));
    field_max_dhexa.setModel(new SpinnerNumberModel(theOptions.MAX_DHEXA,0,99,1));
    field_min_neu5gc.setModel(new SpinnerNumberModel(theOptions.MIN_NEU5GC,0,99,1));
    field_max_neu5gc.setModel(new SpinnerNumberModel(theOptions.MAX_NEU5GC,0,99,1));
    field_min_neu5ac.setModel(new SpinnerNumberModel(theOptions.MIN_NEU5AC,0,99,1));
    field_max_neu5ac.setModel(new SpinnerNumberModel(theOptions.MAX_NEU5AC,0,99,1));
    field_min_neu5gclac.setModel(new SpinnerNumberModel(theOptions.MIN_NEU5GCLAC,0,99,1));
    field_max_neu5gclac.setModel(new SpinnerNumberModel(theOptions.MAX_NEU5GCLAC,0,99,1));
    field_min_neu5aclac.setModel(new SpinnerNumberModel(theOptions.MIN_NEU5ACLAC,0,99,1));
    field_max_neu5aclac.setModel(new SpinnerNumberModel(theOptions.MAX_NEU5ACLAC,0,99,1));
    field_min_kdo.setModel(new SpinnerNumberModel(theOptions.MIN_KDO,0,99,1));
    field_max_kdo.setModel(new SpinnerNumberModel(theOptions.MAX_KDO,0,99,1));
    field_min_kdn.setModel(new SpinnerNumberModel(theOptions.MIN_KDN,0,99,1));
    field_max_kdn.setModel(new SpinnerNumberModel(theOptions.MAX_KDN,0,99,1));
    field_min_mur.setModel(new SpinnerNumberModel(theOptions.MIN_MUR,0,99,1));
    field_max_mur.setModel(new SpinnerNumberModel(theOptions.MAX_MUR,0,99,1));

    field_min_s.setModel(new SpinnerNumberModel(theOptions.MIN_S,0,99,1));
    field_max_s.setModel(new SpinnerNumberModel(theOptions.MAX_S,0,99,1));
    field_min_p.setModel(new SpinnerNumberModel(theOptions.MIN_P,0,99,1));
    field_max_p.setModel(new SpinnerNumberModel(theOptions.MAX_P,0,99,1));
    field_min_ac.setModel(new SpinnerNumberModel(theOptions.MIN_AC,0,99,1));
    field_max_ac.setModel(new SpinnerNumberModel(theOptions.MAX_AC,0,99,1));
    field_min_pyr.setModel(new SpinnerNumberModel(theOptions.MIN_PYR,0,99,1));
    field_max_pyr.setModel(new SpinnerNumberModel(theOptions.MAX_PYR,0,99,1));
    field_min_pc.setModel(new SpinnerNumberModel(theOptions.MIN_PC,0,99,1));
    field_max_pc.setModel(new SpinnerNumberModel(theOptions.MAX_PC,0,99,1));
    }

    private void setActions() {
    button_ok.addActionListener(this);
    button_cancel.addActionListener(this);
    field_reducingend.addActionListener(this);
    field_max_or1.addChangeListener(this);
    field_max_or2.addChangeListener(this);
    field_max_or3.addChangeListener(this);
    }

    private void enableItems() {
    field_other_redend_name.setEnabled(field_reducingend.getSelectedItem().equals("Other..."));
    field_other_redend_mass.setEnabled(field_reducingend.getSelectedItem().equals("Other..."));
    field_or1_name.setEnabled(((Integer)field_max_or1.getValue())>0);
    field_or2_name.setEnabled(((Integer)field_max_or2.getValue())>0);
    field_or3_name.setEnabled(((Integer)field_max_or3.getValue())>0);
    field_or1_mass.setEnabled(((Integer)field_max_or1.getValue())>0);
    field_or2_mass.setEnabled(((Integer)field_max_or2.getValue())>0);
    field_or3_mass.setEnabled(((Integer)field_max_or3.getValue())>0);
    }

    
    private boolean retrieveData() {    

    if( ResidueDictionary.findResidueType(field_or1_name.getText())!=null ) {
        JOptionPane.showMessageDialog(this,"Invalid residue name", "The identifier " + field_or1_name.getText() + " is already in use.", JOptionPane.ERROR_MESSAGE);
        return false;
    }
    else if( ResidueDictionary.findResidueType(field_or2_name.getText())!=null ) {
        JOptionPane.showMessageDialog(this,"Invalid residue name", "The identifier " + field_or2_name.getText() + " is already in use.", JOptionPane.ERROR_MESSAGE);
        return false;
    }
    else if( ResidueDictionary.findResidueType(field_or3_name.getText())!=null ) {        
        JOptionPane.showMessageDialog(this,"Invalid residue name", "The identifier " + field_or3_name.getText() + " is already in use.", JOptionPane.ERROR_MESSAGE);
        return false;
    }        

    theOptions.DERIVATIZATION = (String)field_derivatization.getSelectedItem();
    if( field_reducingend.getSelectedItem().equals("Other...") ) {
        theOptions.REDUCING_END = "XXX";
        theOptions.OTHER_REDEND_NAME = TextUtils.trim(field_other_redend_name.getText());
        theOptions.OTHER_REDEND_MASS = Double.valueOf(field_other_redend_mass.getText());
        
        if( theOptions.OTHER_REDEND_NAME==null || theOptions.OTHER_REDEND_NAME.length()==0 )
        theOptions.OTHER_REDEND_NAME = "XXX";
    }
    else 
        theOptions.REDUCING_END = (String)field_reducingend.getSelectedItem();

    theOptions.MIN_PEN = (Integer)field_min_pen.getValue();
    theOptions.MAX_PEN = (Integer)field_max_pen.getValue();
    theOptions.MIN_HEX = (Integer)field_min_hex.getValue();
    theOptions.MAX_HEX = (Integer)field_max_hex.getValue();
    theOptions.MIN_HEP = (Integer)field_min_hep.getValue();
    theOptions.MAX_HEP = (Integer)field_max_hep.getValue();
    theOptions.MIN_HEXN = (Integer)field_min_hexn.getValue();
    theOptions.MAX_HEXN = (Integer)field_max_hexn.getValue();
    theOptions.MIN_HEXNAC = (Integer)field_min_hexnac.getValue();
    theOptions.MAX_HEXNAC = (Integer)field_max_hexnac.getValue();
    theOptions.MIN_DPEN = (Integer)field_min_dpen.getValue();
    theOptions.MAX_DPEN = (Integer)field_max_dpen.getValue();
    theOptions.MIN_DHEX = (Integer)field_min_dhex.getValue();
    theOptions.MAX_DHEX = (Integer)field_max_dhex.getValue();
    theOptions.MIN_DDHEX = (Integer)field_min_ddhex.getValue();
    theOptions.MAX_DDHEX = (Integer)field_max_ddhex.getValue();
    theOptions.MIN_MEHEX = (Integer)field_min_mehex.getValue();
    theOptions.MAX_MEHEX = (Integer)field_max_mehex.getValue();

    theOptions.MIN_OR1 = (Integer)field_min_or1.getValue();
    theOptions.MAX_OR1 = (Integer)field_max_or1.getValue();
    theOptions.MIN_OR2 = (Integer)field_min_or2.getValue();
    theOptions.MAX_OR2 = (Integer)field_max_or2.getValue();
    theOptions.MIN_OR3 = (Integer)field_min_or3.getValue();
    theOptions.MAX_OR3 = (Integer)field_max_or3.getValue();
    theOptions.OR1_NAME = (field_or1_name.getText()==null || field_or1_name.getText().length()==0) ?"Or1" :field_or1_name.getText(); 
    theOptions.OR2_NAME = (field_or2_name.getText()==null || field_or2_name.getText().length()==0) ?"Or2" :field_or2_name.getText(); 
    theOptions.OR3_NAME = (field_or3_name.getText()==null || field_or3_name.getText().length()==0) ?"Or3" :field_or3_name.getText(); 
    theOptions.OR1_MASS = Double.valueOf(field_or1_mass.getText());
    theOptions.OR2_MASS = Double.valueOf(field_or2_mass.getText());
    theOptions.OR3_MASS = Double.valueOf(field_or3_mass.getText());

    theOptions.MIN_HEXA = (Integer)field_min_hexa.getValue();
    theOptions.MAX_HEXA = (Integer)field_max_hexa.getValue();
    theOptions.MIN_DHEXA = (Integer)field_min_dhexa.getValue();
    theOptions.MAX_DHEXA = (Integer)field_max_dhexa.getValue();
    theOptions.MIN_NEU5GC = (Integer)field_min_neu5gc.getValue();
    theOptions.MAX_NEU5GC = (Integer)field_max_neu5gc.getValue();
    theOptions.MIN_NEU5AC = (Integer)field_min_neu5ac.getValue();
    theOptions.MAX_NEU5AC = (Integer)field_max_neu5ac.getValue();
    theOptions.MIN_NEU5GCLAC = (Integer)field_min_neu5gclac.getValue();
    theOptions.MAX_NEU5GCLAC = (Integer)field_max_neu5gclac.getValue();
    theOptions.MIN_NEU5ACLAC = (Integer)field_min_neu5aclac.getValue();
    theOptions.MAX_NEU5ACLAC = (Integer)field_max_neu5aclac.getValue();
    theOptions.MIN_KDO = (Integer)field_min_kdo.getValue();
    theOptions.MAX_KDO = (Integer)field_max_kdo.getValue();
    theOptions.MIN_KDN = (Integer)field_min_kdn.getValue();
    theOptions.MAX_KDN = (Integer)field_max_kdn.getValue();
    theOptions.MIN_MUR = (Integer)field_min_mur.getValue();
    theOptions.MAX_MUR = (Integer)field_max_mur.getValue();

    theOptions.MIN_S = (Integer)field_min_s.getValue();
    theOptions.MAX_S = (Integer)field_max_s.getValue();
    theOptions.MIN_P = (Integer)field_min_p.getValue();
    theOptions.MAX_P = (Integer)field_max_p.getValue();
    theOptions.MIN_AC = (Integer)field_min_ac.getValue();
    theOptions.MAX_AC = (Integer)field_max_ac.getValue();
    theOptions.MIN_PYR = (Integer)field_min_pyr.getValue();
    theOptions.MAX_PYR = (Integer)field_max_pyr.getValue();
    theOptions.MIN_PC = (Integer)field_min_pc.getValue();
    theOptions.MAX_PC = (Integer)field_max_pc.getValue();

    return true;
    }

    public void stateChanged(ChangeEvent e) {
    enableItems();
    }

    public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    
    if (action == "OK") {
        if( retrieveData() ) {
        return_status = action;
        closeDialog();
        }
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
       jLabel3 = new JLabel();
        field_derivatization = new JComboBox();
        jLabel12 = new JLabel();
        field_reducingend = new JComboBox();
        jLabel1 = new JLabel();
        field_other_redend_mass = new JTextField();
        jSeparator1 = new JSeparator();
        jLabel4 = new JLabel();
        jLabel6 = new JLabel();
        jLabel7 = new JLabel();
        jLabel8 = new JLabel();
        field_min_pen = new JSpinner();
        field_max_pen = new JSpinner();
        jLabel9 = new JLabel();
        jLabel10 = new JLabel();
        field_min_hex = new JSpinner();
        field_max_hex = new JSpinner();
        jLabel11 = new JLabel();
        jLabel13 = new JLabel();
        field_min_hep = new JSpinner();
        field_max_hep = new JSpinner();
        jLabel14 = new JLabel();
        jLabel15 = new JLabel();
        field_min_hexn = new JSpinner();
        field_max_hexn = new JSpinner();
        jLabel16 = new JLabel();
        jLabel2 = new JLabel();
        field_min_hexnac = new JSpinner();
        field_max_hexnac = new JSpinner();
        jLabel17 = new JLabel();
        jLabel18 = new JLabel();
        field_min_dpen = new JSpinner();
        field_max_dpen = new JSpinner();
        jLabel19 = new JLabel();
        jLabel20 = new JLabel();
        field_min_dhex = new JSpinner();
        field_max_dhex = new JSpinner();
        jLabel21 = new JLabel();
        jLabel22 = new JLabel();
        field_min_ddhex = new JSpinner();
        field_max_ddhex = new JSpinner();
        jLabel23 = new JLabel();
        jLabel24 = new JLabel();
        field_min_hexa = new JSpinner();
        field_max_hexa = new JSpinner();
        jLabel26 = new JLabel();
        jLabel27 = new JLabel();
        jLabel28 = new JLabel();
        jLabel29 = new JLabel();
        field_min_dhexa = new JSpinner();
        field_max_dhexa = new JSpinner();
        jLabel30 = new JLabel();
        jLabel31 = new JLabel();
        field_min_neu5gc = new JSpinner();
        field_max_neu5gc = new JSpinner();
        jLabel32 = new JLabel();
        jLabel33 = new JLabel();
        field_min_neu5ac = new JSpinner();
        field_max_neu5ac = new JSpinner();
        jLabel34 = new JLabel();
        jLabel35 = new JLabel();
        field_min_neu5gclac = new JSpinner();
        field_max_neu5gclac = new JSpinner();
        jLabel36 = new JLabel();
        jLabel37 = new JLabel();
        field_min_neu5aclac = new JSpinner();
        field_max_neu5aclac = new JSpinner();
        jLabel38 = new JLabel();
        jLabel39 = new JLabel();
        field_min_mehex = new JSpinner();
        field_max_mehex = new JSpinner();
        jLabel40 = new JLabel();
        jLabel41 = new JLabel();
        field_min_kdo = new JSpinner();
        field_max_kdo = new JSpinner();
        jLabel42 = new JLabel();
        jLabel43 = new JLabel();
        field_min_kdn = new JSpinner();
        field_max_kdn = new JSpinner();
        jLabel44 = new JLabel();
        jLabel45 = new JLabel();
        field_min_mur = new JSpinner();
        field_max_mur = new JSpinner();
        jLabel46 = new JLabel();
        jLabel47 = new JLabel();
        field_min_s = new JSpinner();
        field_max_s = new JSpinner();
        jSeparator2 = new JSeparator();
        button_ok = new JButton();
        button_cancel = new JButton();
        jLabel48 = new JLabel();
        jLabel49 = new JLabel();
        field_min_or1 = new JSpinner();
        field_max_or1 = new JSpinner();
        jLabel50 = new JLabel();
        jLabel51 = new JLabel();
        field_min_or2 = new JSpinner();
        field_max_or2 = new JSpinner();
        jLabel52 = new JLabel();
        jLabel53 = new JLabel();
        field_min_or3 = new JSpinner();
        field_max_or3 = new JSpinner();
        jLabel54 = new JLabel();
        jLabel55 = new JLabel();
        jLabel56 = new JLabel();
        jLabel57 = new JLabel();
        jLabel58 = new JLabel();
        jLabel59 = new JLabel();
        jLabel60 = new JLabel();
        jLabel61 = new JLabel();
        field_min_p = new JSpinner();
        field_min_ac = new JSpinner();
        field_min_pyr = new JSpinner();
        field_min_pc = new JSpinner();
        field_max_p = new JSpinner();
        field_max_ac = new JSpinner();
        field_max_pyr = new JSpinner();
        field_max_pc = new JSpinner();
        jLabel62 = new JLabel();
        field_or1_mass = new JTextField();
        jLabel63 = new JLabel();
        field_or2_mass = new JTextField();
        jLabel64 = new JLabel();
        field_or3_mass = new JTextField();
        jLabel5 = new JLabel();
        field_other_redend_name = new JTextField();
        jLabel65 = new JLabel();
        field_or1_name = new JTextField();
        field_or2_name = new JTextField();
        jLabel66 = new JLabel();
        field_or3_name = new JTextField();
        jLabel67 = new JLabel();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeDialog();
            }
        });

        jLabel3.setText("Derivatization");

        field_derivatization.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel12.setText("Reducing end");

        field_reducingend.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("mass");

        field_other_redend_mass.setText("jTextField1");
        field_other_redend_mass.setMinimumSize(new java.awt.Dimension(84, 19));

        jLabel4.setText("Pentose");

        jLabel6.setText("Min");

        jLabel7.setText("Max");

        jLabel8.setText("Pen");

        jLabel9.setText("Hexose");

        jLabel10.setText("Hex");

        jLabel11.setText("Heptose");

        jLabel13.setText("Hep");

        jLabel14.setText("Hexosamine");

        jLabel15.setText("HexN");

        jLabel16.setText("N-Acetyl Hexosamine");

        jLabel2.setText("HexNAc");

        jLabel17.setText("Deoxy-Pentose");

        jLabel18.setText("dPen");

        jLabel19.setText("Deoxy-Hexose");

        jLabel20.setText("dHex");

        jLabel21.setText("DiDeoxy-Hexose");

        jLabel22.setText("ddHex");

        jLabel23.setText("Hexuronic Acid");

        jLabel24.setText("HexA");

        jLabel26.setText("Min");

        jLabel27.setText("Max");

        jLabel28.setText("Dehydro Hexuronic Acid");

        jLabel29.setText("dHexA");

        jLabel30.setText("N-Glycolyl Neuraminic Acid");

        jLabel31.setText("Neu5Gc");

        jLabel32.setText("N-Acetyl Neuraminic acid");

        jLabel33.setText("Neu5Ac");

        jLabel34.setText("Lactonized Neu5Gc");

        jLabel35.setText("Neu5Gc-Lac");

        jLabel36.setText("Lactonized Neu5Ac");

        jLabel37.setText("Neu5Ac-Lac");

        jLabel38.setText("Methyl-Hexose");

        jLabel39.setText("MeHex");

        jLabel40.setText("KDO");

        jLabel41.setText("KDO");

        jLabel42.setText("KDN");

        jLabel43.setText("KDN");

        jLabel44.setText("Muramic Acid");

        jLabel45.setText("Mur");

        jLabel46.setText("Solphate");

        jLabel47.setText("S");

        button_ok.setText("OK");

        button_cancel.setText("Cancel");

        jLabel48.setText("Other residue");

        jLabel49.setText("Or1");

        jLabel50.setText("Other residue");

        jLabel51.setText("Or2");

        jLabel52.setText("Other residue");

        jLabel53.setText("Or3");

        jLabel54.setText("Phosphate");

        jLabel55.setText("Acetate");

        jLabel56.setText("Pyruvate");

        jLabel57.setText("Phosphocholine");

        jLabel58.setText("P");

        jLabel59.setText("Ac");

        jLabel60.setText("Pyr");

        jLabel61.setText("PC");

        jLabel62.setText("mass");

        field_or1_mass.setText("jTextField1");

        jLabel63.setText("mass");

        field_or2_mass.setText("jTextField1");

        jLabel64.setText("mass");

        field_or3_mass.setText("jTextField1");

        jLabel5.setText("name");

        field_other_redend_name.setText("jTextField1");
        field_other_redend_name.setMinimumSize(new java.awt.Dimension(84, 19));

        jLabel65.setText("Or1 name");

        field_or1_name.setText("jTextField1");

        field_or2_name.setText("jTextField1");

        jLabel66.setText("Or2 name");

        field_or3_name.setText("jTextField1");

        jLabel67.setText("Or3 name");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(jLabel12)
                                .add(jLabel1)
                                .add(jLabel5)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(field_other_redend_mass, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(field_other_redend_name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(field_reducingend, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(field_derivatization, 0, 80, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(293, 293, 293)
                        .add(button_ok)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(button_cancel))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(layout.createSequentialGroup()
                                                .add(jLabel67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(field_or3_name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(layout.createSequentialGroup()
                                                .add(jLabel66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(field_or2_name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel9)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel4)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel11)
                                            .add(jLabel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 158, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel17)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel19)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                                .add(jLabel65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(field_or1_name, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabel8)
                                            .add(jLabel10)
                                            .add(jLabel13)
                                            .add(jLabel15)
                                            .add(jLabel2)
                                            .add(jLabel18)
                                            .add(jLabel20)
                                            .add(jLabel22)
                                            .add(jLabel39)
                                            .add(jLabel49)
                                            .add(jLabel51)
                                            .add(jLabel53)
                                            .add(jLabel62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jLabel63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jLabel64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                    .add(jLabel21)
                                    .add(jLabel38)
                                    .add(jLabel48))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(layout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(field_or3_mass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(27, 27, 27)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(layout.createSequentialGroup()
                                                .add(field_min_or3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                                .add(field_max_or3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(layout.createSequentialGroup()
                                                .add(field_min_or2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                                .add(field_max_or2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(layout.createSequentialGroup()
                                                .add(field_min_or1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                                .add(field_max_or1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(layout.createSequentialGroup()
                                                .add(field_min_mehex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                                .add(field_max_mehex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(layout.createSequentialGroup()
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(field_min_pen, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(jLabel6)
                                                    .add(field_min_hex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(field_min_hep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(field_min_hexn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(field_min_hexnac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(field_min_dpen, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(field_min_dhex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(field_max_dhex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(field_max_dpen, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(field_max_hexnac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(field_max_hexn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(field_max_hep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(field_max_hex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(jLabel7)
                                                    .add(field_max_pen, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                            .add(layout.createSequentialGroup()
                                                .add(field_min_ddhex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                                .add(field_max_ddhex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(field_or1_mass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                    .add(layout.createSequentialGroup()
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(field_or2_mass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabel23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jLabel28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 187, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabel24)
                                            .add(jLabel29)))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 187, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel31))
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 187, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(jLabel33))
                                    .add(jLabel40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabel34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 187, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jLabel46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jLabel36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 132, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabel47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jLabel35)
                                            .add(jLabel41)
                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel43, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel37, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(jLabel58)
                                            .add(jLabel59)
                                            .add(jLabel60)
                                            .add(jLabel61)))
                                    .add(jLabel54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel55)
                                    .add(jLabel56)
                                    .add(jLabel57))
                                .add(21, 21, 21)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_pc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_pc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_pyr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_pyr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_ac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_ac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_p, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_p, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_s, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_s, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_mur, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_mur, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_kdn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_kdn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_kdo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_kdo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_neu5aclac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_neu5aclac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_neu5gclac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_neu5gclac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_neu5ac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_neu5ac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_neu5gc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_neu5gc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(field_min_hexa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(jLabel26))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(jLabel27)
                                            .add(field_max_hexa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                    .add(layout.createSequentialGroup()
                                        .add(field_min_dhexa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 12, Short.MAX_VALUE)
                                        .add(field_max_dhexa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                            .add(jLabel50)
                            .add(jLabel52)
                            .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {button_cancel, button_ok}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(field_derivatization, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(field_reducingend, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(field_other_redend_name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(field_other_redend_mass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jLabel7)
                    .add(jLabel26)
                    .add(jLabel27))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jLabel8)
                    .add(field_min_pen, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_pen, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel24)
                    .add(jLabel23)
                    .add(field_min_hexa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_hexa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(jLabel10)
                    .add(field_min_hex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_hex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel29)
                    .add(jLabel28)
                    .add(field_min_dhexa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_dhexa, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(jLabel13)
                    .add(field_min_hep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_hep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel30)
                    .add(jLabel31)
                    .add(field_min_neu5gc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_neu5gc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel14)
                    .add(jLabel15)
                    .add(field_min_hexn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_hexn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel32)
                    .add(jLabel33)
                    .add(field_min_neu5ac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_neu5ac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jLabel16)
                    .add(field_min_hexnac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_hexnac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel34)
                    .add(jLabel35)
                    .add(field_min_neu5gclac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_neu5gclac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel17)
                    .add(jLabel18)
                    .add(field_min_dpen, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_dpen, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel37)
                    .add(field_min_neu5aclac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_neu5aclac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel36))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel19)
                    .add(jLabel20)
                    .add(field_min_dhex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_dhex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel40)
                    .add(jLabel41)
                    .add(field_min_kdo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_kdo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel21)
                    .add(jLabel22)
                    .add(field_min_ddhex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_ddhex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel42)
                    .add(jLabel43)
                    .add(field_min_kdn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_kdn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel38)
                    .add(jLabel39)
                    .add(field_min_mehex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_mehex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel44)
                    .add(jLabel45)
                    .add(field_min_mur, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_mur, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel46)
                    .add(jLabel47)
                    .add(field_min_s, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_s, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel48)
                    .add(jLabel49)
                    .add(field_min_or1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_or1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel50)
                    .add(jLabel51)
                    .add(field_min_or2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_or2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel54)
                    .add(jLabel58)
                    .add(field_min_p, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_p, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel52)
                    .add(jLabel53)
                    .add(field_min_or3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_or3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel55)
                    .add(jLabel59)
                    .add(field_min_ac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_ac, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel56)
                    .add(jLabel60)
                    .add(field_min_pyr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_pyr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel62)
                    .add(field_or1_mass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel65)
                    .add(field_or1_name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel57)
                    .add(jLabel61)
                    .add(field_min_pc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_max_pc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel63)
                    .add(field_or2_mass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_or2_name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel66))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel64)
                    .add(field_or3_mass, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_or3_name, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel67))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(button_ok)
                    .add(button_cancel))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {button_cancel, button_ok}, org.jdesktop.layout.GroupLayout.VERTICAL);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    /** Closes the dialog */
    private void closeDialog() {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
   private JButton button_cancel;
    private JButton button_ok;
    private JComboBox field_derivatization;
    private JSpinner field_max_ac;
    private JSpinner field_max_ddhex;
    private JSpinner field_max_dhex;
    private JSpinner field_max_dhexa;
    private JSpinner field_max_dpen;
    private JSpinner field_max_hep;
    private JSpinner field_max_hex;
    private JSpinner field_max_hexa;
    private JSpinner field_max_hexn;
    private JSpinner field_max_hexnac;
    private JSpinner field_max_kdn;
    private JSpinner field_max_kdo;
    private JSpinner field_max_mehex;
    private JSpinner field_max_mur;
    private JSpinner field_max_neu5ac;
    private JSpinner field_max_neu5aclac;
    private JSpinner field_max_neu5gc;
    private JSpinner field_max_neu5gclac;
    private JSpinner field_max_or1;
    private JSpinner field_max_or2;
    private JSpinner field_max_or3;
    private JSpinner field_max_p;
    private JSpinner field_max_pc;
    private JSpinner field_max_pen;
    private JSpinner field_max_pyr;
    private JSpinner field_max_s;
    private JSpinner field_min_ac;
    private JSpinner field_min_ddhex;
    private JSpinner field_min_dhex;
    private JSpinner field_min_dhexa;
    private JSpinner field_min_dpen;
    private JSpinner field_min_hep;
    private JSpinner field_min_hex;
    private JSpinner field_min_hexa;
    private JSpinner field_min_hexn;
    private JSpinner field_min_hexnac;
    private JSpinner field_min_kdn;
    private JSpinner field_min_kdo;
    private JSpinner field_min_mehex;
    private JSpinner field_min_mur;
    private JSpinner field_min_neu5ac;
    private JSpinner field_min_neu5aclac;
    private JSpinner field_min_neu5gc;
    private JSpinner field_min_neu5gclac;
    private JSpinner field_min_or1;
    private JSpinner field_min_or2;
    private JSpinner field_min_or3;
    private JSpinner field_min_p;
    private JSpinner field_min_pc;
    private JSpinner field_min_pen;
    private JSpinner field_min_pyr;
    private JSpinner field_min_s;
    private JTextField field_or1_mass;
    private JTextField field_or1_name;
    private JTextField field_or2_mass;
    private JTextField field_or2_name;
    private JTextField field_or3_mass;
    private JTextField field_or3_name;
    private JTextField field_other_redend_mass;
    private JTextField field_other_redend_name;
    private JComboBox field_reducingend;
    private JLabel jLabel1;
    private JLabel jLabel10;
    private JLabel jLabel11;
    private JLabel jLabel12;
    private JLabel jLabel13;
    private JLabel jLabel14;
    private JLabel jLabel15;
    private JLabel jLabel16;
    private JLabel jLabel17;
    private JLabel jLabel18;
    private JLabel jLabel19;
    private JLabel jLabel2;
    private JLabel jLabel20;
    private JLabel jLabel21;
    private JLabel jLabel22;
    private JLabel jLabel23;
    private JLabel jLabel24;
    private JLabel jLabel26;
    private JLabel jLabel27;
    private JLabel jLabel28;
    private JLabel jLabel29;
    private JLabel jLabel3;
    private JLabel jLabel30;
    private JLabel jLabel31;
    private JLabel jLabel32;
    private JLabel jLabel33;
    private JLabel jLabel34;
    private JLabel jLabel35;
    private JLabel jLabel36;
    private JLabel jLabel37;
    private JLabel jLabel38;
    private JLabel jLabel39;
    private JLabel jLabel4;
    private JLabel jLabel40;
    private JLabel jLabel41;
    private JLabel jLabel42;
    private JLabel jLabel43;
    private JLabel jLabel44;
    private JLabel jLabel45;
    private JLabel jLabel46;
    private JLabel jLabel47;
    private JLabel jLabel48;
    private JLabel jLabel49;
    private JLabel jLabel5;
    private JLabel jLabel50;
    private JLabel jLabel51;
    private JLabel jLabel52;
    private JLabel jLabel53;
    private JLabel jLabel54;
    private JLabel jLabel55;
    private JLabel jLabel56;
    private JLabel jLabel57;
    private JLabel jLabel58;
    private JLabel jLabel59;
    private JLabel jLabel6;
    private JLabel jLabel60;
    private JLabel jLabel61;
    private JLabel jLabel62;
    private JLabel jLabel63;
    private JLabel jLabel64;
    private JLabel jLabel65;
    private JLabel jLabel66;
    private JLabel jLabel67;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JSeparator jSeparator1;
    private JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
    
}
