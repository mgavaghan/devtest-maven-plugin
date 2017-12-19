package org.gavaghan.devtest.plugin;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.plugin.logging.Log;

/**
 * Runnable used to decompress all jars in a DevTest installation.
 * 
 * @author <a href="mailto:mike@gavaghan.org">Mike Gavaghan</a>
 */
class Decompressor implements Runnable
{
	/** The source folder to begin looking for jars. */
	private final File mSource;

	/** The Maven logger. */
	private final Log mLog;

	/** The name of the current entry offered to the compressor. */
	private ZipEntry mCurrentEntry;

	/** The current file contents offered to the compressor. */
	private byte[] mCurrentData;

	/** Working buffer. */
	private final byte[] mBuffer = new byte[65536];

	/** Flag indicating we're out of data. */
	private boolean mFinished = false;
	
	/** Success flag. */
	private boolean mSuccess;

	/**
	 * Expand all of the contents of a JAR file.
	 * 
	 * @param jar
	 * @param created
	 * @throws IOException
	 */
	private void expandJAR(File jar, Map<String, String> created) throws IOException
	{
		try (FileInputStream fis = new FileInputStream(jar); BufferedInputStream bis = new BufferedInputStream(fis, 1024 * 1024); ZipInputStream zis = new ZipInputStream(bis))
		{
			ZipEntry srcEntry;

			while ((srcEntry = zis.getNextEntry()) != null)
			{
				// don't copy the activemq stuff - this causes testing problems
				String name = srcEntry.getName();
				if (name.startsWith("META-INF/") && (name.indexOf("activemq") < 0)) continue;
				
				// don't copy these, either
				if (name.startsWith("license/")) continue;

				// avoid duplicates
				if (created.containsKey(name)) continue;
				created.put(name, name);

				// if it's a directory, reuse entry
				if (srcEntry.isDirectory())
				{
					setCurrent(srcEntry, null);
				}
				// if it's not a directory, copy data
				else
				{
					ByteArrayOutputStream baos = new ByteArrayOutputStream();

					ZipEntry destEntry = new ZipEntry(srcEntry.getName());
					destEntry.setTime(srcEntry.getTime());
					destEntry.setLastModifiedTime(srcEntry.getLastModifiedTime());

					int got;

					while ((got = zis.read(mBuffer)) > 0)
					{
						baos.write(mBuffer, 0, got);
					}

					setCurrent(destEntry, baos.toByteArray());
				}
			}
		}
	}

	/**
	 * Recursively dive from the source folder look for JARs.
	 * 
	 * @param folder
	 * @param created
	 * @throws IOException
	 */
	private void scanAllJARS(File folder, Map<String, String> created) throws IOException
	{
		File[] contents = folder.listFiles();
		if (contents == null) return;

		mLog.debug("Scanning " + folder.getPath());

		for (File entry : contents)
		{
			if (entry.isDirectory())
			{
				scanAllJARS(entry, created);
			}
			else if (entry.getName().toLowerCase().endsWith(".jar") || entry.getName().toLowerCase().endsWith(".zip"))
			{
				expandJAR(entry, created);
			}
			else
			{
				mLog.debug("Skipping " + entry.getPath());
			}
		}
	}

	/**
	 * 
	 * @param source
	 * @param log
	 */
	public Decompressor(File source, Log log)
	{
		mSource = source;
		mLog = log;
	}

	/**
	 * Indicate we're out of data.
	 */
	public synchronized void setFinished()
	{
		mFinished = true;
		notifyAll();
	}

	/**
	 * Set the next available data buffer to compress. Waits until the previous
	 * one is consumed.
	 * 
	 * @param srcEntry
	 * @param data
	 */
	public synchronized void setCurrent(ZipEntry srcEntry, byte[] data)
	{
		while (mCurrentEntry != null)
		{
			try
			{
				wait();
			}
			catch (InterruptedException ignored)
			{
			}
		}

		mCurrentEntry = srcEntry;
		mCurrentData = data;
		notifyAll();
	}

	/**
	 * Get the next available entry.  Called by the compressor.
	 * 
	 * @param srcEntry
	 * @param data
	 */
	public synchronized boolean getCurrent(ZipEntry[] srcEntry, byte[][] data)
	{
		if (mFinished) return false;

		while ((mCurrentEntry == null) && !mFinished)
		{
			try
			{
				wait();
			}
			catch (InterruptedException ignored)
			{
			}
		}

		srcEntry[0] = mCurrentEntry;
		data[0] = mCurrentData;

		mCurrentEntry = null;
		mCurrentData = null;
		notifyAll();

		return !mFinished;
	}
	
	/**
	 * Determine if the process was successful.
	 * 
	 * @return
	 */
	public boolean isSuccess()
	{
		return mSuccess;
	}

	/**
	 * Work loop.
	 */
	@Override
	public void run()
	{
		try
		{
			Map<String, String> created = new HashMap<String, String>();
			scanAllJARS(mSource, created);
			mSuccess = true;
		}
		catch (IOException exc)
		{
			mLog.error("Failed to decompress DevTest jars", exc);
		}
		finally
		{
			setFinished();
		}
	}
}
