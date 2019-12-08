package framework.info.grid;

import java.util.ArrayList;
import java.util.Date;
import framework.info.Block;
import framework.info.BlockType;
import framework.info.SleepAlgorithm;

/**
 * The schedule contains the ArrayList for managing blocks. 
 * Schedule handles operations involving the actual structure containing the blocks
 * This includes adding, removing, and retrieving blocks.
 * 
 * Schedule also maintains the previousSleepTime for some reason.
 * It defaults to a date with 0time
 * 
 * @author Andre Allan Ponce
 *
 */
public class Schedule {
	
	public Schedule(){
		//grid = new Block[NUM_ROWS][NUM_COLS];
		grid = new ArrayList<Block>();
		previousSleepTime = new Date(0L); 
	}
	
	/**
	 * Binary inserts a block into the Schedule
	 * If the prospective block spans two days (goes
	 * over a midnight), this method will split the block
	 * into two blocks and binary inserts both of them.
	 * 
	 * @param b - the block to add
	 * @return true if we added the block, false if not
	 */
	public boolean addBlock(Block b){
		int location = findLocationFor(b);
		//boolean test = ;
		//System.out.println(test);
		if(canInsertAt(b,location)){
			if(SleepAlgorithm.doesBlockSpanTwoDays(b)){
				grid.add(location,SleepAlgorithm.getFirstHalfBlockOfBlock(b));
				grid.add(location+1,SleepAlgorithm.getLastHalfBlockOfBlock(b));
			}else{
				grid.add(location,b);
			}
			return true;
		}
		return false;
		//int debugInt = binaryInsert(b,0,grid.size());
		//System.out.println(debugInt);
	}
	
	/**
	 * Varient of addBlock that creates the block to add
	 * before adding it to the Schedule (block splitting still applies)
	 */
	public boolean addBlock(Date date, long length, BlockType type){
		Block b = new Block(date,length,type);
		return addBlock(b);
	}
	
	// this method didnt happen
	//public void editBlock(Date date, int param){
		//int loc = findBlockAt(date);
		//if(isValidIndex(loc)){	
			//switch(param){
				//case Block.DATE_PARAM:{
					//grid.get(loc).setDate(
				//}
				//case Block.LENGTH_PARAM:
				//case Block.TYPE_PARAM:
				//case Block.REOCCUR_PARAM:
			//}
	//}
	
	public void clearSchedule(){
		grid.clear();
	}
	
	/**
	 * Retrieves a block from the Schedule given a date that overlaps in a Block
	 * 
	 * @return Block at the given Date, or null if there was no Block at the given date.
	 */
	public Block getBlockAt(Date date){
		int loc = findBlockAt(date);
		if(isValidIndex(loc)){
			return grid.get(loc);
		}
		return null;
	}
	
	/**
	 * returns the index of the block at the given Date
	 * the index is the location of the block in grid
	 */
	public int findBlockAt(Date date){
		int loc = findLocationFor(date);
		if(overlapsAt(date,loc)){
			return loc;
		}
		return -1;
	}
	
	/**
	 * findlocation method using a Block.
	 * Basically passes it to the other findLocationFor
	 * 
	 * @return the index the block is located or 0 if the grid is empty
	 */
	public int findLocationFor(Block block){
		if(grid.size() > 0){
			return findLocationFor(block,0,grid.size());
		}else{
			return 0;
		}
	}
	
	/**
	 * this returns ref to the grid.
	 * i.e: you can remove items from the ArrayList returned from this
	 * and the change will be reflected in this schedule
	 */
	public ArrayList<Block> getAllBlocks(){
		return grid;
	}
	
	/**
	 * gets all the blocks from date From to date To
	 * From is inclusive, to is inclusive
	 * 
	 * Changes in these blocks will NOT be reflected in the grid
	 */
	public ArrayList<Block> getAllBlocksFrom(Date from, Date to){
		int fromLoc = findBlockAt(from);
		int toLoc = findBlockAt(to);
		ArrayList<Block> blocks = new ArrayList<Block>();
		if(isValidIndex(fromLoc) && isValidIndex(toLoc)){
			for(int i = fromLoc; i <= toLoc; i++){
				blocks.add(grid.get(i));
			}
		}
		return blocks;
	}
	
	/**
	 * This returns new list of blocks on the grid 
	 * of type Type
	 * changes to the returned list will NOT be reflected
	 * in the grid.
	 */ 
	public ArrayList<Block> getAllBlocksOfType(BlockType type){
		ArrayList<Block> list = new ArrayList<Block>();
		for(Block b : grid){
			if(b.getType() == type){
				list.add(b);
			}
		}
		return list;
	}
	
	public Date getPreviousSleep(){
		return previousSleepTime;
	}
	
	/**
	 * @return the object that contains date, or null if no object contains the date
	 */
	public Block removeBlockAt(Date date){
		int loc = findBlockAt(date);
		if(isValidIndex(loc)){
			return grid.remove(loc);
		}
		return null;
	}
	
	/**
	 * Removes all the blocks in grid of BlockType type
	 */
	public void removeBlocksOfType(BlockType type){
		for(int i = grid.size()-1; i>= 0; i--){
			if(grid.get(i).getType() == type){
				grid.remove(i);
			}
		}
	}
	
	/**
	 * removes all blocks in the grid that dont reoccur
	 */
	public void removeNonReocurringBlocks(){
		for(int i = grid.size()-1; i >= 0; i--){
			if(!grid.get(i).isReocurring()){
				grid.remove(i);
			}
		}
	}
	
	public void setPreviousSleep(Date sleep){
		previousSleepTime = sleep;
	}
	
	@Override
	public String toString(){
		String contents = "";
		if(grid.size() > 0){
			for(Block b : grid){
				contents = contents + b + "\n";
			}
		}else{
			contents = "Schedule Emtpy";
		}
		return contents;
	}
	
	//=============================
	// PRIVATE METHODS
	//=============================
	/**
	 * checking for insertion only requires that the block be not overlapping with others
	 * 
	 * @return true if no blocks overlap b at location loc, false if otherwise
	 */
	private boolean canInsertAt(Block b, int loc){
		if(grid.size() > 0){
			if(isValidIndexForInsertion(loc)){
				if(isValidIndex(loc)){
					return !doesOverlapWithSurroundings(b,loc);
				} 
				// this case only happens the loc == grid.size()
				return !b.overlapsWith(grid.get(loc-1));
			}
			return false;
		}
		// since theres no items in the grid, the location about 0
		return loc == 0; 
	}
	
	/**
	 * Checks if the given block overlaps with any of the blocks at loc in the grid
	 * Assumes that the block b goes into the grid at loc
	 * Also assumes that the loc is within the bounds of the grid
	 * assumes block is NOT in grid
	 * 
	 * This method checks the block at loc-1 and the block at loc for overlapping, 
	 * unless loc == 0, which then the method only checks for the block at loc
	 * 
	 * @param b - the block to check for overlap
	 * @param loc - the location the block b would be in the grid
	 * @return true if the given block overlaps with the block at loc or the block 
	 * 	before loc, false otherwise.
	 */
	private boolean doesOverlapWithSurroundings(Block b, int loc){
		if(loc == 0){
			return b.overlapsWith(grid.get(loc));
		}
		return b.overlapsWith(grid.get(loc-1)) || b.overlapsWith(grid.get(loc));
	}
	
	// findLocation method that uses a date instead.
	// basically passes it on to the binary search version
	private int findLocationFor(Date date){
		return findLocationFor(new Block(date,0L,BlockType.FREE));
	}
	
	/**
	 * Finds the location in the array where block could be located, inserted, or 
	 * overlaps with another block
	 * Used for figuring where a block could be inserted, removed, or accessed.
	 * Does binarySearch (Assumes that the size() > 0)
	 * 
	 * The location returned will be:
	 * 		the index of the block that overlaps with this block
	 * OR if no block overlaps with this block,
	 * 		the index of the block that would be right after this block 
	 * 
	 * @return the location to insert, remove, or access a block. 
	 */
	private int findLocationFor(Block block, int start, int end){
		//System.out.println(start+":"+end);
		if(end-start > 1){
			int mid = (start+end)/2;
			if(block.overlapsWith(grid.get(mid))){
				return mid;
			}else if(block.isAfter(grid.get(mid))){
				return findLocationFor(block,mid+1,end);
			}else{ // block is before the block at mid
				return findLocationFor(block,start,mid);
			}
		}else if(end-start == 0){
			return end;
		}else if(block.overlapsWith(grid.get(start)) || block.isBefore(grid.get(start))){
			return start;
		}else{ // block is def after the block at start
			return end; 
		}
	}
	
	private boolean isBlockAt(int loc){
		return isValidIndex(loc);
	}
	
	private boolean isIndexAtEdges(int loc){
		return loc == 0 || loc == grid.size()-1;
	}
	
	private boolean isValidIndex(int loc){
		return loc >= 0 && loc < grid.size();
	}
	
	// a valid index for insertion is 0 to grid.size()
	private boolean isValidIndexForInsertion(int loc){
		return loc == grid.size() || isValidIndex(loc);
	}
	
	// checks if this date overlaps with the block at loc
	private boolean overlapsAt(Date date, int loc){
		return grid.get(loc).overlapsWith(new Block(date,0L,BlockType.FREE));
	}
	


//	private Block[][] grid;
	private ArrayList<Block> grid;
	private Date previousSleepTime;
	
	// TEST METHOD
	//~ public static void main(String[] args){
		//~ Schedule s = new Schedule();
		//~ Date d = new Date();
		
		
		//~ Block b = new Block(d, 1200000L, BlockType.SLEEP);
		//~ //s.getCalendar().set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		//~ Block b2 = new Block(new Date(d.getTime()+b.getLength()),120000L, BlockType.WORK);
		//~ Block b3 = new Block(new Date(b2.getDate().getTime()+b2.getLength()),120000L, BlockType.SLEEP);
		//~ //System.out.println(b.getDate().getTime());
		//~ //System.out.println(s);
		//~ s.addBlock(b);
		//~ s.addBlock(b2);
		//~ s.addBlock(b3);
		//~ System.out.println(s);
		//~ System.out.println(s.findBlockAt(b3.getDate()));
		//~ //s.getAllBlocksOfType(BlockType.SLEEP).get(0).setType(BlockType.FREE);
		//~ //System.out.println(s);
		//~ //s.getAllBlocks().get(0).setType(BlockType.EVENT);
		//~ //System.out.println(s);
		//~ //s.removeBlockAt(b2.getDate());
		//~ //s.getAllBlocks().remove(1);
		//~ //System.out.println(s);
		//~ //System.out.println(d);
		//~ //System.out.println(b);
	//~ }
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define ScheduleUML
 * 
 * class Schedule{
 * 	-grid : ArrayList<Block>
 * 	-previousSleepTime : Date
 * 
 * 	+Schedule()
 * 	+addBlock(Block) : boolean
 * 	+addBlock(Date, long, BlockType) : boolean
 * 	+clearSchedule() : void
 * 	+getBlockAt(Date) : Block
 * 	+findBlockAt(Date) : int
 * 	+findLocationFor(Block) : int
 * 	+getAllBlocks() : ArrayList<Block>
 * 	+getAllBlocksFrom(Date, Date) : ArrayList<Block>
 * 	+getAllBlocksOfType(BlockType) : ArrayList<Block>
 * 	+getPreviousSleep() : Date
 * 	+removeBlockAt(Date) : Block
 * 	+removeBlocksOfType(BlockType) : void
 * 	+removeNonReocurringBlocks() : void
 * 	+setPreviousSleep(Date) : void
 * 	+toString() : String
 * 	-canInsertAt(Block, int) : boolean
 * 	-doesOverlapWithSurroundings(Block, int) : boolean
 * 	-findLocationFor(Date) : int
 * 	-findLocationFor(Block, int, int) : int
 * 	-isBlockAt(int) : boolean
 * 	-isIndexAtEdges(int) : boolean
 * 	-isValidIndex(int) : boolean
 * 	-isValidIndexForInsertion(int) : boolean
 * 	-overlapsAt(Date, int) : boolean
 * }
 * 
 * @enduml
 * 
 */
