#! /usr/bin/env python

import os
import subprocess
import sys
import time

from pulselib import *

MASTER_PORT = 7688
AGENT_PORT = 7689


def log(message):
    print '%s: %s' % (time.strftime('%d %b %Y %H:%M:%S'), message)
    
    
def getRoot(programName):
    path = os.path.split(programName)[0]
    path = os.path.join(path, '..')
    return os.path.normpath(path)
    
    
def removeDir(dead):
    for root, dirs, files in os.walk(dead, topdown=False):
        for name in files:
            os.remove(os.path.join(root, name))
        for name in dirs:
            os.rmdir(os.path.join(root, name))
    os.rmdir(dead)
        
        
def createTestDir():
    name = 'agent-upgrade-test'
    if os.path.exists(name):
        removeDir(name)
    os.makedirs(name)
    return name
    
    
def unpackAndStart(packageFile, unpackDir, port, service):        
    package = PulsePackage(packageFile)
    base = package.extractTo(unpackDir)
    server = Pulse(base, port)
    server.start(service=service)
    return server


def setupMaster(port):
    ret = subprocess.call(['ant', '-Dmaster.port=' + str(port), 'setup.master'])
    if ret != 0:
        raise Exception('Master setup failed')
    
    
def testNormalUpgrade(agent, master, build):
    # Check the agent build
    log('Checking agent build...')
    agentProxy = agent.getServerProxy()
    agentBuild = agentProxy.RemoteApi.getBuildNumber(agent.getAdminToken())
    if agentBuild != 101999000:
        raise Exception('Unexpected agent build "' + agentBuild + '"')
    log('Agent build OK')
    
    log('Adding agent...')
    masterProxy = master.getServerProxy()
    if not masterProxy.RemoteApi.addAgent(master.getAdminToken(), 'upgrade-agent', 'localhost', AGENT_PORT):
        raise Exception('Unable to add agent')
    log('Agent added')
    
    # Now we just assume that the agent will begin upgrading.  Periodically
    # check the status of the agent until it is online or we have waited too
    # long.
    log('Waiting for agent to upgrade...')
    startTime = time.time()
    endTime = startTime + 300 # 5 minutes
    while time.time() < endTime:
        time.sleep(5)
        agentStatus = masterProxy.RemoteApi.getAgentStatus(master.getAdminToken(), 'upgrade-agent')
        if agentStatus == 'idle':
            # Wow, we made it
            elapsed = time.time() - startTime
            log('Agent upgrade complete (%.2f seconds)' % elapsed)
            log('Checking new agent build number...')
            agentBuild = agentProxy.RemoteApi.getBuildNumber(agent.getAdminToken())
            if agentBuild != build:
                raise Exception('Unexpected agent build after upgrade "' + agentBuild + '"')
            log('Build number matches!')
            return
        else:
            log('Agent status is "%s", still waiting...' % agentStatus)
    
    # If we got here we timed out
    raise Exception('Agent upgrade time out')
    
    
def upgradeTest(version, build, service):
    buildDir = 'build'
    masterPackageFile = os.path.join(buildDir, 'pulse-' + version + '.tar.gz')
    if not os.path.isfile(masterPackageFile):
        raise Exception('Master package file "' + masterPackageFile + '" does not exist')

    oldAgentPackageFile = os.path.join('master', 'src', 'acceptance', 'data', 'pulse-agent-1.1.999.zip')
    if not os.path.isfile(oldAgentPackageFile):
        raise Exception('Old agent package "' + oldAgentPackageFile + '" does not exist')

    testDir = createTestDir()
    agent = None
    master = None
    try:
        log('Starting old agent...')
        agent = unpackAndStart(oldAgentPackageFile, testDir, AGENT_PORT, service)
        log('Old agent started.')
        log('Starting master...')
        master = unpackAndStart(masterPackageFile, testDir, MASTER_PORT, service)
        log('Master started.')
        
        log('Setting up master...')
        setupMaster(MASTER_PORT)
        log('Master set up.')        
        
        testNormalUpgrade(agent, master, build)
        
        log('Stopping agent...')
        agent.stop(service=service)
        agent = None
        log('Agent stopped...')
        log('Stopping master...')
        master.stop(service=service)
        master = None
        log('Master stopped.')
        
        log('Agent upgrade completed successfully!')
    finally:
        if agent is not None:
            agent.cleanup(service=service)
        if master is not None:
            master.cleanup(service=service)


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print >> sys.stderr, "Usage: %s <version> <build number> [ service ]" % sys.argv[0]
        sys.exit(1)
        
    service = False
    if len(sys.argv) > 3 and sys.argv[3] == "service":
        log('Running as a service')
        service = True
       
    rootDir = getRoot(sys.argv[0])
    os.chdir(rootDir)
    version = sys.argv[1]
    build = int(sys.argv[2])
    upgradeTest(version, build, service)
