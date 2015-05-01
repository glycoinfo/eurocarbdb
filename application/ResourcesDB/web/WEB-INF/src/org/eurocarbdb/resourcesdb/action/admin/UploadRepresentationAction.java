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
package org.eurocarbdb.resourcesdb.action.admin;

import java.io.File;
import java.io.FileInputStream;

import org.eurocarbdb.resourcesdb.io.*;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.nonmonosaccharide.*;
import org.eurocarbdb.resourcesdb.representation.*;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.action.EMenu;

public class UploadRepresentationAction extends MsdbAdminAction {

    private static final long serialVersionUID = 1L;
    
    public static final String ACTIONNAME = "upload_representation.action";
    
    public static final String INPUT1 = "input1";
    public static final String INPUT2 = "input2";
    
    public static final String MOLECULE_CLASS_MONOSACCHARIDE = "monosaccharide";
    public static final String MOLECULE_CLASS_SUBSTITUENT = "substituent";
    public static final String MOLECULE_CLASS_AGLYCON = "aglycon";
    
    private String moleculeClass = null;
    private String moleculeName = null;
    private int moleculeId = 0;
    
    private String type = null;
    private String format = null;
    private byte[] data = null;
    
    private File dataFile = null;
    private String dataFileFileName = null;
    private String dataFileContentType = null;
    
    private Monosaccharide ms = null;
    private SubstituentTemplate subst = null;
    private AglyconTemplate aglycon = null;
    
    private ResidueRepresentation representation = null;
    
    public String getMoleculeClass() {
        return this.moleculeClass;
    }

    public void setMoleculeClass(String classStr) {
        this.moleculeClass = classStr;
    }

    public int getMoleculeId() {
        return this.moleculeId;
    }

    public void setMoleculeId(int molId) {
        this.moleculeId = molId;
    }

    public String getMoleculeName() {
        return this.moleculeName;
    }

    public void setMoleculeName(String nameStr) {
        this.moleculeName = nameStr;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] dataArr) {
        this.data = dataArr;
    }
    
    public void setDataFile(File inFile) {
        this.dataFile = inFile;
    }
    
    public File getDataFile() {
        return this.dataFile;
    }
    
    public void setDataFileContentType(String contentType) {
        this.dataFileContentType = contentType;
    }
    
    public String getDataFileContentType() {
        return this.dataFileContentType;
    }
    
    public void setDataFileName(String fileName) {
        this.dataFileFileName = fileName;
    }
    
    public String getDataFileName() {
        return this.dataFileFileName;
    }
    
    public String getFormat() {
        return this.format;
    }

    public void setFormat(String formatStr) {
        this.format = formatStr;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String typeStr) {
        this.type = typeStr;
    }

    public Monosaccharide getMs() {
        return this.ms;
    }

    public void setMs(Monosaccharide mono) {
        this.ms = mono;
    }

    public AglyconTemplate getAglycon() {
        return aglycon;
    }

    public void setAglycon(AglyconTemplate aglycon) {
        this.aglycon = aglycon;
    }

    public ResidueRepresentation getRepresentation() {
        return representation;
    }

    public void setRepresentation(ResidueRepresentation representation) {
        this.representation = representation;
    }

    public SubstituentTemplate getSubst() {
        return subst;
    }

    public void setSubst(SubstituentTemplate subst) {
        this.subst = subst;
    }
    
    private byte[] getFileContent(File inFile) {
        byte[] byteArr = null;
        try {
            FileInputStream inputStream = new FileInputStream(inFile);
            byteArr = new byte[(int) inFile.length()];
            inputStream.read(byteArr);
        } catch(Exception e) {
            System.err.println("Exception in getFileContent(): " + e);
            e.printStackTrace();
        }
        return byteArr;
    }

    public String execute() throws Exception {
        this.setMainMenuItems();
        this.setSubMenuItems(EMenu.QUERY);
        this.setCurrentSubMenuItem(EMenu.QUERY_MONOSACC);
        if(this.getMoleculeClass() == null || (this.getMoleculeId() == 0 && this.getMoleculeName() == null)) {
            return INPUT1;
        }
        if(this.getMoleculeClass().equals(MOLECULE_CLASS_MONOSACCHARIDE)) {
            if(this.getMoleculeId() != 0) {
                this.ms = HibernateAccess.getMonosaccharideFromDB(this.getMoleculeId());
                if(this.ms == null) {
                    this.setErrorMsg("Monosaccharide " + this.getMoleculeId() + " not found in DB.");
                    return INPUT1;
                }
            } else {
                this.ms = HibernateAccess.getMonosaccharideFromDB(this.getMoleculeName());
                if(this.ms == null) {
                    this.setErrorMsg("Monosaccharide " + this.getMoleculeName() + " not found in DB.");
                    return INPUT1;
                }
            }
        } else if(this.getMoleculeClass().equals(MOLECULE_CLASS_SUBSTITUENT)) {
            
        } else if(this.getMoleculeClass().equals(MOLECULE_CLASS_AGLYCON)) {
            
        }
        if(this.getDataFile() != null) {
            this.setData(this.getFileContent(this.getDataFile()));
        }
        if(this.getData() == null || this.getData().length == 0 || this.getFormat() == null || this.getType() == null) {
            return INPUT2;
        }
        ResidueRepresentationType reprType = ResidueRepresentationType.forName(this.getType());
        ResidueRepresentationFormat reprFormat = ResidueRepresentationFormat.forName(this.getFormat());
        ResidueRepresentation repr = new ResidueRepresentation(reprType, reprFormat);
        if(!repr.checkFormatAndTypeConsistency()) {
            this.setErrorMsg(this.getType() + " representation needs " + reprType.getFormatType() + " format, " + this.getFormat() + " is a " + reprFormat.getFormatType() + " format.");
            return INPUT2;
        }
        repr.setData(this.getData());
        if(this.getMoleculeClass().equals(MOLECULE_CLASS_MONOSACCHARIDE)) {
            this.getMs().addRepresentation(repr);
            HibernateAccess.storeOrUpdateMonosaccharideRepresentation(repr);
            this.setRepresentation(repr);
        }
        return SUCCESS;
    }    
    
}
