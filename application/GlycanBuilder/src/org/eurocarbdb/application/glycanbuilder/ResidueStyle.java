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

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

/**
   This class contains the information about the graphic style of a
   residue in a certain notation. The style specify shape, colors and
   text. The style will be used by the {@link ResidueRenderer}
   instances to render a residue. The identifier of the style must be
   the same as the corresponding {@link ResidueType}.

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/


public class ResidueStyle {

    private String  name;

    private String  shape;
    private Color   shape_color;

    private String  fill_style;
    private boolean fill_negative;
    private Color   fill_color;

    private String  text;
    private Color   text_color;

    /**
       Create a default style that represent a residue by its type
       name only.
     */
    public ResidueStyle() {    
    name = "#empty";

    shape       = null;
    shape_color = Color.black;

    fill_style     = "empty";
    fill_negative  = false;
    fill_color     = Color.white;

    text       = null;
    text_color = Color.black;
    }

    /**
      Create a new residue style from an initialization string.
       @throws Exception if the string is in the wrong format
     */
    public ResidueStyle(String init) throws Exception {
    Vector<String> tokens = TextUtils.tokenize(init,"\t");
    if( tokens.size()!=8 ) 
        throw new Exception("Invalid string format: " + init);

    name = tokens.elementAt(0);    
    
    shape       = (tokens.elementAt(6).equals("none") || tokens.elementAt(1).equals("-")) ?null :tokens.elementAt(1);
    shape_color = parseColor(tokens.elementAt(2));
    
    fill_style    = tokens.elementAt(3);
    fill_negative = (tokens.elementAt(4).equals("yes") || tokens.elementAt(4).equals("true"));
    fill_color    = parseColor(tokens.elementAt(5));
    
    text       = (tokens.elementAt(6).equals("none") || tokens.elementAt(6).equals("-")) ?null : tokens.elementAt(6);
    text_color = parseColor(tokens.elementAt(7));
    }


    private static Color parseColor(String init) {
    Vector<String> tokens = TextUtils.tokenize(init,",");
    if( tokens.size()!=3 ) 
        return Color.black;
    
    int r = Integer.parseInt(tokens.elementAt(0));
    int g = Integer.parseInt(tokens.elementAt(1));
    int b = Integer.parseInt(tokens.elementAt(2));

    return new Color(r,g,b);
    }

    /**
       Return the identifier of the style
     */
    public String getName() {
    return name;
    }
    
    /**
       Return the identifier of the shape that is used to draw the
       contour of the residue. Valid identifiers are: "point",
       "square", "circle", "diamond", "rhombus", "star", "sixstar",
       "sevenstar", "pentagon", "hexagon", "heptagon", "triangle",
       "hatdiamond", "rhatdiamond", "bracket", "startrep", "endrep",
       "acleavage_?_?", "bcleavage", "ccleavage", "xcleavage_?_?",
       "ycleavage", "zcleavage", "end".
     */
    public String getShape() {
    return shape;
    }

    /**
       Return <code>true</code> if the residue will be represented as
       a geometrical shape.
     */
    public boolean hasShape() {
    return (shape!=null);
    }

    /**
       Return the color used to draw the contour of the residue.
     */
    public Color getShapeColor() {
    return shape_color;
    }

    /**
       Return the style of the fill used for the inside of the
       residue. The fill styles can be one of: "empty", "full",
       "left", "top", "right", "bottom", "topleft", "topright",
       "bottomright", "bottomleft", "circle", "checkered", "arc_?_?".
     */
    public String getFillStyle() {
    return fill_style;
    }

    /**
       Return <code>true</code> if the inside fill should be
       inverted
     */
    public boolean isFillNegative() {
    return fill_negative;
    }

    /**
       Return the color used to fill the inside of the residue.
     */
    public Color getFillColor() {
    return fill_color;
    }

    /**
       Return <code>true</code> if the residue will be represented
       only by a text.
     */
    public boolean isTextOnly() {
    return (shape==null && text!=null);
    }

    /**
       Return the text that should be written on the residue (can be
       <code>null</code>)
     */
    public String getText() {
    return text;
    }

    /**
       Get the color used to write text on the residue
     */
    public Color getTextColor() {
    return text_color;
    }

    /**
       Return the style to represent a residue as a specific text.
     */
    static public ResidueStyle createText(String text) {
    ResidueStyle ret = new ResidueStyle();
    ret.text = text;
    return ret;    
    }

    /**
       Return the style to represent a residue as the start of a
       repeat block.
     */
    static public ResidueStyle createStartRepetition() {
    ResidueStyle ret = new ResidueStyle();
    ret.shape = "startrep";
    ret.fill_style = "full";
    ret.fill_color = Color.black;
    return ret;    
    }

    /**
       Return the style to represent a residue as the end of a
       repeat block.
     */
    static public ResidueStyle createEndRepetition() {
    ResidueStyle ret = new ResidueStyle();
    ret.shape = "endrep";
    ret.fill_style = "full";
    ret.fill_color = Color.black;
    return ret;    
    }

    /**
       Return the style to represent a residue as an anchor point
     */
    static public ResidueStyle createAttachPoint() {
    ResidueStyle ret = new ResidueStyle();
    ret.shape = "point";
    return ret;
    
    }

    /**
       Return the style to represent a residue as a reducing end
       marker
    */
    static public ResidueStyle createReducingEnd() {
    ResidueStyle ret = new ResidueStyle();
    ret.shape = "end";
    return ret;
    }


    /**
       Return the style to represent a residue as a bracket to specify
       uncertain antennae
    */
    static public ResidueStyle createBracket() {
    ResidueStyle ret = new ResidueStyle();
    ret.shape = "bracket";
    return ret;
    }
      
    /**
       Return the style to represent a residue as a type A ring
       fragment
    */
    static public ResidueStyle createACleavage(int start, int end) {
    ResidueStyle ret = new ResidueStyle();
    ret.shape = "hexagon";
    ret.fill_style  = "arc_" + start + "_" + end;
    ret.fill_color  = Color.gray;
    return ret;
    }
    
    /**
       Return the style to represent a residue as a type B cleavage
    */
    static public ResidueStyle createBCleavage() {
    ResidueStyle ret = new ResidueStyle();
    ret.shape  = "bcleavage";
    return ret;
    }    
    
    /**
       Return the style to represent a residue as a type C cleavage
    */
    static public ResidueStyle createCCleavage() {
    ResidueStyle ret = new ResidueStyle();
    ret.shape  = "ccleavage";
    return ret;
    }    
   

    /**
       Return the style to represent a residue as a type X ring fragment
    */
    static public ResidueStyle createXCleavage(int start, int end) {
    ResidueStyle ret = new ResidueStyle();
    ret.shape = "hexagon";
    ret.fill_style  = "arc_" + start + "_" + end;
    ret.fill_color  = Color.gray;
    return ret;
    }

    /**
       Return the style to represent a residue as a type Y cleavage
    */
    static public ResidueStyle createYCleavage() {
    ResidueStyle ret = new ResidueStyle();
    ret.shape  = "ycleavage";
    return ret;
    }    

    /**
       Return the style to represent a residue as a type Z cleavage
    */
    static public ResidueStyle createZCleavage() {
    ResidueStyle ret = new ResidueStyle();
    ret.shape  = "zcleavage";
    return ret;
    }    

}
