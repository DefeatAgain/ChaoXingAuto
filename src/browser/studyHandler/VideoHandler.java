package browser.studyHandler;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.teamdev.jexplorer.dom.DOMDocument;
import com.teamdev.jexplorer.dom.DOMElement;
import com.teamdev.jexplorer.dom.DOMNodeList;
import com.teamdev.jexplorer.internal.dom.ElementImpl;
import com.teamdev.jexplorer.internal.dom.IFrameImpl;

import browser.httpclients.SearchVideoTopic;
import exception.VideoCanNotPlayException;
import exception.VideoNotFoundException;
import exception.VideoTopicException;
import net.sf.json.JSONObject;
import threadpool.ThreadPool;

public class VideoHandler {
	private VideoWebListener vw;
	boolean ended;
	Timer video_timer;
	static boolean video_start = false;
	public static boolean  video_muted =false;
	public static boolean  video_topic =false;
	
	private Logger a = Logger.getLogger(VideoHandler.class);
	public VideoHandler(VideoWebListener videoWebListener) {
		this.vw = videoWebListener;
	}

	@Deprecated
	boolean isTask(DOMDocument dom) {
		return true;
		/*
		 * DOMElement aim = dom.getElementById("ext-gen1039"); String class_1 =
		 * aim.get(0).getAttribute("class"); String class_2 = null; if (aim.size() > 1)
		 * { class_2 = aim.get(1).getAttribute("class"); } System.out.println(class_1 +
		 * "="); if (class_1.contains("finished") && class_2 != null &&
		 * class_2.contains("finished")) return false; return true;
		 */
	}



	void findVideo(DOMDocument dom) {
		ElementImpl dct1 = (ElementImpl) dom.getElementById("dct1");
		if (dct1.getTitle().equals("视频")) {
			dct1.click();
			return;
		}
		ElementImpl dct2 = (ElementImpl) dom.getElementById("dct2");
		if (dct2.getTitle().equals("视频")) {
			dct2.click();
			return;
		}
		ElementImpl dct3 = (ElementImpl) dom.getElementById("dct3");
		if (dct3.getTitle().equals("视频")) {
			dct3.click();
			return;
		}
		throw new VideoNotFoundException("未发现视频标签");
	}

	void play(IFrameImpl[] videos_iframe) {
		if (video_start)
			return;
		video_start = true;
		// 所有视频iframe
		final int video_length = videos_iframe.length;
		// 视频所在dom
		DOMDocument[] videos_dom = new DOMDocument[video_length];// 每个视频的iframe
		for (int i = 0; i < video_length; i++)
			videos_dom[i] = videos_iframe[i].getContentDocument();
		// 所有视频的按钮的列表
		DOMNodeList[] videoButtonLists = new DOMNodeList[video_length];
		for (int i = 0; i < videos_dom.length; i++)
			videoButtonLists[i] = getVideoBunttons(videos_dom[i]);
		// 大按钮
		ElementImpl[] big_btns = new ElementImpl[video_length];
		for (int i = 0; i < big_btns.length; i++)
			big_btns[i] = (ElementImpl) videoButtonLists[i].item(0);// button一共有两个
		// 视频的播放按钮
		ElementImpl[] video_play_btns = new ElementImpl[video_length];
		for (int i = 0; i < video_play_btns.length; i++)
			video_play_btns[i] = (ElementImpl) videoButtonLists[i].item(1);

		// 监视
		Timer video_timer = new Timer();
		this.video_timer = video_timer;
		TimerTask task1 = new TimerTask() {
			Object[][] objs = null;
			int i = 0;
			String currentChapter= vw.current_chapter.getId();
			{
				ended = false;
				if (big_btns[i] != null) {
					if(video_muted)
					vw.brwoser.executeScript(
							"document.querySelector('iframe').contentDocument.querySelectorAll('iframe')["+i+"].contentDocument.querySelector('video').muted=true;");
					big_btns[i].click();
					objs = getVideoTopic(videos_iframe[i]);
				} else {
					this.cancel();
					video_start = false;
				}
			}
			@Override
			public void run() {
				try {
					doVideo(videos_iframe, video_length, big_btns, video_play_btns);
				} catch (Exception e) {
					a.error(e.getMessage(), e);
					this.cancel();
					video_start = false;
					throw new VideoCanNotPlayException(e);
				}
			}
			private void doVideo(IFrameImpl[] videos_iframe, final int video_length, ElementImpl[] big_btns,
					ElementImpl[] video_play_btns) {
				if (vw.brwoser.isDisposed()) {
					this.cancel();
					video_start = false;
					return;
				}
				ElementImpl video_play_btn = video_play_btns[i];
				if (video_play_btn == null) {
					this.cancel();
					video_start = false;
					return;// 如果找不到按钮直接结束
				}
				String classs = video_play_btn.getClassName();
				a.debug(classs);
				if (classs.contains("paused") && !classs.contains("ended") && !findVideoTopic(videos_iframe[i]) && this.currentChapter.equals(vw.current_chapter.getId())) {
					video_play_btn.click();
				} else if(findVideoTopic(videos_iframe[i])){
					try {fillVideoTopic(objs,videos_iframe[i]);} catch (Exception e) {
						chooseVideoTagForError();
					}
				}else if (!this.currentChapter.equals(vw.current_chapter.getId())) {
					this.cancel();
					video_start = false;
					return;
				}else if (classs.contains("ended")) {
					i++;// 开始检测下一个视频
					if (i == video_length) {
						if(ManualStudy.manual) {
							video_start = false;
							this.cancel();
							return;
						}
						ThreadPool.sleep(3000);
						chooseWorkTag();
						video_start = false;
						this.cancel();
						return;
					} else {
						if(video_muted)
							vw.brwoser.executeScript(
								"document.querySelector('iframe').contentDocument.querySelectorAll('iframe')["+i+"].contentDocument.querySelector('video').muted=true;");
						big_btns[i].click();// 第一次点大按钮
						objs = getVideoTopic(videos_iframe[i]);
					}

				}
			}
		};
		a.info("video is started...");
		video_timer.schedule(task1, 500, 1000);
	}


	void chooseVideoTagForError() {
		DOMDocument dom = vw.brwoser.getDocument();
		ElementImpl now = vw.getCurrentTitle(dom);
		if(now!=null) 
			now.click();	
		else
			throw new VideoCanNotPlayException("未知错误"); 
	}
	
	
	private void chooseWorkTag() {
		DOMDocument dom = vw.brwoser.getDocument();
		ElementImpl now = vw.getCurrentTitle(dom);
		String id = now.getId();
		a.debug(id);
		id = id.substring(3);
//		a.info(id);
		int Id = Integer.parseInt(id)+1;
		ElementImpl next = (ElementImpl) dom.getElementById("dct"+Id);
		if(next!=null) 
			next.click();	
//			a.info("next has been click!!");
		else
			vw.changeChapter();
	}

	private void fillVideoTopic(Object[][] objs,IFrameImpl ifi) {
		DOMDocument dom = ifi.getContentDocument();
		ElementImpl sub = (ElementImpl) dom.getElementById("ext-gen1045");
		if(!sub.getClassName().contains("submit"))
			throw new VideoCanNotPlayException("未发现提交按钮");
		DOMNodeList inputs = dom.getElementsByName("ans-videoquiz-opt");
		if(inputs.getLength()==0)
			throw new VideoCanNotPlayException("未发现按钮");
		DOMNodeList divs = dom.getElementsByTagName("div");
		String topic = null;
		for (int i = 0; i < divs.getLength(); i++) {
			ElementImpl title = (ElementImpl) divs.item(i);
			if(title.getClassName().contains("ans-videoquiz-title"))
				topic=title.getTextContent();
		}
		if(topic==null)
			throw new VideoCanNotPlayException("未发现题目");
		for (int i = 0; i < objs.length; i++) {
			Object[] obj = objs[i];
			String _topic = (String) obj[0]; 
			String[] answers = (String[]) obj[1];
			String ans = "ABCDEFG";
			if(topic.contains(_topic)) {
				for (int j = 0; j < answers.length; j++) {
					String answer = answers[j].trim();
					a.info(answer);
					int index = ans.indexOf(answer);
					if(index!=-1&&index<inputs.getLength()) {
						ElementImpl input = (ElementImpl) inputs.item(index);
						input.click();
						ThreadPool.sleep(500);
					}else
						throw new VideoCanNotPlayException("填充题目失败");
				}
				ThreadPool.sleep(1000);
				a.info("submin....");
				sub.click();
				break;
			}			
		}
	}
	
	private boolean findVideoTopic(IFrameImpl ifi) {
		DOMDocument dom = ifi.getContentDocument();
		if(dom==null)
			return false;
		DOMElement sub = dom.getElementById("ext-gen1045");
		a.info("发现视频开始处理。。。");
		if(sub==null)
			return false;
		else
			return true;
	}

	private Object[][] getVideoTopic(IFrameImpl ifi) {
		String mid = getMid(ifi);
		Future<Object[][]> res = ThreadPool.EXECUTOR_SERVICE.submit(new Callable<Object[][]>() {
			@Override
			public Object[][] call() throws Exception {
				return new SearchVideoTopic(vw.brwoser).findQuestion(mid);
			}
		});
		Object[][] obj = null;
		try {
			obj = res.get();
		} catch (Exception e) {
			a.error(e.getMessage(),e);
			throw new VideoTopicException("处理视频题目失败",e);
		}
		/*if (obj!=null) {
			for (int i = 0; i < obj.length; i++) {
				Object[] obj1 = obj[i];
				String topic = (String) obj1[0];
				String[] option = (String[]) obj1[1];
				System.out.println(topic);
				for (int j = 0; j < option.length; j++) {
					System.out.println(option[j]);
				}
			}
		}*/
		return obj;
	}

	private String getMid(IFrameImpl ifi) {
		String data = ifi.getAttribute("data");
		a.debug(data);
		JSONObject json = JSONObject.fromObject(data);
		String mid = json.getString("mid");
		return mid;
	}

	private DOMNodeList getVideoBunttons(DOMDocument dom) {
		DOMNodeList videoBunttons = dom.getElementsByTagName("button");
		return videoBunttons;
	}

}
