package org.apache.maven.scm.provider.git.gitexe.command;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Command line construction utility.
 * 
 * @author Brett Porter
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id$
 */
public final class GitCommandLineUtils
{

    private GitCommandLineUtils()
    {
    }

    public static void addTarget( Commandline cl, List<File> files )
    {
        if ( files == null || files.isEmpty() )
        {
            return;
        }
        final File workingDirectory = cl.getWorkingDirectory();
        try
        {
            final String canonicalWorkingDirectory = workingDirectory.getCanonicalPath();
            for ( File file : files )
            {
                String relativeFile = file.getPath();
                
                final String canonicalFile = file.getCanonicalPath();
                if ( canonicalFile.startsWith( canonicalWorkingDirectory ) )
                {
                    // so we can omit the starting characters
                    relativeFile = canonicalFile.substring( canonicalWorkingDirectory.length() );

                    if ( relativeFile.startsWith( File.separator ) )
                    {
                        relativeFile = relativeFile.substring( File.separator.length() );
                    }
                }

                // no setFile() since this screws up the working directory!
                cl.createArg().setValue( relativeFile );
            }
        }
        catch ( IOException ex )
        {
            throw new IllegalArgumentException( "Could not get canonical paths for workingDirectory = "
                + workingDirectory + " or files=" + files, ex );
        }
    }

    public static Commandline getBaseGitCommandLine( File workingDirectory, String command )
    {
        if ( command == null || command.length() == 0 )
        {
            return null;
        }

        Commandline cl = new Commandline();

        cl.setExecutable( "git" );

        cl.createArg().setValue( command );

        if ( workingDirectory != null )
        {
            cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );
        }

        return cl;
    }

    public static int execute( Commandline cl, StreamConsumer consumer, CommandLineUtils.StringStreamConsumer stderr,
                               ScmLogger logger )
        throws ScmException
    {
        if ( logger.isInfoEnabled() )
        {
            logger.info( "Executing: " + cl );
            logger.info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
        }

        int exitCode;
        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        return exitCode;
    }

    public static int execute( Commandline cl, CommandLineUtils.StringStreamConsumer stdout,
                               CommandLineUtils.StringStreamConsumer stderr, ScmLogger logger )
        throws ScmException
    {
        if ( logger.isInfoEnabled() )
        {
            logger.info( "Executing: " + cl );
            logger.info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
        }

        int exitCode;
        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        return exitCode;
    }

}