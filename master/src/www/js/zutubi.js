// setup the namespace for the zutubi components.
var ZUTUBI = window.ZUTUBI || {};

// if it is already defined, great, otherwise create it.
ZUTUBI.widget = ZUTUBI.widget || {};

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
                this.tpl = '<div unselectable="on" class="x-select-item" tabindex="-1">{' + this.displayField + '}</div>';
            }
            else
            {
                this.tpl = '<div class="x-select-item  x-unselectable" tabindex="-1">{' + this.displayField + '}</div>';
            }
        }

        this.view = new Ext.View(this.el, this.tpl, {
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
            this.entryHeight = this.el.child('div').getHeight() + 2;
        }

        this.el.setHeight(this.entryHeight * this.size);
        this.view.on('selectionchange', this.onSelectionChange, this);
        this.initValue();
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
    getNodePath : function(node)
    {
        var tree = node.getOwnerTree();
        return this.dataUrl + '/' + tree.getNodeConfigPath(node);
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

            this.transId = Ext.lib.Ajax.request(this.requestMethod, this.getNodePath(node) + '?ls', cb, params);
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


ZUTUBI.ConfigTree = function(id, config)
{
    ZUTUBI.ConfigTree.superclass.constructor.call(this, id, config);
    this.dead = false;
}

Ext.extend(ZUTUBI.ConfigTree, Ext.tree.TreePanel, {
    configPathToTreePath: function(configPath)
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
            treePath = this.pathSeparator + this.root.id + treePath;
        }

        // Workaround odd Ext behaviour: selecting needs a leading slash
        // except when selecting the root.
        if (treePath.lastIndexOf('/') == 0)
        {
            treePath = treePath.substring(1);
        }

        //alert('cpttp(' + configPath + ') -> ' + treePath);
        return treePath;
    },

    treePathToConfigPath: function(treePath)
    {
        var configPath = treePath;
        
        if (!this.rootVisible)
        {
            configPath = configPath.substring(this.root.id.length + 1);
        }

        if(configPath.length > 0 && configPath.substring(0, 1) == '/')
        {
            configPath = configPath.substring(1);    
        }

        if (this.pathPrefix)
        {
            configPath = this.pathPrefix + this.pathSeparator + configPath;
        }

        //console.log('tptcp(' + treePath + ') -> ' + configPath);
        return configPath;
    },

    selectConfigPath: function(configPath)
    {
        this.getSelectionModel().clearSelections();
        this.selectPath(this.configPathToTreePath(configPath));
    },

    expandToPath: function(path)
    {
        this.expandPath(this.configPathToTreePath(path));        
    },

    getNodeConfigPath: function(node)
    {
        return this.treePathToConfigPath(node.getPath());
    },

    getNodeByConfigPath: function(configPath)
    {
        var treePath = this.configPathToTreePath(configPath);
        if(treePath.substring(0, 1) == '/')
        {
            treePath = treePath.substring(1);
        }
        
        var keys = treePath.split(this.pathSeparator);

        if (keys[0] != this.root.id)
        {
            return null;
        }

        var node = this.root;
        for(var i = 1; node && i < keys.length; i++)
        {
            node = node.findChild('id', keys[i]);
        }
        
        return node;
    },

    handleResponse: function(response)
    {
        var tree = this;

        if(response.addedFiles)
        {
            each(response.addedFiles, function(addition) { tree.addNode(addition.parentPath, {id: addition.baseName, text: addition.displayName, iconCls: addition.iconCls, leaf: addition.leaf}); });
        }

        if(response.renamedPaths)
        {
            each(response.renamedPaths, function(rename) { tree.renameNode(rename.oldPath, rename.newName, rename.newDisplayName); });
        }

        if(response.removedPaths)
        {
            each(response.removedPaths, function(path) { tree.removeNode(path) });
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

    renameNode: function(oldPath, newName, newDisplayName)
    {
        if(oldPath)
        {
            var node = this.getNodeByConfigPath(oldPath);
            if(node)
            {
                this.setNodeId(node, newName);
                node.setText(newDisplayName);
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
    },

    setNodeId: function(node, id)
    {
        this.unregisterNode(node);
        node.id = id;
        node.attributes.id = id;
        this.registerNode(node);
    }
});


ZUTUBI.TemplateTree = function(scope, id, config)
{
    this.scope = scope;
    this.dead = false;
    ZUTUBI.TemplateTree.superclass.constructor.call(this, id, config);
}

Ext.extend(ZUTUBI.TemplateTree, ZUTUBI.ConfigTree, {
    handleResponse: function(response)
    {
        var tree = this;

        if (response.addedFiles)
        {
            each(response.addedFiles, function(addition) {
                if (addition.parentTemplatePath && addition.parentPath == tree.scope)
                {
                    tree.addNode(addition.parentTemplatePath, {id: addition.baseName, text: addition.displayName, iconCls: addition.iconCls, leaf: addition.templateLeaf});
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

    translatePath: function(path)
    {
        var pieces = path.split(this.pathSeparator);
        if (pieces.length == 2 && pieces[0] == this.scope)
        {
            var id = pieces[1];
            var node = this.getNodeById(id);
            if (node)
            {
                return this.getNodeConfigPath(node);
            }
        }
        
        return null;
    }
});

ZUTUBI.Form = function(config)
{
    var template = new Ext.Template('<tr id="x-form-row-{0}" class="x-form-row {5}">' +
                                        '<td class="x-form-label"><label for="{0}" style="{2}">{1}</td>' +
                                        '<td class="x-form-label-annotation" id="x-form-label-annotation-{0}"></td>' +
                                        '<td class="x-form-separator">{4}</td>' +
                                        '<td><div id="x-form-el-{0}" class="x-form-element" style="{3}">' +
                                        '</div></td>' +
                                    '</tr>');
    template.disableFormats = true;
    template.compile();

    config.root = new Ext.form.Layout({
        labelAlign: 'right',
        fieldTpl: template,
        autoCreate: { tag: 'table', cls: 'x-form-ct' }
    });

    ZUTUBI.Form.superclass.constructor.call(this, config);
};

Ext.extend(ZUTUBI.Form, Ext.form.Form, {
    displayMode: false,

    add : function()
    {
        var a = arguments;
        for(var i = 0, len = a.length; i < len; i++)
        {
            a[i].form = this;
        }
        
        ZUTUBI.Form.superclass.add.apply(this, a);
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

    annotateField: function(id, annotationName, imageName, tooltip)
    {
        var rowEl = Ext.get('x-form-row-' + id);
        var cellEl = rowEl.createChild({tag: 'td', cls: 'x-form-annotation'});
        var imageEl = cellEl.createChild({tag: 'img', src: imageName, id: id + '.' + annotationName});
        if(tooltip)
        {
            imageEl.dom.qtip = tooltip;
        }
    },

    updateButtons: function()
    {
        if(this.displayMode)
        {
            var dirty = this.isDirty();
            if(!dirty)
            {
                this.clearInvalid();
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
    },

    clearInvalid: function()
    {
        ZUTUBI.Form.superclass.clearInvalid.call(this);
        var statusEl = Ext.get(this.formName + '.status');
        statusEl.update('');
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
}

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

        var alignEl;
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
            alignEl = this.combo.wrap;
        }
        else
        {
            this.input = this.wrap.createChild({tag: 'input', type: 'text', cls: 'x-form-text', id: this.id + '.choice'});
            this.input.setWidth(this.width);
            this.choice = this.input;
            alignEl = this.input;
        }

        if(Ext.isIE || Ext.isIE7)
        {
            alignEl.alignTo(this.el, 'tl-bl');
        }

        this.choice.on('focus', this.onInputFocus, this);
        this.choice.on('blur', this.onInputBlur, this);

        this.addButton = new ZUTUBI.ImageButton(this.wrap, {image: '/images/buttons/sb-add-up.gif', overImage: '/images/buttons/sb-add-over.gif', downImage: '/images/buttons/sb-add-down.gif'});
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

        this.removeButton = new ZUTUBI.ImageButton(this.wrap, {image: '/images/buttons/sb-delete-up.gif', overImage: '/images/buttons/sb-delete-over.gif', downImage: '/images/buttons/sb-delete-down.gif'});
        this.removeButton.on('click', this.onRemove, this);

        this.upButton = new ZUTUBI.ImageButton(this.wrap, {image: '/images/buttons/sb-up-up.gif', overImage: '/images/buttons/sb-up-over.gif', downImage: '/images/buttons/sb-up-down.gif'});
        this.upButton.on('click', this.onUp, this);

        this.downButton = new ZUTUBI.ImageButton(this.wrap, {image: '/images/buttons/sb-down-up.gif', overImage: '/images/buttons/sb-down-over.gif', downImage: '/images/buttons/sb-down-down.gif'});
        this.downButton.on('click', this.onDown, this);

        var cls = 'x-item-picker';

        if(!this.tpl)
        {
            if(Ext.isIE || Ext.isIE7)
            {
                this.tpl = '<div unselectable="on" class="' + cls + '-item">{' + this.displayField + '}</div>';
            }
            else
            {
                this.tpl = '<div class="' + cls + '-item  x-unselectable">{' + this.displayField + '}</div>';
            }
        }

        this.view = new Ext.View(this.el, this.tpl, {
            singleSelect:true, store: this.store, selectedClass: this.selectedClass
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
        for(var i = 0; i < this.hiddenFields.length; i++)
        {
            this.hiddenFields[i].remove();
        }
        this.hiddenFields = [];
        
        for(var i = 0; i < value.length; i++)
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

Ext.form.Checkbox.prototype.onResize = function()
{
    Ext.form.Checkbox.superclass.onResize.apply(this, arguments);
}

Ext.override(Ext.View, {
    onClick : function(e)
    {
        if(this.disabled)
        {
            if(e)
            {
                e.preventDefault();
                return;
            }
        }
        
        var item = this.findItemFromChild(e.getTarget());
        if (item)
        {
            var index = this.indexOf(item);
            if (this.onItemClick(item, index, e) !== false)
            {
                this.fireEvent("click", this, index, item, e);
            }
        }
        else
        {
            this.clearSelections();
        }
    },

    onItemClick : function(item, index, e)
    {
        if (!this.fireEvent("beforeclick", this, index, item, e))
        {
            return false;
        }
        if (this.multiSelect || this.singleSelect)
        {
            if (this.multiSelect && e.shiftKey && this.getSelectionCount() > 0)
            {
                var lastIndex = this.getSelectedIndexes()[this.getSelectionCount() - 1];
                this.select(this.getNodes(lastIndex, index), false);
            }
            else if (this.isSelected(this.getNode(item)) && e.ctrlKey)
            {
                this.unselect(item);
            }
            else
            {
                this.select(item, this.multiSelect && e.ctrlKey);
            }
            e.preventDefault();
        }
        return true;
    },

    unselect : function(nodeInfo, suppressEvent)
    {
        var node = this.getNode(nodeInfo);
        if (node && this.isSelected(node))
        {
            if (this.fireEvent("beforeselect", this, node, this.selections))
            {
                Ext.fly(node).removeClass(this.selectedClass);
                this.selections.remove(node);
                if (suppressEvent !== true)
                {
                    this.fireEvent("selectionchange", this, this.selections);
                }
            }
        }
    },

    select : function(nodeInfo, keepExisting, suppressEvent)
    {
        if (nodeInfo instanceof Array)
        {
            if (!keepExisting)
            {
                this.clearSelections(true);
            }
            for (var i = 0, len = nodeInfo.length; i < len; i++)
            {
                this.select(nodeInfo[i], true, true);
            }
            if (suppressEvent !== true)
            {
                this.fireEvent("selectionchange", this, this.selections);
            }
        }
        else
        {
            var node = this.getNode(nodeInfo);
            if (node)
            {
                if (this.isSelected(node))
                {
                    if (this.getSelectionCount() > 1)
                    {
                        this.clearSelections(true);
                    }
                    else
                    {
                        return;
                    }
                }
                else if (!keepExisting)
                {
                    this.clearSelections(true);
                }

                if (this.fireEvent("beforeselect", this, node, this.selections))
                {
                    Ext.fly(node).addClass(this.selectedClass);
                    this.selections.push(node);
                    if (suppressEvent !== true)
                    {
                        this.fireEvent("selectionchange", this, this.selections);
                    }
                }
            }
        }
    }
});

Ext.override(Ext.Element, {
    mask: function(msg, msgCls)
    {
        if (!Ext.isIE7 && this.getStyle("position") == "static")
        {
            this.setStyle("position", "relative");
        }

        if (this._maskMsg)
        {
            this._maskMsg.remove();
        }
        if (this._mask)
        {
            this._mask.remove();
        }

        this._mask = Ext.DomHelper.append(this.dom, {cls:"ext-el-mask"}, true);

        this.addClass("x-masked");
        this._mask.setDisplayed(true);
        if (typeof msg == 'string')
        {
            this._maskMsg = Ext.DomHelper.append(this.dom, {cls:"ext-el-mask-msg", cn:{tag:'div'}}, true);

            var mm = this._maskMsg;
            mm.dom.className = msgCls ? "ext-el-mask-msg " + msgCls : "ext-el-mask-msg";
            mm.dom.firstChild.innerHTML = msg;
            mm.setDisplayed(true);
            mm.center(this);
        }

        if (Ext.isIE && !(Ext.isIE7 && Ext.isStrict) && this.getStyle('height') == 'auto')
        {
            this._mask.setHeight(this.getHeight());
        }

        return this._mask;
    }
});
