package browser.studyHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.teamdev.jexplorer.Browser;
import com.teamdev.jexplorer.dom.DOMDocument;
import com.teamdev.jexplorer.dom.DOMElement;
import com.teamdev.jexplorer.dom.DOMNode;
import com.teamdev.jexplorer.dom.DOMNodeList;
import com.teamdev.jexplorer.dom.DOMNodeType;
import com.teamdev.jexplorer.event.DisposeListener;
import com.teamdev.jexplorer.internal.dom.ElementImpl;
import com.teamdev.jexplorer.internal.dom.IFrameImpl;

import browser.httpclients.SearchAnswer;
import browser.httpclients.SearchVideoTopic;
import exception.NoMoreAnswer;
import exception.TagCanNotFoundException;
import exception.VideoCanNotPlayException;
import exception.VideoTopicException;
import net.sf.json.JSONObject;
import threadpool.ThreadPool;
import window.dialog.ErrorSolveDialog;
import window.dialog.WarningSolveDialog;

public class ManualStudy {
	private Shell parent_shell;
	public Shell dialog_shell;

	public static boolean manual = true;
	private Timer video_timer;
	private Browser browser;
	private String current_chapter_id;
	private String current_title_id;

	private String ext_id = "";
	private Logger a = Logger.getLogger(ManualStudy.class);

	public ManualStudy(Browser b, Dialog dialog) {
		this.parent_shell = dialog.getParent();
		this.browser = b;
		b.addDisposeListener(new DisposeListener() {

			@Override
			public void onDispose(Browser browser) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						dialog_shell.dispose();
					}
				});
			}
		});
	}

	public void doVideo() {
		try {
			DOMDocument dom = browser.getDocument();
			Map<IFrameImpl, ElementImpl[]> videoMap = new LinkedHashMap<>();
			checkChapter();
			checkTitle();
			DOMNodeList dl0 = dom.getElementsByTagName("iframe");
			for (int i = 0; i < dl0.getLength(); i++) {
				findVideo((IFrameImpl) dl0.item(i), videoMap);
			}
			play(videoMap);
		} catch (Exception e) {
			a.error(e.getMessage(), e);
			ErrorSolveDialog.open(parent_shell, "ERROR", e.getMessage(), false, null);
		}
	}

	private void play(Map<IFrameImpl, ElementImpl[]> videoMap) {
		if (videoMap.size() == 0)
			throw new VideoCanNotPlayException("未发现视频!");
		IFrameImpl[] doms = new IFrameImpl[videoMap.size()];
		int index = 0;
		for (IFrameImpl d : videoMap.keySet()) {
			doms[index++] = d;
		}
		this.video_timer = new Timer();
		video_timer.schedule(new TimerTask() {
			int index = 0;
			ElementImpl[] btns = videoMap.get(doms[index]);
			String cur_id_chapter = current_chapter_id;
			String cur_id_title = current_title_id;
			Object[][] objs;
			DOMDocument dom = doms[index].getContentDocument();
			{
				if (btns[0] == null)
					throw new VideoCanNotPlayException("按钮没找到");
				btns[0].click();
				objs = getVideoTopic(doms[index]);
			}

			@Override
			public void run() {
				if (browser.isDisposed()) {
					this.cancel();
					return;
				}
				checkChapter();
				checkTitle();
				if (!cur_id_chapter.equals(current_chapter_id) || !cur_id_title.equals(current_title_id)) {
					this.cancel();
					return;
				}
				String className = btns[1].getClassName();
				if (className.contains("paused") && !className.contains("ended")) {
//					removeVideoTopic(doms[index]);
					if (btns[1] == null)
						throw new VideoCanNotPlayException("按钮没找到");
					if (findVideoTopic(dom))
						try {
							fillVideoTopic(objs, dom);
						} catch (Exception e) {
							a.error(e.getMessage(), e);
						}
					btns[1].click();
				} else if (className.contains("ended")) {
					index++;
					if (index == doms.length) {
						this.cancel();
						return;
					}
					btns = videoMap.get(doms[index]);
					ThreadPool.sleep(2000);
					if (btns[0] == null)
						throw new VideoCanNotPlayException("按钮没找到");
					btns[0].click();
//					removeVideoTopic(doms[index]);
				}
			}

			private void removeVideoTopic(DOMDocument domDocument) {
				DOMElement ext1 = domDocument.getElementById("ext-comp-1035");
				DOMElement ext2 = domDocument.getElementById("ext-comp-1036");
				DOMNode parent = null;
				if (ext1 != null) {
					parent = ext1.getParentNode();
					while (parent.getNodeType().equals(DOMNodeType.TextNode))
						parent = ext1.getPreviousSibling();
					parent.removeChild(ext1);
					a.debug(parent.getNodeName() + "...." + ext1.getClassName());
				}
				if (ext2 != null) {
					parent.removeChild(ext2);
					a.debug(parent.getNodeName() + "...." + ext2.getClassName());
				}
			}
		}, 500, 1000);
	}

	private void fillVideoTopic(Object[][] objs, DOMDocument dom) {
//		DOMDocument dom = ifi.getContentDocument();
		ElementImpl sub = (ElementImpl) dom.getElementById(ext_id);
		if (!sub.getClassName().contains("submit"))
			throw new VideoCanNotPlayException("未发现提交按钮");
		DOMNodeList inputs = dom.getElementsByName("ans-videoquiz-opt");
		if (inputs.getLength() == 0)
			throw new VideoCanNotPlayException("未发现按钮");
		DOMNodeList divs = dom.getElementsByTagName("div");
		String topic = null;
		for (int i = 0; i < divs.getLength(); i++) {
			ElementImpl title = (ElementImpl) divs.item(i);
			if (title.getClassName().contains("ans-videoquiz-title"))
				topic = title.getTextContent();
		}
		if (topic == null)
			throw new VideoCanNotPlayException("未发现题目");
		for (int i = 0; i < objs.length; i++) {
			Object[] obj = objs[i];
			String _topic = (String) obj[0];
			String[] answers = (String[]) obj[1];
			String ans = "ABCDEFG";
			if (topic.contains(_topic)) {
				for (int j = 0; j < answers.length; j++) {
					String answer = answers[j].trim();
					a.info(answer);
					int index = ans.indexOf(answer);
					if (index != -1 && index < inputs.getLength()) {
						ElementImpl input = (ElementImpl) inputs.item(index);
						input.click();
						ThreadPool.sleep(500);
					} else
						throw new VideoCanNotPlayException("填充题目失败");
				}
				ThreadPool.sleep(1000);
				a.info("submit....");
				sub.click();
				break;
			}
		}
	}

	private boolean findVideoTopic(DOMDocument dom) {
//		DOMDocument dom = ifi.getContentDocument();
		if (dom == null)
			return false;
		String str1 = "ext-gen10";

		for (int i = 40; i < 99; i++) {
			DOMElement sub = dom.getElementById("ext-gen10" + i);
			if (sub != null) {
				ext_id = "ext-gen10" + i;
				return true;
			}
		}
		return false;
	}

	private Object[][] getVideoTopic(IFrameImpl ifi) {
		String mid = getMid(ifi);
		Future<Object[][]> res = ThreadPool.EXECUTOR_SERVICE.submit(new Callable<Object[][]>() {
			@Override
			public Object[][] call() throws Exception {
				return new SearchVideoTopic(browser).findQuestion(mid);
			}
		});
		Object[][] obj = null;
		try {
			obj = res.get();
		} catch (Exception e) {
			a.error(e.getMessage(), e);
			throw new VideoTopicException("处理视频题目失败", e);
		}
		/*
		 * if (obj!=null) { for (int i = 0; i < obj.length; i++) { Object[] obj1 =
		 * obj[i]; String topic = (String) obj1[0]; String[] option = (String[])
		 * obj1[1]; System.out.println(topic); for (int j = 0; j < option.length; j++) {
		 * System.out.println(option[j]); } } }
		 */
		return obj;
	}

	private String getMid(IFrameImpl ifi) {
		String data = ifi.getAttribute("data");
		a.debug(data);
		JSONObject json = JSONObject.fromObject(data);
		String mid = json.getString("mid");
		return mid;
	}

	protected void checkTitle() {
		DOMDocument dom = browser.getDocument();
		ElementImpl dct1 = (ElementImpl) dom.getElementById("dct1");
		if (dct1 != null && dct1.getClassName().contains("currents")) {
			current_title_id = "dct1";
			return;
		}
		ElementImpl dct2 = (ElementImpl) dom.getElementById("dct2");
		if (dct2 != null && dct2.getClassName().contains("currents")) {
			current_title_id = "dct2";
			return;
		}
		ElementImpl dct3 = (ElementImpl) dom.getElementById("dct3");
		if (dct3 != null && dct3.getClassName().contains("currents")) {
			current_title_id = "dct3";
			return;
		}
		throw new TagCanNotFoundException("无法找到目前的选项卡");
	}

	protected void checkChapter() {
		DOMDocument dom = browser.getDocument();
		DOMNodeList hdl = dom.getElementsByTagName("h4");
		if (hdl != null && hdl.getLength() != 0)
			for (int i = 0; i < hdl.getLength(); i++) {
				ElementImpl temp = (ElementImpl) hdl.item(i);
				if (temp.getClassName().contains("currents")) {
					current_chapter_id = temp.getId();
					break;
				}
			}
	}

	/*
	 * 递归寻找video标签
	 */
	private void findVideo(IFrameImpl ifi, Map<IFrameImpl, ElementImpl[]> map) {
		DOMDocument dom = ifi.getContentDocument();
		DOMNodeList dl0 = dom.getElementsByTagName("video");
		if (dl0 != null && dl0.getLength() != 0) {
			DOMNodeList videoBunttons = dom.getElementsByTagName("button");
			ElementImpl[] Bunttons = new ElementImpl[videoBunttons.getLength()];
			for (int i = 0; i < 2; i++) {
				ElementImpl btn = (ElementImpl) videoBunttons.item(i);
				String Class = btn.getClassName();
				if (Class.contains("vjs-big-play-button") || Class.contains("vjs-play-control"))
					Bunttons[i] = btn;
			}
			map.put(ifi, Bunttons);
		} else {
			DOMNodeList ifdl = dom.getElementsByTagName("iframe");
			if (ifdl != null && ifdl.getLength() != 0)
				for (int i = 0; i < ifdl.getLength(); i++) {
					IFrameImpl ifr = (IFrameImpl) ifdl.item(i);
					findVideo(ifr, map);
				}
		}
	}

	public void cancelVideoTimer() {
		video_timer.cancel();
		browser.refresh();
	}

	public void queryQues() {
		try {
			DOMDocument dom = browser.getDocument();
			Map<String, Map<ElementImpl, String>> quesMap = new LinkedHashMap<>();
			getQuesFrame(dom, quesMap);
			fillAnswers(quesMap);
		} catch (Exception e) {
			a.error(e.getMessage(), e);
			ErrorSolveDialog.open(parent_shell, "ERROR", e.getMessage(), false, null);
		}
	}

	/*
	 * 填充答案
	 */
	private void fillAnswers(Map<String, Map<ElementImpl, String>> quesMap) {
		SearchAnswer sa = new SearchAnswer();
		String[] allQues = new String[quesMap.size()];
//		int index = 0;
//		index = 0;
		for (String topic : quesMap.keySet()) {
			String ques = getTopicType(topic) + "~" + dealStr(topic);
//			allQues[index++] = ques;
			Object[][] answer = sa.getAnswer(new String[]{ques});
			Map<ElementImpl, String> frame = quesMap.get(topic);
			if (answer[0][1] instanceof NoMoreAnswer || answer[0].length == 0)
				continue;
			for (ElementImpl input : frame.keySet()) {
				String context = frame.get(input);
//				a.debug(context);
				String[] content = (String[]) answer[0][1];
				for (int i = 0; i < content.length; i++) {
					a.info(content[i]);
//					a.info(context);
					if (content[i].contains(context) 
							|| context.contains(content[i])) {
						input.click();
						ThreadPool.sleep(1000);
						
					}
				}
			}
		}
	}

	private String dealStr(String str) {
		return str;/*.substring(str.indexOf("】")+1)
				.replaceAll(" ", "")
				.replaceAll("（", ")")
				.replaceAll("）", "(")
				.replaceAll("？", "")
				.replaceAll("。", ".")
				.replaceAll("，", ",")
				.replaceAll("“", "\\\"")
				.replaceAll("”", "\\\"");*/
	}

	private int getTopicType(String topic) {
		int res;
		String type = topic.substring(topic.indexOf("【")+1,topic.indexOf("】"));
		a.info(type);
		switch (type) {
		case "单选题":
			res = 1;
			break;
		case "多选题":
			res = 2;
			break;
		case "判断题":
			res = 3;
			break;
		case "填空题":
			res = 4;
			break;
		default:
			res = -1;
			break;
		}
		return res;
	}

	/*
	 * 搜索全部问题
	 */
	private void getQuesFrame(DOMDocument dom, Map<String, Map<ElementImpl, String>> quesMap) {
		DOMNodeList divs = dom.getElementsByTagName("div");
		if (divs != null) {
			for (int i = 0; i < divs.getLength(); i++) {
				ElementImpl ei = (ElementImpl) divs.item(i);
				String Class = ei.getClassName();
				a.debug(Class);
				if (Class.contains("ZyTop")) {
					String point = ei.getTextContent().trim();
					a.info(point);
					if (point.contains("已完成") || point.contains("成绩")) {
						WarningSolveDialog.open(parent_shell, "已完成", "该作业已完成");
						break;
					}
				}
				if (Class.contains("Zy_TItle")) {
					Map<ElementImpl, String> frame = new LinkedHashMap<>();
					// 获取问题
					DOMNodeList innerDIVs = ei.getChildNodes();
					String question = null;
					for (int j = 0; j < innerDIVs.getLength(); j++) {
						DOMNode node = innerDIVs.item(j);
						String textnode = node.getTextContent();
						if (textnode.contains("】") || textnode.contains("【")) {
							question = textnode;
							a.info(question);
							a.debug(textnode);
						}
					}
					// 获取选项
					DOMNode form = ei.getNextSibling();
					while (form.getNodeType().equals(DOMNodeType.TextNode)) {
						form = form.getNextSibling();
					}
					DOMNodeList uls = ((ElementImpl) form).getElementsByTagName("ul");
					DOMNodeList lis = uls.item(0).getChildNodes();
					for (int j = 0; j < lis.getLength(); j++) {
						DOMNode node = lis.item(j);
						if (node.getNodeName().equalsIgnoreCase("li")) {
							ElementImpl li = (ElementImpl) node;
							DOMNodeList inputs = li.getElementsByTagName("input");
							for (int k = 0; k < inputs.getLength(); k++) {
								ElementImpl input = (ElementImpl) inputs.item(k);
								String value = input.getAttribute("value");
								if (value.equals("true") || value.equals("false")) {
									frame.put(input, value);
								} else {
									DOMNodeList as = li.getElementsByTagName("a");
									String chooser = as.item(k).getTextContent();
									a.debug(value + chooser);
									frame.put(input, value + chooser);
								}
							}
						}
					}
					quesMap.put(question, frame);
				}
			}
		}
		DOMNodeList ifrs = dom.getElementsByTagName("iframe");
		if (ifrs != null && ifrs.getLength() != 0)
			for (int i = 0; i < ifrs.getLength(); i++) {
				IFrameImpl ifi = (IFrameImpl) ifrs.item(i);
				getQuesFrame(ifi.getContentDocument(), quesMap);
			}
	}
}
