package framework.info.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.GregorianCalendar;

import javax.swing.*;

import framework.info.Block;

//Graphical block consists of label for name and background color of panel
@SuppressWarnings("serial")
public class GraphicBlock extends JPanel {
	Block info; //extra info from Block: start/end time, re-occurring (locked/unlocked) and BlockType
	int dayOfEvent; //corresponds to day of week event is assigned to, (0 to 6) is (Monday to Sunday)

	//used for putting info into blockEditMenu
	Integer hour = 0;
	Integer endHour = 0;
	Integer minute = 0;
	Integer endMinute = 0;
	String ampm = "AM";
	String endampm = "AM";
	
	JButton trash;
	JLabel nameLabel;
	
	//used for drawing block
	int x = 0;
	int y = 0;
	int blockLength = 0;
	
	GraphicBlock(Block b, int d){
		setEventDay(d);
		setBlock(b);
		
		trash = new JButton("X");
		trash.setBounds(0, 0, 45, 20);
		trash.setToolTipText("Delete this block");
		GraphicBlock x = this;
		this.setLayout(null);
		
		//remove block from schedule and visually
		trash.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				GridPanel g = (GridPanel) trash.getParent().getParent();
				GridPanel.removeBlock(x); //remove from list
				g.remove(trash.getParent()); //remove GraphicBlock, the JPanel
				
				GraphicSchedule.getSchedule().removeEvent(x.getBlock().getDate()); //remove block from schedule
				GraphicSchedule.getSchedule().applyAlgorithm(); //apply the algorithm
				GraphicSchedule.getSchedule().saveBlocks(); //save changes
				g.update(g.getGraphics());
			}
		});
		this.add(trash);
		
		nameLabel = new JLabel(getName());
		nameLabel.setBounds(0, 15, 120, 25);
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(nameLabel);
		
		this.setBackground(getBackgroundColor());
		this.setToolTipText(getName());
		
		GregorianCalendar calendar1 = new GregorianCalendar();
		calendar1.setTime(this.getBlock().getDate());
		calendar1.set(GregorianCalendar.SECOND, 0);
		
		hour = calendar1.get(GregorianCalendar.HOUR_OF_DAY);
		minute = calendar1.get(GregorianCalendar.MINUTE);
		
		if (hour > 12 || (hour == 12 && minute >= 0))
			ampm = "PM";
		
		setX(); //sets x coordinate for drawing
		setY(); //sets y coordinate for drawing
		setBlockLength(); //sets length for drawing
		
		GregorianCalendar calendar2 = new GregorianCalendar();
		calendar2.set(GregorianCalendar.SECOND, 0);
		
		int blockMinutes = 0; //should only have values 0, 15, 30 or 45
		
		if (blockLength % 48 != 0){ //if time has minutes other than 0
			blockMinutes = (((blockLength % 48) / 12) * 15);
		}
		
		calendar2.set(GregorianCalendar.HOUR_OF_DAY,
				calendar1.get(GregorianCalendar.HOUR_OF_DAY) + (blockLength/48));
		calendar2.set(GregorianCalendar.MINUTE, blockMinutes);
		
		endHour = calendar2.get(GregorianCalendar.HOUR_OF_DAY);
		endMinute = calendar2.get(GregorianCalendar.MINUTE);
		
		if (endHour > 12 || (endHour == 12 && endMinute >= 0))
			endampm = "PM";
	}
	
	GraphicBlock(GraphicBlock g){
		this(g.getBlock(), g.getEventDay());
	}
	
	@Override
	public void paintComponent(Graphics g){ //set the color of the block
		g.setColor(getBackgroundColor());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}
	
	public String getName(){
		return getBlock().getName();
	}
	
	public Color getBackgroundColor(){
		return getBlock().getColor();
	}
	
	public int getEventDay(){
		return dayOfEvent;
	}
	
	public void setEventDay(int dayOfEvent){
		this.dayOfEvent = dayOfEvent;
	}
	
	public Block getBlock(){
		return info;
	}
	
	public void setBlock(Block info){
		this.info = info;
	}

	public Integer getHour(){
		return hour%12;
	}
	
	public Integer getEndHour(){
		return endHour%12;
	}
	
	public Integer getMinute(){
		return minute;
	}
	
	public Integer getEndMinute(){
		return endMinute + minute;
	}
	
	public String getAMPM(){
		return ampm;
	}
	
	public String getEndAMPM(){
		return endampm;
	}
	
	public void setY(){
		y = 58 + (48 * hour) + ((minute/15)*12);
	}
	
	@Override
	public int getY(){
		return y;
	}
	
	@Override
	public int getX(){
		return x;
	}
	
	public void setX(){
		x = 41 + (125 * getEventDay());
	}
	
	public void setBlockLength(){
		blockLength = (int) (((double) getBlock().getLength()/3600000)*48);
	}
	
	public int getBlockLength(){
		return blockLength;
	}
	
	@Override
	public String toString(){
		return "GraphicBlock: " + getName() + ", color: " + getBackgroundColor().toString()
				+ dayOfEvent + "\n" + info.toString();
	}
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define GraphicBlockUML
 * 
 * class GraphicBlock{
 * 	-info : Block
-dayOfEvent : int
-hour : Integer
-endHour : Integer
-minute : Integer
-endMinute : Integer
-ampm : String
-endampm : String
-trash : JButton
-nameLabel : JLabel
-x : int
-y : int
-blockLength : int
* 
+paintComponent(Graphics) : void
+getName() : String
+getEventDay() : int
+setEventDay(int) : void
+getBlock() : Block
+setBlock(Block) : void
+getHour() : Integer
+getEndHour() : Integer
+getMinute() : Integer
+getEndMinute() : Integer
+getAMPM() : String
+getEndAMPM() : String
+setX() : void
+setY() : void
+getX() : int
+getY() : int
+setBlockLength() : void
+getBlockLength() : int
+toString() : String
 * }
 * @enduml
 * 
 */
