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
public class BadAcquisitionPlanner {

	/**
	 * Write a .dat file which represents the acquisition planning problem for a particular satellite
	 * @param pb planning problem
	 * @param satellite satellite for which the acquisition plan must be built
	 * @param datFilename name of the .dat file generated
	 * @param solutionFilename name of the file in which CPLEX solution will be written
	 * @throws IOException
	 */
	public static void writeDatFile(PlanningProblem pb, Satellite satellite, 
			String datFilename, String solutionFilename) throws IOException{
		// generate OPL data (only for the satellite selected)
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(datFilename, false)));

		// get all acquisition windows involved in the problem
		List<AcquisitionWindow> acquisitionWindows = new ArrayList<AcquisitionWindow>();
		for(CandidateAcquisition a : pb.candidateAcquisitions){
			for(AcquisitionWindow w : a.acquisitionWindows){
				if(w.satellite == satellite){
					acquisitionWindows.add(w);
				}
			}
		}			

		// write the number of acquisition windows
		int nAcquisitionWindows = acquisitionWindows.size();
		writer.write("NacquisitionWindows = " + nAcquisitionWindows + ";");

		// write the index of each acquisition
		writer.write("\nCandidateAcquisitionIdx = [");
		if(!acquisitionWindows.isEmpty()){
			writer.write(""+acquisitionWindows.get(0).candidateAcquisition.idx);
			for(int i=1;i<nAcquisitionWindows;i++){
				writer.write(","+acquisitionWindows.get(i).candidateAcquisition.idx);
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
		for(Satellite satellite : pb.satellites){
			String datFilename = "output/acqPlanning_"+satellite.name+".dat";
			String solutionFilename = "solutionAcqPlan_"+satellite.name+".txt";
			writeDatFile(pb, satellite, datFilename, solutionFilename);
		}
	}

}
