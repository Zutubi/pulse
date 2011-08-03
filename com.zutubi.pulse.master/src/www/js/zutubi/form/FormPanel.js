// dependency: ./namespace.js
// dependency: ext/package.js
// dependency: ./Form.js
// dependency: ./FormLayout.js

Zutubi.form.FormPanel = function(config)
{
    config.layout = new Zutubi.form.FormLayout({});
    Zutubi.form.FormPanel.superclass.constructor.call(this, config);
};

Ext.extend(Zutubi.form.FormPanel, Ext.form.FormPanel, {
    displayMode: false,
    buttonAlign: 'center',

    createForm: function()
    {
        delete this.initialConfig.listeners;
        return new Zutubi.form.Form(this.initialConfig);
    },

    onRender: function(ct, position)
    {
        Zutubi.form.FormPanel.superclass.onRender.call(this, ct, position);
        this.form.el.update('<table><tbody></tbody></table>');
        this.layoutTarget = this.form.el.first().first();
    },

    getLayoutTarget: function()
    {
        return this.layoutTarget;
    },

    add: function()
    {
        var a, i, len;

        a = arguments;
        for(i = 0, len = a.length; i < len; i++)
        {
            a[i].form = this;
        }

        Zutubi.form.FormPanel.superclass.add.apply(this, a);
        return this;
    },

    markRequired: function(id, tooltip)
    {
        var cellEl, spanEl;

        cellEl = Ext.get('x-form-label-annotation-' + id);
        spanEl = cellEl.createChild({tag: 'span', cls: 'required', id: id + '.required', html: '*'});
        if(tooltip)
        {
            spanEl.dom.qtip = tooltip;
        }
    },

    enableField: function(id)
    {
        var field, rowEl, actionDomEls, i;

        field = this.findById(id);
        if(field)
        {
            field.enable();

            rowEl = this.getFieldRowEl(id);
            if (rowEl)
            {
                Ext.get(rowEl).removeClass('x-item-disabled');

                actionDomEls = this.getFieldActionDomEls(id);
                if (actionDomEls)
                {
                    for(i = 0; i < actionDomEls.length; i++)
                    {
                        Ext.get(actionDomEls[i]).removeClass('x-item-disabled');
                    }
                }
            }
        }
    },

    disableField: function(id)
    {
        var field, rowEl, actionDomEls, i;

        field = this.findById(id);
        if(field)
        {
            field.clearInvalid();
            field.disable();

            rowEl = this.getFieldRowEl(id);
            if (rowEl)
            {
                Ext.get(rowEl).addClass('x-item-disabled');

                actionDomEls = this.getFieldActionDomEls(id);
                if (actionDomEls)
                {
                    for(i = 0; i < actionDomEls.length; i++)
                    {
                        Ext.get(actionDomEls[i]).addClass('x-item-disabled');
                    }
                }
            }
        }
    },

    getFieldActionDomEls: function(id)
    {
        var rowEl;

        rowEl = this.getFieldRowEl(id);
        return Ext.query("*[class*='field-action']", rowEl.dom);
    },

    getFieldRowEl: function(id)
    {
        return Ext.get('x-form-row-' + id);
    },

    createAnnotationCell: function(id, annotationName)
    {
        var rowEl;

        rowEl = this.getFieldRowEl(id);
        return rowEl.createChild({tag: 'td', cls: 'x-form-annotation', id: id + '.' + annotationName});
    },
    
    annotateField: function(id, annotationName, imageName, tooltip)
    {
        var cellEl, imageEl;

        cellEl = this.createAnnotationCell(id, annotationName);
        imageEl = cellEl.createChild({tag: 'img', src: imageName});
        if(tooltip)
        {
            imageEl.dom.qtip = tooltip;
        }

        return imageEl;
    },
    
    annotateFieldWithMenu: function(id, annotationName, tooltip)
    {
        var cellEl, menuId, linkEl, buttonEl;

        cellEl = this.createAnnotationCell(id, annotationName);
        menuId = id + '-' + annotationName + '-menu';
        linkEl = cellEl.createChild({
            tag: 'a',
            id: menuId + '-link',
            cls: 'unadorned',
            href: '#',
            onclick: 'Zutubi.MenuManager.toggleMenu(this); return false'
        });
        
        buttonEl = linkEl.createChild({
            tag: 'img',
            id: menuId + '-button',
            src: window.baseUrl + Ext.BLANK_IMAGE_URL,
            cls: annotationName + ' field-action' + (this.readOnly ? ' x-item-disabled' : '')
        });
        
        if (tooltip)
        {
            buttonEl.dom.qtip= tooltip;
        }
        
        return menuId;
    },

    updateButtons: function()
    {
        var dirty, i;

        if(this.displayMode)
        {
            dirty = this.form.isDirty();
            if(!dirty)
            {
                this.form.clearInvalid();
            }

            for(i = 0; i < this.buttons.length; i++)
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
        var f;

        f = this.getForm();
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
        var panel, form;

        panel = this;
        form = this.getForm();
        form.items.each(function(field) {
            var el;

            el = field.getEl();
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
