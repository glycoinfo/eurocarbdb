<#assign title>EUROCarbDB administration actions</#assign>
<#include "/template/common/header.ftl" />

<h1>Administrative tasks</h1>
<ul>
  <li>
    <a href="<@ww.url action="create_contributor" />">Register new user</a>
  </li>
  <li>
    <a href="<@ww.url action="create_database" />">Create database</a>
  </li>
  <!-- li>
    <a href="<@ww.url action="init_load_carbbank" />">Load structures from CarbBank raw file</a>
  </li -->  
  <li>
    <a href="<@ww.url action="test_exception" />">Test exception</a>
  </li>
</ul>
<#include "/template/common/footer.ftl" />