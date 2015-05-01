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

import org.eurocarbdb.application.glycanbuilder.*;

import org.eurocarbdb.application.glycoworkbench.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.PrinterJob;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.Range;
import org.jfree.chart.plot.XYPlot;

public class PeakAnnotationCalibrationPanel extends DocumentPanel<AnnotatedPeakList> implements ActionListener, ComponentListener, GlycanWorkspace.Listener, BaseDocument.DocumentChangeListener {        

    private static final int MOD_MASK = MouseEvent.CTRL_MASK | MouseEvent.SHIFT_MASK | MouseEvent.ALT_MASK | MouseEvent.META_MASK | MouseEvent.ALT_GRAPH_MASK;    
    
    // components

    protected JLabel      theStructure;

    protected DefaultXYDataset theDataset;
    protected XYPlot      thePlot;
    protected JFreeChart  theChart;
    protected ChartPanel  theChartPanel;

    protected JToolBar    theToolBarDocument;
    protected JToolBar    theToolBarEdit;

    // data
    protected int current_ind = 0;
    protected String accuracy_unit;
  
    // actions    
    
    protected JButton accunit_button = null;

    protected boolean was_moving = false;
    protected boolean is_moving = false;
    protected Point2D mouse_start_point = null;
    protected Cursor hand_cursor = null;
   
    //

    public PeakAnnotationCalibrationPanel() {
    super();
    }

    protected void initSingletons() {
    super.initSingletons();
    accuracy_unit = "da";
    }

    protected void initComponents() {
    setLayout(new BorderLayout());    

    // create structure viewer    
    theStructure = new JLabel();
    theStructure.setBorder(new BevelBorder(BevelBorder.RAISED));
    add(theStructure,BorderLayout.NORTH);                
    
    // create chart
    theDataset = new DefaultXYDataset();
    theChart = org.jfree.chart.ChartFactory.createScatterPlot("Calibration", "m/z ratio", "accuracy (Da)", theDataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, true, false, false);
    thePlot = (XYPlot)theChart.getPlot();        
    theChartPanel = new ChartPanel(theChart);
    theChartPanel.setDomainZoomable(true);
    theChartPanel.setRangeZoomable(true);
    theChartPanel.setPopupMenu(null);    
    add(theChartPanel,BorderLayout.CENTER);
    
    // create toolbar
    JPanel theToolBarPanel = new JPanel(new BorderLayout());
    theToolBarDocument = createToolBarDocument(); 
    theToolBarEdit = createToolBarEdit(); 
    theToolBarPanel.add(theToolBarDocument, BorderLayout.NORTH);
    theToolBarPanel.add(theToolBarEdit, BorderLayout.CENTER);
    add(theToolBarPanel,BorderLayout.SOUTH);

    // load cursors
    hand_cursor = FileUtils.createCursor("hand");
    }

    protected void finalSettings() {
    super.finalSettings();
    
    theChartPanel.addMouseMotionListener( new MouseMotionAdapter() {
        public void mouseDragged(MouseEvent e) {
            onMouseDragged(e);
        }
        });

    theChartPanel.addMouseListener( new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
            onMousePressed(e);
        }                

        public void mouseReleased(MouseEvent e) {
            onMouseReleased(e);
        }

        public void mouseClicked(MouseEvent e) {      
            onMouseClicked(e);
        }
        });    
    }

    public AnnotatedPeakList getDocumentFromWorkspace(GlycanWorkspace workspace) {
    return ( workspace!=null ) ?workspace.getAnnotatedPeakList() :null;
    }
    
    public void setDocumentFromWorkspace(GlycanWorkspace workspace) {
    if( theDocument!=null )
        theDocument.removeDocumentChangeListener(this);
    
    theDocument = getDocumentFromWorkspace(workspace);
    if( theDocument==null )
        theDocument = new AnnotatedPeakList();

    theDocument.addDocumentChangeListener(this);

    current_ind = 0;    
    updateView();
    updateActions();
    }   

    protected void createActions() {
    theActionManager.add("accunit=da",FileUtils.defaultThemeManager.getImageIcon("da"),"Show accuracy in Da",-1, "",this);
    theActionManager.add("accunit=ppm",FileUtils.defaultThemeManager.getImageIcon("ppm"),"Show accuracy in PPM",-1, "",this);

    theActionManager.add("new",FileUtils.defaultThemeManager.getImageIcon("new"),"Clear",KeyEvent.VK_N, "",this);

    theActionManager.add("close",FileUtils.defaultThemeManager.getImageIcon("close"),"Close structure",KeyEvent.VK_S, "",this);
    theActionManager.add("last",FileUtils.defaultThemeManager.getImageIcon("last"),"Last structure",KeyEvent.VK_L, "",this);
    theActionManager.add("next",FileUtils.defaultThemeManager.getImageIcon("next"),"Next structure",KeyEvent.VK_N, "",this);

    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "",this);

    //theActionManager.add("undo",FileUtils.defaultThemeManager.getImageIcon("undo"),"Undo",KeyEvent.VK_U, "",this);
    //theActionManager.add("redo",FileUtils.defaultThemeManager.getImageIcon("redo"),"Redo", KeyEvent.VK_R, "",this);

    theActionManager.add("arrow",FileUtils.defaultThemeManager.getImageIcon("arrow"),"Activate zoom",-1, "",this);
    theActionManager.add("hand",FileUtils.defaultThemeManager.getImageIcon("hand"),"Activate moving",-1, "",this);

    theActionManager.add("zoomnone",FileUtils.defaultThemeManager.getImageIcon("zoomnone"),"Reset zoom",-1, "",this);
    theActionManager.add("zoomin",FileUtils.defaultThemeManager.getImageIcon("zoomin"),"Zoom in",-1, "",this);
    theActionManager.add("zoomout",FileUtils.defaultThemeManager.getImageIcon("zoomout"),"Zoom out",-1, "",this);
    }

    protected void updateActions() {
    theActionManager.get("close").setEnabled(theDocument.getNoStructures()!=0);
    theActionManager.get("last").setEnabled(current_ind>0);
    theActionManager.get("next").setEnabled(current_ind<(theDocument.getNoStructures()-1));

    //theActionManager.get("undo").setEnabled(theDocument.getUndoManager().canUndo());
    //theActionManager.get("redo").setEnabled(theDocument.getUndoManager().canRedo());

    theActionManager.get("arrow").setEnabled(is_moving);
    theActionManager.get("hand").setEnabled(!is_moving);
    
    theActionManager.get("zoomnone").setEnabled(true);
    theActionManager.get("zoomin").setEnabled(true);
    theActionManager.get("zoomout").setEnabled(true);
    }

    private JToolBar createToolBarDocument() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
      
    toolbar.add(theActionManager.get("last"));
    toolbar.add(theActionManager.get("close"));    
    toolbar.add(theActionManager.get("next"));

    toolbar.addSeparator();
    
    toolbar.add(accunit_button = new JButton(theActionManager.get("accunit=ppm")) );
    accunit_button.setText(null);
    toolbar.add(theActionManager.get("new"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("print"));


    //toolbar.addSeparator();

    //toolbar.add(theActionManager.get("undo"));
    //toolbar.add(theActionManager.get("redo"));
    
    return toolbar;
    }

    private JToolBar createToolBarEdit() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("arrow"));
    toolbar.add(theActionManager.get("hand"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("zoomnone"));
    toolbar.add(theActionManager.get("zoomin"));
    toolbar.add(theActionManager.get("zoomout"));

    return toolbar;
    }            


    protected JPopupMenu createPopupMenu() {

    JPopupMenu menu = new JPopupMenu();

    menu.add(theActionManager.get("zoomnone"));
    menu.add(theActionManager.get("zoomin"));
    menu.add(theActionManager.get("zoomout"));

    return menu;
    }

    //-----------------
    // data

    public boolean isChartEmpty() {
    return (theDocument.getNoStructures()==0 || theDocument.getNoAnnotatedPeaks(current_ind)==0);
    }

    public double screenToDataX(double length) {
    Rectangle2D data_area = theChartPanel.getScreenDataArea();
    double mz_unit  = thePlot.getDomainAxis().lengthToJava2D(1.,data_area,thePlot.getDomainAxisEdge());
    return length/mz_unit;
    }

    public double screenToDataY(double length) {
    Rectangle2D data_area = theChartPanel.getScreenDataArea();
    double int_unit  = thePlot.getRangeAxis().lengthToJava2D(1.,data_area,thePlot.getRangeAxisEdge());
    return length/int_unit;
    }

    public Point2D screenToDataCoords(Point2D p) {

    Rectangle2D data_area = theChartPanel.getScreenDataArea();
    double x = thePlot.getDomainAxis().java2DToValue(p.getX(),data_area,thePlot.getDomainAxisEdge());
    double y = thePlot.getRangeAxis().java2DToValue(p.getY(),data_area,thePlot.getRangeAxisEdge());
    return new Point2D.Double(x,y);
    }


    public double screenToDataCoordX(double x) {
    Rectangle2D data_area = theChartPanel.getScreenDataArea();
    return thePlot.getDomainAxis().java2DToValue(x,data_area,thePlot.getDomainAxisEdge());
    }

    public double screenToDataCoordY(double y) {
    Rectangle2D data_area = theChartPanel.getScreenDataArea();
    return thePlot.getRangeAxis().java2DToValue(y,data_area,thePlot.getRangeAxisEdge());
    }


    public void clear() {
    current_ind = 0;
    theDocument.clear();
    }              
        

    //-----------
    // Visualization

    protected void updateData() {
    current_ind = Math.min(current_ind,theDocument.getNoStructures()-1);    
    current_ind = Math.max(current_ind,0);
    }


    protected void updateView() {
    if( theStructure!=null ) {
        if( theDocument.getNoStructures()>0 )
        theStructure.setIcon(new ImageIcon(theWorkspace.getGlycanRenderer().getImage(theDocument.getStructure(current_ind),false,true,true,0.667)));
        else
        theStructure.setIcon(null);
    }
    
    // update data
    if( theDocument.getNoStructures()>0 ) {
        theDataset.removeSeries("best");
        theDataset.removeSeries("all");
        if( accuracy_unit.equals("ppm") ) {
        theDataset.addSeries("best", theDocument.getBestCalibrationDataPPM(current_ind));
        theDataset.addSeries("all", theDocument.getCalibrationDataPPM(current_ind));
        }
        else {
        theDataset.addSeries("best", theDocument.getBestCalibrationData(current_ind));
        theDataset.addSeries("all", theDocument.getCalibrationData(current_ind));
        }
    }
    
    // update axis
    if( accuracy_unit.equals("ppm") ) 
        thePlot.getRangeAxis().setLabel("accuracy (PPM)");
    else
        thePlot.getRangeAxis().setLabel("accuracy (Da)");

    theChartPanel.setDomainZoomable(!is_moving);
    theChartPanel.setRangeZoomable(!is_moving);

    onZoomNone();
    }   
    
    //-----------------
    // actions    

    public void closeCurrent() {
    if( theDocument.getNoStructures()>0 ) {
        int old_ind = current_ind;
        current_ind = Math.min(current_ind,theDocument.getNoStructures()-2);        
        current_ind = Math.max(current_ind,0);

        // long action
        theApplication.haltInteractions();
        theDocument.removePeakAnnotationsAt(old_ind);
        theApplication.restoreInteractions();
    }
    }

    public void showLast() {
    if( theDocument.getNoStructures()>0 && current_ind>0 ) {
        current_ind--;
        updateView();
    }
    }
    
    public void showNext() {
    if( theDocument.getNoStructures()>0 && current_ind<(theDocument.getNoStructures()-1) ) {
        current_ind++;
        updateView();
    }
    }      

    public void showStructure(int s_ind) {
    if( s_ind>=0 && s_ind<theDocument.getNoStructures() ) {
        current_ind = s_ind;
        updateView();
    }
    }
    
    public void onNew() {
    clear();
    }

    public void onPrint() {
    PrinterJob pj = theWorkspace.getPrinterJob();
    if( pj==null )
        return;

    try {            
        pj.setPrintable(theChartPanel);
        if( pj.printDialog() ) 
        pj.print();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    /*public void onUndo()  {
    try {
        theDocument.getUndoManager().undo();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }

    public void onRedo()  {
    try {
        theDocument.getUndoManager().redo();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    } 
    */         

     
    public void onActivateZooming() {
    is_moving = false;
    theChartPanel.setCursor(Cursor.getDefaultCursor());      
    theChartPanel.setDomainZoomable(true);
    theChartPanel.setRangeZoomable(true);
    }

    public void onActivateMoving() {
    is_moving = true;
    theChartPanel.setCursor(hand_cursor);
    theChartPanel.setRangeZoomable(false);
    theChartPanel.setDomainZoomable(false);
    }

    public void onZoomNone() {
    if( !isChartEmpty() ) {
        Range mz_range = theDocument.getMZRange();
        if( mz_range.getLength()==0. ) 
        mz_range = new Range(mz_range.getLowerBound()-10,mz_range.getLowerBound()+10);
        thePlot.getDomainAxis().setRange(mz_range);
        
        if( accuracy_unit.equals("da") ) {
        Range acc_range = theDocument.getAccuracyRange(current_ind);
        if( acc_range.getLength()==0. ) 
            acc_range = new Range(acc_range.getLowerBound()-0.1,acc_range.getLowerBound()+0.1);
        thePlot.getRangeAxis().setRange(acc_range);
        }
        else {
        Range acc_range = theDocument.getAccuracyRangePPM(current_ind);
        if( acc_range.getLength()==0. ) 
            acc_range = new Range(acc_range.getLowerBound()-100,acc_range.getLowerBound()+100);
        thePlot.getRangeAxis().setRange(acc_range);
        }
    }
    else {
        thePlot.getDomainAxis().setRange(new Range(0.,1.));
        if( accuracy_unit.equals("da") )     
        thePlot.getRangeAxis().setRange(new Range(-1.,1.));
        else 
        thePlot.getRangeAxis().setRange(new Range(-1000.,1000.));
    }
    }

    public void onZoomIn() {
    thePlot.getDomainAxis().resizeRange(0.5);
    thePlot.getRangeAxis().resizeRange(0.5);
    }

    public void onZoomOut() {
    thePlot.getDomainAxis().resizeRange(2.0);
    thePlot.getRangeAxis().resizeRange(2.0);        
    }
        
    public void onSetAccuracyUnit(String accunit) {
    if( accunit.equals("da") ) {
        accuracy_unit = "da";
        accunit_button.setAction(theActionManager.get("accunit=ppm"));
        accunit_button.setText(null);
        updateView();
    }
    else if( accunit.equals("ppm") ) {
        accuracy_unit = "ppm";
        accunit_button.setAction(theActionManager.get("accunit=da"));
        accunit_button.setText(null);
        updateView();
    }
    }

    //-----------
    // listeners

    public void actionPerformed(ActionEvent e) {

    String action = GlycanAction.getAction(e);
    String param  = GlycanAction.getParam(e);

    if( action.equals("last") )
        showLast();
    else if( action.equals("next") )
        showNext();
    else if( action.equals("close") )
        closeCurrent();

    else if( action.equals("accunit") )
        onSetAccuracyUnit(param);

    else if( action.equals("new") )
        onNew();

    else if( action.equals("print") )
        onPrint();

    /*else if( action.equals("undo") )
        onUndo();
    else if( action.equals("redo") )
        onRedo();
    */

    else if( action.equals("arrow") )
        onActivateZooming();
    else if( action.equals("hand") )
        onActivateMoving();

    else if( action.equals("zoomnone") )
        onZoomNone();
    else if( action.equals("zoomin") )
        onZoomIn();
    else if( action.equals("zoomout") )
        onZoomOut();

    updateActions();
    }   


    public void onMousePressed(MouseEvent e) {
    was_moving = is_moving;
    if( e.getButton()==e.BUTTON1 && theChartPanel.getScreenDataArea().contains(e.getPoint()) ) {
        mouse_start_point = e.getPoint();
        if( (e.getModifiers() & MOD_MASK)==e.SHIFT_MASK ) 
        onActivateMoving();                
    }
    else
        mouse_start_point = null;
    }        

    private void onMouseDragged(MouseEvent e) {
    if( mouse_start_point!=null ) {
        if( is_moving ) {
        // moving
        double mz_delta = screenToDataX(mouse_start_point.getX() - e.getPoint().getX());
        double acc_delta = screenToDataY(e.getPoint().getY() - mouse_start_point.getY());

        // update mz
        if( mz_delta>0. ) {
            double old_upper_bound = thePlot.getDomainAxis().getUpperBound();
            double old_lower_bound = thePlot.getDomainAxis().getLowerBound();
            double new_upper_bound = old_upper_bound+mz_delta;
            double new_lower_bound = old_lower_bound + new_upper_bound - old_upper_bound;
            
            thePlot.getDomainAxis().setRange(new Range(new_lower_bound,new_upper_bound));
        }
        else {
            double old_upper_bound = thePlot.getDomainAxis().getUpperBound();
            double old_lower_bound = thePlot.getDomainAxis().getLowerBound();
            double new_lower_bound = old_lower_bound+mz_delta;
            double new_upper_bound = old_upper_bound + new_lower_bound - old_lower_bound;
            
            thePlot.getDomainAxis().setRange(new Range(new_lower_bound,new_upper_bound));            
        }

        // update acc
        if( acc_delta>0. ) {
            double old_upper_bound = thePlot.getRangeAxis().getUpperBound();
            double old_lower_bound = thePlot.getRangeAxis().getLowerBound();
            double new_upper_bound = old_upper_bound+acc_delta;
            double new_lower_bound = old_lower_bound + new_upper_bound - old_upper_bound;
            
            thePlot.getRangeAxis().setRange(new Range(new_lower_bound,new_upper_bound));
        }
        else {
            double old_upper_bound = thePlot.getRangeAxis().getUpperBound();
            double old_lower_bound = thePlot.getRangeAxis().getLowerBound();
            double new_lower_bound = old_lower_bound+acc_delta;
            double new_upper_bound = old_upper_bound + new_lower_bound - old_lower_bound;
            
            thePlot.getRangeAxis().setRange(new Range(new_lower_bound,new_upper_bound));
        }
        
        mouse_start_point = e.getPoint();
        }                   
    }
    }        
    
    public void onMouseReleased(MouseEvent e) {    
    // restore zooming
    if( !was_moving && is_moving )
        onActivateZooming();

    mouse_start_point = null;
    }
        
    public void onMouseClicked(MouseEvent e) {      

    // find peak under mouse
    if( e.getButton()==MouseEvent.BUTTON3 &&  e.getClickCount()==1 && (e.getModifiers() & MOD_MASK)==e.BUTTON3_MASK ) {        
        // open popup
        createPopupMenu().show(theChartPanel, e.getX(), e.getY()); 
    }
    }        
  
}
