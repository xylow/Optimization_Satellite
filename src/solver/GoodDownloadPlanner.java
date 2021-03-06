package solver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import params.Params;
import problem.Acquisition;
import problem.AcquisitionWindow;
import problem.CandidateAcquisition;
import problem.DownloadWindow;
import problem.PlanningProblem;
import problem.RecordedAcquisition;
import problem.ProblemParserXML;
import problem.Satellite;

/**
 * Class implementing a download planner which tries to insert downloads into the plan
 * by ordering acquisitions following an increasing order of their realization time, and by
 * considering download windows chronologically 
 * @author cpralet
 *
 */
public class GoodDownloadPlanner {
	
	public static void writeDatFile(SolutionPlan plan, Satellite satellite, 
			String datFilename, String solutionFilename) throws IOException{
		
		// Preparing data for the .DAT file
		PlanningProblem pb = plan.pb;
		List<CandidateAcquisition> acqPlan = plan.plannedAcquisitions;

		
		boolean firstLine = true;

		// plan downloads for each satellite independently (possible due to the configuration of the constellation)
			int NumberofRecorded = 0;
			// get all recorded acquisitions associated with this satellite
			List<Acquisition> candidateDownloads = new ArrayList<Acquisition>();
			for(RecordedAcquisition dl : pb.recordedAcquisitions){
				if(dl.satellite == satellite) {
					candidateDownloads.add(dl);
					NumberofRecorded = NumberofRecorded + 1;
				}
					
			}
			// get all planned acquisitions associated with this satellite
			for(CandidateAcquisition a : acqPlan){
				if(a.selectedAcquisitionWindow.satellite == satellite)
					candidateDownloads.add(a);
			}
			// sort acquisitions by increasing start time
			Collections.sort(candidateDownloads, new Comparator<Acquisition>(){
				@Override
				public int compare(Acquisition a0, Acquisition a1) {
					double start0 = a0.getAcquisitionTime(); 
					double start1 = a1.getAcquisitionTime();
					if(start0 < start1)
						return -1;
					if(start0 > start1)
						return 1;
					return 0;
				}

			});

			// sort download windows by increasing start time
			List<DownloadWindow> downloadWindows = new ArrayList<DownloadWindow>();
			for(DownloadWindow w : pb.downloadWindows){
				if(w.satellite == satellite)
					downloadWindows.add(w);
			}
			Collections.sort(downloadWindows, new Comparator<DownloadWindow>(){
				@Override
				public int compare(DownloadWindow a0, DownloadWindow a1) {
					double start0 = a0.start; 
					double start1 = a1.start;
					if(start0 < start1)
						return -1;
					if(start0 > start1)
						return 1;
					return 0;
				}
			});			

			// chronological traversal of all download windows combined with a chronological traversal of acquisitions which are candidate for being downloaded
			int currentDownloadWindowIdx = 0;
			DownloadWindow currentWindow = downloadWindows.get(currentDownloadWindowIdx);
			double currentTime = currentWindow.start;
			for(Acquisition a : candidateDownloads){
				currentTime = Math.max(currentTime, a.getAcquisitionTime());
				double dlDuration = a.getVolume() / Params.downlinkRate;
				while(currentTime + dlDuration > currentWindow.end){					
					currentDownloadWindowIdx++;
					if(currentDownloadWindowIdx < downloadWindows.size()){
						currentWindow = downloadWindows.get(currentDownloadWindowIdx);
						currentTime = Math.max(currentTime, currentWindow.start);
					}
					else
						break;
				}
				
				if(currentDownloadWindowIdx >= downloadWindows.size())
					break;

				if(firstLine){
					firstLine = false;
				}
				currentTime += dlDuration;
			}
		

		// generate OPL data (only for the satellite selected)
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(datFilename, false)));

		// get all download windows involved in the problem
//		List<DownloadWindow> downloadWindows = new ArrayList<DownloadWindow>();
//		for(CandidateAcquisition a : pb.candidateAcquisitions){
//			for(AcquisitionWindow w : a.acquisitionWindows){
//				if(w.satellite == satellite){
//					acquisitionWindows.add(w);
//				}
//			}
//		}			

		// write the number of acquisition windows
		writer.write("TotalMissionTime = " + pb.horizonEnd + ";");

		// write the number of acquisition windows
		int nDownloadWindows = downloadWindows.size();
		writer.write("\nNdownloadWindows = " + nDownloadWindows + ";");

		int nCandidateDownloads = candidateDownloads.size();
		writer.write("\nNcandidates = " + nCandidateDownloads + ";");
		
		// Writes the number of total recorded candidates
		writer.write("\nNRECcandidates = " + NumberofRecorded + ";");

		// write the index of each download window
		writer.write("\nDownloadWindowIdx = [");
		if(!downloadWindows.isEmpty()){
			writer.write(""+downloadWindows.get(0).idx);
			for(int i=1;i<nDownloadWindows;i++){
				writer.write(","+downloadWindows.get(i).idx);
			}
		}
		writer.write("];");

		// write the index of each candidate download
		writer.write("\nCandidateDownloadIdx = [");
		if(!candidateDownloads.isEmpty()){
			writer.write(""+candidateDownloads.get(0).getIdx());
			for(int i=1;i<nCandidateDownloads;i++){
				writer.write(","+candidateDownloads.get(i).getIdx());
			}
		}
		writer.write("];");
		
		// write the cost of each acquisition
		writer.write("\nCostFunc = [");
		if(!candidateDownloads.isEmpty()){
			writer.write(""+candidateDownloads.get(0).DownloadCost);
			for(int i=1;i<nCandidateDownloads;i++){
				writer.write(","+candidateDownloads.get(i).DownloadCost);
			}
		}
		writer.write("];");
		
		// write the ending time of the acquisition of each candidate download
		writer.write("\nEarliestStartTime = [");
		if(!candidateDownloads.isEmpty()){
			writer.write(""+candidateDownloads.get(0).getAcquisitionTime());
			for(int i=1;i<nCandidateDownloads;i++){
				writer.write(","+candidateDownloads.get(i).getAcquisitionTime());
			}
		}
		writer.write("];");

		// write the start time of each download window
		writer.write("\nWindowEndTime = [");
		if(!downloadWindows.isEmpty()){
			writer.write(""+downloadWindows.get(0).end);
			for(int i=1;i<nDownloadWindows;i++){
				writer.write(","+downloadWindows.get(i).end);
			}
		}
		writer.write("];");

		// write the end time of each download window
		writer.write("\nWindowStartTime = [");
		if(!downloadWindows.isEmpty()){
			writer.write(""+downloadWindows.get(0).start);
			for(int i=1;i<nDownloadWindows;i++){
				writer.write(","+downloadWindows.get(i).start);
			}
		}
		writer.write("];");

		// write the DownloadTime of each acquisition
		writer.write("\nDuration = [");
		if(!candidateDownloads.isEmpty()){
			writer.write(""+candidateDownloads.get(0).getVolume() / Params.downlinkRate);
			for(int i=1;i<nCandidateDownloads;i++){
				writer.write(","+candidateDownloads.get(i).getVolume() / Params.downlinkRate);
			}
		}
		writer.write("];");

		// write the name of the file in which the result will be written
		writer.write("\nOutputFile = \"" + solutionFilename + "\";");

		// close the writer
		writer.flush();
		writer.close();		
	}
	
	public static void main(String[] args) throws XMLStreamException, FactoryConfigurationError, IOException{
		ProblemParserXML parser = new ProblemParserXML(); 
		PlanningProblem pb = parser.read(Params.systemDataFile,Params.planningDataFile);
		SolutionPlan plan = new SolutionPlan(pb);
		plan.readAcquisitionPlan("output/GoodSolutionAcqPlan.txt");
		pb.printStatistics();
		for(Satellite satellite : pb.satellites){
			String datFilename = "output/DLPlanning_"+satellite.name+".dat";
			String solutionFilename = "solutionDLPlan_"+satellite.name+".txt";
			writeDatFile(plan, satellite, datFilename, solutionFilename);
		}
	}
	
}
