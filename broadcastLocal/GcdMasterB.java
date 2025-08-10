package broadcastLocal;

import java.net.*;
import java.io.*;
import java.util.*;

public class GcdMasterB {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Uso: java GcdMaster <x> <y> <z>");
            return;
        }

        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        int z = Integer.parseInt(args[2]);

        List<InetAddress> slaveIPs = new ArrayList<>();
        List<Integer> slavePorts = new ArrayList<>();

        DatagramSocket socket = new DatagramSocket(4000);
        socket.setBroadcast(true);

        // Envia broadcast para descobrir os slaves
        String discoveryMsg = "DISCOVER_GCD_SLAVE";
        byte[] data = discoveryMsg.getBytes();

        int[] ports = {3001, 3002, 3003, 3004, 3005, 3006, 3007, 3008};

        for (int port : ports) {
            DatagramPacket discoveryPacket = new DatagramPacket(data, data.length,
                    InetAddress.getByName("127.0.0.1"), port); 
            socket.send(discoveryPacket);
        }

        // Recebe até 3 respostas
        socket.setSoTimeout(5000); 
        try {
            for (int i = 0; i < 3; i++) {
                byte[] recvBuf = new byte[255];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(receivePacket);

                String msg = new String(receivePacket.getData()).trim();
                String[] parts = msg.split(",");
                int slavePort = Integer.parseInt(parts[1]);

                slaveIPs.add(receivePacket.getAddress());
                slavePorts.add(slavePort);

                System.out.println("Slave " + parts[0] + " encontrado: " +
                        receivePacket.getAddress().getHostAddress() + ":" + slavePort);
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Descoberta finalizada. Escravos encontrados: " + slaveIPs.size());
        }

        if (slaveIPs.size() < 2) {
            System.out.println("São necessários pelo menos dois escravos para o cálculo.");
            return;
        }

        long start = System.nanoTime();

        // Envia x,y para slave1
        sendToSlave(socket, x + "," + y, slaveIPs.get(0), slavePorts.get(0));

        // Envia y,z para slave2
        sendToSlave(socket, y + "," + z, slaveIPs.get(1), slavePorts.get(1));

        int result1 = receiveResult(socket);
        int result2 = receiveResult(socket);

        if (result1 == 1 || result2 == 1) {
            if (slaveIPs.size() >= 3) {
                sendToSlave(socket, "0,0", slaveIPs.get(2), slavePorts.get(2));
            }
            System.out.println("Resultado = 1");
        } else {
            if (slaveIPs.size() >= 3) {
                sendToSlave(socket, result1 + "," + result2, slaveIPs.get(2), slavePorts.get(2));
                int finalResult = receiveResult(socket);
                long end = System.nanoTime();
                System.out.println("Resultado = " + finalResult);
                System.out.println("Tempo de execução = " + (end - start) / 1000000 + " ms");
            } else {
                System.out.println("Resultado intermediário (sem 3º escravo): GCD(" + result1 + ", " + result2 + ")");
            }
        }

        socket.close();
    }

    private static void sendToSlave(DatagramSocket socket, String msg, InetAddress ip, int port) throws IOException {
        byte[] data = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
        socket.send(packet);
    }

    private static int receiveResult(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[255];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return Integer.parseInt(new String(packet.getData()).trim());
    }
}
