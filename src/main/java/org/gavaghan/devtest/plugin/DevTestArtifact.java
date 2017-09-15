package org.gavaghan.devtest.plugin;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.repository.ArtifactRepository;

public class DevTestArtifact extends DefaultArtifact
{
	public DevTestArtifact(File jar)  throws IOException
	{
		super("com.ca.plugin", jar.getCanonicalFile().getName(), "version", "compile", "type", null, new DevTestArtifactHandler());
		
		setFile(jar);
	}

	@Override
	public String getDownloadUrl()
	{
		System.err.println("DevTestArtifact::getDownloadUrl() - " + super.getDownloadUrl());
		return super.getDownloadUrl();
	}

	@Override
	public File getFile()
	{
		System.err.println("DevTestArtifact::getFile() - " + super.getFile());
		return super.getFile();
	}

	@Override
	public ArtifactRepository getRepository()
	{
		System.err.println("DevTestArtifact::getRepository() - " + super.getRepository());
		return super.getRepository();
	}
}
