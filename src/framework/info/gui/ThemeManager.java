package framework.info.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Centralized look-and-feel + shared component styling.
 */
public final class ThemeManager {
    static final Color APP_BACKGROUND = new Color(23, 26, 34);
    static final Color PANEL_BACKGROUND = new Color(32, 37, 48);
    static final Color GRID_BACKGROUND = new Color(244, 246, 250);

    private ThemeManager() {
    }

    public static void applyGlobalTheme() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // fallback is default Swing LAF
        }

        UIManager.put("Button.arc", 18);
        UIManager.put("Component.arc", 16);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ScrollBar.width", 11);
        UIManager.put("Component.focusWidth", 1);
    }

    public static void styleSidebarButton(JButton button) {
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setFocusPainted(false);
    }

    public static void styleCard(JComponent component) {
        component.setBackground(PANEL_BACKGROUND);
        component.setForeground(Color.WHITE);
    }

    public static void refreshTree(JComponent component) {
        SwingUtilities.updateComponentTreeUI(component);
    }
}
