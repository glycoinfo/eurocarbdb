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
* @author Logan
*
*/
public class UnvalidatedGlycoNode extends GlycoNode 
{
    private String m_strName = "";
    
    public void setName(String a_strName) throws GlycoconjugateException
    {
        if ( a_strName == null )
        {
            throw new GlycoconjugateException("null is not a valide name.");
        }
        this.m_strName = a_strName;
    }
    
    public String getName()
    {
        return this.m_strName;
    }
    
    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.Visitable#accept(org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor)
     */
    public void accept(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {
        a_objVisitor.visit(this);        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.sugar.GlycoNode#copy()
     */
    public UnvalidatedGlycoNode copy() throws GlycoconjugateException
    {
        UnvalidatedGlycoNode t_objCopy = new UnvalidatedGlycoNode();
        t_objCopy.setName( this.m_strName );
        return t_objCopy;
    }
}
