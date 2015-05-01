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

import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ProgressDialog extends JDialog implements ActionListener {

    private boolean is_canceled = false;

    // components
    private JLabel theMessage;
    private JLabel theNote;
    private JProgressBar theBar;
    private JButton cancel_button;

    //
    public ProgressDialog(JFrame parent, String message, String note, int min, int max) {
    super(parent,"Progress",false);
    /*this.getContentPane().setLayout(new BorderLayout());
    
    // set labels
    JPanel labels_panel = new JPanel();
    labels_panel.setLayout(new BoxLayout(labels_panel,BoxLayout.Y_AXIS));

    theMessage = new JLabel(message);
    theMessage.setBorder(new EmptyBorder(10,10,10,10));
    labels_panel.add(theMessage);
    
    theNote = new JLabel(note);
    theNote.setBorder(new EmptyBorder(10,10,10,10));
    labels_panel.add(theNote);

    this.getContentPane().add(labels_panel,BorderLayout.NORTH);
    
    // set bar
    if( min==-1 || max==-1 ) {
        theBar = new JProgressBar();
        theBar.setIndeterminate(true);
    }
    else
        theBar = new JProgressBar(min,max);
    this.getContentPane().add(theBar,BorderLayout.CENTER);

    // set buttons
    JPanel buttons_panel = new JPanel(new FlowLayout());
    cancel_button = new JButton(new GlycanAction("Stop",(Icon)null,"Stop",-1,"",this));
    buttons_panel.add(cancel_button);
    this.getContentPane().add(buttons_panel,BorderLayout.SOUTH);
     */


    this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS));
    
    theMessage = new JLabel(message);
    theMessage.setBorder(new EmptyBorder(10,10,10,10));
    this.getContentPane().add(theMessage);
    
    theNote = new JLabel(note);
    theNote.setBorder(new EmptyBorder(10,10,10,10));
    this.getContentPane().add(theNote);

    // set bar
    if( min==-1 || max==-1 ) {
        theBar = new JProgressBar();
        theBar.setIndeterminate(true);
    }
    else
        theBar = new JProgressBar(min,max);
    this.getContentPane().add(theBar);
    
    // set buttons
    JPanel buttons_panel = new JPanel(new FlowLayout());
    cancel_button = new JButton(new GlycanAction("Stop",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Stop",-1,"",this));
    buttons_panel.add(cancel_button);
    this.getContentPane().add(buttons_panel);     

    // set dialog

    pack();
    setResizable(false);    
    setLocationRelativeTo(parent);
    }

    public boolean isCanceled() {
    return is_canceled;
    }

    public void setNote(String note) {
    theNote.setText(note);
    this.pack();
    }

    public void setValue(int val) {
    theBar.setValue(val);
    }

    public void actionPerformed(ActionEvent e) {

    String action = GlycanAction.getAction(e);

    if( action.equals("Stop") ) {
        is_canceled = true;
        setVisible(false);
    }

    }
}