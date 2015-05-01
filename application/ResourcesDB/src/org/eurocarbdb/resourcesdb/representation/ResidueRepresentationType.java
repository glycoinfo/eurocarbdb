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


public enum ResidueRepresentationType {

    CFG_SYMBOL("cfg_symbol", ResidueRepresentationFormat.FORMAT_TYPE_GRAPHICS, "icon used in the sugar graphs of the CFG"),
    CFG_SYMBOL_BW("cfg_symbol_bw", ResidueRepresentationFormat.FORMAT_TYPE_GRAPHICS, "icon used in the sugar graphs of the CFG (grayscale version)"),
    OXFORD_SYMBOL("oxford_symbol", ResidueRepresentationFormat.FORMAT_TYPE_GRAPHICS, "icon used in the sugar graphs of the Oxford encoding scheme"),
    HAWORTH("haworth", ResidueRepresentationFormat.FORMAT_TYPE_GRAPHICS, "Haworth formula"),
    FISCHER("fischer", ResidueRepresentationFormat.FORMAT_TYPE_GRAPHICS, "Fischer formula"),
    COORDINATES("coordinates", ResidueRepresentationFormat.FORMAT_TYPE_COORDINATES, "3d structural coordinates");
    
    private String typeName;
    private String formatType;
    private String description;
    
    private ResidueRepresentationType(String nameStr, String formatTypeStr, String desc) {
        this.setTypeName(nameStr);
        this.setFormatType(formatTypeStr);
        this.setDescription(desc);
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String desc) {
        this.description = desc;
    }
    
    public String getTypeName() {
        return this.typeName;
    }
    
    private void setTypeName(String nameStr) {
        this.typeName = nameStr;
    }
    
    public String getFormatType() {
        return this.formatType;
    }

    private void setFormatType(String formatTypeStr) {
        this.formatType = formatTypeStr;
    }

    public static ResidueRepresentationType forName(String nameStr) {
        for(ResidueRepresentationType rrt : ResidueRepresentationType.values()) {
            if(rrt.getTypeName().equalsIgnoreCase(nameStr)) {
                return rrt;
            }
        }
        return null;
    }
}
