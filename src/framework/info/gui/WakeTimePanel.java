package framework.info.gui;

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class WakeTimePanel extends JPanel {
	@Override
	public void paintComponent(Graphics g){ //draw info on background of panel
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
		g.setFont(font);
		g.drawString("Please enter the time you would like to wake up for each day", 10, 30);
		g.drawString("of the week. You can always edit this later by going to the", 10, 55);
		g.drawString("options menu. All times are for the morning. (AM)", 10, 80);
		
		g.drawString(":", 120, 140);
		g.drawString(":", 120, 225);
		g.drawString(":", 120, 310);
		g.drawString(":", 120, 395);
		
		g.drawString(":", 355, 140);
		g.drawString(":", 355, 225);
		g.drawString(":", 355, 310);

		g.drawString("Are you OK", 500, 150);
		g.drawString("with naps?", 500, 180);
	}
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define WakeTimePanelUML
 * 
 * class WakeTimePanel{
 * 	+paintComponent(Graphics) : void
 * }
 * @enduml
 */
