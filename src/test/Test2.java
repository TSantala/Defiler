package test;

import common.DFileID;

import dfs.CDFS;

public class Test2 extends Thread{

	private CDFS myDFS;

	public Test2(CDFS dfs){
		myDFS = dfs;
	}

	public void run(){
		DFileID file1 = myDFS.listAllDFiles().get(0);
		DFileID file0 = myDFS.createDFile();
		//DFileID file1 = myDFS.createDFile();
		//DFileID file2 = myDFS.createDFile();
		byte[] buffer0 = new byte[5000];
		for(int i = 0; i<5000; i++){
			buffer0[i] = (byte) (i%10);
		}
		byte[] buffer1 = {1,2,3,4,5,6,3,8,9,0};
		byte[] buffer1out = new byte[10];
		
		myDFS.write(file1, buffer1, 0, 10);
		myDFS.read(file1, buffer1out, 0, 10);

		byte[] buffer0out = new byte[5000];
		
		myDFS.write(file0, buffer0, 0, 5000);
		myDFS.read(file0, buffer0out, 0, 5000);

		boolean equal = true;
		for(int i = 0; i<buffer0.length; i++){
			if(buffer0[i] != buffer0out[i]){
			//	System.out.print("" + buffer0[i] + " " + buffer0out[i]);
				equal = false;
			}
		}
					
		if(equal)
			System.out.println("TEST2 SUCCESSFUL!");
		else
			System.out.println("TEST2 has failed.");
	}
}