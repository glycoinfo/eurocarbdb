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
*   Last commit: $Rev: 1561 $ by $Author: glycoslave $ on $Date:: 2009-07-21 #$  
*/
package org.eurocarbdb.sugar;

public class BasicLinkage implements Linkage
{
    static Molecule Default_Linkage 
        = new BasicMolecule( "example", 
                                "-", 
                                "simple-linkage", 
                                null, //BasicMolecule.H2O.getComposition(), 
                                - BasicMolecule.H2O.getMass(),
                                - BasicMolecule.H2O.getAvgMass()
                                );
    
    Molecule parent;
    Molecule child;
    Molecule archetype;
    
    public BasicLinkage( Molecule parent, Molecule child )
    {
        this.parent    = parent;
        this.child     = child;
        this.archetype = Default_Linkage; 
    }
    
    
    public double getMass()
    {
        return this.archetype.getMass();
    }

    
    public double getAvgMass()
    {
        return this.archetype.getAvgMass();
    }

    
    public String getName()
    {
        return this.archetype.getName();
    }

    
    public String getFullName()
    {
        return this.archetype.getFullName();
    }

    
    
/*
    public String getType()
    {
        return this.archetype.getType();
    }
        
    
    public Composition getComposition()
    {
        return this.archetype.getComposition();
    }
    
    
    public Composition getElementalComposition()
    {
        return this.archetype.getElementalComposition();
    }
*/

    private int parentTerminus = 0;
    private int childTerminus = 0;
    
    public int getParentTerminus()
    {
        return parentTerminus;   
    }
    
    
    public int getChildTerminus()
    {
        return childTerminus;   
    }

    
    public LinkageType getLinkageType()
    {
        return null;
    }
    
    
    public boolean isDefinite()
    {
        return parentTerminus > 0
            && childTerminus > 0
        ;
    }
    
    /*
    public Residue getParent()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Residue getChild()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Linkage attach( Linkage linked_child )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Linkage[] getAttachedChildren()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Linkage getAttachedParent()
    {
        // TODO Auto-generated method stub
        return null;
    }
    */
    
}
