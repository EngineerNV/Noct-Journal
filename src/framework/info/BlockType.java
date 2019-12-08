package framework.info;

/**
 * Helps keep the different blocks different.
 * 
 * @author Andre Allan Ponce
 *
 */
public enum BlockType {

	CLASS, EVENT, FREE, NAP, SLEEP, WORK;
	
	// for saving
	public byte toByte(){
		switch(this){
		case CLASS:	return ID_CLASS;
		case EVENT:	return ID_EVENT;
		case FREE:	return ID_FREE;
		case NAP:	return ID_NAP;
		case SLEEP:	return ID_SLEEP;
		case WORK:	return ID_WORK;
		default:	return ID_DEBUG;
		}
	}
	
	public String toString(){
		switch(this){
		case CLASS:	return "class";
		case EVENT:	return "event";
		case FREE:	return "free";
		case NAP:	return "nap";
		case SLEEP:	return "sleep";
		case WORK:	return "work";
		default:	return "no"; // we should never get here.
		}
	}
	
	public static final byte ID_CLASS = 1;
	public static final byte ID_EVENT = 2;
	public static final byte ID_FREE = 3;
	public static final byte ID_NAP = 4;
	public static final byte ID_SLEEP = 5;
	public static final byte ID_WORK = 6;
	public static final byte ID_DEBUG = 0;
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define BlockTypeUML
 * 
 * enum BlockType{
 * 	CLASS
 * 	EVENT
 * 	FREE
 * 	NAP
 * 	SLEEP
 * 	WORK
 * 
 * 	{static} -ID_CLASS : byte
 * 	{static} -ID_EVENT : byte
 * 	{static} -ID_FREE : byte
 * 	{static} -ID_NAP : byte
 * 	{static} -ID_SLEEP : byte
 * 	{static} -ID_WORK : byte
 * 	{static} -ID_DEBUG : byte
 * 
 * 	+toByte() : byte
 * 	+toString() : String
 * }
 * @enduml
 * 
 */
