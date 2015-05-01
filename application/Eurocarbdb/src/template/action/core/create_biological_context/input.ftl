<div class="yui-navset yui-skin-sam">
    <@ww.textfield id="bc_tax_search" name="taxonomySearch" label="Enter a taxonomy name" />
    <@ww.textfield id="bc_tis_tax_search" name="tissueTaxonomySearch" label="Enter a tissue name" />
  
    <@ww.textfield id="bc_disease_search" name="diseaseDropdown" label="Enter a disease name" />
    <div id="seen_diseases"></div>
    <div style="clear:both; height: 0px;"></div>

    <@ww.textfield id="bc_perturbation_search" name="perturbationDropdown" label="Enter a perturbation name" />
    <div id="seen_perturbations"></div>
    <div style="clear:both; height: 0px;"></div>

    <@ww.textfield id="bc_comment" name="commentBox" label="Enter comments"/>

    <@ww.submit value="Next" />
</div>

<script type="text/javascript">

connect(ECDB,'onload',function() {
  new ECDB.Autocompleter('user_autocompleter.action','taxonomy_name').apply($('bc_tax_search'));
  new ECDB.Autocompleter('user_autocompleter.action','tissue_name').apply($('bc_tis_tax_search'));
  var disease_ac = new ECDB.Autocompleter('user_autocompleter.action','disease_name').apply($('bc_disease_search'));
  var perturbation_ac = new ECDB.Autocompleter('user_autocompleter.action','perturbation_name').apply($('bc_perturbation_search'));


  disease_ac.itemSelectEvent.subscribe(
    partial(
        ECDB.appendMultipleValues,
        'disease',
        $('bc_disease_search'),
        $('seen_diseases')
        )
  );

  perturbation_ac.itemSelectEvent.subscribe(
    partial(
        ECDB.appendMultipleValues,
        'perturbation',
        $('bc_perturbation_search'),
        $('seen_perturbations')
        )
  );

});

</script>