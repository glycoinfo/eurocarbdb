<#if (scan.annotations?exists && scan.annotations?size > 0) >
  <h3>Annotated with</h3>
  <div>
  <#list scan.annotations as ann>
    <div class="annotation_summary"><@ecdb.sugar_image id=ann.parentStructure.id/>
      <p>
        <#if contributor?exists && ann.contributor.equals(currentContributor)>
        <@ecdb.actionlink class="ecdb_button edit_annotation_link" name="edit_annotations!input" params="annotation.annotationId=${ann.annotationId?c}">Edit</@>
        <@ecdb.actionlink class="ecdb_button delete_annotation_link" name="delete_annotations" params="annotation.annotationId=${ann.annotationId?c}">Delete</@>
        <#else>
        <@ecdb.actionlink class="ecdb_button show_annotation_link" name="show_scan" params="annotation.annotationId=${ann.annotationId?c}">Show</@>        
        </#if>
        </p>
    </div>
  </#list>
  <div style="height: 0px; width: 100%; clear: both; float: none;"></div>
  </div>
</#if>