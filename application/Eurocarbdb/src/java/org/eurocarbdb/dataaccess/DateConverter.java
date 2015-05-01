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
*   Last commit: $Rev: 1325 $ by $Author: hirenj $ on $Date:: 2009-06-29 #$  
*/
package org.eurocarbdb.dataaccess;

import java.util.Date;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;

import ognl.TypeConverter;

import org.apache.log4j.Logger;

 /**
 * Convert dates across to a well defined internal format for EuroCarbDB.
 * The behaviour of the conversion is changed only for the input data
 * and not for the output of data, which uses the locale setting to define
 * the format for the date.
 */
public class DateConverter extends ognl.DefaultTypeConverter {

    protected static final Logger log = Logger.getLogger( DateConverter.class.getName() );

    private static final String formatString = "yyyy/MM/dd";

    public Object convertValue(Map ognlContext, Object value, Class toType) {

        if( toType == String.class )
        {

            // Return the date in the locale defined format
            return DateFormat.getDateInstance().format((Date) value);

        } else if( toType == Date.class )
        {

            DateFormat dateFormat = new SimpleDateFormat(formatString);

            // Pull out the required string value from an array if an
            // array is passed in
            if (value.getClass().isArray()) {
                value = ((Object[]) value)[0];            
            }

            try
            {                
                return dateFormat.parse((String) value);
            } catch (ParseException e) {
                // Warn that we can't understand the date, and return null
                // since we don't throw exceptions here and the defined type
                // that we need to return seems to be poorly defined in doco
                log.warn("Could not parse date",e);
                return null;
            }
        }
        return null;
    }
}