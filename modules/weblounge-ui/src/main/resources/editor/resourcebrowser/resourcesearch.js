steal.plugins('jquery/view/tmpl', 'jquery/event/key', 'jquery/controller', 'jqueryui/effects', 'editor/mediaeditor')
.views('//editor/resourcebrowser/views/resourcepagessearch.tmpl', '//editor/resourcebrowser/views/resourcemediasearch.tmpl')
.resources('fileuploader')
.models('../../models/file')
.css('resources/fileuploader')
.then(function($) {

	$.Controller('Editor.Resourcesearch', 
	{	
		init: function(el) {
			if(this.options.resourceType == 'pages') {
				$(el).html('//editor/resourcebrowser/views/resourcepagessearch.tmpl', {runtime: this.options.runtime});
			}
			else if(this.options.resourceType == 'media') {
				$(el).html('//editor/resourcebrowser/views/resourcemediasearch.tmpl', {});
				
				this.map = new Array();
				
				var uploader = new qq.FileUploader({
				    // pass the dom node (ex. $(selector)[0] for jQuery users)
				    element: document.getElementById('wbl-mediaFileUploader'),
				    params: {language: this.options.language},
					// validation    
					// ex. ['jpg', 'jpeg', 'png', 'gif'] or []
					allowedExtensions: [],        
					// each file size limit in bytes
					// this option isn't supported in all browsers
					sizeLimit: 0, // max size   
					minSizeLimit: 0, // min size
//					onCancel: this._cancel,
					onComplete: $.proxy(function(id, fileName, response) {
						if($.isEmptyObject(response)) return;
						
						// Save the uploaded file for Tagging not used now
						this.map[id] = {resourceId: response.url.substring(response.url.lastIndexOf('/') + 1), eTag: response.eTag};
						
						var pendingCircle = this.element.parent().parent().find("#pending-batch");
						var count = parseInt(pendingCircle.html());
						if(isNaN(count)) count = 0;
						pendingCircle.html(++count).fadeIn();
						var listItem = $(this.element.find('ul.qq-upload-list li#' + id));
						listItem.effect("transfer", { to: this.element.parent().parent().find("#pending-batch") }, 1000);
						listItem.delay(5000).fadeOut(400, function() {
						    $(this).remove();
						});
				    }, this),
				    // path to server-side upload script
				    action: '/system/weblounge/files/uploads'
				});
			}
			
			$("input#wbl-resourceSearch").keypress($.proxy(function(ev) {
				if(ev.key() == '\r') {
					ev.preventDefault();
					$(ev.target).trigger('searchResources', ev.target.value);
				}
			}, this));
		},
		
	    "img.wbl-addPageImg click": function(el, ev) {
	    	ev.stopPropagation();
			$('.wbl-menu').hide();
			$('#wbl-pagecreator').editor_pagecreator({language: this.options.language, runtime: this.options.runtime});
	    }
	    
	});

});
