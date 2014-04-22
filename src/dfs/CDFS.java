package dfs;

import java.util.List;
import java.util.ArrayList;

import virtualdisk.CVirtualDisk;
import common.Constants;
import common.DFileID;

import dblockcache.CBufferCache;
import dblockcache.CBuffer;

public class CDFS extends DFS{

	private ArrayList<DFileID> fileIDs;
	private List<Integer> freedIDs;
	private int countID = 0;
	private String _volName;
	private boolean _format;

	private CBufferCache _cache;
	private CVirtualDisk _disk;

	/* 
	 * @volName: Explicitly overwrite volume name
	 * @format: If format is true, the system should erase the underlying disk contents and reinitialize the volume.
	 */

	public CDFS(String volName, boolean format) {
		_volName = volName;
		_format = format;
		_cache = new CBufferCache(this, _volName, _format);
		_disk = _cache.getDisk();
		freedIDs = new ArrayList<Integer>();
		this.init();
	}

	public CDFS(boolean format) {
		this(Constants.vdiskName,format);
	}

	public CDFS() {
		this(Constants.vdiskName,false);
	}

	@Override
	public void init() {
		fileIDs = new ArrayList<DFileID>();
		if(!_format){
			fileIDs = _disk.getFilesOnInit();
		}
	}

	/* creates a new DFile and returns the DFileID, which is useful to uniquely identify the DFile*/
	public synchronized DFileID createDFile() {
		int newID;
		if(countID == Constants.MAX_DFILES)
			newID = freedIDs.remove(0);
		else
			newID = countID++;
		DFileID newFile = new DFileID(newID);
		fileIDs.add(newFile);
		_disk.writeFileInode(newID);
		return newFile;
	}

	/* destroys the file specified by the DFileID */
	public synchronized void destroyDFile(DFileID dFID){
		fileIDs.remove(dFID);
		int blockID = Constants.INODE_REGION_NUM_BLOCKS + dFID.getDFileID()*Constants.MAX_FILE_BLOCKS;
		_cache.clearFileBlocks(blockID);
		_disk.clearFileInode(dFID.getDFileID());
		freedIDs.add(dFID.getDFileID());
	}

	/*
	 * reads the file dfile named by DFileID into the buffer starting from the
	 * buffer offset startOffset; at most count bytes are transferred
	 */
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {

		int blockID = Constants.INODE_REGION_NUM_BLOCKS + dFID.getDFileID()*Constants.MAX_FILE_BLOCKS;
		int totalRead = 0;

		while(count!=0){

			int toRead = Constants.BLOCK_SIZE;
			if(toRead > count)
				toRead = count;

			CBuffer cbuf= (CBuffer) _cache.getBlock(blockID++);;
			int amountRead;
			synchronized(cbuf){
				amountRead = cbuf.read(buffer, startOffset, toRead);
				_cache.releaseBlock(cbuf);
			}

			if(amountRead == -1){
				System.out.println("Error occured while reading, breaking loop");
				break;
			}

			totalRead += amountRead;
			count -= amountRead;
			startOffset+= Constants.BLOCK_SIZE;
		}
		return totalRead;
	}

	/*
	 * writes to the file specified by DFileID from the buffer starting from the
	 * buffer offset startOffset; at most count bytes are transferred
	 */
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {

		int blockID = Constants.INODE_REGION_NUM_BLOCKS + dFID.getDFileID()*Constants.MAX_FILE_BLOCKS;
		int totalWritten = 0;
		
		System.out.println("Write was called");

		while(count!=0){

			int toWrite = Constants.BLOCK_SIZE;
			if(toWrite > count) {
				System.out.println("less than one block to write");
				toWrite = count;
			}

			System.out.println("Getting cbuf");
			CBuffer cbuf = (CBuffer) _cache.getBlock(blockID++);
			int amountWritten;
			synchronized(cbuf){
				amountWritten = cbuf.write(buffer, startOffset, toWrite);
				_cache.releaseBlock(cbuf);
			}

			if(amountWritten == -1){
				System.out.println("Error occured while reading, breaking loop");
				break;
			}

			totalWritten += amountWritten;
			count -= amountWritten;
			startOffset+= Constants.BLOCK_SIZE;
		}
		
		System.out.println("Out of write loop");
		return totalWritten;
	}

	/* 
	 * List all the existing DFileIDs in the volume
	 */
	public List<DFileID> listAllDFiles() {
		return fileIDs;
	}

	/* Write back all dirty blocks to the volume, and wait for completion. */
	public void sync(){
		_cache.sync();
	}

}
