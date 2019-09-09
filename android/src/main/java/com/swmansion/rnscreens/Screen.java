package com.swmansion.rnscreens;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.facebook.react.uimanager.PointerEvents;
import com.facebook.react.uimanager.ReactPointerEventsView;

public class Screen extends ViewGroup implements ReactPointerEventsView {

  public static class ScreenFragment extends Fragment {

    private Screen mScreenView;

    public ScreenFragment() {
      throw new IllegalStateException("Screen fragments should never be restored");
    }

    @SuppressLint("ValidFragment")
    public ScreenFragment(Screen screenView) {
      super();
      mScreenView = screenView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
      return mScreenView;
    }
  }

  private static OnAttachStateChangeListener sShowSoftKeyboardOnAttach = new OnAttachStateChangeListener() {

    @Override
    public void onViewAttachedToWindow(View view) {
      InputMethodManager inputMethodManager =
              (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.showSoftInput(view, 0);
      view.removeOnAttachStateChangeListener(sShowSoftKeyboardOnAttach);
    }

    @Override
    public void onViewDetachedFromWindow(View view) {

    }
  };

  private final Fragment mFragment;
  private @Nullable ScreenContainer mContainer;
  private boolean mActive;
  private boolean mTransitioning;

  public Screen(Context context) {
    super(context);
    mFragment = new ScreenFragment(this);
  }

  @Override
  protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
    // no-op
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    clearDisappearingChildren();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    // This method implements a workaround for RN's autoFocus functionality. Because of the way
    // autoFocus is implemented it sometimes gets triggered before native text view is mounted. As
    // a result Android ignores calls for opening soft keyboard and here we trigger it manually
    // again after the screen is attached.
    View view = getFocusedChild();
    if (view != null) {
      while (view instanceof ViewGroup) {
        view = ((ViewGroup) view).getFocusedChild();
      }
      if (view instanceof TextView) {
        TextView textView = (TextView) view;
        if (textView.getShowSoftInputOnFocus()) {
          textView.addOnAttachStateChangeListener(sShowSoftKeyboardOnAttach);
        }
      }
    }
  }

  /**
   * While transitioning this property allows to optimize rendering behavior on Android and provide
   * a correct blending options for the animated screen. It is turned on automatically by the container
   * when transitioning is detected and turned off immediately after
   */
  public void setTransitioning(boolean transitioning) {
    if (mTransitioning == transitioning) {
      return;
    }
    mTransitioning = transitioning;
    super.setLayerType(transitioning ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE, null);
  }

  @Override
  public boolean hasOverlappingRendering() {
    return mTransitioning;
  }

  @Override
  public PointerEvents getPointerEvents() {
    return mTransitioning ? PointerEvents.NONE : PointerEvents.AUTO;
  }

  @Override
  public void setLayerType(int layerType, @Nullable Paint paint) {
    // ignore - layer type is controlled by `transitioning` prop
  }

  public void setNeedsOffscreenAlphaCompositing(boolean needsOffscreenAlphaCompositing) {
    // ignore - offscreen alpha is controlled by `transitioning` prop
  }

  protected void setContainer(@Nullable ScreenContainer mContainer) {
    this.mContainer = mContainer;
  }

  protected @Nullable ScreenContainer getContainer() {
    return mContainer;
  }

  protected Fragment getFragment() {
    return mFragment;
  }

  public void setActive(boolean active) {
    if (active == mActive) {
      return;
    }
    mActive = active;
    if (mContainer != null) {
      mContainer.notifyChildUpdate();
    }
  }

  public boolean isActive() {
    return mActive;
  }
}
