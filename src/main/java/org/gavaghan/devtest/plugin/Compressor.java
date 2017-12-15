package org.gavaghan.devtest.plugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.logging.Log;

/**
 * Compress items for all of the DevTest jars into one master jar.
 * 
 * @author <a href="mailto:mike@gavaghan.org">Mike Gavaghan</a>
 */
class Compressor implements Runnable
{
	/** The Decompressor gives us our work. */
	private final Decompressor mCompressor;

	/** Target file. */
	private final File mDest;

	/** The Maven logger. */
	private final Log mLog;
	
	/** Success flag. */
	private boolean mSuccess;
	
	/** Finished flag. */
	private boolean mFinished;

	/**
	 * 
	 * @throws IOException
	 */
	private void compress() throws IOException
	{
		ZipEntry[] entryAry = new ZipEntry[1];
		byte[][] dataAry = new byte[1][];

		try (FileOutputStream fos = new FileOutputStream(mDest); BufferedOutputStream bos = new BufferedOutputStream(fos, 5 * 1024 * 1024); ZipOutputStream zos = new ZipOutputStream(bos))
		{
			for (;;)
			{
				zos.setLevel(9);

				// get more data
				if (!mCompressor.getCurrent(entryAry, dataAry)) break;

				ZipEntry entry = entryAry[0];
				byte[] data = dataAry[0];

				zos.putNextEntry(entry);
				
				//mLog.warn(entry.getName());

				// (data != null) zos.write(data);
				if (data != null)
				{
					File newFile = new File("C:/Users/gavmi01/Documents/GIT/devtest-jar-builder/target/classes/" + entry.getName());
					newFile.getParentFile().mkdirs();
					
					try (FileOutputStream classOS = new FileOutputStream(newFile))
					{
						classOS.write(data);
						classOS.flush();
					}
				}

				zos.closeEntry();
			}

			zos.flush();
			bos.flush();
			fos.flush();
		}
	}

	/**
	 * 
	 * @param compressor
	 * @param dest
	 * @param log
	 */
	public Compressor(Decompressor compressor, File dest, Log log)
	{
		mCompressor = compressor;
		mDest = dest;
		mLog = log;
	}
	
	/**
	 * Determine if the process was successful.
	 * 
	 * @return
	 */
	public boolean isSuccess()
	{
		return mSuccess && mCompressor.isSuccess();
	}
	
	/**
	 * Set the finished flag.
	 */
	public synchronized void setFinished()
	{
		mFinished = true;
		notifyAll();
	}
	
	/**
	 * Wait for compressor to finish.
	 */
	public synchronized void waitForFinish()
	{
		while (mFinished == false)
		{
			try
			{
				wait();
			}
			catch (InterruptedException ignored)
			{
			}
		}
		
		return;
	}

	@Override
	public void run()
	{
		try
		{
			compress();
			mSuccess = true;
		}
		catch (IOException exc)
		{
			mLog.error("Failed to compress DevTest jars", exc);
		}
		finally
		{
			mLog.debug("Compress complete");
			setFinished();
		}
	}
}
