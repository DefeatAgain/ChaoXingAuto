package browser.httpclients;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamdev.jexplorer.Browser;
import com.teamdev.jexplorer.dom.DOMDocument;
import com.teamdev.jexplorer.dom.DOMNodeList;
import com.teamdev.jexplorer.internal.dom.ElementImpl;
import com.teamdev.jexplorer.internal.dom.IFrameImpl;

import browser.BrowserUtils;
import exception.VideoDownloadException;
import net.sf.json.JSONObject;
import threadpool.ThreadPool;
import window.dialog.ErrorSolveDialog;

public class VideoDownload{

	private BasicCookieStore bs = new BasicCookieStore();
	private Browser origin;
	private Object[] para;
	private int fid;
	private ProgressBar bar;
	private Label lblErrors;
	private int index = 0;
	private Shell shell;
	
	private Logger a = Logger.getLogger(VideoDownload.class);

	public VideoDownload(Browser origin, int fid, ProgressBar bar, Label lblErrors,
			Shell shell) {
		this.origin = origin;
		this.fid = fid;
		this.bar = bar;
		this.lblErrors = lblErrors;
	}

	public void download(String detail, String path) {
		if (detail.equals("sd"))
			detail = "http";
		else if (detail.equals("current"))
			detail = "current";
		else
			detail = "http" + detail;

		BrowserUtils.convertCookieToHttpClient(origin, bs, origin.getLocationURL());
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(bs).build();
		Object[] para = getPara(origin);
		this.para = para;
		String[] iframe_srcs = (String[]) para[0];
		String[] video_srcs = (String[]) para[2];
		try {
			JSONObject[] srcs = check(httpClient);
			HttpGet hg = new HttpGet();
			hg.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
			hg.setHeader("Host", "s1.ananas.chaoxing.com");
			hg.setHeader("Range", "bytes=0-");
			hg.setHeader("Accept-Encoding", "identity;q=1, *;q=0");
			for (int i = 0; i < srcs.length; i++) {
				hg.setHeader("Referer", iframe_srcs[i]);
				if (detail.equals("current"))
					hg.setURI(new URI(video_srcs[i]));
				else
					hg.setURI(new URI(srcs[i].getString(detail)));
				HttpResponse hr = httpClient.execute(hg);
				int statuscode = hr.getStatusLine().getStatusCode();
				a.debug(hg.getURI().toString()+":"+statuscode);
				switch (statuscode) {
				case 206: {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							bar.setVisible(true);
							bar.setSelection(0);
						}
					});
					File dir = new File(path);
					if (!dir.exists())
						dir.mkdirs();
					File file = new File(path, detail + srcs[i].getString("filename"));
					InputStream in = hr.getEntity().getContent();
					BufferedOutputStream bos =new BufferedOutputStream(new FileOutputStream(file));
					BufferedInputStream bis = new BufferedInputStream(in);
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								bar.setMaximum(bis.available());
							} catch (IOException e) {
								a.error(e.getMessage(),e);
							}
							bar.setState(SWT.NORMAL);
							bar.setMinimum(0);
							lblErrors.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
							lblErrors.setText("正在下载");
							lblErrors.setVisible(true);
						}
					});
					index = 0;
					byte[] bytes = new byte[8192];
					while ((index = bis.read(bytes)) != -1) {
						bos.write(bytes,0,index);
						bos.flush();
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								bar.setSelection(bar.getSelection() + index);
								try {
									bar.setMaximum(bis.available());
								} catch (IOException e) {
									a.error(e.getMessage(),e);
								}
							}
						});
					}
					bos.close();
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							bar.setSelection(bar.getMaximum());
							lblErrors.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
							lblErrors.setText("下载完成");
							lblErrors.setVisible(true);
						}
					});
					break;
				}
				case 404: {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {

							lblErrors.setText("未发现该文件");
							lblErrors.setVisible(true);
						}
					});
					break;
				}
				case 403: {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							lblErrors.setText("未发现该文件");
							lblErrors.setVisible(true);
						}
					});
					break;
				}
				default: {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							lblErrors.setText("未知错误");
							lblErrors.setVisible(true);
						}
					});
					break;
				}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if(e instanceof net.sf.json.JSONException) {
						lblErrors.setText("未发现文件");
						lblErrors.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
						lblErrors.setVisible(true);
						bar.setState(SWT.ERROR);
					}else {
					lblErrors.setText(e.getMessage());
					lblErrors.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
					lblErrors.setVisible(true);
					bar.setState(SWT.ERROR);
					}
				}
			});
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				a.error(e.getMessage(),e);
				throw new RuntimeException(e.getMessage());
			}
		}

	}

	// 获取视频地址
	private JSONObject[] check(CloseableHttpClient httpClient) throws Exception {
		String[] iframe_srcs = (String[]) para[0];
		String[] iframe_objects = (String[]) para[1];
		JSONObject[] first_rs = new JSONObject[iframe_objects.length];
		try {
			HttpGet hg = new HttpGet();
			hg.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
			hg.setHeader("Host", "mooc1-2.chaoxing.com");
			hg.setHeader("Accept-Encoding", "gzip, deflate, br");
			for (int i = 0; i < iframe_objects.length; i++) {
				hg.setHeader("Referer", iframe_srcs[i]);
				JSONObject json = JSONObject.fromObject(iframe_objects[i]);
				String objectid = json.getString("objectid");
				hg.setURI(new URI("https://mooc1-2.chaoxing.com/ananas/status/" + objectid + "?k=" + fid
						+ "&flag=normal&_dc=" + System.currentTimeMillis()));
				a.debug("https://mooc1-2.chaoxing.com/ananas/status/" + objectid + "?k=" + fid
						+ "&flag=normal&_dc=" + System.currentTimeMillis());
				HttpResponse hr = httpClient.execute(hg);
				
				JSONObject res = JSONObject.fromObject(EntityUtils.toString(hr.getEntity()));
				if (!res.containsValue("success"))
					throw new VideoDownloadException("请求失败");
				first_rs[i] = res;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof VideoDownloadException) {
				throw e;
			} else
				throw new RuntimeException(e.getMessage());
		}
		return first_rs;
	}
	
	private Object[] getPara(Browser b) {
		Future<Object[]> res = ThreadPool.EXECUTOR_SERVICE.submit(new Callable<Object[]>() {
			@Override
			public Object[] call() throws Exception {
				DOMDocument f1 = ((IFrameImpl) b.getDocument().getElementsByTagName("iframe").item(0))
						.getContentDocument();
				DOMNodeList dl = f1.getElementsByTagName("iframe");
				ArrayList<IFrameImpl> temp = new ArrayList<IFrameImpl>(2);
				for (int i = 0; i < dl.getLength(); i++) {
					IFrameImpl ifi = (IFrameImpl) dl.item(i);
					if (ifi.getAttribute("src").contains("video"))
						temp.add(ifi);
				}
				IFrameImpl[] ill = temp.toArray(new IFrameImpl[] {});
				String[] iframe_src = new String[ill.length];
				String[] iframe_object = new String[ill.length];
				String[] video_src = new String[ill.length];
				for (int i = 0; i < ill.length; i++) {
					IFrameImpl ifi = (IFrameImpl) ill[i];
					iframe_src[i] = "https://mooc1-2.chaoxing.com" + ifi.getAttribute("src");// src中没有主机
					iframe_object[i] = ifi.getAttribute("data");
					ElementImpl video = (ElementImpl) ifi.getContentDocument().getElementById("video_html5_api");
					if (video != null)
						video_src[i] = video.getAttribute("src");
					else
						throw new VideoDownloadException("未找到视频源");
				}
				a.debug("iframe_src"+iframe_src+"iframe_object"+iframe_object+"video_src"+video_src);
				return new Object[] { iframe_src, iframe_object, video_src };
			}
		});
		Object[] result = null;
		try {
			result = res.get();
		} catch (Exception e) {
			a.error(e.getMessage(), e);
			ErrorSolveDialog.open(shell, "Exception", e.getMessage(), false, null);
		}
		return result;

	}

}
