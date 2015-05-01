<#assign title>Login failed</#assign>
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
<h2>There has been a problem logging in. Please try logging in again.</h2>
<p><a onclick="window.close();" href="#">Close</a></p>
<script type="text/javascript">
  if (window.opener && window.opener.ECDB) {
    window.opener.ECDB.login_success = false;
  }
</script>
<#include "/template/common/footer.ftl" />