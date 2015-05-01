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
package org.eurocarbdb.resourcesdb.template;

import org.eurocarbdb.resourcesdb.monosaccharide.StereoConfiguration;

public class BasetypeTemplate {
    
    private String baseName;
    private String stereocode;
    private Boolean isSuperclass;
    private int size;
    private int carbonylPosition;
    private String longName;
    private StereoConfiguration defaultConfiguration;
    private int defaultRingend;
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public Boolean isSuperclass() {
        return isSuperclass;
    }

    public void setIsSuperclass(Boolean isSuperclass) {
        this.isSuperclass = isSuperclass;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getStereocode() {
        return stereocode;
    }

    public void setStereocode(String stereoCode) {
        this.stereocode = stereoCode;
    }

    /**
     * @return the carbonylPosition
     */
    public int getCarbonylPosition() {
        return carbonylPosition;
    }

    /**
     * @param carbonylPosition the carbonylPosition to set
     */
    public void setCarbonylPosition(int carbonylPosition) {
        this.carbonylPosition = carbonylPosition;
    }

    /**
     * @return the defaultConfiguration
     */
    public StereoConfiguration getDefaultConfiguration() {
        return defaultConfiguration;
    }

    /**
     * @param defaultConfiguration the defaultConfiguration to set
     */
    public void setDefaultConfiguration(StereoConfiguration defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
    }

    /**
     * @return the defaultRingend
     */
    public int getDefaultRingend() {
        return defaultRingend;
    }

    /**
     * @param defaultRingend the defaultRingend to set
     */
    public void setDefaultRingend(int defaultRingend) {
        this.defaultRingend = defaultRingend;
    }

    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public BasetypeTemplate() {
        init();
    }
    
    public BasetypeTemplate(String baseName, String longName, String stereocode, int size, boolean isSuperclass) {
        init();
        setBaseName(baseName);
        setLongName(longName);
        setStereocode(stereocode);
        setSize(size);
        setIsSuperclass(isSuperclass);
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        setBaseName("");
        setLongName("");
        setSize(0);
        setStereocode("");
        setDefaultConfiguration(StereoConfiguration.Unknown);
        setDefaultRingend(0);
        setIsSuperclass(false);
    }
    
    /**
     * Check, if this BasetypeTemplate actually is a TrivialnameTemplate, i.e. if it is an instance of the original or the extended class.
     * @return
     */
    public boolean isTrivialname() {
        if(this.getClass().equals(TrivialnameTemplate.class)) {
            return(true);
        }
        return(false);
    }
    
    public String toString() {
        String outStr = "";
        outStr += this.getBaseName();
        outStr += " [" + this.getSize() + "|" + this.getStereocode() + "|" + this.getDefaultConfiguration() + "|" + this.getDefaultRingend() + "]";
        return(outStr);
    }
}
