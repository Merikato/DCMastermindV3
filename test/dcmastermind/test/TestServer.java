/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dcmastermind.test;

import dcmastermind.MMPacket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author 1432581
 */
public class TestServer {
    
    public static void main(String[] args) throws IOException{
        System.out.println("Server started");
        int server_port = 50000;
        ServerSocket server_socket = new ServerSocket(server_port);
        
        
        for(;;){
            Socket client_socket = server_socket.accept();
            MMPacket mmp = new MMPacket(client_socket);
            byte[] bs = mmp.readPacket();
            mmp.writePacket(bs);
            System.out.println(new String(bs,StandardCharsets.UTF_8));
            client_socket.close();
        }
    }
}
