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
/**
* 
*/
package org.eurocarbdb.MolecularFramework.sugar;

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* @author rene
*
*/
public class Substituent extends GlycoNode 
{
    private SubstituentType m_enumSubstType;

    public Substituent(SubstituentType a_enumType ) throws GlycoconjugateException
    {
        this.setSubstituentType(a_enumType);
    }
    
    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.Visitable#accept(org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor)
     */
    public void accept(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        a_objVisitor.visit(this);        
    }
    
    public void setSubstituentType(SubstituentType a_enumType) throws GlycoconjugateException
    {
        if ( a_enumType == null )
        {
            throw new GlycoconjugateException("Invalide substituent.");
        }
        this.m_enumSubstType = a_enumType;
    }
    
    public SubstituentType getSubstituentType()
    {
        return this.m_enumSubstType;
    }

    /**
     * @return
     * @throws GlycoconjugateException 
     */
    public Substituent copy() throws GlycoconjugateException 
    {
        return new Substituent(this.m_enumSubstType);
    }
}
