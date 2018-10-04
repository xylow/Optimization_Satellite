package problem;

/**
 * Class used for representing a download window, defined by a satellite, a station, and a temporal interval 
 * @author cpralet
 *
 */
public class DownloadWindow {

	/** Satellite associated with this download window */
	public final Satellite satellite;
	/** Station associated with this download window */
	public final Station station;
	/** Start time of this download window */
	public final double start;
	/** End time of this download window */
	public final double end;
	/** Index of this download window in the list of download windows of the problem */
	public final int idx;
	
	/**
	 * Create a download window
	 * @param id 
	 * @param satellite
	 * @param station
	 * @param start
	 * @param end
	 */
	public DownloadWindow(Satellite satellite, Station station, double start, double end, int idxInDownloadWindows){
		this.satellite = satellite;
		this.station = station;
		this.start = start;
		this.end = end;
		this.idx = idxInDownloadWindows; 
	}
	
	@Override
	public String toString(){
		return "DownloadWindow("+satellite.name+", "+station.name+", ["+start+","+end+"])";
	}
}
