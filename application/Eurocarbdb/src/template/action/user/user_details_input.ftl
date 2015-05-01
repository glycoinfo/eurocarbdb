<#assign title>User details for ${contributor.contributorName}</#assign>
<#include "/template/common/header.ftl" />

<@ww.form>
<@ww.textfield label="Full name" name="contributor.fullName"/>
<@ww.textfield label="Institution" name="contributor.institution"/>
<@ww.submit value="Update"/>
</@ww.form>

<br>
<h3> OR </h3>
<br>
    
        <a class="loginout" href="${base}/changePassword.action">Change Password</a>

<#include "/template/common/footer.ftl" />