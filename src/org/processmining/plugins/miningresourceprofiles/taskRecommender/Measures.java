package org.processmining.plugins.miningresourceprofiles.taskRecommender;


public class Measures{
	
	public String resourceName = "";
	public Double resourcePastTasks = 0.0;
	
	public Double resPrediction = 0.0; //1 - if there was a prediction; 0 - otherwise
	public Double performedOnePredTask = 0.0; //1 - at least 1 predicted task was performed
	public Double performedAllPredTasks = 0.0; //1 - if all predicted tasks are performed
	public Double recommendedAllRealTasks = 0.0; //1 - if all real tasks are recommended
	public Double predRealIntersection = 0.0; //1 - if predicted == real

	public Double performedInTimeOrNONE = 0.0; //tasks or NONE
	public Double realPredNONE; //1.0 - none; 0.0 - tasks
	public Double performedEventually = 0.0;
	public Double realDur = 0.0;	
	public Double predDur = 0.0;
	public Double durDiffSub = 0.0;
	public Double durDiffDiv = 0.0;
	
	public Measures() {
	} 
	
	public Measures(String r) {
		resourceName = r;
	} 
	
	public void printMeasures()
	{
		System.out.println("Measures for: " + resourceName);
		System.out.println("Recommended: " + resPrediction);
		System.out.println("Performed in time: " + performedOnePredTask);
		System.out.println("Performed eventually: " + performedEventually);
		System.out.println("Durations for eventually performed tasks (real, predicted, diffSub, diffDiv):" + realDur + " --- " + predDur + " --- " + durDiffSub + " --- " + durDiffDiv);
	}
	
	public void printMeasuresTasksWithintime()
	{
		System.out.println("Measures for: " + resourceName);
		System.out.println("Recommended: " + resPrediction);
		System.out.println("NONE-NONE: " + realPredNONE);
		System.out.println("Performed in time or NONE: " + performedInTimeOrNONE);
		System.out.println("Performed in time: " + performedOnePredTask);
	}
	
	public void printMeasuresFirstTask()
	{
		System.out.println("Measures for: " + resourceName);
		System.out.println("resPrediction: " + resPrediction);
		System.out.println("performedOnePredTask: " + performedOnePredTask);
		System.out.println("performedAllPredTasks: " + performedAllPredTasks);
		System.out.println("recommendedAllRealTasks: " + recommendedAllRealTasks);
		System.out.println("predRealIntersection: " + predRealIntersection);
	}

	
	
}




