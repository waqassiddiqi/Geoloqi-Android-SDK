package com.geoloqi.android.sample.receiver;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class CurrentLocationOverlay extends ItemizedOverlay<OverlayItem> 
	implements SampleReceiver.OnLocationChangedListener {
	
	private final List<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext = null;
	
	private Paint accuracyPaint;
	private Drawable centerDrawable;
	private Drawable drawable;
	private int width;
	private int height;
	private Point center;
	private Point left;
	private GeoPoint lastKnownPoint;
	private Location lastKnownLocation;
	private SampleReceiver srvReceiver;
	
	private Runnable firstFixRunnable = null;
	private boolean firstFixRun = false;
	
	public CurrentLocationOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}
	
	public CurrentLocationOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		this.mContext = context;
		this.centerDrawable = defaultMarker;
		
	}
	
	private void checkFirstRunnable() {
	    if (!firstFixRun && lastKnownLocation != null && firstFixRunnable != null) {
	        firstFixRunnable.run();
	    }
	}
	
	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
	    populate();
	}
	
	private void replaceOverlay(OverlayItem overlay) {
	    mOverlays.clear();
	    mOverlays.add(overlay);
	    populate();
	}	
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		drawMyLocation(canvas, mapView, lastKnownLocation, lastKnownPoint);
	}
	
	
	public void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLoc) {
		accuracyPaint = new Paint();
	    accuracyPaint.setAntiAlias(true);
	    accuracyPaint.setStrokeWidth(2.0f);
	    
	    drawable = centerDrawable;
	    width = drawable.getIntrinsicWidth();
	    height = drawable.getIntrinsicHeight();
	    center = new Point();
	    left = new Point();
	    
	    Projection projection = mapView.getProjection();
	    double latitude = lastFix.getLatitude();
	    double longitude = lastFix.getLongitude();
	    float accuracy = lastFix.getAccuracy();

	    float[] result = new float[1];

	    Location.distanceBetween(latitude, longitude, latitude, longitude + 1, result);
	    float longitudeLineDistance = result[0];

	    GeoPoint leftGeo = new GeoPoint((int) (latitude * 1e6), (int) ((longitude - accuracy
	            / longitudeLineDistance) * 1e6));
	    projection.toPixels(leftGeo, left);
	    projection.toPixels(myLoc, center);
	    int radius = center.x - left.x;

	    accuracyPaint.setColor(0xff6666ff);
	    accuracyPaint.setStyle(Style.STROKE);
	    canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

	    accuracyPaint.setColor(0x186666ff);
	    accuracyPaint.setStyle(Style.FILL);
	    canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

	    drawable.setBounds(center.x - width / 2, center.y - height / 2, center.x + width / 2,
	            center.y + height / 2);
	    drawable.draw(canvas);
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}
	
	private OverlayItem createCenterOverlay(GeoPoint point) {
	    OverlayItem i = new OverlayItem(point, "Location", null);
	    i.setMarker(centerDrawable);
	    return i;
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	private GeoPoint createGeoPoint(Location loc) {
	    int lat = (int) (loc.getLatitude() * 1E6);
	    int lng = (int) (loc.getLongitude() * 1E6);
	    return new GeoPoint(lat, lng);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		checkFirstRunnable();
		this.lastKnownLocation = location;
		this.lastKnownPoint = createGeoPoint(location);
		replaceOverlay(createCenterOverlay(lastKnownPoint));		
	}
	
	public boolean runOnFirstFix(Runnable runnable) {

	    if (lastKnownLocation != null) {
	        runnable.run();
	        return true;
	    }

	    firstFixRunnable = runnable;
	    return false;
	}
}
