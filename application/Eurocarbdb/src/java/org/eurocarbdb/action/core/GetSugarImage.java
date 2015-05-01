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
*   Last commit: $Rev: 1870 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-02-23 #$  
*/

package org.eurocarbdb.action.core;

//  stdlib imports
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.awt.image.BufferedImage;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.action.EurocarbAction;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;
//import com.opensymphony.xwork.ActionSupport;

/**
*
*   @author aceroni
*/
@org.eurocarbdb.action.ParameterChecking() // Don't know what to add to the white/blacklist
public class GetSugarImage extends EurocarbAction
{
    private static final long serialVersionUID = 1L;

    /** Logging handle. */
    private static final Logger log = Logger.getLogger( GetSugarImage.class );
    
    private boolean download = false;
    
    private int nGlycanSequenceId = -1;

    private String inputType = "gws";
    private String outputType = "svg";
    private String sequences = "Man";

    private String  notation    = null; //GraphicOptions.NOTATION_CFG;
    private String  display     = GraphicOptions.DISPLAY_NORMALINFO;
    private int     orientation = GraphicOptions.RL;
    private double  scale       = 1.;
    private boolean opaque      = true;

    private int tolerate_unknown = 0;
    private boolean showInfo   = true;
    private boolean showMasses = false;
    private boolean showRedend = false;

    private Integer margin_left = null;
    private Integer margin_top = null;
    private Integer margin_right = null;
    private Integer margin_bottom = null;

    private byte[] outputBuffer = new byte[0];
    private String lastError = "";
    private String lastErrorStack = "";
    
    public void setGlycanSequenceId(int nSeqId)
    {
        nGlycanSequenceId = nSeqId;
    }
    
    public int getGlycanSequenceId()
    {
        return nGlycanSequenceId;
    }
    
    public void setDownload(String s) 
    {
        download = Boolean.valueOf(s);
    }

    public String getDownload() 
    {
        return "" + download;
    }

    public static Map<String,String> getInputTypes() 
    {
        return GlycanParserFactory.getImportFormats(true);
    }

    public void setInputType(String s)
    {
        inputType = s;
    }
    
    public String getInputType()
    {
        return inputType;
    }

    public static Map<String,String> getStringOutputTypes() 
    {
        return GlycanParserFactory.getExportFormats();
    }

    public static Map<String,String> getImageOutputTypes() 
    {
        return SVGUtils.getExportFormats();
    }
    
    public void setOutputType(String s)
    {
        outputType = s;
    }
    
    public String getOutputType()
    {
        return outputType;
    }

    public void setSequences(String s) 
    {
        sequences = s;
    }

    public String getSequences() 
    {
        return sequences;
    }

    public void setNotation(String s) 
    {
        notation = s;
    }

    public String getNotation() 
    {
        return notation;
    }

    public void setDisplay(String s) 
    {
        display = s;
    }

    public String getDisplay() 
    {
        return display;
    }

    public void setOrientation(String s) 
    {
        orientation = Integer.parseInt(s);
    }

    public String getOrientation() 
    {
        return "" + orientation;
    }

    public void setScale(String s) 
    {
        scale = Double.parseDouble(s);
    }

    public String getScale() 
    {
        return "" + scale;
    }

    public void setTolerateUnknown(int i) 
    {
        tolerate_unknown = i;
    }

    public int getTolerateUnknown() 
    {
        return tolerate_unknown;
    }
    
    public void setOpaque(String s) 
    {
        opaque = Boolean.valueOf(s);
    }

    public String getOpaque() 
    {
        return "" + opaque;
    }

    public void setShowInfo(String s) 
    {
        showInfo = Boolean.valueOf(s);
    }

    public String getShowInfo() 
    {
        return "" + showInfo;
    }

    public void setShowMasses(String s) 
    {
        showMasses = Boolean.valueOf(s);
    }

    public String getShowMasses() 
    {
        return "" + showMasses;
    }

    public void setShowRedend(String s) 
    {
        showRedend = Boolean.valueOf(s);
    }

    public String getShowRedend() 
    {
        return "" + showRedend;
    }
    
    public void setMarginLeft(Integer i) {
    // System.out.println("Setting left margin " + i);
    margin_left = i;
    }

    public Integer getMarginLeft() {
    return margin_left;
    }

    public void setMarginTop(Integer i) {
    margin_top = i;
    }

    public Integer getMarginTop() {
    return margin_top;
    }

    public void setMarginRight(Integer i) {
    margin_right = i;
    }

    public Integer getMarginRight() {
    return margin_right;
    }

    public void setMarginBottom(Integer i) {
    margin_bottom = i;
    }

    public Integer getMarginBottom() {
    return margin_bottom;
    }

    public String getLastError() 
    {
        return lastError;
    }

    public String getLastErrorStack() 
    {
        return lastErrorStack;
    }

    // results

    public InputStream getStream() 
    {  
        return new ByteArrayInputStream(outputBuffer);
    }

    /**
     * @see com.opensymphony.xwork.ActionSupport#execute()
     */
    @Override
    public String execute() throws Exception
    {
        try
        {    
            LogUtils.clearLastError();
            lastError = "";
            lastErrorStack = "";
                    
            // set notation
            if( notation==null ) 
            {
                notation = getSugarImageNotation();
                if( notation == null )
                    notation = GraphicOptions.NOTATION_CFG;
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( 
                    "Settings: "
                    + "glycanSequenceId: " + nGlycanSequenceId
                    + "; download: "       + download
                    + "; inputType: "      + inputType
                    + "; outputType: "     + outputType
                    + "; sequences: "      + sequences
                    + "; notation: "       + notation
                    + "; display: "        + display
                    + "; orientation: "    + orientation
                    + "; showInfo: "       + showInfo
                    + "; showMasses: "     + showMasses
                    + "; showRedend: "     + showRedend
                    + "; tolerate_unknown: " + tolerate_unknown
                );
            }
                    
            // load from database
            if( nGlycanSequenceId > 0 ) 
            {
                log.debug("retrieving from database");
                
                GlycanSequence glyseq = Eurocarb.lookup( GlycanSequence.class, getGlycanSequenceId() );
                
                if ( glyseq != null )  
                {
                    setSequences( glyseq.getSequenceGWS() );
                    setInputType("gws");
                }
            }
            
            // init singletons
            GlycanWorkspace theWorkspace = new GlycanWorkspace(null,false);        
            org.eurocarbdb.resourcesdb.util.Utils.setTemplateDataIfNotSet();
    
            GlycanDocument theDoc = theWorkspace.getStructures(); 
            GlycanRenderer theGlycanRenderer = theWorkspace.getGlycanRenderer();
    

            theWorkspace.getGraphicOptions().initCustomDisplay(display);
            theWorkspace.setNotation(notation);    
            theWorkspace.setDisplay(GraphicOptions.DISPLAY_CUSTOM);

            theWorkspace.getGraphicOptions().ORIENTATION = orientation;
            //theWorkspace.getGraphicOptions().SHOW_INFO = showInfo;
            theWorkspace.getGraphicOptions().SHOW_INFO_CUSTOM = showInfo;
            theWorkspace.getGraphicOptions().SHOW_MASSES = showMasses;
            theWorkspace.getGraphicOptions().SHOW_REDEND = showRedend;           
            
            if( margin_left != null ) 
                theWorkspace.getGraphicOptions().MARGIN_LEFT_CUSTOM = margin_left;        
            if( margin_top != null ) 
                theWorkspace.getGraphicOptions().MARGIN_TOP_CUSTOM = margin_top;
            if( margin_right != null ) 
                theWorkspace.getGraphicOptions().MARGIN_RIGHT_CUSTOM = margin_right;
            if( margin_bottom != null ) 
                theWorkspace.getGraphicOptions().MARGIN_BOTTOM_CUSTOM = margin_bottom;

            if( LogUtils.getLastError().length()> 0 ) 
            {
                lastError = LogUtils.getLastError();
                lastErrorStack = LogUtils.getLastErrorStack();
                log.warn("error: " + lastError);
                log.warn("stack: " + lastErrorStack);
                log.debug("Error");
                return "error";
            }
    
            log.trace("attempting to parse input sequence");
    
            // parse structures
            if( tolerate_unknown!=0 ) 
            {
                /*boolean open = false;
                try 
                {
                    // try strict
                    theDoc.importFromString(sequences,inputType);
                    open = true;
                }
                catch(Exception e) {}
                
                if( !open ) 
                {
                    // try again, tolerate and output text
                    theDoc.importFromString(sequences,inputType,true);
                    theWorkspace.setNotation(GraphicOptions.NOTATION_TEXT);    
            }*/
                theDoc.importFromString(sequences,inputType,true);                 
            }
            else
                theDoc.importFromString(sequences,inputType);
    
            if( LogUtils.getLastError().length()> 0 ) 
            {
                lastError = LogUtils.getLastError();
                lastErrorStack = LogUtils.getLastErrorStack();
                log.warn("error: " + lastError);
                log.warn("stack: " + lastErrorStack);
                log.debug("Error");
                return "error";
            }
    
            log.trace("finished parsing input sequence");
    
            // output file
            String ret = "";
            if ( GlycanParserFactory.isSequenceFormat(outputType) ){
                outputBuffer = theDoc.toString(outputType).getBytes();
                log.debug("Rendering image");
            }else 
            {
                // System.out.println("left margin " + theWorkspace.getGraphicOptions().MARGIN_LEFT );     
                outputBuffer = SVGUtils.export( 
                    theGlycanRenderer,
                    theDoc.getStructures(),
                    showMasses,
                    showRedend,
                    scale,
                    outputType
                );
                log.debug("Rendering image");
            }
    
            if( LogUtils.getLastError().length()> 0 ) 
            {
                lastError = LogUtils.getLastError();
                lastErrorStack = LogUtils.getLastErrorStack();
                log.warn("error: " + lastError);
                log.warn("stack: " + lastErrorStack);
                log.debug("Error");
                return "error";
            }
    
            log.trace("temporary structure image file created");
    
            // return
    
            ret = "success_" + outputType;
            if ( download )
                ret = ret + "_download";
    
            log.debug("returning " + ret);
            return ret;
        } 
        catch ( Exception e )
        {
            lastError = LogUtils.getError(e);
            lastErrorStack = LogUtils.getErrorStack(e);        
            log.warn("error: " + lastError);
            log.warn("stack: " + lastErrorStack);
            return "error";
        }
        
    } // end execute()
    
}
