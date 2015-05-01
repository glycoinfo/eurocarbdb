<style type="text/css">
/*<![CDATA[*/

  .deleteable_box {
    width: 60%;
    float: none;
  }
  .deleteable_box a {
    text-decoration: none !important;
    color: #ffffff !important;
  }
  .deleteable_box a:hover {
    text-decoration: none !important;
    color: #ffffff !important;
  }
  div.yui-content > div {
    position: relative;
  }
  input[type="submit"] {
    position: absolute;
    top: 0px;
    right: 30px;
  }
/*]]>*/
</style>

<#if ( references?exists && references?size > 0 )>
<p>Current references (Total: ${references.size()?c})</p>
<div>
  <#list references as r>
    <div class="deleteable_box"><div class="delete_area"><a href="?deleteReference=${references.indexOf(r)+1}">X</a></div><div class="label"><@ecdb.reference ref=r/></div></div>
  </#list>
</div>
<#else/>
<p>
  There are currently no references.
</p>
</#if>


<div id="references_forms">
<p>Specify the details of a reference to be added:</p>
<div id="form_boxes">
<ul class="yui-nav">
  <li class="selected"><a href="#input_journal">Journal</a></li>
  <li><a href="#input_database">Database</a></li>
  <li><a href="#input_website">Website</a></li>
</ul>
<div class="yui-content">
  <div id="input_journal">
  <@ww.form id="input_journal_frm" method="post">
      <@ww.submit value="Add to entry" />
      ${additional_fields}
      <@ww.textfield size="20" maxlength="20" name="journalReference.pubmedId" label="Pubmed ID"/>
      <@ww.textfield size="40" maxlength="100" name="journalReference.authors" label="Authors"/>
      <@ww.textfield size="40" maxlength="200" name="journalReference.title" label="Title"/>
      <@ww.textfield type="text" size="40" maxlength="100" name="journalReference.journal.journalTitle" label="Journal"/>
      <@ww.textfield size="5" maxlength="5" name="journalReference.journalVolume" label="Vol"/>
      <@ww.textfield size="5" maxlength="5" name="journalReference.firstPage" label="Start Page"/>
      <@ww.textfield size="5" maxlength="5"  name="journalReference.lastPage" label="End page"/>
      <@ww.textfield size="5" maxlength="5"  name="journalReference.publicationYear" label="Year"/>
      <@ww.textfield name="journalReference.referenceComments" label="Comments"/>
      <@ww.hidden name="journalReference.referenceType" value="journal"/>
  </@ww.form>
  </div>
  <div id="input_database">    
  <@ww.form id="input_database_frm" method="post">
      ${additional_fields}
      <@ww.submit value="Add to entry"/>
      <@ww.textfield size="20" maxlength="20" name="reference.externalReferenceName" label="Resource name"/>
      <@ww.textfield size="20" maxlength="20" name="reference.externalReferenceId" label="External ID"/>
      <@ww.textfield size="40" maxlength="200" name="reference.url" label="External URL"/>
      <@ww.textfield name="reference.referenceComments" label="Comments"/>
      <@ww.hidden name="reference.referenceType" value="database"/>
  </@ww.form>
  </div>
  <div id="input_website">
    <@ww.form id="input_website_frm" method="post">
        ${additional_fields}
        <@ww.submit value="Add to entry"/>
        <@ww.textfield size="40" maxlength="200" name="reference.url" label="External URL"/>
        <@ww.textfield name="reference.referenceComments" label="Comments"/>
        <@ww.hidden name="reference.referenceType" value="website"/>
    </@ww.form>
  </div>
</div>
</div>

<script type="text/javascript">

ECDB.disableSelectedControls = function() {
  var target_el = $('input_journal_frm_journalReference_pubmedId');
  var disabled_flag = target_el.value != '';
  var all_inputs = $$('#input_journal input[type="text"]');
  for (var el in all_inputs) {
    if (all_inputs[el] == target_el) {
      continue;
    }
    all_inputs[el].disabled = disabled_flag;
    if (disabled_flag) {
      if (all_inputs[el].style.display != 'none') {
        blindUp(all_inputs[el]);
      }
    } else {
      if (all_inputs[el].style.display == 'none') {
        blindDown(all_inputs[el]);
      }
    }
  }
};

connect(ECDB,'onload',
  function() {
    new YAHOO.widget.TabView('form_boxes');  
  }  
);

connect(ECDB,'onload',
  function() {
    connect($('input_journal_frm_journalReference_pubmedId'),'onblur', ECDB.disableSelectedControls);
    ECDB.disableSelectedControls();
  }
);

ECDB.NEED_TO_LOAD_SCRIPTS=false;

</script>

</div>