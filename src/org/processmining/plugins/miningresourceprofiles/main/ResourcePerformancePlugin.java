package org.processmining.plugins.miningresourceprofiles.main;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import com.fluxicon.slickerbox.components.HeaderBar;

public class ResourcePerformancePlugin {
	static MainGUILeft maingui = new MainGUILeft();
	
		@Plugin(
			name = "Task Recommender", 
			parameterLabels = { "Event log" }, 
			returnLabels = {"Resource Analysis Results"}, 
			returnTypes = {HeaderBar.class}, 
			userAccessible = true, 
			help = ""
		)
		@PluginVariant(variantLabel = "Task Recommender", requiredParameterLabels = { 0 })
		@UITopiaVariant(
			affiliation = "QUT", 
			author = "A.Pika", 
			email = "a.pika@qut.edu.au"
		)

public HeaderBar  main (UIPluginContext context, XLog inputlog) throws Exception {
		
			Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
			return maingui.displayMainGUI(inputlog);

}
}
