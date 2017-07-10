package cz.zcu.kiv.eeg.gtn.data.providers.online.bva;

import java.util.LinkedList;

/**
 * 
 *
 * Thread-safe linked list, used for the buffered data.
 * Only used methods are overriden.
 */
public class SynchronizedLinkedListByte extends LinkedList<Byte> {

    private static final long serialVersionUID = 1L;

    @Override
    public synchronized void addLast(Byte b) {
        super.add(b);
    }

    @Override
    public synchronized Byte removeFirst() {
        return super.removeFirst();
    }

    @Override
    public synchronized boolean isEmpty() {
        return super.isEmpty();
    }

}
