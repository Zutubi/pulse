// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./OverviewPanel.js

(function($)
{
    var OverviewPanel = Zutubi.admin.OverviewPanel,
        ns = ".kendoZaOverviewPanel",
        CLICK = "click" + ns;

    Zutubi.admin.ProjectOverviewPanel = OverviewPanel.extend({
        init: function(options)
        {
            var that = this,
                project = options.project;

            options = jQuery.extend({
                scope: "projects",
                item: project
            }, that.options, options);

            that.scmTemplate = kendo.template(that.options.scmTemplate);

            OverviewPanel.fn.init.call(this, options);
        },

        options: {
            id: "project-overview",
            scmTemplate: '<strong>#: type #</strong> :: #: summary #'
        },

        destroy: function()
        {
            this.view.element.find(".k-overview-error").off(ns);
            this.view.element.find(".k-overview-suggestion").off(ns);
            OverviewPanel.fn.destroy.call(this);
        },

        showValidationErrors: function(instance)
        {
            var errors = instance.validationErrors;

            // We override this to filter out errors that we render already in the summary table.
            if (errors && errors.hasOwnProperty(""))
            {
                errors[""] = jQuery.grep(errors[""], function(error)
                {
                    return error.indexOf("must be configured to complete this project") < 0;
                });
            }

            OverviewPanel.fn.showValidationErrors.call(this, instance);
        },

        _addSummary: function(tableEl)
        {
            this._addRow(tableEl, "scm", "scm", this._addScmSummary);
            this._addRow(tableEl, "type", "recipes", this._addRecipesSummary);
            this._addRow(tableEl, "stages", "build stages", this._addStagesSummary);
            this._addRow(tableEl, "dependencies", "dependencies", this._addDependenciesSummary);
        },

        _addMissingConfigLink: function(cellEl, property)
        {
            var anchor = $('<a class="k-overview-error">required ' + property + ' configuration is missing, click to configure</a>');
            cellEl.append(anchor);
            anchor.on(CLICK, jQuery.proxy(this._editClicked, this, property));
        },

        _addSuggestedConfigLink: function(parentEl, text, path)
        {
            var anchor = $('<a class="k-overview-suggestion">' + kendo.htmlEncode(text) + '</a>');
            parentEl.append(anchor);
            anchor.on(CLICK, jQuery.proxy(this._editClicked, this, path));
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
            else if (this.options.project.concrete)
            {
                this._addMissingConfigLink(cellEl, "scm");
            }
            else
            {
                cellEl.append('<span class="k-understated">not configured</span>');
            }
        },

        _addRecipesSummary: function(cellEl)
        {
            var type = this.getNested(this.options.project, "type"),
                symbolicName,
                recipes;

            if (type)
            {
                symbolicName = type.type.symbolicName;
                if (symbolicName === "zutubi.multiRecipeTypeConfig")
                {
                    recipes = this.getNested(type, "recipes");
                    if (recipes && recipes.nested && recipes.nested.length > 0)
                    {
                        this._addMultiRecipeSummary(recipes.nested, cellEl);
                    }
                    else if (this.options.project.concrete)
                    {
                        this._addSuggestedConfigLink(cellEl, "built-in type with no recipes defined, click to configure", "type/recipes");
                    }
                    else
                    {
                        cellEl.append('<span class="k-understated">built-in project type :: no recipes configured</span>');
                    }
                }
                else if (symbolicName === "zutubi.customTypeConfig")
                {
                    cellEl.append("custom XML file");
                }
                else if (symbolicName === "zutubi.versionedTypeConfig")
                {
                    cellEl.append("versioned XML file :: " + kendo.htmlEncode(type.properties.pulseFileName || "[default]"));
                }
            }
            else if (this.options.project.concrete)
            {
                this._addMissingConfigLink(cellEl, "type");
            }
            else
            {
                cellEl.append('<span class="k-understated">project type not configured</span>');
            }
        },

        _addMultiRecipeSummary: function(recipes, cellEl)
        {
            var tableEl,
                i,
                recipe,
                commands;

            if (this.options.project.concrete && recipes.length === 1)
            {
                commands = this.getNested(recipes[0], "commands");
                if (!commands || !commands.nested || commands.nested.length === 0)
                {
                    if (Zutubi.admin.lastAddedPath() === "projects/" + this.options.project.properties.name)
                    {
                        Zutubi.admin.clearLastAddedPath();
                        this._promptForCommands(recipes[0]);
                    }
                }
            }

            tableEl = this._addSubtable(cellEl);
            for (i = 0; i < recipes.length; i++)
            {
                recipe = recipes[i];
                this._addRow(tableEl, "type/recipes/" + recipe.properties.name, recipe.properties.name, jQuery.proxy(this._addRecipeSummary, this, recipe));
            }
        },

        _addRecipeSummary: function(recipe, cellEl)
        {
            var commands = this.getNested(recipe, "commands"),
                plural = "";

            if (commands && commands.nested && commands.nested.length > 0)
            {
                if (commands.nested.length > 1)
                {
                    plural = "s";
                }

                cellEl.append(String(commands.nested.length) + " command" + plural + " :: [" + this._collectNames(commands.nested) + "]");
            }
            else if(this.options.project.concrete)
            {
                this._addSuggestedConfigLink(cellEl, "recipe has no commands defined, click to configure", "type/recipes/" + recipe.properties.name);
            }
            else
            {
                cellEl.append('<span class="k-understated">no commands configured</span>');
            }
        },

        _collectNames: function(collection)
        {
            var result = "",
                i;

            for (i = 0; i < Math.min(collection.length, 4); i++)
            {
                if (result)
                {
                    result += ", ";
                }

                result += kendo.htmlEncode(collection[i].properties.name);
            }

            if (collection.length > 4)
            {
                result += ", ...";
            }

            return result;
        },

        _promptForCommands: function(recipe)
        {
            // A little special case of workflow for adding a new concrete built-in project, that
            // guides the user to create commands.
            var pw = new Zutubi.core.PromptWindow({
                title: "configure commands",
                messageHTML: "You have added a new concrete project with a default empty recipe, would you like to add some commands to the recipe now?",
                buttons: [{
                    label: "configure recipe",
                    spriteCssClass: "fa fa-check-circle",
                    value: true
                }, {
                    label: "no, thanks",
                    spriteCssClass: "fa fa-times-circle",
                    value: false
                }],

                select: jQuery.proxy(this._commandPromptSelect, this, recipe)
            });
            pw.show();
        },

        _commandPromptSelect: function(recipe, ok)
        {
            if (ok)
            {
                this._editClicked("type/recipes/" + recipe.properties.name);
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
            else if (this.options.project.concrete)
            {
                this._addSuggestedConfigLink(cellEl, "no build stages defined, click to configure", "stages");
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
                agent = Zutubi.config.subPath(agent, 1, 2);
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
