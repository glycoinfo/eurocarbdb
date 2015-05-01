<html>
<body>
<#assign l={'1', '2'}>
<#assign h={'1':'jan', '2':'feb'}>

<@ww.form>
  <@ww.select label="ValuesList: " list=l/><br>
  <@ww.select label="ValuesMap: " list=h/><br>
  <@ww.select label="ValuesAction: " list=values/><br>
  <@ww.select label="ValuesActionMap: " list=mapValues/><br>
  ValuesHTML: <select>
    <option>jan</option>
    <option>feb</option>
  </select>
</@ww.form>
</body>
</html>
