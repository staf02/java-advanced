package info.kgeorgiy.ja.stafeev.hello;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class NonblockingUDPPacket {
    ByteBuffer buffer;
    SocketAddress address;

    public NonblockingUDPPacket(ByteBuffer buffer, SocketAddress address) {
        this.buffer = buffer;
        this.address = address;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }
}
