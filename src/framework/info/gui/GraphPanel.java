package framework.info.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

//function that would have drawn the graph
@SuppressWarnings("serial")
public class GraphPanel extends JPanel {
	//draws graph outline
		@Override
		public void paintComponent(Graphics g){ 
			this.setLayout(null);
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			
			g.setColor(Color.BLACK);
			
			//horizontal lines indicating hours
			g.setColor(new Color(230, 230, 230));
			for (int y = 105; y < this.getHeight(); y += 48)
				g.drawLine(41, y, this.getWidth()-2, y);
			
			
			writeGraphDays(g);
			
			g.setColor(Color.BLACK);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
			String[] hoursOfSleep = {"10", "9", "8", "7", "6", "5", "4", "2"};
			
			for (int y = 80, a = 0; a < hoursOfSleep.length; y += 96, a++){
				if (a == 5 || a == 6 || a == 11 || a == 12)
					g.drawString(hoursOfSleep[a], -3, y);
				else
					g.drawString(hoursOfSleep[a], 0, y+3);
			}
		}

		
public void writeGraphDays(Graphics g){
	Font font1 = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
	g.setColor(Color.BLACK);
	g.setFont(font1);
	
	g.drawString("Monday", 48, 750);
	g.drawString("Tuesday", 170, 1000);
	
	g.setFont(font1);
	g.drawString("Wednesday", 293, 750);
	
	g.setFont(font1);
	g.drawString("Thursday", 420, 1000);
	
	g.setFont(font1);
	g.drawString("Friday", 554, 1000);
	
	g.setFont(font1);
	g.drawString("Saturday", 672, 1000);
	
	g.setFont(font1);
	g.drawString("Sunday", 800, 1000);
	}
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define GraphPanelUML
 * 
 * class GraphPanel{
 * 	+paintComponent(Graphics) : void
 * }

 * @enduml
 */
