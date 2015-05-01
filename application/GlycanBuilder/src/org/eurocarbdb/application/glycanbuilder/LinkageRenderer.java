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

package org.eurocarbdb.application.glycanbuilder;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

import static org.eurocarbdb.application.glycanbuilder.Geometry.*;

/**
   Objects of this class are used to create a graphical representation
   of a {@link Linkage} object given the current graphic options
   ({@link GraphicOptions}. The rules to draw the linkage in the
   different notations are stored in the {@link
   LinkageStyleDictionary}.

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class LinkageRenderer {

    protected LinkageStyleDictionary  theLinkageStyleDictionary; 
              
    protected GraphicOptions theGraphicOptions;

    /**
       Empty constructor.      
     */
    public LinkageRenderer() {
    theLinkageStyleDictionary = new LinkageStyleDictionary();

     theGraphicOptions = new GraphicOptions();
    }

    /**
       Create a new linkage renderer copying the style dictionary and
       graphic options from the <code>src</code> object.
    */
    public LinkageRenderer(GlycanRenderer src) {
    theLinkageStyleDictionary = src.getLinkageStyleDictionary();
     theGraphicOptions = src.getGraphicOptions();
    }

    /**
       Return the graphic options used by this object.
     */
    public GraphicOptions getGraphicOptions() {
    return theGraphicOptions;
    }

    /**
       Set the graphic options used by this object.
     */
    public void setGraphicOptions(GraphicOptions opt) {
    theGraphicOptions = opt;
    }   

    /**
       Return the linkage style dictionary used by this object.
     */
    public LinkageStyleDictionary getLinkageStyleDictionary() {
    return theLinkageStyleDictionary; 
    }

    /**
       Set the linkage style dictionary used by this object.
     */
    public void setLinkageStyleDictionary(LinkageStyleDictionary linkageStyleDictionary) {
    theLinkageStyleDictionary = linkageStyleDictionary;
    }


    // --------
    
    /**
       Draw the line part of a linkage on a graphic context using the
       specified bounding boxes.
       @param g2d the graphic context
       @param link the linkage to be drawn
       @param selected <code>true</code> if the residue should be
       shown as selected
       @param parent_bbox the bounding box of the parent residue
       @param parent_border_bbox the bounding box of the parent
       residue including the residues on border
       @param child_bbox the bounding box of the child residue
       @param child_border_bbox the bounding box of the child residue
       including the residues on border
     */
    public void paintEdge(Graphics2D g2d, Linkage link, boolean selected, Rectangle parent_bbox, Rectangle parent_border_bbox, Rectangle child_bbox, Rectangle child_border_bbox) {

    if( link==null )
        return;

    Stroke edge_stroke = createStroke(link,selected);
    Shape edge_shape   = createShape(link,parent_bbox,child_bbox);

    // draw edge
    if( edge_shape!=null ) {
        g2d.setStroke(edge_stroke);
        g2d.setColor(Color.black);
        g2d.draw(edge_shape);    
        g2d.setStroke(new BasicStroke(1));
    }

    // paint linkage info
    //if( theGraphicOptions.SHOW_INFO )
    //paintInfo(g2d,link,parent_bbox,parent_border_bbox,child_bbox,child_border_bbox);    
    }

    /**
       Draw the text part of a linkage on a graphic context using the
       specified bounding boxes.
       @param g2d the graphic context
       @param link the linkage to be drawn
       @param parent_bbox the bounding box of the parent residue
       @param parent_border_bbox the bounding box of the parent
       residue including the residues on border
       @param child_bbox the bounding box of the child residue
       @param child_border_bbox the bounding box of the child residue
       including the residues on border
     */
    public void paintInfo(Graphics2D g2d, Linkage link, Rectangle parent_bbox, Rectangle parent_border_bbox, Rectangle child_bbox, Rectangle child_border_bbox) {

    if( link==null || !theGraphicOptions.SHOW_INFO )
        return;

    LinkageStyle style = theLinkageStyleDictionary.getStyle(link);

    Font old_font = g2d.getFont();
    Font new_font = new Font(theGraphicOptions.LINKAGE_INFO_FONT_FACE,Font.PLAIN,theGraphicOptions.LINKAGE_INFO_SIZE);
    g2d.setFont(new_font);

    Residue child = link.getChildResidue();
    if( style.showParentLinkage(link) )
        paintInfo(g2d,link.getParentPositionsString(),parent_bbox,parent_border_bbox,child_bbox,child_border_bbox,true,false,link.hasMultipleBonds());
    if( style.showAnomericCarbon(link) ) 
        paintInfo(g2d,link.getChildPositionsString(),parent_bbox,parent_border_bbox,child_bbox,child_border_bbox,false,true,link.hasMultipleBonds());
    if( style.showAnomericState(link,child.getAnomericState()) ) 
        paintInfo(g2d,TextUtils.toGreek(child.getAnomericState()),parent_bbox,parent_border_bbox,child_bbox,child_border_bbox,false,false,link.hasMultipleBonds());        

    g2d.setFont(old_font);
    }

    private void paintInfo(Graphics2D g2d, String text, Rectangle p, Rectangle pb, Rectangle c, Rectangle cb, boolean toparent, boolean above, boolean multiple) {       
    Dimension tb = textBounds(text,theGraphicOptions.LINKAGE_INFO_FONT_FACE,theGraphicOptions.LINKAGE_INFO_SIZE);
    Point pos = computePosition(tb,p,pb,c,cb,toparent,above,multiple);
    
    g2d.clearRect(pos.x,(int)(pos.y-tb.getHeight()),(int)tb.getWidth(),(int)tb.getHeight());
    g2d.drawString(text,pos.x,pos.y);
    }

    private Point computePosition(Dimension tb, Rectangle p, Rectangle pb, Rectangle c, Rectangle cb, boolean toparent, boolean above, boolean multiple) {       
    Point cp = center(p);
    Point cc = center(c);

    double r = 0.5 * theGraphicOptions.LINKAGE_INFO_SIZE;
    double cx=0.,cy=0.,R=0.,angle=0.;
    if( toparent ) {
        cx = cp.x;
        cy = cp.y;
        angle = angle(cc,cp);
        R = getExclusionRadius(cp,angle,pb)+2;
    }
    else {
        cx = c.x+c.width/2;
        cy = c.y+c.height/2;
        angle = angle(cp,cc);
        R = getExclusionRadius(cc,angle,cb)+2;
    }
    double space = (multiple) ?4. :2.;
    
    /*double pangle = angle(cc,cp);
    boolean add = (pangle>-Math.PI/2. && pangle<Math.PI/2.);
    if( !above )
        add = !add;
    */

    boolean add = above;
    if( toparent )
        add = !add;

    double tx=0.,ty=0.;
    if( add ) {
        tx = cx+(R+r)*Math.cos(angle)+(r+space)*Math.cos(angle-Math.PI/2.);
        ty = cy+(R+r)*Math.sin(angle)+(r+space)*Math.sin(angle-Math.PI/2.);
    }
    else {
        tx = cx+(R+r)*Math.cos(angle)+(r+space)*Math.cos(angle+Math.PI/2.);
        ty = cy+(R+r)*Math.sin(angle)+(r+space)*Math.sin(angle+Math.PI/2.);
    }    

    //tx -= theGraphicOptions.LINKAGE_INFO_SIZE/2.;
    //ty += theGraphicOptions.LINKAGE_INFO_SIZE/2.;
    tx -= tb.getWidth()/2;
    ty += tb.getHeight()/2;

    return new Point((int)tx,(int)ty);
    }       

    private Stroke createStroke(Linkage link, boolean selected) {
    LinkageStyle style = theLinkageStyleDictionary.getStyle(link);

    if( style.isDashed() ) {
        float[] dashes = {5.f,5.f};
        return new BasicStroke((selected) ?2.f :1.f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,1.f,dashes,0.f);
    }

    return new BasicStroke((selected) ?2.f :1.f);
    }

    static private Shape createLine(Point p1, Point p2, boolean multiple) {
    if( multiple ) {
        GeneralPath gp = new GeneralPath();
        double a = angle(p1,p2);

        Shape line1 = createLine(translate(p1,2.*Math.cos(a+Math.PI/2),2.*Math.sin(a+Math.PI/2)),
                     translate(p2,2.*Math.cos(a+Math.PI/2),2.*Math.sin(a+Math.PI/2)),
                     false);
        gp.append(line1,false);

        Shape line2 = createLine(translate(p1,2.*Math.cos(a-Math.PI/2),2.*Math.sin(a-Math.PI/2)),
                     translate(p2,2.*Math.cos(a-Math.PI/2),2.*Math.sin(a-Math.PI/2)),
                     false);
        gp.append(line2,false);

        return gp;
    }
    return createLine(p1,p2);
    }

    static private Shape createLine(Point p1, Point p2) {    
    Polygon l = new Polygon();
    l.addPoint(p1.x,p1.y);
    l.addPoint(p2.x,p2.y);
    return l;
    }

    static private Shape createCurve(Point p1, Point p2) {
    
    double cx = (p1.x+p2.x)/2.;
    double cy = (p1.y+p2.y)/2.;
    double r = distance(p1,p2)/2.;
    double angle = angle(p1,p2);

    // start point
    double x1 = cx+r*Math.cos(angle);
    double y1 = cy+r*Math.sin(angle);

    // end point
    double x2 = cx+r*Math.cos(angle+Math.PI);
    double y2 = cy+r*Math.sin(angle+Math.PI);

    // ctrl point 1
    double cx1 = cx+0.1*r*Math.cos(angle);
    double cy1 = cy+0.1*r*Math.sin(angle);
    double tx1 = cx1+r*Math.cos(angle+Math.PI/2.);
    double ty1 = cy1+r*Math.sin(angle+Math.PI/2.);

    // ctrl point 2
    double cx2 = cx+0.1*r*Math.cos(angle+Math.PI);
    double cy2 = cy+0.1*r*Math.sin(angle+Math.PI);
    double tx2 = cx2+r*Math.cos(angle-Math.PI/2.);
    double ty2 = cy2+r*Math.sin(angle-Math.PI/2.);    
    
    return new CubicCurve2D.Double(x1,y1,tx1,ty1,tx2,ty2,x2,y2);
    }
    

    static private Shape createCurve(Point p1, Point p2, boolean multiple) {
    if( multiple ) {
        GeneralPath gp = new GeneralPath();
        double a = angle(p1,p2);
            
        Shape curve1 = createCurve(translate(p1,2.*Math.cos(a+Math.PI/2),2.*Math.sin(a+Math.PI/2)),
                       translate(p2,2.*Math.cos(a+Math.PI/2),2.*Math.sin(a+Math.PI/2)),
                       false);
        gp.append(curve1,false);

        Shape curve2 = createCurve(translate(p1,2.*Math.cos(a-Math.PI/2),2.*Math.sin(a-Math.PI/2)),
                       translate(p2,2.*Math.cos(a-Math.PI/2),2.*Math.sin(a-Math.PI/2)),
                       false);
        gp.append(curve2,false);

        return gp;
    }
    return createCurve(p1,p2);
    }

    private Shape createShape(Linkage link, Rectangle parent_bbox, Rectangle child_bbox) {
    LinkageStyle style = theLinkageStyleDictionary.getStyle(link);
    String edge_style  = style.getShape(); 
    
    Point parent_center = center(parent_bbox);
    Point child_center  = center(child_bbox);

    if( edge_style.equals("none") )
        return null;
    if( edge_style.equals("empty") )
        return null;
    if( edge_style.equals("line") )
        return createLine(parent_center,child_center,link.hasMultipleBonds());
    if( edge_style.equals("curve") )
        return createCurve(parent_center,child_center,link.hasMultipleBonds());
    
    return createLine(parent_center,child_center);
    }
}