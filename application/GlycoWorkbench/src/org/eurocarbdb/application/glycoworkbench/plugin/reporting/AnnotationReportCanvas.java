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
import java.text.DecimalFormat;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;

import static org.eurocarbdb.application.glycanbuilder.Geometry.*; 

public class AnnotationReportCanvas extends JComponent implements SVGUtils.Renderable, BaseDocument.DocumentChangeListener, MouseListener, MouseMotionListener, Printable  {

    public interface SelectionChangeListener {    
    public void selectionChanged(SelectionChangeEvent e);    
    }

    public static class SelectionChangeEvent {
    private AnnotationReportCanvas src;

    public SelectionChangeEvent(AnnotationReportCanvas _src) {
        src = _src;
    }

    public AnnotationReportCanvas getSource() {
        return src;
    }       
    }
  
    private AnnotationReportDocument theDocument;
    private GlycanRenderer  theGlycanRenderer;
    private JScrollPane theScrollPane = null;
 
    private DefaultXYDataset   theDataset;
    private DefaultXYDataset   maxIntensityDataset;
    private XYPlot      thePlot;
    private JFreeChart  theChart;

    private boolean first_time;
    private boolean first_time_init_pos;

    // drawing

    private HashSet<AnnotationObject> selections;
    private HashMap<AnnotationObject,Rectangle> rectangles;        
    private HashMap<AnnotationObject,Rectangle> rectangles_text;        
    private HashMap<AnnotationObject,Rectangle> rectangles_complete;        
    private HashMap<AnnotationObject,Polygon> connections;        
    private HashMap<AnnotationObject,Point2D> connections_cp;        
    
    private boolean is_printing;
    private Point mouse_start_point = null;
    private Point mouse_end_point = null;
    private AnnotationObject start_position = null;
    private boolean is_dragndrop = false;
    private boolean is_resizing = false;
    private boolean is_movingcp = false;
    private boolean was_dragged = false;

    private Rectangle draw_area;
    private Rectangle chart_area;
    private Rectangle2D data_area;
    
    private AnnotationReportOptions theOptions;
    private GraphicOptions theGraphicOptions;

    // 
    private Vector<SelectionChangeListener> listeners;

    // construction

    public AnnotationReportCanvas(AnnotationReportDocument doc, boolean init_pos) {

    theDocument = doc;
    theDocument.addDocumentChangeListener(this);

    theOptions = theDocument.getAnnotationReportOptions();
    theGraphicOptions = theDocument.getGraphicOptions();
    theGlycanRenderer = theDocument.getWorkspace().getGlycanRenderer();

    // create chart
    updateChart();    

    // finish setting up
    listeners = new Vector<SelectionChangeListener>();
    
    is_printing = false;

    // adding data and structures
    setScale(1.);        
    resetSelection();

    // add events
    addMouseMotionListener( this );
    addMouseListener( this );         
            
    first_time = true;
    first_time_init_pos = init_pos;
    }

    public void setScrollPane(JScrollPane sp) {
    theScrollPane = sp;
    }

    public AnnotationReportDocument getDocument() {
    return theDocument;
    }

    public BuilderWorkspace getWorkspace() {
    return theDocument.getWorkspace();
    }

    public AnnotationReportOptions getAnnotationReportOptions() {
    return theDocument.getAnnotationReportOptions();
    }

    public GraphicOptions getGraphicOptions() {
    return theDocument.getGraphicOptions();
    }

    // drawing

    public void beforeRendering() {
    is_printing = true;

    // make sure everything is initialized
    updateDrawArea(true);
    updateData();
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
    paintAnnotations(g2d);

    // dispose graphic object
    g2d.dispose();    

    if( !is_printing ) {
        if( first_time ) {
        if( first_time_init_pos )
            placeStructures(true);
        else
            theDocument.fireDocumentInit();
        first_time = false;
        }
        else
        revalidate();    
    }

    }   

    protected void paintChart(Graphics2D g2d) {
    ChartRenderingInfo cri = new ChartRenderingInfo();
    theChart.draw(g2d,chart_area,cri);
    data_area = cri.getPlotInfo().getDataArea();
    }

    protected Rectangle computeRectangles() { 
    return computeRectangles(new PositionManager(), new BBoxManager());
    }

    protected Rectangle computeRectangles(PositionManager pman, BBoxManager bbman) { 

    DecimalFormat mz_df  = new DecimalFormat("0.0");

    Rectangle all_bbox = null;
    rectangles = new HashMap<AnnotationObject,Rectangle>();
    rectangles_text = new HashMap<AnnotationObject,Rectangle>(); 
    rectangles_complete = new HashMap<AnnotationObject,Rectangle>();
    for( AnnotationObject a : theDocument.getAnnotations() ) {

        // set scale
        theGlycanRenderer.getGraphicOptions().setScale(theOptions.SCALE_GLYCANS*theDocument.getScale(a));    
        
        // compute bbox
        Point2D anchor = dataToScreenCoords(theDocument.getAnchor(a));                       
        Rectangle bbox = theGlycanRenderer.computeBoundingBoxes(a.getStructures(),false,false,pman,bbman,false);

        int x = (int)anchor.getX()-bbox.width/2;
        int y = (int)anchor.getY()-bbox.height-theOptions.ANNOTATION_MARGIN-theOptions.ANNOTATION_MZ_SIZE;
        bbman.translate(x-bbox.x,y-bbox.y,a.getStructures());
        bbox.translate(x-bbox.x,y-bbox.y);

        // save bbox
        rectangles.put(a,bbox);

        // compute text bbox        
        String mz_text = mz_df.format(a.getPeakPoint().getX());
        Dimension mz_dim = textBounds(mz_text, theOptions.ANNOTATION_MZ_FONT , theOptions.ANNOTATION_MZ_SIZE);        
        Rectangle text_bbox = new Rectangle((int)anchor.getX()-mz_dim.width/2,
                        (int)anchor.getY()-2*theOptions.ANNOTATION_MARGIN/3-mz_dim.height,
                        mz_dim.width,mz_dim.height);

        // save text bbox
        rectangles_text.put(a,text_bbox);
        rectangles_complete.put(a,expand(union(bbox,text_bbox),theOptions.ANNOTATION_MARGIN/2));

        // update all bbox
        all_bbox = union(all_bbox,bbox);
        all_bbox = union(all_bbox,text_bbox);
    }
    if( all_bbox==null )
        return new Rectangle(0,0,0,0);
    return all_bbox;
    }

    public Rectangle2D computeSizeData(AnnotationObject a) {
    // compute bbox
    theGlycanRenderer.getGraphicOptions().setScale(theOptions.SCALE_GLYCANS*theDocument.getScale(a));    
    Rectangle bbox = theGlycanRenderer.computeBoundingBoxes(a.getStructures(),false,false,new PositionManager(),new BBoxManager(),false);
    
    // compute text bbox        
    DecimalFormat mz_df  = new DecimalFormat("0.0");
    String mz_text = mz_df.format(a.getPeakPoint().getX());
    Dimension mz_dim = textBounds(mz_text, theOptions.ANNOTATION_MZ_FONT , theOptions.ANNOTATION_MZ_SIZE);        

    // update bbox
    double width = Math.max(bbox.getWidth(),mz_dim.getWidth());
    double height = bbox.getHeight()+theOptions.ANNOTATION_MARGIN+theOptions.ANNOTATION_MZ_SIZE;
    return new Rectangle2D.Double(0,0,screenToDataX(width),screenToDataY(height));
    }


    protected Point2D computeAnchor( Rectangle rect, Point2D cp, Point2D peak ) {
    Point2D anchor;

    if( peak.getY()>bottom(rect) ) {
        if( cp.getY()>bottom(rect) ) 
        anchor = new Point2D.Double(midx(rect),bottom(rect));
        else if( cp.getY()<top(rect) )
        anchor = new Point2D.Double(midx(rect),top(rect));
        else if( cp.getX()<left(rect) ) 
        anchor = new Point2D.Double(left(rect),midy(rect));                
        else 
        anchor = new Point2D.Double(right(rect),midy(rect));
    }
    else {
        if( peak.getY()<top(rect) )
        anchor = new Point2D.Double(midx(rect),top(rect));
        else if( peak.getX()<left(rect) ) 
        anchor = new Point2D.Double(left(rect),midy(rect));                
        else 
        anchor = new Point2D.Double(right(rect),midy(rect));
    }
    return anchor;
    }

    protected void computeConnections() {

    connections = new HashMap<AnnotationObject,Polygon>();
    connections_cp = new HashMap<AnnotationObject,Point2D>();

    for( AnnotationObject a : theDocument.getAnnotations() ) {
        Rectangle rect = rectangles_complete.get(a);
        Point2D cp = dataToScreenCoords(theDocument.getControlPoint(a));
        Point2D peak = dataToScreenCoords(a.getPeakPoint());

        // select anchor
        Point2D anchor = computeAnchor(rect,cp,peak);
        boolean add_cp = (peak.getY()>bottom(rect));
        
        if( anchor.distance(peak)>10 ) {
        // create shape
        Polygon connection = new Polygon();
        connection.addPoint((int)anchor.getX(),(int)anchor.getY());
        if( add_cp ) connection.addPoint((int)cp.getX(),(int)cp.getY());
        connection.addPoint((int)peak.getX(),(int)peak.getY());
        if( add_cp ) connection.addPoint((int)cp.getX(),(int)cp.getY());
        
        // save
        connections.put(a,connection);
        if( add_cp )
            connections_cp.put(a,cp);
        }
    }
    }           
  
    protected void paintAnnotations(Graphics2D g2d) {

    DecimalFormat mz_df  = new DecimalFormat("0.0");

    // set font
    Font old_font = g2d.getFont();
    Font new_font = new Font(theOptions.ANNOTATION_MZ_FONT,Font.PLAIN,theOptions.ANNOTATION_MZ_SIZE);

    // compute bboxes
    PositionManager pman = new PositionManager();
    BBoxManager bbman = new BBoxManager();    
    computeRectangles(pman,bbman);

    // compute connections
    computeConnections();
    
    // paint connections
    for( AnnotationObject a : theDocument.getAnnotations() ) {
        boolean selected = !is_printing && selections.contains(a);

        // paint arrow        
        Polygon connection = connections.get(a);
        if( connection!=null ) {
        g2d.setColor(theOptions.CONNECTION_LINES_COLOR);
        g2d.setStroke( (selected) ?new BasicStroke((float)(1.+theOptions.ANNOTATION_LINE_WIDTH)) :new BasicStroke((float)theOptions.ANNOTATION_LINE_WIDTH));
        g2d.draw(connection);
        g2d.setStroke(new BasicStroke(1));
        }

                
        // paint control point       
        if( selected ) {            
        g2d.setColor(Color.black);            
        Point2D cp = connections_cp.get(a);
        if( cp!=null ) {
            int s = (int)(2+theOptions.ANNOTATION_LINE_WIDTH);
            g2d.fill(new Rectangle((int)cp.getX()-s,(int)cp.getY()-s,2*s,2*s));        
        }
        }
    }
    
    // paint glycans
    for( AnnotationObject a : theDocument.getAnnotations() ) {
        boolean highlighted = a.isHighlighted();
        boolean selected = !is_printing && selections.contains(a);
    
        // set scale
        theGlycanRenderer.getGraphicOptions().setScale(theOptions.SCALE_GLYCANS*theDocument.getScale(a));                   

        // paint highlighted region
        if( highlighted ) {
        Rectangle c_bbox = rectangles_complete.get(a);
                
        g2d.setColor(theOptions.HIGHLIGHTED_COLOR);        
        g2d.setXORMode(Color.white);
        g2d.fill(c_bbox);
        g2d.setPaintMode();

        g2d.setColor(Color.black);        
        g2d.draw(c_bbox);
        }       

        // paint glycan
        for( Glycan s: a.getStructures() ) 
        theGlycanRenderer.paint(g2d,s,null,null,false,false,pman,bbman);        
     
        // paint MZ text
        g2d.setFont(new_font);
        g2d.setColor(theOptions.MASS_TEXT_COLOR);    
        String mz_text = mz_df.format(a.getPeakPoint().getX());
        Rectangle mz_bbox = rectangles_text.get(a);
        g2d.drawString(mz_text,mz_bbox.x,mz_bbox.y+mz_bbox.height);
              
        // paint selection        
        if( selected ) {
        // paint rectangle
        Rectangle c_bbox = rectangles_complete.get(a);
        
        g2d.setStroke( new BasicStroke(highlighted ?2 :1) );
        g2d.setColor(Color.black);        

        g2d.draw(c_bbox);

        g2d.setStroke(new BasicStroke(1));

        // paint resize points        
        Polygon p1 = new Polygon();
        int cx1 = right(c_bbox);
        int cy1 = top(c_bbox);
        p1.addPoint(cx1,cy1);
        p1.addPoint(cx1-2*theOptions.ANNOTATION_MARGIN/3,cy1);
        p1.addPoint(cx1,cy1+2*theOptions.ANNOTATION_MARGIN/3);        
        g2d.fill(p1);

        Polygon p2 = new Polygon();
        int cx2 = left(c_bbox);
        int cy2 = top(c_bbox);
        p2.addPoint(cx2,cy2);
        p2.addPoint(cx2+2*theOptions.ANNOTATION_MARGIN/3,cy2);
        p2.addPoint(cx2,cy2+2*theOptions.ANNOTATION_MARGIN/3);        
        g2d.fill(p2);    
        }
    }

    g2d.setFont(old_font);
    }
   

    private void xorRectangle(Point start_point, Point end_point) {       
    Graphics g = getGraphics();
    g.setXORMode(Color.white);
    g.setColor(Color.gray);
    
    Rectangle rect = makeRectangle(start_point,end_point);
    g.drawRect(rect.x,rect.y,rect.width,rect.height);    
    }

    private void xorSelections(Point start_point, Point end_point) {       
    Graphics g = getGraphics();
    g.setXORMode(Color.white);
    g.setColor(Color.gray);

    int dx = end_point.x-start_point.x;
    int dy = end_point.y-start_point.y;
    for( AnnotationObject a : selections ) {
        Rectangle rect = rectangles_complete.get(a);
        g.drawRect(dx+rect.x, dy+rect.y, rect.width, rect.height);        
    }    
    }

    
    private double scaleFactor(AnnotationObject selection, Point start_point, Point end_point) { 
    Rectangle rect = rectangles.get(selection);
    
    double scale = 1.;
    if( start_point.x>midx(rect) ) {
        scale = Math.min((double)(end_point.x-start_point.x+rect.width/2.)/(double)(rect.width/2.),
                 (double)(start_point.y-end_point.y+rect.height)/(double)(rect.height));
    }
    else {
        scale = Math.min((double)(start_point.x-end_point.x+rect.width/2.)/(double)(rect.width/2.),
                 (double)(start_point.y-end_point.y+rect.height)/(double)(rect.height));
    }

    return Math.max(0.,scale);    
    }


    private void xorConnection(AnnotationObject selection, Point start_point, Point end_point) { 

    Graphics g = getGraphics();
    g.setXORMode(Color.white);
    g.setColor(Color.gray);

    Rectangle rect = rectangles_complete.get(selection);
    Point2D peak = dataToScreenCoords(selection.getPeakPoint());

    // select anchor
    Point2D anchor = computeAnchor(rect,end_point,peak);
        
    // draw connection
    g.drawLine((int)anchor.getX(),(int)anchor.getY(),(int)end_point.getX(),(int)end_point.getY());
    g.drawLine((int)end_point.getX(),(int)end_point.getY(),(int)peak.getX(),(int)peak.getY());
    }   
        
    private void xorResizing(AnnotationObject selection, Point start_point, Point end_point) { 
    Graphics g = getGraphics();
    g.setXORMode(Color.white);
    g.setColor(Color.gray);
       
    Rectangle rect = rectangles.get(selection);
    
    double scale = 1.;
    if( start_point.x>midx(rect) ) {
        scale = Math.min((double)(end_point.x-start_point.x+rect.width/2.)/(double)(rect.width/2.),
                 (double)(start_point.y-end_point.y+rect.height)/(double)(rect.height));
    }
    else {
        scale = Math.min((double)(start_point.x-end_point.x+rect.width/2.)/(double)(rect.width/2.),
                 (double)(start_point.y-end_point.y+rect.height)/(double)(rect.height));
    }
    scale = Math.max(0.,scale);        

    g.drawRect((int)(midx(rect)-rect.width*scale/2.),(int)(bottom(rect)-rect.height*scale),
           (int)(rect.width*scale),(int)(rect.height*scale));
    }    
    
    public Dimension getPreferredSize() {
    if( is_printing )
        return draw_area.getSize();
    return theOptions.getViewDimension(draw_area.getSize());
    }            
    
    public Dimension getMinimumSize() {
    return new Dimension(0,0);
    }

    public double screenToDataX(double length) {
    return length / thePlot.getDomainAxis().lengthToJava2D(1.,data_area,thePlot.getDomainAxisEdge());
    }

    public double screenToDataY(double length) {
    return length/thePlot.getRangeAxis().lengthToJava2D(1.,data_area,thePlot.getRangeAxisEdge());
    }

    public Point2D screenToDataCoords(Point2D p) {

    double x = thePlot.getDomainAxis().java2DToValue(p.getX(),data_area,thePlot.getDomainAxisEdge());
    double y = thePlot.getRangeAxis().java2DToValue(p.getY(),data_area,thePlot.getRangeAxisEdge());
    return new Point2D.Double(x,y);
    }
    
    public Point2D dataToScreenCoords(Point2D p) {

    double x = thePlot.getDomainAxis().valueToJava2D(p.getX(),data_area,thePlot.getDomainAxisEdge());
    double y = thePlot.getRangeAxis().valueToJava2D(p.getY(),data_area,thePlot.getRangeAxisEdge());
    return new Point2D.Double(x,y);
    }

    public AnnotationObject getAnnotationAtPoint(Point2D p) {
    for( Map.Entry<AnnotationObject,Rectangle> e : rectangles_complete.entrySet() ) {
        if( e.getValue().contains(p) )
        return e.getKey();        
    }    
    return null;
    }

    public AnnotationObject getConnectionAtPoint(Point2D p) {
    for( Map.Entry<AnnotationObject,Polygon> e : connections.entrySet() ) {
        if( e.getValue().intersects(p.getX()-3,p.getY()-3,6.,6.) )
        return e.getKey();        
    }    
    return null;
    }

    public AnnotationObject getCPAtPoint(Point2D p) {
    for( Map.Entry<AnnotationObject,Point2D> e : connections_cp.entrySet() ) {
        if( e.getValue().distance(p)<=3 )
        return e.getKey();        
    }    
    return null;
    }


    public Collection<AnnotationObject> getAnnotationsInside(Rectangle r) {
    Vector<AnnotationObject> ret = new Vector<AnnotationObject>();
    if( r!=null ) {
        for( Map.Entry<AnnotationObject,Rectangle> e : rectangles_complete.entrySet() ) {
        if( r.intersects(e.getValue()) )
            ret.add(e.getKey());
        }
    }
    return ret;
    }

    // selection

    public void selectAll() {
    selections.addAll(theDocument.getAnnotations());
    fireUpdatedSelection();
    }

    public void enforceSelection(Point2D p) {
    AnnotationObject a = getAnnotationAtPoint(p);
    if( a==null )
        a = getConnectionAtPoint(p);

    if( !isSelected(a) )
        setSelection(a);
    }
      

    public void resetSelection() {
    selections = new HashSet<AnnotationObject>();
    fireUpdatedSelection();
    }

    public boolean hasSelection() {
    return selections.size()>0;
    }

    public boolean isSelected(AnnotationObject a) {
    return selections.contains(a);
    }

    public void setSelection(AnnotationObject a) {
    selections.clear();
    if( a!=null )
        selections.add(a);
    fireUpdatedSelection();
    }

    public void addSelection(AnnotationObject a) {
    if( a!=null ) {
        selections.add(a);
        fireUpdatedSelection();
    }
    }
    
    public void setSelection(Collection<AnnotationObject> toselect) {
    selections.clear();
    selections.addAll(toselect);
    fireUpdatedSelection();
    }

    public void addSelection(Collection<AnnotationObject> toselect) {
    selections.addAll(toselect);
    fireUpdatedSelection();
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
    theOptions.setScale(scale);
    double scaleg = theGraphicOptions.setScale(theOptions.SCALE_GLYCANS);
    theOptions.setScale(scale*scaleg/theOptions.SCALE_GLYCANS); // set to nearest feasible scale

    updateView();
    }

    public double getScale() {
    return theOptions.SCALE;
    }    

    public void moveSelections(int dx, int dy) {
    HashSet<AnnotationObject> old_selections = selections;

    double ddx = screenToDataX(dx);
    double ddy = screenToDataY(dy);    
    theDocument.move(selections,ddx,ddy);
    
    selections = old_selections;
    }

    public void moveControlPointTo(AnnotationObject selection, Point2D p) {
    Point2D dp = screenToDataCoords(p);
    theDocument.moveControlPointTo(selection,dp.getX(),dp.getY());
    }

    public void rescaleSelections(double factor) {
    HashSet<AnnotationObject> old_selections = selections;

    theDocument.rescale(selections,factor);

    selections = old_selections;
    }

    public void resetSelectionsScale() {
    HashSet<AnnotationObject> old_selections = selections;

    theDocument.resetScale(selections);

    selections = old_selections;
    }


    public void highlightSelections() {
    HashSet<AnnotationObject> old_selections = selections;

    boolean all_highlighted = true;
    for( AnnotationObject a : selections )
        all_highlighted = all_highlighted && a.isHighlighted();
    
    theDocument.setHighlighted(selections,!all_highlighted);

    selections = old_selections;
    }


    public void updateAnnotations(Glycan parent, PeakAnnotationCollection pac, boolean merge) {
    // update document
    Vector<AnnotationObject> added = new Vector<AnnotationObject>();
    boolean changed = theDocument.updateData(parent,pac,added,false,merge);

    if( added.size()>0 ) {
        // update canvas
        updateDrawArea(false);
        resetSelection();
        repaint();
        
        // place new structures
        placeStructures(added,false);
    }
    else if( changed )
        theDocument.fireDocumentChanged();
    }

    public void cut() {
    copy();
    delete();
    }

    public void copy() {
    Vector<Glycan> sel_structures = new Vector<Glycan>();
    for( AnnotationObject a : selections ) 
        sel_structures.addAll(a.getStructures());
    ClipUtils.setContents(new GlycanSelection(theGlycanRenderer,sel_structures));
    }

    public void delete() {
    theDocument.remove(selections);    
    }   

    public void getScreenshot() {
    ClipUtils.setContents(getImage());
    }
    
    public BufferedImage getImage() {
    // Create an image that supports transparent pixels
    Dimension d = getPreferredSize();
    BufferedImage img = GraphicUtils.createCompatibleImage(d.width,d.height,false);    

    // prepare graphics context
    Graphics2D g2d = img.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);    
    g2d.setBackground(new Color(255,255,255,0));
    
    // paint
    is_printing = true;
    this.paint(g2d);
    is_printing = false;

    return img;
    }

    public void placeStructures() {
    placeStructures(theDocument.getAnnotations(),false);
    }
    
    private void placeStructures(boolean init) {
    placeStructures(theDocument.getAnnotations(),init);
    }

    private void placeStructures(Vector<AnnotationObject> annotations, boolean init) {
    
    // get starting point
    double y = thePlot.getRangeAxis().getRange().getUpperBound();    
    double cur_x = thePlot.getDomainAxis().getRange().getLowerBound();

    double all_width = 0.;
    for( AnnotationObject a : annotations ) {
        if( a.hasAnnotations() ) 
        all_width += screenToDataX(rectangles_complete.get(a).width);
    }

    double min_pp_x = annotations.firstElement().getPeakPoint().getX();
    double max_pp_x = annotations.lastElement().getPeakPoint().getX();
    double center_pp_x = (max_pp_x + min_pp_x)/2;

    cur_x = Math.max(cur_x,center_pp_x-all_width/2);

    // place annotations
    for( AnnotationObject a : annotations ) {
        Point2D pp = a.getPeakPoint();
        if( a.hasAnnotations() ) {
        double cur_width = screenToDataX(rectangles_complete.get(a).width);
        double x = cur_x + cur_width/2.;
        theDocument.getAnchor(a).setLocation(x,y);
        theDocument.getControlPoints().put(a,theDocument.computeControlPoint(new Point2D.Double(x,y),pp));
        
        cur_x += cur_width;        
        }
        else {
        theDocument.getAnchor(a).setLocation(pp.getX(),pp.getY());
        theDocument.getControlPoints().put(a,theDocument.computeControlPoint(pp,pp));
        }
    }

    // refine control points
    for( int i=0; i<annotations.size(); i++ ) {
        AnnotationObject ai = annotations.get(i);
        Point2D aai   = theDocument.getAnchor(ai);
        Point2D cpi   = theDocument.getControlPoint(ai);

        if( aai.getX()<cpi.getX() ) {
        for( int l=i+1; l<annotations.size(); l++ ) {
            AnnotationObject al = annotations.get(l);
            Point2D aal   = theDocument.getAnchor(al);
            Point2D cpl   = theDocument.getControlPoint(al);
         
            if( aal.getX()>cpi.getX() )
            break;
            if( cpl.getY()<cpi.getY() ) {
            cpl.setLocation(cpl.getX(),cpi.getY());
            ai = al;
            aai = aal;
            cpi = cpl;
            }
            else
            break;
        }
        }
        else {
        for( int l=i-1; l>=0; l-- ) {
            AnnotationObject al = annotations.get(l);
            Point2D aal   = theDocument.getAnchor(al);
            Point2D cpl   = theDocument.getControlPoint(al);

            if( aal.getX()<cpi.getX() )
            break;
            if( cpl.getY()<cpi.getY() ) {
            cpl.setLocation(cpl.getX(),cpi.getY());
            ai = al;
            aai = aal;
            cpi = cpl;
            }
            else 
            break;
        }
        }
    }

    // fire events
    if( init )
        theDocument.fireDocumentInit();
    else
        theDocument.fireDocumentChanged();
    }

    public boolean canGroupSelections() {
    return theDocument.canGroup(selections);
    }

    public void groupSelections() {    
    theDocument.group(selections);
    }

    public void ungroupSelections() {    
    theDocument.ungroup(selections,this);
    }

    // events
    public void documentInit(BaseDocument.DocumentChangeEvent e) {
    updateChart();
    //updateDrawArea(false);
    resetSelection();
    updateView();
    updateView();
    }

    public void documentChanged(BaseDocument.DocumentChangeEvent e) {
    updateDrawArea(false);
    resetSelection();
    thePlot.getRenderer().setPaint(theOptions.SPECTRUM_COLOR);
    repaint();
    }

    public void addSelectionChangeListener(SelectionChangeListener l) {
    if( l!=null )
        listeners.add(l);
    }

    public void removeSelectionChangeListener(SelectionChangeListener l) {
    if( l!=null )
        listeners.remove(l);
    }

    public void fireUpdatedSelection() {
    for( Iterator<SelectionChangeListener> i=listeners.iterator(); i.hasNext(); ) 
        i.next().selectionChanged(new SelectionChangeEvent(this));
    repaint();
    }

    public void updateView() {    
    updateDrawArea(true);
    updateData();
    repaint();
    }

    private void updateChart() {
    String x_label = "m/z ratio";
    String y_label = (theDocument.isShowRelativeIntensities()) ?"Intensity %" :"Intensity";
    theDataset = new DefaultXYDataset();

    if( theDocument.getPeakData()!=null ) {
        theChart = org.jfree.chart.ChartFactory.createScatterPlot(null, x_label, y_label, theDataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, false, false, false);
        thePlot = (XYPlot)theChart.getPlot();    
        thePlot.setRenderer(new StandardXYItemRenderer(StandardXYItemRenderer.LINES));
    }
    else {
        theChart = org.jfree.chart.ChartFactory.createScatterPlot(null, x_label, y_label, new XYBarDataset(theDataset,0.001), org.jfree.chart.plot.PlotOrientation.VERTICAL, false, false, false);
        thePlot = (XYPlot)theChart.getPlot();    
        thePlot.setRenderer(new XYBarRenderer());
    }

    
    if( theDocument.isShowRelativeIntensities() && theOptions.SHOW_MAX_INTENSITY ) {
        // set second axis
        ValueAxis second_axis = new NumberAxis("");
        thePlot.setRangeAxis(1, second_axis);
        
        // set dataset
        maxIntensityDataset = new DefaultXYDataset();       
        thePlot.setDataset(1, maxIntensityDataset);
        thePlot.mapDatasetToRangeAxis(1, 1);
        
        // set invisible renderer
        StandardXYItemRenderer r = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES);
        r.setBaseShapesVisible(false);
        thePlot.setRenderer(1,r);        
    }        

    theChart.setBackgroundPaint(Color.white);
    theChart.setBorderVisible(false);
    thePlot.getRenderer().setPaint(theOptions.SPECTRUM_COLOR);
    thePlot.setOutlinePaint(null);
    thePlot.setDomainGridlinesVisible(false);
    thePlot.setRangeGridlinesVisible(false);
    }

    private void updateData() {

    double[][] data = theDocument.getData(data_area,false);

    if( theDocument.isShowRelativeIntensities() && data!=null && data[0].length>0 ) {
        // get max intensity
        double min_int = data[1][0];
        double max_int = data[1][0];
        for( int i=1; i<data[1].length; i++ ) {
        max_int = Math.max(max_int,data[1][i]);
        min_int = Math.min(min_int,data[1][i]);
        }
        
        // normalize
        for( int i=0; i<data[1].length; i++ ) 
        data[1][i] = 100.*data[1][i]/max_int;

        // set max intensity data
        if( maxIntensityDataset!=null ) {                
        double[][] s = new double[2][];
        s[0] = new double[]{data[0][0],data[0][data[0].length-1]};
        s[1] = new double[]{min_int,max_int};
        maxIntensityDataset.removeSeries("max");        
        maxIntensityDataset.addSeries("max",s);        
        
        ((NumberAxis)thePlot.getRangeAxis(1)).setTickUnit(new org.jfree.chart.axis.NumberTickUnit(max_int));
        }
    }
    
    // set peak data
      theDataset.removeSeries("intensities");
    theDataset.addSeries("intensities",data);

  }

    protected void updateDrawArea(boolean update_chart) {

    // update data area
    if( update_chart ) {
        draw_area = theOptions.getDefaultDrawArea();
        chart_area = theOptions.getDefaultChartArea();
        data_area = null;       
    
        paintChart(GraphicUtils.createImage(theOptions.getDefaultViewDimension(),true).createGraphics());    
    }
    
    // update glycan bboxes
    Rectangle all_bbox = computeRectangles();

    // update draw area
    int add_left = Math.max(0,chart_area.x-all_bbox.x);
    int add_top = Math.max(0,chart_area.y-all_bbox.y);
    int add_right = Math.max(0,all_bbox.x+all_bbox.width-chart_area.x-chart_area.width);
    int add_bottom = Math.max(0,all_bbox.y+all_bbox.height-chart_area.y-chart_area.height);

    draw_area.width = chart_area.width + add_left + add_right + 2*theOptions.CHART_X_MARGIN;
    draw_area.height = chart_area.height + add_top + add_bottom + 2*theOptions.CHART_Y_MARGIN;
        
    chart_area.x = draw_area.x + add_left + theOptions.CHART_X_MARGIN;
    chart_area.y = draw_area.y + add_top + theOptions.CHART_Y_MARGIN;
    }

    public void mouseEntered(MouseEvent e) {    
    }
    
    public void mouseExited(MouseEvent e) {    
    }

    public boolean isInResizeCorner(AnnotationObject selection, Point p) {
    Rectangle rect = rectangles_complete.get(selection);

    int size = 2*theOptions.ANNOTATION_MARGIN/3;

    Rectangle corner1 = new Rectangle(left(rect),top(rect),size,size);
    if( corner1.contains(p) )
        return true;

    Rectangle corner2 = new Rectangle(right(rect)-size,top(rect),size,size);
    if( corner2.contains(p) )
        return true;

    return false;
    }

    public void mousePressed(MouseEvent e) {
    if( MouseUtils.isPushTrigger(e) || MouseUtils.isCtrlPushTrigger(e) ) {
        start_position = getAnnotationAtPoint(e.getPoint());
        mouse_start_point = e.getPoint();
        mouse_end_point = null;
        
        if( start_position!=null ) {
        if( isInResizeCorner(start_position,mouse_start_point) ) { 
            // start resizing
            setSelection(start_position);
            is_resizing = true;
        }
        else {        
            // start DnD
            if( !isSelected(start_position) )
            setSelection(start_position);
            is_dragndrop = true;
        }
        }
        else {
        start_position = getCPAtPoint(e.getPoint());
        if( start_position!=null ) {
            // start moving cp
            setSelection(start_position);
            is_movingcp = true;
        }
        else {
            // start selection
            is_dragndrop = false;
        }
        }
    }
    was_dragged = false;
    }        
    
    public void mouseMoved(MouseEvent e) {
    }
    
    public void mouseDragged(MouseEvent e) {
    was_dragged = true;

    // if is dragging don't update selection
    if( is_dragndrop ) {    
        if( mouse_end_point!=null )
        xorSelections(mouse_start_point,mouse_end_point);
        mouse_end_point = e.getPoint();                    
        xorSelections(mouse_start_point,mouse_end_point);            
    }
    else if( is_resizing ) {        
        if( mouse_end_point!=null )
        xorResizing(start_position,mouse_start_point,mouse_end_point);
        mouse_end_point = e.getPoint();                    
        xorResizing(start_position,mouse_start_point,mouse_end_point);
    }
    else if( is_movingcp ) {
        if( mouse_end_point!=null )
        xorConnection(start_position,mouse_start_point,mouse_end_point);
        mouse_end_point = e.getPoint();                    
        xorConnection(start_position,mouse_start_point,mouse_end_point);
    }
    else if( mouse_start_point!=null ) {        
        if( mouse_end_point!=null )
        xorRectangle(mouse_start_point,mouse_end_point);
        mouse_end_point = e.getPoint();                    
        xorRectangle(mouse_start_point,mouse_end_point);            
    }

    dragAndScroll(e);    
    }        
    
    public void mouseReleased(MouseEvent e) {

    // Drag and drop
    if( is_dragndrop && was_dragged ) {
        if( mouse_end_point!=null )
        xorSelections(mouse_start_point,mouse_end_point);

        moveSelections(e.getPoint().x-mouse_start_point.x,
               e.getPoint().y-mouse_start_point.y);
    }
    else if( is_resizing && was_dragged ) {
        if( mouse_end_point!=null )
        xorResizing(start_position,mouse_start_point,mouse_end_point);      
        rescaleSelections(scaleFactor(start_position,mouse_start_point,e.getPoint()));
    }
    else if( is_movingcp && was_dragged ) {
        if( mouse_end_point!=null )
        xorConnection(start_position,mouse_start_point,mouse_end_point);      
        moveControlPointTo(start_position,e.getPoint());
    }
    else if( mouse_start_point!=null ) {
        if( mouse_end_point!=null )
        xorRectangle(mouse_start_point,mouse_end_point);
        
        Rectangle mouse_rect = makeRectangle(mouse_start_point,e.getPoint());
        if( MouseUtils.isNothingPressed(e) ) 
        setSelection(getAnnotationsInside(mouse_rect));                
        else if( MouseUtils.isCtrlPressed(e) ) 
        addSelection(getAnnotationsInside(mouse_rect));        
    }
    
    // reset
    start_position = null;
    is_resizing = false;
    is_movingcp = false;
    is_dragndrop = false;
    was_dragged = false;
    mouse_start_point = null;
    mouse_end_point = null;
    repaint();
    }
    
    public void mouseClicked(MouseEvent e) {      
    AnnotationObject a = getAnnotationAtPoint(e.getPoint());
    if( a==null )
        a = getConnectionAtPoint(e.getPoint());

    if( a!=null ) {
        if( MouseUtils.isAddSelectTrigger(e) ||
        MouseUtils.isSelectAllTrigger(e) )
        addSelection(a);    
        else if( MouseUtils.isSelectTrigger(e) ) 
        setSelection(a);
    }
    else
        resetSelection();
    }



    private void dragAndScroll(MouseEvent e) {
    // move view if near borders                
    Point point = e.getPoint();
    JViewport view = theScrollPane.getViewport();
    Rectangle inner = view.getViewRect();
    inner.grow(-10,-10);
    
    if( !inner.contains(point) ) {
        Point orig = view.getViewPosition(); 
        if( point.x<inner.x )
        orig.x -= 10;
        else if( point.x>(inner.x+inner.width) )
        orig.x += 10;            
        if( point.y<inner.y )
        orig.y -= 10;
        else if( point.y>(inner.y+inner.height) )
        orig.y += 10;
        
        int maxx = getBounds().width-view.getViewRect().width;
        int maxy = getBounds().height-view.getViewRect().height;
        if( orig.x<0 )
        orig.x = 0;
        if( orig.x>maxx )
        orig.x = maxx;
        if( orig.y<0 )
        orig.y = 0;
        if( orig.y>maxy )
        orig.y = maxy;
        
        view.setViewPosition(orig);                    
    }    
    }

    

}
