package problem;

/**
 * Class used for representing a satellite
 * @author cpralet
 *
 */
public class Satellite {

	/** Name of the satellite */
	public final String name;

	/**
	 * Create a satellite
	 * @param name
	 */
	public Satellite(String name){
		this.name = name;
	}
	
	@Override
	public String toString(){
		return name;
	}
}
