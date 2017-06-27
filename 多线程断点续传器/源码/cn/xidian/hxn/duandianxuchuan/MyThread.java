package cn.xidian.hxn.duandianxuchuan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


public class MyThread implements Runnable {
/**
 * 实现一个多线程请求类
 * 功能：
 * 		线程单独下载指定下载内容（文件的部分片段）
 * 使用：
 * 		传入三个参数：
 * 			startIndex：文件起始位置（闭区间）
 * 			endIndex：文件下载终点（闭区间）
 * 			threadId：程序外部传入线程ID编号
 * */
	int startIndex;
	int endIndex;
	int threadId; 
	String path;
	//构造函数
	public MyThread(int startIndex,int endIndex,int threadId) {
		super();
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.threadId = threadId;
	}
	public void run() {
	 /**
	  * 设计思路：
	  * 		首先实现多线程下载
	  * 				1. 获取链接，设置请求头
	  * 				2. 设置Range：byte＝startIndex－endIndex（请求片段下载）
	  * 				3. 接受响应（206）
	  * 				4. 获取链接的输入流
	  * 				5. 使用RandomAccessFile类对本地目标文件进行写（seek(startIndex)）
	  * 				6. 关闭当前线程
	  * 				7. 当所有线程均被关闭后结束
	  * 		实现断点续传功能
	  * 				1. 创建当前线程对应的文件保存文件下载进度
	  * 				2. 当在请求时判断该记录保存文件是否存在
	  * 					存在：本次请求文件下载的开始索引＝上次文件下载进度＋当前设置的文件开始位置
	  * 					不存在：文件第一次下载
	  * */
		URL url;
		try {
			File threadTempFile = new File(threadId+".txt");
			//判断是否第一次下载	
				if(threadTempFile.exists()){
					//做测试
					System.out.println("断点续传功能开启");		
					FileInputStream fis = new FileInputStream(threadTempFile);
					BufferedReader bis = new BufferedReader(new InputStreamReader(fis));
					//做测试
					System.out.println("线程"+threadId+"起初分配开始位置是"+startIndex);
					startIndex += Integer.parseInt(bis.readLine());
					//做测试
					System.out.println("线程"+threadId+"断点续传的开始位置是"+startIndex);
					fis.close();
					}
			//请求文件
				System.out.println("线程" + threadId + "下载区间是" + startIndex +"====" + endIndex);
				url = new URL(Multidownload.path);
				HttpURLConnection connect = (HttpURLConnection) url.openConnection();
				connect.setRequestMethod("GET");
				connect.setConnectTimeout(5000);
				connect.setReadTimeout(5000);
				//设置请求数据的区间
				connect.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
				if(connect.getResponseCode() == 206){
					//获取下载流
					InputStream download = connect.getInputStream();
					//获取目标文件的引用
					File downLoadFile = new File("downLoadFile.txt");
					RandomAccessFile raf = new RandomAccessFile(downLoadFile, "rwd");
					//设置开始写的位置
					raf.seek(startIndex);
					//流copy（为演示效果，此处字节数组设置较小）
					byte[] b = new byte[10];
					int len = 0;
					int total = 0;
					while((len = download.read(b)) != -1){
						raf.write(b, 0, len);
						//一定要把当前下载记录写在线程文件中做保留
						RandomAccessFile threadTempFileRaf = new RandomAccessFile(threadTempFile, "rwd");
						total += len; 
						//默认UTF-8编码
						threadTempFileRaf.write((total+"").getBytes());
						threadTempFileRaf.close();
						//作测试
						System.out.println("线程" + threadId + "下载了" + total);
						//设置断点测试
//						if(total == 150){
//							return;
//						}
					}
					System.out.println("线程" + threadId + "下载过程结束========");
					raf.close();
					//线程完成计数
					Multidownload.finishedThread++;
					//线程安全（同步）
					synchronized(Multidownload.path){
						if(Multidownload.finishedThread == Multidownload.ThreadNum){
							//传
							for(int i = 0; i <Multidownload.ThreadNum ; i++){
								File filefinish = new File(i + ".txt");
								filefinish.delete();
							}
							Multidownload.finishedThread = 0;
						}
					}
					
				}
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
					System.out.println("读文件出错");
				} catch (IOException e) {
					e.printStackTrace();
				} 
		
	}

}
