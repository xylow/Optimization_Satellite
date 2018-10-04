package solver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import problem.Acquisition;
import problem.CandidateAcquisition;
import problem.PlanningProblem;

/**
 * Class used for representing a solution plan (describing both the acquisition plan and the download plan)
 * @author cpralet
 *
 */
public class SolutionPlan {

	/** Problem for which the solution plan is built */
	public final PlanningProblem pb;
	/** List of all candidate acquisitions which are realized in the plan */
	public final List<CandidateAcquisition> plannedAcquisitions;
	/** List of all acquisitions which are downloaded in the plan */
	public final List<Acquisition> plannedDownload;

	/**
	 * Build a solution plan
	 * @param pb
	 */
	public SolutionPlan(PlanningProblem pb){
		this.pb = pb;
		plannedAcquisitions = new ArrayList<CandidateAcquisition>();
		plannedDownload = new ArrayList<Acquisition>();
	}

	/**
	 * Add to the acquisition plan all acquisitions which are selected according to the input file
	 * @param filename
	 * @throws IOException
	 */
	public void readAcquisitionPlan(String filename) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String[] tab = new String[4];
		String currentLine = br.readLine();
		while(currentLine != null){
			tab = currentLine.trim().split(" ");
			CandidateAcquisition a = pb.getCandidateAcquisition(Integer.parseInt(tab[0]));
			if(a.selectedAcquisitionWindow == null){ // acquisition not planned yet
				a.selectedAcquisitionWindow = a.getAcquisitionWindow(Integer.parseInt(tab[1]));
				a.selectedAcquisitionStartTime = Double.parseDouble(tab[2]);
				a.selectedAcquisitionEndTime = Double.parseDouble(tab[3]);
				plannedAcquisitions.add(a);				
			}
			currentLine = br.readLine();
		}		
		br.close();		
	}

	/**
	 * Add to the download plan all downloads which are selected according to the input file
	 * @param filename
	 * @throws IOException
	 */
	public void readDownloadPlan(String filename) throws IOException{	
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String[] tab = new String[4];
		String currentLine = br.readLine();
		while(currentLine != null){
			tab = currentLine.trim().split(" ");
			boolean recorded = tab[0].equals("REC");  
			int idx = Integer.parseInt(tab[1]);
			Acquisition a = recorded ? pb.getRecordedAcquisition(idx) : pb.getCandidateAcquisition(idx);
			a.selectedDownloadWindow = pb.getDownloadWindow(Integer.parseInt(tab[2]));
			a.selectedDownloadStartTime = Double.parseDouble(tab[3]);
			a.selectedDownloadEndTime = Double.parseDouble(tab[4]);			
			plannedDownload.add(a);				
			currentLine = br.readLine();
		}		
		br.close();	
	}

}
