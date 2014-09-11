package frameDesign;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;

import android.content.Context;
import android.os.Looper;
import file.BasicCalculator;
import file.BasicFileCache;
import file.Cache;
import file.IndexPoolOverflowException;

public class ConcurrentHandler {
	
	private static final int DEFAULT_CONTORL_NUM = 5;
	
	private int ContorlNum = DEFAULT_CONTORL_NUM;
	
	private Semaphore mSemaphore;
	
	public int getContorlNum() {
		return ContorlNum;
	}


	public void setContorlNum(int contorlNum) {
		ContorlNum = contorlNum;
	}

	private void releaseThreadSemaphore(){
		if(!Thread.currentThread().isInterrupted()){
			mSemaphore.release();
		}
	}
	
	private Cache mCache;
	
	private HttpHeap mHttpHeap;
	
	private ResponseHandler mCallBack;
	
	private CacheJudgement mCacheJudge;
	
	private ResponseParse mResponseParse;
	
	private CacheHandler mCacheHandler;
	
	public ConcurrentHandler(Cache mCache, HttpHeap mHttpHeap,
			ResponseHandler mCallBack, CacheJudgement mCacheJudge,
			ResponseParse mResponseParse) {
		this.mCache = mCache;
		this.mHttpHeap = mHttpHeap;
		this.mCallBack = mCallBack;
		this.mCacheJudge = mCacheJudge;
		this.mResponseParse = mResponseParse;
		this.mCacheHandler = new CacheHandler(mCache, mResponseParse, mCacheJudge, mCallBack);
		this.mNetworkHandler = new NetworkHandler(mCache, mHttpHeap, mResponseParse, mCallBack);
		service = Executors.newCachedThreadPool();
		mSemaphore = new Semaphore(ContorlNum);
	}
	
	public static final String DEFAULT_CACHE_DIR = "spillover";
	
	public ConcurrentHandler(Context context){
		this(new BasicFileCache(new BasicCalculator(),new File(context.getCacheDir(), DEFAULT_CACHE_DIR)),new HttpLaunch()
			,new CallBackResponse(new android.os.Handler(Looper.getMainLooper())),new CacheJudgement(),new HttpResponseParse());
	}
	
	protected ExecutorService service;
	
	/**
	 *	外部请求用for一个整体循环
	 * 
	 * @param request
	 * @throws InterruptedException 
	 */
	public void add(final Request<?> request) {
		service.execute(new Runnable() {
			@Override
			public void run() {
				try {
					mSemaphore.acquire();
				} catch (InterruptedException e1) {
					
				}
				try {
					Cache.Entry entry = mCache.get(request.getUrl());
					if(entry == null){
						noReqCacheRequest(request);
					}
					if(mCacheJudge.hasTTl(entry.ttl) || mCacheJudge.hasExpired(entry.expires)){ 
						String callBackdata = null;
			        	callBackdata = mResponseParse.byteToEntity(entry.datas,entry.headers);
			        	mCallBack.callBack(request, callBackdata);
			        	releaseThreadSemaphore();
			        	return;
					}
					
					mCacheHandler.setNotModifyHeader(request, entry);
					noReqCacheRequest(request);
				} catch (IOException e) {
					noReqCacheRequest(request);
				}
				releaseThreadSemaphore();
			}
		});
	}
	
	private NetworkHandler mNetworkHandler;
	
	public void noReqCacheRequest(Request<?> request){
		try{
			BasicHttpResponse response = mHttpHeap.handlerRequest(request);
			if(response == null){
				releaseThreadSemaphore();
				mCallBack.callErrorBack(request);
				return;
			}
			byte[] responseContent = mResponseParse.entityToBytes(
					response.getEntity(), new ByteArrayPool(mNetworkHandler.DEFAULT_POOL_SIZE));
			Map<String,String> responseHeaders = convertHeaders(response.getAllHeaders());
	        StatusLine statusLine = response.getStatusLine();
	        int statusCode = statusLine.getStatusCode();
	        
			//304操作;
	        if(statusCode == HttpStatus.SC_NOT_MODIFIED){
	        	mNetworkHandler.noModifiedHandler(request,responseHeaders);
	        	return;
			}
	        
	        // 设好缓存 
			if(request.shouldCache()){
				Cache.Entry entry = new Cache.Entry();
				long ttl = mResponseParse.parseTtl(responseHeaders.get("Cache-Control"));
				if(ttl == -1){
					mNetworkHandler.callBackResult(request, responseContent, responseHeaders);
					return; 
				} 
				entry.ttl = ttl;
				mNetworkHandler.cacheWithoutTTL(request.getUrl(),entry,responseHeaders,responseContent);
			}
			mNetworkHandler.callBackResult(request, responseContent, responseHeaders);
		} catch (IOException e) { 
			mCallBack.callErrorBack(request);
		} catch (ServerError e) {
			e.printStackTrace();
		} catch (IndexPoolOverflowException e) {
			e.printStackTrace();
		}
	}
	
    private static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
    
    
}


/**
final Semaphore semp = new Semaphore(5);
final int[] receveCount = {0};
ExecutorService threadPool = Executors.newCachedThreadPool();
for(int j=0;j<newslist.length;j++){
	final int i = j;
	threadPool.execute(new Runnable() {
		
		@Override
		public void run() {
			final int index = i;
			//try {
				//semp.acquire();
				NewsContentParams param=new NewsContentParams();
				param.setId(newslist[i].getId());
				NetHelper2.ajaxStrFromCacheAndServerInMain(getActivity(), mRequestConAPI, param, new AjaxCallback<String>(){
		
					@Override
					public void callback(String url, String object, AjaxStatus status) {
						switch(status.getCode()){
						case NetHelper2.STATE_SUCCESS:
							NewsContentResult result=new Gson().fromJson(object, NewsContentResult.class);
							switch(result.getStatus()){
							case Result.STATUS_SUCCESS:
								NewsContent content=result.getData();	
								
								String con = Util.clrearEmptyLine(content.getContent());
								if(!con.replace(" ", "").isEmpty()){
									newslist[index].setPreview(con);
								}else{
									newslist[index].setPreview(null);
								}
							}
							break;
						default:
							newslist[index].setPreview(null);
						}
							
						receveCount[0]++;
						if(receveCount[0]==newslist.length){ 
							if(isNeedRefresh){
								mAdapter.clearNews();
							}
							mAdapter.addNews(newslist);
							
						}
					}
				},0,mHandler);
				//semp.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	});
}
	onFinishLoading();
	*/
