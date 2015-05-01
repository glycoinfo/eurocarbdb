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
package org.eurocarbdb.MolecularFramework.io.kcf;

/**
* @author rene
*
*/
public class KCFBlock
{
    private Double m_dUp    = null;
    private Double m_dDown    = null;
    private Double m_dLeft    = null;
    private Double m_dRight    = null;
    private int m_iRepeatMin = 0;
    private int m_iRepeatMax = 0;
    
    public KCFBlock ()
    {}
    
    public boolean setUpDown(double a_dValueOne,double a_dValueTwo)
    {
        double t_dUp = 0;
        double t_dDown = 0;
        if ( a_dValueOne > a_dValueTwo )
        {
            t_dUp = a_dValueOne;
            t_dDown = a_dValueTwo;
        }
        else
        {
            t_dUp = a_dValueTwo;
            t_dDown = a_dValueOne;
        }
        if ( this.m_dUp == null )
        {
            this.m_dUp = t_dUp;
        }
        else
        {
            if ( this.m_dUp != t_dUp )
            {
                return false;
            }
        }
        if ( this.m_dDown == null )
        {
            this.m_dDown = t_dDown;
        }
        else
        {
            if ( this.m_dDown != t_dDown )
            {
                return false;
            }
        }
        return true;
    }
    
    public void setLeftRight(double a_dValue)
    {
        if ( this.m_dRight == null )
        {
            this.m_dRight = a_dValue;
        }
        else
        {
            if ( this.m_dRight > a_dValue )
            {
                this.m_dLeft = a_dValue;
            }
            else
            {
                this.m_dLeft = this.m_dRight;
                this.m_dRight = a_dValue;
            }
        }
    }
    
    public double getUp()
    {
        return this.m_dUp;
    }
    
    public double getDown()
    {
        return this.m_dDown;
    }
    
    public double getLeft()
    {
        return this.m_dLeft;
    }
    
    public double getRight()
    {
        return this.m_dRight;
    }
    
    public void setMin(int a_iValue)
    {
        this.m_iRepeatMin = a_iValue;
    }
    
    public void setMax(int a_iValue)
    {
        this.m_iRepeatMax = a_iValue;
    }
    
    public int getMin()
    {
        return this.m_iRepeatMin;
    }
    
    public int getMax()
    {
        return this.m_iRepeatMax;
    }
}
