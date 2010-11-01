// dependency: zutubi/pulse/namespace.js

/**
 * A namespace for functionality that relates to Pulse projects and builds.
 */
window.Zutubi.pulse.project = window.Zutubi.pulse.project || {
    /**
     * Returns the src attribute value for an image of a given type and value.
     *
     * @param type  designates what type of image (which translates to where it is located)
     * @param value the pretty form of an enumeration constant identifying the specific image
     */
    imageSource: function(type, value) {
        return window.baseUrl + '/images/' + type + '/' + value.replace(' ', '').toLowerCase() + '.gif';
    },
    
    /**
     * Returns an HTML fragment with an image and label, for example a build status icon and
     * pretty status value.
     *
     * @param type  designates the type of value (e.g. 'health', 'status')
     * @param value the label in the final fragment (usually the pretty form of an enumeration
     *              constant)
     */
    imageLabel: function(type, value) {
        return '<img alt="' + value + '" src="' + Zutubi.pulse.project.imageSource(type, value) + '"/> ' + value;    
    },
    
    /**
     * A collection of renderers: functions that take a value, and perhaps some context, and
     * render that value into an HTML fragment.  For example, renderers are supplied for various
     * project and build properties like health and revisions.
     */
    renderers: {
        link: function(value, data) {
            var result = '';
            if (data.link)
            {
                result += '<a href="' + window.baseUrl + data.link + '">'; 
            }
            
            result += Ext.util.Format.htmlEncode(value);
             
             if (data.link)
             {
                 result += '</a>';
             }
             
             return result;
        },

        DATE_TEMPLATE: new Ext.XTemplate(
            '<a href="#" class="unadorned" title="{absolute}" onclick="toggleDisplay(\'relative-{id}\'); toggleDisplay(\'absolute-{id}\'); return false;">' +
                '<img alt="toggle format" src="{[window.baseUrl]}/images/calendar.gif"/>' +
            '</a> ' +
            '<span id="relative-{id}">{relative}</span>' +
            '<span id="absolute-{id}" style="display: none">{absolute}</span>'
        ),
        
        date: function(date) {
            // e.g. {relative: '3 hours ago', absolute: '12/03/2010 10:01:33pm' }
            return Zutubi.pulse.project.renderers.DATE_TEMPLATE.apply({
                id: Ext.id(),
                absolute: date.absolute,
                relative: date.relative
            });
        },
        
        health: function(health) {
            return Zutubi.pulse.project.imageLabel('health', health);
        },

        project: function(name) {
            return '<a href="' + window.baseUrl + '/browse/projects/' + encodeURIComponent(name) + '">' +
                       Ext.util.Format.htmlEncode(name) +
                   '</a>';
        },

        revision: function(revision) {
            if (revision)
            {
                var result = '';
                var abbreviate = revision.revisionString.length > 10;
                if (abbreviate)
                {
                    result += '<span title="' + Ext.util.Format.htmlEncode(revision.revisionString) + '">';
                }

                if (revision.link)
                {
                    result += '<a href="' + revision.link + '">';
                }

                result += Ext.util.Format.htmlEncode(abbreviate ? revision.revisionString.substring(0, 7) + '...' : revision.revisionString);

                if (revision.link)
                {
                    result += '</a>';
                }

                if (abbreviate)
                {
                    result += '</span>';
                }

                return result;
            }
            else
            {
                return 'none';
            }
        },
                
        PAUSE_TEMPLATE: new Ext.XTemplate(
            '<a class="unadorned" href="{[window.baseUrl]}/projectState.action?projectName={encodedProjectName}&amp;pause={pause}">' +
                '<img src="{[window.baseUrl]}/images/{icon}.gif"/> ' +
                '{label}' +
            '</a>'
        ),

        projectState: function(state, project) {
            // e.g. { pretty: 'idle', canPause: true, canResume: false }
            var result = state.pretty;
            if (state.canPause)
            {
                result += '&nbsp;&nbsp;' + Zutubi.pulse.project.renderers.PAUSE_TEMPLATE.apply({
                    encodedProjectName: encodeURIComponent(project.name),
                    pause: true,
                    label: 'pause',
                    icon: 'control_pause_blue'
                });
            }
            else if (state.canResume)
            {
                result += '&nbsp;&nbsp;' + Zutubi.pulse.project.renderers.PAUSE_TEMPLATE.apply({
                    encodedProjectName: encodeURIComponent(project.name),
                    pause: false,
                    label: 'resume',
                    icon: 'control_play_blue'
                });
            }
            return result;
        },

        projectSuccessRate: function(value) {
            return '' + value + '% (errors excluded)';
        },
        
        STATISTICS_TEMPLATE: new Ext.XTemplate(
            '<tpl if="ok"><img class="centre" title="{ok} ({percentOk}%) ok" src="{[window.baseUrl]}/images/box-success.gif" height="10" width="{percentOk}"/></tpl>' +
            '<tpl if="failed"><img class="centre" title="{failed} ({percentFailed}%) failed" src="{[window.baseUrl]}/images/box-failure.gif" height="10" width="{percentFailed}"/></tpl>' +
            '<tpl if="error"><img class="centre" title="{error} ({percentError}%) errors" src="{[window.baseUrl]}/images/box-error.gif" height="10" width="{percentError}"/></tpl>' +
            '<br/>{total} builds (ok: {ok}, f: {failed}, e: {error})'
        ),
        
        projectStatistics: function(statistics) {
            // e.g. { total: 9, ok: 6, failed: 1 }
            if (statistics.total == 0)
            {
                return '<span class="understated">no builds</span>';
            }

            var getPercent = function(n) {
                return (n * 100 / statistics.total).toFixed(0);
            };
            
            var error = statistics.total - statistics.ok - statistics.failed;
            return Zutubi.pulse.project.renderers.STATISTICS_TEMPLATE.apply({
                baseUrl: window.baseUrl,
                ok: statistics.ok,
                percentOk: getPercent(statistics.ok),
                failed: statistics.failed,
                percentFailed: getPercent(statistics.failed),
                error: error,
                percentError: getPercent(error),
                total: statistics.total
            });
        },

        ID_TEMPLATE: new Ext.XTemplate(
            '<a href="{link}">build {number}</a>&nbsp;' +
            '<a class="unadorned" id="bactions-{id}-link" onclick="Zutubi.MenuManager.toggleMenu(this); return false">' +
                '<img src="{[window.baseUrl]}/images/default/s.gif" class="popdown floating-widget" id="bactions-{id}-button" alt="build menu"/>' +
            '</a>'
        ),
        
        buildId: function(number, build) {
            if (number > 0)
            {
                if (build.link)
                {
                    Zutubi.MenuManager.registerMenu('bactions-' + build.uniqueId, getBuildMenuItems.createDelegate(this, [build.link]));
                    return Zutubi.pulse.project.renderers.ID_TEMPLATE.apply({
                        number: number,
                        id: build.uniqueId,
                        link: window.baseUrl + '/' + build.link
                    });
                }
                else
                {
                    return 'build ' + number;
                }
            }
            else
            {
                return '[pending]';
            }
        },
        
        buildOwner: function(owner, build) {
            if (build.personal)
            {
                return '<img alt="personal" src="' + window.baseUrl + '/images/user.gif"/> ' +
                       Ext.util.Format.htmlEncode(owner);
            }
            else
            {
                return  Zutubi.pulse.project.renderers.project(owner);
            }
        },

        buildStatus: function(status, build) {
            if (status == 'in progress' && build.elapsed && build.elapsed.prettyEstimatedTimeRemaining)
            {
                return '<img alt="in progress" src="' + window.baseUrl + '/images/status/inprogress.gif"/> ' +
                       Zutubi.pulse.project.renderers.buildElapsed(build.elapsed, build);
            }
            else if (status == 'queued' && build.prettyQueueTime)
            {
                return '<img alt="queued" src="' + window.baseUrl + '/images/status/queued.gif"/> ' +
                       'queued (' + build.prettyQueueTime + ')';
            }
            else
            {
                return Zutubi.pulse.project.imageLabel('status', status);
            }
        },
        
        buildRevision: function(revision, build) {
            // e.g. { revisionString: '1234' [, link: 'link to change viewer'] }
            if (build.personal)
            {
                return 'personal';
            }
            else
            {
                return Zutubi.pulse.project.renderers.revision(revision);
            }
        },
        
        buildTests: function(testSummary, build)
        {
            return Zutubi.pulse.project.renderers.link(testSummary, {link: build.link + 'tests/'});
        },
        
        ELAPSED_TEMPLATE: new Ext.XTemplate(
            '<tpl if="percentComplete &gt; 0"><img class="centre" title="{prettyElapsed} ({percentComplete}%) elapsed" src="{[window.baseUrl]}/images/box-elapsed.gif" height="10" width="{percentComplete}"/></tpl>' +
            '<tpl if="percentRemaining &gt; 0"><img class="centre" title="{prettyEstimatedTimeRemaining} ({percentRemaining}%) remaining" src="{[window.baseUrl]}/images/box-remaining.gif" height="10" width="{percentRemaining}"/></tpl>'
        ),
        
        buildElapsed: function(stamps)
        {
            // e.g. { prettyElapsed: '3 mins 32 secs' [, estimatedPercentComplete: 90, prettyEstimatedTimeRemaining: '21 secs']}
            if (stamps.prettyEstimatedTimeRemaining)
            {
                return Zutubi.pulse.project.renderers.ELAPSED_TEMPLATE.apply({
                    prettyElapsed: stamps.prettyElapsed,
                    prettyEstimatedTimeRemaining: stamps.prettyEstimatedTimeRemaining,
                    percentComplete: stamps.estimatedPercentComplete,
                    percentRemaining: 100 - stamps.estimatedPercentComplete
                });
            }
            else
            {
                return stamps.prettyElapsed;
            }
        },
        
        buildFeatureCount: function(count)
        {
            if (count < -0)
            {
                return 'n/a';
            }
            else
            {
                return '' + count;
            }
        },
        
        STAGE_TEMPLATE: new Ext.XTemplate(
            '<li>' +
                '<img alt="{status}" src="{source}"/> ' +
                '<a href="{detailsLink}">{name:htmlEncode}</a> ' +
                '<span class="understated">//</span> ' +
                '<a class="unadorned" href="{logLink}"><img alt="view stage log" src="{[window.baseUrl]}/images/script.gif"/></a> ' +
                '<a href="{logLink}">log</a>' +
            '</li>'
        ),
        
        buildStages: function(stages, build)
        {
            if (stages && stages.length > 0)
            {
                var result = '<ul class="actions">';
                for (var i = 0, l = stages.length; i < l; i++)
                {
                    var stage = stages[i];
                    result += Zutubi.pulse.project.renderers.STAGE_TEMPLATE.apply({
                        name: stage.name,
                        detailsLink: window.baseUrl + '/' + build.link + 'details/' + encodeURIComponent(stage.name) + '/',
                        logLink: window.baseUrl + '/' + build.link + 'logs/stage/' + encodeURIComponent(stage.name) + '/',
                        status: stage.status,
                        source: Zutubi.pulse.project.imageSource('status', stage.status)
                    });
                }
                
                result += '</ul>';
                return result;
            }
            else
            {
                return '<span class="understated">no stages</span>';
            }
        },

        COMMENT_TEMPLATE: new Ext.XTemplate(
            '{abbreviated} ' +
            '<a class="unadorned" id="comment-{id}-link" onclick="Zutubi.FloatManager.showHideFloat(\'comments\', \'comment-{id}\'); return false">' +
                '<img src="{[window.baseUrl]}/images/default/s.gif" class="popdown floating-widget" id="comment-{id}-button" alt="full comment"/>' +
            '</a>' +
            '<div id="comment-{id}" style="display: none">' +
            '<table class="content" style="margin: 0">' +
                '<tr>' +
                    '<th class="heading" colspan="5">' +
                        '<span class="action">' +
                            '<a href="#" onclick="Zutubi.FloatManager.showHideFloat(\'comments\', \'comment-{id}\'); return false;"><img alt="close" src="{[window.baseUrl]}/images/delete.gif"/>close</a>' +
                        '</span>' +
                        'comment ' +
                    '</th>' +
                '</tr>' +
                '<tr>' +
                    '<td><pre>{comment}</pre></td>' +
                '</tr>' +
            '</table>' +
            '</div>'
        ),

        changelistComment: function(comment, changelist)
        {
            if (comment.comment)
            {
                if (comment.abbreviated)
                {
                    return Zutubi.pulse.project.renderers.COMMENT_TEMPLATE.apply({
                        abbreviated: comment.abbreviated,
                        comment: comment.comment,
                        id: changelist.id
                    });
                }
                else
                {
                    return comment.comment;
                }
            }
            else
            {
                return '<span class="understated">no comment</span>';
            }
        },

        changelistActions: function(id)
        {
            return '<a href="../changes/' + id + '">view</a>';
        }
    }
};

Ext.apply(Zutubi.pulse.project, {
    /**
     * A collection of KeyValue configurations that can be used to build tables for displaying
     * projects, builds, changelists etc.
     */
    configs: {
        /**
         * Configurations for build properties.  The names correspond to custom column keys stored
         * in user preferences.
         */
        build: {
            id: {
                name: 'id',
                key: 'build id',
                cls: 'right',
                renderer: Zutubi.pulse.project.renderers.buildId
            },
            
            project: {
                name: 'project',
                renderer: Zutubi.pulse.project.renderers.project
            },

            owner: {
                name: 'owner',
                renderer: Zutubi.pulse.project.renderers.buildOwner
            },
            
            status: {
                name: 'status',
                renderer: Zutubi.pulse.project.renderers.buildStatus
            },
            
            reason: {
                name: 'reason',
                renderer: Ext.util.Format.htmlEncode
            },
            
            rev: {
                name: 'rev',
                key: 'revision',
                renderer: Zutubi.pulse.project.renderers.buildRevision
            },
            
            tests: {
                name: 'tests',
                renderer: Zutubi.pulse.project.renderers.buildTests
            },

            errors: {
                name: 'errors',
                renderer: Zutubi.pulse.project.renderers.buildFeatureCount
            },

            warnings: {
                name: 'warnings',
                renderer: Zutubi.pulse.project.renderers.buildFeatureCount
            },

            when: {
                name: 'when',
                renderer: Zutubi.pulse.project.renderers.date
            },

            completed: {
                name: 'completed',
                renderer: Zutubi.pulse.project.renderers.date
            },

            elapsed: {
                name: 'elapsed',
                renderer: Zutubi.pulse.project.renderers.buildElapsed
            },

            stages: {
                name: 'stages',
                renderer: Zutubi.pulse.project.renderers.buildStages
            }
        },

        /**
         * Configurations for changelist properties.
         */
        changelist: {
            rev: {
                name: 'revision',
                renderer: Zutubi.pulse.project.renderers.revision
            },

            when: {
                name: 'when',
                renderer: Zutubi.pulse.project.renderers.date
            },

            comment: {
                name: 'comment',
                renderer: Zutubi.pulse.project.renderers.changelistComment
            },

            actions: {
                name: 'id',
                key: 'actions',
                renderer: Zutubi.pulse.project.renderers.changelistActions
            }
        }
    }
});
