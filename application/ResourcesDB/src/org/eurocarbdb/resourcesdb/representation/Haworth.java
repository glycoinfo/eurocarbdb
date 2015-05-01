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
package org.eurocarbdb.resourcesdb.representation;

import java.awt.*;
import java.util.ArrayList;

import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.atom.Atom;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.monosaccharide.*;

/**
* Create Haworth projections of monosaccharide rings using the <code>SvgFactory</code> class
* @author Thomas Lütteke
*
*/
public class Haworth extends SvgFactory {
    
    /**
     * The X coordinates of the default basepoints of a pyranose ring
     */
    private static int[] pyranoseX = {100, 80, 40, 20, 40, 80};
    /**
     * The Y coordinates of the default basepoints of a pyranose ring
     */
    private static int[] pyranoseY = {60, 90, 90, 60, 30, 30};
    
    /**
     * The X coordinates of the default basepoints of a furanose ring
     */
    private static int[] furanoseX = {100, 85, 35, 20, 60};
    /**
     * The Y coordinates of the default basepoints of a furanose ring
     */
    private static int[] furanoseY = {50, 80, 80, 50, 30};
    
    /**
     * The basepoints, i.e. the corner points of the haworth penta- or hexagon shape
     */
    private ArrayList<Point> basepoints;
    /**
     * The center of the haworth penta- or hexagon shape
     */
    private Point baseCenter;
    
    private static final String OH = "OH";
    private static final String HO = "HO";
    private static final String CH3 = "CH3";
    private static final String H3C = "H3C";
    private static final String COOH = "COOH";
    private static final String HOOC = "HOOC";
    private static final String COO = "COO";
    private static final String OOC = "OOC";
    
    private static int defaultWidth = 120;
    private static int defaultHeight = 120;
    
    private int lineLength = 15;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /**
     * Constructor, creates a Haworth object using the default width and height values
     */
    public Haworth() {
        super();
        this.setSvgWidth(defaultWidth);
        this.setSvgHeight(defaultHeight);
        this.getSVGGraph2D().setSVGCanvasSize(new Dimension(defaultWidth, defaultHeight));
    }
    
    /**
     * Constructor, creates a Haworth object with the given dimensions
     * @param width the width of the SVG graphic
     * @param height the height of the SVG graphic
     */
    public Haworth(int width, int height) {
        super();
        this.setSvgWidth(width);
        this.setSvgHeight(height);
        this.getSVGGraph2D().setSVGCanvasSize(new Dimension(width, height));
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    private ArrayList<Point> getBasepoints() {
        return this.basepoints;
    }
    
    private Point getBasepoint(int index) {
        return(this.getBasepoints().get(index - 1));
    }
    
    private void setBasepoints(ArrayList<Point> pointList) {
        this.basepoints = pointList;
    }
    
    private Point getBaseCenter() {
        return this.baseCenter;
    }

    private void setBaseCenter(Point baseCenter) {
        this.baseCenter = baseCenter;
    }

    /**
     * Set the basepoints of a pyranose ring from the coordinates of the <code>pyranoseX</code> and <code>pyranoseY</code> arrays
     */
    private void setPyranoseBasepoints() {
        setBasepoints(new ArrayList<Point>());
        for(int i = 0; i < 6; i++) {
            this.getBasepoints().add(new Point(pyranoseX[i], pyranoseY[i]));
        }
    }
    
    /**
     * Set the basepoints of a furanose ring from the coordinates of the <code>furanoseX</code> and <code>furanoseY</code> arrays
     */
    private void setFuranoseBasepoints() {
        setBasepoints(new ArrayList<Point>());
        for(int i = 0; i < 5; i++) {
            this.getBasepoints().add(new Point(furanoseX[i], furanoseY[i]));
        }
    }
    
    //*****************************************************************************
    //*** drawing methods: ********************************************************
    //*****************************************************************************
    
    /**
     * Draw a pyranose ring template, i.e. the hexagon shape with the ring oxygen, but without any exocyclic atoms
     */
    private void drawPyranoseRingTemplate() {
        String ringOStr = "O";
        if(this.getMonosacc().getRingEnd() > 0) {
            Substitution ringOSubst = this.getMonosacc().getSubstitution(null, this.getMonosacc().getRingEnd(), LinkageType.DEOXY);
            if(ringOSubst != null) {
                Atom ringAtom;
                try {
                    ringAtom = ringOSubst.getTemplate().getLinkingAtom(ringOSubst.getIntValueSubstituentPosition1());
                } catch(ResourcesDbException rEx) {
                    ringAtom = null;
                }
                if(ringAtom == null) {
                    ringOStr = "X";
                } else {
                    ringOStr = ringAtom.getElementSymbol();
                }
            }
        }
        int oOffset = this.getCharacterCenterXOffset(ringOStr, true);
        this.drawLine(getBasepoint(4).x, getBasepoint(4).y, getBasepoint(5).x, getBasepoint(5).y);
        this.drawLineWithOffset(getBasepoint(6).x, getBasepoint(6).y, getBasepoint(5).x, getBasepoint(5).y, this.getStringWidth(ringOStr));
        this.drawLineWithOffset(getBasepoint(6).x, getBasepoint(6).y, getBasepoint(1).x, getBasepoint(1).y, this.getTextSize() * 0.6);
        Polygon pg = new Polygon();
        int dp = Math.abs(getBasepoint(1).y - getBasepoint(2).y) / 10;
        pg.addPoint(getBasepoint(1).x, getBasepoint(1).y);
        pg.addPoint(getBasepoint(2).x, getBasepoint(2).y - dp);
        pg.addPoint(getBasepoint(3).x, getBasepoint(3).y - dp);
        pg.addPoint(getBasepoint(4).x, getBasepoint(4).y);
        pg.addPoint(getBasepoint(3).x, getBasepoint(3).y + dp);
        pg.addPoint(getBasepoint(2).x, getBasepoint(2).y + dp);
        this.fillShape(pg);
        this.drawString(ringOStr, getBasepoint(6).x + oOffset, getBasepoint(6).y + (this.getTextSize() / 2));
    }
    
    /**
     * Draw a furanose ring template, i.e. the pentagon shape with the ring oxygen, but without any exocyclic atoms
     */
    private void drawFuranoseRingTemplate() {
        String ringOStr = "O";
        int oOffset = this.getCharacterCenterXOffset(ringOStr, false);
        this.drawLineWithOffset(getBasepoint(5).x, getBasepoint(5).y, getBasepoint(4).x, getBasepoint(4).y, this.getTextSize() * 0.6);
        this.drawLineWithOffset(getBasepoint(5).x, getBasepoint(5).y, getBasepoint(1).x, getBasepoint(1).y, this.getTextSize() * 0.6);
        Polygon pg = new Polygon();
        int dp = Math.abs(getBasepoint(1).y - getBasepoint(2).y) / 10;
        pg.addPoint(getBasepoint(1).x, getBasepoint(1).y);
        pg.addPoint(getBasepoint(2).x, getBasepoint(2).y - dp);
        pg.addPoint(getBasepoint(3).x, getBasepoint(3).y - dp);
        pg.addPoint(getBasepoint(4).x, getBasepoint(4).y);
        pg.addPoint(getBasepoint(3).x, getBasepoint(3).y + dp);
        pg.addPoint(getBasepoint(2).x, getBasepoint(2).y + dp);
        this.fillShape(pg);
        this.drawString(ringOStr, getBasepoint(5).x + oOffset, getBasepoint(5).y + (int)(Math.floor(this.getTextSize() * 0.6)));
    }
    
    /**
     * Draw a pyranose residue in Haworth projection
     * @throws ResourcesDbException
     */
    private void drawPyranose() throws ResourcesDbException {
        this.setPyranoseBasepoints();
        this.calculateBasecenter();
        this.drawPyranoseRingTemplate();
        //*** draw modifications / OH groups at ring carbons: ***
        for(int i = 1; i < 6; i++) {
            this.drawPyranoseRingPosition(i);
        }
    }
    
    /**
     * Add a single position to a pyranose ring graphic
     * If exocyclic carbons are present at ring positions 1 or 5, these are drawn together with these positions (represented by an "R" in case more than one exocyclic carbon is present at a position)
     * @param ringPos the carbon position within the pyranose ring (1 to 5, i.e. C1 to C5 of an aldose or e.g. C2 to C6 of a 2-ketose)
     * @throws ResourcesDbException
     */
    private void drawPyranoseRingPosition(int i) throws ResourcesDbException {
        this.drawRingPosition(i);
    }
    
    private void drawRingPosition(int ringPos) throws ResourcesDbException {
        Monosaccharide ms = this.getMonosacc();
        
        //*** 'pos' marks position within the pyranose ring, while 'msPos' marks position within the monosaccharide ***
        //*** (values differ for monosaccharides with a carbonyl position other than one, e.g. ketoses) ***
        int msPos = ringPos - 1 + ms.getRingStart();
        
        //*** get stereo configuration: ***
        StereoConfiguration sConf = ms.getStereocode().getPositionConfiguration(msPos);
        if(msPos == ms.getRingEnd()) {
            sConf = StereoConfiguration.invert(sConf);
        }
        
        //*** calculate coordinates: ***
        Point p = this.getBasepoint(ringPos);
        //*** coordinates of line start (x1/y1), end (x2/y2) and label (xt/yt) of the OH group or an o-linked or deoxy substituent: ***
        int x1 = p.x;
        int y1 = p.y;
        int x2 = x1;
        int y2 = y1;
        int yt = y1;
        int xt = x1;
        //*** coordinates of line end (x2_c/y2_c) and label (xt_c/yt_c) of a potential c-linked substituent (line start is the same as above)  ***
        int x2_c = x1;
        int y2_c = y1;
        int xt_c = xt;
        int yt_c = yt;
        
        boolean alignLabelRight = false;
        boolean alignLabelRight_c = false;
        String substLabel = "";
        String substLabel_c = null;
        int bo = 1;
        if(sConf.equals(StereoConfiguration.Dexter)) {
            x2 = p.x;
            y2 = p.y + this.lineLength;
            yt = y2 + this.getTextSize();
            bo = 1;
            x2_c = p.x;
            y2_c = p.y - this.lineLength;
            yt_c = y2_c - 2;
        } else if(sConf.equals(StereoConfiguration.Laevus)) {
            x2 = p.x;
            y2 = p.y - this.lineLength;
            yt = y2 - 2;
            x2_c = p.x;
            y2_c = p.y + this.lineLength;
            yt_c = y2_c + this.getTextSize();
            bo = 1;
        } else if(sConf.equals(StereoConfiguration.Nonchiral)) {
            for(CoreModification mod : ms.getCoreModificationsByPosition(msPos)) {
                if(mod.getTemplate().equals(CoreModificationTemplate.DEOXY)) {
                    bo = 0;
                }
                if(mod.getTemplate().equals(CoreModificationTemplate.EN)) {
                    if(mod.getIntValuePosition1() == msPos) {
                        this.drawRingDoubleBond(ringPos);
                    }
                    int xc = this.getBaseCenter().x;
                    int yc = this.getBaseCenter().y;
                    x2 = x1 - xc;
                    y2 = y1 - yc;
                    double len = 15.0 / Math.round((float)Math.sqrt(x2 * x2 + y2 * y2));
                    
                    x2 = (int) (x1 + len * x2);
                    y2 = (int) (y1 + len * y2);
                    if(x2 > x1) {
                        xt = x2;
                    } else {
                        xt = x2;
                        alignLabelRight = true;
                    }
                    if(y2 > y1) {
                        yt = y2 + this.getSVGGraph2D().getFontMetrics().getHeight();
                    } else if(y2 == y1) {
                        yt = y2 + this.getSVGGraph2D().getFontMetrics().getHeight() / 2;
                    } else {
                        yt = y2 - 2;
                    }
                }
                if(mod.getTemplate().equals(CoreModificationTemplate.SP2)) {
                    bo = 2;
                }
            }
        } else {
            throw new MonosaccharideException("Unsupported stereochemistry in Haworth formula: " + sConf.getFullname());
        }
        if(bo > 0) {
            //*** at position 4 (any chirality) or at position 2 (and subst. pointing up, i.e. L-chirality) the label has to point to the right: ***
            //TODO: check position 3, take into account chirality of position 2 to avoid overlappings (esp. if both 2 and 3 point up)
            if(ringPos == 4) {
                alignLabelRight = true;
            } else if(ringPos == 2) {
                if(sConf.equals(StereoConfiguration.Laevus)) {
                    alignLabelRight = true;
                }
            }
            //*** get position label: ***
            if(msPos != ms.getRingEnd()) {
                ArrayList<Substitution> substList = ms.getSubstitutionsByPosition(msPos);
                if(substList != null && substList.size() > 0) {
                    for(Substitution subst : substList) {
                        if(!subst.getLinkagetype1().equals(LinkageType.H_LOSE)) {
                            if(alignLabelRight) {
                                substLabel = subst.getTemplate().getMirroredHaworthName();
                                if(subst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                                    substLabel += "O";
                                }
                            } else {
                                substLabel = subst.getTemplate().getHaworthName();
                                if(subst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                                    substLabel = "O" + substLabel;
                                }
                            }
                        } else {
                            //*** add c-linked substituent: ***
                            if(alignLabelRight_c) {
                                substLabel_c = subst.getTemplate().getMirroredHaworthName();
                            } else {
                                substLabel_c = subst.getTemplate().getHaworthName();
                            }
                        }
                    }
                } 
                if(substLabel.equals("")) {
                    if(alignLabelRight) {
                        substLabel = Haworth.HO;
                    } else {
                        substLabel = Haworth.OH;
                    }
                }
            }
            //*** carbon connected to ring oxygen: check, if "tail" (C6 (aldopyranose) or respective groups) has to be added ***
            if(msPos == ms.getRingEnd()) {
                if(msPos == ms.getSize()) {
                    //TODO: check for substituents with r/s-config linkage type
                    return;
                }
                if((ms.getSize() - msPos) == 1) {
                    if(ms.isAldaric() || ms.isUronic()) {
                        ArrayList<Substitution> substList = ms.getSubstitutionsByPosition(ms.getSize());
                        Substitution subst = null;
                        if(substList != null && substList.size() > 0) {
                            subst = substList.get(0);
                            if(!subst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                                throw new ResourcesDbException("cannot draw haworth projection for monosaccharide with substitution at acid group and linkage type " + subst.getLinkagetypeStr1());
                            }
                        }
                        if(sConf.equals(StereoConfiguration.Laevus) || sConf.equals(StereoConfiguration.Nonchiral) || ms.getRingtype().equals(Ringtype.FURANOSE)) {
                            if(subst == null) {
                                substLabel = HOOC;
                            } else {
                                substLabel = subst.getTemplate().getHaworthName() + "-" + OOC;
                            }
                            alignLabelRight = true;
                        } else {
                            if(subst == null) {                            
                                substLabel = COOH;
                            } else {
                                substLabel = COO + "-" + subst.getTemplate().getHaworthName();
                            }
                        }
                    } else {
                        if(!ms.hasCoreModification(CoreModificationTemplate.DEOXY, msPos + 1)) {
                            if(sConf.equals(StereoConfiguration.Laevus) || sConf.equals(StereoConfiguration.Nonchiral) || ms.getRingtype().equals(Ringtype.FURANOSE)) {
                                this.drawLine(x2, y2, x2 - this.lineLength, y2);
                                String c6label = Haworth.HO;
                                ArrayList<Substitution> substList = ms.getSubstitutionsByPosition(msPos + 1);
                                if(substList != null && substList.size() > 0) {
                                    Substitution subst = substList.get(0);
                                    c6label = subst.getTemplate().getMirroredHaworthName();
                                    if(subst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                                        c6label += "O";
                                    }
                                }
                                this.drawString(c6label, x2 - this.lineLength - 2 - this.getSVGGraph2D().getFontMetrics().stringWidth(c6label), y2 - 2 + this.getSVGGraph2D().getFontMetrics().getHeight() / 2);
                            } else {
                                this.drawLine(x2, y2, x2 + this.lineLength, y2);
                                String c6label = Haworth.OH;
                                ArrayList<Substitution> substList = ms.getSubstitutionsByPosition(msPos + 1);
                                if(substList != null && substList.size() > 0) {
                                    Substitution subst = substList.get(0);
                                    c6label = subst.getTemplate().getHaworthName();
                                    if(subst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                                        c6label = "O" + c6label;
                                    }
                                }
                                this.drawString(c6label, x2 + this.lineLength + 2, y2 - 2 + this.getSVGGraph2D().getFontMetrics().getHeight() / 2);
                            }
                        } else {
                            if(sConf.equals(StereoConfiguration.Laevus) || sConf.equals(StereoConfiguration.Nonchiral) || ms.getRingtype().equals(Ringtype.FURANOSE)) {
                                substLabel = H3C;
                                alignLabelRight = true;
                            } else {
                                substLabel = CH3;
                            }
                        }
                    }
                } else {
                    if(ms.getRingStart() > 2) {
                        substLabel = "R2";
                    } else {
                        substLabel = "R";
                    }
                }
            }
            //*** draw position data: ***
            if(bo == 1) {
                this.drawLine(x1, y1, x2, y2);
                if(!substLabel.equals("")) {
                    if(xt == x1) {
                        xt += this.getCharacterCenterXOffset(substLabel, alignLabelRight);
                    } else if(xt == x2 && alignLabelRight) {
                        xt -= this.getSVGGraph2D().getFontMetrics().stringWidth(substLabel);
                    }
                    this.drawString(substLabel, xt, yt);
                }
                if(substLabel_c != null) {
                    this.drawLine(x1, y1, x2_c, y2_c);
                    if(xt_c == x1) {
                        xt_c += this.getCharacterCenterXOffset(substLabel, alignLabelRight);
                    } else if(xt_c == x2_c && alignLabelRight) {
                        xt_c -= this.getSVGGraph2D().getFontMetrics().stringWidth(substLabel);
                    }
                    if(!substLabel_c.equals("")) {
                        this.drawString(substLabel_c, xt_c, yt_c);
                    }
                }
            } else if(bo == 2) {
                throw new MonosaccharideException("Double bonds outside the ring are not supported yet.");
            }
        }
        //*** ring position 1: check, if monosaccharide position > 1 (i.e. exocyclic backbone carbons have to be added) ***
        if(ringPos == 1 && msPos > 1) { //*** carbonyl position > 1: add C1 ***
            int x2b = x1;
            int y2b = y1;
            int xt2 = x1;
            int yt2 = y1;
            String substLabelC1 = null;
            if(sConf.equals(StereoConfiguration.Dexter)) {
                y2b = y1 - this.lineLength;
                yt2 = y2b - 2;
            } else if(sConf.equals(StereoConfiguration.Laevus)) {
                y2b = y1 + this.lineLength;
                yt2 = y2b + this.getTextSize();
            } else if(sConf.equals(StereoConfiguration.Nonchiral)) {
                y2b = y1;
                yt2 = y2b + this.getTextSize() / 2;
                x2b = x1 + this.lineLength;
                xt2 = x2b + 2;
            }
            this.drawLine(x1, y1, x2b, y2b);
            if(msPos == 2) { //*** only C1 to be added ***
                if(ms.isAldaric() || ms.isAldonic()) {
                    ArrayList<Substitution> substList = ms.getSubstitutionsByPosition(1);
                    Substitution subst = null;
                    if(substList != null && substList.size() > 0) {
                        subst = substList.get(0);
                        if(!subst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                            throw new ResourcesDbException("cannot draw haworth projection for monosaccharide with substitution at acid group and linkage type " + subst.getLinkagetypeStr1());
                        }
                    }
                    if(subst == null) {
                        substLabelC1 = COOH;
                    } else {
                        substLabelC1 = COO + "-" + subst.getTemplate().getHaworthName();
                    }
                } else {
                    if(!ms.hasCoreModification(CoreModificationTemplate.DEOXY, 1)) {
                        if(sConf.equals(StereoConfiguration.Nonchiral)) {
                            this.drawLine(x2b, y2b, x2b, y2b - this.lineLength);
                            xt2 = x2b + this.getCharacterCenterXOffset(substLabelC1, false);
                            yt2 = y2b - this.lineLength - 2;
                        } else {
                            this.drawLine(x1, y2b, x1 + this.lineLength, y2b);
                            xt2 = x1 + this.lineLength + 2;
                            if(sConf.equals(StereoConfiguration.Dexter)) {
                                yt2 = yt2 + this.getTextSize() / 2 + 2;
                            } else if(sConf.equals(StereoConfiguration.Laevus)) {
                                yt2 = yt2 - this.getTextSize() / 2 - 2;
                            }
                        }
                        substLabelC1 = Haworth.OH;
                        ArrayList<Substitution> substList = ms.getSubstitutionsByPosition(1);
                        if(substList != null && substList.size() > 0) {
                            substLabelC1 = substList.get(0).getTemplate().getHaworthName();
                        }
                    }
                }
            } else { //*** more than one atom to be added ***
                if(ms.getSize() - ms.getRingEnd() > 1) {
                    substLabelC1 = "R1";
                } else {
                    substLabelC1 = "R";
                }
            }
            if(substLabelC1 != null) {
                if(xt2 == x1) {
                    xt2 += this.getCharacterCenterXOffset(substLabelC1, false);
                }
                this.drawString(substLabelC1, xt2, yt2);
            }
        }
    }
    
    /**
     * Draw a furanose residue in Haworth projection
     * @throws ResourcesDbException
     */
    private void drawFuranose() throws ResourcesDbException {
        this.setFuranoseBasepoints();
        this.calculateBasecenter();
        this.drawFuranoseRingTemplate();
        //*** draw modifications / OH groups at ring carbons: ***
        for(int i = 1; i < 5; i++) {
            this.drawFuranoseRingPosition(i);
        }
    }
    
    /**
     * Add a single position to a furanose ring graphic
     * If exocyclic carbons are present at ring positions 1 or 4, these are drawn together with these positions (represented by an "R" in case more than one exocyclic carbon is present at a position)
     * @param ringPos the carbon position within the furanose ring (1 to 4, i.e. C1 to C5 of an aldose or e.g. C2 to C6 of a 2-ketose)
     * @throws ResourcesDbException
     */
    private void drawFuranoseRingPosition(int rPos) throws ResourcesDbException {
        this.drawRingPosition(rPos);
    }
    
    /**
     * Draw a line to indicate a double bond within a ring
     * @param pos the first position of the double bond
     */
    private void drawRingDoubleBond(int pos) {
        int xc = this.getBaseCenter().x;
        int yc = this.getBaseCenter().y;
        int lx1 = this.getBasepoint(pos).x;
        int ly1 = this.getBasepoint(pos).y;
        int lx2 = this.getBasepoint(pos + 1).x;
        int ly2 = this.getBasepoint(pos + 1).y;
        int dx1 = Math.abs(lx1 - xc);
        int dy1 = Math.abs(ly1 - yc);
        int dx2 = Math.abs(lx2 - xc);
        int dy2 = Math.abs(ly2 - yc);
        if(lx1 < xc)
            lx1 = (int) (lx1 + 0.2 * dx1);
        if(lx1 > xc)
            lx1 = (int) (lx1 - 0.2 * dx1);
        if(ly1 < yc)
            ly1 = (int) (ly1 + 0.2 * dy1);
        if(ly1 > yc)
            ly1 = (int) (ly1 - 0.2 * dy1);
        if(lx2 < xc)
            lx2 = (int) (lx2 + 0.2 * dx2);
        if(lx2 > xc)
            lx2 = (int) (lx2 - 0.2 * dx2);
        if(ly2 < yc)
            ly2 = (int) (ly2 + 0.2 * dy2);
        if(ly2 > yc)
            ly2 = (int) (ly2 - 0.2 * dy2);
        this.drawLine(lx1, ly1, lx2, ly2);
    }
    
    private void addRests() {
        SvgFactory r1 = null;
        SvgFactory r2 = null;
        if(this.getMonosacc().getRingStart() > 2) {
            r1 = this.drawRest(this.getMonosacc().getRingStart(), 1);
        }
        if(this.getMonosacc().getSize() - this.getMonosacc().getRingEnd() > 1) {
            r2 = this.drawRest(this.getMonosacc().getRingEnd(), this.getMonosacc().getSize());
        }
        int rx = this.getXMax() + 5;
        int ry = 0;
        if(r1 != null) {
            String rLabel;
            int rx1, ry1;
            if(r2 == null) {
                rLabel = "R=";
                rx1 = rx;
                ry1 = ry + this.getTextSize();
            } else {
                rLabel = "R1=";
                rx1 = rx + this.getStringWidth(rLabel);
                ry1 = ry;
            }
            this.drawString(rLabel, rx, ry + this.getTextSize());
            this.addSubGraphic(r1, rx1, ry1);
            ry = r1.getYMax() + 5;
            //TODO: add r1 subgraph to svg tree
        }
        if(r2 != null) {
            //TODO: add r2 subgraph to svg tree
            String rLabel;
            int rx2, ry2;
            if(r1 == null) {
                rLabel = "R=";
                rx2 = rx;
                ry2 = ry + this.getTextSize();
            } else {
                rLabel = "R2=";
                rx2 = rx + this.getStringWidth(rLabel);
                ry2 = ry;
            }
            this.drawString(rLabel, rx2, ry2);
            this.addSubGraphic(r2, rx2, ry2);
        }
    }
    
    private SvgFactory drawRest(int msStartPos, int msEndPos) {
        SvgFactory rest = new SvgFactory();
        Monosaccharide ms = this.getMonosacc();
        int msPos;
        int currentY = 0;
        if(msStartPos > msEndPos) {
            msPos = msStartPos;
            while(msPos >= msEndPos) {
                msPos --;
                rest.drawLine(0, currentY, 0, currentY + this.lineLength);
                currentY += this.lineLength;
                if(!ms.hasCoreModification(CoreModificationTemplate.DEOXY, msPos)) {
                    
                }
            }
        } else {
            msPos = msStartPos; 
            while(msPos <= msEndPos) {
                msPos ++;
                rest.drawLine(0, currentY, 0, currentY + this.lineLength);
                currentY += this.lineLength;
                if(!ms.hasCoreModification(CoreModificationTemplate.DEOXY, msPos)) {
                    
                }
            }
        }
        return rest;
    }
    
    public void drawMonosaccharide(Monosaccharide ms) throws ResourcesDbException {
        if(MonosaccharideValidation.checkFuzziness(ms)) {
            throw new ResourcesDbException("Cannot draw Haworth projection for monosaccharide with uncertain / fuzzy properties.");
        }
        if(ms.hasCoreModification(CoreModificationTemplate.ANHYDRO)) {
            throw new ResourcesDbException("Cannot draw Haworth projection for monosaccharide with anhydro modification.");
        }
        /*for(CoreModification mod : ms.getCoreModifications(CoreModificationTemplate.ACID.getName())) {
            if(ms.getSubstitutionsByPosition(mod.getIntValuePosition1()).size() > 0) {
                throw new ResourcesDbException("Cannot (yet) draw Haworth projection for monosaccharide with substitution at acid modification.");
            }
        } */
        this.setMonosacc(ms);
        if(ms.getRingtype().equals(Ringtype.PYRANOSE)) {
            this.drawPyranose();
        } else if(ms.getRingtype().equals(Ringtype.FURANOSE)) {
            this.drawFuranose();
        } else {
            throw new ResourcesDbException("Cannot draw Haworth projection for monosaccharide with ring type " + ms.getRingtype().getName());
        }
        this.addRests(); //*** add exocyclic parts, if necessary ***
        this.checkSize(); //*** check, if all elements of the graphic are within the visible area, rescale or resize if not ***
    }

    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    private void calculateBasecenter() throws MonosaccharideException {
        if(this.getBasepoints() == null) {
            throw new MonosaccharideException("Cannot calculate basecenter: basepoints not set");
        }
        int size = this.getBasepoints().size();
        if(size == 0) {
            throw new MonosaccharideException("Cannot calculate basecenter: basepoints not set");
        } else {
            int xSum = 0;
            int ySum = 0;
            for(Point p : this.getBasepoints()) {
                xSum += p.getX();
                ySum += p.getY();
            }
            this.setBaseCenter(new Point(xSum / size, ySum / size));
        }
    }
    
}
