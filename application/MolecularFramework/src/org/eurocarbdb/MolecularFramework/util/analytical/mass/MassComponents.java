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
package org.eurocarbdb.MolecularFramework.util.analytical.mass;

import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
import org.eurocarbdb.MolecularFramework.sugar.SubstituentType;
import org.eurocarbdb.MolecularFramework.sugar.Superclass;

/**
* @author rene
*
*/
public class MassComponents
{
    public double getSuperclassMass(Superclass a_objSuper, boolean a_bMonoisotopic) throws GlycoMassException
    {
        if ( a_objSuper == Superclass.SUG )
        {
            throw new GlycoMassException("Mass calculation for superclass SUG not possible." );
        }
        double t_dResult = 0;
        if ( a_bMonoisotopic )
        {
            t_dResult = 30.0105646861;
        }
        else
        {
            t_dResult = 30.0260223327743;
        }
        return (t_dResult * a_objSuper.getCAtomCount());
    }

    public double getModificationMass( ModificationType a_objModi , boolean a_bMonoisotopic , int t_iPositionOne) throws GlycoMassException
    {
        double t_dResult = 0;
        if ( a_objModi == ModificationType.ACID )
        {
            if ( t_iPositionOne == 1 )
            {
                if ( a_bMonoisotopic )
                {
                    t_dResult = 15.99491462210000;
                }
                else
                {
                    t_dResult = 15.99940492835830;
                }
            }
            else
            {
                if ( a_bMonoisotopic )
                {
                    t_dResult = 13.9792645581;
                }
                else
                {
                    t_dResult = 13.9835234207067;
                }
            }
        }
        else if ( a_objModi == ModificationType.ALDI )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 2.01565006400000;
            }
            else
            {
                t_dResult = 2.01588150765158;
            }
        }
        else if ( a_objModi == ModificationType.DEOXY )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = -15.99491462210000;
            }
            else
            {
                t_dResult = -15.99940492835830;
            }
        }
        else if ( a_objModi == ModificationType.DOUBLEBOND )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = -2.01565006400000;
            }
            else
            {
                t_dResult = -2.01588150765158;
            }
        }
        else if ( a_objModi == ModificationType.KETO )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = -2.01565006400000;
            }
            else
            {
                t_dResult = -2.01588150765158;
            }
        }
        else
        {
            throw new GlycoMassException("Mass calculation for modification " + a_objModi.getName() + " is not supported." );
        }        
        return t_dResult;
    }

    public double getSubstitutionsMass( SubstituentType a_objSubst , boolean a_bMonoisotopic ) throws GlycoMassException
    {
        double t_dResult = 0;
        if ( a_objSubst == SubstituentType.ACETYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 43.01838971810000;
            }
            else
            {
                t_dResult = 43.04469898336460;
            }
        }
        else if ( a_objSubst == SubstituentType.ACYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 43.01838971810000;
            }
            else
            {
                t_dResult = 43.04469898336460;
            }
        }
        else if ( a_objSubst == SubstituentType.AMIDINO )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 43.02962310600000;
            }
            else
            {
                t_dResult = 43.04804434505730;
            }
        }
        else if ( a_objSubst == SubstituentType.AMINO )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 16.01872406900000;
            }
            else
            {
                t_dResult = 16.02262460105930;
            }
        }
        else if ( a_objSubst == SubstituentType.ANHYDRO )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 0;
            }
            else
            {
                t_dResult = 0;
            }
        }
        else if ( a_objSubst == SubstituentType.CHLORO )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 34.96885271000000;
            }
            else
            {
                t_dResult = 35.45253819335800;
            }
        }
        else if ( a_objSubst == SubstituentType.ETHANOLAMINE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 44.05002419700000;
            }
            else
            {
                t_dResult = 44.07585940989140;
            }
        }
        else if ( a_objSubst == SubstituentType.ETHYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 29.03912516000000;
            }
            else
            {
                t_dResult = 29.06117556265790;
            }
        }
        else if ( a_objSubst == SubstituentType.FLOURO )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 18.99840320000000;
            }
            else
            {
                t_dResult = 18.99840320000000;
            }
        }
        else if ( a_objSubst == SubstituentType.FORMYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 29.00273965410000;
            }
            else
            {
                t_dResult = 29.01808157894850;
            }
        }
        else if ( a_objSubst == SubstituentType.GLYCOLYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 59.01330434020000;
            }
            else
            {
                t_dResult = 59.04410391172290;
            }
        }
        else if ( a_objSubst == SubstituentType.HYDROXYMETHYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 31.01838971810000;
            }
            else
            {
                t_dResult = 31.03396308660010;
            }
        }
        else if ( a_objSubst == SubstituentType.IMINO )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 15.01089903700000;
            }
            else
            {
                t_dResult = 15.01468384723350;
            }
        }
        else if ( a_objSubst == SubstituentType.LACTONE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 0;
            }
            else
            {
                t_dResult = 0;
            }
        }
        else if ( a_objSubst == SubstituentType.METHYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 15.02347509600000;
            }
            else
            {
                t_dResult = 15.03455815824180;
            }
        }
        else if ( a_objSubst == SubstituentType.N_ACETYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 58.02928875510000;
            }
            else
            {
                t_dResult = 58.05938283059810;
            }
        }
        else if ( a_objSubst == SubstituentType.N_ALANINE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 73.04018779210000;
            }
            else
            {
                t_dResult = 73.07406667783160;
            }
        }
        else if ( a_objSubst == SubstituentType.N_AMIDINO )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 58.04052214300000;
            }
            else
            {
                t_dResult = 58.06272819229080;
            }
        }
        else if ( a_objSubst == SubstituentType.N_DIMETHYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 44.05002419700000;
            }
            else
            {
                t_dResult = 44.07585940989140;
            }
        }
        else if ( a_objSubst == SubstituentType.N_FORMYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 44.01363869110000;
            }
            else
            {
                t_dResult = 44.03276542618210;
            }
        }
        else if ( a_objSubst == SubstituentType.N_GLYCOLYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 74.02420337720000;
            }
            else
            {
                t_dResult = 74.05878775895640;
            }
        }
        else if ( a_objSubst == SubstituentType.N_METHYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 47.00506163800000;
            }
            else
            {
                t_dResult = 47.01626042206760;
            }
        }
        else if ( a_objSubst == SubstituentType.N_METHYLCARBAMOYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 58.02928875510000;
            }
            else
            {
                t_dResult = 58.05938283059810;
            }
        }
        else if ( a_objSubst == SubstituentType.N_SUCCINATE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 116.03476806330000;
            }
            else
            {
                t_dResult = 116.09554598849500;
            }
        }
        else if ( a_objSubst == SubstituentType.N_SULFATE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 95.97553862530000;
            }
            else
            {
                t_dResult = 96.08692407993420;
            }
        }
        else if ( a_objSubst == SubstituentType.N_TRIFLOUROACETYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 110.99319822710000;
            }
            else
            {
                t_dResult = 111.02282941529500;
            }
        }
        else if ( a_objSubst == SubstituentType.NITRATE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 45.99290324920000;
            }
            else
            {
                t_dResult = 46.00555295012430;
            }
        }
        else if ( a_objSubst == SubstituentType.PHOSPHATE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 80.97415544030000;
            }
            else
            {
                t_dResult = 80.98785780272650;
            }
        }
        else if ( a_objSubst == SubstituentType.PHOSPHO_CHOLINE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 166.06330479730000;
            }
            else
            {
                t_dResult = 166.13562867204000;
            }
        }
        else if ( a_objSubst == SubstituentType.PHOSPHO_ETHANOLAMINE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 123.00852957330000;
            }
            else
            {
                t_dResult = 123.04783570496600;
            }
        }
        else if ( a_objSubst == SubstituentType.PYROPHOSPHATE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 160.94048584860000;
            }
            else
            {
                t_dResult = 160.96777485162700;
            }
        }
        else if ( a_objSubst == SubstituentType.PYRUVATE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 71.01330434020000;
            }
            else
            {
                t_dResult = 71.05483980848730;
            }
        }
        else if ( a_objSubst == SubstituentType.R_CARBOXYETHYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 73.02895440420000;
            }
            else
            {
                t_dResult = 73.07072131613890;
            }
        }
        else if ( a_objSubst == SubstituentType.R_CARBOXYMETHYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 59.01330434020000;
            }
            else
            {
                t_dResult = 59.04410391172290;
            }
        }
        else if ( a_objSubst == SubstituentType.R_PYRUVATE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 72.02112937220000;
            }
            else
            {
                t_dResult = 72.06278056231310;
            }
        }
        else if ( a_objSubst == SubstituentType.S_CARBOXYETHYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 73.02895440420000;
            }
            else
            {
                t_dResult = 73.07072131613890;
            }
        }
        else if ( a_objSubst == SubstituentType.S_CARBOXYMETHYL )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 59.01330434020000;
            }
            else
            {
                t_dResult = 59.04410391172290;
            }
        }
        else if ( a_objSubst == SubstituentType.S_PYRUVATE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 72.02112937220000;
            }
            else
            {
                t_dResult = 72.06278056231310;
            }
        }
        else if ( a_objSubst == SubstituentType.SULFATE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 80.96463958830000;
            }
            else
            {
                t_dResult = 81.07224023270070;
            }
        }
        else if ( a_objSubst == SubstituentType.THIO )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 32.97989572200000;
            }
            else
            {
                t_dResult = 33.07402544762580;
            }
        }
        else if ( a_objSubst == SubstituentType.TRIPHOSPHATE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 240.90681625690000;
            }
            else
            {
                t_dResult = 240.94769190052800;
            }
        }
        else if ( a_objSubst == SubstituentType.X_PYRUVATE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = 72.02112937220000;
            }
            else
            {
                t_dResult = 72.06278056231310;
            }
        }
        else
        {
            throw new GlycoMassException("Mass calculation for substituent " + a_objSubst.getName() + " is not supported." );
        }        
        return t_dResult;
        // BROMO ; DIPHOSPHO_ETHANOLAMINE ; EPOXY ; LACTONE ; SUCCINATE
    }

    /**
     * @param linkageType
     * @param monoisotopic
     * @return
     * @throws GlycoMassException 
     */
    public double getLinkageTypeMass(LinkageType a_objLinkageType, boolean a_bMonoisotopic) throws GlycoMassException
    {
        double t_dResult = 0;
        if ( a_objLinkageType == LinkageType.DEOXY )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = -17.0027396541;
            }
            else
            {
                t_dResult = -17.00734568218410;
            }
        }
        else if ( a_objLinkageType == LinkageType.H_AT_OH || a_objLinkageType == LinkageType.H_LOSE )
        {
            if ( a_bMonoisotopic )
            {
                t_dResult = -1.007825032;
            }
            else
            {
                t_dResult = -1.00794075382579;
            }

        }
        else
        {
            throw new GlycoMassException("Mass calculation for Linkagetype " + a_objLinkageType + " is not supported." );
        }
        return t_dResult;
    }
}
