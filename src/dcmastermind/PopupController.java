
package dcmastermind;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
/**
 * PopupController class for the Help window.
 * 
 * @author Rafia Anwar, Evan Glicakis, and Seaim Khan
 */
public class PopupController implements Initializable{
    
    @FXML
    private Button btnCloseHelp;
    
    /**
     * Handles the closing of the Help window.
     * 
     * @param event The ActionEvent
     */
    @FXML
    private void handleCloseClick(ActionEvent event) {        
        Stage stage = (Stage) btnCloseHelp.getScene().getWindow();
        stage.close();      
    }
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {}    
}
