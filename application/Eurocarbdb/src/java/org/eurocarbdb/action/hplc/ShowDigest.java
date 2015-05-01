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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/

package org.eurocarbdb.action.hplc;

import java.util.Set;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.Profile;
import org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;


public class ShowDigest extends EurocarbAction {

    private Profile parent = null;
    private HplcPeaksAnnotated annotated = null;
    private Set<HplcPeaksAnnotated> peaks;
  
    private int parent_id =1 ;
 
    public String execute() throws Exception {



        if ( annotated != null ) {
 
            return INPUT;
    }
    else {
        parent = Eurocarb.getEntityManager().lookup( Profile.class, parent_id );
        if (parent == null) { addFieldError("parent_id", "Invalid parent  id");  return INPUT;}
         
    peaks = parent.getParentProfile().getHplcPeaksAnnotateds();

        
        return SUCCESS;
    }
    }      
 
    public Set<HplcPeaksAnnotated> getPeaks() { return peaks; }

    public HplcPeaksAnnotated getHplcPeaksAnnotated() {
        return annotated;
    }

    public void setHplcPeaksAnnotated (HplcPeaksAnnotated annotated) {
        this.annotated = annotated;
    }



    /*public Collection getPeaks()
    {
    }
*/

    public Profile getProfile() {
        return parent;
    }

    public void setProfile (Profile parent) {
        this.parent = parent;
    }


    public int getProfileId()
    {
        return this.parent_id;
    }

    public void setProfileId( int id )
    {
        this.parent_id = id;
    }


}
