package test;

import common.DFileID;

import dfs.CDFS;

public class Test2 extends Thread{

	private CDFS myDFS;

	public Test2(CDFS dfs){
		myDFS = dfs;
	}

	public void run(){
		DFileID file0 = myDFS.createDFile();
		byte[] buffer0 = new byte[5000];
		for(int i = 0; i<5000; i++){
			buffer0[i] = (byte) (i%10);
		}

		myDFS.write(file0, buffer0, 0, 5000);
		byte[] buffer0out = new byte[5000];
		myDFS.read(file0, buffer0out, 0, 5000);

		boolean equal = true;
		for(int i = 0; i<buffer0.length; i++){
			if(buffer0[i] != buffer0out[i])
				equal = false;
		}
					
		if(equal)
			System.out.println("TEST2 SUCCESSFUL!");
		else
			System.out.println("TEST2 has failed.");
	}
}