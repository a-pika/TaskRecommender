package org.processmining.plugins.miningresourceprofiles.taskRecommender;

import java.util.Vector;

public class FutureTasks{
	
	public Resource res = null;
	public boolean predicted; // true - task/s or 'none' predicted; false - no prediction - not enough similar resources or task support
	public Vector<String> rFutureTasks = new Vector<String>(); //sorted by time
	public Vector<Double> rFutureDurations = new Vector<Double>(); //sorted
	public Vector<Long> rPredResNum = new Vector<Long>(); 	
	
	public FutureTasks() {
		predicted = true;
	} 
	
	public FutureTasks(Resource r) {
		predicted = true;
		res = r;
	} 
	
	public void printFutureTasks()
	{
		System.out.println("---");
		System.out.println(res.resourceName);
		System.out.println(rFutureTasks);
		System.out.println(rFutureDurations);
		System.out.println(rPredResNum);
	}
	
	public void getFutureTasks(InputParametersTR ip)
	{
		for(int i=0; i<res.rTasks.size(); i++)
			if(!(res.rTimes.elementAt(i).before(res.resourceSplitTime)))
			{ 
				rFutureTasks.add(res.rTasks.elementAt(i));
				rFutureDurations.add((double) ((res.rTimes.elementAt(i).getTime() - res.resourceSplitTime.getTime())/ip.durationTimeUnit));
			}
	}
	
	//TODO
	public void getFirstFutureTasks(InputParametersTR ip)
	{
		
		double minDur = -1.0;
		
		for(int i=0; i<res.rTasks.size(); i++)
			if(!(res.rTimes.elementAt(i).before(res.resourceSplitTime)))
			{
				minDur = (double) ((res.rTimes.elementAt(i).getTime() - res.resourceSplitTime.getTime()) / (double) ip.durationTimeUnit);
				break;
			}
		
		if(ip.considerDaysInEvaluation)
			minDur += (double) ip.days;
		
		for(int i=0; i<res.rTasks.size(); i++)
			if(!(res.rTimes.elementAt(i).before(res.resourceSplitTime)))
			{
				double curDur = (double) ((res.rTimes.elementAt(i).getTime() - res.resourceSplitTime.getTime()) / (double) ip.durationTimeUnit);
				
				if(curDur <= minDur)
				{
					rFutureTasks.add(res.rTasks.elementAt(i));
					rFutureDurations.add(curDur);
				}
			}
	
		//v1
	/*	for(int i=0; i<res.rTasks.size(); i++)
			if(!(res.rTimes.elementAt(i).before(res.resourceSplitTime)))
			{ 
				//use this to get all new tasks performed by a resource the same day as the first new task
				//double curDur = (double) ((res.rTimes.elementAt(i).getTime() - res.resourceSplitTime.getTime()) / ip.durationTimeUnit);
				
				double curDur = (double) ((res.rTimes.elementAt(i).getTime() - res.resourceSplitTime.getTime()) / (double) ip.durationTimeUnit);
				//System.out.println("curDur: " + curDur);
				
				if(curDur <= minDur)
				{
					minDur = curDur;
					rFutureTasks.add(res.rTasks.elementAt(i));
					rFutureDurations.add(curDur);
				}
			}
	 */	
		
		}

	
	
}




