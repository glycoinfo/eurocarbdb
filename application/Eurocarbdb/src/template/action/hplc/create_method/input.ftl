<#assign title>Create Method</#assign>
<#include "/template/common/header.ftl" />

<script type="text/javascript">

// This function checks if the username field
// is at least 6 characters long.
// If so, it attaches class="welldone" to the
// containing fieldset.

function checkLength(whatYouTyped) {
        var fieldset = whatYouTyped.parentNode;
        var txt = whatYouTyped.value;
        if (txt.length > 5) {
                fieldset.className = "welldone";
        }
        else {
                fieldset.className = "";
        }
}


// this part is for the form field hints to display
// only on the condition that the text input has focus.
// otherwise, it stays hidden.

function addLoadEvent(func) {
  var oldonload = window.onload;
  if (typeof window.onload != 'function') {
    window.onload = func;
  } else {
    window.onload = function() {
      oldonload();
      func();
    }
  }
}


function prepareInputsForHints() {
  var inputs = document.getElementsByTagName("input");
  for (var i=0; i<inputs.length; i++){
    inputs[i].onfocus = function () {
      this.parentNode.getElementsByTagName("span")[0].style.display = "inline";
    }
    inputs[i].onblur = function () {
      this.parentNode.getElementsByTagName("span")[0].style.display = "none";
    }
  }
}
addLoadEvent(prepareInputsForHints);

</script>

<h1>Method Description</h1>

<p>Enter a brief description of the HPLC running method used</p>

<@ww.form>

<@ww.hidden label="Profile Id" name="profileId"/>
<table>
  <tr>
    <td>
      Temperature 
    </td>
    <td>
      <@ww.textfield name="temperature" value="30" onkeyup="checkLength(this);" />
    <span class="hint">Temperature must be recorded in degrees Celcius</span> 
    </td>
<!--    <td>
      Celcius 
    </td>
-->
  </tr>
  <tr>
    <td>
      Solvent A 
    </td>
    <td>
      <@ww.textfield name="solventA" value="50mM Ammonium Formate pH4.4" onkeyup="checkLength(this);" />
     <span class="hint"> Enter a full description of solvents used</span>
    </td>
  </tr>
  <tr>
    <td>
     Solvent B
    </td>
    <td>
      <@ww.textfield name="solventB" value="Acetonitrile" onkeyup="checkLength(this);" />
     <span class="hint"> Enter a full description of solvents used</span> 
    </td>
  </tr>
  <tr>
    <td>
     Solvent C
    </td>
    <td> 
      <@ww.textfield name="solventC" onkeyup="checkLength(this);" />
    <span class="hint"> Enter a full description of solvents used</span>
    </td>
  </tr>
   <tr>
    <td>
    Solvent D
    </td>
    <td>
      <@ww.textfield name="solventD" onkeyup="checkLength(this);" />
    <span class="hint"> Enter a full description of solvents used</span> 
    </td>
  </tr>
  <tr>
    <td>
    Flow Rate
    </td>
    <td>
     <@ww.textfield name="flowRate" value="0.4" onkeyup="checkLength(this);" />
    <span class="hint">Enter flow rate of HPLC in ml/min</span> 
    </td>
    </tr>
  <tr>
    <td>
    Flow Gradient
    </td>
    <td>
    <@ww.textfield name="flowGradient" value="20-58% A over 152 mins" onkeyup="checkLength(this);"/>
    <span class="hint">Enter flow gradient changes (no fixed format to date) </span>
    </td>
    </tr>

    <tr><td></td><td><@ww.submit name="submit"/></td</tr>
</table>

</@ww.form>

<#include "/template/common/footer.ftl" />

