
package dcmastermindclient;

import dcmastermind.MMPacket;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 * Client class that retrieves the IP to connect to, and sends/receives packets 
 * for the client. 
 * 
 * @author Rafia Anwar, Evan Glicakis, and Seaim Khan
 */
public class Client {
    private String server_ip;
    private int port;
    private MMPacket mmPacket;
    private byte[] clues;
    
    /**
     * Default contructor, sets ip to localhost.
     */
    public Client(){
        this.server_ip = "localhost";
        this.port = 50000;
    }
    
    /**
     * Constructor to allow client to connect to a host with an IP address on
     * default port 50000
     * @param ip The host IP address
     */
    public Client(String ip){
        this.server_ip = ip;
        this.port = 50000;
    }
    
    /**
     * Constructor to allow client to connect to a host with an IP address on
     * a port set by the client.
     * 
     * @param ip The host IP address
     * @param port The port
     */
    public Client(String ip, int port){
        this.server_ip = ip;
        this.port = port;
    }

    /**
     * Gets the server's IP address.
     * 
     * @return An IP address
     */
    public String getServer_Ip() {
        return server_ip;
    }

    /**
     * Sets the server's IP address.
     * 
     * @param server_ip The server's IP address
     */
    public void setServer_Ip(String server_ip) {
        this.server_ip = server_ip;
    }
    
    /**
     * Gets the port number.
     * 
     * @return A port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number.
     * 
     * @param port The port number.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets an MMPacket object.
     * 
     * @return an MMPacket object.
     */
    public MMPacket getMmPacket() {
        return mmPacket;
    }

    /**
     * Sets an MMPacket object.
     * 
     * @param mmPacket The MMPacket object.
     */
    public void setMmPacket(MMPacket mmPacket) {
        this.mmPacket = mmPacket;
    }

    /**
     * Gets a set of clues.
     * 
     * @return A byte array of clues
     */
    public byte[] getClues() {
        return clues;
    }

    /**
     * Sets a set of clues.
     * 
     * @param clues The byte array of clues
     */
    public void setClues(byte[] clues) {
        this.clues = clues;
    }
    /**
     * Plays a turn from the UI and receives the clues from the server 
     * after sending the guesses.
     * 
     * @param guess A set of guesses
     * @throws IOException if unable to read/write packets.
     */
    public void play_turn(int[] guess) throws IOException{
        byte[] packet = new byte[guess.length];
        for(int i = 0; i < packet.length; i++){
            packet[i] = convertIntToByte(guess[i]);
        }
        // send out the guess.
        System.out.println("sent");
        mmPacket.writePacket(packet);
        //read the clues received back
        clues = mmPacket.readPacket();
        System.out.println("received clues...\n" + "Clues: " + Arrays.toString(clues));
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
     * Creates a socket and implements the MMPacket.
     * 
     * @throws IOException if unable to create the socket.
     */
    public void createSocket() throws IOException{
        Socket soc = new Socket(server_ip, port);
        mmPacket = new MMPacket(soc);
    }
    
    /**
     * Starts the game for the client.
     * 
     * @throws IOException if unable to read/write packets
     */
    public void startGame() throws IOException{
        System.out.println("sending start message to server...");
        byte[] startMessage = new byte[]{0x00000011, 0,0,0};
        System.out.println("sending packet: " + Arrays.toString(startMessage));
        mmPacket.writePacket(startMessage);
        System.out.println("packet sent.");
        if(mmPacket.readPacket()[0] == 0x0000000A){
            System.out.println("aOK");
        }
    }
    
  
}
