<#assign title>Load carbbank glycan sequences</#assign>
<#include "/template/common/header.ftl" />
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<h1>${title}</h1>

<@ww.form>
<p>
    You are about to parse and load Carbbank glycan sequence data.
    Loading all sequences will take quite a while. You can load 
    a subset of Carbbank by setting the following fields:
</p>
<p>
    <@ww.textfield label="Start at Carbbank record" 
                   name="firstRecord" 
                   value="1" />
    <@ww.textfield label="Number of records to load (negative number or zero means 'all')" 
                   name="loadLimit" 
                   value="-1" />
</p>

<@ww.submit value="Continue ->" />

</@ww.form>

<#include "/template/common/footer.ftl" />
