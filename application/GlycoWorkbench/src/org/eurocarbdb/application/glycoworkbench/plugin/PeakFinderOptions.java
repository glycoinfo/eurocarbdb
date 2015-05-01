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
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;
import java.util.*;

public class PeakFinderOptions {
    
    public String DERIVATIZATION = "Und";
    public String REDUCING_END = "freeEnd";

    public String OTHER_REDEND_NAME = "XXX";
    public double OTHER_REDEND_MASS = 0.;

    public String OR1_NAME = "Or1";
    public String OR2_NAME = "Or2";
    public String OR3_NAME = "Or3";
    public double OR1_MASS = 0.;
    public double OR2_MASS = 0.;
    public double OR3_MASS = 0.;

    public int MIN_PEN=0,       MAX_PEN=0;
    public int MIN_HEX=0,       MAX_HEX=8;
    public int MIN_HEP=0,       MAX_HEP=0;
    public int MIN_HEXN=0,      MAX_HEXN=0;
    public int MIN_HEXNAC=0,    MAX_HEXNAC=7;
    public int MIN_DPEN=0,      MAX_DPEN=0;
    public int MIN_DHEX=0,      MAX_DHEX=2;
    public int MIN_DDHEX=0,     MAX_DDHEX=0;
    public int MIN_MEHEX=0,     MAX_MEHEX=0;

    public int MIN_OR1=0,       MAX_OR1=0;
    public int MIN_OR2=0,       MAX_OR2=0;
    public int MIN_OR3=0,       MAX_OR3=0;

    public int MIN_HEXA=0,      MAX_HEXA=0;
    public int MIN_DHEXA=0,     MAX_DHEXA=0;
    public int MIN_NEU5GC=0,    MAX_NEU5GC=0;
    public int MIN_NEU5AC=0,    MAX_NEU5AC=2;    
    public int MIN_NEU5GCLAC=0, MAX_NEU5GCLAC=0;
    public int MIN_NEU5ACLAC=0, MAX_NEU5ACLAC=0;
    public int MIN_KDO=0,       MAX_KDO=0;
    public int MIN_KDN=0,       MAX_KDN=0;
    public int MIN_MUR=0,       MAX_MUR=0;

    public int MIN_S=0,         MAX_S=0;
    public int MIN_P=0,         MAX_P=0;
    public int MIN_AC=0,        MAX_AC=0;
    public int MIN_PYR=0,       MAX_PYR=0;
    public int MIN_PC=0,        MAX_PC=0;
  
    // serialization    

    public void store(Configuration config) {
    config.put("PeakfinderOptions","derivatization",DERIVATIZATION);    
    config.put("PeakfinderOptions","reducing_end",REDUCING_END);    
    config.put("PeakfinderOptions","other_redend_name",OTHER_REDEND_NAME);    
    config.put("PeakfinderOptions","other_redend_mass",OTHER_REDEND_MASS);    

    config.put("PeakfinderOptions","min_pen",MIN_PEN);    
    config.put("PeakfinderOptions","max_pen",MAX_PEN);    
    config.put("PeakfinderOptions","min_hex",MIN_HEX);    
    config.put("PeakfinderOptions","max_hex",MAX_HEX);    
    config.put("PeakfinderOptions","min_hep",MIN_HEP);    
    config.put("PeakfinderOptions","max_hep",MAX_HEP);    
    config.put("PeakfinderOptions","min_hexn",MIN_HEXN);    
    config.put("PeakfinderOptions","max_hexn",MAX_HEXN);    
    config.put("PeakfinderOptions","min_hexnac",MIN_HEXNAC);    
    config.put("PeakfinderOptions","max_hexnac",MAX_HEXNAC);    
    config.put("PeakfinderOptions","min_dpen",MIN_DPEN);    
    config.put("PeakfinderOptions","max_dpen",MAX_DPEN);    
    config.put("PeakfinderOptions","min_dhex",MIN_DHEX);    
    config.put("PeakfinderOptions","max_dhex",MAX_DHEX);    
    config.put("PeakfinderOptions","min_ddhex",MIN_DDHEX);    
    config.put("PeakfinderOptions","max_ddhex",MAX_DDHEX);    
    config.put("PeakfinderOptions","min_mehex",MIN_MEHEX);    
    config.put("PeakfinderOptions","max_mehex",MAX_MEHEX);

    config.put("PeakfinderOptions","min_or1",MIN_OR1);    
    config.put("PeakfinderOptions","max_or1",MAX_OR1);    
    config.put("PeakfinderOptions","min_or2",MIN_OR2);    
    config.put("PeakfinderOptions","max_or2",MAX_OR2);    
    config.put("PeakfinderOptions","min_or3",MIN_OR3);    
    config.put("PeakfinderOptions","max_or3",MAX_OR3);    
    config.put("PeakfinderOptions","or1_mass",OR1_MASS);    
    config.put("PeakfinderOptions","or2_mass",OR2_MASS);    
    config.put("PeakfinderOptions","or3_mass",OR3_MASS);    

    config.put("PeakfinderOptions","min_hexa",MIN_HEXA);    
    config.put("PeakfinderOptions","max_hexa",MAX_HEXA);    
    config.put("PeakfinderOptions","min_dhexa",MIN_DHEXA);    
    config.put("PeakfinderOptions","max_dhexa",MAX_DHEXA);    
    config.put("PeakfinderOptions","min_neu5gc",MIN_NEU5GC);    
    config.put("PeakfinderOptions","max_neu5gc",MAX_NEU5GC);    
    config.put("PeakfinderOptions","min_neu5ac",MIN_NEU5AC);    
    config.put("PeakfinderOptions","max_neu5ac",MAX_NEU5AC);    
    config.put("PeakfinderOptions","min_neu5gclac",MIN_NEU5GCLAC);    
    config.put("PeakfinderOptions","max_neu5gclac",MAX_NEU5GCLAC);    
    config.put("PeakfinderOptions","min_neu5aclac",MIN_NEU5ACLAC);    
    config.put("PeakfinderOptions","max_neu5aclac",MAX_NEU5ACLAC);    
    config.put("PeakfinderOptions","min_kdp",MIN_KDO);    
    config.put("PeakfinderOptions","max_kdo",MAX_KDO);    
    config.put("PeakfinderOptions","min_kdn",MIN_KDN);    
    config.put("PeakfinderOptions","max_kdn",MAX_KDN);    
    config.put("PeakfinderOptions","min_mur",MIN_MUR);    
    config.put("PeakfinderOptions","max_mur",MAX_MUR);    

    config.put("PeakfinderOptions","min_s",MIN_S);    
    config.put("PeakfinderOptions","max_s",MAX_S);    
    config.put("PeakfinderOptions","min_p",MIN_P);    
    config.put("PeakfinderOptions","max_p",MAX_P);    
    config.put("PeakfinderOptions","min_ac",MIN_AC);    
    config.put("PeakfinderOptions","max_ac",MAX_AC);    
    config.put("PeakfinderOptions","min_pyr",MIN_PYR);    
    config.put("PeakfinderOptions","max_pyr",MAX_PYR);    
    config.put("PeakfinderOptions","min_pc",MIN_PC);    
    config.put("PeakfinderOptions","max_pc",MAX_PC);    

    }

    public void retrieve(Configuration config) {

    DERIVATIZATION = config.get("PeakfinderOptions","derivatization",DERIVATIZATION);    
    REDUCING_END = config.get("PeakfinderOptions","reducing_end",REDUCING_END);    
    OTHER_REDEND_NAME = config.get("PeakfinderOptions","other_redend_name",OTHER_REDEND_NAME);    
    OTHER_REDEND_MASS = config.get("PeakfinderOptions","other_redend_mass",OTHER_REDEND_MASS);    

    MIN_PEN = config.get("PeakfinderOptions","min_pen",MIN_PEN);    
    MAX_PEN = config.get("PeakfinderOptions","max_pen",MAX_PEN);    
    MIN_HEX = config.get("PeakfinderOptions","min_hex",MIN_HEX);    
    MAX_HEX = config.get("PeakfinderOptions","max_hex",MAX_HEX);    
    MIN_HEP = config.get("PeakfinderOptions","min_hep",MIN_HEP);    
    MAX_HEP = config.get("PeakfinderOptions","max_hep",MAX_HEP);    
    MIN_HEXN = config.get("PeakfinderOptions","min_hexn",MIN_HEXN);    
    MAX_HEXN = config.get("PeakfinderOptions","max_hexn",MAX_HEXN);    
    MIN_HEXNAC = config.get("PeakfinderOptions","min_hexnac",MIN_HEXNAC);    
    MAX_HEXNAC = config.get("PeakfinderOptions","max_hexnac",MAX_HEXNAC);    
    MIN_DPEN = config.get("PeakfinderOptions","min_dpen",MIN_DPEN);    
    MAX_DPEN = config.get("PeakfinderOptions","max_dpen",MAX_DPEN);    
    MIN_DHEX = config.get("PeakfinderOptions","min_dhex",MIN_DHEX);    
    MAX_DHEX = config.get("PeakfinderOptions","max_dhex",MAX_DHEX);    
    MIN_DDHEX = config.get("PeakfinderOptions","min_ddhex",MIN_DDHEX);    
    MAX_DDHEX = config.get("PeakfinderOptions","max_ddhex",MAX_DDHEX);    
    MIN_MEHEX = config.get("PeakfinderOptions","min_mehex",MIN_MEHEX);    
    MAX_MEHEX = config.get("PeakfinderOptions","max_mehex",MAX_MEHEX);

    MIN_OR1 = config.get("PeakfinderOptions","min_or1",MIN_OR1);    
    MAX_OR1 = config.get("PeakfinderOptions","max_or1",MAX_OR1);    
    MIN_OR2 = config.get("PeakfinderOptions","min_or2",MIN_OR2);    
    MAX_OR2 = config.get("PeakfinderOptions","max_or2",MAX_OR2);    
    MIN_OR3 = config.get("PeakfinderOptions","min_or3",MIN_OR3);    
    MAX_OR3 = config.get("PeakfinderOptions","max_or3",MAX_OR3);    
    OR1_MASS = config.get("PeakfinderOptions","or1_mass",OR1_MASS);    
    OR2_MASS = config.get("PeakfinderOptions","or2_mass",OR2_MASS);    
    OR3_MASS = config.get("PeakfinderOptions","or3_mass",OR3_MASS);    

    MIN_HEXA = config.get("PeakfinderOptions","min_hexa",MIN_HEXA);    
    MAX_HEXA = config.get("PeakfinderOptions","max_hexa",MAX_HEXA);    
    MIN_DHEXA = config.get("PeakfinderOptions","min_dhexa",MIN_DHEXA);    
    MAX_DHEXA = config.get("PeakfinderOptions","max_dhexa",MAX_DHEXA);    
    MIN_NEU5GC = config.get("PeakfinderOptions","min_neu5gc",MIN_NEU5GC);    
    MAX_NEU5GC = config.get("PeakfinderOptions","max_neu5gc",MAX_NEU5GC);    
    MIN_NEU5AC = config.get("PeakfinderOptions","min_neu5ac",MIN_NEU5AC);    
    MAX_NEU5AC = config.get("PeakfinderOptions","max_neu5ac",MAX_NEU5AC);    
    MIN_NEU5GCLAC = config.get("PeakfinderOptions","min_neu5gclac",MIN_NEU5GCLAC);    
    MAX_NEU5GCLAC = config.get("PeakfinderOptions","max_neu5gclac",MAX_NEU5GCLAC);    
    MIN_NEU5ACLAC = config.get("PeakfinderOptions","min_neu5aclac",MIN_NEU5ACLAC);    
    MAX_NEU5ACLAC = config.get("PeakfinderOptions","max_neu5aclac",MAX_NEU5ACLAC);    
    MIN_KDO = config.get("PeakfinderOptions","min_kdp",MIN_KDO);    
    MAX_KDO = config.get("PeakfinderOptions","max_kdo",MAX_KDO);    
    MIN_KDN = config.get("PeakfinderOptions","min_kdn",MIN_KDN);    
    MAX_KDN = config.get("PeakfinderOptions","max_kdn",MAX_KDN);    
    MIN_MUR = config.get("PeakfinderOptions","min_mur",MIN_MUR);    
    MAX_MUR = config.get("PeakfinderOptions","max_mur",MAX_MUR);    

    MIN_S = config.get("PeakfinderOptions","min_s",MIN_S);    
    MAX_S = config.get("PeakfinderOptions","max_s",MAX_S);    
    MIN_P = config.get("PeakfinderOptions","min_p",MIN_P);    
    MAX_P = config.get("PeakfinderOptions","max_p",MAX_P);    
    MIN_AC = config.get("PeakfinderOptions","min_ac",MIN_AC);    
    MAX_AC = config.get("PeakfinderOptions","max_ac",MAX_AC);    
    MIN_PYR = config.get("PeakfinderOptions","min_pyr",MIN_PYR);    
    MAX_PYR = config.get("PeakfinderOptions","max_pyr",MAX_PYR);    
    MIN_PC = config.get("PeakfinderOptions","min_pc",MIN_PC);    
    MAX_PC = config.get("PeakfinderOptions","max_pc",MAX_PC);    
    }


}
