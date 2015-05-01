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
package org.eurocarbdb.resourcesdb.monosaccharide;

public class MonosaccharideLinkingPosition {

    private int dbId = -1;
    private int position = 0;
    private boolean isAnomeric = false;
    private Substitution linkingSubstitution = null;
    private String comment = null;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public MonosaccharideLinkingPosition() {
        this.init();
    }
    
    public MonosaccharideLinkingPosition(int pos) {
        this.init();
        this.setPosition(pos);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public int getDbId() {
        return this.dbId;
    }
    
    public void setDbId(int id) {
        this.dbId = id;
    }
    
    public int getPosition() {
        return this.position;
    }
    
    public void setPosition(int pos) {
        this.position = pos;
    }
    
    public boolean isAnomeric() {
        return this.isAnomeric;
    }
    
    public boolean getIsAnomeric() {
        return this.isAnomeric;
    }
    
    public void setIsAnomeric(boolean flag) {
        this.isAnomeric = flag;
    }
    
    public Substitution getLinkingSubstitution() {
        return this.linkingSubstitution;
    }
    
    public void setLinkingSubstitution(Substitution subst) {
        this.linkingSubstitution = subst;
    }
    
    public String getComment() {
        return this.comment;
    }
    
    public void setComment(String theComment) {
        this.comment = theComment;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        this.dbId = -1;
        this.position = 0;
        this.isAnomeric = false;
        this.linkingSubstitution = null;
        this.comment = null;
    }
    
    public String toString() {
        String outStr = "";
        outStr += "Link.Pos. " + this.getPosition();
        if(this.isAnomeric) {
            outStr +=" (anomeric)";
        }
        if(this.linkingSubstitution != null) {
            outStr += " via subst. " + this.linkingSubstitution.getName();
        }
        if(this.getComment() != null) {
            outStr += " {" + this.getComment() + "}";
        }
        return outStr;
    }
}
