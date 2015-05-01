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

import java.util.*;
import java.text.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.awt.print.*;
import javax.swing.*;

public class FragmentCanvas extends JComponent implements Printable, MouseListener, MouseMotionListener, ActionListener {

    public static final int MARGIN_FRAGMENTS = 18;
    public static final int MARGIN_COLUMN = 18;
    public static final int MARGIN_CHILD = 0;
    public static final int MARGIN_CLOSE_BUTTON = 24;
    public static final int SIZE_CLOSE_BUTTON = 12;  

    public static final String TEXT_FONT_FACE = "SansSerif.plain";
    public static final int    TEXT_SIZE = 12;

    protected boolean canvasRequiresRevalidate;

    // Classes

    public interface SelectionChangeListener {    
    public void selectionChanged(SelectionChangeEvent e);    
    }

    public static class SelectionChangeEvent {
    private FragmentCanvas src;

    public SelectionChangeEvent(FragmentCanvas _src) {
        src = _src;
    }

    public FragmentCanvas getSource() {
        return src;
    }       
    }

    public static class FragmentNode {

    public Residue       position = null;
    public FragmentEntry entry = null;
    public Vector<FragmentNode> children = new Vector<FragmentNode>(); 

    public boolean zoom = false;

    Rectangle close_bbox    = null;
    Rectangle fragment_bbox = null;
    Rectangle type_bbox     = null;
    Rectangle mzs_bbox      = null;
    Rectangle children_bbox = null;
    Rectangle node_bbox     = null;
    Rectangle all_bbox      = null;

    public FragmentNode(Residue p, Glycan f) {
        position = p;
        entry = new FragmentEntry(f,Fragmenter.getFragmentType(f));
    }

    public FragmentNode(Residue p, FragmentEntry _entry) {
        position = p;
        entry = _entry;
    }

    public boolean add(FragmentNode toadd) {
        if( toadd==null )
        return false;

        for( FragmentNode child : children ) {
        if( child.position==toadd.position && 
            child.entry.name.equals(toadd.entry.name) &&
            child.entry.structure.equals(toadd.entry.structure) ) 
            return false;         
        }

        children.add(toadd);
        return true;
    }
           
    public void clearChildren() {
        children.clear();
    }

    }

    public class SearchResult {

    public static final int NO_BUTTON = 0;
    public static final int CLOSE_BUTTON = 1;

    protected FragmentNode parent = null;
    protected int          button = NO_BUTTON;
    protected Residue      residue = null;
    protected Linkage      linkage = null;
    protected boolean      is_on_border = false;
    protected boolean      can_do_cleavage = false;
    protected boolean      can_do_ringfragment = false;

    public SearchResult() {
    }

    public SearchResult(FragmentNode fn) {
        parent = fn;
    }

    public SearchResult(FragmentNode fn, int b) {
        parent = fn;
        button = b;
    }

    public SearchResult(FragmentNode fn, Residue r) {
        parent = fn;
        residue = r;

        // validate
        Glycan s = (parent!=null) ?parent.entry.fragment :null;
        if( residue!=null ) {
        is_on_border = thePosManager.isOnBorder(residue);
        if( is_on_border ) {
            if( Fragmenter.canDoCleavage(s,residue,true) )
            can_do_cleavage = true;
            else
            residue = null;
        }
        else {
            if( Fragmenter.canDoRingFragment(s,residue,true) )
            can_do_ringfragment = true;
            else
            residue = null;
        }
        }
    }

    public SearchResult(FragmentNode fn, Linkage l) {
        parent = fn;
        linkage = l;

        Glycan s = (parent!=null) ?parent.entry.fragment :null;
        if( linkage!=null ) {
        if( Fragmenter.canDoCleavage(s,linkage.getChildResidue(),true) )
            can_do_cleavage = true;
        else
            linkage = null;
        }
    }

    public boolean isEmpty() {
        return ( parent==null || (button==NO_BUTTON && residue==null && linkage==null) );        
    }

    public FragmentNode getParent() {
        return parent;
    }

    public Linkage getSelectedLinkage() {    
        return linkage;
    }

    public HashSet<Linkage> getSelectedLinkages() {    
        HashSet<Linkage> set = new HashSet<Linkage>();
        if( linkage!=null )
        set.add(linkage);
        return set;
    }


    public Residue getSelectedResidue() {    
        return residue;
    }

    public HashSet<Residue> getSelectedResidues() {    
        HashSet<Residue> set = new HashSet<Residue>();
        if( residue!=null )
        set.add(residue);
        return set;
    }

    public Residue getFragmentResidue() {
        if( residue!=null )
        return residue;
        if( linkage!=null )
        return linkage.getChildResidue();
        return null;
    }

    public Linkage getFragmentLinkage() {
        if( linkage!=null )
        return linkage;
        if( residue!=null && can_do_cleavage )
        return residue.getParentLinkage();
        return null;
    }

    public boolean isCloseButton() {
        return (button==CLOSE_BUTTON);
    }


    public boolean isOnBorder() {
        return is_on_border;
    }


    public boolean canDoCleavage() { 
        return can_do_cleavage;
    }

    public boolean canDoRingFragment() {
        return can_do_ringfragment;
    }

    public boolean equals(Object other) {
        if( !(other instanceof SearchResult) ) 
        return false;
        
        SearchResult sr = (SearchResult)other;
        if( this.parent!=sr.parent )
        return false;
        if( this.button!=sr.button )
        return false;
        if( this.residue!=sr.residue )
        return false;
        if( this.linkage!=sr.linkage )
        return false;
        return true;
    }

    public int hashCode() {
        return parent.hashCode() + Integer.valueOf(button).hashCode() + residue.hashCode() + linkage.hashCode();
    }
    }


    
    //
    protected static final long serialVersionUID = 0L;    
    protected JScrollPane theScrollPane = null;
    
    // data
    protected Fragmenter   fragmenter;
    protected FragmentNode theRoot;
 
    // selection
    protected FragmentNode with_button_pressed = null;    
    protected SearchResult current_search = null;    

    protected FragmentNode          current_node;
    protected Vector<FragmentNode>  all_nodes;
    protected HashSet<FragmentNode> selected_nodes;

    // painting
    protected GlycanRenderer  theGlycanRenderer;
    protected StyledTextCellRenderer theTextRenderer;

    protected Rectangle       all_bbox;
    protected BBoxManager     theBBoxManager;
    protected PositionManager thePosManager;
    protected boolean         is_printing;

    protected Cursor cut_cursor = null;   
    protected ImageIcon minus_button = null;
    protected ImageIcon minus_button_pressed = null;
    protected ImageIcon cross_button = null;
    protected ImageIcon cross_button_pressed = null;

    protected JLabel sel_label = new JLabel();

    protected Vector<SelectionChangeListener> listeners;

    //-----------------------

    public FragmentCanvas() {
    // init
    theGlycanRenderer = new GlycanRenderer();
    theTextRenderer = new StyledTextCellRenderer();

    thePosManager  = new PositionManager();
    theBBoxManager = new BBoxManager();

    theRoot = null;
    
    current_search = new SearchResult();
    current_node = null;
    all_nodes = new Vector<FragmentNode>();
    selected_nodes = new HashSet<FragmentNode>();

    theGlycanRenderer = new GlycanRenderer();
    thePosManager  = new PositionManager();
    theBBoxManager = new BBoxManager();
    all_bbox = null;
    is_printing = false;

    fragmenter = new Fragmenter();

    listeners = new Vector<SelectionChangeListener>();

    // set the canvas 
    this.setOpaque(true);
    this.setBackground(Color.white);

    // load cursor
    cut_cursor = FileUtils.createCursor("cut");
    minus_button = FileUtils.defaultThemeManager.getImageIcon("fcb_minus");
    cross_button = FileUtils.defaultThemeManager.getImageIcon("fcb_cross");
    minus_button_pressed = FileUtils.defaultThemeManager.getImageIcon("fcb_minusp2");
    cross_button_pressed = FileUtils.defaultThemeManager.getImageIcon("fcb_crossp");

    // add mouse events
    addMouseMotionListener(this);
    addMouseListener(this);

    }

    public JScrollPane getScrollPane() {
    return theScrollPane;
    }

    public void setScrollPane(JScrollPane sp) {
    theScrollPane = sp;
    }

    public GlycanRenderer getGlycanRenderer() {
    return theGlycanRenderer;
    }

    public void setGlycanRenderer(GlycanRenderer r) {
    theGlycanRenderer = r;
    }

    private void createActions() {
    }

    
    private JPopupMenu createPopupMenu(Residue r) {

    JPopupMenu menu = new JPopupMenu();
    
    // add actions to create cross ring cleavages for this residue in string order
    JMenu ax_menu = new JMenu("A/X fragments");
    JMenu a_menu = new JMenu("A fragments");
    JMenu x_menu = new JMenu("X fragments");

    ax_menu.add( new GlycanAction("cleave=a,x",null,"All positions",-1,"",this) );
    a_menu.add( new GlycanAction("cleave=a",null,"All positions",-1,"",this) );
    x_menu.add( new GlycanAction("cleave=x",null,"All positions",-1,"",this) );
    for( int fp=0; fp<=3; fp++ ) {
        for( int lp=fp+2; lp<=5 && (lp-fp)<=4; lp++ ) {
        ax_menu.add( new GlycanAction("cleave=a"+fp+""+lp+",x"+fp+""+lp,null,"Position " + fp + "," + lp,-1,"",this) );
        a_menu.add( new GlycanAction("cleave=a"+fp+""+lp,null,"Position " + fp + "," + lp,-1,"",this) );
        x_menu.add( new GlycanAction("cleave=x"+fp+""+lp,null,"Position " + fp + "," + lp,-1,"",this) );
        }
    }

    menu.add(ax_menu);
    menu.add(a_menu);
    menu.add(x_menu);

    return menu;
    }

    private JPopupMenu createPopupMenu(Linkage l, boolean on_border) {

    JPopupMenu menu = new JPopupMenu();
    
    // add actions to create glycosidic cleavages
    if( on_border ) 
        menu.add( new GlycanAction("cleave=y",null,"Y fragment",-1,"",this) );
    else {
        menu.add( new GlycanAction("cleave=b,y",null,"B/Y fragments",-1,"",this) );
        menu.add( new GlycanAction("cleave=c,z",null,"C/Z fragments",-1,"",this) );
        menu.add( new GlycanAction("cleave=b",null,"B fragment",-1,"",this) );
        menu.add( new GlycanAction("cleave=y",null,"Y fragment",-1,"",this) );
        menu.add( new GlycanAction("cleave=c",null,"C fragment",-1,"",this) );
        menu.add( new GlycanAction("cleave=z",null,"Z fragment",-1,"",this) );
        menu.add( new GlycanAction("cleave=b,y,c,z",null,"All fragments",-1,"",this) );
    }

    return menu;
    }

    //-------------------
    // JComponent

  
    public Dimension getPreferredSize() {
    return theGlycanRenderer.computeSize(all_bbox);
    }            
    
    public Dimension getMinimumSize() {
    return new Dimension(0,0);
    }

    public PositionManager getPositionManager() {
    return thePosManager;
    }

    //-------------------------
    // Data
        
    public void setStructure(Glycan structure) {
    if( structure==null ) 
        theRoot = null;
    else 
        theRoot = new FragmentNode(null,structure);
    
    current_search = new SearchResult();
    refreshAllNodes();
    resetSelection();
    this.canvasRequiresRevalidate=true;
    repaint();
    }

    public void setStructure(Glycan structure, Residue frag_at) {
    if( structure==null ) 
        theRoot = null;
    else {
        theRoot = new FragmentNode(null, structure);
        if( frag_at!=null ) {        
        // update position manager
        theGlycanRenderer.assignPositions(structure,thePosManager); 
        
        // check if it's ring cleavage
        if( thePosManager.isOnBorder(frag_at) )
            onAddFragments(theRoot,frag_at,"y");
        else
            onAddFragments(theRoot,frag_at,"a,x");
        }
    }

    current_search = new SearchResult();
    refreshAllNodes();
    resetSelection();
    this.canvasRequiresRevalidate=true;
    repaint();
    }

    public void setStructure(Glycan structure, Linkage frag_at) {
    if( structure==null ) 
        theRoot = null;
    else {
        theRoot = new FragmentNode(null,structure);
        if( frag_at!=null )
        onAddFragments(theRoot,frag_at.getChildResidue(),"b,y");
    }

    current_search = new SearchResult();
    refreshAllNodes();
    resetSelection();
    this.canvasRequiresRevalidate=true;
    repaint();
    }

    private boolean addFragment(FragmentNode parent, Residue r, Glycan f) {
    if( parent==null || r==null || f==null )
        return false;
    if( parent.add(new FragmentNode(r,f)) ) {
        refreshAllNodes();
        return true;
    }
    return false;
    }    

    private void clearChildren(FragmentNode parent) {
    parent.clearChildren();
    refreshAllNodes();
    }

    private void refreshAllNodes() {
    all_nodes.clear();
    getAllNodes(all_nodes,theRoot);
    }

    static private void getAllNodes(Vector<FragmentNode> buffer, FragmentNode current) {
    if( current==null ) 
        return;

    buffer.add(current);
    for( FragmentNode child : current.children) 
        getAllNodes(buffer,child);
    }


    //-------------------    
    // painting

    protected void paintComponent(Graphics g) {
    if (isOpaque()) { //paint background
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }

    // prepare graphic object
    Graphics2D g2d = (Graphics2D)g.create();        
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    // set clipping area
    Rectangle clipRect = new Rectangle();
    g.getClipBounds(clipRect);


    // set scale
    GraphicOptions theGraphicOptions = theGlycanRenderer.getGraphicOptions();
    boolean old_flag = theGraphicOptions.SHOW_INFO;        
    double old_scale = theGraphicOptions.SCALE;

    theGraphicOptions.SHOW_INFO = false;

    // compute bounding boxes
    all_bbox = null;
    if( theRoot!=null ) {
        thePosManager.reset();
        theBBoxManager.reset();

        // compute columns
        Rectangle s_bbox = computeStructuresBoundingBoxes(theGraphicOptions.MARGIN_LEFT,theGraphicOptions.MARGIN_TOP,theRoot);
        Rectangle t_bbox = computeTypesBoundingBoxes(Geometry.right(s_bbox) + MARGIN_COLUMN, theRoot);
        Rectangle m_bbox = computeMZsBoundingBoxes(Geometry.right(t_bbox) + MARGIN_COLUMN, theRoot);
        computeNodeBoundingBoxes(theGraphicOptions.MARGIN_LEFT,theRoot);

        // allocate viewport size
        if( theScrollPane!=null ) {
        int max_width = theScrollPane.getViewport().getExtentSize().width - theGraphicOptions.MARGIN_LEFT - theGraphicOptions.MARGIN_RIGHT - MARGIN_CLOSE_BUTTON;
        int cur_width = s_bbox.width + MARGIN_COLUMN + t_bbox.width + MARGIN_COLUMN + m_bbox.width;
        if( cur_width < max_width ) {
            int delta = max_width - cur_width;        
            s_bbox = computeStructuresBoundingBoxes(theGraphicOptions.MARGIN_LEFT,theGraphicOptions.MARGIN_TOP,theRoot);
            t_bbox = computeTypesBoundingBoxes(Geometry.right(s_bbox) + delta/3 + MARGIN_COLUMN, theRoot);
            m_bbox = computeMZsBoundingBoxes(Geometry.right(t_bbox) + delta/3 + MARGIN_COLUMN, theRoot);    
            computeNodeBoundingBoxes(theGraphicOptions.MARGIN_LEFT,theRoot);

            all_bbox = new Rectangle(theGraphicOptions.MARGIN_LEFT, theGraphicOptions.MARGIN_TOP, max_width, 0);
        }
        }
        
        all_bbox = Geometry.union(all_bbox,s_bbox);
        all_bbox = Geometry.union(all_bbox,t_bbox);
        all_bbox = Geometry.union(all_bbox,m_bbox);        
    }

    
    // draw        
    paint(g2d,theRoot);
    paintSelection(g2d);
    
    //System.err.println("Painting fragment canvas");
    if(this.canvasRequiresRevalidate==true){
    	this.canvasRequiresRevalidate=false;
    	revalidate();
    }
    
    

    // reset scale
    theGraphicOptions.setScale(old_scale);
    theGraphicOptions.SHOW_INFO = old_flag;

    // dispose graphic object
    g2d.dispose();    
    }      

    private Rectangle computeStructuresBoundingBoxes(int cur_left, int cur_top, FragmentNode fn) {
    if( fn==null )
        return null;
    
    GraphicOptions theGraphicOptions = theGlycanRenderer.getGraphicOptions();
    if( fn.zoom )
        theGraphicOptions.setScale(Math.max(theGraphicOptions.SCALE_CANVAS,1.));
    else
        theGraphicOptions.setScale(0.75*theGraphicOptions.SCALE_CANVAS);
    // 
    fn.all_bbox = new Rectangle(cur_left,cur_top,1,1);

    // compute bbox for fragment
    fn.fragment_bbox = theGlycanRenderer.computeBoundingBoxes(fn.entry.fragment,cur_left + MARGIN_CLOSE_BUTTON,cur_top,false,true,thePosManager,theBBoxManager);
    fn.all_bbox = Geometry.union(fn.all_bbox,fn.fragment_bbox);
        
    // compute bbox for close button
    if( fn.children.size()>0 ) {
        //if( fn!=theRoot || fn.children.size()>0 ) { 
        int left = cur_left + MARGIN_CLOSE_BUTTON/3 - SIZE_CLOSE_BUTTON/2;
        int top  = cur_top + fn.fragment_bbox.height/2 - SIZE_CLOSE_BUTTON/2;
        fn.close_bbox = new Rectangle(left,top,SIZE_CLOSE_BUTTON,SIZE_CLOSE_BUTTON);
    }
    else
        fn.close_bbox = null;
    cur_left = cur_left + MARGIN_CLOSE_BUTTON;

    // compute bbox for children
    fn.children_bbox = null;
    for( FragmentNode child : fn.children) {
        cur_top = Geometry.bottom(fn.all_bbox) + MARGIN_FRAGMENTS;
        Rectangle child_bbox = computeStructuresBoundingBoxes(cur_left + MARGIN_CHILD, cur_top, child);
        fn.all_bbox = Geometry.union(fn.all_bbox,child_bbox);
        fn.children_bbox = Geometry.union(fn.children_bbox,child_bbox);
    }
    
    return fn.all_bbox;
    }

    private Rectangle computeTypesBoundingBoxes(int cur_left, FragmentNode fn) {

    Dimension d = getTextBounds(fn.entry.name);
    fn.type_bbox = new Rectangle(cur_left,Geometry.midy(fn.fragment_bbox)-d.height/2,d.width,d.height);

    Rectangle all_bbox = fn.type_bbox;
    for( FragmentNode child : fn.children) 
        all_bbox = Geometry.union(all_bbox,computeTypesBoundingBoxes(cur_left,child));

    return all_bbox;
    }    
        
    private Rectangle computeMZsBoundingBoxes(int cur_left, FragmentNode fn) {
    DecimalFormat df = new DecimalFormat("0.0000");    
    Dimension d = getTextBounds(df.format(fn.entry.mz_ratio.doubleValue()));
    fn.mzs_bbox = new Rectangle(cur_left,Geometry.midy(fn.fragment_bbox)-d.height/2,d.width,d.height);

    Rectangle all_bbox = fn.mzs_bbox;
    for( FragmentNode child : fn.children) 
        all_bbox = Geometry.union(all_bbox,computeMZsBoundingBoxes(cur_left,child));

    return all_bbox;
    }    

    private void computeNodeBoundingBoxes(int cur_left, FragmentNode fn) {
    int width = fn.fragment_bbox.width + MARGIN_COLUMN + fn.type_bbox.width + MARGIN_COLUMN + fn.mzs_bbox.width;
    fn.node_bbox = new Rectangle(cur_left,fn.fragment_bbox.y-MARGIN_FRAGMENTS/2,width,fn.fragment_bbox.height+ MARGIN_FRAGMENTS);

    for( FragmentNode child : fn.children) 
        computeNodeBoundingBoxes(cur_left + MARGIN_CHILD, child);
    }

    private Dimension getTextBounds(String text) {
    Component c = theTextRenderer.getRendererComponent(new Font(TEXT_FONT_FACE,Font.PLAIN,TEXT_SIZE),Color.black,this.getBackground(),text);
    return c.getPreferredSize();
    }

    private void paintIcon(Graphics2D g2d, Rectangle bbox, ImageIcon icon) {
    if( g2d!=null && bbox!=null && icon!=null ) {
        AffineTransform at = new AffineTransform();
        at.setToTranslation(bbox.x,bbox.y);
        g2d.drawImage(icon.getImage(),at,null);
    }
    }

    private void paintText(Graphics2D g2d, Rectangle bbox, String text) {
    //g2d.drawString(fn.entry.name, Geometry.left(fn.type_bbox), Geometry.bottom(fn.type_bbox));
    Component c = theTextRenderer.getRendererComponent(new Font(TEXT_FONT_FACE,Font.PLAIN,TEXT_SIZE),Color.black,this.getBackground(),text);
    SwingUtilities.paintComponent(g2d,c,this,bbox.x,bbox.y,bbox.width,bbox.height);
    }

    private void paint(Graphics2D g2d, FragmentNode fn) {
    if( fn==null )
        return;

    GraphicOptions theGraphicOptions = theGlycanRenderer.getGraphicOptions();
    if( fn.zoom )
        theGraphicOptions.setScale(Math.max(theGraphicOptions.SCALE_CANVAS,1.));
    else
        theGraphicOptions.setScale(0.75*theGraphicOptions.SCALE_CANVAS);

    // create selection
    HashSet<Residue> selected_residues = current_search.getSelectedResidues();
    HashSet<Linkage> selected_linkages = current_search.getSelectedLinkages();

    // paint button
    if( !is_printing && fn.close_bbox!=null)     
        paintIcon(g2d,fn.close_bbox,minus_button);
    
    // paint fragment
    theGlycanRenderer.paint(g2d,fn.entry.fragment,selected_residues,selected_linkages,false,true,thePosManager,theBBoxManager);

    // paint type
    paintText(g2d,fn.type_bbox,fn.entry.name);

    // paint mzs
    DecimalFormat df = new DecimalFormat("0.0000");    
    paintText(g2d,fn.mzs_bbox,df.format(fn.entry.mz_ratio.doubleValue()));

    // paint separator line
    if( Geometry.bottom(fn.fragment_bbox)!=Geometry.bottom(all_bbox) ) {
        g2d.setColor(Color.gray);    
        g2d.drawLine(Geometry.left(fn.fragment_bbox),Geometry.bottom(fn.fragment_bbox) + MARGIN_FRAGMENTS/2,
             Geometry.right(all_bbox)+theGraphicOptions.MARGIN_RIGHT + MARGIN_CLOSE_BUTTON,Geometry.bottom(fn.fragment_bbox) + MARGIN_FRAGMENTS/2);
    }
        
    // paint children
    for( FragmentNode child : fn.children) 
        paint(g2d,child);
    }



    private void paintSelection(Graphics2D g2d) {
    GraphicOptions theGraphicOptions = theGlycanRenderer.getGraphicOptions();
    
    for(Iterator<FragmentNode> i = all_nodes.iterator(); i.hasNext(); ) {        
        FragmentNode n = i.next();
        if( selected_nodes.contains(n) ) {
        Rectangle bbox = n.node_bbox;
        for( ; i.hasNext(); ) {
            FragmentNode t = i.next();
            if( selected_nodes.contains(t) ) 
            bbox = Geometry.union(bbox,t.node_bbox);
            else
            break;
        }
        
        g2d.setColor(UIManager.getColor("Table.selectionBackground"));
        g2d.fill(new Rectangle(theGraphicOptions.MARGIN_LEFT/2-3,bbox.y,5,bbox.height));        
        }        
    }    
    
    // paint cur_structure
    if( current_node!=null && current_node.node_bbox!=null ) {
        Rectangle cur_bbox = current_node.node_bbox;
        UIManager.getBorder("Table.focusCellHighlightBorder").paintBorder(sel_label,g2d,theGraphicOptions.MARGIN_LEFT/2-3,cur_bbox.y,5,cur_bbox.height);        
    }
    }

 
    //------------------
    // actions

    public void onAddFragments(String arguments) {
    onAddFragments(current_search.getParent(),current_search.getFragmentResidue(),arguments);    
    }

    public void onAddFragments(FragmentNode fn, Residue r, String arguments) {
    if( fn==null || r==null )
        return;

    Glycan s = fn.entry.fragment;
    Vector<String> fragment_types = TextUtils.tokenize(arguments,",");
    for( String t : fragment_types ) {
        char type = t.charAt(0); 
        if( type=='b' ) {
        for( Glycan fragment : fragmenter.getAllBFragmentsWithLabiles(s,r) )
            addFragment(fn,r,fragment);
        }
        else if( type=='c' ) {
        for( Glycan fragment : fragmenter.getAllCFragmentsWithLabiles(s,r) )
            addFragment(fn,r,fragment);
        }
        else if( type=='y' ) {
        if( r.isLabile() )
            addFragment(fn,r,fragmenter.getLFragment(s,r));
        else {
            for( Glycan fragment : fragmenter.getAllYFragmentsWithLabiles(s,r) )
            addFragment(fn,r,fragment);
        }
        }
        else if( type=='z' ) {
        for( Glycan fragment : fragmenter.getAllZFragmentsWithLabiles(s,r) )
            addFragment(fn,r,fragment);
        }
        else if( type=='a' || type=='x' ) {
        if( t.length()==1 ) {
            // try all 
            for( int fp=0; fp<=3; fp++ ) {
            for( int lp=fp+2; lp<=5 && (lp-fp)<=4; lp++ ) {
                CrossRingFragmentType crt = CrossRingFragmentDictionary.getCrossRingFragmentType(type,fp,lp, r);
                if( crt!=null ) {
                if( type=='a' ) {
                    for( Glycan fragment : fragmenter.getAllAFragmentsWithLabiles(s,r,crt) )
                    addFragment(fn,r,fragment);
                }
                else {
                    for( Glycan fragment : fragmenter.getAllXFragmentsWithLabiles(s,r,crt) )
                    addFragment(fn,r,fragment);
                }
                }
            }
            }
        }
        else {
            int fp = (int)(t.charAt(1) - '0');
            int lp = (int)(t.charAt(2) - '0');
            CrossRingFragmentType crt = CrossRingFragmentDictionary.getCrossRingFragmentType(type,fp,lp, r);
            if( crt!=null ) {
            if( type=='a' ) {
                for( Glycan fragment : fragmenter.getAllAFragmentsWithLabiles(s,r,crt) )
                addFragment(fn,r,fragment);
            }
            else {
                for( Glycan fragment : fragmenter.getAllXFragmentsWithLabiles(s,r,crt) )
                addFragment(fn,r,fragment);
            }            
            }
        }
        }
    }
    this.canvasRequiresRevalidate=true;
    repaint();        
    }    

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

    
    //----------------
    // selection
   

    public SearchResult getWhatsAt(Point p) {
    return getWhatsAt(theRoot,p);
    }

    private SearchResult getWhatsAt(FragmentNode fn, Point p) {
    if( fn==null )
        return new SearchResult();

    // match button
    if( fn.close_bbox!=null && fn.close_bbox.contains(p) )
        return new SearchResult(fn,SearchResult.CLOSE_BUTTON);

    // search in the fragment
    SearchResult ret = getWhatsAt(fn,fn.entry.fragment,p);
    if( !ret.isEmpty() )
        return ret;

    // search in the children
    for( FragmentNode child : fn.children ) {
        ret = getWhatsAt(child,p);
        if( !ret.isEmpty() )
        return ret;
    }
    return new SearchResult(fn);
    }

    private SearchResult getWhatsAt(FragmentNode fn, Glycan f, Point p) {
    if( f==null )
        return new SearchResult(fn);
    
    SearchResult ret = getWhatsAt(fn,f.getRoot(),p);
    if( !ret.isEmpty() )
        return ret;

    return getWhatsAt(fn,f.getBracket(),p);
    }

    private SearchResult getWhatsAt(FragmentNode fn, Residue r, Point p) {
    if( r==null )
        return new SearchResult(fn);

    // match current
    Rectangle cur_bbox = theBBoxManager.getCurrent(r);
    if( cur_bbox==null ) 
        return new SearchResult(fn);    
    if( cur_bbox.contains(p) )
        return new SearchResult(fn,r);
    
    for( Linkage l : r.getChildrenLinkages() ) {
        // match child
        Residue child = l.getChildResidue();
        Rectangle child_bbox = theBBoxManager.getCurrent(child);        
        if( child_bbox!=null ) {
        if( child_bbox.contains(p) )
            return new SearchResult(fn,child);

        // match link
        if( Geometry.distance(p,Geometry.center(cur_bbox),Geometry.center(child_bbox))<4. )
            return new SearchResult(fn,l);

        // search in the child
        SearchResult ret = getWhatsAt(fn,child,p);
        if( !ret.isEmpty() )
            return ret;
        }
    }
    return new SearchResult(fn);
    }

    private void updateSearch(MouseEvent e) {
    // search
    SearchResult ret = getWhatsAt(e.getPoint());

    // decide actions
    boolean to_repaint = (!current_search.equals(ret));
    boolean on_something = ret.canDoCleavage() || ret.canDoRingFragment();
    
    // set cursor
    if( on_something && getCursor()!=cut_cursor ) 
        setCursor(cut_cursor);
    else if( !on_something && getCursor()==cut_cursor ) 
        setCursor(Cursor.getDefaultCursor());        

    // repaint canvas
    if( to_repaint ){
    	this.canvasRequiresRevalidate=true;
    	repaint();
    }

    current_search = ret;
    }        

    public FragmentNode getNodeAtPoint(Point p) {
    for( FragmentNode n : all_nodes) {
        if( n.node_bbox!=null && n.node_bbox.contains(p) )
        return n;
    }
    return null;
    }

    public boolean hasSelection() {
    return (selected_nodes.size()>0);
    }

    public void resetSelection() {    
    selected_nodes.clear();
    current_node = null;

    fireUpdatedSelection();
    }
    
    public boolean hasCurrentNode() {
    return (current_node!=null);
    }
    
    public FragmentNode getCurrentNode() {
    return current_node;
    }

    private void setCurrentNode(FragmentNode node) {
    if( node!=null )
        selected_nodes.add(node);
    current_node = node;

    fireUpdatedSelection();
    }

    public boolean isSelected(FragmentNode node) {
    if( node==null )
        return false;
    return selected_nodes.contains(node);
    }
 
  
    public Collection<FragmentNode> getSelectedNodes() {
    return selected_nodes;
    }

    public Collection<Glycan> getSelectedFragments() {

    Vector<Glycan> ret = new Vector<Glycan>();
    for( FragmentNode fn : selected_nodes ) {
        if( fn.entry.fragment!=null )        
        ret.add(fn.entry.fragment);
    }
        
    return ret;
    }
        
    
    public void setSelection(FragmentNode node) {
    if( node==null ) 
        resetSelection();    
    else {
        selected_nodes.clear();
        selected_nodes.add(node);
        current_node = node;
        
        fireUpdatedSelection();        
    }
    }  

    public void addSelection(FragmentNode node) {
    if( node!=null ) {
        selected_nodes.add(node);
        current_node = node;

        fireUpdatedSelection();
    }
    }

    public void addSelectionPathTo(FragmentNode node) {
    if( node!=null ) {
        if( current_node==null ) 
        selected_nodes.add(node);        
        else 
        selected_nodes.addAll(getPath(current_node,node));           
        current_node = node;

        fireUpdatedSelection();
    }
    }  

    private Vector<FragmentNode> getPath(FragmentNode from, FragmentNode to) {
    Vector<FragmentNode> ret = new Vector<FragmentNode>();
    if( from==null || to==null )
        return ret;

    int ind1 = all_nodes.indexOf(from);
    int ind2 = all_nodes.indexOf(to);
    if( ind1<=ind2 ) {
        for( int i=ind1; i<=ind2; i++ )
        ret.add(all_nodes.elementAt(i));
    }
    else {
        for( int i=ind2; i<=ind1; i++ )
        ret.add(all_nodes.elementAt(i));
    }

    return ret;
    }

    public void toggleZoom(FragmentNode node) {
    if( node!=null ) {
        node.zoom = !node.zoom;
        this.canvasRequiresRevalidate=true;
        repaint();
    }
    }


    //----------------------------
    // listeners


    public void actionPerformed(ActionEvent e) {

    String action = GlycanAction.getAction(e);
    String param  = GlycanAction.getParam(e);

    if( action.equals("cleave") ) 
        onAddFragments(param);
    }


    private void showPopup(MouseEvent e) {
    if( current_search.canDoCleavage() )
        createPopupMenu(current_search.getFragmentLinkage(),current_search.isOnBorder()).show(this, e.getX(), e.getY());           
    else if( current_search.canDoRingFragment() )
        createPopupMenu(current_search.getFragmentResidue()).show(this, e.getX(), e.getY());           
    }

    public void drawButton(FragmentNode fn, ImageIcon icon) {
    Graphics2D g2d = (Graphics2D)this.getGraphics();
    paintIcon(g2d,with_button_pressed.close_bbox,icon);
    g2d.dispose();
    }

    public void mousePressed(MouseEvent e) {    
    if( MouseUtils.isPopupTrigger(e) ) {
        updateSearch(e);
        showPopup(e);
    }
    else if( MouseUtils.isPushTrigger(e) ) {        
        updateSearch(e);
        if( current_search.isCloseButton() ) {
        // paint pressed button
        with_button_pressed = current_search.getParent();
        drawButton(with_button_pressed,minus_button_pressed);
        }
    }
    }        

    public void mouseDragged(MouseEvent e) {    
    if( with_button_pressed!=null ) {
        updateSearch(e);
        if( current_search.getParent()!=with_button_pressed || !current_search.isCloseButton() ) {
        // reset button
        drawButton(with_button_pressed,minus_button);
        with_button_pressed = null;
        }
    }
    }        
    
    public void mouseReleased(MouseEvent e) {
    if( with_button_pressed!=null ) {
        updateSearch(e);

        if( current_search.getParent()==with_button_pressed && current_search.isCloseButton() ) {
        // clear children
        clearChildren(current_search.parent);
        }
        with_button_pressed = null;
        this.canvasRequiresRevalidate=true;
        repaint();
    }
    if( MouseUtils.isPopupTrigger(e) ) {
        updateSearch(e);
        showPopup(e);
    }
    }    
    
    public void mouseClicked(MouseEvent e) {      

    // update search
    if( MouseUtils.isSelectTrigger(e) ) {
        updateSearch(e);

        if( current_search.canDoCleavage() ) {
        // glycosidic cleavages
        onAddFragments("b,y");
        }
        if( current_search.canDoRingFragment() ) {
        // cross ring fragments
        onAddFragments("a,x");
        }
    }

    // update selection
    FragmentNode n = getNodeAtPoint(e.getPoint());
    if( n!=null ) {
        if( MouseUtils.isSelectTrigger(e) ) 
        setSelection(n);
        else if( MouseUtils.isAddSelectTrigger(e) )
        addSelection(n);
        else if( MouseUtils.isSelectAllTrigger(e) )
        addSelectionPathTo(n);    
        else if( MouseUtils.isActionTrigger(e) ) 
        toggleZoom(n);
    }
    else 
        resetSelection();
    }
    
    public void mouseEntered(MouseEvent e) {    
    }

    public void mouseExited(MouseEvent e) {    
    }

    public void mouseMoved(MouseEvent e) {    
    updateSearch(e);
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
    this.canvasRequiresRevalidate=true;
    repaint();
    //showSelection();
    }  
}
