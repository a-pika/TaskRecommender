package org.processmining.plugins.miningresourceprofiles.taskRecommender;


public class InputParametersTR{
	
	public boolean logHasResources;
	public double timeSplitPoint; // fraction of r's time
	public long durationTimeUnit;
	public double minResSimilarity;
	public int minSimResources;
	public double minTaskSupport;
	public long maxTaskPredPeriod;
	public long days;
	public boolean considerResourceLearningPeriod; //true - time till r's last 'new task' event; false - time till r's last event
	public boolean considerDaysInLearning; 
	public boolean considerDaysInEvaluation;
	public int minPastTasks;
	public int minTasksResFilter;
	public int minDurationFilter;
	//public int numberOfResourcesWithMinTasks;
	public boolean resetSplitTime;
	public int numberOfResources;
	//public boolean filterNumberOfTasks;
	//public boolean filterDuration;
	
	
	/*public InputParametersTR(double predictionPoint, long searchDur, double resSim, int minSimR, double minTaskSup, int minPT) {
		
		considerDaysInLearning = true;
		considerDaysInEvaluation = true;
		considerResourceLearningPeriod = true;
		logHasResources = true;
		durationTimeUnit = 3600000*24; // 1 day
		days = searchDur;
		maxTaskPredPeriod = searchDur*3600000*24L; 
		timeSplitPoint = predictionPoint; 
		minResSimilarity = resSim;
		minTaskSupport = minTaskSup;
		minSimResources = minSimR;
		minPastTasks = minPT;
		resetSplitTime = false;
	} 
*/	
	
/*	public InputParametersTR() {
		
		
		considerDaysInLearning = true;
		considerDaysInEvaluation = true;
		considerResourceLearningPeriod = true; 
		logHasResources = true;
		durationTimeUnit = 3600000*24; // 1 day
		days = 5;
		maxTaskPredPeriod = days*3600000*24L; 
		timeSplitPoint = 0.3; //0.2; 
		minResSimilarity = 0.5;
		minTaskSupport = 0.4;
		minSimResources = 1;
		minPastTasks = 1;
		resetSplitTime = true;
	
	} 
*/	
	
	
	public InputParametersTR(int minTasksRFilter, int minDurFilter, long searchDur, double resSim, int minSimR, double minTaskSup, int minPT, double splitPoint, int numRes) {
		
		minTasksResFilter = minTasksRFilter;
		minDurationFilter = minDurFilter;
		considerDaysInLearning = true;
		considerDaysInEvaluation = true;
		considerResourceLearningPeriod = true;
		logHasResources = true;
		durationTimeUnit = 3600000*24; // 1 day
		days = searchDur;
		maxTaskPredPeriod = searchDur*3600000*24L; 
		minResSimilarity = resSim;
		minTaskSupport = minTaskSup;
		minSimResources = minSimR;
		minPastTasks = minPT;
		resetSplitTime = false; 
		timeSplitPoint = splitPoint;
		numberOfResources = numRes;
		
		
		//false - time split; true - task split
		//filterNumberOfTasks = false;
		//filterDuration = false;		
	}
	
	public void printConfiguration()
	{
		//String conf = "Configuration: splitPoint:" + timeSplitPoint + "  days:" + days + "  minResSimilarity:" + minResSimilarity + "  minSimResources:" + minSimResources + "  minTaskSupport:" + minTaskSupport;
		//String conf = "Configuration: minTaskFilter:" + minTasksResFilter + "  days:" + days + " minPastTasks:"+minPastTasks+ "  minResSimilarity:" + minResSimilarity + "  minSimResources:" + minSimResources + "  minTaskSupport:" + minTaskSupport;

		String conf = "Conf: taskFilter:" + minTasksResFilter + " timeFilter:" + minDurationFilter + " days:" + days + " split:" + timeSplitPoint + 
		" pastTasks:"+minPastTasks+ " resSim:" + minResSimilarity + " simRes:" + minSimResources + " taskSup:" + minTaskSupport + " numRes:" + numberOfResources;
		
		System.out.println(conf);
	}
	
	public String getConfiguration()
	{
		String conf = timeSplitPoint + "_" + days + "_" + minResSimilarity + "_" + minSimResources + "_" + minTaskSupport + "_" + minPastTasks;
		//String conf = timeSplitPoint + "_" + minResSimilarity + "_" + minSimResources + "_" + minTaskSupport;
		return conf;
	}
	
	public String getConfigurationNoSplit()
	{
		String conf = minTasksResFilter + "_" + days + "_" + minPastTasks + "_" + minResSimilarity + "_" + minSimResources + "_" + minTaskSupport;
		//String conf = timeSplitPoint + "_" + minResSimilarity + "_" + minSimResources + "_" + minTaskSupport;
		return conf;
	}

	
}




