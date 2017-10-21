package info.hiergiltdiestfu.waste.extractor;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.QueryParam;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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

	
	
	private static final String SR_API_HEAD_URI_WITH_PARAMS = "http://stadtplan.dresden.de/project/cardo3Apps/IDU_DDStadtplan/abfall/detailpage.aspx?POS-ADR=%s|%s";
	@ResponseBody
	@RequestMapping(path="/health-description")
	public Health health() {
		return new Health(100, "Looking good");
		//TODO test availability of SR API and report on that
	}
	
	@ResponseBody
	@RequestMapping(path="/next-disposal")
	public NextDisposalRuns nextDisposal(@QueryParam("street") String street, @QueryParam("number") String number) throws Throwable {
		final NextDisposalRuns result = new NextDisposalRuns(String.format("%s %s", street, number));
		final Map<String, Pair<String, String>> rawResult = getRawDisposalRunsFromStadtreinigung(street, number);
		
		rawResult.forEach((type, data) -> {
			try {
				result.getNextRunPerDisposalType().add(new DisposalRun(type, data.first(), data.second()));
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		});
		
		{ 
			DisposalRun run = new DisposalRun();
			
			run.setRunTime(new Date());
			run.setType(WasteType.TEST);
			
			result.getNextRunPerDisposalType().add(run);
		}
		
		return result;
	}
	
	final HttpClientBuilder ClientBuilder = HttpClientBuilder.create();
	final HttpClient client = ClientBuilder.build();
	
	private Map<String, Pair<String, String>> getRawDisposalRunsFromStadtreinigung(String street, String number) throws ClientProtocolException, IOException {
		final Map<String, Pair<String, String>> result = new HashMap<>(8, 0.75f);
		final String targetURI = String.format(SR_API_HEAD_URI_WITH_PARAMS, URLEncoder.encode(street, "UTF-8"), URLEncoder.encode(number, "UTF-8"));
		
		Document doc = Jsoup.connect(targetURI).get();
		Elements detailsTable = doc.select("td#ux_standort_details tr.fett, td#ux_standort_details tr.fett + tr, td#ux_standort_details tr.fett + tr + tr");

//		System.out.println(detailsTable.text());
		
		if (detailsTable.size() % 3 != 0) throw new IllegalStateException("Size of select from SR API is not a multiple of 3! This is supicious - check your Selector!");

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
