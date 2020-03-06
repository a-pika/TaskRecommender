package org.processmining.plugins.miningresourceprofiles.main;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JLabel;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.miningresourceprofiles.taskRecommender.Analysis;
import org.processmining.plugins.miningresourceprofiles.taskRecommender.FutureTasks;
import org.processmining.plugins.miningresourceprofiles.taskRecommender.InputParametersTR;
import org.processmining.plugins.miningresourceprofiles.taskRecommender.Measures;
import org.processmining.plugins.miningresourceprofiles.taskRecommender.Resource;

import com.fluxicon.slickerbox.components.HeaderBar;

public class MainGUILeft{
	
 
	public HeaderBar displayMainGUI(final XLog log) throws Exception
   {
		//TODO - specify path for the output
		String filePathSummary = "C:\\temp\\_summary.csv";
		
		Analysis an = new Analysis();
		Vector<InputParametersTR> ipVec = new Vector<InputParametersTR>();
		Vector<Measures> measuresVec = new Vector<Measures>();
		long startTime = System.currentTimeMillis();			
		
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date   logStart       = format.parse ( "2010-10-02 17:20:39" );   // wabo-r
		//Date   logStart       = format.parse ( "2011-10-01 08:38:44" );   // bpic12
		//Date   logStart       = format.parse ( "2016-01-01 19:51:15" );   // bpic17-6m
		
		
		Vector<Integer> minTasksResFilter = new Vector<Integer>();
		minTasksResFilter.add(0);
		//minTasksResFilter.add(8); //bpic12
		minTasksResFilter.add(9); //bpic17, wabo-r
		//minTasksResFilter.add(13); //bpic17
		//minTasksResFilter.add(12); //bpic12
		minTasksResFilter.add(14); //wabo-r
		
		Vector<Integer> minDurationResFilter = new Vector<Integer>(); //in days
		minDurationResFilter.add(0);
		//minDurationResFilter.add(30); 
		//minDurationResFilter.add(45); 
		//minDurationResFilter.add(60);
		
		Vector<Double> splitPoints = new Vector<Double>();
		//splitPoints.add(0.25);
		splitPoints.add(0.50);
		//splitPoints.add(0.66);
		//splitPoints.add(0.75);
		//splitPoints.add(0.80);
		//splitPoints.add(0.90);

		Vector<Long> durations = new Vector<Long>(); //days
		durations.add((long) 0); 
		//durations.add((long) 7); 
		durations.add((long) 30); 
									
		Vector<Integer> minPastTasks = new Vector<Integer>();
		//minPastTasks.add(3);
		//minPastTasks.add(4);
		minPastTasks.add(5);
		//minPastTasks.add(6);
		//minPastTasks.add(7);
		//minPastTasks.add(8);
		//minPastTasks.add(9);
		//minPastTasks.add(10);
		//minPastTasks.add(12);
		//minPastTasks.add(15);
			
		Vector<Double> resourceSim = new Vector<Double>();
		//resourceSim.add(0.1);
		//resourceSim.add(0.2);					
		//resourceSim.add(0.3);
		//resourceSim.add(0.4);
		//resourceSim.add(0.5);
		resourceSim.add(0.6);
		resourceSim.add(0.7);					
		resourceSim.add(0.8);
		//resourceSim.add(0.9);
		//resourceSim.add(1.0);
			
		Vector<Integer> simResources = new Vector<Integer>();
		//simResources.add(1); 
		simResources.add(2); 
		simResources.add(3);
		simResources.add(4); 
/*					simResources.add(5);
		simResources.add(6); 
		simResources.add(7); 
		simResources.add(8);
		simResources.add(9); 
		simResources.add(10); 
*/					
		Vector<Double> taskSupport = new Vector<Double>();
		//taskSupport.add(0.1);
		//taskSupport.add(0.2);
		//taskSupport.add(0.3);
		//taskSupport.add(0.4);
		//taskSupport.add(0.5);
		taskSupport.add(0.6);
		taskSupport.add(0.7);
		taskSupport.add(0.8);
		//taskSupport.add(0.9);
		//taskSupport.add(1.0);
	

//for(Integer minDurFilter : minDurationResFilter)	
//	{
		
for(Integer minTasksFilter : minTasksResFilter)	
{
	Set<String> resources = new HashSet<String>();
	resources = an.getResourcesTaskFilter(log,minTasksFilter); //'minimum tasks' filter
	
	Vector<Resource> resourceHistories = new Vector<Resource>();
	
	for(String r:resources)
	{
		Resource res = an.getResourceHistoryNoSplitTime(r, log);
		//double resDur = (double)(res.resourceEnd.getTime() - res.resourceStart.getTime())/(double)(3600000*24);
		//'minimum duration' filter
		//if(resDur >= minDurFilter)					
		 resourceHistories.add(res);
	}
			
	for(Resource r:resourceHistories)
		r = an.getRTasksTimes(r);
	
	
	//////////////////////////////////
for(Integer minDurFilter : minDurationResFilter)	
for(Double splitPoint : splitPoints)		
for(Long days: durations)
	for(Integer minPTasks: minPastTasks)	
		for(Double resSim : resourceSim)
			for(Integer simRes : simResources)
				for(Double taskSup : taskSupport)
					
	{	
			Vector<Measures> m = new Vector<Measures>();
			InputParametersTR ip = new InputParametersTR(minTasksFilter, minDurFilter, days, resSim, simRes, taskSup, minPTasks, splitPoint, resourceHistories.size());
			//ipVec.add(ip);
		
		
		for(Resource r:resourceHistories)
			//r = an.setSplitTimeNumberOfTasks(r, ip);
			r = an.setSplitTimeFractionOfTime(r, ip);
		
		Set<String> newEmployees = an.getNewEmployees(resourceHistories, ip, logStart);
		ip.numberOfResources = newEmployees.size();
		
		ipVec.add(ip);
		/////////////////////////////
				
		for(int i=0; i<resourceHistories.size(); i++)
		{
			// Select resource and split date
			Resource curR = resourceHistories.elementAt(i);
			Date splitDate = curR.resourceSplitTime;
			
			
			if(newEmployees.contains(curR.resourceName))
			{
				
				// Get r's first future tasks - first task started after or on split date 
				FutureTasks rFutureTasks = new FutureTasks(curR);
				rFutureTasks.getFirstFutureTasks(ip);
				
				// v2.2 'first task' prediction strategy - NONE is not a valid prediction; ignore resources 
				FutureTasks rPredFutureTasks = an.getFirstTaskPredictionNoNONE(curR, splitDate, resourceHistories, ip, i);
				
				// v2.3 'first task/s' prediction strategy - no NONE - 4 measures
				Measures rMeasures = an.getMeasuresFirstTasks(rFutureTasks, rPredFutureTasks, ip, curR);
				m.add(rMeasures);
			}
									
		}
		
	Measures avgMeasures = an.getAVGMeasuresFirstTask(m,ip);
	measuresVec.add(avgMeasures);
	
	System.out.println("----------------------------------------------------------------");
	ip.printConfiguration();
	avgMeasures.printMeasuresFirstTask();
	
}	//End of ip configuration 
	}	//End of min. tasks res. filter
		//} //End of min. duration res. filter
		
		//String filePathSummary = "C:\\temp\\_summary.csv";
		File csvSum = an.saveSummaryFirstTasksNoSplit4M(ipVec, measuresVec, filePathSummary);
		
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		double minutes = (double)duration/(60000);
		System.out.println("minutes: " + minutes);	
	//TODO - end
		

		
	final HeaderBar pane = new HeaderBar("");
	pane.setLayout(new GridBagLayout());
	final GridBagConstraints c = new GridBagConstraints();
	final HeaderBar mainPane = new HeaderBar("");
	mainPane.setLayout(new GridBagLayout());
	
	JLabel TRlab = new JLabel("Results are in: " + filePathSummary);
	TRlab.setHorizontalAlignment(JLabel.CENTER);
	TRlab.setForeground(Color.WHITE);
	mainPane.add(TRlab);
	pane.add(mainPane,c);
   
   return pane;
   } 
}

