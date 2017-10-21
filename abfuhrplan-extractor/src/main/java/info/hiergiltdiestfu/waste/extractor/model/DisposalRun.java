package info.hiergiltdiestfu.waste.extractor.model;

import java.text.DateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisposalRun {

	private final static Pattern dateExtract = Pattern.compile("\\S+ ([\\d\\.]{10}).*");
	private final static DateFormat dateParser = DateFormat.getDateInstance();
	
	private WasteType type;
	private Date runTime;
	private String interval;
	
	public String getInterval() {
		return interval;
	}
	public void setInterval(String interval) {
		this.interval = interval;
	}
	public DisposalRun(String typeName, String date, String interval) throws Throwable {
		this.type = WasteType.fromValue(typeName);
		this.interval = interval;
		
//		System.err.println("Date from API looks like this: "+date);
		final Matcher matcher = dateExtract.matcher(date);
		if (matcher.matches()) {
			final String extract = matcher.group(1);
//			System.err.println("Extracted looks like "+extract);
			runTime = dateParser.parse(extract);
//			System.err.println("Parsed and reformatted? "+ dateParser.format(parsed));
		}
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
