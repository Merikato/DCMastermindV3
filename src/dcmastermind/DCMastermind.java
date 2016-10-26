
package dcmastermind;

import dcmastermindclient.Client;
import java.io.IOException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 * DCMastermind class that represents the main class for the JavaFX GUI of the
 * game.
 * 
 * @author Rafia Anwar, Evan Glicakis, and Seaim Khan
 */
public class DCMastermind extends Application {
    
    /**
     * Starts the UI.
     * 
     * @param stage A JavaFX Stage object
     */
    @Override
    public void start(Stage stage) {
        //The IP address is the machine's IP
        String IPAddress = JOptionPane
                .showInputDialog("Enter the Server IP Address");
        try{     
            if(isValid(IPAddress)){
                
                Client client = new Client(IPAddress);      
        
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(this.getClass()
                        .getResource("FXMLDocument.fxml"));
                Parent root =  loader.load();
                FXMLDocumentController controller = loader.getController();
                controller.initializeAll(client);
        
                Scene scene = new Scene(root);
                stage.setTitle("MASTERMIND");
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show(); 
            }
            else {
                JOptionPane.showMessageDialog(null,
                        "Problem connecting to the server.\n\n Exiting game..");
                System.exit(0);
            }
          }
        catch(IOException ie){
                 System.exit(0);
          }
        catch(Exception e){
                System.exit(0);
          }
    
    }

    public static void main(String[] args) {
        launch(args);
        
    }
    
    /**
     * Checks if IP address is valid.
     * 
     * @param IPAddress An IP address
     * @return A boolean representing whether or not the IP address is valid.
     */
    private boolean isValid(String IPAddress) {
        if(IPAddress == null)
            return false;
        if(IPAddress.isEmpty())
            return false;
        if(IPAddress.matches("[0-9.]*"))
            return true;
        return false;
    }
    
}
