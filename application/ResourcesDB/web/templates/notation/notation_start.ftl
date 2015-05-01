<#include "../header.ftl">
<h1>Notation</h1>
<p class="longtext">
The use of different notations in the various carbohydrate resources impedes the exchange of data and crosslinking between these resources.
This section gives general information that is helpful to understand the concepts of the notations used for <a href="notation.action?topic=monosacc">monosaccharides</a>,
which are composed of a <a href="notation.action?topic=basetype">basetype</a> and <a href="notation.action?topic=subst">substituents</a>.
Furthermore, a brief description of the <a href="notation.action?topic=schemes">notation schemes</a> supported by MonoSaccharideDB is presented.
</p>
<h2 id="uniformity">Uniformity of residue names</h2>
<p class="longtext">
In some notations, more than one name exists for single monosaccharides. This especially applies to residues that feature a loss of stereochemistry.
In glycosciences.de, for example, the residue b-D-4-deoxy-xylHexp is sometimes named b-D-4-deoxy-Glcp, and b-D-4-deoxy-Galp would also be feasible.
Furthermore, a simple change of the order of elements in a residue name, such as b-D-2-deoxy-araHexp vs. 2-deoxy-b-D-araHexp, results in different name strings, which are thus regarded as different residues in a database.
A third cause of non-uniformity of residue names is the use or non-use of trivial names, e.g. a-L-Fucp vs. a-L-6-deoxy-Galp.
</p>
<p class="longtext">
The non-uniformity of carbohydrate residue names leads to a number of problems in addition to the difficulties in data exchange and crosslinking mentioned above.
When various names for one single monosaccharide (and thus also for a carbohydrate chain that contains this residue) are possible, the information on a single carbohydrate chain can be spread over multiple entries in one database.
The user has to know all the possible names and try all combinations thereof to ensure to find all the available information.
</p>
<p class="longtext">
To overcome these problems, MonoSaccharideDB attempts to provide unique residue names by defining one primary alias name for each notation scheme and residue (provided the residue can be encoded in a notation at all).
</p>
<br>
<#include "../footer.ftl">