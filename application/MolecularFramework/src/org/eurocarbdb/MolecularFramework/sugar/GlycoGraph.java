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
package org.eurocarbdb.MolecularFramework.sugar;


import java.util.ArrayList;
import java.util.Iterator;

/**
* @author Logan
*
*/
public interface GlycoGraph 
{
    public boolean addEdge(GlycoNode a_objParent, GlycoNode a_objChild, GlycoEdge a_objLinkage)  throws GlycoconjugateException;
    
    /**
     * Adds a Residue to the sugar. The linkage and the parent Residue is given.
     * @param a_objParent
     * @param a_objLinkage
     * @param a_objChild
     */
    public boolean addNode(GlycoNode a_objParent, GlycoEdge a_objLinkage, GlycoNode a_objChild)  throws GlycoconjugateException;
    
    /**
     * Adds a Residue to the sugar.
     * @param a_objResidue
     * @throws GlycoconjugateException 
     */
    public boolean addNode(GlycoNode a_objResidue) throws GlycoconjugateException;
    
    /**
     * Delivers all residues that do not have a parent residue.
     * @return Arraylist of all residues
     */
    public ArrayList<GlycoNode> getRootNodes() throws GlycoconjugateException;
    
    /**
     * Delivers an iterator over all RESIDUES of the sugar
     * @return Iterator
     */
    public Iterator<GlycoNode> getNodeIterator();
    
    /**
     * Returns true if all residues of the SugarUnit are connected to one tree  
     * 
     * @return
     */
    public boolean isConnected()  throws GlycoconjugateException;
    
    public boolean removeNode(GlycoNode a_objNode) throws GlycoconjugateException;
    
    public boolean removeEdge(GlycoEdge a_objEdge) throws GlycoconjugateException;
    
    public ArrayList<GlycoNode> getNodes();
    
    public boolean containsNode(GlycoNode a_objNode);
    
    public boolean isParent(GlycoNode a_objParent, GlycoNode a_objNode);
}
