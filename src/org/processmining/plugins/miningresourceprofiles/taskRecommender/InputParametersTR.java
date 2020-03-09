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
	public boolean resetSplitTime;
	public int numberOfResources;


	
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
	}
	
	public void printConfiguration()
	{
		String conf = "Conf: taskFilter:" + minTasksResFilter + " timeFilter:" + minDurationFilter + " days:" + days + " split:" + timeSplitPoint + 
		" pastTasks:"+minPastTasks+ " resSim:" + minResSimilarity + " simRes:" + minSimResources + " taskSup:" + minTaskSupport + " numRes:" + numberOfResources;
		
		System.out.println(conf);
	}
	
	public String getConfiguration()
	{
		String conf = timeSplitPoint + "_" + days + "_" + minResSimilarity + "_" + minSimResources + "_" + minTaskSupport + "_" + minPastTasks;
		return conf;
	}
	
	public String getConfigurationNoSplit()
	{
		String conf = minTasksResFilter + "_" + days + "_" + minPastTasks + "_" + minResSimilarity + "_" + minSimResources + "_" + minTaskSupport;
		return conf;
	}
	
}




