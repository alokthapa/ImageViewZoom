package it.sephiroth.android.library.imagezoom;



import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewConfiguration;

public class ImageViewTouch extends ImageViewTouchBase {

	static final float SCROLL_DELTA_THRESHOLD = 1.0f;
	protected ScaleGestureDetector mScaleDetector;
	protected GestureDetector mGestureDetector;
	protected int mTouchSlop;
	protected float mScaleFactor;
	protected int mDoubleTapDirection;
	protected OnGestureListener mGestureListener;
	protected OnScaleGestureListener mScaleListener;
	protected boolean mDoubleTapEnabled = true;
	protected boolean mScaleEnabled = true;
	protected boolean mScrollEnabled = true;
	@SuppressWarnings("unused")
	private OnImageViewTouchDoubleTapListener mDoubleTapListener;
	private OnImageViewTouchSingleTapListener mSingleTapListener;
	private OnImageViewTouchScrollListener mScrollListener;
	private OnImageViewTouchScaleListener mImageScaleListener;

	public ImageViewTouch ( Context context, AttributeSet attrs ) {
		super( context, attrs );
	}
	


	@Override
	protected void init() {
		super.init();
		mTouchSlop = ViewConfiguration.get( getContext() ).getScaledTouchSlop();
		mGestureListener = getGestureListener();
		mScaleListener = getScaleListener();

		mScaleDetector = new ScaleGestureDetector( getContext(), mScaleListener );
		mGestureDetector = new GestureDetector( getContext(), mGestureListener, null, true );

		mDoubleTapDirection = 1;
	}

	public void setDoubleTapListener( OnImageViewTouchDoubleTapListener listener ) {
		mDoubleTapListener = listener;
	}

	public void setSingleTapListener( OnImageViewTouchSingleTapListener listener ) {
		mSingleTapListener = listener;
	}

	public void setScrollListener( OnImageViewTouchScrollListener listener ) {
		this.mScrollListener = listener;
	}

	public void setScaleListener( OnImageViewTouchScaleListener listener ) {
		this.mImageScaleListener = listener;
	}
	
	
	public void setDoubleTapEnabled( boolean value ) {
		mDoubleTapEnabled = value;
	}

	public void setScaleEnabled( boolean value ) {
		mScaleEnabled = value;
	}

	public void setScrollEnabled( boolean value ) {
		mScrollEnabled = value;
	}

	public boolean getDoubleTapEnabled() {
		return mDoubleTapEnabled;
	}

	protected OnGestureListener getGestureListener() {
		return new GestureListener();
	}

	protected OnScaleGestureListener getScaleListener() {
		return new ScaleListener();
	}
	
	
	
	@Override
	protected void _setImageDrawable( final Drawable drawable, final Matrix initial_matrix, float min_zoom, float max_zoom ) {
		super._setImageDrawable( drawable, initial_matrix, min_zoom, max_zoom );
		mScaleFactor = getMaxScale() / 3;
	}

	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		mScaleDetector.onTouchEvent( event );

		if ( !mScaleDetector.isInProgress() ) {
			mGestureDetector.onTouchEvent( event );
		}

		int action = event.getAction();
		switch ( action & MotionEvent.ACTION_MASK ) {
			case MotionEvent.ACTION_UP:
				if ( getScale() < getMinScale() ) {
					zoomTo( getMinScale(), 50 );
				}
				break;
		}
		return true;
	}


	@Override
	protected void onZoomAnimationCompleted( float scale ) {

		if( LOG_ENABLED ) {
			Log.d( LOG_TAG, "onZoomAnimationCompleted. scale: " + scale + ", minZoom: " + getMinScale() );
		}

		if ( scale < getMinScale() ) {
			zoomTo( getMinScale(), 50 );
		}
	}

	protected float onDoubleTapPost( float scale, float maxZoom ) {
		if ( mDoubleTapDirection == 1 ) {
			if ( ( scale + ( mScaleFactor * 2 ) ) <= maxZoom ) {
				return scale + mScaleFactor;
			} else {
				mDoubleTapDirection = -1;
				return maxZoom;
			}
		} else {
			mDoubleTapDirection = 1;
			return 1f;
		}
	}
	
	@Override
	protected Matrix postScale(float scale, float centerX, float centerY )
	{
		Matrix m = super.postScale(scale,centerX,centerY);
		
		
		if (mImageScaleListener != null)
		{
			mImageScaleListener.onScale(null,m,scale,  centerX, centerY);
		}
		return m;
	}

	public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
		if ( !mScrollEnabled ) return false;

		if ( e1 == null || e2 == null ) return false;
		if ( e1.getPointerCount() > 1 || e2.getPointerCount() > 1 ) return false;
		if ( mScaleDetector.isInProgress() ) return false;
		if ( getScale() == 1f ) return false;

		mUserScaled = true;
		RectF scrolledby = scrollBy( -distanceX, -distanceY );
		invalidate();
		
		if (null != this.mScrollListener)
		{
			this.mScrollListener.onScroll(e1, e2, currentscale, -scrolledby.left, -scrolledby.top);
		}

		return true;
	}

	public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {
		
		if ( !mScrollEnabled ) return false;

		if ( e1.getPointerCount() > 1 || e2.getPointerCount() > 1 ) return false;
		if ( mScaleDetector.isInProgress() ) return false;
		if ( getScale() == 1f ) return false;

		float diffX = e2.getX() - e1.getX();
		float diffY = e2.getY() - e1.getY();

		if ( Math.abs( velocityX ) > 800 || Math.abs( velocityY ) > 800 ) {
			mUserScaled = true;
			scrollBy( diffX / 2, diffY / 2, 300 );
			invalidate();
			return true;
		}
		return false;
	}

    public float[] getActualPoints (float [] values)

    {
        Matrix now = getImageMatrix();
        Matrix inv =new Matrix();
        now.invert(inv);

        inv.mapPoints(values);

        return values;
    }

    public float[] getScaledPoints(float []values)
    {
        Matrix now = getImageMatrix();

        now.mapPoints(values);

        return values;


    }

	private float currentscale = 1f;
	public class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapConfirmed( MotionEvent e ) {

			if ( null != mSingleTapListener ) {
                float [] pts = getActualPoints(new float[] {e.getX(),e.getY()});
				mSingleTapListener.onSingleTapConfirmed(e, currentscale, pts[0],pts[1]);
			}
			

			return super.onSingleTapConfirmed( e );
		}

//		@Override
//		public boolean onDoubleTap( MotionEvent e ) {
//			Log.i( LOG_TAG, "onDoubleTap. double tap enabled? " + mDoubleTapEnabled );
//			if ( mDoubleTapEnabled ) {
//				mUserScaled = true;
//				float scale = getScale();
//				float targetScale = scale;
//				targetScale = onDoubleTapPost( scale, getMaxScale() );
//				targetScale = Math.min( getMaxScale(), Math.max( targetScale, getMinScale() ) );
//				currentscale = targetScale;
//				zoomTo( targetScale, e.getX(), e.getY(), DEFAULT_ANIMATION_DURATION );
//				invalidate();
//			}
//			if ( null != mDoubleTapListener ) {
//				mDoubleTapListener.onDoubleTap(e, currentscale);
//			}
//
//			return super.onDoubleTap( e );
//		}

		@Override
		public void onLongPress( MotionEvent e ) {
			if ( isLongClickable() ) {
				if ( !mScaleDetector.isInProgress() ) {
					setPressed( true );
					performLongClick();
				}
			}
		}

		@Override
		public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
			return ImageViewTouch.this.onScroll( e1, e2, distanceX, distanceY );
		}

//		@Override
//		public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {
//			return ImageViewTouch.this.onFling( e1, e2, velocityX, velocityY );
//		}
	}

	
	
	public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		
		protected boolean mScaled = false;

		
		@Override
		public boolean onScale( ScaleGestureDetector detector ) {
			
			float span = detector.getCurrentSpan() - detector.getPreviousSpan();
			float targetScale = getScale() * detector.getScaleFactor();
			
			if ( mScaleEnabled ) {
				if( mScaled && span != 0 ) {
					mUserScaled = true;
					targetScale = Math.min( getMaxScale(), Math.max( targetScale, getMinScale() - 0.1f ) );
					currentscale = targetScale;
					zoomTo( targetScale, detector.getFocusX(), detector.getFocusY() );
					mDoubleTapDirection = 1;
					invalidate();
					
					
					return true;
				}
				
				// This is to prevent a glitch the first time 
				// image is scaled.
				if( !mScaled ) mScaled = true;
			}
			return true;
		}
	}

	public interface OnImageViewTouchDoubleTapListener {

		void onDoubleTap(MotionEvent e, float scale);
	}

	public interface OnImageViewTouchSingleTapListener {

		void onSingleTapConfirmed(MotionEvent e, float scale, float realX, float realY);
	}
	public interface OnImageViewTouchScrollListener {

		void onScroll(MotionEvent e1, MotionEvent e2, float scale, float distanceX, float distanceY);
	}
	public interface OnImageViewTouchScaleListener {

		void onScale(MotionEvent e,Matrix matrix,float scale,  float centerX, float centerY);
	}
}
