<#assign title>Create a new Experiment</#assign>
<#include "/template/ui/user/header.ftl" />

<h1>${title}</h1>

<p>

</p>

<@ww.form>

<@ww.textfield name="experiment.experimentName" 
               label="Enter experiment name" 
               size="40" />

<@ww.textarea name="experiment.experimentComments" 
              label="Enter any applicable comments"
              rows="6" cols="39" 
              />

<@ww.submit value="Continue ->" />              
              
</@ww.form>

<#include "/template/ui/user/footer.ftl" />

