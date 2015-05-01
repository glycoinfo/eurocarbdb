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
package org.eurocarbdb.applications.ms.glycopeakfinder.util;

/**
* @author rene
*
*/
public class ErrorTextEnglish
{
    public static final String POS_NEG_IONS         = "Positive and negative ions are not permitted within the same spectrum.";
    public static final String MAX_PEAK             = "The maximum number of quantifiable peaks is at present limited to %d peaks. You have entered %d peaks in the mass-list.";
    public static final String ACCURACY_PPM         = "The upper limit for 'Accuracy of Mass' is automatically adjusted to %f ppm. You have entered %f ppm.";
    public static final String ACCURACY_U           = "The upper limit for 'Accuracy of Mass' is automatically adjusted to %f u. You have entered %f u.";
    public static final String ACCURACY_NEGATIV     = "The 'Accuracy of Mass' has to be a positive number.";
    public static final String WRONG_RESIDUE_FOR_DB = "GLYCOSCIENCES.de does not support a composition search with %s as residue.";
    public static final String PEAKLIST_SYMBOL      = "One line in the peak-list contains '%s' instead of a positive value.";
    public static final String PEAKLIST_SYMBOL_2    = "One line in the peak-list contains '%s' instead of two positive values.";
    public static final String CHARGE_NO_SELECT     = "At least one charge state must be selected.";
    public static final String ION_NO_SELECT        = "At least one ion must be selected.";
    public static final String ION_OTHER_MASS       = "The mass value for 'other ion' has to be positive mass.";
    public static final String RESIDUE_OTHER_MASS   = "The mass value for 'other residue' has to be positive.";
    public static final String RESIDUE_MIN_MAX      = "The minimum occurrence of residue '%s' has to be smaller than or equal to the maximum occurrence.";
    public static final String RESIDUE_NUMBER       = "Minimum and maximum occurrence of residues '%s' have to be positive values.";
    public static final String MODI_OTHER_MASS      = "The mass value for 'other modification' has to be positive.";
    public static final String FRAGMENT_MASS        = "The mass value '%.3f u' with charge-state '%d' exceeds the maximum limit of %d.";
    public static final String NO_SELECT            = "No compostion was selected for the database search.";
    public static final String MAX_MASS              = "The upper limit for mass values is %.3f u. The specra contains mass values up to %.3f u.";
    public static final String EMPTY_MASS              = "The mass list is empty.";
    public static final String DB_ERROR              = "There was a critical error in the database. Please contact administrator.";
    public static final String LIPID_SPHINGO        = "For calculation of a lipid at the reducing end, please select a sphingosine moiety.";
    public static final String LIPID_FATTY_ACID        = "For calculation of a lipid at the reducing end, please select a fatty acid moiety.";
    public static final String PEPTID_EMPTY            = "No amino-acid sequence was entered.";
    public static final String NO_PER_ALLOWED       = "For caculations with peptides or lipids, no persubstitution is allowed.";
    public static final String NO_FRAGMENTTYPE      = "No fragment-type selected.";
    public static final String NO_FRAGMENT_NUMBER   = "No number of fragmentations selected.";
    public static final String NO_CHARGE            = "No charge-state selected.";
    public static final String NO_EXCHANGE          = "No ion-exchange state selected.";
    public static final String NO_RESIDUE           = "No residue selected.";
    public static final String MASS_SHIFT           = "Incorrect value for mass shift.";
    public static final String MODI_OTHER_NAME        = "The name for the other modification is empty.";
    public static final String ION_OTHER_NAME        = "The name for the other ion is empty.";
    public static final String MOLECULE_GAIN        = "The gain value for '%s' has to be zero or a positiv number.";
    public static final String MOLECULE_LOSS        = "The loss value for '%s' has to be zero or a positiv number.";
    public static final String NO_EXCHANGE_ION        = "At least one ion must be selected for ion-exchange.";
    public static final String MOLECULES_GAIN        = "Gain of the molecule '%s' have to be a positive value.";
    public static final String MOLECULES_LOSS_MIN    = "Loss of the molecule '%s' have to be a positive value.";
    public static final String MOLECULES_LOSS_MAX    = "The upper-limit for loss of the molecule '%s' is maximum %d. You have entered %d.";
    public static final String MOLECULES_OTHER_NAME    = "The name for the other molecule is empty.";
    public static final String MOLECULES_OTHER_MASS    = "The mass value for 'other molecule' has to be a positive mass.";
    public static final String NO_OTHER_EXCHANGE_ION_NAME    = "The name for the other ion-exchange is empty.";
    public static final String NO_OTHER_EXCHANGE_ION_MASS    = "The mass value for 'other ion-exchange' has to be a positive mass.";
    public static final String CALCULATION          = "Critical error during calculation. Please contact admin.";
    public static final String MOTIF                 = "Unable to load motif.";
    public static final String MAX_CHARGE             = "The upper limit for charge is 4. The specra contains larger charge values.";
    public static final String PRECURSOR_PEAKLIST   = "The precursor must be part of the peaklist.";
    public static final String MISSING_PRECUSROR     = "For MS<sup>2</sup> the precursor must be set.";
    public static final String ONLY_ONE_FOR_DB         = "For database search only one selected composition is allowed.";
    public static final String UNKNOWN_DB             = "Unknown database name : ";
    public static final String NO_DB                 = "No database selected for search.";
    public static final String CRITICAL_COMPOSITION_ERROR     = "Critical error : Unable to perform composition search.";
    public static final String INVALIDE_PAGE         = "Error while database result paging.";
    public static final String MAX_ANNOTATION_RANGE = "Maximum number of annotations per peaks must be between 1 and %d.";
    public static final String MAX_ANNOTATION         = "Maximum number of annotations per peak must be a number.";
}
