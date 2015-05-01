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
package org.eurocarbdb.MolecularFramework.io.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.SugarImporterText;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;

/**
* http://www.glycominds.com/index.asp?menu=Research&page=glycoit#
* 
* @author Logan
*
*/
public class SugarImporterCFG extends SugarImporterText
{
    private boolean m_bAllowCyclic = true;
    private HashMap<Integer,CFGUnderdeterminedTree> m_hashSubtrees = new HashMap<Integer,CFGUnderdeterminedTree>(); 
    /* (non-Javadoc)
     * @see org.eurocarbdb.MolecularFramework.io.SugarImporterText#start()
     */
    @Override
    protected void start() throws SugarImporterException 
    {
        this.m_hashSubtrees.clear();
        // split up uncertain terminal definitions
        String[] t_aStrings = this.m_strText.split("%\\|");
        int t_iMax = t_aStrings.length - 1;
        this.m_bAllowCyclic = true;
        for (int t_iCounter = 0; t_iCounter < t_iMax; t_iCounter++) 
        {
            // store uncertain terminal residue
            this.m_strText = t_aStrings[t_iCounter]+ "$";
            this.m_iPosition = -1;
            this.m_iLength = this.m_strText.length();
            this.nextToken();
            CFGUnderdeterminedTree t_objTree = this.parseUnderdetermined();
            this.m_hashSubtrees.put(t_objTree.getId(),t_objTree);
            if ( this.m_cToken != '$' )
            {
                throw new SugarImporterException("CFG007",-2);
            }
        }
        this.m_bAllowCyclic = true;
        this.m_strText = t_aStrings[t_aStrings.length-1];
        this.m_iPosition = -1;
        this.m_iLength = this.m_strText.length();
        // start parsing
        this.nextToken();
        this.mainchain(this.m_objSugar);
        // finished ?
        if ( this.m_cToken != '$' )
        {
            throw new SugarImporterException("CFG005",-1);
        }
    }

    /**
     * @param subtree
     * @return
     * @throws SugarImporterException 
     * @throws  
     */
    private CFGUnderdeterminedTree parseUnderdetermined() throws SugarImporterException 
    {
        CFGUnderdeterminedTree t_objCFGSubTree = new CFGUnderdeterminedTree();
        UnderdeterminedSubTree t_objSubTree = new UnderdeterminedSubTree();
        // parse 
        CFGSubTree t_objBranch = this.subbranch(t_objSubTree);
        if ( t_objBranch.getId() != null )
        {
            throw new SugarImporterException("CFG012",this.m_iPosition);
        }
        GlycoEdge t_objEdge = t_objBranch.getGlycoEdge();
        for (Iterator<Linkage> t_iterLinkage = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();) 
        {
            Linkage t_objLink = t_iterLinkage.next();
            try 
            {
                t_objLink.setParentLinkageType(LinkageType.H_AT_OH);
                t_objLink.setChildLinkageType(LinkageType.DEOXY);
            } 
            catch (GlycoconjugateException e) 
            {}            
        }
        t_objSubTree.setConnection(t_objEdge);        
        // parse = and number
        if ( this.m_cToken != '=' )
        {
            throw new SugarImporterException("CFG006",this.m_iPosition);
        }
        this.nextToken();
        t_objCFGSubTree.setId(this.number());
        t_objCFGSubTree.setTree(t_objSubTree);
        return t_objCFGSubTree;
    }

    // <residuename> { <position> { <subbranch> } <residuename> }
    private void mainchain(GlycoGraph a_objGraph) throws SugarImporterException 
    {        
        try 
        {
            int t_iStartPosition = 0;
            CFGSubTree t_objSubTreeMain = new CFGSubTree();
            ArrayList<CFGSubTree> t_aSubTrees = new ArrayList<CFGSubTree>();
            ArrayList<Integer> t_aPostions;
            UnvalidatedGlycoNode t_objNode;
            int t_iDigit = (int)this.m_cToken;
            if ( t_iDigit > 47 && t_iDigit < 58 )
            {
                // uncertain subtree
                t_objSubTreeMain.setId(this.number());
                if ( this.m_cToken != '%' )
                {
                    throw new SugarImporterException("CFG004",this.m_iPosition);
                }  
                this.nextToken();
            }
            else
            {                
                t_iStartPosition = this.m_iPosition;
                this.residuename();
                t_objNode = new UnvalidatedGlycoNode();
                t_objNode.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
                a_objGraph.addNode(t_objNode);
                t_objSubTreeMain.setGlycoNode(t_objNode);
            }
            while ( this.m_cToken != '$' && this.m_cToken != '#' && this.m_cToken != ';' && this.m_cToken != ':' )
            {
                t_aSubTrees.add(t_objSubTreeMain);
                if ( t_objSubTreeMain.getId() == null )
                {
                    t_aPostions = this.position();
                    Linkage t_objLinkage = new Linkage();
                    t_objLinkage.addChildLinkage(1);
                    t_objLinkage.setParentLinkages(t_aPostions);
                    GlycoEdge t_objEdge = new GlycoEdge();
                    t_objEdge.addGlycosidicLinkage(t_objLinkage);
                    t_objSubTreeMain.setGlycoEdge(t_objEdge);
                }
                while ( this.m_cToken == '(' )
                {
                    this.nextToken();
                    t_aSubTrees.add(this.subbranch(a_objGraph));
                    if ( this.m_cToken != ')' )
                    {
                        throw new SugarImporterException("CFG004",this.m_iPosition);
                    }
                    this.nextToken();
                }
                // create parent residue
                t_iStartPosition = this.m_iPosition;
                this.residuename();
                t_objNode= new UnvalidatedGlycoNode();
                t_objNode.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
                a_objGraph.addNode(t_objNode);
                // add branches
                for (Iterator<CFGSubTree> t_iterSubTrees = t_aSubTrees.iterator(); t_iterSubTrees.hasNext();) 
                {
                    CFGSubTree t_objSubTree = t_iterSubTrees.next();
                    if ( t_objSubTree.getId() == null )
                    {
                        // normal branch
                        a_objGraph.addEdge(t_objNode,t_objSubTree.getGlycoNode(),t_objSubTree.getGlycoEdge());
                    }
                    else
                    {
                        // uncertain subtree
                        CFGUnderdeterminedTree t_objUTree = this.m_hashSubtrees.get(t_objSubTree.getId());
                        try 
                        {
                            this.addUncertainBranch(a_objGraph,t_objUTree,t_objNode);    
                        } 
                        catch (GlycoconjugateException e) 
                        {
                            throw new SugarImporterException("CFG011",this.m_iPosition);
                        }                            
                    }
                }
                t_aSubTrees.clear();
                t_objSubTreeMain.setGlycoNode(t_objNode);
                t_objSubTreeMain.setId(null);
            }
            // aglyca ?
            if ( this.m_cToken == '#' || this.m_cToken == ';' || this.m_cToken == ':' )
            {
                t_iStartPosition = this.m_iPosition;
                while ( this.m_cToken != '$' )
                {                    
                    this.nextToken();
                }
                t_objNode = new UnvalidatedGlycoNode();
                t_objNode.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
                a_objGraph.addNode(t_objNode);
                Linkage t_objLinkage = new Linkage();
                t_objLinkage.addChildLinkage(1);
                t_objLinkage.addParentLinkage(Linkage.UNKNOWN_POSITION);
                GlycoEdge t_objEdge = new GlycoEdge();
                t_objEdge.addGlycosidicLinkage(t_objLinkage);
                a_objGraph.addEdge(t_objNode,t_objSubTreeMain.getGlycoNode(),t_objEdge);
            }
        }
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("COMMON013",this.m_iPosition);
        }
    }

    /**
     * @param graph
     * @return
     * @throws SugarImporterException 
     */
    private ArrayList<Integer> position() throws SugarImporterException 
    {
        ArrayList<Integer> t_aPositions = new ArrayList<Integer>(); 
        if ( this.m_cToken == '?' )
        {
            t_aPositions.add(Linkage.UNKNOWN_POSITION);
            this.nextToken();
        }
        else
        {
            t_aPositions.add(this.number());
            while ( this.m_cToken == '/' )
            {
                this.nextToken();
                t_aPositions.add(this.number());
            }
        }
        return t_aPositions;
    }

    /**
     * @param graph
     * @throws SugarImporterException 
     */
    private void residuename() throws SugarImporterException     
    {
        if ( this.m_cToken == '?' )
        {
            this.nextToken();
            // modification ?
            if ( this.m_cToken == '[' )
            {
                this.modification();
            }
            // unknown residue
            if ( this.m_cToken == '?' || this.m_cToken == 'a' || this.m_cToken == 'b'  || this.m_cToken == 'o' )
            {
                this.nextToken();
            }
            else
            {
                if ( this.m_cToken != '$' && this.m_cToken != ';' && this.m_cToken != ':' && this.m_cToken != '#' )
                {
                    throw new SugarImporterException("CFG000",this.m_iPosition);
                }
            }
        }
        else
        {
            boolean t_bNameMissing = true;
            while ( (this.m_cToken >= 'A' && this.m_cToken <= 'Z') || this.m_cToken == '\'' || this.m_cToken == '^' || this.m_cToken == '~' )
            {
                this.nextToken();
                t_bNameMissing = false;
            }
            if ( t_bNameMissing )
            {
                throw new SugarImporterException("CFG001",this.m_iPosition);
            }
            if ( this.m_cToken == '[' )
            {
                this.modification();
            }
            if ( this.m_cToken == '?' || this.m_cToken == 'a' || this.m_cToken == 'b' || this.m_cToken == 'o' )
            {
                this.nextToken();
            }
            else
            {
                if ( this.m_cToken != '$' && this.m_cToken != ';' && this.m_cToken != ':' && this.m_cToken != '#' )
                {
                    throw new SugarImporterException("CFG000",this.m_iPosition);
                }
            }
        }
    }

    /**
     * @param node
     * @param graph
     * @throws SugarImporterException 
     */
    private void modification() throws SugarImporterException 
    {
        int t_iDigit;
        if ( this.m_cToken != '[' )
        {
            throw new SugarImporterException("CFG002",this.m_iPosition);
        }
        this.nextToken();
        while ( this.m_cToken != ']' )
        {
            if ( this.m_cToken < 'A' || this.m_cToken > 'Z' )
            {
                if ( this.m_cToken < 'a' || this.m_cToken > 'z' )
                {
                    t_iDigit = (int)this.m_cToken;
                    if ( t_iDigit < 48 && t_iDigit > 57 )
                    {
                        if ( this.m_cToken != '*' )
                        {
                            throw new SugarImporterException("CFG003",this.m_iPosition);
                        }
                    }
                }
            }
            this.nextToken();
        }
        this.nextToken();
    }

    // <residuename> <position> { { <subbranch> } <residuename> <position> }
    private CFGSubTree subbranch(GlycoGraph a_objGraph) throws SugarImporterException 
    {
        CFGSubTree t_objSubTreeMain = new CFGSubTree();
        ArrayList<CFGSubTree> t_aSubTrees = new ArrayList<CFGSubTree>();
        UnvalidatedGlycoNode t_objNode;
        ArrayList<Integer> t_aPostions;
        try 
        {
            int t_iDigit = (int)this.m_cToken;
            if ( t_iDigit > 47 && t_iDigit < 58 )
            {
                // uncertain subtree
                t_objSubTreeMain.setId(this.number());
                if ( this.m_cToken != '%' )
                {
                    throw new SugarImporterException("CFG004",this.m_iPosition);
                }  
                this.nextToken();
            }
            else
            {
                // <residuename> 
                int t_iStartPosition = this.m_iPosition;
                this.residuename();
                t_objNode = new UnvalidatedGlycoNode();
                t_objNode.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
                a_objGraph.addNode(t_objNode);
                // <residuename> <position> 
                t_aPostions = this.position();
                Linkage t_objLinkage = new Linkage();
                t_objLinkage.addChildLinkage(1);
                t_objLinkage.setParentLinkages(t_aPostions);
                GlycoEdge t_objEdge = new GlycoEdge();
                t_objEdge.addGlycosidicLinkage(t_objLinkage);
                t_objSubTreeMain.setGlycoEdge(t_objEdge);
                t_objSubTreeMain.setGlycoNode(t_objNode);
            }
            while ( this.m_cToken != ')' && this.m_cToken != '=' )
            {
                t_aSubTrees.add(t_objSubTreeMain);
                // <residuename> <position> {  }
                while ( this.m_cToken == '(' )
                {
                    // <residuename> <position> { { <subbranch> } }
                    this.nextToken();
                    t_aSubTrees.add(this.subbranch(a_objGraph));
                    if ( this.m_cToken != ')' )
                    {
                        throw new SugarImporterException("CFG004",this.m_iPosition);
                    }
                    this.nextToken();
                }
                // <residuename> <position> { { <subbranch> } <residuename> }
                int t_iStartPosition = this.m_iPosition;
                this.residuename();
                t_objNode = new UnvalidatedGlycoNode();
                t_objNode.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
                a_objGraph.addNode(t_objNode);
                // add branches
                for (Iterator<CFGSubTree> t_iterSubTrees = t_aSubTrees.iterator(); t_iterSubTrees.hasNext();) 
                {
                    CFGSubTree t_objSubTree = t_iterSubTrees.next();
                    if ( t_objSubTree.getId() == null )
                    {
                        // normal branch
                        a_objGraph.addEdge(t_objNode,t_objSubTree.getGlycoNode(),t_objSubTree.getGlycoEdge());
                    }
                    else
                    {
                        // uncertain subtree
                        CFGUnderdeterminedTree t_objUTree = this.m_hashSubtrees.get(t_objSubTree.getId());
                        try 
                        {
                            this.addUncertainBranch(a_objGraph,t_objUTree,t_objNode);    
                        } 
                        catch (GlycoconjugateException e) 
                        {
                            throw new SugarImporterException("CFG011",this.m_iPosition);
                        }                            
                    }
                }
                t_aSubTrees.clear();
                // <residuename> <position> { { <subbranch> } <residuename> <position> }
                t_aPostions = this.position();
                t_objSubTreeMain.setId(null);
                t_objSubTreeMain.setGlycoNode(t_objNode);
                Linkage t_objLinkage = new Linkage();
                t_objLinkage.addChildLinkage(1);
                t_objLinkage.setParentLinkages(t_aPostions);
                GlycoEdge t_objEdge = new GlycoEdge();
                t_objEdge.addGlycosidicLinkage(t_objLinkage);
                t_objSubTreeMain.setGlycoEdge(t_objEdge);
            }
        }
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("COMMON013",this.m_iPosition);
        }
        return t_objSubTreeMain;
    }

    /**
     * @param graph
     * @param tree
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void addUncertainBranch(GlycoGraph a_objGraph, CFGUnderdeterminedTree a_objTree,GlycoNode a_objNode) throws SugarImporterException, GlycoconjugateException 
    {
        if ( a_objTree == null )
        {
            throw new SugarImporterException("CFG009",this.m_iPosition);
        }
        if ( a_objGraph.getClass() == Sugar.class )
        {
            Sugar t_objSugar = (Sugar)a_objGraph;
            if ( !a_objTree.isAdded() )
            {
                t_objSugar.addUndeterminedSubTree(a_objTree.getTree());
                a_objTree.setAdded(true);
            }
            t_objSugar.addUndeterminedSubTreeParent(a_objTree.getTree(),a_objNode);
        }
        else if ( a_objGraph.getClass() == SugarUnitRepeat.class )
        {
            SugarUnitRepeat t_objSugar = (SugarUnitRepeat)a_objGraph;
            if ( !a_objTree.isAdded() )
            {
                t_objSugar.addUndeterminedSubTree(a_objTree.getTree());
                a_objTree.setAdded(true);
            }
            t_objSugar.addUndeterminedSubTreeParent(a_objTree.getTree(),a_objNode);
        }
        else
        {
            throw new SugarImporterException("CFG010",this.m_iPosition);
        }        
    }
}
