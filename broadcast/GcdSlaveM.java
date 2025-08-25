package broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class GcdSlaveM {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Uso: java GcdSlave <porta> ");
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

            // A mensagem é de da master tentanto descobrir os slaves via broadcast
            if (message.equals("DISCOVER_GCD_SLAVE")) {
                String response = processId + "," + socket.getLocalPort();
                byte[] sendData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(sendData, sendData.length,
                        packet.getAddress(), 4000);
                socket.send(responsePacket);
            } else {
                String[] parts = message.split(",");
                long a = Long.parseLong(parts[0]);
                long b = Long.parseLong(parts[1]);

                if (a == 0 && b == 0) {
                    System.out.println("Slave " + processId + " recebeu comando para finalizar.");
                    break;
                }

                long result = gcd(a, b);
                byte[] sendData = String.valueOf(result).getBytes();
                DatagramPacket resultPacket = new DatagramPacket(sendData, sendData.length,
                        packet.getAddress(), 4000);
                System.out.println("Aqui 1");
                socket.send(resultPacket);
                System.out.println("Aqui 2");

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

