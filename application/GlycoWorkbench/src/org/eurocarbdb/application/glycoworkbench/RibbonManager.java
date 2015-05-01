package org.eurocarbdb.application.glycoworkbench;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eurocarbdb.application.glycanbuilder.Context;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.RibbonContextualTaskGroup;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

public class RibbonManager {
	public enum RibbonTaskChangeState {
		SET_TO_DEFAULT_BAND,
		CURRENT_BAND_SUPPORTS_CONTEXT,
		NO_BAND_SUPPORTS_CONTEXT;
	}
	
	protected Map<Context,RibbonTask> defaultContextRibbonTask;
	protected Map<RibbonTask,Set<Context>> ribbonTaskContextSupport;
	protected JRibbon jRibbon;
	
	protected RibbonTask enabledTask;
	protected RibbonTask lastTask;
	
	protected Context defaultContext;
	
	public void setDefaultContext(Context context){
		defaultContext=context;
	}
	
	public RibbonManager(JRibbon _jRibbon){
		jRibbon=_jRibbon;
		defaultContextRibbonTask=new HashMap<Context,RibbonTask>();
		ribbonTaskContextSupport=new HashMap<RibbonTask,Set<Context>>();
	}
	
	public void setRibbonAsDefault(RibbonTask ribbonTask,Context context){
		defaultContextRibbonTask.put(context, ribbonTask);
	}
	
	public void registerContextSupport(RibbonTask ribbonTask,Set<Context> contexts){
		ribbonTaskContextSupport.put(ribbonTask, contexts);
	}
	
	public RibbonTaskChangeState setCurrentContext(Context context,boolean switchToDefault){
		synchronized(jRibbon){
			setAllAssociatedTasksVisible(context,true);
			
			Set<Context> contexts=getRibbonTaskContexts(jRibbon.getSelectedTask());
			if(contexts==null || !contexts.contains(context)){
				RibbonTask defaultRibbonTask=defaultContextRibbonTask.get(context);
				if(defaultRibbonTask !=null ){
					lastTask=jRibbon.getSelectedTask();
					enabledTask=defaultRibbonTask;
					
					if(switchToDefault){
						jRibbon.setSelectedTask(defaultRibbonTask);	
					}
					
					return RibbonTaskChangeState.SET_TO_DEFAULT_BAND;
				}else{
					return RibbonTaskChangeState.NO_BAND_SUPPORTS_CONTEXT;
				}
			}else{
				return RibbonTaskChangeState.CURRENT_BAND_SUPPORTS_CONTEXT; 
			}
		}
	}
	
	public void setAllAssociatedTasksVisible(Context context,boolean visible){
		synchronized(jRibbon){
			boolean rememberLast=false;
			for(RibbonTask ribbonTask:ribbonTaskContextSupport.keySet()){
				if(ribbonTaskContextSupport.get(ribbonTask).contains(context)){
					RibbonContextualTaskGroup group=ribbonTask.getContextualGroup();
					if(group!=null){
						if(jRibbon.isVisible(group)!=visible){
							if(visible){
								rememberLast=true;
							}
							jRibbon.setVisible(group, visible);
						}
					}
				}
			}
			if(rememberLast){
				lastTask=jRibbon.getSelectedTask();
			}
			
		}
	}
	
	public void undoContextChange(Context context){
		synchronized(jRibbon){
			RibbonTask selectedTask=jRibbon.getSelectedTask();
			if(enabledTask!=null){
				RibbonContextualTaskGroup group=enabledTask.getContextualGroup();
				if(group!=null){
					for(int i=0;i<group.getTaskCount();i++){
						
						RibbonTask taskOnGroup=group.getTask(i);
						if(taskOnGroup==selectedTask){
							jRibbon.setSelectedTask(lastTask);
							break;
						}
					}
				}
			}
			
			setAllAssociatedTasksVisible(context, false);	
		}
	}
	
	public Set<Context> getRibbonTaskContexts(RibbonTask task){
		return ribbonTaskContextSupport.get(task);
	}

}
