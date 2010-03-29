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

    getSelectedConfigPath: function(){
        var node = this.getSelectionModel().getSelectedNode();
        if (node)
        {
            return this.toConfigPathPrefix(node.getPath('baseName'));
        }
        return null;
    },

    getSelectedTreePath: function(){
        var node = this.tree.getSelectionModel().getSelectedNode();
        if (node)
        {
            return node.getPath('baseName');
        }
        return null;
    },

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

ZUTUBI.FSTreeLoader = function(config)
{
    var baseUrl = config.baseUrl;
    this.preloadDepth = config.preloadDepth || 0;
    
    ZUTUBI.FSTreeLoader.superclass.constructor.call(this, {
        dataUrl: baseUrl + '/ajax/xls.action',
        baseParams: config
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
        if (this.preloadDepth && node.getDepth() == 0)
        {
            buf.push("&depth=", this.preloadDepth);
        }
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
                listEl.createChild({tag: 'li', html: Ext.util.Format.htmlEncode(errors[i])});
            }
        }
    }
});

ZUTUBI.FormLayout = function(config)
{
    if(!config.fieldTpl)
    {
        config.fieldTpl = new Ext.Template('<tr id="x-form-row-{id}" class="x-form-row {itemCls}">' +
                                        '<td class="x-form-label"><label for="{id}" style="{labelStyle}">{label}{labelSeparator}</td>' +
                                        '<td class="x-form-label-annotation" id="x-form-label-annotation-{id}"></td>' +
                                        '<td class="x-form-separator">{4}</td>' +
                                        '<td><div id="x-form-el-{id}" class="x-form-element" style="{elementStyle}">' +
                                        '</div><div class="{clearCls}"></div></td>' +
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
        this.form.el.update('<table><tbody></tbody></table>');
        this.layoutTarget = this.form.el.first().first();
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
    height: 130,
    displayField: 'text',
    valueField: 'value',
    selectedClass: 'x-item-picker-selected',
    hiddenFields: [],
    allowReordering: true,
    value: [],
    optionStore: undefined,
    defaultAutoCreate: {tag: 'div', cls: 'x-item-picker', tabindex: '0', style:"outline:none;border:0px"},

    onRender: function(ct, position)
    {
        ZUTUBI.ItemPicker.superclass.onRender.call(this, ct, position);
        this.el.on('click', this.onClick, this);

        if(this.optionStore)
        {
            this.combo = new Ext.form.ComboBox({
                store: this.optionStore,
                mode: 'local',
                forceSelection: true,
                name: 'combo.' + this.name,
                displayField: this.displayField,
                valueField: this.valueField,
                triggerAction: 'all',
                id: this.id + '.choice'
            });
            this.choice = this.combo;
        }
        else
        {
            this.input = new Ext.form.TextField({
                tag: 'input',
                type: 'text',
                cls: 'x-form-text',
                id: this.id + '.choice'
            });
            this.choice = this.input;
        }

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
            cls: 'x-item-picker-list',
            tpl: this.tpl,
            itemSelector: '.x-item-picker-item',
            singleSelect: true,
            store: this.store,
            selectedClass: this.selectedClass
        });

        this.ValueRecord = Ext.data.Record.create({name: 'text'}, {name: 'value'});

        var icons = new Ext.Panel({
            border:false,
            header:false,
            layout:"vbox",
            padding:'1 0 0 0',
            layoutConfig:
            {
                align:"center"
            }
        });

        this.removeButton = new Ext.Button({
            icon : window.baseUrl + '/images/buttons/sb-delete-up.gif'
        });
        this.removeButton.on('click', this.onRemove, this);
        icons.add(this.removeButton);
        icons.add({xtype:'spacer',flex:1});

        if (this.allowReordering)
        {
            this.upButton = new Ext.Button({
                icon : window.baseUrl + '/images/buttons/sb-up-up.gif'
            });
            this.upButton.on('click', this.onUp, this);
            icons.add(this.upButton);
            icons.add({xtype:'spacer',flex:1});
            this.downButton = new Ext.Button({
                icon : window.baseUrl + '/images/buttons/sb-down-up.gif'
            });
            this.downButton.on('click', this.onDown, this);
            icons.add(this.downButton);
            icons.add({xtype:'spacer',flex:1});
        }

        this.addButton = new Ext.Button({
            icon : window.baseUrl + '/images/buttons/sb-add-up.gif'
        });
        this.addButton.on('click', this.onAdd, this);
        icons.add(this.addButton);

        this.view.flex = 1;
        var internalPanel = new Ext.Container({
            width:this.width - 22, // 22 is the width needed by the icons to display correctly.
            layout:"vbox",
            layoutConfig:{align:"stretch"},
            items:[this.view,this.choice]
        });

        this.panel = new Ext.Panel({
            border:false,
            height:this.height,
            layout:"hbox",
            layoutConfig:{align:"stretch"},
            items:[internalPanel,icons]
        });
        this.panel.render(this.el);

        this.nav = new Ext.KeyNav( (this.input) ? this.input.el : this.el, {
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
    },

    doLayout: function()
    {
        if(this.rendered)
        {
            this.panel.doLayout();
        }
    },

    afterRender: function()
    {
        ZUTUBI.ItemPicker.superclass.afterRender.call(this);
        if(this.value)
        {
            this.setValue(this.value);
            this.originalValue = this.value;
        }
    },

    onClick: function(evt)
    {
        if (!this.disabled && this.input)
        {
            this.input.focus();
        }
    },

    navUp: function(ctrl)
    {
        if(this.allowReordering && ctrl)
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
        if(this.allowReordering && ctrl)
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
            text = this.input.getValue();
            this.input.setValue('');
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
            var index = this.appendItem(text, value);
            this.view.select(index);
            this.ensureSelectionVisible();
            this.fireEvent('change', this, evt);
        }
    },

    appendItem: function(text, value)
    {
        var r = new this.ValueRecord({text: text, value: value});
        if (this.allowReordering)
        {
            this.store.add(r);
        }
        else
        {
            this.store.addSorted(r);
        }

        this.hiddenFields.push(this.el.createChild({tag: 'input', type: 'hidden', name: this.name, value: value}));
        return this.store.indexOf(r);
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

    /**
     * Clear / reset the values of the item picker to an empty list.
     */
    clear: function()
    {
        if(!this.ValueRecord)
        {
            // Superclass onRender calls before we are ready.
            return;
        }

        this.store.removeAll();
        for(i = 0; i < this.hiddenFields.length; i++)
        {
            this.hiddenFields[i].remove();
        }
        this.hiddenFields = [];
    },

    /**
     * Use the response data to define the options.
     * @param response  data to be used as options
     * @param append    if true, the data will be appended to the existing options.
     * If false, the existing options will be replaced.
     */
    loadOptions: function(response, append)
    {
        this.optionStore.loadData(response, append);
    },

    /**
     * Get a list of the current option keys.
     */
    getOptionValues: function()
    {
        var optionValues = [];
        this.optionStore.each(function(record)
        {
            optionValues.push(record.data.value);
        });
        return optionValues;
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
            this.input.disable();
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
            this.input.enable();
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
                    url: window.baseUrl + '/ahelp/' + encodeURIPath(path) + '?' + type + '=',
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

ZUTUBI.TailSettingsWindow = function(config)
{
    ZUTUBI.TailSettingsWindow.superclass.constructor.call(this, config);
};

Ext.extend(ZUTUBI.TailSettingsWindow, Ext.Window, {
    modal: true,
    title: 'tail view settings',
    closeAction: 'close',

    initComponent: function()
    {
        var tailWindow = this;
        this.form = new Ext.FormPanel({
            method: 'POST',
            labelWidth: 180,
            width: 255,
            labelAlign: 'right',
            bodyStyle: 'padding: 10px; background: transparent;',
            border: false,
            items: [{
                xtype: 'textfield',
                name: 'maxLines',
                id: 'settings-max-lines',
                fieldLabel: 'maximum lines to show',
                value: tailWindow.initialMaxLines,
                width: 50
            }, {
                xtype: 'textfield',
                name: 'refreshInterval',
                id: 'settings-refresh-interval',
                fieldLabel: 'refresh interval (seconds)',
                value: tailWindow.initialRefreshInterval,
                width: 50
            }],
            buttons: [{
                text: 'apply',
                handler: function() {
                    tailWindow.apply();
                }
            }, {
                text: 'cancel',
                handler: function() {
                    tailWindow.close();
                }
            }],
            listeners: {
                afterLayout: {
                    fn: function() {
                        new Ext.KeyNav(this.getForm().getEl(), {
                            'enter': function() {
                                tailWindow.apply();
                            },
                            scope: this
                        });
                    },
                    single: true
                }
            }
        });

        Ext.apply(this, {
            layout: 'form',
            autoHeight: true,
            items: [this.form],
            focus: function() {
                this.form.items.get(0).focus(true);
            }
        });

        ZUTUBI.TailSettingsWindow.superclass.initComponent.call(this);
    },

    apply: function()
    {
        var tailWindow = this;
        this.form.getForm().submit({
            clientValidation: true,
            url: window.baseUrl + '/ajax/saveTailSettings.action',
            success: function() {
                tailWindow.close();
                var mask = new Ext.LoadMask(Ext.getBody(), {msg:"Applying..."});
                mask.show();
                window.location.reload(true);
            },
            failure: function(form, action) {
                tailWindow.close();
                switch (action.failureType) {
                    case Ext.form.Action.CONNECT_FAILURE:
                        Ext.Msg.alert('Ajax communication failed.', 'failure');
                        break;
                    case Ext.form.Action.SERVER_INVALID:
                       Ext.Msg.alert('Server error.', 'failure');
               }
            }
        });
    }
});

/**
 * A tree browser window that supports navigation of the Pulse File System.
 *
 * @cfg fs              the file system to be used. Defaults to 'pulse'.
 * @cfg showFiles       if true, files will be shown.  Defaults to true.
 * @cfg showHidden      if true, hidden files will be shown.  Defaults to false.
 * @cfg autoExpandRoot  if true, the root node is automatically expanded.  Defaults to true.
 * @cfg baseUrl         the base url for communicating with the Pulse server.  All requests
 *                      to the server will be prefixed with this base url.  Defaults to window.baseUrl.
 * @cfg basePath        the base file system path from which browsing will begin.
 * @cfg prefix          the prefix path is applied after the base path, and is used to filter
 *                      any nodes that do not contain that prefix
 */
ZUTUBI.PulseFileSystemBrowser = Ext.extend(Ext.Window, {

    // window configuration defaults.
    id:'pulse-file-system-browser',
    layout:'fit',
    width:300,
    height:500,
    closeAction:'close',
    modal: true,
    plain: true,

    // tree configuration defaults
    fs: 'pulse',
    showFiles: true,
    showHidden: false,
    showRoot: false,
    autoExpandRoot: true,
    rootBaseName: '',

    basePath : '',

    defaultTreeConfig : {},

    initComponent: function() {

        ZUTUBI.PulseFileSystemBrowser.superclass.initComponent.apply(this, arguments);

        this.target = Ext.getCmp(this.target);

        var statusBar = new Ext.ux.StatusBar({
            defaultText:'',
            useDefaults:true
        });

        this.loader = new ZUTUBI.FSTreeLoader({
            baseUrl:this.baseUrl,
            fs:this.fs,
            basePath:this.basePath,
            showFiles:this.showFiles,
            showHidden:this.showHidden
        });

        this.loader.on('beforeload', function()
        {
            statusBar.setStatus({text: 'Loading...'});
        });
        this.loader.on('load', function()
        {
            statusBar.clearStatus();
        });
        this.loader.on('loadexception', function()
        {
            statusBar.setStatus({
                text: 'An error has occured',
                iconCls: 'x-status-error',
                clear: true
            });
        });

        this.tree = new ZUTUBI.ConfigTree(Ext.apply({
            loader: this.loader,
            layout: 'fit',
            border: false,
            animate: false,
            autoScroll: true,
            bbar: statusBar,
            rootVisible:this.showRoot,
            bodyStyle: 'padding: 10px'
        }, this.defaultTreeConfig));

        this.tree.setRootNode(new Ext.tree.AsyncTreeNode({
            baseName: this.rootBaseName,
            expanded: this.autoExpandRoot,
            allowDrag: false,
            allowDrop: false
        }));

        this.tree.on('afterlayout', this.showMask, this, {single:true});
        this.loader.on('load', this.hideMask, this, {single:true});
        this.loader.on('loadexception', this.hideMask, this, {single:true});

        this.add(this.tree);
    },

    showMask: function()
    {
        this.initialLoadingMask = new Ext.LoadMask(this.tree.getEl(), { msg: "Loading..." });
        this.initialLoadingMask.show();
    },

    hideMask: function() {
        this.initialLoadingMask.hide();
    },

    show: function() {
        ZUTUBI.PulseFileSystemBrowser.superclass.show.apply(this, arguments);

        if (this.target)
        {
            var initVal = this.target.getValue();
            if (initVal)
            {
                this.tree.selectConfigPath(initVal);
            }
        }
    }
});

/**
 * A PulseFileSystemBrowser with a toolbar and some buttons.
 */
ZUTUBI.LocalFileSystemBrowser = Ext.extend(ZUTUBI.PulseFileSystemBrowser, {

    initComponent: function() {

        this.fs = 'local';
        
        this.defaultTreeConfig = {
            tbar : new Ext.Toolbar()
        };

        ZUTUBI.LocalFileSystemBrowser.superclass.initComponent.apply(this, arguments);

        var toolbar = this.tree.getTopToolbar();
        var statusBar = this.tree.getBottomToolbar();

        var uhbtn = new ZUTUBI.SelectNodeButton({
            icon: this.baseUrl + '/images/house.gif',
            tooltip: 'go to user home',
            tree: this.tree
        });
        var rbtn = new ZUTUBI.ReloadSelectedNodeButton({
            icon: this.baseUrl + '/images/arrow_refresh.gif',
            tooltip: 'refresh folder',
            tree: this.tree
        });
        var afbtn = new ZUTUBI.CreateFolderButton({
            icon: this.baseUrl + '/images/folder_add.gif',
            tooltip: 'create new folder',
            baseUrl:this.baseUrl,
            basePath:this.basePath,
            tree: this.tree,
            sbar: statusBar
        });
        var dfbtn = new ZUTUBI.DeleteFolderButton({
            icon: this.baseUrl + '/images/folder_delete.gif',
            cls: 'x-btn-icon',
            tooltip: 'delete folder',
            baseUrl:this.baseUrl,
            basePath:this.basePath,
            tree: this.tree,
            sbar: statusBar
        });

        toolbar.add(uhbtn);
        toolbar.add('-');
        toolbar.add(afbtn);
        toolbar.add(dfbtn);
        toolbar.add(rbtn);

        Ext.Ajax.request({
            url: this.baseUrl + '/ajax/getHome.action',
            success: function(rspObj)
            {
                var data = Ext.util.JSON.decode(rspObj.responseText);
                uhbtn.setPath(data.userHome);
            },
            failure: function(rspObj)
            {
                statusBar.setStatus({
                    text: 'Failed to contact server.',
                    iconCls: 'x-status-error',
                    clear: true // auto-clear after a set interval
                });
            }
        });

        this.sbmtBtn = new Ext.Button({
            text:'ok',
            disabled:true,
            handler: function()
            {
                var node = this.tree.getSelectionModel().getSelectedNode();
                var p = node.getPath('baseName');
                if (!this.tree.rootVisible)
                {
                    p = p.substring(this.tree.root.attributes.baseName.length + 1);
                }

                if(p.length > 0 && p.substring(0, 1) == '/')
                {
                    p = p.substring(1);
                }

                this.target.setValue(p);
                // hax - this works because it is globally available.
                // Not worth trying change this at the moment.
                updateButtons();

                this.close();
            }.createDelegate(this)
        });

        this.clsBtn = new Ext.Button({
            text: 'cancel',
            handler: function()
            {
                this.close();
            }.createDelegate(this)
        });

        this.addButton(this.sbmtBtn);
        this.addButton(this.clsBtn);

        this.tree.getSelectionModel().on('selectionchange', this.onSelectionChange.createDelegate(this));
    },

    onSelectionChange: function(selectionModel, node) {
        if (node)
        {
            this.sbmtBtn.disabled && this.sbmtBtn.enable();
            node.ensureVisible();
        }
        else
        {
            this.sbmtBtn.disabled || this.sbmtBtn.disable();
        }
    }

});

ZUTUBI.WorkingCopyFileSystemBrowser = Ext.extend(ZUTUBI.PulseFileSystemBrowser, {

    initComponent: function() {

        this.defaultTreeConfig = {
            tbar : new Ext.Toolbar()
        };

        ZUTUBI.WorkingCopyFileSystemBrowser.superclass.initComponent.apply(this, arguments);

        var toolbar = this.tree.getTopToolbar();

        var rbtn = new ZUTUBI.ReloadSelectedNodeButton({
            icon: this.baseUrl + '/images/arrow_refresh.gif',
            tooltip: 'refresh',
            disabled: false,
            tree: this.tree
        });

        toolbar.add(rbtn);

        this.clsBtn = new Ext.Button({
            text: 'close',
            handler: function()
            {
                this.close();
            }.createDelegate(this)
        });

        this.addButton(this.clsBtn);
    }
});


/**
 * Button used to select a node in a tree.
 *
 * @cfg path        the tree path to be selected when this button is clicked.
 * @cfg tree        the tree in which the path will be selected.
 */
ZUTUBI.SelectNodeButton = Ext.extend(Ext.Button, {

    cls: 'x-btn-icon',

    initComponent : function() {
        if (this.path === undefined) {
            this.disabled = true;
        }

        ZUTUBI.SelectNodeButton.superclass.initComponent.apply(this, arguments);
    },
        
    onClick : function()
    {
        this.tree.selectConfigPath(this.path);
    },

    setPath: function(path){
        this.path = path;
        if (this.path !== undefined)
        {
            this.enable();
        }
        else
        {
            this.disable();
        }
    }
});

/**
 * @cfg tree    the tree in which the selected node (or root if no node is selected)
 *              will be reloaded.
 */
ZUTUBI.ReloadSelectedNodeButton = Ext.extend(Ext.Button, {

    cls: 'x-btn-icon',
    disabled: true,

    initComponent: function() {
        
        ZUTUBI.ReloadSelectedNodeButton.superclass.initComponent.apply(this, arguments);

        this.tree.getSelectionModel().on('selectionchange', this.onNodeSelectionChange.createDelegate(this));
    },

    onNodeSelectionChange: function(selectionModel, node) {
        if (this.canReload(node) && this.disabled)
        {
            this.enable();
        }
        else if (!this.canReload(node) && !this.disabled)
        {
            this.disable();
        }
    },

    canReload: function(node) {
        return !node || node.reload;
    },

    onClick : function()
    {
        if (!this.disabled)
        {
            var node = this.tree.getSelectionModel().getSelectedNode();
            if (node === null)
            {
                node = this.tree.getRootNode();
            }
            node.reload();
        }
    }
});

ZUTUBI.DeleteFolderButton = Ext.extend(Ext.Button, {

    initComponent : function() {
        if (this.path === undefined) {
            this.disabled = true;
        }

        ZUTUBI.SelectNodeButton.superclass.initComponent.apply(this, arguments);

        this.tree.getSelectionModel().on('selectionchange', this.onNodeSelectionChange.createDelegate(this));
    },

    onNodeSelectionChange: function(selectionModel, node) {
        if (this.isFolder(node) && this.disabled)
        {
            this.enable();
        }
        else if (!this.isFolder(node) && !this.disabled)
        {
            this.disable();
        }
    },

    isFolder: function(node) {
        return node && node.reload;
    },

    onClick: function(){
        var that = this;
        Ext.MessageBox.confirm('confirm', 'Are you sure you want to delete the folder?', function(btn){
            if (btn == 'yes')
            {
                that.onDelete();
            }
        });
    },

    onDelete: function() {

        this.sbar.setStatus({
            text: 'Deleting folder...'
        });

        var path = this.tree.getSelectedConfigPath();

        Ext.Ajax.request({
            url: this.baseUrl + '/ajax/rmdir.action',
            params: {
                path:path,
                basePath:this.basePath
            },
            success: this.onSuccess,
            failure: this.onFailure,
            scope: this
        });
    },

    onFailure : function(response, options){
        this.sbar.setStatus({
            text: 'Failed to delete folder.',
            iconCls: 'x-status-error',
            clear: true // auto-clear after a set interval
        });
    },

    onSuccess : function(response, options){
        // check for errors.
        var decodedResponse = Ext.util.JSON.decode(response.responseText);
        if (decodedResponse.actionErrors[0])
        {
            this.sbar.setStatus({
                text: decodedResponse.actionErrors[0],
                iconCls: 'x-status-error',
                clear: true // auto-clear after a set interval
            });
            return;
        }

        this.sbar.setStatus({
            text: 'Folder deleted.',
            clear: true // auto-clear after a set interval
        });

        var deletedNode = this.tree.getSelectionModel().getSelectedNode();
        if (deletedNode.previousSibling)
        {
            deletedNode.previousSibling.select();
        }
        else if (deletedNode.nextSibling)
        {
            deletedNode.nextSibling.select();
        }
        else
        {
            deletedNode.parentNode.select();
        }

        var deletedPath = this.tree.toConfigPathPrefix(deletedNode.getPath('baseName'));
        this.tree.removeNode(deletedPath);
    }
});

/**
 * @cfg tree    the tree to which the new folder will be added.
 */
ZUTUBI.CreateFolderButton = Ext.extend(Ext.Button, {

    win : undefined,

    initComponent : function() {
        if (this.path === undefined) {
            this.disabled = true;
        }

        ZUTUBI.SelectNodeButton.superclass.initComponent.apply(this, arguments);

        this.tree.getSelectionModel().on('selectionchange', this.onNodeSelectionChange.createDelegate(this));
    },

    onClick: function(){
        var that = this;
        Ext.MessageBox.prompt('create folder', 'folder name:', function(btn, txt) {
            if (btn == 'ok')
            {
                that.onOk(txt);
            }
        });
    },

    onOk: function(name) {

        this.sbar.setStatus({
            text: 'Creating folder...'
        });
        this.newFolderName = name;
        var path = this.tree.getSelectedConfigPath();

        Ext.Ajax.request({
            url: this.baseUrl + '/ajax/mkdir.action',
            params: {
                path:path,
                name:name,
                basePath:this.basePath
            },
            success: this.onSuccess,
            failure: this.onFailure,
            scope: this
        });
    },

    onFailure : function(response, options){
        this.sbar.setStatus({
            text: 'Failed to create folder.',
            clear: true // auto-clear after a set interval
        });
    },

    onSuccess : function(response, options){
        // check for errors.
        var decodedResponse = Ext.util.JSON.decode(response.responseText);
        if (decodedResponse.actionErrors[0])
        {
            this.sbar.setStatus({
                text: decodedResponse.actionErrors[0],
                iconCls: 'x-status-error',
                clear: true // auto-clear after a set interval
            });
            return;
        }

        this.sbar.setStatus({
            text: 'Folder created.',
            clear: true // auto-clear after a set interval
        });
        
        var name = this.newFolderName;

        var selected = this.tree.getSelectionModel().getSelectedNode();
        if (!selected.expanded)
        {
            selected.expand(false, true, function(node){
                var newFolder = node.findChild('baseName', name);
                newFolder.select();
            });
        }
        else
        {
            this.tree.addNode(this.tree.getSelectedConfigPath(), name);
            // newly added nodes have a text field rather than a baseName
            var newFolder = selected.findChild('text', name);
            newFolder.attributes['baseName'] = name; // since everything else uses baseName, lets add it here.
            newFolder.select();
        }
    },

    onNodeSelectionChange: function(selectionModel, node) {
        if (this.isFolder(node) && this.disabled)
        {
            this.enable();
        }
        else if (!this.isFolder(node) && !this.disabled)
        {
            this.disable();
        }
    },

    isFolder: function(node) {
        return node && node.reload;
    }
});


if(Ext.ux.tree) { ZUTUBI.ArtifactsTree = Ext.extend(Ext.ux.tree.TreeGrid,
{
    MAX_COLUMN_WIDTH: 600,

    style: 'padding: 0 8px 8px 8px',
    bodyStyle: 'border-top: solid 1px #bbb; border-bottom: dotted 1px #bbb;',
    border: false,
    layout: 'fit',
    enableHdMenu: false,

    tooltips: {
        archive: 'download a zip archive of this artifact',
        decorate: 'go to a decorated view of this file',
        download: 'download this file',
        link: 'navigate to the external link',
        view: 'view this html artifact'
    },

    initComponent: function()
    {
        var tree = this;
        var config = {
            loader: new ZUTUBI.FSTreeLoader({
                baseUrl: window.baseUrl,
                fs: 'pulse',
                basePath: 'projects/' + this.initialConfig.projectId + '/builds/' + this.initialConfig.buildId + '/artifacts',
                showFiles: true,
                preloadDepth: 3,
                filterFlag: this.initialConfig.filter
            }),

            selModel: new Ext.tree.DefaultSelectionModel({onNodeClick: Ext.emptyFn}),

            tbar: {
                id: 'artifacts-toolbar',
                items: [{
                    xtype: 'label',
                    text: 'filter:'
                }, ' ', {
                    xtype: 'combo',
                    id: 'filter-combo',
                    width: 200,
                    mode: 'local',
                    triggerAction: 'all',
                    editable: false,
                    store: new Ext.data.ArrayStore({
                        idIndex: 0,
                        fields: [
                            'filter',
                            'text'
                        ],
                        data: [
                            ['', 'all artifacts'],
                            ['explicit', 'explicit artifacts only'],
                            ['featured', 'featured artifacts only']
                        ]
                    }),
                    valueField: 'filter',
                    displayField: 'text',
                    value: this.initialConfig.filter,
                    listeners: {
                        select: function(combo, record) {
                            tree.setFilterFlag(record.get('filter'));
                        }
                    }
                }, {
                    xtype: 'xztblink',
                    id: 'save-filter-link',
                    text: 'save',
                    icon: window.baseUrl + '/images/save.gif',
                    listeners: {
                        click: function() {
                            tree.saveFilterFlag(Ext.getCmp('filter-combo').getValue());
                        }
                    }
                }, {
                    xtype: 'tbtext',
                    text: '<span class="understated">//</span>'
                }, {
                    xtype: 'xztblink',
                    id: 'expand-all-link',
                    text: 'expand all',
                    icon: window.baseUrl + '/images/expand.gif',
                    listeners: {
                        click: function() {
                            tree.expandAll();
                        }
                    }
                }, {
                    xtype: 'xztblink',
                    id: 'collapse-all-link',
                    text: 'collapse all',
                    icon: window.baseUrl + '/images/collapse.gif',
                    listeners: {
                        click: function() {
                            tree.collapseAll();
                        }
                    }
                }]
            },

            columns: [{
                header: 'name',
                tpl: '{text:htmlEncode}',
                width: 400
            }, {
                header: 'size',
                width: 100,
                dataIndex: 'size',
                tpl: '<tpl if="extraAttributes.size">{[Ext.util.Format.fileSize(values.extraAttributes.size)]}</tpl>',
                align: 'right',
                sortType: function(node) {
                    var extraAttributes = node.attributes.extraAttributes;
                    if (extraAttributes && extraAttributes.size)
                    {
                        return extraAttributes.size;
                    }
                    else
                    {
                        return 0;
                    }
                }
            }, {
                header: 'actions',
                width: 120,
                sortable: false,
                tpl: '<tpl if="extraAttributes.actions">' +
                         '<tpl for="extraAttributes.actions">' +
                             '&nbsp;<a href="#" onclick="window.location=\'{url}\';">' +
                                 '<img alt="{type}" src="'+ window.baseUrl + '/images/artifacts/{type}.gif" ext:qtip="{[ZUTUBI.ArtifactsTree.prototype.tooltips[values.type]]}"/>' +
                             '</a>' +
                         '</tpl>' +
                     '</tpl>'
            }]
        };

        Ext.apply(this, config);
        Ext.apply(this.initialConfig, config);

        ZUTUBI.ArtifactsTree.superclass.initComponent.apply(this, arguments);

        this.loading = true;
        this.on('beforerender', this.setInitialColumnWidths, this, {single: true});
        this.on('expandnode', this.initialExpand, this, {single: true});
    },

    setInitialColumnWidths: function()
    {
        // If there is more than enough width for our columns,
        // stretch the first one to fill.
        var availableWidth = this.ownerCt.getSize().width;
        var columns = this.columns;
        var firstWidth = columns[0].width;
        var remainingWidth = 0;
        var count = columns.length;
        for (var i = 1; i < count; i++)
        {
            remainingWidth += columns[i].width;
        }

        var buffer = Ext.getScrollBarWidth() + 20;
        if (availableWidth > firstWidth + remainingWidth + buffer)
        {
            var newWidth = availableWidth - remainingWidth - buffer;
            if (newWidth > this.MAX_COLUMN_WIDTH)
            {
                newWidth = this.MAX_COLUMN_WIDTH;
            }
            this.columns[0].width = newWidth;
        }
    },

    smallEnough: function(node)
    {
        var children = node.attributes.children;
        return children && children.length < 9;
    },

    initialExpand: function(node)
    {
        var depth = node.getDepth();
        if (depth < 3)
        {
            var children = node.childNodes;
            var count = children.length;
            for (var i = 0; i < count; i++)
            {
                var child = children[i];
                if (depth < 2 || this.smallEnough(child))
                {
                    child.expand(false, false, this.initialExpand, this);
                }
            }

            if (depth == 0)
            {
                this.loading = false;
            }
        }
    },

    setFilterFlag: function(flag)
    {
        this.loader.baseParams.filterFlag = flag;
        this.loading = true;
        this.getEl().mask('Reloading...');
        this.on('expandnode', this.initialExpand, this, {single: true});
        this.getRootNode().reload(function() {
            this.getOwnerTree().getEl().unmask();
        });
    },

    saveFilterFlag: function(flag)
    {
        Ext.Ajax.request({
           url: window.baseUrl + '/ajax/saveArtifactsFilter.action',
           success: function() { showStatus('Filter saved.','success'); },
           failure: function() { showStatus('Unable to save filter.','failure'); },
           params: { filter: flag }
        });
    }
}); }

ZUTUBI.Toolbar = {};
ZUTUBI.Toolbar.LinkItem = Ext.extend(Ext.Toolbar.Item, {
    /**
     * @cfg {String} icon  URL of the image to show beside the link.
     * @cfg {String} text  The text to be shown in the link.
     * @cfg {String} url   The URL to link to.
     */

    initComponent: function()
    {
        ZUTUBI.Toolbar.LinkItem.superclass.initComponent.call(this);

        this.addEvents(
            /**
             * @event click
             * Fires when this button is clicked
             * @param {LinkItem} this
             * @param {EventObject} e The click event
             */
            'click'
        );
    },

    // private
    onRender: function(ct, position) 
    {
        this.autoEl = {
            tag: 'span',
            cls: 'xz-tblink',
            children: [{
                tag: 'a',
                cls: 'unadorned',
                href: '#',
                children: [{
                    tag: 'img',
                    alt: this.text || '',
                    src: this.icon
                }]
            }, {
                tag: 'a',
                href: '#',
                html: this.text || ''
            }]
        };
        ZUTUBI.Toolbar.LinkItem.superclass.onRender.call(this, ct, position);
        this.mon(this.el, {scope: this, click: this.onClick});
    },

    onClick: function(e)
    {
        if (e)
        {
            e.preventDefault();
        }

        this.fireEvent('click', this, e);
    }
});
Ext.reg('xztblink', ZUTUBI.Toolbar.LinkItem);
