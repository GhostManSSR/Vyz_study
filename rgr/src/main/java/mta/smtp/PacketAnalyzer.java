package mta.smtp;

public class PacketAnalyzer {

    public static void analyze(String line) {

        System.out.println("SMTP PACKET: " + line);

        if (line.startsWith("AUTH")) {
            System.out.println("AUTH detected");
        }

        if (line.startsWith("DATA")) {
            System.out.println("DATA section start");
        }
    }
}
