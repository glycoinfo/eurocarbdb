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
*   Last commit: $Rev: 1924 $ by $Author: khaleefah $ on $Date:: 2010-06-21 #$  
*/

package org.eurocarbdb.action.ms;

import org.eurocarbdb.action.*;
import org.eurocarbdb.action.exception.*;

import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;

import org.eurocarbdb.application.glycanbuilder.FileUtils;
import org.eurocarbdb.application.glycanbuilder.SVGUtils;
import org.eurocarbdb.application.glycoworkbench.plugin.reporting.AnnotationReportDocument;
import org.eurocarbdb.application.glycoworkbench.plugin.reporting.AnnotationReportCanvas;

import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.awt.*;
import java.awt.image.*;
import org.apache.log4j.Logger;

/**
* @author             aceroni
* @version            $Rev: 1924 $
*/

public class CreateScanImage extends EurocarbAction implements RequiresLogin, EditingAction {

    protected static final Logger log = Logger.getLogger( CreateScanImage.class.getName() );

    private File annotationReportFile = null;
    private String annotationReportFileContentType = null;
    private String annotationReportFileFilename = null;

    private File scanImageFile = null;
    private String scanImageFileContentType = null;
    private String scanImageFileFilename = null;

    private int scan_id = -1;   
    private Scan scan = null;

    public int getScanId() {
        return scan_id;
    }

    public void setScanId(int id) {
        this.scan_id = id;
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }
    
    public void setAnnotationReportFile(File file) {
    this.annotationReportFile = file;
    }

    public void setAnnotationReportFileContentType(String contentType) {
    this.annotationReportFileContentType = contentType;
    }

    public void setAnnotationReportFileFileName(String filename) {
    this.annotationReportFileFilename = filename;
    }

    public void setScanImageFile(File file) {
    this.scanImageFile = file;
    }

    public void setScanImageFileContentType(String contentType) {
    this.scanImageFileContentType = contentType;
    }

    public void setScanImageFileFileName(String filename) {
    this.scanImageFileFilename = filename;
    }

    /**
     * Check that the scan's parent acquisition is owned by the current
     * contributor
     */
    public void checkPermissions() throws InsufficientPermissions
    {
        if (! getScan().getAcquisition().getContributor().equals(Eurocarb.getCurrentContributor())) {
            throw new InsufficientPermissions(this,"Acquisition does not belong to logged in user");
        }
    }


    public String execute() throws Exception {
    
    if( submitAction.equals("Back") )
        return "back";       

    // retrieve saved scan
    if( scan==null && (scan_id<=0 || (scan = Eurocarb.getEntityManager().lookup( Scan.class, scan_id))==null) ) {
        this.addFieldError( "scan_id", "Invalid scan id " + scan_id );
        return "error";
    }
    else
        Eurocarb.getEntityManager().refresh(scan);  

    // upload image from file
    if( submitAction.equals("Upload") ) {
        if( annotationReportFile==null && scanImageFile==null )
        return "input";


        // create scan image
        ScanImage scanImage = new ScanImage();
        scanImage.setScan(scan);
        
        BufferedImage image = null;
        if( annotationReportFile!=null ) {
        try {
            // try to read annotation report file
            AnnotationReportDocument ard = new AnnotationReportDocument();
            byte[] data = FileUtils.binaryContent(annotationReportFile);            
            ard.fromXMLString(data);
                        
            image = SVGUtils.getImage(new AnnotationReportCanvas(ard,false));
            scanImage.setAnnotationReport(data);            
        }
        catch(Exception e) {
            this.addFieldError( "annotationReportFile", "Error while reading annotation report file: " + e.getMessage() );
            // System.out.println("Read annotation report");
            e.printStackTrace();            
            return "error";
        }
        }
        else {
        try {            
            // try to read image from file
            image = ImageIO.read(scanImageFile);
        }
        catch(Exception e) {
            this.addFieldError( "scanImageFile", "Error while reading image file: " + e.getMessage() );
            return "error";
        }
        }
        
        // set images
        scanImage.setFullSize(getImageBytes(image));
        scanImage.setMediumSize(getImageBytes(getScaledImage(image,600)));
        scanImage.setThumbnail(getImageBytes(getScaledImage(image,100)));

        
        // store scan image
        Eurocarb.getEntityManager().store(scanImage);  
        
        // update scan
 //       scan.setScanImage(scanImage);        
        Eurocarb.getEntityManager().update(scan);  
        
        return "finish";
    }

    // remove previous image
    if( submitAction.equals("Delete") ) {
/*        if( scan.getScanImage()!=null ) {
        Eurocarb.getEntityManager().remove(scan.getScanImage());  
        scan.setScanImage(null);
        Eurocarb.getEntityManager().update(scan);  
        }*/
        return "input";
    }

    // check if image is existing
/*    if( scan.getScanImage()!=null )
        return "exists";*/
    
    return "input";
    }         

    private BufferedImage getScaledImage(BufferedImage src, int new_width) {
        
    double sf = (double)new_width/(double)src.getWidth();
    int new_height = (int)(src.getHeight()*sf);
    
    BufferedImage ret = new BufferedImage(new_width,new_height, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics2D = ret.createGraphics();
    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics2D.drawImage(src, 0, 0, new_width, new_height, null);
    
    return ret;
    }

    static private byte[] getImageBytes(BufferedImage image) throws Exception {
    ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next(); 
    ImageWriteParam param = writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(1);   
        
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    writer.setOutput(new MemoryCacheImageOutputStream(bos));        
    writer.write(null,new IIOImage(image,null,null),param);

    return bos.toByteArray();            
    }
}


