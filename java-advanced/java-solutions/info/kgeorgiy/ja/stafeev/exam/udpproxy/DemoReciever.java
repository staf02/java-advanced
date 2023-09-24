package info.kgeorgiy.ja.stafeev.exam.udpproxy;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class DemoReciever {
    public static void main(final String[] args) throws IOException, InterruptedException {
        final var address = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8880);
        final DatagramSocket socket = new DatagramSocket(address);
        System.out.println(address);
        final DatagramPacket datagramPacket = new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
        while (true) {
            socket.receive(datagramPacket);
            final String s = new String(datagramPacket.getData(),
                    datagramPacket.getOffset(),
                    datagramPacket.getLength(),
                    StandardCharsets.UTF_8);
            System.out.println(s);
        }
    }
}
