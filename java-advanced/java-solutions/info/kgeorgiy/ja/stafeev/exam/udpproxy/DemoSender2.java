package info.kgeorgiy.ja.stafeev.exam.udpproxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class DemoSender2 {
    public static void main(final String[] args) throws IOException, InterruptedException {
        final DatagramSocket socket = new DatagramSocket(8882);
        final InetSocketAddress serverAddress = new InetSocketAddress(8888);
        final DatagramPacket datagramPacket = new DatagramPacket(new byte[0], 0, serverAddress);
        while (true) {
            datagramPacket.setData("My name is Grisha".getBytes());
            socket.send(datagramPacket);
            Thread.sleep(1000);
        }
    }
}
