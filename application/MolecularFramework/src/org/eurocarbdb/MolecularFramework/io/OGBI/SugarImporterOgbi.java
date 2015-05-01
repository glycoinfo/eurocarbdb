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
package org.eurocarbdb.MolecularFramework.io.OGBI;

import java.util.ArrayList;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.SugarImporterText;
import org.eurocarbdb.MolecularFramework.sugar.Anomer;
import org.eurocarbdb.MolecularFramework.sugar.BaseType;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.SubstituentType;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.Superclass;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;

/**
* 
* start         ::=    <corefucose> ( <m> | [ 'X' ] <a> )
* corefucose    ::=    ['F' '(' '3' ')'] ['F' '(' '6' ')'] 
* m            ::=    'M' <m_type> [ 'B' ] [ 'X']
* m_type        ::=    '1' | 
*                    '2' <d> |
*                    '3' |
*                    '4' <d> |
*                    '5' |
*                    '6' <d> |
*                     '7' <d> |
*                     '8' <d> |
*                     '9'            
* d            ::= [ 'D' <number> { ',' 'D' <number> } ]
* a            ::= 'A' <number> '['  <number> { ',' <number> } ']' [ 'B' ] [ 'F' [ '(' <number> { ',' <number> } ')' ] <number> [ <branch_rest> ]
* branch_rest    ::= 'G' [ '(' <number> { ',' <number> } ')' ] <number>
* 
*  
*  
* @author rene
*
*/
public class SugarImporterOgbi extends SugarImporterText
{
    private boolean m_bFuc6 = false;
    private boolean m_bFuc3 = false;
    private Monosaccharide m_objCoreGlcNac;
    private Monosaccharide m_objManCore;
    private Monosaccharide m_objMan3;
    private Monosaccharide m_objMan6;
    private Monosaccharide m_objGlcNAc32;
    private Monosaccharide m_objGlcNAc34;
    private Monosaccharide m_objGlcNAc62;
    private Monosaccharide m_objGlcNAc66;
    private int m_iAtype;
    private UnderdeterminedSubTree m_objAoneUnderGraph = null;
    private Monosaccharide m_objAoneUnderGlcNAc;
    private Integer m_iAbranchPosition = null;
    private ArrayList<Integer> m_aPositionD = new ArrayList<Integer>();
    
    
    /**
     * @see org.eurocarbdb.MolecularFramework.io.SugarImporterText#start()
     */
    @Override
    protected void start() throws SugarImporterException
    {
        try 
        {
            this.clear();
            this.m_objSugar = new Sugar();
            if ( this.m_cToken == 'F' )
            {
                this.coreFuc();
            }
            if ( this.m_cToken == 'F' )
            {
                this.coreFuc();
            }
            if ( this.m_cToken == 'M' )
            {
                this.parseM();
            }
            else if ( this.m_cToken == 'A' )
            {
                this.createCore();
                this.parseA(-1);
            }
            else if ( this.m_cToken == 'X' )
            {
                this.createCore();
                this.nextToken();
                this.addXylose();
                this.parseA(-1);
            }
            // $        
            if ( ! this.finished() )
            {
                throw new SugarImporterException("OGBI009", this.m_iPosition);
            }
        }
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("COMMON013",this.m_iPosition);
        }
    }

    /**
     * a_iType = -1 for complex glycans 
     *         > 0  for hybrid glycans
     * @throws SugarImporterException 
     * 
     */
    private void parseA(int a_iTypeM) throws SugarImporterException, GlycoconjugateException
    {
        if ( this.m_cToken != 'A' )
        {
            throw new SugarImporterException("OGBI007",this.m_iPosition);
        }
        this.nextToken();
        this.m_iAtype = this.natural_number();
        if ( this.m_cToken == '[' )
        {
            this.nextToken();
            this.m_iAbranchPosition = this.natural_number();
            if ( this.m_cToken != ']' )
            {
                throw new SugarImporterException("OGBI010",this.m_iPosition);
            }
            this.nextToken();
        }
        this.addGlcNAcType(a_iTypeM);        
        if ( this.m_cToken == 'B' )
        {
            this.nextToken();
            this.addBiSection();
        }
        // this.m_iAType  stores the a type
        // this.m_objAoneUnderGraph != null if a A1 Underdeterminded unit was created the residue is stored in this.m_objAoneUnderGlcNAc
        // otherwise
        // this.m_objGlcNAc32
        // this.m_objGlcNAc34
        // this.m_objGlcNAc62
        // this.m_objGlcNAc66
        // are set according to the A-Type
        // fucose at glcnac
        if ( this.m_cToken == 'F' )
        {
            this.nextToken();
            Integer t_iFucGlcNAcLinkPosition = null;
            if ( this.m_cToken == '(' )
            {
                this.nextToken();                 
                t_iFucGlcNAcLinkPosition = this.natural_number();                
                if ( this.m_cToken != ')' )
                {
                    throw new SugarImporterException("OGBI014",this.m_iPosition);
                }
                this.nextToken();
            }
            int t_iFucGlcNAcNumber = this.natural_number();
            this.addFucAtGlcNAc(t_iFucGlcNAcNumber,t_iFucGlcNAcLinkPosition);
        }        
        // gal
        if ( this.m_cToken == 'G' )
        {
            this.parseG();
        }
    }

    private void parseG() throws SugarImporterException, GlycoconjugateException
    {
        // this.m_iAType  stores the a type
        // this.m_objAoneUnderGraph != null if a A1 Underdeterminded unit was created the residue is stored in this.m_objAoneUnderGlcNAc
        // otherwise
        // this.m_objGlcNAc32
        // this.m_objGlcNAc34
        // this.m_objGlcNAc62
        // this.m_objGlcNAc66
        // are set to null or an object according to the A-Type
        this.nextToken();
        ArrayList<Integer> t_aGalLinkPosition = new ArrayList<Integer>(); 
        if ( this.m_cToken == '(' )
        {
            this.nextToken();
            t_aGalLinkPosition.add(this.natural_number());
            while ( this.m_cToken != ')' )
            {
                if ( this.m_cToken != ',' )
                {
                    throw new SugarImporterException("OGBI010",this.m_iPosition);
                }
                this.nextToken();
                t_aGalLinkPosition.add(this.natural_number());
            }
            this.nextToken();
        }
        int t_iGalNumber = this.natural_number();
        ArrayList<OgbiResidue> t_aResidues = this.addGalAtGlcNac(t_iGalNumber,t_aGalLinkPosition);
        // fucose at gal
        ArrayList<Integer> t_aFucLinkPosition = new ArrayList<Integer>();
        if ( this.m_cToken == 'F' )
        {
            this.nextToken();
            if ( this.m_cToken == '(' )
            {
                this.nextToken();
                t_aFucLinkPosition.add(this.natural_number());
                while ( this.m_cToken != ')' )
                {
                    if ( this.m_cToken != ',' )
                    {
                        throw new SugarImporterException("OGBI014",this.m_iPosition);
                    }
                    this.nextToken();
                    t_aFucLinkPosition.add(this.natural_number());
                }
                this.nextToken();
            }
            int t_iFucNumber = this.natural_number();
            this.addFucAtGal(t_iFucNumber,t_aFucLinkPosition,t_aResidues);
        }        
        this.afterG(t_aResidues);
    }
    
    private void afterG(ArrayList<OgbiResidue> a_aParents) throws SugarImporterException, GlycoconjugateException
    {
        ArrayList<OgbiResidue> t_aParents = a_aParents;    
        // Ga
        if ( this.m_cToken == 'G' && this.aheadToken(1) == 'a' )
        {
            this.nextToken();
            this.nextToken();
            ArrayList<Integer> t_aGalGalLinkPosition = new ArrayList<Integer>();
            if ( this.m_cToken == '(' )
            {
                this.nextToken();
                t_aGalGalLinkPosition.add(this.natural_number());
                while ( this.m_cToken != ')' )
                {
                    if ( this.m_cToken != ',' )
                    {
                        throw new SugarImporterException("OGBI010",this.m_iPosition);
                    }
                    this.nextToken();
                    t_aGalGalLinkPosition.add(this.natural_number());
                }
                this.nextToken();
            }
            int t_iGalGalNumber = this.natural_number();
            t_aParents = this.addGalGal(t_iGalGalNumber,t_aGalGalLinkPosition,t_aParents);
        }
        // S
        if ( this.m_cToken == 'S' )
        {
            this.nextToken();
            ArrayList<Integer> t_aSiaLinkPositions = new ArrayList<Integer>(); 
            if ( this.m_cToken == '(' )
            {
                this.nextToken();
                t_aSiaLinkPositions.add(this.natural_number());
                while ( this.m_cToken != ')' )
                {
                    if ( this.m_cToken != ',' )
                    {
                        throw new SugarImporterException("OGBI014",this.m_iPosition);
                    }
                    this.nextToken();
                    if ( this.m_cToken == '?' )
                    {
                        t_aSiaLinkPositions.add(Linkage.UNKNOWN_POSITION);
                        this.nextToken();
                    }
                    else
                    {
                        t_aSiaLinkPositions.add(this.natural_number());
                    }
                }
                this.nextToken();
            }
            int t_iSiaNumber = this.natural_number();
            this.addSia(t_iSiaNumber,t_aSiaLinkPositions,t_aParents);
        }    
    }

    /**
     * @param siaNumber
     * @param siaLinkPositions
     * @param parents
     * @throws GlycoconjugateException 
     * @throws SugarImporterException 
     */
    private void addSia(int a_iSiaNumber, ArrayList<Integer> a_aLinkPositions, ArrayList<OgbiResidue> a_aParents) throws GlycoconjugateException, SugarImporterException 
    {
        if ( a_iSiaNumber > a_aParents.size() )
        {
            throw new SugarImporterException("OGBI040",this.m_iPosition);
        }
        if ( a_iSiaNumber == a_aParents.size() )
        {
            if ( a_aLinkPositions.size() < 2 )
            {
                // linkage position unknown or all the same
                int t_iPosition = Linkage.UNKNOWN_POSITION;
                if ( a_aLinkPositions.size() == 1 )
                {
                    t_iPosition = a_aLinkPositions.get(0);
                }
                for (Iterator<OgbiResidue> t_iterParent = a_aParents.iterator(); t_iterParent.hasNext();) 
                {
                    OgbiResidue t_objResidue = t_iterParent.next();
                    Monosaccharide t_objSia = new Monosaccharide(Anomer.Alpha,Superclass.NON);
                    t_objSia.setRing(2,6);
                    t_objSia.addBaseType(BaseType.DGRO);
                    t_objSia.addBaseType(BaseType.DGAL);
                    Modification t_objModi = new Modification(ModificationType.DEOXY,3);
                    t_objSia.addModification(t_objModi);
                    t_objModi = new Modification(ModificationType.ACID,1);
                    t_objSia.addModification(t_objModi);
                    t_objModi = new Modification(ModificationType.KETO,2);
                    t_objSia.addModification(t_objModi);
                    t_objResidue.m_objGraph.addNode(t_objSia);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iPosition,2,LinkageType.DEOXY);
                    t_objResidue.m_objGraph.addEdge(t_objResidue.m_objMS, t_objSia, t_objEdge);
                    Substituent t_objSubst = new Substituent(SubstituentType.N_ACETYL);
                    t_objResidue.m_objGraph.addNode(t_objSubst);
                    t_objEdge = this.createEdge(LinkageType.DEOXY,5,1,LinkageType.NONMONOSACCHARID);
                    t_objResidue.m_objGraph.addEdge(t_objSia, t_objSubst, t_objEdge);
                }
            }
            else
            {
                // all other linkage positions ==> underdeterminded
                if ( a_aParents.get(0).m_bUnderdeterminded )
                {
                    // error nested underdeterminded
                    throw new SugarImporterException("OGBI035",this.m_iPosition);
                }
                if ( a_aLinkPositions.size() != a_iSiaNumber )
                {
                    throw new SugarImporterException("OGBI041",this.m_iPosition);
                }
                for (Iterator<Integer> t_iterPosition = a_aLinkPositions.iterator(); t_iterPosition.hasNext();) 
                {
                    UnderdeterminedSubTree t_objTree = new UnderdeterminedSubTree();
                    Monosaccharide t_objSia = new Monosaccharide(Anomer.Alpha,Superclass.NON);
                    t_objSia.setRing(2,6);
                    t_objSia.addBaseType(BaseType.DGRO);
                    t_objSia.addBaseType(BaseType.DGAL);
                    Modification t_objModi = new Modification(ModificationType.DEOXY,3);
                    t_objSia.addModification(t_objModi);
                    t_objModi = new Modification(ModificationType.ACID,1);
                    t_objSia.addModification(t_objModi);
                    t_objModi = new Modification(ModificationType.KETO,2);
                    t_objSia.addModification(t_objModi);
                    t_objTree.addNode(t_objSia);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iterPosition.next(),2,LinkageType.DEOXY);
                    t_objTree.setConnection(t_objEdge);
                    Substituent t_objSubst = new Substituent(SubstituentType.N_ACETYL);
                    t_objTree.addNode(t_objSubst);
                    t_objEdge = this.createEdge(LinkageType.DEOXY,5,1,LinkageType.NONMONOSACCHARID);
                    t_objTree.addEdge(t_objSia, t_objSubst, t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objTree);
                    for (Iterator<OgbiResidue> t_iterParent = a_aParents.iterator(); t_iterParent.hasNext();) 
                    {
                        OgbiResidue t_objResidue = t_iterParent.next();
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree, t_objResidue.m_objMS);
                    }
                }
            }
        }
        else
        {
            // a_iFucNumber < a_aParents.size()
            if ( a_aLinkPositions.size() < 2 )
            {
                if ( a_aParents.get(0).m_bUnderdeterminded )
                {
                    // error nested underdeterminded
                    throw new SugarImporterException("OGBI035",this.m_iPosition);
                }
                // linkage position unknown or all the same
                int t_iPosition = Linkage.UNKNOWN_POSITION;
                if ( a_aLinkPositions.size() == 1 )
                {
                    t_iPosition = a_aLinkPositions.get(0);
                }
                for (int t_iCounter = 0; t_iCounter < a_iSiaNumber; t_iCounter++) 
                {
                    UnderdeterminedSubTree t_objTree = new UnderdeterminedSubTree();
                    Monosaccharide t_objSia = new Monosaccharide(Anomer.Alpha,Superclass.NON);
                    t_objSia.setRing(2,6);
                    t_objSia.addBaseType(BaseType.DGRO);
                    t_objSia.addBaseType(BaseType.DGAL);
                    Modification t_objModi = new Modification(ModificationType.DEOXY,3);
                    t_objSia.addModification(t_objModi);
                    t_objModi = new Modification(ModificationType.ACID,1);
                    t_objSia.addModification(t_objModi);
                    t_objModi = new Modification(ModificationType.KETO,2);
                    t_objSia.addModification(t_objModi);
                    t_objTree.addNode(t_objSia);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iPosition,2,LinkageType.DEOXY);
                    t_objTree.setConnection(t_objEdge);
                    Substituent t_objSubst = new Substituent(SubstituentType.N_ACETYL);
                    t_objTree.addNode(t_objSubst);
                    t_objEdge = this.createEdge(LinkageType.DEOXY,5,1,LinkageType.NONMONOSACCHARID);
                    t_objTree.addEdge(t_objSia, t_objSubst, t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objTree);
                    for (Iterator<OgbiResidue> t_iterParent = a_aParents.iterator(); t_iterParent.hasNext();) 
                    {
                        OgbiResidue t_objResidue = t_iterParent.next();
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree, t_objResidue.m_objMS);
                    }
                }
            }
            else
            {
                // all other linkage positions ==> underdeterminded
                if ( a_aParents.get(0).m_bUnderdeterminded )
                {
                    // error nested underdeterminded
                    throw new SugarImporterException("OGBI035",this.m_iPosition);
                }
                if ( a_aLinkPositions.size() != a_iSiaNumber )
                {
                    throw new SugarImporterException("OGBI039",this.m_iPosition);
                }
                for (Iterator<Integer> t_iterPosition = a_aLinkPositions.iterator(); t_iterPosition.hasNext();) 
                {
                    UnderdeterminedSubTree t_objTree = new UnderdeterminedSubTree();
                    Monosaccharide t_objSia = new Monosaccharide(Anomer.Alpha,Superclass.NON);
                    t_objSia.setRing(2,6);
                    t_objSia.addBaseType(BaseType.DGRO);
                    t_objSia.addBaseType(BaseType.DGAL);
                    Modification t_objModi = new Modification(ModificationType.DEOXY,3);
                    t_objSia.addModification(t_objModi);
                    t_objModi = new Modification(ModificationType.ACID,1);
                    t_objSia.addModification(t_objModi);
                    t_objModi = new Modification(ModificationType.KETO,2);
                    t_objSia.addModification(t_objModi);
                    t_objTree.addNode(t_objSia);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iterPosition.next(),2,LinkageType.DEOXY);
                    t_objTree.setConnection(t_objEdge);
                    Substituent t_objSubst = new Substituent(SubstituentType.N_ACETYL);
                    t_objTree.addNode(t_objSubst);
                    t_objEdge = this.createEdge(LinkageType.DEOXY,5,1,LinkageType.NONMONOSACCHARID);
                    t_objTree.addEdge(t_objSia, t_objSubst, t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objTree);
                    for (Iterator<OgbiResidue> t_iterParent = a_aParents.iterator(); t_iterParent.hasNext();) 
                    {
                        OgbiResidue t_objResidue = t_iterParent.next();
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree, t_objResidue.m_objMS);
                    }
                }
            }
        }        
    }

    /**
     * @param galNumber
     * @param fucLinkPosition
     * @param residues
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void addFucAtGal(int a_iFucNumber, ArrayList<Integer> a_aFucLinkPosition, ArrayList<OgbiResidue> a_aParents) throws SugarImporterException, GlycoconjugateException 
    {
        if ( a_iFucNumber > a_aParents.size() )
        {
            throw new SugarImporterException("OGBI038",this.m_iPosition);
        }
        if ( a_iFucNumber == a_aParents.size() )
        {
            if ( a_aFucLinkPosition.size() < 2 )
            {
                // linkage position unknown or all the same
                int t_iPosition = Linkage.UNKNOWN_POSITION;
                if ( a_aFucLinkPosition.size() == 1 )
                {
                    t_iPosition = a_aFucLinkPosition.get(0);
                }
                for (Iterator<OgbiResidue> t_iterGal = a_aParents.iterator(); t_iterGal.hasNext();) 
                {
                    OgbiResidue t_objResidue = t_iterGal.next();
                    Monosaccharide t_objFuc = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
                    t_objFuc.setRing(1,5);
                    t_objFuc.addBaseType(BaseType.LGAL);
                    Modification t_objModi = new Modification(ModificationType.DEOXY,6);
                    t_objFuc.addModification(t_objModi);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iPosition,1,LinkageType.DEOXY);
                    t_objResidue.m_objGraph.addNode(t_objFuc);
                    t_objResidue.m_objGraph.addEdge(t_objResidue.m_objMS, t_objFuc, t_objEdge);
                }
            }
            else
            {
                // all other linkage positions ==> underdeterminded
                if ( a_aParents.get(0).m_bUnderdeterminded )
                {
                    // error nested underdeterminded
                    throw new SugarImporterException("OGBI035",this.m_iPosition);
                }
                if ( a_aFucLinkPosition.size() != a_iFucNumber )
                {
                    throw new SugarImporterException("OGBI039",this.m_iPosition);
                }
                for (Iterator<Integer> t_iterPosition = a_aFucLinkPosition.iterator(); t_iterPosition.hasNext();) 
                {
                    UnderdeterminedSubTree t_objTree = new UnderdeterminedSubTree();
                    Monosaccharide t_objFuc = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
                    t_objFuc.setRing(1,5);
                    t_objFuc.addBaseType(BaseType.LGAL);
                    Modification t_objModi = new Modification(ModificationType.DEOXY,6);
                    t_objFuc.addModification(t_objModi);
                    t_objTree.addNode(t_objFuc);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iterPosition.next(),1,LinkageType.DEOXY);
                    t_objTree.setConnection(t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objTree);
                    for (Iterator<OgbiResidue> t_iterParent = a_aParents.iterator(); t_iterParent.hasNext();) 
                    {
                        OgbiResidue t_objResidue = t_iterParent.next();
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree, t_objResidue.m_objMS);
                    }
                }
            }
        }
        else
        {
            // a_iFucNumber < a_aParents.size()
            if ( a_aFucLinkPosition.size() < 2 )
            {
                if ( a_aParents.get(0).m_bUnderdeterminded )
                {
                    // error nested underdeterminded
                    throw new SugarImporterException("OGBI035",this.m_iPosition);
                }
                // linkage position unknown or all the same
                int t_iPosition = Linkage.UNKNOWN_POSITION;
                if ( a_aFucLinkPosition.size() == 1 )
                {
                    t_iPosition = a_aFucLinkPosition.get(0);
                }
                for (int t_iCounter = 0; t_iCounter < a_iFucNumber; t_iCounter++) 
                {
                    UnderdeterminedSubTree t_objTree = new UnderdeterminedSubTree();
                    Monosaccharide t_objFuc = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
                    t_objFuc.setRing(1,5);
                    t_objFuc.addBaseType(BaseType.LGAL);
                    Modification t_objModi = new Modification(ModificationType.DEOXY,6);
                    t_objFuc.addModification(t_objModi);
                    t_objTree.addNode(t_objFuc);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iPosition,1,LinkageType.DEOXY);
                    t_objTree.setConnection(t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objTree);
                    for (Iterator<OgbiResidue> t_iterParent = a_aParents.iterator(); t_iterParent.hasNext();) 
                    {
                        OgbiResidue t_objResidue = t_iterParent.next();
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree, t_objResidue.m_objMS);
                    }
                }
            }
            else
            {
                // all other linkage positions ==> underdeterminded
                if ( a_aParents.get(0).m_bUnderdeterminded )
                {
                    // error nested underdeterminded
                    throw new SugarImporterException("OGBI035",this.m_iPosition);
                }
                if ( a_aFucLinkPosition.size() != a_iFucNumber )
                {
                    throw new SugarImporterException("OGBI039",this.m_iPosition);
                }
                for (Iterator<Integer> t_iterPosition = a_aFucLinkPosition.iterator(); t_iterPosition.hasNext();) 
                {
                    UnderdeterminedSubTree t_objTree = new UnderdeterminedSubTree();
                    Monosaccharide t_objFuc = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
                    t_objFuc.setRing(1,5);
                    t_objFuc.addBaseType(BaseType.LGAL);
                    Modification t_objModi = new Modification(ModificationType.DEOXY,6);
                    t_objFuc.addModification(t_objModi);
                    t_objTree.addNode(t_objFuc);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iterPosition.next(),1,LinkageType.DEOXY);
                    t_objTree.setConnection(t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objTree);
                    for (Iterator<OgbiResidue> t_iterParent = a_aParents.iterator(); t_iterParent.hasNext();) 
                    {
                        OgbiResidue t_objResidue = t_iterParent.next();
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree, t_objResidue.m_objMS);
                    }
                }
            }
        }
    }

    /**
     * @param a_iFucGlcNAcLinkPosition 
     * @param a_iFucGlcNAcNumber 
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     * 
     */
    private void addFucAtGlcNAc(int a_iFucGlcNAcNumber, Integer a_iFucGlcNAcLinkPosition) throws SugarImporterException, GlycoconjugateException 
    {
        if ( this.m_iAtype < a_iFucGlcNAcNumber )
        {
            throw new SugarImporterException("OGBI026",this.m_iPosition);
        }
        int t_iPosition = Linkage.UNKNOWN_POSITION;
        if ( a_iFucGlcNAcLinkPosition != null )
        {
            t_iPosition = a_iFucGlcNAcLinkPosition;
        }
        if ( this.m_iAtype == a_iFucGlcNAcNumber )
        {            
            if ( this.m_objGlcNAc32 != null )
            {
                this.addFuc(this.m_objGlcNAc32,this.m_objSugar,t_iPosition);
            }
            if ( this.m_objGlcNAc34 != null )
            {
                this.addFuc(this.m_objGlcNAc34,this.m_objSugar,t_iPosition);
            }
            if ( this.m_objGlcNAc62 != null )
            {
                this.addFuc(this.m_objGlcNAc62,this.m_objSugar,t_iPosition);
            }
            if ( this.m_objGlcNAc66 != null )
            {
                this.addFuc(this.m_objGlcNAc66,this.m_objSugar,t_iPosition);
            }
            if ( this.m_objAoneUnderGlcNAc != null )
            {
                this.addFuc(this.m_objAoneUnderGlcNAc,this.m_objAoneUnderGraph,t_iPosition);
            }
        }
        else
        {
            // this.m_iAbranchPosition = 3 or 6
            if ( this.m_iAbranchPosition != null )
            {
                if ( this.m_iAbranchPosition == 3 )
                {
                    this.addFuc(this.m_objGlcNAc32,this.m_objSugar,t_iPosition);
                }
                else if ( this.m_iAbranchPosition == 6 )
                {
                    this.addFuc(this.m_objGlcNAc62,this.m_objSugar,t_iPosition);
                }
                else
                {
                    throw new SugarImporterException("OGBI028",this.m_iPosition);
                }
            }
            else
            {
                if ( this.m_objAoneUnderGraph != null )
                {
                    throw new SugarImporterException("OGBI035",this.m_iPosition);
                }
                // Underdetermined
                for (int t_iCounter = 0; t_iCounter < a_iFucGlcNAcNumber; t_iCounter++) 
                {
                    UnderdeterminedSubTree t_objTree = new UnderdeterminedSubTree();
                    Monosaccharide t_objFuc = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
                    t_objFuc.setRing(1,5);
                    t_objFuc.addBaseType(BaseType.LGAL);
                    Modification t_objModi = new Modification(ModificationType.DEOXY,6);
                    t_objFuc.addModification(t_objModi);
                    t_objTree.addNode(t_objFuc);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iPosition,1,LinkageType.DEOXY);
                    t_objTree.setConnection(t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objTree);
                    if ( this.m_objGlcNAc32 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree,this.m_objGlcNAc32);
                    }
                    if ( this.m_objGlcNAc34 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree,this.m_objGlcNAc34);
                    }
                    if ( this.m_objGlcNAc62 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree,this.m_objGlcNAc62);
                    }
                    if ( this.m_objGlcNAc66 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree,this.m_objGlcNAc66);
                    }
                }                
            }
        }
    }

    /**
     * @param monosaccharide
     * @param sugar
     * @param position
     * @throws GlycoconjugateException 
     */
    private void addFuc(Monosaccharide a_objParent, GlycoGraph a_objGraph , int a_iLinkPosition) throws GlycoconjugateException 
    {
        Monosaccharide t_objFuc = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
        t_objFuc.setRing(1,5);
        t_objFuc.addBaseType(BaseType.LGAL);
        Modification t_objModi = new Modification(ModificationType.DEOXY,6);
        t_objFuc.addModification(t_objModi);
        a_objGraph.addNode(t_objFuc);
        GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,a_iLinkPosition,1,LinkageType.DEOXY);
        a_objGraph.addEdge(a_objParent,t_objFuc,t_objEdge);
    }

    /**
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     * 
     */
    private void parseM() throws SugarImporterException, GlycoconjugateException 
    {
        if ( this.m_cToken != 'M' )
        {
            throw new SugarImporterException("OGBI006",this.m_iPosition);
        }
        this.nextToken();
        int t_iTypeM = this.number();
        this.createCoreMan();
        this.addTypeM(t_iTypeM);
        if ( this.m_cToken == 'B' )
        {
            this.nextToken();
            this.addBiSection();
        }
        if ( this.m_cToken == 'X' )
        {
            this.nextToken();
            this.addXylose();
        }
        if ( this.m_cToken == 'A' )
        {
            if ( t_iTypeM == 2 || t_iTypeM == 4 || t_iTypeM == 5 )
            {
                this.parseA(t_iTypeM);
            }
            else
            {
                throw new SugarImporterException("OGBI033",this.m_iPosition);
            }
        }
    }

    private void parseD() throws SugarImporterException 
    {
        if ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        while ( this.m_cToken == 'D' )
        {
            this.nextToken();
            this.m_aPositionD.add(this.natural_number());
            if ( this.m_cToken == ',' )
            {
                this.nextToken();
            }
        }
    }

    private void addTypeM(int a_iType) throws GlycoconjugateException, SugarImporterException 
    {
        GlycoEdge t_objEdge;
        if ( a_iType == 1 )
        {
            return;
        }
        if ( a_iType == 2 )
        {
            this.parseD();
            if ( this.m_aPositionD.size() == 1 )
            {
                if ( this.m_aPositionD.get(0) != 1 )
                {
                    throw new SugarImporterException("OGBI015",this.m_iPosition);
                }
                this.m_objMan6 = this.createMan(true);
                t_objEdge = this.createEdge(LinkageType.H_AT_OH,6,1,LinkageType.DEOXY);
                this.m_objSugar.addEdge(this.m_objManCore,this.m_objMan6,t_objEdge);
            }
            else if ( this.m_aPositionD.size() > 1 )
            {
                throw new SugarImporterException("OGBI015",this.m_iPosition);
            }
            else
            {
                this.m_objMan3 = this.createMan(true);
                t_objEdge = this.createEdge(LinkageType.H_AT_OH,3,1,LinkageType.DEOXY);
                this.m_objSugar.addEdge(this.m_objManCore,this.m_objMan3,t_objEdge);
            }
            return;
        }
        // create the standard man
        this.m_objMan3 = this.createMan(true);
        t_objEdge = this.createEdge(LinkageType.H_AT_OH,3,1,LinkageType.DEOXY);
        this.m_objSugar.addEdge(this.m_objManCore,this.m_objMan3,t_objEdge);
        this.m_objMan6 = this.createMan(true);
        t_objEdge = this.createEdge(LinkageType.H_AT_OH,6,1,LinkageType.DEOXY);
        this.m_objSugar.addEdge(this.m_objManCore,this.m_objMan6,t_objEdge);
        if ( a_iType == 3 )
        {
            return;
        }
        if ( a_iType == 4 )
        {
            this.parseD();
            GlycoNode t_objMan = this.createMan(false);
            if ( this.m_aPositionD.size() == 0 )
            {
                // unknown position
                UnderdeterminedSubTree t_objSubTree = new UnderdeterminedSubTree();
                t_objSubTree.addNode(t_objMan);
                t_objEdge = this.createEdge(LinkageType.H_AT_OH,Linkage.UNKNOWN_POSITION,1,LinkageType.DEOXY);
                t_objSubTree.setConnection(t_objEdge);
                this.m_objSugar.addUndeterminedSubTree(t_objSubTree);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, this.m_objMan3);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, this.m_objMan6);
            }
            else if ( this.m_aPositionD.size() == 1 )
            {
                this.m_objSugar.addNode(t_objMan);
                if ( this.m_aPositionD.get(0) == 1 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,Linkage.UNKNOWN_POSITION,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(this.m_objMan6, t_objMan, t_objEdge);
                }
                else if ( this.m_aPositionD.get(0) == 2 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,Linkage.UNKNOWN_POSITION,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(this.m_objMan3, t_objMan, t_objEdge);
                }
                else
                {
                    throw new SugarImporterException("OGBI016",this.m_iPosition);
                }
            }
            else
            {
                throw new SugarImporterException("OGBI016",this.m_iPosition);
            }
            return;
        }    
        GlycoNode t_objMan63 = this.createMan(true);
        t_objEdge = this.createEdge(LinkageType.H_AT_OH,3,1,LinkageType.DEOXY);
        this.m_objSugar.addEdge(this.m_objMan6, t_objMan63, t_objEdge);
        GlycoNode t_objMan66 = this.createMan(true);
        t_objEdge = this.createEdge(LinkageType.H_AT_OH,6,1,LinkageType.DEOXY);
        this.m_objSugar.addEdge(this.m_objMan6, t_objMan66, t_objEdge);
        if ( a_iType == 5 )
        {
            return;
        }
        else if ( a_iType == 6 )
        {
            this.parseD();
            GlycoNode t_objMan = this.createMan(false);
            if ( this.m_aPositionD.size() == 0 )
            {
                // unknown position
                UnderdeterminedSubTree t_objSubTree = new UnderdeterminedSubTree();
                t_objSubTree.addNode(t_objMan);
                t_objEdge = this.createEdge(LinkageType.H_AT_OH,Linkage.UNKNOWN_POSITION,1,LinkageType.DEOXY);
                t_objSubTree.setConnection(t_objEdge);
                this.m_objSugar.addUndeterminedSubTree(t_objSubTree);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, this.m_objMan3);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, t_objMan63);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, t_objMan66);
            }
            else if ( this.m_aPositionD.size() == 1 )
            {
                this.m_objSugar.addNode(t_objMan);
                if ( this.m_aPositionD.get(0) == 1 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(this.m_objMan3, t_objMan, t_objEdge);
                }
                else if ( this.m_aPositionD.get(0) == 2 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(t_objMan63, t_objMan, t_objEdge);
                }
                else if ( this.m_aPositionD.get(0) == 3 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(t_objMan66, t_objMan, t_objEdge);
                }
                else
                {
                    throw new SugarImporterException("OGBI017",this.m_iPosition);
                }
            }
            else
            {
                throw new SugarImporterException("OGBI017",this.m_iPosition);
            }
            return;
        }
        GlycoNode t_objMan32 = this.createMan(true);
        t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
        this.m_objSugar.addEdge(this.m_objMan3, t_objMan32, t_objEdge);
        if ( a_iType == 7 )
        {
            this.parseD();
            GlycoNode t_objMan = this.createMan(false);
            if ( this.m_aPositionD.size() == 0 )
            {
                // unknown position
                UnderdeterminedSubTree t_objSubTree = new UnderdeterminedSubTree();
                t_objSubTree.addNode(t_objMan);
                t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                t_objSubTree.setConnection(t_objEdge);
                this.m_objSugar.addUndeterminedSubTree(t_objSubTree);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, t_objMan32);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, t_objMan63);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, t_objMan66);
            }
            else if ( this.m_aPositionD.size() == 1 )
            {
                this.m_objSugar.addNode(t_objMan);
                if ( this.m_aPositionD.get(0) == 1 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(t_objMan66, t_objMan, t_objEdge);
                }
                else if ( this.m_aPositionD.get(0) == 2 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(t_objMan63, t_objMan, t_objEdge);
                }
                else if ( this.m_aPositionD.get(0) == 3 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(t_objMan32, t_objMan, t_objEdge);
                }
                else
                {
                    throw new SugarImporterException("OGBI018",this.m_iPosition);
                }
            }
            else
            {
                throw new SugarImporterException("OGBI018",this.m_iPosition);
            }
        }
        else if ( a_iType == 8 )
        {
            this.parseD();
            if ( this.m_aPositionD.size() == 0 )
            {
                // unknown position
                GlycoNode t_objMan = this.createMan(false);
                UnderdeterminedSubTree t_objSubTree = new UnderdeterminedSubTree();
                t_objSubTree.addNode(t_objMan);
                t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                t_objSubTree.setConnection(t_objEdge);
                this.m_objSugar.addUndeterminedSubTree(t_objSubTree);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, t_objMan32);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, t_objMan63);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, t_objMan66);
                // 
                t_objMan = this.createMan(false);
                t_objSubTree = new UnderdeterminedSubTree();
                t_objSubTree.addNode(t_objMan);
                t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                t_objSubTree.setConnection(t_objEdge);
                this.m_objSugar.addUndeterminedSubTree(t_objSubTree);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, t_objMan32);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, t_objMan63);
                this.m_objSugar.addUndeterminedSubTreeParent(t_objSubTree, t_objMan66);
            }
            else if ( this.m_aPositionD.size() == 2 )
            {
                if ( this.m_aPositionD.get(0) == this.m_aPositionD.get(1) )
                {
                    throw new SugarImporterException("OGBI021",this.m_iPosition);
                }
                GlycoNode t_objMan = this.createMan(true);
                if ( this.m_aPositionD.get(0) == 1 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(t_objMan66, t_objMan, t_objEdge);
                }
                else if ( this.m_aPositionD.get(0) == 2 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(t_objMan63, t_objMan, t_objEdge);
                }
                else if ( this.m_aPositionD.get(0) == 3 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(t_objMan32, t_objMan, t_objEdge);
                }
                else
                {
                    throw new SugarImporterException("OGBI020",this.m_iPosition);
                }
                t_objMan = this.createMan(true);
                if ( this.m_aPositionD.get(1) == 1 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(t_objMan66, t_objMan, t_objEdge);
                }
                else if ( this.m_aPositionD.get(1) == 2 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(t_objMan63, t_objMan, t_objEdge);
                }
                else if ( this.m_aPositionD.get(1) == 3 )
                {
                    t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(t_objMan32, t_objMan, t_objEdge);
                }
                else
                {
                    throw new SugarImporterException("OGBI020",this.m_iPosition);
                }
            }
            else
            {
                throw new SugarImporterException("OGBI020",this.m_iPosition);
            }
        }
        else if ( a_iType == 9 )
        {
            GlycoNode t_objMan322 = this.createMan(true);
            t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
            this.m_objSugar.addEdge(t_objMan32, t_objMan322, t_objEdge);
            GlycoNode t_objMan632 = this.createMan(true);
            t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
            this.m_objSugar.addEdge(t_objMan63, t_objMan632, t_objEdge);
            GlycoNode t_objMan662 = this.createMan(true);
            t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
            this.m_objSugar.addEdge(t_objMan66, t_objMan662, t_objEdge);
        }
        else
        {
            throw new SugarImporterException("OGBI019",this.m_iPosition);
        }
    }

    private Monosaccharide createMan(boolean a_bInsertIntoSugar) throws GlycoconjugateException 
    {
        Monosaccharide t_objMan = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
        t_objMan.setRing(1,5);
        t_objMan.addBaseType(BaseType.DMAN);
        if ( a_bInsertIntoSugar )
        {
            this.m_objSugar.addNode(t_objMan);
        }
        return t_objMan;
    }

    private void createCoreMan() throws GlycoconjugateException 
    {
        Substituent t_objSubst;
        GlycoEdge t_objEdge;
        Monosaccharide t_objGlcNac2;
        // first glcnac
        this.m_objCoreGlcNac = new Monosaccharide(Anomer.Beta,Superclass.HEX);
        this.m_objCoreGlcNac.setRing(1,5);
        this.m_objCoreGlcNac.addBaseType(BaseType.DGLC);
        t_objSubst = new Substituent(SubstituentType.N_ACETYL);
        t_objEdge = this.createEdge(LinkageType.DEOXY,2,1,LinkageType.NONMONOSACCHARID);
        this.m_objSugar.addNode(this.m_objCoreGlcNac);
        this.m_objSugar.addNode(t_objSubst);
        this.m_objSugar.addEdge(this.m_objCoreGlcNac,t_objSubst,t_objEdge);
        // second glcnac
        t_objGlcNac2 = new Monosaccharide(Anomer.Beta,Superclass.HEX);
        t_objGlcNac2.setRing(1,5);
        t_objGlcNac2.addBaseType(BaseType.DGLC);
        t_objSubst = new Substituent(SubstituentType.N_ACETYL);
        t_objEdge = this.createEdge(LinkageType.DEOXY,2,1,LinkageType.NONMONOSACCHARID);
        this.m_objSugar.addNode(t_objGlcNac2);
        this.m_objSugar.addNode(t_objSubst);
        this.m_objSugar.addEdge(t_objGlcNac2,t_objSubst,t_objEdge);
        // Glcnac - Glcnac
        t_objEdge = this.createEdge(LinkageType.H_AT_OH,4,1,LinkageType.DEOXY);
        this.m_objSugar.addEdge(this.m_objCoreGlcNac,t_objGlcNac2,t_objEdge);
        // core mannose
        this.m_objManCore = new Monosaccharide(Anomer.Beta,Superclass.HEX);
        this.m_objManCore.setRing(1,5);
        this.m_objManCore.addBaseType(BaseType.DMAN);
        t_objEdge = this.createEdge(LinkageType.H_AT_OH,4,1,LinkageType.DEOXY);
        this.m_objSugar.addNode(this.m_objManCore);
        this.m_objSugar.addEdge(t_objGlcNac2,this.m_objManCore,t_objEdge);
        // core fucose? 
        if ( this.m_bFuc3 )
        {
            Monosaccharide t_objFuc = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
            t_objFuc.setRing(1,5);
            t_objFuc.addBaseType(BaseType.DGAL);
            Modification t_objModi = new Modification(ModificationType.DEOXY,6);
            t_objFuc.addModification(t_objModi);
            this.m_objSugar.addNode(t_objFuc);
            t_objEdge = this.createEdge(LinkageType.H_AT_OH,3,1,LinkageType.DEOXY);
            this.m_objSugar.addEdge(this.m_objCoreGlcNac,t_objFuc,t_objEdge);
        }
        if ( this.m_bFuc6 )
        {
            Monosaccharide t_objFuc = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
            t_objFuc.setRing(1,5);
            t_objFuc.addBaseType(BaseType.DGAL);
            Modification t_objModi = new Modification(ModificationType.DEOXY,6);
            t_objFuc.addModification(t_objModi);
            this.m_objSugar.addNode(t_objFuc);
            t_objEdge = this.createEdge(LinkageType.H_AT_OH,6,1,LinkageType.DEOXY);
            this.m_objSugar.addEdge(this.m_objCoreGlcNac,t_objFuc,t_objEdge);
        }    
    }

    private void coreFuc() throws SugarImporterException 
    {
        this.nextToken();
        if ( this.m_cToken == '(')
        {
            this.nextToken();
            int t_iNumber = this.natural_number(); 
            if ( t_iNumber == 6 )
            {
                if ( this.m_bFuc6 )
                {
                    throw new SugarImporterException("OGBI005",this.m_iPosition);
                }
                this.m_bFuc6 = true;
            }
            else if ( t_iNumber == 3 )
            {
                if ( this.m_bFuc3 )
                {
                    throw new SugarImporterException("OGBI005",this.m_iPosition);
                }
                this.m_bFuc3 = true;
            }
            else
            {
                throw new SugarImporterException("OGBI002",this.m_iPosition);
            }
            if ( this.m_cToken != ')' )
            {
                throw new SugarImporterException("OGBI004",this.m_iPosition);
            }
            this.nextToken();
        }
        else
        {
            throw new SugarImporterException("OGBI003",this.m_iPosition);
        }
    }

    /**
     * 
     */
    private void clear() 
    {
        this.m_bFuc3 = false;
        this.m_bFuc6 = false;
        this.m_objManCore = null;
        this.m_objMan3 = null;
        this.m_objMan6 = null;
        this.m_objCoreGlcNac = null;
        this.m_iAbranchPosition = null;
        this.m_iAtype = -1;
        this.m_objGlcNAc32 = null;
        this.m_objGlcNAc34 = null;
        this.m_objGlcNAc62 = null;
        this.m_objGlcNAc66 = null;
        this.m_aPositionD.clear();
        this.m_objAoneUnderGraph = null;
        this.m_objAoneUnderGlcNAc = null;
    }
    
    private void createCore() throws GlycoconjugateException
    {
        Substituent t_objSubst;
        GlycoEdge t_objEdge;
        Monosaccharide t_objGlcNac2;
        // first glcnac
        this.m_objCoreGlcNac = new Monosaccharide(Anomer.Beta,Superclass.HEX);
        this.m_objCoreGlcNac.setRing(1,5);
        this.m_objCoreGlcNac.addBaseType(BaseType.DGLC);
        t_objSubst = new Substituent(SubstituentType.N_ACETYL);
        t_objEdge = this.createEdge(LinkageType.DEOXY,2,1,LinkageType.NONMONOSACCHARID);
        this.m_objSugar.addNode(this.m_objCoreGlcNac);
        this.m_objSugar.addNode(t_objSubst);
        this.m_objSugar.addEdge(this.m_objCoreGlcNac,t_objSubst,t_objEdge);
        // second glcnac
        t_objGlcNac2 = new Monosaccharide(Anomer.Beta,Superclass.HEX);
        t_objGlcNac2.setRing(1,5);
        t_objGlcNac2.addBaseType(BaseType.DGLC);
        t_objSubst = new Substituent(SubstituentType.N_ACETYL);
        t_objEdge = this.createEdge(LinkageType.DEOXY,2,1,LinkageType.NONMONOSACCHARID);
        this.m_objSugar.addNode(t_objGlcNac2);
        this.m_objSugar.addNode(t_objSubst);
        this.m_objSugar.addEdge(t_objGlcNac2,t_objSubst,t_objEdge);
        // Glcnac - Glcnac
        t_objEdge = this.createEdge(LinkageType.H_AT_OH,4,1,LinkageType.DEOXY);
        this.m_objSugar.addEdge(this.m_objCoreGlcNac,t_objGlcNac2,t_objEdge);
        // core mannose
        this.m_objManCore = new Monosaccharide(Anomer.Beta,Superclass.HEX);
        this.m_objManCore.setRing(1,5);
        this.m_objManCore.addBaseType(BaseType.DMAN);
        t_objEdge = this.createEdge(LinkageType.H_AT_OH,4,1,LinkageType.DEOXY);
        this.m_objSugar.addNode(this.m_objManCore);
        this.m_objSugar.addEdge(t_objGlcNac2,this.m_objManCore,t_objEdge);
        // 3 mannose
        this.m_objMan3 = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
        this.m_objMan3.setRing(1,5);
        this.m_objMan3.addBaseType(BaseType.DMAN);
        t_objEdge = this.createEdge(LinkageType.H_AT_OH,3,1,LinkageType.DEOXY);
        this.m_objSugar.addNode(this.m_objMan3);
        this.m_objSugar.addEdge(this.m_objManCore,this.m_objMan3,t_objEdge);
        // 6 mannose
        this.m_objMan6 = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
        this.m_objMan6.setRing(1,5);
        this.m_objMan6.addBaseType(BaseType.DMAN);
        t_objEdge = this.createEdge(LinkageType.H_AT_OH,6,1,LinkageType.DEOXY);
        this.m_objSugar.addNode(this.m_objMan6);
        this.m_objSugar.addEdge(this.m_objManCore,this.m_objMan6,t_objEdge);
        // core fucose? 
        if ( this.m_bFuc3 )
        {
            Monosaccharide t_objFuc = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
            t_objFuc.setRing(1,5);
            t_objFuc.addBaseType(BaseType.DGAL);
            Modification t_objModi = new Modification(ModificationType.DEOXY,6);
            t_objFuc.addModification(t_objModi);
            this.m_objSugar.addNode(t_objFuc);
            t_objEdge = this.createEdge(LinkageType.H_AT_OH,3,1,LinkageType.DEOXY);
            this.m_objSugar.addEdge(this.m_objCoreGlcNac,t_objFuc,t_objEdge);
        }
        if ( this.m_bFuc6 )
        {
            Monosaccharide t_objFuc = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
            t_objFuc.setRing(1,5);
            t_objFuc.addBaseType(BaseType.DGAL);
            Modification t_objModi = new Modification(ModificationType.DEOXY,6);
            t_objFuc.addModification(t_objModi);
            this.m_objSugar.addNode(t_objFuc);
            t_objEdge = this.createEdge(LinkageType.H_AT_OH,6,1,LinkageType.DEOXY);
            this.m_objSugar.addEdge(this.m_objCoreGlcNac,t_objFuc,t_objEdge);
        }
    }

    private GlycoEdge createEdge(LinkageType a_enumParentType, int a_iParentPos, int a_iChildPos, LinkageType a_enumChildType) throws GlycoconjugateException 
    {
        GlycoEdge t_objEdge = new GlycoEdge();
        Linkage t_objLinkage = new Linkage();
        t_objLinkage.addChildLinkage(a_iChildPos);
        t_objLinkage.addParentLinkage(a_iParentPos);
        t_objLinkage.setParentLinkageType(a_enumParentType);
        t_objLinkage.setChildLinkageType(a_enumChildType);
        t_objEdge.addGlycosidicLinkage(t_objLinkage);
        return t_objEdge;
    }
    
    private void addBiSection() throws GlycoconjugateException
    {
        Monosaccharide t_objBi = new Monosaccharide(Anomer.Beta,Superclass.HEX);
        t_objBi.setRing(1,5);
        t_objBi.addBaseType(BaseType.DGLC);
        this.m_objSugar.addNode(t_objBi);
        GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,4,1,LinkageType.DEOXY);
        this.m_objSugar.addEdge(this.m_objManCore,t_objBi,t_objEdge);
        // NAc
        Substituent t_objSubst = new Substituent(SubstituentType.N_ACETYL);
        this.m_objSugar.addNode(t_objSubst);
        t_objEdge = this.createEdge(LinkageType.DEOXY,2,1,LinkageType.NONMONOSACCHARID);
        this.m_objSugar.addEdge(t_objBi,t_objSubst,t_objEdge);
    }

    private void addXylose() throws GlycoconjugateException
    {
        Monosaccharide t_objX = new Monosaccharide(Anomer.Beta,Superclass.PEN);
        t_objX.setRing(1,5);
        t_objX.addBaseType(BaseType.DXYL);
        this.m_objSugar.addNode(t_objX);
        GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
        this.m_objSugar.addEdge(this.m_objManCore,t_objX,t_objEdge);
    }

    private Monosaccharide addGlcNAc( GlycoGraph a_objGraph ) throws GlycoconjugateException
    {
        Monosaccharide t_objGlcNAc = new Monosaccharide(Anomer.Beta,Superclass.HEX);
        t_objGlcNAc.setRing(1,5);
        t_objGlcNAc.addBaseType(BaseType.DGLC);
        a_objGraph.addNode(t_objGlcNAc);
        // NAc
        Substituent t_objSubst = new Substituent(SubstituentType.N_ACETYL);
        a_objGraph.addNode(t_objSubst);
        GlycoEdge t_objEdge = this.createEdge(LinkageType.DEOXY,2,1,LinkageType.NONMONOSACCHARID);
        a_objGraph.addEdge(t_objGlcNAc,t_objSubst,t_objEdge);
        return t_objGlcNAc;
    }
    
    private void addGlcNAcType(int a_iTypeM) throws SugarImporterException, GlycoconjugateException 
    {
        if ( this.m_iAtype < 1 || this.m_iAtype > 4 )
        {
            throw new SugarImporterException("OGBI011",this.m_iPosition);
        }
        if ( this.m_iAtype == 1 )
        {
            if ( this.m_iAbranchPosition == null )
            {
                if ( a_iTypeM == -1 )
                {
                    // Unknown position
                    this.m_objAoneUnderGraph = new UnderdeterminedSubTree();
                    this.m_objAoneUnderGlcNAc = this.addGlcNAc(this.m_objAoneUnderGraph);
                    // linkage and parents
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objAoneUnderGraph.setConnection(t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(this.m_objAoneUnderGraph);
                    this.m_objSugar.addUndeterminedSubTreeParent(this.m_objAoneUnderGraph,this.m_objMan3);
                    this.m_objSugar.addUndeterminedSubTreeParent(this.m_objAoneUnderGraph,this.m_objMan6);
                }
                else
                {
                    // must be 2 at 3 branch
                    this.m_objGlcNAc32 = this.addGlcNAc(this.m_objSugar);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(this.m_objMan3,this.m_objGlcNAc32,t_objEdge);
                }
            }
            else 
            {
                if ( this.m_iAbranchPosition == 3 )
                {
                    // 2 at 3 branch
                    this.m_objGlcNAc32 = this.addGlcNAc(this.m_objSugar);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(this.m_objMan3,this.m_objGlcNAc32,t_objEdge);
                }
                else if ( this.m_iAbranchPosition == 6 )
                {
                    if ( a_iTypeM != -1 )
                    {
                        throw new SugarImporterException("OGBI034",this.m_iPosition);
                    }
                    // 2 at 6 branch
                    this.m_objGlcNAc62 = this.addGlcNAc(this.m_objSugar);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                    this.m_objSugar.addEdge(this.m_objMan6,this.m_objGlcNAc62,t_objEdge);
                }
                else
                {
                    throw new SugarImporterException("OGBI003",this.m_iPosition);
                }
            }            
        }
        else if ( this.m_iAtype == 2 )
        {
            if ( a_iTypeM == -1 )
            {
                // 2 at 3 branch
                this.m_objGlcNAc32 = this.addGlcNAc(this.m_objSugar);
                GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                this.m_objSugar.addEdge(this.m_objMan3,this.m_objGlcNAc32,t_objEdge);
                // 2 at 6 branch
                this.m_objGlcNAc62 = this.addGlcNAc(this.m_objSugar);
                t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                this.m_objSugar.addEdge(this.m_objMan6,this.m_objGlcNAc62,t_objEdge);
            }
            else
            {
                // 2 at 3 branch
                this.m_objGlcNAc32 = this.addGlcNAc(this.m_objSugar);
                GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                this.m_objSugar.addEdge(this.m_objMan3,this.m_objGlcNAc32,t_objEdge);
                // 4 at 3 branch
                this.m_objGlcNAc34 = this.addGlcNAc(this.m_objSugar);
                t_objEdge = this.createEdge(LinkageType.H_AT_OH,4,1,LinkageType.DEOXY);
                this.m_objSugar.addEdge(this.m_objMan3,this.m_objGlcNAc34,t_objEdge);
            }
        }
        else if ( this.m_iAtype == 3 )
        {
            if ( this.m_iAbranchPosition == null )
            {
                // 2 at 3 branch
                this.m_objGlcNAc32 = this.addGlcNAc(this.m_objSugar);
                GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                this.m_objSugar.addEdge(this.m_objMan3,this.m_objGlcNAc32,t_objEdge);
                // 4 at 3 branch
                this.m_objGlcNAc34 = this.addGlcNAc(this.m_objSugar);
                t_objEdge = this.createEdge(LinkageType.H_AT_OH,4,1,LinkageType.DEOXY);
                this.m_objSugar.addEdge(this.m_objMan3,this.m_objGlcNAc34,t_objEdge);
                // 2 at 6 branch
                this.m_objGlcNAc62 = this.addGlcNAc(this.m_objSugar);
                t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
                this.m_objSugar.addEdge(this.m_objMan6,this.m_objGlcNAc62,t_objEdge);
            }
            else
            {
                throw new SugarImporterException("OGBI024",this.m_iPosition);
            }
        }
        else if ( this.m_iAtype == 4 )
        {
            // 2 at 3 branch
            this.m_objGlcNAc32 = this.addGlcNAc(this.m_objSugar);
            GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
            this.m_objSugar.addEdge(this.m_objMan3,this.m_objGlcNAc32,t_objEdge);
            // 4 at 3 branch
            this.m_objGlcNAc34 = this.addGlcNAc(this.m_objSugar);
            t_objEdge = this.createEdge(LinkageType.H_AT_OH,4,1,LinkageType.DEOXY);
            this.m_objSugar.addEdge(this.m_objMan3,this.m_objGlcNAc34,t_objEdge);
            // 2 at 6 branch
            this.m_objGlcNAc62 = this.addGlcNAc(this.m_objSugar);
            t_objEdge = this.createEdge(LinkageType.H_AT_OH,2,1,LinkageType.DEOXY);
            this.m_objSugar.addEdge(this.m_objMan6,this.m_objGlcNAc62,t_objEdge);
            // 6 at 6 branch
            this.m_objGlcNAc66 = this.addGlcNAc(this.m_objSugar);
            t_objEdge = this.createEdge(LinkageType.H_AT_OH,6,1,LinkageType.DEOXY);
            this.m_objSugar.addEdge(this.m_objMan6,this.m_objGlcNAc66,t_objEdge);
        }
        else
        {
            throw new SugarImporterException("OGBI024",this.m_iPosition);
        }
    }

    private ArrayList<OgbiResidue> addGalAtGlcNac(int a_iGalNumber, ArrayList<Integer> a_aGalLinkPosition) throws SugarImporterException, GlycoconjugateException 
    {
        ArrayList<OgbiResidue> t_aResult = new ArrayList<OgbiResidue>();
        // A2G2
        // A2G(4)1
        // A2G(3,4)2
        if ( a_iGalNumber > this.m_iAtype )
        {
            throw new SugarImporterException("OGBI013",this.m_iPosition);
        }
        if ( a_iGalNumber == this.m_iAtype )
        {
            if ( a_aGalLinkPosition.size() < 2 )
            {
                Monosaccharide t_objGal = null;
                // unknown linkage position
                int t_iLinkagePosition = Linkage.UNKNOWN_POSITION;
                if ( a_aGalLinkPosition.size() == 1 )
                {
                    t_iLinkagePosition = a_aGalLinkPosition.get(0);
                }
                if ( this.m_objAoneUnderGraph != null )
                {
                    t_objGal = this.createGal(this.m_objAoneUnderGraph,t_iLinkagePosition,this.m_objAoneUnderGlcNAc);
                    t_aResult.add( new OgbiResidue(t_objGal, this.m_objAoneUnderGraph,true) );
                }
                else
                {
                    if ( this.m_objGlcNAc32 != null )
                    {
                        t_objGal = this.createGal(this.m_objSugar,t_iLinkagePosition,this.m_objGlcNAc32);
                        t_aResult.add( new OgbiResidue(t_objGal, this.m_objSugar,false) );
                    }
                    if ( this.m_objGlcNAc34 != null )
                    {
                        t_objGal = this.createGal(this.m_objSugar,t_iLinkagePosition,this.m_objGlcNAc34);
                        t_aResult.add( new OgbiResidue(t_objGal, this.m_objSugar,false) );
                    }
                    if ( this.m_objGlcNAc62 != null )
                    {
                        t_objGal = this.createGal(this.m_objSugar,t_iLinkagePosition,this.m_objGlcNAc62);
                        t_aResult.add( new OgbiResidue(t_objGal, this.m_objSugar,false) );
                    }
                    if ( this.m_objGlcNAc66 != null )
                    {
                        t_objGal = this.createGal(this.m_objSugar,t_iLinkagePosition,this.m_objGlcNAc66);
                        t_aResult.add( new OgbiResidue(t_objGal, this.m_objSugar,false) );
                    }
                }
            }
            else if ( a_aGalLinkPosition.size() == a_iGalNumber )
            {
                // each another linkage ==> Underdeterminded
                for (Iterator<Integer> t_iterPositions = a_aGalLinkPosition.iterator(); t_iterPositions.hasNext();) 
                {
                    UnderdeterminedSubTree t_objSubtree = new UnderdeterminedSubTree();
                    Monosaccharide t_objGal = new Monosaccharide(Anomer.Beta,Superclass.HEX);
                    t_objGal.setRing(1,5);
                    t_objGal.addBaseType(BaseType.DGAL);
                    t_objSubtree.addNode(t_objGal);
                    // linkage and parents
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iterPositions.next(),1,LinkageType.DEOXY);
                    t_objSubtree.setConnection(t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objSubtree);
                    if ( this.m_objGlcNAc32 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc32);    
                    }
                    if ( this.m_objGlcNAc34 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc34);    
                    }
                    if ( this.m_objGlcNAc62 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc62);    
                    }
                    if ( this.m_objGlcNAc66 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc66);    
                    }
                    t_aResult.add( new OgbiResidue(t_objGal, t_objSubtree ,true) );
                }
            }
            else 
            {
                // false
                throw new SugarImporterException("OGBI030",this.m_iPosition);
            }
        }        
        else if ( this.m_iAbranchPosition != null )
        {
            if ( a_aGalLinkPosition.size() > 1 )
            {
                throw new SugarImporterException("OGBI036",this.m_iPosition);
            }
            // unknown linkage position
            int t_iLinkagePosition = Linkage.UNKNOWN_POSITION;
            if ( a_aGalLinkPosition.size() == 1 )
            {
                t_iLinkagePosition = a_aGalLinkPosition.get(0);
            }
            // furhter branchposition given (only for A2) 
            if ( a_iGalNumber != 1 )
            {
                throw new SugarImporterException("OGBI037",this.m_iPosition);
            }
            else
            {
                if ( this.m_iAbranchPosition == 3 )
                {
                    if ( this.m_objGlcNAc32 == null )
                    {
                        throw new SugarImporterException("OGBI031",this.m_iPosition);
                    }
                    else
                    {
                        Monosaccharide t_objGal = this.createGal(this.m_objSugar,t_iLinkagePosition,this.m_objGlcNAc32);
                        t_aResult.add( new OgbiResidue(t_objGal, this.m_objSugar,false) );
                    }
                }
                else if ( this.m_iAbranchPosition == 6 )
                {
                    if ( this.m_objGlcNAc62 == null )
                    {
                        throw new SugarImporterException("OGBI032",this.m_iPosition);
                    }
                    else
                    {
                        Monosaccharide t_objGal = this.createGal(this.m_objSugar,t_iLinkagePosition,this.m_objGlcNAc62);
                        t_aResult.add( new OgbiResidue(t_objGal, this.m_objSugar,false) );
                    }
                }
                else
                {
                    throw new SugarImporterException("OGBI028",this.m_iPosition);
                }
            }
        }
        else
        {
            // less Gal than GlcNAc ==> underdeterminded
            if ( a_aGalLinkPosition.size() < 2 )
            {
                // unknown or one linkage
                int t_iLinkagePosition = Linkage.UNKNOWN_POSITION;
                if ( a_aGalLinkPosition.size() == 1 )
                {
                    t_iLinkagePosition = a_aGalLinkPosition.get(0);
                }
                for (int t_iCounter = 0; t_iCounter < a_iGalNumber ; t_iCounter++) 
                {
                    UnderdeterminedSubTree t_objSubtree = new UnderdeterminedSubTree();
                    Monosaccharide t_objGal = new Monosaccharide(Anomer.Beta,Superclass.HEX);
                    t_objGal.setRing(1,5);
                    t_objGal.addBaseType(BaseType.DGAL);
                    t_objSubtree.addNode(t_objGal);
                    // linkage and parents
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iLinkagePosition,1,LinkageType.DEOXY);
                    t_objSubtree.setConnection(t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objSubtree);
                    if ( this.m_objGlcNAc32 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc32);    
                    }
                    if ( this.m_objGlcNAc34 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc34);    
                    }
                    if ( this.m_objGlcNAc62 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc62);    
                    }
                    if ( this.m_objGlcNAc66 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc66);    
                    }
                    t_aResult.add( new OgbiResidue(t_objGal, t_objSubtree ,true) );
                }
            }
            else if ( a_aGalLinkPosition.size() == a_iGalNumber )
            {
                // each another linkage ==> Underdeterminded
                for (Iterator<Integer> t_iterPositions = a_aGalLinkPosition.iterator(); t_iterPositions.hasNext();) 
                {
                    UnderdeterminedSubTree t_objSubtree = new UnderdeterminedSubTree();
                    Monosaccharide t_objGal = new Monosaccharide(Anomer.Beta,Superclass.HEX);
                    t_objGal.setRing(1,5);
                    t_objGal.addBaseType(BaseType.DGAL);
                    t_objSubtree.addNode(t_objGal);
                    // linkage and parents
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iterPositions.next(),1,LinkageType.DEOXY);
                    t_objSubtree.setConnection(t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objSubtree);
                    if ( this.m_objGlcNAc32 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc32);    
                    }
                    if ( this.m_objGlcNAc34 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc34);    
                    }
                    if ( this.m_objGlcNAc62 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc62);    
                    }
                    if ( this.m_objGlcNAc66 != null )
                    {
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objSubtree,this.m_objGlcNAc66);    
                    }
                    t_aResult.add( new OgbiResidue(t_objGal, t_objSubtree ,true) );
                }
            }
            else 
            {
                // false
                throw new SugarImporterException("OGBI030",this.m_iPosition);
            }
        }
        return t_aResult;
    }

    /**
     * @param aoneUnderGraph
     * @param linkagePosition
     * @param glcNAc32
     * @throws GlycoconjugateException 
     */
    private Monosaccharide createGal(GlycoGraph a_objGraph, int a_iLinkagePosition, Monosaccharide a_objParent) throws GlycoconjugateException 
    {
        Monosaccharide t_objGal = new Monosaccharide(Anomer.Beta,Superclass.HEX);
        t_objGal.setRing(1,5);
        t_objGal.addBaseType(BaseType.DGAL);
        a_objGraph.addNode(t_objGal);
        GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,a_iLinkagePosition,1,LinkageType.DEOXY);
        a_objGraph.addEdge(a_objParent,t_objGal,t_objEdge);
        return t_objGal;
    }

    private ArrayList<OgbiResidue> addGalGal(int a_iGalNumber, ArrayList<Integer> a_aLinkPositions, ArrayList<OgbiResidue> a_aParents) throws GlycoconjugateException, SugarImporterException 
    {
        ArrayList<OgbiResidue> t_aResult = new ArrayList<OgbiResidue>();
        if ( a_iGalNumber > a_aParents.size() )
        {
            throw new SugarImporterException("OGBI042",this.m_iPosition);
        }
        if ( a_iGalNumber == a_aParents.size() )
        {
            if ( a_aLinkPositions.size() < 2 )
            {
                // linkage position unknown or all the same
                int t_iPosition = Linkage.UNKNOWN_POSITION;
                if ( a_aLinkPositions.size() == 1 )
                {
                    t_iPosition = a_aLinkPositions.get(0);
                }
                for (Iterator<OgbiResidue> t_iterParent = a_aParents.iterator(); t_iterParent.hasNext();) 
                {
                    OgbiResidue t_objResidue = t_iterParent.next();
                    Monosaccharide t_objGal = this.createGal(t_objResidue.m_objGraph,t_iPosition,t_objResidue.m_objMS);
                    t_objGal.setAnomer(Anomer.Alpha);
                    t_aResult.add( new OgbiResidue(t_objGal, t_objResidue.m_objGraph,t_objResidue.m_bUnderdeterminded) );                    
                }
            }
            else
            {
                // all other linkage positions ==> underdeterminded
                if ( a_aParents.get(0).m_bUnderdeterminded )
                {
                    // error nested underdeterminded
                    throw new SugarImporterException("OGBI035",this.m_iPosition);
                }
                if ( a_aLinkPositions.size() != a_iGalNumber )
                {
                    throw new SugarImporterException("OGBI043",this.m_iPosition);
                }
                for (Iterator<Integer> t_iterPosition = a_aLinkPositions.iterator(); t_iterPosition.hasNext();) 
                {
                    UnderdeterminedSubTree t_objTree = new UnderdeterminedSubTree();
                    Monosaccharide t_objGal = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
                    t_objGal.setRing(1, 5);
                    t_objGal.addBaseType(BaseType.DGAL);
                    t_objTree.addNode(t_objGal);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iterPosition.next(),1,LinkageType.DEOXY);
                    t_objTree.setConnection(t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objTree);
                    for (Iterator<OgbiResidue> t_iterParent = a_aParents.iterator(); t_iterParent.hasNext();) 
                    {
                        OgbiResidue t_objResidue = t_iterParent.next();
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree, t_objResidue.m_objMS);
                    }
                    t_aResult.add( new OgbiResidue(t_objGal, t_objTree,true) );
                }
            }
        }
        else
        {
            // a_iFucNumber < a_aParents.size()
            if ( a_aLinkPositions.size() < 2 )
            {
                if ( a_aParents.get(0).m_bUnderdeterminded )
                {
                    // error nested underdeterminded
                    throw new SugarImporterException("OGBI035",this.m_iPosition);
                }
                // linkage position unknown or all the same
                int t_iPosition = Linkage.UNKNOWN_POSITION;
                if ( a_aLinkPositions.size() == 1 )
                {
                    t_iPosition = a_aLinkPositions.get(0);
                }
                for (int t_iCounter = 0; t_iCounter < a_iGalNumber; t_iCounter++) 
                {
                    UnderdeterminedSubTree t_objTree = new UnderdeterminedSubTree();
                    Monosaccharide t_objGal = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
                    t_objGal.setRing(1, 5);
                    t_objGal.addBaseType(BaseType.DGAL);
                    t_objTree.addNode(t_objGal);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iPosition,1,LinkageType.DEOXY);
                    t_objTree.setConnection(t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objTree);
                    for (Iterator<OgbiResidue> t_iterParent = a_aParents.iterator(); t_iterParent.hasNext();) 
                    {
                        OgbiResidue t_objResidue = t_iterParent.next();
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree, t_objResidue.m_objMS);
                    }
                    t_aResult.add( new OgbiResidue(t_objGal, t_objTree,true) );                    
                }
            }
            else
            {
                // all other linkage positions ==> underdeterminded
                if ( a_aParents.get(0).m_bUnderdeterminded )
                {
                    // error nested underdeterminded
                    throw new SugarImporterException("OGBI035",this.m_iPosition);
                }
                if ( a_aLinkPositions.size() != a_iGalNumber )
                {
                    throw new SugarImporterException("OGBI043",this.m_iPosition);
                }
                for (Iterator<Integer> t_iterPosition = a_aLinkPositions.iterator(); t_iterPosition.hasNext();) 
                {
                    UnderdeterminedSubTree t_objTree = new UnderdeterminedSubTree();
                    Monosaccharide t_objGal = new Monosaccharide(Anomer.Alpha,Superclass.HEX);
                    t_objGal.setRing(1, 5);
                    t_objGal.addBaseType(BaseType.DGAL);
                    t_objTree.addNode(t_objGal);
                    GlycoEdge t_objEdge = this.createEdge(LinkageType.H_AT_OH,t_iterPosition.next(),1,LinkageType.DEOXY);
                    t_objTree.setConnection(t_objEdge);
                    this.m_objSugar.addUndeterminedSubTree(t_objTree);
                    for (Iterator<OgbiResidue> t_iterParent = a_aParents.iterator(); t_iterParent.hasNext();) 
                    {
                        OgbiResidue t_objResidue = t_iterParent.next();
                        this.m_objSugar.addUndeterminedSubTreeParent(t_objTree, t_objResidue.m_objMS);
                    }
                    t_aResult.add( new OgbiResidue(t_objGal, t_objTree,true) );                    
                }
            }
        }        
        return t_aResult;
    }

}
