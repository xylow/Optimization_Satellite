package problem;

/**
 * Class used for representing a ground station used for download activities
 * @author cpralet
 *
 */
public class Station {

	/** Name of this station */
	public final String name;
	
	/**
	 * Create a ground station
	 * @param name
	 */
	public Station(String name){
		this.name = name;
	}
		
	@Override
	public String toString(){
		return "Station("+name+")";
	}
}
