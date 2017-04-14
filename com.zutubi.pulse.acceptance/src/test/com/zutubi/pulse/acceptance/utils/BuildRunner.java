/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.acceptance.rpc.RemoteApiClient;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.adt.Pair;

import java.lang.reflect.Array;
import java.util.Hashtable;
import java.util.List;

import static com.zutubi.util.CollectionUtils.asPair;

/**
 * The build runner is an acceptance test support class that caters specifically
 * to the triggering and monitoring of builds. 
 */
public class BuildRunner
{
    private RemoteApiClient remoteApi;

    /**
     * Create a new instance of the build runner.
     *
     * @param remoteApi  the xml rpc helper configured to connect to the pulse server
     * on which builds will be run.
     */
    public BuildRunner(RemoteApiClient remoteApi)
    {
        this.remoteApi = remoteApi;
    }

    /**
     * Trigger a build for the specified project and assert that it is successful.
     *
     * @param project   the project for which a build is being triggered.
     * @param options   the trigger options
     * @return  the build number
     *
     * @throws Exception on error or if the build was not successful.
     */
    public int triggerSuccessfulBuild(ProjectConfigurationHelper project, Pair<String, Object>... options) throws Exception
    {
        return triggerSuccessfulBuild(project.getConfig(), options);
    }

    /**
     * Trigger a build for the specified project and assert that it is successful.
     *
     * @param project   the project for which a build is being triggered.
     * @param options   the trigger options
     * @return  the build number
     * 
     * @throws Exception on error or if the build was not successful.
     */
    public int triggerSuccessfulBuild(ProjectConfiguration project, Pair<String, Object>... options) throws Exception
    {
        int buildNumber = triggerAndWaitForBuild(project, options);

        ResultState buildStatus = remoteApi.getBuildStatus(project.getName(), buildNumber);
        if (!ResultState.SUCCESS.equals(buildStatus))
        {
            throw new RuntimeException("Expected success, had " + buildStatus + " instead.");
        }
        return buildNumber;
    }

    /**
     * Trigger a build for the specified project and assert that it fails
     *
     * @param project   the project for which a build is being triggered.
     * @param options   the trigger options
     * @return  the build number
     *
     * @throws Exception on error or if the build status was not failure.
     */
    public int triggerFailedBuild(ProjectConfigurationHelper project, Pair<String, Object>... options) throws Exception
    {
        return triggerFailedBuild(project.getConfig(), options);
    }

    /**
     * Trigger a build for the specified project and assert that it fails
     *
     * @param project   the project for which a build is being triggered.
     * @param options   the trigger options
     * @return  the build number
     *
     * @throws Exception on error or if the build status was not failure.
     */
    public int triggerFailedBuild(ProjectConfiguration project, Pair<String, Object>... options) throws Exception
    {
        int buildNumber = triggerAndWaitForBuild(project, options);

        ResultState buildStatus = remoteApi.getBuildStatus(project.getName(), buildNumber);
        if (!ResultState.FAILURE.equals(buildStatus))
        {
            throw new RuntimeException("Expected failure, had " + buildStatus + " instead.");
        }
        return buildNumber;
    }

    /**
     * Trigger a build for the specified project and wait for it to complete.
     *
     * @param project   the project for which a build is being triggered.
     * @param options   the trigger options.
     * @return the build number
     *
     * @throws Exception on error or if the we timed out waiting for the build to complete.
     */
    public int triggerAndWaitForBuild(ProjectConfigurationHelper project, Pair<String, Object>... options) throws Exception
    {
        return triggerAndWaitForBuild(project.getConfig(), options);
    }

    /**
     * Trigger a build for the specified project and wait for it to complete.
     *
     * @param project   the project for which a build is being triggered.
     * @param options   the trigger options.
     * @return the build number
     *
     * @throws Exception on error or if the we timed out waiting for the build to complete.
     */
    public int triggerAndWaitForBuild(ProjectConfiguration project, Pair<String, Object>... options) throws Exception
    {
        List<String> requestIds = triggerBuild(project, options);
        int buildNumber = 0;
        for (String requestId : requestIds)
        {
            buildNumber = Math.max(remoteApi.waitForBuildToComplete(project.getName(), requestId), buildNumber);
        }
        return buildNumber;
    }

    /**
     * Trigger a build of the specified project.
     *
     * @param project   the project for which the build is being triggered.
     * @param options   the build options.
     *
     * @return  the request ids of the triggered builds.
     *
     * @throws Exception on error.
     */
    public List<String> triggerBuild(ProjectConfigurationHelper project, Pair<String, Object>... options) throws Exception
    {
        return triggerBuild(project.getConfig(), options);
    }

    /**
     * Trigger a build of the specified project.
     *
     * @param project   the project for which the build is being triggered.
     * @param options   the build options.
     * 
     * @return  the request ids of the triggered builds.
     * 
     * @throws Exception on error.
     */
    public List<String> triggerBuild(ProjectConfiguration project, Pair<String, Object>... options) throws Exception
    {
        // projects that are not initialised will 'drop' the trigger request. 
        remoteApi.waitForProjectToInitialise(project.getName());
        
        Hashtable<String, Object> triggerOptions = new Hashtable<String, Object>();
        if (options != null)
        {
            for (Pair<String, Object> option : options)
            {
                triggerOptions.put(option.getFirst(), option.getSecond());
            }
        }

        return remoteApi.triggerBuild(project.getName(), triggerOptions);
    }

    /**
     * Trigger a build of the specified project, with the 'rebuild' trigger option set to
     * true.
     *
     * @param project   the project to be built.
     * @param options   the build options.
     *
     * @return  the request ids of the triggered builds.
     *
     * @throws Exception on error.
     */
    public List<String> triggerRebuild(ProjectConfigurationHelper project, Pair<String, Object>... options) throws Exception
    {
        return triggerRebuild(project.getConfig(), options);
    }

    /**
     * Trigger a build of the specified project, with the 'rebuild' trigger option set to
     * true.
     *
     * @param project   the project to be built.
     * @param options   the build options.
     *
     * @return  the request ids of the triggered builds.
     *
     * @throws Exception on error.
     */
    @SuppressWarnings({"unchecked"})
    public List<String> triggerRebuild(ProjectConfiguration project, Pair<String, Object>... options) throws Exception
    {
        Pair<String, Object>[] args = (Pair<String, Object>[]) Array.newInstance(Pair.class, options.length + 1);
        System.arraycopy(options, 0, args, 0, options.length);
        args[args.length - 1] = asPair("rebuild", (Object)"true");
        return triggerBuild(project, args);
    }
}
