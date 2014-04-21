import java.util.List;

import common.DFileID;

import test.*;

import dfs.CDFS;


public class Main {

	public static void main(String[] args){
		
		CDFS myDFS = null;
		
		if(args.length == 0)
			myDFS = new CDFS();
		else if(args.length == 1){
			boolean format = args[0].toLowerCase().equals("true");
			myDFS = new CDFS(format);
		}
		else if(args.length == 2){
			boolean format = args[0].toLowerCase().equals("true");
			String volName = args[1];
			myDFS = new CDFS(volName,format);
		}

		System.out.println("Start!");

//		Test1 test1 = new Test1(myDFS);
//		test1.start();
//		
//		Test2 test2 = new Test2(myDFS);
//		test2.start();
		
		List<DFileID> stored = myDFS.listAllDFiles();
		
		for(DFileID id : stored)
			System.out.println("Restored file: "+id.getDFileID());
		
//		while(!stored.isEmpty()){
//			System.out.println("Removing one!");
//			myDFS.destroyDFile(stored.remove(0));
//		}
	}

}
