package broadcast;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class GcdMasterM {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        List<InetSocketAddress> slaveAddresses = new ArrayList<>();

        DatagramSocket socket = new DatagramSocket(4000);
        socket.setBroadcast(true);

        int opc;
        do {
            System.out.println(
                    "\nEscolha a opção desejada:\n" +
                    "0 - Sair (Finished GCD)\n" +
                    "1 - Calcular o GCD\n" +
                    "2 - Listar máquinas via broadcast\n" +
                    "3 - Visualizar resultados da execução"
            );
            System.out.print("Opção: ");
            opc = scanner.nextInt();

            switch (opc) {
                case 0:
                    System.out.println("Programa encerrado.");
                    break;

                case 1:
                    if (slaveAddresses.isEmpty()) {
                        System.out.println("Nenhum nó encontrado. Use a opção 2 primeiro!");
                    } else {
                        System.out.print("Digite os números separados por espaço: ");
                        scanner.nextLine(); // consumir quebra de linha
                        String[] nums = scanner.nextLine().trim().split("\\s+");
                        LinkedList<Long> novosNumeros = new LinkedList<>();
                        try {
                            for (String n : nums) {
                                novosNumeros.add(Long.parseLong(n));
                            }
                            System.out.println("Calculando GCD...");
                            gcdCalculate(socket, novosNumeros, slaveAddresses);
                        } catch (NumberFormatException e) {
                            System.out.println("Entrada inválida! Digite apenas números.");
                        }
                    }
                    break;

                case 2:
                    System.out.println("Descobrindo máquinas via broadcast...");
                    slaveAddresses = sendBroadCast();
                    break;

                case 3:
                    readResultFile("gcd_results.txt");
                    break;

                default:
                    System.out.println("Opção inválida! Tente novamente.");
            }
        } while (opc != 0);

        socket.close();
        scanner.close();
    }

    // Descoberta via broadcast
    private static List<InetSocketAddress> sendBroadCast() throws IOException {
        List<InetSocketAddress> slaveAddresses = new ArrayList<>();

        DatagramSocket socket = new DatagramSocket(); 
        socket.setBroadcast(true);

        String discoveryMsg = "DISCOVER_GCD_SLAVE";
        byte[] data = discoveryMsg.getBytes();

        DatagramPacket discoveryPacket = new DatagramPacket(
                data, data.length, InetAddress.getByName("255.255.255.255"), 3000);
        socket.send(discoveryPacket);

        socket.setSoTimeout(1000);
        try {
            while (slaveAddresses.size() < 10) {
                byte[] recvBuf = new byte[255];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(receivePacket);

                String msg = new String(receivePacket.getData()).trim();
                String[] parts = msg.split(",");
                int slavePort = Integer.parseInt(parts[1]);

                InetAddress slaveIP = receivePacket.getAddress();
                InetSocketAddress slaveAddress = new InetSocketAddress(slaveIP, slavePort);
                if (!slaveAddresses.contains(slaveAddress)) {
                    slaveAddresses.add(slaveAddress);
                    System.out.println("Slave " + parts[0] + " encontrado: " +
                            slaveIP.getHostAddress() + ":" + slavePort);
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Descoberta finalizada. Escravos encontrados: " + slaveAddresses.size());
        }

        if (slaveAddresses.isEmpty()) {
            System.out.println("Nenhum escravo encontrado.");
        }
        socket.close();

        return slaveAddresses;
    }

    // GCD
    private static void gcdCalculate(DatagramSocket socket, LinkedList<Long> numbers, List<InetSocketAddress> slaveAddresses) throws IOException {
        long start = System.nanoTime();

        Queue<Long> queue = new LinkedList<>(numbers);
        int slaveIndex = 0;

        while (queue.size() > 1) {
            long a = queue.poll();
            long b = queue.poll();

            InetSocketAddress addr = slaveAddresses.get(slaveIndex % slaveAddresses.size());
            InetAddress ip = addr.getAddress();
            int port = addr.getPort();

            slaveIndex++;

            String values = HelperClass.makeMessage(a, b);
            sendToSlave(socket, values, ip, port);
            long gcd = receiveResult(socket);

            queue.add(gcd);
        }

        long end = System.nanoTime();
        long durationMillis = (end - start) / 1_000_000L;
        long result = queue.poll();

        System.out.println("Resultado final (GCD): " + result);
        System.out.println("Tempo de execução = " + durationMillis + " ms");

        saveResultInFile(result, durationMillis, "gcd_results.txt");
    }

    private static void sendToSlave(DatagramSocket socket, String msg, InetAddress ip, int port) throws IOException {
        byte[] data = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
        socket.send(packet);
    }

    private static long receiveResult(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[255];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return Long.parseLong(new String(packet.getData()).trim());
    }

    // Salvar resultados como se fosse log
    private static void saveResultInFile(long gcd, long durationMillis, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, true))) {
            String dataHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            pw.println(dataHora + " - Resultado GCD: " + gcd + ", Tempo: " + durationMillis + " ms");
        } catch (IOException e) {
            System.out.println("Erro ao salvar resultado: " + e.getMessage());
        }
    }

    private static void readResultFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Arquivo de resultados não encontrado!");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            System.out.println("=== Resultados do GCD ===");
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("=========================");
        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo: " + e.getMessage());
        }
    }
}
