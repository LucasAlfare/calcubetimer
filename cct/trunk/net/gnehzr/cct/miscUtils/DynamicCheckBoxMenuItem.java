package net.gnehzr.cct.miscUtils;

import javax.swing.JCheckBoxMenuItem;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.Configuration.ConfigurationChangeListener;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

@SuppressWarnings("serial")
public class DynamicCheckBoxMenuItem extends JCheckBoxMenuItem implements StatisticsUpdateListener, DynamicStringSettable, ConfigurationChangeListener{
	private DynamicString s;

	public DynamicCheckBoxMenuItem(){
		s = null;
		Configuration.addConfigurationChangeListener(this);
	}

	public DynamicCheckBoxMenuItem(DynamicString s){
		setDynamicString(s);
	}

	public void setDynamicString(DynamicString s){
		if(s != null){
			s.getStatistics().removeStatisticsUpdateListener(this);
		}
		this.s = s;
		if(s != null){
			s.getStatistics().addStatisticsUpdateListener(this);
			update();
		}
	}
	
	public void update(){
		if(s != null) setText(s.toString());
	}

	public void configurationChanged(){
		update();
	}
}