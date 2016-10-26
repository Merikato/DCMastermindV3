/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dcmastermindclient;

import dcmastermind.DCMastermind;
import dcmastermind.MMPacket;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import javafx.application.Application;
import javax.swing.JOptionPane;

/**
 * MMClient class that interacts with the UI and sends/receives packets on its
 * behalf.
 * 
 * @author Rafia Anwar, Evan Glicakis, and Seaim Khan
 */
public class MMClient {
    //Ask user to input IP address
    private String ipAddress="192.168.2.232";
    private int port = 50000;
    private  MMPacket mmPacket;
    private byte[] clues ;
    private byte[] serverAnswers;
    private Socket socket;
    private boolean playAgain;
    private byte[] guess;

    //Ask user to start game or quit
    public static void main(String[] args) throws IOException
    {
        MMClient c = new MMClient();
        c.createSocket();
        c.continueGame();
    }
    
    /**
     * Default no-parameter constructor.
     */
    public MMClient(){}
    
    /**
     * Handles messages for continuous gameplay until client stops.
     * 
     * @throws IOException if unable to read/write packets.
     */
    public void continueGame() throws IOException{
        playAgain=playAgainGame();

        //While loop to keep playing until client quits
        while (playAgain == true){
            startGame(); //Sends server the start messaage
            
            //Check server message
            if(mmPacket.readPacket()[0] == 0x0000000A){
                System.out.println("Ready to draw board");
                displayGameBoard();
            }

        playAgain = playAgainGame();
        }
    }
    
    /**
     * Starts the game for the client.
     * 
     * @throws IOException if unable to read/write packets.
     */
    private void startGame() throws IOException{
        System.out.println("Send message to server");
        byte[] startMessage = new byte[]{0x00000011, 0,0,0};
        System.out.println("Sending packet: " + Arrays.toString(startMessage));
        mmPacket.writePacket(startMessage);
    }
    
    /**
     * Creates a socket and implements the MMPacket.
     * 
     * @throws IOException if unable to create the socket.
     */
    public void createSocket() throws IOException{
        //Create socket that is connected to server on specified port
        socket = new Socket(ipAddress, port);
        System.out.println("Connected to server... Sending echo string");

        mmPacket = new MMPacket(socket);
        System.out.println("CREATED PACKET");
    }
    
    /**
     * Handles "play again" screen after a game ends.
     * 
     * @return A boolean representing playing again or quitting.
     * @throws IOException if unable to close socket.
     */
    public boolean playAgainGame() throws IOException{
        String userResponse = JOptionPane.showInputDialog("Enter P for play "
                + "and Q for quit:");
        if(userResponse.equalsIgnoreCase("p"))
            playAgain = true;
        else {
            socket.close();
            System.exit(0);
            playAgain =  false;
        }
        return playAgain;
    }

    /**
     * Launches the game UI.
     */
    private void displayGameBoard(){
        //Display board
        Application.launch(DCMastermind.class);
    }


    /**
     * Handles messages for single game instance of Mastermind.
     * 
     * @throws IOException if unable to read/write packets.
     */
    public void playGame() throws IOException{

        byte[] userAnswers = guess;
        if(userAnswers.length == 4)
        {
            for(int i=0; i < 10;i++){
                //Sends the user answer to server
                mmPacket.writePacket(userAnswers);
                System.out.println("Sent guess " 
                        + Arrays.toString(userAnswers));
                clues = mmPacket.readPacket();

                System.out.println("Clues-client: " + Arrays.toString(clues));
                if(i == 9){
                    System.out.println("End...");

                    byte[] b = mmPacket.readPacket();
                    System.out.println("End?" + b[0]);

                    if(b[0] == 0xFFFFFFFF){
                        endGame();
                        continueGame();
                    }
                }
               /*
                * Display clues on board
                * If user won, display a message, and call play again.
                * Else, continue in the loop and get the user array.
                */
            }

            serverAnswers = mmPacket.readPacket();
            //Display a message
        }
    }
    
    /**
     * Fills in a user answer set.
     * 
     * @return A user answer set.
     */
    public byte[] fillInUserAnswerArray(){
        byte[] userAnswers = new byte[4];
        return userAnswers;
    }

    /**
     * Sets a byte array of guesses.
     * 
     * @param guess A guess set.
     */
    public void setGuess(int[] guess) {
        byte[] bytes = new byte[4];
        for(int i : guess)
            bytes[i] = convertIntToByte(i);
        
        this.guess = bytes;
    }

    /**
     * Converts int representing colour to a byte.
     * 
     * @param i A colour integer
     * @return A colour byte
     */
    private byte convertIntToByte(int i){
        switch(i){
            case 2:
                return 0x00000002;
            case 3:
                return 0x00000003;
            case 4:
                return 0x00000004;
            case 5:
                return 0x00000005;
            case 6:
                return 0x00000006;
            case 7:
                return 0x00000007;
            case 8:
                return 0x00000008;
            case 9:
                return 0x00000009;
            default:
                return 0x1;
        }
    }

    /**
     * Ends the game for the client.
     * 
     * @throws IOException if unable to read packets.
     */
    private void endGame()throws IOException {
        System.out.println("End Game");
        byte[] answerSet = mmPacket.readPacket();
        System.out.println("Answer set: " + Arrays.toString(answerSet));
        System.out.println("");
    }
}
