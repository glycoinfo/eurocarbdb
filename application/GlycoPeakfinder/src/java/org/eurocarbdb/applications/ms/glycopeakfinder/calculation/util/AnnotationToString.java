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
package org.eurocarbdb.applications.ms.glycopeakfinder.calculation.util;

import java.util.Iterator;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.AnnotationEntity;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.PeakAnnotation;

/**
* @author Logan
*
*/
public class AnnotationToString 
{
    public String composition(PeakAnnotation a_objAnnotation) 
    {
        AnnotationEntity t_objEntity = null;
        String t_strResult = "";
        for (Iterator<AnnotationEntity> t_iterCompo = a_objAnnotation.getResidues().iterator(); t_iterCompo.hasNext();) 
        {
            t_objEntity = t_iterCompo.next();
            t_strResult += String.format("%s%d",t_objEntity.getId(),t_objEntity.getNumber());
        }
        if ( a_objAnnotation.getDerivatisation() != null )
        {
            if ( !a_objAnnotation.getDerivatisation().equals("none") )
            {
                t_strResult += "-" + a_objAnnotation.getDerivatisation().toUpperCase();
            }
        }        
        return t_strResult;
    }

    public String smallMolecules(PeakAnnotation a_objAnnotation) 
    {
        String t_strResult = "";
        AnnotationEntity t_objEntity = null;
        for (Iterator<AnnotationEntity> t_iterMol = a_objAnnotation.getGain().iterator(); t_iterMol.hasNext();) 
        {
            t_objEntity = t_iterMol.next();
            if ( t_objEntity.getNumber() > 1 )
            {
                t_strResult += String.format("+%d%s",t_objEntity.getNumber(),t_objEntity.getId());
            }
            else
            {
                t_strResult += "+" + t_objEntity.getId();
            }                    
        }
        for (Iterator<AnnotationEntity> t_iterMol = a_objAnnotation.getLoss().iterator(); t_iterMol.hasNext();) 
        {
            t_objEntity = t_iterMol.next();
            if ( t_objEntity.getNumber() > 1 )
            {
                t_strResult += String.format("-%d%s",t_objEntity.getNumber(),t_objEntity.getId());
            }
            else
            {
                t_strResult += "-" + t_objEntity.getId();
            }                    
        }
        return t_strResult;
    }

    public String chargedIon(PeakAnnotation a_objAnnotation) 
    {
        String t_strResult = "";
        AnnotationEntity t_objEntity = null;
        int t_iCharge = 0;
        for (Iterator<AnnotationEntity> t_oterIon = a_objAnnotation.getIons().iterator(); t_oterIon.hasNext();) 
        {
            t_objEntity = t_oterIon.next();
            if ( t_objEntity.getNumber() > 1 )
            {
                t_strResult += String.format(";%d%s",t_objEntity.getNumber(),t_objEntity.getId());                        
            }
            else
            {
                t_strResult += ";" + t_objEntity.getId();
            }
            t_iCharge += t_objEntity.getNumber();
        }
        if ( a_objAnnotation.getIonExchange().size() > 0 )
        {
            if ( a_objAnnotation.getIonExchange().size() > 1 )
            {
                t_strResult += String.format(";-%dH+",a_objAnnotation.getIonExchange().size());
            }
            else
            {
                t_strResult += ";-H+";
            }                    
            for (Iterator<AnnotationEntity> t_oterIon = a_objAnnotation.getIonExchange().iterator(); t_oterIon.hasNext();) 
            {
                t_objEntity = t_oterIon.next();
                if ( t_objEntity.getNumber() > 1 )
                {
                    t_strResult += String.format(";%d%s",t_objEntity.getNumber(),t_objEntity.getId());
                }
                else
                {
                    t_strResult += ";" + t_objEntity.getId();
                }
            }
        }
        if ( t_strResult.length() > 0 )
        {
            return t_strResult.substring(1);
        }
        else
        {
            return "";
        }

    }

    public String ion(PeakAnnotation a_objAnnotation) 
    {
        String t_strResult = "";
        AnnotationEntity t_objEntity = null;
        for (Iterator<AnnotationEntity> t_iterFragment = a_objAnnotation.getFragments().iterator(); t_iterFragment.hasNext();) 
        {
            t_objEntity = t_iterFragment.next();
            t_strResult += ";" + t_objEntity.getId();
        }
        if ( t_strResult.length() > 0 )
        {
            return t_strResult.substring(1);
        }
        else
        {
            return "";
        }
    }
}
