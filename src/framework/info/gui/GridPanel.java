package framework.info.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JPanel;

import framework.info.BlockType;

@SuppressWarnings("serial")
public class GridPanel extends JPanel {
	static ArrayList<GraphicBlock> blockList = new ArrayList<GraphicBlock>(); //holds all visual blocks
	
	//draws basic grid
	@Override
	public void paintComponent(Graphics g){
		g.setColor(Color.WHITE);
		g.fillRect(40, 0, this.getWidth(), this.getHeight());
		
		//horizontal lines indicating times
		g.setColor(new Color(230, 230, 230));
		for (int y = 105; y < this.getHeight(); y += 48)
			g.drawLine(41, y, this.getWidth()-2, y);
		
		//vertical lines separating days
		g.setColor(Color.GRAY);
		for (int x = 40; x < this.getWidth(); x += 125)
			g.drawLine(x, 0, x, this.getHeight());

		//box outline
		g.drawRect(40, 0, this.getWidth()-41, this.getHeight()-1);
		g.drawLine(40, 60, this.getWidth(), 60);
		
		writeDays(g);
		
		g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		String[] times = {"12AM", "2 AM", "4 AM", "6 AM", "8 AM", "10AM", "12PM", "2 PM", "4 PM", "6 PM",
				"8 PM", "10PM", "12AM"};
		
		for (int y = 60, a = 0; a < times.length; y += 96, a++){
			if (a == 5 || a == 6 || a == 11 || a == 12)
				g.drawString(times[a], -3, y);
			else
				g.drawString(times[a], 0, y+3);
		}
	}
	
	//writes name of days on top
	public void writeDays(Graphics g){
		Font font1 = new Font(Font.SANS_SERIF, Font.PLAIN, 30);
		g.setColor(Color.BLACK);
		g.setFont(font1);
		
		g.drawString("Monday", 48, 40);
		g.drawString("Tuesday", 170, 40);
		
		Font font2 = new Font(Font.SANS_SERIF, Font.PLAIN, 23);
		g.setFont(font2);
		g.drawString("Wednesday", 293, 40);
		
		Font font3 = new Font(Font.SANS_SERIF, Font.PLAIN, 28);
		g.setFont(font3);
		g.drawString("Thursday", 420, 40);
		
		Font font4 = new Font(Font.SANS_SERIF, Font.PLAIN, 36);
		g.setFont(font4);
		g.drawString("Friday", 554, 40);
		
		g.setFont(font3);
		g.drawString("Saturday", 672, 40);
		
		g.setFont(font1);
		g.drawString("Sunday", 800, 40);
		
		//underline the current day
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		int[] days = {GregorianCalendar.MONDAY, GregorianCalendar.TUESDAY, GregorianCalendar.WEDNESDAY,
				GregorianCalendar.THURSDAY, GregorianCalendar.FRIDAY, GregorianCalendar.SATURDAY,
				GregorianCalendar.SUNDAY};
		
		g.setColor(Color.WHITE);
		for (int i = 0; i < 7; i++){
			if (calendar.get(GregorianCalendar.DAY_OF_WEEK) == days[i])
				g.setColor(Color.RED);
			
			g.drawLine(45+(125*i), 48, (45+(125*i))+115, 48);
			g.setColor(Color.WHITE);
		}
	}
	
	public void addBlock(GraphicBlock block){
		blockList.add(block); //add to list
		this.drawBlock(block); //draw the block
	}
	
	public GraphicBlock getBlock(GraphicBlock g){
		if (blockList.indexOf(g) == -1)
			return null;
		else
			return blockList.get(blockList.indexOf(g));
	}
	
	public static void removeBlock(GraphicBlock block){
		blockList.remove(block);
	}
	
	public void removeAllBlocks(){
		blockList.clear(); //clear list of blocks
		this.removeAll(); //remove blocks from this container
		this.update(this.getGraphics());
	}
	
	public void removeAllSleepBlocks(){ //only remove sleep blocks
		for (int i = 0; i < blockList.size(); i++){
			GraphicBlock b = blockList.get(i);
			if (b.getBlock().getType() == BlockType.SLEEP){
				removeBlock(b);
				this.remove(b);
			}
		}
		this.update(this.getGraphics());
	}
	
	//draws all the existing blocks in the list
	public void drawBlocks(){
		for (GraphicBlock block : blockList)
			drawBlock(block);
	}
	
	//adds block visually to grid
	public void drawBlock(GraphicBlock block){
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(block.getBlock().getDate());
		
		block.setBounds(block.getX(), block.getY(), 124, block.getBlockLength());
		
		this.add(block);
		this.update(this.getGraphics());
	}
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define GridPanelUML
 * 
 * class GridPanel{
 * 	-blockList : ArrayList<GraphicBlock> {static}
 * 
+paintComponent(Graphics) : void
+writeDays(Graphics) : void
+addBlock(GraphicBlock) : void
+getBlock(GraphicBlock) : GraphicBlock
+removeBlock(GraphicBlock) : void {static}
+removeAllBlocks() : void
+removeAllSleepBlocks() : void
+drawBlocks() : void
+drawBlock(GraphicBlock) : void
 * }
 * @enduml
 */
