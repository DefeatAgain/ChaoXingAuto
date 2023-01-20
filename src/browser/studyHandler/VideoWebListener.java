package browser.studyHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Shell;

import com.teamdev.jexplorer.Browser;
import com.teamdev.jexplorer.dom.DOMDocument;
import com.teamdev.jexplorer.dom.DOMNode;
import com.teamdev.jexplorer.dom.DOMNodeList;
import com.teamdev.jexplorer.dom.DOMNodeType;
import com.teamdev.jexplorer.event.NavigationAdapter;
import com.teamdev.jexplorer.internal.dom.ElementImpl;
import com.teamdev.jexplorer.internal.dom.IFrameImpl;
import com.teamdev.jexplorer.internal.ipc.ChannelException;

import exception.ChapterCanNotChangeException;
import exception.ChapterFinshed;
import exception.ConfirmWorkFailException;
import exception.TagCanNotFoundException;
import exception.VideoCanNotPlayException;
import exception.VideoNotFoundException;
import exception.VideoTopicException;
import threadpool.ThreadPool;
import window.dialog.ErrorSolveDialog;
import window.dialog.WarningSolveDialog;


public class VideoWebListener extends NavigationAdapter {
	private Shell shell;
	Browser brwoser;
	private VideoHandler vh = new VideoHandler(this);
	private WorkHandler wh = new WorkHandler(this);
	ElementImpl current_chapter = null;
	Future<?> now;
	
	private Logger a = Logger.getLogger(VideoWebListener.class);
	public VideoWebListener(CTabItem item) {
		this.shell = item.getParent().getParent().getShell();
	}

	@Override
	public void mainDocumentCompleted(Browser browser, String url) {
		if (browser.isDisposed())
			return;
		a.debug(url);
		this.brwoser = browser;
		if (!url.contains("studentstudy?"))
			return;
		DOMDocument dom = brwoser.getDocument();
		check_chapter(dom);
	}

	@Override
	public void frameDocumentCompleted(Browser browser, String url) {
		if (browser.isDisposed())
			return;
		a.debug(url);
		if(ManualStudy.manual)
			return;
		if (!browser.getLocationURL().contains("studentstudy?"))
			return;
		if (!url.contains("video") && !url.contains("cards?") && !url.contains("work"))
			return;
		check_chapter(browser.getDocument());
		
		now = ThreadPool.EXECUTOR_SERVICE.submit(new Runnable() {
			@Override
			public void run() {
				try {
					DOMDocument dom = browser.getDocument();
					ElementImpl current = getCurrentTitle(dom);
					if (current != null) {
						String title = current.getTitle();
						cancelVideoTimer();
						cancelWorkThread();
						switch (title) {
						case "课前学习":
						case "学习目标":
							ThreadPool.sleep(1000);
							vh.findVideo(dom);
							break;
						case "视频":
							ThreadPool.sleep(1500);
							if (!findVideoIframe(dom))
								break;
							vh.play(getVideoFrame(dom));
							break;
						case "章节测验":
						case "测验":
							ThreadPool.sleep(1000);
							if (!findQuestionIframe(dom))
								break;
							wh.query(getQuestionIframe(dom),now);
							break;
						default:
							ThreadPool.sleep(1000);
							a.error("未找到任何标签！！");
							//vh.findVideo(dom);
							break;
						}
					} else
						throw new TagCanNotFoundException("无法找到标签");
				} catch (Exception e) {
					if (e instanceof VideoCanNotPlayException) 
						vh.chooseVideoTagForError();
					else if(e instanceof TagCanNotFoundException)
						//视为阅读页面
						WarningSolveDialog.open(shell, "注意","未发现标签，可能为阅读页面");
					else if(e instanceof VideoNotFoundException)
						browser.dispose();
					else if(e instanceof VideoTopicException) {
						ErrorSolveDialog.open(shell, "Error", "请在控制台开启禁止视频题目", false, null);
						browser.dispose();
					}else if(e instanceof ChapterCanNotChangeException) {
						ErrorSolveDialog.open(shell, "Error", "网页内容已发生变化，请等待更新", false, null);
						browser.dispose();
					}else if(e instanceof NullPointerException) {
						ErrorSolveDialog.open(shell, "Error","未知错误", false, null);
						cancelWorkThread();
						cancelVideoTimer();
					}else if(e instanceof ConfirmWorkFailException && e.getMessage().equals("未知错误"))
						brwoser.refresh();
					else if(e instanceof ConfirmWorkFailException && e.getMessage().equals("提交按钮无法找到"))
						changeChapter();
					else if(e instanceof ConfirmWorkFailException) {
						WarningSolveDialog.open(shell, "注意","出现不支持的答案类型");
						Thread.currentThread().interrupt();
					}else if(e instanceof ChapterFinshed) {
						WarningSolveDialog.open(shell, "注意","未发现章节");
						browser.dispose();
					}else if(e instanceof ChannelException) {
						ErrorSolveDialog.open(shell, "Error","请把浏览器最大化", false, null);
						brwoser.dispose();
					}
					else {
						cancelWorkThread();
						cancelVideoTimer();
					}
					a.error(e.getMessage(),e);
				}
			}
		});

	}
	
	
	private void cancelWorkThread() {
		WorkHandler.work_start=false;
		if(wh.thread!=null)
			wh.thread.cancel(false);
			
	}

	private void cancelVideoTimer() {
		VideoHandler.video_start = false;
		if (vh.video_timer != null)
			vh.video_timer.cancel();
	}

	private IFrameImpl[] getVideoFrame(DOMDocument dom) {
		DOMNodeList dl = dom.getElementsByTagName("iframe");
		DOMNode f1 = dl.item(0);
		DOMDocument f2 = ((IFrameImpl) f1).getContentDocument();
		DOMNodeList videodl = f2.getElementsByTagName("iframe");
		List<IFrameImpl> iil = new ArrayList<IFrameImpl>(2);
		for (int i = 0; i < videodl.getLength(); i++) {
			IFrameImpl ii = (IFrameImpl) videodl.item(i);
			if (ii.getSrc().contains("video"))
				iil.add(ii);
		}
		return iil.toArray(new IFrameImpl[] {});
	}

	private boolean findVideoIframe(DOMDocument dom) {
		DOMNodeList dl = dom.getElementsByTagName("iframe");
		if (dl==null||dl.getLength() <= 0)
			return false;
		DOMNode f1 = dl.item(0);
		DOMDocument f2 = ((IFrameImpl) f1).getContentDocument();
		DOMNodeList d2 = f2.getElementsByTagName("iframe");
		if (d2==null||d2.getLength() <= 0)
			return false;
		return true;
	}

	private IFrameImpl getQuestionIframe(DOMDocument dom) {
		DOMNodeList dl1 = dom.getElementsByTagName("iframe");
		if (dl1.getLength() <= 0)
			return null;
		DOMNode if1 = dl1.item(0);
		DOMDocument d1 = ((IFrameImpl) if1).getContentDocument();
		DOMNodeList dl2 = d1.getElementsByTagName("iframe");
		if (dl2.getLength() <= 0)
			return null;
		DOMNode if2 = dl2.item(0);
		DOMDocument d2 = ((IFrameImpl) if2).getContentDocument();
		DOMNodeList dl3 = d2.getElementsByTagName("iframe");
		if (dl3.getLength() <= 0)
			return null;

		return (IFrameImpl) dl3.item(0);
	}

	private boolean findQuestionIframe(DOMDocument dom) {
		DOMNodeList dl1 = dom.getElementsByTagName("iframe");
		if (dl1==null||dl1.getLength() <= 0)
			return false;
		DOMNode if1 = dl1.item(0);
		DOMDocument d1 = ((IFrameImpl) if1).getContentDocument();
		DOMNodeList dl2 = d1.getElementsByTagName("iframe");
		if (dl2==null||dl2.getLength() <= 0)
			return false;
		DOMNode if2 = dl2.item(0);
		DOMDocument d2 = ((IFrameImpl) if2).getContentDocument();
		DOMNodeList dl3 = d2.getElementsByTagName("iframe");
		if (dl3==null||dl3.getLength() <= 0)
			return false;
		return true;
	}

	private void check_chapter(DOMDocument dom) {
		DOMNodeList hdl = getChapter(dom);
		if (hdl != null)
			for (int i = 0; i < hdl.getLength(); i++) {
				ElementImpl temp = (ElementImpl) hdl.item(i);
				if (temp.getClassName().contains("currents")) {
					this.current_chapter = temp;
					break;
				}
			}
	}

	private DOMNodeList getChapter(DOMDocument dom) {
		DOMNodeList hdl = dom.getElementsByTagName("h4");
		if (hdl != null && hdl.getLength() != 0)
			return hdl;
		return null;
	}
	
	/*
	 * 切换页面
	 */
	void changeChapter() {
		DOMNodeList chapters = getChapter(brwoser.getDocument());
		ElementImpl next = null;
		for (int i = 0; i < chapters.getLength(); i++) {
			ElementImpl node = (ElementImpl) chapters.item(i);
			if(!node.getId().contains("cur"))
				continue;
			if(node.getClassName().contains("current")) {
				if(i+1==chapters.getLength())
					throw new ChapterFinshed();
				next = (ElementImpl) chapters.item(i+1);
			}
		}
		DOMNodeList dl = next.getChildNodes();
		ElementImpl span = null;
		for (int i = 0; i < dl.getLength(); i++) {
			if(dl.item(i).getNodeType().equals(DOMNodeType.TextNode))
				continue;
			ElementImpl spans = (ElementImpl) dl.item(i);
			if (spans.getClassName().contains("roundpointStudent")) {
				span = spans;
//				System.out.println(span.getClassName());
				break;
			}
		}
		if(span==null)
			throw new ChapterCanNotChangeException("未发现章节点,可能已完成全部章节");
//		System.out.println(span.getClassName());
			for (int i = 0; span.getClassName().contains("lock"); i++) {
				ThreadPool.sleep(2000);
				a.info("章节锁定");
				if(i>10)
					current_chapter.click();
			}
			ThreadPool.sleep(1000);
			a.info("changechapter" + next.getId());
			next.click();
	}


	
	/*
	 * 查找Tag
	 */
	ElementImpl getCurrentTitle(DOMDocument dom) {
		ElementImpl dct1 = (ElementImpl) dom.getElementById("dct1");
		if (dct1 != null && dct1.getClassName().contains("currents"))
			return dct1;
		ElementImpl dct2 = (ElementImpl) dom.getElementById("dct2");
		if (dct2 != null && dct2.getClassName().contains("currents"))
			return dct2;
		ElementImpl dct3 = (ElementImpl) dom.getElementById("dct3");
		if (dct3 != null && dct3.getClassName().contains("currents"))
			return dct3;
		throw new TagCanNotFoundException("无法找到目前的选项卡");
	}
}
