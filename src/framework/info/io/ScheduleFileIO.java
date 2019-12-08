package framework.info.io;

import java.awt.Color;

import java.io.FileWriter; // logging
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;
import java.io.UTFDataFormatException;
import java.io.File;

import java.util.HashMap; // maybe
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

import framework.info.grid.Schedule;
import framework.info.Block;
import framework.info.BlockType;
import framework.info.SleepAlgorithm;

import framework.util.ByteConverter;

/**
 * 
 * file saving/loading for the schdule
 * Files are saved in the following format:
 * version=<version number>
 * $<Timestamp when sleep button was clicked (in long)>
 * E<Timestamp when program was exited (in long)>
 * P<boolean, true when NAPs are preferred>
 * S<stat stuff>
 * W<monday's wakeuptime (in long)><tuesday's wakeuptime (in long)>...
 * #<block1's type><block1's date (in long)><length (in long)><boolean, true when block is reocccuring>
 * #(next block in grid)...
 * 
 * Lengths will be used to ensure data is written to 
 * the correct spots 
 * 
 * 
 * @author Andre Allan Ponce
 * 
 */
public class ScheduleFileIO{
	
	public static void init(){
		offsetLengths = new HashMap<String, Long>();
		filename = DEFAULT_FILENAME;
		initLengths();
	}
	
	public static boolean doesFileExist(){
		try{
			File file = new File(filename);
			return file.exists();
		}catch(Exception e){
			logToFile(e.getMessage());
		}
		return false;
	}
	
	public static String getFilename(){
		return filename;
	}
	
	// save the error message to file!
	public static void logToFile(String message){
		try{
			FileWriter file = new FileWriter(LOG_FILENAME);
			file.write(new Date() + "\n");
			file.write(message+"\n");
			file.close();
		}catch(IOException e){
			System.out.println(ERROR_FATAL);
		}
	}
	
	public static Schedule loadSchedule(){
		Schedule s = new Schedule();
		RandomAccessFile file = createFile();
		try{
			readVersion(file);
			s.setPreviousSleep(readSleepTimestamp(file));
			SleepAlgorithm.setProgramExitTimestamp(readExitTimestamp(file));
			SleepAlgorithm.setNapPreference(readPreference(file));
			loadStats(file);
			loadWakeUpTimes(file);
			s = readBlocks(file, s);
		}catch(Exception e){
			logToFile(e.getMessage());
		}
		return s;
	}
	
	public static void saveAllBlocks(ArrayList<Block> blocks){
		RandomAccessFile file = createFile();
		try{
			saveBlocksToFile(file, blocks);
		}catch(IOException e){
			logToFile(e.getMessage());
		}finally{
			closeFile(file);
		}
	}
	
	/**
	 * This method was never implemented
	 * DO NOT USE
	 */
	private static void saveEditedBlocks(ArrayList<Block> blocks){
		// we never made this.
	}
	
	public static void saveExit(Date exit){
		saveTimestamp(exit, true);
	}
	
	public static void savePreference(boolean pref){
		RandomAccessFile file = createFile();
		try{
			savePreferenceToFile(file, pref);
		}catch(IOException e){
			logToFile(e.getMessage());
		}finally{
			closeFile(file);
		}
	}
	
	public static void saveSchedule(Schedule s){
		RandomAccessFile file = createFile();
		try{
			file.setLength(0);
			writeVersion(file);
			writeSleepTimestamp(file, s.getPreviousSleep());
			writeExitTimestamp(file, SleepAlgorithm.getExitTimestamp());
			writeNapPreference(file, true); // for now
			writeStats(file, SleepAlgorithm.getStats());
			writeWakeUpTimes(file, SleepAlgorithm.getWakeTimes());
			writeBlocks(file, s.getAllBlocks());
		}catch(Exception e){
			logToFile(e.getMessage());
		}finally{
			closeFile(file);
		}
	}
	
	public static void saveSleep(Date sleep){
		saveTimestamp(sleep, false);
	}
	
	public static void saveStats(long[] stats){
		RandomAccessFile file = createFile();
		try{
			saveStatsToFile(file, stats);
		}catch(Exception e){
			logToFile(e.getMessage());
		}finally{
			closeFile(file);
		}
	}
	
	public static void saveWakeUpTimes(GregorianCalendar[] times){
		RandomAccessFile file = createFile();
		try{
			saveWakeUpTimesToFile(file, times);
		}catch(Exception e){
			logToFile(e.getMessage());
		}finally{
			closeFile(file);
		}
	}
	
	public static void setFilename(String filename){
		ScheduleFileIO.filename = filename;
	}
	
	//===================================
	// PRIVATE METHODS
	//===================================
	
	// setup all the offset lengths
	private static void initLengths(){
		offsetLengths.put(SAVE_VERSION, new Long(VERSION.length() + VERSION_NUMBER.length()+BYTE_OFFSET));
		offsetLengths.put(SAVE_SLEEP, new Long(Character.BYTES + Long.BYTES));
		offsetLengths.put(SAVE_EXIT, new Long(Character.BYTES + Long.BYTES));
		offsetLengths.put(SAVE_PREF, new Long(Character.BYTES + BOOLEAN_BYTE_SIZE));
		offsetLengths.put(SAVE_STAT, new Long(Character.BYTES + ( SleepAlgorithm.getStatKeyLength() * Long.BYTES ))); // to be changed
		offsetLengths.put(SAVE_WAKE, new Long(Character.BYTES + (Long.BYTES * SleepAlgorithm.NUMBER_OF_DAYS)));
		offsetLengths.put(SAVE_BLOCK, new Long(Character.BYTES + BLOCKTYPE_BYTE_SIZE + Long.BYTES + Long.BYTES + BOOLEAN_BYTE_SIZE));
	}
	
	/**a hackish way of calculating the offset for 
	 * writing to file
	 * the cases (SAVE_XYZ) represent the operation we
	 * intend to do
	 * This means the offset is <length of the record before this operation> + <offset needed for the operation before this operation>
	 * 
	 * ie:
	 * SAVE_PREF = the sum of the lengths for:
	 * 	versionNumber
	 * 	sleepTimeStamp
	 * 	exitTimeStamp
	 * 
	 * SAVE_STAT = the sum of:
	 * 	SAVE_PREF operation's offset
	 * 	napPreferenceLength
	 * 
	 * since switch cases have the fall through "feature" when break is missing,
	 * we can take advantadge of that to avoid using 
	 * the assembly-line if statements. 
	 * (yes, the current switch/case usage here is assembly-line-like, too, 
	 * but it saves us from too much branch prediction)
	 * 
	 * Also, I will admit that this code is terribly unreadable.
	 */
	private static long calculateOffset(String operation){
		long offset = 0L;
		switch(operation){
		case SAVE_BLOCK:	offset += getOffset(SAVE_WAKE);
		case SAVE_WAKE:		offset += getOffset(SAVE_STAT);
		case SAVE_STAT:		offset += getOffset(SAVE_PREF);
		case SAVE_PREF:		offset += getOffset(SAVE_EXIT);
		case SAVE_EXIT:		offset += getOffset(SAVE_SLEEP);
		case SAVE_SLEEP:	offset += getOffset(SAVE_VERSION);
		case SAVE_VERSION:	break;
		default: break; // we shouldnt' be goin here
		}
		return offset;
	}
	
	private static void closeFile(RandomAccessFile file){
		try{
			file.close();
		}catch(Exception e){
			logToFile(e.getMessage());
		}
	}
	
	/**
	 * Converts the byte we read into its corresponding BlockType
	 * 
	 */
	private static BlockType convertByteToBlockType(byte id){
		switch(id){
		case BlockType.ID_CLASS:	return BlockType.CLASS;
		case BlockType.ID_EVENT:	return BlockType.EVENT;
		case BlockType.ID_FREE:		return BlockType.FREE;
		case BlockType.ID_NAP:		return BlockType.NAP;
		case BlockType.ID_SLEEP:	return BlockType.SLEEP;
		case BlockType.ID_WORK:		return BlockType.WORK;
		default:					return null; // we should never be going here
		}
	}
	
	// may return null if ile could not be created
	private static RandomAccessFile createFile(){
		try{
			RandomAccessFile file = new RandomAccessFile(filename, MODE);
			return file;
		}catch(FileNotFoundException e){
			logToFile(e.getMessage());
			return null;
		}
	}
	
//	private static byte[] formatLong(long num){
//		
//	}
	
	private static long getOffset(String key){
		return offsetLengths.get(key).longValue();
	}
	
	private static void loadStats(RandomAccessFile file) throws IOException, DataFormatException{
		long[] stats = readStats(file);
		for(int i = 0; i < stats.length; i++){
			SleepAlgorithm.setStat(i, stats[i]);
			//System.out.println(stats[i]);
		}
	}
	
	private static void loadWakeUpTimes(RandomAccessFile file) throws IOException, DataFormatException{
		GregorianCalendar[] times = readWakeUpTimes(file);
		for(int i = 0; i < SleepAlgorithm.NUMBER_OF_DAYS; i++){
			SleepAlgorithm.setTime(times[i],i);
		}
	}
	
	private static String parseVersion(String version) throws DataFormatException{
		int equalIndex = version.indexOf('=');
		if(equalIndex < 0){
			throw new DataFormatException(ERROR_READING_FILE + SAVE_VERSION + ERROR_NOT_FOUND);
		}else if(equalIndex >= version.length()){
			throw new DataFormatException(ERROR_READING_FILE + SAVE_VERSION + ERROR_FORMAT);
		}else{
			return version.substring(equalIndex+1);
		}
	}
	
	private static Block readBlock(RandomAccessFile file) throws IOException, DataFormatException{
		if(BLOCK == file.readChar()){
			try{
				BlockType type = convertByteToBlockType(file.readByte());
				long start = file.readLong();
				long length = file.readLong();
				boolean reoccur = file.readBoolean();
				Color color = new Color(file.readInt());
				String name = file.readUTF();
				return new Block(new Date(start), length, type, reoccur, color, name);
			}catch(IOException e){
				throw new DataFormatException(ERROR_READING_FILE + SAVE_BLOCK + ERROR_FORMAT + " AT "+file.getFilePointer());
			}
		}
		throw new DataFormatException(ERROR_READING_FILE + SAVE_BLOCK + ERROR_NOT_FOUND + " AT "+file.getFilePointer());
	}
	
	private static Schedule readBlocks(RandomAccessFile file, Schedule schedule) throws IOException, DataFormatException{
		boolean endOfFile = false;
		while(!endOfFile){
			try{
				if(file.readChar() == BLOCK){
					file.seek(file.getFilePointer()-Character.BYTES);
					schedule.addBlock(readBlock(file));
				}else{
					endOfFile = true;
				}
			}catch(EOFException e){
				endOfFile = true;
			}catch(IOException e){
				throw e;
			}
		}
		return schedule;
	}
	
	private static Date readExitTimestamp(RandomAccessFile file) throws IOException, DataFormatException{
		return readTimestamp(file, PROGRAM_EXIT_TIME_STAMP, SAVE_EXIT);
	}
	
	private static boolean readPreference(RandomAccessFile file) throws IOException, DataFormatException{
		if(PREFERENCE == file.readChar()){
			try{
				return file.readBoolean();
			}catch(IOException e){
				throw new DataFormatException(ERROR_READING_FILE + SAVE_PREF + ERROR_FORMAT);
			}
		}
		throw new DataFormatException(ERROR_READING_FILE + SAVE_PREF + ERROR_NOT_FOUND);
	}
	
	private static Date readSleepTimestamp(RandomAccessFile file) throws IOException, DataFormatException{
		return readTimestamp(file, SLEEP_TIME_STAMP, SAVE_SLEEP);
	}
	
	private static long[] readStats(RandomAccessFile file) throws IOException, DataFormatException{
		long[] stats = new long[SleepAlgorithm.getStatKeyLength()];
		if(STATS == file.readChar()){
			try{
				for(int i = 0; i < stats.length; i++){
					stats[i] = file.readLong();
				}
				return stats;
			}catch(IOException e){
				throw new DataFormatException(ERROR_READING_FILE + SAVE_STAT + ERROR_FORMAT);
			}
		}
		throw new DataFormatException(ERROR_READING_FILE + SAVE_STAT + ERROR_NOT_FOUND);
	}
	
	private static Date readTimestamp(RandomAccessFile file, char symbol, String operation) throws IOException, DataFormatException{
		if(symbol == file.readChar()){
			try{
				return new Date(file.readLong());
			}catch(IOException e){
				throw new DataFormatException(ERROR_READING_FILE + operation + ERROR_FORMAT);
			}
		}
		throw new DataFormatException(ERROR_READING_FILE + operation + ERROR_NOT_FOUND);
	}
	
	private static String readVersion(RandomAccessFile file) throws IOException, DataFormatException{
		try{
			String version = file.readUTF();
			return parseVersion(version);
		}catch(UTFDataFormatException e){
			throw new DataFormatException(ERROR_READING_FILE + SAVE_VERSION + ERROR_FORMAT);
		}catch(DataFormatException e){
			throw e;
		}catch(IOException e){
			throw e;
		}
	}
	
	private static GregorianCalendar readWakeUpTime(RandomAccessFile file) throws IOException{
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date(file.readLong()));
		return gc;
	}
	
	private static GregorianCalendar[] readWakeUpTimes(RandomAccessFile file) throws IOException, DataFormatException{
		if(file.readChar() == WAKE){
			try{
				GregorianCalendar[] times = new GregorianCalendar[SleepAlgorithm.NUMBER_OF_DAYS];
				for(int i = 0; i < times.length; i++){
					times[i] = readWakeUpTime(file);
				}
				return times;
			}catch(Exception e){
				throw new DataFormatException(ERROR_READING_FILE + SAVE_WAKE + ERROR_FORMAT);
			}
		}
		throw new DataFormatException(ERROR_READING_FILE + SAVE_WAKE + ERROR_NOT_FOUND);
	}
	
	private static void saveBlocksToFile(RandomAccessFile file, ArrayList<Block> blocks) throws IOException{
		file.seek(calculateOffset(SAVE_BLOCK));
		writeBlocks(file, blocks);
	}
	
	private static void saveBlockToFile(RandomAccessFile file, Block b) throws IOException{
		writeBlock(file, b);
	}
	
	private static void savePreferenceToFile(RandomAccessFile file, boolean pref) throws IOException{
		file.seek(calculateOffset(SAVE_PREF));
		writeNapPreference(file, pref);
	}

	private static void saveStatsToFile(RandomAccessFile file, long[] placeholder) throws IOException{
		file.seek(calculateOffset(SAVE_STAT));
		writeStats(file, placeholder);
	}
	
	private static void saveWakeUpTimesToFile(RandomAccessFile file, GregorianCalendar[] gc) throws IOException{
		file.seek(calculateOffset(SAVE_WAKE));
		writeWakeUpTimes(file, gc);
	}
	
	// when isExit is true, we're saving the exit stamp
	// when false, we save the sleep stamp
	private static void saveTimestamp(Date stamp, boolean isExit){
		RandomAccessFile file = createFile();
		try{
			saveTimestampToFile(file, stamp, isExit);
		}catch(IOException e){
			logToFile(e.getMessage());
		}finally{
			closeFile(file);
		}
	}
	
	private static void saveTimestampToFile(RandomAccessFile file, Date stamp, boolean isExit) throws IOException{
		if(isExit){
			file.seek(calculateOffset(SAVE_EXIT));
			writeExitTimestamp(file, stamp);
		}else{
			file.seek(calculateOffset(SAVE_SLEEP));
			writeSleepTimestamp(file, stamp);
		}
	}
	
	private static void writeBlock(RandomAccessFile file, Block b) throws IOException{
		
		file.write(ByteConverter.charToByte(BLOCK));
		file.writeByte(b.getType().toByte()); // blocktype
		file.write(ByteConverter.longToByte(b.getDate().getTime())); // start of block
		file.write(ByteConverter.longToByte(b.getLength())); // length of block
		file.writeBoolean(b.isReocurring()); // reoccur or nah
		file.write(ByteConverter.intToByte(b.getColor().getRGB()));
		file.writeUTF(b.getName());
	}
	
	private static void writeBlocks(RandomAccessFile file, ArrayList<Block> blocks) throws IOException{
		for(Block b: blocks){
			writeBlock(file, b);
		}
	}
	
	private static void writeExitTimestamp(RandomAccessFile file, Date exit) throws IOException{
		writeTimestamp(file,PROGRAM_EXIT_TIME_STAMP,exit);
	}
	
	private static void writeNapPreference(RandomAccessFile file, boolean pref) throws IOException{
		file.write(ByteConverter.charToByte(PREFERENCE));
		file.writeBoolean(pref);
	}
	
	private static void writeSleepTimestamp(RandomAccessFile file, Date sleep) throws IOException{
		writeTimestamp(file,SLEEP_TIME_STAMP,sleep);
	}
	
	private static void writeStats(RandomAccessFile file, long[] stats) throws IOException{
		file.write(ByteConverter.charToByte(STATS));
		for(int i = 0; i < stats.length; i++){
			file.write(ByteConverter.longToByte(stats[i]));
		}
	}
	
	private static void writeTimestamp(RandomAccessFile file, char type, Date time) throws IOException{
		file.write(ByteConverter.charToByte(type));
		file.write(ByteConverter.longToByte(time.getTime()));
	}
	
	private static void writeWakeUpTime(RandomAccessFile file, GregorianCalendar gc) throws IOException{
		file.write(ByteConverter.longToByte(gc.getTime().getTime()));
	}
	
	private static void writeWakeUpTimes(RandomAccessFile file, GregorianCalendar[] times) throws IOException{
		file.write(ByteConverter.charToByte(WAKE));
		for(GregorianCalendar gc : times){
			writeWakeUpTime(file,gc);
		}
	}
	
	private static void writeVersion(RandomAccessFile file) throws IOException{
		file.writeUTF(VERSION+VERSION_NUMBER);
	}
	
	private static String filename;
	
	private static final String DEFAULT_FILENAME = "default";
	private static final String LOG_FILENAME = "log";
	
	// symbols for the saved file
	private static final String VERSION = "version=";
	private static final char SLEEP_TIME_STAMP = '$';
	private static final char PROGRAM_EXIT_TIME_STAMP = 'E';
	private static final char PREFERENCE = 'P';
	private static final char BLOCK = '#';
	private static final char STATS = 'S';
	private static final char WAKE = 'W';
	
	// Lengths for writing file 
	// these will be set after the init method is called.
	private static HashMap<String, Long> offsetLengths;
	
	// sizes of certain datatypes (for Lengthting)
	//private static final int Character.BYTES = 2;
	//private static final int Integer.BYTES = 4;
	//private static final int Long.BYTES = 8;
	private static final int BOOLEAN_BYTE_SIZE = 1;
	private static final int BLOCKTYPE_BYTE_SIZE = 1;
	private static final int BYTE_OFFSET = 2;
	
	// other file data
	private static final String VERSION_NUMBER = "0.1";
	private static final String MODE = "rwd"; // for RandomaccessFile
	
	// error messages
	private static final String ERROR_FATAL = "FATAL I/O ERROR";
	private static final String ERROR_READING_FILE = "ERROR READING FILE: ";
	private static final String ERROR_NOT_FOUND = " NOT FOUND";
	private static final String ERROR_FORMAT = " FORMAT INVALID";
	
	// Operation IDs
	private static final String SAVE_VERSION = "VERSION";
	private static final String SAVE_SLEEP = "SLEEP TIMESTAMP";
	private static final String SAVE_EXIT = "EXIT TIMESTAMP";
	private static final String SAVE_PREF = "PREFERENCE";
	private static final String SAVE_STAT = "STATS";
	private static final String SAVE_BLOCK = "BLOCK"; // maybe
	private static final String SAVE_WAKE = "WAKE";
	
	//// TEST METHOD
	//public static void main(String[] args){
		////SchedulePlanner sp = new SchedulePlanner();
		//Schedule s = new Schedule();
		////sp.goToSleep();
		//GregorianCalendar gc = new GregorianCalendar();
		//SleepAlgorithm.init();
		//SleepAlgorithm.createWakeUpTimes(4, gc);
		//SleepAlgorithm.MakeSleepTimes(s);
		//SleepAlgorithm.setProgramExitTimestamp(new Date());
		//ScheduleFileIO.init();
		////ScheduleFileIO.saveSchedule(s, new Date());
		//System.out.println(s);
		//s = new Schedule();
		//System.out.println(s);
		////RandomAccessFile file = ScheduleFileIO.createFile();
		////readFilesTest(file);
		//s = ScheduleFileIO.loadSchedule();
		//System.out.println(s);
		//ScheduleFileIO.logToFile("lol what");
	//}
	
	//public static void readFilesTest(RandomAccessFile file){
		//try{
			//System.out.println(readVersion(file));
			////System.out.println(file.getFilePointer());
			//System.out.println(readSleepTimestamp(file));
			////System.out.println(file.getFilePointer());
			//System.out.println(readExitTimestamp(file));
			////System.out.println(file.getFilePointer());
			//System.out.println(readPreference(file));
			////System.out.println(file.getFilePointer());
			//System.out.println(readStats(file));
			//Schedule s = readBlocks(file, new Schedule());
			//ArrayList<Block> blocks = s.getAllBlocks();
			//for(Block b : blocks){
				//System.out.println(b);
			//}
		//}catch(Exception e){
			//ScheduleFileIO.logToFile(e.getMessage());
		//}
	//}
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define ScheduleFileIOUML
 * 
 * class ScheduleFileIO{
 * 	-filename : String {static}
 * 	-DEFAULT_FILENAME : String {static}
 * 	-LOG_FILENAME : String {static}
 * 	-VERSION : String {static}
 * 	-SLEEP_TIME_STAMP : char {static}
 * 	-PROGRAM_EXIT_TIME_STAMP : char {static}
 * 	-PREFERENCE : char {static}
 * 	-BLOCK : char {static}
 * 	-STATS : char {static}
 * 	-WAKE : char {static}
 * 	-offsetLengths : HashMap<String, Long> {static}
 * 	-BOOLEAN_BYTE_SIZE : int {static}
 * 	-BLOCKTYPE_BYTE_SIZE : int {static}
 * 	-BYTE_OFFSET : int {static}
 * 	-VERSION_NUMBER : String {static}
 * 	-MODE : String {static}
 * 	-ERROR_FATAL : String {static}
 * 	-ERROR_READING_FILE : String {static}
 * 	-ERROR_NOT_FOUND : String {static}
 * 	-ERROR_FORMAT : String {static}
 * 	-SAVE_VERSION : String {static}
 * 	-SAVE_SLEEP : String {static}
 * 	-SAVE_EXIT : String {static}
 * 	-SAVE_PREF : String {static}
 * 	-SAVE_STAT : String {static}
 * 	-SAVE_BLOCK : String {static}
 * 	-SAVE_WAKE : String {static}
 * 
 * 	+init() : void {static}
 * 	+doesFileExist() : boolean {static}
 * 	+getFileName() : String {static}
 * 	+logToFile(String) : void {static}
 * 	+loadSchedule() : Schedule {static}
 * 	+saveAllBlocks(ArrayList<Block>) : void {static}
 * 	+saveExit(Date) : void {static}
 * 	+savePreference(boolean) : void {static}
 * 	+saveSchedule(Schedule) : void {static}
 * 	+saveSleep(Date) : void {static}
 * 	+saveStats(long[]) : void {static}
 * 	+saveWakeUpTimes(GregorianCalendar[]) : void {static}
 * 	+setFileName(String) : void {static}
 * 	-initLengths() : void {static}
 * 	-calculateOffset(String) : long {static}
 * 	-closeFile(RandomAccessFile) : void {static}
 * 	-convertBytetoBlockType(byte) : BlockType {static}
 * 	-createFile() : RandomAccessFile {static}
 * 	-getOffset(String) : long {static}
 * 	-loadStats(RandomAccessFile) : void {static}
 * 	-loadWakeUpTimes(RandomAccessFile) : void {static}
 * 	-parseVersion(String) : String {static}
 * 	-readBlock(RandomAccessFile) : Block {static}
 * 	-readBlocks(RandomAccessFile, Schedule) : Schedule {static}
 * 	-readExitTimestamp(RandomAccessFile) : Date {static}
 * 	-readPreference(RandomAccessFile) : boolean {static}
 * 	-readSleepTimestamp(RandomAccessFile) : Date {static}
 * 	-readStats(RandomAccessFile) : long[] {static}
 * 	-readTimestamp(RandomAccessFile, char, String) : Date {static}
 * 	-readVersion(RandomAccessFile) : String {static}
 * 	-readWakeUpTime(RandomAccessFile) : GregorianCalendar {static}
 * 	-readWakeUpTimes(RandomAccessFile) : GregorianCalendar[] {static}
 * 	-saveBlocksToFile(RandomAccessFile, ArrayList<Block>) : void {static}
 * 	-saveBlockToFile(RandomAccessFile, Block) : void {static}
 * 	-savePreferenceToFile(RandomAcessFile, boolean) : void {static}
 * 	-saveStatsToFile(RandomAccessFile, long[]) : void {static}
 * 	-saveWakeUpTimesToFile(RandomAccessFile, GregorianCalendar[]) : void {static}
 * 	-saveTimestamp(Date, boolean) : void {static}
 * 	-saveTimestampToFile(RandomAccessFile, Date, boolean) : void {static}
 * 	-writeBlock(RandomAccessFile, Block) : void {static}
 * 	-writeBlocks(RandomAccessFile, ArrayList<Block>) : void {static}
 * 	-writeExitTimestamp(RandomAccessFile, Date) : void {static}
 * 	-writeNapPreference(RandomAccessFile, boolean) : void {static}
 * 	-writeSleepTimestamp(RandomAccessFile, Date) : void {static}
 * 	-writeStats(RandomAccessFile, long[]) : void {static}
 * 	-writeTimestamp(RandomAccessFile, char, Date) : void {static}
 * 	-writeWakeUpTime(RandomAccessFile, GregorianCalendar) : void {static}
 * 	-writeWakeUpTimes(RandomAccessFile, GregorianCalendar[]) : void {static}
 * 	-writeVersion(RandomAccessFile) : void {static}
 * }
 * 
 * @enduml
 */
