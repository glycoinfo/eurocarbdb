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
package org.eurocarbdb.MolecularFramework.io;


public class SugarImporterException extends Exception 
{
    private String m_strErrorCode = "";
    private int m_iPosition = -1;
    private int m_iLine = -1;
    private Exception m_objException = null;
    
    public SugarImporterException(String a_strErrorCode, int a_iPosition) 
    {
        super();
        this.m_iPosition = a_iPosition;
        this.m_strErrorCode = a_strErrorCode;
    }
    
    /**
     * @param string
     */
    public SugarImporterException(String a_strErrorCode) 
    {
        super();
        this.m_strErrorCode = a_strErrorCode;
    }

    /**
     * @param string
     * @param x
     * @param y
     */
    public SugarImporterException(String a_strErrorCode, int a_iPosition, int a_iLine)
    {
        super();
        this.m_iPosition = a_iPosition;
        this.m_strErrorCode = a_strErrorCode;
        this.m_iLine = a_iLine;
    }

    /**
     * @param message
     * @param e
     */
    public SugarImporterException(String message, Throwable e) 
    {
        super(message,e);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public int getPosition()
    {
        return this.m_iPosition;
    }
    
    public int getLine()
    {
        return this.m_iLine;
    }
    
    public String getErrorCode()
    {
        return this.m_strErrorCode;
    }

    /**
     * 
     * @return ErrorText or null
     */
    public String getErrorText()
    {
        return ErrorTextEng.getErrorText(this.m_strErrorCode);
    }
    
    /**
     * 
     * @return Exception or null
     */
    public Exception getException()
    {
        return this.m_objException;
    }
}
