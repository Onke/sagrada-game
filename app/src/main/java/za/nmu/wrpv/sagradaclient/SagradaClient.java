package za.nmu.wrpv.sagradaclient;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import nmu.sagrada.protocol.ContinueTurn;
import nmu.sagrada.protocol.Difficulty;
import nmu.sagrada.protocol.GameMode;
import nmu.sagrada.protocol.Join;
import nmu.sagrada.protocol.Message;
import nmu.sagrada.protocol.PlaceDie;
import nmu.sagrada.protocol.Quit;
import nmu.sagrada.protocol.RematchRequest;
import nmu.sagrada.protocol.SkipTurn;
import nmu.sagrada.protocol.WindowSelected;

public final class SagradaClient {
    public interface Listener {
        void onMessage(Message message);
        void onConnectionError(String message);
    }

    private static final String TAG = "SagradaClient";
    private static final int PORT = 5000;
    private final Listener listener;
    private final BlockingQueue<Message> outgoing = new LinkedBlockingQueue<>();
    private final AtomicBoolean closed = new AtomicBoolean(true);
    private Socket socket;
    private Thread reader;
    private Thread writer;

    public SagradaClient(Listener listener) { this.listener = listener; }

    public void connect(String address, String name, GameMode mode, int botCount, Difficulty difficulty) {
        if (!closed.compareAndSet(true, false)) return;
        outgoing.clear();
        outgoing.offer(new Join(name, mode, botCount, difficulty));
        reader = new Thread(() -> readLoop(address), "sagrada-reader");
        reader.start();
    }

    public void selectWindow(String windowId) { send(new WindowSelected(windowId)); }
    public void placeDie(int draftIndex, int cellIndex) { send(new PlaceDie(draftIndex, cellIndex)); }
    public void skipTurn() { send(new SkipTurn()); }
    public void continueTurn() { send(new ContinueTurn()); }
    public void requestRematch(int botCount, Difficulty difficulty) {
        send(new RematchRequest(botCount, difficulty));
    }

    public void disconnect() {
        if (!closed.get()) send(new Quit());
    }

    public void closeImmediately() { closeSocket(); }

    private void send(Message message) {
        if (!closed.get()) outgoing.offer(message);
    }

    private void readLoop(String address) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, PORT), 7000);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            writer = new Thread(() -> writeLoop(output), "sagrada-writer");
            writer.start();
            while (!closed.get()) {
                Object value = input.readObject();
                if (value instanceof Message) listener.onMessage((Message) value);
            }
        } catch (EOFException ignored) {
        } catch (Exception error) {
            if (!closed.get()) {
                Log.e(TAG, "Connection failed", error);
                listener.onConnectionError(error.getMessage() == null ? "Connection failed" : error.getMessage());
            }
        } finally {
            closeSocket();
        }
    }

    private void writeLoop(ObjectOutputStream output) {
        try {
            while (!closed.get()) {
                Message message = outgoing.take();
                output.writeObject(message);
                output.flush();
                output.reset();
                if (message instanceof Quit) break;
            }
        } catch (IOException | InterruptedException error) {
            if (!closed.get()) listener.onConnectionError("Connection to the server was lost.");
        } finally {
            closeSocket();
        }
    }

    private void closeSocket() {
        if (!closed.compareAndSet(false, true)) return;
        if (writer != null) writer.interrupt();
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }
}
