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

package org.eurocarbdb.application.glycoworkbench;

import org.eurocarbdb.application.glycanbuilder.*;

public class AnnotationOptions {

    // annotation options   

    public boolean NEGATIVE_MODE = false;    
    public int MAX_NO_H_IONS = 1;
    public int MAX_NO_NA_IONS = 0;
    public int MAX_NO_LI_IONS = 0;
    public int MAX_NO_K_IONS = 0;
    public int MAX_NO_CHARGES = 1; 
    
    public boolean COMPUTE_EXCHANGES = false;
    public int MAX_EX_NA_IONS = 999;
    public int MAX_EX_LI_IONS = 999;
    public int MAX_EX_K_IONS  = 999;

    public boolean DERIVE_FROM_PARENT = true;      

    public double  MASS_ACCURACY       = 1.;
    public static final String MASS_ACCURACY_DA  = "Da";
    public static final String MASS_ACCURACY_PPM = "ppm";
    public String  MASS_ACCURACY_UNIT  = MASS_ACCURACY_DA;

    public boolean LOSS_H2O = false;
    public boolean LOSS_NH3 = false;
    public boolean LOSS_CO2 = false;

    public boolean GAIN_H2O = false;
    public boolean GAIN_NH3 = false;
    public boolean GAIN_CO2 = false;

    // pojo
    
    public boolean getPositiveMode() {
    return !NEGATIVE_MODE;
    }

    public void setPositiveMode(boolean f) {
    NEGATIVE_MODE = !f;    
    }
    

    public boolean getNegativeMode() {
    return NEGATIVE_MODE;
    }

    public void setNegativeMode(boolean f) {
    NEGATIVE_MODE = f;    
    }
    
    public int getMaxNoHIons() {
    return MAX_NO_H_IONS;
    }

    public void setMaxNoHIons(int i) { 
    MAX_NO_H_IONS = i;
    }

    public int getMaxNoNaIons() {
    return MAX_NO_NA_IONS;
    }

    public void setMaxNoNaIons(int i) { 
    MAX_NO_NA_IONS = i;
    }

    public int getMaxNoLiIons() {
    return MAX_NO_LI_IONS;
    }

    public void setMaxNoLiIons(int i) { 
    MAX_NO_LI_IONS = i;
    }

    public int getMaxNoKIons() {
    return MAX_NO_K_IONS;
    }

    public void setMaxNoKIons(int i) { 
    MAX_NO_K_IONS = i;
    }

    public int getMaxNoCharges() {
    return MAX_NO_CHARGES;
    }

    public void setMaxNoCharges(int i) { 
    MAX_NO_CHARGES = i;
    }
    
    public boolean getComputeExchanges() {
    return COMPUTE_EXCHANGES;
    }

    public void setComputeExchanges(boolean f) {
    COMPUTE_EXCHANGES = f;    
    }

    public int getMaxExNaIons() {
    return MAX_EX_NA_IONS;
    }

    public void setMaxExNaIons(int i) { 
    MAX_EX_NA_IONS = i;
    }

    public int getMaxExLiIons() {
    return MAX_EX_LI_IONS;
    }

    public void setMaxExLiIons(int i) { 
    MAX_EX_LI_IONS = i;
    }

    public int getMaxExKIons() {
    return MAX_EX_K_IONS;
    }

    public void setMaxExKIons(int i) { 
    MAX_EX_K_IONS = i;
    }

    public boolean getDeriveFromParent() {
    return DERIVE_FROM_PARENT;
    }

    public void setDeriveFromParent(boolean f) {
    DERIVE_FROM_PARENT = f;    
    }
    
    public double getMassAccuracy() {
    return MASS_ACCURACY;
    }

    public void setMassAccuracy(double v) {
    MASS_ACCURACY = v;
    }

    public String getMassAccuracyUnit() {
    return MASS_ACCURACY_UNIT;
    }

    public void setMassAccuracyUnit(String s) {
    MASS_ACCURACY_UNIT = s;
    }
    
    // methods

    public AnnotationOptions derive(Glycan parent) {
    AnnotationOptions ret = new AnnotationOptions();
    
    IonCloud charges = parent.getMassOptions().ION_CLOUD;
    ret.NEGATIVE_MODE = charges.isNegative();       
    ret.MAX_NO_H_IONS = Math.abs(charges.get(MassOptions.ION_H));
    ret.MAX_NO_NA_IONS = Math.abs(charges.get(MassOptions.ION_NA));
    ret.MAX_NO_LI_IONS = Math.abs(charges.get(MassOptions.ION_LI));
    ret.MAX_NO_K_IONS = Math.abs(charges.get(MassOptions.ION_K));
    ret.MAX_NO_CHARGES = charges.getNoCharges();

    IonCloud exchanges = parent.getMassOptions().NEUTRAL_EXCHANGES;
    ret.MAX_EX_NA_IONS = exchanges.get(MassOptions.ION_NA);
    ret.MAX_EX_LI_IONS = exchanges.get(MassOptions.ION_LI);
    ret.MAX_EX_K_IONS = exchanges.get(MassOptions.ION_K);
    ret.COMPUTE_EXCHANGES = (ret.MAX_EX_NA_IONS>0 || ret.MAX_EX_LI_IONS>0 || ret.MAX_EX_K_IONS>0 );
    
    ret.DERIVE_FROM_PARENT = false;    
    
    ret.LOSS_H2O = this.LOSS_H2O;
    ret.LOSS_NH3 = this.LOSS_NH3;
    ret.LOSS_CO2 = this.LOSS_CO2;

    ret.GAIN_H2O = this.GAIN_H2O;
    ret.GAIN_NH3 = this.GAIN_NH3;
    ret.GAIN_CO2 = this.GAIN_CO2;

    ret.MASS_ACCURACY = this.MASS_ACCURACY;
    ret.MASS_ACCURACY_UNIT = this.MASS_ACCURACY_UNIT;

    return ret;
    }

    public AnnotationOptions clone() {
    AnnotationOptions ret = new AnnotationOptions();

    ret.NEGATIVE_MODE = this.NEGATIVE_MODE;
    ret.MAX_NO_H_IONS = this.MAX_NO_H_IONS;
    ret.MAX_NO_NA_IONS = this.MAX_NO_NA_IONS;
    ret.MAX_NO_LI_IONS = this.MAX_NO_LI_IONS;
    ret.MAX_NO_K_IONS = this.MAX_NO_K_IONS;
    ret.MAX_NO_CHARGES = this.MAX_NO_CHARGES;
    
    ret.COMPUTE_EXCHANGES = this.COMPUTE_EXCHANGES;
    ret.MAX_EX_NA_IONS = this.MAX_EX_NA_IONS;
    ret.MAX_EX_LI_IONS = this.MAX_EX_LI_IONS;
    ret.MAX_EX_K_IONS  = this.MAX_EX_K_IONS;

    ret.DERIVE_FROM_PARENT = this.DERIVE_FROM_PARENT;    
    
    ret.LOSS_H2O = this.LOSS_H2O;
    ret.LOSS_NH3 = this.LOSS_NH3;
    ret.LOSS_CO2 = this.LOSS_CO2;

    ret.GAIN_H2O = this.GAIN_H2O;
    ret.GAIN_NH3 = this.GAIN_NH3;
    ret.GAIN_CO2 = this.GAIN_CO2;

    ret.MASS_ACCURACY = this.MASS_ACCURACY;
    ret.MASS_ACCURACY_UNIT = this.MASS_ACCURACY_UNIT;

    return ret;
    }
    
    public void store(Configuration config) {

    config.put("AnnotationOptions","mass_accuracy",MASS_ACCURACY);    
    config.put("AnnotationOptions","mass_accuracy_unit",MASS_ACCURACY_UNIT);

    config.put("AnnotationOptions","negative_mode",NEGATIVE_MODE);
    config.put("AnnotationOptions","max_no_h_ions",MAX_NO_H_IONS);
    config.put("AnnotationOptions","max_no_na_ions",MAX_NO_NA_IONS);
    config.put("AnnotationOptions","max_no_li_ions",MAX_NO_LI_IONS);
    config.put("AnnotationOptions","max_no_k_ions",MAX_NO_K_IONS);
    config.put("AnnotationOptions","max_no_charges",MAX_NO_CHARGES);

    config.put("AnnotationOptions","compute_exchanges",COMPUTE_EXCHANGES);
    config.put("AnnotationOptions","max_ex_na_ions",MAX_EX_NA_IONS);
    config.put("AnnotationOptions","max_ex_li_ions",MAX_EX_LI_IONS);
    config.put("AnnotationOptions","max_ex_k_ions",MAX_EX_K_IONS);
    
    config.put("AnnotationOptions","derive_from_parent", DERIVE_FROM_PARENT);

    config.put("AnnotationOptions","loss_h2o",LOSS_H2O);
    config.put("AnnotationOptions","loss_nh3",LOSS_NH3);
    config.put("AnnotationOptions","loss_co2",LOSS_CO2);

    config.put("AnnotationOptions","gain_h2o",GAIN_H2O);
    config.put("AnnotationOptions","gain_nh3",GAIN_NH3);
    config.put("AnnotationOptions","gain_co2",GAIN_CO2);
    }

    public void retrieve(Configuration config) {
    MASS_ACCURACY      = config.get("AnnotationOptions","mass_accuracy",MASS_ACCURACY); 
    MASS_ACCURACY_UNIT = config.get("AnnotationOptions","mass_accuracy_unit",MASS_ACCURACY_UNIT); 

    NEGATIVE_MODE  = config.get("AnnotationOptions","negative_mode",NEGATIVE_MODE);
    MAX_NO_H_IONS = config.get("AnnotationOptions","max_no_h_ions",MAX_NO_H_IONS);
    MAX_NO_NA_IONS = config.get("AnnotationOptions","max_no_na_ions",MAX_NO_NA_IONS);
    MAX_NO_LI_IONS = config.get("AnnotationOptions","max_no_li_ions",MAX_NO_LI_IONS);
    MAX_NO_K_IONS = config.get("AnnotationOptions","max_no_k_ions",MAX_NO_K_IONS);
    MAX_NO_CHARGES = config.get("AnnotationOptions","max_no_charges",MAX_NO_CHARGES);

    COMPUTE_EXCHANGES = config.get("AnnotationOptions","compute_exchanges",COMPUTE_EXCHANGES);
    MAX_EX_NA_IONS = config.get("AnnotationOptions","max_ex_na_ions",MAX_EX_NA_IONS);
    MAX_EX_LI_IONS = config.get("AnnotationOptions","max_ex_li_ions",MAX_EX_LI_IONS);
    MAX_EX_K_IONS = config.get("AnnotationOptions","max_ex_k_ions",MAX_EX_K_IONS);

    DERIVE_FROM_PARENT = config.get("AnnotationOptions","derive_from_parent", DERIVE_FROM_PARENT);

    LOSS_H2O = config.get("AnnotationOptions","loss_h2o",LOSS_H2O);
    LOSS_NH3 = config.get("AnnotationOptions","loss_nh3",LOSS_NH3);
    LOSS_CO2 = config.get("AnnotationOptions","loss_co2",LOSS_CO2);
    
    GAIN_H2O = config.get("AnnotationOptions","gain_h2o",GAIN_H2O);
    GAIN_NH3 = config.get("AnnotationOptions","gain_nh3",GAIN_NH3);
    GAIN_CO2 = config.get("AnnotationOptions","gain_co2",GAIN_CO2);
    }
    
}