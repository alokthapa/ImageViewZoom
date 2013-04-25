package it.sephiroth.android.library.imagezoom.graphics;

import java.io.InputStream;
import java.util.ArrayList;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

/**
 * Fast bitmap drawable. Does not support states. it only
 * support alpha and colormatrix
 * @author alessandro
 *
 */

public class FastBitmapDrawable extends Drawable implements IBitmapDrawable {

	protected Bitmap mBitmap;
	protected Paint mPaint;
	protected ArrayList<PointF> points;
	protected Bitmap tag;
	
	
	private float xoffset;
	private float yoffset;

	public FastBitmapDrawable( Bitmap b, Bitmap tag ,  ArrayList<PointF> points ) {
		mBitmap = b;
		mPaint = new Paint();
		mPaint.setDither( true );
		mPaint.setFilterBitmap( true );
		this.points = points;
		this.tag = tag;
		
		
		//TODO find a way to program this
		xoffset = 10;
		yoffset = -40;
	}
	
	public FastBitmapDrawable( Resources res, InputStream is ){
		this(BitmapFactory.decodeStream(is),null,null );
	}

	@Override
	public void draw( Canvas canvas ) {
		
		int width = mBitmap.getWidth();
		int scaledWidth = mBitmap.getScaledWidth(canvas);
		
		int height = mBitmap.getHeight();
		int scaledHeight = mBitmap.getScaledHeight(canvas);
		
		float widthratio = width/scaledWidth;
		float heightratio = height/scaledHeight;
		

		canvas.drawBitmap( mBitmap, 0.0f, 0.0f, mPaint );
//		for(PointF p : points)
//		{
//			canvas.drawBitmap(tag, p.x +xoffset,p.y-yoffset,mPaint);
//		}
		
	}

	@Override 
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha( int alpha ) {
		mPaint.setAlpha( alpha );
	}

	@Override
	public void setColorFilter( ColorFilter cf ) {
		mPaint.setColorFilter( cf );
	}

	@Override
	public int getIntrinsicWidth() {
		return mBitmap.getWidth();
	}

	@Override
	public int getIntrinsicHeight() {
		return mBitmap.getHeight();
	}

	@Override
	public int getMinimumWidth() {
		return mBitmap.getWidth();
	}

	@Override
	public int getMinimumHeight() {
		return mBitmap.getHeight();
	}
	
	public void setAntiAlias( boolean value ){
		mPaint.setAntiAlias( value );
		invalidateSelf();
	}

	@Override
	public Bitmap getBitmap() {
		return mBitmap;
	}
}