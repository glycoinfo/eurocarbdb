<#include "../header.ftl">
<h1>Notation Schemes</h1>

<p class="longtext">
To be able to efficiently handle various carbohydrate notations, MonoSaccharideDB has implemented a "Notation Scheme" concept.
Each alias name, regardless if it denotes a monosaccharide, a substituent, an aglycon or any other component, is assigned to a notation scheme.
For each component, there is exactly one primary alias name defined for each scheme (or, strictly speaking, none or one primary alias, as some components are not defined in all notations).
In addition, there might be several secondary alias names available.
</p>
<p class="bold">
Currently, the MonoSaccharideDB web interface supports the following notation schemes:
</p>
<hr class="spacerline">
<div class="bold" id="msdb">MonoSaccharideDB</div>
<p class="longtext">
MonoSaccharideDB's internal notation format. Basetype and substituent names are the same as in <a href="#glycoct">GlycoCT</a>, with the exception that "anhydro" and "lactone" modifications are included in the basetype here, while they are defined as substituents in GlycoCT.
As most glycobiologist and also most carbohydrate databases consider a monosaccharide that contains substituents as one residue, they are defined in this way in MonoSaccharideDB as well.
</p>
<hr class="spacerline">
<div class="bold" id="carbbank">CarbBank</div>
<p class="longtext">
The CarbBank notation is based on the <a href="http://www.chem.qmul.ac.uk/iupac/2carb/38.html#383" class="external_link" target="_blank">IUPAC extended</a> notation.
It is used by the Complex Carbohydrate Structure Database (CCSD), which is better known by the name of its query software "carbbank".
CarbBank style notation forms the basis of several other schemes, such as the <a href="#glycosciences">Glycosciences.de</a> or the Sweet2 schemes.
</p>
<hr class="spacerline">
<div class="bold" id="glycosciences">Glycosciences.de</div>
<p class="longtext">
The Notation used in the <a href="http://www.glycosciences.de/" class="external_link" target="_blank">Glycosciences.de</a> web portal. It is based on the <a href="#carbbank">CarbBank</a> notation.
</p>
<hr class="spacerline">
<div class="bold" id="glycoct">GlycoCT</div>
<p class="longtext">
The nomenclature that is used by <a href="http://www.eurocarbdb.org" class="external_link" target="_blank">EUROCarbDB</a>. For more information see <a href="http://www.eurocarbdb.org/recommendations/encoding/" class="external_link" target="_blank">www.eurocarbdb.org/recommendations/encoding/</a>.
</p>
<hr class="spacerline">
<div class="bold" id="pdb">Protein Data Bank (PDB)</div>
<p class="longtext">
The 3-letter residue names as used by the <a href="http://www.pdb.org" class="external_link" target="_blank">Protein Data Bank</a> (PDB). Unlike most of the other notations, these names cannot be generated automatically by conversion routines but have to be assigned manually by the database administrators.
Some PDB residue names that encode carbohydrate residues define disaccharides or oligosaccharides. These are not implemented in MonoSaccharideDB, as the scope of this database are monosaccharide residues.
</p>
<hr class="spacerline">
<div class="bold" id="bcsdb">Bacterial Carbohydrate Structure Database (BCSDB)</div>
<p class="longtext">
The notation used by the Russian <a href="http://www.glyco.ac.ru/bcsdb/start.shtml" class="external_link" target="_blank">BCSDB</a>.
For more information on this notation, see the <a href="http://www.glyco.ac.ru/bcsdb/help/rules.html" class="external_link" target="_blank">description on the bcsdb homepage</a>.
</p>
<hr class="spacerline">
<div class="bold" id="cfg">Consortium for Functional Glycomics LinearCode (CFG)</div>
<p class="longtext">
The LinearCode notation used by the US <a href="http://www.functionalglycomics.org" class="external_link" target="_blank">Consortium for Functional Glycomics</a> (CFG).
</p>
<hr class="spacerline">
<br>
<#include "../footer.ftl">