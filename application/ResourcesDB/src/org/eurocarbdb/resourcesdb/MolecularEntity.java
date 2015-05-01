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
package org.eurocarbdb.resourcesdb;

import java.util.ArrayList;
import java.util.List;

import org.eurocarbdb.resourcesdb.atom.*;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideException;
import org.eurocarbdb.resourcesdb.representation.*;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;

/**
* A class to store general properties that are common to all molecular entities within the ResourcesDB.
* Molecular entities can be e.g. residues, subresidues or residue templates.
* There should be no instances of this class but instead instances of classes that extend this one should be used.
* @author Thomas Luetteke
*
*/
public abstract class MolecularEntity extends ResourcesDbObject {
    private String name;
    private Composition composition;
    private Double monoMass;
    private Double avgMass;
    private String formula;
    private String smiles;
    private String inchi;
    private List<Atom> atoms;
    private Double charge;
    private List<ResidueRepresentation> representations;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /**
     * Constructor, initializes molecular entity
     */
    public MolecularEntity() {
        init();
    }
    
    public MolecularEntity(TemplateContainer container) {
        init();
        this.setTemplateContainer(container);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************

    /**
     * Get the name
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the element composition
     * @return the composition
     */
    public Composition getComposition() {
        return this.composition;
    }

    /**
     * Set the element composition
     * @param composition the composition to set
     */
    public void setComposition(Composition composition) {
        this.composition = composition;
    }

    /**
     * Get the average mass (Double Object)
     * If the average mass is not set yet and a composition is present the average mass is calculated from the composition
     * @return the average mass as a Double object
     */
    public Double getAvgMass() {
        if(this.avgMass == null && this.getComposition() != null) {
            this.setAvgMass(this.getComposition().getAvgMass());
        }
        return this.avgMass;
    }

    /**
     * Get the average mass (simple double value).
     * If the getAvgMass() method returns null, 0.0 is returned.
     * @return the average mass as a double value
     */
    public double getAvgMassValue() {
        if(this.getAvgMass() == null) {
            return(0.0);
        }
        return this.getAvgMass().doubleValue();
    }

    /**
     * Set the average mass using a Double Object
     * @param avgMass the average mass to set
     */
    public void setAvgMass(Double avgMass) {
        this.avgMass = avgMass;
    }

    /**
     * Set the average mass using a simple double value
     * @param mass the average mass to set
     */
    public void setAvgMass(double mass) {
        this.avgMass = new Double(mass);
    }

    /**
     * Get the monoisotopic mass of the non-monosaccharide template (Double Object)
     * If the monoisotopic mass is not set yet and a composition is present the monoisotopic mass is calculated from the composition
     * @return the monoisotopic mass as a Double object
     */
    public Double getMonoMass() {
        if(this.monoMass == null && this.getComposition() != null) {
            this.setMonoMass(this.getComposition().getMonoMass());
        }
        return this.monoMass;
    }

    /**
     * Get the monoisotopic mass of the non-monosaccharide template (simple double value).
     * If the getMonoMass() method returns null, 0.0 is returned.
     * @return the monoisotopic mass as a double value
     */
    public double getMonoMassValue() {
        if(this.getMonoMass() == null) {
            return(0.0);
        }
        return this.getMonoMass().doubleValue();
    }

    /**
     * Set the monoisotopic mass of the non-monosaccharide template using a Double Object
     * @param monoMass the monoisotopic mass to set
     */
    public void setMonoMass(Double monoMass) {
        this.monoMass = monoMass;
    }

    /**
     * Set the monoisotopic mass of the non-monosaccharide template using a simple double value
     * @param mass the monoisotopic mass to set
     */
    public void setMonoMass(double mass) {
        this.monoMass = new Double(mass);
    }

    /**
     * Get the InChi description
     * @return the InChi String
     */
    public String getInchi() {
        return this.inchi;
    }

    /**
     * Set the InChi description
     * @param inchi the InChi String to set
     */
    public void setInchi(String inchi) {
        this.inchi = inchi;
    }

    /**
     * Get the Smiles description
     * @return the Smiles String
     */
    public String getSmiles() {
        return this.smiles;
    }

    /**
     * Set the Smiles description
     * @param smiles the Smiles String to set
     */
    public void setSmiles(String smiles) {
        this.smiles = smiles;
    }

    /**
     * Get the chemical formula
     * @return the formula
     */
    public String getFormula() {
        return this.formula;
    }

    /**
     * Set the chemical formula
     * @param formulaStr the formula to set
     */
    public void setFormula(String formulaStr) {
        this.formula = formulaStr;
    }

    /**
     * Get the List of atoms
     * @return the list of atoms
     */
    public List<Atom> getAtoms() {
        return this.atoms;
    }

    /**
     * Set the atoms
     * @param atoms the atoms to set
     */
    public void setAtoms(List<Atom> atoms) {
        this.atoms = atoms;
    }
    
    /**
     * Add an atom to the list of atoms
     * @param a the atom to add
     */
    public void addAtom(Atom a) {
        if(this.atoms == null) {
            this.atoms = new ArrayList<Atom>();
        }
        this.atoms.add(a);
    }
    
    /**
     * Add a new atom to the list of atoms and link it to an already present atom
     * @param newAtom the new atom to add
     * @param existingAtom the atom to which the new atom will be linked
     * @param bo the bond order of the linkage
     * @throws MonosaccharideException in case the second atom is not an already present one
     */
    public void addAtom(Atom newAtom, Atom existingAtom, double bo) throws MonosaccharideException {
        this.addAtom(newAtom);
        if(!this.getAtoms().contains(existingAtom)) {
            throw new MonosaccharideException("Cannot establish bond to atom " + existingAtom.toString() + " (not present in atom list).");
        }
        newAtom.addConnection(existingAtom, bo);
        existingAtom.addConnection(newAtom, bo);
    }
    
    /**
     * Add a bond between two atoms of this molecular entity
     * @param a the first atom
     * @param b the second atom
     * @param bo the bond order
     * @throws MonosaccharideException in case any of the given atoms is not included in the atoms list of this molecular entity
     */
    public void addBond(Atom a, Atom b, double bo) throws MonosaccharideException {
        if(!this.getAtoms().contains(a)) {
            throw new MonosaccharideException("Cannot establish bond from atom " + a.toString() + " (not present in atom list).");
        }
        if(!this.getAtoms().contains(b)) {
            throw new MonosaccharideException("Cannot establish bond to atom " + b.toString() + " (not present in atom list).");
        }
        a.addConnection(b, bo);
        b.addConnection(a, bo);
    }
    
    /**
     * Get an atom identified by its ID
     * @param atomId the id of the atom
     * @return the atom with the requested id or null if that atom is not found
     */
    public Atom getAtomById(int atomId) {
        for(Atom a : this.getAtoms()) {
            if(a.getId() == atomId) {
                return(a);
            }
        }
        return(null);
    }
    
    /**
     * Get an atom identified by its name
     * @param atomName the name of the atom
     * @return the atom with the requested name or null if that atom is not found
     */
    public Atom getAtomByName(String atomName) {
        for(Atom a : this.getAtoms()) {
            if(a.getName().equals(atomName)) {
                return(a);
            }
        }
        return(null);        
    }
    
    /**
     * Removes the first occurrence of an atom in the atom list.
     * @param a the atom to remove
     * @param removeHydrogens a flag to indicate if hydrogen atoms that are attached to the removed atom shall be removed as well
     */
    public void removeAtom(Atom a, boolean removeHydrogens) {
        if(a != null) {
            this.getAtoms().remove(a);
            for(AtomConnection ac : a.getConnections()) {
                Atom b = ac.getToAtom();
                if(b == a) {
                    b = ac.getFromAtom();
                }
                if(removeHydrogens && b.getElement().getSymbol().equals("H")) {
                    //*** remove attached hydrogen atoms: ***
                    this.getAtoms().remove(b);
                } else {
                    //*** remove connections to atom a: ***
                    b.removeConnection(a);
                }
            }
        }
    }
    
    /**
     * Get a clone of the atom List.
     * Not only the List but also the contained atoms are cloned by this method.
     * @return a clone of the atom list
     */
    public List<Atom> getAtomListClone() {
        //*** create new list: ***
        List<Atom> retList = new ArrayList<Atom>();
        //*** write atom clones to new list: ***
        for(Atom a : this.getAtoms()) {
            retList.add(a.clone());
        }
        //*** adjust connections: ***
        for(Atom a : retList) {
            for(AtomConnection ac : a.getConnections()) {
                ac.setFromAtom(a);
                int toAtomIndex = this.getAtoms().indexOf(ac.getToAtom());
                if(toAtomIndex != -1) {
                    ac.setToAtom(retList.get(toAtomIndex));
                }
            }
        }
        return(retList);
    }

    /**
     * Get the charge (Double Object)
     * @return the charge
     */
    public Double getCharge() {
        if(this.charge == null && this.getAtoms() != null && this.getAtoms().size() > 0) {
            this.setChargeFromAtoms();
        }
        return this.charge;
    }
    
    /**
     * Get the charge (simple double value)
     * @return the charge
     */
    public double getChargeValue() {
        if(this.getCharge() == null) {
            return(0.0);
        }
        return(this.getCharge().doubleValue());
    }

    /**
     * Set the charge using a Double Object
     * @param charge
     */
    public void setCharge(Double charge) {
        this.charge = charge;
    }

    /**
     * Set the charge using a simple double value
     * @param charge
     */
    public void setCharge(double charge) {
        this.charge = new Double(charge);
    }
    
    /**
     * Set the charge by adding up the charges of the atoms 
     */
    public void setChargeFromAtoms() {
        if(this.getAtoms() != null) {
            double chargeSum = 0;
            for(Atom a : this.getAtoms()) {
                chargeSum += a.getChargeValue();
            }
            this.setCharge(chargeSum);
        }
    }

    //*****************************************************************************
    //*** representation related methods: *****************************************
    //*****************************************************************************
    
    /**
     * Get all representations of this molecular entity
     * @return a list of representations
     */
    public List<ResidueRepresentation> getRepresentations() {
        return this.representations;
    }
    
    /**
     * Get a representation of this molecular entity by type and format
     * @param type the representation type
     * @param format the representation format (may be null, in that case the first representation of the given type is returned)
     * @return the first representation in the list that is of the given type and format or null if no representation matches the parameters
     */
    public ResidueRepresentation getRepresentation(ResidueRepresentationType type, ResidueRepresentationFormat format) {
        try {
            if(this.getRepresentations() != null) {
                for(ResidueRepresentation resRep : this.getRepresentations()) {
                    if(resRep.getType().equals(type)) {
                        if(format == null || resRep.getFormat().equals(format)) {
                            return resRep;
                        }
                    }
                }
            }
        } catch(Exception ex) {
            System.out.println("Exception in MolecularEntity.getRepresentation:" + ex);
        }
        return null;
    }

    /**
     * Set the representations of this molecular entity
     * @param representationsList the list of representations to set
     */
    public void setRepresentations(List<ResidueRepresentation> representationsList) {
        this.representations = representationsList;
    }
    
    /**
     * Add a residue representation to this molecular entity
     * @param resRepr the representation to add
     */
    public void addRepresentation(ResidueRepresentation resRepr) {
        if(this.getRepresentations() == null) {
            this.setRepresentations(new ArrayList<ResidueRepresentation>());
        }
        this.getRepresentations().add(resRepr);
    }
    
    /**
     * Add a List of representations to this molecular entity.
     * @param repList the representations to add
     */
    public void addRepresentations(List<ResidueRepresentation> repList) {
        if(repList != null) {
            for(ResidueRepresentation rep : repList) {
                if(rep != null) {
                    this.addRepresentation(rep);
                }
            }
        }
    }
    
    public int getCoordinatesId(ResidueRepresentationFormat format) {
        ResidueRepresentation monoRep;
        monoRep = this.getRepresentation(ResidueRepresentationType.COORDINATES, format);
        if(monoRep != null) {
            return monoRep.getDbId();
        }
        return 0;
    }
    
    public int getPdbCoordinatesId() {
        return this.getCoordinatesId(ResidueRepresentationFormat.PDB);
    }
    
    public int getMol2CoordinatesId() {
        return this.getCoordinatesId(ResidueRepresentationFormat.MOL2);
    }
    
    public int getChemCompId() {
        return this.getCoordinatesId(ResidueRepresentationFormat.CHEM_COMP);
    }
    
    public int getImageId(ResidueRepresentationFormat format) {
        for(ResidueRepresentation rep : this.getRepresentations()) {
            if(rep.getType().getFormatType().equals(ResidueRepresentationFormat.FORMAT_TYPE_GRAPHICS)) {
                if(format == null || format.equals(rep.getFormat())) {
                    return rep.getDbId();
                }
            }
        }
        return 0;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    /**
     * Initialize the properties of the molecular entity
     */
    public void init() {
        this.setName("");
        this.setAtoms(null);
        this.setComposition(null);
        this.setMonoMass(null);
        this.setAvgMass(null);
        this.setSmiles(null);
        this.setInchi(null);
        this.setFormula(null);
        this.setRepresentations(new ArrayList<ResidueRepresentation>());
    }
    
}
