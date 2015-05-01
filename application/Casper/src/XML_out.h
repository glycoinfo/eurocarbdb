/* 
This file is part of CASPER.

    CASPER is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CASPER is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with CASPER.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2010 Roland Stenutz, Magnus Lundborg, Göran Widmalm
*/

/* Filename = XML_out.h */
/* Prefix = XML_ */
/* Output of casper-spectra/structures in XML */

/* Format:
<casper>
<code>
casper structure
</code>

<spectrum nucleus="13C">
<peak residue="label" atom="Cn">cs</peak>


</spectrum>

<spectrum nucleus="1H">

<spectrum nucleus="1H 1H">

<spectrum nucleus="13C 1H">
*/

#ifndef _XML_OUT_H
#define _XML_OUT_H

void XML_code(struct BU_Struct *structure);
void XML_residues(struct BU_Struct *structure);
void XML_linkages(struct BU_Struct *structure);
void XML_spec_c(struct BU_Struct *structure);
void XML_spec_h(struct BU_Struct *structure);
int XML_structure();

#endif
