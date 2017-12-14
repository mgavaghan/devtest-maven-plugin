package org.gavaghan.devtest.plugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Mojo to create a single jar out of all of the DevTest jars.
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
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		getLog().debug("Building master JAR...");
		File dest = new File("C:/Users/gavmi01/Desktop/devtest-" + mDevTestVersion + ".jar");
		createJar(dest);
	}
	
	private void createJar(File dest)
	{
		Decompressor decomp = new Decompressor(new File(mDevTestHome, "lib"), getLog());
		Compressor comp = new Compressor(decomp, dest, getLog());
		
		Thread compThread = new Thread(comp);
		Thread decompThread = new Thread(decomp);
		
		decompThread.start();
		compThread.start();
		
		comp.waitForFinish();
	}
}
