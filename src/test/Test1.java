package test;

import common.DFileID;

import dfs.CDFS;

	public class Test1 extends Thread{

		private CDFS myDFS;

		public Test1(CDFS dfs){
			myDFS = dfs;
		}

		public void run(){
			DFileID file0 = myDFS.createDFile();
			DFileID file1 = myDFS.createDFile();
			byte[] buffer0 = {1,2,3,4,5,6,7,8,9,0};

			myDFS.write(file0, buffer0, 0, 10);
			byte[] buffer0out = new byte[10];
			myDFS.read(file0, buffer0out, 0, 10);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myDFS.write(file1, buffer0, 0, 10);
			myDFS.read(file1, buffer0out, 0, 10);
			
			boolean equal = true;
			for(int i = 0; i<buffer0.length; i++){
				System.out.println("buffer0 " + buffer0[i] + " buffer0out " + buffer0out[i]);
				if(buffer0[i] != buffer0out[i])
					
					equal = false;
			}
			
			if(equal)
				System.out.println("TEST1 SUCCESSFUL!");
			else
				System.out.println("TEST1 has failed.");
		}
	}