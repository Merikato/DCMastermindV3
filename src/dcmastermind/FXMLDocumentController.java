
package dcmastermind;

import dcmastermindclient.Client;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;

/**
 * FXMLDocumentController class to handle the client interaction with the game
 * UI.
 * 
 * @author Rafia Anwar, Evan Glicakis, and Seaim Khan
 */
public class FXMLDocumentController implements Initializable {

    private Client client;

    @FXML
    private Label label;
    
    @FXML
    private Label IPLabel;

    @FXML
    private Button btnGuess;
    @FXML
    private MenuItem btnOpenHelp;
    @FXML
    private Button btnCloseHelp;
    @FXML
    private MenuItem btnNewGame;
    @FXML
    private MenuItem btnCloseGame;

    @FXML
    private GridPane gameboard;

    @FXML
    private Circle btnRed;
    @FXML
    private Circle btnYellow;
    @FXML
    private Circle btnGreen;
    @FXML
    private Circle btnBlue;
    @FXML
    private Circle btnPurple;
    @FXML
    private Circle btnPink;
    @FXML
    private Circle btnWhite;
    
    @FXML
    private Circle btnLime;
    @FXML
    private Circle btnBrown;

    private boolean gameOver = false;
    private Circle selected;
    private int rowCount = 10;
    private int currentColor;
    private int[] guessArray = new int[4];
    private Circle[] row;
    private byte[] clues;
    private MMPacket mmp;
    private byte[] answer_set;
    private byte[] start_message;

    
    //Calls super constructor  
    public FXMLDocumentController() {
        super();
    }

    //Handles the Close click.
    @FXML
    private void handleCloseClick(ActionEvent event) throws IOException {
        System.out.println("Closing socket...");
        mmp.getSocket().close();
        Platform.exit();
    }
    
    //Handles the selection of colors for guessing.
    @FXML
    private void handleColorClick(MouseEvent event) {

        selected.setStroke(Color.BLACK);
        selected.setStrokeWidth(1);
        currentColor = -1;

        selected = (Circle) event.getSource();
        selected.setStroke(Color.ALICEBLUE);
        selected.setStrokeWidth(2);
        currentColor = Integer.parseInt(selected.getId());

    }

    //Handles the click to choose where to place colored guess.
    @FXML
    private void handlePositionClick(MouseEvent event) {
        Circle position = (Circle) event.getSource();
        position.setFill(Color.TRANSPARENT);

        position = (Circle) event.getSource();
        position.setFill(selected.getFill());
        //If the column index is 0, the get returns null.
        if (GridPane.getColumnIndex(position) == null) {
            guessArray[0] = currentColor;
        } else {
            guessArray[GridPane.getColumnIndex(position)] = currentColor;
        }
    }

    /**
     * 
     * @param event
     * @throws IOException 
     */
    @FXML
    private void handleGuessClick(ActionEvent event) throws IOException {
        
        
        // Checks if the guess is complete before sending to the server.
        for(int i : guessArray){
            if(i == 0){
                return;
            }
        }
        if(client.getIsTest()){
            
            answer_set = client.testAnswerSet(guessArray);
            System.out.println("Answer set: " + Arrays.toString(answer_set));
            client.setIstest(false);
        }
        // Disable event listener for the current row of colour to avoid user
        // changing them.
        for(Circle c : row){
            c.setOnMousePressed(null);
        }

        client.play_turn(guessArray);
        clues = client.getClues();
        
        //Checks if clues match the answer set.
        if(check_win(clues)){
            System.out.println("You won!");
            rowCount = 0;
            answer_set = mmp.readPacket();
            show_answer_set(answer_set);
            displayEndLabel(true);
            btnGuess.setDisable(true);
            return;
        }
        display_clues(clues);
        rowCount--;
        
        // Check if it's the last turn.
        System.out.println("turn: " + rowCount);
        if(rowCount == 0){
            System.out.println("last turn");
            byte b[] = mmp.readPacket();
            if(b[0] ==  0xFFFFFFFF){
                //END GAME
                System.out.println("last turn");
                answer_set = mmp.readPacket();
                System.out.println(Arrays.toString(answer_set));
                show_answer_set(answer_set);
                displayEndLabel(false);
            }
            
        }
        
        //Adds clickable position circles to next row
        if(rowCount != 0)
            addCirclesToRow();
        // Reset the guess array after sending it to the server.
        guessArray = new int[4];
        clues = new byte[4];
        
    }
    
    //Method to determine win if clues match.
    private boolean check_win(byte[] clues){
        boolean win = true;
        for(byte b : clues){
            if(b != 1)
                win = false;
        }
        return win;
    }

    //Handles the opening of the Help window.
   @FXML
    private void handleHelpClick(ActionEvent event) throws IOException {
        Stage stage;
        Scene scene;
        stage = new Stage();
        Parent parent = FXMLLoader.load(PopupController.class
                .getResource("AboutPage.fxml"));
        scene = new Scene(parent);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    //Handles a new game instance.
    @FXML
    private void handleNewGameClick(ActionEvent event) {
        if(label.isVisible()){
            try {
                //Let the server know we are ending the game.
                System.out.println("new game -- client");
                mmp.writePacket(new byte[]{0x22,0,0,0});
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
        resetGame();
        try {
            client.startGame();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        addCirclesToRow();
    }

    //Resets the board for a new game.
    private void resetGame() {
        ObservableList<Node> boardContent = gameboard.getChildren();
        
        int row = 10;
        int col = 0;
        Node n = gameboard.getChildren().get(0);
//        boardContent.clear();
//        gameboard.setGridLinesVisible(true);
//        boardContent.add(0,n);
        
        for (Node content : boardContent) {
            if (content instanceof Circle){
                row--;
                col--;
                System.out.println(content);
                Circle circle = (Circle) content;

                circle.setFill(Color.TRANSPARENT);
                circle.setStrokeWidth(1);
                circle.setVisible(false);
                circle.setDisable(false); 
            }else if (content instanceof HBox) {
                HBox hbox = (HBox) content;
                hbox.setVisible(false);
            }
            
        }

        gameboard.setDisable(false);
        btnGuess.setDisable(false);
        guessArray = new int[]{0,0,0,0};
        clues = new byte[4];
        label.setVisible(false);
        rowCount = 10;
        gameOver = false;
    }
    
    /**
     * Private helper method to show the answer set
     * @param answer_set 
     */
    private void show_answer_set(byte[] answer_set){
        row = new Circle[]{new Circle(13.5), new Circle(13.5), new Circle(13.5),
            new Circle(13.5)};
        int col = 0;
        for(int i = 0; i < answer_set.length; i++){
            row[i].setFill(int_to_color(answer_set[i]));
            row[i].setStrokeType(StrokeType.OUTSIDE);
            row[i].setStroke(Color.BLACK);
            row[i].setStrokeWidth(1);
            gameboard.add(row[i], col, 0);
            col++;               
        }
        
    }
    
    /**
     * Private helper method to turn ints into their color code value 
     * for displaying the answer set's circles.
     * 
     * @param i Integer value
     * @return A color representation of the integer.
     */
    private Color int_to_color(int i){
        switch(i){
            case 2: return Color.RED;
            case 3: return Color.YELLOW;
            case 4: return Color.GREEN;
            case 5: return Color.BLUE;
            case 6: return Color.web("#551a8b");
            case 7: return Color.PINK;
            case 8: return Color.web("#00fc00");
            case 9: return Color.BROWN;
        }
        return Color.TRANSPARENT;
    }
    
    /**
     * Adds the circles to the current row
     */
    private void addCirclesToRow() {
        row = new Circle[]{new Circle(13.5), new Circle(13.5), new Circle(13.5),
            new Circle(13.5)};
        int col = 0;
        for (Circle c : row) {
            c.setOnMousePressed(this::handlePositionClick);
            c.setFill(Color.TRANSPARENT);
            c.setStrokeType(StrokeType.OUTSIDE);
            c.setStroke(Color.BLACK);
            c.setStrokeWidth(1);
            
            gameboard.add(c, col, rowCount);
            col++;
        }
    }

    //Displays the verdict after a game is finished.
    private void displayEndLabel(boolean result) {
        label.setVisible(true);

        if (result) {
            label.setText("You Win!");
            label.setTextFill(Color.GREEN);
        } else {
            label.setText("You Lose!");
            label.setTextFill(Color.RED);
        }
    }
    
    //Displays the clues on the screen.
    private void display_clues(byte[] clues) {
        if (clues == null) {
            clues = new byte[4];
        }
        HBox hints = new HBox();
        for (Node n : gameboard.getChildren()) {
            if (n instanceof HBox && rowCount == GridPane.getRowIndex(n)) {
                hints = (HBox) n;
                hints.setVisible(true);
                break;
            }
        }

        ObservableList<Node> childs = hints.getChildren();
        System.out.println(childs.size());
        boolean is_placed;
        for (int i = 0; i < clues.length; i++) {
            is_placed = false;
            if (clues[i] >= 0 && clues[i] < 3) {
                for (int j = 0; j < 4 && !is_placed; j++) {

                    Ellipse ellipse = (Ellipse) childs.get(j);

                    if (clues[i] == 1 && !ellipse.isVisible()) {
                        //one is white
                        ellipse.setFill(Color.BLACK);
                        is_placed = true;
                        ellipse.setVisible(true);
                    } else if (clues[i] == 0 && !ellipse.isVisible()) {
                        ellipse.setFill(Color.WHITE);
                        ellipse.setVisible(true);
                        is_placed = true;
                    }

                }
            }
        }

    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {}
    
    //Initializes all the default values for game to function.
    public void initializeAll(Client client){
        try{
            this.client = client;
            if(client.getIsTest()){
                client.createSocket();
                this.mmp = client.getMmPacket();
                
                
            }else{
                client.createSocket();
                client.startGame();
                this.mmp = client.getMmPacket();
                System.out.println("Game Started");
                
            }
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        try {
            IPLabel.setText("Connected to client at: " + Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        selected = btnRed;
        currentColor = 2;
        addCirclesToRow();
    }
    
    //Sets the client
    public void setClient(Client client){     
        this.client = client;
    }
    
    public void closeSocket(){
        
        try {
            mmp.writePacket(new byte[]{0x25,0,0,0});
            this.mmp.getSocket().close();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
