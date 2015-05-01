<#include "../header.ftl">
<h1>Substituent Notation</h1>

<p class="longtext">Monosaccharides often carry substituents, which form an additional level of complexity in monosaccharide notation.
Substituents are treated in various ways in the different notations.
In <a href="notation.action?topic=schemes#glycoct">GlycoCT</a>, for instance, all substituents are handled as separate residues, while e.g. the <a href="notation.action?topic=schemes#glycosciences">glycosciences.de</a> notation includes most substituents in the monosaccharide name.
Sometimes, especially in <a href="notation.action?topic=schemes#bcsdb">BCSDB</a> residue names, the substituents are split, i.e. part of the substituent is included in the monosaccharide name, while another part of the same substituent is regarded as a separate residue.
For example, of an "n-acetyl" substituent, the amino part is represented by an "N" in the BCSDB monosaccharide name, while the acetyl part is added as a separate "Ac" residue.
</p>
<p class="longtext">In some notations, more than one name is used for the same substituent. In some cases this is necessary to distinguish between different linkage types (see below), but often this introduces ambivalence.
To be able to read the various names but on at the same time to generate unique names, MonoSaccharideDB contains manually curated alias lists for substituent names. These lists contain only one primary alias per notation scheme and linkage type, which is used by the encoder routines to generate the residue names.
In addition, various secondary alias names can be present, which are used by the importer routines when parsing a residue name into the internal representation.
</p>

<h3 id="linktype">Linkage Type</h3>

<p class="longtext">Substituents can be linked to the monosaccharide basetype by a number of linkage types, which are listed in the subsequent table:
</p>

<table class="contenttable">
<tr><td class="tablehead">Name</td><td class="tablehead">Description</td></tr>
<tr><td class="tablecontent">H_AT_OH</td><td class="tablecontent">A standard O-linked substituent, i.e. the substituent replaces the hydrogen of an OH group.</td></tr>
<tr><td class="tablecontent">DEOXY</td><td class="tablecontent">The substituent is linked directly to the basetype backbone by replacing the OH group.</td></tr>
<tr><td class="tablecontent">H_LOSE</td><td class="tablecontent">The substituent is linked directly to the basetype backbone by replacing the hydrogen atom.</td></tr>
<tr><td class="tablecontent">R_CONFIG</td><td class="tablecontent">The substituent is linked directly to the basetype backbone by replacing a hydrogen atom at a terminal position, which would be non-chiral without the substituent, resulting in an R-configuration of the carbon.</td></tr>
<tr><td class="tablecontent">S_CONFIG</td><td class="tablecontent">Same as R_CONFIG, but resulting in an S-Configuration of the carbon.</td></tr>
</table>
<br>
<p class="longtext">Apart from <a href="notation.action?topic=schemes#glycoct">GlycoCT</a> and the <a href="notation.action?topic=schemes#msdb">MonoSaccharideDB</a> internal notation, the linkage type is not stated explicitly. Instead, it is implied in the substituent's name.
In <a href="notation.action?topic=schemes#carbbank">CarbBank</a> notation, for example, a methyl residue is called "Me" or "OMe" if it is linked with an H_AT_OH linkage, while "CMe" is used to denote a methyl that is linked via an H_LOSE linkage.
Therefore, the synonyms list on the <span class="italic">notation</span> tab of the MonoSaccharideDB substituent entry pages lists the linkage type that is implied in the alias names.
</p>
<br>
<#include "../footer.ftl">