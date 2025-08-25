package broadcastGcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class GcdSlaveM {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Uso: java GcdSlaveM <id> ");
            return;
        }

        int processId = Integer.parseInt(args[0]);

        DatagramSocket socket = new DatagramSocket(3000);
        System.out.println("Slave " + processId + " escutando na porta " + socket.getLocalPort());

        while (true) {
            byte[] buffer = new byte[255];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String message = new String(packet.getData(), 0, packet.getLength()).trim();

            if (message.equals("EXIT")) {
                System.out.println("Slave " + processId + " finalizado.");
                break;
            } else {
                String[] parts = message.split(",");
                long a = Long.parseLong(parts[0]);
                long b = Long.parseLong(parts[1]);

                long result = gcd(a, b);
                byte[] sendData = String.valueOf(result).getBytes();
                DatagramPacket resultPacket = new DatagramPacket(sendData, sendData.length,
                        packet.getAddress(), 4000);
                socket.send(resultPacket);
            }
        }

        socket.close();
    }

    private static long gcd(long a, long b) {
        if (a == 0) return b;
        if (b == 0) return a;
        while (a != b) {
            if (a > b) a -= b;
            else b -= a;
        }
        return a;
    }
}
