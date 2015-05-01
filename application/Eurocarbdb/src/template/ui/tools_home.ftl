<#assign title>EurocarbDB Tools</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<ul>
    <li> 
        <a href="${base}/gwb/home.action">GlycoWorkBench</a>
        <br/>A comprehensive tool for the semi-automated identification 
        and annotation of carbohydrate molecular and fragmentation mass spectra. 
    </li>
    <li>
        <a href="${base}/gpf/Introduction.action">GlycoPeakFinder</a> 
        <br/>A tool for dynamically calculating potential carbohydrate 
        compositions from a given mass.
    </li>
    <li> 
        <a href="${base}/select_instrument.action">AutoGU</a> 
        <br/>Integrated version of the 
        <a href="http://glycobase.nibrt.ie/cgi-bin/profile_upload.cgi">AutoGU</a>
        online tool, which, together with 
        <a href="http://glycobase.nibrt.ie/cgi-bin/public/glycobase.cgi">Glycobase</a>,
        provides for the interpretation of HPLC data. 
    </li>
    <li> 
#        <a href="${base}/ww/nmr/casper.action">CASPER</a> 
	<a href="http://www.casper.organ.su.se/eurocarbdb/casper.action">CASPER</a>
        <br/>Integrated version of the <a href="http://www.casperold.organ.su.se/casper/">
        CASPER</a> online tool for the structure elucidation of oligo- and polysaccharides 
        using NMR data.
    </li>
</ul>

<#include "/template/common/footer.ftl" />

