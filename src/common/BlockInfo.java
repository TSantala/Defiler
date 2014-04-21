package common;

public class BlockInfo {
	
	int _blockID;
	int _fileID;
	
	public BlockInfo(int blockID, int fileID){
		_blockID = blockID;
		_fileID = fileID;
	}
	
	public int getBlockID(){
		return _blockID;
	}
	
	public int getFileID(){
		return _fileID;
	}
	
	@Override
	public boolean equals(Object other){
		BlockInfo otherBI = (BlockInfo) other;
		return (_blockID == otherBI.getBlockID() && _fileID == otherBI.getFileID());
	}

}
