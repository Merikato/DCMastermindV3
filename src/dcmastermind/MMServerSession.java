
package dcmastermind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * MMServerSession class that handles game session logic.
 * 
 * @author Rafia Anwar, Evan Glicakis, and Seaim Khan
 */
public class MMServerSession {

    private boolean playAgain = true;
    private boolean gameOver = false;
    private int[] colours;
    MMPacket mmPacket;
    
    /**
     * Constructor initializes an integer array for colours and an MMPacket
     * object.
     * 
     * @param mmp An MMPacket object
     */
    public MMServerSession(MMPacket mmp) {
        
        colours = new int[]{2,3,4,5,6,7,8,9};
        mmPacket = mmp;
    }
    
    /**
     * Determines whether to continue or end play and writes packets
     * accordingly.
     * 
     * @return A boolean value representing continuing or ending play.
     * @throws IOException if unable to read/write packets.
     */
    private boolean setPlayAgainValue() throws IOException{
        System.out.println("server reading");
        byte[] packet= mmPacket.readPacket();
        System.out.println("Packet: " + Arrays.toString(packet));
        boolean play = false;
       
        //Continue play
        if(packet[0] == 0x00000011){
            play = true;
            gameOver = false;
            System.out.println("Sent start message");
            mmPacket.writePacket(new byte[]{0x0000000A,0,0,0});
            return play;
        }
        //End play
        else {
            play = false;
            boolean validColour=false;
            //check if incoming array is array of colors
            for(int i=0;i<4;i++){
                if(setColour(packet[i]) == -1)
                    validColour=false;
                else 
                    validColour=true;
            }
            byte[] testAnswerSet=new byte[4];
            for(int i=0;i<4;i++)
                testAnswerSet[i]=packet[i];
            
            mmPacket.writePacket(testAnswerSet);  
       }     
       return play;
    }
    
    /**
     * Contains the game logic for the server and reading and writing of 
     * packets accordingly.
     * 
     * @throws IOException if unable to read/write packets concerning game.
     */
    public void action() throws IOException{
        int counter=0;
        
        while(playAgain && !mmPacket.getSocket().isClosed()){
            setPlayAgainValue();
            int[] answerSet = createAnswerSet(); //Generate answer set
            while(!gameOver & !mmPacket.getSocket().isClosed()){
                
                // Read packet from user.
                byte[] colorMessage = mmPacket.readPacket();
                System.out.println("received packet: "
                        + Arrays.toString(colorMessage));
                //Check if message is color    

                int colorRange = setColour(colorMessage[0]);
                if(colorRange != -1){
                    //Get user answer
                    int[] clientGuesses=new int[4];
                    for(int i=0;i<4;i++)
                        clientGuesses[i]=setColour(colorMessage[i]);
                   
                    //Compare the answers
                    //Reply with clues
                    int[] clues= clueGenerator(clientGuesses,answerSet);
                  
                    System.out.println("User guesses: "
                            +Arrays.toString(clientGuesses));
                    System.out.println("Answer set: "
                            +Arrays.toString(answerSet));
                    
                    //Send it to the client
                    byte[] replyClientClues=convertIntCluesArrayToBytes(clues);
                    mmPacket.writePacket(replyClientClues);
                    System.out.println("Clues: "+Arrays.toString(clues));
                    System.out.println("Clues in bytes: " 
                            + Arrays.toString(replyClientClues));
                   
                    counter++;
                    System.out.println("Turn: " + counter);
                   
                    //If 10th submission then 0xFFFFFFFF
                    if(counter == 10){
                        System.out.println("Sent game over");
                        gameOver = true;
                        mmPacket.writePacket(new byte[]{0xFFFFFFFF, 0 , 0 ,0 });
                        byte[] resp = new byte[4];
                        for(int i = 0 ; i < answerSet.length; i++){
                            resp[i] = convertIntToByte(answerSet[i]);
                        }
                        //Send answer set
                        System.out.println("Sent answer set to client");
                        mmPacket.writePacket(resp);
                    }
  
                }
                
            }
        }        
    }
    
    /**
     * Converts the int array of clues to a byte array.
     * 
     * @param clues The int array of clues.
     * @return The byte array of clues.
     */
    private byte[] convertIntCluesArrayToBytes(int[] clues){
        byte[] byteClues = new byte[clues.length];
        for (int i=0;i < clues.length;i++)
           byteClues[i]= convertIntCluesToBytes(clues[i]);
        
        return byteClues;
    }
    
    /**
     * Converts a single clue to a byte.
     * 
     * @param clue A clue
     * @return A byte representation of a clue.
     */
    private byte convertIntCluesToBytes(int clue){
        switch(clue){
            case 0: 
                return 0x00000000; // Inplace
            case 1: 
                return 0x00000001; // Outplace
            default: 
                return 20;
        }
    }

    /**
     * Generates clues depending on the client's guesses.
     * 
     * @param clientGuesses The set of client's guesses
     * @param answerSet The actual set of answers
     * @return An array of clues depending on the accuracy of guesses.
     */
    private int[] clueGenerator(int[] clientGuesses,int[] answerSet){
        
        List<Integer> clueList = new ArrayList<>();
        int[] cloneAnswerSet = new int[4];
    
        //Clones guesses to a new answer set.
        for(int i=0;i <4;i++)
            cloneAnswerSet[i]=clientGuesses[i];
        
        //Check for in-place clues
        for(int i=0;i < 4;i++){
            if(answerSet[i] == cloneAnswerSet[i]){
                cloneAnswerSet[i]=9; //so it will not be matched twice
                clueList.add(1);
            }
        }
        
        //Check for in-place clues
        for(int guess=0;guess < 4;guess++){
            for(int ans=0;ans<4;ans++){                            
                System.out.println("In the for loop, out of place clue");

                    if(answerSet[guess] == cloneAnswerSet[ans] 
                            && cloneAnswerSet[guess] != 9){
                        cloneAnswerSet[ans]=9;
                        clueList.add(0);
                        System.out.println("In the if, out of place clue");
                    }
                }
        }
        
        //Fill the rest of the clue set with empty clues if there are less
        //than 4 clues in the set.
        while(clueList.size() < 4)
            clueList.add(11);
        
        int[] clues=new int[clueList.size()];
        
        //Clone list elements of clues into int array elements.
        for(int i=0;i<clueList.size();i++)
            clues[i]=clueList.get(i);
        
        return clues;
    }
  
    /**
     * Randomly generates an answer set.
     * 
     * @return An set of randomly generated answers.
     */
    private int[] createAnswerSet(){
        int[] randomSet = new int[4];
        Random random = new Random();
        for(int i = 0; i < randomSet.length; i++){
            int randomInt = random.nextInt(colours.length - 0);
            randomSet[i] = colours[randomInt];
        }

        return randomSet;
    }
    
    /**
     * Generates a byte array of colours.
     * 
     * @param array An int array of colours
     * @return A byte array of colours
     */
    private byte[] colourBytes(int[] array){
        byte[] colours = new byte[array.length];
        for(int i = 0; i < colours.length; i++){
           colours[i]= convertIntToByte(i);
        }
        
        return colours;
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
                return 0x00000002; // red
            case 3: 
                return 0x00000003; // yellow
            case 4: 
                return 0x00000004; // green
            case 5:
                return 0x00000005; // blue
            case 6:
                return 0x00000006; // purple
            case 7:
                return 0x00000007; // pink
            case 8:
                return 0x00000008; // light-green
            case 9:
                return 0x00000009; // brown
            default: 
                return -1;
        }
    }
    
    /**
     * Sets the colour representation of a byte.
     * 
     * @param value A byte value
     * @return A colour integer
     */
    private int setColour(byte value){
        switch(value){
            case 0x00000002: 
                return 2; // red
            case 0x00000003: 
                return 3; // yellow
            case 0x00000004: 
                return 4; // green
            case 0x00000005:
                return 5; // blue
            case 0x00000006:
                return 6; // purple
            case 0x00000007:
                return 7; // pink
            case 0x00000008:
                return 8; // light-green
            case 0x00000009:
                return 9; // brown
            default: 
                return -1;
        }
    }
}
