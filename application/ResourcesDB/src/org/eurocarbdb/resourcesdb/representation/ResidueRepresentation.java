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


public class ResidueRepresentation {

    private ResidueRepresentationType type;
    private ResidueRepresentationFormat format;
    private int residueId;
    private int dbId;
    private int width;
    private int height;
    private boolean locked;
    private byte[] data;
    
    public ResidueRepresentation() {
        this.init();
    }
    
    public ResidueRepresentation(ResidueRepresentationType type, ResidueRepresentationFormat format) {
        this.init();
        this.setType(type);
        this.setFormat(format);
    }
    
    public byte[] getData() {
        return this.data;
    }
    
    public void setData(byte[] data) {
        this.data = data;
    }
    
    public void setData(String dataStr) {
        this.setData(dataStr.getBytes());
    }
    
    public int getDbId() {
        return this.dbId;
    }
    
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }
    
    public ResidueRepresentationFormat getFormat() {
        return this.format;
    }
    
    public void setFormat(ResidueRepresentationFormat repFormat) {
        this.format = repFormat;
    }
    
    public String getFormatStr() {
        if(this.getFormat() == null) {
            return null;
        }
        return this.getFormat().getFormatName();
    }
    
    public void setFormatStr(String formatName) {
        ResidueRepresentationFormat repFormat = ResidueRepresentationFormat.forName(formatName);
        this.setFormat(repFormat);
    }
    
    public boolean checkFormatAndTypeConsistency() {
        if(this.getFormat() != null && this.getType() != null) {
            if(this.getFormat().getFormatType().equals(this.getType().getFormatType())) {
                return true;
            }
        }
        return false;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getMonosaccharideId() {
        return this.residueId;
    }
    
    public void setMonosaccharideId(int monosaccharideId) {
        this.residueId = monosaccharideId;
    }
    
    public ResidueRepresentationType getType() {
        return this.type;
    }
    
    public void setType(ResidueRepresentationType type) {
        this.type = type;
    }
    
    public String getTypeStr() {
        if(this.getType() == null) {
            return null;
        }
        return this.getType().getTypeName();
    }
    
    public void setTypeStr(String typeStr) {
        this.setType(ResidueRepresentationType.forName(typeStr));
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setSize(int width, int height) {
        this.setWidth(width);
        this.setHeight(height);
    }
    
    public boolean isLocked() {
        return this.locked;
    }

    public boolean getLocked() {
        return this.locked;
    }

    public void setLocked(boolean lockedFlag) {
        this.locked = lockedFlag;
    }

    public void init() {
        this.setDbId(0);
        this.setMonosaccharideId(0);
        this.setData((byte[])null);
        this.setFormat(null);
        this.setType(null);
        this.setWidth(0);
        this.setHeight(0);
        this.setLocked(false);
    }
    
    public String toString() {
        String outStr = "";
        outStr += "ResidueRepresentation[type: " + this.getTypeStr() + " format: " + this.getFormatStr() + " width: " + this.getWidth() + " height: " + this.getHeight() + " data size: ";
        if(this.getData() == null) {
            outStr += "0";
        } else {
            outStr += this.getData().length;
        }
        outStr += "]";
        return outStr;
    }
}
