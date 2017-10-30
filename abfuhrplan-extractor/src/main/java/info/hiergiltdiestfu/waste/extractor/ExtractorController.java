package info.hiergiltdiestfu.waste.extractor;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.*;

import javax.ws.rs.QueryParam;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netflix.util.Pair;

import info.hiergiltdiestfu.waste.extractor.model.DisposalRun;
import info.hiergiltdiestfu.waste.extractor.model.Health;
import info.hiergiltdiestfu.waste.extractor.model.NextDisposalRuns;
import info.hiergiltdiestfu.waste.extractor.model.WasteType;

@Controller
public class ExtractorController {

	private final Map<String, Pair<Long, NextDisposalRuns>> CACHE = new HashMap<>(MAX_CACHE_ENTRIES / 4);	
	
	private static final long MAX_CACHE_AGE_MILLIS = 1000*60*60;
	private static final int MAX_CACHE_ENTRIES = 2*4;//64*4;
	private static final String SR_API_HEAD_URI_WITH_PARAMS = "http://stadtplan.dresden.de/project/cardo3Apps/IDU_DDStadtplan/abfall/detailpage.aspx?POS-ADR=%s|%s";
	@ResponseBody
	@RequestMapping(path="/health-description")
	public Health health() {
		return new Health(100, "Looking good");
		//TODO test availability of SR API and report on that
		//TODO change context path to /health for eureka compat
	}
	
	@ResponseBody
	@RequestMapping(path="/next-disposal")
	public NextDisposalRuns nextDisposal(@QueryParam("street") String street, @QueryParam("number") String number) throws Throwable {
		if (CACHE.size()>=MAX_CACHE_ENTRIES) {

			System.err.println("CACHE: Expunging entries that are too old");
			Collection<Pair<Long, NextDisposalRuns>> values = CACHE.values();
			List<Pair<Long, ?>> tooOld = values.stream().filter(p->p.first()<=System.currentTimeMillis()-MAX_CACHE_AGE_MILLIS).collect(Collectors.toList());
			values.removeAll(tooOld);
			System.err.println(String.format("CACHE: That freed up %d entries", tooOld.size()));

			if (CACHE.size()>=MAX_CACHE_ENTRIES) {
				System.err.println("CACHE: But that wasn't enough. Now I'm gonna choose at random, sorry.");
				values = CACHE.values();
				List<?> random = values.stream().filter(p->Math.random()>0.75f).collect(Collectors.toList());
				values.removeAll(random);
				System.err.println(String.format("CACHE: That freed up %d entries", random.size()));
			}

		}

		if (CACHE.containsKey(street+number)) {
			final Pair<Long, NextDisposalRuns> cached = CACHE.get(street+number);
			if (System.currentTimeMillis()-cached.first() > MAX_CACHE_AGE_MILLIS) { CACHE.remove(street+number); }
			else return cached.second();
		}
	
		final NextDisposalRuns result = new NextDisposalRuns(String.format("%s %s", street, number));
		final Map<String, Pair<String, String>> rawResult = getRawDisposalRunsFromStadtreinigung(street, number);
		
		rawResult.forEach((type, data) -> {
			try {
				result.getNextRunPerDisposalType().add(DisposalRunAdapter.build(type, data.first(), data.second()));
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		});
		
		CACHE.put(street+number, new Pair<Long, NextDisposalRuns>(System.currentTimeMillis(), result));

		return result;
	}
	
	private Map<String, Pair<String, String>> getRawDisposalRunsFromStadtreinigung(String street, String number) throws IOException {
		final Map<String, Pair<String, String>> result = new HashMap<>(8, 0.75f);
		final String targetURI = String.format(SR_API_HEAD_URI_WITH_PARAMS, URLEncoder.encode(street, "UTF-8"), URLEncoder.encode(number, "UTF-8"));
		
		Document doc = Jsoup.connect(targetURI).get();
		Elements detailsTable = doc.select("td#ux_standort_details tr.fett, td#ux_standort_details tr.fett + tr, td#ux_standort_details tr.fett + tr + tr");

//		System.out.println(detailsTable.text());
		
		if (detailsTable.size() % 3 != 0) throw new IllegalStateException("Size of select from SR API is not a multiple of 3! This is suspicious - check your Selector!");

		int step = 0;
		String wasteType = null, interval = null, nextRun = null;
		for (Element e: detailsTable) {
			switch (step) {
			case 0:
				//clean up (new round!)
				wasteType = interval = nextRun = null;
				//assume waste type 
				wasteType = e.child(0).text();
				break;
			case 1:
				//assume interval
				if (!"Ihr Abfuhrtag:".equals(e.child(0).text())) throw new IllegalStateException("Unexpected element in SR API response: "+e.outerHtml());
				interval = e.child(1).text();
				break;
			case 2:
				//assume next run
				if (!"nächste Abfuhr:".equals(e.child(0).text())) throw new IllegalStateException("Unexpected element in SR API response: "+e.outerHtml());
				nextRun = e.child(1).text();
				break;
			default: throw new IllegalStateException("Should've wrapped around the state machine Oo");
			
			}
			step = ++step % 3;
			if (0 == step) {
				if (result.containsKey(wasteType)) throw new IllegalStateException("Duplicate entries in SR API response for waste type "+wasteType);
				result.put(wasteType, new Pair<>(nextRun, interval));
			}
		}
		
		return result;
	}
	
	
}
