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
package org.eurocarbdb.MolecularFramework.io.GlycoCT;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraphAlternative;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class SugarExporterGlycoCTCondensed implements GlycoVisitor
{
    
    private String m_sCTCode="";
    private String m_sRES;
    private String m_sLIN;
    private String m_sREP;
    private String m_sUND;
    private String m_sALT;
    
    private boolean m_bStrict = true;
    
    private Integer m_iResCounter;
    private Integer m_iLinkageCounter;
    private Integer m_iEdgeCounter;
    private ArrayList<SugarUnitRepeat> m_aRepeats = new ArrayList<SugarUnitRepeat>();
    private ArrayList<NonMonosaccharide> m_aNonMS = new ArrayList<NonMonosaccharide>(); 
    private ArrayList<UnderdeterminedSubTree> m_aUnderdeterminedTrees = new ArrayList<UnderdeterminedSubTree>(); 
    private ArrayList <SugarUnitAlternative> m_aAlternativeUnits = new ArrayList <SugarUnitAlternative> ();
    private HashMap<GlycoNode,Integer> m_hashNodeID = new HashMap<GlycoNode,Integer>();
    private HashMap<GlycoEdge,Integer> m_hashEdgeID = new HashMap<GlycoEdge,Integer>();
    private HashMap<Linkage,Integer> m_hashLinkageID = new HashMap<Linkage,Integer>();
    private String m_strLineSeparator = "\n";

    public void setLineSeparator(String a_strSep)
    {
        this.m_strLineSeparator = a_strSep;
    }
    
    public void setStrict(boolean a_bStrict)
    {
        this.m_bStrict = a_bStrict;
    }
    
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException {
        this.m_iResCounter++;
        this.m_hashNodeID.put(a_objMonosaccharid,this.m_iResCounter);        
        this.m_sRES+=    m_iResCounter+
        "b:"+
        a_objMonosaccharid.getGlycoCTName();
        
        this.m_sRES+=this.m_strLineSeparator;
        
        GlycoEdge t_objEdge = a_objMonosaccharid.getParentEdge(); 
        if ( t_objEdge != null )
        {
            this.writeEdge( t_objEdge, 
                    this.m_hashNodeID.get(t_objEdge.getParent()),
                    this.m_hashNodeID.get(t_objEdge.getChild()));
        }        
    }
    
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {
        this.m_aNonMS.add(a_objResidue);    
    }
    
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException {
        this.m_iResCounter++;
        this.m_hashNodeID.put(a_objRepeat,this.m_iResCounter);
        this.m_aRepeats.add(a_objRepeat);
        
        this.m_sRES+=    m_iResCounter+
        "r:r"
        +String.valueOf(this.m_aRepeats.size());
        
        this.m_sRES+=this.m_strLineSeparator;
        
        for (UnderdeterminedSubTree t_oSubtree : a_objRepeat.getUndeterminedSubTrees())
        {
            this.m_aUnderdeterminedTrees.add(t_oSubtree);            
        }
        
        GlycoEdge t_objEdge = a_objRepeat.getParentEdge(); 
        if ( t_objEdge != null )
        {
            this.writeEdge( t_objEdge, 
                    this.m_hashNodeID.get(t_objEdge.getParent()),
                    this.m_hashNodeID.get(t_objEdge.getChild()));
        }        
        
    }
    
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {    
        this.m_iResCounter++;
        this.m_hashNodeID.put(a_objSubstituent,this.m_iResCounter);        
        this.m_sRES+=    m_iResCounter+
        "s:"+
        a_objSubstituent.getSubstituentType().getName();
        
        this.m_sRES+=this.m_strLineSeparator;
        
        GlycoEdge t_objEdge = a_objSubstituent.getParentEdge(); 
        if ( t_objEdge != null )
        {
            this.writeEdge( t_objEdge, 
                    this.m_hashNodeID.get(t_objEdge.getParent()),
                    this.m_hashNodeID.get(t_objEdge.getChild()));
        }        
    }
    
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
        GlycoEdge t_objEdge = a_objCyclic.getParentEdge(); 
        if ( t_objEdge != null )
        {
            this.writeEdge( t_objEdge, 
                    this.m_hashNodeID.get(t_objEdge.getParent()),
                    this.m_hashNodeID.get(a_objCyclic.getCyclicStart()));
        }        
    }
    
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
        this.m_iResCounter++;
        this.m_hashNodeID.put(a_objAlternative,this.m_iResCounter);
        this.m_aAlternativeUnits.add(a_objAlternative);
        
        this.m_sRES+=    m_iResCounter+
        "a:a"
        +String.valueOf(this.m_aAlternativeUnits.size());
        
        this.m_sRES+=this.m_strLineSeparator;
        
        GlycoEdge t_objEdge = a_objAlternative.getParentEdge(); 
        if ( t_objEdge != null )
        {
            this.writeEdge( t_objEdge, 
                    this.m_hashNodeID.get(t_objEdge.getParent()),
                    this.m_hashNodeID.get(t_objEdge.getChild()));
        }        
    }
    
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("UnvalidatedGlycoNode is NOT handled in GlycoCT.");
    }
    
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
        //stays empty
    }
    
    public void start(GlycoNode a_objNode) throws GlycoVisitorException 
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverse(a_objNode);
        makeCode (t_objTraverser);
    }
    
    
    public void start(Sugar a_objSugar) throws GlycoVisitorException
    {
        this.clear();
        
        // Nebenlaeufige Zusatzinformationen
        if (a_objSugar.getUndeterminedSubTrees().size()>0){
        for (UnderdeterminedSubTree t_oSubtree : a_objSugar.getUndeterminedSubTrees())
        {
            this.m_aUnderdeterminedTrees.add(t_oSubtree);            
        }
        }
        
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        makeCode(t_objTraverser);
        
    }
    
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {        
        return new GlycoCTTraverser(a_objVisitor);
    }
    
    public void clear() {
        this.m_iResCounter = 0;
        this.m_iLinkageCounter = 1;
        this.m_iEdgeCounter = 0;
        this.m_aRepeats.clear();
        this.m_aUnderdeterminedTrees.clear(); 
        this.m_hashEdgeID.clear();
        this.m_hashNodeID.clear();
        this.m_aNonMS.clear();
        this.m_aAlternativeUnits.clear();
        this.m_hashLinkageID.clear();
        this.m_sRES="RES" + this.m_strLineSeparator;
        this.m_sLIN="";
        this.m_sREP="";
        this.m_sCTCode="";
        this.m_sUND="";
        this.m_sUND="";
        this.m_sALT="";
        
    }
    
    public String getCompressedHashCode () throws GlycoVisitorException{
        try {                        
            return zipToString(this.m_sCTCode);
        } catch (Exception e) {
            throw new GlycoVisitorException("Error: Compressing problem");
        }        
    }
    public static String zipToString(String zip) throws Exception{
        
        byte[] t_aSingleBytes = zip.getBytes();    
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LevelGZIPOutputStream zos;
        
        zos = new LevelGZIPOutputStream(baos,9);
        
        for(int i = 0; i < t_aSingleBytes.length; i++) {
            zos.write(t_aSingleBytes[i]);
        }
        zos.close();
        baos.close();
                
        byte[] baFileContentCompressed = baos.toByteArray();
        String base64 = new sun.misc.BASE64Encoder().encode( baFileContentCompressed );
        return(base64);
    }
    
    
    
    public String getHashCode (){        
        return this.m_sCTCode;
    }
        
    private void writeEdge(GlycoEdge a_objEdge, Integer a_iParentID, Integer a_iChildID) throws GlycoVisitorException 
    {
        if ( a_iParentID == null || a_iChildID == null )        {
            // connection to aglyca
            return;
        }
        
        this.m_iEdgeCounter++;
        if ( a_objEdge.getGlycosidicLinkages().size() == 0)
        {
            throw new GlycoVisitorException("A edge without a linkage object is not valid.");
        }
        ArrayList <Linkage> t_oLinkages = a_objEdge.getGlycosidicLinkages();
        GlycoCTLinkageComparator t_linComp = new GlycoCTLinkageComparator();
        Collections.sort(t_oLinkages,t_linComp);        
        
        for (Linkage t_oLin : t_oLinkages) 
        {
            
            this.m_sLIN+=this.m_iLinkageCounter.toString()+":";            
            this.m_iLinkageCounter++;
            this.m_sLIN+=a_iParentID+String.valueOf(t_oLin.getParentLinkageType().getType())+"(";
            
            ArrayList <Integer> t_oLinParents = t_oLin.getParentLinkages();
            Collections.sort(t_oLinParents);
            
            for (Integer i : t_oLinParents) 
            {
                this.m_sLIN+=i+"|";
            }
            this.m_sLIN=this.m_sLIN.substring(0,this.m_sLIN.length()-1);
            this.m_sLIN+="+";
            
            ArrayList <Integer> t_oLinChilds = t_oLin.getChildLinkages();
            Collections.sort(t_oLinChilds);
            
            for (Integer i : t_oLinChilds) 
            {
                this.m_sLIN+=i+"|";
            }
            this.m_sLIN=this.m_sLIN.substring(0,this.m_sLIN.length()-1);
            this.m_sLIN+=")";
            this.m_sLIN+=a_iChildID+String.valueOf(t_oLin.getChildLinkageType().getType());
            this.m_sLIN+=this.m_strLineSeparator;
            
        }
        
    }
    private void makeCode (GlycoTraverser t_objTraverser) throws GlycoVisitorException {
//         concatenate main CT - substrings        
        this.m_sCTCode = this.m_sRES;
        if (this.m_sLIN!=""){            
            this.m_sCTCode+="LIN" + this.m_strLineSeparator;

            
            this.m_sCTCode+=this.m_sLIN;
        }
        // traverse Repeat subsections
        if ( this.m_aRepeats.size() > 0 )
        {
            int t_iCounter = 0;            
            while ( t_iCounter < this.m_aRepeats.size() ){
                
                this.m_sRES="RES" + this.m_strLineSeparator;

                this.m_sLIN="";
                
                
                t_objTraverser = this.getTraverser(this);
                t_objTraverser.traverseGraph(this.m_aRepeats.get(t_iCounter));
                
                this.m_sREP+="REP"+(t_iCounter+1)+":";
                //internal linkage
                GlycoEdge t_oEdgeInternal =this.m_aRepeats.get(t_iCounter).getRepeatLinkage();
                if ( t_oEdgeInternal != null )
                {
                    if ( t_oEdgeInternal.getGlycosidicLinkages().size() == 0 )
                    {
                        throw new GlycoVisitorException("An internal repeat without a linkage object is not valid.");
                    }
                    ArrayList <Linkage> t_aLinkages = t_oEdgeInternal.getGlycosidicLinkages();
                    GlycoCTLinkageComparator t_oLinComp = new GlycoCTLinkageComparator();
                    Collections.sort(t_aLinkages,t_oLinComp);
                    
                    for (Linkage t_lin : t_aLinkages){
                        this.m_sREP+=this.m_hashNodeID.get(t_oEdgeInternal.getParent())+
                        String.valueOf(t_lin.getParentLinkageType().getType())+
                        "(";
                        // Position section
                        ArrayList <Integer> t_oLinParents = t_lin.getParentLinkages();
                        Collections.sort(t_oLinParents);
                        
                        for (Integer i : t_oLinParents) 
                        {
                            this.m_sREP+=i+"|";
                        }
                        this.m_sREP=this.m_sREP.substring(0,this.m_sREP.length()-1);
                        this.m_sREP+="+";
                        
                        ArrayList <Integer> t_oLinChilds = t_lin.getChildLinkages();
                        Collections.sort(t_oLinChilds);
                        
                        for (Integer i : t_oLinChilds) 
                        {
                            this.m_sREP+=i+"|";
                        }
                        this.m_sREP=this.m_sREP.substring(0,this.m_sREP.length()-1);
                        this.m_sREP+=")";                    
                        this.m_sREP+=this.m_hashNodeID.get(t_oEdgeInternal.getChild())+
                        String.valueOf(t_lin.getChildLinkageType().getType())+
                        "|";                    
                        
                    }
                    this.m_sREP=this.m_sREP.substring(0,this.m_sREP.length()-1);
                }
                
                this.m_sREP+="="+String.valueOf(this.m_aRepeats.get(t_iCounter).getMinRepeatCount())+
                "-"+
                String.valueOf(this.m_aRepeats.get(t_iCounter).getMaxRepeatCount());
                
                this.m_sREP+=this.m_strLineSeparator + this.m_sRES;
                
                if (this.m_sLIN!="")
                {            
                    this.m_sREP+="LIN" + this.m_strLineSeparator + this.m_sLIN;
                }                
                t_iCounter++;
            }
        }
        
        // traverse Underdetermined subtrees, array already prefilled
        GlycoCTUnderdeterminedSubtreeComparator t_oSubTreeComp = new GlycoCTUnderdeterminedSubtreeComparator();
        Collections.sort(this.m_aUnderdeterminedTrees,t_oSubTreeComp);

        Integer t_iUndetCounter =1;
        for (UnderdeterminedSubTree t_oUndetSubtree : m_aUnderdeterminedTrees){
            
            this.m_sRES="RES" + this.m_strLineSeparator;
            
            this.m_sLIN="";            
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_oUndetSubtree);
            
            this.m_sUND += "UND"+
            t_iUndetCounter+":"+
            String.valueOf(t_oUndetSubtree.getProbabilityLower())+
            ":"+
            String.valueOf(t_oUndetSubtree.getProbabilityUpper());

                this.m_sUND+=this.m_strLineSeparator;
            
            // get and write parent IDs
            this.m_sUND+="ParentIDs:";
            ArrayList <GlycoNode> t_oParentNodes = t_oUndetSubtree.getParents();
            
            ArrayList<Integer> t_aNodes = new ArrayList<Integer>();
            for (GlycoNode t_objNode : t_oParentNodes) 
            {
                Integer t_iParent = this.m_hashNodeID.get(t_objNode);
                if ( t_iParent == null )
                {
                    throw new GlycoVisitorException("Error: Parent residue for underdetermined subtree missing");
                }
                t_aNodes.add(t_iParent);
            }
            Collections.sort(t_aNodes);            
            for (Integer t_node : t_aNodes)
            {
                this.m_sUND+=String.valueOf(t_node)+"|";                
            }

            this.m_sUND=this.m_sUND.substring(0,this.m_sUND.length()-1);
            
            this.m_sUND+=this.m_strLineSeparator;

            t_oUndetSubtree.getParents();
            
            // Multiple subtree - main graph connections treated here
            if ( t_oUndetSubtree.getConnection() != null )
            {
                ArrayList <Linkage> t_aLin = t_oUndetSubtree.getConnection().getGlycosidicLinkages();
                if ( t_aLin.size() == 0 )
                {
                    throw new GlycoVisitorException("A subtree connection without a linkage object is not valid.");
                }
                GlycoCTLinkageComparator t_oLinComp = new GlycoCTLinkageComparator ();
                Collections.sort(t_aLin,t_oLinComp);
                
                Integer t_iCount =1;
                for (Linkage t_oLin : t_aLin){
                    this.m_sUND+="SubtreeLinkageID"+
                    t_iCount+":"+
                    String.valueOf(t_oLin.getParentLinkageType().getType())
                    +"(";
                    
                    ArrayList <Integer> t_oLinParents = t_oLin.getParentLinkages();
                    Collections.sort(t_oLinParents);
                    
                    for (Integer i : t_oLinParents) 
                    {
                        this.m_sUND+=i+"|";
                    }
                    this.m_sUND=this.m_sUND.substring(0,this.m_sUND.length()-1);
                    this.m_sUND+="+";
                    
                    ArrayList <Integer> t_oLinChilds = t_oLin.getChildLinkages();
                    Collections.sort(t_oLinChilds);
                    
                    for (Integer i : t_oLinChilds) 
                    {
                        this.m_sUND+=i+"|";
                    }
                    this.m_sUND=this.m_sUND.substring(0,this.m_sUND.length()-1);                
                    this.m_sUND+=")"+
                    String.valueOf(t_oLin.getChildLinkageType().getType());                
                    

                    this.m_sUND+=this.m_strLineSeparator;

                    t_iCount++;
                }
            }            
            //concatenate substrings Header RES LIN
            this.m_sUND+=this.m_sRES;
            
            if (this.m_sLIN!="")
            {            
                this.m_sUND+="LIN" + this.m_strLineSeparator;
                
                this.m_sUND+=this.m_sLIN;
            }                
            t_iUndetCounter++;
        }
        // Alternative subgraphs
        Integer t_iAltCounter =1;
        for (SugarUnitAlternative t_oAltUnit : m_aAlternativeUnits){
            this.m_sALT+="ALT"+t_iAltCounter + this.m_strLineSeparator;

            ArrayList <GlycoGraphAlternative> t_aAltGraph = t_oAltUnit.getAlternatives();
            GlycoCTGraphAlternativeComparator t_comp = new GlycoCTGraphAlternativeComparator();
            Collections.sort(t_aAltGraph,t_comp);
            
            Integer t_countAltSubgraph=1;
            for (GlycoGraphAlternative t_oAltSubGraph : t_aAltGraph){
                this.m_sALT+="ALTSUBGRAPH"+t_countAltSubgraph;
                
                
                // traverse subtree
                ArrayList<GlycoNode> t_oAltSubGraphRoots = new ArrayList<GlycoNode>();
                try {
                    t_oAltSubGraphRoots = t_oAltSubGraph.getRootNodes();
                } catch (GlycoconjugateException e) {
                    
                }        
                //    priorize according to GlycoCT all isolated subgraphs and process consecutivly
                GlycoCTGlycoNodeComparator t_oNodeComparator = new GlycoCTGlycoNodeComparator();
                Collections.sort(t_oAltSubGraphRoots,t_oNodeComparator);           
                
                this.m_sRES="RES" + this.m_strLineSeparator;
                
                this.m_sLIN="";
                for (GlycoNode t_oNode : t_oAltSubGraphRoots){
                    t_objTraverser = this.getTraverser(this);
                    t_objTraverser.traverse(t_oNode);
                }
                // compose ALT parent connection - LEAD IN
                GlycoNode t_oLeadIn = t_oAltSubGraph.getLeadInNode();
                if (t_oLeadIn!=null){
                    this.m_sALT+="\nLEAD-IN RES:"+String.valueOf(this.m_hashNodeID.get(t_oLeadIn));    
                }
                // compose ALT child connection - LEAD OUT (K: out, V: inside)
                HashMap<GlycoNode,GlycoNode> t_hMapping = t_oAltSubGraph.getLeadOutNodeToNode();
                
                if (!t_hMapping.isEmpty()){

                    this.m_sALT+=this.m_strLineSeparator;
                    
                    this.m_sALT+="LEAD-OUT RES:";
                    ArrayList <GlycoNode> t_aTempNodes = new ArrayList <GlycoNode> ();
                    for (GlycoNode t_node: t_hMapping.keySet()){
                        t_aTempNodes.add(t_node);
                    }
                    Collections.sort(t_aTempNodes,t_oNodeComparator);                     
                    for (GlycoNode t_oNodeMain : t_aTempNodes){
                        if (t_oAltSubGraph.containsNode(t_hMapping.get(t_oNodeMain))){                            
                            Integer t_iNodeMain = this.m_hashNodeID.get(t_oNodeMain);
                            Integer t_iLeadOut = this.m_hashNodeID.get(t_hMapping.get(t_oNodeMain));
                            
                            this.m_sALT+=String.valueOf(t_iLeadOut)+"+"+String.valueOf(t_iNodeMain)+"|";
                        }
                    }

                    this.m_sALT+=this.m_strLineSeparator;
                    
                }                
                //concatenate substrings Header RES LIN
                this.m_sALT+=this.m_sRES;
                
                if (this.m_sLIN!="")
                {            
                    this.m_sALT+="LIN" + this.m_strLineSeparator;
                    
                    this.m_sALT+=this.m_sLIN;
                }                
                t_countAltSubgraph++;                
            }             
        }        
        // concatenate final string
        if (this.m_sREP!="")
        {
            this.m_sCTCode+="REP" + this.m_strLineSeparator + this.m_sREP;
        }            
        if (this.m_sUND!="")
        {
            this.m_sCTCode+="UND" + this.m_strLineSeparator + this.m_sUND;
        }    
        if (this.m_sALT!="")
        {
            this.m_sCTCode+="ALT" + this.m_strLineSeparator + this.m_sALT;
        }    
        if ( this.m_aNonMS.size() > 0 )
        {
            if ( this.m_bStrict )
            {
                throw new GlycoVisitorException("NonMonosaccharide units are not allowed for GlycoCT{condensed}");
            }
            else
            {
                this.m_sCTCode+= this.exportNonMS();
            }
        }
        
    }

    /**
     * @return
     * @throws GlycoVisitorException 
     */
    private String exportNonMS() throws GlycoVisitorException 
    {
        Integer t_iCounter = 0;
        String t_strResult = "";
        for (Iterator<NonMonosaccharide> t_iterNon = this.m_aNonMS.iterator(); t_iterNon.hasNext();) 
        {
            t_iCounter++;
            NonMonosaccharide t_objNon = t_iterNon.next();
            t_strResult += "NON" + t_iCounter.toString() + "\n";
            Linkage t_objLink = null;
            if ( t_objNon.getParentEdge() == null )
            {
                if ( t_objNon.getChildEdges().size() != 1 )
                {
                    throw new GlycoVisitorException("NonMonosaccharide units with more or less than one connection are not allowed for GlycoCT{condensed}");                    
                }
                for (Iterator<GlycoEdge> t_iter = t_objNon.getChildEdges().iterator(); t_iter.hasNext();) 
                {
                    GlycoEdge t_objEdge = t_iter.next();
                    if ( t_objEdge.getGlycosidicLinkages().size() != 1 )
                    {
                        throw new GlycoVisitorException("NonMonosaccharide units with more or less than one linkage are not allowed for GlycoCT{condensed}");                        
                    }
                    for (Iterator<Linkage> t_iterLink = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLink.hasNext();) 
                    {
                        t_objLink = t_iterLink.next();                        
                    }
                    Integer t_iID = this.m_hashNodeID.get(t_objEdge.getChild());
                    t_strResult += "Child:" + t_iID.toString() + "\nLinkage:";
                }
            }
            else
            {
                if ( t_objNon.getChildEdges().size() > 0 )
                {
                    throw new GlycoVisitorException("NonMonosaccharide units with parent and child connection are not allowed for GlycoCT{condensed}");                    
                }
                GlycoEdge t_objEdge = t_objNon.getParentEdge();
                if ( t_objEdge.getGlycosidicLinkages().size() != 1 )
                {
                    throw new GlycoVisitorException("NonMonosaccharide units with more or less than one linkage are not allowed for GlycoCT{condensed}");                        
                }
                for (Iterator<Linkage> t_iterLink = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLink.hasNext();) 
                {
                    t_objLink = t_iterLink.next();                        
                }
                Integer t_iID = this.m_hashNodeID.get(t_objEdge.getParent());
                t_strResult += "Parent:" + t_iID.toString() + "\nLinkage:";
            }
            t_strResult+= String.valueOf(t_objLink.getParentLinkageType().getType())+ "(";
            // Position section
            ArrayList <Integer> t_oLinParents = t_objLink.getParentLinkages();
            Collections.sort(t_oLinParents);            
            for (Integer i : t_oLinParents) 
            {
                t_strResult+=i+"|";
            }
            t_strResult=t_strResult.substring(0,t_strResult.length()-1);
            t_strResult+="+";
            
            ArrayList <Integer> t_oLinChilds = t_objLink.getChildLinkages();
            Collections.sort(t_oLinChilds);
            
            for (Integer i : t_oLinChilds) 
            {
                t_strResult+=i+"|";
            }
            t_strResult=t_strResult.substring(0,t_strResult.length()-1);
            t_strResult+=")";                    
            t_strResult+=String.valueOf(t_objLink.getChildLinkageType().getType())+"\n";            
            t_strResult += "HistoricalEntity:" + t_objNon.getName() + "\n";
        }
        return t_strResult;        
    }
    
}