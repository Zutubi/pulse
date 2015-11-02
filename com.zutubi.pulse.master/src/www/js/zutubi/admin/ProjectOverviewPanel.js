// dependency: ./namespace.js


(function($)
{
    var Observable = kendo.Observable,
        ns = ".kendoOverviewPanel",
        CLICK = "click" + ns,
        ID = "project-overview";

    Zutubi.admin.ProjectOverviewPanel = Observable.extend({
        init: function (options)
        {
            var that = this,
                project = options.project;

            that.path = "projects/" + project.properties.name;
            that.options = jQuery.extend({}, that.options, options);
            that.rowTemplate = kendo.template(that.options.rowTemplate);
            that.scmTemplate = kendo.template(that.options.scmTemplate);

            Observable.fn.init.call(this);

            that.view = new kendo.View(
                '<div id="#= id #" class="k-overview-panel">' +
                    '<h1><a class="k-summary-edit">#: name # <span class="fa fa-pencil"></span></a></h1>' +
                    '<table id="#= id #-summary"></table>' +
                '</div>', {
                    wrap: false,
                    evalTemplate: true,
                    model: {
                        id: ID,
                        name: project.properties.name
                }
            });

            that.view.render($(options.containerSelector));
            that.view.element.find(".k-summary-edit").on(CLICK, jQuery.proxy(this._editClicked, this, ""));

            that._addSummary($("#" + ID + "-summary"), project);
        },

        options: {
            rowTemplate: '<tr><th><a class="k-summary-edit">#: heading #</span></a></th><td></td>',
            scmTemplate: '<strong>#: type #</strong> :: #: summary #'
        },

        events: [
        ],

        destroy: function()
        {
            this.view.element.find(".k-summary-edit").off(ns);
            this.view.destroy();
        },

        _addSummary: function(tableEl, project)
        {
            this._addRow(tableEl, "scm", "scm", this._addScmSummary);
            this._addRow(tableEl, "stages", "build stages", this._addStagesSummary);
            this._addRow(tableEl, "dependencies", "dependencies", this._addDependenciesSummary);
        },

        _addRow: function(tableEl, key, heading, summary)
        {
            var rowEl = $(this.rowTemplate({heading: heading})).appendTo(tableEl),
                cellEl = rowEl.children("td");

            rowEl.find(".k-summary-edit:first").on(CLICK, jQuery.proxy(this._editClicked, this, key));

            if (typeof summary === "function")
            {
                summary.call(this, cellEl, this.options.project);
            }
            else
            {
                cellEl.append(summary);
            }
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

        _addScmSummary: function(cellEl, project)
        {
            var scm = this.getNested(project, "scm"),
                summary;

            if (scm)
            {
                summary = this.scmTemplate({type: this.getSimple(scm, "type"), summary: this.getSimple(scm, "summary") });
                if (summary.indexOf("\n") >= 0)
                {
                    cellEl.addClass("k-preformatted");
                }
                cellEl.append(summary);
            }
            else
            {
                cellEl.append('<span class="k-understated">not configured</span>');
            }
        },

        _addStagesSummary: function(cellEl, project)
        {
            var stages = this.getNested(project, "stages"),
                tableEl,
                i,
                stage;

            if (stages && stages.nested.length > 0)
            {
                tableEl = $('<table></table>');
                for (i = 0; i < stages.nested.length; i++)
                {
                    stage = stages.nested[i];
                    this._addRow(tableEl, "stages/" + stage.properties.name, stage.properties.name, this._getStageSummary(stage));
                }

                cellEl.addClass("k-summary-wrapper");
                cellEl.append(tableEl);
            }
            else
            {
                cellEl.append('<span class="k-understated">none configured</span>');
            }
        },

        _getStageSummary: function(stage)
        {
            return kendo.htmlEncode(this.getSimple(stage, "recipe", "[default]") + " @ " + this.getSimple(stage, "agent", "[any capable]"));
        },

        _addDependenciesSummary: function(cellEl, project)
        {
            var dependencies = this.getNested(project, "dependencies"),
                projects = this.getNested(dependencies, "dependencies"),
                tableEl = $('<table></table>'),
                i,
                upstream;

            this._addRow(tableEl, "dependencies", "status", dependencies.properties.status);
            this._addRow(tableEl, "dependencies", "version", kendo.htmlEncode(dependencies.properties.version));

            if (projects && projects.nested.length > 0)
            {
                nested = "";
                for (i = 0; i < projects.nested.length; i++)
                {
                    if (nested)
                    {
                        nested += ", ";
                    }

                    upstream = projects.nested[i];
                    nested += this.getSimple(upstream, "projectName") + " @ " + this.getSimple(upstream, "revision");
                }

                this._addRow(tableEl, "dependencies", "upstream", kendo.htmlEncode(nested));
            }

            cellEl.addClass("k-summary-wrapper");
            cellEl.append(tableEl);
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

            return value || defaultValue;
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
