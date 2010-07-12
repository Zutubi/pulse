#! /usr/bin/env python

from xmlrpclib import ServerProxy, Error
import sys
import random
import getopt

VERBOSE = False

GLOBAL_PROJECT_TEMPLATE = 'global project template'
GLOBAL_AGENT_TEMPLATE   = 'global agent template'

def get_agent_path(name):
    return 'agents/' + name

def create_agent(api, token, name, host, port):

    if VERBOSE:
        print 'Creating agent ' + name

    agent = api.createDefaultConfig(token, 'zutubi.agentConfig')
    agent['name'] = name
    agent['host'] = host
    agent['port'] = port

    api.insertTemplatedConfig(token, get_agent_path(GLOBAL_AGENT_TEMPLATE), agent, False)

def agent_exists(api, token, name):

    return api.configPathExists(token, get_agent_path(name))

def create_agents(api, token, numAgents):

    host = 'localhost'
    name_template = 'Agent %(port)i'

    for port in range(8890, 8890 + numAgents):
        name = name_template % {'port':port}
        if not agent_exists(api, token, name):
            create_agent(api, token, name, 'localhost', port)

def get_project_path(name):
    return 'projects/' + name

def create_template_project(api, token, name, parent):

    if VERBOSE:
        print 'Creating template project ' + name

    project = api.createDefaultConfig(token, 'zutubi.projectConfig')
    project['name'] = name
    api.insertTemplatedConfig(token, get_project_path(parent), project, True)

def create_project(api, token, name, parent):

    if VERBOSE:
        print 'Creating project ' + name

    scm = api.createDefaultConfig(token, 'zutubi.subversionConfig')
    scm['url'] = 'svn://localhost:3088/accept/trunk/triviant'

    type = api.createDefaultConfig(token, 'zutubi.multiRecipeTypeConfig')
    type['defaultRecipe'] = 'default'

    project = api.createDefaultConfig(token, 'zutubi.projectConfig')
    project['name'] = name
    project['scm'] = scm
    project['type'] = type
    api.insertTemplatedConfig(token, get_project_path(parent), project, False)

    stagesPath = get_project_path(project['name']) + '/stages'
    stage = api.createDefaultConfig(token, 'zutubi.stageConfig')
    stage['name'] = 'default'
    api.insertConfig(token, stagesPath, stage)

    name_template = 'Stage %(index)02i'

    for index in range(0, 15):
        stage['name'] = name_template % {'index' : index}
        api.insertConfig(token, stagesPath, stage)

    recipesPath = get_project_path(project['name']) + '/type/recipes'
    recipe = api.createDefaultConfig(token, 'zutubi.recipeConfig')
    recipe['name'] = 'default'
    api.insertConfig(token, recipesPath, recipe)

    commandsPath = get_project_path(project['name']) + '/type/recipes/' + recipe['name'] + '/commands'

    sleep = api.createDefaultConfig(token, 'zutubi.sleepCommandConfig')
    sleep['name'] = 'pre build snooze'
    sleep['interval'] = random.randint(0, 250)
    api.insertConfig(token, commandsPath, sleep)

    if random.randint(0, 10) > 2:
        build = api.createDefaultConfig(token, 'zutubi.printCommandConfig')
        build['name'] = 'fake build'
        build['message'] = "I'm building, really I am."
        api.insertConfig(token, commandsPath, build)
    else:
        build = api.createDefaultConfig(token, 'zutubi.antCommandConfig')
        build['name'] = 'ant build'
        api.insertConfig(token, commandsPath, build)

    sleep = api.createDefaultConfig(token, 'zutubi.sleepCommandConfig')
    sleep['name'] = 'post build snooze'
    sleep['interval'] = random.randint(0, 250)
    api.insertConfig(token, commandsPath, sleep)

    triggersPath = get_project_path(project['name']) + '/triggers/every hour'
    trigger = api.getConfig(token, triggersPath)
    trigger['cron'] = '0 %(minuteA)i,%(minuteB)i * * * ?' % {'minuteA' : random.randint(1, 29),
                                                             'minuteB' : random.randint(30, 59)}
    api.saveConfig(token, triggersPath, trigger, True)

def project_exists(api, token, name):

    return api.configPathExists(token, get_project_path(name))

def tweak_global_project_template(api, token):

    if not api.configPathExists(token, get_project_path(GLOBAL_PROJECT_TEMPLATE) + '/triggers/every hour'):

        triggersPath = get_project_path(GLOBAL_PROJECT_TEMPLATE) + '/triggers'
        trigger = api.createDefaultConfig(token, 'zutubi.cronTriggerConfig')
        trigger['name'] = 'every hour'
        trigger['cron'] = '0 0 * * * ?'

        if VERBOSE:
            print 'insert into ' + triggersPath

        api.insertConfig(token, triggersPath, trigger)

    # todo: update the cleanup rules to delete the artifacts but not the builds

def create_projects(api, token, numProjects):

    parent = GLOBAL_PROJECT_TEMPLATE
    for index in range(0, numProjects):

        if random.randint(1, 10) > 1:

            name = 'Project %(index)04i' % {'index':index}
            if not project_exists(api, token, name):
                create_project(api, token, name, parent)

        else:

            name = 'Template Project %(index)04i' % {'index':index}
            if not project_exists(api, token, name):
                create_template_project(api, token, name, GLOBAL_PROJECT_TEMPLATE)
                parent = name

def trigger_project(api, token, name):

    if VERBOSE:
        print 'Triggering project ' + name

    return api.triggerBuild(token, name)


def usage():
    print 'Options:'
    print ' -p arg, --projects arg : the number of projects to be created'
    print ' -a arg, --agents arg   : the number of agents to be created'
    print ' -v, --verbose          : verbose feedback'
    print ' -h, --help             : this message'
    print ' --user arg             : the username for authenticating the connection to the Pulse server'
    print ' --pass arg             : the password for authenticating the connection to the Pulse server'
    print ' --host arg             : the host address of the pulse server, eg: localhost:8080'

    
def main(argv):

    try:
        opts, args = getopt.getopt(argv, "p:a:vh", ["projects=", "agents=", "verbose", "help", "user=", "pass=", "host="])
    except getopt.GetoptError:
        usage()
        sys.exit(2)

    numAgents = 5
    numProjects = 25

    host = "localhost:8080"
    user = "admin"
    password = "admin"

    for opt, arg in opts:
        if opt in ("-h", "--help"):
            usage()
            sys.exit()
        elif opt in ('-v', '--verbose'):
            global VERBOSE
            VERBOSE = True
        elif opt in ("-a", "--agents"):
            numAgents = int(arg)
        elif opt in ("-p", "--projects"):
            numProjects = int(arg)
        elif opt in ("--user"):
            user = arg
        elif opt in ("--pass"):
            password = arg
        elif opt in ("--host"):
            host = arg

    server = ServerProxy("http://" + host + "/xmlrpc")
    api = server.RemoteApi

    token = api.login(user, password)
    try:

        tweak_global_project_template(api, token)
        create_agents(api, token, numAgents)
        create_projects(api, token, numProjects)

    except Error, v:
        print "Error:", v
    finally:
        api.logout(token)


if __name__ == "__main__":

    main(sys.argv[1:])
