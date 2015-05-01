<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>CASPER Determine Structure</#assign>
<#assign onload_function>enable();switchSpectra();</#assign>

<#include "/template/common/header.ftl" />

<script type="text/javascript">
<!--
function numeric(str)
{
  str=str.replace(/[^\d\s\.-]/g,"");
  return str;
}
function text(str)
{
  str=str.replace(/[^\w\s\.\(\)]/g,"");
  return str;
}
function intstr(str)
{
  var i=parseInt(str);
  str=i.toString()
  if(str=="NaN")
  {
    return "0";
  }
  else
  {
    return str;
  }
}
function enable()
{
  f=document.forms['casper_determine_start'];
  f.elements['dimensions'][0].disabled=false;
  f.elements['dimensions'][1].disabled=false;
  f.elements['cShifts_ant'].disabled=false;
  f.elements['hShifts_ant'].disabled=false;
  f.elements['cHShifts_ant'].disabled=false;
  f.elements['cShifts_act'].disabled=false;
  f.elements['hShifts_act'].disabled=false;
  f.elements['cHShifts_act'].disabled=false;
}
function disable()
{
  f=document.forms['casper_determine_start'];
  f.elements['dimensions'][0].disabled=true;
  f.elements['dimensions'][1].disabled=true;
  f.elements['cShifts_ant'].disabled=true;
  f.elements['hShifts_ant'].disabled=true;
  f.elements['cHShifts_ant'].disabled=true;
  f.elements['cShifts_act'].disabled=true;
  f.elements['hShifts_act'].disabled=true;
  f.elements['cHShifts_act'].disabled=true;
}
function check_input()
{
  var free = new Array();
  nres=0;
  anomer=0;
  nonano=0;
  multilink=0;

  f=document.forms['casper_determine_start'];

  f.elements['name'].value=numeric(f.elements['name'].value);
  f.elements['cShifts'].value=numeric(f.elements['cShifts'].value);
  f.elements['hShifts'].value=numeric(f.elements['hShifts'].value);
  f.elements['cCorrection'].value=numeric(f.elements['cCorrection'].value);
  f.elements['hCorrection'].value=numeric(f.elements['hCorrection'].value);

  if(f.elements['cCorrection'].value=='')
  {
    f.elements['cCorrection'].value='0';
  }
  if(f.elements['hCorrection'].value=='')
  {
    f.elements['hCorrection'].value='0';
  }
  
  for (i=1; i<9; i++)
  {
    t=f.elements['residue'+i].value.split('.');
    // skip if this residue is not used (resN-field)
    if (t[0]=='')
      continue;
    nres++;
    temp=anomer+nonano;

    // anomeric position
    if (f.elements['linkToPos'+i][0].checked)
    {
      anomer++;
    }
    // other positions
    for(j=1; j<6; j++)
    {
      if (f.elements['linkToPos'+i][j].checked)
      {
        nonano++;
      }
    }
    if(f.elements['linkToPos'+i][6].checked)
    {
      multilink=1;
    }
    if((temp!=0 && nres==1)&& temp==anomer+nonano)
    {
      alert("Residue "+i+" is not linked!");
      return;
    }
  }
  if(nres==1 && (anomer+nonano==0 || anomer!=nonano))
  {
    f.elements['linkToPos1'][6].checked=true;
  }   

  if(nonano!=anomer && multilink==0)
  {
    alert("Inconsistent linkage data");
    return;
  }

  if(nres==0 && f.elements['project'].value=='')
  {
    alert('No residues selected');
    return;
  }

  if ((Number(f.elements['jHHsmall'].value)+
       Number(f.elements['jHHmedium'].value)+
       Number(f.elements['jHHlarge'].value))>nres)
  {
    alert('Too many 3JHH couplings (maximum '+nres+')');
    return;
  };
  if ((Number(f.elements['jCHsmall'].value)+
       Number(f.elements['jCHlarge'].value))>nres)
  {
    alert('Too many 1JCH couplings (maximum '+nres+')');
    return;
  };

  if ((f.elements['cShifts_act'].value<1)&&
      (f.elements['hShifts_act'].value<1)&&
      (f.elements['cHShifts_act'].value<1)&&
      (f.elements['project'].value==''))
  {
    alert("Chemical shifts are required");
    return;
  }
/*  if (f.elements['actual_hsqcchshift'].value%1!=0)
  {
    alert("The chemical shifts must be paired");
    return;
  }*/
  disable();
  f.submit();
  enable();
}

// check consistency of entered positions
function check_pos(opt)
{
// make sure only allowed positions are selected
 f=document.forms['casper_determine_start'];
 t=f.elements['residue'+opt].value.split('.');
 if (t[1].indexOf('1')==-1)
  f.elements['linkToPos'+opt][0].checked=false;
 if (t[1].indexOf('2')==-1)
  f.elements['linkToPos'+opt][1].checked=false;
 if (t[1].indexOf('3')==-1)
  f.elements['linkToPos'+opt][2].checked=false;
 if (t[1].indexOf('4')==-1)
  f.elements['linkToPos'+opt][3].checked=false;
 if (t[1].indexOf('5')==-1)
  f.elements['linkToPos'+opt][4].checked=false;
 if (t[1].indexOf('6')==-1)
  f.elements['linkToPos'+opt][5].checked=false;
 if (t[0]=="")
  f.elements['linkToPos'+opt][6].checked=false;
// other things to do when changing residue
 count_required();
}

function count_required()
{
 f=document.forms['casper_determine_start'];
 total_c=0.0;
 total_h=0.0;
 for (i=1; i<8; i++)
 {
  t=f.elements['residue'+i].value.split('.');
  total_c+=Number(t[2]);
  total_h+=Number(t[3]);
 };
 f.elements['cShifts_ant'].value=total_c;
 f.elements['hShifts_ant'].value=total_h;
 f.elements['cHShifts_ant'].value=total_h;
}

function count_actual(field)
{
  s=document.forms['casper_determine_start'].elements[field].value;

  if(s=='')
  {
    c=0;
  }
  else
  {
    m=s.match(/\s*[\d\.]+\s*/g);

    if(m==0)
    {
      c=0;
    }

    else
    {
      c=m.length;

      if(field=='cHShifts')
      {
        c=c/2;
      }
    }
  }
  document.forms['casper_determine_start'].elements[field+'_act'].value=c;
}

function switchSpectra()
{
    f=document.forms['casper_determine_start'];
    d=document.getElementById('oneDSpectra');
    dd=document.getElementById('twoDSpectra');
    if(f.elements['dimensions'][0].checked)
    {
	d.style.display='';
	dd.style.display='none';
    }
    if(f.elements['dimensions'][1].checked)
    {
	d.style.display='none';
	dd.style.display='';
    }
}

function select_all(opt)
{
  f=document.forms['casper_determine_start'];
  if(f.elements['linkToPos'+opt][6].checked)
  {
    for(i=0;i<6;i++)
    {
      f.elements['linkToPos'+opt][i].checked=true;
    }
  }
  else
  {
    for(i=0;i<6;i++)
    {
      f.elements['linkToPos'+opt][i].checked=false;
    }    
  }
  check_pos(opt);
}

// -->
</script>
<#macro unit_rows count>
  <#list 1..count as x>
    <tr>
      <td>
        <select name="residue${x}" id="residue${x}" onchange="check_pos(${x})"
	 style="width: 175px"
	 title="Select residue if known or use one of the unknown alternatives.">
	  <option value="..0.0">none</option>
	  <#list residueList as residue>
	    <#if x==1 || !residue.name?ends_with("OMe")>
	      <option value="${residue.value}">${residue.name}</option>
	    </#if>
	  </#list>
	</select>
      </td>
      <#list 1..6 as i>
        <td>
	  <input type="checkbox" name="linkToPos${x}" value="${i}" 
	  onclick="check_pos(${x})"
	  title="Check if linked at position ${i}."/>
        </td>
      </#list>
      <td>
        <input type="checkbox" name="linkToPos${x}" value="*"
	onclick="select_all(${x})"
	title="Check if linkages are uncertain. Considered possible positions."/>
      </td>
    </tr>
  </#list>
</#macro>

<#include "../citing.ftl" />

<h1>${title}</h1>

<@ww.form method="post" action="casper_determine_start"
enctype="multipart/form-data">
<input type="hidden" name="structure" value="" />

<table>
  <tr><td>
      <@ww.file name="project" label="CCPN project" title="Load a CCPN project (.tgz format)" onchange="document.forms['casper_determine_start'].elements['disableCcpn'][0].checked=true" /></td>
    <td style="padding-left: 20px">
	<div class="wwgrp">
	<div class="wwlbl">
	<label for="disableCcpn" class="label">
        Disable CCPN: </label></div><br />
	<div class="wwctrl">
	<input type="radio" id="disableCcpn" name="disableCcpn" value="false" checked="checked" title="If disabled speed increases, but projects cannot be loaded or saved."/>No
	<input type="radio" name="disableCcpn" value="true" title="If disabled speed increases, but projects cannot be loaded or saved."/>Yes
	</div></div>
    </td>
  </tr>

  <tr><td>
      <@ww.textfield value="${name}" name="name" label="Title"
		     onchange="this.value=text(this.value)" size="45"
		     maxlength="60"
		     title="Title of the structure/project"/>
    </td>
    <td style="padding-left: 20px">
      <div class="wwgrp">
      <div class="wwlbl">
      <label for="graphicalStructures" class="label">
      Show graphical structures: </label></div><br />
      <div class="wwctrl">
      <input type="radio" id="graphicalStructures" name="graphicalStructures" value="false" checked="checked" title="Show results as text."/>No
      <input type="radio" name="graphicalStructures" value="true" title="Show results in chosen notation. Can be slow."/>Yes
      </div></div>
    </td>

  </tr>

</table>
<table>
  <tr><td>
      <table>
	<tr>
	  <td><label for="residue1" class="label">Residue</label></td>
	  <td colspan="7" align="center"><label for="linkToPos1" class="label">Linkage positions</label></td>
	</tr>
	<tr>
	  <td></td>
	  <td align="center">1</td><td align="center">2</td>
	  <td align="center">3</td><td align="center">4</td>
	  <td align="center">5</td><td align="center">6</td>
	  <td align="center">*</td>
	</tr>
	<@unit_rows count=8 />
      </table>
    </td>
    <td>
      Chemical shifts
      <input name="dimensions" type="radio" value="1D" onchange="count_required();switchSpectra();" checked="checked" title="Switch between input of 1D and 2D spectra." />1D
      <input name="dimensions" type="radio" value="2D" onchange="count_required();switchSpectra();" title="Switch between input of 1D and 2D spectra." />2D
      <div id="oneDSpectra">
      <table>
        <tr><td>
	    <@ww.textarea value="${cShifts}" name="cShifts" cols="35" rows="10"
	      		  maxlength="250"
		      	  onchange="this.value=numeric(this.value);count_actual('cShifts')"
			  title="Experimental C shifts for structure calculation.">
	      <label class="label"><sup>13</sup>C chemical shifts</label>
	    </@ww.textarea>
	  </td>
	  <td>
	    <@ww.textarea value="${hShifts}" name="hShifts" cols="35" rows="10"
    	  		  maxlength="250"
	      		  onchange="this.value=numeric(this.value);count_actual('hShifts')"
			  title="Experimental H shifts for structure calculation.">
	      <label class="label"><sup>1</sup>H chemical shifts</label>
	    </@ww.textarea>
	  </td>
        </tr>
        <tr>
      	  <td>
	    <@ww.textfield name="cCorrection" value="${cCorrection}" size="3"
	  		   label="Correct by subtracting (ppm)"
	       		   maxlength="8"
	       		   onchange="this.value=numeric(this.value)"
		       	   title="Correct for systematic errors from e.g. referencing."/>
      	  </td>
      	  <td>
	    <@ww.textfield name="hCorrection" value="${hCorrection}" size="3"
	  		   label="Correct by subtracting (ppm)"
	       		   maxlength="8"
	       		   onchange="this.value=numeric(this.value)"
		     	   title="Correct for systematic errors from e.g. referencing."/>
	  </td>
        </tr>
	<tr>
	  <td>
	    Number of chemical shifts<br />
	    anticipated: <input type="text" name="cShifts_ant" readonly="readonly"
	    		 	size="3" />
	    actual: <input type="text" name="cShifts_act" readonly="readonly"
	    	    	   size="2" />
	  </td>
	  <td>
	    Number of chemical shifts<br />
	    anticipated: <input type="text" name="hShifts_ant" readonly="readonly"
	    		       	size="3" />
	    actual: <input type="text" name="hShifts_act" readonly="readonly"
	    	    	   size="2" />
	  </td>
	</tr>
      </table>
      </div>
      <div id="twoDSpectra" style="display:none">
      <table>
        <tr><td>
	    <@ww.textarea value="${cHShifts}" name="cHShifts" cols="40" rows="9"
	      		  maxlength="250"
		      	  onchange="this.value=numeric(this.value);count_actual('cHShifts')"
			  title="Experimental C-H shifts (e.g. HSQC) for structure calculation.">
	      <label class="label">C-H chemical shifts</label>
	    </@ww.textarea>
	  </td>
        </tr>
	<tr>
	  <td>
	    Number of chemical shift pairs<br />
	    anticipated: <input type="text" name="cHShifts_ant" readonly="readonly"
	    		 	size="3" />
	    actual: <input type="text" name="cHShifts_act" readonly="readonly"
	    	    	   size="2" />
	  </td>
	</tr>
      </table>
      </div>
    </td></tr>
    <tr><td colspan="2">
        Minimum number of coupling constants of different magnitudes
    </td></tr>
    <tr><td>
    <table>
      <tr><td></td>
      <td>small</td><td>medium</td><td>large</td></tr>
      <tr><td>
          <sup>3</sup><i>J</i><sub>HH</sub>
        </td>
        <td>
          <input type="text" name="jHHsmall" value="${jHHsmall}" size="1" 
          maxlength="2" onchange="this.value=intstr(numeric(this.value))"
	  title="To reduce possible structures specify minimum number of jHH&lt;2 Hz"/>
          (&lt;2 Hz)
        </td>
        <td>
          <input type="text" name="jHHmedium" value="${jHHmedium}" size="1" 
          maxlength="2" onchange="this.value=intstr(numeric(this.value))"
	  title="To reduce possible structures specify minimum number of jHH 2-7 Hz"/>
          (2-7 Hz)
        </td>
        <td>
          <input type="text" name="jHHlarge" value="${jHHlarge}" size="1" 
          maxlength="2" onchange="this.value=intstr(numeric(this.value))"
	  title="To reduce possible structures specify minimum number of jHH&gt;7 Hz"/>
          (&gt;7 Hz)
        </td>
      </tr>
      <tr><td>
          <sup>1</sup><i>J</i><sub>CH</sub>
        </td>
        <td>
          <input type="text" name="jCHsmall" value="${jCHsmall}" size="1"
          maxlength="2" onchange="this.value=intstr(numeric(this.value))"
	  title="To reduce possible structures specify minimum number of jCH&lt;169 Hz"/>
          (&lt;169 Hz)
        </td>
        <td></td>
        <td>
          <input type="text" name="jCHlarge" value="${jCHlarge}" size="1"
          maxlength="2" onchange="this.value=intstr(numeric(this.value))"
	  title="To reduce possible structures specify minimum number of jCH&gt;169 Hz"/>
          (&gt;169 Hz)
        </td>
      </tr>
    </table>
    </td></tr>
  <tr><td colspan="2" align="right">
      <input value="Start determination" type="button" onclick="check_input()"
      title="Start calculations. Time depends on structure and spectra uncertainties." />
  </td></tr>
</table>
</@ww.form>
	  

<#include "/template/common/footer.ftl" />

