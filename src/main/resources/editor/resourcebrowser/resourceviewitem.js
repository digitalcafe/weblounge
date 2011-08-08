steal.plugins().then(function($) {

 /*
  * 
  */
  $.Controller('Editor.Resourceviewitem', {
    defaults: {
  		page: {}
    }
  },

  {

    init: function(el) {
    },
    
	"a.wbl-pagePath click": function(el, ev) {
		ev.preventDefault();
		el.trigger('openDesigner');
	},
	
	_openSettings: function(resourceItem) {
		switch(this.options.resourceType) {
		case 'pages':
			Page.findOne({id: resourceItem.attr('id')}, $.proxy(function(page) {
				// TODO Locking
				$('#wbl-pageheadeditor').editor_pageheadeditor({page: page, language: this.options.language, runtime: this.options.runtime});
			}, this));
			break;
		case 'media':
			var map = new Array({resourceId: resourceItem.attr('id')});
			$('div#wbl-tagger').editor_tagger({
				map: map, 
				language: this.options.language, 
				runtime: this.options.runtime,
				success: function() {
					resourceItem.parents('#wbl-mainContainer').trigger('updateLastMedia');
				}
			});
			break;
		}
	}

  });

});
