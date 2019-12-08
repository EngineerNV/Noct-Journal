package framework.info.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class StartPanel extends JPanel {
	@Override
	public void paintComponent(Graphics g){ //draw info on background of panel
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		g.setColor(Color.BLACK);
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 40);
		g.setFont(font);
		g.drawString("NOC-JOURNAL", 45, 45);
		
		Font font2 = new Font(Font.SANS_SERIF, Font.PLAIN, 22);
		g.setFont(font2);
		g.drawString("by", 178, 80);
		g.drawString("Victor Z.", 148, 105);
		g.drawString("Nick V.", 154, 130);
		g.drawString("Lonny R.", 146, 155);
		g.drawString("Andre P.", 143, 180);
	}
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define StartPanelUML
 * 
 * class StartPanel{
 * 	+paintComponent(Graphics) : void
 * }
 * @enduml
 */
