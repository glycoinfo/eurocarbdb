<#assign title>Schema creation</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>
<script type="text/javascript">
//<![CDATA[
function copy_path() {
	document.getElementById('psql_binary').value = document.getElementById('upload_control').value;
}
//]]>
</script>
<@ww.form action="create_database" onsubmit="copy_path()">
	<@ww.file id="upload_control" label="Select PSQL binary"/>
	<@ww.hidden id="psql_binary" name="psqlBinary"/>
	<@ww.submit />
</@ww.form>

<#include "/template/common/footer.ftl" />