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
package org.eurocarbdb.applications.ms.glycopeakfinder.storage;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Logan
*
*/
public class MassResidue 
{
    private boolean m_bUseAX = false;
    private boolean m_bUseE = false;
    private boolean m_bUseF = false;
    private boolean m_bUseG = false;
    private boolean m_bUseH = false;
    private String m_strName = "";
    private String m_strAbbr = "";
    private int m_iMin = 0;
    private int m_iMax = 0;
    private String m_strID = "";
    private ArrayList<Compound> m_aAX = new ArrayList<Compound>(); 
    private boolean m_bHasAX = false;
    private boolean m_bHasE = false;
    private boolean m_bHasF = false;
    private boolean m_bHasG = false;
    private boolean m_bHasH = false;
    private String m_strPersubstitution = "none";
    private String m_strMassType = "mono";
    private HashMap<String,Double> m_hashMass = new HashMap<String,Double>();
    
    public void setPersubstitution(String a_strPerSub)
    {
        this.m_strPersubstitution = a_strPerSub;
    }
    
    public void setResidueMasses(HashMap<String,Double> a_dMass)
    {
        this.m_hashMass = a_dMass;
    }
    
    public double getMass()
    {
        String t_strKey = "mass_" + this.m_strPersubstitution + "_" + this.m_strMassType;
        if ( !this.m_hashMass.containsKey(t_strKey) )
        {
            return -1000;
        }
        return this.m_hashMass.get(t_strKey);
    }
    
    public void setId(String a_strID)
    {
        this.m_strID = a_strID;
    }
    
    public String getId()
    {
        return this.m_strID;
    }
    
    public void setHasAx(boolean a_bHas)
    {
        this.m_bHasAX = a_bHas;        
    }
    
    public boolean getHasAx()
    {
        return this.m_bHasAX;
    }
    
    public void setUseAX(boolean a_bUse)
    {
        this.m_bUseAX = a_bUse;
    }
    
    public boolean getUseAx()
    {
        return this.m_bUseAX;
    }
    
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
    
    public void setListAx(ArrayList<Compound> a_aAX)
    {
        this.m_aAX = a_aAX;
    }
    
    public ArrayList<Compound> getListAx()
    {
        return this.m_aAX;
    }

    public void setAbbr(String a_strAbbr) 
    {
        this.m_strAbbr = a_strAbbr;
    }
    
    public String getAbbr()
    {
        return this.m_strAbbr;
    }

    public void setMassType(String a_strMassType) 
    {
        this.m_strMassType = a_strMassType;
    }
    
    public void setHasE(boolean a_bHas)
    {
        this.m_bHasE = a_bHas;        
    }
    
    public boolean getHasE()
    {
        return this.m_bHasE;
    }
    
    public void setUseE(boolean a_bUse)
    {
        this.m_bUseE = a_bUse;
    }
    
    public boolean getUseE()
    {
        return this.m_bUseE;
    }
    
    public void setHasG(boolean a_bHas)
    {
        this.m_bHasG = a_bHas;        
    }
    
    public boolean getHasG()
    {
        return this.m_bHasG;
    }
    
    public void setUseG(boolean a_bUse)
    {
        this.m_bUseG = a_bUse;
    }
    
    public boolean getUseG()
    {
        return this.m_bUseG;
    }
    
    public void setHasH(boolean a_bHas)
    {
        this.m_bHasH = a_bHas;        
    }
    
    public boolean getHasH()
    {
        return this.m_bHasH;
    }
    
    public void setUseH(boolean a_bUse)
    {
        this.m_bUseH = a_bUse;
    }
    
    public boolean getUseH()
    {
        return this.m_bUseH;
    }
    
    public void setHasF(boolean a_bHas)
    {
        this.m_bHasF = a_bHas;        
    }
    
    public boolean getHasF()
    {
        return this.m_bHasF;
    }
    
    public void setUseF(boolean a_bUse)
    {
        this.m_bUseF = a_bUse;
    }
    
    public boolean getUseF()
    {
        return this.m_bUseF;
    }
    
}