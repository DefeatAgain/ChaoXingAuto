package browser.studyHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.teamdev.jexplorer.dom.DOMDocument;
import com.teamdev.jexplorer.dom.DOMNode;
import com.teamdev.jexplorer.dom.DOMNodeList;
import com.teamdev.jexplorer.dom.DOMNodeType;
import com.teamdev.jexplorer.internal.dom.ElementImpl;
import com.teamdev.jexplorer.internal.dom.IFrameImpl;

import browser.httpclients.SearchAnswer;
import exception.ConfirmWorkFailException;
import exception.NoMoreAnswer;
import exception.WorkFrameCanNotFoundException;
import threadpool.ThreadPool;

public class WorkHandler {

	private VideoWebListener vw;
	private SearchAnswer sa;
	static boolean work_start = false;
	public static boolean work_random = false;
	Future<?> thread = null;
	private IFrameImpl ifr;

	private Logger a = Logger.getLogger(VideoHandler.class);
	public WorkHandler(VideoWebListener vw) {
		this.vw = vw;
	}

	void query(IFrameImpl findQues, Future<?> now) {
		if (work_start)
			return;
		work_start = true;
		this.ifr = findQues;
		if (checkPoint()) {// 检测是否已完成
			work_start = false;
			return;
		}
		thread = now;
		sa = new SearchAnswer();
		List<ElementImpl> ques = getQuesetions(findQues);
		String[] topics = getTopics(ques);
		Map<String, Map<ElementImpl, String>> framework = getFramework(topics, ques);
		doWork(framework);
	}

	/*
	 * 检查已完成
	 */
	private boolean checkPoint() {
		DOMDocument dom = ifr.getContentDocument();
		DOMNodeList dl = dom.getElementsByTagName("h3");
		if (dl.getLength() != 0) {
			DOMNode ei = dl.item(0);
			DOMNodeList spans = ei.getChildNodes();
			for (int i = 0; i < spans.getLength(); i++) {
				DOMNode span = spans.item(i);
				if (span.getNodeType().equals(DOMNodeType.TextNode))
					continue;
				if (span.getNodeName().equalsIgnoreCase("span")) {
					String point = span.getTextContent();
					a.info(point);
					if (point.trim().equals("已完成")) {
						vw.changeChapter();
						return true;
					}
				}
			}
		}
		return false;
	}

	private void doWork(Map<String, Map<ElementImpl, String>> framework) {
		String[] topic = new String[framework.size()];
		int i = 0;
		for (String key : framework.keySet()) {
			topic[i++] = key;
		}
		Object[][] obj = sa.getAnswer(topic);
		boolean allRight = fillInput(obj, framework, topic);
		submuit(ifr, allRight);
		chooseTagConfirm();
		work_start = false;
	}

	/*
	 * 选择章节确认
	 */
	private void chooseTagConfirm() {
		DOMDocument dom = vw.brwoser.getDocument();
		ElementImpl now = vw.getCurrentTitle(dom);
		if (now != null)
			now.click();
		else
			throw new ConfirmWorkFailException("未知错误");
	}

	/*
	 * 填空
	 */
	private boolean fillInput(Object[][] obj, Map<String, Map<ElementImpl, String>> framework, String[] topic) {
		boolean allRight = true;
		for (int i = 0; i < obj.length; i++) {
			Map<ElementImpl, String> inner = framework.get(topic[i]);
			Object[] ques = obj[i];
			String[] answer = null;
			if (ques[1] instanceof NoMoreAnswer) {
				Object answer2 = sa.getAnswer2(topic[i]);
				if(answer2 instanceof NoMoreAnswer&& work_random) {
					answer = getRandomAnswer(topic[i],inner);
				}else if (answer2 instanceof NoMoreAnswer) {
					allRight = false;
					continue;
				}else
					answer = getAnswerFrom2((String) answer2);
			} else
				answer = (String[]) ques[1];
			for (int j = 0; j < answer.length; j++) {
				for (ElementImpl ei : inner.keySet()) {
					String content = inner.get(ei);
					a.info(content);
					if (content.equals(answer[j]) || content.contains(answer[j]) || answer[j].contains(content)) {
						ei.click();
						ThreadPool.sleep(500);
						break;
					}
				}
			}
		}
		ThreadPool.sleep(2000);
		return allRight;

	}

	private String[] getRandomAnswer(String topic, Map<ElementImpl, String> inner) {
		String type = topic.split("~")[0];
		int _type = Integer.parseInt(type);
		String[] answer = new String[inner.size()];
		int i=0;
		for(ElementImpl ei:inner.keySet()) {
			answer[i++]=inner.get(ei);
		}
		Random r = new Random();
		if (_type==1||_type==3) {
			int index = r.nextInt(inner.size());
			a.debug(answer[index]);
			return new String[] { answer[index] };
		}else if(_type==2) {
			LinkedHashSet<String> sh = new LinkedHashSet<String>();
			for ( i = 0; i < inner.size(); i++) {
				sh.add(answer[r.nextInt(inner.size())]);
			}
			return sh.toArray(new String[] {});
		}
			throw new ConfirmWorkFailException("随机答案失败");
	}

	/*
	 * 提交答案
	 */
	private void submuit(IFrameImpl findQues, boolean allRight) {
		ElementImpl[] btns = getSubBtn(findQues);
		if (allRight) {
			ThreadPool.sleep(1000);
			btns[0].click();
			ThreadPool.sleep(2000);
//			System.out.println(btns[1].getAttribute("href"));
			btns[1].click();
		}else
			Thread.currentThread().interrupt();
	}

	/*
	 * 从第二个题库中获取答案
	 */
	private String[] getAnswerFrom2(String answer2) {
		String[] answer;
		if (answer2.equals("正确"))
			answer = new String[] { "true" };
		else if (answer2.equals("错误"))
			answer = new String[] { "false" };
		else if (answer2.indexOf("#") != -1) {
			answer = answer2.trim().split("#");
		} else
			answer = new String[] { answer2 };
		return answer;
	}

	/*
	 * 获取问题的选项
	 */
	private Map<String, Map<ElementImpl, String>> getFramework(String[] topics, List<ElementImpl> ques) {
		Map<String, Map<ElementImpl, String>> framework = new LinkedHashMap<>();
		for (int i = 0; i < topics.length; i++) {
			Map<ElementImpl, String> entity = new LinkedHashMap<>();// 答案 与 按钮
			DOMNode node = ques.get(i).getNextSibling();
			while (node.getNodeType().equals(DOMNodeType.TextNode))
				node = node.getNextSibling();
			ElementImpl ei = (ElementImpl) node;
			DOMNodeList uls = ei.getElementsByTagName("ul");
			if (uls.getLength() == 0)
				break;
			ElementImpl ul = (ElementImpl) uls.item(0);
			DOMNodeList lis = ul.getChildNodes();
			for (int j = 0; j < lis.getLength(); j++) {
				DOMNode li = lis.item(j);
				String nodeName = li.getNodeName();
				if (!nodeName.equalsIgnoreCase("li"))
					continue;
				ElementImpl _li = (ElementImpl) li;
				DOMNodeList inputs = _li.getElementsByTagName("input");// 选项按钮
				ElementImpl input = null;
				if (inputs.getLength() != 0) {
					input = (ElementImpl) inputs.item(0);
					String value = input.getAttribute("value");
					a.debug(value);
					if (value.equals("true") || value.equals("false")) {
						entity.put(input, value);
						continue;
					}
				}
				DOMNodeList as = _li.getElementsByTagName("a");// 选项内容
				ElementImpl _a = null;
				if (as.getLength() != 0) {
					_a = (ElementImpl) as.item(0);
					a.debug(_a.getTextContent());
				}
				if (_a != null && input != null)
					entity.put(input, _a.getTextContent());
			}
			framework.put(topics[i], entity);
		}
		return framework;
	}

	/*
	 * 获取问题
	 */
	private String[] getTopics(List<ElementImpl> ques) {
		String[] temp = new String[ques.size()];
		int k = 0;
		for (ElementImpl ei : ques) {
			DOMNodeList dl = ei.getChildNodes();
			for (int i = 0; i < dl.getLength(); i++) {
				DOMNode node = dl.item(i);
				String textnode = node.getTextContent();
				if (textnode.contains("】") || textnode.contains("【")) {
					temp[k++] = textnode;
					a.info(textnode);
				}
			}
		}
		// 处理题目
		for (int i = 0; i < temp.length; i++) {
			int index1 = temp[i].indexOf("【");
			int index2 = temp[i].indexOf("】");
			String type_str = temp[i].substring(index1 + 1, index2);
			int type = getTopicType(type_str);
			if (temp[i].contains("？") || temp[i].contains("。") || temp[i].contains("(") || temp[i].contains("?")) {
				int[] indexs = new int[] { temp[i].indexOf("？"), temp[i].indexOf("。"), temp[i].indexOf("("),
						temp[i].indexOf("?") };
				Arrays.sort(indexs);
				for (int j = 0; j < indexs.length; j++) {
					if (indexs[j] <= 0)
						continue;
					else {
						temp[i] = temp[i].substring(index2 + 1, indexs[j]);
						break;
					}
				}
			} else
				temp[i] = temp[i].substring(index2 + 1, temp[i].length());
			temp[i] = type + "~" + temp[i];
			a.debug(temp[i]);
		}
		return temp;
	}

	/*
	 * 获取题目类型
	 */
	private int getTopicType(String type) {
		int res;
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
	 * 获取题目DIV
	 */
	private List<ElementImpl> getQuesetions(IFrameImpl findQues) {
		DOMDocument dom = findQues.getContentDocument();
		if (dom == null)
			return null;
		DOMNodeList is = dom.getElementsByTagName("div");
		List<ElementImpl> timeList = new ArrayList<>();
		for (int i = 0; i < is.getLength(); i++) {
			ElementImpl ei = (ElementImpl) is.item(i);
			a.debug(ei.getClassName());
			if (ei.getClassName().contains("Zy_TItle"))
				timeList.add(ei);
		}
		if(timeList.size()==0)
			throw new WorkFrameCanNotFoundException();
		return timeList;
	}

	/*
	 * 获取提交按钮
	 */
	private ElementImpl[] getSubBtn(IFrameImpl findQues) {
		DOMDocument dom = findQues.getContentDocument();
		if (dom == null)
			return null;
		ElementImpl[] btns = new ElementImpl[2];
		DOMNodeList is = dom.getElementsByTagName("a");
		for (int i = 0, k = 0; i < is.getLength() && k < 2; i++) {
			ElementImpl ei = (ElementImpl) is.item(i);
			String className = ei.getClassName();
			a.debug(className);
			if (className.contains("Btn_blue_1") || className.contains("bluebtn"))
				btns[k++] = ei;
		}
		for (int i = 0; i < btns.length; i++) {
			if(btns[i]==null)
				throw new ConfirmWorkFailException("提交按钮无法找到");
		}
		return btns;
	}
}
