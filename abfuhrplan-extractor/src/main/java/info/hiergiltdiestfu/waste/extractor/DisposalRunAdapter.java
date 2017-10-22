package info.hiergiltdiestfu.waste.extractor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.hiergiltdiestfu.waste.extractor.model.DisposalRun;
import info.hiergiltdiestfu.waste.extractor.model.WasteType;

public abstract class DisposalRunAdapter {

	private final static Pattern dateExtract = Pattern.compile("\\S+ ([\\d\\.]{10}).*");
	private final static DateFormat dateParser;
	static {
		DateFormat inter = DateFormat.getDateInstance();
		try {
			((SimpleDateFormat) inter).applyPattern("dd.MM.yyyy");
		} catch (Throwable t) {
			inter = new SimpleDateFormat("dd.MM.yyyy");
		}
		dateParser = inter;
	}

	public static synchronized final DisposalRun build(String typeName, String date, String interval) throws Throwable {
		final WasteType type = WasteType.fromValue(typeName);

		// System.err.println("Date from API looks like this: "+date);
		final Matcher matcher = dateExtract.matcher(date);
		if (matcher.matches()) {
			final String extract = matcher.group(1);
			// System.err.println("Extracted looks like "+extract);
			Date runTime = dateParser.parse(extract);
			// System.err.println("Parsed and reformatted? "+ dateParser.format(parsed));

			final DisposalRun result = new DisposalRun();
			result.setType(type);
			result.setInterval(interval);
			result.setRunTime(runTime);
			return result;
		}

		throw new IllegalArgumentException("Could not extract next run. Did the timestamp format change? " + date);
	}
}
