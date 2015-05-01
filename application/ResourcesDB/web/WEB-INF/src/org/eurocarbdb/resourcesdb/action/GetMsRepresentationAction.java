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
package org.eurocarbdb.resourcesdb.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eurocarbdb.resourcesdb.*;
import org.eurocarbdb.resourcesdb.io.*;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.representation.*;

/**
* Get a monosaccharide representation from the database.
* The associated action needs either a representation id as a parameter or a monosaccharide id, a representation type and a representation format.
* If a representation id is given, the other parameters are ignored.
* 
* @author thomas
*
*/
public class GetMsRepresentationAction extends MsdbDefaultAction {

    private static final long serialVersionUID = 1L;
    
    public static final String SUCCESS_PNG = "success_png";
    public static final String SUCCESS_JPG = "success_jpg";
    public static final String SUCCESS_SVG = "success_svg";
    public static final String SUCCESS_PDB = "success_pdb";
    public static final String SUCCESS_CHEMCOMP = "success_chemcomp";
    public static final String SUCCESS_MOL2 = "success_mol2";
    
    private int monosaccId = 0;
    private String monosaccName = null;
    private String namescheme = null;
    private String repType = null;
    private String repFormat = null;
    private int representationId = 0;
    private ResidueRepresentation monoRep = null;
    private boolean preserveOrientation = false;
    
    public int getMonosaccId() {
        return this.monosaccId;
    }
    
    public void setMonosaccId(int msId) {
        this.monosaccId = msId;
    }
    
    public String getMonosaccName() {
        return monosaccName;
    }

    public void setMonosaccName(String monosaccName) {
        this.monosaccName = monosaccName;
    }

    public String getNamescheme() {
        return namescheme;
    }

    public void setNamescheme(String namescheme) {
        this.namescheme = namescheme;
    }

    public String getRepFormat() {
        return this.repFormat;
    }
    
    public void setRepFormat(String repFormatStr) {
        this.repFormat = repFormatStr;
    }
    
    public int getRepresentationId() {
        return this.representationId;
    }
    
    public void setRepresentationId(int repId) {
        this.representationId = repId;
    }
    
    public String getRepType() {
        return this.repType;
    }
    
    public void setRepType(String repTypeStr) {
        this.repType = repTypeStr;
    }
    
    public boolean isPreserveOrientation() {
        return preserveOrientation;
    }

    public void setPreserveOrientation(boolean flag) {
        this.preserveOrientation = flag;
    }

    public InputStream getPngStream() {
        ByteArrayInputStream outStream = new ByteArrayInputStream(this.monoRep.getData());
        return outStream;
    }
    
    public InputStream getJpgStream() {
        ByteArrayInputStream outStream = new ByteArrayInputStream(this.monoRep.getData());
        return outStream;
    }
    
    public InputStream getSvgStream() {
        ByteArrayInputStream outStream = new ByteArrayInputStream(this.monoRep.getData());
        return outStream;
    }
    
    public InputStream getChemCompStream() {
        ByteArrayInputStream outStream = new ByteArrayInputStream(this.monoRep.getData());
        return outStream;
    }
    
    public InputStream getPdbStream() {
        ByteArrayInputStream outStream = new ByteArrayInputStream(this.monoRep.getData());
        return outStream;
    }
    
    public InputStream getMol2Stream() {
        ByteArrayInputStream outStream = new ByteArrayInputStream(this.monoRep.getData());
        return outStream;
    }
    
    public ResidueRepresentation buildRepresentation(Monosaccharide ms, ResidueRepresentationType type, ResidueRepresentationFormat format) {
        ResidueRepresentation repr = new ResidueRepresentation(type, format);
        try {
            if(type.equals(ResidueRepresentationType.HAWORTH)) {
                Haworth h = new Haworth();
                h.drawMonosaccharide(ms);
                if(format.equals(ResidueRepresentationFormat.SVG)) {
                    repr.setData(h.getSvgByteArr());
                } else if(format.equals(ResidueRepresentationFormat.PNG)) {
                    repr.setData(h.createPngImage());
                } else if(format.equals(ResidueRepresentationFormat.JPG)) {
                    repr.setData(h.createJpgImage());
                } else {
                    //this.setErrorMsg("Cannot create Haworth representation in " + this.getRepFormat() + " format.");
                    return null;
                }
            } else if(type.equals(ResidueRepresentationType.FISCHER)) {
                Fischer f = new Fischer();
                f.drawMonosaccharide(ms);
                if(format.equals(ResidueRepresentationFormat.SVG)) {
                    repr.setData(f.getSvgByteArr());
                } else if(format.equals(ResidueRepresentationFormat.PNG)) {
                    repr.setData(f.createPngImage());
                } else if(format.equals(ResidueRepresentationFormat.JPG)) {
                    repr.setData(f.createJpgImage());
                } else {
                    //this.setErrorMsg("Cannot create Fischer representation in " + this.getRepFormat() + " format.");
                    return null;
                }
            } else {
                //this.setErrorMsg("Representation type not supported by rep. builder: " + this.getRepType());
                return null;
            }
        } catch(Exception ex) {
            return null;
        }
        return repr;
    }
    
    public String execute() {
        if(this.getRepresentationId() != 0) {
            //*** get representation by its database id: ***
            this.monoRep = HibernateAccess.getMonosaccharideRepresentation(this.getRepresentationId());
        } else if(this.getMonosaccId() != 0 && this.getRepFormat() != null && this.getRepType() != null) {
            //*** get representation by monosaccharide id, type and format: ***
            this.monoRep = HibernateAccess.getMonosaccharideRepresentation(this.getMonosaccId(), this.getRepFormat(), this.getRepType());
        } else if(this.getMonosaccName() != null && this.getRepFormat() != null && this.getRepType() != null) {
            try {
                Config conf = this.getConfig();
                conf.setPreserveAlditolOrientation(this.isPreserveOrientation());
                MonosaccharideConverter converter = new MonosaccharideConverter(conf);
                GlycanNamescheme scheme = GlycanNamescheme.getGlycanNameschemeByNamestr(this.getNamescheme());
                if(scheme == null) {
                    scheme = GlycanNamescheme.AUTO;
                }
                Monosaccharide ms = converter.parseMsNamestr(this.getMonosaccName(), scheme);
                ResidueRepresentationType representationType = ResidueRepresentationType.forName(this.getRepType());
                if(representationType == null) {
                    this.setErrorMsg("Unknown residue representation type: " + this.getRepType());
                    return ERROR;
                }
                ResidueRepresentationFormat representationFormat = ResidueRepresentationFormat.forName(this.getRepFormat());
                if(representationFormat == null) {
                    this.setErrorMsg("Unknown residue representation format: " + this.getRepFormat());
                    return ERROR;
                }
                this.monoRep = this.buildRepresentation(ms, representationType, representationFormat);
            } catch(Exception me) {
                this.setCaughtException(me);
                this.setErrorMsg(me.getMessage());
                return ERROR;
            }
        }
        if(this.monoRep != null && this.monoRep.getFormat() != null) {
            if(this.monoRep.getFormat().equals(ResidueRepresentationFormat.PNG)) {
                return SUCCESS_PNG;
            }
            if(this.monoRep.getFormat().equals(ResidueRepresentationFormat.JPG)) {
                return SUCCESS_JPG;
            }
            if(this.monoRep.getFormat().equals(ResidueRepresentationFormat.SVG)) {
                return SUCCESS_SVG;
            }
            if(this.monoRep.getFormat().equals(ResidueRepresentationFormat.PDB)) {
                return SUCCESS_PDB;
            }
            if(this.monoRep.getFormat().equals(ResidueRepresentationFormat.CHEM_COMP)) {
                return SUCCESS_CHEMCOMP;
            }
            if(this.monoRep.getFormat().equals(ResidueRepresentationFormat.MOL2)) {
                return SUCCESS_MOL2;
            }
        }
        this.setErrorMsg("Could not get/build the requested representation.");
        return ERROR;
    }
}
