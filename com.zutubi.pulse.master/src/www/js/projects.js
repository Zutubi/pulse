// dependency: ext/package.js
// dependency: ext/StatusBar.js
// dependency: zutubi.js

Zutubi.PROJECT_CELLS = '<td class="monitoring-{monitoring}" rowspan="{rowspan}"><img src="{base}/images/health/{health}.gif"/></td>' +
                       '<td class="fit-width" rowspan="{rowspan}">' +
                           '<img alt="-" src="{base}/images/default/s.gif" class="project-name" style="width: {indent}px; height: 16px;"/> ' +
                           '<span class="nowrap"><a href="{homeLink}">{name:htmlEncode}</a></span>' +
                           '<tpl if="responsibleMessage">' +
                               ' <img alt="fixing" id="{id}_fixing" src="{base}/images/config/actions/takeResponsibility.gif" ext:qtip="{responsibleMessage:htmlEncode}' +
                               '<tpl if="responsibleComment">' +
                                   ': {responsibleComment:htmlEncode}' +
                               '</tpl>' +
                               '"/>' +
                           '</tpl>' +
                       '</td>' +
                       '<td class="actions-menu project-actions" rowspan="{rowspan}">' +
                           '<a class="unadorned" id="{id}-actions-link" onclick="Zutubi.MenuManager.toggleMenu(this); return false">' +
                               '<img src="{base}/images/default/s.gif" class="popdown floating-widget" id="{id}-actions-button" alt="project menu"/>' +
                           '</a>' +
                       '</td>';

Zutubi.BUILD_CELLS = '<td class="build-id fit-width">' +
                         '<a id="{buildId}-link" href="{buildLink}">build {buildNumber}</a>' +
                         '<tpl if="commentsTip">&nbsp;<img ext:qtip="{commentsTip}" src="{[window.baseUrl]}/images/comment.gif"/></tpl>' +
                     '</td>' +
                     '<td class="actions-menu">' +
                         '<a class="unadorned" id="{buildId}-bactions-link" onclick="Zutubi.MenuManager.toggleMenu(this); return false">' +
                             '<img src="{base}/images/default/s.gif" class="popdown floating-widget" id="{buildId}-bactions-button" alt="build menu"/>' +
                         '</a>' +
                     '</td>' +
                     '<td class="build-status fit-width">' +
                         ':: <span id="{buildId}.status"><img alt="status" src="{base}/images/{statusIcon}"/> {status}</span>' +
                     '</td>' +
                     '<tpl for="columns"><td class="build-column">{.} <tpl if="xindex &lt; xcount"></tpl></td></tpl>';

Zutubi.NO_BUILD_CELLS = '<td colspan="{noBuildSpan}" class="understated">this project has never been built</td>';

Zutubi.ConcreteProject = function(data, columnCount, showHideLinks) {
    this.data = data;
    this.columnCount = columnCount;
    this.showHideLinks = showHideLinks;
    this.hidden = false;
    this.rows = [];
};

Zutubi.ConcreteProject.prototype = {
    buildTemplate: new Ext.XTemplate('<tr class="project-row project-health-{health}" id="{id}">' + Zutubi.PROJECT_CELLS + Zutubi.BUILD_CELLS + '</tr>').compile(),

    noBuildTemplate: new Ext.XTemplate('<tr class="project-row project-health-{health}" id="{id}">' + Zutubi.PROJECT_CELLS + Zutubi.NO_BUILD_CELLS + '</tr>').compile(),

    noProjectTemplate: new Ext.XTemplate('<tr class="project-row project-health-{health}" id="{buildId}">' + Zutubi.BUILD_CELLS + '</tr>').compile(),

    addBuildData: function(templateData, build, index) {
        templateData.buildId = 'b' + String(index + 1) + '.' + templateData.id;
        templateData.buildNumber = build.number;
        templateData.buildLink = window.baseUrl + '/browse/projects/' + encodeURIComponent(templateData.name) + '/builds/' + build.number + '/';
        templateData.status = build.status;
        templateData.statusIcon = build.statusIcon;
        templateData.commentsTip = getCommentsTooltip(build);
        templateData.columns = build.columns;
    },

    render: function(previousRowEl) {
        var templateData, builds, i, build;

        templateData = {
            id: this.data.id,
            health: this.data.health,
            monitoring: this.data.monitoring.toLowerCase(),
            responsibleMessage: this.data.responsibleMessage,
            responsibleComment: this.data.responsibleComment,
            rowspan: this.data.built ? this.data.buildRows.length : 1,
            base: window.baseUrl,
            indent: this.data.depth * 12,
            homeLink: window.baseUrl + '/' + this.getProjectTabLink('home'),
            name: this.data.name,
            noBuildSpan: this.columnCount + 3
        };

        // This is slightly more complex than adding the project row and then
        // selectively choosing to add the build cells to it only if there is
        // a build available - but adding cells fails in IE7.
        if (this.data.built)
        {
            builds = this.data.buildRows;

            this.addBuildData(templateData, builds[0], 0);
            this.el = this.buildTemplate.insertAfter(previousRowEl, templateData, true);
            this.rows.push(this.el);

            for (i = 0; i < builds.length; i++)
            {
                build = builds[i];
                this.addBuildData(templateData, build, i);

                // First build is already rendered in the project row above.
                if (i > 0)
                {
                    this.rows.push(this.noProjectTemplate.insertAfter(this.rows[this.rows.length - 1], templateData, true));
                }

                Zutubi.MenuManager.registerMenu(templateData.buildId + '-bactions', this.getMenuItems.createDelegate(this, ['bactions', templateData.buildNumber, templateData.buildId + '-bactions']));
            }
        }
        else
        {
            this.el = this.noBuildTemplate.insertAfter(previousRowEl, templateData, true);
            this.rows.push(this.el);
        }

        Zutubi.MenuManager.registerMenu(this.data.id + '-actions', this.getMenuItems.createDelegate(this, ['actions', '', this.data.id + '-actions']));
        return this.rows[this.rows.length - 1];
    },

    setRowsHidden: function(hide) {
        var i;
        for (i = 0; i < this.rows.length; i++)
        {
            if (hide)
            {
                this.rows[i].addClass('project-hidden');
            }
            else
            {
                this.rows[i].removeClass('project-hidden');
            }
        }
    },

    hide: function() {
        if (!this.hidden)
        {
            this.hidden = true;
            this.setRowsHidden(true);
        }
    },

    show: function() {
        if (this.hidden)
        {
            this.hidden = false;
            this.setRowsHidden(false);
        }
    },

    getProjectTabLink: function(tab) {
        return 'browse/projects/' + encodeURIComponent(this.data.name) + '/' + tab + '/';
    },

    generateHandler: function(action, projectId, projectName, domId) {
        var functionName;

        functionName = 'gen_' + action + projectId;
        window[functionName] = function() {
            actionPath('projects/' + projectName, action, false, false);
            Zutubi.FloatManager.showHideFloat('menus', domId);
        };
    
        return functionName + '(); return false;';
    },

    getMenuItems: function(menuType, menuArg, id) {
        var items, encodedName, item;

        if (menuType === 'actions')
        {
            encodedName = encodeURIComponent(this.data.name);
            encodedName = encodedName.replace(/'/g, "%27");
            
            items = [{
                id: 'home',
                url: this.getProjectTabLink('home'),
                image: 'house.gif'
            }, {
                id: 'reports',
                url: this.getProjectTabLink('reports'),
                image: 'chart_bar.gif'
            }, {
                id: 'history',
                url: this.getProjectTabLink('history'),
                image: 'time.gif'
            }, {
                id: 'dependencies',
                url: this.getProjectTabLink('dependencies'),
                image: 'dependencies.gif'
            }, {
                id: 'log',
                url: this.getProjectTabLink('log'),
                image: 'script.gif'
            }, {
                id: 'configuration',
                url: 'admin/projects/' + encodedName + '/',
                image: 'pencil.gif'
            }];

            if (this.data.canTrigger)
            {
                item = {
                    id: 'trigger',
                    image: 'lightning.gif'
                };

                if (this.data.prompt)
                {
                    item.url = 'editBuildProperties!input.action?projectName=' + encodedName;
                }
                else
                {
                    item.onclick = this.generateHandler('trigger', this.data.projectId, this.data.name, id);
                }

                items.push(item);
            }

            if (this.data.canRebuild)
            {
                items.push({
                    id: 'with dependencies',
                    image: 'lightning.gif',
                    onclick: this.generateHandler('rebuild', this.data.projectId, this.data.name, id)
                });
            }

            if (this.showHideLinks)
            {
                items.push({
                    id: 'hide',
                    onclick: 'hideDashboardProject(\'' + encodedName + '\'); return false;',
                    image: 'close.gif'
                });
            }
        }
        else
        {
            items = getBuildMenuItems('browse/projects/' + encodeURIComponent(this.data.name) + '/builds/' + menuArg + '/');
        }

        return items;
    },

    destroy: function() {
        var i, key;

        for (i = 0; i < this.rows.length; i++)
        {
            this.rows[i].select('a').removeAllListeners();
            this.rows[i].select('span').removeAllListeners();
        }
    }
};

Zutubi.ProjectContainer = function(model, columnCount, showHideLinks) {
    this.model = model;
    this.children = [];
    this.hidden = false;
    this.childrenRendered = false;
    this.columnCount = columnCount;
    this.showHideLinks = showHideLinks;
};

Zutubi.ProjectContainer.prototype = {
    renderChildren: function() {
        var children, previousRowEl, i, childData, child;

        this.childrenRendered = true;

        children = this.model.children;
        if (children)
        {
            previousRowEl = this.el;
            for (i = 0; i < children.length; i++)
            {
                childData = children[i];
                if (childData.concrete)
                {
                    child = new Zutubi.ConcreteProject(childData, this.columnCount, this.showHideLinks);
                }
                else
                {
                    child = new Zutubi.TemplateProject(childData, this.columnCount, this.showHideLinks);
                }

                previousRowEl = child.render(previousRowEl);
                this.children.push(child);
            }

            return previousRowEl;
        }
        else
        {
            return this.el;
        }
    },

    onClick: function() {
        if(this.model.collapsed)
        {
            this.expand();
        }
        else
        {
            this.collapse();
        }
    },

    collapse: function() {
        if (!this.model.collapsed)
        {
            this.hideDescendants();
            this.model.collapsed = true;
            this.el.addClass('project-collapsed');
        }
    },

    collapseAll: function() {
        var i, child;

        this.collapse();
        for (i = 0; i < this.children.length; i++)
        {
            child = this.children[i];
            if (!child.data.concrete)
            {
                child.collapseAll();
            }
        }
    },

    hide: function() {
        if (!this.hidden)
        {
            this.el.addClass('project-hidden');
            this.hidden = true;
        }

        if (!this.model.collapsed)
        {
            this.hideDescendants();
        }
    },

    hideDescendants: function() {
        var i, child;
        
        for (i = 0; i < this.children.length; i++)
        {
            child = this.children[i];
            child.hide();
        }
    },

    expand: function() {
        if (this.model.collapsed)
        {
            if (!this.childrenRendered)
            {
                this.renderChildren();
            }
            
            this.showDescendants();
            this.model.collapsed = false;
            this.el.removeClass('project-collapsed');
        }
    },

    expandAll: function() {
        var i, child;

        this.expand();
        for (i = 0; i < this.children.length; i++)
        {
            child = this.children[i];
            if (!child.data.concrete)
            {
                child.expandAll();
            }
        }
    },

    show: function() {
        if (this.hidden)
        {
            this.el.removeClass('project-hidden');
            this.hidden = false;
        }

        if (!this.model.collapsed)
        {
            this.showDescendants();
        }
    },

    showDescendants: function() {
        var i, child;
        
        for(i = 0; i < this.children.length; i++)
        {
            child = this.children[i];
            child.show();
        }
    },

    destroy: function() {
        var i, child;

        this.el.removeAllListeners();
        for (i = 0; i < this.children.length; i++)
        {
            child = this.children[i];
            child.destroy();
        }
    }
};

Zutubi.TemplateProject = function(data, columnCount, showHideLinks) {
    Zutubi.TemplateProject.superclass.constructor.call(this, data, columnCount, showHideLinks);
    this.data = data;
};

Ext.extend(Zutubi.TemplateProject, Zutubi.ProjectContainer, {
    template: new Ext.Template(
        '<tr class="project-row {healthStyles} {expandable}" id="{id}">' +
            '<td class="health-{health}">&nbsp;</td>' +
            '<td class="fit-width">' +
                '<img alt="-" src="{base}/images/default/s.gif" class="project-name" style="width: {indent}px; height: 16px;"/> ' +
                '<span class="nowrap">{name:htmlEncode}</span>' +
            '</td>' +
            '<td class="project-template-actions">&nbsp;</td>' +
            '<td class="fit-width" colspan="3" id="{id}.building">{building}</td>' +
            '<td class="build-details" colspan="{columnCount}">' +
                'summary :: ' +
                '<img alt="health: ok" src="{base}/images/health/ok.gif"> ok: {okCount} ' +
                '<img alt="health: warnings" src="{base}/images/health/warnings.gif"> warnings: {warningCount} ' +
                '<img alt="health: broken" src="{base}/images/health/broken.gif"> broken: {brokenCount}' +
            '</td>' +
        '</tr>').compile(),

    render: function(previousRowEl) {
        var children, healthStyles, canExpand, expandable;

        children = this.data.children;
        healthStyles = '';

        if ((this.data.okCount + this.data.unknownCount) === 0)
        {
            healthStyles += ' project-health-no-ok';
        }

        if (this.data.warningCount === 0)
        {
            healthStyles += ' project-health-no-warnings';
        }

        if (this.data.brokenCount === 0)
        {
            healthStyles += ' project-health-no-broken';
        }

        canExpand = children && children.length > 0;
        expandable = canExpand ? 'project-expandable' : '';
        if (canExpand && this.data.collapsed)
        {
            expandable += ' project-collapsed';
        }

        this.el = this.template.insertAfter(previousRowEl, {
            expandable: expandable,
            id: this.data.id,
            healthStyles: healthStyles,
            health: this.data.health,
            base: window.baseUrl,
            indent: this.data.depth * 12,
            name: this.data.name,
            building: this.data.building,
            okCount: this.data.okCount,
            warningCount: this.data.warningCount,
            brokenCount: this.data.brokenCount,
            columnCount: this.columnCount
        }, true);

        if (canExpand)
        {
            this.el.on('click', this.onClick, this);
            this.el.addClassOnOver('project-highlighted');
        }

        if (this.data.collapsed)
        {
            return this.el;
        }
        else
        {
            return this.renderChildren();
        }
    }
});

Zutubi.ProjectGroup = function(data, columnCount, rssEnabled, showHideLinks, last) {
    Zutubi.ProjectGroup.superclass.constructor.call(this, data.root, columnCount, showHideLinks);
    this.data = data;
    this.rssEnabled = rssEnabled;
    this.last = last;
};

Ext.extend(Zutubi.ProjectGroup, Zutubi.ProjectContainer, {
    template: new Ext.XTemplate('<tr class="project-row project-expandable {collapsed}" id="{id}">' +
            '<td class="group-header health-{health}">&nbsp;</td>' +
            '<td class="group-header fit-width" colspan="5"><img src="{base}/images/default/s.gif" alt="-" class="group-name"/> {name:htmlEncode}</td>' +
            '<td class="group-header build-details" colspan="{columnCount}">' +
                '<img alt="health: ok" src="{base}/images/health/ok.gif"> ok: {okCount} ' +
                '<img alt="health: warnings" src="{base}/images/health/warnings.gif"> warnings: {warningCount} ' +
                '<img alt="health: broken" src="{base}/images/health/broken.gif"> broken: {brokenCount}&nbsp;' +
                '<tpl if="rssEnabled"><a class="unadorned" id="rss.builds.{id}" href="{base}/rss.action?groupName={encodedName}" onclick="stopEventPropagation(event);"><img alt="rss" src="{base}/images/feed-icon-16x16.gif"></a>&nbsp;</tpl>' +
                '<tpl if="showHideLinks &amp;&amp; labelled"><a class="unadorned" id="hide.{id}" href="#" onclick="hideDashboardGroup(\'{encodedName}\'); stopEventPropagation(event); return false;"><img alt="hide group" src="{base}/images/close.gif"></a></tpl>' +
            '</td>').compile(),

    render: function(parentEl) {
        var spacerClass,
            encodedName;

        encodedName = encodeURIComponent(this.data.groupName).replace(/'/g, "%27");

        this.el = this.template.append(parentEl, {
            id: this.data.id,
            health: this.data.root.health,
            base: window.baseUrl,
            name: this.data.groupName,
            okCount: this.data.root.okCount,
            warningCount: this.data.root.warningCount,
            brokenCount: this.data.root.brokenCount,
            encodedName: encodedName,
            collapsed: this.model.collapsed ? 'project-collapsed' : '',
            columnCount: this.columnCount,
            rssEnabled: this.rssEnabled,
            showHideLinks: this.showHideLinks,
            labelled: this.data.labelled
        }, true);

        this.el.on('click', this.onClick, this);
        this.el.addClassOnOver('project-highlighted');
        if (!this.model.collapsed)
        {
            this.renderChildren();
        }

        spacerClass = 'project-group-spacer';
        if (this.last)
        {
            spacerClass += ' project-group-last';
        }
        return parentEl.createChild({tag: 'tr', cls: spacerClass, children: [{ tag: 'td', colspan: '6', html: '&nbsp;'}]});
    },

    cleanupHandler: function(prefix)
    {
        var el;

        el = Ext.get(prefix + this.data.id);
        if (el)
        {
            el.dom.onclick = null;
        }
    },

    destroy: function()
    {
        Zutubi.ProjectGroup.superclass.destroy.call(this);
        this.cleanupHandler('rss.builds.');
        this.cleanupHandler('hide.');
    }
});

Zutubi.ProjectsTable = function(containerEl, columnCount, rssEnabled, isDashboard, isAnonymous) {
    this.containerEl = containerEl;
    this.columnCount = columnCount;
    this.rssEnabled = rssEnabled;
    this.isDashboard = isDashboard;
    this.isAnonymous = isAnonymous;
    this.groups = {};
    this.toolbarRendered = false;
};

Zutubi.ProjectsTable.prototype = {
    renderToolbar: function(projectsFilter) {
        var table = this;
        Ext.fly('build-toolbar').setStyle('display', '');
        this.toolbar = new Ext.Toolbar({
            el: 'build-toolbar',
            items: [{
                xtype: 'label',
                text: 'filter:'
            }, ' ', ' ', {
                xtype: 'combo',
                id: 'projects-filter',
                width: 230,
                editable: false,
                mode: 'local',
                triggerAction: 'all',
                store: new Ext.data.ArrayStore({
                    idIndex: 0,
                    fields: [
                        'filter',
                        'text'
                    ],
                    data: [
                        ['', 'show all projects'],
                        ['ok', 'only show healthy projects'],
                        ['warnings', 'only show projects with warnings'],
                        ['broken', 'only show broken projects']
                    ]
                }),
                valueField: 'filter',
                displayField: 'text',
                value: projectsFilter,
                listeners: {
                    select: function(combo, record) {
                        table.applyFilter(record.get('filter'));
                    }
                }
            }, {
                xtype: 'xztblink',
                id: 'save-filter-link',
                hidden: table.isAnonymous,
                text: 'save filter',
                icon: window.baseUrl + '/images/save.gif',
                listeners: {
                    click: function() {
                        table.saveFilter();
                    }
                }
            }, '->', {
                xtype: 'xztblink',
                id: 'expand-all-link',
                text: 'expand all',
                icon: window.baseUrl + '/images/expand.gif',
                listeners: {
                    click: function() {
                        table.expandAll();
                    }
                }
            }, {
                xtype: 'xztblink',
                id: 'collapse-all-link',
                text: 'collapse all',
                icon: window.baseUrl + '/images/collapse.gif',
                listeners: {
                    click: function() {
                        table.collapseAll();
                    }
                }
            }, {
                xtype: 'xztblink',
                id: 'save-layout-link',
                hidden: table.isAnonymous,
                text: 'save layout',
                icon: window.baseUrl + '/images/save.gif',
                listeners: {
                    click: function() {
                        table.saveLayout();
                    }
                }
            }]
        });
        this.toolbar.render();
        this.toolbarRendered = true;
    },

    destroyToolbar: function() {
        this.toolbar.destroy();
        this.toolbarRendered = false;
    },

    applyFilter: function(filter) {
        this.containerEl.removeClass('project-filter-broken');
        this.containerEl.removeClass('project-filter-ok');
        this.containerEl.removeClass('project-filter-warnings');
        if (filter)
        {
            this.containerEl.addClass('project-filter-' + filter);
        }
    },

    saveFilter: function() {
        var toolbarEl;

        toolbarEl = this.toolbar.el;
        toolbarEl.mask('Saving...');
        runAjaxRequest({
            url: window.baseUrl + '/ajax/saveProjectsFilter.action',
            params: { filter: Ext.getCmp('projects-filter').getValue(), dashboard: this.isDashboard },
            callback: function() { toolbarEl.unmask(); }
        });
    },

    expandAll: function() {
        var toolbarEl, groups;

        toolbarEl = this.toolbar.el;
        toolbarEl.mask('Expanding...');
        groups = this.groups;
        (function() {
            var group;

            for(group in groups)
            {
                groups[group].expandAll();
            }
            toolbarEl.unmask();
        }.defer(1));
    },

    collapseAll: function() {
        var toolbarEl, groups;

        toolbarEl = this.toolbar.el;
        toolbarEl.mask('Collapsing...');
        groups = this.groups;
        (function() {
            var group;

            for(group in groups)
            {
                groups[group].collapseAll();
            }
            toolbarEl.unmask();
        }.defer(1));
    },

    getCurrentLayout: function() {
        var layout, groupName, group;

        layout = [];
        for(groupName in this.groups)
        {
            group = this.groups[groupName];
            layout.push({
                group: this.getGroupName(group.data),
                collapsed: group.model.collapsed,
                layout: this.getGroupLayout(group.data)
            });
        }
        return layout;
    },

    saveLayout: function() {
        var toolbarEl;

        toolbarEl = this.toolbar.el;
        toolbarEl.mask('Saving...');
        runAjaxRequest({
            url: window.baseUrl + '/ajax/saveProjectsLayout.action',
            params: { layout: Ext.util.JSON.encode(this.getCurrentLayout()), dashboard: this.isDashboard },
            callback: function() { toolbarEl.unmask(); }
        });
    },

    update: function(projectsFilter, groupsData) {
        var el, previousGroups, key, i, tableEl, groupData, groupName, group;

        el = this.containerEl;
        previousGroups = this.groups;

        for (key in previousGroups)
        {
            previousGroups[key].destroy();
        }

        el.update('<table class="project-group"><tbody></tbody></table>');
        tableEl = el.first().first();

        this.groups = {};
        if (groupsData.length === 0)
        {
            if (this.toolbarRendered)
            {
                this.destroyToolbar();
            }

            el.update('No projects found.');
        }
        else
        {
            if (!this.toolbarRendered)
            {
                this.renderToolbar(projectsFilter);
            }

            for (i = 0; i < groupsData.length; i++)
            {
                groupData = groupsData[i];
                groupName = this.getGroupName(groupData);
                this.applyPreviousState(groupData, previousGroups[groupName]);

                group = new Zutubi.ProjectGroup(groupData, this.columnCount, this.rssEnabled, this.isDashboard, i === groupsData.length - 1);
                group.render(tableEl);
                this.groups[groupName] = group;
            }

            this.applyFilter(projectsFilter);
        }
    },

    getGroupName: function(groupData) {
        if (groupData.labelled)
        {
            return groupData.groupName;
        }
        else
        {
            return '';
        }
    },

    forEachTemplate: function(groupData, f) {
        this.forEachChildTemplate(groupData.root, f);
    },

    forEachChildTemplate: function(templateData, f) {
        var children, i, childData;

        children = templateData.children;
        if (children)
        {
            for (i = 0; i < children.length; i++)
            {
                childData = children[i];
                if (!childData.concrete)
                {
                    f.call(this, childData);
                    this.forEachChildTemplate(childData, f);
                }
            }
        }
    },

    applyPreviousState: function(groupData, previousGroup)
    {
        var groupLayout, previous;

        if (previousGroup)
        {
            groupData.root.collapsed = previousGroup.model.collapsed;

            groupLayout = this.getGroupLayout(previousGroup.data);
            this.forEachTemplate(groupData, function(templateData) {
                previous = groupLayout[templateData.name];
                if (previous !== null)
                {
                    templateData.collapsed = previous;
                }
            });
        }
    },

    getGroupLayout: function(groupData)
    {
        var groupLayout;

        groupLayout = {};
        this.forEachTemplate(groupData, function(templateData) {
            groupLayout[templateData.name] = templateData.collapsed;
        });
        return groupLayout;
    }
};

Zutubi.ChangesTable = function(containerEl, idSuffix, title, showWho) {
    this.containerEl = containerEl;
    this.idSuffix = idSuffix;
    this.title = title;
    this.showWho = showWho;
    this.renderedComments = {};
    this.renderedBuilds = {};
};

Zutubi.ChangesTable.prototype = {
    tableTemplate: new Ext.XTemplate('<table id="{idSuffix}-changes-table" class="two-content"><tbody>' +
                                         '<tr><th class="two-heading" colspan="7">{title}</th></tr>' +
                                         '<tr>' +
                                             '<th class="content leftmost">revision</th>' +
                                             '<tpl if="showWho"><th class="content">who</th></tpl>' +
                                             '<th class="content">when</th>' +
                                             '<th class="content">comment</th>' +
                                             '<th class="content">status</th>' +
                                             '<th class="content">affected builds</th>' +
                                             '<th class="content rightmost">actions</th>' +
                                         '</tr>' +
                                     '</tbody></table>').compile(),

    changeTemplate: new Ext.XTemplate('<tr>' +
                                          '<td class="content leftmost">' +
                                              '<tpl if="url"><a href="{url}"></tpl>' +
                                                  '<tpl if="shortRevision">' +
                                                      '<span title="' +
                                                  '</tpl>' +
                                                  '{revision:htmlEncode}' +
                                              '<tpl if="shortRevision">' +
                                                  '">{shortRevision:htmlEncode}</span>' +
                                              '</tpl>' +
                                              '<tpl if="url"></a></tpl>' +
                                          '</td>' +
                                          '<tpl if="showWho"><td class="content">{who}</td></tpl>' +
                                          '<td class="content">' +
                                              '<a href="#" class="unadorned" title="{date}" onclick="toggleDisplay(\'{id}_{idSuffix}_time\'); toggleDisplay(\'{id}_{idSuffix}_date\'); return false;">' +
                                                  '<img alt="toggle format" src="{base}/images/calendar.gif"/>' +
                                              '</a> ' +
                                              '<span id="{id}_{idSuffix}_time" style="display: {relativeDisplay}">{time}</span>' +
                                              '<span id="{id}_{idSuffix}_date" style="display: {absoluteDisplay}">{date}</span>' +
                                          '</td>' +
                                          '<td class="content" id="${id}_{idSuffix}_cell">' +
                                              '<tpl if="shortComment">' +
                                                  '{shortComment} ' +
                                                  '<span id="{id}-{idSuffix}-comment-link">' +
                                                      '<img src="{base}/images/default/s.gif" class="popdown floating-widget" id="{id}-{idSuffix}-comment-button" alt="show full comment"/>' +
                                                  '</span>' +
                                              '</tpl>' +
                                              '<tpl if="!shortComment">{comment}</tpl>' +
                                          '</td>' +
                                          '<td class="content">' +
                                              '<img alt="{aggregateStatus}" src="{base}/images/{aggregateStatusIcon}"/> {aggregateStatus}' +
                                          '</td>' +
                                          '<td class="content">' +
                                              '<tpl if="buildCount == 0">' +
                                                  'no builds' +
                                              '</tpl>' +
                                              '<tpl if="buildCount == 1">' +
                                                  '<a href="{base}/browse/projects/{[values.builds[0].encodedProject]}/home/">{[Ext.util.Format.htmlEncode(values.builds[0].project)]}</a> :: ' +
                                                  '<a href="{base}/browse/projects/{[values.builds[0].encodedProject]}/builds/{[values.builds[0].number]}/">build {[values.builds[0].number]}</a>' +
                                              '</tpl>' +
                                              '<tpl if="buildCount &gt; 1">' +
                                                  '{buildCount} builds ' +
                                                  '<span id="{id}-{idSuffix}-builds-link">' +
                                                      '<img src="{base}/images/default/s.gif" class="popdown floating-widget" id="{id}-{idSuffix}-builds-button" alt="show all builds"/>' +
                                                  '</span>' +
                                              '</tpl>' +
                                          '</td>' +
                                          '<td class="content rightmost"><a href="{base}/dashboard/changes/{id}/">view</a></td>' +
                                      '</tr>').compile(),

    commentTemplate: new Ext.Template('<div id="{id}-{idSuffix}-comment" style="display: none">' +
                                          '<table class="content" style="margin: 0">' +
                                              '<tr>' +
                                                  '<th class="heading" colspan="5">' +
                                                       '<span class="action">' +
                                                           '<a href="#" onclick="Zutubi.FloatManager.showHideFloat(\'comments\', \'{id}-{idSuffix}-comment\'); return false;">' +
                                                               '<img alt="close" src="{base}/images/delete.gif"/> close' +
                                                           '</a>' +
                                                       '</span>' +
                                                       'comment' +
                                                   '</th>' +
                                               '</tr>' +
                                               '<tr><td><pre>{comment}</pre></td></tr>' +
                                           '</table>' +
                                       '</div>').compile(),

    buildsTemplate: new Ext.XTemplate('<div id="{id}-{idSuffix}-builds" style="display: none">' +
                                          '<table class="content" style="margin: 0">' +
                                              '<tr>' +
                                                  '<th class="heading" colspan="4">' +
                                                      '<span class="action">' +
                                                          '<a href="#" onclick="Zutubi.FloatManager.showHideFloat(\'builds\', \'{id}-{idSuffix}-builds\'); return false;">' +
                                                              '<img alt="close" src="{base}/images/delete.gif"/> close' +
                                                          '</a>' +
                                                      '</span>' +
                                                      'build results' +
                                                  '</th>' +
                                              '</tr>' +
                                              '<tr><th class="content">project</th><th class="content">build id</th><th class="content">status</th></tr>' +
                                              '<tpl for="builds">' +
                                                  '<tr>' +
                                                      '<td class="content"><a href="{base}/browse/projects/{encodedProject}/home/">{project:htmlEncode}</a></td>' +
                                                      '<td class="content-right"><a href="{base}/browse/projects/{encodedProject}/builds/{number}/">{number}</a></td>' +
                                                      '<td class="{statusClass}"><img alt="{status}" src="{base}/images/{statusIcon}"/> {status}</td>' +
                                                  '</tr>' +
                                              '</tpl>' +
                                          '</table>' +
                                      '</div>').compile(),

    update: function(changes) {
        var tableEl, tbodyEl, i, change, commentButton, buildsButton;

        this.destroy();
        this.containerEl.update('');

        if (changes && changes.length > 0)
        {
            tableEl = this.tableTemplate.append(this.containerEl, this, true);
            tbodyEl = tableEl.first();

            for (i = 0; i < changes.length; i++)
            {
                change = changes[i];
                change.base = window.baseUrl;
                change.absoluteDisplay = window.preferences.absoluteTimestamps ? 'inline' : 'none';
                change.relativeDisplay = window.preferences.absoluteTimestamps ? 'none' : 'inline';
                change.idSuffix = this.idSuffix;
                change.showWho = this.showWho;
                if (!change.who)
                {
                    change.who = 'anonymous';
                }

                this.changeTemplate.append(tbodyEl, change);
                if (change.shortComment)
                {
                    commentButton = Ext.get(change.id + '-' + this.idSuffix + '-comment-link');
                    commentButton.on('click', this.toggleComment.createDelegate(this, [change]));
                }

                buildsButton = Ext.get(change.id + '-' + this.idSuffix + '-builds-link');
                if (buildsButton)
                {
                    buildsButton.on('click', this.toggleBuilds.createDelegate(this, [change]));
                }
            }
        }
        else
        {
            this.containerEl.update('No changes to show.');
        }
    },

    toggleComment: function(change) {
        this.renderComment(change);
        Zutubi.FloatManager.showHideFloat('comments', change.id + '-' + this.idSuffix + '-comment');
    },

    renderComment: function(change) {
        if (this.renderedComments[change.id])
        {
            return;
        }

        this.renderedComments[change.id] = this.commentTemplate.append(Ext.getBody(), change, true);
    },

    toggleBuilds: function(change) {
        this.renderBuilds(change);
        Zutubi.FloatManager.showHideFloat('builds', change.id + '-' + this.idSuffix + '-builds');
    },

    renderBuilds: function(change) {
        if (this.renderedBuilds[change.id])
        {
            return;
        }

        this.renderedBuilds[change.id] = this.buildsTemplate.append(Ext.getBody(), change, true);
    },

    destroy: function() {
        var key;

        this.containerEl.select('a').removeAllListeners();
        this.containerEl.select('span').removeAllListeners();

        for (key in this.renderedBuilds)
        {
            this.renderedBuilds[key].remove();
        }

        for (key in this.renderedComments)
        {
            this.renderedComments[key].remove();
        }
    }
};


Zutubi.ActiveView = function(url, refreshInterval, updateFn, updateScope, el, failureMessage) {
    this.url = url;
    this.refreshInterval = refreshInterval;
    this.updateFn = updateFn;
    this.updateScope = updateScope;
    this.el = el;
    this.failureMessage = failureMessage;
    this.initialised = false;
};

Zutubi.ActiveView.prototype = {
    init: function() {
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
    },

    load: function(callback) {
        var view;

        view = this;
        Ext.Ajax.request({
            url: view.url,
            timeout: 120000,
            
            success: function(transport/*, options*/)
            {
                view.updateFn.call(view.updateScope, eval('(' + transport.responseText + ')'));
                view.initialised = true;
                if (callback)
                {
                    callback();
                }
            },

            failure: function(/*transport, options*/)
            {
                // Stop trying to refresh.
                if (view.runner)
                {
                    view.runner.stopAll();
                }

                // If we never initialised the view, show an error message.
                if (!view.initialised)
                {
                    view.el.update(view.failureMessage);
                }

                if (callback)
                {
                    callback();
                }
            }
        });
    }
};
