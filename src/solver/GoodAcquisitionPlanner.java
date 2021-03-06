package solver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import params.Params;
import problem.AcquisitionWindow;
import problem.CandidateAcquisition;
import problem.PlanningProblem;
import problem.ProblemParserXML;
import problem.Satellite;

/**
 * Acquisition planner which solves the acquisition problem for each satellite separately,
 * and which only tries to maximize the number of acquisitions realized. To do this, this
 * planner generates OPL data files.
 * @author cpralet
 *
 */
public class GoodAcquisitionPlanner {

	/**
	 * Write a .dat file which represents the acquisition planning problem for a particular satellite
	 * @param pb planning problem
	 * @param satellite satellite for which the acquisition plan must be built
	 * @param datFilename name of the .dat file generated
	 * @param solutionFilename name of the file in which CPLEX solution will be written
	 * @throws IOException
	 */
	public static void writeDatFile(PlanningProblem pb, 
			String datFilename, String solutionFilename) throws IOException{
		// generate OPL data (only for the satellite selected)
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(datFilename, false)));

		// get all acquisition windows involved in the problem
		List<AcquisitionWindow> acquisitionWindows = new ArrayList<AcquisitionWindow>();
		for(CandidateAcquisition a : pb.candidateAcquisitions){
			for(AcquisitionWindow w : a.acquisitionWindows){
				acquisitionWindows.add(w);							// Adding every ACQ window
			}
		}			

		// write the number of acquisition windows
		int nAcquisitionWindows = acquisitionWindows.size();
		writer.write("NacquisitionWindows = " + nAcquisitionWindows + ";");
		
		// write the total number of candidate acquisitions
				int nCandidateAcquisitions = pb.candidateAcquisitions.size();
				writer.write("\nNcandidates = " + nCandidateAcquisitions + ";");
		
		// write the number of satellites in the problem
//				int nSatellites = pb.satellites.size();
//				writer.write("Nsatellites = " + nSatellites + ";");

		// write the index of each acquisition
		writer.write("\nCandidateAcquisitionIdx = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).candidateAcquisition.idx);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).candidateAcquisition.idx);
			}
		}
		writer.write("];");

//		// write the Idx of the candidate acquisition associated to each acquisition window
//		writer.write("\nAcqWindCandAcqIdx = [");
//		if(!acquisitionWindows.isEmpty()){
//			writer.write(""+acquisitionWindows.get(0).candidateAcquisition.idx);
//			for(int i=1;i<nAcquisitionWindows;i++){
//				writer.write(","+acquisitionWindows.get(i).candidateAcquisition.idx);
//			}
//		}
//		writer.write("];");
		
		// write the cost of each acquisition
		writer.write("\nCostFunc = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).Cost);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).Cost);
			}
		}
		writer.write("];");
		
		// write the priority of each acquisition
		writer.write("\nCandidateAcquisitionPri = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).candidateAcquisition.priority);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).candidateAcquisition.priority);
			}
		}
		writer.write("];");

		// write the index of each acquisition window
		writer.write("\nAcquisitionWindowIdx = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).idx);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).idx);
			}
		}
		writer.write("];");
		
		// write the satellite index linked with each acquisition window
				writer.write("\nSatelliteIdx = [");
				if(!acquisitionWindows.isEmpty()){
					writer.write(""+1);			// Dummy satellite 1
					writer.write(","+2);		// Dummy satellite 2
					writer.write(","+acquisitionWindows.get(0).satellite.idx);
					for(int i=1;i<nAcquisitionWindows;i++){
						writer.write(","+acquisitionWindows.get(i).satellite.idx);
					}
				}
				writer.write("];");

		// write the earliest acquisition start time associated with each acquisition window
		writer.write("\nEarliestStartTime = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).earliestStart);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).earliestStart);
			}
		}
		writer.write("];");

		// write the latest acquisition start time associated with each acquisition window
		writer.write("\nLatestStartTime = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).latestStart);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).latestStart);
			}
		}
		writer.write("];");

		// write the duration of acquisitions in each acquisition window
		writer.write("\nDuration = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).duration);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).duration);
			}
		}
		writer.write("];");

		// write the cloud probability of acquisitions in each acquisition window
		writer.write("\ncloudProba = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).cloudProba);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).cloudProba);
			}
		}
		writer.write("];");

		// write the zenith-angle of acquisitions in each acquisition window
		writer.write("\nZenangle = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).zenithAngle);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).zenithAngle);
			}
		}
		writer.write("];");
		
		// write the roll angle of acquisitions in each acquisition window
		writer.write("\nRollangle = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).rollAngle);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).rollAngle);
			}
		}
		writer.write("];");
		
		// write the volume of acquisitions in each acquisition window
		writer.write("\nVolume = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).volume);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).volume);
			}
		}
		writer.write("];");
		
		// write the quota of the user of acquisitions in each acquisition window
		writer.write("\nCandidateAcquisitionQuota = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).candidateAcquisition.user.quota);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).candidateAcquisition.user.quota);
			}
		}
		writer.write("];");
		
		// write the transition times between acquisitions in acquisition windows
		writer.write("\nTransitionTimes = [");
		for(int i=0;i<nAcquisitionWindows;i++){
			AcquisitionWindow a1 = acquisitionWindows.get(i);
			if(i != 0) writer.write(",");
			writer.write("\n\t[");
			for(int j=0;j<nAcquisitionWindows;j++){
				if(j != 0) writer.write(",");
				writer.write(""+pb.getTransitionTime(a1, acquisitionWindows.get(j)));
			}	
			writer.write("]");
		}
		writer.write("\n];");



		// write the quota of the user
		//				writer.write("\nQuotas = [");
		//				for(int i=0;i<nUsers;i++){
		//					AcquisitionWindow a1 = acquisitionWindows.get(i);
		//					if(i != 0) writer.write(",");
		//					writer.write("\n\t[");
		//					for(int j=0;j<nAcquisitionWindows;j++){
		//						if(j != 0) writer.write(",");
		//						writer.write(""+pb.getTransitionTime(a1, acquisitionWindows.get(j)));
		//					}	
		//					writer.write("]");
		//				}
		//				writer.write("\n];");



		// write the quota of the user
		//				writer.write("\nQuotas = [");
		//				for(int i=0;i<nUsers;i++){
		//					AcquisitionWindow a1 = acquisitionWindows.get(i);
		//					if(i != 0) writer.write(",");
		//					writer.write("\n\t[");
		//					for(int j=0;j<nAcquisitionWindows;j++){
		//						if(j != 0) writer.write(",");
		//						writer.write(""+pb.getTransitionTime(a1, acquisitionWindows.get(j)));
		//					}	
		//					writer.write("]");
		//				}
		//				writer.write("\n];");

		// write the name of the file in which the result will be written
		writer.write("\nOutputFile = \"" + solutionFilename + "\";");

		// close the writer
		writer.flush();
		writer.close();		
	}

	public static void main(String[] args) throws XMLStreamException, FactoryConfigurationError, IOException{
		ProblemParserXML parser = new ProblemParserXML(); 
		PlanningProblem pb = parser.read(Params.systemDataFile,Params.planningDataFile);
		pb.printStatistics();
		String datFilename = "output/GoodAcqPlanning.dat";
		String solutionFilename = "GoodSolutionAcqPlan.txt";
		writeDatFile(pb, datFilename, solutionFilename);
	}

}
