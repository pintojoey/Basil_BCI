package cz.zcu.kiv.eeg.gtn.data.processing.structures;

import java.util.ArrayList;
import java.util.List;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;

/**
 * Implementation of the buffer. Can add / remove
 * data to the end / from the beginning of the array.
 * Offsets of markers are updated accordingly.
 * 
 * @author lvareka
 *
 * */
public class Buffer implements IBuffer {
	private volatile double[][] data;
	private volatile List<EEGMarker> markers;
	private final int CAPACITY = 100000;
	private  EEGStartMessage metadata;
	
	public Buffer() {
		this.data = null;
		this.markers = new ArrayList<>();
	}

	@Override
	public synchronized void initialize(EEGStartMessage meta){
        this.metadata = meta;
	}

    @Override
    public EEGStartMessage getMetadata() {
        return  metadata;
    }

    @Override
	public synchronized void add(double[][] newData, List<EEGMarker> markers) {
		this.addMarkers(markers);
		
		if (this.data == null) {
			this.data = newData;
		} else {
			// merge two float arrays by creating a new connected one
			try {
				double [][] mergedData = new double[this.data.length][this.data[0].length + newData[0].length];
				for (int i = 0; i < this.data.length; i++) {
                    System.arraycopy(this.data[i], 0, mergedData[i], 0, this.data[i].length);
                    System.arraycopy(newData[i],   0, mergedData[i], this.data[i].length - 1, newData[i].length);
                }
				this.data = mergedData;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			
	}

	/**
	 * Add new markers - it is necessary to update offsets using
	 * the current data array size
	 * 
	 * @param markers - new markers to add onto the end of the array
	 */
	private void addMarkers(List<EEGMarker> markers) {
		if (this.data != null && this.data.length > 0) {
			for (EEGMarker marker: markers) {
				marker.incrementOffset(this.data[0].length);
			}
		}
		
		this.markers.addAll(markers);
	}

	@Override
	public EEGDataPackage get() {
		return new EEGDataPackage(this.data, this.markers, this.metadata);
	}

	@Override
	public synchronized EEGDataPackage getAndRemove(int size) {
		double[][] toRemove = new double[this.data.length][size];
		double[][] newData  = new double[this.data.length][this.data[0].length - size]; 
		for (int i = 0; i < this.data.length; i++) {
			System.arraycopy(this.data[i], 0,    toRemove[i], 0, size);
			System.arraycopy(this.data[i], size, newData[i], 0, this.data[0].length - size);
		}

		List<EEGMarker> markersToRemove = this.removeMarkers(size);
		this.data = newData;
		return new EEGDataPackage(toRemove, markersToRemove, this.metadata);
	}

	private List<EEGMarker> removeMarkers(int size) {
		List<EEGMarker> toRemove = new ArrayList<>();
		
		for (EEGMarker marker : this.markers) {
			if (marker.getOffset() < size) {
				toRemove.add(marker);
			} else {
				marker.decrementOffset(size);
			}
		}
		this.markers.removeAll(toRemove);
		
		return toRemove;
	}

	@Override
	public List<EEGMarker> getMarkers() {
		return this.markers;
	}

	@Override
	public synchronized List<EEGMarker> getAndRemoveMarkers(int count) {
		List<EEGMarker> markersToRemove = this.markers.subList(0, count);
		this.markers.removeAll(markersToRemove);
		return markersToRemove;
	}

	@Override
	public int size() {
		if (this.data == null || this.data.length == 0)
			return 0;
		else return this.data[0].length;
	}

	@Override
	public int getCapacity() {
		return CAPACITY;
	}

	@Override
	public int getMarkersSize() {
		return this.markers.size();
	}

	@Override
	public boolean isFull() {
		return size() >= getCapacity();
	}

	@Override
	public synchronized void clear() {
		this.data = null;
		this.markers = new ArrayList<>();
		System.gc();
	}
}
