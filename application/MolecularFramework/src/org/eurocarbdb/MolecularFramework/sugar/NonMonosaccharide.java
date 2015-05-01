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
package org.eurocarbdb.MolecularFramework.sugar;

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;


public class NonMonosaccharide extends GlycoNode 
{
    public static final int POSITION_UNKNOWN   = -1;
    private String m_strName = "";
    private int m_iAttachPosition = NonMonosaccharide.POSITION_UNKNOWN;
    
    /**
     * @see org.eurocarbdb.util.Visitable#accept(org.eurocarbdb.util.GlycoVisitor)
     */
    public void accept(GlycoVisitor a_objVisitor)  throws GlycoVisitorException
    {
        a_objVisitor.visit(this);        
    }

    
    public NonMonosaccharide(String a_strName) throws GlycoconjugateException
    {
        this.setName(a_strName);
    }
    
    public void setName(String a_strName) throws GlycoconjugateException
    {
        if ( a_strName == null )
        {
            throw new GlycoconjugateException("null is not allowed for a name.");
        }
        this.m_strName = a_strName;
    }
    
    public String getName()
    {
        return this.m_strName;
    }
    
    public void setAttachPosition(int a_iPosition)
    {
        this.m_iAttachPosition = a_iPosition;
    }
    
    public int getAttachPosition()
    {
        return this.m_iAttachPosition;
    }

    /**
     * @throws GlycoconjugateException 
     * @see org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharides#copy()
     */
    @Override
    public NonMonosaccharide copy() throws GlycoconjugateException 
    {
        NonMonosaccharide t_objCopy = new NonMonosaccharide( this.m_strName );
        t_objCopy.setAttachPosition( this.m_iAttachPosition );
        return t_objCopy;
    }

}