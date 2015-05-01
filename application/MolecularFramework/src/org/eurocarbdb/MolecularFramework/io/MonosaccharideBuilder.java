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
package org.eurocarbdb.MolecularFramework.io;

import java.util.ArrayList;

import org.eurocarbdb.MolecularFramework.sugar.Anomer;
import org.eurocarbdb.MolecularFramework.sugar.BaseType;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Superclass;

/**
* @author rene
*
*/
public class MonosaccharideBuilder
{
    
    public static Monosaccharide fromGlycoCT(String a_strNameGlycoCT) throws GlycoconjugateException
    {
        try 
        {
            String a_strName = a_strNameGlycoCT + "$";
            int t_iPosition = 0;
            char t_cToken = a_strName.charAt(t_iPosition);            
            // anomer
            Anomer t_objAnomer;
            t_objAnomer = Anomer.forSymbol( t_cToken );
            if ( a_strName.charAt(++t_iPosition) != '-' )
            {
                return null;
            }
            t_iPosition++;
            // configuration
            int t_iMaxPos = a_strName.indexOf(":",t_iPosition) - 7;
            ArrayList<BaseType> t_aConfiguration = new ArrayList<BaseType>(); 
            String t_strInformation = "";
            while (t_iPosition < t_iMaxPos )
            {
                t_strInformation = "";
                for (int t_iCounter = 0; t_iCounter < 4; t_iCounter++)
                {
                    t_strInformation += a_strName.charAt(t_iPosition++);
                }
                t_aConfiguration.add(BaseType.forName(t_strInformation));
                if ( a_strName.charAt(t_iPosition++) != '-' )
                {
                    return null;
                }
            }
            // superclass
            t_strInformation = "";
            for (int t_iCounter = 0; t_iCounter < 3; t_iCounter++)
            {
                t_strInformation += a_strName.charAt(t_iPosition++);
            }
            Superclass t_objSuper;
            t_objSuper = Superclass.forName(t_strInformation.toLowerCase());    
            Monosaccharide t_objMS = new Monosaccharide(t_objAnomer,t_objSuper);   
            t_objMS.setBaseType(t_aConfiguration);
            if ( a_strName.charAt(t_iPosition++) != '-' )
            {
                return null;
            }
            // ring
            int t_iRingStart;
            if ( a_strName.charAt(t_iPosition) == 'x' )
            {
                t_iRingStart = Monosaccharide.UNKNOWN_RING;
                t_iPosition++;
            }
            else
            {
                if ( a_strName.charAt(t_iPosition) == '0' )
                {
                    t_iPosition++;
                    t_iRingStart = 0;
                }
                else
                {
                    int t_iDigit = (int) a_strName.charAt(t_iPosition++);
                    if ( t_iDigit < 49 || t_iDigit > 57 )
                    {
                        return null;
                    }
                    t_iRingStart = t_iDigit - 48;
                    // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
                    t_iDigit = (int) a_strName.charAt(t_iPosition);
                    while ( t_iDigit > 47 && t_iDigit < 58 )
                    {
                        t_iRingStart = ( t_iRingStart * 10 ) + ( t_iDigit - 48 );
                        t_iPosition++;
                        t_iDigit = (int) a_strName.charAt(t_iPosition);
                    }
                }                
            }
            if ( a_strName.charAt(t_iPosition++) != ':' )
            {
                return null;
            }
            if ( a_strName.charAt(t_iPosition) == 'x' )
            {
                t_objMS.setRing(t_iRingStart,Monosaccharide.UNKNOWN_RING);
                t_iPosition++;
            }
            else
            {   
                int t_iRingEnd = 0;
                if ( a_strName.charAt(t_iPosition) == '0' )
                {
                    t_iPosition++;
                    t_iRingEnd = 0;
                }
                else
                {
                    int t_iDigit = (int) a_strName.charAt(t_iPosition++);
                    if ( t_iDigit < 49 || t_iDigit > 57 )
                    {
                        return null;
                    }
                    t_iRingEnd = t_iDigit - 48;
                    // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
                    t_iDigit = (int) a_strName.charAt(t_iPosition);
                    while ( t_iDigit > 47 && t_iDigit < 58 )
                    {
                        t_iRingEnd = ( t_iRingEnd * 10 ) + ( t_iDigit - 48 );
                        t_iPosition++;
                        t_iDigit = (int) a_strName.charAt(t_iPosition);
                    }
                }                
                t_objMS.setRing(t_iRingStart,t_iRingEnd);
            }
            // modifications
            while ( a_strName.charAt(t_iPosition) == '|' )
            {
                int t_iPosOne;
                Integer t_iPosTwo = null;
                t_iPosition++;
                if ( a_strName.charAt(t_iPosition) == 'x' )
                {
                    t_iPosOne = Modification.UNKNOWN_POSITION;
                    t_iPosition++;
                }
                else
                {
                    if ( a_strName.charAt(t_iPosition) == '0' )
                    {
                        t_iPosition++;
                        t_iPosOne = 0;
                    }
                    else
                    {
                        int t_iDigit = (int) a_strName.charAt(t_iPosition++);
                        if ( t_iDigit < 49 || t_iDigit > 57 )
                        {
                            return null;
                        }
                        t_iPosOne = t_iDigit - 48;
                        // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
                        t_iDigit = (int) a_strName.charAt(t_iPosition);
                        while ( t_iDigit > 47 && t_iDigit < 58 )
                        {
                            t_iPosOne = ( t_iPosOne * 10 ) + ( t_iDigit - 48 );
                            t_iPosition++;
                            t_iDigit = (int) a_strName.charAt(t_iPosition);
                        }
                    }
                }
                if ( a_strName.charAt(t_iPosition) == ',' )
                {
                    t_iPosition++;
                    if ( a_strName.charAt(t_iPosition) == '0' )
                    {
                        t_iPosition++;
                        t_iPosTwo = 0;
                    }
                    else
                    {
                        int t_iDigit = (int) a_strName.charAt(t_iPosition++);
                        if ( t_iDigit < 49 || t_iDigit > 57 )
                        {
                            return null;
                        }
                        t_iPosTwo = t_iDigit - 48;
                        // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
                        t_iDigit = (int) a_strName.charAt(t_iPosition);
                        while ( t_iDigit > 47 && t_iDigit < 58 )
                        {
                            t_iPosTwo = ( t_iPosTwo * 10 ) + ( t_iDigit - 48 );
                            t_iPosition++;
                            t_iDigit = (int) a_strName.charAt(t_iPosition);
                        }
                    }                    
                }
                if ( a_strName.charAt(t_iPosition++) != ':' )
                {
                    return null;
                }
                t_strInformation = "";
                boolean t_bNext = true;
                while ( t_bNext )
                {
                    t_cToken = a_strName.charAt(t_iPosition);
                    t_bNext = false;
                    if ( t_cToken >= 'A' && t_cToken <= 'Z' )
                    {
                        t_iPosition++;
                        t_bNext = true;
                        t_strInformation += t_cToken;
                    }
                    else if ( t_cToken >= 'a' && t_cToken <= 'z' )
                    {
                        t_iPosition++;
                        t_bNext = true;
                        t_strInformation += t_cToken;
                    }
                }       
                ModificationType t_enumMod;
                t_enumMod = ModificationType.forName(t_strInformation);  
                Modification t_objModi = new Modification(t_enumMod,t_iPosOne,t_iPosTwo);
                t_objMS.addModification(t_objModi);
            } 
            return t_objMS;
        } 
        catch (IndexOutOfBoundsException e) 
        {
            return null;
        }       
    }    
}
