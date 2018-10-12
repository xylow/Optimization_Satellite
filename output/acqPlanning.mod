/** Number of acquisition opportunities */
int NacquisitionWindows = ...;
/** Acquisition range */
range AcquisitionWindows = 1..NacquisitionWindows;
range AcquisitionWindowsPlusZero = 0..NacquisitionWindows;
range AcquisitionWindowsExt = -1..NacquisitionWindows;
//int a[i in -1..0] = i+2;
//range Satints = 1..2;
//int InitialSats[Satints];
//InitialSats[1] = 1;

/** Index of the acquisition in the list of candidate acquisitions of the problem */
int CandidateAcquisitionIdx[AcquisitionWindows] = ...;
/** Index of the acquisition window in the list of windows associated with the same candidate acquisition */
int AcquisitionWindowIdx[AcquisitionWindows] = ...;
/** Index of the satellites linked with each acq window in the list of windows associated with the same candidate acquisition */
int SatelliteIdx[AcquisitionWindowsExt] = ...;
//int SatelliteIdxExt[AcquisitionWindowsExt]; 
//SatelliteIdxExt = append(a, SatelliteIdx);
//int SatelliteIdxExt[AcquisitionWindowsExt] = a union SatelliteIdx;


/** Earliest start time associated with each acquisition window */
float EarliestStartTime[AcquisitionWindows] = ...;
/** Latest start time associated with each acquisition window */
float LatestStartTime[AcquisitionWindows] = ...;
/** Acquisition duration associated with each acquisition window */
float Duration[AcquisitionWindows] = ...;

/** Required transition time between each pair of successive acquisitions windows */
float TransitionTimes[AcquisitionWindows][AcquisitionWindows] = ...;

/** File in which the result will be written */
string OutputFile = ...;

/** Boolean variable indicating whether an acquisition window is selected */
dvar int selectAcq[AcquisitionWindowsExt] in 0..1;
/** next[a1][a2] = 1 when a1 is the selected acquisition window that precedes a2 */
dvar int next[AcquisitionWindowsExt][AcquisitionWindowsExt] in 0..1;
/** Acquisition start time in each acquisition window */
dvar float+ startTime[a in AcquisitionWindows] in EarliestStartTime[a]..LatestStartTime[a];

execute{
	cplex.tilim = 60; // 60 seconds
}

// maximize the number of acquisition windows selected
maximize sum(a in AcquisitionWindows) selectAcq[a];

constraints {
	
	// default selection of the dummy acquisition window numbered by 0 and -1
	selectAcq[-1] 	== 1;
	selectAcq[0] 	== 1;
	
	
	// Acquisitions that do not share the same satellite cannot be linked
	forall(a1,a2 in AcquisitionWindows : SatelliteIdx[a1] != SatelliteIdx[a2]){
		next[a1][a2] == 0;
		next[a2][a1] == 0;
	} 
	
	// An acquisition window is selected if and only if it has a (unique) precedessor and a (unique) 
	// successor in the plan that shares the same satellite
	forall(a1 in AcquisitionWindowsPlusZero){
		sum(a2 in AcquisitionWindowsPlusZero : a2 != a1 && SatelliteIdx[a1] == SatelliteIdx[a2]) next[a1][a2] == selectAcq[a1];
		sum(a2 in AcquisitionWindowsPlusZero : a2 != a1 && SatelliteIdx[a1] == SatelliteIdx[a2]) next[a2][a1] == selectAcq[a1];
		next[a1][a1] == 0;
	}

	// Restriction of possible successive selected acquisition windows by using earliest and latest acquisition times
	forall(a1,a2 in AcquisitionWindows : a1 != a2 && SatelliteIdx[a1] == SatelliteIdx[a2] && EarliestStartTime[a1] + Duration[a1] + TransitionTimes[a1][a2] >= LatestStartTime[a2]){
		next[a1][a2] == 0;
	}

	// Temporal separation constraints between successive acquisition windows (big-M formulation)
	forall(a1,a2 in AcquisitionWindows : a1 != a2 && SatelliteIdx[a1] == SatelliteIdx[a2] && EarliestStartTime[a1] + Duration[a1] + TransitionTimes[a1][a2] < LatestStartTime[a2]){
		startTime[a1] + Duration[a1] + TransitionTimes[a1][a2]  <= startTime[a2] 
                + (1-next[a1][a2])*(LatestStartTime[a1]+Duration[a1]+TransitionTimes[a1][a2]-EarliestStartTime[a2]);
	}

}

execute {
	var ofile = new IloOplOutputFile(OutputFile);
	for(var i=1; i <= NacquisitionWindows; i++) { 
		if(selectAcq[i] == 1){
			ofile.writeln(CandidateAcquisitionIdx[i] + " " + AcquisitionWindowIdx[i] + " " + startTime[i] + " " + (startTime[i]+Duration[i]));
		}
	}
}
