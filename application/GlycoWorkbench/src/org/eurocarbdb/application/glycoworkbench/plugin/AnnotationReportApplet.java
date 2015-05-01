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

import org.eurocarbdb.application.glycoworkbench.plugin.reporting.*;
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
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;


public class AnnotationReportApplet extends JApplet implements ActionListener, MouseListener, AnnotationReportCanvas.SelectionChangeListener, BaseDocument.DocumentChangeListener {
    
    // singletons
    private ActionManager theActionManager;  
 
    private JMenuBar theMenuBar;
    private JToolBar theToolBar;
    private AnnotationReportCanvas theCanvas;
    private JScrollPane theScrollPane;
    
    private ButtonGroup display_button_group = null;
    protected HashMap<String,ButtonModel> display_models = null;  

    public AnnotationReportApplet() {
    }
    
    public void paint(Graphics g) {
      
         super.paint(g);

         // Draw a 2-pixel border
     g.setColor(Color.black);

         int width = getSize().width;  // Width of the applet.
         int height = getSize().height; // Height of the applet.
         g.drawRect(0,0,width-1,height-1);
         g.drawRect(1,1,width-3,height-3);
    }

    public Insets getInsets() {
    return new Insets(2,2,2,2);
    }


    public void init() {
    super.init();

    LogUtils.setGraphicalReport(true);

    // create singletons 
    theActionManager = new ActionManager();

    // create interface
    createUI();
    }

    public void start() {
    super.start();
    }

    public void stop() {
    super.stop();
    }

    public void destroy() {
    super.destroy();
    }

    public void createUI() {

    // set the layout
    getContentPane().setLayout(new BorderLayout());

    // set singletons
    theCanvas = new AnnotationReportCanvas(new AnnotationReportDocument(),false);
    theCanvas.getDocument().addDocumentChangeListener(this);

    // initialize the action set
    createActions();

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

        setSize(900,700);       
    
    updateActions();    
    }

    private void createActions() {    

    //file
    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "ctrl P",this);

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

    theActionManager.add("displaysettings",ThemeManager.getEmptyIcon(ICON_SIZE.TINY),"Change structure display settings",KeyEvent.VK_S,"",this);
    theActionManager.add("reportsettings",ThemeManager.getEmptyIcon(ICON_SIZE.TINY),"Change report display settings",KeyEvent.VK_R,"",this);
    }

    private void updateActions() {

    theActionManager.get("undo").setEnabled(theCanvas.getDocument().getUndoManager().canUndo());
    theActionManager.get("redo").setEnabled(theCanvas.getDocument().getUndoManager().canRedo());

    theActionManager.get("cut").setEnabled(theCanvas.hasSelection());
    theActionManager.get("copy").setEnabled(theCanvas.hasSelection());
    theActionManager.get("delete").setEnabled(theCanvas.hasSelection());

    theActionManager.get("enlarge").setEnabled(theCanvas.hasSelection());
    theActionManager.get("resetsize").setEnabled(theCanvas.hasSelection());
    theActionManager.get("shrink").setEnabled(theCanvas.hasSelection());

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

    file_menu.add(theActionManager.get("print"));

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
    
    menu.add(theActionManager.get("group"));
    menu.add(theActionManager.get("ungroup"));

    return menu;
    }

    private JToolBar createToolBar() {
    
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

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

    toolbar.add(theActionManager.get("zoomnone"));
    toolbar.add(theActionManager.get("zoomin"));
    toolbar.add(theActionManager.get("zoomout"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("orientation"));

    return toolbar;
    }

    // actions

    public void setDocument(String src) {
    try {
        theCanvas.getDocument().fromXMLString(src);
    }
    catch(Exception e) {
        LogUtils.report(e);
        theCanvas.getDocument().init();
    }
    }

    public void setDocument(byte[] src) {
    try {
        theCanvas.getDocument().fromXMLString(src);
    }
    catch(Exception e) {
        LogUtils.report(e);
        theCanvas.getDocument().init();
    }
    }

    public String getDocument() {
    return theCanvas.getDocument().toXMLString();
    }

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
    GraphicOptionsDialog dlg = new GraphicOptionsDialog(null,options);
    
    dlg.setVisible(true);
    if( dlg.getReturnStatus().equals("OK") ) {
        theCanvas.getWorkspace().setDisplay(options.DISPLAY);        
        display_button_group.setSelected(display_models.get(options.DISPLAY),true);
        theCanvas.repaint();
    }
    }


    public void onChangeReportSettings() {
    AnnotationReportOptionsDialog dlg = new AnnotationReportOptionsDialog(null,theCanvas.getAnnotationReportOptions());
    
    dlg.setVisible(true);
    if( dlg.getReturnStatus().equals("OK") ) 
        theCanvas.updateView();    
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

    // events
    
    public void actionPerformed(ActionEvent e) {

    String action = GlycanAction.getAction(e);
    String param  = GlycanAction.getParam(e);    
    
    if( action.equals("print") )
        onPrint();

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
    updateActions();
    }

    public void documentChanged(BaseDocument.DocumentChangeEvent e) {
    updateActions();
    }
}
