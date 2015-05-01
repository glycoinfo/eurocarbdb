<html>
  <body>
    <ul>
      <#list matchingTissueTaxonomies?sort as t >
      <li>${ t.tissueTaxon }</li>
      </#list>	
    </ul>
  </body>
</html>
  