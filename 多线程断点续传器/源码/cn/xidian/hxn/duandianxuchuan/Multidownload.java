package cn.xidian.hxn.duandianxuchuan;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Multidownload {
	/**
	 * 1. 客户发出请求文件资源
	 * 2. 服务器反馈文件信息（并由客户端获取文件程长度）
	 * 3. 客户端对下载资源进行文件分割，并创建多线程，实现多线程下载
	 * 4. 如果有断点，重新下载时先读取上次下载记录，并设置请求资源位置
	 * 5. 下载成功
	 * */
	static int ThreadNum = 3;	//线程个数
	static String path="http://127.0.0.1:8080/1/1.txt";	//初始化文件文件下载地址
	static int finishedThread = 0;	//初始化下载完成的线程个数
	public static void main(String[] args){
		/**
		 * 1. 使用URL链接服务器
		 * 2. 获取链接
		 * 3. 设置请求参数
		 * 4. 接受响应，获取文件大小
		 * 5. 对文件进行分割
		 * 6. 多线程请求下载（自己实现类）
		 * */
		try {
			
			URL url = new URL(path);
			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setRequestMethod("GET");
			connect.setReadTimeout(5000);
			connect.setReadTimeout(5000);
			//接受响应
			if(connect.getResponseCode()==200){
				//获取请求文件长度
				int length = connect.getContentLength();
				//创建存储文件
				File downLoadFile = new File("downLoadFile.txt");
				//创建随机存储文件
				RandomAccessFile raf = new RandomAccessFile(downLoadFile, "rwd"); 
				//设置文件大小
				raf.setLength(length);
				//关闭流对象
				raf.close();
				//对线程请求(调用函数)
				multithreadRequest(length,Multidownload.ThreadNum);
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("获取链接出错");
		}
	}

	//循环遍历文件切割，分配每一个线程请求资源的索引位置
	private static void multithreadRequest(int length, int threadNum) {
		int everyLength = length/Multidownload.ThreadNum;
		for (int i = 0; i < threadNum; i++) {
			int startIndex = i * everyLength;
			int endIndex = ((i + 1) * everyLength) + 1;
			//特别注意最后一个线程的结束位置定死，确保文件全部下载
			if(i == threadNum-1){
				endIndex = length - 1;
			}
			new Thread(new MyThread(startIndex,endIndex,i)).start();
		}
	}


}
