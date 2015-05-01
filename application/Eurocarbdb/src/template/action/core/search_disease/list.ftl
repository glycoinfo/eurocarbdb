<html>
  <body>
    <ul>
      <#list matchingDiseases?sort as d >
      <li>${ d.diseaseName }</li>
      </#list>	
    </ul>
  </body>
</html>
  