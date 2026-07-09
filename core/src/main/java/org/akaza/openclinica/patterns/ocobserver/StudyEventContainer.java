package org.akaza.openclinica.patterns.ocobserver;



public class StudyEventContainer {
	private Object event = null;
	private StudyEventChangeDetails changeDetails = null;
	
	public StudyEventContainer(Object event, StudyEventChangeDetails changeDetails)
	{
		this.event = event;
		this.changeDetails = changeDetails;
	}

	public Object getEvent() {
		return event;
	}

	public void setEvent(Object event) {
		this.event = event;
	}

	public StudyEventChangeDetails getChangeDetails() {
		return changeDetails;
	}

	public void setChangeDetails(StudyEventChangeDetails changeDetails) {
		this.changeDetails = changeDetails;
	}

}
