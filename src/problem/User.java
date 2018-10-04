package problem;

/**
 * Class used for representing a user
 * @author cpralet
 *
 */
public class User {

	/** Index of the user in the list of users */
	public final int idx;
	/** Name of the user */
	public final String name;
	/** Usage quota associated with the user */
	public final double quota;
	
	/**
	 * Create a user
	 * @param name
	 * @param quota
	 */
	public User(int idx, String name, double quota){
		this.idx = idx;
		this.name = name;
		this.quota = quota;
	}
	
	@Override
	public String toString(){
		return name;
	}
	
}
