package ass4;

import java.io.*;
import java.net.*;

public class Client {
    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket;

    /**
     * Constructor initializes the requestType String and
     * initializes the sendReceiveSocket DatagramSocket.
     */
    public Client() {
        try {
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /*
     * sendAndReceive deals with sending a packet to the Intermediate Host and then requesting an answer from the Intermediate Host.
     */
    public void sendAndReceive() {
        for(int i = 1; i <= 1000; i++) {
            byte[] msg = new byte[1012];
            byte[] fileName = new byte[1000];
            String mode = "netascii";
            byte[] modeBytes = mode.getBytes();
            
            for(int j = 0; j <= 999; j++) {
            	fileName[j] = (byte) j;
            }
            
            /*
             * Cycle through request types
             */
            byte zero = 0;
            if(i == 1000) {
                msg[0] = zero;
                byte invalid = 9;
                msg[1] = invalid;
            } else if(i % 2 == 0) {
                msg[0] = zero;
                byte one = 1;
                msg[1] = one;
            } else {
                msg[0] = zero;
                byte two = 2;
                msg[1] = two;
            }

            System.arraycopy(fileName, 0, msg, 2, fileName.length);
            msg[fileName.length + 2] = zero;
            System.arraycopy(modeBytes, 0, msg, fileName.length + 3, modeBytes.length);
            msg[fileName.length + modeBytes.length + 3] = zero;

            byte[] answer = new byte[1012];
            receivePacket = new DatagramPacket(answer, answer.length);

            answer = rpc_send(msg);

            /*
             * Request message is the word "get" in ASCII code.
             */
            byte[] request = new byte[1012];
            request[0] = (byte) 103;
            request[1] = (byte) 101;
            request[2] = (byte) 116;

            answer = rpc_send(request);
        }
    }

    /**
     * rpc_send deals with sending a byte array to the Intermediate Host and returning a response of byte array type.
     * @param msg is a byte array containing the message to send
     * @return is a byte array containing the response to the sent message
     */
    private byte[] rpc_send(byte[] msg) {
        /*
         * Copy the message into the packet.
         */
        try {
            sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 23);
        } catch(UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        /*
         * Send packet using sendReceiveSocket
         */
        try {
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        /*
         * Block until DatagramSocket receives a DatagramPacket.
         */
        try {
            sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return receivePacket.getData();
    }
    
    public static void main(String args[]) {
		Client c = new Client();
		c.sendAndReceive();
	}
}
