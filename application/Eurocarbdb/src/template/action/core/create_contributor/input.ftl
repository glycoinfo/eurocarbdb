<#assign title>Create contributor</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<@ww.form style="plain">

<@ww.textfield label="Enter the name by which you would like this contributor to be identified" name="contributor.contributorName" required="true"/>
<@ww.password label="Enter the password for login" name="contributor.password" required="true" />
<@ww.password label="Repeat the password for security" name="repeatedPassword" required="true" />
<@ww.checkbox label="Grant the user administrative rights" name="contributor.isAdmin" />
<p>Additional information</p>
<@ww.textfield label="Full name" name="contributor.fullName"/>
<@ww.textfield label="Institution" name="contributor.institution"/>

<@ww.submit value="Continue ->" />

</@ww.form>

<#include "/template/common/footer.ftl" />
