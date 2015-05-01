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
*   Last commit: $Rev: 1922 $ by $Author: khaleefah $ on $Date:: 2010-06-18 #$  
*/
package org.eurocarbdb.dataaccess.ms;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
 *  eurocarb_devel.ScanImage
 *  06/03/2010 20:22:49
 * 
 */
public class ScanImage implements Serializable{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( ScanImage.class );

    private Integer scanImageId;
    private Scan scan;
    private byte[] fullSize;
    private byte[] mediumSize;
    private byte[] thumbnail;
    private String fileName;
    private byte[] annotationReport;

    public ScanImage() {
    }

    public ScanImage(Integer scanImageId, String fileName) {
        this.scanImageId = scanImageId;
        this.fileName = fileName;
    }

    public ScanImage(Integer scanImageId, Scan scan, byte[] fullSize, byte[] mediumSize, byte[] thumbnail, String fileName, byte[] annotationReport) {
        this.scanImageId = scanImageId;
        this.scan = scan;
        this.fullSize = fullSize;
        this.mediumSize = mediumSize;
        this.thumbnail = thumbnail;
        this.fileName = fileName;
        this.annotationReport = annotationReport;
    }

    public Integer getScanImageId() {
        return scanImageId;
    }

    public void setScanImageId(Integer scanImageId) {
        this.scanImageId = scanImageId;
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public byte[] getFullSize() {
        return fullSize;
    }

    public void setFullSize(byte[] fullSize) {
        this.fullSize = fullSize;
    }

    public byte[] getMediumSize() {
        return mediumSize;
    }

    public void setMediumSize(byte[] mediumSize) {
        this.mediumSize = mediumSize;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getAnnotationReport() {
        return annotationReport;
    }

    public void setAnnotationReport(byte[] annotationReport) {
        this.annotationReport = annotationReport;
    }

}
