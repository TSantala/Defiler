package virtualdisk;
/*
 * VirtualDisk.java
 *
 * A virtual asynchronous disk.
 *
 */

import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;

import common.Constants;
import common.Constants.DiskOperationType;
import dblockcache.CBuffer;
import dblockcache.DBuffer;

public abstract class VirtualDisk implements IVirtualDisk {

	protected String _volName;
	protected RandomAccessFile _file;
	protected int _maxVolSize;

	/*
	 * Start an asynchronous request to the underlying device/disk/volume. 
	 * -- buf is a DBuffer object that needs to be read/write from/to the volume.	
	 * -- operation is either READ or WRITE  
	 */
	public abstract void startRequest(DBuffer buf, DiskOperationType operation) throws IllegalArgumentException,
			IOException;
	
	/*
	 * Clear the contents of the disk by writing 0s to it
	 */
	protected void formatStore() {
		byte b[] = new byte[Constants.BLOCK_SIZE];
		setBuffer((byte) 0, b, Constants.BLOCK_SIZE);
		for (int i = 0; i < Constants.NUM_OF_BLOCKS; i++) {
			try {
				int seekLen = i * Constants.BLOCK_SIZE;
				_file.seek(seekLen);
				_file.write(b, 0, Constants.BLOCK_SIZE);
			} catch (Exception e) {
				System.out.println("Error in format: WRITE operation failed at the device block " + i);
			}
		}
	}

	/*
	 * helper function: setBuffer
	 */
	protected static void setBuffer(byte value, byte b[], int bufSize) {
		for (int i = 0; i < bufSize; i++) {
			b[i] = value;
		}
	}

	/*
	 * Reads the buffer associated with DBuffer to the underlying
	 * device/disk/volume
	 */
	protected int readBlock(DBuffer buf) throws IOException {
		int seekLen = buf.getBlockID() * Constants.BLOCK_SIZE;
		/* Boundary check */
		if (_maxVolSize < seekLen + Constants.BLOCK_SIZE) {
			return -1;
		}
		_file.seek(seekLen);
		return _file.read(buf.getBuffer(), 0, Constants.BLOCK_SIZE);
	}

	/*
	 * Writes the buffer associated with DBuffer to the underlying
	 * device/disk/volume
	 */
	protected void writeBlock(DBuffer buf) throws IOException {
		System.out.println("Write was called! Here on the DISK! ********");
		int seekLen = buf.getBlockID() * Constants.BLOCK_SIZE;
		_file.seek(seekLen);
		_file.write(buf.getBuffer(), 0, Constants.BLOCK_SIZE);
	}
}
