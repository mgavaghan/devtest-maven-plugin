package org.gavaghan.devtest.plugin;

import java.io.File;
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
 * Mojo to collate all of the DevTest jar contents into a single directory
 * structure for Maven to create a master JAR out of.
 * 
 * @author <a href="mailto:mike@gavaghan.org">Mike Gavaghan</a>
 */
@Mojo(name = "create-master-jar", defaultPhase = LifecyclePhase.COMPILE)
public class CreateMasterDevTestJarMojo extends AbstractMojo
{
	/** Path to DevTest installation. */
	@Parameter(alias = "devtest-home", required = true)
	private File mDevTestHome;

	/** Path to DevTest installation. */
	@Parameter(alias = "devtest-version", required = true)
	private String mDevTestVersion;

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
		Build build = maven.getBuild();

		File target = new File(build.getDirectory());
		File dest = new File(target, "classes");
		getLog().debug("Collator DevTest jar contents to: " + dest.getAbsolutePath());

		// build a decompressor to dive through the DevTest jars
		Decompressor decomp = new Decompressor(new File(mDevTestHome, "lib"), getLog());

		// build a collator to bring them all together
		Collator comp = new Collator(decomp, dest, getLog());

		// create and start the threads
		Thread compThread = new Thread(comp);
		Thread decompThread = new Thread(decomp);

		decompThread.start();
		compThread.start();

		comp.waitForFinish();
		getLog().debug("Completed.");
	}
}
