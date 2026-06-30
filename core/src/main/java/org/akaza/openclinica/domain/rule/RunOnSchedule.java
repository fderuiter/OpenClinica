package org.akaza.openclinica.domain.rule;

import javax.xml.bind.annotation.*;

import java.security.Timestamp;

@XmlRootElement(name="RunOnSchedule")
@XmlAccessorType(XmlAccessType.FIELD)
public class RunOnSchedule {

	@XmlAttribute(name="Time")
    private String runTime;

	
	public RunOnSchedule() {
		super();
	}


	public RunOnSchedule(String runTime){
		this.runTime=runTime;
		
	}


	public String getRunTime() {
		return runTime;
	}


	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}


	
	

	
	
	
	
}
