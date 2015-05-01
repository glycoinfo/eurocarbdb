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

import java.io.File;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.PrinterJob;
import java.util.*;
import javax.swing.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;



public class SpectraPanel extends DocumentPanel<SpectraDocument> implements ActionListener, MouseListener, MouseMotionListener {        

    public interface SelectionChangeListener {    
    public void selectionChanged(SelectionChangeEvent e);    
    }

    public static class SelectionChangeEvent {
    private SpectraPanel src;

    public SelectionChangeEvent(SpectraPanel _src) {
        src = _src;
    }

    public SpectraPanel getSource() {
        return src;
    }       
    }

    // components
    protected AnnotatedPeakList theSearchResults;
    protected DefaultXYDataset theDataset;
    protected DefaultXYDataset theIsotopesDataset;
    protected XYPlot      thePlot;
    protected JFreeChart  theChart;
    protected ChartPanel  theChartPanel;

    protected JToolBar    theToolBar;

    // data
    protected int current_ind;
    protected TreeMap<Double,Double> visibleData;
    
    // selection
    protected Peak current_peak;
    protected TreeSet<Peak> selected_peaks;

    // actions    
    protected String shown_mslevel;    
    protected JButton mslevel_button;

    protected boolean was_moving;
    protected boolean is_moving;
    protected Point2D mouse_start_point;
    protected Rectangle2D zoom_rectangle;    
    protected Cursor hand_cursor;

    protected GlycanAction ms_action;
    protected GlycanAction msms_action;

    protected boolean update_isotope_curves;
    protected boolean automatic_update_isotope_curves;
    protected boolean show_all_isotopes;
    protected JButton isotopes_button;
    protected JButton ftmode_button;

    // 
    protected Vector<SelectionChangeListener> listeners;

    //

    public SpectraPanel()  {
    super();
    }

    protected void initSingletons() {
    super.initSingletons();

    theSearchResults=null;

    selected_peaks = new TreeSet<Peak>();
    shown_mslevel = "msms";    
    current_ind = 0;
    visibleData = new TreeMap<Double,Double>();    

    update_isotope_curves = false;
    automatic_update_isotope_curves = true;
    show_all_isotopes = false;
    }

    protected void initComponents() {
    setLayout(new BorderLayout());

    // create chart
    theDataset = new DefaultXYDataset();
    theIsotopesDataset = new DefaultXYDataset();

    theChart = org.jfree.chart.ChartFactory.createScatterPlot("Spectrum", "m/z ratio", "Intensity", theDataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, false, false, false);
    thePlot = (XYPlot)theChart.getPlot();
    thePlot.setRenderer(new StandardXYItemRenderer(StandardXYItemRenderer.LINES));

    thePlot.setDataset(1, theIsotopesDataset);
    thePlot.setRenderer(1,new StandardXYItemRenderer(StandardXYItemRenderer.LINES));

    theChartPanel = new ChartPanel(theChart);    
    theChartPanel.setDomainZoomable(false);
    theChartPanel.setRangeZoomable(false);    
    theChartPanel.setPopupMenu(null);
    
    //TODO: PAINT-RESTORE
    add(theChartPanel,BorderLayout.CENTER);
    
    // create toolbar
    theToolBar = createToolBar(); 
    //TODO: PAINT-RESTORE
    add(theToolBar,BorderLayout.SOUTH);    
    }

    protected void finalSettings() {

    // load cursors
    hand_cursor = FileUtils.createCursor("hand");    

    // add listeners
    listeners = new Vector<SelectionChangeListener>();

    thePlot.getDomainAxis().addChangeListener(new org.jfree.chart.event.AxisChangeListener() {
        public void axisChanged(org.jfree.chart.event.AxisChangeEvent event) {
            updateChart();
        }
        });

    theChartPanel.addMouseMotionListener( this );
    theChartPanel.addMouseListener( this );

    was_moving = false;
    is_moving = false;
    mouse_start_point = null;
    zoom_rectangle = null;    
    
    super.finalSettings();
    }   

    public SpectraDocument getDocumentFromWorkspace(GlycanWorkspace workspace) {
    return ( workspace!=null ) ?workspace.getSpectra() :null;
    }
   

    public void setDocumentFromWorkspace(GlycanWorkspace workspace) {
    if( theDocument!=null ) 
        theDocument.removeDocumentChangeListener(this);
    if( theSearchResults!=null )
        theSearchResults.removeDocumentChangeListener(this);
    
    theDocument = getDocumentFromWorkspace(workspace);
    if( theDocument==null )
        theDocument = new SpectraDocument();
    theSearchResults = (workspace!=null) ?workspace.getSearchResults() : null;

    theDocument.addDocumentChangeListener(this);
    if( theSearchResults!=null )
        theSearchResults.addDocumentChangeListener(this);

    current_ind = 0;    
    updateView();
    updateActions();
    }     


    protected void createActions() {
    theActionManager.add("mslevel=ms",FileUtils.defaultThemeManager.getImageIcon("msms"),"Change current scan level",-1, "",this);
    theActionManager.add("mslevel=msms",FileUtils.defaultThemeManager.getImageIcon("ms"),"Change current scan level",-1, "",this);

    theActionManager.add("updateisotopecurves=true",FileUtils.defaultThemeManager.getImageIcon("isotopesoff"),"Automatic computation of isotopic distributions inactive",-1, "",this);
    theActionManager.add("updateisotopecurves=false",FileUtils.defaultThemeManager.getImageIcon("isotopeson"),"Automatic computation of isotopic distributions active",-1, "",this);

    theActionManager.add("showallisotopes=true",FileUtils.defaultThemeManager.getImageIcon("ftmodeoff"),"FTICR mode inactive",-1, "",this);
    theActionManager.add("showallisotopes=false",FileUtils.defaultThemeManager.getImageIcon("ftmodeon"),"FTICR mode active",-1, "",this);

    theActionManager.add("close",FileUtils.defaultThemeManager.getImageIcon("close"),"Close structure",KeyEvent.VK_S, "",this);
    theActionManager.add("previous",FileUtils.defaultThemeManager.getImageIcon("previous"),"Previous structure",KeyEvent.VK_L, "",this);
    theActionManager.add("next",FileUtils.defaultThemeManager.getImageIcon("next"),"Next structure",KeyEvent.VK_N, "",this);

    theActionManager.add("edit",FileUtils.defaultThemeManager.getImageIcon("edit"),"Edit scan data",-1, "",this);

    theActionManager.add("new",FileUtils.defaultThemeManager.getImageIcon("new"),"Clear",KeyEvent.VK_N, "",this);
    theActionManager.add("openspectra",FileUtils.defaultThemeManager.getImageIcon("openspectra"),"Open Spectra",KeyEvent.VK_O, "",this);

    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "",this);

    theActionManager.add("addpeaks",FileUtils.defaultThemeManager.getImageIcon("addpeaks"),"Add selected peaks to list",-1, "",this);
    theActionManager.add("annotatepeaks",FileUtils.defaultThemeManager.getImageIcon("annotatepeaks"),"Find possible annotations for selected peaks",-1, "",this);
    theActionManager.add("baselinecorrection",FileUtils.defaultThemeManager.getImageIcon("baseline"),"Baseline correction of current spectrum", -1, "", this);
    theActionManager.add("noisefilter",FileUtils.defaultThemeManager.getImageIcon("noisefilter"),"Filter noise in current spectrum", -1, "", this);
    theActionManager.add("centroid",FileUtils.defaultThemeManager.getImageIcon("centroid"),"Compute peak centroids",-1, "",this);

    theActionManager.add("arrow",FileUtils.defaultThemeManager.getImageIcon("arrow"),"Activate zoom",-1, "",this);
    theActionManager.add("hand",FileUtils.defaultThemeManager.getImageIcon("hand"),"Activate moving",-1, "",this);

    theActionManager.add("zoomnone",FileUtils.defaultThemeManager.getImageIcon("zoomnone"),"Reset zoom",-1, "",this);
    theActionManager.add("zoomin",FileUtils.defaultThemeManager.getImageIcon("zoomin"),"Zoom in",-1, "",this);
    theActionManager.add("zoomout",FileUtils.defaultThemeManager.getImageIcon("zoomout"),"Zoom out",-1, "",this);
    }

    protected void updatePeakActions() {
    if( ms_action==null && theApplication.getPluginManager().getMsPeakActions().size()>0 ) 
        ms_action = theApplication.getPluginManager().getMsPeakActions().iterator().next();

    if( msms_action==null && theApplication.getPluginManager().getMsMsPeakActions().size()>0 ) 
        msms_action = theApplication.getPluginManager().getMsMsPeakActions().iterator().next();
    }

    protected void updateActions() {
    theActionManager.get("close").setEnabled(theDocument.getNoScans()!=0);
    theActionManager.get("previous").setEnabled(current_ind>0);
    theActionManager.get("next").setEnabled(current_ind<(theDocument.getNoScans()-1));

    theActionManager.get("arrow").setEnabled(is_moving);
    theActionManager.get("hand").setEnabled(!is_moving);

    theActionManager.get("edit").setEnabled(theDocument.getNoScans()>0);

    theActionManager.get("zoomnone").setEnabled(theDocument.getNoScans()>0);
    theActionManager.get("zoomin").setEnabled(theDocument.getNoScans()>0);
    theActionManager.get("zoomout").setEnabled(theDocument.getNoScans()>0);

    theActionManager.get("addpeaks").setEnabled(selected_peaks.size()>0);
    theActionManager.get("annotatepeaks").setEnabled(selected_peaks.size()>0);

    theActionManager.get("baselinecorrection").setEnabled(theDocument.getNoScans()!=0);
    theActionManager.get("noisefilter").setEnabled(theDocument.getNoScans()!=0);
    theActionManager.get("centroid").setEnabled(theDocument.getNoScans()!=0);

    updateMsLevel();
    }

    protected void updateData() {
    }

    private JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
      
    toolbar.add(theActionManager.get("previous"));
    toolbar.add(theActionManager.get("close"));    
    toolbar.add(theActionManager.get("next"));

    toolbar.addSeparator();
    
    toolbar.add(theActionManager.get("new"));
    toolbar.add(theActionManager.get("openspectra"));
    toolbar.add(theActionManager.get("edit"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("print"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("arrow"));
    toolbar.add(theActionManager.get("hand"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("zoomnone"));
    toolbar.add(theActionManager.get("zoomin"));
    toolbar.add(theActionManager.get("zoomout"));

    toolbar.addSeparator();

    toolbar.add(mslevel_button = new JButton(theActionManager.get("mslevel=msms")) );
    mslevel_button.setText(null);
    toolbar.add(theActionManager.get("addpeaks"));
    toolbar.add(theActionManager.get("annotatepeaks"));

    toolbar.addSeparator();

    toolbar.add(theActionManager.get("noisefilter"));
    toolbar.add(theActionManager.get("baselinecorrection"));
    toolbar.add(theActionManager.get("centroid"));
    toolbar.add(isotopes_button = new JButton(theActionManager.get("updateisotopecurves=false")) );
    isotopes_button.setText(null);
    toolbar.add(ftmode_button = new JButton(theActionManager.get("showallisotopes=true")) );
    ftmode_button.setText(null);

    return toolbar;
    }            

    private JPopupMenu createPopupMenu(boolean over_peak) {

    JPopupMenu menu = new JPopupMenu();

    if( over_peak ) {
        updatePeakActions();

        menu.add(theActionManager.get("addpeaks"));

        ButtonGroup group = new ButtonGroup();    
        if( shown_mslevel.equals("ms") ) {
        for( GlycanAction a: theApplication.getPluginManager().getMsPeakActions() ) {
            JRadioButtonMenuItem last = new JRadioButtonMenuItem(new GlycanAction(a,"annotatepeaks",-1,"",this));

            menu.add(last);
            last.setSelected(a == ms_action);
            group.add(last);
        }
        }
        else {
        for( GlycanAction a: theApplication.getPluginManager().getMsMsPeakActions() ) {
            JRadioButtonMenuItem last = new JRadioButtonMenuItem(new GlycanAction(a,"annotatepeaks",-1,"",this));

            menu.add(last);
            last.setSelected(a == msms_action);
            group.add(last);
        }
        }
        menu.addSeparator();
    }

    menu.add(theActionManager.get("zoomnone"));
    menu.add(theActionManager.get("zoomin"));
    menu.add(theActionManager.get("zoomout"));

    return menu;
    }        

    //-----------
    // Visualization

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

    public void updateMZAxis() {
    if( theDocument.getNoScans()>0 ) 
        thePlot.getDomainAxis().setRange(theDocument.getPeakDataAt(current_ind).getMZRange());    
    else
        thePlot.getDomainAxis().setRange(new Range(0.,1.));
    }
    
    public void updateIntensityAxis() {
    double max_int = 0.;
    for( int d=0; d<thePlot.getDatasetCount(); d++ ) {
        XYDataset dataset = thePlot.getDataset(d);
        for( int s=0; s<dataset.getSeriesCount(); s++ )
        for( int i=0; i<dataset.getItemCount(s); i++ )
            max_int = Math.max(max_int,dataset.getYValue(s,i));                   
    } 

    if( max_int==0. ) {
        // no data
        return;
    }
    Range new_int_range = new Range(0.,max_int);
    
    // make space for annotations
    Rectangle2D data_area = theChartPanel.getScreenDataArea();        
    if( data_area.getHeight()>0 ) 
        new_int_range = Range.expand(new_int_range,0.,12. / data_area.getHeight());
    thePlot.getRangeAxis().setRange(new_int_range);
    }

    public void updateChart() {
    // auto zoom
    if( theDocument.getNoScans()>0 ) {        
        Range mz_range  = thePlot.getDomainAxis().getRange();
        
        // update data
        double mz_toll = screenToDataX(1.);
        double[][] data = theDocument.getPeakDataAt(current_ind).getData(mz_range,mz_toll);
        
        // update visible data and compute intensity range
        visibleData.clear();
        double min_int = (data[0].length>0) ?data[1][0] :0.;
        double max_int = (data[0].length>0) ?data[1][0] :0.;
        for( int i=0; i<data[0].length; i++ ) {
        min_int = Math.min(min_int,data[1][i]);
        max_int = Math.max(max_int,data[1][i]);
        visibleData.put(data[0][i],data[1][i]);
        }

        //Range new_int_range = new Range(min_int,max_int);
        Range new_int_range = new Range(0.,max_int);
        
        // make space for annotations
        Rectangle2D data_area = theChartPanel.getScreenDataArea();        
        if( data_area.getHeight()>0 ) 
        new_int_range = Range.expand(new_int_range,0.,12. / data_area.getHeight());

        // resize y axis
        thePlot.getRangeAxis().setRange(new_int_range);
        
        // reload dataset
        //theDataset.removeSeries("intensities");
        theDataset.addSeries("intensities",data);

        /*
        for( int i=0; i<theDataset.getSeriesCount(); i++ ) {
        if( theDataset.getSeriesKey(i).equals("intensities") )
            thePlot.getRenderer().setSeriesPaint(i,Color.red);                    
        else
            thePlot.getRenderer().setSeriesPaint(i,Color.blue);
        }
        */
    }
    else {
        thePlot.getRangeAxis().setRange(new Range(0.,1.));
        for( int i=0; i<theDataset.getSeriesCount(); i++ )
        theDataset.removeSeries(theDataset.getSeriesKey(i));
    }


    // restore annotation shapes
    showSelection();
    }

    public void updateTitle() {
    if( theDocument.getNoScans()>0 ) {
        String title = "Spectrum " + theDocument.getScanNum(current_ind) + "/" + theDocument.getNoScans();
        
        int ms_level;
        if( shown_mslevel.equals("ms") ) 
        ms_level = 1;
        else {
        ms_level = theDocument.getScanDataAt(current_ind).getMSLevel();
        if( ms_level==1 )
            ms_level = 2;
        }

        if( ms_level==1 ) 
        title += ", MS";
        else if( ms_level==2 ) 
        title += ", MS/MS";
        else if( ms_level>2 ) 
        title += ", MS" + ms_level;

        if( ms_level>1 ) 
        title += ", precursor= " + new java.text.DecimalFormat("0.0000").format(theDocument.getScanDataAt(current_ind).getPrecursorMZ()) + " Da";

        theChart.setTitle(title);
    }
    else {
        theChart.setTitle("Spectrum");
    }
    }

    public void updateMsLevel() {
    if( theWorkspace.getCurrentScan()!=null )
        onSetMsLevel(theWorkspace.getCurrentScan().isMsMs()?"msms" :"ms",false);
    else
        onSetMsLevel("msms",false);
    }


    public void updateView() {
    
    current_ind = Math.max(0,Math.min(current_ind,theDocument.getNoScans()-1));

    // clear selection
    resetSelection();
      
    // update title
    updateMZAxis();
    updateChart();
    updateTitle();
    }   

    //--------------
    // manage selection

    private Peak findNearestPeak(double mz, double intensity, double mz_toll, double int_toll) {    
    java.util.SortedMap<Double,Double> submap = visibleData.subMap(mz-mz_toll,mz+mz_toll);

    Peak found = null;
    double min_diff = 0.;
    for( Map.Entry<Double,Double> e : submap.entrySet() ) { 
        double diff = Math.abs(e.getValue()-intensity);
        if( diff<=int_toll ) {
        if( found==null || diff<min_diff ) 
            found = new Peak(e.getKey(),e.getValue());        
        }
    }
    return found;
    }

    public Peak findPeakAt(Point2D p) {
    
    if( theDocument.getNoScans()>0 ) {        
        Point2D dp = screenToDataCoords(p);
        double mz_toll  =  screenToDataX(3.);
        double int_toll =  screenToDataY(3.);
        return findNearestPeak(dp.getX(),dp.getY(),mz_toll,int_toll);
    }
    return null;
    }

    public boolean hasSelection() {
    return (selected_peaks.size()>0);
    }
    
    public boolean isSelected(Peak peak) {
    return (peak==null) ?false :selected_peaks.contains(peak);
    }
    
    public void resetSelection() {
    current_peak = null;
    selected_peaks.clear();
    fireUpdatedSelection();
    }

    public void setSelection(Peak peak) {
    if( peak!=null ) {
        selected_peaks.clear();

        selected_peaks.add(peak);
        current_peak = peak;
        
        fireUpdatedSelection();        
    }
    else
        resetSelection();
    }


    public void addSelection(Peak peak) {
    if( peak!=null ) {
        if( !isSelected(peak) ) 
        selected_peaks.add(peak);        
        else
        current_peak = peak;
        fireUpdatedSelection();        
    }
    }

    public void enforceSelection(Peak peak) {
    if( peak!=null ) {
        if( !isSelected(peak) ) 
        setSelection(peak);        
        else {
        current_peak = peak;
        fireUpdatedSelection();
        }
    }
    else
        resetSelection();    
    }

    private void showSelection() {    
    thePlot.clearAnnotations();
    if( selected_peaks.size()>0 ) {        
        double width = screenToDataX(6.);
        double height = screenToDataY(6.);
        double margin = screenToDataY(8.);
        for( Peak p : selected_peaks ) {
        // add annotation
        Shape shape = new Rectangle2D.Double(p.getMZ()-width/2.,p.getIntensity()-height/2.,width,height);
        if( p.equals(current_peak) ) {
            thePlot.addAnnotation( new XYShapeAnnotation(shape,new BasicStroke(2.f),Color.blue) );    
            thePlot.addAnnotation( new XYTextAnnotation(new java.text.DecimalFormat("0.0000").format(p.getMZ()), p.getMZ(), p.getIntensity() + margin ) );    
        }
        else
            thePlot.addAnnotation( new XYShapeAnnotation(shape,new BasicStroke(1.f),Color.black) );    
        }
    }
    }
    
    //-----------------
    // actions    

    public int getCurrentInd() {
    return current_ind;
    }

    public void closeCurrent() {
    if( theDocument.getNoScans()>0 ) {
        int old_ind = current_ind;
        current_ind = Math.min(current_ind,theDocument.getNoScans()-2);
        current_ind = Math.max(current_ind,0);

        theDocument.removeScan(old_ind);

        updateActions();
        updateView();
    }
    }

    public void showPrevious() {
    if( theDocument.getNoScans()>0 && current_ind>0 ) {
        current_ind--;
        updateView();
    }
    }
    
    public void showNext() {
    if( theDocument.getNoScans()>0 && current_ind<(theDocument.getNoScans()-1) ) {
        current_ind++;
        updateView();
    }
    }      

    public void showScan(int s_ind) {
    if( s_ind>=0 && s_ind<theDocument.getNoScans() ) {
        current_ind = s_ind;
        updateView();
    }
    }    

    public void onEditScanData() {
    if( theDocument.getNoScans()>0 )
        new ScanDataPropertiesDialog(theApplication,theDocument.getScanDataAt(current_ind), theWorkspace).setVisible(true);

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
    
    public void onActivateZooming() {
    is_moving = false;
    theChartPanel.setCursor(Cursor.getDefaultCursor());        
    }

    public void onActivateMoving() {
    is_moving = true;
    //theChartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    theChartPanel.setCursor(hand_cursor);
    }

    public void onZoomNone() {
    if( theDocument.getNoScans()>0 )
        thePlot.getDomainAxis().setRange(theDocument.getPeakDataAt(current_ind).getMZRange()); 
    }

    public void onZoomIn() {
    if( theDocument.getNoScans()>0 )
        thePlot.getDomainAxis().resizeRange(0.5);
    }

    public void onZoomOut() {
    if( theDocument.getNoScans()>0 ) {
        thePlot.getDomainAxis().resizeRange(2.0);
        if( thePlot.getDomainAxis().getRange().contains(theDocument.getPeakDataAt(current_ind).getMinMZ()) ||
        thePlot.getDomainAxis().getRange().contains(theDocument.getPeakDataAt(current_ind).getMaxMZ()) )
        thePlot.getDomainAxis().setRange(theDocument.getPeakDataAt(current_ind).getMZRange());
    }
    }

    public void onSetMsLevel(String mslevel, boolean changedoc) {
    shown_mslevel = mslevel;
    if( mslevel.equals("ms") ) {
        mslevel_button.setAction(theActionManager.get("mslevel=msms"));
        mslevel_button.setText(null);
        if( changedoc )
        theWorkspace.setMsMs(theWorkspace.getCurrentScan(),false);
    }
    else {
        mslevel_button.setAction(theActionManager.get("mslevel=ms"));
        mslevel_button.setText(null);
        if( changedoc )
        theWorkspace.setMsMs(theWorkspace.getCurrentScan(),true);
    }
    updateTitle();
    }

    public void onSetUpdateIsotopeCurves(Boolean f) {
    automatic_update_isotope_curves = f;
    
    if( automatic_update_isotope_curves ) 
        isotopes_button.setAction(theActionManager.get("updateisotopecurves=false"));
    else
        isotopes_button.setAction(theActionManager.get("updateisotopecurves=true"));
    isotopes_button.setText(null);
    }

    public void onSetShowAllIsotopes(Boolean f) {
    show_all_isotopes = f;
    
    if( show_all_isotopes ) 
        ftmode_button.setAction(theActionManager.get("showallisotopes=false"));
    else
        ftmode_button.setAction(theActionManager.get("showallisotopes=true"));
    ftmode_button.setText(null);
    }

    public boolean onAddPeaks() {
    try {
        if( selected_peaks.size()>0 ) {
        // add selected peaks to list
        theApplication.getWorkspace().getPeakList().mergeData(selected_peaks);
        theApplication.getPluginManager().show("PeakList","PeakList");
        return true;
        }
        return false;
    }
    catch(Exception e) { 
        LogUtils.report(e);
        return false;
    }
    }

    public boolean onAnnotatePeaks(String parent_action) {
    try {
        if( selected_peaks.size()>0 ) {
        updatePeakActions();
        
        if( shown_mslevel.equals("ms") ) {
            if( parent_action!=null )            
            ms_action = theApplication.getPluginManager().getMsPeakAction(parent_action);            
            if( theApplication.getPluginManager().runAction(ms_action,new PeakList(selected_peaks)) ) {
            update_isotope_curves = automatic_update_isotope_curves;
            return true;
            }
            return false;
        }

        if( parent_action!=null )            
            msms_action = theApplication.getPluginManager().getMsMsPeakAction(parent_action);            
        if( theApplication.getPluginManager().runAction(msms_action,new PeakList(selected_peaks)) ) {
            update_isotope_curves = automatic_update_isotope_curves;
            return true;
        }
        return false;
        }
        return false;
    }
    catch(Exception e) {  
        LogUtils.report(e);
        return false;
    }
    }

    private void removeIsotopeCurves() {
    while( theIsotopesDataset.getSeriesCount()>0 )
        theIsotopesDataset.removeSeries(theIsotopesDataset.getSeriesKey(0));
    }

    private void updateIsotopeCurves() {    
    update_isotope_curves = false;    

    TreeMap<Peak,Collection<Annotation>> map = new TreeMap<Peak,Collection<Annotation>>();
    for( PeakAnnotationMultiple pam : theSearchResults.getAnnotations() ) {
        if( map.get(pam.getPeak())==null )
        map.put(pam.getPeak(),new Vector<Annotation>());
        
        Collection<Annotation> dest = map.get(pam.getPeak());
        for( Vector<Annotation> va : pam.getAnnotations() )
        dest.addAll(va);
    }    
    addIsotopeCurves(map);
    }      

    
    public void addIsotopeCurves(TreeMap<Peak,Collection<Annotation>> annotations) {
    
    if( theDocument.size()==0 )
        return;

    // remove old curves
    removeIsotopeCurves();

    // add curves
    if( annotations!=null ) {

        // set renderer
        if( show_all_isotopes ) {
        thePlot.setRenderer(1,new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES));
        thePlot.getRenderer(1).setShape(new Ellipse2D.Double(0,0,7,7));
        }
        else
        thePlot.setRenderer(1,new StandardXYItemRenderer(StandardXYItemRenderer.LINES));

        MSUtils.IsotopeList isotope_list = new MSUtils.IsotopeList(show_all_isotopes);
        for(Map.Entry<Peak,Collection<Annotation>> pa : annotations.entrySet() ) {
        Peak p = pa.getKey();
        double[] best_peak = theDocument.getPeakDataAt(current_ind).findNearestPeak(p.getMZ());
        
        // get compositions
        HashSet<Molecule> compositions = new HashSet<Molecule>();    
        for( Annotation a : pa.getValue() ) {
            try { 
            compositions.add(a.getFragmentEntry().fragment.computeIon());    
            }
            catch(Exception e) {
            e.printStackTrace();
            }
        }
        
        // collect curves for this peak
        HashMap<String,double[][]> all_curves = new HashMap<String,double[][]>();
        for( Molecule m : compositions ) {
            try {
            double[][] data = MSUtils.getIsotopesCurve(1,m,show_all_isotopes);

            // overlay the distribution with the existing list of isotopes
            isotope_list.adjust(data,best_peak[0],best_peak[1]);             

            all_curves.put(m.toString(),data);
            }
            catch(Exception e) {
            LogUtils.report(e);
            }
        }    

        // add average curve for this peak
        if( all_curves.size()>1 ) {
            double[][] data = MSUtils.average(all_curves.values(),show_all_isotopes);

            // add the average to the chart
            String name = "average-"+p.getMZ();
            theIsotopesDataset.addSeries(name, data);    
            thePlot.getRenderer(1).setSeriesPaint(theIsotopesDataset.indexOf(name),Color.magenta);
            thePlot.getRenderer(1).setSeriesStroke(theIsotopesDataset.indexOf(name),new BasicStroke(2));            

            // add the average to the isotope list
            isotope_list.add(data,false);
        }
        else if( all_curves.size()==1 ) {
            // add the only curve to the isotope list
            isotope_list.add(all_curves.values().iterator().next(),false); 
        } 

        // add the other curves
        for( Map.Entry<String,double[][]> e : all_curves.entrySet() ) {
            String name = e.getKey() + "-" + p.getMZ();
            theIsotopesDataset.addSeries(name, e.getValue());    
            thePlot.getRenderer(1).setSeriesPaint(theIsotopesDataset.indexOf(name),Color.blue);            
        }
        }        

    }    
    updateIntensityAxis();    
    }
       

    public void noiseFilter() {
    theDocument.noiseFilter(current_ind);
    }
        
    public void baselineCorrection() {
    theDocument.baselineCorrection(current_ind);
    }


    public void computeCentroids() {
    ((SpectraPlugin)theApplication.getPluginManager().get("Spectra")).computeCentroids(current_ind);
    }

 

    //-----------
    // listeners

    public void addSelectionChangeListener(SelectionChangeListener l) {
    if( l!=null )
        listeners.add(l);
    }

    public void removeSelectionChangeListener(SelectionChangeListener l) {
    if( l!=null )
        listeners.remove(l);
    }
    
    public void fireUpdatedSelection() {
    for( SelectionChangeListener l : listeners ) 
        l.selectionChanged(new SelectionChangeEvent(this));
    removeIsotopeCurves();
    updateIntensityAxis();
    showSelection();
    updateActions();
    }

    public void actionPerformed(ActionEvent e) {

    String action = GlycanAction.getAction(e);
    String param  = GlycanAction.getParam(e);    

    if( action.equals("previous") )
        showPrevious();
    else if( action.equals("next") )
        showNext();
    else if( action.equals("close") )
        closeCurrent();

    else if( action.equals("new") )
        theApplication.onNew(theDocument);
    else if( action.equals("openspectra") )
        theApplication.onOpen(null,theDocument,false);

    else if( action.equals("edit") )
        onEditScanData();

    else if( action.equals("print") )
        onPrint();

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
    
    else if( action.equals("mslevel") ) 
        onSetMsLevel(param,true);
    else if( action.equals("addpeaks") )
        onAddPeaks();
    else if( action.equals("annotatepeaks") )
        onAnnotatePeaks(param);
    else if( action.equals("updateisotopecurves") ) 
        onSetUpdateIsotopeCurves(Boolean.valueOf(param));
    else if( action.equals("showallisotopes") ) 
        onSetShowAllIsotopes(Boolean.valueOf(param));
    else if( action.equals("noisefilter") )
        noiseFilter();
    else if( action.equals("baselinecorrection") )
        baselineCorrection();
    else if( action.equals("centroid") )
        computeCentroids();
    
    updateActions();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    
    if( MouseUtils.isPopupTrigger(e) ) {
        // open popup
        current_peak = findPeakAt(e.getPoint());
        enforceSelection(current_peak);        
        createPopupMenu(current_peak!=null).show(theChartPanel, e.getX(), e.getY()); 
    }
    else {
        was_moving = is_moving;
        if( (MouseUtils.isPushTrigger(e) || MouseUtils.isMoveTrigger(e) ) && theChartPanel.getScreenDataArea().contains(e.getPoint()) ) {
        mouse_start_point = e.getPoint();
        if( MouseUtils.isMoveTrigger(e) ) 
            onActivateMoving();                
        }
    }
    }        

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    if( mouse_start_point!=null && theDocument.getNoScans()>0 ) {
        if( is_moving ) {
        // moving
        double mz_delta = screenToDataX(mouse_start_point.getX() - e.getPoint().getX());
        if( mz_delta>0. ) {
            double old_upper_bound = thePlot.getDomainAxis().getUpperBound();
            double old_lower_bound = thePlot.getDomainAxis().getLowerBound();
            double new_upper_bound = Math.min(old_upper_bound+mz_delta,theDocument.getPeakDataAt(current_ind).getMaxMZ());
            double new_lower_bound = old_lower_bound + new_upper_bound - old_upper_bound;
        
            thePlot.getDomainAxis().setRange(new Range(new_lower_bound,new_upper_bound));
        }
        else {
            double old_upper_bound = thePlot.getDomainAxis().getUpperBound();
            double old_lower_bound = thePlot.getDomainAxis().getLowerBound();
            double new_lower_bound = Math.max(old_lower_bound+mz_delta,theDocument.getPeakDataAt(current_ind).getMinMZ());
            double new_upper_bound = old_upper_bound + new_lower_bound - old_lower_bound;
        
            thePlot.getDomainAxis().setRange(new Range(new_lower_bound,new_upper_bound));
        }
        
        mouse_start_point = e.getPoint();
        }    
        else {
        // zooming                
        Graphics2D g2 = (Graphics2D) theChartPanel.getGraphics();
        g2.setXORMode(java.awt.Color.gray);

        // delete old rectangle
        if( zoom_rectangle!=null) 
            g2.draw(zoom_rectangle);

        // create new rectangle
        double start_x = Math.min(e.getX(),mouse_start_point.getX());
        double end_x = Math.max(e.getX(),mouse_start_point.getX());

        Rectangle2D data_area = theChartPanel.getScreenDataArea((int)start_x,(int)mouse_start_point.getY());
        double xmax = Math.min(end_x, data_area.getMaxX());
        zoom_rectangle = new Rectangle2D.Double(start_x,data_area.getMinY(),xmax - start_x, data_area.getHeight());

        // draw new rectangle
        g2.draw(zoom_rectangle);
        g2.dispose();          
        }            
    }
    }        
    
    public void mouseReleased(MouseEvent e) {    
    if( MouseUtils.isPopupTrigger(e) ) {        
        // clear all
        if( zoom_rectangle!=null ) {
        Graphics2D g2 = (Graphics2D) getGraphics();
        g2.setXORMode(java.awt.Color.gray);
        g2.draw(zoom_rectangle);
        g2.dispose();
        }
        mouse_start_point = null;
        zoom_rectangle = null;

        // open popup
        current_peak = findPeakAt(e.getPoint());
        enforceSelection(current_peak);        
        createPopupMenu(current_peak!=null).show(theChartPanel, e.getX(), e.getY()); 
    }
    else {
        if( zoom_rectangle!=null && mouse_start_point!=null ) {
        if( Math.abs(e.getX() - mouse_start_point.getX()) > 10 ) {
            //if( e.getX() < mouse_start_point.getX() ) {
            // unzoom all
            //    onZoomNone();
            //}
            //else {        
 
            // zoom area           
            double start_x = Math.min(e.getX(),mouse_start_point.getX());
            double end_x = Math.max(e.getX(),mouse_start_point.getX());

            Rectangle2D data_area = theChartPanel.getScreenDataArea((int)start_x,(int)mouse_start_point.getY());
         
            double new_lower_bound = screenToDataCoordX(start_x);
            double new_upper_bound = screenToDataCoordX(Math.min(end_x, data_area.getMaxX()));
            thePlot.getDomainAxis().setRange(new Range(new_lower_bound,new_upper_bound));                    
        }
        else {
            // clear rectangle
            Graphics2D g2 = (Graphics2D) getGraphics();
            g2.setXORMode(java.awt.Color.gray);
            g2.draw(zoom_rectangle);
            g2.dispose();
        }
        }
        
        // restore zooming
        if( !was_moving && is_moving )
        onActivateZooming();

        zoom_rectangle = null;
        mouse_start_point = null;
    }
    }
        
    public void mouseClicked(MouseEvent e) {      

    // find peak under mouse
    current_peak = findPeakAt(e.getPoint());

    if( MouseUtils.isSelectTrigger(e) ) 
        setSelection(current_peak);
    else if( MouseUtils.isAddSelectTrigger(e) ) 
        addSelection(current_peak);
    else if( MouseUtils.isActionTrigger(e) ) {
        setSelection(current_peak);        
        onAnnotatePeaks(null);
    }
    else if( MouseUtils.isCtrlActionTrigger(e) ) {
        setSelection(current_peak);        
        onAddPeaks();
    }
    }           
    
    public void documentChanged(BaseDocument.DocumentChangeEvent e) {
    if( e.getSource()==theWorkspace.getSearchResults() ) {
        if( update_isotope_curves )
        updateIsotopeCurves();
    }
    else {
        removeIsotopeCurves();
        super.documentChanged(e);
    }
    }

    public void internalDocumentChanged(GlycanWorkspace.Event e) {    
    updateMsLevel();
    }

    public void componentResized(ComponentEvent e) {
    if( !checkForUpdates() )
        updateChart();
    }

    public void componentShown(ComponentEvent e) {
    if( !checkForUpdates() )
        updateChart();
    }

   

}
