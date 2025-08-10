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

public class GcdSlave {
    public static void main(String[] args) throws IOException {
        int processId = 1; // 1 or 2 or 3
        int firstValueSlave; // X if slave process 1 or Y if slave process 2
        int secondValueSlave; // Y if slave process 1 or Z if slave process 2
        int slavePort = 3001; // standard slave port
        InetAddress masterIP = null; // standard masterIP
        int masterPort = 6789; // standard master port

        
        if (args != null && args.length == 3) {
            processId = Integer.parseInt(args[0]);
            slavePort  = Integer.parseInt(args[1]); 
            masterIP  = InetAddress.getByName(args[2]);
        } else {
            System.out.println("Wrong number of arguments.");
        }

        // create socket of slave process
        DatagramSocket socket = new DatagramSocket(slavePort); 

        // receives the values of the master process
        byte[] receiveBuffer = new byte[255];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        socket.receive(receivePacket);
        String[] result = new String(receivePacket.getData()).trim().split(",");
        firstValueSlave = Integer.parseInt(result[0]);
        secondValueSlave = Integer.parseInt(result[1]);

        // calculates the GCD
        int gcdResult = gcd(firstValueSlave, secondValueSlave);
        String dataString = Integer.toString(gcdResult);
        
        if (firstValueSlave != 0 && secondValueSlave != 0) {
            // sends result to master process
            byte[] sendBuffer = new byte[255];
            sendBuffer = dataString.getBytes();
            DatagramPacket datagram = new DatagramPacket(sendBuffer, sendBuffer.length, masterIP, masterPort);
            socket.send(datagram);
            socket.close();
        }
    }

    // method to calculate the GCD
    private static int gcd(int firstValueSlave, int secondValueSlave) {
        if (firstValueSlave == 0 && secondValueSlave == 0) {
            // finish
            return 0;
        } else {
            while (firstValueSlave != secondValueSlave) {
                if (firstValueSlave < secondValueSlave) {
                    secondValueSlave = secondValueSlave - firstValueSlave;
                } else {
                    firstValueSlave = firstValueSlave - secondValueSlave;
                }
            }
        }
        return firstValueSlave;
    }
}