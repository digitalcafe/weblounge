steal.plugins(
	'jquery/controller/view',
	'jquery/view/tmpl')
.views('//editor/resourcebrowser/views/init.tmpl')
.css('resourcebrowser')
.then('resourcescrollview', 'resourcelistview', 'resourcesearch')
.then(function($) {

  $.Controller('Editor.Resourcebrowser',
	{
		defaults: {
			resources: {},
			resourceType: 'pages'
		}
	},
	{
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/init.tmpl', {});
			this._initViewItems();
			this._showPendingNotification();
			this.searchFlag = true;
		},
		
		update: function(showSearchBox) {
			if(showSearchBox == true && !(this.editorSelectionMode == true)) {
				this.searchFlag = true;
				this.element.find('div.wbl-view').hide();
				this.searchBox.show();
				this.scrollView.hide();
				this.listView.hide();
			} 
			else if(this.searchFlag == false) {
				this.element.find('div.wbl-view').show();
				this.activeElement.show();
				this.searchBox.hide();
			}
			
			if(this.searchFlag == true) return;
			this._showPendingNotification();
			this.scrollView.editor_resourcescrollview({
				resources: this.options.resources,
				language: this.options.language,
				resourceType: this.options.resourceType,
				runtime: this.options.runtime
			});
			this.listView.editor_resourcelistview({
				resources: this.options.resources,
				language: this.options.language,
				resourceType: this.options.resourceType,
				runtime: this.options.runtime
			});
		},
		
		_initViewItems: function() {
			this.searchBox = this.find('div.wbl-searchBox').editor_resourcesearch({
				resourceType: this.options.resourceType,
				language: this.options.language, 
				runtime: this.options.runtime
			});
			this.scrollView = this.find('div.wbl-thumbnailView');
			this.listView = this.find('div.wbl-listView');
			this.activeElement = this.scrollView;
			$('nav.weblounge div.wbl-view').buttonset();
			$('nav.weblounge div.wbl-filter').buttonset();
			
			$('nav.weblounge button.wbl-list').button({
				icons: {primary: "wbl-iconList"},
				text: false });
			$('nav.weblounge button.wbl-tree').button({
				icons: {primary: "wbl-iconTree"},
				disabled: true,
				text: false });
			$('nav.weblounge button.wbl-thumbnails').button({
				disabled: false,
				icons: {primary: "wbl-iconThumbnails"},
				text: false });
			this.element.find('div.wbl-view').hide();
		},
		
		_enableEditorSelectionMode: function(isMultiSelect, preSelection, resourceMode) {
			if(!$.isEmptyObject(resourceMode)) {
				this.resourceMode = resourceMode;
			}
			
			this.lastParams = {preferredversion: 1};
			this.lastParams.filter = '';
			if (preSelection) {
				$.each(preSelection, $.proxy(function(i, id) {
					if(id == '') return;
					this.lastParams.filter += 'id:' + id + ' ';
				}, this));
			}
			
			this.lastQuery = {page: $.proxy(function(params) {
				Page.findAll(params, $.proxy(function(pages) {
					this.options.resources = pages;
					this.searchFlag = false;
					this._updateEditorSelectionMode(isMultiSelect, preSelection, this.lastParams.filter);
				}, this));
			}, this), media: $.proxy(function(params) {
				Editor.File.findAll(params, $.proxy(function(media) {
					this.options.resources = media;
					this.searchFlag = false;
					this._updateEditorSelectionMode(isMultiSelect, preSelection, this.lastParams.filter);
				}, this));
			}, this)};
			this._loadResources(this.lastParams, this.lastQuery);
		},
		
		_updateEditorSelectionMode: function(isMultiSelect, preSelection, filter) {
			this.activeElement.show();
			this.searchBox.hide();
			this.element.find('div.wbl-view').show();
			
			var mode = 'editorSelection';
			if(isMultiSelect) mode = 'editorMultiSelection';
			
			this.editorSelectionMode = true;
			this.scrollView.editor_resourcescrollview({
				resources: this.options.resources,
				language: this.options.language,
				resourceType: this.options.resourceType,
				runtime: this.options.runtime,
				mode: mode
			});
			this.listView.editor_resourcelistview({
				resources: this.options.resources,
				language: this.options.language,
				resourceType: this.options.resourceType,
				runtime: this.options.runtime,
				mode: mode
			});
			
			this.element.find('nav.wbl-icons button').hide();
			if(filter != '')
				this.element.find('nav.wbl-icons input#wbl-filter').val(filter);
			
			if(this.activeElement.is(this.scrollView)) {
				this.scrollView.editor_resourcescrollview('_selectResources', preSelection);
			} else {
				this.listView.editor_resourcelistview('_selectResources', preSelection);
			}
			
        	var selectedElements; 
			if(this.activeElement.hasClass('wbl-thumbnailView')) {
				selectedElements = this.find('div.wbl-scrollViewItem.wbl-marked');
			} else if(this.activeElement.hasClass('wbl-listView')) {
				selectedElements = this.find('tr.wbl-pageEntry input:checked').parents('tr.wbl-pageEntry');
			}
			
        	if(selectedElements.length > 0) {
        		$('button.wbl-editorSelectionOK').button('option', 'disabled', false);
        	} else {
        		$('button.wbl-editorSelectionOK').button('option', 'disabled', true);
        	}
		},
		
		_disableEditorSelectionMode: function() {
			this.editorSelectionMode = false;
			this.resourceMode = {};
			this.scrollView.editor_resourcescrollview({
				resources: this.options.resources,
				language: this.options.language,
				resourceType: this.options.resourceType,
				runtime: this.options.runtime,
				mode: 'normal'
			});
			this.listView.editor_resourcelistview({
				resources: this.options.resources,
				language: this.options.language,
				resourceType: this.options.resourceType,
				runtime: this.options.runtime,
				mode: 'normal'
			});
		},
		
		_getSelection: function(success) {
			if(this.activeElement.hasClass('wbl-thumbnailView')) {
				this.options.selectedResources = this.find('div.wbl-scrollViewItem.wbl-marked');
			} else if(this.activeElement.hasClass('wbl-listView')) {
				this.options.selectedResources = this.find('tr.wbl-pageEntry input:checked').parents('tr.wbl-pageEntry');
			}
			if(this.options.selectedResources.lenght < 1) {
				success(null);
			} else {
				var resources = new Array();
				this.options.selectedResources.each($.proxy(function(index, elem) {
					var resource = null;
					if(this.options.resourceType == 'pages') {
						resource = this._getPage($(elem).attr('id'));
					} else {
						resource = this._getFile($(elem).attr('id'));
					}
					if(resource == null) return;
					resources.push(resource);
				}, this));
				success(resources);
			}
		},
		
		_showPendingNotification: function() {
			var query = {page: $.proxy(function(params) {
				Page.findPending(params, $.proxy(function(pages) {
					if($.isEmptyObject(pages)) {
						this.element.find("#pending-batch").html('0').fadeOut();
					} else {
						this.element.find("#pending-batch").html(pages.length).fadeIn();
					}
				}, this));
			}, this), media: $.proxy(function(params) {
				Editor.File.findPending(params, $.proxy(function(media) {
					if($.isEmptyObject(media)) {
						this.element.find("#pending-batch").html('0').fadeOut();
					} else {
						this.element.find("#pending-batch").html(media.length).fadeIn();
					}
				}, this));
			}, this)};
			this._loadResources({}, query);
		},
		
		_showResourceScrollView: function(resources) {
			if(this.searchFlag == true) return;
			this.options.resources = resources;
			var element = this.find('div.wbl-thumbnailView');
			this._toggleElement(element);
			element.editor_resourcescrollview({
				resources: this.options.resources, 
				resourceType: this.options.resourceType,
				language: this.options.language, 
				runtime: this.options.runtime
			});
		},
		
		_showResourceListView: function(resources) {
			if(this.searchFlag == true) return;
			this.options.resources = resources;
			var element = this.find('div.wbl-listView');
			this._toggleElement(element);
			element.editor_resourcelistview({
				resources: this.options.resources,
				resourceType: this.options.resourceType,
				language: this.options.language, 
				runtime: this.options.runtime
			});
		},
		
		/**
		 * Remove deleted resource from resource array
		 */
        _removeResource: function(id) {
	    	var index = -1;
	    	$.each(this.options.resources, function(i, resources) {
	    		if(resources.id == id) {
	    			index = i;
	    			return false;
	    		};
    		});
	    	
	    	if(index == -1) return;
	    	
			this.options.resources.splice(index, 1);
			this.update();
        },
        
        /**
         * Get Page from the resourceId
         */
	    _getPage: function(id) {
	    	var page = null;
	    	$.each(this.options.resources, function(i, resource) {
	    		if(resource.id == id) {
	    			page = resource;
	    			return false;
	    		}
    		});
	    	if(page == null) return null;
	    	return new Page({value: page});
	    },
	    
	    /**
	     * Get File from the resourceId
	     */
	    _getFile: function(id) {
	    	var file = null;
	    	$.each(this.options.resources, function(i, resource) {
	    		if(resource.id == id) {
	    			file = resource;
	    			return false;
	    		}
	    	});
	    	if(file == null) return null;
	    	return new Editor.File({value: file});
	    },
		
		_toggleElement: function(el) {
        	this.activeElement.hide();
        	this.activeElement = el;
        	el.show();
        },
        
        _updateLast: function() {
        	this._loadResources(this.lastParams, this.lastQuery);
        },
        
		_showMessage: function(messageText) {
			$('.wbl-message').removeClass('wbl-error').addClass('wbl-success').css('visibility', 'visible').delay(3000).queue(function() {
				$(this).empty().css('visibility', 'hidden');
				$(this).dequeue();
			});
			$('.wbl-message').html(messageText);
		},
		
		_showErrorMessage: function(messageText) {
			$('.wbl-message').removeClass('wbl-success').addClass('wbl-error').css('visibility', 'visible').delay(3000).queue(function() {
				$(this).empty().css('visibility', 'hidden');
				$(this).dequeue();
			});
			$('.wbl-message').html(messageText);
		},
        
        _loadResources: function(params, functions) {
        	if(!$.isEmptyObject(this.resourceMode)) {
        		params.type = this.resourceMode;
        	} else {
        		delete params.type;
        	}
			switch(this.options.resourceType) {
			case 'pages':
				functions.page(params);
				break;
			case 'media':
				functions.media(params);
				break;
			}
        },
        
        /**
         * Unmark the scrollViewItems if you click outside of a item
         */
        "div click": function(el, ev) {
        	ev.stopPropagation();
        	if(!(el.is(this.element.find('div.wbl-scrollViewItem')) || el.is(this.element.find('div.wbl-imageContainer')))) {
        		this.element.find('div.wbl-scrollViewItem.wbl-marked').removeClass('wbl-marked');
        	}
        	
        	var selectedElements; 
			if(this.activeElement.hasClass('wbl-thumbnailView')) {
				selectedElements = this.find('div.wbl-scrollViewItem.wbl-marked');
			} else if(this.activeElement.hasClass('wbl-listView')) {
				selectedElements = this.find('tr.wbl-pageEntry input:checked').parents('tr.wbl-pageEntry');
			}
			
			// Enable or disable delete button
			if(selectedElements.length > 0) {
				$('button.wbl-delete').button("enable");
			} else {
				$('button.wbl-delete').button("disable");
			}
			
        	if(this.editorSelectionMode == true && selectedElements.length > 0) {
        		$('button.wbl-editorSelectionOK').button('option', 'disabled', false);
        	} else if(this.editorSelectionMode == true) {
        		$('button.wbl-editorSelectionOK').button('option', 'disabled', true);
        	}
        },
        
		"input searchResources": function(el, ev, searchValue) {
			if(searchValue == undefined) searchValue = '';
        	this.lastQuery = {page: $.proxy(function(params) {
        		Page.findBySearch(params, $.proxy(function(pages) {
        			this.options.resources = pages;
        			this.searchFlag = false;
        			this.update();
        		}, this));
        	}, this), media: $.proxy(function(params) {
    			Editor.File.findBySearch(params, $.proxy(function(pages) {
    				this.options.resources = pages;
    				this.searchFlag = false;
    				this.update();
    			}, this));
        	}, this)};
        	this.lastParams = {search: searchValue, preferredversion: 1};
        	this._loadResources(this.lastParams, this.lastQuery);
		},
		
		"input filterResources": function(el, ev, filterValue) {
			if(filterValue == undefined) filterValue = '';
			this.lastParams.filter = filterValue;
			this._loadResources(this.lastParams, this.lastQuery)
		},
		
		"button.wbl-list click": function(el, ev) {
			this._showResourceListView(this.options.resources);
		},
		
		"button.wbl-thumbnails click": function(el, ev) {
			this._showResourceScrollView(this.options.resources);
		},
        
        // Delete Pages
        "div#wbl-mainContainer deleteResources": function(el, ev, resources) {
        	switch(this.options.resourceType) {
        	case 'pages':
        		$(resources).each($.proxy(function(index, element) {
        			var page = this._getPage(element.id);
        			if(page == null) return;
        			var locked = page.isLocked();
                	var userLocked = page.isLockedUser(this.options.runtime.getUserLogin());
                	
                	if(page.getPath() == '/') {
                		this._showErrorMessage("Can't delete root page");
                	} else if(!locked || (locked && userLocked)) {
                		Page.destroy({id: element.id}, $.proxy(function() {
                			this._showMessage('Seite gel&ouml;scht!');
                			this._removeResource(element.id);
                			
                			// Relocate to Root if current page was deleted
                			if(page.getPath() == window.currentPagePath) {
                				location.href = window.currentLanguage + '/?edit&_=' + new Date().getTime();
                			}
                		}, this), $.proxy(function(jqXHR, textStatus, errorThrown) {
                			if(jqXHR.status == 412) {
                				this._showErrorMessage("Can't delete" + page.getPath() + ": Page has active referrers!");
                			} else {
                				this._showErrorMessage("Can't delete" + page.getPath() + ": " + errorThrown);
                			}
                		}, this));
                	} else {
                		this._showErrorMessage("Can't delete " + page.getPath() + ": Page is locked by " + page.getLockOwner() + "!");
                	}
        		}, this))
        		break;
        	case 'media':
        		$(resources).each($.proxy(function(index, element) {
        			Editor.File.destroy({id: element.id}, $.proxy(function() {
        				this._showMessage('Media gel&ouml;scht!');
        				this._removeResource(element.id);
        			}, this), $.proxy(function(jqXHR, textStatus, errorThrown) {
              			if(jqXHR.status == 412) {
            				this._showErrorMessage("Can't delete file: Media has active referrers!");
            			} else {
            				this._showErrorMessage("Can't delete file: " + errorThrown);
            			}
        			}, this));
        		}, this))
        		break;
        	}
        },
        
        // Duplicate Pages
        "div#wbl-mainContainer duplicateResources": function(el, ev, resources) {
        	// TODO
        	this._showMessage('Seite dupliziert!');
        	this.update();
        },
        
        "div#wbl-mainContainer showMessage": function(el, ev, message) {
        	this._showMessage(message);
        },
        
        "div#wbl-mainContainer showErrorMessage": function(el, ev, errorMessage) {
        	this._showErrorMessage(errorMessage);
        },
        
        // Favorize Pages
        "div#wbl-mainContainer favorizeResources": function(el, ev, resources) {
        	// TODO
			if(resources.length) {
				this._showMessage('Zu Favoriten hinzugef&uuml;gt');
			} else {
				this._showErrorMessage('Es wurde keine Seite markiert.');
			}
        	this.update();
        },
        
		"button.wbl-recent click": function(el, ev) {
			this.lastQuery = {page: $.proxy(function(params) {
				Page.findRecent(params, $.proxy(function(pages) {
					this.options.resources = pages;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this), media: $.proxy(function(params) {
				Editor.File.findRecent(params, $.proxy(function(media) {
					this.options.resources = media;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this)};
			this.lastParams = {preferredversion: 1};
			this._loadResources(this.lastParams, this.lastQuery);
		},
		
		"button.wbl-favorites click": function(el, ev) {
			// TODO
//			this.searchFlag = false;
			this.update();
		},
		
		"button.wbl-pending click": function(el, ev) {
			this.lastQuery = {page: $.proxy(function(params) {
				Page.findPending(params, $.proxy(function(pages) {
					this.options.resources = pages;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this), media: $.proxy(function(params) {
				Editor.File.findPending(params, $.proxy(function(media) {
					this.options.resources = media;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this)};
			this.lastParams = {};
			this._loadResources(this.lastParams, this.lastQuery);
		},
		
		"button.wbl-all click": function(el, ev) {
			this.lastQuery = {page: $.proxy(function(params) {
				Page.findAll(params, $.proxy(function(pages) {
					this.options.resources = pages;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this), media: $.proxy(function(params) {
				Editor.File.findAll(params, $.proxy(function(media) {
					this.options.resources = media;
					this.searchFlag = false;
					this.update();
				}, this));
			}, this)};
			this.lastParams = {preferredversion: 1};
			this._loadResources(this.lastParams, this.lastQuery);
		}
	});
});
