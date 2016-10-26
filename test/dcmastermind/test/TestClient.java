/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dcmastermind.test;

import dcmastermind.MMPacket;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author 1432581
 */
public class TestClient {
    
    public static void main(String[] args) throws IOException{
        System.out.println("line1");
        int server_port = 50000;
         System.out.println("line2");
        byte[] packet = new byte[]{0x00000000,0x00000001,0x00000002,0x000002};
        Socket soc = new Socket("10.172.11.194",server_port);
        System.out.println("Connected to server...sending echo string");
        MMPacket mmp = new MMPacket(soc);
        mmp.writePacket(packet);
        packet = mmp.readPacket();
        for(byte b : packet)
            System.out.println(b);
        soc.close();
    }
}
