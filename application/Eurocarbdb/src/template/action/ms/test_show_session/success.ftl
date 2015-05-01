
<html>
  <head>
    <title>Test session</title>
  </head>
  <body>
    <h1>Session content</h1>
    
    <table>
      <thead>
	<tr>
	  <th>Key</th>
	  <th>Value</th>
	</tr>
      </thead>
      <tbody>
	<#list action.getSession().entrySet() as e>
	<tr>
	  <td>${e.key}</td> 
	  <td>${e.value}</td>
	</tr>
	</#list>
      </tbody>
    </table>

    <p>
      <a href="test_fill_session.action">Fill session</a>
    </p>
    
  </body>
</html>