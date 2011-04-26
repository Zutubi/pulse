//dependency: ext/package.js
//dependency: ./namespace.js

 /**
  * A panel that loads its data from an AJAX action in JSON form, optionally at regular intervals.
  * The panel will usually be composed of multiple child components, each of which must support an
  * update function which will be passed new data when it arrives.  The update function must support
  * being called any time after component initialisation (in particular, it may be called before
  * rendering or afterwards, and in the latter case should update the rendered view).
  *
  * @cfg {String} id              ID of this component, which by convention should be the prefix
  *                               of the IDs of all children.
  * @cfg {String} url             URL to request JSON data from.
  * @cfg {String} loadingId       ID of an existing element on the page that can be used for
  *                               reporting the initial load (and, if it fails, an error message).
  * @cfg {String} containerId     ID of an existing Ext container in which to drop the component
  *                               when it is ready to render.
  * @cfg {Number} refreshInterval Interval at which to reload an update data.  May be set to zero
  *                               to only load once (default is zero).
  * @cfg {Array}  dataKeys        Array of keys for child components to update with new data, may be
  *                               omitted if dataMapping is present.
  * @cfg {Object} dataMapping     An object that maps from a component id suffix to the data key
  *                               used to update that component, may be omitted if dataKeys is
  *                               present.
  */
Zutubi.ActivePanel = Ext.extend(Ext.Panel, {
    initComponent: function()
    {
        Ext.applyIf(this, {
            loadingMessage: 'Loading data...',
            failureMessage: 'Unable to load data.',
            refreshInterval: 0
        });
        
        Ext.fly(this.loadingId).update('<img alt="loading" src="' + window.baseUrl + '/images/inprogress.gif"/> ' + this.loadingMessage);
        
        if (this.refreshInterval > 0)
        {
            this.runner = new Ext.util.TaskRunner();
            this.runner.start({
                run: this.load,
                scope: this,
                args: [],
                interval: 1000 * this.refreshInterval
            });
        }
        else
        {
            this.load();
        }
        
        Zutubi.ActivePanel.superclass.initComponent.apply(this, arguments);
    },
    
    update: function(data)
    {
        this.data = data;
        
        if (this.dataKeys)
        {
            for (var i = 0, l = this.dataKeys.length; i < l; i++)
            {
                var key = this.dataKeys[i];
                Ext.getCmp(this.id + '-' + key).update(l == 1 ? data : data[key]);    
            }
        }
        else
        {
            for (var key in this.dataMapping)
            {
                var path = this.dataMapping[key];
                var properties = path.split('.');
                var componentData = data;
                for (var i = 0, l = properties.length; componentData && i < l; i++)
                {
                    componentData = componentData[properties[i]];
                }
                
                Ext.getCmp(this.id + '-' + key).update(componentData);
            }
        }
    },

    load: function(callback)
    {
        var panel = this;
        Ext.Ajax.request({
            url: panel.url,
            timeout: 120000,
            
            success: function(transport/*, options*/)
            {
                Ext.fly(panel.loadingId).update('');
                panel.update(eval('(' + transport.responseText + ')'));
                if (!panel.initialised)
                {
                    var container = Ext.getCmp(panel.containerId);
                    if (container.contentEl)
                    {
                        Ext.fly(container.contentEl).setStyle({margin: 0, padding: 0});
                    }
                    container.add(panel);
                    container.doLayout();
                    panel.initialised = true;
                }
                
                if (callback)
                {
                    callback();
                }
            },

            failure: function(transport /*, options*/)
            {
                // Stop trying to refresh.
                if (panel.runner)
                {
                    panel.runner.stopAll();
                }

                // If we never initialised the view, show an error message.
                if (!panel.initialised)
                {
                    console.dir(transport);
                    var message = panel.failureMessage;
                    if (transport.statusText)
                    {
                        message += ' (' + unescape(transport.statusText.replace(/\+/g, ' ')) + ')';
                    }
                        
                    Ext.fly(panel.loadingId).update(message);
                }

                if (callback)
                {
                    callback();
                }
            }
        });
    },
    
    /**
     * Checks a child panel for content.  If it has none, the panel is
     * collapsed automatically.
     *
     * @param idSuffix suffix of the id for the child panel
     * @param fn       a function that should accept a child item from the
     *                 panel and return true if that item has content, false
     *                 otherwise
     */
    checkPanelForContent: function(idSuffix, fn)
    {
        var panel = Ext.getCmp(this.id + '-' + idSuffix);
        var childWithContent = panel.items.find(fn);
        
        if (childWithContent)
        {
            if (!panel.hasHadContent && panel.collapsed)
            {
                if (this.rendered)
                {
                    panel.expand();
                }
                else
                {
                    panel.collapsed = false;
                }
            }

            panel.hasHadContent = true;
        }
        else
        {
            if (this.rendered)
            {
                panel.collapse();
            }
            else
            {
                panel.collapsed = true;
            }
        }
    }
});
