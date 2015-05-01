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
package org.eurocarbdb.MolecularFramework.util.visitor;



/**
* @author rene
*
*/
public class GlycoVisitorException extends Exception
{
    protected String m_strMessage;

    /**
     * @param message
     */
    public GlycoVisitorException(String a_strMessage)
    {
        super(a_strMessage);
        this.m_strMessage = a_strMessage;
    }

    /**
     * @param message
     */
    public GlycoVisitorException(String a_strMessage,Throwable a_objThrowable)
    {
        super(a_strMessage,a_objThrowable);
        this.m_strMessage = a_strMessage;
    }

    public String getErrorMessage()
    {
        return this.m_strMessage;
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
