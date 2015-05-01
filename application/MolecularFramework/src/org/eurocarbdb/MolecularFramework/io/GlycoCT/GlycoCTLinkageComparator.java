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
package org.eurocarbdb.MolecularFramework.io.GlycoCT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eurocarbdb.MolecularFramework.sugar.Linkage;

/**
* @author sherget
*
*/
public class GlycoCTLinkageComparator implements Comparator <Linkage>  {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(T, T)
     */
    public int compare(Linkage arg0, Linkage arg1) {
        
        ArrayList <Integer> t_parents0= arg0.getParentLinkages();
        ArrayList <Integer> t_parents1= arg1.getParentLinkages();
        
        Collections.sort(t_parents0);
        Collections.sort(t_parents1);
        
        // first Parent Linkage Array bigger
        if ( t_parents0.size() > t_parents1.size() )
        {
            for (int i = 0; i < t_parents1.size(); i++) 
            {
                if (t_parents0.get(i) < t_parents1.get(i))
                {
                    return 1;
                }
                else if ( t_parents0.get(i) > t_parents1.get(i))
                {
                    return -1;
                }
            }    
            return -1;
        }
        // second Parent Linkage Array bigger
        else if (t_parents0.size() < t_parents1.size())
        {
            for (int i = 0; i < t_parents0.size(); i++) 
            {
                if ( t_parents0.get(i) < t_parents1.get(i) )
                {
                    return 1;
                }
                else if ( t_parents0.get(i) > t_parents1.get(i))
                {
                    return -1;
                }
            }
            return 1;
        }
        // same length of Parent Linkage Array
        else 
        {         
            for (int i = 0; i < t_parents0.size(); i++) 
            {
                if ( t_parents0.get(i) < t_parents1.get(i))
                {
                    return -1;
                }
                else if ( t_parents0.get(i) > t_parents1.get(i))
                {
                    return 1;
                }
            }
        }        
        
        ArrayList <Integer> t_childs0= arg0.getChildLinkages();
        ArrayList <Integer> t_childs1= arg1.getChildLinkages();
        
        Collections.sort(t_childs0);
        Collections.sort(t_childs1);
        
        
        // first Child Linkage Array bigger
        if ( t_childs0.size() > t_childs1.size() )
        {
            for (int i = 0; i < t_childs1.size(); i++) 
            {
                if (t_childs0.get(i) < t_childs1.get(i))
                {
                    return 1;
                }
                else if ( t_childs0.get(i) > t_childs1.get(i))
                {
                    return -1;
                }
            }    
            return -1;
        }
        // second Child Linkage Array bigger
        else if (t_childs0.size() < t_childs1.size())
        {
            for (int i = 0; i < t_childs0.size(); i++) 
            {
                if ( t_childs0.get(i) < t_childs1.get(i) )
                {
                    return 1;
                }
                else if ( t_childs0.get(i) > t_childs1.get(i))
                {
                    return -1;
                }
            }
            return 1;
        }
        // same length of Child Linkage Array
        else 
        {         
            for (int i = 0; i < t_childs0.size(); i++) 
            {
                if ( t_childs0.get(i) < t_childs1.get(i))
                {
                    return -1;
                }
                else if ( t_childs0.get(i) > t_childs1.get(i))
                {
                    return 1;
                }
            }
        }
        
        
        // Linkage type comparison        
        
        // parent type
        Character t_cParentType0 = arg0.getParentLinkageType().getType();        
        Character t_cParentType1 = arg1.getParentLinkageType().getType();
        
        Integer t_compareParent = t_cParentType0.compareTo(t_cParentType1);
        
        if (t_compareParent==1){
            return 1;
        }
        else if (t_compareParent==-1){
            return -1;
        }
        
        // child type
        Character t_cChildType0 = arg0.getChildLinkageType().getType();        
        Character t_cChildType1 = arg1.getChildLinkageType().getType();
        
        Integer t_compareChild = t_cChildType0.compareTo(t_cChildType1);
        
        if (t_compareChild==1){
            return 1;
        }
        else if (t_compareChild==-1){
            return -1;
        }
        
        // cannot distinguish        
        return 0;        
    }
}
