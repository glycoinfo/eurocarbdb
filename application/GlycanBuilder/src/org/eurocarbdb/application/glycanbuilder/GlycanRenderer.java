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

import java.util.*;
import java.text.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import javax.swing.*;
import org.w3c.dom.*;

import static org.eurocarbdb.application.glycanbuilder.Geometry.*;

/**
   Objects of this class are used to create a graphical representation
   of a {@link Glycan} object given the current graphic options
   ({@link GraphicOptions}). The rules to draw the structures in the
   different notations are stored in the style dictionaries: {@link
   ResidueStyleDictionary}, {@link LinkageStyleDictionary} and {@link
   ResiduePlacementDictionary}. The classes {@link ResidueRenderer}
   and {@link LinkageRenderer} are used to draw the different parts of
   the structure. The graphical representation is created in three
   steps: first the position of each residue around the parent is
   computed using the rules on residue placements from the {@link
   ResiduePlacementDictionary} and stored in the {@link
   PositionManager}; second the bounding box of each residue in the
   structure is computed from its position and the parent's bounding
   box, and the values are stored in a {@link BBoxManager}; third the
   residues are drawn inside their bounding boxes and the linkages are
   drawn by connecting the centers of the bounding boxes. The output
   can be directed to a {@link Graphics2D} object or to an image.
   
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class GlycanRenderer {    
       
    protected ResidueRenderer theResidueRenderer;
    protected LinkageRenderer theLinkageRenderer;

    // style
    protected ResiduePlacementDictionary  theResiduePlacementDictionary; 
    protected ResidueStyleDictionary      theResidueStyleDictionary; 
    protected LinkageStyleDictionary      theLinkageStyleDictionary; 

    protected GraphicOptions theGraphicOptions;

    /**
       Empty constructor.
     */
    public GlycanRenderer() {
    theResiduePlacementDictionary = new ResiduePlacementDictionary();
    theResidueStyleDictionary = new ResidueStyleDictionary();
    theLinkageStyleDictionary = new LinkageStyleDictionary();

    theResidueRenderer = new ResidueRenderer(this);
    theLinkageRenderer = new LinkageRenderer(this);
    }

    /**
       Copy constructor. All dictionaries and options are copied from
       the <code>src</code> object.
    */
    public GlycanRenderer(GlycanRenderer src) {
    
    if( src==null ) {
        theResiduePlacementDictionary = new ResiduePlacementDictionary();
        theResidueStyleDictionary = new ResidueStyleDictionary();
        theLinkageStyleDictionary = new LinkageStyleDictionary();
        
        theGraphicOptions = new GraphicOptions();
    }
    else {
        theResiduePlacementDictionary = src.theResiduePlacementDictionary;
        theResidueStyleDictionary = src.theResidueStyleDictionary;
        theLinkageStyleDictionary = src.theLinkageStyleDictionary;
        
        theGraphicOptions = src.theGraphicOptions.clone();
    }

    theResidueRenderer = new ResidueRenderer(this);
    theLinkageRenderer = new LinkageRenderer(this);
    }

    
    // ---

    /**
       Return the residue renderer used by this object.
     */
    public ResidueRenderer getResidueRenderer() {
    return theResidueRenderer;
    }

    /**
       Set the residue renderer used by this object.
     */
    public void setResidueRenderer(ResidueRenderer r) {
    theResidueRenderer = r;
    }

    /**
       Return the linkage renderer used by this object.
     */
    public LinkageRenderer getLinkageRenderer() {
    return theLinkageRenderer;
    }

    /**
       Set the linkage renderer used by this object.
     */
    public void setLinkageRenderer(LinkageRenderer r) {
    theLinkageRenderer = r;
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
    theResidueRenderer.setGraphicOptions(theGraphicOptions);
    theLinkageRenderer.setGraphicOptions(theGraphicOptions);
    }   
    
    
    /**
       Return the residue placement dictionary used by this object.
     */
    public ResiduePlacementDictionary getResiduePlacementDictionary() {
    return theResiduePlacementDictionary; 
    }

    /**
       Set the residue placement dictionary used by this object.
     */
    public void setResiduePlacementDictionary(ResiduePlacementDictionary residuePlacementDictionary) {
    theResiduePlacementDictionary = residuePlacementDictionary;
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
    theResidueRenderer.setResidueStyleDictionary(theResidueStyleDictionary);
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
    theLinkageRenderer.setLinkageStyleDictionary(theLinkageStyleDictionary);
    }

    //----------------- 
    // Painting    
       
    /**
       Draw a glycan structure on a graphics context using the
       calculated bounding boxes.
       @param g2d the graphic context
       @param structure the glycan structure to be drawn
       @param selected_residues the set of residues that must be shown
       as selected
       @param selected_linkages the set of linkages that must be shown
       as selected
       @param show_mass <code>true</code> if the mass information
       about the structure should be displayed
       @param show_redend <code>true</code> if the reducing end marker
       should be displayed
       @param posManager the object used to spatially arrange the
       residues
       @param bboxManager the object used to store the residue
       bounding boxes
     */
    public void paint(Graphics2D g2d, Glycan structure, HashSet<Residue> selected_residues, HashSet<Linkage> selected_linkages, boolean show_mass, boolean show_redend, PositionManager posManager, BBoxManager bboxManager) {    
    paint(g2d,structure,selected_residues,selected_linkages,null,show_mass,show_redend,posManager,bboxManager);
    }
    
    /**
       Draw a glycan structure on a graphics context using the
       calculated bounding boxes.
       @param g2d the graphic context
       @param structure the glycan structure to be drawn
       @param selected_residues the set of residues that must be shown
       as selected
       @param selected_linkages the set of linkages that must be shown
       as selected
       @param active_residues the set of residues that are active, all
       the others will be displayed with less bright colors
       @param show_mass <code>true</code> if the mass information
       about the structure should be displayed
       @param show_redend <code>true</code> if the reducing end marker
       should be displayed
       @param posManager the object used to spatially arrange the
       residues
       @param bboxManager the object used to store the residue
       bounding boxes
     */
    public void paint(Graphics2D g2d, Glycan structure, HashSet<Residue> selected_residues, HashSet<Linkage> selected_linkages, Collection<Residue> active_residues, boolean show_mass, boolean show_redend, PositionManager posManager, BBoxManager bboxManager) {    
    	
    if( structure==null || structure.isEmpty() )
        return;

    selected_residues = (selected_residues!=null) ?selected_residues :new HashSet<Residue>();
    selected_linkages = (selected_linkages!=null) ?selected_linkages :new HashSet<Linkage>();

    if( structure.isComposition() ) 
        paintComposition(g2d,structure.getRoot(),structure.getBracket(),selected_residues,posManager,bboxManager);    
    else {
        paintResidue(g2d,structure.getRoot(show_redend),selected_residues,selected_linkages,active_residues,posManager,bboxManager);
        paintBracket(g2d,structure.getBracket(),selected_residues,selected_linkages,active_residues,posManager,bboxManager);
    }
    if( show_mass )
        displayMass(g2d,structure,show_redend,bboxManager);
    }

    protected void displayMass(Graphics2D g2d, Glycan structure, boolean show_redend, BBoxManager bboxManager) {    
    Rectangle structure_all_bbox = bboxManager.getComplete(structure.getRoot(show_redend)); 
    
    g2d.setColor(Color.black);    
    g2d.setFont(new Font(theGraphicOptions.MASS_TEXT_FONT_FACE,Font.PLAIN,theGraphicOptions.MASS_TEXT_SIZE));
        
    String text = getMassText(structure);
    g2d.drawString(text, Geometry.left(structure_all_bbox), Geometry.bottom(structure_all_bbox)+theGraphicOptions.MASS_TEXT_SPACE+theGraphicOptions.MASS_TEXT_SIZE);    
        
    } 

    private String getMassText(Glycan structure) {
    StringBuilder sb = new StringBuilder();
    DecimalFormat df = new DecimalFormat("0.0000");
    double mz = structure.computeMZ();

    sb.append("m/z: ");
    if( mz<0. )
        sb.append("???");
    else
        sb.append(df.format(mz));
    sb.append(" [");
    sb.append(structure.getMassOptions().toString());
    sb.append("]");

    return sb.toString();
    }
    

    private void paintComposition(Graphics2D g2d, Residue root, Residue bracket, HashSet<Residue> selected_residues, PositionManager posManager, BBoxManager bboxManager) {
    ResAngle orientation = posManager.getOrientation(root);

    String text = makeCompositionText(root,bracket,orientation,true);
    Rectangle text_rect = bboxManager.getCurrent(bracket);

    // draw selected contour 
    if( selected_residues.contains(bracket) ) {        
        float[] dashes = {5.f,5.f};
        g2d.setStroke(new BasicStroke(2.f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,1.f,dashes,0.f));
        g2d.setColor(Color.black);
        g2d.draw(text_rect);
        g2d.setStroke(new BasicStroke(1));
    }

    Font font = new Font(theGraphicOptions.COMPOSITION_FONT_FACE,Font.PLAIN,theGraphicOptions.COMPOSITION_FONT_SIZE);

    StyledTextCellRenderer stcr = new StyledTextCellRenderer(false);
    stcr.getRendererComponent(font,Color.black,Color.white,text);    
    BufferedImage img = SVGUtils.getImage(stcr,false);

    if( orientation.equals(0) || orientation.equals(180) )       
        g2d.drawImage(img,null,text_rect.x,text_rect.y);
    else {
        g2d.rotate(-Math.PI/2.0);
        g2d.drawImage(img,null,-text_rect.y-text_rect.height,text_rect.x);
        g2d.rotate(Math.PI/2.0);
    }
    }
    

    private void paintResidue(Graphics2D g2d, Residue node, HashSet<Residue> selected_residues, HashSet<Linkage> selected_linkages, Collection<Residue> active_residues, PositionManager posManager, BBoxManager bboxManager) {    
    if( node==null ) 
        return;    

    Rectangle parent_bbox  = bboxManager.getParent(node);
    Rectangle node_bbox    = bboxManager.getCurrent(node);
    Rectangle border_bbox  = bboxManager.getBorder(node);
    Rectangle support_bbox = bboxManager.getSupport(node);

    if( node_bbox==null ) // not shown
        return;    
    
    // paint edges
    for(Linkage link : node.getChildrenLinkages() ) {
    
        Residue child = link.getChildResidue();        
        Rectangle child_bbox = bboxManager.getCurrent(child);
        Rectangle child_border_bbox = bboxManager.getBorder(child);

        if( child_bbox!=null && !posManager.isOnBorder(child) ) {
        boolean selected = (selected_residues.contains(node) && selected_residues.contains(child)) || selected_linkages.contains(link);
        boolean active = (active_residues==null || (active_residues.contains(node) && active_residues.contains(child)));
        theLinkageRenderer.paintEdge(g2d,link,selected,node_bbox,border_bbox,child_bbox,child_border_bbox);                
        }
    }
    
    // paint node
    boolean selected = selected_residues.contains(node);
    boolean active = (active_residues==null || active_residues.contains(node));
    theResidueRenderer.paint(g2d,node,selected,active,posManager.isOnBorder(node),parent_bbox,node_bbox,support_bbox,posManager.getOrientation(node));
    
    // paint children
    for(Linkage link : node.getChildrenLinkages() ) 
        paintResidue(g2d,link.getChildResidue(),selected_residues,selected_linkages,active_residues,posManager,bboxManager);

    // paint info
    for(Linkage link : node.getChildrenLinkages() ) {
    
        Residue child = link.getChildResidue();        
        Rectangle child_bbox = bboxManager.getCurrent(child);
        Rectangle child_border_bbox = bboxManager.getBorder(child);

        if( child_bbox!=null && !posManager.isOnBorder(child) ) 
        theLinkageRenderer.paintInfo(g2d,link,node_bbox,border_bbox,child_bbox,child_border_bbox);                        
    }
    
    }

    private void paintBracket(Graphics2D g2d, Residue bracket, HashSet<Residue> selected_residues, HashSet<Linkage> selected_linkages, Collection<Residue> active_residues, PositionManager posManager, BBoxManager bboxManager) {    
    if( bracket==null )
        return;
    
    Rectangle parent_bbox  = bboxManager.getParent(bracket);
    Rectangle bracket_bbox = bboxManager.getCurrent(bracket);
    Rectangle support_bbox = bboxManager.getSupport(bracket);

    // paint bracket
    boolean selected = selected_residues.contains(bracket);
    boolean active = (active_residues==null || active_residues.contains(bracket));
    theResidueRenderer.paint(g2d,bracket,selected,active,false,parent_bbox,bracket_bbox,support_bbox,posManager.getOrientation(bracket));

    // paint antennae
    for( Linkage link : bracket.getChildrenLinkages() ) {
        Residue child = link.getChildResidue();
        int quantity  = bboxManager.getLinkedResidues(child).size()+1;

        Rectangle node_bbox         = bboxManager.getParent(child);
        Rectangle child_bbox        = bboxManager.getCurrent(child);
        Rectangle child_border_bbox = bboxManager.getBorder(child);
        
        if( child_bbox!=null ) {        
        // paint edge
        if( !posManager.isOnBorder(child) ) {
            selected = (selected_residues.contains(bracket) && selected_residues.contains(child)) || selected_linkages.contains(link);
            active = (active_residues==null || (active_residues.contains(bracket) && active_residues.contains(child)));    
            theLinkageRenderer.paintEdge(g2d,link,selected,node_bbox,node_bbox,child_bbox,child_border_bbox);
        }
        
        // paint child
        paintResidue(g2d,child,selected_residues,selected_linkages,active_residues,posManager,bboxManager);    

        // paint info
        if( !posManager.isOnBorder(child) ) 
            theLinkageRenderer.paintInfo(g2d,link,node_bbox,node_bbox,child_bbox,child_border_bbox);

        // paint quantity
        if( quantity>1 ) 
            paintQuantity(g2d,child,quantity,bboxManager);        
        }                
    }
    }

    
    protected void paintQuantity(Graphics2D g2d, Residue antenna, int quantity, BBoxManager bboxManager) {    
    ResAngle orientation = theGraphicOptions.getOrientationAngle();

    // get dimensions
    String text;
    if( orientation.equals(0) || orientation.equals(-90))  
        text = "x" + quantity;
    else
        text = quantity + "x";

    Dimension text_dim = textBounds(text,theGraphicOptions.NODE_FONT_FACE,theGraphicOptions.NODE_FONT_SIZE);
    
    // retrieve bounding box
    Rectangle text_rect = null;
    Rectangle antenna_bbox = bboxManager.getComplete(antenna);

    if( orientation.equals(0) )     
        text_rect = new Rectangle(right(antenna_bbox)+3,midy(antenna_bbox)-1-text_dim.height/2,text_dim.width,text_dim.height); // left to right
    else if( orientation.equals(180) )       
        text_rect = new Rectangle(left(antenna_bbox)-3-text_dim.width,midy(antenna_bbox)-1-text_dim.height/2,text_dim.width,text_dim.height); // right to left
    else if( orientation.equals(90) )       
        text_rect = new Rectangle(midx(antenna_bbox)-text_dim.height/2,bottom(antenna_bbox)+3,text_dim.height,text_dim.width); // top to bottom
    else 
        text_rect = new Rectangle(midx(antenna_bbox)-text_dim.height/2,top(antenna_bbox)-3-text_dim.width,text_dim.height,text_dim.width); // bottom to top

    // paint text
    g2d.setColor(Color.black);    
    g2d.setFont(new Font(theGraphicOptions.NODE_FONT_FACE,Font.PLAIN,theGraphicOptions.NODE_FONT_SIZE));    

    if( orientation.equals(0) || orientation.equals(180) )        
        g2d.drawString(text,left(text_rect),bottom(text_rect));
    else {
        g2d.rotate(-Math.PI/2.0); 
        g2d.drawString(text,-bottom(text_rect),right(text_rect));
        //g2d.drawString(text,-(int)(text_rect.y+text_rect.height),(int)(text_rect.x+text_rect.width));
        g2d.rotate(+Math.PI/2.0); 

    }

    }

    //-----------------
    // Positioning 


    /**
       Add the margins to a structure bounding box.
     */
    public Dimension computeSize(Rectangle all_bbox) { 
    if( all_bbox==null || all_bbox.width==0 || all_bbox.height==0 ) 
        return new Dimension(1,1);
        //return new Dimension(theGraphicOptions.MARGIN_LEFT+theGraphicOptions.MARGIN_RIGHT,theGraphicOptions.MARGIN_TOP+theGraphicOptions.MARGIN_BOTTOM);    
    return new Dimension(theGraphicOptions.MARGIN_LEFT+all_bbox.width+theGraphicOptions.MARGIN_RIGHT,theGraphicOptions.MARGIN_TOP+all_bbox.height+theGraphicOptions.MARGIN_BOTTOM);
    }

 
    /**
       Compute the residue bounding boxes for a set of structures.
       @param structures the list of structures to be displayed
       @param show_masses <code>true</code> if the mass information
       about the structure should be displayed
       @param show_redend <code>true</code> if the reducing end marker
       should be displayed
       @param posManager the object used to spatially arrange the
       residues
       @param bboxManager the object used to store the residue
       bounding boxes
     */
    public Rectangle computeBoundingBoxes(Collection<Glycan> structures, boolean show_masses, boolean show_redend, PositionManager posManager, BBoxManager bboxManager) {    
    return computeBoundingBoxes(structures,show_masses,show_redend,posManager,bboxManager,true);
    }

    /**
       Compute the residue bounding boxes for a set of structures.
       @param structures the list of structures to be displayed
       @param show_masses <code>true</code> if the mass information
       about the structure should be displayed
       @param show_redend <code>true</code> if the reducing end marker
       should be displayed
       @param posManager the object used to spatially arrange the
       residues
       @param bboxManager the object used to store the residue
       bounding boxes
       @param reset <code>true</code> if the bounding boxes manager
       should be re-initialized
     */
    public Rectangle computeBoundingBoxes(Collection<Glycan> structures, boolean show_masses, boolean show_redend, PositionManager posManager, BBoxManager bboxManager, boolean reset) {           

    if( reset ) {
        // init bboxes    
        posManager.reset();
        bboxManager.reset();
    }
    
    // compute bounding boxes;
    Rectangle all_bbox = new Rectangle(theGraphicOptions.MARGIN_TOP,theGraphicOptions.MARGIN_LEFT,0,0);
    int cur_top = theGraphicOptions.MARGIN_TOP;
    for( Iterator<Glycan> i=structures.iterator(); i.hasNext(); ) {
        // compute glycan bbox
        Rectangle glycan_bbox = computeBoundingBoxes(i.next(),theGraphicOptions.MARGIN_LEFT,cur_top,show_masses,show_redend,posManager,bboxManager);

        all_bbox = Geometry.union(all_bbox,glycan_bbox);                
        cur_top = Geometry.bottom(all_bbox) + theGraphicOptions.STRUCTURES_SPACE;
    }    
    
    return all_bbox;
    }        
 
    /**
       Compute the residue bounding boxes for a single structures.
       @param structure the structure to be displayed
       @param cur_left the left position where to display the structure
       @param cur_top the top position where to display the structure
       @param show_mass <code>true</code> if the mass information
       about the structure should be displayed
       @param show_redend <code>true</code> if the reducing end marker
       should be displayed
       @param posManager the object used to spatially arrange the
       residues
       @param bboxManager the object used to store the residue
       bounding boxes
    */
    public Rectangle computeBoundingBoxes(Glycan structure, int cur_left, int  cur_top, boolean show_mass, boolean show_redend, PositionManager posManager, BBoxManager bboxManager) {
    if( structure==null )
        return new Rectangle(cur_left,cur_top,0,0);    

    try {
        bboxManager.setGraphicOptions(theGraphicOptions);
      
        if( !structure.isEmpty() ) {

        Residue root,bracket;
        ResAngle orientation = theGraphicOptions.getOrientationAngle();

        if( structure.isComposition() ) {
            root = structure.getRoot();
            bracket = structure.getBracket();

            // assign positions
            assignPositionComposition(root,posManager);
            assignPositionComposition(bracket,posManager);

            // compute bounding boxes       
            computeBoundingBoxesComposition(root,bracket,posManager,bboxManager);    
        }
        else {
            root = structure.getRoot(show_redend);
            bracket = structure.getBracket();
    
             // assign positions
            posManager.add(root,new ResAngle(),orientation,false,true);       
            assignPosition(root,false,orientation,root,posManager);
            
            posManager.add(bracket,new ResAngle(),orientation,false,true);       
            assignPosition(bracket,false,orientation,bracket,posManager);

            // compute bounding boxes       
            computeBoundingBoxes(root,posManager,bboxManager);    
            computeBoundingBoxesBracket(bracket,root,theGraphicOptions.COLLAPSE_MULTIPLE_ANTENNAE,posManager,bboxManager);        
        }

        // add bracket bbox
        Rectangle bbox = union(bboxManager.getComplete(root),bboxManager.getComplete(bracket));
        bboxManager.setComplete(root,bbox);        

        // translate if necessary
        bboxManager.translate(cur_left-bbox.x,cur_top-bbox.y,root);
        bboxManager.translate(cur_left-bbox.x,cur_top-bbox.y,bracket);
        bbox.translate(cur_left-bbox.x,cur_top-bbox.y);
        
        // add masses
        if( show_mass ) {
            Dimension d = textBounds(getMassText(structure),theGraphicOptions.MASS_TEXT_FONT_FACE,theGraphicOptions.MASS_TEXT_SIZE);
            Rectangle text_bbox = new Rectangle(cur_left,bottom(bbox)+theGraphicOptions.MASS_TEXT_SPACE,d.width,d.height);
            bbox = union(bbox,text_bbox);
        }

        return bbox;
        }
    }
    catch(Exception e) {
        LogUtils.report(e);
    }    
    return new Rectangle(cur_left,cur_top,0,0);    
    }      

    /**
       Compute the positions of each residue and store them in the
       position manager.
     */

    public void assignPositions(Glycan structure, PositionManager posManager) {
    if( structure==null )
        return;    

    try {
        ResAngle orientation = theGraphicOptions.getOrientationAngle();
        Residue root = structure.getRoot(true);
        Residue bracket = structure.getBracket();
        
        posManager.add(root,new ResAngle(),orientation,false,true);       
        assignPosition(root,false,orientation,root,posManager);
        
        posManager.add(bracket,new ResAngle(),orientation,false,true); 
        assignPosition(bracket,false,orientation,bracket,posManager);
    }
    catch(Exception e) {
        LogUtils.report(e);
    }    
    }

    private void assignPositionComposition(Residue current, PositionManager posManager) throws Exception {
    if( current==null )
        return;

    posManager.add(current,theGraphicOptions.getOrientationAngle(),new ResAngle(),false,false);       
    for( Linkage l : current.getChildrenLinkages() )
        assignPositionComposition(l.getChildResidue(),posManager);
    }

    private void assignPosition(Residue current, boolean sticky, ResAngle orientation, Residue turning_point, PositionManager posManager) throws Exception {
    if( current==null )
        return;        

    // init positions
    BookingManager bookManager = new BookingManager(posManager.getAvailablePositions(current,orientation));

    // add children to the booking manager
    for(Iterator<Linkage> i=current.iterator(); i.hasNext(); ) {
        Linkage link = i.next();
        Residue child = link.getChildResidue();        
        Residue matching_child = (child.getCleavedResidue()!=null) ?child.getCleavedResidue() :child;
        
        // get placement
        ResiduePlacement placement = matching_child.getPreferredPlacement();
        if( placement==null || (!current.isSaccharide() && !current.isBracket()) || !bookManager.isAvailable(placement) ) 
        placement = theResiduePlacementDictionary.getPlacement(current,link,matching_child,sticky);

        // set placement
        bookManager.add(child,placement);
    }
    
    // place children
    bookManager.place();
    
    // store positions
    for(Iterator<Linkage> i=current.iterator(); i.hasNext(); ) {
        Residue child = i.next().getChildResidue();
        
        ResiduePlacement child_placement = bookManager.getPlacement(child);        
        ResAngle  child_pos = bookManager.getPosition(child);
        posManager.add(child,orientation,child_pos,child_placement.isOnBorder(),child_placement.isSticky());       
        
        ResAngle child_orientation = posManager.getOrientation(child);
        Residue child_turning_point = (child_orientation.equals(orientation)) ?turning_point :child;
        assignPosition(child,child_placement.isSticky(),child_orientation,child_turning_point,posManager);
    }
    }      

    //----------------
    // Bounding boxe

    /**
       Return a string representation of the composition of a glycan
       structure
       @param styled <code>true</code> if the returned text is
       displayed in a {@link StyledTextCellRenderer}
     */
    public String makeCompositionText(Glycan g, boolean styled) {
    return makeCompositionText(g,theGraphicOptions.getOrientationAngle(theGraphicOptions.RL),styled);
    }

    /**
       Return a string representation of the composition of a glycan
       structure
    */
    static public String makeCompositionTextPlain(Glycan g) {
    if( !g.isComposition() )
        g = g.getComposition();
    return makeCompositionText(g.getRoot(),g.getBracket(),new ResAngle(0),false);
    }
    
    /**
       Return a string representation of the composition of a glycan
       structure
       @param orientation the orientation at which the text will be
       displayed
       @param styled <code>true</code> if the returned text is
       displayed in a {@link StyledTextCellRenderer}
     */
    static public String makeCompositionText(Glycan g, ResAngle orientation, boolean styled) {
    if( !g.isComposition() )
        g = g.getComposition();
    return makeCompositionText(g.getRoot(),g.getBracket(),orientation,styled);
    }

    static private String makeCompositionText(Residue root, Residue bracket, ResAngle orientation, boolean styled) {
    
    Vector<String> cleavages = new Vector<String>();
    TreeMap<String,Integer> residues = new TreeMap<String,Integer>();

    // get components
    for( Linkage l : bracket.getChildrenLinkages() ) {
        Residue r = l.getChildResidue();
        if( r.isCleavage() ) {
        if( !r.getType().isLCleavage() ) 
            cleavages.add( r.getCleavageType() );
        }
        else {
        String type = r.getResidueName();
        Integer old_num = residues.get(type);
        if( old_num==null )
            residues.put(type,1);
        else 
            residues.put(type,old_num+1);    
        }
    }

    // build name
    StringBuilder text = new StringBuilder();

    // write left/top part
    if( orientation.equals(180) || orientation.equals(90) ) {
        if( cleavages.size()>0 ) {
        for( String s : cleavages )
            text.append(s);
        text.append('-');
        }
    }
    else {
        if( !root.getTypeName().equals("freeEnd") ) {
        if( root.isCleavage() )
            text.append(root.getCleavageType());
        else 
            text.append(root.getResidueName());
        text.append('-');
        }
    }

    // write middle
    for( Map.Entry<String,Integer> e : residues.entrySet() ) {
        text.append(e.getKey());
        
        if( styled ) {
        text.append("_{");
        text.append(e.getValue());
        text.append('}');
        }
        else
        text.append(e.getValue());
    }
    
    // write right/bottom part
    if( orientation.equals(180) || orientation.equals(90) ) {
        if( !root.getTypeName().equals("freeEnd") ) {
        text.append('-');
        if( root.isCleavage() )
            text.append(root.getCleavageType());
        else 
            text.append(root.getResidueName());
        }
    }
    else {
        if( cleavages.size()>0 ) {
        text.append('-');
        for( String s : cleavages )
            text.append(s);
        }        
    }

    return text.toString();
    }

    private void computeBoundingBoxesComposition(Residue root, Residue bracket, PositionManager posManager, BBoxManager bboxManager) {
    ResAngle orientation = posManager.getOrientation(root);

    String text = makeCompositionText(root,bracket,orientation,true);
    Font font = new Font(theGraphicOptions.COMPOSITION_FONT_FACE,Font.PLAIN,theGraphicOptions.COMPOSITION_FONT_SIZE);

    StyledTextCellRenderer stcr = new StyledTextCellRenderer(false);
    stcr.getRendererComponent(font,Color.black,Color.white,text);
    
    Dimension d = stcr.getPreferredSize();
    if( orientation.equals(0) || orientation.equals(180) )       
        bboxManager.setAllBBoxes(bracket,new Rectangle(0,0,d.width,d.height));
    else
        bboxManager.setAllBBoxes(bracket,new Rectangle(0,0,d.height,d.width));

    bboxManager.linkSubtree(bracket,root);
    bboxManager.linkSubtree(bracket,bracket);    
    }

    private void computeBoundingBoxes(Residue node, PositionManager posManager, BBoxManager bboxManager) throws Exception {
    if( node==null )
        return;

    // compute all bboxes
    ResAngle orientation = posManager.getOrientation(node);
    if( orientation.equals(0) )       
        computeBoundingBoxesLR(node,posManager,bboxManager);
    else if( orientation.equals(180) )       
        computeBoundingBoxesRL(node,posManager,bboxManager);
    else if( orientation.equals(90) )       
        computeBoundingBoxesTB(node,posManager,bboxManager);
    else if( orientation.equals(-90) )       
        computeBoundingBoxesBT(node,posManager,bboxManager);
    else
        throw new Exception("Invalid orientation " + orientation + " at node " + node.id);
    }


    private void computeBoundingBoxesLR(Residue node, PositionManager posManager, BBoxManager bboxManager) throws Exception {
    if( node==null )
        return;

    Rectangle node_bbox = theResidueRenderer.computeBoundingBox(node,posManager.isOnBorder(node),0,0,posManager.getOrientation(node),theGraphicOptions.NODE_SIZE,Integer.MAX_VALUE);
    bboxManager.setAllBBoxes(node,node_bbox);
    
    //-----------------
    // place substituents

    Vector<Residue> region_m90b = posManager.getChildrenAtPosition(node,new ResAngle(-90),true);
    for( int i=0; i<region_m90b.size(); i++ ) {
        computeBoundingBoxes(region_m90b.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignBottomsOnRight(region_m90b.subList(0,i),region_m90b.subList(i,i+1),theGraphicOptions.NODE_SUB_SPACE);
    }

    Vector<Residue> region_p90b = posManager.getChildrenAtPosition(node,new ResAngle(90),true);
    for( int i=0; i<region_p90b.size(); i++ ) {
        computeBoundingBoxes(region_p90b.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignTopsOnLeft(region_p90b.subList(0,i),region_p90b.subList(i,i+1),theGraphicOptions.NODE_SUB_SPACE);
    }

    // ----------------
    // place positions

    // position -90 (top)
    Vector<Residue> region_m90 = posManager.getChildrenAtPosition(node,new ResAngle(-90),false);
    for( int i=0; i<region_m90.size(); i++ ) {
        computeBoundingBoxes(region_m90.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignBottomsOnRight(region_m90.subList(0,i),region_m90.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position +90 (bottom)
    Vector<Residue> region_p90 = posManager.getChildrenAtPosition(node,new ResAngle(90),false);
    for( int i=0; i<region_p90.size(); i++ ) {
        computeBoundingBoxes(region_p90.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignTopsOnLeft(region_p90.subList(0,i),region_p90.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position 0 (right)
    Vector<Residue> region_0 = posManager.getChildrenAtPosition(node,new ResAngle(0));
    for( int i=0; i<region_0.size(); i++ ) {
        computeBoundingBoxes(region_0.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignLeftsOnBottom(region_0.subList(0,i),region_0.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position -45 (top right)
    Vector<Residue> region_m45 = posManager.getChildrenAtPosition(node,new ResAngle(-45));
    for( int i=0; i<region_m45.size(); i++ ) {
        computeBoundingBoxes(region_m45.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignLeftsOnBottom(region_m45.subList(0,i),region_m45.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position 45 (bottom right)
    Vector<Residue> region_p45 = posManager.getChildrenAtPosition(node,new ResAngle(45));
    for( int i=0; i<region_p45.size(); i++ ) {
        computeBoundingBoxes(region_p45.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignLeftsOnBottom(region_p45.subList(0,i),region_p45.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // ----------------
    // align substituents with node
    bboxManager.alignCentersOnTop(node_bbox,region_m90b,theGraphicOptions.NODE_SUB_SPACE);
    bboxManager.alignCentersOnBottom(node_bbox,region_p90b,theGraphicOptions.NODE_SUB_SPACE);

    Union<Residue> border_nodes = new Union<Residue>(region_m90b).and(region_p90b);
    Rectangle border_bbox = bboxManager.getComplete(border_nodes.and(node));    

    // align position 0,45,-45 with node to not clash with 90b,-90b
    if( region_0.size()>0 ) {
        bboxManager.alignLeftsOnTop(region_0,region_m45,theGraphicOptions.NODE_SPACE);
        bboxManager.alignLeftsOnBottom(region_0,new Union<Residue>(region_0).and(region_m45),region_p45,region_p45,theGraphicOptions.NODE_SPACE);    
        bboxManager.alignCentersOnRight(node_bbox,border_nodes,region_0,new Union<Residue>(region_0).and(region_m45).and(region_p45),theGraphicOptions.NODE_SPACE);
    }
    else if( region_m45.size()==0 ) 
        bboxManager.alignCornersOnRightAtBottom(node_bbox,border_nodes,region_p45,theGraphicOptions.NODE_SPACE,theGraphicOptions.NODE_SPACE);
    else if( region_p45.size()==0 ) 
        bboxManager.alignCornersOnRightAtTop(node_bbox,border_nodes,region_m45,theGraphicOptions.NODE_SPACE,theGraphicOptions.NODE_SPACE);
    else 
        bboxManager.alignSymmetricOnRight(node_bbox,border_nodes,region_m45,region_p45,theGraphicOptions.NODE_SPACE,2*theGraphicOptions.NODE_SPACE+theGraphicOptions.NODE_SIZE);    

    // align positions -90 and 90 with node to not clash with the rest
    Union<Residue> ex_nodes = new Union<Residue>(region_0).and(region_m45).and(region_p45).and(border_nodes);
    bboxManager.alignCentersOnTop(node_bbox,ex_nodes,region_m90,region_m90,theGraphicOptions.NODE_SPACE);
    bboxManager.alignCentersOnBottom(node_bbox,ex_nodes,region_p90,region_p90,theGraphicOptions.NODE_SPACE);    

    // ----------------
    // set rectangles
    Union<Residue> all_nodes = ex_nodes.and(region_m90).and(region_p90).and(node);

    bboxManager.setCurrent(node,node_bbox);
    bboxManager.setBorder(node,border_bbox);
    bboxManager.setComplete(node,bboxManager.getComplete(all_nodes));
    if( node.hasChildren() ) 
        bboxManager.setSupport(node,new Rectangle(midx(node_bbox)+theGraphicOptions.NODE_SPACE+theGraphicOptions.NODE_SIZE,midy(node_bbox),0,0));    
    for(Iterator<Linkage> i=node.iterator(); i.hasNext(); ) 
        bboxManager.setParent(i.next().getChildResidue(),node_bbox);
    }

     private void computeBoundingBoxesRL(Residue node, PositionManager posManager, BBoxManager bboxManager) throws Exception {
    if( node==null )
        return;

    Rectangle node_bbox = theResidueRenderer.computeBoundingBox(node,posManager.isOnBorder(node),0,0,posManager.getOrientation(node),theGraphicOptions.NODE_SIZE,Integer.MAX_VALUE);
    bboxManager.setAllBBoxes(node,node_bbox);

    //-----------------
    // place substituents

    // -90 border (bottom)
    Vector<Residue> region_m90b = posManager.getChildrenAtPosition(node,new ResAngle(-90),true);
    for( int i=0; i<region_m90b.size(); i++ ) {
        computeBoundingBoxes(region_m90b.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignTopsOnLeft(region_m90b.subList(0,i),region_m90b.subList(i,i+1),theGraphicOptions.NODE_SUB_SPACE);
    }

    // +90 border (top)
    Vector<Residue> region_p90b = posManager.getChildrenAtPosition(node,new ResAngle(90),true);
    for( int i=0; i<region_p90b.size(); i++ ) {
        computeBoundingBoxes(region_p90b.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignBottomsOnRight(region_p90b.subList(0,i),region_p90b.subList(i,i+1),theGraphicOptions.NODE_SUB_SPACE);
    }

    // ----------------
    // place positions

    // position -90 (bottom)
    Vector<Residue> region_m90 = posManager.getChildrenAtPosition(node,new ResAngle(-90),false);
    for( int i=0; i<region_m90.size(); i++ ) {
        computeBoundingBoxes(region_m90.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignTopsOnLeft(region_m90.subList(0,i),region_m90.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position +90 (top)
    Vector<Residue> region_p90 = posManager.getChildrenAtPosition(node,new ResAngle(90),false);
    for( int i=0; i<region_p90.size(); i++ ) {
        computeBoundingBoxes(region_p90.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignBottomsOnRight(region_p90.subList(0,i),region_p90.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position 0 (left)
    Vector<Residue> region_0 = posManager.getChildrenAtPosition(node,new ResAngle(0),false);
    for( int i=0; i<region_0.size(); i++ ) {
        computeBoundingBoxes(region_0.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignRightsOnTop(region_0.subList(0,i),region_0.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position -45 (bottom left)
    Vector<Residue> region_m45 = posManager.getChildrenAtPosition(node,new ResAngle(-45),false);
    for( int i=0; i<region_m45.size(); i++ ) {
        computeBoundingBoxes(region_m45.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignRightsOnTop(region_m45.subList(0,i),region_m45.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position 45 (top left)
    Vector<Residue> region_p45 = posManager.getChildrenAtPosition(node,new ResAngle(45),false);
    for( int i=0; i<region_p45.size(); i++ ) {
        computeBoundingBoxes(region_p45.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignRightsOnTop(region_p45.subList(0,i),region_p45.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // ----------------
    // align substituents with node
    bboxManager.alignCentersOnBottom(node_bbox,region_m90b,theGraphicOptions.NODE_SUB_SPACE);
    bboxManager.alignCentersOnTop(node_bbox,region_p90b,theGraphicOptions.NODE_SUB_SPACE);

    Union<Residue> border_nodes = new Union<Residue>(region_m90b).and(region_p90b);
    Rectangle border_bbox = bboxManager.getComplete(border_nodes.and(node));    

    // align position 0,45,-45 with node to not clash with 90b,-90b
    if( region_0.size()>0 ) {
        bboxManager.alignRightsOnBottom(region_0,region_m45,theGraphicOptions.NODE_SPACE);
        bboxManager.alignRightsOnTop(region_0,new Union<Residue>(region_0).and(region_m45),region_p45,region_p45,theGraphicOptions.NODE_SPACE);    
        bboxManager.alignCentersOnLeft(node_bbox,border_nodes,region_0,new Union<Residue>(region_0).and(region_m45).and(region_p45),theGraphicOptions.NODE_SPACE);    
    }
    else if( region_m45.size()==0 ) 
        bboxManager.alignCornersOnLeftAtTop(node_bbox,border_nodes,region_p45,theGraphicOptions.NODE_SPACE,theGraphicOptions.NODE_SPACE);
    else if( region_p45.size()==0 ) 
        bboxManager.alignCornersOnLeftAtBottom(node_bbox,border_nodes,region_m45,theGraphicOptions.NODE_SPACE,theGraphicOptions.NODE_SPACE);
    else 
        bboxManager.alignSymmetricOnLeft(node_bbox,border_nodes,region_p45,region_m45,theGraphicOptions.NODE_SPACE,2*theGraphicOptions.NODE_SPACE+theGraphicOptions.NODE_SIZE);    

    // align positions -90 and 90 with node to not clash with the rest
    Union<Residue> ex_nodes = new Union<Residue>(region_0).and(region_m45).and(region_p45).and(border_nodes);
    bboxManager.alignCentersOnBottom(node_bbox,ex_nodes,region_m90,region_m90,theGraphicOptions.NODE_SPACE);
    bboxManager.alignCentersOnTop(node_bbox,ex_nodes,region_p90,region_p90,theGraphicOptions.NODE_SPACE);

    // ----------------
    // set rectangles
    Union<Residue> all_nodes = ex_nodes.and(region_m90).and(region_p90).and(node);

    bboxManager.setCurrent(node,node_bbox);
    bboxManager.setBorder(node,border_bbox);
    bboxManager.setComplete(node,bboxManager.getComplete(all_nodes));
    if( node.hasChildren() ) 
        bboxManager.setSupport(node,new Rectangle(midx(node_bbox)-theGraphicOptions.NODE_SPACE-theGraphicOptions.NODE_SIZE,midy(node_bbox),0,0));    
    for(Iterator<Linkage> i=node.iterator(); i.hasNext(); ) 
        bboxManager.setParent(i.next().getChildResidue(),node_bbox);
    }

    private void computeBoundingBoxesTB(Residue node, PositionManager posManager, BBoxManager bboxManager) throws Exception {
    if( node==null )
        return;

    Rectangle node_bbox = theResidueRenderer.computeBoundingBox(node,posManager.isOnBorder(node),0,0,posManager.getOrientation(node),theGraphicOptions.NODE_SIZE,Integer.MAX_VALUE);
    bboxManager.setAllBBoxes(node,node_bbox);

    //-----------------
    // place substituents

    // -90 border (right)
    Vector<Residue> region_m90b = posManager.getChildrenAtPosition(node,new ResAngle(-90),true);
    for( int i=0; i<region_m90b.size(); i++ ) {
        computeBoundingBoxes(region_m90b.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignLeftsOnBottom(region_m90b.subList(0,i),region_m90b.subList(i,i+1),theGraphicOptions.NODE_SUB_SPACE);
    }

    // +90 border (left)
    Vector<Residue> region_p90b = posManager.getChildrenAtPosition(node,new ResAngle(90),true);
    for( int i=0; i<region_p90b.size(); i++ ) {
        computeBoundingBoxes(region_p90b.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignRightsOnTop(region_p90b.subList(0,i),region_p90b.subList(i,i+1),theGraphicOptions.NODE_SUB_SPACE);
    }
    // ----------------
    // place positions

    // position -90 (right)
    Vector<Residue> region_m90 = posManager.getChildrenAtPosition(node,new ResAngle(-90),false);
    for( int i=0; i<region_m90.size(); i++ ) {
        computeBoundingBoxes(region_m90.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignLeftsOnBottom(region_m90.subList(0,i),region_m90.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position +90 (left)
    Vector<Residue> region_p90 = posManager.getChildrenAtPosition(node,new ResAngle(90),false);
    for( int i=0; i<region_p90.size(); i++ ) {
        computeBoundingBoxes(region_p90.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignRightsOnTop(region_p90.subList(0,i),region_p90.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position 0 (bottom)
    Vector<Residue> region_0 = posManager.getChildrenAtPosition(node,new ResAngle(0));
    for( int i=0; i<region_0.size(); i++ ) {
        computeBoundingBoxes(region_0.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignTopsOnLeft(region_0.subList(0,i),region_0.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position -45 (bottom right)
    Vector<Residue> region_m45 = posManager.getChildrenAtPosition(node,new ResAngle(-45));
    for( int i=0; i<region_m45.size(); i++ ) {
        computeBoundingBoxes(region_m45.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignTopsOnLeft(region_m45.subList(0,i),region_m45.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position 45 (bottom left)
    Vector<Residue> region_p45 = posManager.getChildrenAtPosition(node,new ResAngle(45));
    for( int i=0; i<region_p45.size(); i++ ) {
        computeBoundingBoxes(region_p45.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignTopsOnLeft(region_p45.subList(i,i+1),region_p45.subList(0,i),theGraphicOptions.NODE_SPACE);
    }

    // ----------------
    // align substituents with node
    bboxManager.alignCentersOnRight(node_bbox,region_m90b,theGraphicOptions.NODE_SUB_SPACE);
    bboxManager.alignCentersOnLeft(node_bbox,region_p90b,theGraphicOptions.NODE_SUB_SPACE);

    Union<Residue> border_nodes = new Union<Residue>(region_m90b).and(region_p90b);
    Rectangle border_bbox = bboxManager.getComplete(border_nodes.and(node));    

    // align position 0,45,-45 with node to not clash with 90b,-90b
    if( region_0.size()>0 ) {
        bboxManager.alignTopsOnRight(region_0,region_m45,theGraphicOptions.NODE_SPACE);
        bboxManager.alignTopsOnLeft(region_0,new Union<Residue>(region_0).and(region_m45),region_p45,region_p45,theGraphicOptions.NODE_SPACE);    
        bboxManager.alignCentersOnBottom(node_bbox,border_nodes,region_0,new Union<Residue>(region_0).and(region_m45).and(region_p45),theGraphicOptions.NODE_SPACE);    
    }
    else if( region_m45.size()==0 ) 
        bboxManager.alignCornersOnBottomAtLeft(node_bbox,border_nodes,region_p45,theGraphicOptions.NODE_SPACE,theGraphicOptions.NODE_SPACE);
    else if( region_p45.size()==0 ) 
        bboxManager.alignCornersOnBottomAtRight(node_bbox,border_nodes,region_m45,theGraphicOptions.NODE_SPACE,theGraphicOptions.NODE_SPACE);
    else 
        bboxManager.alignSymmetricOnBottom(node_bbox,border_nodes,region_p45,region_m45,2*theGraphicOptions.NODE_SPACE,2*theGraphicOptions.NODE_SPACE+theGraphicOptions.NODE_SIZE);    

    // align positions -90 and 90 with node to not clash with the rest
    Union<Residue> ex_nodes = new Union<Residue>(region_0).and(region_m45).and(region_p45).and(border_nodes);
    bboxManager.alignCentersOnRight(node_bbox,ex_nodes,region_m90,region_m90,theGraphicOptions.NODE_SPACE);
    bboxManager.alignCentersOnLeft(node_bbox,ex_nodes,region_p90,region_p90,theGraphicOptions.NODE_SPACE);

    // ----------------
    // set rectangles
    Union<Residue> all_nodes = ex_nodes.and(region_m90).and(region_p90).and(node);

    bboxManager.setCurrent(node,node_bbox);
    bboxManager.setBorder(node,border_bbox);
    bboxManager.setComplete(node,bboxManager.getComplete(all_nodes));
    if( node.hasChildren() ) 
        bboxManager.setSupport(node,new Rectangle(midx(node_bbox),midy(node_bbox)+theGraphicOptions.NODE_SPACE+theGraphicOptions.NODE_SIZE,0,0));    
    for(Iterator<Linkage> i=node.iterator(); i.hasNext(); ) 
        bboxManager.setParent(i.next().getChildResidue(),node_bbox);
    }

    private void computeBoundingBoxesBT(Residue node, PositionManager posManager, BBoxManager bboxManager) throws Exception {
    if( node==null )
        return;
    
    Rectangle node_bbox = theResidueRenderer.computeBoundingBox(node,posManager.isOnBorder(node),0,0,posManager.getOrientation(node),theGraphicOptions.NODE_SIZE,Integer.MAX_VALUE);
    bboxManager.setAllBBoxes(node,node_bbox);

    //-----------------
    // place substituents

    // -90 border (left)
    Vector<Residue> region_m90b = posManager.getChildrenAtPosition(node,new ResAngle(-90),true);
    for( int i=0; i<region_m90b.size(); i++ ) {
        computeBoundingBoxes(region_m90b.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignRightsOnTop(region_m90b.subList(0,i),region_m90b.subList(i,i+1),theGraphicOptions.NODE_SUB_SPACE);
    }

    // +90 border (right)
    Vector<Residue> region_p90b = posManager.getChildrenAtPosition(node,new ResAngle(90),true);
    for( int i=0; i<region_p90b.size(); i++ ) {
        computeBoundingBoxes(region_p90b.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignLeftsOnBottom(region_p90b.subList(0,i),region_p90b.subList(i,i+1),theGraphicOptions.NODE_SUB_SPACE);
    }
    // ----------------
    // place positions

    // position -90 (left)
    Vector<Residue> region_m90 = posManager.getChildrenAtPosition(node,new ResAngle(-90),false);
    for( int i=0; i<region_m90.size(); i++ ) {
        computeBoundingBoxes(region_m90.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignRightsOnTop(region_m90.subList(0,i),region_m90.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position +90 (right)
    Vector<Residue> region_p90 = posManager.getChildrenAtPosition(node,new ResAngle(90),false);
    for( int i=0; i<region_p90.size(); i++ ) {
        computeBoundingBoxes(region_p90.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignLeftsOnBottom(region_p90.subList(0,i),region_p90.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position 0 (top)
    Vector<Residue> region_0 = posManager.getChildrenAtPosition(node,new ResAngle(0));
    for( int i=0; i<region_0.size(); i++ ) {
        computeBoundingBoxes(region_0.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignBottomsOnRight(region_0.subList(0,i),region_0.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position -45 (top left)
    Vector<Residue> region_m45 = posManager.getChildrenAtPosition(node,new ResAngle(-45));
    for( int i=0; i<region_m45.size(); i++ ) {
        computeBoundingBoxes(region_m45.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignBottomsOnRight(region_m45.subList(0,i),region_m45.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // position 45 (top right)
    Vector<Residue> region_p45 = posManager.getChildrenAtPosition(node,new ResAngle(45));
    for( int i=0; i<region_p45.size(); i++ ) {
        computeBoundingBoxes(region_p45.elementAt(i),posManager,bboxManager);
        if( i>0 ) bboxManager.alignBottomsOnRight(region_p45.subList(0,i),region_p45.subList(i,i+1),theGraphicOptions.NODE_SPACE);
    }

    // ----------------
    // align substituents with node
    bboxManager.alignCentersOnLeft(node_bbox,region_m90b,theGraphicOptions.NODE_SUB_SPACE);
    bboxManager.alignCentersOnRight(node_bbox,region_p90b,theGraphicOptions.NODE_SUB_SPACE);

    Union<Residue> border_nodes = new Union<Residue>(region_m90b).and(region_p90b);
    Rectangle border_bbox = bboxManager.getComplete(border_nodes.and(node));    

    // align position 0,45,-45 with node to not clash with 90b,-90b
    if( region_0.size()>0 ) {
        bboxManager.alignBottomsOnLeft(region_0,region_m45,theGraphicOptions.NODE_SPACE);
        bboxManager.alignBottomsOnRight(region_0,new Union<Residue>(region_0).and(region_m45),region_p45,region_p45,theGraphicOptions.NODE_SPACE);    
        bboxManager.alignCentersOnTop(node_bbox,border_nodes,region_0,new Union<Residue>(region_0).and(region_m45).and(region_p45),theGraphicOptions.NODE_SPACE);    
    }
    else if( region_m45.size()==0 ) 
        bboxManager.alignCornersOnTopAtRight(node_bbox,border_nodes,region_p45,theGraphicOptions.NODE_SPACE,theGraphicOptions.NODE_SPACE);
    else if( region_p45.size()==0 ) 
        bboxManager.alignCornersOnTopAtLeft(node_bbox,border_nodes,region_m45,theGraphicOptions.NODE_SPACE,theGraphicOptions.NODE_SPACE);
    else 
        bboxManager.alignSymmetricOnTop(node_bbox,border_nodes,region_m45,region_p45,theGraphicOptions.NODE_SPACE,2*theGraphicOptions.NODE_SPACE+theGraphicOptions.NODE_SIZE);    

    // align positions -90 and 90 with node to not clash with the rest
    Union<Residue> ex_nodes = new Union<Residue>(region_0).and(region_m45).and(region_p45).and(border_nodes);
    bboxManager.alignCentersOnLeft(node_bbox,ex_nodes,region_m90,region_m90,theGraphicOptions.NODE_SPACE);
    bboxManager.alignCentersOnRight(node_bbox,ex_nodes,region_p90,region_p90,theGraphicOptions.NODE_SPACE);

    // ----------------
    // set rectangles
    Union<Residue> all_nodes = ex_nodes.and(region_m90).and(region_p90).and(node);

    bboxManager.setCurrent(node,node_bbox);
    bboxManager.setBorder(node,border_bbox);
    bboxManager.setComplete(node,bboxManager.getComplete(all_nodes));
    if( node.hasChildren() ) 
        bboxManager.setSupport(node,new Rectangle(midx(node_bbox),midy(node_bbox)-theGraphicOptions.NODE_SPACE-theGraphicOptions.NODE_SIZE,0,0));    
    for(Iterator<Linkage> i=node.iterator(); i.hasNext(); ) 
        bboxManager.setParent(i.next().getChildResidue(),node_bbox);
    }
    
    //----------------
    // Bounding boxes bracket

    private void computeBoundingBoxesBracket(Residue bracket, Residue root, boolean collapse_multiple_antennae, PositionManager posManager, BBoxManager bboxManager) throws Exception {
    if( bracket==null || root==null )
        return;        

    // compute all bboxes
    ResAngle orientation = posManager.getOrientation(bracket);

    if( orientation.equals(0) ) 
        computeBoundingBoxesBracketLR(bracket,root,collapse_multiple_antennae,posManager,bboxManager);
    else if( orientation.equals(180) ) 
        computeBoundingBoxesBracketRL(bracket,root,collapse_multiple_antennae,posManager,bboxManager);
    else if( orientation.equals(90) ) 
        computeBoundingBoxesBracketTB(bracket,root,collapse_multiple_antennae,posManager,bboxManager);
    else if( orientation.equals(-90) ) 
        computeBoundingBoxesBracketBT(bracket,root,collapse_multiple_antennae,posManager,bboxManager);
    else
        throw new Exception("Invalid orientation " + orientation);
    }

    private void computeBoundingBoxesBracketLR(Residue bracket, Residue root, boolean collapse_multiple_antennae, PositionManager posManager, BBoxManager bboxManager) throws Exception {
    if( bracket==null || root==null  )
        return;
    
    ResAngle orientation = posManager.getOrientation(bracket);

    // compute children bounding boxes
    int id=0;
    int max_quantity = 1;
    Vector<Residue> antennae = new Vector<Residue>();
    TreeMap<String,Pair<Residue,Integer>> unique_antennae = new TreeMap<String,Pair<Residue,Integer>>();    
    for( int i=0; i<bracket.getNoChildren(); i++ ) {
        Residue child = bracket.getChildAt(i); // avoid concurrent modification of iterator!!
        String child_str = (collapse_multiple_antennae) ?GWSParser.writeSubtree(child,false) : (""+(id++));

        Pair<Residue,Integer> value = unique_antennae.get(child_str);
        if( value==null ) {
        Residue antenna = child;

        // create fake attachment point for non-border residues
        if( !posManager.isOnBorder(antenna) ) {            
            antenna = ResidueDictionary.newResidue("#attach");
            child.insertParent(antenna);
            posManager.add(antenna,orientation,new ResAngle(),false,true);       
        }        
        
        // set child bbox
        computeBoundingBoxesLR(antenna,posManager,bboxManager);        
        if( antennae.size()>0 )
            bboxManager.alignLeftsOnBottom(antennae.lastElement(),antenna,theGraphicOptions.NODE_SPACE);
        
        // add antenna to the list
        antennae.add(antenna);
        unique_antennae.put(child_str,new Pair<Residue,Integer>(child,1));
        }
        else {
        // link to other antenna
        bboxManager.linkSubtrees(value.getFirst(),child);

        // update quantity for repeated antenna
        int new_quantity = value.getSecond() + 1;
        unique_antennae.put(child_str,new Pair<Residue,Integer>(value.getFirst(),new_quantity));
        max_quantity = Math.max(max_quantity,new_quantity);
        }
    }        
        
    // compute bracket bbox
    Rectangle structure_bbox = new Rectangle(bboxManager.getComplete(root));
    Rectangle bracket_bbox = new Rectangle(right(structure_bbox),top(structure_bbox),theGraphicOptions.NODE_SIZE,structure_bbox.height);            

    // align antennae       
    if( antennae.size()>0 ) 
        bboxManager.alignCentersOnRight(bracket_bbox,antennae,0);
    Rectangle antennae_bbox = (antennae.size()>0) ?new Rectangle(bboxManager.getComplete(antennae)) :null;
    Rectangle all_bbox = union(bracket_bbox,antennae_bbox);
    
    // compute bbox for quantities
    if( max_quantity>1 ) {
        Dimension quantity_text_dim = textBounds(max_quantity + "x" ,theGraphicOptions.NODE_FONT_FACE,theGraphicOptions.NODE_FONT_SIZE);
        all_bbox.width += quantity_text_dim.width+2;
    }

    // restore linkages
    for( Residue antenna: antennae ) {
        if( !posManager.isOnBorder(antenna) )
        bracket.removeChild(antenna);
    }    

    // set bboxes
    bboxManager.setParent(bracket,structure_bbox);
    bboxManager.setCurrent(bracket,bracket_bbox);
    bboxManager.setBorder(bracket,bracket_bbox);
    bboxManager.setComplete(bracket,all_bbox);
    bboxManager.setSupport(bracket,bracket_bbox);
    }

    private void computeBoundingBoxesBracketRL(Residue bracket, Residue root, boolean collapse_multiple_antennae, PositionManager posManager, BBoxManager bboxManager) throws Exception {
    if( bracket==null || root==null )
        return;

    ResAngle orientation = posManager.getOrientation(bracket);

    // compute children bounding boxes
    int id=0;
    int max_quantity = 1;
    Vector<Residue> antennae = new Vector<Residue>();
    TreeMap<String,Pair<Residue,Integer>> unique_antennae = new TreeMap<String,Pair<Residue,Integer>>();    
    for( int i=0; i<bracket.getNoChildren(); i++ ) {
        Residue child = bracket.getChildAt(i); // avoid concurrent modification of iterator!!
        String child_str = (collapse_multiple_antennae) ?GWSParser.writeSubtree(child,false) : (""+(id++));

        Pair<Residue,Integer> value = unique_antennae.get(child_str);
        if( value==null ) {
        Residue antenna = child;

        // create fake attachment point for non-border residues
        if( !posManager.isOnBorder(antenna) ) {            
            antenna = ResidueDictionary.newResidue("#attach");
            child.insertParent(antenna);
            posManager.add(antenna,orientation,new ResAngle(),false,true);       
        }        
        
        // set child bbox
        computeBoundingBoxesRL(antenna,posManager,bboxManager);        
        if( antennae.size()>0 )
            bboxManager.alignRightsOnTop(antennae.lastElement(),antenna,theGraphicOptions.NODE_SPACE);
            
        // add antenna to the list
        antennae.add(antenna);
        unique_antennae.put(child_str,new Pair<Residue,Integer>(child,1));
        }
        else {
        // link to other antenna
        bboxManager.linkSubtrees(value.getFirst(),child);

        // update quantity for repeated antenna
        int new_quantity = value.getSecond() + 1;
        unique_antennae.put(child_str,new Pair<Residue,Integer>(value.getFirst(),new_quantity));
        max_quantity = Math.max(max_quantity,new_quantity);
        }
    }        


    // compute bracket bbox
    Rectangle structure_bbox = new Rectangle(bboxManager.getComplete(root));
    Rectangle bracket_bbox = new Rectangle(left(structure_bbox)-theGraphicOptions.NODE_SIZE,top(structure_bbox),theGraphicOptions.NODE_SIZE,structure_bbox.height);            

    // align antennae
    if( antennae.size()>0 ) 
        bboxManager.alignCentersOnLeft(bracket_bbox,antennae,0);
    Rectangle antennae_bbox = (antennae.size()>0) ?new Rectangle(bboxManager.getComplete(antennae)) :null;
    Rectangle all_bbox = union(bracket_bbox,antennae_bbox);

    // compute bbox for quantities
    if( max_quantity>1 ) {
        Dimension quantity_text_dim = textBounds(max_quantity + "x" ,theGraphicOptions.NODE_FONT_FACE,theGraphicOptions.NODE_FONT_SIZE);
        all_bbox.x     -= quantity_text_dim.width+2;
        all_bbox.width += quantity_text_dim.width+2;
    }

    // restore linkages
    for( Residue antenna: antennae ) {
        if( !posManager.isOnBorder(antenna) )
        bracket.removeChild(antenna);
    }

    // set bboxes
    bboxManager.setParent(bracket,structure_bbox);
    bboxManager.setCurrent(bracket,bracket_bbox);    
    bboxManager.setBorder(bracket,bracket_bbox);
    bboxManager.setComplete(bracket,all_bbox);
    bboxManager.setSupport(bracket,bracket_bbox);
     }

    private void computeBoundingBoxesBracketTB(Residue bracket, Residue root, boolean collapse_multiple_antennae, PositionManager posManager, BBoxManager bboxManager) throws Exception {
    if( bracket==null || root==null )
        return;

    ResAngle orientation = posManager.getOrientation(bracket);

    // compute children bounding boxes
    int id=0;
    int max_quantity = 1;
    Vector<Residue> antennae = new Vector<Residue>();
    TreeMap<String,Pair<Residue,Integer>> unique_antennae = new TreeMap<String,Pair<Residue,Integer>>();    
    for( int i=0; i<bracket.getNoChildren(); i++ ) {
        Residue child = bracket.getChildAt(i); // avoid concurrent modification of iterator!!
        String child_str = (collapse_multiple_antennae) ?GWSParser.writeSubtree(child,false) : (""+(id++));

        Pair<Residue,Integer> value = unique_antennae.get(child_str);
        if( value==null ) {
        Residue antenna = child;

        // create fake attachment point for non-border residues
        if( !posManager.isOnBorder(antenna) ) {            
            antenna = ResidueDictionary.newResidue("#attach");
            child.insertParent(antenna);
            posManager.add(antenna,orientation,new ResAngle(),false,true);       
        }        
        
        // set child bbox
        computeBoundingBoxesTB(antenna,posManager,bboxManager);        
        if( antennae.size()>0 )
            bboxManager.alignBottomsOnLeft(antennae.lastElement(),antenna,theGraphicOptions.NODE_SPACE);        
                
        // add antenna to the list
        antennae.add(antenna);
        unique_antennae.put(child_str,new Pair<Residue,Integer>(child,1));
        }
        else {
        // link to other antenna
        bboxManager.linkSubtrees(value.getFirst(),child);

        // update quantity for repeated antenna
        int new_quantity = value.getSecond() + 1;
        unique_antennae.put(child_str,new Pair<Residue,Integer>(value.getFirst(),new_quantity));
        max_quantity = Math.max(max_quantity,new_quantity);
        }
    }    


    // compute bracket bbox
    Rectangle structure_bbox = new Rectangle(bboxManager.getComplete(root));
    Rectangle bracket_bbox = new Rectangle(left(structure_bbox),bottom(structure_bbox),width(structure_bbox),theGraphicOptions.NODE_SIZE);            

    // align antennae
    if( antennae.size()>0 ) 
        bboxManager.alignCentersOnBottom(bracket_bbox,antennae,0);
    Rectangle antennae_bbox = (antennae.size()>0) ?new Rectangle(bboxManager.getComplete(antennae)) :null;
    Rectangle all_bbox = union(bracket_bbox,antennae_bbox);

    // compute bbox for quantities
    if( max_quantity>1 ) {
        Dimension quantity_text_dim = textBounds(max_quantity + "x" ,theGraphicOptions.NODE_FONT_FACE,theGraphicOptions.NODE_FONT_SIZE);
        all_bbox.height += quantity_text_dim.width+2; // the string is rotated
    }

    // restore linkages
    for( Residue antenna: antennae ) {
        if( !posManager.isOnBorder(antenna) )
        bracket.removeChild(antenna);
    }

    // set bboxes
    bboxManager.setParent(bracket,structure_bbox);
    bboxManager.setCurrent(bracket,bracket_bbox);    
    bboxManager.setBorder(bracket,bracket_bbox);
    bboxManager.setComplete(bracket,all_bbox);
    bboxManager.setSupport(bracket,bracket_bbox);
    }

    private void computeBoundingBoxesBracketBT(Residue bracket, Residue root, boolean collapse_multiple_antennae, PositionManager posManager, BBoxManager bboxManager) throws Exception {
    if( bracket==null || root==null )
        return;

    ResAngle orientation = posManager.getOrientation(bracket);

    // compute children bounding boxes
    int id=0;
    int max_quantity = 1;
    Vector<Residue> antennae = new Vector<Residue>();
    TreeMap<String,Pair<Residue,Integer>> unique_antennae = new TreeMap<String,Pair<Residue,Integer>>();    
    for( int i=0; i<bracket.getNoChildren(); i++ ) {
        Residue child = bracket.getChildAt(i); // avoid concurrent modification of iterator!!
        String child_str = (collapse_multiple_antennae) ?GWSParser.writeSubtree(child,false) : (""+(id++));

        Pair<Residue,Integer> value = unique_antennae.get(child_str);
        if( value==null ) {
        Residue antenna = child;

        // create fake attachment point for non-border residues
        if( !posManager.isOnBorder(antenna) ) {            
            antenna = ResidueDictionary.newResidue("#attach");
            child.insertParent(antenna);
            posManager.add(antenna,orientation,new ResAngle(),false,true);       
        }        
        
        // set child bbox
        computeBoundingBoxesBT(antenna,posManager,bboxManager);        
        if( antennae.size()>0 )
            bboxManager.alignTopsOnRight(antennae.lastElement(),antenna,theGraphicOptions.NODE_SPACE);        
                
        // add antenna to the list
        antennae.add(antenna);
        unique_antennae.put(child_str,new Pair<Residue,Integer>(child,1));
        }
        else {
        // link to other antenna
        bboxManager.linkSubtrees(value.getFirst(),child);

        // update quantity for repeated antenna
        int new_quantity = value.getSecond() + 1;
        unique_antennae.put(child_str,new Pair<Residue,Integer>(value.getFirst(),new_quantity));
        max_quantity = Math.max(max_quantity,new_quantity);
        }
    }    

        
    // compute bracket bbox
    Rectangle structure_bbox = new Rectangle(bboxManager.getComplete(root));
    Rectangle bracket_bbox = new Rectangle(left(structure_bbox),top(structure_bbox)-theGraphicOptions.NODE_SIZE,width(structure_bbox),theGraphicOptions.NODE_SIZE);            
    // align antennae
    if( antennae.size()>0 ) 
        bboxManager.alignCentersOnTop(bracket_bbox,antennae,0);
    Rectangle antennae_bbox = (antennae.size()>0) ?new Rectangle(bboxManager.getComplete(antennae)) :null;
    Rectangle all_bbox = union(bracket_bbox,antennae_bbox);

    // compute bbox for quantities
    if( max_quantity>1 ) {
        Dimension quantity_text_dim = textBounds(max_quantity + "x" ,theGraphicOptions.NODE_FONT_FACE,theGraphicOptions.NODE_FONT_SIZE);
        all_bbox.y      -= quantity_text_dim.width+2; // the string is rotated
        all_bbox.height += quantity_text_dim.width+2;
    }

    // restore linkages
    for( Residue antenna: antennae ) {
        if( !posManager.isOnBorder(antenna) )
        bracket.removeChild(antenna);
    }

    // set bboxes
    bboxManager.setParent(bracket,structure_bbox);
    bboxManager.setCurrent(bracket,bracket_bbox);
    bboxManager.setBorder(bracket,bracket_bbox);
    bboxManager.setComplete(bracket,all_bbox);
    bboxManager.setSupport(bracket,bracket_bbox);
    }    
   
    //-------------------------
    // Export graphics   

    /**
       Return a graphical representation of a structure as an image
       object.
       @param structure the structure to be displayed
       @param opaque <code>true</code> if a non transparent background
       should be used
       @param show_masses <code>true</code> if the mass information
       about the structure should be displayed
       @param show_redend <code>true</code> if the reducing end marker
       should be displayed    
     */
    public BufferedImage getImage(Glycan structure, boolean opaque, boolean show_masses, boolean show_redend) {
    Vector<Glycan> structures = new Vector<Glycan>();
    if( structure!=null )
        structures.add(structure);
    return getImage(structures,opaque,show_masses,show_redend,1.);
    }    

    /**
       Return a graphical representation of a structure as an image
       object.
       @param structure the structure to be displayed
       @param opaque <code>true</code> if a non transparent background
       should be used
       @param show_masses <code>true</code> if the mass information
       about the structure should be displayed
       @param show_redend <code>true</code> if the  end marker
       should be displayed 
       @param scale the scale factor that should be applied to all
       dimensions
     */
    public BufferedImage getImage(Glycan structure, boolean opaque, boolean show_masses, boolean show_redend, double scale) {
    Vector<Glycan> structures = new Vector<Glycan>();
    if( structure!=null )
        structures.add(structure);
    return getImage(structures,opaque,show_masses,show_redend,scale);
    }    
    
    /**
       Return a graphical representation of a set of structures as an
       image object.
       @param structures the set of structures to be displayed
       @param opaque <code>true</code> if a non transparent background
       should be used
       @param show_masses <code>true</code> if the mass information
       about the structure should be displayed
       @param show_redend <code>true</code> if the  end marker
       should be displayed    
     */
    public BufferedImage getImage(Collection<Glycan> structures, boolean opaque, boolean show_masses, boolean show_redend) {
    return getImage(structures,opaque,show_masses,show_redend,1.);
    }

    /**
       Return a graphical representation of a set of structures as an
       image object.
       @param structures the set of structures to be displayed 
       @param opaque <code>true</code> if a non transparent background
       should be used
       @param show_masses <code>true</code> if the mass information
       about the structure should be displayed
       @param show_redend <code>true</code> if the  end marker
       should be displayed 
       @param scale the scale factor that should be applied to all
       dimensions
     */
    public BufferedImage getImage(Collection<Glycan> structures, boolean opaque, boolean show_masses, boolean show_redend, double scale) {
    if( structures == null )
        structures = new Vector<Glycan>();

    // set scale
    GraphicOptions view_opt = theGraphicOptions;
    boolean old_flag = view_opt.SHOW_INFO;        
    view_opt.SHOW_INFO = (old_flag && scale==1.);
    view_opt.setScale(scale*view_opt.SCALE_CANVAS);    

    // compute bounding boxes;    
    PositionManager posManager = new PositionManager();
    BBoxManager bboxManager = new BBoxManager();
    Rectangle all_bbox = computeBoundingBoxes(structures,show_masses,show_redend,posManager,bboxManager);

    // Create an image that supports transparent pixels
    Dimension d = computeSize(all_bbox);
    BufferedImage img = GraphicUtils.createCompatibleImage(d.width,d.height,opaque);    

    // prepare graphics context
    Graphics2D g2d = img.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

    if( opaque ) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);    

        // clear background
        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, d.width, d.height);
        //g2d.setColor(Color.white);
        //g2d.fillRect(0, 0, d.width, d.height);
    }
    else {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);    
        g2d.setBackground(new Color(255,255,255,0));
    }

    // paint structures
    for( Glycan s : structures ) 
        paint(g2d,s,null,null,show_masses,show_redend,posManager,bboxManager);
    
    // reset scale
    view_opt.setScale(1.);
    view_opt.SHOW_INFO = old_flag;

    return img;
    }
      
}
