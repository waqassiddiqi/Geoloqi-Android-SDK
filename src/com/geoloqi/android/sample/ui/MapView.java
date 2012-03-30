package com.geoloqi.android.sample.ui;

import java.util.ArrayList;
import java.util.List;

import com.geoloqi.android.sample.Constants;
import com.geoloqi.android.sample.R;
import com.geoloqi.android.sample.receiver.CurrentLocationOverlay;
import com.geoloqi.android.sample.receiver.SampleReceiver;
import com.geoloqi.android.sample.receiver.SampleReceiver.OnLocationChangedListener;
import com.geoloqi.android.sdk.LQSession;
import com.geoloqi.android.sdk.LQTracker;
import com.geoloqi.android.sdk.LQTracker.LQTrackerProfile;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.android.sdk.service.LQService.LQBinder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapView extends MapActivity implements SampleReceiver.OnLocationChangedListener {
	
	//Geoloqi Specific
	private SampleReceiver mLocationReceiver = new SampleReceiver();
	
	//Google MapView Controls
	private com.google.android.maps.MapView mapView;
	private MapController mapCtrl;	
	
	//Overlays
	private MyPositionOverlay myPositionOverlay = null;
	private OverlayItem myPositionOverlayItem = null;
	private List<Overlay> mapOverlays = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.map);
	    	    
	    //Start Geoloqi tracking service
	    Intent intent = new Intent(this, LQService.class);
        intent.setAction(LQService.ACTION_DEFAULT);
        intent.putExtra(LQService.EXTRA_SDK_ID, Constants.LQ_SDK_ID);
        intent.putExtra(LQService.EXTRA_SDK_SECRET, Constants.LQ_SDK_SECRET);
        startService(intent);              
	    
	    mapView = (com.google.android.maps.MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    
	    mapOverlays = mapView.getOverlays();
	    
	    mapCtrl = mapView.getController();	  	   
	    this.mapCtrl.setZoom(17);	    	   
	    
	    myPositionOverlay = new MyPositionOverlay(
	    		this.getResources().getDrawable(R.drawable.marker), this);	    	    
	    
	}
	
	@Override
    public void onResume() {
        super.onResume();        
        
        // Wire up the sample location receiver
        final IntentFilter filter = new IntentFilter();       
        filter.addAction(SampleReceiver.ACTION_LOCATION_CHANGED);
        registerReceiver(mLocationReceiver, filter);        
    }
	
	@Override
    public void onPause() {
        super.onPause();
        
        unregisterReceiver(mLocationReceiver);
    }
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		
		GeoPoint point = new GeoPoint((int)(location.getLatitude() * 1e6), (int)(location.getLongitude() * 1e6));
		
		if(this.myPositionOverlayItem != null) {
			this.mapView.getOverlays().remove(myPositionOverlay);
			this.myPositionOverlay.removeOverlay(myPositionOverlayItem);
		}
		
		myPositionOverlayItem = new OverlayItem(point, "Salam!", "You'r here");		
		myPositionOverlay.addOverlay(myPositionOverlayItem);
		mapOverlays.add(myPositionOverlay);
		
		this.mapCtrl.animateTo(point);
		
		mapView.invalidate();
	}
}
