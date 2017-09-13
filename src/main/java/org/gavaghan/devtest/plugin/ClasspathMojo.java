package org.gavaghan.devtest.plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Mojo to generate source code from Thrift IDL files.
 * 
 * @author <a href="mailto:mike@gavaghan.org">Mike Gavaghan</a>
 */
@Mojo(name = "build-classpath")
public class ClasspathMojo extends AbstractMojo
{
	/** Path to DevTEst installation. */
	@Parameter(alias = "devtest-home", required = true)
	private File mDevTestHome;

	/**
	 * Log runtime settings
	 * 
	 * @param maven
	 */
	private void logSettings(MavenProject maven)
	{
		getLog().info("Using DevTest home: " + mDevTestHome);
		
		try
		{
			for (String elem : maven.getCompileClasspathElements())
			{
				getLog().info("Source element: " + elem);
			}
		}
		catch (DependencyResolutionRequiredException exc)
		{
			// ignored
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		Map<?, ?> context = getPluginContext();
		MavenProject maven = (MavenProject) context.get("project");

		// log settings from pom
		logSettings(maven);

		try
		{
			// check for 'lib' directory
			File lib = new File(mDevTestHome, "lib");
			if (!lib.exists()) throw new MojoFailureException(lib.getCanonicalPath() + " does not exist");
			if (!lib.canRead()) throw new MojoFailureException(lib.getCanonicalPath() + " is not readable");

			Set<Artifact> artifacts = maven.getArtifacts();

			findJars(lib, artifacts);
			
			logSettings(maven);
		}
		catch (IOException exc)
		{
			throw new MojoExecutionException("Unable to add DevTest libraries to path", exc);
		}
	}

	private void findJars(File root, Set<Artifact> artifacts) throws IOException
	{
		for (File file : root.listFiles())
		{
			// can't read it?  move on
			if (!file.canRead())  continue;
			
			// if directory, dive
			if (file.isDirectory())
			{
				findJars(file, artifacts);
			}
			
			// else, if jar
			else if (file.getName().toLowerCase().endsWith(".jar"))
			{
				getLog().info("Adding: " + file.getCanonicalPath());

			}
		}
	}
}
