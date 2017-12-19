package org.gavaghan.devtest.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;

import org.apache.maven.plugin.logging.Log;

/**
 * Collate items from all of the DevTest jars into one master directory
 * structure so Maven can put it all in one jar.
 * 
 * @author <a href="mailto:mike@gavaghan.org">Mike Gavaghan</a>
 */
class Collator implements Runnable
{
	/** The Decompressor gives us our work. */
	private final Decompressor mDecompressor;

	/** Target folder. */
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
	private void collate() throws IOException
	{
		ZipEntry[] entryAry = new ZipEntry[1];
		byte[][] dataAry = new byte[1][];

		for (;;)
		{
			// get more data - break when done
			if (!mDecompressor.getCurrent(entryAry, dataAry)) break;

			ZipEntry entry = entryAry[0];
			byte[] data = dataAry[0];

			// as long as there's data, write out the file
			if (data != null)
			{
				File newFile = new File(mDest, entry.getName());
				newFile.getParentFile().mkdirs();

				try (FileOutputStream classOS = new FileOutputStream(newFile))
				{
					classOS.write(data);
					classOS.flush();
				}
			}
		}
	}

	/**
	 * 
	 * @param decompressor
	 * @param dest
	 * @param log
	 */
	public Collator(Decompressor decompressor, File dest, Log log)
	{
		mDecompressor = decompressor;
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
		return mSuccess && mDecompressor.isSuccess();
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		try
		{
			collate();
			mSuccess = true;
		}
		catch (IOException exc)
		{
			mLog.error("Failed to collate DevTest jars", exc);
		}
		finally
		{
			mLog.debug("Collate complete");
			setFinished();
		}
	}
}
