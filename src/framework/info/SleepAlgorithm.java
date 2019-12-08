package framework.info;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.HashMap;

import framework.SchedulePlanner;
import framework.info.grid.Schedule;
/**
 * A mostly static class that handles the sleep algorithm.
 * Given a schedule, this class should adjust sleep times according to the sleep hours
 * we want
 * 
 * @author Andre Allan Ponce & Nick Legend 
 *
 */
public class SleepAlgorithm {

	public static final int RECOMMENDED_SLEEP_HOURS = 56; 
	
	// CONSTANTS for the days for wakeUpTimes
	public static final int MONDAY = 0;
	public static final int TUESDAY = 1;
	public static final int WEDNESDAY = 2;
	public static final int THURSDAY = 3;
	public static final int FRIDAY = 4;
	public static final int SATURDAY = 5;
	public static final int SUNDAY = 6;
	
	// i mean, this could change in the future...
	public static final int NUMBER_OF_DAYS = 7;

	// CONSTANTS for STATS
	public static final int SLEEP_NEEDED = 0;
	public static final int HOURS_SLEPT = 1;
	public static final int SLEEP_MISSED = 2;
	public static final int AVG_HOURS_SLEPT = 3;
	public static final int NUM_OF_DAY_PASS = 4;

	
	public static void applyAlgorithm(Schedule schedule, GregorianCalendar today){
		stats(schedule, today);
		if(getNapPreference()){
			addNaps(schedule, today);
			addLongerSleep(schedule, today);
			return;
		}
		addLongerSleep(schedule, today);
			
	}

	/**
	 * 
	 * prefferNaps is for when you know that naps are wanted
	 * Max of 1 hour
	 * The rest of the time is recovered by longer sleep or vice versa   
	 * 
	 * 
	 * @param schedule
	 */
	public static void addNaps( Schedule schedule, GregorianCalendar currentDay){
		int currentDayIndex = 0;//counter used to find our 1st current day block
		ArrayList<Block> sleepBlocks = schedule.getAllBlocksOfType(BlockType.SLEEP);
		int size = sleepBlocks.size();//going to get all the blocks between sleep blocks
		//this will be used to look for adding naps between blocks on a Day Wakeup - sleep
		if(hasRequiredSleep(schedule)){// leave if required sleep found
			return;
		}
		for(Block b : sleepBlocks){
			//the purpose of this loop is to find my index for the current day
			if(areDatesSameDay(b.getDate(),currentDay.getTime())){
				break;// if the current day index is found stop loop
			}
			currentDayIndex++;
		}
		for(int i = currentDayIndex; i< size-1; i++){//-1 is to make sure we don't enter the last element
			// need to generate an array of blocks for each day then look inside
			if(sleepMissing(schedule) == 0 ){// no missing sleep
				return;
			}
			organizeNapInDay(i, sleepBlocks, schedule, currentDay);//ornganizing one day of Naps
		}
	}

	/**
	 * Checks if the two given Dates are on the same day
	 * Uses a GregorianCalendar to figure it out
	 * 
	 * @author Andre Allan Ponce
	 * @param d1 - givne date to check
	 * @param d2 - other given date to check
	 * @return true if the dates have the same day, false otherwise.
	 */
	public static boolean areDatesSameDay(Date d1, Date d2){
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(d1);
		int dayOne = gc.get(GregorianCalendar.DAY_OF_MONTH);
		gc.setTime(d2);
		return  dayOne == gc.get(GregorianCalendar.DAY_OF_MONTH);
	}
	
	//the Add longer sleep method needs to be tested
	//the only flaw in the idea is that it doesn't consider if the block overlaps
	//I need a function in block the checks if it overlaps with anything
	public static void addLongerSleep( Schedule schedule, GregorianCalendar currentDay){

		
		Block previousBlock;//this is new
		Block currentBlock;//temp variable for forloops
		long time;// repersents where the block starts for time
		long period;//repersents how long the block is 
		int currentDayIndex = 0;//counter starting at the current day of sleep block
		ArrayList<Block> sleepBlocks = schedule.getAllBlocksOfType(BlockType.SLEEP);
		int size = sleepBlocks.size();//size of array of only sleep blocks

		if(hasRequiredSleep(schedule) == true){
			return;
		}
		for(Block b : sleepBlocks){// look through all sleep blocks
			//the purpose of this loop is to find my index for the current day
			if(areDatesSameDay(b.getDate(),currentDay.getTime())){// if they are the same day
			if(sleepBlocks.get(currentDayIndex).overlapsWith(b)){//i dont remember doing this
					currentDayIndex++;
				}
				break;// if the current day index is found stop loop
			}
			currentDayIndex++;// try the next index
		}

		for(int i = currentDayIndex; i< size; i++){
			//the purpose of this loop is to add a max of 2hours sleep a day,from present & future 
			
			previousBlock = sleepBlocks.get(i);//setting up default value
			currentBlock = sleepBlocks.get(i);

			if(i != 0){
				previousBlock = sleepBlocks.get(i-1); // need to track previous block
				currentBlock = sleepBlocks.get(i);
			}
			if(previousBlock == currentBlock){//skip index = 0
				continue;
			}

			period = currentBlock.getLength();//length of currentBlock
			time = currentBlock.getDate().getTime();//block starting time
			long missingTime = sleepMissing(schedule) ;//total missing time
			//missing time needs to be refreshed with every iteration
			if(timeGap(previousBlock, currentBlock) == 0){//if the time gap is 0 
				//it is the 2nd sleep block for that day, do not add to this one
				continue;
			}
			
			if( missingTime >= TWO_HOURS ){//if there is more than two hours or two hours
				//System.out.println("am i not?");
				currentBlock.setLength(period + TWO_HOURS);//setting the length 
				currentBlock.getDate().setTime(time - TWO_HOURS);//adjusting the start time
				checkOverlap(currentBlock, schedule);//make sure there isnt overlap
			}
			else if(missingTime == 0){//if there is no time missing
				break;//stop the for loop
			}
			else{
				
				currentBlock.setLength(period + missingTime);//extending period
				currentBlock.getDate().setTime(time - missingTime);//earlier sleep
				checkOverlap(currentBlock, schedule);//make sure there isn't overlap
			}
		}	

	}

	// rewrote this so its also simpler
	// basically calls schcedule's remove nonreoccur 
	// which does the loop to remove nonreoccur
	// @author Andre Allan Ponce
	public static void cleanCalendar(Schedule schedule ){
		schedule.removeNonReocurringBlocks();
	}

	// rewrote this so its simpler.
	// basically calls schedule's clearSchedule, which
	// iself calls ArrayList's clear method.
	// @author Andre Allan Ponce
	public static void clearCalendar(Schedule schedule ){
		schedule.clearSchedule();
	}
	
	
	
	/**
	 * Checks if a block spans two days
	 * A block spans two days if at least 15 minutes 
	 * of the block goes over a midnight.
	 * 
	 * @author Andre Allan Ponce
	 * @return true if the block spans two days, false if not
	 */
	public static boolean doesBlockSpanTwoDays(Block b){
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(b.getDate());
		int day = gc.get(GregorianCalendar.DAY_OF_WEEK);
		gc.setTime(new Date(b.getDate().getTime()+b.getLength()-FIFTHTEEN_MINUTES));
		return day != gc.get(GregorianCalendar.DAY_OF_WEEK);
	}
	
	/**
	 * Creates a block that consists of the first half
	 * of a block if it spans two days
	 * 
	 * @author Andre Allan Ponce
	 * @return a block that conssits of the given block before midnight if the given block spans two days,
	 * 	or a copy of the given block if the the block does NOT span two days.
	 */
	public static Block getFirstHalfBlockOfBlock(Block b){
		return getBlockFromBlock(b, true);
	}

	/**
	 * Retrieves the length of the given Block before 
	 * midnight if the block spans two days
	 * 
	 * @author Andre Allan Ponce
	 * @return the length (in long) of the given block that is before midnight if the block spans two days,
	 * or the length of the given block (in long) if the block does NOT span two days.
	 */
	public static long getFirstHalfOfBlock(Block b){
		return getPartOfBlockTime(b,true);
	}
	
	/**
	 *  Creates a block that aonsists the last half of 
	 * 	 a block if it spans two days
	 * 	MAY RETURN NULL.
	 * 
	 * @author Andre Allan Ponce
	 * @return a block that consists of the given block after midnight if the given block spans two days,
	 * 	or NULL.
	 */
	public static Block getLastHalfBlockOfBlock(Block b){
		return getBlockFromBlock(b, false);
	}
	
	/**
	 * Retrieves the length of the given block after
	 * midnight if the block spans two days
	 * 
	 * @author Andre Allan Ponce
	 * @return the lenght (in long) of the given block that is after midnight if the block spans two days,
	 * or 0 if the block does NOt span two days
	 */
	public static long getLastHalfOfBlock(Block b){
		return getPartOfBlockTime(b,false);
	}

	public static boolean getNapPreference(){
		return isNapPreferred;
	}

	public static Date getExitTimestamp(){
		return programExitTimestamp;
	}

	/**
	 * Example uses:
	 * SleepAlgorithm.getStat(SleepAlgorithm.SLEEP_MISSED);
	 * 
	 * @author Andre Allan Ponce
	 * @param param - field of stat to retreive
	 * @return the corresponding number of that stat (in long)
	 */
	public static long getStat(int param){
		if(isValidIndexForStatParams(param)){
			return statParams.get(statKeys[param]).longValue();
		}
		return 0L;
	}

	public static int getStatKeyLength(){
		return statKeys.length;
	}

	/**
	 * Returns the stats in statParams as a long array
	 * Each element in the array corresponds to its 
	 * key's index in statKeys.
	 * 
	 * i.e:
	 * statParam.get(statKeys[SLEEP_MISSED]) == getStats()[SLEEP_MISSED]
	 * 
	 * @author Andre Allan Ponce
	 * @return long array representation of the stats in statParam
	 */
	public static long[] getStats(){
		long[] statsArray = new long[statKeys.length];
		for(int i = 0; i < statsArray.length; i++){
			statsArray[i] = getStat(i);
		}
		return statsArray;
	}

	/**
	 * Wakeup times are stored as GregorianCalendars.
	 * If we want to get the wakeup time, this method will
	 * retrieve the corresponding one as a Date object
	 * 
	 * @author Andre Allan Ponce
	 * @param day - the day to retreive the wake upt ime
	 * @return the wakeup time of day as a Date
	 */
	public static Date getTime(int day){
		if(isValidIndexForWakeUpTimes(day)){
			return wakeUpTimes[day].getTime();
		}
		return null; //we should not be going here
	}

	// geet all the wakeup time as an array of
	// Gregorian Calendars
	public static GregorianCalendar[] getWakeTimes(){
		return wakeUpTimes;
	}
	
	public static boolean hasRequiredSleep(Schedule schedule){
		if(sleepMissing(schedule) != 0 ){
			return false;
		}
		return true;
	}
	
	public static void init(){
		wakeUpTimes = new GregorianCalendar[NUMBER_OF_DAYS];
		initWakeUpTimes();
		initStats();
		isNapPreferred = false;
		programExitTimestamp = new Date(0L);
	}
	
	
	//subtracting one hour till 8 hours of sleep has been succeeded.
	public static void MakeSleepTimes( Schedule schedule){
		for(int i =0 ; i<NUMBER_OF_DAYS+1; i++){
			if(i==7){//adding in sunday sleep time
				GregorianCalendar cal = new GregorianCalendar();
				Date date = wakeUpTimes[0].getTime();//temp date object
				cal.setTime(date);
				cal.add(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, 1);
				date = cal.getTime();
				date.setTime(date.getTime() - EIGHT_HOURS);
				schedule.addBlock(date, EIGHT_HOURS, BlockType.SLEEP);
				break;
			}
			Date date = wakeUpTimes[i].getTime();//temp date object
			date.setTime(date.getTime() - EIGHT_HOURS);
			schedule.addBlock(date, EIGHT_HOURS, BlockType.SLEEP);
		}
	}
	
	// quick method to set a GreogiranCalendars's
	// values to midnight
	// @author Andre Allan Ponce
	public static void setCalendarToMidnight(GregorianCalendar gc){
		gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
		gc.set(GregorianCalendar.MINUTE, 0);
		gc.set(GregorianCalendar.SECOND, 0);
	}

	public static void setNapPreference(boolean nap){
		isNapPreferred = nap;
	}

	public static void setProgramExitTimestamp(Date exit){
		programExitTimestamp = exit;
	}

	// sets a statParam
	// mostly used internally or by FileIO.
	public static void setStat(int param, long stat){
		if(isValidIndexForStatParams(param)){
			statParams.put(statKeys[param], new Long(stat));
		}
	}

	/**
	 * Setting time requires the time to be set as well as the day param
	 * 
	 * @author Andre Allan Ponce
	 * @param time - the time to be set in GregorianCalendar
	 * @param day - the day field given from this class
	 */
	public static void setTime(GregorianCalendar time, int day){
		if(isValidIndexForWakeUpTimes(day)){
			wakeUpTimes[day] = time;
		}
	}

	/**
	 * Additional setting wakeup time method using
	 * Date for compatibility
	 * 
	 * @author Andre Allan Ponce
	 * @param time - the time to be set in Date
	 * @param day- the day field given from this class.
	 */
	public static void setTime(Date time, int day){
		if(isValidIndexForWakeUpTimes(day)){
			wakeUpTimes[day].setTime(time);
		}
	}


	//if the user is strating the calendar on a day later than monday then we automatically
	//assume that the user got 8 hours in the past. That way we can just focus on the future.
	// we shouldn't generate sleep blocks for past days the user first opened the calendar on
	// TODO: Take code that calculates total Sleep and put it in separate method that returns total sleep. This method should call that method and compare the returned value with required value.
	public static long sleepMissing(Schedule schedule){
		long requiredSleep = 230400000L - EIGHT_HOURS; //64 -8 hours throughout the week
		long totalSleep = 0L;
		ArrayList<Block> napBlocks = schedule.getAllBlocksOfType(BlockType.NAP);
		ArrayList<Block> sleepBlocks = schedule.getAllBlocksOfType(BlockType.SLEEP);
		for(Block b : sleepBlocks){
			totalSleep += b.getLength();//adding up all the total sleep
		}
		for(Block b : napBlocks){
			totalSleep += b.getLength();//adding up all the total sleep
		}
		long lostSleep = requiredSleep - totalSleep; // tracking the missing sleep
		if(totalSleep < requiredSleep){
			return lostSleep;
		}
		return 0;

	}

	// has paramaters for average days and counters for those days
	// index 0-6 0 being monday -6 sunday
	// returns an array that has 0-1 
	//0 stats has numbers of hours missing
	//1 stats has number of hours to go in the week  
	public static void stats(Schedule s, GregorianCalendar today ){

		int currentDayIndex = 0;//counter starting at the current day of sleep block
		ArrayList<Block> sleepBlocks = s.getAllBlocksOfType(BlockType.SLEEP);
		long hoursSlept = 0;
		long sleepMiss = 0;
		int iterate = 0;
		for(Block b : sleepBlocks){
			//the purpose of this loop is to find my index for the current day
			
			if(areDatesSameDay(b.getDate(),today.getTime())){
				break;// if the current day index is found stop loop
			}
			currentDayIndex++;
		}
		for(int i=0 ; i<currentDayIndex ; i=i+2){
			iterate++;
		}

		for(int i =0; i<currentDayIndex ; i++){
			
			

			
			setStat(NUM_OF_DAY_PASS,getStat(NUM_OF_DAY_PASS)+1);//adding number if days passed				
			
			hoursSlept += sleepBlocks.get(i).getLength();
		}
		setStat(SLEEP_MISSED, (8*iterate)-(hoursSlept - TWO_HOURS*2));
		setStat(HOURS_SLEPT,hoursSlept - TWO_HOURS*2);
		setStat(SLEEP_NEEDED, (56*ONE_HOUR)-(hoursSlept - TWO_HOURS*2) );
//		setStat(AVG_HOURS_SLEPT, getStat(HOURS_SLEPT)/getStat(NUM_OF_DAY_PASS) );
	}
	
	/**
	 * Creates a block that is either the first or last half
	 * of the given block if the given block spans two days.
	 * The first half is the block before midnight, the 
	 * last half is the block after midnight
	 * 
	 * @param b - the block to split
	 * @param firstHalf - true if we want firstHalf of the block, false if we want the last half.
	 * @return a Block that is either the firstHalf of the block,
	 * 	 the lastHalf of the given block, 
	 * 	 a copy of the given block if the block does not span two days and firstHalf is true, 
	 *	or null if the given block does not span two days and firstHalf is false.
	 * 
	 */
	private static void checkOverlap(Block test, Schedule schedule){
		ArrayList<Block> allBlocks = schedule.getAllBlocks();
		int index = schedule.findLocationFor(test);//get the index of block in array list
		Block previousBlock = allBlocks.get(index);// default value used for if statement, if test equals this I will not use the if statment
		if(index != 0){//this will only check for a previous block if the index isnt at the 1st element
			previousBlock = allBlocks.get(index-1);
		}
		Block nextBlock = allBlocks.get(index);// default value used for if statement, if test equals this I will not use the if statment
		if(index+1 != allBlocks.size()){//this will make sure the block isnt already at the end of array
			nextBlock = allBlocks.get(index+1);
		}
		long startTimeOfTestBlock = test.getDate().getTime();
		long endTimeOfTestBlock = test.getDate().getTime() + test.getLength();
		long endingTimeOfPreviousBlock = previousBlock.getLength() + previousBlock.getDate().getTime();
		long startTimeOfNextBlock = nextBlock.getDate().getTime();
		long timeOverlap;// this variable hold the info for the time being overlapped 
		long testLength = test.getLength(); // how long test block lasts
		
		if(previousBlock == nextBlock){ // weird error! shouldn't run overlap
			return;
		}
		if(test != previousBlock  &&startTimeOfTestBlock < endingTimeOfPreviousBlock  ){
			timeOverlap = endingTimeOfPreviousBlock - startTimeOfTestBlock;
			//if the block overlaps with its end make sure there is 15mins of space 
			test.getDate().setTime(startTimeOfTestBlock + timeOverlap+ FIFTHTEEN_MINUTES);
			test.setLength(testLength - timeOverlap - FIFTHTEEN_MINUTES);
		}

		if (endTimeOfTestBlock > startTimeOfNextBlock && test != previousBlock ){
			
			//if the block overlaps with its beginning make sure there is 15mins of space
			timeOverlap = endTimeOfTestBlock - startTimeOfNextBlock;
			test.getDate().setTime(startTimeOfTestBlock - timeOverlap - FIFTHTEEN_MINUTES);
			test.setLength(testLength - timeOverlap - FIFTHTEEN_MINUTES);
		}


	}
	private static Block getBlockFromBlock(Block b, boolean firstHalf){
		if(doesBlockSpanTwoDays(b)){
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(b.getDate());
			gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
			setCalendarToMidnight(gc);
			if(firstHalf){
				return new Block(b.getDate(), getFirstHalfOfBlock(b), b.getType(), b.isReocurring(), b.getColor(), b.getName());
			}
			return new Block(gc.getTime(), getLastHalfOfBlock(b), b.getType(), b.isReocurring(), b.getColor(), b.getName());
		}
		if(firstHalf){
			return new Block(b);
		}
		return null;
	}

	/**
	 * Retrieves either the first or last half length of
	 * a given block if that block spans two days
	 * 
	 * @author Andre Allan Ponce
	 * @param b - the block to split length
	 * @param firstHalf - true if we want the firstHalf length, false if we want the lastHalf length
	 * @return the length of the given Block before midnight (in long) if firstHalf is true, 
	 * 	the length of the given Block after midnight (in long) if firstHalf is false,
	 * 	the length of the given block (in long) if the block does not span two days and firstHalf is true
	 * 	or 0 if the block does not span two days and first half is false.
	 * 
	 */
	private static long getPartOfBlockTime(Block b, boolean firstHalf){
		if(doesBlockSpanTwoDays(b)){
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(b.getDate());
			gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
			setCalendarToMidnight(gc);
			if(firstHalf){
				return gc.getTime().getTime()-b.getDate().getTime();
			}
			return b.getDate().getTime()+b.getLength()-gc.getTime().getTime();
		}
		if(firstHalf){
			return b.getLength();
		}
		return 0L;
	}
	
	
	//tested 
	// when testing don't enter the last index of array it purposely doesnt work
	//this will help organize the nap in one day throughout the week
	//can only have 1 hour per day of sleep
	//naps can only be made if they are at least 15 minutes long eliminating 1 minute nap blocks
	//makes sure they have 15 minutes before they go to sleep and wake up for events
	//don't need to check over lap because it will never overlap with the way it is inputed
	
	//this is created to assist finding places for naps 
	//it will return the difference in time between two blocks start to end
	//tested, should work
	

	private static void initStats(){
		statParams = new HashMap<String, Long>(statKeys.length);
		for(int i = 0; i < statKeys.length; i++){
			statParams.put(statKeys[i], new Long(0L));
		}
	}

	private static void initWakeUpTimes(){
		for(int i = 0; i < NUMBER_OF_DAYS; i++){
			wakeUpTimes[i] = new GregorianCalendar();
		}
	}

	private static boolean isValidIndexForStatParams(int loc){
		return loc >= 0 && loc < statParams.size();
	}

	private static boolean isValidIndexForWakeUpTimes(int loc){
		return loc >= 0 && loc < wakeUpTimes.length;
	}
	
	private static void organizeNapInDay(int dayIndex, ArrayList<Block> sleep, Schedule schedule, GregorianCalendar today){
		if(timeGap(sleep.get(dayIndex),sleep.get(dayIndex+1)) == 0){
			return; //if these blocks are somehow 2 blocks made together for one block gtfo
		}
		//get all the blocks between them because that is a whole day 
		ArrayList<Block> oneDay = schedule.getAllBlocksFrom(sleep.get(dayIndex).getDate(),sleep.get(dayIndex+1).getDate());
		
		int size = oneDay.size();//size of our day
		long timeFree;// time in between blocks
		long timeAdded;// the amount of time added for a nap
		long totalTimeAddedInDay = 0L;
		ArrayList<Block> napsAdded = new ArrayList<Block>();// intializing array for naps added so far
		
		for(int i = 0; i< size-1 ; i++){//running the for loop through the day  except the end
			timeFree = timeGap(oneDay.get(i),oneDay.get(i+1));// getting the space 
			
			for(Block b : napsAdded){
				totalTimeAddedInDay = totalTimeAddedInDay + b.getLength();
			}
			if(totalTimeAddedInDay >= ONE_HOUR - FIFTHTEEN_MINUTES){ // just in case this is messing up it will skip if it is greater or equal to 45 minutes
				//stopping function
				return;
			}
			if(oneDay.get(i).getDate().getTime() < today.getTime().getTime() ){
				//if the time is less than the current time ignore the block in the day 
				continue;
			}
			if(sleepMissing(schedule) == 0 ){
				return;
			}
			//if there is just enough time to add the time missing which is under a hour
			if(timeFree - FIFTHTEEN_MINUTES - FIFTHTEEN_MINUTES >= sleepMissing(schedule) && sleepMissing(schedule) <= ONE_HOUR){
				timeAdded = sleepMissing(schedule);//add missing time
				Date when = new Date();//create date object
				when.setTime(oneDay.get(i).getDate().getTime() + oneDay.get(i).getLength() + FIFTHTEEN_MINUTES);
				Block nap = new Block( when, timeAdded, BlockType.NAP); //create the nap
				if(oneDay.get(i).getType() != BlockType.SLEEP){ // if the block behind is a sleep block dont add
					schedule.addBlock(nap);//add nap to schedule 
					napsAdded.add(nap);//this will track naps that have been added	
				}
			}
			//if there is time that is between 45min - 1 hour
			else if(timeFree >= FOURTYFIVE_MINUTES && timeFree <= ONE_HOUR ){
				timeAdded = timeFree - FIFTHTEEN_MINUTES - FIFTHTEEN_MINUTES; // there needs to be at least 15 minutes before and after an event
				Date when = new Date();
				when.setTime(oneDay.get(i).getDate().getTime() + oneDay.get(i).getLength() + FIFTHTEEN_MINUTES);
				Block nap = new Block( when, timeAdded, BlockType.NAP);
				if(oneDay.get(i).getType() != BlockType.SLEEP){ // if the block behind is a sleep block dont add
					schedule.addBlock(nap);//add nap to schedule 
					napsAdded.add(nap);//this will track naps that have been added	
				}
				
			}
			//if there is more than one hour available 
			else if(timeFree - (2*FIFTHTEEN_MINUTES) >= ONE_HOUR){
				timeAdded = ONE_HOUR ;
				Date when = new Date();
				when.setTime(oneDay.get(i).getDate().getTime() + oneDay.get(i).getLength() + FIFTHTEEN_MINUTES);
				if(oneDay.get(i).getType() == BlockType.SLEEP){ // if the block behind is a sleep block dont add
					when.setTime(oneDay.get(i).getDate().getTime() + oneDay.get(i).getLength() + ONE_HOUR*3);
					//adding 3 hours plus away from wakeup block
				}
				Block nap = new Block( when, timeAdded, BlockType.NAP);
				checkOverlap(nap, schedule); // only time i need to check overlap in here
				schedule.addBlock(nap);
				napsAdded.add(nap);
			}
		}
	}
	private static long timeGap(Block startBlock, Block endBlock){
		long start = startBlock.getLength() + startBlock.getDate().getTime();
		long end = endBlock.getDate().getTime();
		return end - start;
	}
	private static boolean isNapPreferred;
	private static Date programExitTimestamp;

	// wakeUpTimes holds GregorianCalendars
	private static GregorianCalendar[] wakeUpTimes;
	
	// stats are held in this HashMap
	private static HashMap<String, Long> statParams;

	// the keys for the HashMap are held here
	private static final String[] statKeys = {"Sleep Needed", "Hours Slept", "Sleep Missed", "Avg Hours Day", "# of Weekdays"};

	// time constants.
	private static final long EIGHT_HOURS = 28800000L;
	private static final long SIX_HOURS = 21600000L;
	private static final long TWO_HOURS = 7200000L;
	private static final long ONE_HOUR = 3600000L;
	private static final long FIFTHTEEN_MINUTES = 900000L;
	private static final long FOURTYFIVE_MINUTES = 2700000L;


	// TEST METHODS

	public static void main(String [] args)
	{
		Schedule s = new Schedule();
		Date d = new Date();

		//SleepAlgorithm hey
		GregorianCalendar gc = new GregorianCalendar();
		SleepAlgorithm.init();


		//// testing for same dates
		//Date d2 = new Date();
		//System.out.println(areDatesSameDay(d,d2)); // should be true

		//gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
		//d2 = gc.getTime();
		//System.out.println(areDatesSameDay(d,d2)); // should be false

		//gc.add(GregorianCalendar.DAY_OF_MONTH, 6);
		//d2 = gc.getTime();
		//System.out.println(areDatesSameDay(d,d2)); // still false;


		// for testing blocks
		createWakeUpTimes(4, gc);
		SleepAlgorithm.MakeSleepTimes(s);
		//int size = s.getAllBlocks().size();
		//System.out.println(s.findLocationFor(s.getAllBlocks().get(size-1)));
		//System.out.println(s.findBlockAt(s.getAllBlocks().get(size-1).getDate()));
		//s.getAllBlocks().remove(s.getAllBlocks().size() -1);
		Date t = new Date();
		t.setTime(s.getAllBlocks().get(2).getDate().getTime() + s.getAllBlocks().get(2).getLength());

		s.addBlock(t, TWO_HOURS, BlockType.SLEEP);
		//System.out.println(timeGap(s.getAllBlocks().get(0), s.getAllBlocks().get(1)));
	Block test = s.getAllBlocks().get(7);
			//test.getDate().setTime(test.getDate().getTime() +SIX_HOURS);
				//test.setLength(test.getLength() - SIX_HOURS);
				//test = s.getAllBlocks().get(5);
			//test.getDate().setTime(test.getDate().getTime() +SIX_HOURS);
				//test.setLength(test.getLength() - SIX_HOURS);
				test = s.getAllBlocks().get(6);
				test.getDate().setTime(test.getDate().getTime() +SIX_HOURS);
				test.setLength(test.getLength() - SIX_HOURS);
		//System.out.println(sleepMissing(s));	
		//organizeNapInDay(1,s.getAllBlocks(), s);
		gc.setTime(s.getAllBlocks().get(1).getDate());
		applyAlgorithm( s, gc);
		//System.out.println(hasRequiredSleep(s));
		//System.out.println(sleepMissing(s));	
		//Date when = new Date();
		//when.setTime(s.getAllBlocks().get(5).getDate().getTime() + s.getAllBlocks().get(5).getLength()+ FIFTHTEEN_MINUTES);
		//Block nap = new Block( when, ONE_HOUR, BlockType.NAP);
		//System.out.println(when);
		//s.addBlock(nap);
		ArrayList<Block> te = s.getAllBlocksOfType(BlockType.SLEEP);
		te.remove(0);
		
		//System.out.println(s.getAllBlocksOfType(BlockType.NAP));//Why cant I add a nap block?????




		//System.out.println(doesBlockSpanTwoDays(s.getAllBlocks().get(0)));
		//System.out.println(getFirstHalfOfBlock(s.getAllBlocks().get(0)));
		//System.out.println(getLastHalfOfBlock(s.getAllBlocks().get(0)));


		//Block b = new Block(d, 1200000L, BlockType.SLEEP);
		////s.getCalendar().set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		//Block b2 = new Block(new Date(d.getTime()+b.getLength()),120000L, BlockType.WORK);
		//Block b3 = new Block(new Date(b2.getDate().getTime()+b2.getLength()),120000L, BlockType.SLEEP);
		//System.out.println(s);
		//s.addBlock(b);
		//s.addBlock(b2);
		//s.addBlock(b3);
		//s.getAllBlocksOfType(BlockType.SLEEP).get(0).setType(BlockType.FREE);
		//System.out.println(s);
		//s.getAllBlocks().get(0).setType(BlockType.EVENT);
		//s.getAllBlocks().get(1).setReocurring(true);
		//System.out.println(s);
		////s.removeBlockAt(b2.getDate());
		//hey.cleanCalendar(s);
		//System.out.println(s);

		////System.out.println(d);
		////System.out.println(b);

	}

	// Explicit test method that sets up
	// wake up Times
	// this should be either commented out
	// or set to private in final version.
	// this is NOT to be shown in UML
	private static void createWakeUpTimes(int hour, GregorianCalendar gc){
		//gc.set(GregorianCalendar
		gc.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
		gc.set(GregorianCalendar.HOUR_OF_DAY, hour);
		gc.set(GregorianCalendar.MINUTE, 0);
		gc.set(GregorianCalendar.SECOND, 0);

		for(int i = 0; i < 7; i++){
			SleepAlgorithm.setTime(gc.getTime(), i);
			gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
		}
	}



}

/* PLANTUML CODE
 * 
 * @startuml
 * !define SleepAlgorithmUML
 * 
 * class SleepAlgorithm{
 * 	+RECOMMENDED_SLEEP_HOURS : int {static}
+MONDAY : int {static}
+TUESDAY : int {static}
+WEDNESDAY : int {static}
+THURSDAY : int {static}
+FRIDAY : int {static}
+SATURDAY : int {static}
+SUNDAY : int {static}
+NUMBER_OF_DAYS : int {static}
+SLEEP_NEEDED : int {static}
+HOURS_SLEPT : int {static}
+SLEEP_MISSED : int {static}
+AVG_HOURS_SLEPT : int {static}
+NUM_OF_DAY_PASS : int {static}
-isNapPreferred : boolean {static}
-programExitTimestamp : Date {static}
-wakeUpTimes : GregorianCalendar[] {static}
-statParams : HashMap<String, Long> {static}
-statKeys : String[] {static}
-EIGHT_HOURS : long {static}
-SIX_HOURS : long {static}
-TWO_HOURS : long {static}
-ONE_HOUR : long {static}
-FIFTHTEEN_MINUTES : long {static}
-FOURTYFIVE_MINUTES : long {static}

+applyAlgorithm(Schedule, GregorianCalendar) : void {static}
+addNaps(Schedule, GregorianCalendar) : void {static}
+areDatesSameDay(Date, Date) : Boolean {static}
+addLongerSleep(Schedule, GregorianCalendar) : void {static}
+clearCalendar(Schedule) : void {static}
+doesBlockSpanTwoDays(Block) : boolean {static}
+getFirstHalfBlockOfBlock(Block) : Block {static}
+getFirstHalfOfBlock(Block) : long {static}
+getLastHalfBlockOfBlock(Block) : Block {static}
+getLastHalfOfBlock(Block) : long {static}
+getNapPreference() : boolean {static}
+getExitTimestamp() : Date {static}
+getStat(int) : long {static}
+getStatKeyLength() : int {static}
+getStats() : long[] {static}
+getWakeTimes() :  {static}
+hasRequiredSleep(Schedule) : boolean {static}
+init() : void {static}
+MakeSleepTimes(Schedule) : void {static}
+setCalendarToMidnight(GregorianCalendar) : void {static}
+setNapPreference(boolean) : void {static}
+setProgramExitTimestamp(Date) : void {static}
+setStat(int, long) : void {static}
+setTime(GregorianCalendar, int) : void {static}
+setTime(Date, int) : void {static}
+sleepMissing(Schedule) : long {static}
+stats(Schedule, GregorianCalendar) : void {static}
-checkOverlap(Block, Schedule) : void {static}
-createWakeUpTimes(int, GregorianCalendar) : void {static}
-getBlockFromBlock(Block, boolean) : Block {static}
-getPartOfBlockTime(Block, boolean) : long {static}
-initStats() : void {static}
-initWakeUpTimes() : void {static}
-isValidIndexForStatParams(int) : boolean {static}
-isValidIndexForWakeUpTimes(int) : boolean {static}
-organizeNapInDay(int, ArrayList<Block>, Schedule, GregorianCalendar) : void {static}
-timeGap(Block, Block) : long {static}
 * }
 * @enduml
 */
