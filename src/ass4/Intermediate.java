package ass4;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class Intermediate implements Runnable {
	static Thread clientHost;
	static Thread serverHost;
	
    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket;
    boolean isClient;
    int clientPort, serverPort;
    SharedMemory mem;
    boolean lastPacket = false;
    
    private ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
    private RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    private Map<Long, Long> threadInitialCPU = new HashMap<Long, Long>();
    private long initialUptime = runtimeMxBean.getUptime() * 1000000; 

    /*
     * Default constructor does nothing.
     */
    public Intermediate() {
    	
    }
    
    /*
     * Constructor for Intermediate Host class which implements Runnable.
     * Assign the shared memory object, determine if thread is a Client thread or Server thread,
     * initialize the DatagramSocket's.
     */
    public Intermediate(int status, SharedMemory sm) {
        mem = sm;
        try {
            if(status == 0) {
                isClient = true;
                sendReceiveSocket = new DatagramSocket(23);
            } else if(status == 1) {
                isClient = false;
                sendReceiveSocket = new DatagramSocket(69);
            }
        } catch(SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    
    /**
     * Performs receiving a message, replying with confirmation, and putting onto shared memory for Client thread,
     * and receiving a request, replying with message, and getting from shared memory for Server thread.
     */
    public void run() {
        DatagramPacket rcv;
        byte[] sentMsg;
        byte[] msgToSend = new byte[1012];
        byte[] successMsg = {9, 9, 9, 9};
        while(!lastPacket) {
            if(isClient) {
                // got data from client
                rcv = getInfo();
                clientPort = rcv.getPort();
                sentMsg = rcv.getData();
                mem.put(sentMsg);
                sendReply(successMsg);
                
                // got request from client
                rcv = getInfo();
                sentMsg = rcv.getData();
                if(sentMsg[0] == (byte) 103) {
                	byte[] serverAnswer = mem.get();
                    sendReply(serverAnswer);
                }
            } else {
                // got request from server
                rcv = getInfo();
                serverPort = rcv.getPort();
                sentMsg = rcv.getData();
                if(sentMsg[0] == (byte) 103) {
                    msgToSend = mem.get();
                	sendReply(msgToSend);
                }
                
                // got data from server
                rcv = getInfo();
                sentMsg = rcv.getData();
                mem.put(sentMsg);
                sendReply(successMsg);
            }
        }
    }

    private DatagramPacket getInfo() {
        /*
         * Blocks until the sendReceiveSocket receives a packet.
         */
        byte[] data = new byte[1012];
        receivePacket = new DatagramPacket(data, data.length);
        try {
            sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        if((receivePacket.getData()[1] == (byte) 9) || (receivePacket.getData()[0] == (byte) 1)) {
        	lastPacket = true;
        }
        
        return receivePacket;
    }

    private void sendReply(byte[] msg) {
        if(isClient) {
            /*
             * Copy the message into the packet.
             */
            try {
                sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), clientPort);
            } catch(UnknownHostException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            /*
             * Copy the message into the packet.
             */
            try {
                sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), serverPort);
            } catch(UnknownHostException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        /*
         * Send packet using sendReceiveSocket.
         */
        try {
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
    	SharedMemory sm = new SharedMemory();
		
    	clientHost = new Thread(new Intermediate(0, sm), "Client Host");
		serverHost = new Thread(new Intermediate(1, sm), "Server Host");
		
		// allow for user to start Client and Server classes.
		try {
	        Thread.sleep(15000);
	    } catch (InterruptedException e ) {
	        e.printStackTrace();
	        System.exit(1);
	    }
		
		clientHost.start();
		serverHost.start();
		
		// java.lang.management Method
		//new Intermediate().measure();
		
		// System.nanoTime Method
		new Intermediate().timing();
    }
    
    /*
     * java.lang.management method for timing execution of intermediate threads.
     */
    private void measure() {
    	ThreadInfo[] threadInfos = threadMxBean.dumpAllThreads(false, false);
    	for (ThreadInfo info : threadInfos) {
    		threadInitialCPU.put(info.getThreadId(), threadMxBean.getThreadCpuTime(info.getThreadId()));
    	}
    	
    	// join and wait for threads clientHost and serverHost to complete
    	try {
			clientHost.join();
			serverHost.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	long upTime = runtimeMxBean.getUptime() * 1000000;
    	
    	Map<Long, Long> threadCurrentCPU = new HashMap<Long, Long>();
    	threadInfos = threadMxBean.dumpAllThreads(false, false);
    	for (ThreadInfo info : threadInfos) {
    		threadCurrentCPU.put(info.getThreadId(), threadMxBean.getThreadCpuTime(info.getThreadId()));
    	}
    	
    	// elapsedTime is in nanoseconds.
    	long elapsedTime = (upTime - initialUptime);
    	
    	// Print elapsed time in nanoseconds
    	System.out.println("Execution time using java.lang.management method in ns: " + elapsedTime);
    }
    
    /*
     * System.nanoTime method for timing execution of intermediate threads.
     */
    public void timing() {
    	// startTime of execution in nanoseconds
    	long startTime = System.nanoTime();
    	
    	// join and wait for threads clientHost and serverHost to complete
    	try {
			clientHost.join();
			serverHost.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	// endTime of execution in nanoseconds
    	long endTime = System.nanoTime();
    	
    	//timeElapsed is in nanoseconds
    	long timeElapsed = endTime - startTime;
    	
    	//Print time elapsed in nanoseconds
    	System.out.println("Execution time using System.nanoTime method in ns: " + timeElapsed);
    }
}
