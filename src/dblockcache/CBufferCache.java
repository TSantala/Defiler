package dblockcache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import virtualdisk.CVirtualDisk;
import common.Constants;
import common.DFileID;
import dfs.CDFS;

public class CBufferCache extends DBufferCache{

	private int _cacheSize;
	private CVirtualDisk _disk;

	List<CBuffer> _cache = new ArrayList<CBuffer>();

	/*
	 * Constructor: allocates a cacheSize number of cache blocks, each
	 * containing BLOCK-size bytes data, in memory
	 */
	public CBufferCache(CDFS dfs, String volName, boolean format) {
		_cacheSize = Constants.NUM_OF_CACHE_BLOCKS;
		try {
			_disk = new CVirtualDisk(volName, format, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Get buffer for block specified by blockID. The buffer is "held" until the
	 * caller releases it. A "held" buffer cannot be evicted: its block ID
	 * cannot change.
	 */
	public DBuffer getBlock(int blockID) {
		int index = this.getCacheAddress(blockID);
		if(index >= 0){
			CBuffer temp = _cache.remove(index);
			temp.setIsHeld(true);
			_cache.add(temp);
			return temp;
		}
		// Not in cache.
		CBuffer newBlock = new CBuffer(blockID, this, _disk);
		newBlock.setIsHeld(true);
		//synchronized(newBlock) {
			if(_cache.size() > _cacheSize){
				System.out.println("Cache size over max!!!");
			}
			if(_cache.size() == _cacheSize){
				for(int i = 0; i < _cacheSize; i++){
					if(!_cache.get(i).isBusy()){
						CBuffer beingEvicted = _cache.get(i);
						if(!beingEvicted.checkClean())
							beingEvicted.startPush();
						_cache.remove(i);
						break;
					}
				}
			}
			_cache.add(newBlock);
		//}
		return newBlock;
	}

	/* Release the buffer so that others waiting on it can use it */
	public synchronized void releaseBlock(DBuffer buf){
		((CBuffer) buf).setIsHeld(false);
	}

	public synchronized int getCacheAddress(int blockID){
		for(int i = 0; i < _cache.size(); i++){
			if(_cache.get(i).getBlockID() == blockID)
				return i;
		}
		// Block not in cache.
		return -1;
	}

	private void helperBufferSync(DBuffer buf){
		if(!buf.checkClean()){
			buf.startPush();
			while(!buf.waitClean()){
				// waiting...
				buf.startPush();
			}
		}
	}
	
	public CVirtualDisk getDisk(){
		return _disk;
	}

	/*
	 * sync() writes back all dirty blocks to the volume and wait for completion.
	 * The sync() method should maintain clean block copies in DBufferCache.
	 */
	public synchronized void sync(){
		for(CBuffer buf : _cache){
			this.helperBufferSync(buf);
		}
	}

	public void clearFileBlocks(int blockID) {
		for(int i = blockID; i< blockID+Constants.MAX_FILE_BLOCKS; i++){
			int index = this.getCacheAddress(i);
			if(index >= 0)
				_cache.remove(index);
		}
	}

}
