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

public class AnnotationReportEditor extends JFrame implements ActionListener, MouseListener, AnnotationReportCanvas.SelectionChangeListener, BaseDocument.DocumentChangeListener {

    private GlycoWorkbench theApplication;
    private ActionManager theActionManager;  
    private ReportingPlugin thePlugin;
    
    private JMenuBar theMenuBar;
    private JToolBar theToolBar;
    private AnnotationReportCanvas theCanvas;
    private JScrollPane theScrollPane;
    
    private ButtonGroup display_button_group = null;
    protected HashMap<String,ButtonModel> display_models = null;  

    public AnnotationReportEditor(GlycoWorkbench application, ReportingPlugin plugin, AnnotationReportDocument doc, boolean init_pos) {

    // set singletons
    theApplication = application;
    theActionManager = new ActionManager();    
    theCanvas = new AnnotationReportCanvas(doc,init_pos);
    theCanvas.getDocument().addDocumentChangeListener(this);
    thePlugin = plugin;

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
    theCanvas.addSelectionChangeListener(this);

    updateTitle();
        setSize(900,700);       
    setLocationRelativeTo(theApplication);
    
    updateActions();    
    }

    private void createActions() {    

    //file
    for(Map.Entry<String,String> e : SVGUtils.getExportFormats().entrySet() )
        theActionManager.add("export=" + e.getKey(),FileUtils.defaultThemeManager.getImageIcon(""),"Export to " + e.getValue() + "...",-1, "",this);
    theActionManager.add("open",FileUtils.defaultThemeManager.getImageIcon("open"),"Open...",KeyEvent.VK_O, "ctrl O",this);
    theActionManager.add("save",FileUtils.defaultThemeManager.getImageIcon("save"),"Save",KeyEvent.VK_S, "ctrl S",this);
    theActionManager.add("saveas",FileUtils.defaultThemeManager.getImageIcon("saveas"),"Save as...",KeyEvent.VK_A, "shift ctrl S",this);
    theActionManager.add("update",FileUtils.defaultThemeManager.getImageIcon("update"),"Update annotations",KeyEvent.VK_U, "",this);
    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "ctrl P",this);
    theActionManager.add("close",FileUtils.defaultThemeManager.getImageIcon("close"),"Close",KeyEvent.VK_C, "ctrl Q",this);

    //edit
    
    theActionManager.add("undo",FileUtils.defaultThemeManager.getImageIcon("undo"),"Undo",KeyEvent.VK_U, "ctrl Z",this);
    theActionManager.add("redo",FileUtils.defaultThemeManager.getImageIcon("redo"),"Redo", KeyEvent.VK_R, "ctrl Y",this);

    theActionManager.add("cut",FileUtils.defaultThemeManager.getImageIcon("cut"),"Cut",KeyEvent.VK_T, "ctrl X",this);
    theActionManager.add("copy",FileUtils.defaultThemeManager.getImageIcon("copy"),"Copy",KeyEvent.VK_C, "ctrl C",this);
    theActionManager.add("delete",FileUtils.defaultThemeManager.getImageIcon("delete"),"Delete",KeyEvent.VK_DELETE, "",this);
    theActionManager.add("screenshot",FileUtils.defaultThemeManager.getImageIcon("screenshot"),"Screenshot",KeyEvent.VK_PRINTSCREEN, "PRINTSCREEN",this);    

    theActionManager.add("selectall",FileUtils.defaultThemeManager.getImageIcon("selectall"),"Select all",KeyEvent.VK_A, "ctrl A",this);
    theActionManager.add("selectnone",FileUtils.defaultThemeManager.getImageIcon("selectnone"),"Select none",KeyEvent.VK_E, "ESCAPE",this);

    theActionManager.add("enlarge",FileUtils.defaultThemeManager.getImageIcon("enlarge"),"Enlarge selected structures",-1, "",this);
    theActionManager.add("resetsize",FileUtils.defaultThemeManager.getImageIcon("resetsize"),"Reset size of selected structures to default value",-1, "",this);
    theActionManager.add("shrink",FileUtils.defaultThemeManager.getImageIcon("shrink"),"Shrink selected structures",-1, "",this);
    theActionManager.add("highlight",FileUtils.defaultThemeManager.getImageIcon("highlight"),"Set highlight for selected structures",-1, "",this);

    theActionManager.add("ungroup",FileUtils.defaultThemeManager.getImageIcon("ungroup"),"Ungroup selected structures",-1, "",this);
    theActionManager.add("group",FileUtils.defaultThemeManager.getImageIcon("group"),"Group selected structures",-1, "",this);
    
    theActionManager.add("placestructures",FileUtils.defaultThemeManager.getImageIcon("placestructures"),"Automatic place all structures",-1, "",this);

    // view
    theActionManager.add("zoomnone",FileUtils.defaultThemeManager.getImageIcon("zoomnone"),"Reset zoom",-1, "",this);
    theActionManager.add("zoomin",FileUtils.defaultThemeManager.getImageIcon("zoomin"),"Zoom in",-1, "",this);
    theActionManager.add("zoomout",FileUtils.defaultThemeManager.getImageIcon("zoomout"),"Zoom out",-1, "",this);

    theActionManager.add("notation=" + GraphicOptions.NOTATION_CFG, ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3), "CFG notation", KeyEvent.VK_C, "", this);
    theActionManager.add("notation=" + GraphicOptions.NOTATION_CFGBW, ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3), "CFG black and white notation", KeyEvent.VK_B, "", this);
    theActionManager.add("notation=" + GraphicOptions.NOTATION_CFGLINK, ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3), "CFG with linkage placement notation", KeyEvent.VK_L, "", this);
    theActionManager.add("notation=" + GraphicOptions.NOTATION_UOXF, ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3), "UOXF notation", KeyEvent.VK_O, "", this);
    theActionManager.add("notation=" + GraphicOptions.NOTATION_TEXT, ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3), "Text only notation", KeyEvent.VK_T, "", this);    

    theActionManager.add("display=" + GraphicOptions.DISPLAY_COMPACT, ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3), "compact view", KeyEvent.VK_O, "", this);
    theActionManager.add("display=" + GraphicOptions.DISPLAY_NORMAL, ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3), "normal view", KeyEvent.VK_N, "", this);
    theActionManager.add("display=" + GraphicOptions.DISPLAY_NORMALINFO, ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3), "normal view with linkage info", KeyEvent.VK_I, "", this);
    theActionManager.add("display=" + GraphicOptions.DISPLAY_CUSTOM, ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3), "custom view with user settings", KeyEvent.VK_K, "", this);

    theActionManager.add("orientation",getOrientationIcon(),"Change orientation",-1,"",this);

    theActionManager.add("displaysettings",FileUtils.defaultThemeManager.getImageIcon(""),"Change structure display settings",KeyEvent.VK_S,"",this);
    theActionManager.add("reportsettings",FileUtils.defaultThemeManager.getImageIcon(""),"Change report display settings",KeyEvent.VK_R,"",this);
    }

    private void updateActions() {

    theActionManager.get("save").setEnabled(theCanvas.getDocument().hasChanged());

    theActionManager.get("undo").setEnabled(theCanvas.getDocument().getUndoManager().canUndo());
    theActionManager.get("redo").setEnabled(theCanvas.getDocument().getUndoManager().canRedo());

    theActionManager.get("cut").setEnabled(theCanvas.hasSelection());
    theActionManager.get("copy").setEnabled(theCanvas.hasSelection());
    theActionManager.get("delete").setEnabled(theCanvas.hasSelection());

    theActionManager.get("enlarge").setEnabled(theCanvas.hasSelection());
    theActionManager.get("resetsize").setEnabled(theCanvas.hasSelection());
    theActionManager.get("shrink").setEnabled(theCanvas.hasSelection());
    theActionManager.get("highlight").setEnabled(theCanvas.hasSelection());

    theActionManager.get("group").setEnabled(theCanvas.canGroupSelections());

    theActionManager.get("orientation").putValue(Action.SMALL_ICON,getOrientationIcon());
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

    file_menu.add(theActionManager.get("open"));
    file_menu.add(theActionManager.get("save"));
    file_menu.add(theActionManager.get("saveas"));

    file_menu.addSeparator();

    file_menu.add(theActionManager.get("update"));

    file_menu.addSeparator();

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
    edit_menu.add(theActionManager.get("undo"));
    edit_menu.add(theActionManager.get("redo"));
    edit_menu.addSeparator();
    edit_menu.add(theActionManager.get("cut"));
    edit_menu.add(theActionManager.get("copy"));
    edit_menu.add(theActionManager.get("delete"));
    edit_menu.add(theActionManager.get("screenshot"));
    edit_menu.addSeparator();
    edit_menu.add(theActionManager.get("selectall"));
    edit_menu.add(theActionManager.get("selectnone"));
    edit_menu.addSeparator();
    edit_menu.add(theActionManager.get("enlarge"));
    edit_menu.add(theActionManager.get("resetsize"));
    edit_menu.add(theActionManager.get("shrink"));
    edit_menu.addSeparator();
    edit_menu.add(theActionManager.get("highlight"));
    edit_menu.addSeparator();
    edit_menu.add(theActionManager.get("group"));
    edit_menu.add(theActionManager.get("ungroup"));
    edit_menu.addSeparator();
    edit_menu.add(theActionManager.get("placestructures"));

    return edit_menu;
    }

    private JMenu createViewMenu() {
    GraphicOptions view_opt = theCanvas.getWorkspace().getGraphicOptions();

    JMenu view_menu = new JMenu("View");
    view_menu.setMnemonic(KeyEvent.VK_V);
    

    // zoom 
    view_menu.add(theActionManager.get("zoomnone"));
    view_menu.add(theActionManager.get("zoomin"));
    view_menu.add(theActionManager.get("zoomout"));

    view_menu.addSeparator();
    
    // notation 
    JRadioButtonMenuItem last = null;
    ButtonGroup groupn = new ButtonGroup();    

    view_menu.add(last = new JRadioButtonMenuItem(theActionManager.get("notation=" + GraphicOptions.NOTATION_CFG)));
    last.setSelected(view_opt.NOTATION.equals(GraphicOptions.NOTATION_CFG));
    groupn.add(last);

    view_menu.add(last = new JRadioButtonMenuItem(theActionManager.get("notation=" + GraphicOptions.NOTATION_CFGBW)));
    last.setSelected(view_opt.NOTATION.equals(GraphicOptions.NOTATION_CFGBW));
    groupn.add(last);

    view_menu.add(last = new JRadioButtonMenuItem(theActionManager.get("notation=" + GraphicOptions.NOTATION_CFGLINK)));
    last.setSelected(view_opt.NOTATION.equals(GraphicOptions.NOTATION_CFGLINK));
    groupn.add(last);

    view_menu.add(last = new JRadioButtonMenuItem(theActionManager.get("notation=" + GraphicOptions.NOTATION_UOXF)));
    last.setSelected(view_opt.NOTATION.equals(GraphicOptions.NOTATION_UOXF));
    groupn.add(last);

    view_menu.add(last = new JRadioButtonMenuItem(theActionManager.get("notation=" + GraphicOptions.NOTATION_TEXT)));
    last.setSelected(view_opt.NOTATION.equals(GraphicOptions.NOTATION_TEXT));
    groupn.add(last);
    
    view_menu.addSeparator();

    // display 

    display_button_group = new ButtonGroup();
    display_models = new HashMap<String,ButtonModel>();  

    view_menu.add(last = new JRadioButtonMenuItem(theActionManager.get("display=" + GraphicOptions.DISPLAY_COMPACT)));
    last.setSelected(view_opt.DISPLAY.equals(GraphicOptions.DISPLAY_COMPACT));
    display_models.put(GraphicOptions.DISPLAY_COMPACT,last.getModel());
    display_button_group.add(last);

    view_menu.add(last = new JRadioButtonMenuItem(theActionManager.get("display=" + GraphicOptions.DISPLAY_NORMAL)));
    last.setSelected(view_opt.DISPLAY.equals(GraphicOptions.DISPLAY_NORMAL));
    display_models.put(GraphicOptions.DISPLAY_NORMAL,last.getModel());
    display_button_group.add(last);

    view_menu.add(last = new JRadioButtonMenuItem(theActionManager.get("display=" + GraphicOptions.DISPLAY_NORMALINFO)));
    last.setSelected(view_opt.DISPLAY.equals(GraphicOptions.DISPLAY_NORMALINFO));
    display_models.put(GraphicOptions.DISPLAY_NORMALINFO,last.getModel());
    display_button_group.add(last);

    view_menu.add(last = new JRadioButtonMenuItem(theActionManager.get("display=" + GraphicOptions.DISPLAY_CUSTOM)));
    last.setSelected(view_opt.DISPLAY.equals(GraphicOptions.DISPLAY_CUSTOM));
    display_models.put(GraphicOptions.DISPLAY_CUSTOM,last.getModel());
    display_button_group.add(last);

    //view_menu.add( lastcb = new JCheckBoxMenuItem(theActionManager.get("showinfo")) );
    //lastcb.setState(view_opt.SHOW_INFO);

    view_menu.addSeparator();

    // orientation

    view_menu.add(theActionManager.get("orientation"));

    view_menu.addSeparator();

    view_menu.add(theActionManager.get("displaysettings"));
    view_menu.add(theActionManager.get("reportsettings"));

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
    
    menu.addSeparator();

    menu.add(theActionManager.get("cut"));
    menu.add(theActionManager.get("copy"));
    menu.add(theActionManager.get("delete"));    

    menu.addSeparator();

    menu.add(theActionManager.get("enlarge"));
    menu.add(theActionManager.get("resetsize"));
    menu.add(theActionManager.get("shrink"));

    menu.addSeparator();

    menu.add(theActionManager.get("highlight"));

    menu.addSeparator();
    
    menu.add(theActionManager.get("group"));
    menu.add(theActionManager.get("ungroup"));

    return menu;
    }

    private JToolBar createToolBar() {
    
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    toolbar.add(theActionManager.get("open"));    
    toolbar.add(theActionManager.get("save"));
    toolbar.add(theActionManager.get("saveas"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("print"));
    
    toolbar.addSeparator();

    toolbar.add(theActionManager.get("undo"));
    toolbar.add(theActionManager.get("redo"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("cut"));
    toolbar.add(theActionManager.get("copy"));
    toolbar.add(theActionManager.get("delete"));
    toolbar.add(theActionManager.get("screenshot"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("enlarge"));
    toolbar.add(theActionManager.get("resetsize"));
    toolbar.add(theActionManager.get("shrink"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("highlight"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("zoomnone"));
    toolbar.add(theActionManager.get("zoomin"));
    toolbar.add(theActionManager.get("zoomout"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("orientation"));

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

    public void onUndo()  {
    try {
        theCanvas.getDocument().getUndoManager().undo();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    public void onRedo()  {
    try {
        theCanvas.getDocument().getUndoManager().redo();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }    
    
    public void onUpdate() {
    // ask for mode
    final String[] options = new String[] {
        "Update the report",
        "Update the peaklist",
        "Merge report and peaklist"};
    final int UPDATE_REPORT = 0;
    final int UPDATE_PEAKLIST = 1;
    final int MERGE = 2;

    int option = JOptionPane.showOptionDialog(this,"Select one of the following options","Update mode selection", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
    if( option==JOptionPane.CLOSED_OPTION )
        return;

    // search the correct peak annotation collection
    Glycan parent = theCanvas.getDocument().getParentStructure();    
    AnnotatedPeakList apl = theApplication.getWorkspace().getAnnotatedPeakList();

    int selected = -1;
    if( parent!=null ) {        
        for( int i=0; i<apl.getNoStructures(); i++ ) {
        if( parent.equalsStructure(apl.getStructure(i)) ) {
            if( selected== -1 ) 
            selected = i;
            else {
            // multiple profiles (strange but possible)
            selected = -1;
            break;
            }
        }
        }
    }
    
    // ask for the structure if none found
    if( selected==-1 ) {
        StructureSelectorDialog dlg = new StructureSelectorDialog(this,"Select parent","Select the structure corresponding\nto the current report",apl.getStructures(),false,theApplication.getWorkspace().getGlycanRenderer());
        
        dlg.setVisible(true);
        if( dlg.isCanceled() || dlg.getSelectedIndex()==-1 )
        return;
        
        selected = dlg.getSelectedIndex();    
    }

    if( option==UPDATE_REPORT )
        theCanvas.updateAnnotations(parent,apl.getPeakAnnotationCollection(selected),false);
    else if( option==UPDATE_PEAKLIST ) {
        AnnotationReportDocument doc = theCanvas.getDocument();
        apl.updateAnnotations(selected,doc.getStartMZ(),doc.getEndMZ(),doc.getPeakAnnotations(),false);
    }
    else if( option==MERGE ) {
        AnnotationReportDocument doc = theCanvas.getDocument();
        theCanvas.updateAnnotations(parent,apl.getPeakAnnotationCollection(selected),true);
        apl.updateAnnotations(selected,doc.getStartMZ(),doc.getEndMZ(),doc.getPeakAnnotations(),true);
    }    

    //
    //System.err.println("cannot find structure");
    }

    public void setNotation(String notation) {
    theCanvas.getWorkspace().setNotation(notation);    
    theCanvas.updateView();
    }

    public void setDisplay(String display) {
    theCanvas.getWorkspace().setDisplay(display);        
    theCanvas.updateView();
    }      

    public void onChangeOrientation() {
    theCanvas.getWorkspace().getGraphicOptions().ORIENTATION = (theCanvas.getWorkspace().getGraphicOptions().ORIENTATION+1)%4;

    theCanvas.updateView();
    }  

    private Icon getOrientationIcon() {
    int orientation = theCanvas.getWorkspace().getGraphicOptions().ORIENTATION;
    if( orientation==GraphicOptions.LR )
        return FileUtils.defaultThemeManager.getImageIcon("lr");
    if( orientation==GraphicOptions.RL )
        return FileUtils.defaultThemeManager.getImageIcon("rl");
    if( orientation==GraphicOptions.TB )
        return FileUtils.defaultThemeManager.getImageIcon("tb");       
    if( orientation==GraphicOptions.BT )
        return FileUtils.defaultThemeManager.getImageIcon("bt");
    return null;
    }    

    public void onChangeDisplaySettings() {
    GraphicOptions options = theCanvas.getWorkspace().getGraphicOptions();
    GraphicOptionsDialog dlg = new GraphicOptionsDialog(this,options);
    
    dlg.setVisible(true);
    if( dlg.getReturnStatus().equals("OK") ) {
        // update document and canvas
        theCanvas.getWorkspace().setDisplay(options.DISPLAY);        
        display_button_group.setSelected(display_models.get(options.DISPLAY),true);
        theCanvas.getDocument().fireDocumentChanged();        
    }
    }


    public void onChangeReportSettings() {
    AnnotationReportOptionsDialog dlg = new AnnotationReportOptionsDialog(this,theCanvas.getAnnotationReportOptions());
    
    dlg.setVisible(true);
    if( dlg.getReturnStatus().equals("OK") ) {
        // update document and canvas
        theCanvas.getDocument().fireDocumentChanged();

        // update default options
        thePlugin.getAnnotationReportOptions().setValues(theCanvas.getAnnotationReportOptions());
    }
    }

    public void updateTitle() {
    String title = FileHistory.getAbbreviatedName(theCanvas.getDocument().getFileName()) + " - Annotation report" ;
    if( theCanvas.getDocument().hasChanged() )
        title = "* " + title;
    setTitle(title);
    }

    public void onOpen() {
    theApplication.onOpen(null,theCanvas.getDocument(),false);
    }

    public void onSave() {
    theApplication.onSave(theCanvas.getDocument());        
    }

    public void onSaveAs() {
    theApplication.onSaveAs(theCanvas.getDocument());        
    }

    public void onPrint() {
    try {       
        PrinterJob pj = theCanvas.getWorkspace().getPrinterJob();
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

    else if( action.equals("open") )
        onOpen();
    else if( action.equals("save") )
        onSave();
    else if( action.equals("saveas") )
        onSaveAs();

    else if( action.equals("print") )
        onPrint();
    else if( action.equals("close") )
        this.setVisible(false);

    else if( action.equals("undo") )
        onUndo();
    else if( action.equals("redo") )
        onRedo();

    else if( action.equals("cut") )
        theCanvas.cut();
    else if( action.equals("copy") )
        theCanvas.copy();
    else if( action.equals("delete") )
        theCanvas.delete();
    else if( action.equals("screenshot") )
        theCanvas.getScreenshot();

    else if( action.equals("update") )
        onUpdate();
    
    else if( action.equals("selectall") )
        theCanvas.selectAll();
    else if( action.equals("selectnone") )
        theCanvas.resetSelection();

    else if( action.equals("enlarge") )
        theCanvas.rescaleSelections(6./5.);
    else if( action.equals("resetsize") )
        theCanvas.resetSelectionsScale();
    else if( action.equals("shrink") )
        theCanvas.rescaleSelections(5./6.);

    else if( action.equals("highlight") )
        theCanvas.highlightSelections();

    else if( action.equals("group") )
        theCanvas.groupSelections();
    else if( action.equals("ungroup") )
        theCanvas.ungroupSelections();
    else if( action.equals("placestructures") )
        theCanvas.placeStructures();

    // display
    if( action.equals("zoomnone") )
        onZoomNone();
    else if( action.equals("zoomin") )
        onZoomIn();
    else if( action.equals("zoomout") )
        onZoomOut();
    else if( action.equals("notation") ) 
        setNotation(param);          
    else if( action.equals("display") ) 
        setDisplay(param);              
    else if( action.equals("displaysettings") ) 
        onChangeDisplaySettings();
    else if( action.equals("reportsettings") ) 
        onChangeReportSettings();
    else if( action.equals("orientation") ) 
        onChangeOrientation();

    updateActions();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    if( MouseUtils.isPopupTrigger(e) ) {       
        theCanvas.enforceSelection(e.getPoint());
        createPopupMenu().show(theCanvas, e.getX(), e.getY());   
    }
    }

    public void mouseReleased(MouseEvent e) {
    if( MouseUtils.isPopupTrigger(e) ) {       
        theCanvas.enforceSelection(e.getPoint());
        createPopupMenu().show(theCanvas, e.getX(), e.getY());   
    }
    }

    public void mouseClicked(MouseEvent e) {          
    }

    public void selectionChanged(AnnotationReportCanvas.SelectionChangeEvent e) {    
    updateActions();
    }    

    public void documentInit(BaseDocument.DocumentChangeEvent e) {
    updateTitle();
    updateActions();
    }

    public void documentChanged(BaseDocument.DocumentChangeEvent e) {
    updateTitle();
    updateActions();
    }
}
