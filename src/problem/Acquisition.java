package problem;

/**
 * Class used for representing acquisition 
 * (both acquisitions already recorded onboard and candidate acquisitions) 
 * @author cpralet
 *
 */
public abstract class Acquisition {

	/** Name of the acquisition */
	public final String name;
	/** User associated with the acquisition */
	public final User user;
	/** Priority level associated with acquisition */
	public final int priority;
	
	/** Download window selected for this acquisition (value null if the acquisition is not downloaded) */
	public DownloadWindow selectedDownloadWindow;
	/** Start time of the download (if any) */
	public double selectedDownloadStartTime;
	/** End time of the download (if any) */
	public double selectedDownloadEndTime;
	 
	
	/**
	 * Create an acquisition
	 * @param name name of the acquisition
	 * @param user user associated with the acquisition
	 * @param priority priority associated with the acquisition
	 */
	public Acquisition(String name, User user, int priority){
		this.name = name;
		this.priority = priority;
		this.user = user;
	}

	/**
	 * 
	 * @return the satellite used for realizing this acquisition (if any)
	 */
	public abstract Satellite getSatellite();
	
	/**
	 * 
	 * @return the time at which the acquisition ends, if it is realized (in seconds from the start time of the planning horizon)
	 */
	public abstract double getAcquisitionTime();
	
	/**
	 * 
	 * @return the volume (in bits) associated with the acquisition
	 */
	public abstract long getVolume();
	
	
}
