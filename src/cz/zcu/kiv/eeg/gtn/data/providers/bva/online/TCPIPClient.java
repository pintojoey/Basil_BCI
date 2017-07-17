package cz.zcu.kiv.eeg.gtn.data.providers.bva.online;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

/**

 *
 * TCP/IP client connected to the RDA server. The connection is 
 * ensured by using the Socket class and by handling exceptions. Data are
 * processed byte by byte. Cache (linkedlist) is used into which obtained bytes from the
 * server are stored. DataTokenizer is above this class translating obtained
 * bytes into interpretable RDA. Bytes can be obtained using the read() method.
 * read().
 */
public class TCPIPClient extends Thread {

    /**
     * Stream of incoming bytes
     */
    private DataInputStream Sinput;
    private Socket socket;
    private SynchronizedLinkedListByte buffer = new SynchronizedLinkedListByte();
    private static final Logger logger = Logger.getLogger(TCPIPClient.class);
    private boolean isRunning;

    
    public TCPIPClient(String ip, int port) throws Exception {

    	// create a socket and connect it to the server
        try {
            socket = new Socket(ip, port);
        } catch (Exception e) {
            logger.error("Server connection error:" + e);
            throw new Exception(e.getMessage());
        }
        logger.debug("Connection acquired: "
                + socket.getInetAddress() + ":"
                + socket.getPort());

        // create a data stream to read from the socket
        try {
            Sinput = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            logger.error("Input stream error: " + e);
            throw new Exception(e.getMessage());
        }
    }

  
    @Override
    public void run() {
        // read data from the server and store them into the buffer
        Byte response;
        try {
            isRunning = true;
            while (isRunning) {
                try {
                    response = Sinput.readByte();
                    buffer.addLast(response);
                } catch (Exception e) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error while reading from the server: " + e);
        }

        try {
            Sinput.close();
        } catch (Exception e) {
        }
    }
    
    public void requestStop(){
        isRunning = false;
    }

    /**
     * Returns an array of selected size.
     *
     * @param value array size needed
     * @return byte array of selected size
     */
    public byte[] read(int value) {
        byte[] response = new byte[value];
        for (int i = 0; i < value; i++) {
            while (true) {
                if (!buffer.isEmpty()) {
                    try {
                        response[i] = buffer.removeFirst();
                        break;
                    } catch (NoSuchElementException e) {
                        logger.error("Error creating a new input stream: " + e.getMessage());
                    }
                }
            }
        }
        return response;
    }

  
    public boolean hasNext() {
        return buffer.isEmpty();
    }

}
