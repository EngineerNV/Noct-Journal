package framework.info.gui;

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel {
	@Override
	public void paintComponent(Graphics g){ //draws info on background of panel
		Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 25);
		g.setFont(font);
		g.drawString("Sleep is important. Without it the average", 5, 32);
		g.drawString("human cannot function properly. Balancing", 5, 62);
		g.drawString("sleep along with all of your other priorities ", 5, 92);
		g.drawString("can be challenging. With Noc-Journal we", 5, 122);
		g.drawString("seek to eliminate this problem with an", 5, 152);
		g.drawString("easy to use application!", 5, 182);
		g.drawString("Made By:", 195, 222);
		g.drawString("Victor Z., Nick V., Lonny R., Andre P.", 35, 252);
	}
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define AboutPanelUML
 * 
 * class AboutPanel{
 * 	+paintComponent(Graphics) : void
 * }
 * @enduml
 */
