package test;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.android_spillover.R;

import frameDesign.ConcurrentHandler;
import frameDesign.Request;

public class MainActivity extends Activity{

	private ListView mListView = null;
	
	private BaseAdapter ad ;
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainview);
		handler = new ConcurrentHandler(this);
		initWeiget();
		
		/*Request<Bitmap> request = new Request<Bitmap>("http://sh.suijue.com/picture/1/223/1.jpg",
				new Request.ResponseListener<Bitmap>() {

				@Override
				public void callErrorBack() {
					
				}

				@Override
				public void callBack(Object responseData) {
					Bitmap b = (Bitmap)responseData;
					if(b == null){
						Log.i("DemoLog", "-------");
					}
					mImageView.setImageBitmap(b);						
				}


		}) {
			
			@Override
			public boolean shouldCache() {
				return true;
			}

			@Override
			public Map<String, String> getHeader() {
				return null;
			}

			@Override
			public Map<String, String> getParam() {
				return null;
			}


			@Override
			protected Bitmap handlerCallBack(byte[] responseContent,
					String callBackdata) {
				return Bytes2Bimap(responseContent,Config.RGB_565);
			}
			
		};
		try {
			SpillOver.newRequestQueue(MainActivity.this).add(request);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IndexPoolOverflowException e) {
			e.printStackTrace();
		}
		*/
		ad = new BaseAdapter() {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item, null);
				ImageView img = (ImageView)view.findViewById(R.id.img_);
				if(maps[position] != null){
					img.setImageBitmap(maps[position]);
				}
				return view;
			}
			
			@Override
			public long getItemId(int position) {
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				return null;
			}
			
			@Override
			public int getCount() {
				return requestUrls.length;
			}
		};
		mListView.setAdapter(ad);
		
		for(int i = 0 ; i < requestUrls.length ; i++){
			add(i);
		}
	}
	
	ConcurrentHandler handler = null;
	
	private String[] requestUrls = {
			"http://192.168.1.104:8080/Test/1.png",
			"http://192.168.1.104:8080/Test/2.png",
			"http://192.168.1.104:8080/Test/3.png",
			"http://192.168.1.104:8080/Test/4.png",
			"http://192.168.1.104:8080/Test/5.png",
			"http://192.168.1.104:8080/Test/6.png",
			"http://192.168.1.104:8080/Test/7.png",
			"http://192.168.1.104:8080/Test/8.png",
			"http://192.168.1.104:8080/Test/9.png",
			"http://192.168.1.104:8080/Test/11.png",
			"http://192.168.1.104:8080/Test/12.png",
			"http://192.168.1.104:8080/Test/13.png",
			"http://192.168.1.104:8080/Test/14.png",
			"http://192.168.1.104:8080/Test/15.png",
			"http://192.168.1.104:8080/Test/16.png",
	};
	
	private Bitmap Bytes2Bimap(byte[] b,Config config){   
	    if(b.length!=0){
	    	BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
	    	decodeOptions.inPreferredConfig = config;
	        return BitmapFactory.decodeByteArray(b, 0, b.length,decodeOptions);
	    } else {
	        return null;
	    }
	}
	
	private Bitmap[] maps = new Bitmap[requestUrls.length];
	
	private void initWeiget() {
		mListView = (ListView)findViewById(R.id.listview);
	}
	
	private void add(final int position ){
		
		handler.add(new Request<Bitmap>(requestUrls[position],new Request.ResponseListener<Bitmap>() {


					@Override
					public void callErrorBack() {
						Log.i("DemoLog", "ssssssss");
					}

					@Override
					public void callBack(Object responseData) {
						Bitmap b = (Bitmap)responseData;
						maps[position] = b;
						ad.notifyDataSetChanged();
					}
	
		}) {
	
				@Override
				public boolean shouldCache() {
					return true;
				}

				@Override
				public Map<String, String> getHeader() {
					Map<String,String> map = new HashMap<String, String>();
					return null;
				}
	
				@Override
				public Map<String, String> getParam() {
					Map<String,String> map = new HashMap<String, String>();
					return null;
				}

				@Override
				protected Bitmap handlerCallBack(byte[] responseContent,
						String callBackdata) {
					return Bytes2Bimap(responseContent,Config.RGB_565);
				}
				
			});
		}
		
	
}
