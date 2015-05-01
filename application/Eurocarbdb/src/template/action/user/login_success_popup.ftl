<#assign title>Login successful</#assign>
<#include "/template/common/header.ftl" />

<style type="text/css">
  div#perspectives {
    display: none;
  }
  
  #refine {
    display: none;
  }
</style>
<h1>${title}</h1>
<h2>You have successfully logged in</h2>
<p>You will be redirected to where you required a log-in</p>
<p><a onclick="window.close();" href="#">Close</a></p>
<script type="text/javascript">
  if (window.opener && window.opener.ECDB) {
    window.opener.ECDB.login_success = true;
  }
</script>
<#include "/template/common/footer.ftl" />
