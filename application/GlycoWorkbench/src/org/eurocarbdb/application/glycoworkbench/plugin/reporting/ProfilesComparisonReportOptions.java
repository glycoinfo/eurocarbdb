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

package org.eurocarbdb.application.glycoworkbench.plugin.reporting;

import org.eurocarbdb.application.glycoworkbench.plugin.*;
import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

public class ProfilesComparisonReportOptions {

    public static int NONE = 0;

    public static final int BASEPEAK = 1;
    public static final int SUM = 2;
    public static final int AVERAGE = 3;
    public static final int STANDARDIZATION = 4;
    public static final int NORMALIZATION_DEFAULT = AVERAGE;
    public int NORMALIZATION = NORMALIZATION_DEFAULT;
    
    public static final int MONOSACCHARIDES = 1;
    public static final int DISACCHARIDES = 2;
    public static final int CORES = 3;
    public static final int TERMINALS = 4;
    public static final int DECONVOLUTION_DEFAULT = NONE;
    public int DECONVOLUTION = DECONVOLUTION_DEFAULT;
  
    public static final int TABLE = 1;
    public static final int BARS = 2;
    public static final int ERRORBARS = 3;
    public static final int DISTRIBUTIONS = 4;
    public static final int REPRESENTATION_DEFAULT = TABLE;
    public int REPRESENTATION = REPRESENTATION_DEFAULT;
   
    public boolean NORMALIZEBYROW = true;

    public void store(Configuration config) {

        config.put("ProfilesComparisonReportOptions","normalization",NORMALIZATION);
        config.put("ProfilesComparisonReportOptions","deconvolution",DECONVOLUTION);
        config.put("ProfilesComparisonReportOptions","representation",REPRESENTATION);
        config.put("ProfilesComparisonReportOptions","normalizebyrow",NORMALIZEBYROW);
    }

    public void retrieve(Configuration config) {
        
    NORMALIZATION = config.get("ProfilesComparisonReportOptions","normalization",NORMALIZATION);
    DECONVOLUTION = config.get("ProfilesComparisonReportOptions","deconvolution",DECONVOLUTION);
    REPRESENTATION = config.get("ProfilesComparisonReportOptions","representation",REPRESENTATION);
        NORMALIZEBYROW = config.get("ProfilesComparisonReportOptions","normalizebyrow",NORMALIZEBYROW);
    }

}