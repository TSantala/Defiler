/**********************************************
 * Please DO NOT MODIFY the format of this file
 **********************************************/

/*************************
 * Team Info & Time spent
 *************************/

	Name1: Timo Santala
	NetId1: tjs40
	Time spent: 10 hours  

	Name2: Matthew Parides
	NetId2: mmp37
	Time spent: 10 hours

/******************
 * Files to submit
 ******************/

	lab4.jar // An executable jar including all the source files and test cases.
	README	// This file filled with the lab implementation details.
    Source code // I believe this is also requested of us.

/************************
 * Implementation details
 *************************/

/* 
 * This section should contain the implementation details and a overview of the
 * results. You are required to provide a good README document along with the
 * implementation details. In particular, you can pseudocode to describe your
 * implementation details where necessary. However that does not mean to
 * copy/paste your Java code. Rather, provide clear and concise text/pseudocode
 * describing the primary algorithms (for e.g., scheduling choices you used)
 * and any special data structures in your implementation. We expect the design
 * and implementation details to be 3-4 pages. A plain textfile is encouraged.
 * However, a pdf is acceptable. No other forms are permitted.
 *
 * In case of lab is limited in some functionality, you should provide the
 * details to maximize your partial credit.  
 * */
 
 As should be clear after a cursory look over our main method, the executable jar may be run with no commands, in which case the previous
 volume is used (format = false) and the default file name is used, one command, which should be the word 'true' or any other word and is
 used as the boolean input for format (to use the previous volume or reset), or two commands, the first being 'true'/other and the second
 being the volume name.
 
 Our main method created a CDFS object (each of the objects we created to extend the precursor classes provided we have prefix'ed with a C)
 which sets our entire Defiler system. The Defiler creates the CBufferCache, which in turn instantiates the CVirtualDisk, which is
 initialized based on the String volName and boolean format.
 
 Our overall design almost precisely mirrors that discussed in class. The simplifications allowed for in the Defiler system made this project
 very manageable for us. The process begins with the creation of a file, by calling CDFS.createDFile(). File IDs are incremented until the 
 maximum number of files are created, at which point previous, destroyed file IDs are recycled. The one major detraction our project has from 
 class design discussions is the other functionality in createDFile(): we have the disk write to the inode region to indicate that the new 
 file ID exists as a file now. Similarly, in a destroyDFile call, the disk is informed that this file no longer exists (in addition to removing
 the file from the list of files and enabling the ID to be recycled for future files created). 
 
 Once a file is created, reads and writes may be issued to this file, through CDFS.read(...) and CDFS.write(...). We make great use of this
 lab's simplifications for these methods. First, we take advantage of the assumption that our disk has enough space for all possible files all
 of max possible file size. With this knowledge, the location of a file's data is trivial: blockID = [size of inode region, in blocks] + 
 fileID * [max file size, in blocks]. Given the simplification expressed above, this arithmetic should be patently accurate. With the starting 
 blockID acquired, we loop until all requested read/write bytes have been handled (count bytes) or until an error occurs, in which case we return
 however many bytes were successfully handled. We acquire the associated block from the cache (we'll discuss this process shortly), request a 
 read/write through this block, then release the block once the read/write completes. We then increment the offset in the buffer and repeat this 
 process until the requested number of bits have been processed.
 
 The only other functionality in the CDFS is to listAllDFiles and sync(); listing the files is trivial an sync is simply passed on to the cache.
 
 The core functionality of the cache is within CBufferCache.getBlock(id). We begin by checking if the requested block is already in our cache, 
 in which case we remove and re-add the buffer (to implement our LRU policy), mark the buffer as held (recall the CDFS will downcall to inform 
 that it is no longer held), and return the block. Easy-peasy. If the block is not within the cache, we create a new CBuffer, set held = true, 
 and add it to the cache. If the cache is full, we loop through by increasing index (LRU policy, see above) to find the block to be evicted, 
 push the buffer's contents to disk if it isn't clean (CBuffer.startPush()), then evict the buffer. We finally return our newly allocated CBuffer.
 Apart from self-explanatory helper methods, the only other functionality in the cache is the sync the cache, by which each CBuffer in the cache
 is looped through and has its contents pushed to disk if it's dirty. Bam, we have a cache.
 
 Next we have the CBuffer, which arguably presents the core functionality of this project (especially in terms of concurrency). Each CBuffer 
 accesses the disk through startFetch() and startPush() to fill the buffers contents with valid data or push the buffers current data, respectively.
 At the beginning or either call, the buffer is labelled 'pinned' (as discussed in lecture). At the end of a fetch, the buffer isValid = true, while 
 at the end of a push, the buffer's isClean = true. An upcall to CBuffer.ioComplete by the disk will inform the buffer that it is not longer pinned.
 A block is deemed busy if it is either held or pinned (being held is discussed in the cache paragraph of this analysis). The waitValid and waitClean
 methods are very straightforward. Finally, read and write is simply performed by looping through the buffers and filling the appropriate data! But 
 first, for a read we call this.waitValid() to ensure we return a byte[] with valid data. A write may always proceed without checking: after the write, 
 the buffer isValid = true and isClean = false.
 
 Finally, the core functionality of CVirtualDisk was provided to us in the skeleton and we did not require any manipulation. Our only augmentations to
 CVirtualDisk were for inode implementation. Given the data organization discussed in the above CDFS paragraph, the inode region for this project is 
 only needed on startup, when we must know which files exist if format=false. To this end, we require only 1 block for our inode region (with the
 assumption that block_size > MAX_NUM_DFILES, which seems very reasonable). The inode simply consists of a byte representing each possible DFile; if 
 it is 1, the file exists, if not, the file does not exist. It is based on this information that we return the starting files to CDFS on its call to 
 CVirtualDisk.getFilesOnInit(). The methods writeFileInode and clearFileInode are straightforward (and the manner in which they are called is discussed 
 with the CDFS).
 
 Concurrency - Our program has concurrency control such that buffers lock when they are in action (reading/writing from the cache or
 the disk), and so that only one buffer reads or writes on the disk at any given time. On the CDFS level, any read or write
 locks the buffer so that it cannot be acted upon by another source during the execution of the requested action. This locking
 is repeated on the CVirtualDisk level so that calls to sync will lock the buffers, preventing calls from users during the time of
 a sync from messing with the blocks that are syncing to disk. We also lock the buffers anytime that we affect the status variables
 so that we have proper control over the timing of state changes of blocks. Additionally, we have synchronization through all
 three layers on the creation and deletion of files so that no file will be doubly created/destroyed.
 
 Testing - Our main method runs two separate test programs - each its own thread. These tests succeed and sufficiently show
 the success of our cache, reads/writes to disk, and concurrency. 
 (Tested using 2 byte blocks and a 2 block cache for concurrency - tested using the standard constants for multi-block read/writes)
  Test 1 does a simple write to one block of one file. Test 2 gets that same file from CDFS and writes conflicting data to the disk.
  The concurrency control in our program ensures that test 1 writes and reads back its own data as a pair without interleaving the
  conflicting writes/reads from test2. the multi-block read/write test writes 5000 bytes to 1024 byte blocks and checks them for
  matching data. When we ran these tests with the given parameters, they both succeeded (reads/write pairs returned the proper data).

/************************
 * Feedback on the lab
 ************************/

/*
 * Any comments/questions/suggestions/experiences that you would help us to
 * improve the lab.
 * */
 I think this lab would have greatly benefited from some instructor-provided test cases for us to (at some level) evaluate our work to 
 some standard. That said, I think we were able to effectively guage the success of our project with intelligent, concise testing.

/************************
 * References
 ************************/

/*
 * List of collaborators involved including any online references/citations.
 * */
 Class lectures, especially those devoted to Defiler concepts.
 Stackoverflow, mostly for Java syntax issues.