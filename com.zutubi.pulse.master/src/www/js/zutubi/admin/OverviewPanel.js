// dependency: ./namespace.js
// dependency: zutubi/config/package.js

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
            var that = this,
                icon,
                concrete = options.item.concrete,
                name = options.item.properties.name;

            Observable.fn.init.call(this);

            that.options = jQuery.extend({}, that.options, options);
            that.rowTemplate = kendo.template(that.options.rowTemplate);

            that.path = options.scope + "/" + name;

            that.view = new kendo.View(
                '<div id="#= id #" class="k-overview-panel">' +
                    '<h1><span class="fa k-overview-icon"></span> <a class="k-summary-edit">#: name #</a></h1>' +
                    '<table id="#= id #-summary"></table>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        id: options.id,
                        name: name
                    }
                });

            that.view.render(options.container);
            that.tableEl = $("#" + options.id + "-summary");

            icon = that.view.element.find(".k-overview-icon");
            icon.addClass(concrete ? "fa-circle" : "fa-circle-thin");
            icon.kendoTooltip({
                content: (concrete ? "concrete " : "template ") + options.scope.substring(0, options.scope.length - 1)
            });
            that.view.element.find(".k-summary-edit").on(CLICK, jQuery.proxy(this._editClicked, this, ""));

            that.showValidationErrors(options.item);
            that._addSummary(that.tableEl);
        },

        options: {
            rowTemplate: '<tr><th><a class="k-summary-edit">#: heading #</span></a></th><td></td>'
        },

        destroy: function()
        {
            this.view.element.find(".k-summary-edit").off(ns);
            this.view.element.find(".k-overview-validation-errors a").off(ns);
            this.view.destroy();
        },

        showValidationErrors: function(instance)
        {
            var errors = [],
                list,
                error,
                item,
                i;

            this._collectErrors(errors, instance, "");
            if (errors.length > 0)
            {
                $('<h2 class="k-overview-validation-header"><span class="fa fa-exclamation"></span> validation errors detected, click to navigate:</h2>').insertBefore(this.tableEl);
                list = $('<ul class="k-overview-validation-errors"></ul>');
                for (i = 0; i < errors.length; i++)
                {
                    error = errors[i];
                    item = $('<li><a>' + kendo.htmlEncode(error.path + ": " + (error.field ? error.field + ": " : "") + error.message) + '</a></li>');
                    list.append(item);
                    item.find('a').on(CLICK, jQuery.proxy(this._editClicked, this, error.path));
                }

                list.insertBefore(this.tableEl);
            }
        },

        _collectErrors: function(result, instance, path)
        {
            var errors = instance.validationErrors,
                field,
                fieldErrors,
                i,
                child;

            if (errors)
            {
                for (field in errors)
                {
                    if (errors.hasOwnProperty(field))
                    {
                        fieldErrors = errors[field];
                        for (i = 0; i < fieldErrors.length; i++)
                        {
                            result.push({path: path, field: field, message: fieldErrors[i]});
                        }
                    }
                }
            }

            if (instance.nested)
            {
                for (i = 0; i < instance.nested.length; i++)
                {
                    child = instance.nested[i];
                    if (!child.deeplyValid)
                    {
                        this._collectErrors(result, child, Zutubi.config.appendPath(path, child.key));
                    }
                }
            }
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

            if (value === null || typeof value === "undefined" || (typeof value === "string" && value === "" && defaultValue))
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
