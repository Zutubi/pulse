// setup the namespace for the zutubi components.
var ZUTUBI = window.ZUTUBI || {};

// if it is already defined, great, otherwise create it.
ZUTUBI.widget = ZUTUBI.widget || {};

ZUTUBI.FloatManager = function() {
    var idByCategory = {};
    var displayedCategories = 0;
    var showTime = new Date();
    var initialised = false;

    function initialise()
    {
        Ext.getDoc().addKeyListener(27, function() {
            if(displayedCategories > 0)
            {
                hideAll();
            }
        });
    }

    function unpress(id)
    {
        var buttonEl = Ext.get(id + '_button');
        if(buttonEl)
        {
            buttonEl.removeClass('popdown-pressed');
        }
    }

    function press(id)
    {
        var buttonEl = Ext.get(id + '_button');
        if(buttonEl)
        {
            buttonEl.addClass('popdown-pressed');
        }
    }

    function onMouseDown(e)
    {
        if(showTime.getElapsed() > 50 && displayedCategories > 0 && !e.getTarget(".floating-widget"))
        {
            hideAll();
        }
    }

    function hideAll()
    {
        for (var category in idByCategory)
        {
            unpress(idByCategory[category]);
            Ext.get(getWindowId(category)).setDisplayed(false);
        }

        idByCategory = {};
        displayedCategories = 0;
        Ext.getDoc().un('mousedown', onMouseDown);
    }

    function getWindowId(category)
    {
        return category + '-window';
    }

    return {
        showHideFloat: function(category, id, align)
        {
            if(!initialised)
            {
                initialise();
            }
            
            showTime = new Date();

            var windowId = getWindowId(category);
            var windowEl = Ext.get(windowId);
            var displayedId = idByCategory[category];
            if(windowEl && displayedId == id)
            {
                unpress(id);
                windowEl.setDisplayed(false);
                delete idByCategory[category];
                if (--displayedCategories == 0)
                {
                    Ext.getDoc().un('mousedown', onMouseDown);
                }
            }
            else
            {
                var contentId = category + '-window-content';
                if(!windowEl)
                {
                    windowEl = Ext.DomHelper.append(document.body, '<div id="' + windowId + '" class="floating floating-widget" style="display: none;"><div id="' + contentId + '"></div></div>', true);
                }
                else if(windowEl.isDisplayed())
                {
                    unpress(displayedId);
                }

                idByCategory[category] = id;
                if (++displayedCategories == 1)
                {
                    Ext.getDoc().on('mousedown', onMouseDown);
                }
                
                getElement(contentId).innerHTML = getElement(id).innerHTML;

                press(id);
                if (!windowEl.isDisplayed())
                {
                    windowEl.setDisplayed(true);
                }

                var linkEl = Ext.get(id + "_link");
                windowEl.anchorTo(linkEl, align);
            }
        }
    };
}();

/**
 * Function to show or hide a small floating window.  Used to popup full
 * comments from summaries and lists of build results.  Note that it is
 * critical that the element being popped up is a direct child of the
 * document body (Ext.anchorTo requires this).
 *
 * The category is used to differentiate popups of different purposes.  If
 * the user requests a popup of category X, and a popup is already showing of
 * the same category, then this latter popup will be reused and moved.
 */
function showHideFloat(category, id, align)
{
    if (!align)
    {
        align = 'tr-br?';
    }

    ZUTUBI.FloatManager.showHideFloat(category, id, align);
}

/**
 * @class ZUTUBI.Select
 * @extends Ext.form.Field
 * Basic select box.  Supports multi-select, which is not available in Ext
 * currently.
 * @constructor
 * Creates a new TextField
 * @param {Object} config Configuration options
 */
ZUTUBI.Select = function(config)
{
    ZUTUBI.Select.superclass.constructor.call(this, config);
    this.addEvents({
        'change': true
    });
    this.hiddenName = config.name;
};

Ext.extend(ZUTUBI.Select, Ext.form.Field, {
    /**
     * @cfg {Ext.data.Store} store The data defining select items.
     * selections.
     */
    store: undefined,
    /**
     * @cfg {Boolean} multiple True if this field should allow multiple
     * selections.
     */
    multiple: false,
    /**
     * @cfg {Number} size The size of the select element.
     */
    size : undefined,
    /**
     * @cfg {String} displayField The store field to use as item text.
     */
    displayField : 'text',
    /**
     * @cfg {String} valueField The store field to use as item values.
     */
    valueField : 'value',
    hiddenFields: {},
    entryHeight: 22,

    onRender : function(ct, position)
    {
        this.el = ct.createChild({tag: 'div', cls: 'x-select', id: this.id});
        if(!this.tpl){
            if(Ext.isIE || Ext.isIE7)
            {
                this.tpl = '<tpl for="."><div unselectable="on" class="x-select-item" tabindex="-1">{' + this.displayField + '}</div></tpl>';
            }
            else
            {
                this.tpl = '<tpl for="."><div class="x-select-item  x-unselectable" tabindex="-1">{' + this.displayField + '}</div></tpl>';
            }
        }

        this.view = new Ext.DataView({
            renderTo: this.el,
            tpl: this.tpl,
            itemSelector: '.x-select-item',
            multiSelect :this.multiple,
            store: this.store,
            selectedClass: 'x-select-selected'
        });

        var count = this.store.getCount();
        if(!this.size)
        {
            if(count < 3)
            {
                this.size = 3;
            }
            else if(count > 10)
            {
                this.size = 10;
            }
            else
            {
                this.size = count;
            }
        }

        if(count > 0)
        {
            this.entryHeight = this.el.child("div.x-select-item").getHeight() + 2;
        }

        this.el.setHeight(this.entryHeight * this.size);
        this.view.on('selectionchange', this.onSelectionChange, this);
    },

    initValue: function()
    {
        this.setValue(this.value);
        this.originalValue = this.getValue();
    },

    onSelectionChange: function()
    {
        this.updateHiddenFields();
        this.fireEvent('change');
    },

    updateHiddenFields: function()
    {
        var value = this.getValue();
        var valueMap = {};

        for(var i = 0; i < value.length; i++)
        {
            var iv = value[i];
            valueMap[iv] = true;
            if(!this.hiddenFields[iv])
            {
                this.addHiddenField(iv);
            }
        }

        for(var key in this.hiddenFields)
        {
            if(!valueMap[key])
            {
                this.hiddenFields[key].remove();
                delete this.hiddenFields[key];
            }
        }
    },

    addHiddenField: function(value)
    {
        this.hiddenFields[value] = this.el.createChild({tag: 'input', type: 'hidden', name: this.name, value: value});        
    },

    findRecord : function(prop, value)
    {
        var record;
        if(this.store.getCount() > 0)
        {
            this.store.each(function(r) {
                if(r.data[prop] == value)
                {
                    record = r;
                    return false;
                }
            });
        }
        return record;
    },

    getValue: function()
    {
        var value = [];
        var selections = this.view.getSelectedIndexes();
        for(var i = 0; i < selections.length; i++)
        {
            value.push(this.store.getAt(selections[i]).get(this.valueField));
        }

        // Ordering does not matter, and always sorting helps simplify
        // dirty-checking.
        value.sort();
        return value;
    },

    setValue: function(value)
    {
        this.view.clearSelections();
        for(var key in this.hiddenFields)
        {
            this.hiddenFields[key].remove();
        }
        this.hiddenFields = {};
        
        for(var i = 0; i < value.length; i++)
        {
            var record = this.findRecord(this.valueField, value[i]);
            if (record)
            {
                this.view.select(this.store.indexOf(record), true, true);
                this.addHiddenField(value[i]);
            }
        }
    },

    onDisable: function()
    {
        ZUTUBI.Select.superclass.onDisable.call(this);
        this.view.disabled = true;
    },

    onEnable: function()
    {
        ZUTUBI.Select.superclass.onEnable.call(this);
        this.view.disabled = false;
    }
});

ZUTUBI.ConfigTreeLoader = function(base)
{
    ZUTUBI.ConfigTreeLoader.superclass.constructor.call(this, {
        dataUrl: base,
        requestMethod: 'get'
    });
};

Ext.extend(ZUTUBI.ConfigTreeLoader, Ext.tree.TreeLoader, {
    getNodeURL : function(node)
    {
        var tree = node.getOwnerTree();
        var path = tree.toConfigPathPrefix(node.getPath('baseName'));
        return this.dataUrl + '/' + encodeURIPath(path);
    },

    requestData : function(node, callback)
    {
        if(this.fireEvent("beforeload", this, node, callback))
        {
            var params = this.getParams(node);
            var cb = {
                success: this.handleResponse,
                failure: this.handleFailure,
                scope: this,
                argument: {callback: callback, node: node}
            };

            this.transId = Ext.lib.Ajax.request(this.requestMethod, this.getNodeURL(node) + '?ls', cb, params);
        }
        else
        {
            // if the load is cancelled, make sure we notify
            // the node that we are done
            if(typeof callback == "function")
            {
                callback();
            }
        }
    }
});


ZUTUBI.ConfigTree = function(config)
{
    ZUTUBI.ConfigTree.superclass.constructor.call(this, config);
    this.dead = false;
};

Ext.extend(ZUTUBI.ConfigTree, Ext.tree.TreePanel, {
    toTreePathPrefix: function(configPath)
    {
        var treePath = configPath;

        if (this.pathPrefix && treePath.indexOf(this.pathPrefix) == 0)
        {
            treePath = treePath.substring(this.pathPrefix.length);
        }

        if(treePath.length > 0 && treePath.substring(0, 1) != '/')
        {
            treePath = '/' + treePath;
        }

        if (!this.rootVisible)
        {
            treePath = this.pathSeparator + this.root.attributes.baseName + treePath;
        }

        if (treePath.substring(0, 1) == '/')
        {
            treePath = treePath.substring(1);
        }

        return treePath;
    },

    toConfigPathPrefix: function(treePath)
    {
        var configPath = treePath;
        
        if (!this.rootVisible)
        {
            configPath = configPath.substring(this.root.attributes.baseName.length + 1);
        }

        if(configPath.length > 0 && configPath.substring(0, 1) == '/')
        {
            configPath = configPath.substring(1);    
        }

        if (this.pathPrefix)
        {
            configPath = this.pathPrefix + this.pathSeparator + configPath;
        }

        return configPath;
    },

    selectConfigPath: function(configPath)
    {
        this.getSelectionModel().clearSelections();
        this.expandToPath(configPath, function(found, node) {
            if (found)
            {
                node.select();
            }
        });
    },

    expandToPath : function(path, callback)
    {
        path = this.toTreePathPrefix(path);
        var keys = path.split(this.pathSeparator);
        var current = this.root;
        if (current.attributes['baseName'] != keys[0])
        {
            if(callback)
            {
                callback(false, null);
            }
            return;
        }

        var index = 0;
        var skippedLast = false;
        var f = function() {
            if (++index == keys.length)
            {
                if (callback)
                {
                    callback(true, current);
                }
                return;
            }

            if (!skippedLast && current.attributes.extraAttributes && current.attributes.extraAttributes.collapsedCollection)
            {
                skippedLast = true;
                f();
            }
            else
            {
                skippedLast = false;
                current.expand(false, false, function() {
                    var c = current.findChild('baseName', keys[index]);
                    if (!c)
                    {
                        if(callback)
                        {
                            callback(false, current);
                        }
                        return;
                    }

                    current = c;
                    f();
                });
            }
        };

        f();
    },

    getNodeConfigPath: function(node)
    {
        var p = node.parentNode;
        var b = [node.attributes['baseName']];
        while (p)
        {
            if (p.attributes.extraAttributes && p.attributes.extraAttributes.collapsedCollection)
            {
                b.unshift(p.attributes.extraAttributes.collapsedCollection);
            }
            b.unshift(p.attributes['baseName']);
            p = p.parentNode;
        }

        return this.toConfigPathPrefix('/' + b.join('/'));
    },

    getNodeByConfigPath: function(configPath)
    {
        var path = this.toTreePathPrefix(configPath);
        var keys = path.split(this.pathSeparator);
        var current = this.root;
        if (current.attributes['baseName'] != keys[0])
        {
            return null;
        }

        var skippedLast = false;
        for(var i = 1; current && i < keys.length; i++)
        {
            if (!skippedLast && current.attributes.extraAttributes && current.attributes.extraAttributes.collapsedCollection)
            {
                skippedLast = true;
            }
            else
            {
                skippedLast = false;
                current = current.findChild('baseName', keys[i]);
            }
        }
        return current;
    },

    handleResponse: function(response)
    {
        var tree = this;

        if(response.addedFiles)
        {
            each(response.addedFiles, function(addition) { tree.addNode(addition.parentPath, {baseName: addition.baseName, text: addition.displayName, iconCls: addition.iconCls, leaf: addition.leaf, extraAttributes: {collapsedCollection: addition.collapsedCollection}}); });
        }

        if(response.renamedPaths)
        {
            each(response.renamedPaths, function(rename) { tree.renameNode(rename.oldPath, rename.newName, rename.newDisplayName, rename.collapsedCollection); });
        }

        if(response.removedPaths)
        {
            each(response.removedPaths, function(path) { tree.removeNode(path); });
        }
    },

    redirectToNewPath: function(response)
    {
        if (response.newPath)
        {
            this.selectConfigPath(response.newPath);
        }
    },

    addNode: function(parentPath, config)
    {
        if (parentPath)
        {
            var parentNode = this.getNodeByConfigPath(parentPath);
            if (parentNode)
            {
                var newNode = this.getLoader().createNode(config);
                parentNode.leaf = false;
                parentNode.appendChild(newNode);
            }
        }
    },

    renameNode: function(oldPath, newName, newDisplayName, collapsedCollection)
    {
        if(oldPath)
        {
            var node = this.getNodeByConfigPath(oldPath);
            if(node)
            {
                node.attributes.baseName = newName;
                node.setText(newDisplayName);
                if (!node.attributes.extraAttributes)
                {
                    node.attributes.extraAttributes = {};
                }

                node.attributes.extraAttributes.collapsedCollection = collapsedCollection;
            }
        }
    },

    removeNode: function(path)
    {
        if (path)
        {
            var node = this.getNodeByConfigPath(path);
            if (node)
            {
                if(node.isRoot)
                {
                    this.dead = true;
                }
                else
                {
                    node.parentNode.removeChild(node);
                }
            }
        }
    }
});


ZUTUBI.TemplateTree = function(scope, config)
{
    this.scope = scope;
    this.dead = false;
    ZUTUBI.TemplateTree.superclass.constructor.call(this, config);
};

Ext.extend(ZUTUBI.TemplateTree, ZUTUBI.ConfigTree, {
    handleResponse: function(response)
    {
        var tree = this;

        if (response.addedFiles)
        {
            each(response.addedFiles, function(addition) {
                if (addition.parentTemplatePath && addition.parentPath == tree.scope)
                {
                    tree.addNode(addition.parentTemplatePath, { baseName: addition.baseName, text: addition.displayName, iconCls: addition.iconCls, leaf: addition.templateLeaf});
                }
            });
        }

        if (response.renamedPaths)
        {
            each(response.renamedPaths, function(rename) { tree.renameNode(tree.translatePath(rename.oldPath), rename.newName, rename.newDisplayName); });
        }

        if (response.removedPaths)
        {
            each(response.removedPaths, function(path) { tree.removeNode(tree.translatePath(path)); } );
        }
    },

    redirectToNewPath: function(response)
    {
        if (response.newTemplatePath)
        {
            this.selectConfigPath(response.newTemplatePath);
        }
    },

    findNodeByAttribute: function(attribute, value, node)
    {
        node = node || this.root;
        if (node.attributes[attribute] == value)
        {
            return node;
        }

        var cs = node.childNodes;
        for(var i = 0, len = cs.length; i < len; i++)
        {
            var found = this.findNodeByAttribute(attribute, value, cs[i]);
            if (found)
            {
                return found;
            }
        }

        return null;
    },

    translatePath: function(path)
    {
        var pieces = path.split(this.pathSeparator);
        if (pieces.length == 2 && pieces[0] == this.scope)
        {
            var baseName = pieces[1];
            var node = this.findNodeByAttribute('baseName', baseName);
            if (node)
            {
                return this.getNodeConfigPath(node);
            }
        }
        
        return null;
    }
});

ZUTUBI.FSTreeLoader = function(base)
{
    ZUTUBI.FSTreeLoader.superclass.constructor.call(this, {
        dataUrl: base + '/ajax/xls.action'
    });
};

Ext.extend(ZUTUBI.FSTreeLoader, Ext.tree.TreeLoader, {
    getParams : function(node)
    {
        var buf = [];
        var bp = this.baseParams;
        for (var key in bp)
        {
            if (typeof bp[key] != "function")
            {
                buf.push(encodeURIComponent(key), "=", encodeURIComponent(bp[key]), "&");
            }
        }
        buf.push("path=", encodeURIComponent(node.getPath("baseName")));
        return buf.join("");
    }
});


ZUTUBI.Form = function(config)
{
    ZUTUBI.Form.superclass.constructor.call(this, null, config);
};

Ext.extend(ZUTUBI.Form, Ext.form.BasicForm, {
    clearInvalid: function()
    {
        ZUTUBI.Form.superclass.clearInvalid.call(this);
        var statusEl = Ext.get(this.formName + '.status');
        statusEl.update('');
    },

    handleActionErrors: function(errors)
    {
        var statusEl = Ext.get(this.formName + '.status');
        statusEl.update('');

        if(errors && errors.length > 0)
        {
            var listEl = statusEl.createChild({tag: 'ul', cls: 'validation-error'});
            for(var i = 0; i < errors.length; i++)
            {
                listEl.createChild({tag: 'li', html: errors[i]});
            }
        }
    }
});

ZUTUBI.FormLayout = function(config)
{
    if(!config.fieldTpl)
    {
        config.fieldTpl = new Ext.Template('<tr id="x-form-row-{0}" class="x-form-row {5}">' +
                                        '<td class="x-form-label"><label for="{0}" style="{2}">{1}</td>' +
                                        '<td class="x-form-label-annotation" id="x-form-label-annotation-{0}"></td>' +
                                        '<td class="x-form-separator">{4}</td>' +
                                        '<td><div id="x-form-el-{0}" class="x-form-element" style="{3}">' +
                                        '</div><div class="{6}"></div></td>' +
                                    '</tr>');
        config.fieldTpl.disableFormats = true;
        config.fieldTpl.compile();
    }

    config.labelAlign = 'right';
    ZUTUBI.FormLayout.superclass.constructor.call(this, config);
};

Ext.extend(ZUTUBI.FormLayout, Ext.layout.FormLayout, {
    renderItem: function(c, position, target)
    {
        if(c && !c.rendered && c.isFormField && c.inputType == 'hidden')
        {
            target = target.up('form');
        }

        ZUTUBI.FormLayout.superclass.renderItem.call(this, c, position, target);
    },

    setContainer: function(ct)
    {
        ZUTUBI.FormLayout.superclass.setContainer.call(this, ct);
        // Forcibly override the behaviour of the default layout (adds
        // padding to the element).
        this.elementStyle = '';
    }
});

ZUTUBI.FormPanel = function(config)
{
    config.layout = new ZUTUBI.FormLayout({});
    ZUTUBI.FormPanel.superclass.constructor.call(this, config);
};

Ext.extend(ZUTUBI.FormPanel, Ext.form.FormPanel, {
    displayMode: false,

    createForm: function(){
        delete this.initialConfig.listeners;
        return new ZUTUBI.Form(this.initialConfig);
    },

    onRender : function(ct, position){
        ZUTUBI.FormPanel.superclass.onRender.call(this, ct, position);
        this.layoutTarget = this.form.el.createChild({tag: 'table', cls: 'x-form'}).createChild({tag: 'tbody'});
    },

    getLayoutTarget: function()
    {
        return this.layoutTarget;
    },
    
    add : function()
    {
        var a = arguments;
        for(var i = 0, len = a.length; i < len; i++)
        {
            a[i].form = this;
        }
        
        ZUTUBI.FormPanel.superclass.add.apply(this, a);
        return this;
    },

    markRequired: function(id, tooltip)
    {
        var cellEl = Ext.get('x-form-label-annotation-' + id);
        var spanEl = cellEl.createChild({tag: 'span', cls: 'required', id: id + '.required', html: '*'});
        if(tooltip)
        {
            spanEl.dom.qtip = tooltip;
        }
    },

    enableField: function(id)
    {
        var field = this.findById(id);
        if(field)
        {
            field.enable();

            var rowEl = this.getFieldRowEl(id);
            if (rowEl)
            {
                Ext.get(rowEl).removeClass('x-item-disabled');

                var actionDomEls = this.getFieldActionDomEls(id);
                if (actionDomEls)
                {
                    for(var i = 0; i < actionDomEls.length; i++)
                    {
                        Ext.get(actionDomEls[i]).removeClass('x-item-disabled');
                    }
                }
            }
        }
    },

    disableField: function(id)
    {
        var field = this.findById(id);
        if(field)
        {
            field.clearInvalid();
            field.disable();

            var rowEl = this.getFieldRowEl(id);
            if (rowEl)
            {
                Ext.get(rowEl).addClass('x-item-disabled');

                var actionDomEls = this.getFieldActionDomEls(id);
                if (actionDomEls)
                {
                    for(var i = 0; i < actionDomEls.length; i++)
                    {
                        Ext.get(actionDomEls[i]).addClass('x-item-disabled');
                    }
                }
            }
        }
    },

    getFieldActionDomEls: function(id)
    {
        var rowEl = this.getFieldRowEl(id);
        return Ext.query("a[class*='field-action']", rowEl.dom);
    },

    getFieldRowEl: function(id)
    {
        return Ext.get('x-form-row-' + id);
    },

    annotateField: function(id, annotationName, imageName, tooltip)
    {
        var rowEl = this.getFieldRowEl(id);
        var cellEl = rowEl.createChild({tag: 'td', cls: 'x-form-annotation'});
        var imageEl = cellEl.createChild({tag: 'img', src: imageName, id: id + '.' + annotationName});
        if(tooltip)
        {
            imageEl.dom.qtip = tooltip;
        }

        return imageEl;
    },

    updateButtons: function()
    {
        if(this.displayMode)
        {
            var dirty = this.form.isDirty();
            if(!dirty)
            {
                this.form.clearInvalid();
            }

            for(var i = 0; i < this.buttons.length; i++)
            {
                if(dirty)
                {
                    this.buttons[i].enable();
                }
                else
                {
                    this.buttons[i].disable();
                }
            }
        }
    },

    submitForm: function (value)
    {
        var f = this.getForm();
        Ext.get(this.formName + '.submitField').dom.value = value;
        if(value == 'cancel')
        {
            Ext.DomHelper.append(f.el.parent(), {tag: 'input', type: 'hidden', name: 'cancel', value: 'true'});
        }

        f.clearInvalid();
        if (this.ajax)
        {
            window.formSubmitting = true;
            f.submit({
                clientValidation: false,
                waitMsg: 'Submitting...'
            });
        }
        else
        {
            if(value == 'cancel' || f.isValid())
            {
                f.el.dom.submit();
            }
        }
    },

    defaultSubmit: function()
    {
        if (!this.readOnly)
        {
            this.submitForm(this.defaultSubmitValue);
        }
    },

    handleFieldKeypress: function (evt)
    {
        if (evt.getKey() != evt.RETURN || this.readOnly)
        {
            return true;
        }
        else
        {
            this.defaultSubmit();
            evt.preventDefault();
            return false;
        }
    },

    attachFieldKeyHandlers: function()
    {
        var panel = this;
        var form = this.getForm();
        form.items.each(function(field) {
            var el = field.getEl();
            if(el)
            {
                if (field.getXType() == 'checkbox')
                {
                    el = field.innerWrap;
                }

                el.set({tabindex: window.nextTabindex++ });

                if (field.submitOnEnter)
                {
                    el.on('keypress', function(event){ return panel.handleFieldKeypress(event); });
                }
                el.on('keyup', panel.updateButtons.createDelegate(panel));
                el.on('click', panel.updateButtons.createDelegate(panel));
            }
        });
    }
});

ZUTUBI.CheckFormPanel = function(mainFormPanel, options)
{
    this.mainFormPanel = mainFormPanel;
    ZUTUBI.CheckFormPanel.superclass.constructor.call(this, options);
};

Ext.extend(ZUTUBI.CheckFormPanel, ZUTUBI.FormPanel, {
    createForm: function() {
        delete this.initialConfig.listeners;
        return new ZUTUBI.CheckForm(this.mainFormPanel.getForm(), this.initialConfig);
    },

    defaultSubmit: function()
    {
        var f = this.getForm();
        if (f.isValid())
        {
            f.clearInvalid();
            window.formSubmitting = true;
            f.submit({
                clientValidation: false
            });
        }
    }
});

ZUTUBI.CheckForm = function(mainForm, options)
{
    ZUTUBI.CheckForm.superclass.constructor.call(this, options);
    this.mainForm = mainForm;
};

Ext.extend(ZUTUBI.CheckForm, ZUTUBI.Form, {
    isValid: function()
    {
        // Call both, they have side-effects.
        var mainValid = this.mainForm.isValid();
        var valid = ZUTUBI.CheckForm.superclass.isValid.call(this);
        return mainValid && valid;
    },

    markInvalid: function(errors)
    {
        for(var i = 0; i < errors.length; i++)
        {
            var fieldError = errors[i];
            var id = fieldError.id;
            var field;

            if(id.lastIndexOf('_check') == id.length - 6)
            {
                field = this.mainForm.findField(id.substr(0, id.length - 6));
            }
            else
            {
                field = this.findField(id);
            }
            
            if(field)
            {
                field.markInvalid(fieldError.msg);
            }
        }
    },

    submit: function(options)
    {
        var params = options.params || {};
        var mainParams = this.mainForm.getValues(false);

        for(var param in mainParams)
        {
           params[param + '_check'] = mainParams[param];
        }

        options.params = params;
        ZUTUBI.CheckForm.superclass.submit.call(this, options);
    }
});

ZUTUBI.ImageButton = function(renderTo, config)
{
    ZUTUBI.ImageButton.superclass.constructor.call(this, config);
    this.addEvents({
        'click': true,
        'mouseover': true,
        'mouseout': true,
        'mousedown': true
    });

    this.defaultAutoCreate = { tag: 'img', src: this.image, cls: 'x-image-button' };
    this.render(renderTo);
};

Ext.extend(ZUTUBI.ImageButton, Ext.BoxComponent, {
    onRender: function(ct, position)
    {
        ZUTUBI.ImageButton.superclass.onRender.call(this, ct, position);
        this.el = ct.createChild(this.getAutoCreate(), position);
    },

    afterRender: function()
    {
        this.el.on('click', this.onClick,  this);
        this.el.on("mouseover", this.onMouseOver, this);
        this.el.on("mouseout", this.onMouseOut, this);
        this.el.on("mousedown", this.onMouseDown, this);
    },

    onClick: function(evt)
    {
        this.fireEvent('click', evt);
    },

    onMouseOver : function(e){
        if(!this.disabled){
            this.el.set({src: this.overImage});
            this.fireEvent('mouseover', this, e);
        }
    },

    onMouseOut : function(e){
        if(!e.within(this.el,  true)){
            this.el.set({src: this.image});
            this.fireEvent('mouseout', this, e);
        }
    },

    onMouseDown : function(e){
        if(!this.disabled && e.button == 0){
            this.el.set({src: this.downImage});
            Ext.get(document).on('mouseup', this.onMouseUp, this);
        }
    },

    onMouseUp : function(e){
        if(e.button == 0){
            this.el.set({src: this.image});
            Ext.get(document).un('mouseup', this.onMouseUp, this);
        }
    }
});


ZUTUBI.ItemPicker = function(config)
{
    ZUTUBI.ItemPicker.superclass.constructor.call(this, config);
    this.addEvents({
        'change': true
    });
    this.hiddenName = config.name;
};

Ext.extend(ZUTUBI.ItemPicker, Ext.form.Field, {
    width: 100,
    height: 100,
    displayField: 'text',
    valueField: 'value',
    selectedClass: 'x-item-picker-selected',
    hiddenFields: [],
    value: [],
    optionStore: undefined,
    defaultAutoCreate: {tag: 'div', cls: 'x-item-picker-list', tabindex: '0'},

    onRender: function(ct, position)
    {
        ZUTUBI.ItemPicker.superclass.onRender.call(this, ct, position);
        this.wrap = this.el.wrap({tag: 'div', cls: 'x-item-picker'});
        this.wrap.on('click', this.onClick, this);
        // Allow width for buttons
        this.wrap.setWidth(this.width + 24);
        
        this.el.setWidth(this.width);
        this.el.setHeight(this.height);
        this.el.on('focus', this.onInputFocus, this);
        this.el.on('blur', this.onInputBlur, this);

        if(this.optionStore)
        {
            var fieldConfig = {
                store: this.optionStore,
                mode: 'local',
                forceSelection: true,
                name: 'combo.' + this.name,
                displayField: this.displayField,
                valueField: this.valueField,
                triggerAction: 'all',
                width: this.width,
                id: this.id + '.choice'
            };

            this.combo = new Ext.form.ComboBox(fieldConfig);
            this.combo.render(this.wrap);
            this.choice = this.combo.getEl();
            this.alignEl = this.combo.wrap;
        }
        else
        {
            this.input = this.wrap.createChild({tag: 'input', type: 'text', cls: 'x-form-text', id: this.id + '.choice'});
            this.input.setWidth(this.width);
            this.choice = this.input;
            this.alignEl = this.input;
        }

        this.choice.on('focus', this.onInputFocus, this);
        this.choice.on('blur', this.onInputBlur, this);

        this.addButton = new ZUTUBI.ImageButton(this.wrap, {image: window.baseUrl + '/images/buttons/sb-add-up.gif', overImage: window.baseUrl + '/images/buttons/sb-add-over.gif', downImage: window.baseUrl + '/images/buttons/sb-add-down.gif'});
        this.addButton.on('click', this.onAdd, this);

        this.nav = new Ext.KeyNav(this.input || this.el, {
            "up": function(evt)
            {
                this.navUp(evt.ctrlKey);
            },

            "down": function(evt)
            {
                this.navDown(evt.ctrlKey);
            },

            "enter": function(evt)
            {
                if(this.input)
                {
                    this.onAdd(evt);
                }
            },

            scope: this
        });

        this.removeButton = new ZUTUBI.ImageButton(this.wrap, {image: window.baseUrl + '/images/buttons/sb-delete-up.gif', overImage: window.baseUrl + '/images/buttons/sb-delete-over.gif', downImage: window.baseUrl + '/images/buttons/sb-delete-down.gif'});
        this.removeButton.on('click', this.onRemove, this);

        this.upButton = new ZUTUBI.ImageButton(this.wrap, {image: window.baseUrl + '/images/buttons/sb-up-up.gif', overImage: window.baseUrl + '/images/buttons/sb-up-over.gif', downImage: window.baseUrl + '/images/buttons/sb-up-down.gif'});
        this.upButton.on('click', this.onUp, this);

        this.downButton = new ZUTUBI.ImageButton(this.wrap, {image: window.baseUrl + '/images/buttons/sb-down-up.gif', overImage: window.baseUrl + '/images/buttons/sb-down-over.gif', downImage: window.baseUrl + '/images/buttons/sb-down-down.gif'});
        this.downButton.on('click', this.onDown, this);

        var cls = 'x-item-picker';

        if(!this.tpl)
        {
            if(Ext.isIE || Ext.isIE7)
            {
                this.tpl = '<tpl for="."><div unselectable="on" class="' + cls + '-item">{' + this.displayField + '}</div></tpl>';
            }
            else
            {
                this.tpl = '<tpl for="."><div class="' + cls + '-item  x-unselectable">{' + this.displayField + '}</div></tpl>';
            }
        }

        this.view = new Ext.DataView({
            renderTo: this.el,
            tpl: this.tpl,
            itemSelector: '.x-item-picker-item',
            singleSelect: true,
            store: this.store,
            selectedClass: this.selectedClass
        });

        this.ValueRecord = Ext.data.Record.create({name: 'text'}, {name: 'value'});
    },

    afterRender: function()
    {
        ZUTUBI.ItemPicker.superclass.afterRender.call(this);
        this.alignButtons();
        if(this.value)
        {
            this.setValue(this.value);
            this.originalValue = this.value;
        }
    },

    onPosition: function()
    {
        this.alignButtons();
    },

    onResize: function()
    {
        this.alignButtons();
    },

    onClick: function()
    {
        if (!this.disabled && this.input)
        {
            this.input.focus();
        }
    },

    onInputFocus: function()
    {
        this.choice.addClass('x-form-focus');
        this.el.addClass('x-form-focus');
    },

    onInputBlur: function()
    {
        this.choice.removeClass('x-form-focus');
        this.el.removeClass('x-form-focus');
    },

    alignButtons: function()
    {
        if(Ext.isIE)
        {
            this.alignEl.alignTo(this.el, 'tl-bl');
        }
        this.addButton.getEl().alignTo(this.el, 'tl-br', [2, 0]);
        this.removeButton.getEl().alignTo(this.el, 'tl-tr', [2, 0]);
        this.upButton.getEl().alignTo(this.el, 'l-r', [2, 0]);
        this.downButton.getEl().alignTo(this.upButton.getEl(), 't-b', [0, 2]);
    },

    navUp: function(ctrl)
    {
        if(ctrl)
        {
            this.onUp();
        }
        else
        {
            var selected = this.getSelection();
            if(selected > 0)
            {
                this.view.select(selected - 1);
            }
        }

        this.ensureSelectionVisible();
    },

    navDown: function(ctrl)
    {
        if(ctrl)
        {
            this.onDown();
        }
        else
        {
            var selected = this.getSelection();
            if(selected >= 0 && selected < this.store.getCount() - 1)
            {
                this.view.select(selected + 1);
            }
        }

        this.ensureSelectionVisible();
    },

    onAdd: function(evt)
    {
        var text = '';
        var value;

        if(this.input)
        {
            text = this.input.dom.value;
            this.input.dom.value = '';
            value = text;
        }
        else
        {
            var selectedIndexes = this.combo.view.getSelectedIndexes();
            if(selectedIndexes.length > 0)
            {
                var record = this.combo.store.getAt(selectedIndexes[0]);
                if(record)
                {
                    text = record.get(this.displayField);
                    value = record.get(this.valueField);
                }
            }
        }

        if(text.length > 0)
        {
            this.appendItem(text, value);
            this.view.select(this.store.getCount() - 1);
            this.ensureSelectionVisible();
            this.fireEvent('change', this, evt);
        }
    },

    appendItem: function(text, value)
    {
        this.store.add(new this.ValueRecord({text: text, value: value}));
        this.hiddenFields.push(this.wrap.createChild({tag: 'input', type: 'hidden', name: this.name, value: value}));
    },
    
    onRemove: function(evt)
    {
        var selected = this.getSelection();
        if(selected >= 0)
        {
            this.store.remove(this.store.getAt(selected));
            this.hiddenFields[selected].remove();
            this.hiddenFields.splice(selected, 1);
            this.fireEvent('change', this, evt);
        }
    },

    onUp: function(evt)
    {
        var selected = this.getSelection();
        if(selected > 0)
        {
            var record = this.store.getAt(selected);
            this.store.remove(record);
            this.store.insert(selected - 1, record);
            this.view.select(selected - 1);

            var hidden = this.hiddenFields.splice(selected, 1)[0];
            this.hiddenFields.splice(selected - 1, 0, hidden);
            hidden.insertBefore(this.hiddenFields[selected]);

            this.ensureSelectionVisible();
            this.fireEvent('change', this, evt);
        }
    },

    onDown: function(evt)
    {
        var selected = this.getSelection();
        if(selected >= 0 && selected < this.store.getCount() - 1)
        {
            var record = this.store.getAt(selected);
            this.store.remove(record);
            this.store.insert(selected + 1, record);
            this.view.select(selected + 1);

            var hidden = this.hiddenFields.splice(selected, 1)[0];
            this.hiddenFields.splice(selected + 1, 0, hidden);
            hidden.insertAfter(this.hiddenFields[selected]);

            this.ensureSelectionVisible();
            this.fireEvent('change', this, evt);
        }
    },

    ensureSelectionVisible: function()
    {
        var nodes = this.view.getSelectedNodes();
        if(nodes.length > 0)
        {
            var selectedEl = Ext.get(nodes[0]);
            selectedEl.scrollIntoView(this.el);
        }
    },

    getValue: function()
    {
        var value = [];
        this.store.each(function(r) { value.push(r.get('value')); });
        return value;
    },

    setValue: function(value)
    {
        if(!this.ValueRecord)
        {
            // Superclass onRender calls before we are ready.
            return;
        }

        this.store.removeAll();
        var i;
        for(i = 0; i < this.hiddenFields.length; i++)
        {
            this.hiddenFields[i].remove();
        }
        this.hiddenFields = [];
        
        for(i = 0; i < value.length; i++)
        {
            var text = this.getTextForValue(value[i]);
            if(text)
            {
                this.appendItem(text, value[i]);
            }
        }
    },

    getTextForValue: function(value)
    {
        if(this.input)
        {
            return value;
        }
        else
        {
            var record = this.optionStore.data.find(function(r) { return r.get(this.valueField) == value; }, this);
            if(record)
            {
                return record.get(this.displayField);
            }
            else
            {
                return null;
            }
        }
    },

    getSelection: function()
    {
        var selections = this.view.getSelectedIndexes();
        if(selections.length > 0)
        {
            return selections[0];
        }
        else
        {
            return -1;
        }
    },

    onDisable: function()
    {
        ZUTUBI.Select.superclass.onDisable.call(this);
        if(this.input)
        {
            this.input.dom.disabled = true;
        }
        else
        {
            this.combo.disable();
        }

        this.addButton.disable();
        this.removeButton.disable();
        this.upButton.disable();
        this.downButton.disable();
        this.view.disabled = true;
    },

    onEnable: function()
    {
        ZUTUBI.Select.superclass.onEnable.call(this);
        if(this.input)
        {
            this.input.dom.disabled = false;
        }
        else
        {
            this.combo.enable();
        }

        this.addButton.enable();
        this.removeButton.enable();
        this.upButton.enable();
        this.downButton.enable();
        this.view.disabled = false;
    }    
});

ZUTUBI.DetailPanel = function(config)
{
    ZUTUBI.DetailPanel.superclass.constructor.call(this, config);
};

Ext.extend(ZUTUBI.DetailPanel, Ext.Panel, {
    helpPath: "",
    helpType: "",

    initComponent: function()
    {
        Ext.apply(this, {
            layout: 'fit',
            id: 'detail-panel',
            contentEl: 'center',
            border: false,
            autoScroll: true,
            bodyStyle: 'padding: 10px'
        });

        ZUTUBI.DetailPanel.superclass.initComponent.call(this);
    },

    clearHelp: function()
    {
        this.helpPath = '';
        this.helpType = '';
    },

    getHelp: function()
    {
        return {path: this.helpPath, type: this.helpType};
    },

    setHelp: function(path, type)
    {
        this.helpPath = path;
        this.helpType = type || '';
    },
    
    load: function(o)
    {
        this.clearHelp();
        this.body.load(o);
    },

    update: function(html)
    {
        this.clearHelp();
        this.body.update(html, true);
    }
});

ZUTUBI.HelpPanel = function(config)
{
    ZUTUBI.HelpPanel.superclass.constructor.call(this, config);
};

Ext.extend(ZUTUBI.HelpPanel, Ext.Panel, {
    shownPath: "",
    shownType: "",
    syncOnExpand: true,

    initComponent: function()
    {
        Ext.apply(this, {
            tbar:  [{
                icon: window.baseUrl + '/images/arrow_left_right.gif',
                cls: 'x-btn-icon',
                tooltip: 'synchronise help',
                onClick: this.synchronise.createDelegate(this)
            }, '-', {
                icon: window.baseUrl + '/images/expand.gif',
                cls: 'x-btn-icon',
                tooltip: 'expand all',
                onClick: this.expandAll.createDelegate(this)
            }, {
                icon: window.baseUrl + '/images/collapse.gif',
                cls: 'x-btn-icon',
                tooltip: 'collapse all',
                onClick: this.collapseAll.createDelegate(this)
            }, '->', {
                icon: window.baseUrl + '/images/close.gif',
                cls: 'x-btn-icon',
                tooltip: 'hide help',
                onClick: this.collapse.createDelegate(this)
            }]
        });

        ZUTUBI.HelpPanel.superclass.initComponent.call(this);

        this.on('expand', this.expanded.createDelegate(this));
    },

    expanded: function()
    {
        if (this.syncOnExpand)
        {
            this.syncOnExpand = false;
            this.synchronise();
        }
    },
    
    synchronise: function(field)
    {
        var location = detailPanel.getHelp();
        this.showHelp(location.path, location.type, field);
    },

    showHelp: function(path, type, field)
    {
        if(this.collapsed)
        {
            this.syncOnExpand = false;
            this.expand(false);
        }

        this.loadPath(path, type, this.gotoField.createDelegate(this, [field]));
    },

    loadPath: function(path, type, cb)
    {
        if(!path)
        {
            path = '';
        }

        if(!type)
        {
            type = '';
        }

        if(path != this.shownPath || type != this.shownType || type == 'wizard')
        {
            if(path)
            {
                var panel = this;
                this.body.load({
                    url: window.baseUrl + '/ahelp/' + path + '?' + type + '=',
                    scripts: true,
                    callback: function() {
                        panel.shownPath = path;
                        panel.shownType = type;
                        var helpEl = Ext.get('config-help');
                        var fieldHeaders = helpEl.select('.field-expandable .field-header', true);
                        fieldHeaders.on('click', function(e, el) {
                            var expandableEl = Ext.fly(el).parent('.field-expandable');
                            if(expandableEl)
                            {
                                expandableEl.toggleClass('field-expanded');
                            }
                        });

                        fieldHeaders.addClassOnOver('field-highlighted');

                        if(cb)
                        {
                            cb();
                        }
                    }
                });
            }
            else
            {
                this.body.update('No help available.', false, cb);                
            }
        }
        else
        {
            if(cb)
            {
                cb();
            }
        }
    },

    gotoField: function(field)
    {
        if(field)
        {
            var rowEl = Ext.get('field-row-' + field);
            if(rowEl)
            {
                if(rowEl.hasClass('field-expandable'))
                {
                    this.expandField(rowEl);
                }

                var top = (rowEl.getOffsetsTo(this.body)[1]) + this.body.dom.scrollTop;
                this.body.scrollTo('top', top - 10);
                rowEl.highlight();
            }
        }
    },

    expandField: function(el)
    {
        el.addClass('field-expanded');
    },

    expandAll: function()
    {
        this.expandField(this.selectExpandableFields());
    },

    collapseField: function(el)
    {
        el.removeClass('field-expanded');
    },

    collapseAll: function()
    {
        this.collapseField(this.selectExpandableFields());
    },

    selectExpandableFields: function()
    {
        return this.body.select('.field-expandable');
    }
});

Ext.reg('xzhelppanel', ZUTUBI.HelpPanel);

Ext.form.Checkbox.prototype.onResize = function()
{
    Ext.form.Checkbox.superclass.onResize.apply(this, arguments);
};

// Bug fix lifted directly from:
// http://extjs.com/forum/showthread.php?t=45982
Ext.override(Ext.form.ComboBox, {
    initEvents : function()
    {
        Ext.form.ComboBox.superclass.initEvents.call(this);
        this.keyNav = new Ext.KeyNav(this.el, {
            "up" : function(e)
            {
                this.inKeyMode = true;
                this.selectPrev();
            },
            "down" : function(e)
            {
                if (!this.isExpanded())
                {
                    this.onTriggerClick();
                }
                else
                {
                    this.inKeyMode = true;
                    this.selectNext();
                }
            },
            "enter" : function(e)
            {
                this.onViewClick();
                this.delayedCheck = true;
                this.unsetDelayCheck.defer(10, this);
            },
            "esc" : function(e)
            {
                this.collapse();
            },
            "tab" : function(e)
            {
                this.onViewClick(false);
                return true;
            },
            scope : this,
            doRelay : function(foo, bar, hname)
            {
                if (hname == 'down' || this.scope.isExpanded())
                {
                    return Ext.KeyNav.prototype.doRelay.apply(this, arguments);
                }
                return true;
            },
            forceKeyDown : true
        });
        this.queryDelay = Math.max(this.queryDelay || 10,
                this.mode == 'local' ? 10 : 250);
        this.dqTask = new Ext.util.DelayedTask(this.initQuery, this);
        if (this.typeAhead)
        {
            this.taTask = new Ext.util.DelayedTask(this.onTypeAhead, this);
        }
        if ((this.editable !== false) && !this.enableKeyEvents)
        {
            this.el.on("keyup", this.onKeyUp, this);
        }
        if (this.forceSelection)
        {
            this.on('blur', this.doForce, this);
        }
    },
    
    onKeyUp : function(e)
    {
        if (this.editable !== false && !e.isSpecialKey())
        {
            this.lastKey = e.getKey();
            this.dqTask.delay(this.queryDelay);
        }
        Ext.form.ComboBox.superclass.onKeyUp.call(this, e);
    }
});

ZUTUBI.ProjectModel = function(key) {
    this.key = key;
    this.collapsed = false;
    this.hidden = false;
    this.children = [];
    this.rowCount = 1;
};

ZUTUBI.ProjectModel.prototype = {
    addChild: function(child) {
        this.children.push(child);
    },

    getEl: function() {
        return Ext.get(this.key);
    },

    toggle: function() {
        if(this.collapsed)
        {
            this.expand();
        }
        else
        {
            this.collapse();
        }
    },
    
    collapse: function() {
        if(!this.collapsed)
        {
            this.hideDescendents();
            this.collapsed = true;
            this.getEl().addClass('project-collapsed');
        }
    },

    setRowDisplay: function(display) {
        Ext.get(this.key).setStyle('display', display);
        if(this.rowCount > 1)
        {
            for(var i = 2; i <= this.rowCount; i++)
            {
                Ext.get('b' + i + '.' + this.key).setStyle('display', display);
            }
        }
    },

    hide: function() {
        if(!this.hidden)
        {
            this.setRowDisplay('none');
            this.hidden = true;
        }

        if(!this.collapsed)
        {
            this.hideDescendents();
        }
    },

    hideDescendents: function() {
        for(var i = 0; i < this.children.length; i++)
        {
            var child = this.children[i];
            child.hide();
        }
    },

    expand: function() {
        if(this.collapsed)
        {
            this.showDescendents();
            this.collapsed = false;
            this.getEl().removeClass('project-collapsed');
        }
    },

    show: function() {
        if(this.hidden)
        {
            this.setRowDisplay('');
            this.hidden = false;
        }

        if(!this.collapsed)
        {
            this.showDescendents();
        }
    },

    showDescendents: function() {
        for(var i = 0; i < this.children.length; i++)
        {
            var child = this.children[i];
            child.show();
        }
    }
};
