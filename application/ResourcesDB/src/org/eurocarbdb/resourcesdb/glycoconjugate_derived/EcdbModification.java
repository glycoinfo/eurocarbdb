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
package org.eurocarbdb.resourcesdb.glycoconjugate_derived;



/**
* @author Logan, Thomas
*
*/
public class EcdbModification 
{
    private Integer m_iPositionOne = null;
    private Integer m_iPositionTwo = null;
    public static final int UNKNOWN_POSITION   = 0;

    private EcdbModificationType m_enumModification;
    
    public EcdbModification(String symbol, Integer a_iPosition) throws GlycoconjugateException
    {
        this.m_enumModification = EcdbModificationType.forName(symbol);
         this.setPositionOne(a_iPosition);
    }
    
    public EcdbModification(EcdbModificationType a_enumModtype, Integer a_iPosition) throws GlycoconjugateException {
        this.m_enumModification = a_enumModtype;
        this.setPositionOne(a_iPosition);
    }

    public EcdbModification(String a_strModi, Integer a_iPositionOne , Integer a_iPositionTwo) throws GlycoconjugateException
    {
        this.m_enumModification = EcdbModificationType.forName(a_strModi);
        this.setPositionOne(a_iPositionOne);
        this.setPositionTwo(a_iPositionTwo);
    }

    public EcdbModification(EcdbModificationType a_enumModtype, Integer a_iPositionOne, Integer a_iPositionTwo) throws GlycoconjugateException {
        this.m_enumModification = a_enumModtype;
        this.setPositionOne(a_iPositionOne);
        if(a_iPositionTwo != null) {
            this.setPositionTwo(a_iPositionTwo);
        }
    }

    private void setPositionOne(Integer a_iPosition) throws GlycoconjugateException
    {
        if ( a_iPosition < EcdbModification.UNKNOWN_POSITION )
        {
            throw new GlycoconjugateException("Invalid value for attach position");
        }
        this.m_iPositionOne = a_iPosition;
    }
    
    public int getPositionOne()
    {
        return this.m_iPositionOne;
    }

    private void setPositionTwo(Integer a_iPosition) throws GlycoconjugateException
    {
        if ( a_iPosition < EcdbModification.UNKNOWN_POSITION )
        {
            throw new GlycoconjugateException("Invalid value for attach position");
        }
        this.m_iPositionTwo = a_iPosition;
    }

    public int getPositionTwo()
    {
        return this.m_iPositionTwo;
    }
    
    public String getName()
    {
        return this.m_enumModification.getName();
    }
    
    public EcdbModificationType getModificationType() {
        return this.m_enumModification;
    }
    
    public boolean hasPositionTwo(){
        
        if (this.m_iPositionTwo==null){
            return false;
        }
        else return true;
    }
    
    public String toString() {
        String outStr = "";
        outStr += this.getPositionOne();
        if(this.hasPositionTwo()) {
            outStr += "," + this.getPositionTwo();
        }
        outStr += "-" + this.getName();
        return outStr;
    }
}
