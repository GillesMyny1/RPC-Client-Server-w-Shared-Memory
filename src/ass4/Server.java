package ass4;

import java.io.*;
import java.net.*;

public class Server {
    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket;
    boolean lastPacket = false;

    /*
     * Constructor initializes the DatagramSocket for the Server process.
     */
    public Server() {
        try {
            sendReceiveSocket = new DatagramSocket();
        } catch(SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /*
     * sendAndReceive deals with requesting data from the Intermediate Host and then responding with data to the Intermediate Host.
     */
    public void sendAndReceive() {
    	while(!lastPacket) {
    		byte[] answer = new byte[1012];
            receivePacket = new DatagramPacket(answer, answer.length);

            /*
             * Request message is the word "get" in ASCII code.
             */
            byte[] request = new byte[1012];
            request[0] = (byte) 103;
            request[1] = (byte) 101;
            request[2] = (byte) 116;

            answer = rpc_send(request);
            
            /*
             * Initialize response byte arrays.
             */
            byte[] readMsg = new byte[] {0, 3, 0, 1};
            byte[] writeMsg = new byte[] {0, 4, 0, 0};
            byte[] errorMsg = new byte[] {1, 1, 1, 1};

            if(answer[1] == (byte) 9) {
            	lastPacket = true;
            }
            
            /*
             * Check which response is necessary based on first two bits of received data.
             * Or send back error code.
             */
            if((answer[0] == (byte) 0) && (answer[1] == (byte) 1)) {
                answer = rpc_send(readMsg);
            } else if((answer[0] == (byte) 0) && (answer[1] == (byte) 2)) {
                answer = rpc_send(writeMsg);
            } else {
                answer = rpc_send(errorMsg);
            }
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
            sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 69);
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
		Server s = new Server();
		s.sendAndReceive();
	}
}
