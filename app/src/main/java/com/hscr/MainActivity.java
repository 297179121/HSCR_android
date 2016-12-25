package com.hscr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.identity.Files;
import com.identity.Shell;
import com.identity.globalEnum;

import java.io.IOException;
import java.util.Set;


public class MainActivity extends Activity {
	/** Called when the activity is first created. */
		
	private BluetoothAdapter mAdapter;
	private BluetoothDevice mDevice;
	//private ArrayAdapter<String> mInfoView; 
	private static final int REQUEST_ENABLE_BT = 2;
	private Shell shell;
	//private boolean bInitial = false;
	private boolean bStop = false;
	private boolean bConnected = false;
	private final static int PICK_PHOTO = 1111;


	private WebView webView = null;
	
	
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.w("Activity", "========onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.w("Activity", "========onStart");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.w("Activity", "========onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.w("Activity", "========onStop");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.w("Activity", "=======onResume");

	}

	@Override 
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//0316 Handler.removeCallbacks(mRunnable); 
		bStop = true;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();  
		}			
		Log.w("Activity", "========onDestroy");
		try {
			if(bConnected)
			{	
				Log.w("Activity", "onDestroy bConnected is true");
			if (shell.EndCommunication()) {
				shell.Destroy();
			}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	final class InJavaScript {
        public void runOnAndroidJavaScript(final String str) {
        	handler.post(new Runnable() {
                public void run() { 
                	//showClientMsg("send operation = " +  str);                	
                	responseOpr(str);
                }
            });
        }
    }
	
	public void controlBtn(String msg){			
		String str = "javascript:controlBtn('" + msg + "')";			
		webView.loadUrl(str);
	}
	
	public void showClientMsg(String msg){			
		String str = "javascript:showmymsg('" + msg + "')";			
		webView.loadUrl(str);
	}
	
	public void showlocalmsg(String msg){
		 
		new AlertDialog.Builder(this).setTitle("确认")
		.setMessage("确定吗？")
		.setPositiveButton("是", null)
		.setNegativeButton("否", null) 
   	    .show();
	}
	
	
	
	public void responseOpr(String str){		
		if(str != null && str.equals("1"))
		{
			this.initDevice();			
			
		}else if(str != null && str.equals("2"))
		{
			startDevice();
		}else
		{
			stopRun();
		}		
	}	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_card);
		
		setDispView();
		
		initDevice();
	}
	
	public void startDevice()
	{		
		globalEnum ge = globalEnum.NONE;
		//mInfoView.add("ButtonInitOnClick");	
		try {
			if (shell.Register())
			{ 				
				//showClientMsg("取机具编号成功！");
				ge = shell.Init(); 
				if (ge == globalEnum.INITIAL_SUCCESS) {
					showClientMsg("建立连接成功！");
					// 前台按钮控制
					controlBtn("2");					
					bConnected = true;					    
					new Thread(new GetDataThread()).start();
				} else {
					shell.EndCommunication();//0316
					showClientMsg("建立连接失败,请重新执行应用程序");
				}
			}else 
			{
				showClientMsg("没搜索到蓝牙设备，请重新执行应用程序！");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void stopRun()
	{		
		// TODO Auto-generated method stub
		bStop = true;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try { 
			if(bConnected)
			{ 
				if (shell.EndCommunication()) {
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
					
		android.os.Process.killProcess(android.os.Process.myPid());  
		System.exit(0);		
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	public void setDispView()
	{
        webView = (WebView) findViewById(R.id.webview);
        
        //把本类的一个实例添加到js的全局对象window中，
        //这样就可以使用window.injs来调用它的方法
        webView.addJavascriptInterface(new InJavaScript(), "injs");
        
		//设置支持JavaScript脚本
		WebSettings webSettings = webView.getSettings();  
		webSettings.setJavaScriptEnabled(true);
		//设置可以访问文件
		webSettings.setAllowFileAccess(true);
		//设置支持缩放
		webSettings.setBuiltInZoomControls(true);
		
		webSettings.setDatabaseEnabled(true);  
		String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
		webSettings.setDatabasePath(dir);
		
		//使用localStorage则必须打开
		webSettings.setDomStorageEnabled(true);
		
		webSettings.setGeolocationEnabled(true);
		//webSettings.setGeolocationDatabasePath(dir);
	
		//设置WebViewClient
		webView.setWebViewClient(new WebViewClient(){   
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {   
		        view.loadUrl(url);   
		        return true;   
		    }  
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
		});
		
		
		//设置WebChromeClient
		webView.setWebChromeClient(new WebChromeClient(){
			//处理javascript中的alert
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
				//构建一个Builder来显示网页中的对话框
				Builder builder = new Builder(MainActivity.this);
				builder.setTitle("提示");
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			};
			//处理javascript中的confirm
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
				Builder builder = new Builder(MainActivity.this);
				builder.setTitle("confirm");
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						});
				builder.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								result.cancel();
							}
						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			};
			
			@Override
			//设置网页加载的进度条
			public void onProgressChanged(WebView view, int newProgress) {
				MainActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress * 100);
				super.onProgressChanged(view, newProgress);
			}

			//设置应用程序的标题title
			public void onReceivedTitle(WebView view, String title) {
				MainActivity.this.setTitle(title);
				super.onReceivedTitle(view, title);
			}

			public void onExceededDatabaseQuota(String url,
					String databaseIdentifier, long currentQuota,
					long estimatedSize, long totalUsedQuota,
					WebStorage.QuotaUpdater quotaUpdater) {
				quotaUpdater.updateQuota(estimatedSize * 2);
			}
			
			public void onGeolocationPermissionsShowPrompt(String origin,
					GeolocationPermissions.Callback callback) {
				callback.invoke(origin, true, false);
				super.onGeolocationPermissionsShowPrompt(origin, callback);
			}
			
			public void onReachedMaxAppCacheSize(long spaceNeeded,
					long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
				quotaUpdater.updateQuota(spaceNeeded * 2);
			}
		});
		// 覆盖默认后退按钮的作用，替换成WebView里的查看历史页面  
		webView.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if ((keyCode == KeyEvent.KEYCODE_BACK)
							&& webView.canGoBack()) {
						webView.goBack();
						return true;
					}
				}
				return false;
			}
		});
		
		//webView.loadUrl("file:///android_asset/index.html");
		
		//webView.loadUrl("http://120.26.36.130:8088/hscr/index.jsp");
		
		webHtml(webView);
	}
	
	 
	/**
	* 直接网页显示 
	*/  
	private void webHtml(WebView webView) {
		try {
			//webView.loadUrl("http://120.26.36.130:8088/hscr/index.jsp"); 
		    //webView.loadUrl("http://218.25.20.35:8081/hscr/index.jsp");
			webView.loadUrl("http://192.168.1.102:8080/hscr/index.jsp");
		} catch (Exception ex) {  
		    //ex.printStackTrace();  
			showClientMsg("远程连接失败,请检查网络设置!");
		}  
	} 

	
	private void initDevice(){
		
	    mAdapter = BluetoothAdapter.getDefaultAdapter();
	
		Files file = new Files(this.getApplicationContext());		
		
		if (file.IsExist("IDRegister.lic")) {
			//0316 btnRegist.setEnabled(false);
		}  
		// mInfoView.add("开始"); 
		//writeLogToSD("testJarActivity onCreatee"); 
		if (mAdapter == null) {
			//mInfoView.clear();
			//mInfoView.add("mAdapter is null!");
			showClientMsg("mAdapter is null!");
		}
		// mInfoView.add("mAdapter is success!");  
		if (!mAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(  BluetoothAdapter.ACTION_REQUEST_ENABLE );
	
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}  
		//String ly_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		//System.out.println("现在时间是:"+ly_time);
		
		Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
					String deviceName = device.getName();
				 	String str = deviceName.length()>=4? deviceName.substring(0, 3):"";
					Log.w("pairedDevices", "device.getName().substring(0, 3) is:"+str);				
					if(str.equalsIgnoreCase("SYN")){	
						Log.w("onCreate", "device.getName() is SYNTHESIS");			
						mDevice = device;	
					}else   //是否能进入Else
					{
						Log.w("onCreate", "device.getName() is not SYNTHESIS");
						boolean bAllNum=false;
						str = deviceName.length()>=11?deviceName.substring(0, 10):"";
						bAllNum = str.matches("[0-9]+");
						if(bAllNum==true)
						{			
							mDevice = device;							
						}
					} 
				//	mInfoView.clear();
				//mInfoView.add(device.getName() + "====" + device.getAddress());
			}
			try {
				mAdapter.cancelDiscovery();
				shell = new Shell(this.getApplicationContext(), mDevice);
				
				//mInfoView.add("connect time is:  "+d+"."+md+"s");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.w("test", "Socket connect error！");
				showClientMsg("与机具建立连接失败，请尝试重新启动应用程序!");
			}
			Log.w("test", "Socket connect OK！");
			// 前台按钮控制
			controlBtn("1");
		}
		
	}


	 public boolean OnKeyDown(int keyCode,KeyEvent event){   
	        if (keyCode==KeyEvent.KEYCODE_BACK ) {
	        	bStop = true;
				android.os.Process.killProcess(android.os.Process.myPid()); 
				System.exit(0);
	        }      
	        return false;   
	     }
	
	//0316
	private class GetDataThread implements Runnable{
		private String data ;	
		private byte[] cardInfo = new byte[256];		
		private int count = 0 ;
		private Message msg;//主要改了这个地方，好像启作用了
		private String wltPath="";
		private String termBPath="";
		private boolean bRet = false;			
			
		public GetDataThread(){
			}        
		public void run() {				
				
			// TODO Auto-generated method stub    
			Log.w("Activity", "new sample GetDataThread --");
			globalEnum ge = globalEnum.GetIndentiyCardData_GetData_Failed;
			try {
				//0316n
				//data = null;//
				//msg = handler.obtainMessage(87, data);//发送消息
				//handler.sendMessage(msg);				  
				Thread.sleep(2000);
				
				//globalEnum gFindCard = globalEnum.NONE;				
				while (!bStop) {//0316 gFindCard != globalEnum.FINDCARD_SUCCESS
					//Log.w("Activity", "info listening count:"+count);
					count += 1;  
				    if(count == 10) 
					{
						System.gc(); 
						System.runFinalization(); 
						count = 0;
					}					
				    data = null;//
					msg = handler.obtainMessage(71, data);//发送消息
					handler.sendMessage(msg);		
				    bRet = shell.SearchCard(); 
					if (bRet) {  			
					    data = null;//
						msg = handler.obtainMessage(1, data);//发送消息
						handler.sendMessage(msg);				      
						bRet = shell.SelectCard();
						if(bRet){  
							data = null;//
							msg = handler.obtainMessage(2, data);//发送消息
							handler.sendMessage(msg);						  
							//Thread.sleep(100);  
						
							ge = shell.ReadCard();
							if (ge == globalEnum.GetDataSuccess) {
								data = null;//
								msg = handler.obtainMessage(3, data);//发送消息
								handler.sendMessage(msg);
							
								cardInfo = shell.GetCardInfoBytes();
								/*data = String.format(
									"姓名：%s 性别：%s 民族：%s 出生日期：%s 住址：%s 身份证号：%s 签发机关：%s 有效期：%s-%s",
									shell.GetName(cardInfo), shell.GetGender(cardInfo), shell.GetNational(cardInfo),
									shell.GetBirthday(cardInfo), shell.GetAddress(cardInfo),
									shell.GetIndentityCard(cardInfo), shell.GetIssued(cardInfo),
									shell.GetStartDate(cardInfo), shell.GetEndDate(cardInfo));*/
								
								//data = String.format(
								//		"'%s','%s','%s','%s'",
								//		shell.GetName(cardInfo),shell.GetIndentityCard(cardInfo)
								//		,shell.GetGender(cardInfo), shell.GetNational(cardInfo)); 
								
								//"姓名：%s 性别：%s 民族：%s 出生日期：%s 住址：%s 身份证号：%s",
								data = String.format(
										"'%s','%s','%s','%s','%s'",
										shell.GetName(cardInfo), shell.GetGender(cardInfo), shell.GetNational(cardInfo),
										shell.GetAddress(cardInfo),shell.GetIndentityCard(cardInfo));
								
								msg = handler.obtainMessage(0, data);//发送消息
								handler.sendMessage(msg);								
								
								// 没有模块号，所以屏蔽
								wltPath="/data/data/com.testjar/files/";
								termBPath="/mnt/sdcard/";
								int nret = shell.GetPic(wltPath,termBPath); 
								if(nret > 0)
								{
									Bitmap bm = BitmapFactory.decodeFile("/data/data/com.testjar/files/zp.bmp");
									msg = handler.obtainMessage(100, bm);//发送消息
									handler.sendMessage(msg);

								}else if(nret == -5)
								{
									msg = handler.obtainMessage(101, data);//发送消息
									handler.sendMessage(msg);
							  	}else if(nret == -1)
							  	{ 
							  		msg = handler.obtainMessage(102, data);//发送消息
							  		handler.sendMessage(msg);								  
							  	} 
								//break;//0316  调试用，所以增加
							}//searchCard error							
						}//searchCard error						
					}//searchCard error					
			        SystemClock.sleep(50);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
	
	
	 public Handler handler = new Handler(){//处理UI绘制
	  private String data;
	  private Bitmap bm;	  
	
	 
	  @Override
	  public void handleMessage(Message msg) {//M_ERROR  M_VALIDATE_ERROR I_ERROR I_VALIDATE_ERROR
	   switch (msg.what) {                    //C_ERROR  C_VALIDATE_ERROR D_ERROR D_VALIDATE_ERROR
	   case 0:
		    data = (String) msg.obj;
		    if(data == null){
		    }else {		
				    
				String str = "javascript:getFromAndroid(" + data + ")";			
				webView.loadUrl(str);				
		    }
		    break; 
	   case 71:
			
		    break; 
	   case 100:
		//   bm = (Bitmap) msg.obj;
	   //    iv.setImageBitmap(bm);
	       
	       deleteFile("zp.bmp");
	       
		   break; 
	   case 101:
		   //mInfoView.clear();
			//mInfoView.add("照片解码授权文件不正确");
		   break; 
	   case 102:
		   //mInfoView.clear();
			//mInfoView.add("照片原始数据不正确");
		   break; 
		case 1:
			//mInfoView.clear();
			//mInfoView.add("SearchCard ok"); 
			break; 
		case 2:
			//mInfoView.clear();
			//mInfoView.add("SelectCard ok");
			break; 
		case 3:
		//	mInfoView.clear();
		//	mInfoView.add("ReadCard ok");


			break; 
		case 4:
			//mInfoView.clear();
			//mInfoView.add("SearchCard error");
			break; 
		case 5:
			//mInfoView.clear();
			//mInfoView.add("SelectCard error");
			break; 
		case 6:
			//mInfoView.clear();
			//mInfoView.add("ReadCard error");
			break;
		case 87:
			//mInfoView.clear();
			//mInfoView.add("读卡初始化中，请稍候...");
			showClientMsg("读卡初始化中，请稍候...");
			break;
		case 88:
			//mInfoView.clear();
			//mInfoView.add("机具信息监听中...");
			showClientMsg("机具信息监听中...");
			break;
		case 99:
			//mInfoView.clear();
	      //  iv.setImageBitmap(null);
			break;
		   default:
		    break;
	   }
	  }
	 };
	//0316
}