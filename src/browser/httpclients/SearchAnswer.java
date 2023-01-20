package browser.httpclients;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import browser.BrowserUtils;
import exception.NoMoreAnswer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SearchAnswer {
	private static Logger a = Logger.getLogger(SearchAnswer.class);

	public Object[][] getAnswer(String[] body) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < body.length; i++) {
			String[] str = body[i].split("~");
			sb.append("topic[");
			sb.append(i);
			sb.append("]=");
			sb.append(str[1] + "&");
			sb.append("type[");
			sb.append(i);
			sb.append("]=");
			sb.append( str[0] +  "&");
		}
		a.info(sb.toString());

		String entity = null;
		try {
			SSLContext sslcontext = createIgnoreVerifySSL();
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.INSTANCE)
					.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
			PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
					socketFactoryRegistry);
			HttpClients.custom().setConnectionManager(connManager);

			CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connManager).build();
			HttpPost hp = new HttpPost("https://cx.icodef.com/v2/answer");
			hp.setHeader("Content-Type", "application/x-www-form-urlencoded");
			hp.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
			hp.setHeader("Origin", "https://mooc1-2.chaoxing.com");
			hp.setEntity(new StringEntity(sb.toString(), "UTF-8"));
			HttpResponse hr = httpClient.execute(hp);
			entity = null;
			if (hr.getStatusLine().getStatusCode() == 200)
				entity = EntityUtils.toString(hr.getEntity());
			a.info(entity);
		} catch (Exception e) {
			a.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return anaylseRes(/* type,topic, */ entity);
	}

	private Object[][] anaylseRes(/* String[] type,String[] topic, */String json) {
		JSONArray jsarr = JSONArray.fromObject(json);
		Object[][] res = new Object[jsarr.size()][];
		for (int i = 0; i < jsarr.size(); i++) {
			JSONObject temp = JSONObject.fromObject(jsarr.get(i));
			String topic = temp.getString("topic");
			int index = temp.getInt("index");
			JSONArray result = temp.getJSONArray("result");
			if (result.size() == 0) {
				res[index] = new Object[] { index, new NoMoreAnswer(), topic };
				continue;
			}
			JSONObject correct = JSONObject.fromObject(result.get(0));
			JSONArray answer = correct.getJSONArray("correct");
			String[] content = new String[answer.size()];
			for (int j = 0; j < answer.size(); j++) {
				JSONObject option_content = JSONObject.fromObject(answer.get(j));
				content[j] = option_content.getString("content");
//				System.out.println( option_content.getString("content"));
			}
			a.debug("index" + index + "topic" + topic + "content" + content);
			res[index] = new Object[] { index, content, topic };
		}
		return res;

	}

	private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("SSLv3");

		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

	public Object getAnswer2(String body) {
		/*
		 * String[] str = body.split("~"); while(body.indexOf(" ")!=-1)
		 */
		body = body.replaceAll(" ", "%20");
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost hp = new HttpPost("http://mooc.forestpolice.org/cx/0/" + body);
			hp.setHeader("Content-Type", "application/x-www-form-urlencoded");
			hp.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
			hp.setHeader("Origin", "https://mooc1-2.chaoxing.com");
			HttpResponse hr = httpClient.execute(hp);
			String entity = null;
			if (hr.getStatusLine().getStatusCode() == 200)
				entity = EntityUtils.toString(hr.getEntity());
			a.info(entity);
			return anaylseRes2(entity);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Object anaylseRes2(String anwsers) {
		Object res = null;
		JSONObject json = JSONObject.fromObject(anwsers);
		String data = json.getString("data");
		if (json.getInt("code") == 1)
			res = BrowserUtils.unicode2String(data);
		else
			res = new NoMoreAnswer();
		a.debug(res);
		return res;
	}

	public Object getAnswer3(String body) {
		return new NoMoreAnswer();
		/*
		 * String topic = body.substring(body.indexOf("】")+1).replace(" ", ""); try {
		 * CloseableHttpClient httpClient = HttpClients.createDefault(); HttpPost hp =
		 * new HttpPost("http://api.fm210.cn/wangke/cx.php?w="+topic);
		 * hp.setHeader("User-Agent",
		 * "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36"
		 * ); HttpResponse hr = httpClient.execute(hp); String entity = null; if
		 * (hr.getStatusLine().getStatusCode() == 200) entity =
		 * EntityUtils.toString(hr.getEntity()); a.info(entity); return
		 * anaylseRes3(entity); } catch (Exception e) { throw new RuntimeException(e); }
		 */
	}

	private Object anaylseRes3(String entity) {
		Document dom = Jsoup.parse(entity);
		Elements bodys = dom.getElementsByTag("body");
		Element body = bodys.get(0);
		String context = body.text();
		String answer = context.substring(context.indexOf("答案:") + 3).trim().replace(" ", "");
		if (answer.equals(""))
			return new NoMoreAnswer();
		if (answer.equals("正确") || answer.equals("√"))
			return "true";
		if (answer.equals("错误") || answer.equals("×"))
			return "false";
		return answer;
	}
}
