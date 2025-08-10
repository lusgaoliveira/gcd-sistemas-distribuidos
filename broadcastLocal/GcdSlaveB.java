package broadcastLocal;

import java.net.*;
import java.io.*;

public class GcdSlaveB {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Uso: java GcdSlave <id> <porta>");
            return;
        }

        int processId = Integer.parseInt(args[0]);
        int slavePort = Integer.parseInt(args[1]);

        DatagramSocket socket = new DatagramSocket(slavePort);
        System.out.println("Slave " + processId + " escutando na porta " + slavePort);

        while (true) {
            byte[] buffer = new byte[255];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String message = new String(packet.getData(), 0, packet.getLength()).trim();

            if (message.equals("DISCOVER_GCD_SLAVE")) {
                String response = processId + "," + slavePort;
                byte[] sendData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(sendData, sendData.length,
                        packet.getAddress(), 4000);
                socket.send(responsePacket);
            } else {
                String[] parts = message.split(",");
                int a = Integer.parseInt(parts[0]);
                int b = Integer.parseInt(parts[1]);

                if (a == 0 && b == 0) {
                    System.out.println("Slave " + processId + " recebeu comando para finalizar.");
                    break;
                }

                int result = gcd(a, b);
                byte[] sendData = String.valueOf(result).getBytes();
                DatagramPacket resultPacket = new DatagramPacket(sendData, sendData.length,
                        packet.getAddress(), 4000);
                socket.send(resultPacket);
                break;
            }
        }

        socket.close();
    }

    private static int gcd(int a, int b) {
        if (a == 0) return b;
        if (b == 0) return a;
        while (a != b) {
            if (a > b) a -= b;
            else b -= a;
        }
        return a;
    }
}
