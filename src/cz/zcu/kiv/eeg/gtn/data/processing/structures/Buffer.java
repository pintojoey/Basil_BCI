package cz.zcu.kiv.eeg.gtn.data.processing.structures;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;

public class Buffer implements IBuffer {
	private volatile double[][] data;
	private volatile List<EEGMarker> markers;
	private final int CAPACITY = 100000;
	
	public Buffer() {
		this.data = null;
		this.markers = new ArrayList<EEGMarker>();
	}
	
	@Override
	public synchronized void add(double[][] newData, List<EEGMarker> markers) {
		if (this.data == null) {
			this.data = newData;
		} else {
			// merge two float arrays by creating a new connected one
			double [][] mergedData = new double[this.data.length][this.data[0].length + newData[0].length];
			for (int i = 0; i < this.data.length; i++) {
				System.arraycopy(this.data[i], 0, mergedData, 0, this.data[i].length);
				System.arraycopy(newData[i],   0, mergedData, this.data[i].length, mergedData[i].length);
			}
			this.data = mergedData;
		}
		this.markers.addAll(markers);
		
	}

	
	@Override
	public EEGDataPackage get() {
		return new EEGDataPackage(this.data, this.markers);
	}

	@Override
	public EEGDataPackage getAndRemove(int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EEGMarker> getMarkers() {
		return this.markers;
	}

	@Override
	public List<EEGMarker> getAndRemoveMarkers(int count) {
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
		this.markers = new ArrayList<EEGMarker>();
	}

	

}
