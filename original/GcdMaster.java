/**
 * Concurrent Benchmarks
 * 
 * Title:  parallel_gcd      
 * 
 * Description:  Parallel GCD is a program that calculates the
 *               Greatest Common Divisor (GCD) between three numbers
 *               using three slaves processes.
 *
 * Paradigm:     Message Passing
 *               
 * Year:         2024
 *               
 * @author       George Gabriel Mendes Dourado
 * @version      2.0
 */
 
/* TEST 1
 * java GcdSlave 1 3001 127.0.0.1
 * java GcdSlave 2 3002 127.0.0.1
 * java GcdSlave 3 3003 127.0.0.1
 * java GcdMaster 9 18 6 127.0.0.1 127.0.0.1 127.0.0.1
 * 
 * OUTPUT:
 * Result = 3
 */

/* TEST 2
 * java GcdSlave 1 3001 127.0.0.1
 * java GcdSlave 2 3002 127.0.0.1
 * java GcdSlave 3 3003 127.0.0.1
 * java GcdMaster 8 12 7 127.0.0.1 127.0.0.1 127.0.0.1
 * 
 * OUTPUT:
 * Result = 1
 */

import java.net.*;
import java.io.*;

public class GcdMaster {
    public static void main(String[] args) throws IOException {
    	final int ZERO = 0;
        long start = System.nanoTime();
        int firstValue = 0;  // X
        int secondValue = 0; // Y
        int thirdValue = 0;  // Z
        int receivedValueOfProcess1 = 0; // GCD result received of the slave 1 process
        int receivedValueOfProcess2 = 0; // GCD result received of the slave 2 process
        int receivedValueOfProcess3 = 0; // GCD result received of the slave 3 process
        String data;
        int portNumber = 6789; // standard master port
        InetAddress remoteIPSlave1 = null;
        InetAddress remoteIPSlave2 = null;
        InetAddress remoteIPSlave3 = null;
        int remotePortSlave1 = 3001; // standard slave 1 port
        int remotePortSlave2 = 3002; // standard slave 2 port
        int remotePortSlave3 = 3003; // standard slave 3 port

        if (args != null && args.length == 6) {
            firstValue = Integer.parseInt(args[0]);
            secondValue = Integer.parseInt(args[1]);
            thirdValue = Integer.parseInt(args[2]);
            remoteIPSlave1 = InetAddress.getByName(args[3]);
            remoteIPSlave2 = InetAddress.getByName(args[4]);
            remoteIPSlave3 = InetAddress.getByName(args[5]);
        } else {
            System.out.println("Wrong number of arguments.");
        }
     
        // create socket to master process
        DatagramSocket socket = new DatagramSocket(portNumber);

        // send x and y to the slave 1
        String values = HelperClass.makeMessage(firstValue, secondValue);
        sendValueToSlave(socket, values, remoteIPSlave1, remotePortSlave1);

        // send y and z to the slave 2
        values = HelperClass.makeMessage(secondValue, thirdValue);
        sendValueToSlave(socket, values, remoteIPSlave2, remotePortSlave2);

        byte[] receiveBuffer;
        
        // receiving the results of the GCD returned by the slaves 1 and 2
        for (int i = 1; i <= 2; i++) {
            receiveBuffer = new byte[255];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            data = new String(receivePacket.getData());
            if (i == 1) {
                receivedValueOfProcess1 = (Integer.parseInt(data.trim()));
            } else {
                receivedValueOfProcess2 = (Integer.parseInt(data.trim()));
            }
        }

        /* verification of the returned results. If slave 1 or slave 2 
           returned the result one, then finalize the slave process 3
           sending two data with value ZERO to him */
        if (receivedValueOfProcess1 == 1 || receivedValueOfProcess2 == 1) {
            values = HelperClass.makeMessage(ZERO, ZERO);
            sendValueToSlave(socket, values, remoteIPSlave3, remotePortSlave3);
            System.out.println("Result = 1");
            
        } else { // otherwise, send to slave 3 the results returned by the slaves 1 and 2 
            values = HelperClass.makeMessage(receivedValueOfProcess1, receivedValueOfProcess2);
            sendValueToSlave(socket, values, remoteIPSlave3, remotePortSlave3);
            
            receiveBuffer = new byte[255];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            // receiving the result of the GCD returned by the slave process 3
            long end = System.nanoTime();
            long time = end - start;
            socket.receive(receivePacket);
            data = new String(receivePacket.getData());
            receivedValueOfProcess3 = (Integer.parseInt(data.trim()));
            System.out.println("Result = " + receivedValueOfProcess3);
            System.out.println("Execution time = " + time / 1000000 + " milliseconds");
        }
    }

    // send method
    private static void sendValueToSlave(DatagramSocket socket, String values,
    		InetAddress remoteIP, int remotePort) throws IOException {
        byte[] sendBuffer = new byte[255];
        sendBuffer = values.toString().getBytes();
        DatagramPacket datagram = new DatagramPacket(sendBuffer, sendBuffer.length, remoteIP, remotePort);
        socket.send(datagram);
    }
}
