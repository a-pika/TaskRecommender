package org.processmining.plugins.miningresourceprofiles.taskRecommender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;


public class Analysis{
	
	public Analysis() {} 
	
	public Set<String> getResources(XLog log)
	{
		Set<String> resources = new HashSet<String>();
		for (XTrace t : log) 
		{
			for (XEvent event : t) 
			{
					if(event.getAttributes().get("org:resource") != null)
						resources.add(event.getAttributes().get("org:resource").toString());
			}
		}
		
		return resources;
	}
	
	public Set<String> getResourcesTaskFilter(XLog log, int minTasks)
	{
		Set<String> resources = new HashSet<String>();
		Map<String,Set<String>> resTasks = new HashMap<String,Set<String>>();
		
		for (XTrace t : log) 
		{
			for (XEvent event : t) 
			{
					if(event.getAttributes().get("org:resource") != null && event.getAttributes().get("concept:name") != null)
					{
						String curRes = event.getAttributes().get("org:resource").toString();
						String curTask = event.getAttributes().get("concept:name").toString();
						
						if(resTasks.keySet().contains(curRes))
							resTasks.get(curRes).add(curTask);
						else
						{
							resTasks.put(curRes, new HashSet<String>());
							resTasks.get(curRes).add(curTask);
						}
						
					}						
			}
		}
		
		for(String res: resTasks.keySet())
			if(resTasks.get(res).size() >= minTasks)
				resources.add(res);
		
		//for(String res: resTasks.keySet())
		//	System.out.println(res + "," + resTasks.get(res).size());
		
		//System.out.println("done");
		
		
		//System.out.println("All resources: " + resTasks.size());
		//System.out.println("Resources with min tasks: " + resources.size());
		
		
		return resources;
	}
	
	public Resource getResourceHistoryNoSplitTime(String resource, XLog log)
	{
		Resource r = new Resource(resource);
		Date rStart = new Date(System.currentTimeMillis());
		Date rEnd = new Date(0);
		
		for (XTrace t : log) 
		{
			for (XEvent event : t) 
			{
				String currentResource = null;
				String currentTask = null;
				Date currentTime = null;
				
					if(event.getAttributes().get("org:resource") != null)
						currentResource = event.getAttributes().get("org:resource").toString();
					
					if(event.getAttributes().get("concept:name") != null)
						currentTask = event.getAttributes().get("concept:name").toString();
					
					if(event.getAttributes().get("time:timestamp") != null);
						currentTime = ((XAttributeTimestamp) event.getAttributes().get("time:timestamp")).getValue();
					
			if(currentResource != null && currentTask != null && currentTime != null)
			{
				
				if(currentResource.equals(resource))
				{
					if(currentTime.before(rStart))
						rStart = currentTime;
					
					if(currentTime.after(rEnd))
						rEnd = currentTime;
					
					if(r.taskStart.containsKey(currentTask))
					{
						Date date = r.taskStart.get(currentTask);
						
						if(currentTime.before(date))
							r.taskStart.put(currentTask, currentTime);
					}
					else
					{
						r.taskStart.put(currentTask, currentTime);
					}
					
				}
				
			}
			
			}
		}
		
		r.resourceStart = rStart;
		
		Date rNewTaskEnd = new Date(0);
		for(String res: r.taskStart.keySet())
			if(r.taskStart.get(res).after(rNewTaskEnd))
				rNewTaskEnd = r.taskStart.get(res);
		
		r.resourceEnd = rNewTaskEnd;
		
		return r;
	}
	
	public Resource getResourceHistory(String resource, XLog log, InputParametersTR ip)
	{
		Resource r = new Resource(resource);
		Date rStart = new Date(System.currentTimeMillis());
		Date rEnd = new Date(0);
		
		for (XTrace t : log) 
		{
			for (XEvent event : t) 
			{
				String currentResource = null;
				String currentTask = null;
				Date currentTime = null;
				
					if(event.getAttributes().get("org:resource") != null)
						currentResource = event.getAttributes().get("org:resource").toString();
					
					if(event.getAttributes().get("concept:name") != null)
						currentTask = event.getAttributes().get("concept:name").toString();
					
					if(event.getAttributes().get("time:timestamp") != null);
						currentTime = ((XAttributeTimestamp) event.getAttributes().get("time:timestamp")).getValue();
					
			if(currentResource != null && currentTask != null && currentTime != null)
			{
				
				if(currentResource.equals(resource))
				{
					if(currentTime.before(rStart))
						rStart = currentTime;
					
					if(currentTime.after(rEnd))
						rEnd = currentTime;
					
					if(r.taskStart.containsKey(currentTask))
					{
						Date date = r.taskStart.get(currentTask);
						
						if(currentTime.before(date))
							r.taskStart.put(currentTask, currentTime);
					}
					else
					{
						r.taskStart.put(currentTask, currentTime);
					}
					
				}
				
			}
			
			}
		}
		
		r.resourceStart = rStart;
		
		if(ip.considerResourceLearningPeriod)
		{
			Date rNewTaskEnd = new Date(0);
			for(String res: r.taskStart.keySet())
				if(r.taskStart.get(res).after(rNewTaskEnd))
					rNewTaskEnd = r.taskStart.get(res);
		
			r.resourceEnd = rNewTaskEnd;
		}
		else
			r.resourceEnd = rEnd;
		
		Long duration = r.resourceEnd.getTime() - rStart.getTime();
		r.resourceSplitTime = new Date(rStart.getTime() + (long)(duration*ip.timeSplitPoint));
		
		return r;
	}

	public File saveResourceHistoriesMap(Vector<Resource> resourceHistories, String filePath) throws IOException
	{
		Vector<String> lines = new Vector<String>();
		String title = "resource,startTime,activity\r\n";
		lines.add(title);
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		for(Resource r: resourceHistories)
		{
			for(String task:r.taskStart.keySet())
			{
				String startDate = sdf.format(r.taskStart.get(task));
				String line = r.resourceName + "," + startDate + "," + task + "\r\n";
				lines.add(line);
			}
		}
		
		File file = new File(filePath);
		FileWriter fw = new FileWriter(file, true);
	
		for(int i=0; i<lines.size(); i++)
			fw.write(lines.elementAt(i));
		
		fw.flush();
		fw.close();
	 	
		return file;
	}
	
	public File saveFutureTasks(Vector<FutureTasks> fTasks, String filePath) throws IOException
	{
		Vector<String> lines = new Vector<String>();
		
		for(FutureTasks ft: fTasks)
		{
			String line1 = ft.res.resourceName + "," + ft.rFutureTasks.toString().substring(1, ft.rFutureTasks.toString().length() - 1)+"\r\n";
			
			//rTimes = rTimes.substring(0, rTimes.length() - 1);
			
			lines.add(line1);
			String line2 = ft.res.resourceName + "," + ft.rFutureDurations.toString().substring(1, ft.rFutureDurations.toString().length() - 1)+"\r\n";
			lines.add(line2);
			//String line3 = ft.res.resourceName + "," + ft.rPredResNum.toString().substring(1, ft.rPredResNum.toString().length() - 1)+"\r\n";
			//lines.add(line3);
		}
		
		File file = new File(filePath);
		FileWriter fw = new FileWriter(file, true);
	
		for(int i=0; i<lines.size(); i++)
			fw.write(lines.elementAt(i));
		
		fw.flush();
		fw.close();
	 	
		return file;
	}
	
	//v1-2
	public File saveSummary(Vector<InputParametersTR> ipV, Vector<Measures> mV, String filePath) throws IOException
	{
		Vector<String> lines = new Vector<String>();
		
		String title = "SplitPoint,SearchPeriod(days),ResourceSimilarity,MinSimilarResources,MinPredSupport,Predictions,InTime,Eventually,RealTime,PredTime,Diff(sub),Diff(div)\r\n";
		lines.add(title);
		
		for(int i=0; i<ipV.size(); i++)
		{
			InputParametersTR ip = ipV.elementAt(i);
			Measures m = mV.elementAt(i);
			
			String line = ip.timeSplitPoint+","+ip.days+","+ip.minResSimilarity+","+ip.minSimResources+","+ip.minTaskSupport+","+m.resPrediction+","+m.performedOnePredTask+","+m.performedEventually+","+m.realDur+","+m.predDur+","+m.durDiffSub+","+m.durDiffDiv+"\r\n";
			lines.add(line);
		}
			
		File file = new File(filePath);
		FileWriter fw = new FileWriter(file, true);
	
		for(int i=0; i<lines.size(); i++)
			fw.write(lines.elementAt(i));
		
		fw.flush();
		fw.close();
	 	
		return file;
	}
	
	//v2.2
			public File saveSummaryFirstTasks(Vector<InputParametersTR> ipV, Vector<Measures> mV, String filePath) throws IOException
			{
				Vector<String> lines = new Vector<String>();
				
				String title = "Days,SplitPoint,MinResSimilarity,MinSimResources,MinTaskSupport,minPastTasks," +
						"Predictions,Accuracy\r\n";
				lines.add(title);
				
				for(int i=0; i<ipV.size(); i++)
				{
					InputParametersTR ip = ipV.elementAt(i);
					Measures m = mV.elementAt(i);
					
					String line = ip.days+","+ip.timeSplitPoint+","+ip.minResSimilarity+","+ip.minSimResources+","+ip.minTaskSupport+"," + 
						ip.minPastTasks + "," +	m.resPrediction+","+m.performedOnePredTask+"\r\n";
					lines.add(line);
				}
					
				File file = new File(filePath);
				FileWriter fw = new FileWriter(file, true);
			
				for(int i=0; i<lines.size(); i++)
					fw.write(lines.elementAt(i));
				
				fw.flush();
				fw.close();
			 	
				return file;
			}
			
			//v2.3 
			public File saveSummaryFirstTasksNoSplit4M(Vector<InputParametersTR> ipV, Vector<Measures> mV, String filePath) throws IOException
			{
				Vector<String> lines = new Vector<String>();
				
				String title = "numRes,minDurFilter,minTasksFilter,timeSplit,days,minResSim,minSimRes,minTaskSupport,minPastTasks," +
						"Recommendations,Acc:OneTask,Acc:AllRec,Acc:AllReal,Acc:Intersection\r\n";
				lines.add(title);
				
				for(int i=0; i<ipV.size(); i++)
				{
					InputParametersTR ip = ipV.elementAt(i);
					Measures m = mV.elementAt(i);
					
					String line = ip.numberOfResources+","+ip.minDurationFilter+","+ip.minTasksResFilter+","+ip.timeSplitPoint+","+ip.days+","
					+ip.minResSimilarity+","+ip.minSimResources+","+ip.minTaskSupport+","+ip.minPastTasks+"," 
					+m.resPrediction+","+m.performedOnePredTask+","+m.performedAllPredTasks+","+m.recommendedAllRealTasks+","+m.predRealIntersection+"\r\n";
					lines.add(line);
				}
					
				File file = new File(filePath);
				FileWriter fw = new FileWriter(file, true);
			
				for(int i=0; i<lines.size(); i++)
					fw.write(lines.elementAt(i));
				
				fw.flush();
				fw.close();
			 	
				return file;
			}

	
	//v3
		public File saveSummaryTasksWithinTime(Vector<InputParametersTR> ipV, Vector<Measures> mV, String filePath) throws IOException
		{
			Vector<String> lines = new Vector<String>();
			
			String title = "SplitPoint,PredictionPeriod(days),MinResSimilarity,MinSimilarResources,MinPredSupport," +
					"Predicted,NONE-NONE,InTimeOrNONE,InTime\r\n";
			lines.add(title);
			
			for(int i=0; i<ipV.size(); i++)
			{
				InputParametersTR ip = ipV.elementAt(i);
				Measures m = mV.elementAt(i);
				
				String line = ip.timeSplitPoint+","+ip.days+","+ip.minResSimilarity+","+ip.minSimResources+","+ip.minTaskSupport+"," + 
						m.resPrediction+","+m.realPredNONE+","+m.performedInTimeOrNONE+","+m.performedOnePredTask+"\r\n";
				lines.add(line);
			}
				
			File file = new File(filePath);
			FileWriter fw = new FileWriter(file, true);
		
			for(int i=0; i<lines.size(); i++)
				fw.write(lines.elementAt(i));
			
			fw.flush();
			fw.close();
		 	
			return file;
		}


	public File saveResourceHistoriesSeq(Vector<Resource> resourceHistories, String filePath) throws IOException
	{
		Vector<String> lines = new Vector<String>();
		
		for(Resource r: resourceHistories)
		{
			String rTasks = r.resourceName + "," + r.rTasks + "\r\n";
			lines.add(rTasks);
			String rTimes = r.resourceName + "," + r.rTimes + "\r\n";
			lines.add(rTimes);
		}
		
		File file = new File(filePath);
		FileWriter fw = new FileWriter(file, true);
	
		for(int i=0; i<lines.size(); i++)
			fw.write(lines.elementAt(i));
		
		fw.flush();
		fw.close();
	 	
		return file;
	}


	
	public Resource setSplitTime(Resource r, InputParametersTR ip)
	{
		
		if(r.rTasks.size() > ip.minPastTasks)
			r.resourceSplitTime = new Date(r.rTimes.elementAt(ip.minPastTasks-1).getTime()+1);
		
		return r;
	}
	
		public Set<String> getNewEmployees(Vector<Resource> resourceHistories, InputParametersTR ip, Date logStart)
		{
			Set<String> newEmployees = new HashSet<String>();
			
			for(Resource r:resourceHistories)
			{
				long timeFromStart = r.resourceStart.getTime() - logStart.getTime();
				double daysFromStart = (double) timeFromStart / (double) (3600000*24);
				if(daysFromStart >= ip.minDurationFilter)
					newEmployees.add(r.resourceName);
			}
			
			return newEmployees;
		}
	
	
	public Resource setSplitTimeNumberOfTasks(Resource r, InputParametersTR ip)
	{
		
		if(r.rTasks.size() > ip.minPastTasks)
			r.resourceSplitTime = new Date(r.rTimes.elementAt(ip.minPastTasks-1).getTime()+1);
		else
			r.resourceSplitTime = r.rTimes.elementAt(0);
		
		return r;
	}
	
	
	public Resource setSplitTimeFractionOfTasks(Resource r, InputParametersTR ip)
	{
		int tasks = r.rTasks.size();
		int index = (int) (tasks*ip.timeSplitPoint);
		r.resourceSplitTime = new Date(r.rTimes.elementAt(index).getTime()+1);
		
		return r;
	}
	
	public Resource setSplitTimeFractionOfTime(Resource r, InputParametersTR ip)
		{
			Long duration = r.resourceEnd.getTime() - r.resourceStart.getTime();
			r.resourceSplitTime = new Date(r.resourceStart.getTime() + (long)(duration*ip.timeSplitPoint));
			return r;
		}

	public Resource getRTasksTimes(Resource r)
	{
		
		//System.out.println(r.resourceName);
		
		Vector<String> tasks = new Vector<String>();
		
		for(String task:r.taskStart.keySet())
			tasks.add(task);
		
		//Collections.sort(tasks, Collections.reverseOrder());
		
		int rep = tasks.size();
		
		for(int i=0; i<rep; i++)
		{
			Date currentStartDate = new Date(System.currentTimeMillis());
			String currentStartTask = "";
			
			for(String task:tasks)
			{
				Date startDate = r.taskStart.get(task);
				if(startDate.before(currentStartDate) || startDate.equals(currentStartDate))
					{
						currentStartDate = startDate;
						currentStartTask = task;
					}
			}
			
			r.rTasks.add(currentStartTask);
			r.rTimes.add(currentStartDate);
			
			tasks.remove(currentStartTask);
	
			
		}
		
		//System.out.println(r.rTasks);
		//System.out.println(r.rTimes);
		
		return r;
	}


	
	public Vector<String> getPastTasks(Resource r, Date splitTime)
	{
		Vector<String> rPastTasks = new Vector<String>();
		
		for(int i=0; i<r.rTimes.size(); i++)
			if(r.rTimes.elementAt(i).before(splitTime))
				rPastTasks.add(r.rTasks.elementAt(i));
		
		return rPastTasks;
	}
	
	//v3
		public FutureTasks getTasksWithinPeriodPrediction (Resource r, Date splitDate, Vector<Resource> resources, XLog log, InputParametersTR ip, int rIndex)
		{
			
			//1. Get r's past tasks set					
			Vector<String> rPastTasks = getPastTasks(r,splitDate);
			//System.out.println(rPastTasks);
			
			//2. For all other resources get sets of tasks they performed before split Date
			Map<String,Vector<String>> otherPastTasks = new HashMap<String,Vector<String>>();
			for(int i=0; i<resources.size(); i++)
				if(i != rIndex)
					otherPastTasks.put(resources.elementAt(i).resourceName, getPastTasks(resources.elementAt(i),splitDate));
			
			//System.out.println(otherPastTasks);
			
			//v1
			//3. Find resource similarity - fraction of r’s past task set that was also performed by a given resource at split time
			Map<String, Double> resourceSimilarity = getResSimilarityTaskSet(rPastTasks,otherPastTasks);
			//System.out.println(resourceSimilarity);
			
			//v2
			//3. Find resource similarity: number of 'shared' tasks/ size of a bigger task set
			//Map<String, Double> resourceSimilarity = getResSimilarityTaskSetIntersection(rPastTasks,otherPastTasks);
			//System.out.println(resourceSimilarity);
			
			//4. Get a set of resources with similarity higher than a threshold
			Vector<String> similarResources = new Vector<String>();
			for(String res: resourceSimilarity.keySet())
				if(resourceSimilarity.get(res) >= ip.minResSimilarity)
					similarResources.add(res);
			
			//System.out.println(similarResources);
			
			FutureTasks prediction = new FutureTasks(r);
			
		//if(similarResources.size()>=ip.minSimResources)
			//{
				//5. Find new tasks which similar resources did 
				Map<String,FutureTasks> otherNewTasks = new HashMap<String,FutureTasks>();
				for(String res: similarResources)
				{
					Resource curR = null;
					for(Resource cr: resources)
						if(cr.resourceName.equals(res))
							curR = cr;
					
					//v1 after the completion of r’s set (within a given period) and before the split time 
					//otherNewTasks.put(res, findNewTasks(rPastTasks, splitDate, curR, ip));
					
					//v2 after the completion of r’s set and before the split time 
					//otherNewTasks.put(res, findFirstNewTasks(rPastTasks, splitDate, curR, ip));
					
					//v3 after the completion of r’s set (within a given period) and before the split time 
					otherNewTasks.put(res, findNewTasksWithinTime(rPastTasks, splitDate, curR, ip));
					
				}
					
				//System.out.println("otherNewTasks");
				//for(String res: otherNewTasks.keySet())
				//	System.out.println(res + " --- " + otherNewTasks.get(res).rFutureTasks + " --- " + otherNewTasks.get(res).rFutureDurations);
				
				
				// 6. Get recommendation: most frequent task/s – number of resources - mid duration
				//v1
				//prediction = predictNewTasks(r,otherNewTasks,ip,resourceSimilarity);
				
				
				//v3 
				prediction = predictNewTasksWithinTime(r,otherNewTasks,ip);
				
			//}
		//else
		//	prediction.predicted = false;
				
			
			return prediction;
		}
		

		//v2.2
		public FutureTasks getFirstTaskPredictionNoNONE (Resource r, Date splitDate, Vector<Resource> resources, /*XLog log,*/ InputParametersTR ip, int rIndex)
		{
			
			//1.  Get r's past tasks set					
			Vector<String> rPastTasks = getPastTasks(r,splitDate);
			//System.out.println(rPastTasks);
			r.rPastTasks.addAll(rPastTasks);
			
			FutureTasks prediction = new FutureTasks(r);
			
		if(rPastTasks.size() >= ip.minPastTasks)
		{	
			//2. For all other resources get sets of tasks they performed before split Date
			Map<String,Vector<String>> otherPastTasks = new HashMap<String,Vector<String>>();
			for(int i=0; i<resources.size(); i++)
				if(i != rIndex)
					otherPastTasks.put(resources.elementAt(i).resourceName, getPastTasks(resources.elementAt(i),splitDate));
			
			//System.out.println(otherPastTasks);
			
			//v1
			//3. Find resource similarity - fraction of r’s past task set that was also performed by a given resource before split time
			Map<String, Double> resourceSimilarity = getResSimilarityTaskSet(rPastTasks,otherPastTasks);
			//System.out.println(resourceSimilarity);
			
			//v2
			//3. Find resource similarity: number of 'shared' tasks/ size of a bigger task set
			//Map<String, Double> resourceSimilarity = getResSimilarityTaskSetIntersection(rPastTasks,otherPastTasks);
			//System.out.println(resourceSimilarity);
			
			//4. Get a set of resources with similarity >= than a threshold
			Vector<String> similarResources = new Vector<String>();
			for(String res: resourceSimilarity.keySet())
				if(resourceSimilarity.get(res) >= ip.minResSimilarity)
					similarResources.add(res);
			
			//System.out.println(similarResources);
			
			//FutureTasks prediction = new FutureTasks(r);
			
		if(similarResources.size()>=ip.minSimResources)
			{
					
				//v2 
				//5. Find first new tasks which similar resources did after the completion of r’s set and before the split time 
				Map<String,FutureTasks> otherNewTasks = new HashMap<String,FutureTasks>();
				for(String res: similarResources)
				{
					Resource curSimRes = null;
					for(Resource cr: resources)
						if(cr.resourceName.equals(res))
							curSimRes = cr;
					
					
					//  v2.1 - find first new tasks or empty if no new tasks performed
					otherNewTasks.put(res, findFirstNewTasks(rPastTasks, splitDate, curSimRes, ip));
					
					
					//v2.2 - same as v2.1
					//otherNewTasks.put(res, findFirstNewTasksNoNONE(rPastTasks, splitDate, curR, ip));
				}
		
				//System.out.println("otherNewTasks");
				//for(String res: otherNewTasks.keySet())
				//	System.out.println(res + " --- " + otherNewTasks.get(res).rFutureTasks + " --- " + otherNewTasks.get(res).rFutureDurations);
				
				// 6. Get recommendation: most frequent task/s – number of resources - mid duration
				
				
				//v2.1 - includes NONE as valid prediction
				//prediction = predictNewTasks(r,otherNewTasks,ip,resourceSimilarity);
				
				// v2.2 - no NONE predictions
				prediction = predictNewTasksNoNONE(r,otherNewTasks,ip,resourceSimilarity);
		
			}
		else
			prediction.predicted = false;
		}else		
			prediction.predicted = false;
			
		return prediction;
		}
		
		
		
	//v2
	public FutureTasks getFirstTaskPrediction (Resource r, Date splitDate, Vector<Resource> resources, XLog log, InputParametersTR ip, int rIndex)
	{
		
		//1. Get r's past tasks set					
		Vector<String> rPastTasks = getPastTasks(r,splitDate);
		//System.out.println(rPastTasks);
		
		//2. For all other resources get sets of tasks they performed before split Date
		Map<String,Vector<String>> otherPastTasks = new HashMap<String,Vector<String>>();
		for(int i=0; i<resources.size(); i++)
			if(i != rIndex)
				otherPastTasks.put(resources.elementAt(i).resourceName, getPastTasks(resources.elementAt(i),splitDate));
		
		//System.out.println(otherPastTasks);
		
		//v1
		//3. Find resource similarity - fraction of r’s past task set that was also performed by a given resource at split time
		Map<String, Double> resourceSimilarity = getResSimilarityTaskSet(rPastTasks,otherPastTasks);
		//System.out.println(resourceSimilarity);
		
		//v2
		//3. Find resource similarity: number of 'shared' tasks/ size of a bigger task set
		//Map<String, Double> resourceSimilarity = getResSimilarityTaskSetIntersection(rPastTasks,otherPastTasks);
		//System.out.println(resourceSimilarity);
		
		//4. Get a set of resources with similarity higher than a threshold
		Vector<String> similarResources = new Vector<String>();
		for(String res: resourceSimilarity.keySet())
			if(resourceSimilarity.get(res) >= ip.minResSimilarity)
				similarResources.add(res);
		
		//System.out.println(similarResources);
		
		FutureTasks prediction = new FutureTasks(r);
		
	if(similarResources.size()>=ip.minSimResources)
		{
			//v1
			//5. Find new tasks which similar resources did after the completion of r’s set (within a given period) and before the split time 
	/*		Map<String,FutureTasks> otherNewTasks = new HashMap<String,FutureTasks>();
			for(String res: similarResources)
			{
				Resource curR = null;
				for(Resource cr: resources)
					if(cr.resourceName.equals(res))
						curR = cr;
					
				otherNewTasks.put(res, findNewTasks(rPastTasks, splitDate, curR, ip));
			}
	*/		
			//v2
			//5. Find first new tasks which similar resources did after the completion of r’s set and before the split time 
			Map<String,FutureTasks> otherNewTasks = new HashMap<String,FutureTasks>();
			for(String res: similarResources)
			{
				Resource curR = null;
				for(Resource cr: resources)
					if(cr.resourceName.equals(res))
						curR = cr;
				
				
				otherNewTasks.put(res, findFirstNewTasks(rPastTasks, splitDate, curR, ip));
			}
	
			//System.out.println("otherNewTasks");
			//for(String res: otherNewTasks.keySet())
			//	System.out.println(res + " --- " + otherNewTasks.get(res).rFutureTasks + " --- " + otherNewTasks.get(res).rFutureDurations);
			
			// 6. Get recommendation: most frequent task/s – number of resources - mid duration
			prediction = predictNewTasks(r,otherNewTasks,ip,resourceSimilarity);
		}
	else
		prediction.predicted = false;
			
		
		return prediction;
	}
	
	
	// v1
	public FutureTasks getTaskPredictionsSimilarResourcesTaskSets (Resource r, Date splitDate, Vector<Resource> resources, XLog log, InputParametersTR ip, int rIndex)
	{
		
		//1. Get r's past tasks set					
		Vector<String> rPastTasks = getPastTasks(r,splitDate);
		//System.out.println(rPastTasks);
		
		//2. For all other resources get sets of tasks they performed before split Date
		Map<String,Vector<String>> otherPastTasks = new HashMap<String,Vector<String>>();
		for(int i=0; i<resources.size(); i++)
			if(i != rIndex)
				otherPastTasks.put(resources.elementAt(i).resourceName, getPastTasks(resources.elementAt(i),splitDate));
		
		//System.out.println(otherPastTasks);
		
		//3. Find resource similarity - fraction of r’s past task set that was also performed by a given resource at split time
		Map<String, Double> resourceSimilarity = getResSimilarityTaskSet(rPastTasks,otherPastTasks);
		//System.out.println(resourceSimilarity);
		
		//4. Get a set of resources with similarity higher than a threshold
		Vector<String> similarResources = new Vector<String>();
		for(String res: resourceSimilarity.keySet())
			if(resourceSimilarity.get(res) >= ip.minResSimilarity)
				similarResources.add(res);
		
		//System.out.println(similarResources);
		
		//5. Find new tasks which similar resources did after the completion of r’s set (within a given period) and before the split time 
		Map<String,FutureTasks> otherNewTasks = new HashMap<String,FutureTasks>();
		for(String res: similarResources)
		{
			Resource curR = null;
			for(Resource cr: resources)
				if(cr.resourceName.equals(res))
					curR = cr;
				
			otherNewTasks.put(res, findNewTasks(rPastTasks, splitDate, curR, ip));
		}
		
		//System.out.println("otherNewTasks");
		//for(String res: otherNewTasks.keySet())
			//System.out.println(res + " --- " + otherNewTasks.get(res).rFutureTasks + " --- " + otherNewTasks.get(res).rFutureDurations);
		
		// 6. Get recommendation: most frequent task/s – number of resources - mid duration
		FutureTasks prediction = predictNewTasks(r,otherNewTasks,ip,resourceSimilarity);
		
		return prediction;
	}
	
	
	// v3 all tasks with min support within time t sorted by avg. time period
		public FutureTasks predictNewTasksWithinTime(Resource cR, Map<String,FutureTasks> allNewTasks, InputParametersTR ip)
		{
			
			//0. Get <res,new tasks> map for similar resources with search time ending before split time
			Map<String,FutureTasks> newTasks = new HashMap<String,FutureTasks>();
			for(String res: allNewTasks.keySet())
				if(allNewTasks.get(res).predicted)
					newTasks.put(res, allNewTasks.get(res));
			
			FutureTasks prediction = new FutureTasks(cR);
			
	if(newTasks.keySet().size()>=ip.minSimResources)	
		{
			//1. Get a set of all recommended tasks
			Set<String> allTasksSet = new HashSet<String>();
			for(String res : newTasks.keySet())
				allTasksSet.addAll(newTasks.get(res).rFutureTasks);
			
			Vector<String> allTasks = new Vector<String>();
			allTasks.addAll(allTasksSet);
			allTasks.add("NONE");
			
			//2. Get recommendation frequency
			Vector<Integer> taskFreq = new Vector<Integer>();
						
			for(int i=0; i<allTasks.size(); i++)
				taskFreq.add(0);
			
			for(int i=0; i<allTasks.size()-1; i++)
			{
				for(String res : newTasks.keySet())
					if(newTasks.get(res).rFutureTasks.contains(allTasks.elementAt(i)))
						taskFreq.set(i, taskFreq.elementAt(i)+1);
			}
			
			for(String res : newTasks.keySet())
				if(newTasks.get(res).rFutureTasks.size() == 0)
					taskFreq.set(taskFreq.size()-1, taskFreq.elementAt(taskFreq.size()-1)+1);
			
			Map<String,Integer> predTasksCount = new HashMap<String,Integer>();
			for(int i=0; i<allTasks.size(); i++)
				predTasksCount.put(allTasks.elementAt(i), taskFreq.elementAt(i));
			
			//System.out.println("Recommendation for " + cR.resourceName);
			//System.out.println(allTasks);
			//System.out.println(taskFreq);
			
			//3. Get max frequency
			int max = 0;
			for(int i=0; i<taskFreq.size(); i++)
				if(taskFreq.elementAt(i) > max)
					max = taskFreq.elementAt(i);
			
			//fraction of similar resources which performed a task
			double taskSupport = ip.minTaskSupport * (double) newTasks.keySet().size();
			
			//System.out.println("taskSupport: " + taskSupport + " max task freq.: " + max + " Similar Resources: " + newTasks.keySet().size());
			
		if(max >= taskSupport)
		{
			//4. Get all tasks with frequency that is higher or equal than taskSupport
			Set<String> predTasks = new HashSet<String>();
			for(int i=0; i<allTasks.size(); i++)
				if(taskFreq.elementAt(i) >= taskSupport)
					predTasks.add(allTasks.elementAt(i));
			
			//5. Get pred. task avg. durations
			Map<String,Double> predTasksDur = new HashMap<String,Double>();
						
			for(String task: predTasks)
			{
				Double dur = 0.0;
				Integer count = 0;
				
				for(String res : newTasks.keySet())
				{
					if(newTasks.get(res).rFutureTasks.contains(task))
					{
						int taskIndex = newTasks.get(res).rFutureTasks.indexOf(task);
						double tDur = newTasks.get(res).rFutureDurations.elementAt(taskIndex);
						dur += tDur;
						count ++;
					}
				}
				
				Double avgDur = 0.0;
				
				if(count > 0)
					avgDur = dur/count;
				
				predTasksDur.put(task, avgDur);
				
			}
			
			//System.out.println("predTasksDur: " + predTasksDur);
			
			//6. Sort recommended tasks based on their durations - recommendation
			
			Vector<String> usedTasks = new Vector<String>();
			
			for(int i=0; i<predTasksDur.size(); i++)
			{
				double curMin = System.currentTimeMillis();
				String curtask = "";
				
				for(String t: predTasksDur.keySet())
					if(!usedTasks.contains(t) && predTasksDur.get(t) <= curMin)
					{
						curMin = predTasksDur.get(t);
						curtask = t;
					}
				
				
				prediction.rFutureTasks.add(curtask);
				prediction.rFutureDurations.add(curMin);
				prediction.rPredResNum.add((long)predTasksCount.get(curtask));
				
				usedTasks.add(curtask);
				
			}
		}
			else
			{
				prediction.predicted = false;
			}
			
			//System.out.println("recTasks: " + prediction.rFutureTasks);
			//System.out.println("recTasksDur: " + prediction.rFutureDurations);
		}
	else
	{
		prediction.predicted = false;
	}
			return prediction;
		}
		
	//  v2.2 most frequent task/s sorted by avg. time period
	public FutureTasks predictNewTasksNoNONE(Resource cR, Map<String,FutureTasks> newTasks, InputParametersTR ip, Map<String, Double> resSim)
	{
		//1. Get a set of all recommended tasks
		Set<String> allTasksSet = new HashSet<String>();
		for(String res : newTasks.keySet())
			allTasksSet.addAll(newTasks.get(res).rFutureTasks);
		
		Vector<String> allTasks = new Vector<String>();
		allTasks.addAll(allTasksSet);
		//allTasks.add("NONE");
		
		//2. Get recommendation frequency
		Vector<Integer> taskFreq = new Vector<Integer>();
		
		for(int i=0; i<allTasks.size(); i++)
			taskFreq.add(0);
		
		for(int i=0; i<allTasks.size(); i++)
		{
			for(String res : newTasks.keySet())
				if(newTasks.get(res).rFutureTasks.contains(allTasks.elementAt(i)))
					taskFreq.set(i, taskFreq.elementAt(i)+1);
		}
		
		//System.out.println("allTasks: " + allTasks);
		//System.out.println("taskFreq: " + taskFreq);
		
		// Find resources with prediction
		Set<String> resWithPred = new HashSet<String>();
		for(String res : newTasks.keySet())
			if(newTasks.get(res).rFutureTasks.size() > 0)
				resWithPred.add(res);
		
		
		FutureTasks prediction = new FutureTasks(cR);
		
		if(resWithPred.size() >= ip.minSimResources)
		{
		
		//System.out.println("Recommendation for " + cR.resourceName);
		//System.out.println(allTasks);
		//System.out.println(taskFreq);		
		
		//3. Get max frequency
		int max = 0;
		for(int i=0; i<taskFreq.size(); i++)
			if(taskFreq.elementAt(i) > max)
				max = taskFreq.elementAt(i);
		
		double taskSupport = ip.minTaskSupport * (double) resWithPred.size();
		
		//System.out.println("taskSupport: " + taskSupport + " max task freq.: " + max + " Similar Resources: " + newTasks.keySet().size());
		
	if(max >= taskSupport)
	{
		//4. Get predicted tasks: max freq. if only first new task, all >= taskSupport, if new tasks within days
		Set<String> predTasks = new HashSet<String>();
		if(ip.considerDaysInLearning && ip.days > 0)
		{
			for(int i=0; i<allTasks.size(); i++)
				if(taskFreq.elementAt(i) >= taskSupport)
					predTasks.add(allTasks.elementAt(i));
		}
		else
		{	for(int i=0; i<allTasks.size(); i++)
			if(taskFreq.elementAt(i) == max)
				predTasks.add(allTasks.elementAt(i));
		}
	
		
		//System.out.println("predTasks: " + predTasks);
		
		//5. Get pred. task avg. durations
		Map<String,Double> predTasksDur = new HashMap<String,Double>();
		for(String task: predTasks)
		{
			Double dur = 0.0;
			Integer count = 0;
			
			for(String res : newTasks.keySet())
			{
				if(newTasks.get(res).rFutureTasks.contains(task))
				{
					int taskIndex = newTasks.get(res).rFutureTasks.indexOf(task);
					double tDur = newTasks.get(res).rFutureDurations.elementAt(taskIndex);
					dur += tDur;
					count ++;
				}
			}
			
			Double avgDur = 0.0;
			
			if(count > 0)
				avgDur = dur/(double)count;
			
			predTasksDur.put(task, avgDur);
			
		}
		
		//System.out.println("predTasksDur: " + predTasksDur);
		
		//6. Sort recommended tasks based on their durations - recommendation
		
		Vector<String> usedTasks = new Vector<String>();
		
		for(int i=0; i<predTasksDur.size(); i++)
		{
			double curMin = System.currentTimeMillis();
			String curtask = "";
			
			for(String t: predTasksDur.keySet())
				if(!usedTasks.contains(t) && predTasksDur.get(t) <= curMin)
				{
					curMin = predTasksDur.get(t);
					curtask = t;
				}
			
			if(max >= ip.minTaskSupport)
			{
			prediction.rFutureTasks.add(curtask);
			prediction.rFutureDurations.add(curMin);
			prediction.rPredResNum.add((long) max);
			}
			
			usedTasks.add(curtask);
			
		}
		
		//System.out.println("prediction.rFutureTasks: " + prediction.rFutureTasks);
		//System.out.println("prediction.rFutureDurations: " + prediction.rFutureDurations);
		//System.out.println("prediction.rPredResNum: " + prediction.rPredResNum);
	}
		else
		{
			prediction.predicted = false;
		}
		
		//System.out.println("recTasks: " + prediction.rFutureTasks);
		//System.out.println("recTasksDur: " + prediction.rFutureDurations);
		
	
		}
		else
		{
			prediction.predicted = false;
		}
		return prediction;
	}
	
	

	
	// v1 most frequent task/s sorted by avg. time period
	public FutureTasks predictNewTasks(Resource cR, Map<String,FutureTasks> newTasks, InputParametersTR ip, Map<String, Double> resSim)
	{
		//1. Get a set of all recommended tasks
		Set<String> allTasksSet = new HashSet<String>();
		for(String res : newTasks.keySet())
			allTasksSet.addAll(newTasks.get(res).rFutureTasks);
		
		Vector<String> allTasks = new Vector<String>();
		allTasks.addAll(allTasksSet);
		allTasks.add("NONE");
		
		//2. Get recommendation frequency
		Vector<Integer> taskFreq = new Vector<Integer>();
		
		for(int i=0; i<allTasks.size(); i++)
			taskFreq.add(0);
		
		for(int i=0; i<allTasks.size()-1; i++)
		{
			for(String res : newTasks.keySet())
				if(newTasks.get(res).rFutureTasks.contains(allTasks.elementAt(i)))
					taskFreq.set(i, taskFreq.elementAt(i)+1);
		}
		
		for(String res : newTasks.keySet())
			if(newTasks.get(res).rFutureTasks.size() == 0)
				taskFreq.set(taskFreq.size()-1, taskFreq.elementAt(taskFreq.size()-1)+1);
		
		//System.out.println("Recommendation for " + cR.resourceName);
		//System.out.println(allTasks);
		//System.out.println(taskFreq);
		
		
		//3. Get max frequency
		int max = 0;
		for(int i=0; i<taskFreq.size(); i++)
			if(taskFreq.elementAt(i) > max)
				max = taskFreq.elementAt(i);
		
		FutureTasks prediction = new FutureTasks(cR);
		double taskSupport = ip.minTaskSupport * (double) newTasks.keySet().size();
		
		System.out.println("taskSupport: " + taskSupport + " max task freq.: " + max + " Similar Resources: " + newTasks.keySet().size());
		
	if(max >= taskSupport)
	{
		//4. Get max freq. tasks
		Set<String> predTasks = new HashSet<String>();
		for(int i=0; i<allTasks.size(); i++)
			if(taskFreq.elementAt(i) == max)
				predTasks.add(allTasks.elementAt(i));
		
		//5. Get pred. task avg. durations
		Map<String,Double> predTasksDur = new HashMap<String,Double>();
		for(String task: predTasks)
		{
			Double dur = 0.0;
			Integer count = 0;
			
			for(String res : newTasks.keySet())
			{
				if(newTasks.get(res).rFutureTasks.contains(task))
				{
					int taskIndex = newTasks.get(res).rFutureTasks.indexOf(task);
					double tDur = newTasks.get(res).rFutureDurations.elementAt(taskIndex);
					dur += tDur;
					count ++;
				}
			}
			
			Double avgDur = 0.0;
			
			if(count > 0)
				avgDur = dur/count;
			
			predTasksDur.put(task, avgDur);
			
		}
		
		//System.out.println("predTasksDur: " + predTasksDur);
		
		//6. Sort recommended tasks based on their durations - recommendation
		
		Vector<String> usedTasks = new Vector<String>();
		
		for(int i=0; i<predTasksDur.size(); i++)
		{
			double curMin = System.currentTimeMillis();
			String curtask = "";
			
			for(String t: predTasksDur.keySet())
				if(!usedTasks.contains(t) && predTasksDur.get(t) <= curMin)
				{
					curMin = predTasksDur.get(t);
					curtask = t;
				}
			
			if(max >= ip.minTaskSupport)
			{
			prediction.rFutureTasks.add(curtask);
			prediction.rFutureDurations.add(curMin);
			prediction.rPredResNum.add((long) max);
			}
			
			usedTasks.add(curtask);
			
		}
	}
		else
		{
			prediction.predicted = false;
		}
		
		//System.out.println("recTasks: " + prediction.rFutureTasks);
		//System.out.println("recTasksDur: " + prediction.rFutureDurations);
		
		return prediction;
	}
	
	

	//v3
		public FutureTasks findNewTasksWithinTime(Vector<String> rPastTasks, Date splitDate, Resource cR, InputParametersTR ip)
		{
			
			//1. Find start of search time - completion by cR of all tasks in rPastTasks
			Date searchStartTime = new Date(0);
			
			for(int i=0; i<rPastTasks.size(); i++)
				if(cR.rTasks.contains(rPastTasks.elementAt(i)))
				{
					int taskIndex = cR.rTasks.indexOf(rPastTasks.elementAt(i));
					Date taskDate = cR.rTimes.elementAt(taskIndex);
						if(taskDate.after(searchStartTime) && taskDate.before(splitDate))
							searchStartTime = taskDate;
				}
			
			//2. Find end of search time: searchStartTime + max duration
			Date searchEndTime = new Date(searchStartTime.getTime() + ip.maxTaskPredPeriod);
			FutureTasks newTasks = new FutureTasks(cR);
			
			//System.out.println("Split date: " + splitDate);
			//System.out.println("Search start and end: " + searchStartTime + " --- " + searchEndTime);
	
			//3. Find new tasks performed by cR between searchStartTime and searchEndTime and durations since searchStartTime	
		
		if(searchEndTime.before(splitDate) || searchEndTime.equals(splitDate))
			{
				newTasks.predicted = true;
				
				for(int i=0; i<cR.rTasks.size(); i++)
				{
					if(cR.rTimes.elementAt(i).after(searchStartTime) && 
							(cR.rTimes.elementAt(i).before(searchEndTime) || cR.rTimes.elementAt(i).equals(searchEndTime)))
					{
						newTasks.rFutureTasks.add(cR.rTasks.elementAt(i));
						Double duration = (double) ((cR.rTimes.elementAt(i).getTime() - searchStartTime.getTime()))/ip.durationTimeUnit;
						newTasks.rFutureDurations.add(duration);
					}
				}
			}
			else
			{
				newTasks.predicted = false;
			}
			
			return newTasks;
		}
		

	
	//v1
	public FutureTasks findNewTasks(Vector<String> rPastTasks, Date splitDate, Resource cR, InputParametersTR ip)
	{
		
		//1. Find start of search time - completion by cR of all tasks in rPastTasks
		Date searchStartTime = new Date(0);
		
		for(int i=0; i<rPastTasks.size(); i++)
			if(cR.rTasks.contains(rPastTasks.elementAt(i)))
			{
				int taskIndex = cR.rTasks.indexOf(rPastTasks.elementAt(i));
				Date taskDate = cR.rTimes.elementAt(taskIndex);
					if(taskDate.after(searchStartTime) && taskDate.before(splitDate))
						searchStartTime = taskDate;
			}
		
		//2. Find end of search time: split time + max duration
		Date searchEndTime = new Date(searchStartTime.getTime() + ip.maxTaskPredPeriod);
		if(searchEndTime.after(splitDate))
			searchEndTime = splitDate;
		
		//System.out.println("Split date: " + splitDate);
		//System.out.println("Search start and end: " + searchStartTime + " --- " + searchEndTime);
		
		//3. Find new tasks performed by cR between searchStartTime and searchEndTime and durations since searchStartTime
		FutureTasks newTasks = new FutureTasks(cR);
		for(int i=0; i<cR.rTasks.size(); i++)
		{
			if(cR.rTimes.elementAt(i).after(searchStartTime) && cR.rTimes.elementAt(i).before(searchEndTime))
			{
				newTasks.rFutureTasks.add(cR.rTasks.elementAt(i));
				Double duration = (double) ((cR.rTimes.elementAt(i).getTime() - searchStartTime.getTime()))/ip.durationTimeUnit;
				newTasks.rFutureDurations.add(duration);
			}
		}
		
		return newTasks;
	}
	
	
	//v2.2 - same as v2.1
	/*	public FutureTasks findFirstNewTasksNoNONE(Vector<String> rPastTasks, Date splitDate, Resource cR, InputParametersTR ip)
		{
			
			//1. Find start of search time - completion by cR of all tasks in rPastTasks
			Date searchStartTime = new Date(0);
			
			for(int i=0; i<rPastTasks.size(); i++)
				if(cR.rTasks.contains(rPastTasks.elementAt(i)))
				{
					int taskIndex = cR.rTasks.indexOf(rPastTasks.elementAt(i));
					Date taskDate = cR.rTimes.elementAt(taskIndex);
						if(taskDate.after(searchStartTime) && taskDate.before(splitDate))
							searchStartTime = taskDate;
				}
			
			//2. End of search time: split time
			Date searchEndTime = splitDate;
		
			//Date searchEndTime = new Date(searchStartTime.getTime() + ip.maxTaskPredPeriod);
			//if(searchEndTime.after(splitDate))
			//	searchEndTime = splitDate;
			
			//System.out.println("Split date: " + splitDate);
			//System.out.println("Search start and end: " + searchStartTime + " --- " + searchEndTime);
			
			//3. Find all new tasks performed by cR between searchStartTime and searchEndTime and durations since searchStartTime
			FutureTasks newTasks = new FutureTasks(cR);
			
			for(int i=0; i<cR.rTasks.size(); i++)
			{
				
				//System.out.println("cR.rTimes.elementAt(i): " + cR.rTimes.elementAt(i));
				if(cR.rTimes.elementAt(i).after(searchStartTime) && cR.rTimes.elementAt(i).before(searchEndTime)) 
				{
					newTasks.rFutureTasks.add(cR.rTasks.elementAt(i));
					Double duration = (double) ((cR.rTimes.elementAt(i).getTime() - searchStartTime.getTime()))/ip.durationTimeUnit;
					newTasks.rFutureDurations.add(duration);
				}
			}
			
			//System.out.println("newTasks.rFutureTasks: " + newTasks.rFutureTasks);
			//System.out.println("newTasks.rFutureDurations: " + newTasks.rFutureDurations);
			
			//4. Find min duration
			double minDuration = System.currentTimeMillis();
			for(int i=0; i<newTasks.rFutureTasks.size(); i++)
				if(newTasks.rFutureDurations.elementAt(i) < minDuration)
					minDuration = newTasks.rFutureDurations.elementAt(i);
			
			
			//5. Find last task index with min duration
			int lastMinDurIndex = -1; 
			for(int i=0; i<newTasks.rFutureTasks.size(); i++)
				if(newTasks.rFutureDurations.elementAt(i) == minDuration)
					lastMinDurIndex = i;
			
			//6. Keep only first future task/s
			while(newTasks.rFutureDurations.size() > (lastMinDurIndex+1))
			{
				newTasks.rFutureTasks.remove(newTasks.rFutureTasks.size()-1);
				newTasks.rFutureDurations.remove(newTasks.rFutureDurations.size()-1);
			}
			
			//System.out.println("first - newTasks.rFutureTasks: " + newTasks.rFutureTasks);
			//System.out.println("first - newTasks.rFutureDurations: " + newTasks.rFutureDurations);
			
			return newTasks;
		}
		
	*/
	
	public XLog removeEventsWithUnusedResources(XLog log, Vector<Resource> resHistories)
	{
		Set<String> resources = new HashSet<String>();
		for(Resource r : resHistories)
			resources.add(r.resourceName);
		
		
		XLog newLog = new XLogImpl(log.getAttributes());
		
		for (XTrace t : log) 
		{
			XTrace newTrace = new XTraceImpl(t.getAttributes());
			
				for (XEvent event : t) 
			{
					if(event.getAttributes().get("org:resource") != null)
						if(resources.contains(event.getAttributes().get("org:resource").toString()))
							newTrace.add(event);
			}
			
			newLog.add(newTrace);
		}
		
		return newLog;

	}
	
	//v2.1
	public FutureTasks findFirstNewTasks(Vector<String> rPastTasks, Date splitDate, Resource cR, InputParametersTR ip)
	{
		
		//1. Find start of search time - completion by cR of all tasks in rPastTasks
		Date searchStartTime = new Date(0);
		
		for(int i=0; i<rPastTasks.size(); i++)
			if(cR.rTasks.contains(rPastTasks.elementAt(i)))
			{
				int taskIndex = cR.rTasks.indexOf(rPastTasks.elementAt(i));
				Date taskDate = cR.rTimes.elementAt(taskIndex);
					if(taskDate.after(searchStartTime) && taskDate.before(splitDate))
						searchStartTime = taskDate;
			}
		
		//2. End of search time: split time
		Date searchEndTime = splitDate;
	
		//Date searchEndTime = new Date(searchStartTime.getTime() + ip.maxTaskPredPeriod);
		//if(searchEndTime.after(splitDate))
		//	searchEndTime = splitDate;
		
		//System.out.println("Split date: " + splitDate);
		//System.out.println("Search start and end: " + searchStartTime + " --- " + searchEndTime);
		
		//3. Find all new tasks performed by cR between searchStartTime and searchEndTime 
		// and durations since searchStartTime
		FutureTasks newTasks = new FutureTasks(cR);
		
		for(int i=0; i<cR.rTasks.size(); i++)
		{
			
			//System.out.println("cR.rTimes.elementAt(i): " + cR.rTimes.elementAt(i));
			if(cR.rTimes.elementAt(i).after(searchStartTime) && cR.rTimes.elementAt(i).before(searchEndTime)) 
			{
				newTasks.rFutureTasks.add(cR.rTasks.elementAt(i));
				//Double duration = (double) ((cR.rTimes.elementAt(i).getTime() - searchStartTime.getTime()))/ip.durationTimeUnit;
				Double duration = (double) ((cR.rTimes.elementAt(i).getTime() - searchStartTime.getTime()))/(double)ip.durationTimeUnit;
				//System.out.println("duration: " + duration);
				
				newTasks.rFutureDurations.add(duration);
			}
		}
		
		//System.out.println("newTasks.rFutureTasks: " + newTasks.rFutureTasks);
		//System.out.println("newTasks.rFutureDurations: " + newTasks.rFutureDurations);
		
		//4. Find min duration
		double minDuration = System.currentTimeMillis();
		for(int i=0; i<newTasks.rFutureTasks.size(); i++)
			if(newTasks.rFutureDurations.elementAt(i) < minDuration)
				minDuration = newTasks.rFutureDurations.elementAt(i);
		
		if(ip.considerDaysInLearning)
			minDuration += (double) ip.days;
		
		//5. Find last task index with min duration
		int lastMinDurIndex = -1; 
		for(int i=0; i<newTasks.rFutureTasks.size(); i++)
			if(newTasks.rFutureDurations.elementAt(i) <= minDuration)
				lastMinDurIndex = i;
		
		//System.out.println("lastMinDurIndex: " + lastMinDurIndex);
		//System.out.println("newTasks.rFutureTasks before: " + newTasks.rFutureTasks);
		
		//6. Keep only first future task/s (within days from first task)
		while(newTasks.rFutureDurations.size() > (lastMinDurIndex+1))
		{
			newTasks.rFutureTasks.remove(newTasks.rFutureTasks.size()-1);
			newTasks.rFutureDurations.remove(newTasks.rFutureDurations.size()-1);
		}
		
		//System.out.println("newTasks.rFutureTasks after: " + newTasks.rFutureTasks);
		
		//System.out.println("first - newTasks.rFutureTasks: " + newTasks.rFutureTasks);
		//System.out.println("first - newTasks.rFutureDurations: " + newTasks.rFutureDurations);
		
		return newTasks;
	}
	
	//v1
	public Map<String, Double> getResSimilarityTaskSet(Vector<String> rPastTasks, Map<String,Vector<String>> otherPastTasks)
	{
		Map<String, Double> resSim = new HashMap<String,Double>();
		
		for(String otherRes: otherPastTasks.keySet())
		{
			Double sim = getSim(rPastTasks,otherPastTasks.get(otherRes));
			resSim.put(otherRes, sim);
		}
		
		return resSim;
	}
	
	
	//v2
		public Map<String, Double> getResSimilarityTaskSetIntersection(Vector<String> rPastTasks, Map<String,Vector<String>> otherPastTasks)
		{
			Map<String, Double> resSim = new HashMap<String,Double>();
			
			for(String otherRes: otherPastTasks.keySet())
			{
				Double sim = getIntersection(rPastTasks,otherPastTasks.get(otherRes));
				resSim.put(otherRes, sim);
			}
			
			return resSim;
		}
		
		public Double getIntersection(Vector<String> rt, Vector<String> ot) // number of shared tasks/ size of the bigger task set
		{
			Double sim = 0.0;
			int count = 0;
			
			for(int i=0; i<rt.size(); i++)
				if(ot.contains(rt.elementAt(i)))
					count++;
			
			int biggerSetSize = rt.size();
			if(ot.size() > biggerSetSize)
				biggerSetSize = ot.size();
			
			
			sim = (double)count/(double)biggerSetSize;
			
			return sim;
		}
		
		public Double getSim(Vector<String> rt, Vector<String> ot) // 1: t1 is subset of t2
	{
		Double sim = 0.0;
		int count = 0;
		
		for(int i=0; i<rt.size(); i++)
			if(ot.contains(rt.elementAt(i)))
				count++;
		
		sim = (double)count/(double)rt.size();
		
		return sim;
	}
		
		//v3 
		public Measures getMeasuresTasksWithinTime(FutureTasks real, FutureTasks pred, InputParametersTR ip, Resource r) 
		{
			Measures m = new Measures(r.resourceName);
			
			//Was there a prediction?
			if(pred.predicted)
				m.resPrediction = 1.0;
			else
				m.resPrediction = 0.0;
			
			//If there was a prediction:
			if(m.resPrediction == 1.0)
			{
			
			//Get min. real duration
			//double minRealDur = System.currentTimeMillis();
			//for(int i=0; i<real.rFutureTasks.size(); i++)
			//	if(real.rFutureDurations.elementAt(i) < minRealDur)
			//		minRealDur = real.rFutureDurations.elementAt(i);
			
			//Real future tasks and tasks within time
			Set<String> realTasksWithinTime = new HashSet<String>();
			Set<String> realFutureTasks = new HashSet<String>();
			
			for(int i=0; i<real.rFutureTasks.size(); i++)
			{
				
				realFutureTasks.add(real.rFutureTasks.elementAt(i));
				
				if(real.rFutureDurations.elementAt(i) <= ip.days)
					realTasksWithinTime.add(real.rFutureTasks.elementAt(i));
			}
				
			
			if(realTasksWithinTime.size() == 0)
				realTasksWithinTime.add("NONE");
			
			if(realFutureTasks.size() == 0)
				realFutureTasks.add("NONE");
			
			//Set of predicted tasks
			Set<String> predictedTasks = new HashSet<String>();
			
			for(int i=0; i<pred.rFutureTasks.size(); i++)
				predictedTasks.add(pred.rFutureTasks.elementAt(i));
	
			m.performedOnePredTask = getSetIntersection(realTasksWithinTime,predictedTasks);
			m.performedEventually = getSetIntersection(realFutureTasks,predictedTasks);
			
			//v2 first task
	/*		for(int i=0; i<pred.rFutureTasks.size(); i++)
				if(realTasksWithinTime.contains(pred.rFutureTasks.elementAt(i)))
				{
					m.performedInTime = 1.0;
					m.performedEventually = 1.0;
				}
	*/		
			
			// durations
			Vector<Double> epRealDur = new Vector<Double>();
			Vector<Double> epPredDur = new Vector<Double>();
			
			//Eventually executed tasks
			//int count = 0;
			for(int i=0; i<pred.rFutureTasks.size(); i++)
				if(real.rFutureTasks.contains(pred.rFutureTasks.elementAt(i)))
				{
					//count++;
					int realIndex = real.rFutureTasks.indexOf(pred.rFutureTasks.elementAt(i));
					
					epRealDur.add(real.rFutureDurations.elementAt(realIndex));
					epPredDur.add(pred.rFutureDurations.elementAt(i));
					
				}
			
			//if(count > 0)
			//	m.performedEventually = 1.0;
			
			//For eventually performed tasks:
			//real AVG duration
			m.realDur = getAVG(epRealDur);
			
			//pred. AVG duration
			m.predDur = getAVG(epPredDur);
			
			//duration difference: subtract
			m.durDiffSub = Math.abs(m.realDur - m.predDur);
			
			//duration difference: divide
			m.durDiffDiv = m.predDur/m.realDur;
			}
			
			
			return m;
		}
		
		
		//v3.2
		public Measures getMeasuresTasksWithinTimeNONE(FutureTasks real, FutureTasks pred, InputParametersTR ip, Resource r) 
		{
			Measures m = new Measures(r.resourceName);
			
			//Was there a prediction?
			if(pred.predicted)
				m.resPrediction = 1.0;
			else
				m.resPrediction = 0.0;
			
			//If there was a prediction:
			if(m.resPrediction == 1.0)
			{
				//Real future tasks within time
				Set<String> realTasksWithinTime = new HashSet<String>();
				
				for(int i=0; i<real.rFutureTasks.size(); i++)
					if(real.rFutureDurations.elementAt(i) <= ip.days)
						realTasksWithinTime.add(real.rFutureTasks.elementAt(i));
				
				if(realTasksWithinTime.size() == 0)
					realTasksWithinTime.add("NONE");
				
				//Set of predicted tasks
				Set<String> predictedTasks = new HashSet<String>();
				
				for(int i=0; i<pred.rFutureTasks.size(); i++)
					predictedTasks.add(pred.rFutureTasks.elementAt(i));
		
				m.performedInTimeOrNONE = getSetIntersection(realTasksWithinTime,predictedTasks);
				
				if(realTasksWithinTime.contains("NONE") && predictedTasks.contains("NONE"))
					m.realPredNONE = 1.0;
				else
					m.realPredNONE = 0.0;
					
			}
			
			return m;
		}
		
		public double getSetIntersection(Set<String> set1, Set<String> set2)
		{
			double intersection = 0.0;
			
			int maxSize = 0;
			
			if(set1.size() > set2.size())
				maxSize = set1.size();
			else 
				maxSize = set2.size();
			
			int commonTasks = 0;
			
			for(String task: set1)
				if(set2.contains(task))
					commonTasks++;
			
			intersection = (double)commonTasks / (double) maxSize;
			
			return intersection;
		}
		
		
		//v2 
		public Measures getMeasuresFirstTask(FutureTasks real, FutureTasks pred, InputParametersTR ip, Resource r) 
		{
			Measures m = new Measures(r.resourceName);
			
			//Was there a prediction?
			if(pred.predicted)
				m.resPrediction = 1.0;
			else
				m.resPrediction = 0.0;
			
			//If there was a prediction:
			if(m.resPrediction == 1.0)
			{
			
			//First task
			//Get min. real duration
			double minRealDur = System.currentTimeMillis();
			for(int i=0; i<real.rFutureTasks.size(); i++)
				if(real.rFutureDurations.elementAt(i) < minRealDur)
					minRealDur = real.rFutureDurations.elementAt(i);
			
			Set<String> realFirstTasks = new HashSet<String>();
			for(int i=0; i<real.rFutureTasks.size(); i++)
				if(real.rFutureDurations.elementAt(i) == minRealDur)
					realFirstTasks.add(real.rFutureTasks.elementAt(i));
			
			if(real.rFutureTasks.size() == 0)
				realFirstTasks.add("NONE");
			
			m.performedOnePredTask = 0.0;
			m.performedEventually = 0.0;
			
			for(int i=0; i<pred.rFutureTasks.size(); i++)
				if(realFirstTasks.contains(pred.rFutureTasks.elementAt(i)))
				{
					m.performedOnePredTask = 1.0;
					m.performedEventually = 1.0;
				}
			
			Vector<Double> epRealDur = new Vector<Double>();
			Vector<Double> epPredDur = new Vector<Double>();
			
			//Eventually executed tasks
			int count = 0;
			for(int i=0; i<pred.rFutureTasks.size(); i++)
				if(real.rFutureTasks.contains(pred.rFutureTasks.elementAt(i)))
				{
					count++;
					int realIndex = real.rFutureTasks.indexOf(pred.rFutureTasks.elementAt(i));
					
					epRealDur.add(real.rFutureDurations.elementAt(realIndex));
					epPredDur.add(pred.rFutureDurations.elementAt(i));
					
				}
			
			if(count > 0)
				m.performedEventually = 1.0;
			
			//For eventually performed tasks:
			//real AVG duration
			m.realDur = getAVG(epRealDur);
			
			//pred. AVG duration
			m.predDur = getAVG(epPredDur);
			
			//duration difference: subtract
			m.durDiffSub = Math.abs(m.realDur - m.predDur);
			
			//duration difference: divide
			m.durDiffDiv = m.predDur/m.realDur;
			}
			
			
			return m;
		}
		
		
		//v2.2 
			public Measures getMeasuresFirstTaskNoNONE(FutureTasks real, FutureTasks pred, InputParametersTR ip, Resource r) 
		{
			Measures m = new Measures(r.resourceName);
			m.resourcePastTasks = (double) pred.res.rPastTasks.size();
			
			//Was there a prediction?
			if(pred.predicted)
				m.resPrediction = 1.0;
			else
				m.resPrediction = 0.0;
			
			//If there was a prediction:
			if(m.resPrediction == 1.0)
			{
				
				for(int i=0; i<pred.rFutureTasks.size(); i++)
					if(real.rFutureTasks.contains(pred.rFutureTasks.elementAt(i)))
						m.performedOnePredTask = 1.0;
					else
						m.performedOnePredTask = 0.0;
			}
			
			return m;
		}
			
			
			//v2.3 - 4 measures
			public Measures getMeasuresFirstTasks(FutureTasks real, FutureTasks pred, InputParametersTR ip, Resource r) 
			{
				Measures m = new Measures(r.resourceName);
				m.resourcePastTasks = (double) pred.res.rPastTasks.size();
				
				//Was there a prediction?
				if(pred.predicted)
					m.resPrediction = 1.0;
				else
					m.resPrediction = 0.0;
				
				//If there was a prediction:
				if(m.resPrediction == 1.0)
				{
					
					m.performedOnePredTask = getOneTaskMeasure(pred.rFutureTasks,real.rFutureTasks);
					
					m.performedAllPredTasks = getAllPredTasksMeasure(pred.rFutureTasks,real.rFutureTasks);
					
					m.recommendedAllRealTasks = getAllRealTasksMeasure(pred.rFutureTasks,real.rFutureTasks);
					
					m.predRealIntersection = getIntersectionMeasure(pred.rFutureTasks,real.rFutureTasks);
					
				}
				
				return m;
			}
			
			public Double getOneTaskMeasure(Vector<String> pred, Vector<String> real)
			{
				Double m = 0.0;
				
				for(int i=0; i<pred.size(); i++)
					if(real.contains(pred.elementAt(i)))
						m = 1.0;
				
				return m;
			}
			
			public Double getAllPredTasksMeasure(Vector<String> pred, Vector<String> real)
			{
				Double m = 0.0;
				int count = 0;
				
				for(int i=0; i<pred.size(); i++)
					if(real.contains(pred.elementAt(i)))
						count++;
				
				m = (double) count / (double) pred.size();
				
				return m;
			}
			
			public Double getAllRealTasksMeasure(Vector<String> pred, Vector<String> real)
			{
				Double m = 0.0;				
				int count = 0;
				
				for(int i=0; i<real.size(); i++)
					if(pred.contains(real.elementAt(i)))
						count++;
				
				m = (double) count / (double) real.size();
				
				return m;
			}
			
			public Double getIntersectionMeasure(Vector<String> pred, Vector<String> real)
			{
				Double m = 0.0;
				int count = 0;
				
				for(int i=0; i<pred.size(); i++)
					if(real.contains(pred.elementAt(i)))
						count++;
				
				int maxSize = pred.size();
				if(real.size() > maxSize)
					maxSize = real.size();
				
				m = (double) count / (double) maxSize;
				
				return m;
			}
		
	
		//v1
		public Measures getMeasures(FutureTasks real, FutureTasks pred, InputParametersTR ip, Resource r) 
	{
		Measures m = new Measures(r.resourceName);
		
		//Was there a prediction?
		if(pred.rFutureTasks.size()>0)
		//if(pred.predicted)
			m.resPrediction = 1.0;
		else
			m.resPrediction = 0.0;
		
		//If there was a prediction:
		if(m.resPrediction == 1.0)
		{
		
		//First task
		
		//Get min. real duration
		//double minRealDur = System.currentTimeMillis();
		//for(int i=0; i<real.rFutureTasks.size(); i++)
		//	if(real.rFutureDurations.elementAt(i) < minRealDur)
		//		minRealDur = real.rFutureDurations.elementAt(i);
			
		if(real.rFutureTasks.size()>0)
			if(real.rFutureTasks.elementAt(0).equals(pred.rFutureTasks.elementAt(0)))
				m.performedOnePredTask = 1.0;
			else 
				m.performedOnePredTask = 0.0;
		
		Vector<Double> epRealDur = new Vector<Double>();
		Vector<Double> epPredDur = new Vector<Double>();
		
		//Fraction of eventually executed tasks
		int count = 0;
		for(int i=0; i<pred.rFutureTasks.size(); i++)
			if(real.rFutureTasks.contains(pred.rFutureTasks.elementAt(i)))
			{
				count++;
				int realIndex = real.rFutureTasks.indexOf(pred.rFutureTasks.elementAt(i));
				
				epRealDur.add(real.rFutureDurations.elementAt(realIndex));
				epPredDur.add(pred.rFutureDurations.elementAt(i));
				
			}
		
		m.performedEventually = (double)count/(double)pred.rFutureTasks.size();
		
		//For eventually performed tasks:
		//real AVG duration
		m.realDur = getAVG(epRealDur);
		
		//pred. AVG duration
		m.predDur = getAVG(epPredDur);
		
		//duration difference: subtract
		m.durDiffSub = Math.abs(m.realDur - m.predDur);
		
		//duration difference: divide
		m.durDiffDiv = m.predDur/m.realDur;
		}
		
		
		return m;
	}
	
	public Double getAVG(Vector<Double> d)
	{
		Double avg = -1.0;
		Double sum = 0.0;
		
		for(int i=0; i<d.size(); i++)
			sum += d.elementAt(i);
		
		if(d.size() > 0)
			avg = sum/d.size();
		
		return avg;
	}

	
	public Measures getAVGMeasures(Vector<Measures> m, InputParametersTR ip) {
		Measures avg = new Measures("AVG");
		
		Vector<Double> resPred = new Vector<Double>(); 
		Vector<Double> performedFirst = new Vector<Double>(); 
		Vector<Double> performedEventually = new Vector<Double>(); 
		Vector<Double> realDur = new Vector<Double>(); 	
		Vector<Double> predDur = new Vector<Double>(); 
		Vector<Double> durDiffSub = new Vector<Double>(); 
		Vector<Double> durDiffDiv = new Vector<Double>(); 
		
		for(int i=0; i<m.size(); i++)
		{
			resPred.add(m.elementAt(i).resPrediction);
			
			if(m.elementAt(i).resPrediction > 0)
			{
				performedFirst.add(m.elementAt(i).performedOnePredTask);
				performedEventually.add(m.elementAt(i).performedEventually);
				realDur.add(m.elementAt(i).realDur);
				predDur.add(m.elementAt(i).predDur);
				durDiffSub.add(m.elementAt(i).durDiffSub);
				durDiffDiv.add(m.elementAt(i).durDiffDiv);
			}
		}
		
		avg.resPrediction = getAVG(resPred);
		avg.performedOnePredTask = getAVG(performedFirst);
		avg.performedEventually = getAVG(performedEventually);
		avg.realDur = getAVG(realDur);
		avg.predDur = getAVG(predDur);
		avg.durDiffSub = getAVG(durDiffSub);
		avg.durDiffDiv = getAVG(durDiffDiv);
				
		return avg;
	}

	//v2.2
	public Measures getAVGMeasuresFirstTask(Vector<Measures> m, InputParametersTR ip) {
		Measures avg = new Measures("AVG");
		
		Vector<Double> numPastTasks = new Vector<Double>();
		Vector<Double> resPred = new Vector<Double>(); 
		Vector<Double> performedOnePredTask = new Vector<Double>(); 
		Vector<Double> performedAllPredTasks = new Vector<Double>(); 
		Vector<Double> recommendedAllRealTasks = new Vector<Double>(); 
		Vector<Double> predRealIntersection = new Vector<Double>(); 
	
		for(int i=0; i<m.size(); i++)
		{
			resPred.add(m.elementAt(i).resPrediction);
				
			if(m.elementAt(i).resPrediction > 0)
			{
				numPastTasks.add(m.elementAt(i).resourcePastTasks);
				performedOnePredTask.add(m.elementAt(i).performedOnePredTask);
				performedAllPredTasks.add(m.elementAt(i).performedAllPredTasks);
				recommendedAllRealTasks.add(m.elementAt(i).recommendedAllRealTasks);
				predRealIntersection.add(m.elementAt(i).predRealIntersection);
			}
		}
		
		avg.resourcePastTasks = getAVG(numPastTasks);
		avg.resPrediction = getAVG(resPred);
		avg.performedOnePredTask = getAVG(performedOnePredTask);
		avg.performedAllPredTasks = getAVG(performedAllPredTasks);
		avg.recommendedAllRealTasks = getAVG(recommendedAllRealTasks);
		avg.predRealIntersection = getAVG(predRealIntersection);
				
		return avg;
	}



	//v3
	public Measures getAVGMeasuresTasksWithinTime(Vector<Measures> m, InputParametersTR ip) {
		Measures avg = new Measures("AVG");
		
		Vector<Double> resPred = new Vector<Double>(); 
		Vector<Double> performedInTimeOrNONE = new Vector<Double>(); 
		Vector<Double> performedInTime = new Vector<Double>(); 
		Vector<Double> noneNone = new Vector<Double>(); 	

		for(int i=0; i<m.size(); i++)
		{
			resPred.add(m.elementAt(i).resPrediction);
			
			if(m.elementAt(i).resPrediction > 0)
			{
				performedInTimeOrNONE.add(m.elementAt(i).performedInTimeOrNONE);
				
				if(m.elementAt(i).realPredNONE > 0)
				{
					noneNone.add(1.0);
				}
				else
				{
					noneNone.add(0.0);
					performedInTime.add(m.elementAt(i).performedInTimeOrNONE);
				}
			}
		}
		
		avg.resPrediction = getAVG(resPred);
		avg.performedInTimeOrNONE = getAVG(performedInTimeOrNONE);
		avg.performedOnePredTask = getAVG(performedInTime);
		avg.realPredNONE = getAVG(noneNone);
				
		return avg;
	}



}



// prev.

/*	public Set<String> getPastTasksSet(Resource r, Date splitTime)
{
	Set<String> rPastTasks = new HashSet<String>();
	
	for(int i=0; i<r.rTimes.size(); i++)
		if(r.rTimes.elementAt(i).before(splitTime))
			rPastTasks.add(r.rTasks.elementAt(i));
	
	return rPastTasks;
}
*/

/*
public Vector<String> getTaskTimeSeq(Resource r)
{
	String rTasks = "";
	String rTimes = "";
	
	System.out.println(r.resourceName);
	
	Vector<String> tasks = new Vector<String>();
	
	for(String task:r.taskStart.keySet())
		tasks.add(task);
	
	//Collections.sort(tasks, Collections.reverseOrder());
	
	int rep = tasks.size();
	
	for(int i=0; i<rep; i++)
	{
		Date currentStartDate = new Date(System.currentTimeMillis());
		String currentStartTask = "";
		
		for(String task:tasks)
		{
			Date startDate = r.taskStart.get(task);
			if(startDate.before(currentStartDate) || startDate.equals(currentStartDate))
				{
					currentStartDate = startDate;
					currentStartTask = task;
				}
		}
		
		rTasks += currentStartTask + ",";
		rTimes += currentStartDate + ",";
		
		tasks.remove(currentStartTask);

		
	}
	
	System.out.println(rTasks);
	System.out.println(rTimes);
	
	rTasks = rTasks.substring(0, rTasks.length() - 1);
	rTasks += "\r\n";
	rTimes = rTimes.substring(0, rTimes.length() - 1);
	rTimes += "\r\n";
	
	Vector<String> out = new Vector<String>();
	out.add(rTasks);
	out.add(rTimes);
				
	return out;
}
*/


