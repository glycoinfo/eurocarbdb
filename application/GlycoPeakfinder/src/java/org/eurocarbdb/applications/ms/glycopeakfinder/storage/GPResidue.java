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
package org.eurocarbdb.applications.ms.glycopeakfinder.storage;
/**
* @author rene
*
*/
public class GPResidue
{
    private String m_strName = "";
    private int m_iMin = 0;
    private int m_iMax = 0;
    private boolean m_bAx = false;
    private boolean m_bE = false;
    private boolean m_bF = false;
    private boolean m_bG = false;
    private boolean m_bH = false;
    
    public void setName(String a_strName)
    {
        this.m_strName = a_strName;
    }
    
    public String getName()
    {
        return this.m_strName;
    }
    
    public void setMin(int a_iMin)
    {
        this.m_iMin = a_iMin;
    }
    
    public int getMin()
    {
        return this.m_iMin;
    }
    
    public void setMax(int a_iMax)
    {
        this.m_iMax = a_iMax;
    }
    
    public int getMax()
    {
        return this.m_iMax;
    }

    public boolean getAx() 
    {
        return this.m_bAx;
    }

    public void setAx(boolean a_bAx) 
    {
        this.m_bAx = a_bAx;
    }

    public boolean getE() 
    {
        return this.m_bE;
    }

    public void setE(boolean a_bAx) 
    {
        this.m_bE = a_bAx;
    }

    public boolean getF() 
    {
        return this.m_bF;
    }

    public void setF(boolean a_bAx) 
    {
        this.m_bF = a_bAx;
    }

    public boolean getG() 
    {
        return this.m_bG;
    }

    public void setG(boolean a_bAx) 
    {
        this.m_bG = a_bAx;
    }

    public boolean getH() 
    {
        return this.m_bH;
    }

    public void setH(boolean a_bAx) 
    {
        this.m_bH = a_bAx;
    }
}