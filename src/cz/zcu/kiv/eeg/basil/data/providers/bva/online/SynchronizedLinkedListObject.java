package cz.zcu.kiv.eeg.basil.data.providers.bva.online;

import java.util.LinkedList;

/**

 * 
 * Thread-safe linked list, used as a buffer for RDA object.
 * Only used methods are overriden.
 */

public class SynchronizedLinkedListObject extends LinkedList<Object> {
	private static final long serialVersionUID = 1L;

        @Override
	public synchronized void addLast(Object o){
		super.add(o);
	}
	
        @Override
	public synchronized Object removeFirst(){
		return super.removeFirst();
	}
	
        @Override
	public synchronized boolean isEmpty(){
		return super.isEmpty();
	}

}
