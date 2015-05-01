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

package org.eurocarbdb.application.glycoworkbench.plugin.reporting;

import org.eurocarbdb.application.glycoworkbench.plugin.*;
import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.*;

public class ProfilesComparisonReportChartFrame extends JFrame implements ActionListener, MouseListener {

    private GlycoWorkbench theApplication;
    private ActionManager theActionManager;  
 
    private JMenuBar theMenuBar;
    private JToolBar theToolBar;
    private ProfilesComparisonReportChartCanvas theCanvas;
    private JScrollPane theScrollPane;    

    public ProfilesComparisonReportChartFrame(GlycoWorkbench application, ProfilesComparisonReportDocument doc, ProfilesComparisonReportOptions opt) {

    // set singletons
    theApplication = application;
    theActionManager = new ActionManager();    
         theCanvas = new ProfilesComparisonReportChartCanvas(application,doc,opt);

    // initialize the action set
    createActions();

    // set layout
    getContentPane().setLayout(new BorderLayout());
    
    // set the MenuBar
    theMenuBar = createMenuBar();
    setJMenuBar(theMenuBar);    
    
    // set the toolbars
    UIManager.getDefaults().put("ToolTip.hideAccelerator",Boolean.TRUE);
    theToolBar = createToolBar();
    getContentPane().add(theToolBar,BorderLayout.NORTH);

    // set the drawing pane         
    theScrollPane = new JScrollPane(theCanvas);
    theCanvas.setScrollPane(theScrollPane);
    getContentPane().add(theScrollPane,BorderLayout.CENTER);

    // finish setting up
    theCanvas.addMouseListener( this ); 

        setSize(900,700);       
    setLocationRelativeTo(theApplication);
    
    updateActions();    
    }

    private void createActions() {    

    //file
    for(Map.Entry<String,String> e : SVGUtils.getExportFormats().entrySet() )
        theActionManager.add("export=" + e.getKey(),FileUtils.defaultThemeManager.getImageIcon(""),"Export to " + e.getValue() + "...",-1, "",this);
    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "ctrl P",this);
    theActionManager.add("close",FileUtils.defaultThemeManager.getImageIcon("close"),"Close",KeyEvent.VK_C, "ctrl Q",this);

    //edit
          theActionManager.add("screenshot",FileUtils.defaultThemeManager.getImageIcon("screenshot"),"Screenshot",KeyEvent.VK_PRINTSCREEN, "PRINTSCREEN",this);    

        
    // view
    theActionManager.add("zoomnone",FileUtils.defaultThemeManager.getImageIcon("zoomnone"),"Reset zoom",-1, "",this);
    theActionManager.add("zoomin",FileUtils.defaultThemeManager.getImageIcon("zoomin"),"Zoom in",-1, "",this);
    theActionManager.add("zoomout",FileUtils.defaultThemeManager.getImageIcon("zoomout"),"Zoom out",-1, "",this);    
    }

    private void updateActions() {

    }

    private JMenu createExportDrawingMenu() {
    
    JMenu export_menu = new JMenu("Export to graphical formats");    
    export_menu.setIcon(FileUtils.defaultThemeManager.getImageIcon("export"));

    for(Map.Entry<String,String> e : SVGUtils.getExportFormats().entrySet() )
        export_menu.add(theActionManager.get("export="+e.getKey()));

    return export_menu;
    }


    private JMenu createFileMenu() {

    JMenu file_menu = new JMenu("File");    

    file_menu.add(createExportDrawingMenu());
    
    file_menu.addSeparator();

    file_menu.add(theActionManager.get("print"));

    file_menu.addSeparator();

    file_menu.add(theActionManager.get("close"));    

    return file_menu;
    
    }

    private JMenu createEditMenu() {

    JMenu edit_menu = new JMenu("Edit");    
    edit_menu.setMnemonic(KeyEvent.VK_E);
    edit_menu.add(theActionManager.get("screenshot"));
        
    return edit_menu;
    }

    private JMenu createViewMenu() {
    JMenu view_menu = new JMenu("View");
    view_menu.setMnemonic(KeyEvent.VK_V);
    

    // zoom 
    view_menu.add(theActionManager.get("zoomnone"));
    view_menu.add(theActionManager.get("zoomin"));
    view_menu.add(theActionManager.get("zoomout"));

    return view_menu;
    }


    private JMenuBar createMenuBar() {
     
    JMenuBar menubar = new JMenuBar();
    
    menubar.add(createFileMenu());
    menubar.add(createEditMenu());
    menubar.add(createViewMenu());

    return menubar;
    }

    protected JPopupMenu createPopupMenu() {

    JPopupMenu menu = new JPopupMenu();

    menu.add(theActionManager.get("zoomnone"));
    menu.add(theActionManager.get("zoomin"));
    menu.add(theActionManager.get("zoomout"));    

    return menu;
    }

    private JToolBar createToolBar() {
    
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    toolbar.add(theActionManager.get("print"));
    
    toolbar.addSeparator();

    toolbar.add(theActionManager.get("screenshot"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("zoomnone"));
    toolbar.add(theActionManager.get("zoomin"));
    toolbar.add(theActionManager.get("zoomout"));

    return toolbar;
    }

    // actions

    public void onZoomNone() {
    theCanvas.setScale(1.);
    }

    public void onZoomIn() {
    theCanvas.setScale(theCanvas.getScale()*1.5);
    }

    public void onZoomOut() {
    theCanvas.setScale(theCanvas.getScale()*0.667);
    }          

    public void onPrint() {
    try {       
        PrinterJob pj = theApplication.getWorkspace().getPrinterJob();
        if( pj!=null ) {
        pj.setPrintable(theCanvas);
        if( pj.printDialog() ) 
            theCanvas.print(pj);
        }
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    public void onExportTo(String format) {

        // imposto la dialog per il salvataggio del file
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.addChoosableFileFilter(new ExtensionFileFilter(format));
        
        // visualizzo la dialog
        int returnVal = fileChooser.showSaveDialog(this);        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        
        // aggiunge l'estension
        String filename = fileChooser.getSelectedFile().getAbsolutePath();
        filename = FileUtils.enforceExtension(filename,format);

        // chiede conferma prima di sovrascrivere il file
        File file = new File(filename);                    
        if (file.exists()) {
        int retValue = JOptionPane.showOptionDialog(this, "File exists. Overwrite file: " + filename + "?",
                                "Salva documento", JOptionPane.YES_NO_CANCEL_OPTION, 
                                JOptionPane.QUESTION_MESSAGE, null, null, null);  
        if( retValue!=JOptionPane.YES_OPTION )
            return;
        }            
        
        // esporta il documento su file        
        try {
        SVGUtils.export(filename,theCanvas,format);
        }
        catch(Exception e) {
        LogUtils.report(e);
        }
    }
    }

    // events
    
    public void actionPerformed(ActionEvent e) {

    String action = GlycanAction.getAction(e);
    String param  = GlycanAction.getParam(e);    
    
    if( action.equals("export") )
        onExportTo(param);

    else if( action.equals("print") )
        onPrint();
    else if( action.equals("close") )
        this.setVisible(false);

    else if( action.equals("screenshot") )
        theCanvas.getScreenshot();

        // display
    else if( action.equals("zoomnone") )
        onZoomNone();
    else if( action.equals("zoomin") )
        onZoomIn();
    else if( action.equals("zoomout") )
        onZoomOut();

    updateActions();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    if( MouseUtils.isPopupTrigger(e) ) 
        createPopupMenu().show(theCanvas, e.getX(), e.getY());       
    }

    public void mouseReleased(MouseEvent e) {
    if( MouseUtils.isPopupTrigger(e) )        
        createPopupMenu().show(theCanvas, e.getX(), e.getY());   
    }

    public void mouseClicked(MouseEvent e) {          
    }    

}
