package cz.zcu.kiv.eeg.gtn.data.providers.messaging;

/**
 * Created by Tomas Prokop on 17.07.2017.
 * 
 * Represents one EEG event marker that is defined by
 * name (such as 'S  1', offset in samples relative to the beginning
 * of the data in a package / buffer) and isTarget that 
 * can be used to evaluate the class label e.g. in P300
 * experiments
 * 
 */
public class EEGMarker {

    private final String name;

    private int offset;

    private int expectedClass;

    public EEGMarker(String name, int offset) {
        this.name = name;
        this.offset = offset;
    }

    public int getExpectedClass() {
        return expectedClass;
    }

    public void setExpectedClass(int expectedClass) {
        this.expectedClass = expectedClass;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }
    
    @Override
    public String toString() {
    	return name + ", offset: " + offset;
    }

	public void incrementOffset(int length) {
		this.offset += length;
	}

	public void decrementOffset(int length) {
		this.offset = this.offset - length;
	}
}
