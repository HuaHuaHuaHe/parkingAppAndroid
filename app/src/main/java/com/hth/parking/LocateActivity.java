package com.hth.parking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hth.parking.adapter.DO.SugData;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * 功能描述:
 *
 * @auther huahuahua
 * @date 修改于2020/06
 */
public class LocateActivity extends BaseActivity implements View.OnClickListener{

    /*百度地图组件*/
    private BaiduMap mBaiduMap;
    private MapView mapView = null;

    /*定位服务客户端*/
    private LocationClient mLocationClient = null;

    /*定位按钮*/
    private FloatingActionButton locate_FloatingButton;

    /*定位状态*/
    private boolean isFirstLocate = true;
    private boolean hasInitSuccess = false;

    /*导航始末位置*/
    private BNRoutePlanNode mStartNode;
    private BNRoutePlanNode mEndNode;

    /*sug搜索*/
    private EditText searchCityEditText;
    private AutoCompleteTextView sugSearchTextView;
    private SuggestionSearch mSugSearch;

    /*sug搜索显示在listView上*/
    private ListView mSugListView;
    private List<HashMap<String, String>> suggest;

    /*获取LatLng*/
    List<LatLng> sugInfoList;

    /*根据位置绘制图层时候需要的位置参数*/
    private double mCurrentLat, mCurrentLng;
    private double mEndLat, mEndLng;

    private String mSDCardPath = null;

    private static final String APP_FOLDER_NAME = "BNSDKInParking";

    /*POI搜索按钮*/
    private Button poiSearchButton;
    private PoiSearch mPoiSearch;

    /*POI搜索需要的pt（LatLng信息），在list点击事件中获取*/
    private LatLng mLatLng = null;

    /*注册poi搜索监听器*/
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
        /**
         * 获取POI搜索结果，包括searchInCity，searchNearby，searchInBound返回的搜索结果
         *
         * @param poiResult    Poi检索结果，包括城市检索，周边检索，区域检索
         */
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(LocateActivity.this, "未找到结果" + poiResult.error, Toast.LENGTH_SHORT).show();
            }
            if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                /**
                 * PoiInfo中包含了经纬度、城市、地址信息、poi名称、uid、邮编、电话等等信息；
                 可以在这里画一个自定义的图层了，然后添加点击事件，做一些操作
                 */
                List<PoiInfo> poiInfos = poiResult.getAllPoi();      //poi列表
                LatLng latLng0 = poiInfos.get(0).getLocation();
                redrawMap(latLng0, 16f);

                /*绘制poi点*/
                mBaiduMap.clear();
                for (PoiInfo poiInfo : poiInfos) {
                    String poiStr = poiInfo.getAddress();
                    LatLng poilatLng = poiInfo.getLocation();
                    Log.d("PoiTest",poiStr + "+" + poilatLng);
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_maker);
                    //构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions()
                            .position(poilatLng)
                            .icon(bitmap);
                    //在地图上添加Marker，并显示
                    mBaiduMap.addOverlay(option);

                }
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
            /**
             * 当执行以下请求时，此方法回调
             * PoiDetailSearchOption detailSearchOption = new PoiDetailSearchOption();
             detailSearchOption.poiUid(poiInfo.uid);//设置要查询的poi的uid
             mPoiSearch.searchPoiDetail(detailSearchOption);//查询poi详细信息
             */
            //poiDetailResult里面包含poi的巨多信息，你想要的都有，这里不列举了
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
        }
    };

    /*注册sug提示监听器*/
    OnGetSuggestionResultListener sugListener = new OnGetSuggestionResultListener() {
        /**/
        @Override
        public void onGetSuggestionResult(SuggestionResult suggestionResult) {
            //处理sug检索结果
            if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                return;
            }
            suggest = new ArrayList<>();

            sugInfoList = new ArrayList<>();

            for (SuggestionResult.SuggestionInfo info : suggestionResult.getAllSuggestions()) {
                if (info.getKey() != null && info.getPt() != null) {
                    HashMap map = new HashMap<>();
                    sugInfoList.add(info.getPt());
                    map.put("dis",info.getDistrict());
                    map.put("key",info.getKey());
                    suggest.add(map);
                }
            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(),
                    suggest,
                    R.layout.item_layout,
                    new String[]{"dis","key"},
                    new int[]{R.id.sug_dis,R.id.sug_key});

            sugSearchTextView.setAdapter(simpleAdapter);
            simpleAdapter.notifyDataSetChanged();
        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startPoiSerarch:
                mPoiSearch.searchNearby(new PoiNearbySearchOption()
                        .location(mLatLng)
                        .radius(1000)
                        .keyword("停车场")
                        .pageNum(5));
                sugSearchTextView.clearListSelection();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动*/
        mLocationClient = new LocationClient(getApplicationContext());
        /*注册位置监听函数*/
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_locate);

        /*初始化百度地图组件*/
        mapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        /*显示缩放图标*/
        mapView.showZoomControls(true);

        /* marker点击事件，开始导航*/
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mStartNode = new BNRoutePlanNode.Builder()
                        .latitude(mCurrentLat)
                        .longitude(mCurrentLng)
                        .coordinateType(BNRoutePlanNode.CoordinateType.GCJ02)
                        .build();
                mEndNode = new BNRoutePlanNode.Builder()
                        .latitude(marker.getPosition().latitude)
                        .longitude(marker.getPosition().longitude)
                        .name(marker.getTitle())
                        .coordinateType(BNRoutePlanNode.CoordinateType.GCJ02)
                        .build();
                routePlanToNavi(mStartNode,mEndNode,null);
                return false;
            }
        });

        /*调整地图缩放图标（加减号）的位置*/
//        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
//            @Override
//            public void onMapLoaded() {
//                mapView.setZoomControlsPosition(new Point(200,600));
//            }
//        });

        /*左下角的定位按钮初始化
         * 我的理解是：我自己光标已经在地图上了，只是需0要将地图以我的位置重绘而已*/
        locate_FloatingButton = (FloatingActionButton) findViewById(R.id.location_floatingActionButton);
        locate_FloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redrawMap(new LatLng(mCurrentLat, mCurrentLng), 16f);
            }
        });

        /*POI搜索按钮初始化*/
        poiSearchButton = findViewById(R.id.startPoiSerarch);
        poiSearchButton.setOnClickListener(this);
        /*通过searchCity获得城市信息，不能为空*/
        searchCityEditText = findViewById(R.id.searchCity);
        /*注册POI搜索*/
        mPoiSearch = PoiSearch.newInstance();                    //初始化poi检索
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);   //注册搜索事件监听

        /*注册sug搜索*/
        mSugSearch = SuggestionSearch.newInstance();
        mSugSearch.setOnGetSuggestionResultListener(sugListener);

        /*SugDataFragment实例*/
        /**
         * 问题：为什么这里的City传不过去
         * 分析1：在这里赋值是可以过去的，所有应该是其他原因
         * 分析2：应该是我开始创建这个时候LocationClient之类的还没初始化，我把创建Fragment放在调用它时候创建应该可以解决。
         *        我佛了，还是没有解决！！
         *        就算放在replaceframgment中也不行，我佛了！！
         * 分析3：原来我定义的mCity和mCurrentLat和mCurrentLng的都没有获得值
         * 分析4：我真的佛了，分析2是对的啊，为什么当时该没有成功啊，还有mCity和mCurrentLat和mCurrentLng都获得值了啊！！傻逼软件？神奇的事情！！！
         */
//        searchFragment = new SearchFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString("mCity",mCity); //key-Value
//        searchFragment.setArguments(bundle);

        /*sug搜索显示list*/
        mSugListView = findViewById(R.id.sugList);

        /*位置搜索组件初始化
         * R.id.sug_fragment是locateActivity中的空LinearLayout*/
        sugSearchTextView = (AutoCompleteTextView) findViewById(R.id.sug_search);
        sugSearchTextView.setThreshold(2);
        sugSearchTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length()<=0 ){
                    return;
                }
                /* 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新 */
                mSugSearch.requestSuggestion((new SuggestionSearchOption())
                        .keyword(charSequence.toString())
                        .city(searchCityEditText.getText().toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        sugSearchTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mLatLng = sugInfoList.get(i);
                Log.d("testAAA",mLatLng.toString());
            }
        });

        /*权限初始化、导航初始化（先要初始化文件夹信息）*/
        initpermis();
        if (initDirs()) {
            initNavi();

        }
    }

    /*toolbar初始化方法*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    /*toolbar初始化方法*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.backup:
                break;
            case R.id.delete:
                break;
            case R.id.setting:
                //现在只定义setteing，按下跳转到setting界面
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*onPostResume方法是指onResume方法彻底执行完毕的回调，onPostCreate类似*/
    @Override
    protected void onPostResume() {
        super.onPostResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);  //关闭定位图层
        mSugSearch.destroy();
        mPoiSearch.destroy();
        super.onDestroy();
    }

    /*初始化定位信息*/
    private void initLocation() {
        //声明LocationClient类实例并配置定位参数
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setScanSpan(2000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    /*初始化权限*/
    private void initpermis() {
        List<String> permissionlist = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(LocateActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionlist.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(LocateActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionlist.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(LocateActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionlist.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(LocateActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionlist.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!permissionlist.isEmpty()) {
            String[] permissions = permissionlist.toArray(new String[permissionlist.size()]);
            ActivityCompat.requestPermissions(LocateActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }

    /*初始化导航*/
    private void initNavi() {
        BaiduNaviManagerFactory.getBaiduNaviManager().init(this,
                mSDCardPath, APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {

                    @Override
                    public void onAuthResult(int status, String msg) {
                        String result;
                        if (0 == status) {
                            result = "key校验成功!";
                        } else {
                            result = "key校验失败, " + msg;
                        }
                        Toast.makeText(LocateActivity.this, result, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void initStart() {
                        Toast.makeText(LocateActivity.this.getApplicationContext(),
                                "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void initSuccess() {
                        Toast.makeText(LocateActivity.this.getApplicationContext(),
                                "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                        // 初始化tts
                        initTTS();
                        BaiduNaviManagerFactory.getBaiduNaviManager().enableOutLog(true);
                    }

                    @Override
                    public void initFailed(int errCode) {
                        Toast.makeText(LocateActivity.this.getApplicationContext(),
                                "百度导航引擎初始化失败 " + errCode, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /*初始化语音*/
    private void initTTS() {
        // 使用内置TTS
        BaiduNaviManagerFactory.getTTSManager().initTTS(getApplicationContext(),
                getSdcardDir(), APP_FOLDER_NAME, "20162353");
    }

    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Nullable
    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    /*调起导航的方法*/
    private void routePlanToNavi(BNRoutePlanNode sNode, BNRoutePlanNode eNode, final Bundle bundle) {
        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(sNode);
        list.add(eNode);

        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                bundle,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                Toast.makeText(getApplicationContext(),
                                        "算路开始", Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                Toast.makeText(getApplicationContext(),
                                        "算路成功", Toast.LENGTH_SHORT).show();
                                // 躲避限行消息
                                Bundle infoBundle = (Bundle) msg.obj;
                                if (infoBundle != null) {
                                    String info = infoBundle.getString(
                                            BNaviCommonParams.BNRouteInfoKey.TRAFFIC_LIMIT_INFO
                                    );
                                    Log.d("OnSdkDemo", "info = " + info);
                                }

                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                Toast.makeText(getApplicationContext(),
                                        "算路失败", Toast.LENGTH_SHORT).show();
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                Log.d("OnSdkDemo","算路成功准备进入导航"+"--"+bundle);
                                Toast.makeText(getApplicationContext(),
                                        "算路成功准备进入导航", Toast.LENGTH_SHORT).show();

                                Intent intent = null;
                                if (bundle == null) {
                                    intent = new Intent(LocateActivity.this,
                                            GuideActivity.class);
                                } else {
                                }
                                startActivity(intent);
                                break;
                            default:
                                // nothing
                                break;
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void locationUpdate(BDLocation bdLocation) {
        if (isFirstLocate) {
            redrawMap(new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude()), 16f);
        }
        /*百度地图提供的MyLocationData.Builder类，传入latitude和longitude就可以显示小光标*/
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(bdLocation.getLatitude());
        locationBuilder.longitude(bdLocation.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        mBaiduMap.setMyLocationData(locationData);
    }

    /*依照LatLng为中心来重绘地图*/
    private void redrawMap(LatLng latLng, float v) {
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(v);
        mBaiduMap.animateMapStatus(update);
    }


    /*位置监听器：获取位置并回调*/
    public class MyLocationListener extends BDAbstractLocationListener {

        //可以通过runOnUiThread()方法来更新ui
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    StringBuilder currentPostion = new StringBuilder();
//                    currentPostion
//                }
//            });
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (null != bdLocation && bdLocation.getLocType() != BDLocation.TypeServerError) {
                mCurrentLat = bdLocation.getLatitude();
                mCurrentLng = bdLocation.getLongitude();
                /*如果是第一次进入APP，就调用locationUpdate以自己位置为中心重绘地图*/
                locationUpdate(bdLocation);
            }
        }
    }
}