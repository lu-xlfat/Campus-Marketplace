
package campusmarketplace;

import campusmarketplace.ui.LoginFrame;
import javax.swing.SwingUtilities;

public class CampusMarketplace {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
