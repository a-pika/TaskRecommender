package org.processmining.plugins.miningresourceprofiles.taskRecommender;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class Resource{
	
	public String resourceName = null;
	public Date resourceStart = null;
	public Date resourceEnd = null; //either time of the last event or time of the last new task (in ip)
	public Date resourceSplitTime = null;//(End-Start)*ip.timeSplitPoint
	
	
	public HashMap <String, Date> taskStart = new HashMap<String, Date>();
	public Vector<String> rTasks = new Vector<String>(); //sorted
	public Vector<Date> rTimes = new Vector<Date>(); //sorted
	public Vector<String> rPastTasks = new Vector<String>();
		
	
	
	public Resource() {
	} 
	
	public Resource(String r) {
		resourceName = r;
	} 
	
	public void printResource()
	{
		System.out.println("---");
		System.out.println(resourceName);
		System.out.println(resourceStart + " --- " + resourceSplitTime + " --- " + resourceEnd);
		System.out.println(taskStart);
		System.out.println(rTasks);
		System.out.println(rTimes);
	}
	
	public void printResTimes()
	{
		Vector<Double> times = new Vector<Double>();
		
		for(int i=0; i<rTimes.size(); i++)
			times.add((double)(rTimes.elementAt(i).getTime()-rTimes.elementAt(0).getTime())/(double)(3600000*24));
		
		System.out.println(resourceName + ": "+times);
	}
	
	
}




