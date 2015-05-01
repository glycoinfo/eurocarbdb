<#assign title>GlycoBase 2.0</#assign>
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<@ecdb.use_js_lib name="${'YUI::Tabs'}"/>
<@ecdb.use_js_lib name="${'YUI::Dialog'}"/>


<#include "/template/common/header.ftl" />








<style>
.yui-navset .yui-nav li,.yui-navset .yui-navset-top .yui-nav li,.yui-navset .yui-navset-bottom .yui-nav li{margin:0 .5em 0 0;}.yui-navset-left .yui-nav li,.yui-navset-right .yui-nav li{margin:0 0 .5em;}.yui-navset .yui-content .yui-hidden{position:absolute;left:-999999px;visibility:hidden;}.yui-navset .yui-navset-left .yui-nav,.yui-navset .yui-navset-right .yui-nav,.yui-navset-left .yui-nav,.yui-navset-right .yui-nav{width:6em;}.yui-navset-top .yui-nav,.yui-navset-bottom .yui-nav{width:auto;}.yui-navset .yui-navset-left,.yui-navset-left{padding:0 0 0 6em;}.yui-navset-right{padding:0 6em 0 0;}.yui-navset-top,.yui-navset-bottom{padding:auto;}.yui-nav,.yui-nav li{margin:0;padding:0;list-style:none;}.yui-navset li em{font-style:normal;}.yui-navset{position:relative;zoom:1;}.yui-navset .yui-content,.yui-navset .yui-content div{zoom:1;}.yui-navset .yui-content:after{content:'';display:block;clear:both;}.yui-navset .yui-nav li,.yui-navset .yui-navset-top .yui-nav li,.yui-navset .yui-navset-bottom .yui-nav li{display:inline-block;display:-moz-inline-stack;*display:inline;vertical-align:bottom;cursor:pointer;zoom:1;}.yui-navset-left .yui-nav li,.yui-navset-right .yui-nav li{display:block;}.yui-navset .yui-nav a{position:relative;}.yui-navset .yui-nav li a,.yui-navset-top .yui-nav li a,.yui-navset-bottom .yui-nav li a{display:block;display:inline-block;vertical-align:bottom;zoom:1;}.yui-navset-left .yui-nav li a,.yui-navset-right .yui-nav li a{display:block;}.yui-navset-bottom .yui-nav li a{vertical-align:text-top;}.yui-navset .yui-nav li a em,.yui-navset-top .yui-nav li a em,.yui-navset-bottom .yui-nav li a em{display:block;}.yui-navset .yui-navset-left .yui-nav,.yui-navset .yui-navset-right .yui-nav,.yui-navset-left .yui-nav,.yui-navset-right .yui-nav{position:absolute;z-index:1;}.yui-navset-top .yui-nav,.yui-navset-bottom .yui-nav{position:static;}.yui-navset .yui-navset-left .yui-nav,.yui-navset-left .yui-nav{left:0;right:auto;}.yui-navset .yui-navset-right .yui-nav,.yui-navset-right .yui-nav{right:0;left:auto;}.yui-skin-sam .yui-navset .yui-nav,.yui-skin-sam .yui-navset .yui-navset-top .yui-nav{border:solid #2647a0;border-width:0 0 5px;zoom:1;}.yui-skin-sam .yui-navset .yui-nav li,.yui-skin-sam .yui-navset .yui-navset-top .yui-nav li{margin:0 .16em 0 0;padding:1px 0 0;zoom:1;}.yui-skin-sam .yui-navset .yui-nav .selected,.yui-skin-sam .yui-navset .yui-navset-top .yui-nav .selected{margin:0 .16em -1px 0;}.yui-skin-sam .yui-navset .yui-nav a,.yui-skin-sam .yui-navset .yui-navset-top .yui-nav a{background:#d8d8d8 url(http://yui.yahooapis.com/2.7.0/build/assets/skins/sam/sprite.png) repeat-x;border:solid #a3a3a3;border-width:0 1px;color:#000;position:relative;text-decoration:none;}.yui-skin-sam .yui-navset .yui-nav a em,.yui-skin-sam .yui-navset .yui-navset-top .yui-nav a em{border:solid #a3a3a3;border-width:1px 0 0;cursor:hand;padding:.25em .75em;left:0;right:0;bottom:0;top:-1px;position:relative;}.yui-skin-sam .yui-navset .yui-nav .selected a,.yui-skin-sam .yui-navset .yui-nav .selected a:focus,.yui-skin-sam .yui-navset .yui-nav .selected a:hover{background:#2647a0 url(http://yui.yahooapis.com/2.7.0/build/assets/skins/sam/sprite.png) repeat-x left -1400px;color:#fff;}.yui-skin-sam .yui-navset .yui-nav a:hover,.yui-skin-sam .yui-navset .yui-nav a:focus{background:#bfdaff url(http://yui.yahooapis.com/2.7.0/build/assets/skins/sam/sprite.png) repeat-x left -1300px;outline:0;}.yui-skin-sam .yui-navset .yui-nav .selected a em{padding:.35em .75em;}.yui-skin-sam .yui-navset .yui-nav .selected a,.yui-skin-sam .yui-navset .yui-nav .selected a em{border-color:#243356;}.yui-skin-sam .yui-navset .yui-content{background:#edf5ff;}.yui-skin-sam .yui-navset .yui-content,.yui-skin-sam .yui-navset .yui-navset-top .yui-content{border:1px solid #808080;border-top-color:#243356;padding:.25em .5em;}.yui-skin-sam .yui-navset-left .yui-nav,.yui-skin-sam .yui-navset .yui-navset-left .yui-nav,.yui-skin-sam .yui-navset .yui-navset-right .yui-nav,.yui-skin-sam .yui-navset-right .yui-nav{border-width:0 5px 0 0;Xposition:absolute;top:0;bottom:0;}.yui-skin-sam .yui-navset .yui-navset-right .yui-nav,.yui-skin-sam .yui-navset-right .yui-nav{border-width:0 0 0 5px;}.yui-skin-sam .yui-navset-left .yui-nav li,.yui-skin-sam .yui-navset .yui-navset-left .yui-nav li,.yui-skin-sam .yui-navset-right .yui-nav li{margin:0 0 .16em;padding:0 0 0 1px;}.yui-skin-sam .yui-navset-right .yui-nav li{padding:0 1px 0 0;}.yui-skin-sam .yui-navset-left .yui-nav .selected,.yui-skin-sam .yui-navset .yui-navset-left .yui-nav .selected{margin:0 -1px .16em 0;}.yui-skin-sam .yui-navset-right .yui-nav .selected{margin:0 0 .16em -1px;}.yui-skin-sam .yui-navset-left .yui-nav a,.yui-skin-sam .yui-navset-right .yui-nav a{border-width:1px 0;}.yui-skin-sam .yui-navset-left .yui-nav a em,.yui-skin-sam .yui-navset .yui-navset-left .yui-nav a em,.yui-skin-sam .yui-navset-right .yui-nav a em{border-width:0 0 0 1px;padding:.2em .75em;top:auto;left:-1px;}.yui-skin-sam .yui-navset-right .yui-nav a em{border-width:0 1px 0 0;left:auto;right:-1px;}.yui-skin-sam .yui-navset-left .yui-nav a,.yui-skin-sam .yui-navset-left .yui-nav .selected a,.yui-skin-sam .yui-navset-left .yui-nav a:hover,.yui-skin-sam .yui-navset-right .yui-nav a,.yui-skin-sam .yui-navset-right .yui-nav .selected a,.yui-skin-sam .yui-navset-right .yui-nav a:hover,.yui-skin-sam .yui-navset-bottom .yui-nav a,.yui-skin-sam .yui-navset-bottom .yui-nav .selected a,.yui-skin-sam .yui-navset-bottom .yui-nav a:hover{background-image:none;}.yui-skin-sam .yui-navset-left .yui-content{border:1px solid #808080;border-left-color:#243356;}.yui-skin-sam .yui-navset-bottom .yui-nav,.yui-skin-sam .yui-navset .yui-navset-bottom .yui-nav{border-width:5px 0 0;}.yui-skin-sam .yui-navset .yui-navset-bottom .yui-nav .selected,.yui-skin-sam .yui-navset-bottom .yui-nav .selected{margin:-1px .16em 0 0;}.yui-skin-sam .yui-navset .yui-navset-bottom .yui-nav li,.yui-skin-sam .yui-navset-bottom .yui-nav li{padding:0 0 1px 0;vertical-align:top;}.yui-skin-sam .yui-navset .yui-navset-bottom .yui-nav a em,.yui-skin-sam .yui-navset-bottom .yui-nav a em{border-width:0 0 1px;top:auto;bottom:-1px;}
.yui-skin-sam .yui-navset-bottom .yui-content,.yui-skin-sam .yui-navset .yui-navset-bottom .yui-content{border:1px solid #808080;border-bottom-color:#243356;}

#container {height:3em;}

</style>

<style>
.yui-accordion{position:relative;zoom:1;}.yui-accordion .yui-accordion-item{display:block;}.yui-accordion .yui-accordion-item .yui-accordion-item-bd{height:0;_height:1px;*height:1px;overflow:hidden;zoom:1;}.yui-accordion .yui-accordion-item-active .yui-accordion-item-bd{height:auto;}.yui-accordion-hidden{border:0;height:0;width:0;padding:0;position:absolute;left:-999999px;overflow:hidden;visibility:hidden;}.yui-skin-sam .yui-accordion{font-size:93%;line-height:1.5;*line-height:1.45;border-top:solid 1px #808080;border-left:solid 1px #808080;border-right:solid 1px #808080;background:#fff;padding:0;text-align:left;}.yui-skin-sam .yui-accordion .yui-accordion-item{display:block;border-bottom:solid 1px #808080;}.yui-skin-sam .yui-accordion .yui-accordion-item .yui-accordion-item-hd{line-height:2;*line-height:1.9;background:url(http://yui.yahooapis.com/3.0.0/build/assets/skins/sam/sprite.png) repeat-x 0 0;padding:0;padding:5px;}.yui-skin-sam .yui-accordion .yui-accordion-item .yui-accordion-item-bd{font-size:100%;margin:0;padding:0;display:block;}.yui-skin-sam .yui-accordion .yui-accordion-item-hd a.yui-accordion-item-trigger{width:auto;display:block;color:#000;text-decoration:none;cursor:default;padding:0 5px 0 10px;background:url(http://yui.yahooapis.com/3.0.0/build/assets/skins/sam/sprite.png) no-repeat 110% -345px;}.yui-skin-sam .yui-accordion .yui-accordion-item-active .yui-accordion-item-hd a.yui-accordion-item-trigger{background:url(http://yui.yahooapis.com/3.0.0/build/assets/skins/sam/sprite.png) no-repeat 110% -395px;}
</style>


<script>
		YAHOO.namespace("example.container");

		function init() {
			// Instantiate a Panel from markup
			YAHOO.example.container.panel1 = new YAHOO.widget.Panel("panel1", { width:"320px", visible:false, constraintoviewport:true } );
			YAHOO.example.container.panel1.render();

			// Instantiate a Panel from script
		

			YAHOO.util.Event.addListener("show1", "click", YAHOO.example.container.panel1.show, YAHOO.example.container.panel1, true);
			YAHOO.util.Event.addListener("hide1", "click", YAHOO.example.container.panel1.hide, YAHOO.example.container.panel1, true);

		}

		YAHOO.util.Event.addListener(window, "load", init);
</script>

<script type="text/javascript">
$(document).ready(function() 
    { 
        $("#myTable").tablesorter({sortList: [[1,0]]}); 
        $("#myTable2").tablesorter({sortList: [[1,0]]}); 
    } 
); 
</script>



<style>
		/* module examples */
		div#demo {
		    position:relative;
		    width:100%;
		}
		.yui-accordion .yui-accordion-item {
				text-align: left;
		}
				
</style>
<!-- YUI3 Core //-->
<script type="text/javascript" src="http://yui.yahooapis.com/3.0.0/build/yui/yui-min.js"></script>

<script type="text/javascript">
		YUI({
			modules: {
				'gallery-node-accordion': {
					fullpath: 'http://yui.yahooapis.com/gallery-2009.10.27-23/build/gallery-node-accordion/gallery-node-accordion-min.js',
					requires: ['node-base','node-style','plugin','node-event-delegate','classnamemanager'],
					optional: ['anim'],
					supersedes: []
			  }
		 
			}
		}).use('anim', 'gallery-node-accordion', function (Y) {
			
		    Y.one("#myaccordion").plug(Y.Plugin.NodeAccordion, { 
				anim: Y.Easing.backIn
			});
			
		});
	</script>




<#if ! currentContributor.isGuest() >
<@ecdb.context_box title="Actions" >
<a href="create_hplc_gu.action?glycanSequenceId=${glycan.ogbitranslation?c}" title="Add or associate HPLC data to this entry">Add HPLC data</a>
<a href="show_glycan.action?glycanSequenceId=${glycan.ogbitranslation?c}" title="View core data description">Core Database Information</a>
</@ecdb.context_box>
</#if>

<#if currentContributor.isGuest() >
<@ecdb.context_box title="Actions" >
<a href="show_glycan.action?glycanSequenceId=${glycan.ogbitranslation?c}" title="View core data description">Core Database Information</a>
</@ecdb.context_box>
</#if>

<@ecdb.context_box title="NIBRT">
<p>National Institute for Bioprocessing Research and Training (NIBRT) is located in Dublin, Ireland 
with a mandate to support the development of the bioprocessing industry by: Training highly skilled personnel for the 
bioprocessing industry. Conducting world-class research in key areas of bioprocessing. Providing flexible, 
multi-purpose bioprocessing research and training facilities. For further information please 
refer to <a href="http://www.nibrt.ie">www.nibrt.ie</a> or <a href="mailto:info@nibrt.ie?Subject=GlycoBase">email</a></p>
</@ecdb.context_box>

<#assign max_stereochem_equivalents=10 />
<#assign max_equivalents=10 />
<#assign max_isomers=10 />
<#assign max_superstructs=10 />

<@ecdb.context_box title="NIBRT">
<div id="demo">
			
			<div class="bd">

				<div id="myaccordion" class="yui-accordion">

				    <div class="yui-module yui-accordion-item yui-accordion-item-active first-of-type">

				    	
			            <div class="yui-hd yui-accordion-item-hd">
			            	<a href="#" class="yui-accordion-item-trigger">Stereochemical equivalents</a>
						</div>
			            <div class="yui-bd yui-accordion-item-bd">
			            	<#if (displaySuperStructures?size > 0)>
								<#if (displaySuperStructures?size > max_stereochem_equivalents)>
									<p><a href="${base}/search_glycan_sequence.action?sequenceGWS=${displaySuperStructures[3]}">${displaySuperStructures?size} total</a>, showing most recent 10
    							<#else>
    								<p>${displaySuperStructures?size} structure(s)</p>
    							</#if>
    							<#list displaySuperStructures as gs >
    							<#if gs_index == max_stereochem_equivalents><#break></#if>
   
     							<@ecdb.linked_sugar_image id=gs.glycanSequenceId seq=gs.sequenceGWS?url />
    							</#list>
								<#else>
    								<em>none</em>    
								</#if>
			            </div>
						
					</div>

				    <div class="yui-module yui-accordion-item">
				    	
			            <div class="yui-hd yui-accordion-item-hd">
			            	<a href="#" class="yui-accordion-item-trigger">Equivalent structures</a>
						</div>
			            <div class="yui-bd yui-accordion-item-bd">
			            		<#attempt>
			            		<#if (displayEquivalents?size > 0)>
  								<#if (displayEquivalents?size > max_equivalents)>
    								<p>${displayEquivalents?size} total, showing most recent 10</p>
    							<#else>
    								<p>${displayEquivalents?size} structure(s)</p>
  								</#if>
  								<#list displayEquivalents as gs >
    							<#if gs_index == max_equivalents><#break></#if>
    							<@ecdb.linked_sugar_image id=gs.glycanSequenceId seq=gs.sequenceGWS?url />
  								</#list>   
								</#if>
								
								<#recover>
    							<!-- equivalents query failed -->
								</#attempt>
					    </div>

			
					</div>
					<div class="yui-module yui-accordion-item">
						
			            <div class="yui-hd yui-accordion-item-hd">
			            	<a href="#" class="yui-accordion-item-trigger">Linkage isomers</a>
						</div>
			            <div class="yui-bd yui-accordion-item-bd">
			            	<#attempt>
			            	<#if (displayLinkage?size > 0)>
    						<#if (displayLinkage?size > max_isomers)>
    						<p>${displayLlinkage?size} total, showing most recent 10
    						<#else>
    						<p>${displayLinkage?size} structure(s)</p>
    						</#if>
    						<#list displayLinkage as gs >
    						<#if gs_index == max_isomers><#break></#if>
     						<@ecdb.linked_sugar_image id=gs.glycanSequenceId seq=gs.sequenceGWS?url />
    						</#list>
							<#else>
    						<em>none</em>    
							</#if>
							
							<#recover>
							<!-- linkage isomer query failed -->
							</#attempt>

					    </div>
			
					</div>
					
					<div class="yui-module yui-accordion-item">
						
			            <div class="yui-hd yui-accordion-item-hd">
			            	<a href="#" class="yui-accordion-item-trigger">Superstructures of this structure</a>
						</div>
						<div class="yui-bd yui-accordion-item-bd">
						<#if (displaySuperStructures?size > 0)>
<p>
    ${displaySuperStructures?size}<#if (displaySuperStructures?size > max_superstructs)>
    total, showing most recent 10
    <#else>
    superstructures(s)
    </#if>
    </p>
    <#list displaySuperStructures as gs >
    <#if gs_index == max_superstructs><#break></#if>
    <@ecdb.linked_sugar_image id=gs.glycanSequenceId seq=gs.sequenceGWS?url />
    </#list>
<#else>
    <em>none</em>    
</#if>
						</div>
					
					
			
					</div>

					
			</div>
		</div>
</div>
</@ecdb.context_box>




<@ecdb.context_box title="Information">
<p>The goal of EUROCarbDB is to develop bioinformatic solutions which assist the interpretation and storage of experimental data. We, therefore, invite any groups interested in expanding the resources presented to contact the <a href="mailto:matthew.campbell@nibrt.ie">EUROCarbDB developers</a>.</p>
</@ecdb.context_box>


<#assign testing = displayMultipleCt?size>
<a href="http://www.nibrt.ie"><img src="${base}/images/nibrt_logo.png" align="right"/></a>

<div class="hplc_glycan_entry">
Glycan Id: ${glycan.glycanId?c}
</div>


<h1>HPLC Glycan Details</h1>
<!--
<p><@ecdb.sugar_image id=glycan.ogbitranslation /></p>
-->

<table><tr><b>Glycan Name:</b>${glycan.name}</tr>
<#if (testing == 0) >
<p><img src="get_sugar_image.action?download=true&scale=0.6&outputType=png&glycanSequenceId=${glycan.ogbitranslation?c}"/></p>
</#if>

<#if (testing != 0) >
    <#list displayMultipleCt as multiple>
    <img style="display: block;" src="get_sugar_image.action?download=true&scale=0.4&outputType=png&glycanSequenceId=${multiple.sequenceId?c}"/>
	<#if multiple_has_next></#if>
    </#list>
</#if>

<#assign image = "${base}/images/hplc_structures/${glycan.name}.png" />

<#if (testing = 0) >
<div id="container">
	<div>
		<button id="show1">Show stored image</button> 
	</div>
	
	<div id="panel1">
		<div class="hd">Image created manually - Draggable</div>
		<div class="bd">
		<img src="${base}/images/hplc_structures/${glycan.name}.png" alt="image to be made"/>
		</div>
		<div class="ft">end manual</div>
	</div>
</div>
</#if>

     <#list displayStats as s>
     <tr><td><b>GU Value:</b></td><td>${s[1]?string("0.##")}</td>  
	 </tr>
     <#if s[2] !=1 >
    
     <tr>
       <td><b>Standard Dev:</b></td><td>${s[0]?string("0.##")}</td>
     </tr>
     </#if>
     </#list>
     
</table>

<p></p>
<h2>Monosaccharide Composition</h2>
	<table class="hplc_table_entry">
      	<tr>
        <#if glycan.hex?size != 0 >
        <td>Hex: ${glycan.hex}</td>
	</#if>
        </tr>
        <tr>
        <#if glycan.hexnac?size != 0 >
        <td>HexNAc: ${glycan.hexnac}</td>
        </#if>
        </tr>
 	<tr>
	<#if glycan.neunac?size != 0 >
	<td>NeuNAc: ${glycan.neunac}</td>
	</#if>
        </tr>
	<tr>
	<#if glycan.fucose?size != 0 >
       <td>Fucose: ${glycan.fucose}</td>
	</#if>
	</tr>
	<tr>
	<#if glycan.xylose?size != 0>
	<td>Xylose: ${glycan.xylose}</td>
	</#if>
        </tr>
	</table>

<h2>Digest Pathways</h2>
	  <table class="hplc_table_entry">
	  <#if (testing == 0) >
	  <#list displayDigestSingle as digest>
	  <tr><td><img src="get_sugar_image.action?download=true&scale=0.4&outputType=png&glycanSequenceId=${glycan.ogbitranslation?c}"/></td><td>${digest[0]}<br/></td>
	  <td><a href="show_glycanEntry.action?glycanId=${digest[1]}"><img src="get_sugar_image.action?download=true&scale=0.4&outputType=png&glycanSequenceId=${digest[3]?c}"/></a>
	  </td></tr>
	  <#if digest_has_next></#if>
	  </#list>
	  </#if>
	  </table>

	  <table class="hplc_table_entry" >
	  <#if (testing != 0) >
	  
	  <#list displayMultiDigests as digestMultiple>
	  <#if (digestMultiple[4]?exists)>

	  <tr><td><img style="display: block;"
	  src="get_sugar_image.action?download=true&scale=0.4&outputType=png&glycanSequenceId=${digestMultiple[2]?c}"/></td><td>${digestMultiple[0]}<img src="/ecdb/images/longarrow.gif"/></td><td><a href="show_glycanEntry.action?glycanId=${digestMultiple[1]}"><img src="/ecdb/ww/get_sugar_image.action?download=true&scale=0.4&outputType=png&glycanSequenceId=${digestMultiple[4]?c}"/></a></td></tr>
	  <tr><td>${glycan.name}</td><td></td><td><a href="show_glycanEntry.action?glycanId=${digestMultiple[1]}">${digestMultiple[5]}</a></td></tr>
	  </#if>
	  <#if digestMultiple_has_next></#if> 
	  </#list>
	  </#if> 
	  </table>

	

<h2>Publication  Information</h2>

<div id="demo2" class="yui-navset">
		
		<div class="yui-content">
		<div id="markup">
		<table id="IW-ms-data">
		<thead>
		<tr><th>Author</th><th>Year</th><th>Reported GU</th><th>MS</th><th>MS/MS</th></tr>
		</thead>
		<tbody>
		<#list displayRefs as p>
		<#if (p[4]?exists) >
		<tr><td style="text-align:left"><a href="http://www.ncbi.nlm.nih.gov/pubmed/${p[4]?c}">${p[3]}</a><td style="text-align:center" >${p[5]?c}<td style="text-align:center">${p[0]}<td style="text-align:center">${p[2]}<td style="text-align:center">${p[1]}<#if p_has_next></#if>
      
        </#if>
		</#list>
		</tbody>
		</table>
		</div>
		</div>
</div>
	
<script type="text/javascript">
YAHOO.util.Event.addListener(window, "load", function() {
    YAHOO.example.EnhanceFromMarkup = new function() {
        var myColumnDefs = [
            {key:"author",label:"Author",sortable:true},
            {key:"year",label:"Year", sortable:true},
            {key:"reported gu",label:"Reported GU", sortable:true},
            {key:"ms",label:"MS"},
            {key:"ms/ms",label:"MS/MS"}
        ];

        this.parseNumberFromCurrency = function(sString) {
            // Remove dollar sign and make it a float
            return parseFloat(sString.substring(1));
        };

        this.myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("IW-ms-data"));
        this.myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
        this.myDataSource.responseSchema = {
            fields: [{key:"author"},
                   // {key:"cva",parser:YAHOO.util.DataSource.parseNumber},
                    //{key:"aggscore", parser:YAHOO.util.DataSource.parseNumber},
                    //{key:"avpoints", parser:this.parseNumber},
                    {key:"year"},
                    {key:"reported gu"},
                    {key:"ms"},
                    {key:"ms/ms"}
                   
            ]
        };

        this.myDataTable = new YAHOO.widget.DataTable("markup", myColumnDefs, this.myDataSource,
                {sortedBy:{key:"year",dir:"desc"}});
        };

});

</script>	

<#include "/template/common/footer.ftl" />

