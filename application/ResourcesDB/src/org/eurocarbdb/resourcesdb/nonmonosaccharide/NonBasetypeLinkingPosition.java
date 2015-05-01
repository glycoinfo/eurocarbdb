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
package org.eurocarbdb.resourcesdb.nonmonosaccharide;

import org.eurocarbdb.resourcesdb.atom.Atom;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;

public class NonBasetypeLinkingPosition {
    
    private int position;
    private Atom linkedAtom;
    private Atom replacedAtom;
    private Double bondOrder;
    private LinkageType defaultLinktype;
    
    private int dbId;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public NonBasetypeLinkingPosition() {
        this.init();
    }
    
    public NonBasetypeLinkingPosition(int pos, Atom linkedA, Atom replacedA, Double bo) {
        this.init();
        this.setPosition(pos);
        this.setLinkedAtom(linkedA);
        this.setReplacedAtom(replacedA);
        this.setBondOrder(bo);
    }
    
    public NonBasetypeLinkingPosition(int pos, Atom linkedA, Atom replacedA, Double bo, LinkageType linktype) {
        this.init();
        this.setPosition(pos);
        this.setLinkedAtom(linkedA);
        this.setReplacedAtom(replacedA);
        this.setBondOrder(bo);
        this.setDefaultLinktype(linktype);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int pos) {
        this.position = pos;
    }

    public Double getBondOrder() {
        return this.bondOrder;
    }

    public void setBondOrder(Double bo) {
        this.bondOrder = bo;
    }

    public int getDbId() {
        return this.dbId;
    }

    public void setDbId(int id) {
        this.dbId = id;
    }

    public Atom getLinkedAtom() {
        return this.linkedAtom;
    }

    public void setLinkedAtom(Atom a) {
        this.linkedAtom = a;
    }

    public Atom getReplacedAtom() {
        return this.replacedAtom;
    }

    public void setReplacedAtom(Atom a) {
        this.replacedAtom = a;
    }
    
    public LinkageType getDefaultLinktype() {
        return this.defaultLinktype;
    }

    public void setDefaultLinktype(LinkageType linktype) {
        this.defaultLinktype = linktype;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        this.setPosition(0);
        this.setBondOrder(null);
        this.setLinkedAtom(null);
        this.setReplacedAtom(null);
        this.setDbId(0);
    }

}
