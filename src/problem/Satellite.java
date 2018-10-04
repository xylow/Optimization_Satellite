package problem;

/**
 * Class used for representing a satellite
 * @author cpralet
 *
 */
public class Satellite {

	/** Name of the satellite */
	public final String name;
	
	public final int idx;
	/**
	 * Create a satellite
	 * @param name
	 */
	public Satellite(String name){
		this.name = name;
		this.idx = Integer.parseInt(name.substring(name.length() - 1));
	}
	
	@Override
	public String toString(){
		return name;
	}
}
