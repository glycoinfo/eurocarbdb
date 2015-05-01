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
package org.eurocarbdb.MolecularFramework.io.iupac;

import java.util.ArrayList;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.SugarImporterText;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;

/**
* Galb-4(Fuca-3)GlcNAcb-2Mana-
* or
* Galb1-4(Fuca1-3)GlcNAcb1-2Mana-
* 
*  start         ::= residue [linkageposition] "-" { linkageposition { subbranch } residue [linkageposition] "-" }
*  linkageposition ::=    number | "?"
*  resiude        ::= symbol { symbol }
*  subbranch    ::= "(" fullresidue { { subbranch } fullresidue } ")"
*  fullresidue ::= residue [ linkageposition ] "-" linkageposition  
*  symbol        ::= character | "?" | number
* 
* Galb4(Fuca3)GlcNAcb2Mana-
* 
*/
public class SugarImporterIupacShortV1 extends SugarImporterText
{
    /**
     * start         ::= residue [linkageposition] "-" { linkageposition { subbranch } residue [linkageposition] "-" } 
     */
    protected void start() throws SugarImporterException 
    {
        try 
        {
            this.clear();
            UnvalidatedGlycoNode t_objResiduumChild = new UnvalidatedGlycoNode();
            UnvalidatedGlycoNode t_objResiduumParent = new UnvalidatedGlycoNode();
            GlycoEdge t_objEdge = new GlycoEdge();
            Linkage t_objLinkage = new Linkage();
            int t_iStartPosition = this.m_iPosition;
            // residue 
            this.residue();
            String t_strResidueName = this.m_strText.substring( t_iStartPosition , this.m_iPosition );
            t_objResiduumChild.setName( t_strResidueName );
            this.m_objSugar.addNode(t_objResiduumChild);
            // residue [ linkageposition ]
            if ( this.m_cToken != '-' )
            {
                t_objLinkage.addChildLinkage(this.linkageposition());    
            }            
            else
            {
                t_objLinkage.addChildLinkage(Linkage.UNKNOWN_POSITION);
            }
            if ( this.m_cToken == '-' )
            {
                this.nextToken();
            }
            else if ( this.m_cToken == '$' )
            {}
            else
            {
                throw new SugarImporterException("IUPAC005", this.m_iPosition);
            }            
            while ( this.m_cToken != '$' )
            {
                t_objLinkage.addParentLinkage(this.linkageposition());
                t_objEdge.addGlycosidicLinkage(t_objLinkage);
                ArrayList<IupacSubTree> t_aIupacSubtree = new ArrayList<IupacSubTree>();
                while ( this.m_cToken == '(' )
                {
                    t_aIupacSubtree.add(this.subbranch());                
                }
                t_iStartPosition = this.m_iPosition;
                this.residue();
                t_strResidueName = this.m_strText.substring( t_iStartPosition , this.m_iPosition );
                t_objResiduumParent.setName( t_strResidueName );
                this.m_objSugar.addNode(t_objResiduumParent);
                this.m_objSugar.addEdge(t_objResiduumParent, t_objResiduumChild, t_objEdge);
                // add subtrees
                for (Iterator<IupacSubTree> t_iterSubtree = t_aIupacSubtree.iterator(); t_iterSubtree.hasNext();) 
                {
                    IupacSubTree t_objTree = t_iterSubtree.next();
                    this.m_objSugar.addEdge(t_objResiduumParent, t_objTree.getGlycoNode(), t_objTree.getGlycoEdge());
                }
                t_objResiduumChild = t_objResiduumParent;
                t_objResiduumParent = new UnvalidatedGlycoNode();
                t_objEdge = new GlycoEdge();
                t_objLinkage = new Linkage();
                if ( this.m_cToken != '-' && this.m_cToken != '$' )
                {
                    t_objLinkage.addChildLinkage(this.linkageposition());
                }
                else
                {
                    t_objLinkage.addChildLinkage(Linkage.UNKNOWN_POSITION);
                }
                if ( this.m_cToken == '-' )
                {
                    this.nextToken();
                }
                else if ( this.m_cToken == '$' )
                {}
                else
                {
                    throw new SugarImporterException("IUPAC005", this.m_iPosition);
                }            
            }
            if ( ! this.finished() )
            {
                throw new SugarImporterException("IUPAC002", this.m_iPosition);
            }
        }
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("COMMON013", this.m_iPosition);
        }

    }

    /**
     * subbranch    ::= "(" fullresidue { { subbranch } fullresidue } ")"
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private IupacSubTree subbranch() throws SugarImporterException, GlycoconjugateException
    {
        IupacSubTree t_objTreeChild;
        IupacSubTree t_objTreeParent;
        if ( this.m_cToken != '(' )
        {
            throw new SugarImporterException("IUPAC000", this.m_iPosition);
        }
        this.nextToken();
        t_objTreeChild = this.fullresidue();
        this.m_objSugar.addNode(t_objTreeChild.getGlycoNode());
        while ( this.m_cToken != ')' )
        {
            ArrayList<IupacSubTree> t_aIupacSubtree = new ArrayList<IupacSubTree>();
            while ( this.m_cToken == '(' )
            {
                t_aIupacSubtree.add(this.subbranch());                
            }
            t_objTreeParent = this.fullresidue();
            this.m_objSugar.addNode(t_objTreeChild.getGlycoNode());
            this.m_objSugar.addEdge(t_objTreeParent.getGlycoNode(), t_objTreeChild.getGlycoNode(), t_objTreeChild.getGlycoEdge());
            // add subtrees
            for (Iterator<IupacSubTree> t_iterSubtree = t_aIupacSubtree.iterator(); t_iterSubtree.hasNext();) 
            {
                IupacSubTree t_objTree = t_iterSubtree.next();
                this.m_objSugar.addEdge(t_objTreeParent.getGlycoNode(), t_objTree.getGlycoNode(), t_objTree.getGlycoEdge());
            }
            t_objTreeChild = t_objTreeParent;
        }
        this.nextToken();
        return t_objTreeChild;
    }

    /**
     * fullresidue ::= residue [ linkageposition ] "-" linkageposition  
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private IupacSubTree fullresidue() throws SugarImporterException, GlycoconjugateException 
    {
        IupacSubTree t_objTree = new IupacSubTree();
        UnvalidatedGlycoNode t_objNode = new UnvalidatedGlycoNode();
        GlycoEdge t_objEdge = new GlycoEdge();
        Linkage t_objLinkage = new Linkage();
        int t_iStartPosition = this.m_iPosition;
        // residue 
        this.residue();
        String t_strResidueName = this.m_strText.substring( t_iStartPosition , this.m_iPosition );
        t_objNode.setName( t_strResidueName );
        this.m_objSugar.addNode(t_objNode);
        // residue [ linkageposition ]
        if ( this.m_cToken != '-' )
        {
            t_objLinkage.addChildLinkage(this.linkageposition());
        }
        else
        {
            t_objLinkage.addChildLinkage(Linkage.UNKNOWN_POSITION);
        }
        if ( this.m_cToken != '-' )
        {
            throw new SugarImporterException("IUPAC005", this.m_iPosition);
        }
        this.nextToken();
        t_objLinkage.addParentLinkage(this.linkageposition());
        t_objEdge.addGlycosidicLinkage(t_objLinkage);
        t_objTree.setGlycoEdge(t_objEdge);
        t_objTree.setGlycoNode(t_objNode);
        return t_objTree;
    }

    /**
     * linkageposition ::=    number | "?"
     * @throws SugarImporterException 
     */
    private int linkageposition() throws SugarImporterException 
    {
        if ( this.m_cToken == '?' )
        {
            this.nextToken();
            return Linkage.UNKNOWN_POSITION;
        }
        else
        {
            return this.number();
        }
    }

    /**
     * resiude        ::= symbol { symbol }
     * @throws SugarImporterException 
     */
    private void residue() throws SugarImporterException 
    {
        boolean t_bLoop = true;
        do 
        {
            this.symbol();
            if ( this.m_cToken == '-' )
            {
                t_bLoop = false;
            }
            else if ( this.m_cToken == '?' )
            {
                if ( this.m_iLength > this.m_iPosition + 1 )
                {
                    if ( this.m_strText.charAt( this.m_iPosition + 1) == '-' )
                    {
                        t_bLoop = false;
                    }
                }
            } 
            else if ( this.m_cToken == '$' )
            {
                t_bLoop = false;
            }
            else
            {
                boolean t_bNumber = true;
                int t_iCounter = 0;
                do
                {
                    if ( this.m_iLength > this.m_iPosition + t_iCounter )
                    {
                        int t_iDigit = (int) this.m_strText.charAt( this.m_iPosition + t_iCounter );
                        if ( t_iDigit > 47 && t_iDigit < 58 )
                        {
                            t_bNumber = true;
                        }
                        else if ( this.m_strText.charAt( this.m_iPosition + t_iCounter ) == '-' )
                        {
                            t_bNumber = false;
                            t_bLoop = false;
                        }
                        else
                        {
                            t_bNumber = false;
                        }
                    }
                    t_iCounter++;
                } while ( t_bNumber );
            }
        } while (t_bLoop);
    }

    /**
     * symbol        ::= character | "?" | number
     * @throws SugarImporterException 
     */
    private void symbol() throws SugarImporterException 
    {
        if ( this.m_cToken == '?' )
        {
            this.nextToken();
        }
        else
        {
            int t_iDigit = (int) this.m_cToken;;
            if ( t_iDigit > 47 && t_iDigit < 58 )
            {
                this.number();
            }
            else
            {
                this.character();
            }
        }
    }

    private void clear() 
    {}
}
