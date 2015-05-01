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
package org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage;

/**
* Stores the settings for an annotations.
* The object stores the number of occurrence of this annotation and the id of the annotation.
* Id of the annotation can be the name of the residue, name of the fragment etc. 
*  
* @author Logan
*/
public class AnnotationEntity 
{
    private String m_strID = null;
    private int m_iNumber = 0;
    
    /**
     * Sets number of occurrence of this annotation. 
     * @param a_iNumber    
     */
    public void setNumber(int a_iNumber)
    {
        this.m_iNumber = a_iNumber;
    }
    
    /**
     * Give the number of occurrence of this annotation.
     * @return
     */
    public int getNumber()
    {
        return this.m_iNumber;
    }
    
    /**
     * Sets the id of this annotation. 
     * @param a_strId
     */
    public void setId(String a_strId)
    {
        this.m_strID = a_strId;
    }
    
    /**
     * Give the id of this annotation.
     * @return
     */
    public String getId()
    {
        return this.m_strID;
    }
    
    /**
     * Creates a copy of this annotation object.
     * @return
     */
    public AnnotationEntity copy()
    {
        AnnotationEntity t_objAnnotation = new AnnotationEntity();
        t_objAnnotation.setNumber(this.m_iNumber);
        t_objAnnotation.setId(new String(this.m_strID));
        return t_objAnnotation;
    }
}