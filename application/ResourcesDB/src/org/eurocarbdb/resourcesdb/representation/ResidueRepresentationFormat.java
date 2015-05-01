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

public enum ResidueRepresentationFormat {
    
    GIF("gif", ResidueRepresentationFormat.FORMAT_TYPE_GRAPHICS, false, true, "gif image format"),
    JPG("jpg", ResidueRepresentationFormat.FORMAT_TYPE_GRAPHICS, false, true, "jpg image format"),
    SVG("svg", ResidueRepresentationFormat.FORMAT_TYPE_GRAPHICS, true, false, "scalable vector graphics format"),
    PNG("png", ResidueRepresentationFormat.FORMAT_TYPE_GRAPHICS, false, true, "png image format"),
    PDB("pdb", ResidueRepresentationFormat.FORMAT_TYPE_COORDINATES, false, false, "pdb structure file format"),
    CHEM_COMP("chem_comp", ResidueRepresentationFormat.FORMAT_TYPE_COORDINATES, false, false, "CCPN ChemComp file format"),
    MOL2("mol2", ResidueRepresentationFormat.FORMAT_TYPE_COORDINATES, false, false, "mol2 structure file format");
    
    private String formatName;
    private String formatType;
    private String description;
    private boolean scalable;
    private boolean binary;
    
    public static final String FORMAT_TYPE_GRAPHICS = "graphic";
    public static final String FORMAT_TYPE_COORDINATES = "3d structure";
    
    private ResidueRepresentationFormat(String name, String type, boolean isScalable, boolean isBinary, String desc) {
        this.setFormatName(name);
        this.setFormatType(type);
        this.setDescription(desc);
        this.setScalable(isScalable);
        this.setBinary(isBinary);
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getFormatName() {
        return this.formatName;
    }

    private void setFormatName(String name) {
        this.formatName = name;
    }

    public String getFormatType() {
        return this.formatType;
    }

    private void setFormatType(String type) {
        this.formatType = type;
    }

    public boolean isScalable() {
        return this.scalable;
    }

    private void setScalable(boolean isScalable) {
        this.scalable = isScalable;
    }
    
    public boolean isBinary() {
        return this.binary;
    }

    public void setBinary(boolean flag) {
        this.binary = flag;
    }

    public static ResidueRepresentationFormat forName(String nameStr) {
        for(ResidueRepresentationFormat rrf : ResidueRepresentationFormat.values()) {
            if(rrf.getFormatName().equalsIgnoreCase(nameStr)) {
                return rrf;
            }
        }
        return null;
    }
}
