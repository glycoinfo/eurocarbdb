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
package org.eurocarbdb.MolecularFramework.io;


import java.util.HashMap;

/**
* @author Logan
*
*/
public class ErrorTextEng 
{
    static public String getErrorText( String a_strCode )
    {
        // nicht der cleverste Weg es zu tun
        HashMap<String,String> t_aTexts = new HashMap<String,String>();
        // common parser errors
        t_aTexts.put("COMMON000","Unexpected end of string");
        t_aTexts.put("COMMON002","Unexpected charakter : await a number (1...9) before the '.' in a float number");
        t_aTexts.put("COMMON003","Unexpected charakter : await a char (A...Za...z)");
        t_aTexts.put("COMMON004","Unexpected charakter : await a number (1...9)"); 
        t_aTexts.put("COMMON009","Unexpected charakter : await a number (0...9) after a '.' in a float number");
        t_aTexts.put("COMMON010","Unexpected charakter : await a lowercase char (a...z)");
        t_aTexts.put("COMMON011","Unexpected charakter : await a uppercase char (A...Z)");
        t_aTexts.put("COMMON012","Unable to get ahead token.");
        t_aTexts.put("COMMON013","Imposible to create a sugar. Error of the sugar framework.");
        // kcf errors
        t_aTexts.put("KCF000","Unexpected character : await endsequence '///'");
        t_aTexts.put("KCF001","Unknown format : Sequence format should end.");
        t_aTexts.put("KCF002","Unexpected character : await startsequence 'ENTRY'");
        t_aTexts.put("KCF003","Unexpected character : await ' ' after ENTRY");        
        t_aTexts.put("KCF004","Unexpected character : await ' ' after GO number");
        t_aTexts.put("KCF005","Unexpected character : await 'Glycan'");
        t_aTexts.put("KCF006","Unexpected character : await linebreak after 'Glycan'");
        t_aTexts.put("KCF007","Unexpected character : await GO number G...");
        t_aTexts.put("KCF008","Unexpected character : await 'NODE'");
        t_aTexts.put("KCF009","Unexpected character : await ' ' after node number");
        t_aTexts.put("KCF010","Unexpected character : await ' ' after node name");
        t_aTexts.put("KCF011","Unexpected character : await ' ' after node x coordinate");
        t_aTexts.put("KCF012","Unexpected character : await linebreak after node coordinates");
        t_aTexts.put("KCF013","Unknown format : The count of nodes does not match");
        t_aTexts.put("KCF014","Unexpected number : duplicated residuenumber");
        t_aTexts.put("KCF015","Unexpected number : x values of a bracket must be equal.");
        t_aTexts.put("KCF016","Error in KCF : Not all nodes are connected.");
        t_aTexts.put("KCF017","Error in KCF : Not all parts of the sugar are connected.");
        t_aTexts.put("KCF018","Error in KCF : Not all repeats are connected.");
        t_aTexts.put("KCF019","Error in KCF : Impossible to find root residue (no unique x coordinate).");
        t_aTexts.put("KCF020","Unexpected character : after an edge number");
        t_aTexts.put("KCF021","Unexpected character : await a linebreak after a edge definition");
        t_aTexts.put("KCF023","Unexpected character : await EDGE");
        t_aTexts.put("KCF024","Unexpected character : await ' ' after EDGE");
        t_aTexts.put("KCF025","Unexpected character : await linebreak in EDGE line");
        t_aTexts.put("KCF026","Unknown format : The count of edges does not match");
        t_aTexts.put("KCF027","Unknown node : Missing node definition for this edge");
        t_aTexts.put("KCF030","Unexpected charakter : await a number");
        t_aTexts.put("KCF032","Unexpected character : await ' ' after 'NODE'");
        t_aTexts.put("KCF033","Unexpected character : await linebreak in NODE line");
        t_aTexts.put("KCF040","Unexpected character : await 'BRACKET'");
        t_aTexts.put("KCF043","Unexpected character : await ' ' after BRACKET number");
        t_aTexts.put("KCF044","Unexpected character : await ' ' after BRACKET coordinate");
        t_aTexts.put("KCF045","Unexpected character : await linebreak after bracket line");
        t_aTexts.put("KCF046","Unexpected character : await ' ' after bracket final line number");
        t_aTexts.put("KCF047","Unexpected character : await ' ' after bracket final coordinate");
        t_aTexts.put("KCF048","Unexpected character : await linebreak after bracket final line");
        t_aTexts.put("KCF050","Imposible to create a sugar. Error of the sugar framework.");
        t_aTexts.put("KCF051","Imposible to create a sugar. Problem with nested repeat.");
        t_aTexts.put("KCF052","Imposible to create a sugar. Problem with grouped repeat.");
        t_aTexts.put("KCF053","Imposible to create a sugar. Usage of * without repeat.");
        t_aTexts.put("KCF054","Imposible to create a sugar. Multilinkage internal repeatlinkage is not possible.");
        t_aTexts.put("KCF055","Imposible to create a sugar. Incomplete leftsided repeat.");
        t_aTexts.put("KCF056","Imposible to create a sugar. Not all repeat units are connected.");
        t_aTexts.put("KCF057","Imposible to create a sugar. * can not have parent and child residue.");
        t_aTexts.put("KCF058","Imposible to create a sugar. * must be connected with a repeat unit.");
        t_aTexts.put("KCF059","Imposible to create a sugar. * can not have more than one child residue.");
        // IUPAC2D
        t_aTexts.put("IUPAC2D000","Invalide start of repeat block: should be '-(<position>-'");
        t_aTexts.put("IUPAC2D001","Error each linkage has the pattern '-(<position>-<position>)-'");
        t_aTexts.put("IUPAC2D002","Invalide Linkage: await '|'");
        t_aTexts.put("IUPAC2D003","Invalide linkage, breaks graph above. A linkage above must end with '+'");
        t_aTexts.put("IUPAC2D004","Invalide linkage, breaks graph left.");
        t_aTexts.put("IUPAC2D005","Error each linkage above has the pattern '-(<position>-<position>)+'");
        t_aTexts.put("IUPAC2D006","Invalide repeat statement. There is a character before repeate.");
        t_aTexts.put("IUPAC2D007","Invalide cyclic statement. There is a character before cylcic.");
        t_aTexts.put("IUPAC2D008","Invalide Linkage postion.");
        t_aTexts.put("IUPAC2D009","A alternativ linkage consist of '...<position>/<position>)-'.");
        t_aTexts.put("IUPAC2D010","Imposible to create a sugar. Error of the sugar framework.");
        t_aTexts.put("IUPAC2D011","Invalide Input : Sugar is not connected.");
        t_aTexts.put("IUPAC2D012","One REPEAT or CYCLIC is not closed");
        t_aTexts.put("IUPAC2D013","O linkage can only be attached to a simple residue");
        t_aTexts.put("IUPAC2D014","Unexpected '[' : no repeating unit was opend before");
        t_aTexts.put("IUPAC2D015","Unexpected 'repeat' : repeating unit is closed at another point");
        t_aTexts.put("IUPAC2D016","Unexpected 'cyclic' : cyclic unit is closed at another point");
        t_aTexts.put("IUPAC2D017","Unexpected end of repeat or cyclic : no specieal unit was opend before");
        t_aTexts.put("IUPAC2D018","Unexpected character : expect a ']' for a oping repeat");
        t_aTexts.put("IUPAC2D019","Unexpected character : invalide linkage position, must be a number");
        t_aTexts.put("IUPAC2D020","Not implementetd feature '[...]'");
        t_aTexts.put("IUPAC2D021","Unexpected '[' : repeat unit was closed before.");
        t_aTexts.put("IUPAC2D022","Error in repeat starting linkage. No residue found.");
        t_aTexts.put("IUPAC2D023","Invalide sugar : sugar does not have residues.");
        t_aTexts.put("IUPAC2D024","Unexpected start of line: A line can not start with '.");
        // LINUCS
        t_aTexts.put("LINUCS001","Unexpected character : await '[]' at the beginning of a sequence");
        t_aTexts.put("LINUCS002","Unexpected character : await '[' before a residue definition");
        t_aTexts.put("LINUCS003","Unexpected character : await ']' after a residue definition");
        t_aTexts.put("LINUCS004","Unexpected character : await '{' after a residue definition");
        t_aTexts.put("LINUCS005","Unexpected character : await '}' after a sub-residue definition");
        t_aTexts.put("LINUCS006","Unknown format : Sequence format should end.");
        t_aTexts.put("LINUCS007","Unexpected character : await '/'");
        t_aTexts.put("LINUCS008","Unexpected character : await '[(' before a linkage definition");
        t_aTexts.put("LINUCS009","Unexpected character : await '+' in a linkage");
        t_aTexts.put("LINUCS010","Unexpected character : await ')]' after a linkage definition");
        t_aTexts.put("LINUCS011","Unexpected character : await 'REPEAT'");
        t_aTexts.put("LINUCS012","Unexpected character : await 'CYCLIC'");
        t_aTexts.put("LINUCS013","Unexpected character : await 'UNTIL'");
        t_aTexts.put("LINUCS014","Unexpected REPEAT : a structure with a REPEAT inside must start with '[][LINK]{[(UNTIL+...'");
        t_aTexts.put("LINUCS015","Unexpected CYCLIC : a structure with a CYCLIC inside must start with '[][LINK]{[(UNTIL+...'");
        t_aTexts.put("LINUCS016","A REPEAT or a CYCLIC block must end with ')][LINK]{}'");
        t_aTexts.put("LINUCS017","Unexpected '<' : no repeating unit was opend before");
        t_aTexts.put("LINUCS018","One REPEAT or CYCLIC is not closed");
        t_aTexts.put("LINUCS019","Unexpected end of REPEAT unit");
        t_aTexts.put("LINUCS020","Unexpected end of CYCLIC unit");
        t_aTexts.put("LINUCS021","Imposible to create a sugar. Error of the sugar framework.");
        // BCSDB
        t_aTexts.put("BCSDB001","Unexpected character : await '(' at the beginning of a repeating sugar");
        t_aTexts.put("BCSDB002","Unexpected character : await ')' at the end of a repeating sugar");
        t_aTexts.put("BCSDB003","Unexpected character : await '-' at the end of a repeating sugar");
        t_aTexts.put("BCSDB004","Unexpected character : Sequence format should end.");
        t_aTexts.put("BCSDB005","Unexpected character : await '(' at the beginning of a linkage");
        t_aTexts.put("BCSDB006","Unexpected character : await '-' in a linkage");
        t_aTexts.put("BCSDB007","Unexpected character : await ')' at the end of a linkage");
        t_aTexts.put("BCSDB008","Unexpected character : await ']' at the beginning of a sidechain");
        t_aTexts.put("BCSDB009","Unexpected character : await '[' at the end of a sidechain");
        t_aTexts.put("BCSDB010","Unexpected character : await '-' before and after a 'p'");
        t_aTexts.put("BCSDB011","Unexpected character : Multi linkage seems to link to different residues");
        t_aTexts.put("BCSDB012","Unexpected character : await 'P'");
        t_aTexts.put("BCSDB013","Unexpected character : error while parsing multilinked residue");
        t_aTexts.put("BCSDB014","Unexpected character : error while parsing branch of an branch");
        t_aTexts.put("BCSDB015","Unexpected character : side-chain of a multi linkaged residue can only consist of one residue");
        t_aTexts.put("BCSDB016","Critical error : multi linkaged residue does not exist.");
        t_aTexts.put("BCSDB017","Unexpected character : multi linkaged residue is not connected to a simple residue.");
        t_aTexts.put("BCSDB018","More than one additional definitions (//).");
        t_aTexts.put("BCSDB019","Unknown additional definition (//), missing 'sug'.");
        t_aTexts.put("BCSDB020","Missing '=' in additional definition (//).");
        t_aTexts.put("BCSDB021","Error while replacing sug.");
        // cfg
        t_aTexts.put("CFG000","Unexpected character : missing anomeric information.");
        t_aTexts.put("CFG001","Unexpected character : empty residue name.");
        t_aTexts.put("CFG002","Unexpected character : modification must start with a '['.");
        t_aTexts.put("CFG003","Unexpected character : this character is not allowed in a modification.");
        t_aTexts.put("CFG004","Unexpected character : branch must end with ')'.");
        t_aTexts.put("CFG005","Unexpected character : invalide end of sequence.");
        t_aTexts.put("CFG006","Unexpected character : expect '=' for uncertain branch definition.");
        t_aTexts.put("CFG007","Unexpected character : uncertain branch should be finished.");
        t_aTexts.put("CFG008","Unexpected character : expect '%' in an uncertain branch position.");
        t_aTexts.put("CFG009","Unexpected character : Uncertain subtree was not defined.");
        t_aTexts.put("CFG010","Uncertain subtrees can only be added to repeat units or sugars.");
        t_aTexts.put("CFG011","Uncertain subtrees seems to be contacted to be connected to sugar and a repeat unit or to two different repeat units.");        
        t_aTexts.put("CFG012","Uncertain subtrees in uncertain subtrees are not allowed.");
        // ogbi
        t_aTexts.put("OGBI001","Unexpected character : missing 'c' in core fucose.");
        t_aTexts.put("OGBI002","Unexpected character : core fucosylation must be 3 or 6 linked.");
        t_aTexts.put("OGBI003","Unexpected character : no linkage postion for core fucose.");
        t_aTexts.put("OGBI004","Unexpected character : missing ')' for core fucose.");
        t_aTexts.put("OGBI005","Unexpected character : core fucose is linked two time through the same position");
        t_aTexts.put("OGBI006","Unexpected character : glycan type must be 'M'.");
        t_aTexts.put("OGBI007","Unexpected character : expect 'A' for glycan type.");
        t_aTexts.put("OGBI008","Unexpected character : unvalide glycan type, must be 'M3'.");
        t_aTexts.put("OGBI009","Unexpected character : error sequence not finished.");
        t_aTexts.put("OGBI010","Unexpected character : expect ']' in A type definition.");
        t_aTexts.put("OGBI011","Unexpected character : A type definition must be between 1 and 4.");
        t_aTexts.put("OGBI012","Unexpected character : A 1 type definition for branch can only be 3 or 6.");
        t_aTexts.put("OGBI013","Unexpected character : Gal type can not be higher then A type.");
        t_aTexts.put("OGBI014","Unexpected character : missing ')' in linkage position definition.");
        t_aTexts.put("OGBI015","Unexpected character : only D1 position is allowed for M2.");
        t_aTexts.put("OGBI016","Unexpected character : only D1 or D2 position is allowed for M4.");
        t_aTexts.put("OGBI017","Unexpected character : only D1 D2 or D3 position is allowed for M6.");
        t_aTexts.put("OGBI018","Unexpected character : only D1 D2 or D3 position is allowed for M7.");
        t_aTexts.put("OGBI019","Unexpected character : M1 to M9 are allowed for High Mannoses.");
        t_aTexts.put("OGBI020","Unexpected character : for M8 two possitions (D1 D2 or D3) must be given.");
        t_aTexts.put("OGBI021","Unexpected character : for the two M8 possitions (D1 D2 or D3) two different positions must be given.");
        t_aTexts.put("OGBI022","Unexpected character : there must be a ',' between two positions.");
        t_aTexts.put("OGBI023","Unexpected character : more than one linkage position is not allowed for A1.");
        t_aTexts.put("OGBI024","Unexpected character : only A1 to A4 are allowed.");        
        t_aTexts.put("OGBI025","Unexpected character : [] is not allowed for A3.");
        t_aTexts.put("OGBI026","Unexpected number : number of Fuc at GlcNAc cant be larger then A type.");
        t_aTexts.put("OGBI027","Unexpected number : several positions for Fuc at GlcNAc are not allowed.");
        t_aTexts.put("OGBI028","Unexpected number : only 3 or 6 are allowed for A1 or A2 branch position.");
        t_aTexts.put("OGBI029","Unexpected number : positions for Gal must be one position or a list of n positions (n=number of Gal).");
        t_aTexts.put("OGBI030","Unexpected number : number of Gal and number of attach position does not match.");
        t_aTexts.put("OGBI031","Unexpected number : Gal can not be at the 3-2 GlcNAc (does not exist).");
        t_aTexts.put("OGBI032","Unexpected number : Gal can not be at the 6-2 GlcNAc (does not exist).");
        t_aTexts.put("OGBI033","Unexpected character : hybrid type is only possible for M2, M4, M5.");
        t_aTexts.put("OGBI034","Unexpected character : A1[6] is not valide for a hybrid type.");
        t_aTexts.put("OGBI035","Unexpected character : a nested underdeterminded block is not allowed.");
        t_aTexts.put("OGBI036","Unexpected character : several Gal link position are not allowed for a defined A1 or A2 branch.");
        t_aTexts.put("OGBI037","Unexpected character : only one Gal is allowed for a defined A1 or A2 branch.");
        t_aTexts.put("OGBI038","Unexpected character : the number of Fucose at Gal residue must be less or equal the number of Gal residues.");
        t_aTexts.put("OGBI039","Unexpected number : number of Fuc at Gal and number of attach position does not match.");
        t_aTexts.put("OGBI040","Unexpected character : the number of sialic acids at Gal residue must be less or equal the number of Gal residues.");
        t_aTexts.put("OGBI041","Unexpected number : number of sialic acid at Gal and number of attach position does not match.");
        t_aTexts.put("OGBI042","Unexpected character : the number of Gal at Gal residue must be less or equal the number of Gal residues.");
        t_aTexts.put("OGBI043","Unexpected number : number of Gal at Gal and number of attach position does not match.");        
        // glycoCTC
        t_aTexts.put("GLYCOCTC001","Unexpected character : missing 'LIN' in linkage definition.");
        t_aTexts.put("GLYCOCTC002","Unexpected character : missing 'RES' in residue definition.");
        t_aTexts.put("GLYCOCTC003","Unexpected character : unknown residue type.");
        t_aTexts.put("GLYCOCTC004","Unexpected character : unknown substituent.");
        t_aTexts.put("GLYCOCTC005","Unexpected character : missing ':'.");
        t_aTexts.put("GLYCOCTC006","Unexpected character : Unknown symbol for anomer.");
        t_aTexts.put("GLYCOCTC007","Unexpected character : missing '-'.");
        t_aTexts.put("GLYCOCTC008","Unexpected character : unknown basetpye.");
        t_aTexts.put("GLYCOCTC009","Unexpected character : unknown superclass.");
        t_aTexts.put("GLYCOCTC010","Unexpected character : unknown modification.");
        t_aTexts.put("GLYCOCTC011","Unexpected character : alternative declaration must start with 'a'.");
        t_aTexts.put("GLYCOCTC012","Unexpected character : repeat declaration must start with 'r'.");
        t_aTexts.put("GLYCOCTC013","Unexpected character : unknown LinkageType.");
        t_aTexts.put("GLYCOCTC014","Unexpected character : missing '('.");
        t_aTexts.put("GLYCOCTC015","Unexpected character : missing '+'.");
        t_aTexts.put("GLYCOCTC015","Unexpected character : missing ')'.");
        t_aTexts.put("GLYCOCTC016","Error in linkage : residue can not have more than one parent.");
        t_aTexts.put("GLYCOCTC017","Invalide residue numbers.");
        t_aTexts.put("GLYCOCTC018","Unexpected character : string should end.");
        t_aTexts.put("GLYCOCTC019","Unexpected character : missing 'REP' in repaet definition.");
        t_aTexts.put("GLYCOCTC020","Unexpected character : missing '='.");
        t_aTexts.put("GLYCOCTC021","Unexpected character : only -1 is allowed as an negative number for repeat count.");
        t_aTexts.put("GLYCOCTC022","Unexpected character : repeat unit was not declarated before.");
        t_aTexts.put("GLYCOCTC023","Unexpected character : missing 'ALT' in alternative definition.");
        t_aTexts.put("GLYCOCTC024","Unexpected character : alternative unit was not declarated before.");
        t_aTexts.put("GLYCOCTC025","Unexpected character : missing 'LEAD-IN' definition in alternative unit.");
        t_aTexts.put("GLYCOCTC026","Unexpected character : missing 'LEAD-OUT' definition in alternative unit.");
        t_aTexts.put("GLYCOCTC027","Unexpected character : duplicated childresidue in 'LEAD-OUT' definition in alternative unit.");
        t_aTexts.put("GLYCOCTC028","Unexpected character : missing 'UND' in underdeterminded definition.");
        t_aTexts.put("GLYCOCTC029","Unexpected character : missing 'ParentIDs:' definition in underdeterminded unit.");
        t_aTexts.put("GLYCOCTC030","Unexpected character : missing 'SubtreeLinkageID' definition in underdeterminded unit.");
        t_aTexts.put("GLYCOCTC031","Error in underdeterminded unit : no parent given.");
        t_aTexts.put("GLYCOCTC032","Error in underdeterminded unit : invalide parent id.");
        t_aTexts.put("GLYCOCTC033","Error in <underDeterminedSubtree> all parents must be in the same unit.");
        t_aTexts.put("GLYCOCTC034","Error in <underDeterminedSubtree>: Tree can only be connected to an repeat unit or an sugar."); 
        t_aTexts.put("GLYCOCTC035","Unexpected character : unknown linkage position must be '-1'.");
        t_aTexts.put("GLYCOCTC036","Unexpected character : missing 'ALTSUBGRAPH' definition in alternative unit.");
        t_aTexts.put("GLYCOCTC037","Unexpected character : missing '|'.");
        t_aTexts.put("GLYCOCTC038","Unexpected character : missing 'NON'.");
        t_aTexts.put("GLYCOCTC039","Unexpected character : error in NON number.");
        t_aTexts.put("GLYCOCTC040","Unexpected character : error in NON keywords.");
        t_aTexts.put("GLYCOCTC041","Unexpected character : unsupported NON type.");
        t_aTexts.put("GLYCOCTC042","Unexpected character : unknown residue number for NON connection residue.");
        t_aTexts.put("GLYCOCTC043","Unexpected character : NON residue can not be attached to the sugar.");
        t_aTexts.put("GLYCOCTC044","Unexpected character : NON residue can not be attached to the residue. Residue still have a parent residue.");
        t_aTexts.put("GLYCOCTC045","Unexpected character : missing line terminator.");
        t_aTexts.put("GLYCOCTC046","Line terminator must consist of at least one character.");
        t_aTexts.put("GLYCOCTC047","Line terminator must not be '$'.");
        
        // Glycobase Lille
        t_aTexts.put("GLYCOBASE000","Missing structure type.");
        t_aTexts.put("GLYCOBASE001","Unknown structure type.");
        t_aTexts.put("GLYCOBASE002","Missing reducing end residue.");
        t_aTexts.put("GLYCOBASE003","Missing core type for O-glycan.");
        t_aTexts.put("GLYCOBASE004","Unknown core type for N-glycan.");
        t_aTexts.put("GLYCOBASE005","Unknown core type for o-glycan.");
        t_aTexts.put("GLYCOBASE006","Missing position part in sugar increment \"[incr. ...]\"");
        t_aTexts.put("GLYCOBASE007","Error in position part of sugar increment. \"[incr. ...]\"");
        t_aTexts.put("GLYCOBASE008","Unknown anomer.");
        t_aTexts.put("GLYCOBASE009","Error in ringsize definition.");
        t_aTexts.put("GLYCOBASE010","Error in linkage definition.");
        t_aTexts.put("GLYCOBASE011","Unknown increment position.");
        t_aTexts.put("GLYCOBASE012","Error in linkage position of substituent.");
        t_aTexts.put("GLYCOBASE012","Error in substituent definition \"with ...\".");
        // IUPAC condenced
        t_aTexts.put("IUPAC000","Unexpected character : missing '('.");
        t_aTexts.put("IUPAC001","Unexpected character : missing ')'.");
        t_aTexts.put("IUPAC002","Unexpected character : string should end.");
        t_aTexts.put("IUPAC003","Unexpected character : missing anomer (a,b,o,?).");
        t_aTexts.put("IUPAC004","Unexpected character : missing '['.");
        t_aTexts.put("IUPAC005","Unexpected character : missing '-'.");
        if ( t_aTexts.containsKey(a_strCode) )
        {
            return t_aTexts.get(a_strCode);
        }
        return null;
    }

}
