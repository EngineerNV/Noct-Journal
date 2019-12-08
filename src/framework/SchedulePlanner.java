package framework;

import java.awt.Color; // for testing

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import framework.info.Block;
import framework.info.BlockType;
import framework.info.SleepAlgorithm;
import framework.info.grid.Schedule;
import framework.info.io.ScheduleFileIO;

/**
 * This class contains methods for creating events for a schedule
 * The SchedulePlanner handles all calendar operations and applies them to the schedule.
 * This includes adding events for a day to the schedule, or adding reocurring events 
 * or more.
 * This class also initializes the IO and Alg classes and launches saving and loading
 * 
 * @author Andre Allan Ponce
 *
 */
public class SchedulePlanner {
	
	// Default Constructor
	public SchedulePlanner(){
		ScheduleFileIO.init();
		SleepAlgorithm.init();
		setSchedule(new Schedule());
		calendar = new GregorianCalendar();
	}
	
	// Constructor with a given Scheudle
	public SchedulePlanner(Schedule s){
		SleepAlgorithm.init();
		ScheduleFileIO.init();
		setSchedule(s);
		calendar = new GregorianCalendar();
	}
	
	//// uses SleepAlg constants
	//public boolean addEvent(int startDay, int startHour, int startMinutes, int endDay, int endHour, int endMinutes, boolean reoccur){
		//switch(startDay){
		//case SleepAlgorithm.MONDAY: calendar.(GregorianCalendar
		//}
		//return true;
	//}
	
	// adds an event to the schedle as a block
	public boolean addEvent(Block b){
		return schedule.addBlock(b);
	}
	
	// version without type, defaults to BlockType.EVENT
	public boolean addEvent(Date date, long length){
		return schedule.addBlock(date, length, BlockType.EVENT);
	}
	
	// version with type.
	public boolean addEvent(Date date, long length, BlockType type){
		return schedule.addBlock(date, length, type);
	}
	
	// calls SleepAlgorithm's algorithm
	public void applyAlgorithm(){
		SleepAlgorithm.applyAlgorithm(schedule, new GregorianCalendar());
	}
	
	// erases all non-reoccuring events from the schdule
	public void clean(){
		SleepAlgorithm.cleanCalendar(schedule);
	}
	
	// erases all events from the schdule
	public void clear(){
		SleepAlgorithm.clearCalendar(schedule);
	}
	
	// call this when exiting. Saves the exit timestamp\
	// also save the stats
	public void exitProgram(){
		ScheduleFileIO.saveExit(new Date());
		this.saveStats();
	}
	
	// retrieves the calendar used by this planner
	public GregorianCalendar getCalendar(){
		return calendar;
	}
	
	// retrives a Block at the date
	public Block getEventAt(Date date){
		return schedule.getBlockAt(date);
	}
	
	//public Block getBlockAt(int day, int hour, int minute){
		//return null;
	//}
	
	public Schedule getSchedule(){
		return schedule;
	}
	
	public Date getSleepTimestamp(){
		return schedule.getPreviousSleep();
	}
	
	/**
	 * Creates a new Date with the moment this method was called and saves it to the previous Schedule
	 * Also saves it to the file
	 */
	public void goToSleep(){
		Date d = new Date();
		schedule.setPreviousSleep(d);
		ScheduleFileIO.saveSleep(d);
	}
	
	/**
	 * Attempts to load the file
	 * Calling this method will NOT guarentee that the
	 * file was loaded. If the file failed to load, a 
	 * detailed message will be displayed in the log file
	 * 
	 * @return true if the file exists, false if not
	 */
	public boolean load(){
		if(ScheduleFileIO.doesFileExist()){
			schedule = ScheduleFileIO.loadSchedule();
			return true;
		}
		return false;
	}
	
	//public Block removeEvent(int day, int hour, int minutes){
		//return null;
	//}
	
	public Block removeEvent(Date date){
		return schedule.removeBlockAt(date);
	}
	
	// removes the events of the given type
	public void removeEventsOfType(BlockType type){
		schedule.removeBlocksOfType(type);
	}
	
	// total save
	// Saves ALL parameters that canbe saved
	public void save(){
		ScheduleFileIO.saveSchedule(schedule);
	}
	
	// save blocks and saves stats
	public void saveBlocks(){
		ScheduleFileIO.saveAllBlocks(schedule.getAllBlocks());
		this.saveStats();
	}
	
	// saves the nap pref and the wake up Times
	public void savePreference(){
		ScheduleFileIO.savePreference(SleepAlgorithm.getNapPreference());
		ScheduleFileIO.saveWakeUpTimes(SleepAlgorithm.getWakeTimes());
	}
	
	public void setSchedule(Schedule s){
		this.schedule = s;
	}
	
	// save the Statistics
	public void saveStats(){
		ScheduleFileIO.saveStats(SleepAlgorithm.getStats());
	}
	
	// creates the defeault Date (with 0 time)
	// and saves it to the file 
	// also sets the scheudle to that date
	public void wakeUp(){
		Date d = new Date(0L);
		ScheduleFileIO.saveSleep(d);
		schedule.setPreviousSleep(d); 
	}
	
	private Schedule schedule;
	private GregorianCalendar calendar;
	
	// TEST METHOD
	

	//~ public static void main(String[] args){
		//~ SchedulePlanner sp = new SchedulePlanner();
		//~ //ScheduleFileIO.init();
		//~ System.out.println("File found:"+sp.load());
		//~ System.out.println(sp.getSchedule());
		//~ //sp.applyAlgorithm();
		//~ GregorianCalendar gc = new GregorianCalendar();
		//~ gc.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
		//~ gc.set(GregorianCalendar.HOUR_OF_DAY, 1);
		//~ System.out.println(gc.getTime());
		//~ sp.removeEvent(gc.getTime());
		//~ //System.out.println(sp.getSchedule());
		//~ //printCalendars(SleepAlgorithm.getWakeTimes());
		//~ //System.out.println("Sleep:"+sp.getSleepTimestamp());
		//~ //SleepAlgorithm.setProgramExitTimestamp(new Date());
		//~ //System.out.println(SleepAlgorithm.getExitTimestamp());
		
		//~ //sp.saveBlocks();
		//~ //sp.goToSleep();
		//~ //SleepAlgorithm.init();
		//~ //SleepAlgorithm.createWakeUpTimes(4, sp.getCalendar());
		//~ //SleepAlgorithm.MakeSleepTimes(sp.getSchedule());
		//~ //sp.getSchedule().getAllBlocks().get(0).setType(BlockType.NAP);
		//~ //sp.getSchedule().getAllBlocks().get(3).setType(BlockType.WORK);
		//~ GregorianCalendar gc = new GregorianCalendar();
		//~ //sp.getSchedule().removeBlocksOfType(BlockType.SLEEP);
		//~ Schedule s = sp.getSchedule();
		//~ gc.setTime(s.getAllBlocks().get(s.getAllBlocks().size()-1).getDate());
		//~ System.out.println(sp.getSchedule().findLocationFor(s.getAllBlocks().get(s.getAllBlocks().size()-2)));
		
		//~ //SleepAlgorithm.setProgramExitTimestamp(new Date());
		//~ //sp.getSchedule().getAllBlocks().get(3).setName("test");
		//~ //sp.getSchedule().getAllBlocks().get(4).setColor(Color.BLACK);
		
		//~ SleepAlgorithm.setStat(SleepAlgorithm.SLEEP_MISSED, 1200303L);
		//~ SleepAlgorithm.setStat(SleepAlgorithm.HOURS_SLEPT, 800123414L);
		//~ SleepAlgorithm.setStat(SleepAlgorithm.SLEEP_NEEDED, 54444L);
		//~ SleepAlgorithm.setStat(SleepAlgorithm.AVG_HOURS_SLEPT, 9008585L);
		//~ SleepAlgorithm.setStat(SleepAlgorithm.NUM_OF_DAY_PASS, 1093993L);
		//~ sp.saveStats();
		//~ //printStats(SleepAlgorithm.getStats());
		//~ //System.out.println(SleepAlgorithm.getStat(SleepAlgorithm.SLEEP_MISSED));
		
		//~ System.out.println(sp.getSchedule());
		//~ sp.save();
		//~ //sp.saveBlocks();
		//~ sp.exitProgram();
	//~ }
	
	//~ public static void printStats(long[] stats){
		//~ for(int i = 0; i < stats.length; i++){
			//~ System.out.println(stats[i]);
		//~ }
	//~ }
	
	//~ public static void printCalendars(GregorianCalendar[] times){
		//~ for(int i = 0; i < times.length; i++){
			//~ System.out.println(times[i].getTime());
		//~ }
	//~ }
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define SchedulePlannerUML
 * 
 * class SchedulePlanner{
 * 	-schedule : Schedule
 * 	-calendar : GregorianCalendar
 * 
 * 	+SchedulePlanner()
 * 	+SchedulePlanner(Schedule)
 * 	+addEvent(Block) : boolean
 * 	+addEvent(Date, long) : boolean
 * 	+addEvent(Date, long, BlockType) : boolean
 * 	+applyAlgorithm() : void
 * 	+clean() : void
 * 	+clear() : void
 * 	+exitProgram() : void
 * 	+getCalendar() : GregorianCalendar
 * 	+getEventAt(Date) : Block
 * 	+getSchedule() : Schedule
 * 	+getSleepTimestamp() : Date
 * 	+goToSleep() : void
 * 	+load() : boolean
 * 	+removeEvent(Date) : Block
 * 	+removeEventsOfType(BlockType) : void
 * 	+save() : void
 * 	+saveBlocks() : void
 * 	+savePreference() : void
 * 	+setSchedule(Schedule) : void
 * 	+saveStats() : void
 * 	+wakeUp() : void
 * }
 * @enduml
 */
