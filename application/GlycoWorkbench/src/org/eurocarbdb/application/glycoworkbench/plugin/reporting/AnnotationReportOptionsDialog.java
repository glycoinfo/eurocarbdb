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

package org.eurocarbdb.application.glycoworkbench.plugin.reporting;

import org.eurocarbdb.application.glycoworkbench.plugin.*;
import org.eurocarbdb.application.glycanbuilder.*;
import java.util.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import javax.swing.event.*;
import javax.swing.*;

public class AnnotationReportOptionsDialog extends EscapeDialog implements ActionListener {
    
    private AnnotationReportOptions theOptions = null;
    private HashMap<JButton,Color> field_color_map = new HashMap<JButton,Color>();    
    private JColorChooser color_chooser = new JColorChooser();

    /** Creates new form MassOptionsDialog */
    public AnnotationReportOptionsDialog(Frame parent, AnnotationReportOptions options) {
        super(parent, true);
    
    theOptions = options;
    
        initComponents();
    initData(theOptions);
    setTraversal();
    setActions();
    enableItems();

    // set location
    setLocationRelativeTo(parent);
    }       
 
    private Icon createIcon(Color color) {
    BufferedImage img = GraphicUtils.createImage(150,15,true);
    Graphics2D g2d = img.createGraphics();    
    g2d.setBackground(color);
    g2d.clearRect(0, 0, 150, 15);
    return new ImageIcon(img);
    }

    private void initData(AnnotationReportOptions options) {

    // set models
    field_annotation_margin.setModel(new SpinnerNumberModel(1,1,24,1));
    field_annotation_mz_size.setModel(new SpinnerNumberModel(1,1,24,1));
    field_annotation_mz_font.setModel(new DefaultComboBoxModel(GraphicOptions.getAllFontFaces()));

    field_annotation_line_width.setModel(new SpinnerNumberModel(0.,0.,5.,0.1));
    field_scale_glycans.setModel(new SpinnerNumberModel(0.,0.,2.,0.05));

    // set selections
    field_draw_x_margin.setText(""+options.DRAW_X_MARGIN);
    field_draw_y_margin.setText(""+options.DRAW_Y_MARGIN);

    field_chart_width.setText(""+options.CHART_WIDTH_NONSCALED);
    field_chart_height.setText(""+options.CHART_HEIGHT_NONSCALED);
    field_chart_x_margin.setText(""+options.CHART_X_MARGIN_NONSCALED);
    field_chart_y_margin.setText(""+options.CHART_Y_MARGIN_NONSCALED);

    field_annotation_margin.setValue(options.ANNOTATION_MARGIN_NONSCALED);
    field_annotation_mz_size.setValue(options.ANNOTATION_MZ_SIZE_NONSCALED);
    field_annotation_mz_font.setSelectedItem(options.ANNOTATION_MZ_FONT);
    field_annotation_line_width.setValue(options.ANNOTATION_LINE_WIDTH);

    field_scale_glycans.setValue(options.SCALE_GLYCANS_NONSCALED);

    // set colors
    field_color_map.put(button_spectrum_color,options.SPECTRUM_COLOR);
    field_color_map.put(button_mass_text_color,options.MASS_TEXT_COLOR);
    field_color_map.put(button_connection_lines_color,options.CONNECTION_LINES_COLOR);
    field_color_map.put(button_highlighted_color,options.HIGHLIGHTED_COLOR);

    for( Map.Entry<JButton,Color> e : field_color_map.entrySet() ) 
        e.getKey().setIcon(createIcon(e.getValue()));

        pack();
    }


    private void setTraversal() {
    CustomFocusTraversalPolicy tp = new CustomFocusTraversalPolicy();

    tp.addComponent(field_draw_x_margin);
    tp.addComponent(field_draw_y_margin);

    tp.addComponent(field_chart_width);
    tp.addComponent(field_chart_height);
    tp.addComponent(field_chart_x_margin);
    tp.addComponent(field_chart_y_margin);

    tp.addComponent(field_annotation_margin);
    tp.addComponent(field_annotation_mz_size);
    tp.addComponent(field_annotation_mz_font);
    tp.addComponent(field_annotation_line_width);

    tp.addComponent(field_scale_glycans);
    
    tp.addComponent(button_spectrum_color);
    tp.addComponent(button_mass_text_color);
    tp.addComponent(button_connection_lines_color);
    tp.addComponent(button_highlighted_color);

    tp.addComponent(button_cancel);
    tp.addComponent(button_default);
    tp.addComponent(button_ok);

    this.setFocusTraversalPolicy(tp);

    getRootPane().setDefaultButton(button_ok); 
    }
    
    private void setActions() {
    // set actions for color buttons
    for( Map.Entry<JButton,Color> e : field_color_map.entrySet() )  {
        e.getKey().addActionListener(this);
        e.getKey().setActionCommand("Change color");
    }

    button_default.addActionListener(this);
    button_ok.addActionListener(this);
    button_cancel.addActionListener(this);    
    }

    private void enableItems() {
    
    }

    private void retrieveData() {

    theOptions.DRAW_X_MARGIN = Integer.valueOf(field_draw_x_margin.getText());
    theOptions.DRAW_Y_MARGIN = Integer.valueOf(field_draw_y_margin.getText());

    theOptions.CHART_WIDTH_NONSCALED = Integer.valueOf(field_chart_width.getText());
    theOptions.CHART_HEIGHT_NONSCALED = Integer.valueOf(field_chart_height.getText());
    theOptions.CHART_X_MARGIN_NONSCALED = Integer.valueOf(field_chart_x_margin.getText());
    theOptions.CHART_Y_MARGIN_NONSCALED = Integer.valueOf(field_chart_y_margin.getText());

    theOptions.ANNOTATION_MARGIN_NONSCALED = (Integer)field_annotation_margin.getValue();
    theOptions.ANNOTATION_MZ_SIZE_NONSCALED = (Integer)field_annotation_mz_size.getValue();
    theOptions.ANNOTATION_MZ_FONT = (String)field_annotation_mz_font.getSelectedItem();
    theOptions.ANNOTATION_LINE_WIDTH = (Double)field_annotation_line_width.getValue();

    theOptions.SCALE_GLYCANS_NONSCALED = (Double)field_scale_glycans.getValue();

    theOptions.SPECTRUM_COLOR = field_color_map.get(button_spectrum_color);
    theOptions.MASS_TEXT_COLOR = field_color_map.get(button_mass_text_color);
    theOptions.CONNECTION_LINES_COLOR = field_color_map.get(button_connection_lines_color);
    theOptions.HIGHLIGHTED_COLOR = field_color_map.get(button_highlighted_color);

    theOptions.setScale(theOptions.SCALE);
    }
    
    public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    
    if( action == "Default" ) {
        initData(new AnnotationReportOptions());
    }
    else if( action =="Change color") {
        changeColor((JButton)e.getSource());
    }
    else if( action == "OK" ) {
        return_status = action;
        retrieveData();
        closeDialog();
    }
    else if (action == "Cancel"){
        return_status = action;
        closeDialog();
    }
    }    
    
    public void changeColor( final JButton field ) {
    color_chooser.setColor(field_color_map.get(field));
    JDialog dlg = JColorChooser.createDialog(this,"Select new color",true,color_chooser, new ActionListener() {
        public void actionPerformed(ActionEvent e) {            
            field_color_map.put(field,color_chooser.getColor());
            field.setIcon(createIcon(color_chooser.getColor()));
        }        
        }, null);
    dlg.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
     
        jLabel1 = new JLabel();
        field_draw_x_margin = new JTextField();
        jLabel2 = new JLabel();
        field_draw_y_margin = new JTextField();
        jLabel3 = new JLabel();
        field_chart_width = new JTextField();
        jLabel4 = new JLabel();
        field_chart_height = new JTextField();
        jLabel5 = new JLabel();
        field_chart_x_margin = new JTextField();
        jLabel6 = new JLabel();
        field_chart_y_margin = new JTextField();
        jLabel7 = new JLabel();
        jLabel8 = new JLabel();
        field_annotation_margin = new JSpinner();
        field_annotation_mz_size = new JSpinner();
        jLabel9 = new JLabel();
        field_annotation_mz_font = new JComboBox();
        jLabel10 = new JLabel();
        field_annotation_line_width = new JSpinner();
        jLabel11 = new JLabel();
        field_scale_glycans = new JSpinner();
        jSeparator1 = new JSeparator();
        button_default = new JButton();
        button_ok = new JButton();
        button_cancel = new JButton();
        button_spectrum_color = new JButton();
        jLabel12 = new JLabel();
        button_mass_text_color = new JButton();
        jLabel13 = new JLabel();
        button_connection_lines_color = new JButton();
        jLabel14 = new JLabel();
        button_highlighted_color = new JButton();
        jLabel15 = new JLabel();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeDialog();
            }
        });

        jLabel1.setText("Canvas X margin");

        field_draw_x_margin.setText("jTextField1");

        jLabel2.setText("Canvas Y margin");

        field_draw_y_margin.setText("jTextField1");

        jLabel3.setText("Chart width");

        field_chart_width.setText("jTextField1");

        jLabel4.setText("Chart height");

        field_chart_height.setText("jTextField1");

        jLabel5.setText("Chart X margin");

        field_chart_x_margin.setText("jTextField1");

        jLabel6.setText("Chart Y margin");

        field_chart_y_margin.setText("jTextField1");

        jLabel7.setText("m/z text margin");

        jLabel8.setText("m/z text size");

        jLabel9.setText("m/z text font");

        field_annotation_mz_font.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel10.setText("Line width");

        jLabel11.setText("Structures scale factor");

        button_default.setText("Default");

        button_ok.setText("OK");

        button_cancel.setText("Cancel");

        button_spectrum_color.setText(" ");

        jLabel12.setText("Spectrum color");

        button_mass_text_color.setText(" ");

        jLabel13.setText("Mass text color");

        button_connection_lines_color.setText(" ");

        jLabel14.setText("Connection lines color");

        button_highlighted_color.setText(" ");

        jLabel15.setText("Highlighted color");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(55, 55, 55)
                        .add(button_default)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(button_ok)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(button_cancel))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                        .add(jLabel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                        .add(jLabel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                        .add(jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                        .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                        .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                        .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                        .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                        .add(jLabel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(jLabel12))
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                                .add(jLabel13)
                                .add(jLabel14))
                            .add(layout.createSequentialGroup()
                                .add(jLabel15)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, button_mass_text_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(field_annotation_mz_font, 0, 169, Short.MAX_VALUE)
                            .add(field_draw_y_margin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(field_chart_width, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(field_chart_height, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(field_chart_x_margin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(field_chart_y_margin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(field_annotation_margin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(field_draw_x_margin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(field_annotation_mz_size, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(field_annotation_line_width, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(field_scale_glycans, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(button_spectrum_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, button_connection_lines_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .add(button_highlighted_color, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(new Component[] {button_cancel, button_default, button_ok}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(field_draw_x_margin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(field_draw_y_margin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(field_chart_width, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(field_chart_height, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(field_chart_x_margin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(field_chart_y_margin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(field_annotation_margin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(field_annotation_mz_size, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(field_annotation_mz_font, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(field_annotation_line_width, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(field_scale_glycans, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(button_spectrum_color)
                    .add(jLabel12))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(button_mass_text_color))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel14)
                    .add(button_connection_lines_color))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 10, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(button_highlighted_color)
                    .add(jLabel15))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(button_default)
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
    private JButton button_cancel;
    private JButton button_connection_lines_color;
    private JButton button_default;
    private JButton button_highlighted_color;
    private JButton button_mass_text_color;
    private JButton button_ok;
    private JButton button_spectrum_color;
    private JSpinner field_annotation_line_width;
    private JSpinner field_annotation_margin;
    private JComboBox field_annotation_mz_font;
    private JSpinner field_annotation_mz_size;
    private JTextField field_chart_height;
    private JTextField field_chart_width;
    private JTextField field_chart_x_margin;
    private JTextField field_chart_y_margin;
    private JTextField field_draw_x_margin;
    private JTextField field_draw_y_margin;
    private JSpinner field_scale_glycans;
    private JLabel jLabel1;
    private JLabel jLabel10;
    private JLabel jLabel11;
    private JLabel jLabel12;
    private JLabel jLabel13;
    private JLabel jLabel14;
    private JLabel jLabel15;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
    
}
