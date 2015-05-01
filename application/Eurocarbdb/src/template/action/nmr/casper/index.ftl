<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>CASPER</#assign>

<#include "/template/common/header.ftl" />

<#include "citing.ftl" />

<h1>${title}</h1>

<p>
CASPER calculates chemical shifts of oligo- and polysaccharides based on the
constituent monosaccharides and the glycosylation shifts from substitutions.</p>
<p>There are two main usages of CASPER:</p>
<h2><@ww.a href="casper_simulate.action">Simulate Spectra</@ww.a></h2>
When the carbohydrate structure is known CASPER can calculate the <sup>1</sup>H
and <sup>13</sup>C chemical shifts.
Experimental data can also be submitted to compare it with the calculated
values or two assign a list of chemical shifts to atoms.
<h2><@ww.a href="casper_determine.action">Determine Structure</@ww.a></h2>
If the structure is not known CASPER can determine the carbohydrate sequence
based on NMR data and data from component and methylation analyses. CASPER can
handle uncertain linkage positions and also unknown sugars in the sample in case
the analyses have not been complete. This makes the calculations take longer.
CASPER accepts <sup>1</sup>H, <sup>13</sup>C and CH (from e.g. HSQC or HETCOR) 
NMR data. Coupling constants can be submitted to limit the possible number of
structures and thereby shorten calculation times.
<h3>CCPN</h3>
CASPER is compatible with the <a href="http://www.ccpn.ac.uk/index.html">
CCPN</a> data model. This means that CASPER can load and save CCPN projects
making it a natural part of NMR assignment work flow. Before loading a CCPN
project into CASPER it is best to pick the peaks and merge resonances together
if they are known to originate from the same signal. If the structure is known
it can be built either before loading the CCPN project or using the CASPER web
interface. If the structure is not known it is possible to specify components
in the project, but it is easier to use the CASPER interface instead.

<#include "/template/common/footer.ftl" />

