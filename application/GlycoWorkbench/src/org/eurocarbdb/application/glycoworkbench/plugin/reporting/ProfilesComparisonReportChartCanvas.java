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
import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.general.Dataset;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.ui.TextAnchor;

import static org.eurocarbdb.application.glycanbuilder.Geometry.*; 

public class ProfilesComparisonReportChartCanvas extends JComponent implements SVGUtils.Renderable, Printable  {
      
    private static final int DRAW_X_MARGIN = 20;
    private static final int DRAW_Y_MARGIN = 20;
    private static final int CHART_X_MARGIN = 20;
    private static final int CHART_Y_MARGIN = 20;
    private static final int CHART_WIDTH = 800;    
    private static final int CHART_WIDTH_TICK = 15;
    private static final int CHART_HEIGHT = 600;

    private GlycoWorkbench theApplication;
    private ProfilesComparisonReportDocument theDocument;
    private GlycanRenderer  theGlycanRenderer;
    private JScrollPane theScrollPane = null;
    private ProfilesComparisonReportOptions theOptions;
    private GraphicOptions theGraphicOptions;
 
    private CategoryDataset theDataset;
    private CategoryPlot thePlot;
    private JFreeChart   theChart;

    // drawing  
    private double scale;
    private boolean is_printing;   

    private Rectangle view_area;
    private Rectangle draw_area;
    private Rectangle chart_area;
    private Rectangle2D data_area;
    
    // construction

    public ProfilesComparisonReportChartCanvas(GlycoWorkbench application, ProfilesComparisonReportDocument doc, ProfilesComparisonReportOptions opt) {

    theApplication = application;
    theDocument = doc;        
    theOptions = opt;
    theGraphicOptions = theApplication.getWorkspace().getGraphicOptions();
    theGlycanRenderer = theApplication.getWorkspace().getGlycanRenderer();

    // create chart
    createChart();    

    // finish setting up
    is_printing = false;
    setScale(1.);        
    }

    public void setScrollPane(JScrollPane sp) {
    theScrollPane = sp;
    }  

    // drawing

    public void beforeRendering() {
    is_printing = true;
    }

    public void afterRendering() {
    is_printing = false;
    }

    public Dimension getRenderableSize() {
    return getPreferredSize();
    }

    public void paintRenderable(Graphics2D g2d) {
    paintComponent(g2d);
    }

    protected void paintComponent(Graphics g) {

    // prepare graphic object
    Graphics2D g2d = (Graphics2D)g.create();        
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    // set clipping area
    if( is_printing ) {
        g2d.translate(-draw_area.x,-draw_area.y);
        g2d.setClip(draw_area);       
    }
    
    //paint canvas background
    if( !is_printing ) {
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());    
    }
    
    // paint white background on drawing area    
    g2d.setColor(Color.white);
    g2d.fillRect(draw_area.x, draw_area.y, draw_area.width, draw_area.height);            
    if( !is_printing ) {
        g2d.setColor(Color.black);
        g2d.draw(draw_area);
    }
    
    // paint
    paintChart(g2d);       

    // dispose graphic object
    g2d.dispose();    

        revalidate();    
    }   

    protected void paintChart(Graphics2D g2d) {
    org.jfree.chart.ChartRenderingInfo cri = new org.jfree.chart.ChartRenderingInfo();
    theChart.draw(g2d,chart_area,cri);
    data_area = cri.getPlotInfo().getDataArea();
    }

  

    public Dimension getPreferredSize() {
    if( is_printing )
        return draw_area.getSize();
    return view_area.getSize();
    }            
    
    public Dimension getMinimumSize() {
    return new Dimension(0,0);
    }

    // actions

    public void print( PrinterJob job ) throws PrinterException {  
        // do something before
    is_printing = true;

    job.print();

    // do something after
    is_printing = false;
    }

    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {  
    if (pageIndex > 0) {
        return NO_SUCH_PAGE;
    } 
    else {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setBackground(Color.white);
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        Dimension td = this.getPreferredSize();
        double sx = pageFormat.getImageableWidth()/td.width;
        double sy = pageFormat.getImageableHeight()/td.height;
        double s = Math.min(sx,sy);
        if( s<1. ) 
        g2d.scale(s,s);

        RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);
        this.paint(g2d);
        RepaintManager.currentManager(this).setDoubleBufferingEnabled(true);
        
        return PAGE_EXISTS;
    }
    }

    public void setScale(double scale) {
        this.scale = scale;
        updateView();
    }

    public double getScale() {
    return this.scale;
    }       

    public void getScreenshot() {
    ClipUtils.setContents(SVGUtils.getImage(this));
    }
 

    public void updateView() {    
    updateDrawArea();
    repaint();
    }

    private void createChart() {
        
    // create dataset
        theDataset = createDataset();

    // create axis
    CategoryAxis categoryAxis = new CategoryAxis("");
    categoryAxis.setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_45);
        ValueAxis valueAxis = new NumberAxis("Normalized Intensities");

    // create renderer
    CategoryItemRenderer renderer = null;
    if( theOptions.REPRESENTATION==theOptions.BARS ) 
        renderer = new org.jfree.chart.renderer.category.BarRenderer();
    else if( theOptions.REPRESENTATION==theOptions.ERRORBARS ) 
        renderer = new org.jfree.chart.renderer.category.StatisticalBarRenderer();    
    else if( theOptions.REPRESENTATION==theOptions.DISTRIBUTIONS )  
        renderer = new org.jfree.chart.renderer.category.ScatterRenderer();        

    ItemLabelPosition position1 = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
    renderer.setBasePositiveItemLabelPosition(position1);
    ItemLabelPosition position2 = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER);
    renderer.setBaseNegativeItemLabelPosition(position2);
        
    // create plot
        thePlot = new CategoryPlot(theDataset, categoryAxis, valueAxis, renderer);
        thePlot.setOrientation(org.jfree.chart.plot.PlotOrientation.VERTICAL);

    // add mean values 
    if( theOptions.REPRESENTATION==theOptions.DISTRIBUTIONS ) {
        thePlot.setDataset(1, createMeansDataset());
        thePlot.mapDatasetToRangeAxis(1, 0);
        
        CategoryItemRenderer lr = new org.jfree.chart.renderer.category.LevelRenderer();
        lr.setPaint(Color.black);
        thePlot.setRenderer(1,lr);        
    }

    // create chart
        theChart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, thePlot, true);    
    theChart.setBackgroundPaint(Color.white);
    theChart.setBorderVisible(false);
        
    }

    
    private CategoryDataset createDataset() {
    GlycanRenderer theRenderer = theApplication.getWorkspace().getGlycanRenderer();
    if( theOptions.REPRESENTATION==theOptions.BARS ) {
        org.jfree.data.category.DefaultCategoryDataset ret = new org.jfree.data.category.DefaultCategoryDataset();       
        for( ProfilesComparisonReportDocument.Row row : theDocument.getRows() ) {
        for( int i=0; i<theDocument.getNoColumns(); i++ ) 
            ret.addValue(row.getColumn(i),theDocument.getNames().get(i),row.name);            
        }
        return ret;
    }
    else if( theOptions.REPRESENTATION==theOptions.ERRORBARS ) {
        org.jfree.data.statistics.DefaultStatisticalCategoryDataset ret = new org.jfree.data.statistics.DefaultStatisticalCategoryDataset();       
        for( ProfilesComparisonReportDocument.Row row : theDocument.getRows() ) {
        ret.add(mean(row.intensities_firstgroup),stddev(row.intensities_firstgroup),"First group",row.name);            
        ret.add(mean(row.intensities_secondgroup),stddev(row.intensities_secondgroup),"Second group",row.name);            
        }
        return ret;
    }
    else if( theOptions.REPRESENTATION==theOptions.DISTRIBUTIONS ) {
        org.jfree.data.statistics.DefaultMultiValueCategoryDataset ret = new org.jfree.data.statistics.DefaultMultiValueCategoryDataset();       
        for( ProfilesComparisonReportDocument.Row row : theDocument.getRows() ) {
        ret.add(tolist(row.intensities_firstgroup),"First group",row.name);            
        ret.add(tolist(row.intensities_secondgroup),"Second group",row.name);         
        }
        return ret;
    }
        return null;
    }
    
    private CategoryDataset createMeansDataset() {
    GlycanRenderer theRenderer = theApplication.getWorkspace().getGlycanRenderer();
    
    org.jfree.data.category.DefaultCategoryDataset ret = new org.jfree.data.category.DefaultCategoryDataset();       
    for( ProfilesComparisonReportDocument.Row row : theDocument.getRows() ) {
        ret.addValue(mean(row.intensities_firstgroup),"First group",row.name);            
        ret.addValue(mean(row.intensities_secondgroup),"Second group",row.name);            
    }
    return ret;
    }

    private ArrayList<Double> tolist(double[] array) {
    ArrayList<Double> ret = new ArrayList<Double>();
    for( double d : array )
        ret.add(d);
    return ret;
    }

    private ArrayList<Double> tolist(double value) {
    ArrayList<Double> ret = new ArrayList<Double>();
    ret.add(value);
    return ret;
    }

    private double mean(double[] array) {
    if( array==null || array.length==0 )
        return 0.;

    double mean = 0.;
    for( int i=0; i<array.length; i++ )
        mean += array[i];
    return mean/(double)array.length;
    }

    private double stddev(double[] array) {
    if( array==null || array.length==0 )
        return 0.;

    double mean = mean(array);

    double smean = 0.;
    for( int i=0; i<array.length; i++ )
        smean += array[i]*array[i];    
    return Math.sqrt(smean/(double)array.length - mean*mean);
    }

    private Rectangle getViewArea(double scale) {
        int chart_width = Math.max(CHART_WIDTH,CHART_WIDTH_TICK*theDocument.getNoRows());
        return new Rectangle(0,0,(int)(2*DRAW_X_MARGIN + scale*(2*CHART_X_MARGIN + chart_width)),(int)(2*DRAW_Y_MARGIN + scale*(2*CHART_X_MARGIN + CHART_HEIGHT)));
    }    

    private Rectangle getDrawArea(double scale) {
        int chart_width = Math.max(CHART_WIDTH,CHART_WIDTH_TICK*theDocument.getNoRows());
        return new Rectangle(DRAW_X_MARGIN,DRAW_Y_MARGIN,(int)(scale*(2*CHART_X_MARGIN + chart_width)),(int)(scale*(2*CHART_Y_MARGIN + CHART_HEIGHT)));
    }

    private Rectangle getChartArea(double scale) {
        int chart_width = Math.max(CHART_WIDTH,CHART_WIDTH_TICK*theDocument.getNoRows());
        return new Rectangle((int)(DRAW_X_MARGIN + scale*CHART_X_MARGIN),(int)(DRAW_Y_MARGIN + scale*CHART_Y_MARGIN),(int)(scale*chart_width),(int)(scale*CHART_HEIGHT));
    }

    protected void updateDrawArea() {

    // update data area
        view_area = getViewArea(scale);
        draw_area = getDrawArea(scale);
        chart_area = getChartArea(scale);

        data_area = null;               
        paintChart(GraphicUtils.createImage(view_area.getSize(),true).createGraphics());        
    }

}
