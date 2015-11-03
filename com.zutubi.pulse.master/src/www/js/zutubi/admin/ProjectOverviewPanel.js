// dependency: ./namespace.js
// dependency: ./OverviewPanel.js

(function($)
{
    var OverviewPanel = Zutubi.admin.OverviewPanel;

    Zutubi.admin.ProjectOverviewPanel = OverviewPanel.extend({
        init: function (options)
        {
            var that = this,
                project = options.project;

            options = jQuery.extend({
                scope: "projects",
                name: project.properties.name
            }, that.options, options);

            that.scmTemplate = kendo.template(that.options.scmTemplate);

            OverviewPanel.fn.init.call(this, options);
        },

        options: {
            id: "project-overview",
            scmTemplate: '<strong>#: type #</strong> :: #: summary #'
        },

        _addSummary: function(tableEl)
        {
            this._addRow(tableEl, "scm", "scm", this._addScmSummary);
            this._addRow(tableEl, "stages", "build stages", this._addStagesSummary);
            this._addRow(tableEl, "dependencies", "dependencies", this._addDependenciesSummary);
        },

        _addScmSummary: function(cellEl)
        {
            var scm = this.getNested(this.options.project, "scm"),
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

        _addStagesSummary: function(cellEl)
        {
            var stages = this.getNested(this.options.project, "stages"),
                tableEl,
                i,
                stage;

            if (stages && stages.nested.length > 0)
            {
                tableEl = this._addSubtable(cellEl);
                for (i = 0; i < stages.nested.length; i++)
                {
                    stage = stages.nested[i];
                    this._addRow(tableEl, "stages/" + stage.properties.name, stage.properties.name, this._getStageSummary(stage));
                }
            }
            else
            {
                cellEl.append('<span class="k-understated">none configured</span>');
            }
        },

        _getStageSummary: function(stage)
        {
            var agent = this.getSimple(stage, "agent", "");
            if (agent)
            {
                agent = Zutubi.admin.subPath(agent, 1, 2);
            }
            else
            {
                agent = "[any capable]";
            }

            return kendo.htmlEncode(this.getSimple(stage, "recipe", "[default]") + " @ " + agent);
        },

        _addDependenciesSummary: function(cellEl)
        {
            var dependencies = this.getNested(this.options.project, "dependencies"),
                projects = this.getNested(dependencies, "dependencies"),
                tableEl = this._addSubtable(cellEl),
                i,
                upstream,
                nested;

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
        }
    });
}(jQuery));
