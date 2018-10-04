package problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import params.Params;

/**
 * Class used for representing data of the planning problem to solve
 * @author cpralet
 *
 */
public class PlanningProblem {

	// SYSTEM DATA
	
	/** All satellites involved in the problem */
	public List<Satellite> satellites;
	/** All stations involved in the problem */
	public List<Station> stations;
	/** All users of the system */
	public List<User> users;
	
	// PLANNING DATA
	
	/** Start time of the planning horizon */
	public double horizonStart;
	/** End time of the planning horizon */
	public double horizonEnd;
	/** All candidate acquisitions of the problem */
	public List<CandidateAcquisition> candidateAcquisitions;
	/** All acquisitions already recorded and waiting for being downloaded */
	public List<RecordedAcquisition> recordedAcquisitions;
	/** All download windows of the problem */
	public List<DownloadWindow> downloadWindows;
	
	
	/**
	 * Creation of a planning problem
	 */
	public PlanningProblem(){
		satellites = new ArrayList<Satellite>();
		stations = new ArrayList<Station>();
		users = new ArrayList<User>();
		candidateAcquisitions = new ArrayList<CandidateAcquisition>();
		recordedAcquisitions = new ArrayList<RecordedAcquisition>();	
		downloadWindows = new ArrayList<DownloadWindow>();
	}

	/**
	 * Set the planning horizon associated with the planning problem 
	 * @param horizonStart
	 * @param horizonEnd
	 */
	public void setHorizon(double horizonStart, double horizonEnd){
		this.horizonStart = horizonStart; 
		this.horizonEnd = horizonEnd;
	}

	/**
	 * Add a user to the problem
	 * @param name
	 * @param quota
	 * @return the user created
	 */
	public User addUser(String name, double quota){
		User user = new User(users.size(),name,quota);
		users.add(user);
		return user;
	}

	/**
	 * Add a satellite to the problem
	 * @param name
	 * @return the satellite created
	 */
	public Satellite addSatellite(String name){
		Satellite satellite = new Satellite(name);
		satellites.add(satellite);
		return satellite;
	}

	/**
	 * Add a station to the problem
	 * @param name
	 * @return the station created
	 */
	public Station addStation(String name){
		Station station = new Station(name);
		stations.add(station);
		return station;
	}

	/**
	 * Add a candidate acquisition to the problem
	 * @param id
	 * @param name
	 * @param user
	 * @param priority
	 * @return the acquisition created
	 */
	public CandidateAcquisition addCandidateAcquisition(String name, User user, int priority){
		CandidateAcquisition r = new CandidateAcquisition(name,user,priority, candidateAcquisitions.size());
		candidateAcquisitions.add(r);
		return r;
	}

	/**
	 * Add an acquisition already recorded onboard the satellite
	 * @param acquisition
	 * @param satellite
	 * @param acquisitionTime
	 * @param volume
	 */
	public void addRecordedAcquisition(int id, String name, User user, int priority, Satellite satellite, double acquisitionTime, long volume){
		recordedAcquisitions.add(new RecordedAcquisition(name,user,priority,recordedAcquisitions.size(),satellite,acquisitionTime,volume));
	}

	/**
	 * Add a download window to the problem
	 * @param id unique identifier of the download window
	 * @param satellite satellite associated with the download window
	 * @param station station associated with the download window
	 * @param start start time of the download window added
	 * @param end end time of the download window added
	 */
	public void addDownloadWindow(Satellite satellite, Station station, double start, double end){
		downloadWindows.add(new DownloadWindow(satellite,station,start,end,downloadWindows.size()));		
	}

	/**
	 * 
	 * @param i
	 * @return the ith candidate acquisition in this problem
	 */
	public CandidateAcquisition getCandidateAcquisition(int i){
		return candidateAcquisitions.get(i);
	}

	/**
	 * 
	 * @param i
	 * @return the ith recorded acquisition in this problem
	 */
	public RecordedAcquisition getRecordedAcquisition(int i){
		return recordedAcquisitions.get(i);
	}

	/**
	 * 
	 * @param i
	 * @return the ith download window in this problem
	 */
	public DownloadWindow getDownloadWindow(int i){
		return downloadWindows.get(i);
	}

	/**
	 * 
	 * @param a1 
	 * @param a2
	 * @return an estimation of the minimum transition time between two acquisitions realized in given windows 
	 */
	public double getTransitionTime(AcquisitionWindow a1, AcquisitionWindow a2){
		return Math.abs(a1.rollAngle - a2.rollAngle) / Params.meanRotationSpeed;		
	}

	@Override
	public String toString(){
		return "Satellites: "+satellites
				+"\nStations: "+stations
				+"\nGlobalRequests"+candidateAcquisitions;
	}

	/**
	 * Print some statistics concerning this planning problem
	 */
	public void printStatistics(){
		System.out.println("nSatellites: " + satellites.size());
		System.out.println("nUser: " + users.size());
		System.out.println("nStations: " + stations.size());
		int[] nRecordedForPrio = new int[2];
		int[] nRecordedForUser = new int[users.size()];
		for(RecordedAcquisition a : recordedAcquisitions){
			nRecordedForPrio[a.priority]++;
			nRecordedForUser[a.user.idx]++;
		}
		System.out.println("nRecordedAcquisitions: " + recordedAcquisitions.size() 
				+ "\n\tDetail by priority: " + Arrays.toString(nRecordedForPrio) 
				+ "\n\tDetail by user: " + Arrays.toString(nRecordedForUser));
		int[] nCandidateForPrio = new int[2];
		int[] nCandidateForUser = new int[users.size()];
		int nAcquisitionWindows = 0;
		for(CandidateAcquisition a : candidateAcquisitions){
			nCandidateForPrio[a.priority]++;
			nCandidateForUser[a.user.idx]++;
			nAcquisitionWindows += a.acquisitionWindows.size();
		}
		System.out.println("nCandidateAcquisitions: " + candidateAcquisitions.size() 
				+ "\n\tDetail by priority: " + Arrays.toString(nCandidateForPrio) 
				+ "\n\tDetail by user: " + Arrays.toString(nCandidateForUser));
		System.out.println("nAcquisitionWindows: " + nAcquisitionWindows); 
		
		System.out.println("nDownloadWindows: " + downloadWindows.size()); 
		
	}
	
}
