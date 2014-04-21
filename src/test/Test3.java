package test;

import common.DFileID;

import dfs.CDFS;

	public class Test3 extends Thread{

		private CDFS myDFS;

		public Test3(CDFS dfs){
			myDFS = dfs;
		}

		public void run(){
			System.out.println("Num dfiles on init: " + myDFS.listAllDFiles().size());
		}
	}