package info.hiergiltdiestfu.waste.extractor.model;

import java.util.Date;

public class DisposalRun {

	private WasteType type;
	private Date runTime;
	private String interval;
	
	public String getInterval() {
		return interval;
	}
	public void setInterval(String interval) {
		this.interval = interval;
	}
	public DisposalRun() {	}
	
	public WasteType getType() {
		return type;
	}
	public void setType(WasteType type) {
		this.type = type;
	}
	public Date getRunTime() {
		return runTime;
	}
	public void setRunTime(Date runTime) {
		this.runTime = runTime;
	}
	
}
