package params;

public class Params {

	/** File containing a description of all static data (satellites, users, stations) */
	public final static String systemDataFile = "data/system_data.xml";
	/** File containing a description of all dynamic data (candidate acquisitions, recorded acquisitions...) */
	public final static String planningDataFile = "data/planning_data_04h_simplezones.xml";
//	public final static String planningDataFile = "data/planning_data_04h_complexzones.xml";
//	public final static String planningDataFile = "data/planning_data_12h_simplezones.xml";
//	public final static String planningDataFile = "data/planning_data_12h_complexzones.xml";
//	public final static String planningDataFile = "data/planning_data_24h_simplezones.xml";
//	public final static String planningDataFile = "data/planning_data_24h_complexzones.xml";
	/** Approximation of the rotation speed of the satellite (in radians per second) */
	public final static double meanRotationSpeed = (2*Math.PI)/180; // 2 degrees per second
	/** Rate associated with data downlink to ground stations (in bits per second) */
	public final static double downlinkRate = 1E6;
	
}
