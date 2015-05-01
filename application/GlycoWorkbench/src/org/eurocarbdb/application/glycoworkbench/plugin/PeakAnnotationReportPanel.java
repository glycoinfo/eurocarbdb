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
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.awt.print.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;

public class PeakAnnotationReportPanel extends DocumentPanel<AnnotatedPeakList> implements ActionListener {        

    // components   
    protected DefaultXYDataset theDataset;
    protected XYPlot           thePlot;
    protected JFreeChart       theChart;
    protected ChartPanel       theChartPanel;

    protected JToolBar theToolBar;

    //

    public PeakAnnotationReportPanel()  {
    super();
    }

    protected void initComponents() {
    setLayout(new BorderLayout());

    // create chart
    theDataset = new DefaultXYDataset();
    theChart = org.jfree.chart.ChartFactory.createScatterPlot("Annotation", "Count", "Intensity", theDataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, true, false, false);
    thePlot = (XYPlot)theChart.getPlot();
    thePlot.setRenderer(new StandardXYItemRenderer(StandardXYItemRenderer.LINES));

    theChartPanel = new ChartPanel(theChart);    
    theChartPanel.setDomainZoomable(true);
    theChartPanel.setRangeZoomable(false);    
    //theChartPanel.setPopupMenu(null);
    add(theChartPanel,BorderLayout.CENTER);
    
    // create toolbar
    theToolBar = createToolBar(); 
    add(theToolBar,BorderLayout.SOUTH);    
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

    updateView();
    updateActions();
    }    
    

    protected void createActions() {    

    theActionManager.add("new",FileUtils.defaultThemeManager.getImageIcon("new"),"Clear",KeyEvent.VK_N, "",this);
    theActionManager.add("print",FileUtils.defaultThemeManager.getImageIcon("print"),"Print...",KeyEvent.VK_P, "",this);
    }

    protected void updateActions() {
    }

    private JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
      
    toolbar.add(theActionManager.get("new"));
    
    toolbar.addSeparator();

    toolbar.add(theActionManager.get("print"));

    return toolbar;
    }            
    
    //-----------
    // Visualization
     
    protected void updateData() {
    }

    protected void updateView() {
    // clear
    //for( int i=1; i<theDataset.getSeriesCount(); i++ )
    //theDataset.removeSeries(theDataset.getSeriesKey(i-1));
        
    for( int l=0; l<theDocument.getNoStructures(); l++ ) 
        theDataset.removeSeries("series"+l);
        
    for( int l=0; l<theDocument.getNoStructures(); l++ ) {

        double[][] data = new double[2][];                        
        data[0] = new double[theDocument.getNoPeaks()];
        data[1] = new double[theDocument.getNoPeaks()];

        // get intensities and order them
        LinkedList<Double> sortedList = new LinkedList<Double>();
        for( int i=0; i<theDocument.getNoPeaks(); i++ ) {
        double intensity = theDocument.getPeak(i).getIntensity();
        if( theDocument.getAnnotations(i,l).size()==0 )
            intensity = 0.;
    
        int index = Collections.binarySearch(sortedList, intensity);   
        if (index < 0) 
            index = -index - 1;
        sortedList.add(index, intensity);    
        }

        // set data
        for( int i=0; i<theDocument.getNoPeaks(); i++ ) {
        data[0][i] = i;
        data[1][i] = sortedList.get(theDocument.getNoPeaks()-i-1);
        }    
        theDataset.addSeries("series" + l,data);    
    }
    }
      
    
    //-----------------
    // actions    
   
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
        
        
    //-----------
    // listeners

   

    public void actionPerformed(ActionEvent e) {

    String action = e.getActionCommand();       
    
    if( action.equals("new") )
        theApplication.onNew(theDocument);

    else if( action.equals("print") )
        onPrint();

    updateActions();
    }   
  

}
