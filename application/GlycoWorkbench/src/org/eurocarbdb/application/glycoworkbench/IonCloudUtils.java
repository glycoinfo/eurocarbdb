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
 *   Last commit: $Rev: 1930 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-07-29 #$  
 */

/**
 @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
 */

package org.eurocarbdb.application.glycoworkbench;

import org.eurocarbdb.application.glycanbuilder.*;

import java.util.*;

public class IonCloudUtils {

	static public boolean isRealistic(FragmentEntry fe) {
		if (fe == null)
			return false;

		int no_exchanges = -fe.getNeutralExchanges().get(MassOptions.ION_H);
		if (fe.fragment.countCharges() < no_exchanges)
			return false;

		return (fe.getCharges().and(fe.getNeutralExchanges()).isRealistic());
	}

	static public Vector<IonCloud> getPossibleIonClouds(
			AnnotationOptions ann_opt) {
		Vector<IonCloud> ret = new Vector<IonCloud>();

		IonCloud avail_ions = getAvailableIons(ann_opt);
		if (avail_ions.size() > 0) {
			for (int i = 1; i <= ann_opt.MAX_NO_CHARGES; i++)
				enumerateCombinations(ret, new IonCloud(), avail_ions,
						avail_ions.getIons(), 0, i, ann_opt.NEGATIVE_MODE);
		}
		return ret;
	}

	static public Vector<IonCloud> getPossibleNeutralExchanges(int max_no_ex,
			AnnotationOptions ann_opt) {
		Vector<IonCloud> ret = new Vector<IonCloud>();

		IonCloud avail_ions = getExchangeableIons(ann_opt);

		if (avail_ions.size() > 0) {
			max_no_ex = Math.min(max_no_ex, avail_ions.size());
			for (int i = 0; i <= max_no_ex; i++)
				enumerateCombinations(ret, new IonCloud().and(
						MassOptions.ION_H, -i), avail_ions, avail_ions
						.getIons(), 0, i, false);
		}

		return ret;
	}

	static private IonCloud getAvailableIons(AnnotationOptions ann_opt) {
		IonCloud ret = new IonCloud();

		ret.add(MassOptions.ION_H, ann_opt.MAX_NO_H_IONS);
		if (!ann_opt.NEGATIVE_MODE) {
			ret.add(MassOptions.ION_NA, ann_opt.MAX_NO_NA_IONS);
			ret.add(MassOptions.ION_LI, ann_opt.MAX_NO_LI_IONS);
			ret.add(MassOptions.ION_K, ann_opt.MAX_NO_K_IONS);
		}

		return ret;
	}

	static private IonCloud getExchangeableIons(AnnotationOptions ann_opt) {
		IonCloud ret = new IonCloud();

		ret.add(MassOptions.ION_NA, ann_opt.MAX_EX_NA_IONS);
		ret.add(MassOptions.ION_LI, ann_opt.MAX_EX_LI_IONS);
		ret.add(MassOptions.ION_K, ann_opt.MAX_EX_K_IONS);

		return ret;
	}

	static public void enumerateCombinations(Vector<IonCloud> buffer,
			IonCloud combination, IonCloud limits, Vector<String> collection,
			int ind, int remaining, boolean negative) {

		if (remaining == 0) {
			buffer.add(combination);
			return;
		}
		if (ind == collection.size())
			return;

		// add
		String ion = collection.elementAt(ind);
		/*
		 * if( ind==collection.size()-1 ) { // add all if(
		 * remaining<=limits.get(ion) ) buffer.add(combination.and(ion,negative
		 * ?-remaining :remaining)); return; }
		 */

		int max = Math.min(remaining, limits.get(ion));
		for (int i = 0; i <= max; i++)
			enumerateCombinations(buffer, combination.and(ion, negative ? -i
					: i), limits, collection, ind + 1, remaining - i, negative);
	}

}