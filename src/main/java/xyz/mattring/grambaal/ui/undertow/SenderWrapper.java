package xyz.mattring.grambaal.ui.undertow;

import io.undertow.io.IoCallback;
import io.undertow.io.Sender;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.function.UnaryOperator;

/**
 * Delegates to an inner Sender, optionally applying a UnaryOperator to the ByteBuffer(s) passed to the send method(s).
 */
public class SenderWrapper implements Sender {
    private final Sender delegate;
    private final UnaryOperator<ByteBuffer> optionalByteBufferMunger;

    public SenderWrapper(Sender delegate, UnaryOperator<ByteBuffer> optionalByteBufferMunger) {
        this.delegate = delegate;
        this.optionalByteBufferMunger = optionalByteBufferMunger;
    }

    ByteBuffer munge(ByteBuffer byteBuffer) {
        if (optionalByteBufferMunger != null) {
            return optionalByteBufferMunger.apply(byteBuffer);
        }
        return byteBuffer;
    }

    ByteBuffer[] munge(ByteBuffer[] byteBuffers) {
        if (optionalByteBufferMunger != null) {
            for (int i = 0; i < byteBuffers.length; i++) {
                byteBuffers[i] = optionalByteBufferMunger.apply(byteBuffers[i]);
            }
        }
        return byteBuffers;
    }

    @Override
    public void send(ByteBuffer byteBuffer, IoCallback ioCallback) {
        delegate.send(munge(byteBuffer), ioCallback);
    }

    @Override
    public void send(ByteBuffer[] byteBuffers, IoCallback ioCallback) {
        delegate.send(munge(byteBuffers), ioCallback);
    }

    @Override
    public void send(ByteBuffer byteBuffer) {
        delegate.send(munge(byteBuffer));
    }

    @Override
    public void send(ByteBuffer[] byteBuffers) {
        delegate.send(munge(byteBuffers));
    }

    @Override
    public void send(String s, IoCallback ioCallback) {
        delegate.send(s, ioCallback);
    }

    @Override
    public void send(String s, Charset charset, IoCallback ioCallback) {
        delegate.send(s, charset, ioCallback);
    }

    @Override
    public void send(String s) {
        delegate.send(s);
    }

    @Override
    public void send(String s, Charset charset) {
        delegate.send(s, charset);
    }

    @Override
    public void transferFrom(FileChannel fileChannel, IoCallback ioCallback) {
        delegate.transferFrom(fileChannel, ioCallback);
    }

    @Override
    public void close(IoCallback ioCallback) {
        delegate.close(ioCallback);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
