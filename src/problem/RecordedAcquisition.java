package problem;

/**
 * Class used for representing a candidate download
 * @author cpralet
 *
 */
public class RecordedAcquisition extends Acquisition {

	/** Index of the recorded acquisition in the list of recorded acquisitions of the problem */
	public final int idx;
	/** Satellite associated with this candidate download */
	public final Satellite satellite;	
	/** Time at which the acquisition has been realized (acquisition end time in seconds from the start of the planning horizon) */
	public final double acquisitionTime;		
	/** Volume of the acquisition (in bits) */
	public final long volume;	
	
	/**
	 * Create a candidate download
	 * @param acquisition
	 * @param satellite
	 * @param acquisitionTime
	 * @param volume
	 */
	public RecordedAcquisition(String name, User user, int priority, int idx, Satellite satellite, double acquisitionTime, long volume) {
		super(name,user,priority);
		this.idx = idx;
		this.satellite = satellite;
		this.acquisitionTime = acquisitionTime;		
		this.volume = volume;
	}

	@Override
	public Satellite getSatellite() {
		return satellite;
	}

	@Override
	public double getAcquisitionTime() {
		return acquisitionTime;
	}

	@Override
	public long getVolume() {
		return volume;
	}

}
