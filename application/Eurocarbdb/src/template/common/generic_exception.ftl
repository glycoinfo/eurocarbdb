<#--
This template is used to generate a page for an uncaught exception.
-->
<#assign title>Unexpected error</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<p>
  An unexpected error occurred during execution of the last action 
  (<span><a id="show_exception_details" href="#" onclick="showDetails()"
    >show details</a><a id="hide_exception_details" href="#" onclick="hideDetails()"
    >hide details</a></span>).
</p>
<#if exception?exists>
<p>
If you wish to <@ecdb.create_issue_link 
    summary="Unhandled exception during ${request.requestURI}" 
    text="submit this error as a new issue to the developers"
    exception=exception
/>, a new window will open where 
you can create a new issue for developers to review. This process will only 
take less than a minute. You will need to have (or 
<a href="https://www.google.com/accounts/Login" target="_blank">create</a>) 
a Google account in order to complete the issue creation process. 
</p>

<p>
When creating the issue record, feel free to add information on what you were doing, 
but please do not remove or modify the information below the line '--context---'. 
In particular, consider adding labels (at the bottom) to help us categorise your issue
correctly. Once content, click 'submit'. You will be notified by email if/when this 
issue is reviewed and/or resolved.
</p>

<p>
You can also report a problem to the 
<a href="http://groups.google.com/group/eurocarb-devel/topics" target="_blank">
developer's mailing list</a>, but once again, you will require a Google account 
in order to post. Suffice to say, we greatly prefer problems to be reported via the 
<a href="http://code.google.com/p/eurocarb/issues/list" target="_blank">issue tracker</a>.
</p>

<div id="exception_details">
<hr/>
  <div>
    <p>Cause: ${exception.getMessage()!'(no message)'}</p>
    <p>Stack trace:</p>
    <p>${exceptionStack}</p>
  </div>
</div>
</#if><#-- end if exception?exists -->


<style type="text/css"> 

#show_exception_details {
  display: inline;
}

#hide_exception_details {
  display: none;
}

#exception_details {
  display: none;
}
</style>


<script>      
  function showDetails() {
    document.getElementById('show_exception_details').style.display = 'none';
    document.getElementById('hide_exception_details').style.display = 'inline';
    document.getElementById('exception_details').style.display = 'block';
  }

  function hideDetails() {
    document.getElementById('show_exception_details').style.display = 'inline';
    document.getElementById('hide_exception_details').style.display = 'none';
    document.getElementById('exception_details').style.display = 'none';
  }

  function sendReport() {
    alert('to do, send mail to eurocarbdb-bug-tracking@google.com');
  }
</script>

<#include "/template/common/footer.ftl" />