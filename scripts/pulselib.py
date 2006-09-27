# Utility functions for launching and stopping pulse servers and agents.

import os
import socket
import subprocess
import sys
import time


WINDOWS = os.name == "nt"


def getScript(name):
    if WINDOWS:
        return name + ".bat"
    else:
        return "./" + name + "sh"
    

class Pulse:
    def __init__(self, baseDir, port):
        self.baseDir = os.path.abspath(baseDir)
        self.port = port
        
    def start(self, wait=True):
        """Starts this pulse server.  A local properties file
        is used, output is captured to baseDir/stdout.txt and baseDir/stderr.txt.
        If wait is True, this function will wait for Pulse to start listening on the
        given port before returning.  If the server cannot be started successfully,
        an exception is thrown carrying the reason."""
        os.chdir(os.path.join(self.baseDir, 'bin'))
        self.stdout = open('stdout.txt', 'w')
        self.stderr = open('stderr.txt', 'w')
        self.pop = subprocess.Popen([getScript('startup'), '-p', str(self.port), '-f', 'config.properties'], stdout=self.stdout, stderr=self.stderr, shell=False)
        if wait:
            if not self.waitFor():
                raise Exception('Timed out waiting for server to start')
    
    
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
    
    
    def stop(self, timeout=60):
        """Stops this running pulse server using a shutdown script.  Failure for
        the process to exit in the given timeout (in seconds) will result in an
        exception being thrown."""
        os.chdir(os.path.join(self.baseDir, 'bin'))
        ret = subprocess.call([getScript('shutdown'), '-p', str(self.port), '-f', 'config.properties'], shell=WINDOWS)
        if ret != 0:
            raise Exception('Shutdown scripted exited with code ' + str(ret))
        endTime = time.time() + timeout
        while time.time() < endTime:
            ret = self.pop.poll()
            if ret is None:
                time.sleep(1)
            else:
                self.stdout.close()
                self.stderr.close()
                return
        raise Exception('Timed out waiting for pulse process "' + str(self.pop.pid) + '" to exit')



if __name__ == "__main__":
    if len(sys.argv) > 0:
        p = Pulse(sys.argv[1], 8745)
        p.start()
        print "It is running!"
        time.sleep(20)
        p.stop()
