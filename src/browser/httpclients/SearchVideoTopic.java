package browser.httpclients;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.teamdev.jexplorer.Browser;

import browser.BrowserUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SearchVideoTopic {
	private BasicCookieStore bcs = new BasicCookieStore();
	private Browser origin;
	
	private Logger a = Logger.getLogger(SearchVideoTopic.class);

	
	public SearchVideoTopic(Browser origin) {
		this.origin = origin;
	}

	public Object[][] findQuestion(String mid) {
		try {
			BrowserUtils.convertCookieToHttpClient(origin, bcs, origin.getLocationURL());
			CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(bcs).build();
			HttpGet hg = new HttpGet("https://mooc1-2.chaoxing.com/richvideo/initdatawithviewer?&start=undefined&mid="+mid);
			hg.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
			hg.setHeader("Origin","mooc1-2.chaoxing.com");
			HttpResponse hr = httpClient.execute(hg);
			String entity = null;
			if(hr.getStatusLine().getStatusCode()==200) {
				entity = EntityUtils.toString(hr.getEntity());
				a.info(entity);
			}
			return parseEntity(entity);
		} catch (ParseException | IOException e) {
			a.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private Object[][] parseEntity(String entity) {
		JSONArray json = JSONArray.fromObject(entity);
		if(json.size()==0)
			return null;
		JSONObject data = JSONObject.fromObject(json.get(0));
		JSONArray datas = data.getJSONArray("datas");
		Object[][] topics = new Object[datas.size()][];
		for (int i = 0; i < datas.size(); i++) {
			JSONObject descriptions = JSONObject.fromObject(datas.get(i));
			String topic = descriptions.getString("description");
			JSONArray options = descriptions.getJSONArray("options");
			ArrayList<String> arr = new ArrayList<String>();
			a.debug("topic"+topic);
			for (int j = 0; j < options.size(); j++) {
				JSONObject option = JSONObject.fromObject(options.get(j));
				if(option.getBoolean("isRight")) {
					arr.add(option.getString("name"));	
					a.debug(option.getString("name"));
				}
			}
			topics[i] = new Object[] {topic,arr.toArray(new String[] {})};
		}
		return topics;
	}
}
