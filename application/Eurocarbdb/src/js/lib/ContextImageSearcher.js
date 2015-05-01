/**
 *  @fileoverview       Context Image searcher
 *  @author             hirenj
 */
if (typeof(ContextImageSearcher) == undefined ) {
	ContextImageSearcher = {};
}

if ( typeof(GimageSearch) == undefined ) {
	log(typeof(GimageSearch));
	log("ERROR Missing Google Search API library (GimageSearch)");
}
/**
 * Create a new ContextImageSearcher.
 * Context Image searcher. Runs a google search for the given query. Filters out results
 * that do not come from Tree of Life, and loads up imageElement with the image that is found
 * Usage:
 * <pre>
 *	imagesearcher = new ContextImageSearcher('image_element_id', 'taxon wikipedia filetype:jpg bovine');
 *</pre> 
 * @param {String} imageElement The id of the image element to place results into
 * @param {String} query The query string to execute
 * @constructor 
 */
ContextImageSearcher = function(imageElement,query) {
	this.imageElement = $(imageElement);
	this.query = query;
	this._initialiseImage();
};

ContextImageSearcher.prototype = {
	__class__: ContextImageSearcher, 
	imageElement: null,
	query: null,
};

ContextImageSearcher.prototype._initialiseImage = function() {
	var searcher = new GimageSearch();
  searcher.setNoHtmlGeneration();
  searcher.setResultSetSize(GSearch.LARGE_RESULTSET);
  searcher.setSearchCompleteCallback(this,
                                     this._resultCallback,
                                     [searcher]
                                     );
  searcher.execute(this.query);
};

ContextImageSearcher.prototype._resultCallback = function(searcher) {
	var re_tolweb = /tolweb/i;
	var re_user = /user/i;
	for ( var i in searcher.results ) {
		if ( searcher.results[i].originalContextUrl.match(re_tolweb) &&
			   ( ! searcher.results[i].originalContextUrl.match(re_user) )
		 ) {
			//log("Using image from "+searcher.results[i].originalContextUrl);
			this.imageElement.src = searcher.results[i].url;
			return;
		}
	}
};