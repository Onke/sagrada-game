package za.nmu.wrpv.sagradaclient;

import android.os.Parcel;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import nmu.sagrada.messages.Message;
import nmu.sagrada.messages.MessageReceiver;
import nmu.sagrada.messages.client.Join;
import nmu.sagrada.messages.client.PlaceDie;
import nmu.sagrada.messages.client.PlayerLeft;
import nmu.sagrada.messages.client.PlayerRight;
import nmu.sagrada.messages.client.PrivateObjectiveRequest;
import nmu.sagrada.messages.client.PublicObjectivesRequest;
import nmu.sagrada.messages.client.Quit;
import nmu.sagrada.messages.client.SetupDone;
import nmu.sagrada.messages.client.SkipTurn;
import nmu.sagrada.messages.client.WindowListRequest;
import nmu.sagrada.messages.client.WindowSelected;

public class SagradaClient{

    private String serverAddress;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;

    private BlockingQueue<Message> outGoingMessages;
    private MessageReceiver messageReceiver;

    private Thread readThread;
    private Thread writeThread;

    public SagradaClient(MessageReceiver messageReceiver){
        this.messageReceiver = messageReceiver;
        outGoingMessages = new LinkedBlockingDeque<>();
    }

    private void send(Message message) {
        try {
            outGoingMessages.put(message);
        } catch (InterruptedException e) {
            Log.e("SagradaClient", Objects.requireNonNull(e.getMessage()));
        }
    }

    public void connect(String serverAddress, String name) {
        Log.i("SagradaClient", "Connecting to " + serverAddress + "...");

        this.serverAddress = serverAddress;

        Log.i("SagradaClient", "Starting read loop thread...");
        readThread = new ReadThread();
        readThread.start();

        Log.i("SagradaClient", "Queuing setHandle(" + name + ")");
        send(new Join(name));
    }

    public void disconnect() {
        send(new Quit());
    }

    public void sendPlacedDieMessage(int draftPoolSelection, int windowSelection) {
        send(new PlaceDie(draftPoolSelection, windowSelection));

    }

    public void sendLeftPlayerRequest(String name){
        send(new PlayerLeft(name));
    }
    public void sendRightPlayerRequest(String name){
        send(new PlayerRight(name));
    }

    public void sendWindowSelected(String window){
        send(new WindowSelected(window));
    }

    public void sendSkipTurn(){
        send(new SkipTurn());
    }

    public void sendSetupDone() {
        send(new SetupDone());
    }

    public void sendPrivateObjectiveRequest() {
        send(new PrivateObjectiveRequest());
    }

    public void sendPublicObjectiveRequest() {
        send(new PublicObjectivesRequest());
    }

    public void sendWindowListRequest() {
        send(new WindowListRequest());
    }


    private class ReadThread extends Thread implements Serializable{
        @Override
        public void run() {
            Log.i("SagradaClient", "Starting read loop thread...");

            readThread = this;

            try {
                Socket connection = new Socket(serverAddress, 5000);
                Log.i("SagradaClient", "Connected to " + serverAddress + "...");


                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                Log.i("SagradaClient", "Obtained I/O stream...");

                Log.i("SagradaClient", "Starting write loop thread...");
                writeThread = new WriteThread();
                writeThread.start();

                Log.i("SagradaClient", "Starting read loop thread...");
                Message message;
                do {
                    message = (Message) in.readObject();
                    Log.i("SagradaClient", ">>> " + message);

                    if (messageReceiver != null)
                        messageReceiver.messageReceived(message);
                } while (message.getClass() != Quit.class);

                connection.close();
                Log.i("SagradaClient", "Connection closed...");

            } catch (Exception e) {
                Log.e("SagradaClient", "Exception : " + e.getMessage());
            } finally {
                readThread = null;
            }

        }
    }

    private class WriteThread extends Thread implements Serializable {
        @Override
        public void run() {
            Log.i("SagradaClient", "Starting write loop thread...");
            try {
                while (true) {
                    Message message = outGoingMessages.take();

                    out.writeObject(message);
                    out.flush();

                    Log.i("SagradaClient", "<<< " + message);
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                writeThread = null;
            }
        }
    }
}
