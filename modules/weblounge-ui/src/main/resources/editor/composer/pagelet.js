steal.plugins('jqueryui/dialog',
		'jquery/event/hover',
		'jqueryui/draggable',
		'jqueryui/droppable',
		'jqueryui/resizable',
		'jqueryui/mouse',
        'scripts/bootstrap')
.models('../../models/workbench',
		'../../models/pagelet')
.resources('trimpath-template')
.then('inputconverter')
.then(function($) {

  $.Controller("Editor.Pagelet",
  /* @static */
  {
  },

  /* @prototype */
  {
    /**
     * Initialize a new Pagelet controller.
     */
    init: function(el) {
		this.element.attr('index', this.element.index());
		this.showHover = true;
		$.Hover.delay = 10;
    },
    
    update: function(options) {
    	if(options === undefined) return;
    	this.element.attr('index', this.element.index());
    	this.options.composer = options.composer;
    	this.showHover = true;
    },
    
    enable: function() {
    	this.showHover = true;
    },
    
    disable: function() {
    	this.showHover = false;
    },
    
    _openPageEditor: function(pageletEditor, isNew) {
    	// Parse Pagelet-Editor Data
    	var pagelet = this.options.composer.page.getEditorPagelet(this.options.composer.id, this.element.index(), this.options.composer.language);
    	var editor = $(pageletEditor).find('editor:first');
    	var pageletName = $(pageletEditor).find('pageleteditor:first').attr('name');
    	
    	if(!editor.length) {
    		this._showRenderer();
    		return;
    	}
    	
    	// Process Editor Template
    	var result = this._processTemplate(editor.text(), pagelet);
    	if(result == null) return;
    	
    	// Load Pagelet CSS
    	$(pageletEditor).find('link').each(function(index) {
    		var headLink = $('head link[href="' + $(this).attr('href') + '"][rel="stylesheet"]');
    		if(headLink.length > 0) return;
    		$("head").append("<link>");
    	    var css = $("head").children(":last");
    	    css.attr({
    	      rel:  "stylesheet",
    	      type: $(this).attr('type'),
    	      href: $(this).attr('href')
    	    });
    	});
    	
    	// Load Pagelet Javascript
    	this.scriptIndex = 0;
    	var scripts = $(pageletEditor).find('script');
    	scripts.each($.proxy(function(index, script) {
    		$.getScript($(script).attr('src'), this.callback('_openEditor', scripts.length));
    	}, this));
    	
    	// Hack to create new Dom element
    	var resultDom = $('<div></div>').html(result);
    	this._convertInputs(resultDom, pagelet);
    	
		this.editorDialog = $('#wbl-pageleteditor').html('<form id="wbl-validate" onsubmit="return false;">' + resultDom.html() + '</form>')
		.dialog({
			title: pageletName,
			width: 500,
			modal: true,
			autoOpen: false,
			closeOnEscape: false,
			resizable: true,
			buttons: {
				Abbrechen: function() {
					$("body").css({ overflow: 'visible' });
					$(this).dialog('close');
				},
				OK: $.proxy(function () {
					this.editorDialog.find("form#wbl-validate").submit();
					if(!this.editorDialog.find("form#wbl-validate").valid()) {
                        steal.dev.warn("The dialog contains invalid fields!");
                        // Show error msg and prevent dialog from closing
                        $('.ui-dialog-buttonpane p.error').removeClass('hidden');
                        return;
                    };
					var newPagelet = this._updatePageletValues(pagelet);
					
					// Save New Pagelet and show Renderer
					this.options.composer.page.insertPagelet(newPagelet, this.options.composer.id, this.element.index());
					this._showRenderer(newPagelet);
					
					$("body").css({ overflow: 'visible' });
					this.editorDialog.dialog('destroy');
				}, this)
			},
			close: $.proxy(function () {
				if(isNew == true) {
					this._deletePagelet();
				}
                //remove error msg
                $('.ui-dialog-buttonpane p.error').remove();
				this.editorDialog.dialog('destroy');
			}, this),
			open: $.proxy(function(event, ui) {
				this.editorDialog.trigger('pageletEditorOpen', {
					language: this.options.composer.language,
					runtime: this.options.composer.runtime
				});
                // Disable submit button and show error msg if form is not valid
                $('form#wbl-validate').bind('change keyup', function() {
                    if($(this).validate().checkForm()) {
                        $('button.primary').button( "option", "disabled", false);
                        $('.ui-dialog-buttonpane p.error').addClass('hidden');
                    } else {
                        $('button.primary').button( "option", "disabled", true);
                        $('.ui-dialog-buttonpane p.error').removeClass('hidden');
                    }
                });
                // Disable submit button on load
                if($('form#wbl-validate').validate().checkForm()) {
                    $('button.primary').button( "option", "disabled", false);
                } else {
                    $('button.primary').button( "option", "disabled", true);
                }

			}, this),
			create: function(event, ui) {
				$("body").css({ overflow: 'hidden' });
                $('.ui-dialog-buttonpane').append('<p class="error hidden">Invalid fields</p>').find('.ui-button:first').addClass('danger').end().find('.ui-button:last').addClass('primary');
                $('#tabs a').click(function (e) {
                    e.preventDefault();
                    $(this).tab('show');
                });
                $('#tabs a:first').tab('show');
			}
		});
        // Open the dialog
		this.editorDialog.find("form#wbl-validate").validate();
		
		if(scripts.length < 1) {
			this.editorDialog.dialog('open');
		}
    },
    
    _openEditor: function(size) {
    	this.scriptIndex++;
		if(size == this.scriptIndex) {
			this.editorDialog.dialog('open');
		}
    },
    
    /**
     * Process a TrimPath template
     */
    _processTemplate: function(template, data) {
    	var result = {};
    	try {
    		var templateObject = TrimPath.parseTemplate(template.trim());
    		result = templateObject.process(data);
    	} catch(err) {
    		result.exception = err;
    	} finally {
    		if($.isEmptyObject(result.exception)) return result;
    		alert('TrimPath Process Error: ' + result.exception.message);
    		return null;
    	}
    },
    
    /**
     * Delete this Pagelet and update Composer
     */
    _deletePagelet: function() {
		this.options.composer.page.deletePagelet(this.options.composer.id, this.element.index());
		var composer = this.element.parent();
		this.element.remove();
		composer.editor_composer();
    },
    
    /**
     * Converts editor input fields with Trimpath syntax
     */
    _convertInputs: function(editor, pagelet) {
    	$(editor).find(':input').each(function(index) {
    		if(this.tagName == 'BUTTON') return;
    		var name = $(this).attr('name');
    		if(name == undefined) {
    			alert('Bad editor!');
    			return false;
    		}
    		var element = name.split(':')
    		
    		// Get the index of the current field
    		var index = $(editor).find(':input[name="' + name + '"]').index(this);
    		
    		if($(this).attr('type') == 'text' || $(this).attr('type') == 'hidden') {
    			InputConverter.convertText($(this), element, pagelet, index);
    		}
    		else if($(this).attr('type') == 'checkbox') {
    			InputConverter.convertCheckbox($(this), element, pagelet);
    		}
    		else if($(this).attr('type') == 'radio') {
    			InputConverter.convertRadio($(this), element, pagelet);
    		}
    		else if(this.tagName == 'TEXTAREA') {
    			InputConverter.convertTextarea($(this), element, pagelet, index);
    		}
    		else if(this.tagName == 'SELECT') {
    			InputConverter.convertSelect($(this), element, pagelet);
    		}
    	});
    },
    
    /**
     * Update the pagelet values from the dialog inputs and set the "current"
     */
    _updatePageletValues: function(pagelet) {
		var allInputs = this.editorDialog.find(':input');
		
		if(pagelet.locale == undefined || pagelet.locale.current == undefined) {
			pagelet = this._createNewLocale(pagelet, this.options.composer.language);
		} else {
			pagelet.locale.current.text = {};
			pagelet.properties.property = {};
			this._updateModified(pagelet);
		}
		
		$.each(allInputs, function(i, input) {
			var element = input.name.split(':')
    		if(input.type == 'select-one' || input.type == 'select-multiple') {
    			var optionArray = new Array();
    			$(input).find('option:selected').each(function(){
    				optionArray.push($(this).val());
    			});
    			if(element[0] == 'property') {
    				pagelet.properties.property[element[1]] = optionArray;
    			} 
    			else if(element[0] == 'element') {
    				pagelet.locale.current.text[element[1]] = optionArray;
    			}
    		}
    		else if(input.type == 'checkbox') {
    			if(element[0] == 'property') {
					pagelet.properties.property[element[1]] = input.checked ? ["true"] : ["false"];
    			} 
    			else if(element[0] == 'element') {
					pagelet.locale.current.text[element[1]] = input.checked ? ["true"] : ["false"];
    			}
    		}
    		else if(input.type == 'radio') {
    			if(input.checked == false) return;
    			if(element[0] == 'property') {
    				pagelet.properties.property[element[1]] = [input.value];
    			} 
    			else if(element[0] == 'element') {
    				pagelet.locale.current.text[element[1]] = [input.value];
    			}
    		}
    		else {
    			if(element[0] == 'property') {
    				if($.isEmptyObject(pagelet.properties.property[element[1]]))
    					pagelet.properties.property[element[1]] = new Array();
    				pagelet.properties.property[element[1]].push(input.value);
    			} 
    			else if(element[0] == 'element') {
    				if($.isEmptyObject(pagelet.locale.current.text[element[1]]))
    					pagelet.locale.current.text[element[1]] = new Array();
    				pagelet.locale.current.text[element[1]].push(input.value);
    			}
    		}
		});
		return pagelet;
    },
    
    _showRenderer: function(pagelet) {
		Workbench.getRenderer({
			id: this.options.composer.page.value.id,
			composer: this.options.composer.id,
			language: this.options.composer.language,
			pageletId: this.element.index(),
			pagelet: pagelet
		}, $.proxy(function(renderer) {
			this.element.html(renderer);
		}, this));
    },
    
    /**
     * Create a new locale
     */
    _createNewLocale: function(pagelet, language) {
    	if (pagelet.locale == undefined)
    		pagelet.locale = [];
    	pagelet.locale.current = {
    		text: {},
    		language: language,
    		original: $.isEmptyObject(pagelet.locale.original),
    		modified: {
				user: {
					id: this.options.composer.runtime.getUserLogin(),
					name: this.options.composer.runtime.getUserName(),
					realm: this.options.composer.runtime.getId()
				},
				date: new Date()
			}
    	};
    	pagelet.locale.push(pagelet.locale.current);
		return pagelet;
    },
    
    /**
     * Update the locale modification info
     */
    _updateModified: function(pagelet) {
		pagelet.locale.current.modified = {
			user: {
				id: this.options.composer.runtime.getUserLogin(),
				name: this.options.composer.runtime.getUserName(),
				realm: this.options.composer.runtime.getId()
			},
			date: new Date()
		};
    },
    
	'hoverenter': function(el, ev) {
		if(!this.showHover) return;
		//if(!el.hasClass('wbl-noEditor')) {
        //    this.element.prepend('<i class="icon-pencil" title="Edit pagelet"></i>');
		//}
        //this.element.prepend('<i class="icon-trash" title="Delete pagelet"></i>');
        if(!el.hasClass('wbl-noEditor')) {
            this.element.prepend('<div class="wbl-editing-palet"><i class="icon-trash" title="Delete pagelet"></i><i class="icon-pencil" title="Edit pagelet"></i></div>');
        } else {
            this.element.prepend('<div class="wbl-editing-palet"><i class="icon-trash" title="Delete pagelet"></i></div>');
        }

    },
    
	'hoverleave': function(el, ev) {
		if(!this.showHover) return;
        //this.element.closest('.pagelet').find('i.icon-pencil').remove();
        //this.element.closest('.pagelet').find('i.icon-trash').remove();
        this.element.closest('.pagelet').find('div.wbl-editing-palet').remove();
    },

    'i.icon-pencil click': function(el, ev) {
		Workbench.getPageletEditor({
			id: this.options.composer.page.value.id, 
			composer: this.options.composer.id, 
			language: this.options.composer.language,
			pagelet: this.element.index() 
		}, this.callback('_openPageEditor'));
	},
	
    'i.icon-trash click': function(el, ev) {    
    	this._deletePagelet();
    }

  });

});
