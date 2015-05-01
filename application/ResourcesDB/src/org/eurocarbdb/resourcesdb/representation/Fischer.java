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

import java.awt.Dimension;
import java.util.ArrayList;

import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.monosaccharide.CoreModification;
import org.eurocarbdb.resourcesdb.monosaccharide.CoreModificationTemplate;
import org.eurocarbdb.resourcesdb.monosaccharide.Monosaccharide;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideValidation;
import org.eurocarbdb.resourcesdb.monosaccharide.Ringtype;
import org.eurocarbdb.resourcesdb.monosaccharide.StereoConfiguration;
import org.eurocarbdb.resourcesdb.monosaccharide.Substitution;

/**
* Create Fischer projections of monosaccharides using the <code>SvgFactory</code> class
* @author Thomas Lütteke
*
*/
public class Fischer extends SvgFactory {

    private static int defaultWidth = 120;
    private static int defaultHeight = 120;
    
    private int lineLength = 15;
    
    private int backboneX = 60;
    private int positionY = 0;
    
    private static final String OH = "OH";
    private static final String HO = "HO";
    private static final String H = "H";
    private static final String O = "O";
    private static final String CH3 = "CH3";
    private static final String CH2 = "CH2";
    private static final String CH = "CH";
    private static final String CHO = "CHO";
    private static final String COOH = "COOH";
    private static final String CH2OH = "CH2OH";
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /**
     * Constructor, creates a Fischer object using the default width and height values
     */
    public Fischer() {
        super();
        this.setSvgWidth(defaultWidth);
        this.setSvgHeight(defaultHeight);
        this.getSVGGraph2D().setSVGCanvasSize(new Dimension(defaultWidth, defaultHeight));
    }
    
    /**
     * Constructor, creates a Fischer object with the given dimensions
     * @param width the width of the SVG graphic
     * @param height the height of the SVG graphic
     */
    public Fischer(int width, int height) {
        super();
        this.setSvgWidth(width);
        this.setSvgHeight(height);
        this.getSVGGraph2D().setSVGCanvasSize(new Dimension(width, height));
    }
    
    //*****************************************************************************
    //*** drawing methods: ********************************************************
    //*****************************************************************************
    
    private void drawVerticalLines(double bo) {
        if(bo == 1) {
            this.drawLine(this.backboneX, this.positionY, this.backboneX, this.positionY + this.lineLength);
        } else if(bo == 2) {
            this.drawLine(this.backboneX -2, this.positionY, this.backboneX -2, this.positionY + this.lineLength);
            this.drawLine(this.backboneX +2, this.positionY, this.backboneX +2, this.positionY + this.lineLength);
        } else if(bo == 3) {
            this.drawLine(this.backboneX -3, this.positionY, this.backboneX -3, this.positionY + this.lineLength);
            this.drawLine(this.backboneX, this.positionY, this.backboneX, this.positionY + this.lineLength);
            this.drawLine(this.backboneX +3, this.positionY, this.backboneX +3, this.positionY + this.lineLength);
        }
        this.positionY += this.lineLength;
    }
    
    private void drawChainPosition(int pos) throws ResourcesDbException {
        Monosaccharide ms = this.getMonosacc();
        double boToPrevious = 1;
        String leftLabel = null;
        double leftBo = 1;
        String rightLabel = null;
        double rightBo = 1;
        String positionLabel = "C";
        int cWidthHalf = this.getStringWidth("C") / 2;
        
        StereoConfiguration sConf = ms.getStereocode().getPositionConfiguration(pos);
        if(pos == 1) {
            this.positionY = this.getTextSize() + 2;
            boToPrevious = 0;
            if(ms.isAlditol()) {
                if(ms.hasCoreModification(CoreModificationTemplate.DEOXY, 1)) {
                    positionLabel = Fischer.CH3;
                } else {
                    positionLabel = Fischer.CH2OH;
                }
            } else if(ms.isAldaric() || ms.isAldonic()) {
                positionLabel = Fischer.COOH;
            } else if(ms.hasCoreModification(CoreModificationTemplate.KETO) && !ms.hasCoreModification(CoreModificationTemplate.KETO, 1)) {
                positionLabel = Fischer.CH2OH;
            } else {
                positionLabel = Fischer.CHO;;
            }
            //TODO: implement substituents at position 1
            if(ms.getSubstitutionsByPosition(pos).size() > 0) {
                throw new ResourcesDbException("Fischer projection of monosaccharides with terminal substitutions not yet supported.");
            }
            this.drawString(positionLabel, this.backboneX - cWidthHalf, this.positionY);
            this.positionY += 2;
        } else if(pos < ms.getSize()) {
            for(CoreModification mod : ms.getCoreModificationsByPosition(pos)) {
                if(CoreModificationTemplate.EN.equals(mod.getTemplate()) && mod.getIntValuePosition2() == pos) {
                    boToPrevious = 2;
                }
                if(CoreModificationTemplate.YN.equals(mod.getTemplate()) && mod.getIntValuePosition2() == pos) {
                    boToPrevious = 3;
                }
            }
            Substitution ohLinkedSubst = ms.getSubstitution(null, pos, LinkageType.H_AT_OH);
            if(ohLinkedSubst == null) {
                ohLinkedSubst = ms.getSubstitution(null, pos, LinkageType.DEOXY);
            }
            Substitution cLinkedSubst = ms.getSubstitution(null, pos, LinkageType.H_LOSE);
            if(sConf.equals(StereoConfiguration.Dexter)) {
                if(ohLinkedSubst != null) {
                    rightLabel = ohLinkedSubst.getTemplate().getHaworthName();
                    if(ohLinkedSubst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                        rightLabel = "O-" + rightLabel;
                    }
                    rightBo = ohLinkedSubst.getBondOrder1();
                } else {
                    rightLabel = Fischer.OH;
                }
                if(cLinkedSubst != null) {
                    leftLabel = cLinkedSubst.getTemplate().getMirroredHaworthName();
                    leftBo = cLinkedSubst.getBondOrder1();
                } else {
                    leftLabel = Fischer.H;
                }
            } else if(sConf.equals(StereoConfiguration.Laevus)) {
                if(ohLinkedSubst != null) {
                    leftLabel = ohLinkedSubst.getTemplate().getMirroredHaworthName();
                    if(ohLinkedSubst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                        leftLabel += "-O";
                    }
                    leftBo = ohLinkedSubst.getBondOrder1();
                } else {
                    leftLabel = Fischer.HO;
                }
                if(cLinkedSubst != null) {
                    rightLabel = cLinkedSubst.getTemplate().getHaworthName();
                    rightBo = cLinkedSubst.getBondOrder1();
                } else {
                    rightLabel = Fischer.H;
                }
            } else if(sConf.equals(StereoConfiguration.Nonchiral)) {
                if(ms.hasDoubleBond(pos)) {
                    if(!ms.hasDoubleBond(pos - 1) || !ms.hasDoubleBond(pos + 1)) {
                        //TODO: implement E/Z notation
                        if(ms.hasCoreModification(CoreModificationTemplate.DEOXY, pos)) {
                            rightLabel = Fischer.H;
                            rightBo = 1;
                        } else {
                            Substitution subst = ms.getSubstitution(null, pos, null);
                            if(subst == null) {
                                rightLabel = Fischer.OH;
                                rightBo = 1;
                            } else {
                                rightLabel = subst.getTemplate().getHaworthName();
                                if(subst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                                    rightLabel = "O-" + rightLabel;
                                }
                            }
                        }
                    }
                } else if(ms.hasCoreModification(CoreModificationTemplate.DEOXY, pos)) {
                    rightLabel = Fischer.H;
                    rightBo = 1;
                    leftLabel = Fischer.H;
                    leftBo = 1;
                } else if(ms.hasCoreModification(CoreModificationTemplate.KETO, pos)) {
                    rightLabel = Fischer.O;
                    rightBo = 2;
                } else if(ms.hasCoreModification(CoreModificationTemplate.SP2)) {
                    Substitution subst = ms.getSubstitution(null, pos, null);
                    rightLabel = subst.getTemplate().getHaworthName();
                    rightBo = 2;
                }
            } else {
                throw new ResourcesDbException("Cannot draw Fischer projection for monosaccharide with StereoConfiguration " + sConf.getFullname());
            }
            this.drawVerticalLines(boToPrevious);
            this.positionY += this.getTextSize() + 2;
            this.drawString(positionLabel, this.backboneX - cWidthHalf, this.positionY);
            int lineY = this.positionY - this.getTextSize() / 2;
            if(leftLabel != null) {
                int x1 = this.backboneX - cWidthHalf - 2;
                int x2 = x1 - this.lineLength;
                if(leftBo == 1) {
                    this.drawLine(x1, lineY, x2, lineY);
                } else if(leftBo == 2) {
                    this.drawLine(x1, lineY - 2, x2, lineY - 2);
                    this.drawLine(x1, lineY + 2, x2, lineY + 2);
                }
                this.drawString(leftLabel, x2 - 2 - this.getStringWidth(leftLabel), this.positionY);
            }
            if(rightLabel != null) {
                int x1 = this.backboneX + cWidthHalf + 2;
                int x2 = x1 + this.lineLength;
                if(rightBo == 1) {
                    this.drawLine(x1, lineY, x2, lineY);
                } else if(rightBo == 2) {
                    this.drawLine(x1, lineY - 2, x2, lineY - 2);
                    this.drawLine(x1, lineY + 2, x2, lineY + 2);
                }
                this.drawString(rightLabel, x2 + 2, this.positionY);
            }
            this.positionY += 2;
        } else { //*** pos == ms.size (terminal carbon) ***
            this.drawVerticalLines(boToPrevious);
            this.positionY += this.getTextSize() + 2;
            int centerLabelPosition = this.backboneX - cWidthHalf;
            if(ms.isUronic() || ms.isAldaric()) {
                positionLabel = Fischer.COOH;
            } else if(ms.hasCoreModification(CoreModificationTemplate.DEOXY, pos)) {
                if(boToPrevious == 3) {
                    positionLabel = Fischer.CH;
                } else if(boToPrevious == 2) {
                    positionLabel = Fischer.CH2;
                } else {
                    positionLabel = Fischer.CH3;
                }
            } else if(ms.hasCoreModification(CoreModificationTemplate.KETO, pos)) {
                positionLabel = Fischer.CHO;
            } else {
                positionLabel = Fischer.CH2OH;
            }
            ArrayList<Substitution> substList = ms.getSubstitutionsByPosition(pos);
            if(substList.size() > 0) {
                if(substList.size() == 1) {
                    Substitution subst = substList.get(0);
                    if(ms.isUronic() || ms.isAldaric()) {
                        if(subst.getLinkagetype1().equals(LinkageType.DEOXY)) {
                            positionLabel = "CO" + subst.getTemplate().getHaworthName();
                        } else {
                            positionLabel = "COO" + subst.getTemplate().getHaworthName();
                        }
                    } else if(subst.getLinkagetype1().equals(LinkageType.DEOXY) || subst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                        int hCount = 4 - (int) Math.floor(boToPrevious + subst.getBondOrder1());
                        positionLabel = "C";
                        if(hCount > 0) {
                            positionLabel += "H"; 
                        }
                        if(hCount > 1) {
                            positionLabel += hCount;
                        }
                        if(subst.getLinkagetype1().equals(LinkageType.H_AT_OH)) {
                            positionLabel += "O";
                        }
                        positionLabel += subst.getTemplate().getHaworthName();
                    } else {
                        throw new ResourcesDbException("LinkageType " + subst.getLinkagetypeStr1() + " not yet supported in Fischer projection at terminal position");
                    }
                } else {
                    throw new ResourcesDbException("Multiple substituents at terminal carbon not yet supported in Fischer projection");
                }
            }
            this.drawString(positionLabel, centerLabelPosition, this.positionY);
        }
    }
    
    private void drawRing() throws ResourcesDbException {
        throw new ResourcesDbException("Fischer projection for ring monosaccharide is not yet implemented.");
    }
    
    private void drawOpenChain() throws ResourcesDbException {
        for(int pos = 1; pos <= this.getMonosacc().getSize(); pos ++) {
            this.drawChainPosition(pos);
        }
        this.checkSize();
    }
    
    public void drawMonosaccharide(Monosaccharide ms) throws ResourcesDbException {
        if(MonosaccharideValidation.checkFuzziness(ms)) {
            throw new ResourcesDbException("Cannot draw Fischer projection for monosaccharide with uncertain / fuzzy properties.");
        }
        if(ms.hasCoreModification(CoreModificationTemplate.ANHYDRO)) {
            throw new ResourcesDbException("Cannot draw Fischer projection for monosaccharide with anhydro modification.");
        }
        if(ms.hasCoreModification(CoreModificationTemplate.LACTONE)) {
            throw new ResourcesDbException("Cannot draw Fischer projection for monosaccharide with lactono modification.");
        }
        if(ms.hasCoreModification(CoreModificationTemplate.EPOXY)) {
            throw new ResourcesDbException("Cannot draw Fischer projection for monosaccharide with epoxy modification.");
        }
        this.setMonosacc(ms);
        if(ms.getRingtype().equals(Ringtype.PYRANOSE) || ms.getRingtype().equals(Ringtype.FURANOSE)) {
            this.drawRing();
        } else if(ms.getRingtype().equals(Ringtype.OPEN)) {
            this.drawOpenChain();
        } else {
            throw new ResourcesDbException("Cannot draw Fischer projection for monosaccharide with ring type " + ms.getRingtype().getName());
        }
        this.checkSize(); //*** check, if all elements of the graphic are within the visible area, rescale or resize if not ***
    }

}
