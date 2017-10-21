package info.hiergiltdiestfu.waste.extractor.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class NextDisposalRuns {

	private final List<DisposalRun> nextRunPerDisposalType = new LinkedList<>();

	private final String validFor;
	 
	private final Date validOn;
	
	public NextDisposalRuns(String validFor) {
		super();
		this.validFor = validFor;
		this.validOn = new Date();
	}

	public List<DisposalRun> getNextRunPerDisposalType() {
		return nextRunPerDisposalType;
	}

	public String getValidFor() {
		return validFor;
	}

	public Date getValidOn() {
		return validOn;
	}

}
