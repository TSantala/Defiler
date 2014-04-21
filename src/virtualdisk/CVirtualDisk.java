package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.ArrayList;

import common.Constants;
import common.Constants.DiskOperationType;
import common.DFileID;

import dblockcache.CBuffer;
import dblockcache.CBufferCache;
import dblockcache.DBuffer;

public class CVirtualDisk extends VirtualDisk{
	
	private CBufferCache _cache;

	/*
	 * VirtualDisk Constructors
	 */
	public CVirtualDisk(String volName, boolean format, CBufferCache cache) throws FileNotFoundException,
			IOException {

		_volName = volName;
		_maxVolSize = Constants.BLOCK_SIZE * Constants.NUM_OF_BLOCKS;
		_cache = cache;

		/*
		 * mode: rws => Open for reading and writing, as with "rw", and also
		 * require that every update to the file's content or metadata be
		 * written synchronously to the underlying storage device.
		 */
		_file = new RandomAccessFile(_volName, "rws");

		/*
		 * Set the length of the file to be NUM_OF_BLOCKS with each block of
		 * size BLOCK_SIZE. setLength internally invokes ftruncate(2) syscall to
		 * set the length.
		 */
		_file.setLength(Constants.BLOCK_SIZE * Constants.NUM_OF_BLOCKS);
		
		if(format) {
			this.formatStore();
		}

	}


	/*
	 * Start an asynchronous request to the underlying device/disk/volume. 
	 * -- buf is an DBuffer object that needs to be read/write from/to the volume.	
	 * -- operation is either READ or WRITE  
	 */
	public void startRequest(DBuffer buf, DiskOperationType operation) throws IllegalArgumentException,
			IOException {
		if(operation == DiskOperationType.READ) {
			this.readBlock(buf);
		}
		else if(operation == DiskOperationType.WRITE) {
			this.writeBlock(buf);
		}
		buf.ioComplete();
	}


	public ArrayList<DFileID> getFilesOnInit() {
		ArrayList<DFileID> toReturn = new ArrayList<DFileID>();
		for(int i = 0; i < Constants.MAX_DFILES; i++){
			int fileExists = 0;
			try {
				_file.seek(i);
				fileExists = _file.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(fileExists == 1){
				System.out.println("File stored on disk");
				toReturn.add(new DFileID(i));
			}
		}
		return toReturn;
	}


	public void writeFileInode(int fileID) {
		System.out.println("File inode write!: "+fileID);
		try {
			_file.seek(fileID);
			_file.write((byte) 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void clearFileInode(int fileID) {
		System.out.println("File inode clear!: "+fileID);
		try {
			_file.seek(fileID);
			_file.write((byte) 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
