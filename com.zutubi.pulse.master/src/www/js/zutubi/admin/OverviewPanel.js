// dependency: ./namespace.js

(function($)
{
    var Observable = kendo.Observable,
        ns = ".kendoOverviewPanel",
        CLICK = "click" + ns;

    /**
     * Base for overview panels in template scopes (projects/agents).  Subclasses should provide
     * options:
     *
     *   - id: used to generate element ids (directly used for outer div)
     *   - scope: name of the scope being summarised
     *   - name: name of the entity being summarised
     *
     * And must override:
     *
     *   - _addSummary(containerElement) to fill in the main page content
     *
     * Using utility methods:
     *
     *   - _addRow(tableEl, path, heading, summary) - path is appended to this.path, summary may be
     *     an HTML fragment or a function taking the cellElement as argument
     *   - _addSubtable(cellEl) - adds and returns a table element within a summary cell, allowing
     *     the summary itself to be a table
     *   - getSimple(composite, key, defaultValue) - to get a simple property value
     *   - getNested(composite, key) - to get a nested property value (or null if not present)
     */
    Zutubi.admin.OverviewPanel = Observable.extend({
        init: function (options)
        {
            var that = this;

            Observable.fn.init.call(this);

            that.options = jQuery.extend({}, that.options, options);
            that.rowTemplate = kendo.template(that.options.rowTemplate);

            that.path = options.scope + "/" + options.name;

            that.view = new kendo.View(
                '<div id="#= id #" class="k-overview-panel">' +
                '<h1><a class="k-summary-edit">#: name # <span class="fa fa-pencil"></span></a></h1>' +
                '<table id="#= id #-summary"></table>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        id: options.id,
                        name: options.name
                    }
                });

            that.view.render($(options.containerSelector));
            that.view.element.find(".k-summary-edit").on(CLICK, jQuery.proxy(this._editClicked, this, ""));

            that._addSummary($("#" + options.id + "-summary"));
        },

        options: {
            rowTemplate: '<tr><th><a class="k-summary-edit">#: heading #</span></a></th><td></td>'
        },

        destroy: function()
        {
            this.view.element.find(".k-summary-edit").off(ns);
            this.view.destroy();
        },

        _addSummary: function(tableEl)
        {
            this._addRow(tableEl, "scm", "scm", this._addScmSummary);
            this._addRow(tableEl, "stages", "build stages", this._addStagesSummary);
            this._addRow(tableEl, "dependencies", "dependencies", this._addDependenciesSummary);
        },

        _addRow: function(tableEl, path, heading, summary)
        {
            var rowEl = $(this.rowTemplate({heading: heading})).appendTo(tableEl),
                cellEl = rowEl.children("td");

            rowEl.find(".k-summary-edit:first").on(CLICK, jQuery.proxy(this._editClicked, this, path));

            if (typeof summary === "function")
            {
                summary.call(this, cellEl);
            }
            else
            {
                cellEl.append(summary);
            }
        },

        _addSubtable: function(cellEl)
        {
            var tableEl = $('<table></table>');
            cellEl.addClass("k-summary-wrapper");
            cellEl.append(tableEl);
            return tableEl;
        },

        _editClicked: function(path)
        {
            var fullPath = this.path;
            if (path)
            {
                fullPath += "/" + path;
            }

            Zutubi.admin.openConfigPath(fullPath);
        },

        getSimple: function(composite, property, defaultValue)
        {
            var value;

            if (composite.formattedProperties && composite.formattedProperties.hasOwnProperty(property))
            {
                value = composite.formattedProperties[property];
            }
            else if (composite.properties && composite.properties.hasOwnProperty(property))
            {
                value = composite.properties[property];
            }

            if (value === null || typeof value === "undefined" || (typeof value === "string" && value === ""))
            {
                value = defaultValue;
            }

            return value;
        },

        getNested: function(composite, property)
        {
            var i, value;

            if (composite.nested)
            {
                for (i = 0; i < composite.nested.length; i++)
                {
                    value = composite.nested[i];
                    if (value.key === property)
                    {
                        if (value.kind === "type-selection")
                        {
                            return null;
                        }
                        else
                        {
                            return value;
                        }
                    }
                }
            }

            return null;
        }

    });
}(jQuery));
