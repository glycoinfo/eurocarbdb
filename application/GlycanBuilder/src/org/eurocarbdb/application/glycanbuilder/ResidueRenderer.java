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

package org.eurocarbdb.application.glycanbuilder;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.font.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import static org.eurocarbdb.application.glycanbuilder.Geometry.*;

/**
   Objects of this class are used to create a graphical representation
   of a {@link Residue} object given the current graphic options
   ({@link GraphicOptions}. The rules to draw the residue in the
   different notations are stored in the {@link
   ResidueStyleDictionary}.   

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class ResidueRenderer {
     
    protected ResidueStyleDictionary theResidueStyleDictionary; 
   
    protected GraphicOptions theGraphicOptions;

    /**
       Empty constructor.      
     */
    public ResidueRenderer() {
    theResidueStyleDictionary = new ResidueStyleDictionary();
     theGraphicOptions = new GraphicOptions();
    }

    /**
       Create a new residue renderer copying the style dictionary and
       graphic options from the <code>src</code> object.
    */
    public ResidueRenderer(GlycanRenderer src) {
    theResidueStyleDictionary = src.getResidueStyleDictionary();
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
       Return the residue style dictionary used by this object.
     */
    public ResidueStyleDictionary getResidueStyleDictionary() {
    return theResidueStyleDictionary; 
    }

    /**
       Set the residue style dictionary used by this object.
     */
    public void setResidueStyleDictionary(ResidueStyleDictionary residueStyleDictionary) {
    theResidueStyleDictionary = residueStyleDictionary;
    }

    // --- Data access
    
    /**
       Return a graphical representation of a residue type as an icon
       of <code>max_y_size</code> height.
     */
    public Icon getIcon(ResidueType type, int max_y_size) {
    int orientation = theGraphicOptions.ORIENTATION;
    theGraphicOptions.ORIENTATION = GraphicOptions.RL;
    
    // compute bounding box
    Residue node = new Residue(type);
    Rectangle bbox = computeBoundingBox(node,false,4,4,new ResAngle(),max_y_size-8,max_y_size-8);
    
    // Create an image that supports transparent pixels
    BufferedImage img = GraphicUtils.createCompatibleImage(bbox.width+8, bbox.height+8, false);

    // create a graphic context
    Graphics2D g2d = img.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);    
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);    
    g2d.setBackground(new Color(255,255,255,0));

    // paint the residue
    paint(g2d,node,false,false,null,bbox,null,new ResAngle());

    theGraphicOptions.ORIENTATION = orientation;
    return new ImageIcon(img);
    }
    
    public Image getImage(ResidueType type, int max_y_size) {
    	int orientation = theGraphicOptions.ORIENTATION;
        theGraphicOptions.ORIENTATION = GraphicOptions.RL;
        
        // compute bounding box
        Residue node = new Residue(type);
        Rectangle bbox = computeBoundingBox(node,false,4,4,new ResAngle(),max_y_size-8,max_y_size-8);
        
        // Create an image that supports transparent pixels
        BufferedImage img = GraphicUtils.createCompatibleImage(bbox.width+8, bbox.height+8, false);

        // create a graphic context
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);    
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);    
        g2d.setBackground(new Color(255,255,255,0));

        // paint the residue
        paint(g2d,node,false,false,null,bbox,null,new ResAngle());

        theGraphicOptions.ORIENTATION = orientation;
        
        
        
        return Toolkit.getDefaultToolkit().createImage(img.getSource());
    }
    
    //-----------
    // Bounding box

    /**
       Return the text to be written in the residue representation
       given the residue style in the current notation.
     */
    public String getText(Residue node) {
    if( node==null ) 
        return "";
    
    ResidueType  type  = node.getType();
    ResidueStyle style = theResidueStyleDictionary.getStyle(node);
    String text = style.getText();    

    return (text!=null) ?text :type.getResidueName();
    }

    /**
       Return the text to be written in the residue representation
       given the residue style in the current notation.
       @param on_border <code>true</code> if the residue is displayed
       on the border of its parent, used for substitutions and
       modifications
     */
    public String getText(Residue node, boolean on_border) {
    // special cases
    if( node==null )
        return "";
    if( on_border && node.isSpecial() && !node.isLCleavage() )
        return "*";

    // get text
    String text = null;
    if( on_border && node.isLCleavage() ) 
        text = getText(node.getCleavedResidue());
    else
        text = getText(node);

    // add linkage 
    if( on_border && !node.getParentLinkage().hasUncertainParentPositions() && theGraphicOptions.SHOW_INFO )
        text =  node.getParentLinkage().getParentPositionsString() + text;

    // add brackets for cleavages    
    if( on_border && node.isLCleavage() ) 
        text = "(" + text + ")";

    return text;
    }

    protected Rectangle computeBoundingBox(Residue node, boolean on_border, int x, int y, ResAngle orientation, int node_size, int max_y_size) {    

    // get style
    ResidueStyle style = theResidueStyleDictionary.getStyle(node);
    String shape = style.getShape();

    // compute dimensions
    if( max_y_size<node_size )
        node_size = max_y_size;
    //if( shape==null )    

    Dimension dim;    
    if( shape==null || on_border ) {
        String text = getText(node,on_border);

        int font_size = theGraphicOptions.NODE_FONT_SIZE;
        int x_size = textBounds(text,theGraphicOptions.NODE_FONT_FACE,font_size).width;

        if( x_size > node_size  )         
        dim = new Dimension(x_size,node_size);        
        else
        dim = new Dimension(node_size,node_size);    

        orientation = theGraphicOptions.getOrientationAngle();
    }
    else if( shape.equals("startrep") || shape.equals("endrep") ) {
        int size = Math.min(node_size*2,max_y_size);
        int font_size = theGraphicOptions.LINKAGE_INFO_SIZE;

        dim = new Dimension(size/2,size+2*font_size);
    }
    else if( shape.equals("point") )
        dim = new Dimension(1,1);
    else
        dim = new Dimension(node_size,node_size);
    
    // return bounding box
    if( orientation.equals(0) || orientation.equals(180) )
        return new Rectangle(x,y,dim.width,dim.height);
    return new Rectangle(x,y,dim.height,dim.width);
    }
    

    // ----------    
    // Painting

    static private int sat(int v, int t) {
    if( v>t )
        return t;
    return v;
    }

    static private int sig(int v) {
    return 128+v/2;
    //return v/2;
    }   

    /**
       Draw a residue on a graphic context using the specified
       bounding box.
       @param g2d the graphic context
       @param node the residue to be drawn
       @param selected <code>true</code> if the residue should be
       shown as selected
       @param on_border <code>true</code> if the residue should be
       drawn on the border of its parent
       @param par_bbox the bounding box of the parent residue
       @param cur_bbox the bounding box of the current residue
       @param sup_bbox the bounding box used to decide the spatial
       orientation of the residue
       @param orientation the orientation of the residue
     */
    public void paint(Graphics2D g2d, Residue node, boolean selected, boolean on_border, Rectangle par_bbox, Rectangle cur_bbox, Rectangle sup_bbox, ResAngle orientation) {
    paint(g2d,node,selected,true,on_border,par_bbox,cur_bbox,sup_bbox,orientation);
    }

    /**
       Draw a residue on a graphic context using the specified
       bounding box.
       @param g2d the graphic context
       @param node the residue to be drawn
       @param selected <code>true</code> if the residue should be
       shown as selected
       @param active <code>true</code> if the residue should be
       shown as active
       @param on_border <code>true</code> if the residue should be
       drawn on the border of its parent
       @param par_bbox the bounding box of the parent residue
       @param cur_bbox the bounding box of the current residue
       @param sup_bbox the bounding box used to decide the spatial
       orientation of the residue
       @param orientation the orientation of the residue
     */
    public void paint(Graphics2D g2d, Residue node, boolean selected, boolean active, boolean on_border, Rectangle par_bbox, Rectangle cur_bbox, Rectangle sup_bbox, ResAngle orientation) {
    if( node==null )
        return;

    ResidueStyle style = theResidueStyleDictionary.getStyle(node);    
    
    // draw shape
    Shape  shape = createShape(node,par_bbox,cur_bbox,sup_bbox,orientation);
    Shape  text_shape = createTextShape(node,par_bbox,cur_bbox,sup_bbox,orientation);
    Shape  fill_shape  = createFillShape(node,cur_bbox);       

    Color shape_color  = style.getShapeColor();
    Color fill_color   = style.getFillColor();
    Color text_color = style.getTextColor();
    if( selected )
        fill_color = new Color(sig(fill_color.getRed()),sig(fill_color.getGreen()),sig(fill_color.getBlue()));
    if( !active ) {
        shape_color = new Color(sig(shape_color.getRed()),sig(shape_color.getGreen()),sig(shape_color.getBlue()));
        fill_color = new Color(sig(fill_color.getRed()),sig(fill_color.getGreen()),sig(fill_color.getBlue()));
        text_color = new Color(sig(text_color.getRed()),sig(text_color.getGreen()),sig(text_color.getBlue()));
    }
        
    if( shape!=null && !on_border ) {      
        if( fill_shape!=null ) {
        //Object old_hint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);    
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);

        Shape old_clip = g2d.getClip();
        g2d.clip(shape);

        g2d.setColor((style.isFillNegative()) ?fill_color :Color.white);
        g2d.fill(shape);

        g2d.setColor((style.isFillNegative()) ?Color.white :fill_color);
        g2d.fill(fill_shape);
        
        //if( old_hint!=null )
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,old_hint);

        g2d.setColor(shape_color);        
        g2d.draw(fill_shape);
    
        g2d.setClip(old_clip);
        }

        // draw contour
        g2d.setStroke( (selected) ?new BasicStroke(2) :new BasicStroke(1));
        g2d.setColor(shape_color);        
        g2d.draw(shape);        
        g2d.setStroke(new BasicStroke(1));
    }
    else if( selected ) {
        // draw selected contour for empty shape
        float[] dashes = {5.f,5.f};
        g2d.setStroke(new BasicStroke(2.f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,1.f,dashes,0.f));
        g2d.setColor(shape_color);        
        g2d.draw(cur_bbox);
        g2d.setStroke(new BasicStroke(1));
    }

    // add text shape
    if( text_shape!=null ) {
        g2d.setColor(shape_color);
        g2d.fill(text_shape);
    }
    

    // draw text    
    if( shape==null || on_border || style.getText()!=null ) {
        if( shape==null || on_border )
        orientation = theGraphicOptions.getOrientationAngle();
        else if( style.getText()!=null )
        orientation = new ResAngle(0);

        String text = getText(node,on_border);

        // set font
        //int font_size = sat(9*cur_bbox.width/text.length()/10,theGraphicOptions.NODE_FONT_SIZE);

        int font_size = theGraphicOptions.NODE_FONT_SIZE;
        int x_size    = textBounds(text,theGraphicOptions.NODE_FONT_FACE,font_size).width;
        if( shape!=null ) 
        font_size = sat(8 * font_size * cur_bbox.width / x_size / 10,font_size);
        
        Font new_font = new Font(theGraphicOptions.NODE_FONT_FACE,Font.PLAIN,font_size);
        Font old_font = g2d.getFont();
        g2d.setFont(new_font);
        
        // compute bounding rect
        Rectangle2D.Double text_bound = new Rectangle2D.Double();
        text_bound.setRect(new TextLayout(text,new_font,g2d.getFontRenderContext()).getBounds());
        
        // draw text
        g2d.setColor(text_color);    
        if( orientation.equals(0) || orientation.equals(180) ) {
        Rectangle2D.Double text_rect = new Rectangle2D.Double(midx(cur_bbox)-text_bound.width/2,midy(cur_bbox)-text_bound.height/2,text_bound.width,text_bound.height);
        if( shape==null || fill_shape==null ) 
            g2d.clearRect((int)text_rect.x,(int)text_rect.y,(int)text_rect.width,(int)text_rect.height);
        g2d.drawString(text,(int)text_rect.x,(int)(text_rect.y+text_rect.height));
        }
        else {
        Rectangle2D.Double text_rect = new Rectangle2D.Double(midx(cur_bbox)-text_bound.height/2,midy(cur_bbox)-text_bound.width/2,text_bound.height,text_bound.width);
        if( shape==null || fill_shape==null ) 
            g2d.clearRect((int)text_rect.x,(int)text_rect.y,(int)text_rect.width,(int)text_rect.height);        

        g2d.rotate(-Math.PI/2.0); 
        g2d.drawString(text,-(int)(text_rect.y+text_rect.height),(int)(text_rect.x+text_rect.width));
        g2d.rotate(+Math.PI/2.0); 
        }
        
        g2d.setFont(old_font);
    }

    //g2d.setColor(Color.black);
    //g2d.drawString(""+node.id,left(cur_bbox),bottom(cur_bbox));
    }        

    //------------
    // Shape              


    static private Polygon createDiamond(double x, double y, double w, double h) {
    if( (w%2)==1 )
        w++;
    if( (h%2)==1 )
        h++;
        
    Polygon p = new Polygon();
    p.addPoint((int)(x+w/2), (int)(y));
    p.addPoint((int)(x+w),   (int)(y+h/2));
    p.addPoint((int)(x+w/2), (int)(y+h));
    p.addPoint((int)(x),     (int)(y+h/2));
    return p;
    }

    static private Shape createHatDiamond(double angle, double x, double y, double w, double h) {
    GeneralPath f = new GeneralPath();
    
    // append diamond
    f.append(createDiamond(x,y,w,h),false);

    // append hat
    Polygon p = new Polygon();
    p.addPoint((int)(x-2),(int)(y+h/2-2));
    p.addPoint((int)(x+w/2-2),(int)(y-2));
    f.append(p,false);
    
    return f;
    }

    static private Shape createRHatDiamond(double angle, double x, double y, double w, double h) {
    GeneralPath f = new GeneralPath();
    
    // append diamond
    f.append(createDiamond(x,y,w,h),false);

    // append hat
    Polygon p = new Polygon();
    p.addPoint((int)(x+w+2),(int)(y+h/2-2));
    p.addPoint((int)(x+w/2+2),(int)(y-2));
    f.append(p,false);
    
    return f;
    }

    static private Polygon createRhombus(double x, double y, double w, double h) {
    Polygon p = new Polygon();
    p.addPoint((int)(x+0.50*w), (int)(y));
    p.addPoint((int)(x+0.85*w), (int)(y+0.50*h));
    p.addPoint((int)(x+0.50*w), (int)(y+h));
    p.addPoint((int)(x+0.15*w), (int)(y+0.50*h));
    return p;
    }

    static private Polygon createTriangle(double angle, double x, double y, double w, double h) {
    Polygon p = new Polygon();
    if( angle>=-Math.PI/4. && angle<=Math.PI/4. ) {
        //pointing right
        p.addPoint((int)(x+w), (int)(y+h/2));
        p.addPoint((int)(x),   (int)(y+h));
        p.addPoint((int)(x),   (int)(y));
    }
    else if( angle>=Math.PI/4. && angle<=3.*Math.PI/4. ) {
        //pointing down
        p.addPoint((int)(x+w/2), (int)(y+h));
        p.addPoint((int)(x),     (int)(y));
        p.addPoint((int)(x+w),   (int)(y));
    }
    else if( angle>=-3.*Math.PI/4. && angle<=-Math.PI/4. ) {
        // pointing up
        p.addPoint((int)(x+w/2), (int)(y));
        p.addPoint((int)(x+w),   (int)(y+h));
        p.addPoint((int)(x),     (int)(y+h));
    }
    else {
        //pointing left
        p.addPoint((int)(x),   (int)(y+h/2));
        p.addPoint((int)(x+w), (int)(y+h));
        p.addPoint((int)(x+w), (int)(y));

    }
    
    return p;
    }

    static private Polygon createStar(double x, double y, double w, double h, int points) {
    double rx = w/2.;
    double ry = h/2.;
    double cx = x+w/2.;
    double cy = y+h/2.;
        
    double step = Math.PI/(double)points;         
    double nstep = Math.PI/2.-2.*step;

    double mrx = rx/(Math.cos(step)+Math.sin(step)/Math.tan(nstep));
    double mry = ry/(Math.cos(step)+Math.sin(step)/Math.tan(nstep));

    Polygon p = new Polygon();
    for(int i=0; i<=2*points; i++ ) {
        if( (i%2)==0 ) 
        p.addPoint((int)(cx+rx*Math.cos(i*step-Math.PI/2.)),(int)(cy+ry*Math.sin(i*step-Math.PI/2.)));
        else
        p.addPoint((int)(cx+mrx*Math.cos(i*step-Math.PI/2.)),(int)(cy+mry*Math.sin(i*step-Math.PI/2.)));
    }        
    return p;
    }    

    static private Polygon createPentagon(double x, double y, double w, double h) {
    double rx = w/2.;
    double ry = h/2.;
    double cx = x+w/2.;
    double cy = y+h/2.;
        
    double step = Math.PI/2.5;        
    Polygon p = new Polygon();
    for(int i=0; i<=5; i++ ) {
        p.addPoint((int)(cx+rx*Math.cos(i*step-Math.PI/2.)),(int)(cy+ry*Math.sin(i*step-Math.PI/2.)));
    }        
    return p;
    }

    static private Polygon createHexagon(double x, double y, double w, double h) {
    double rx = w/2.;
    double ry = h/2.;
    double cx = x+w/2.;
    double cy = y+h/2.;
        
    double step = Math.PI/3.;        
    Polygon p = new Polygon();
    for(int i=0; i<=6; i++ ) {
        p.addPoint((int)(cx+rx*Math.cos(i*step)),(int)(cy+ry*Math.sin(i*step)));
    }        
    return p;
    }

    static private Polygon createHeptagon(double x, double y, double w, double h) {
    double rx = w/2.;
    double ry = h/2.;
    double cx = x+w/2.;
    double cy = y+h/2.;
        
    double step = Math.PI/3.5;        
    Polygon p = new Polygon();
    for(int i=0; i<=7; i++ ) {
        p.addPoint((int)(cx+rx*Math.cos(i*step-Math.PI/2.)),(int)(cy+ry*Math.sin(i*step-Math.PI/2.)));
    }        
    return p;
    }

    static private Shape createLine(double angle, double x, double y, double w, double h) {    
    
    double rx = w/2.;
    double ry = h/2.;
    double cx = x+w/2.;
    double cy = y+h/2.;

    Polygon p = new Polygon();

    double x1 = cx+rx*Math.cos(angle-Math.PI/2.);
    double y1 = cy+ry*Math.sin(angle-Math.PI/2.);
    p.addPoint((int)x1,(int)y1);

    double x2 = cx+rx*Math.cos(angle+Math.PI/2.);
    double y2 = cy+ry*Math.sin(angle+Math.PI/2.);
    p.addPoint((int)x2,(int)y2);
               
    return p;
    }


    static private Shape createCleavage(double angle, double x, double y, double w, double h, boolean has_oxygen) {

    GeneralPath f = new GeneralPath();

    double rx = w/2.;
    double ry = h/2.;
    double cx = x+w/2.;
    double cy = y+h/2.;
    
    // create cut 
    double x1 = cx+rx*Math.cos(angle+Math.PI/2.);
    double y1 = cy+ry*Math.sin(angle+Math.PI/2.);
    double x2 = cx+rx*Math.cos(angle-Math.PI/2.);
    double y2 = cy+ry*Math.sin(angle-Math.PI/2.);
    double x3 = x2+rx*Math.cos(angle);
    double y3 = y2+ry*Math.sin(angle);
    
    Polygon p = new Polygon();
    p.addPoint((int)x1,(int)y1);
    p.addPoint((int)x2,(int)y2);
    p.addPoint((int)x3,(int)y3);
    p.addPoint((int)x2,(int)y2);
    f.append(p,false);

    if( has_oxygen ) {
        // create oxygen
        double ox = cx+rx*Math.cos(angle);
        double oy = cy+ry*Math.sin(angle);
        Shape o = new Ellipse2D.Double(ox-rx/3.,oy-ry/3.,rx/1.5,ry/1.5);
        f.append(o,false);    
    }

    return f;
    }

    static private Shape createCrossRingCleavage(double angle, double x, double y, double w, double h, int first_pos, int last_pos) {
    
    //return createArc(x,y,w,h,first_pos,last_pos);

    GeneralPath c = new GeneralPath();
    //c.append(createLine(0,x,y,w,h),false);
    
    // add hexagon
    c.append(createHexagon(x+1,y+1,w-2,h-2),false);
    //return c;       

    // add line    
    double rx = w/2.;
    double ry = h/2.;
    double cx = x+w/2.;
    double cy = y+h/2.;
    
    Polygon p1 = new Polygon();
    p1.addPoint((int)cx,(int)cy);
    p1.addPoint((int)(cx+1.2*rx*Math.cos(angle+first_pos*Math.PI/3-Math.PI/6)),
            (int)(cy+1.2*ry*Math.sin(angle+first_pos*Math.PI/3-Math.PI/6)));
    c.append(p1,false);

    Polygon p2 = new Polygon();
    p2.addPoint((int)cx,(int)cy);
    p2.addPoint((int)(cx+1.2*rx*Math.cos(angle+last_pos*Math.PI/3-Math.PI/6)),
            (int)(cy+1.2*ry*Math.sin(angle+last_pos*Math.PI/3-Math.PI/6)));
    c.append(p2,false);
    return c;

    /*
    double rx = w/2.;
    double ry = h/2.;
    double cx = x+w/2.;
    double cy = y+h/2.;
    
    // add half hexagon

    double step = Math.PI/3.;        
    Polygon p = new Polygon();
    p.addPoint((int)(cx+0.866*rx*Math.cos(angle-Math.PI/2)),(int)(cy+0.866*ry*Math.sin(angle-Math.PI/2)));
    p.addPoint((int)(cx+rx*Math.cos(angle-Math.PI/3)),(int)(cy+ry*Math.sin(angle-Math.PI/3)));
    p.addPoint((int)(cx+rx*Math.cos(angle)),(int)(cy+ry*Math.sin(angle)));
    p.addPoint((int)(cx+rx*Math.cos(angle+Math.PI/3)),(int)(cy+ry*Math.sin(angle+Math.PI/3)));
    p.addPoint((int)(cx+0.866*rx*rx*Math.cos(angle+Math.PI/2)),(int)(cy+0.866*ry*Math.sin(angle+Math.PI/2)));
    c.append(p,false);
    */

    // add pos
    /*
    AffineTransform t = new AffineTransform();
    int fs = (int)(h/3);

    double tx1 = cx + rx*Math.cos(angle+Math.PI/2) + fs*Math.cos(angle+Math.PI);
    double ty1 = cy + ry*Math.sin(angle+Math.PI/2) + fs*Math.sin(angle+Math.PI);
    t.setToTranslation(tx1,ty1);
    c.append(t.createTransformedShape(getTextShape("" + first_pos, theGraphicOptions.LINKAGE_INFO_FONT_FACE, fs)),false);

    double tx2 = cx + rx*Math.cos(angle-Math.PI/2) + fs*Math.cos(angle+Math.PI) + fs*Math.cos(angle+Math.PI/2);
    double ty2 = cy + ry*Math.sin(angle-Math.PI/2) + fs*Math.sin(angle+Math.PI) + fs*Math.sin(angle+Math.PI/2);
    t.setToTranslation(tx2,ty2);
    c.append(t.createTransformedShape(getTextShape("" + last_pos, theGraphicOptions.LINKAGE_INFO_FONT_FACE, fs)),false);
    */
    }

    static private Shape createEnd(double angle, double x, double y, double w, double h) {
    double rx = w/2.;
    double ry = h/2.;
    double cx = x+w/2.;
    double cy = y+h/2.;
    
    // start point
    double x1 = cx+rx*Math.cos(angle-Math.PI/2.);
    double y1 = cy+ry*Math.sin(angle-Math.PI/2.);

    // end point
    double x2 = cx+rx*Math.cos(angle+Math.PI/2.);
    double y2 = cy+ry*Math.sin(angle+Math.PI/2.);

    // ctrl point 1
    double cx1 = cx+0.5*rx*Math.cos(angle-Math.PI/2.);
    double cy1 = cy+0.5*ry*Math.sin(angle-Math.PI/2.);
    double tx1 = cx1+0.5*rx*Math.cos(angle-Math.PI);
    double ty1 = cy1+0.5*ry*Math.sin(angle-Math.PI);

    // ctrl point 2
    double cx2 = cx+0.5*rx*Math.cos(angle+Math.PI/2.);
    double cy2 = cy+0.5*ry*Math.sin(angle+Math.PI/2.);
    double tx2 = cx2+0.5*rx*Math.cos(angle);
    double ty2 = cy2+0.5*ry*Math.sin(angle);    
    
    /*Polygon p = new Polygon();
    p.addPoint((int)x1,(int)y1);
    p.addPoint((int)x2,(int)y2);
    return p;*/
    return new CubicCurve2D.Double(x1,y1,tx1,ty1,tx2,ty2,x2,y2);
    }

    
    static private Shape createBracket(double angle, double x, double y, double w, double h) {    
    
    double rx = w/2.;
    double ry = h/2.;
    double cx = x+w/2.;
    double cy = y+h/2.;

    // first start point
    double x11 = cx+rx*Math.cos(angle-Math.PI/2.)+0.2*rx*Math.cos(angle);
    double y11 = cy+ry*Math.sin(angle-Math.PI/2.)+0.2*ry*Math.sin(angle);

    // first ctrl point 1
    double tx11 = cx+0.9*rx*Math.cos(angle-Math.PI/2.)+0.2*rx*Math.cos(angle-Math.PI);
    double ty11 = cy+0.9*ry*Math.sin(angle-Math.PI/2.)+0.2*ry*Math.sin(angle-Math.PI);

    // first ctrl point 2;
    double tx21 = cx+0.1*rx*Math.cos(angle-Math.PI/2.)+0.2*rx*Math.cos(angle);
    double ty21 = cy+0.1*ry*Math.sin(angle-Math.PI/2.)+0.2*ry*Math.sin(angle);

    // first end point
    double x21 = cx+0.2*rx*Math.cos(angle-Math.PI);
    double y21 = cy+0.2*ry*Math.sin(angle-Math.PI);

    // first shape
    Shape s1 = new CubicCurve2D.Double(x11,y11,tx11,ty11,tx21,ty21,x21,y21);

    // second start point
    double x12 = cx+rx*Math.cos(angle+Math.PI/2.)+0.2*rx*Math.cos(angle);
    double y12 = cy+ry*Math.sin(angle+Math.PI/2.)+0.2*ry*Math.sin(angle);

    // second ctrl point 1
    double tx12 = cx+0.9*rx*Math.cos(angle+Math.PI/2.)+0.2*rx*Math.cos(angle-Math.PI);
    double ty12 = cy+0.9*ry*Math.sin(angle+Math.PI/2.)+0.2*ry*Math.sin(angle-Math.PI);

    // second ctrl point 2;
    double tx22 = cx+0.1*rx*Math.cos(angle+Math.PI/2.)+0.2*rx*Math.cos(angle);
    double ty22 = cy+0.1*ry*Math.sin(angle+Math.PI/2.)+0.2*ry*Math.sin(angle);

    // second end point
    double x22 = cx+0.2*rx*Math.cos(angle-Math.PI);
    double y22 = cy+0.2*ry*Math.sin(angle-Math.PI);

    // second shape
    Shape s2 = new CubicCurve2D.Double(x12,y12,tx12,ty12,tx22,ty22,x22,y22);

    // generate bracket
    GeneralPath b = new GeneralPath();
    b.append(s1,false);    
    b.append(s2,false);    
    return b;
    }

    private Shape createRepetition(double angle, double x, double y, double w, double h) {    
    
    double r = Math.min(w,h);
    double cx = x+w/2.;
    double cy = y+h/2.;
    
    //-----
    // create shape
    Polygon p = new Polygon();

    // first point
    double x1 = cx+r*Math.cos(angle-Math.PI/2.)+r/4.*Math.cos(angle+Math.PI);
    double y1 = cy+r*Math.sin(angle-Math.PI/2.)+r/4.*Math.sin(angle+Math.PI);
    p.addPoint((int)x1,(int)y1);

    // second point
    double x2 = cx+r*Math.cos(angle-Math.PI/2.);
    double y2 = cy+r*Math.sin(angle-Math.PI/2.);
    p.addPoint((int)x2,(int)y2);

    // third point
    double x3 = cx+r*Math.cos(angle+Math.PI/2.);
    double y3 = cy+r*Math.sin(angle+Math.PI/2.);
    p.addPoint((int)x3,(int)y3);

    // fourth point
    double x4 = cx+r*Math.cos(angle+Math.PI/2.)+r/4.*Math.cos(angle+Math.PI);
    double y4 = cy+r*Math.sin(angle+Math.PI/2.)+r/4.*Math.sin(angle+Math.PI);
    p.addPoint((int)x4,(int)y4);

    // close shape
    p.addPoint((int)x3,(int)y3);
    p.addPoint((int)x2,(int)y2);


    return p;
    }


    private Shape createShape(Residue node, Rectangle par_bbox, Rectangle cur_bbox, Rectangle sup_bbox, ResAngle orientation) {
    
    ResidueStyle style = theResidueStyleDictionary.getStyle(node);
    String shape = style.getShape();

    if( shape==null || shape.equals("none") || shape.equals("-") )
        return null;
    
    double x = (double)cur_bbox.getX();
    double y = (double)cur_bbox.getY();
    double w = (double)cur_bbox.getWidth();
    double h = (double)cur_bbox.getHeight();

    // non-oriented shapes
    if( shape.equals("point") )
        return new Rectangle2D.Double(x+w/2.,y+h/2.,0,0);    
    if( shape.equals("square") ) 
        return new Rectangle2D.Double(x,y,w,h);    
    if( shape.equals("circle") ) 
        return new Ellipse2D.Double(x,y,w,h);    
    if( shape.equals("diamond") ) 
        return createDiamond(x,y,w,h);            
    if( shape.equals("rhombus") ) 
        return createRhombus(x,y,w,h);            
    if( shape.equals("star") ) 
        return createStar(x,y,w,h,5);    
    if( shape.equals("sixstar") ) 
        return createStar(x,y,w,h,6);    
    if( shape.equals("sevenstar") ) 
        return createStar(x,y,w,h,7);    
    if( shape.equals("pentagon") ) 
        return createPentagon(x,y,w,h);            
    if( shape.equals("hexagon") ) 
        return createHexagon(x,y,w,h);    
    if( shape.equals("heptagon") ) 
        return createHeptagon(x,y,w,h);            

    Point pp = ( par_bbox!=null ) ?center(par_bbox) :center(cur_bbox);
    Point pc = center(cur_bbox);
    Point ps = ( sup_bbox!=null ) ?center(sup_bbox) :center(cur_bbox);

    // partially oriented shapes
    if( shape.equals("triangle") ) 
        return createTriangle(angle(pp,ps),x,y,w,h);    
    if( shape.equals("hatdiamond") ) 
        return createHatDiamond(angle(pp,ps),x,y,w,h);            
    if( shape.equals("rhatdiamond") ) 
        return createRHatDiamond(angle(pp,ps),x,y,w,h);            

    if( shape.equals("bracket") ) 
        return createBracket(orientation.opposite().getAngle(),x,y,w,h);
    if( shape.equals("startrep") ) 
        return createRepetition(orientation.opposite().getAngle(),x,y,w,h);
    if( shape.equals("endrep") ) 
        return createRepetition(orientation.getAngle(),x,y,w,h);
    
     
    // totally oriented shapes
    if( shape.startsWith("acleavage") ) {
        Vector<String> tokens = TextUtils.tokenize(shape,"_");
        int first_pos = Integer.parseInt(tokens.elementAt(1));
        int last_pos  = Integer.parseInt(tokens.elementAt(2));
        return createCrossRingCleavage(angle(pc,ps),x,y,w,h,first_pos,last_pos);
    }
    if( shape.equals("bcleavage") ) 
        return createCleavage(angle(ps,pc),x,y,w,h,false);
    if( shape.equals("ccleavage") ) 
        return createCleavage(angle(ps,pc),x,y,w,h,true);

    if( shape.startsWith("xcleavage") ) {
        Vector<String> tokens = TextUtils.tokenize(shape,"_");
        int first_pos = Integer.parseInt(tokens.elementAt(1));
        int last_pos  = Integer.parseInt(tokens.elementAt(2));
        return createCrossRingCleavage(angle(pp,pc),x,y,w,h,first_pos,last_pos);
    }
    if( shape.equals("ycleavage") ) 
        return createCleavage(angle(pp,pc),x,y,w,h,true);
    if( shape.equals("zcleavage") ) 
        return createCleavage(angle(pp,pc),x,y,w,h,false);
    
    if( shape.equals("end") ) 
        return createEnd(angle(pp,ps),x,y,w,h);

    return cur_bbox;
    }


    private Shape createRepetitionText(double angle, double x, double y, double w, double h, int min, int max) {    
    
    double r = Math.min(w,h);
    double cx = x+w/2.;
    double cy = y+h/2.;

    double x2 = cx+r*Math.cos(angle-Math.PI/2.);
    double y2 = cy+r*Math.sin(angle-Math.PI/2.);
    double x3 = cx+r*Math.cos(angle+Math.PI/2.);
    double y3 = cy+r*Math.sin(angle+Math.PI/2.);


    GeneralPath ret = new GeneralPath();

    //--------
    // add min repetition
    if( min>=0 || max>=0 ) {
        String text = (min>=0) ?""+min :"0";
        Dimension tb = textBounds(text,theGraphicOptions.LINKAGE_INFO_FONT_FACE,theGraphicOptions.LINKAGE_INFO_SIZE);
        
        double dist = (isUp(angle) || isDown(angle)) ?tb.width/2+4 :tb.height/2+4;
        double xmin,ymin;
        if( isLeft(angle) || isUp(angle) ) {
        xmin = x2+dist*Math.cos(angle-Math.PI/2.)-tb.width/2.;
        ymin = y2+dist*Math.sin(angle-Math.PI/2.)+tb.height/2.;
        }
        else {
        xmin = x3+dist*Math.cos(angle+Math.PI/2.)-tb.width/2.;
        ymin = y3+dist*Math.sin(angle+Math.PI/2.)+tb.height/2.;
        }

        ret.append(getTextShape(xmin,ymin,text,theGraphicOptions.LINKAGE_INFO_FONT_FACE,theGraphicOptions.LINKAGE_INFO_SIZE),false);
    }


    //--------
    // add max repetition    
    if( min>=0 || max>=0 ) {
        String text = (max>=0) ?""+max :"+inf";
        Dimension tb = textBounds(text,theGraphicOptions.LINKAGE_INFO_FONT_FACE,theGraphicOptions.LINKAGE_INFO_SIZE);
        
        double dist = (isUp(angle) || isDown(angle)) ?tb.width/2+4 :tb.height/2+4;
        double xmax,ymax;
        if( isLeft(angle) || isUp(angle) ) {
        xmax = x3+dist*Math.cos(angle+Math.PI/2.)-tb.width/2.;
        ymax = y3+dist*Math.sin(angle+Math.PI/2.)+tb.height/2.;
        }
        else {
        xmax = x2+dist*Math.cos(angle-Math.PI/2.)-tb.width/2.;
        ymax = y2+dist*Math.sin(angle-Math.PI/2.)+tb.height/2.;
        }

        ret.append(getTextShape(xmax,ymax,text,theGraphicOptions.LINKAGE_INFO_FONT_FACE,theGraphicOptions.LINKAGE_INFO_SIZE),false);
    }
    
    return ret;
    }

    private Shape createTextShape(Residue node, Rectangle par_bbox, Rectangle cur_bbox, Rectangle sup_bbox, ResAngle orientation) {
    
    ResidueStyle style = theResidueStyleDictionary.getStyle(node);
    String shape = style.getShape();

    if( shape==null || shape.equals("none") || shape.equals("-") )
        return null;
    
    double x = (double)cur_bbox.getX();
    double y = (double)cur_bbox.getY();
    double w = (double)cur_bbox.getWidth();
    double h = (double)cur_bbox.getHeight();

    if( shape.equals("endrep") ) 
        return createRepetitionText(orientation.getAngle(),x,y,w,h,node.getMinRepetitions(),node.getMaxRepetitions());    

    return null;
    }

    //--------------
    // Fill

    static private Shape createTriangle(double x1, double y1, double x2, double y2, double x3, double y3) {
    Polygon p = new Polygon();
    p.addPoint((int)x1,(int)y1);
    p.addPoint((int)x2,(int)y2);
    p.addPoint((int)x3,(int)y3);
    return p;
    }

    static private Shape createCheckered(double x, double y, double w, double h) {
    GeneralPath c = new GeneralPath();
    c.append(new Rectangle2D.Double(x+w/2.,y,w/2.,h/2.),false);    
    c.append(new Rectangle2D.Double(x,y+h/2.,w/2.,h/2.),false);    
    return c;

    }

    static private Shape createArc(double x, double y, double w, double h, int start_pos, int end_pos) {    
    return new Arc2D.Double(x-0.5*w,y-0.5*h,2*w,2*h,-end_pos*60.+30.,-((start_pos-end_pos+6)%6)*60.,Arc2D.PIE);
    }


    private Shape createFillShape(Residue node, Rectangle cur_bbox) {    

    ResidueStyle style = theResidueStyleDictionary.getStyle(node);
    String fillstyle   = style.getFillStyle();

    double x = (double)cur_bbox.x;
    double y = (double)cur_bbox.y;
    double w = (double)cur_bbox.width;
    double h = (double)cur_bbox.height;

    if( fillstyle.equals("empty") )
        return null;
    if( fillstyle.equals("full") )
        return cur_bbox;
    
    if( fillstyle.equals("left") )
        return new Rectangle2D.Double(x,y,w/2.,h);
    if( fillstyle.equals("top") )
        return new Rectangle2D.Double(x,y,w,h/2.);
    if( fillstyle.equals("right") )
        return new Rectangle2D.Double(x+w/2.,y,w/2.,h);
    if( fillstyle.equals("bottom") )
        return new Rectangle2D.Double(x,y+h/2.,w,h/2.);

    if( fillstyle.equals("topleft") ) 
        return createTriangle(x,y,x+w,y,x,y+h);
    if( fillstyle.equals("topright") ) 
        return createTriangle(x,y,x+w,y,x+w,y+h);
    if( fillstyle.equals("bottomright") ) 
        return createTriangle(x+w,y,x+w,y+h,x,y+h);
    if( fillstyle.equals("bottomleft") ) 
        return createTriangle(x,y,x+w,y+h,x,y+h);

    double cx = x+w/2.;
    double cy = y+h/2.;
    double rx = w/6.;
    double ry = h/6.;
    if( fillstyle.equals("circle") )
        return new Ellipse2D.Double(cx-rx,cy-ry,2.*rx,2.*ry);
    if( fillstyle.equals("checkered") )
        return createCheckered(x,y,w,h);
    if( fillstyle.startsWith("arc") ) {
        Vector<String> tokens = TextUtils.tokenize(fillstyle,"_");
        int first_pos = Integer.parseInt(tokens.elementAt(1));
        int last_pos  = Integer.parseInt(tokens.elementAt(2));
        return createArc(x,y,w,h,first_pos,last_pos);
    }
        

    return null;
    }

}