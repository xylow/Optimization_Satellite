package problem;

/**
 * Class used for representing an acquisition window
 * @author cpralet
 *
 */
public class AcquisitionWindow {

	/** Index of the acquisition window in the list of windows associated with the same candidate acquisition */
	public final int idx;	
	/** Candidate acquisition associated with this acquisition window */
	public final CandidateAcquisition candidateAcquisition;
	/** Satellite associated with this acquisition window */
	public final Satellite satellite;	
	/** Earliest start time of this acquisition window (in seconds from the start time of the planning horizon) */
	public final double earliestStart;
	/** Latest start time of this acquisition window (in seconds from the start time of the planning horizon) */
	public final double latestStart;
	/** Acquisition duration if the acquisition is realized in this window (in seconds) */
	public final double duration;
	/** Volume of the acquisition if it is realized in this window (in bits) */
	public final long volume;
	/** Zenith angle, in radians (zenith-satellite angle viewed from the Earth target concerned by the acquisition and when the satellite is as near as possible from the target) */
	public final double zenithAngle;
	/** Roll angle, in radians (roll angle when the satellite is as near as possible from the target) */
	public final double rollAngle;
	/** Probability of the presence of clouds if acquisition if performed in this window */
	public final double cloudProba;
	
	/**
	 * Create an acquisition opportunity
	 * @param id
	 * @param candidateAcquisition
	 * @param satellite
	 * @param earliestStart
	 * @param latestStart
	 * @param duration
	 * @param zenithAngle
	 * @param rollAngle
	 * @param cloudProba
	 * @param volume
	 */
	public AcquisitionWindow(int idxInAcquisitionWindows, CandidateAcquisition candidateAcquisition, Satellite satellite,  
			double earliestStart, double latestStart, double duration, double zenithAngle, double rollAngle, double cloudProba, long volume) {
		this.idx = idxInAcquisitionWindows;
		this.candidateAcquisition = candidateAcquisition;
		this.satellite = satellite;
		this.earliestStart = earliestStart;
		this.latestStart = latestStart;		
		this.duration = duration;
		this.zenithAngle = zenithAngle;
		this.rollAngle = rollAngle;
		this.cloudProba = cloudProba;
		this.volume = volume;
	}
	
	@Override
	public String toString(){
		return "AcqOpportunity("+candidateAcquisition.name+", " + satellite.name + ", [" + earliestStart+","+latestStart+"])";
	}
	
}
