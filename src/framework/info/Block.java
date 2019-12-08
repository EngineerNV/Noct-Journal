package framework.info;

import java.awt.Color;

import java.util.Date;

/**
 * Class to represent a block on the schedule.
 * We'll keep track of the time using Date class
 *  the Date (starting time) and length is stored here
 * Blocks will be ordered by their date.
 * 
 * Blocks contain:
 * 	Date - the starting Date of this block
 * 	length - the length of this block (in long) (greater than 0)
 * 	type - the type of this Block (in BlockType)
 * 	reoccuring - boolean, when true, this block reoccurs.
 * 	color - the Color of this block. Mainly for graphics
 * 	name - the name of this block (in String)
 * 
 * The most Basic Block takes a Date and length.
 * Defaults:
 * 	type - BlockType.NAP
 * 	reoccuring - false
 * 	color - RGB(0, 200, 255)
 * 	name - "Nap"
 * 
 * @author Andre Allan Ponce
 *
 */
public class Block{
	
	// Basic Constructor
	// Requires starting Date and length
	public Block(Date startDate, long length){
		this(startDate, length, DEFAULT_BLOCKTYPE);
	}
	
	// Constructor using:
	// starting Date, length, and BlockType
	public Block(Date startDate, long length, BlockType type){
		this(startDate, length, type, false);
	}
	
	// Constructor using:
	// starting Date, lenght, BlockType, boolean for reoccuring
	public Block(Date startDate, long length, BlockType type, boolean reocurring){
		this(startDate, length, type, reocurring, new Color(DEFAULT_COLOR_RED, DEFAULT_COLOR_GREEN, DEFAULT_COLOR_BLUE), DEFAULT_NAME);
	}
	
	// Full constructor:
	// all params can be set
	public Block(Date startDate, long length, BlockType type, boolean reoccuring, Color color, String name){
		setDate(startDate);
		setLength(length);
		setType(type);
		setReocurring(reoccuring);
		setColor(color);
		setName(name);
	}
	
	// Copy constructor
	public Block(Block b){
		this(b.getDate(), b.getLength(), b.getType(), b.isReocurring(), b.getColor(), b.getName());
	}
	
	// because Object.equals is used somewhere
	@Override
	public boolean equals(Object b){
		if(b instanceof Block){
			Block block = (Block) b;
			return block.getDate().equals(this.getDate())
				&& block.getLength() == this.getLength()
				&& block.getType() == this.getType()
				&& block.isReocurring() == this.isReocurring()
				&& block.getColor().equals(this.getColor())
				&& block.getName().equals(this.getName());
		}
		return false;
	}
	
	public Color getColor(){
		return color;
	}

	public Date getDate(){
		return date;
	}

	public long getLength(){
		return length;
	}
	
	public String getName(){
		return name;
	}

	public BlockType getType(){
		return type;
	}

	// Methods for comparing two blocks
	// blocks are ordered by their date
	public boolean isAfter(Block b){
		return this.getDate().after(b.getDate());
	}

	public boolean isBefore(Block b){
		return this.getDate().before(b.getDate());
	}

	public boolean isReocurring(){
		return reocurring;
	}
	
	/**
	 * Helps us check for overlapping blocks
	 * 
	 * @param b - block to check if overlaps with this 
	 * @return true if this block overlaps with b, false if not
	 */
	public boolean overlapsWith(Block b){
		if(this.isAfter(b)){
			return this.getDate().getTime() < b.getDate().getTime() + b.getLength();
		}else if(this.isBefore(b)){
			return this.getDate().getTime() + this.getLength() > b.getDate().getTime();
		}
		return true;
	}
	
	public void setColor(Color color){
		this.color = color;
	}

	public void setDate(Date date){
		this.date = date;
	}

	public void setLength(long length){
		if(length >= 0L){
			this.length = length;
		}else{
			this.length = 0L;
		}
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setReocurring(boolean r){
		this.reocurring = r;
	}

	public void setType(BlockType type){
		this.type = type;
	}
	
	@Override
	public String toString(){
		return name+";"+color.getRGB()+";"+"Block-"+type+";From:"+date+";To:"+(new Date(date.getTime()+length));
	}
	
	private long length;
	private Date date;
	private BlockType type;
	private boolean reocurring;
	private Color color;
	private String name;
	
	private static final int DEFAULT_COLOR_RED = 0;
	private static final int DEFAULT_COLOR_GREEN = 200;
	private static final int DEFAULT_COLOR_BLUE = 255;
	
	private static final String DEFAULT_NAME = "Nap";
	
	private static final BlockType DEFAULT_BLOCKTYPE = BlockType.NAP;
	
}

/* PLANTUML Code
 * 
 * @startuml
 * !define BlockUML
 * 
 * class Block{
 * 	-length : long
 * 	-date : Date
 * 	-type : BlockType
 * 	-reoccuring : boolean
 * 	-color : Color
 * 	-name : String
 * 	-DEFAULT_COLOR_RED : int {static}
 * 	-DEFAULT_COLOR_GREEN : int {static}
 * 	-DEFAULT_COLOR_BLUE : int {static}
 * 	-DEFAULT_NAME : String {static}
 * 	-DEFAULT_BLOCKTYPE : BlockType {static}
 * 
 * 	+Block(Date, long)
 * 	+Block(Date, long, BlockType)
 * 	+Block(Date, long, BlockType, boolean)
 * 	+Block(Date, long, BlockType, boolean, Color, String)
 * 	+Block(Block)
 * 	+equals(Object) : boolean
 * 	+getColor() : Color
 * 	+getDate() : Date
 * 	+getLength() : long
 * 	+getName() : String
 * 	+getType() : BlockType
 * 	+isAfter(Block) : boolean
 * 	+isBefore(Block) : boolean
 * 	+isReocurring() : boolean
 * 	+overlapsWith(Block) : boolean
 * 	+setColor(Color) : void
 * 	+setDate(Date) : void
 * 	+setLength(long) : void
 * 	+setName(String) : void
 * 	+setReocurring(boolean) : void
 * 	+setType(BlockType) : void
 * 	+toString()	: String
 * }
 * 
 * @enduml
 */
