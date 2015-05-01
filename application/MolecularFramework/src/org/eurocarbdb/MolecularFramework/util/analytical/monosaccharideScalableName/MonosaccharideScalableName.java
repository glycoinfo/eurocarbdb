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
package org.eurocarbdb.MolecularFramework.util.analytical.monosaccharideScalableName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.io.GlycoCT.GlycoCTGlycoEdgeComparator;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.GlycoCTLinkageComparator;
import org.eurocarbdb.MolecularFramework.sugar.BaseType;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;

import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.SubstituentType;

import org.eurocarbdb.MolecularFramework.sugar.Substituent;


import org.eurocarbdb.MolecularFramework.util.analytical.misc.ComparatorModification;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

public class MonosaccharideScalableName 
{
    private boolean m_bAnomer = true;
    private boolean m_bSuperclass = true;
    private boolean m_bRingsize = true;
    private boolean m_bStereochemistry = true;
    private boolean m_bConfiguration = true;
    private boolean m_bAllModifications = true;
    private ArrayList<ModificationType> m_aModifications = new ArrayList<ModificationType>(); 
    
    private boolean m_bAllSubstituent = true;
    private ArrayList<SubstituentType> m_aSubstitutents = new ArrayList<SubstituentType>();
    private boolean m_bSubstituentFatherLinkage = true;
    private boolean m_bSubstituentChildLinkage = false;
    
    private Monosaccharide m_Monosaccharide = null;
    private boolean m_bLinkageBrackets = false;
    
    public String getName (Monosaccharide a_objMonosaccharid) throws GlycoVisitorException
    {
        this.m_Monosaccharide = a_objMonosaccharid;            
        return this.generateBasicName() + this.generateSubstituentString();            
    }
    
    private String generateSubstituentString() throws GlycoVisitorException 
    {
        String t_strResult = "";
        ArrayList<GlycoEdge>t_aAllChildEdges = this.m_Monosaccharide.getChildEdges();
        ArrayList<GlycoEdge>t_aAllChildEdgesSubstituents = new ArrayList<GlycoEdge>();
        
        GlycoVisitorNodeType t_oVisNodeType = new GlycoVisitorNodeType();
        
        for (GlycoEdge t_oChild : t_aAllChildEdges )
        {
            if (t_oVisNodeType.isSubstituent(t_oChild.getChild()))
            {
                t_aAllChildEdgesSubstituents.add(t_oChild);                
            }
        }
        // List with all substituents containing edges avaiable
        GlycoCTGlycoEdgeComparator t_GGEC = new GlycoCTGlycoEdgeComparator();
        Collections.sort(t_aAllChildEdgesSubstituents,t_GGEC);    
        // sorted
        GlycoCTLinkageComparator t_GCLC = new GlycoCTLinkageComparator ();
        for (GlycoEdge t_edge : t_aAllChildEdgesSubstituents)
        {                
            ArrayList <Linkage> t_aLin = t_edge.getGlycosidicLinkages();
            Collections.sort(t_aLin,t_GCLC);
            // add Linkage string
            // TODO:
//            if ( this.m_bSubstituentChildLinkage || this.m_bSubstituentFatherLinkage )
//            {
//            for (Linkage t_lin : t_aLin)
//            {
//            t_strResult+=this.generateLinkage(t_lin);
//            }
//            }
            // add name
            Substituent s = (Substituent) t_edge.getChild();
            if ( m_bAllSubstituent || this.m_aSubstitutents.contains(s.getSubstituentType()) )
            {                                        
                if ( this.m_bSubstituentChildLinkage || this.m_bSubstituentFatherLinkage )
                {
                    for (Linkage t_lin : t_aLin)
                    {
                        t_strResult+=this.generateLinkage(t_lin);
                    }
                }
                t_strResult+=s.getSubstituentType().getName();    
            }            
        }
        return t_strResult;        
    }
    
    
    private String generateLinkage (Linkage a_objLin)
    {
        String tmp=",";
        if ( this.m_bLinkageBrackets )
        {
            tmp="(";
        }
        //add first linkage
        ArrayList <Integer> t_aParentLinkages = a_objLin.getParentLinkages();
        Collections.sort(t_aParentLinkages);
        ArrayList <Integer> t_aChildLinkages = a_objLin.getChildLinkages();
        Collections.sort(t_aChildLinkages);            
        if (this.m_bSubstituentFatherLinkage)
        {
            for (Integer i : t_aParentLinkages) 
            {
                tmp+=i+"|";
            }
            tmp=tmp.substring(0,tmp.length()-1);            
        }
        if (this.m_bSubstituentChildLinkage)
        {
            tmp+="-";
            for (Integer i : t_aChildLinkages) 
            {
                tmp+=i+"|";
            }
            tmp=tmp.substring(0,tmp.length()-1);
        }
        if ( this.m_bLinkageBrackets )
        {
            tmp+=")";
        }
        return tmp;        
    }
    
    private String generateBasicName() 
    {
        String t_strName = "";
        
        if ( this.m_bAnomer )
        {
            t_strName = this.m_Monosaccharide.getAnomer().getSymbol();
        }
        if ( this.m_bStereochemistry )
        {
            for (Iterator<BaseType> t_iterBase = this.m_Monosaccharide.getBaseType().iterator(); t_iterBase.hasNext();) 
            {
                if ( t_strName.length() != 0 )
                {
                    t_strName += "-";
                }
                
                if (this.m_bConfiguration){
                    t_strName += t_iterBase.next().getName();    
                }
                else {
                    t_strName += t_iterBase.next().getName().substring(1,4);
                }
                            
            }
        }
        if ( this.m_bSuperclass )
        {
            if ( t_strName.length() != 0 )
            {
                t_strName += "-";
            }
            t_strName += this.m_Monosaccharide.getSuperclass().getName();
        }
        if ( this.m_bRingsize )
        {
            if ( this.m_Monosaccharide.getRingStart() == Monosaccharide.UNKNOWN_RING )
            {
                t_strName +="-x";
            }
            else if ( this.m_Monosaccharide.getRingStart() == Monosaccharide.OPEN_CHAIN )
            {
                t_strName += "-0";
            }
            else
            {
                t_strName += "-" + String.valueOf(this.m_Monosaccharide.getRingStart());
            }
            if ( this.m_Monosaccharide.getRingEnd() == Monosaccharide.UNKNOWN_RING )
            {
                t_strName +=":x";
            }
            else if ( this.m_Monosaccharide.getRingEnd() == Monosaccharide.OPEN_CHAIN )
            {
                t_strName += ":0";
            }
            else
            {
                t_strName += ":" + String.valueOf(this.m_Monosaccharide.getRingEnd());
            }
        }
        if ( this.m_bAllModifications ){
        ArrayList<Modification> a_Modification = new ArrayList<Modification>();
        for (Iterator<Modification> t_iterMod = this.m_Monosaccharide.getModification().iterator(); t_iterMod.hasNext();) 
        {
            a_Modification.add(t_iterMod.next());            
        }
        ComparatorModification cf = new ComparatorModification();
        Collections.sort( a_Modification , cf );
        
        
            for (Iterator<Modification> iter = a_Modification.iterator(); iter.hasNext();) 
            {
                Modification element = iter.next();
                if (element.hasPositionTwo())
                {
                    t_strName += "|" + element.getPositionOne() + "," + element.getPositionTwo()+ ":" + element.getName();    
                }
                else 
                {
                    t_strName += "|" + element.getPositionOne() + ":" + element.getName();
                }
            }
            
            if ( this.m_aModifications.size() > 0 )
            {
                for (Iterator<Modification> iter = a_Modification.iterator(); iter.hasNext();) 
                {
                    Modification element = iter.next();
                    if ( this.m_aModifications.contains(element.getModificationType()) )
                    {
                        if (element.hasPositionTwo())
                        {
                            t_strName += "|" + element.getPositionOne() + "," + element.getPositionTwo()+ ":" + element.getName();    
                        }
                        else 
                        {
                            t_strName += "|" + element.getPositionOne() + ":" + element.getName();
                        }
                    }
                }
            }
        }
        return t_strName;
    }
    
    public void reset()
    {
        m_bAnomer = true;
        m_bAllModifications = true;
        m_bSuperclass = true;
        m_bRingsize = true;
        m_bStereochemistry = true;
        this.m_aModifications.clear();
        
        m_bAllSubstituent = true;
        m_bSubstituentFatherLinkage = true;
        m_bSubstituentChildLinkage = false;
        this.m_aSubstitutents.clear();
    }    
    
    public void displayAnomer(Boolean anomer) 
    {
        m_bAnomer = anomer;
    }    
    
    public void displayAllModifications(Boolean modifications) 
    {
        m_bAllModifications = modifications;
    }    
    
    public void displayRingsize(Boolean ringsize) 
    {
        m_bRingsize = ringsize;
    }    
    
    public void displayStereochemistry(boolean stereochemistry) 
    {
        m_bStereochemistry = stereochemistry;
    }    
    
    public void displayConfiguration (boolean configuration) 
    {
        m_bConfiguration = configuration;
    }    
    
    public void displaySubstituentChildLinkage(Boolean substituentChildLinkage) 
    {
        m_bSubstituentChildLinkage = substituentChildLinkage;
    }    
    
    public void displaySubstituentFatherLinkage(Boolean substituentFatherLinkage) 
    {
        m_bSubstituentFatherLinkage = substituentFatherLinkage;
    }    
    
    public void displayAllSubstituent(Boolean substituentIdentity,Boolean substituentFatherLinkage,Boolean substituentChildLinkage) 
    {
        m_bAllSubstituent = substituentIdentity;
        m_bSubstituentFatherLinkage = substituentFatherLinkage;
        m_bSubstituentChildLinkage = substituentChildLinkage;
    }
    
    public void displayAllSubstituent(Boolean substituentIdentity) 
    {
        m_bAllSubstituent = substituentIdentity;
    }
    
    public void displaySuperclass (Boolean superclass) 
    {
        m_bSuperclass = superclass;
    }
    
    public void addDisplayModification( ModificationType a_objModType)
    {
        this.m_aModifications.add(a_objModType);
    }
    
    public void addDisplaySubstituent( SubstituentType a_objSubType)
    {
        this.m_aSubstitutents.add(a_objSubType);
    }
    
    public void setLinkageBrackets(boolean a_bBrackets)
    {
        this.m_bLinkageBrackets = a_bBrackets;
    }

}