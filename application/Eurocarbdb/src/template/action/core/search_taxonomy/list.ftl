<html>
  <body>
    <ul>
      <#list matchingTaxonomies?sort as t >
      <li>${ t.taxon }</li>
      </#list>	
    </ul>
  </body>
</html>
  