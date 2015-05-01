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
package org.eurocarbdb.resourcesdb.glycoconjugate_derived;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eurocarbdb.resourcesdb.glycoconjugate_derived.GlycoconjugateException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.ComparatorModification;

/**
* 
* @author rene
* TODO: 
* - addModificaiton prüfen ob diese modifications hinzugefügt werden darf
*/
public class EcdbMonosaccharide 
{
    private     EcdbAnomer      m_enumAnomer;
    private     ArrayList<EcdbBaseType> m_aBaseType = new ArrayList<EcdbBaseType>();
    private     EcdbSuperclass  m_enumSuperclass;
    // both -1 for unknown ; both 0 vor open ring 
    public static final int UNKNOWN_RING   = -1;
    public static final int OPEN_CHAIN   = 0;
    private     int         m_iRingStart;
    private     int         m_iRingEnd;
    private     ArrayList<EcdbModification> m_aModifications = new ArrayList<EcdbModification>();
    
    public EcdbMonosaccharide(EcdbAnomer a_enumAnomer, EcdbSuperclass a_enumSuperclass) throws GlycoconjugateException
    {
        if ( a_enumAnomer == null )
        {
            throw new GlycoconjugateException("Anomer can't be null");
        }
        this.m_enumAnomer = a_enumAnomer;
        if ( a_enumSuperclass == null )
        {
            throw new GlycoconjugateException("Superclass can't be null");
        }
        this.m_enumSuperclass = a_enumSuperclass;
        this.m_iRingEnd = -1;
        this.m_iRingStart = -1;
        this.m_aModifications.clear();
        this.m_aBaseType.clear();
    }
    
    public void setAnomer(EcdbAnomer a_enumAnomer) throws GlycoconjugateException
    {
        if ( a_enumAnomer == null )
        {
            throw new GlycoconjugateException("Anomer can't be null");
        }
        this.m_enumAnomer = a_enumAnomer;
    }
    
    public void setSuperclass( EcdbSuperclass a_enumSuperclass ) throws GlycoconjugateException
    {
        if ( a_enumSuperclass == null )
        {
            throw new GlycoconjugateException("Superclass can't be null");
        }
        this.m_enumSuperclass = a_enumSuperclass;
    }
    
    public EcdbSuperclass getSuperclass()
    {
        return this.m_enumSuperclass;
    }
    
    /**
     * 
     * @return Anomer or null if not validated
     */
    
    
    public EcdbAnomer getAnomer() 
    {
        return this.m_enumAnomer;
    }
    
    /**
     * -1 ; -1 for unknown
     *  0 ;  0 for open chain
     * @param a_iStart
     * @param a_iEnd
     * @return
     * @throws GlycoconjugateException 
     */
    public void setRing(int a_iStart, int a_iEnd) throws GlycoconjugateException
    {
        if ( a_iStart > a_iEnd )
        {
            throw new GlycoconjugateException("Endpoint must be larger than startpoint");
        }
        if ( a_iStart < -1 )
        {
            throw new GlycoconjugateException("Startpoint must be equal or larger than -1");
        }
        this.m_iRingStart = a_iStart;
        if ( a_iEnd < -1 )
        {
            throw new GlycoconjugateException("Endpoint must be equal or larger than -1");
        }
        this.m_iRingEnd = a_iEnd;
    }
    
    /**
     * 
     * @return Positive Startposition of the ring or -1 if not validated
     */
    public int getRingStart() 
    {
        return this.m_iRingStart;
    }
    
    /**
     * 
     * @return Positive endposition of the ring or -1 if not validated
     */
    public int getRingEnd() 
    {
        return this.m_iRingEnd;
    }
    
    public ArrayList<EcdbModification> getModificationList()
    {
        return this.m_aModifications;
    }
    
    public int getModificationCount()
    {
        return this.m_aModifications.size();
    }
    
    public EcdbModification getModification(int a_iModification) throws IndexOutOfBoundsException 
    {
        return this.m_aModifications.get(a_iModification);
    }
    
    public void addModification( EcdbModification a_objModification ) throws GlycoconjugateException
    {
        if ( a_objModification == null )
        {
            throw new GlycoconjugateException("Modification can't be null");
        }
        this.m_aModifications.add(a_objModification);
    }
    
    public void removeModification( EcdbModification a_objModification )
    {
        this.m_aModifications.remove(a_objModification);
    }
    
    public ArrayList<EcdbBaseType> getBaseTypeList()
    {
        return this.m_aBaseType;
    }
    
    public int getBaseTypeCount()
    {
        return this.m_aBaseType.size();
    }
    
    public EcdbBaseType getBaseType(int a_iBaseType) throws IndexOutOfBoundsException 
    {
        return this.m_aBaseType.get(a_iBaseType);
    }
    
    public void addBaseType( EcdbBaseType a_objBaseType ) throws GlycoconjugateException
    {
        if ( a_objBaseType == null )
        {
            throw new GlycoconjugateException("Basetype can't be null");
        }
        this.m_aBaseType.add(a_objBaseType);
    }
    
    public void setBaseType(ArrayList<EcdbBaseType> a_basetypeList) {
        this.m_aBaseType = a_basetypeList;
    }
    
    public void removeBaseType( EcdbBaseType a_objBaseType)
    {
        this.m_aBaseType.remove(a_objBaseType);
    }
    
    public String getGlycoCTName()
    {
        String anomer=null;
        String name=null;
        String basetypes="";
        String t_Modifications="";
        String ringstart=null;
        String ringend=null;
        
        
        anomer=this.getAnomer().getSymbol();
        
        
        for (Iterator<?> iter = this.getBaseTypeList().iterator(); iter.hasNext();) {
            EcdbBaseType element = (EcdbBaseType) iter.next();            
            basetypes= basetypes+"-"+element.getName();            
        }
        
        if (this.getRingStart()==-1){
            ringstart="x";
        }
        else {
            ringstart=String.valueOf(this.getRingStart());
        }
        
        if (this.getRingEnd()==-1){
            ringend="x";
        }
        else {
            ringend=String.valueOf(this.getRingEnd());
        }
        
        ComparatorModification cf =new ComparatorModification();
        Collections.sort( this.m_aModifications , cf );
        
        for (Iterator<?> iter = this.getModificationList().iterator(); iter.hasNext();) {
            EcdbModification element = (EcdbModification) iter.next();
            if (element.hasPositionTwo()){
                t_Modifications=t_Modifications+
                "|"+
                element.getPositionOne()+
                ","+
                element.getPositionTwo()+
                ":"+element.getName();    
            }
            else {
                t_Modifications=t_Modifications+
                "|"+
                element.getPositionOne()+
                ":"+element.getName();    
            }
        }
        //remove trailing ","
        if (t_Modifications!="") t_Modifications=t_Modifications.substring(1,t_Modifications.length());
        
        name=    anomer+
        basetypes +                
        "-"+                
        this.getSuperclass()+                
        "-"+                
        ringstart+
        ":"+ 
        ringend;
        
        if (t_Modifications!=""){
            name+="|"+t_Modifications;
        }    
        
        return name;
    }
    
    /**
     * Test if the Monosaccharide is theoretically valid.
     * 
     * Modification positions checked
     * Modification coincidence checked
     * Ring start and end: valid numbers including carbonyl group check 
     * Anomer settings: Open ring - anomer flag
     * Superclass validated against stereochemical descriptor+modifications
     * 
     * @return
     * @throws GlycoconjugateException 
     */
    public boolean isValid() throws GlycoconjugateException
    {
        return true;
    }
    
    /**
     * Calculates the stereocode
     * @return
     * @throws GlycoconjugateException 
     */
    public String getStereoCode() throws GlycoconjugateException
    {
        StringBuilder stereocode=null;
        
        //validate monosaccharide before computing
        if (this.isValid()!=true) 
        {
            throw new GlycoconjugateException("Monosaccharide is invalid, cannot compute stereocode");
        }
        
        //Basetype == null -> Superclass definition 
        if (this.getBaseTypeCount()==0){
            stereocode= new StringBuilder("*");
            Integer count=this.getSuperclass().getNumberOfC();
            for (int i = 0; i < count-1; i++) {
                stereocode.append("*");
            }
            return stereocode.toString();
        }
        
        // If basetype is joker form "xgro" -> full stereocode is ***        
        for (Iterator<?> iter = this.getBaseTypeList().iterator(); iter.hasNext();) {
            EcdbBaseType element = (EcdbBaseType) iter.next();            
            if (element.getStereo().contains("*")){
                stereocode= new StringBuilder("*");
                Integer count=this.getSuperclass().getNumberOfC();
                for (int i = 0; i < count-1; i++) {
                    stereocode.append("*");                    
                }
                return stereocode.toString();
            }
        }
        stereocode= new StringBuilder("0");
        //normal case, defined sugar
        for (Iterator<?> iter = this.getBaseTypeList().iterator(); iter.hasNext();) {            
            EcdbBaseType element = (EcdbBaseType) iter.next();            
            stereocode=stereocode.insert(1,element.getStereo());            
        }
        // append trailing zero
        stereocode.append("0");
        
        //insert modifying zeros for stereolosses
        /* conditions
         * 
         * Terminal modifications do not alter stereocode 
         * If stereocode is already set to zero, do nothing 
         * Pure superclasses are excluded
         * 
         */
        ComparatorModification cf =new ComparatorModification();
        // Sort the modification list
        Collections.sort( this.m_aModifications , cf );
        
        // iterate over all modifications
        for (Iterator<?> iter = this.getModificationList().iterator(); iter.hasNext();) {
            EcdbModification element = (EcdbModification) iter.next();            
            // terminal does nothing -> ignore            
            if (this.getSuperclass().getNumberOfC()==element.getPositionOne() |
                    element.getPositionOne()==1) continue;               
            
            Boolean deoxyTrue = false;
            if (element.hasPositionTwo()){
            for (Iterator<?> iter2 = this.getModificationList().iterator(); iter2.hasNext();) {
                EcdbModification element2 = (EcdbModification) iter2.next();
                if ((element.getPositionOne()==element2.getPositionOne() &&
                        element2.getName()==EcdbModificationType.DEOXY.getName())||
                        (element.getPositionTwo()==element2.getPositionOne() &&
                                element2.getName()==EcdbModificationType.DEOXY.getName()))
                {
                    deoxyTrue=true;
                }                    
            }
            }
                
            // bivalent modification
            if (element.hasPositionTwo() && !deoxyTrue){                
                        stereocode.insert(element.getPositionOne()-1,"1");
                        stereocode.insert(element.getPositionTwo()-1,"1");                
            }
            //monovalent modification
            else {                
                stereocode.insert(element.getPositionOne()-1,"1");
            }
        }
        
        return stereocode.toString();
    }
    /**
     * Calculates a bitfield of free OH-groups, "0" no OH, "1" existing OH
     * @return
     * @throws GlycoconjugateException 
     * @throws GlycoconjugateException 
     */
    public Integer getBitfield() throws GlycoconjugateException
    {
        return null;
    }
    
    
    public String getChemoCentricBitfield() throws GlycoconjugateException
    {
        return null;
    }
    /**     
     * Check if Modification exists in Monosaccharide
     * @returns Boolean 
     * @throws GlycoconjugateException 
     * @see java.lang.Object#clone()
     **/
    
    public Boolean hasModification (EcdbModification a_objModification){
        
        if (this.m_aModifications.contains(a_objModification)) {            
            return true;
        }
        else {
        return false;
        }
    }
    /**     
     * Check if Modification exists in Monosaccharide
     * @returns Boolean 
     * @throws GlycoconjugateException 
     * @see java.lang.Object#clone()
     **/
    public Boolean hasModification(EcdbModificationType a_objModiType, Integer positionOne){
        
        for (EcdbModification m: this.m_aModifications){
            if (m.getName()==a_objModiType.getName() && 
                m.getPositionOne()==positionOne    ){
                return true;
            }
        }        
        return false;
    }
    /**     
     * Check if Modification exists in Monosaccharide
     * @returns Boolean 
     * @throws GlycoconjugateException 
     * @see java.lang.Object#clone()
     **/
    public Boolean hasModification(EcdbModificationType a_objModiType, Integer positionOne, Integer positionTwo){
        
        for (EcdbModification m: this.m_aModifications){
            if (m.getName()==a_objModiType.getName() && 
                m.getPositionOne()==positionOne    &&
                m.getPositionTwo()==positionTwo){
                return true;
            }
        }        
        return false;
    }
    
    public Boolean hasChild(Integer position, LinkageType linkageType){    
        // TODO: Implement check 
        return null;
    }
    
    /**
     * Create a clone of the Monosaccharide. Doesn't clone the linkages.
     * @throws GlycoconjugateException 
     * @see java.lang.Object#clone()
     */
    @Override
    public EcdbMonosaccharide clone() 
    {
        EcdbMonosaccharide t_objMS = null;
        try
        {
            // create new MS with Anomer and Superclass
            t_objMS = new EcdbMonosaccharide( this.m_enumAnomer , this.m_enumSuperclass );
            // ring
            t_objMS.setRing( this.m_iRingStart , this.m_iRingEnd );
            // basetype
            for (Iterator<EcdbBaseType> t_iterBase = this.m_aBaseType.iterator(); t_iterBase.hasNext();)
            {
                t_objMS.addBaseType(t_iterBase.next());
            }
            // modification
            for (Iterator<EcdbModification> t_iterModi = this.m_aModifications.iterator(); t_iterModi.hasNext();)
            {
                t_objMS.addModification(t_iterModi.next());
            }
        } 
        catch (GlycoconjugateException e)
        {
        }
        return t_objMS;
    }
    
    public String toString() {
        String outStr = "[";
        outStr += "Anomer: " + this.getAnomer();
        outStr += ", Basetype: " + this.getBaseTypeList();
        outStr += ", Superclass: " + this.getSuperclass();
        outStr += ", Ring: " + this.getRingStart() + ":" + this.getRingEnd();
        outStr += ", Mod: " + this.getModificationList();
        outStr += "]";
        return outStr;
    }
}