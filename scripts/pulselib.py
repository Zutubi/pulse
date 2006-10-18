# Utility functions for launching and stopping pulse servers and agents.

import os
import socket
import subprocess
import sys
import time
import xmlrpclib


WINDOWS = os.name == "nt"


def getScript():
    if WINDOWS:
        return "pulse.bat"
    else:
        return "./pulse"
    

class Pulse:
    def __init__(self, baseDir, port):
        self.baseDir = os.path.abspath(baseDir)
        self.port = port
        self.dataDir = None

    def setDataDir(self, dataDir):
        self.dataDir = dataDir
    
    def getEnv(self):
        return {'PULSE_CONFIG': os.path.join(self.baseDir, 'bin', 'config.properties'), 'PULSE_HOME': self.baseDir, 'PULSE_USER': os.getenv('USERNAME'), 'USERNAME': os.getenv('USERNAME'), 'PULSE_PID': os.path.join(self.baseDir, 'pulse.pid'), 'JAVA_HOME': os.getenv('JAVA_HOME')}
    
    
    def start(self, wait=True, service=False):
        """Starts this pulse server.  A local properties file
        is used, output is captured to baseDir/stdout.txt and baseDir/stderr.txt.
        If wait is True, this function will wait for Pulse to start listening on the
        given port before returning.  If the server cannot be started successfully,
        an exception is thrown carrying the reason."""
        oldDir = os.getcwdu()
        os.chdir(os.path.join(self.baseDir, 'bin'))
        try:
            if service:
                configFile = open('config.properties', 'w')
                configFile.writelines(['webapp.port=%d\n' % self.port])
                if self.dataDir is not None:
                    configFile.writelines(['pulse.data=%s\n' % self.dataDir])
                configFile.close()
                ret = subprocess.call(['init.sh', 'start'], env=self.getEnv())
                if ret != 0:
                    raise Exception('init script exited with code %d' % ret)
                self.pop = True
            else:
                self.stdout = open('stdout.txt', 'w')
                self.stderr = open('stderr.txt', 'w')
                command = [getScript(), 'start', '-p', str(self.port), '-f', 'config.properties']
                if self.dataDir is not None:
                    command += ['-d', self.dataDir]
                self.pop = subprocess.Popen(command, stdout=self.stdout, stderr=self.stderr)
            
            if wait:
                if not self.waitFor():
                    raise Exception('Timed out waiting for server to start')
        finally:
            os.chdir(oldDir)
    
    
    def waitFor(self, timeout=60):
        """Waits for this server to start listening.  If a connection
        can be made to the port before the specified timeout (in seconds) elapses,
        this function returns True.  Otherwise, False is returned."""
        s = socket.socket()
        endTime = time.time() + timeout
        while time.time() < endTime:
            try:
                s.connect(('localhost', self.port))
                s.close()
                # Sleep a little longer just to be sure
                time.sleep(3)
                return True
            except:
                time.sleep(1)
        
        return False
        
        
    def cleanup(self, service=False):
        ret = 0
        if self.pop is not None:
            oldDir = os.getcwdu()
            os.chdir(os.path.join(self.baseDir, 'bin'))
            try:
                if service:
                    ret = subprocess.call(['init.sh', 'stop'], env=self.getEnv())
                    self.pop=None
                else:
                    ret = subprocess.call([getScript(), 'shutdown', '-p', str(self.port), '-f', 'config.properties'])
            finally:
                os.chdir(oldDir)
        
        return ret
    
    
    def stop(self, timeout=60, service=False):
        """Stops this running pulse server using a shutdown script.  Failure for
        the process to exit in the given timeout (in seconds) will result in an
        exception being thrown."""
        ret = self.cleanup(service)
        if ret != 0:
            raise Exception('Shutdown scripted exited with code ' + str(ret))
        if not service:
            endTime = time.time() + timeout
            while time.time() < endTime:
                ret = self.pop.poll()
                if ret is None:
                    time.sleep(1)
                else:
                    self.stdout.close()
                    self.stderr.close()
                    self.pop = None
                    return
            raise Exception('Timed out waiting for pulse process "' + str(self.pop.pid) + '" to exit')
    
    
    def getAdminToken(self):
        activeFilename = os.path.join(self.baseDir, 'active-version.txt')
        activeFile = open(activeFilename)
        activeVersion = activeFile.read()
        activeFile.close()
        versionDir = os.path.join(self.baseDir, 'versions', activeVersion)
        tokenFilename = os.path.join(versionDir, 'system', 'config', 'admin.token')
        tokenFile = open(tokenFilename)
        token = tokenFile.read()
        tokenFile.close()
        return token
        
        
    def getServerProxy(self):
        return xmlrpclib.ServerProxy('http://localhost:' + str(self.port) + '/xmlrpc')
        
        

class PulsePackage:
    def __init__(self, packageFile):
        self.packageFile = packageFile
    
    
    def extractTo(self, destDir):
        if not os.path.exists(destDir):
            os.makedirs(destDir)
        
        if self.packageFile.endswith('.zip'):
            subprocess.call(['unzip', '-qd', destDir, self.packageFile])
        else:
            subprocess.call(['tar', '-zxC', destDir, '-f', self.packageFile])
        
        return os.path.join(destDir, self.getBasename())
        
        
    def getFilename(self):
        return os.path.split(self.packageFile)[1]
        
        
    def getBasename(self):
        f = self.getFilename()
        max = 1
        if f.endswith(".tar.gz"):
            # Evil two part extensions be damned!
            max = 2
            
        return f.rsplit(".", max)[0]


if __name__ == "__main__":
    if len(sys.argv) > 1:
        p = PulsePackage(sys.argv[1])
        p.extractTo(sys.argv[2])

