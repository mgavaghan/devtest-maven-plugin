package org.gavaghan.devtest.plugin;

import org.apache.maven.artifact.handler.ArtifactHandler;

public class DevTestArtifactHandler implements ArtifactHandler
{
	@Override
	public String getClassifier()
	{
		System.err.println("DevTestArtifactHandler::getClassifier()");
		return null;
	}

	@Override
	public String getDirectory()
	{
		System.err.println("DevTestArtifactHandler::getDirectory()");
		return null;
	}

	@Override
	public String getExtension()
	{
		System.err.println("DevTestArtifactHandler::getExtension()");
		return null;
	}

	@Override
	public String getLanguage()
	{
		System.err.println("DevTestArtifactHandler::getLanguage()");
		return null;
	}

	@Override
	public String getPackaging()
	{
		System.err.println("DevTestArtifactHandler::getPackaging()");
		return null;
	}

	@Override
	public boolean isAddedToClasspath()
	{
		System.err.println("DevTestArtifactHandler::isAddedToClasspath()");
		return true;
	}

	@Override
	public boolean isIncludesDependencies()
	{
		System.err.println("DevTestArtifactHandler::isIncludesDependencies()");
		return false;
	}
}
