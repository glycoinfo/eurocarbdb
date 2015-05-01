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
*   Last commit: $Rev: 1231 $ by $Author: glycoslave $ on $Date:: 2009-06-19 #$  
*/

package org.eurocarbdb.sugar;


/**
*   The most basic interface for any chemical entity.
*   
*   In simple terms, MassEntities (MEs) represent any chemical entity
*   that has mass (in other words, everything...).
*   
*   MEs are able to described themselves in terms of physical properties - 
*   chemical names and mnemonics, monoisotopic and average mass, composition, 
*   etc, as well as in terms of other attached MEs.
*
*   MEs can be attached to other MEs to create new, composite MEs. This 
*   attachment process is codified in this class by the method attach().
*   Attached MEs are regarded as sharing a parent-children type relationship,
*   as such, any ME composite type can be mathematically regarded in terms 
*   of a (rooted) directional graph. Cyclic molecules/structures/relationships
*   are possible (by returning the parent as a child of some other child), but
*   care would need to be exercised during iteration to avoid infinite loops.  
*
*   In this way, the ME interface is able to model "simple" molecules, as well
*   as large, complex composite types.
*
*    Created 20-Sep-2005.
*   @author matt
*
*/
public interface Molecule 
{
    //  discrete MEs will most likely be loaded upon startup; this is 
    //  a temporary placeholder -- discrete MEs will be factory constructed.
    //
    public static final Molecule H2O 
        = new BasicMolecule( "H2O", "H2O", null, null, 18.0, 18.0 ); 
    
    
    /*  getMass  *//*************************************************
    *   
    *   Calculates/Returns sum/total monoisotopic mass.
    */
    public double getMass();

    
    /*  getAvgMass  *//**********************************************
    *   
    *   Calculates/Returns sum/total average mass.
    */
    public double getAvgMass();
    
    
    /*  getName  *//*************************************************
    *   
    *   Returns the canonical (short) name/identfier that would normally
    *   be used in a sequence representation. For example, for
    *   the amino-acid Threonine, this would be 'T'. For the 
    *   monosaccharide Glucose, this would be 'Glc'. For a composite
    *   type, such as a oligosaccharide or peptide/protein, this would
    *   be the sequence.  
    */
    public String getName();

    
    /*  getFullName  *//*********************************************
    *   
    *   Returns the name of this entity in its most descriptive form.   
    */
    public String getFullName();
    
    
    /*  getType  *//*************************************************
    *   
    *   Return the canonical 'type' of this entity, for example 'amino-acid'
    *   for an amino-acid, 'monosaccharide' for a monosaccharide, 
    *   'sugar' for an oligosaccharide, etc.
    */
    //public String getType();
           
    
    /*  getComposition  *//******************************************
    *   
    *   Returns the composition of this entity in terms of its 
    *   composing types. For example, for a peptide, the returned
    *   composition types would be amino-acids and their respective
    *   counts; for a carbohydrate, its composing monosaccharides and
    *   substituents (if any).   
    */
    //public Composition getComposition();
    
    
    /*  getComposition  *//******************************************
    *   
    *   Returns the elemental composition of this entity.   
    */
    //public Composition getElementalComposition();

     
    /*  toString  *//************************************************
    *   
    *   This should return the same information as getName.
    *   
    *   @see #getName()
    */
    //public String toString();
    
}
