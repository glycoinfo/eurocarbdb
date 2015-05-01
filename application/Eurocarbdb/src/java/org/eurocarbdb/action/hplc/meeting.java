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
import java.util.List;
import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


public class meeting extends EurocarbAction {

    private Profile parent = null;
    private List<HplcPeaksAnnotated> annotated;
    private Set<HplcPeaksAnnotated> peaks;
      
    private int parent_id;
    private int digest_id;
 
    public String execute() throws Exception {

if ( params.isEmpty() ) return INPUT;


         annotated  = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.PROFILE_DIGEST") .list();
        return SUCCESS;
    }      
 
    public Set<HplcPeaksAnnotated> getPeaks() { return peaks; }

    
    public List<HplcPeaksAnnotated> getHplcPeaksAnnotated() {
        return annotated;
    }

    public void setHplcPeaksAnnotated( List<HplcPeaksAnnotated> annotated) {
        this.annotated = annotated;
    }



    /*public Collection getPeaks()
    {
    }
*/
    public List getAnnotated()
    {
    return this.annotated;
    }

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


    public int getDigestId()
    {
        return this.digest_id;
    }

    public void setDigestId( int id )
    {
        this.digest_id = id;
    }



}
