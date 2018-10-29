/** Number of download opportunities */
float TotalMissionTime = ...;
int NdownloadWindows = ...;
int Ncandidates = ...;
int NRECcandidates = ...;
/** Download range */
range DownloadWindows = 1..NdownloadWindows;
//range DownloadWindowsPlusZero = 0..NdownloadWindows;
range DownloadCandidates = 1..Ncandidates;
range DownloadCandidatesPlusZero = 0..Ncandidates;

/** Index of the download window */
int DownloadWindowIdx[DownloadWindows] = ...;
/** Index of the download  */
int CandidateDownloadIdx[DownloadCandidates] = ...;

/** "Cost" of each download */
float CostFunc[DownloadCandidates] = ...;

///** Candidate download quota */
//float CandidateDownloadQuota[DownloadCandidates] = ...;
///** Candidate download priority */
//int CandidateDowloadPri[DownloadCandidates] = ...;

/** Earliest start time for download candidate */
float EarliestStartTime[DownloadCandidates] = ...;
/** End time for download window */
float WindowEndTime[DownloadWindows] = ...;
/** Start time for download window */
float WindowStartTime[DownloadWindows] = ...;
/** Download duration associated with each acquisition window */
float Duration[DownloadCandidates] = ...;
///** Download candidate propability */
//float cloudProba[DownloadCandidates] = ...;
///** Download candidate zenith angle */
//float Zenangle[DownloadCandidates] = ...;

/** File in which the result will be written */
string OutputFile = ...;

/** Boolean matrix indicating the window allocation of download candidates */
dvar int selectWin[DownloadCandidatesPlusZero][DownloadWindows] in 0..1;
/** next[a1][a2] = 1 when a1 is the selected download candidate that precedes a2 */
dvar int next[DownloadCandidatesPlusZero][DownloadCandidatesPlusZero] in 0..1;
/** Acquisition start time in each acquisition window */
dvar float startTime[a in DownloadCandidates] in EarliestStartTime[a]..(TotalMissionTime-Duration[a]);

dexpr float downsum = sum(c in DownloadCandidates,  w in DownloadWindows) selectWin[c][w];

dexpr float costsum = sum(c in DownloadCandidates, w in DownloadWindows) CostFunc[c]*selectWin[c][w];
/** Boolean variable indicating whether a download candidate is selected */
//dexpr int selectDown[c in DownloadCandidatesPlusZero] = sum(w in DownloadWindowsPlusZero) selectWin[c][w];

execute{
	cplex.tilim = 60*20; // 60 seconds
}

// maximize the value of the download windows selected
maximize costsum;

constraints {


	// default selection of the dummy download windows numbered by 0 and -1 (one for each satellite)
	ct1:selectWin[0][1]==1;
	ct2:forall(w in DownloadWindows: w != 1){
		selectWin[0][w] == 0;
	}
	
	// The same candidate cannot be repeated (for different download windows)
	ct3:forall(c in DownloadCandidatesPlusZero){ 
		sum(w in DownloadWindows) selectWin[c][w] <= 1;			
	}
	
	// Stop real downloads from choosing the dummy download window
	//ct5:sum(c in DownloadCandidatesPlusZero) selectWin[c][0] <= 1;		
	//ct5:forall(c in DownloadCandidates) selectWin[c][0]==0;
	

	
	// A download window is selected if and only if it has a (unique) precedessor and a (unique) 
	// successor in the plan that shares the same satellite
	ct4:forall(c1 in DownloadCandidatesPlusZero){
		sum(c2 in DownloadCandidatesPlusZero : c2 != c1 ) next[c1][c2] == sum(w in DownloadWindows) selectWin[c1][w];
		sum(c2 in DownloadCandidatesPlusZero : c2 != c1 ) next[c2][c1] == sum(w in DownloadWindows) selectWin[c1][w];
//	    sum(c2 in DownloadCandidatesPlusZero : c2 != c1 ) next[c1][c2] == selectDown[c1];
//		sum(c2 in DownloadCandidatesPlusZero : c2 != c1 ) next[c2][c1] == selectDown[c1];
		next[c1][c1] == 0;
	}

	// Window fitting condition --> #TODO: Put selectDown in here, for heuristic
//	ct3:forall(c in DownloadCandidates, w in DownloadWindows){
//		startTime[c] >= (1-selectWin[c][w])*(WindowEndTime[w]-Duration[c]-WindowStartTime[w]) + WindowStartTime[w];
//		startTime[c] <= (1-selectWin[c][w])*(WindowStartTime[w]-WindowEndTime[w] + Duration[c]) + WindowEndTime[w] - Duration[c];
//	}
	ct5:forall(c in DownloadCandidates, w in DownloadWindows){
		startTime[c] >= selectWin[c][w]* WindowStartTime[w];
		startTime[c] <= (1-selectWin[c][w])*(TotalMissionTime - Duration[c]) + selectWin[c][w]*(WindowEndTime[w] - Duration[c]);
	}


	// Temporal separation constraints between successive download candidates (big-M formulation)
	ct6:forall(c1,c2 in DownloadCandidates : c1 != c2 ){
	//	startTime[c1] + Duration[c1] <= startTime[c2] 
	//			+ (1-next[c1][c2])*( (TotalMissionTime - Duration[c1]) + Duration[c1] - EarliestStartTime[c2] );
		startTime[c1] + Duration[c1] <= startTime[c2] + (1-next[c1][c2])*4*TotalMissionTime;
		startTime[c1] >= 0;		
	}

}
execute {
	for(var i=1; i <= Ncandidates; i++){
		writeln(CostFunc[i]*selectWin[i][1]);
		
	}
	writeln("costsum: " + costsum + " #downloads: " + downsum);			
	// Writes the .txt file, that follows the matrix structure
	// (	Candidate Down idx	|	Down start time	| 	Down end time	)
	// Satellite column will probably not be read by java, but can help user to verify results
	var ofile = new IloOplOutputFile(OutputFile);
	for(var i=1; i <= Ncandidates; i++) { 
		for(var j =1; j <= NdownloadWindows; j++){
			if(selectWin[i][j] == 1){
				if(i >= 1 && i <= NRECcandidates)
					ofile.writeln("REC " + CandidateDownloadIdx[i] + " " + DownloadWindowIdx[j] + " " +startTime[i] + " " + (startTime[i]+Duration[i]) );
				else if(i >=1)
					ofile.writeln("CAND " + CandidateDownloadIdx[i] + " " + DownloadWindowIdx[j] + " " +startTime[i] + " " + (startTime[i]+Duration[i]) );	
			}
		}
	}	
}