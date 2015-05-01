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

import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Color;

public class AnnotationReportOptions {

    public static final int DRAW_X_MARGIN_DEFAULT = 20;
    public int DRAW_X_MARGIN = DRAW_X_MARGIN_DEFAULT;
  
    public static final int DRAW_Y_MARGIN_DEFAULT = 20;
    public int DRAW_Y_MARGIN = DRAW_Y_MARGIN_DEFAULT;
  
    public static final int CHART_WIDTH_DEFAULT = 700;
    public int CHART_WIDTH = CHART_WIDTH_DEFAULT;
    public int CHART_WIDTH_NONSCALED = CHART_WIDTH_DEFAULT;

    public static final int CHART_HEIGHT_DEFAULT = 400;
    public int CHART_HEIGHT = CHART_HEIGHT_DEFAULT;
    public int CHART_HEIGHT_NONSCALED = CHART_HEIGHT_DEFAULT;

    public static final int CHART_X_MARGIN_DEFAULT = 20;
    public int CHART_X_MARGIN = CHART_X_MARGIN_DEFAULT;
    public int CHART_X_MARGIN_NONSCALED = CHART_X_MARGIN_DEFAULT;

    public static final int CHART_Y_MARGIN_DEFAULT = 20;
    public int CHART_Y_MARGIN = CHART_Y_MARGIN_DEFAULT;
    public int CHART_Y_MARGIN_NONSCALED = CHART_Y_MARGIN_DEFAULT;

    public static final int ANNOTATION_MARGIN_DEFAULT = 10;
    public int ANNOTATION_MARGIN = ANNOTATION_MARGIN_DEFAULT;
    public int ANNOTATION_MARGIN_NONSCALED = ANNOTATION_MARGIN_DEFAULT;

    public static final int ANNOTATION_MZ_SIZE_DEFAULT = 10;
    public int ANNOTATION_MZ_SIZE = ANNOTATION_MZ_SIZE_DEFAULT;
    public int ANNOTATION_MZ_SIZE_NONSCALED = ANNOTATION_MZ_SIZE_DEFAULT;

    public static final double ANNOTATION_LINE_WIDTH_DEFAULT = 1.;
    public double ANNOTATION_LINE_WIDTH = ANNOTATION_LINE_WIDTH_DEFAULT;
  
    public static final int ANNOTATION_LINE_MINY_DEFAULT = 12;
    public int ANNOTATION_LINE_MINY = ANNOTATION_LINE_MINY_DEFAULT;
    public int ANNOTATION_LINE_MINY_NONSCALED = ANNOTATION_LINE_MINY_DEFAULT;
    
    public static final String ANNOTATION_MZ_FONT_DEFAULT = "SansSerif.plain";
    public String ANNOTATION_MZ_FONT = ANNOTATION_MZ_FONT_DEFAULT;

    public static final double SCALE_GLYCANS_DEFAULT = 0.35;
    public double SCALE_GLYCANS = SCALE_GLYCANS_DEFAULT;
    public double SCALE_GLYCANS_NONSCALED = SCALE_GLYCANS_DEFAULT;

    public static final Color SPECTRUM_COLOR_DEFAULT = Color.black;
    public Color SPECTRUM_COLOR = SPECTRUM_COLOR_DEFAULT;

    public static final Color MASS_TEXT_COLOR_DEFAULT = Color.black;
    public Color MASS_TEXT_COLOR = MASS_TEXT_COLOR_DEFAULT;

    public static final Color CONNECTION_LINES_COLOR_DEFAULT = Color.lightGray;
    public Color CONNECTION_LINES_COLOR = CONNECTION_LINES_COLOR_DEFAULT;
    
    public static final Color HIGHLIGHTED_COLOR_DEFAULT = Color.yellow;
    public Color HIGHLIGHTED_COLOR = HIGHLIGHTED_COLOR_DEFAULT;

    public double SCALE = 1.;    

    // document creation options
    
    public boolean SHOW_RAW_SPECTRUM = true;
    public boolean SHOW_RELATIVE_INTENSITIES = true; 
    public boolean SHOW_EMPTY_ANNOTATIONS = false; 
    public boolean SHOW_MAX_INTENSITY = true;

    public void setScale(double scale) {
    if( scale>0. )
        SCALE = scale;

    CHART_WIDTH = (int)(CHART_WIDTH_NONSCALED * SCALE);
    CHART_HEIGHT = (int)(CHART_HEIGHT_NONSCALED * SCALE);
    CHART_X_MARGIN = (int)(CHART_X_MARGIN_NONSCALED * SCALE);
    CHART_Y_MARGIN = (int)(CHART_Y_MARGIN_NONSCALED * SCALE);
    ANNOTATION_MARGIN = (int)(ANNOTATION_MARGIN_NONSCALED * SCALE);
    ANNOTATION_LINE_MINY = (int)(ANNOTATION_LINE_MINY_NONSCALED * SCALE);
    ANNOTATION_MZ_SIZE = (int)(ANNOTATION_MZ_SIZE_NONSCALED * SCALE); 
    SCALE_GLYCANS = SCALE_GLYCANS_NONSCALED * SCALE;    
    }

    public Dimension getDefaultViewDimension() {
    return new Dimension(CHART_WIDTH + 2*DRAW_X_MARGIN + 2*CHART_X_MARGIN, CHART_HEIGHT + 2*DRAW_Y_MARGIN + 2*CHART_Y_MARGIN);
    }

    public Dimension getViewDimension(Dimension draw) {
    return new Dimension(draw.width + 2*DRAW_X_MARGIN, draw.height + 2*DRAW_Y_MARGIN);
    }

    public Rectangle getDefaultDrawArea() {
    return new Rectangle(DRAW_X_MARGIN,DRAW_Y_MARGIN,CHART_WIDTH + 2*CHART_X_MARGIN,CHART_HEIGHT+ 2*CHART_Y_MARGIN);
    }

    public Rectangle getDefaultChartArea() {
    return new Rectangle(DRAW_X_MARGIN + CHART_X_MARGIN, DRAW_Y_MARGIN + CHART_Y_MARGIN, CHART_WIDTH , CHART_HEIGHT);
    }

    public void setValues(AnnotationReportOptions other) {
    Configuration config = new Configuration();
    other.store(config);
    this.retrieve(config);
    }

    public void store(Configuration config) {

    config.put("AnnotationReportOptions","draw_x_margin",DRAW_X_MARGIN);
    config.put("AnnotationReportOptions","draw_y_margin",DRAW_Y_MARGIN);

    config.put("AnnotationReportOptions","chart_width",CHART_WIDTH_NONSCALED);
    config.put("AnnotationReportOptions","chart_height",CHART_HEIGHT_NONSCALED);
    config.put("AnnotationReportOptions","chart_x_margin",CHART_X_MARGIN_NONSCALED);
    config.put("AnnotationReportOptions","chart_y_margin",CHART_Y_MARGIN_NONSCALED);

    config.put("AnnotationReportOptions","annotation_margin",ANNOTATION_MARGIN_NONSCALED);
    config.put("AnnotationReportOptions","annotation_mz_size",ANNOTATION_MZ_SIZE_NONSCALED);
    config.put("AnnotationReportOptions","annotation_line_width",ANNOTATION_LINE_WIDTH);    
    config.put("AnnotationReportOptions","annotation_line_miny",ANNOTATION_LINE_MINY_NONSCALED);    
    config.put("AnnotationReportOptions","annotation_mz_font",ANNOTATION_MZ_FONT);

    config.put("AnnotationReportOptions","scale_glycans",SCALE_GLYCANS_NONSCALED);    

    config.put("AnnotationReportOptions","show_raw_spectrum",SHOW_RAW_SPECTRUM);    
    config.put("AnnotationReportOptions","show_relative_intensities",SHOW_RELATIVE_INTENSITIES);    
    config.put("AnnotationReportOptions","show_empty_annotations",SHOW_EMPTY_ANNOTATIONS);    
    config.put("AnnotationReportOptions","show_max_intensity",SHOW_MAX_INTENSITY);    
 
    config.put("AnnotationReportOptions","spectrum_color",SPECTRUM_COLOR);    
    config.put("AnnotationReportOptions","mass_text_color",MASS_TEXT_COLOR);    
    config.put("AnnotationReportOptions","connection_lines_color",CONNECTION_LINES_COLOR);    
    config.put("AnnotationReportOptions","highlighted_color",HIGHLIGHTED_COLOR);    
 
    }

    public void retrieve(Configuration config) {

    DRAW_X_MARGIN = config.get("AnnotationReportOptions","draw_x_margin",DRAW_X_MARGIN);
    DRAW_Y_MARGIN = config.get("AnnotationReportOptions","draw_y_margin",DRAW_Y_MARGIN);

    CHART_WIDTH_NONSCALED = config.get("AnnotationReportOptions","chart_width",CHART_WIDTH_NONSCALED);
    CHART_HEIGHT_NONSCALED = config.get("AnnotationReportOptions","chart_height",CHART_HEIGHT_NONSCALED);
    CHART_X_MARGIN_NONSCALED = config.get("AnnotationReportOptions","chart_x_margin",CHART_X_MARGIN_NONSCALED);
    CHART_Y_MARGIN_NONSCALED = config.get("AnnotationReportOptions","chart_y_margin",CHART_Y_MARGIN_NONSCALED);

    ANNOTATION_MARGIN_NONSCALED = config.get("AnnotationReportOptions","annotation_margin",ANNOTATION_MARGIN_NONSCALED);
    ANNOTATION_MZ_SIZE_NONSCALED = config.get("AnnotationReportOptions","annotation_mz_size",ANNOTATION_MZ_SIZE_NONSCALED);
    ANNOTATION_LINE_WIDTH = config.get("AnnotationReportOptions","annotation_line_width",ANNOTATION_LINE_WIDTH);    
    ANNOTATION_LINE_MINY_NONSCALED = config.get("AnnotationReportOptions","annotation_line_miny",ANNOTATION_LINE_MINY_NONSCALED);    
    ANNOTATION_MZ_FONT = config.get("AnnotationReportOptions","annotation_mz_font",ANNOTATION_MZ_FONT);

    SCALE_GLYCANS_NONSCALED = config.get("AnnotationReportOptions","scale_glycans",SCALE_GLYCANS_NONSCALED);    

    SHOW_RAW_SPECTRUM = config.get("AnnotationReportOptions","show_raw_spectrum",SHOW_RAW_SPECTRUM);    
    SHOW_RELATIVE_INTENSITIES = config.get("AnnotationReportOptions","show_relative_intensities",SHOW_RELATIVE_INTENSITIES);
    SHOW_EMPTY_ANNOTATIONS = config.get("AnnotationReportOptions","show_empty_annotations",SHOW_EMPTY_ANNOTATIONS);        
    SHOW_MAX_INTENSITY = config.get("AnnotationReportOptions","show_max_intensity",SHOW_MAX_INTENSITY);        

    SPECTRUM_COLOR = config.get("AnnotationReportOptions","spectrum_color",SPECTRUM_COLOR);    
    MASS_TEXT_COLOR = config.get("AnnotationReportOptions","mass_text_color",MASS_TEXT_COLOR);    
    CONNECTION_LINES_COLOR = config.get("AnnotationReportOptions","connection_lines_color",CONNECTION_LINES_COLOR);    
    HIGHLIGHTED_COLOR = config.get("AnnotationReportOptions","highlighted_color",HIGHLIGHTED_COLOR);    
 
    }

}