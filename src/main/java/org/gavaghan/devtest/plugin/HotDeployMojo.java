package org.gavaghan.devtest.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Mojo to copy a DevTest SDK jar to the hotDeploy folder.
 * 
 * @author <a href="mailto:mike@gavaghan.org">Mike Gavaghan</a>
 */
@Mojo(name = "copy-to-hotDeploy", defaultPhase = LifecyclePhase.INSTALL)
public class HotDeployMojo extends AbstractMojo
{
	/** Filename filter. */
	static private FilenameFilter sJarFinder = new FilenameFilter()
	{
		@Override
		public boolean accept(File dir, String name)
		{
			return name.toLowerCase().endsWith(".jar");
		}
	};

	/** Path to DevTest installation. */
	@Parameter(alias = "devtest-home", required = true)
	private File mDevTestHome;

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

		try
		{
			// check for 'hotDeploy' directory
			File hotDeploy = new File(mDevTestHome, "hotDeploy");
			if (!hotDeploy.exists()) throw new MojoFailureException(hotDeploy.getCanonicalPath() + " does not exist");
			if (!hotDeploy.canWrite()) throw new MojoFailureException(hotDeploy.getCanonicalPath() + " is not writeable");
			getLog().info("hotDeploy folder is: " + hotDeploy.getCanonicalPath());

			// find target
			Build build = maven.getBuild();
			File target = new File(build.getDirectory());
			File[] artifactList = target.listFiles(sJarFinder);

			if ((artifactList == null) || (artifactList.length == 0)) throw new MojoFailureException("No artifact found in: " + hotDeploy.getCanonicalPath());

			// copy artifact
			byte[] buffer = new byte[65536];

			for (File artifact : artifactList)
			{
				getLog().info("Installing artifact: " + artifact.getName());
				
				File dest = new File(hotDeploy, artifact.getName());
				dest.delete(); // delete any existing file

				try (InputStream input = new FileInputStream(artifact); OutputStream output = new FileOutputStream(dest))
				{
					int got;

					for (;;)
					{
						got = input.read(buffer);
						if (got < 0) break;
						output.write(buffer, 0, got);
					}
					
					output.flush();
				}
			}
		}
		catch (IOException exc)
		{
			throw new MojoExecutionException("Unable to install new DevTest artifact", exc);
		}
	}
}
