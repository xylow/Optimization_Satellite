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
public class BadDownloadPlanner {

	
	public static void planDownloads(SolutionPlan plan, String solutionFilename) throws IOException{

		PlanningProblem pb = plan.pb;
		List<CandidateAcquisition> acqPlan = plan.plannedAcquisitions;

		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(solutionFilename, false)));

		boolean firstLine = true;

		// plan downloads for each satellite independently (possible due to the configuration of the constellation)
		for(Satellite satellite : pb.satellites){
			// get all recorded acquisitions associated with this satellite
			List<Acquisition> candidateDownloads = new ArrayList<Acquisition>();
			for(RecordedAcquisition dl : pb.recordedAcquisitions){
				if(dl.satellite == satellite)
					candidateDownloads.add(dl);
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
			if(downloadWindows.isEmpty())
				continue;

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
				else 
					writer.write("\n");
				if(a instanceof RecordedAcquisition)
					writer.write("REC " + ((RecordedAcquisition) a).idx + " " + currentWindow.idx + " " + currentTime + " " + (currentTime+dlDuration));
				else // case CandidateAcquisition
					writer.write("CAND " + ((CandidateAcquisition) a).idx + " " + currentWindow.idx + " " + currentTime + " " + (currentTime+dlDuration));
				currentTime += dlDuration;
			}
		}
		writer.flush();
		writer.close();
	}

	
	public static void main(String[] args) throws XMLStreamException, FactoryConfigurationError, IOException, ParseException{
		ProblemParserXML parser = new ProblemParserXML(); 
		PlanningProblem pb = parser.read(Params.systemDataFile,Params.planningDataFile);
		SolutionPlan plan = new SolutionPlan(pb);
		plan.readAcquisitionPlan("output/solutionAcqPlan_SAT1.txt");
		plan.readAcquisitionPlan("output/solutionAcqPlan_SAT2.txt");
		planDownloads(plan,"output/downloadPlan.txt");		
	}
	
}
