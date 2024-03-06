package adv.drw.layout.behavior;

/*/===========================================================
  by MehrabSp
//===========================================================*/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.util.HashMap;

/**
 * (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
 */
public class AdvanceDrawerLayout extends DrawerLayout {
    private final HashMap<Integer, Setting> settings = new HashMap<>();
    private int defaultScrimColor = -0x67000000;
    private float defaultDrawerElevation = 0f;
    private FrameLayout frameLayout;
    private View drawerView;
    private int statusBarColor = 0;
    private float contrastThreshold = 3f;
    private int cardBackgroundColor = 0;

    public AdvanceDrawerLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AdvanceDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AdvanceDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @SuppressLint("CustomViewStyleable")
    private void init(Context context, AttributeSet attrs, int defStyle) {
        defaultDrawerElevation = getDrawerElevation();
        boolean defaultFitsSystemWindows = getFitsSystemWindows();
        if (!isInEditMode()) {
            statusBarColor = getActivity().getWindow().getStatusBarColor();
        }
        addDrawerListener(new DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                AdvanceDrawerLayout.this.drawerView = drawerView;
                updateSlideOffset(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        frameLayout = new FrameLayout(context);
        frameLayout.setPadding(0, 0, 0, 0);
        super.addView(frameLayout);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        child.setLayoutParams(params);
        addView(child);
    }

    @Override
    public void addView(View child) {
        if (child instanceof NavigationView) {
            super.addView(child);
        } else {
            CardView cardView = new CardView(getContext());
            cardView.setRadius(0f);
            cardView.addView(child);
            cardView.setCardElevation(0f);
            cardView.setCardBackgroundColor(cardBackgroundColor);
            frameLayout.addView(cardView);
        }
    }

    public void setViewScale(int gravity, float percentage) {
        int absGravity = getDrawerViewAbsoluteGravity(gravity);
        Setting setting = settings.get(absGravity);
        if (setting == null) {
            setting = createSetting();
            settings.put(absGravity, setting);
        }
        if (percentage < 1) {
            setStatusBarBackground(null);
            setSystemUiVisibility(0);
        }
        if (setting != null) {
            setting.percentage = percentage;
            setting.scrimColor = Color.TRANSPARENT;
            setting.drawerElevation = 0f;
        }
    }

    public void setViewElevation(int gravity, float elevation) {
        int absGravity = getDrawerViewAbsoluteGravity(gravity);
        Setting setting = settings.get(absGravity);
        if (setting == null) {
            setting = createSetting();
            settings.put(absGravity, setting);
        }
        if (setting != null) {
            setting.scrimColor = Color.TRANSPARENT;
            setting.drawerElevation = 0f;
            setting.elevation = elevation;
        }
    }

    public void setViewScrimColor(int gravity, int scrimColor) {
        int absGravity = getDrawerViewAbsoluteGravity(gravity);
        Setting setting = settings.get(absGravity);
        if (setting == null) {
            setting = createSetting();
            settings.put(absGravity, setting);
        }
        if (setting != null) {
            setting.scrimColor = scrimColor;
        }
    }

    public void setCardBackgroundColor(int gravity, int color) {
        cardBackgroundColor = color;
        for (int i = 0; i < frameLayout.getChildCount(); i++) {
            View child = frameLayout.getChildAt(i);
            if (child instanceof CardView) {
                ((CardView) child).setCardBackgroundColor(cardBackgroundColor);
            }
        }
    }

    public void setRadius(int gravity, float radius) {
        int absGravity = getDrawerViewAbsoluteGravity(gravity);
        Setting setting = settings.get(absGravity);
        if (setting == null) {
            setting = createSetting();
            settings.put(absGravity, setting);
        }
        if (setting != null) {
            setting.radius = radius;
        }
    }

    public Setting getSetting(int gravity) {
        int absGravity = getDrawerViewAbsoluteGravity(gravity);
        return settings.get(absGravity);
    }

    @Override
    public void setDrawerElevation(float elevation) {
        defaultDrawerElevation = elevation;
        super.setDrawerElevation(elevation);
    }

    @Override
    public void setScrimColor(@ColorInt int color) {
        defaultScrimColor = color;
        super.setScrimColor(color);
    }

    public void useCustomBehavior(int gravity) {
        int absGravity = getDrawerViewAbsoluteGravity(gravity);
        if (!settings.containsKey(absGravity)) {
            Setting setting = createSetting();
            settings.put(absGravity, setting);
        }
    }

    public void removeCustomBehavior(int gravity) {
        int absGravity = getDrawerViewAbsoluteGravity(gravity);
        settings.remove(absGravity);
    }

    @Override
    public void openDrawer(@NonNull View drawerView, boolean animate) {
        super.openDrawer(drawerView, animate);
        post(() -> updateSlideOffset(drawerView, isDrawerOpen(drawerView) ? 1f : 0f));
    }

    private void updateSlideOffset(View drawerView, float slideOffset) {
        int absHorizGravity = getDrawerViewAbsoluteGravity(Gravity.START);
        int childAbsGravity = getDrawerViewAbsoluteGravity(drawerView);
        Activity activity = getActivity();
        boolean isRtl = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isRtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        }
        for (int i = 0; i < frameLayout.getChildCount(); i++) {
            View child = frameLayout.getChildAt(i);
            if (child instanceof CardView) {
                CardView cardView = (CardView) child;
                Setting setting = settings.get(childAbsGravity);
                if (setting != null) {
                    if (setting.percentage < 1.0) {
                        if (drawerView.getBackground() instanceof ColorDrawable) {
                            int color = ColorUtils.setAlphaComponent(statusBarColor, (int) (255 - 255 * slideOffset));
                            activity.getWindow().setStatusBarColor(color);
                            int bgColor = ((ColorDrawable) drawerView.getBackground()).getColor();
                            activity.getWindow().getDecorView().setBackgroundColor(bgColor);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                int systemUiVisibility = ColorUtils.calculateContrast(Color.WHITE, bgColor) < contrastThreshold && slideOffset > 0.4 ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
                                setSystemUiVisibility(systemUiVisibility);
                            }
                        } else if (drawerView.getBackground() instanceof MaterialShapeDrawable && ((MaterialShapeDrawable) drawerView.getBackground()).getFillColor() != null) {
                            int color = ColorUtils.setAlphaComponent(statusBarColor, (int) (255 - 255 * slideOffset));
                            activity.getWindow().setStatusBarColor(color);
                            int bgColor = ((MaterialShapeDrawable) drawerView.getBackground()).getFillColor().getDefaultColor();
                            activity.getWindow().getDecorView().setBackgroundColor(bgColor);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                int systemUiVisibility = ColorUtils.calculateContrast(Color.WHITE, bgColor) < contrastThreshold && slideOffset > 0.4 ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
                                setSystemUiVisibility(systemUiVisibility);
                            }
                        }
                    }
                    cardView.setRadius((float) Math.round((setting.radius * slideOffset) * 2) / 2);
                    super.setScrimColor(setting.scrimColor);
                    super.setDrawerElevation(setting.drawerElevation);
                    float percentage = 1f - setting.percentage;
                    ViewCompat.setScaleY(cardView, 1f - percentage * slideOffset);
                    cardView.setCardElevation(setting.elevation * slideOffset);
                    float adjust = setting.elevation;
                    boolean isLeftDrawer = isRtl == (childAbsGravity != absHorizGravity);
                    float width = isLeftDrawer ? drawerView.getWidth() + adjust : -drawerView.getWidth() - adjust;
                    updateSlideOffset(cardView, setting, width, slideOffset, isLeftDrawer);
                } else {
                    super.setScrimColor(defaultScrimColor);
                    super.setDrawerElevation(defaultDrawerElevation);
                }
            }
        }
    }

    public void setContrastThreshold(float contrastThreshold) {
        this.contrastThreshold = contrastThreshold;
    }

    public Activity getActivity() {
        return getActivity(getContext());
    }

    public Activity getActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) return (Activity) context;
        return (context instanceof ContextWrapper) ? getActivity(((ContextWrapper) context).getBaseContext()) : null;
    }

    public void updateSlideOffset(CardView child, Setting setting, float width, float slideOffset, boolean isLeftDrawer) {
        child.setX(width * slideOffset);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerView != null) updateSlideOffset(drawerView, isDrawerOpen(drawerView) ? 1f : 0f);
    }

    public int getDrawerViewAbsoluteGravity(int gravity) {
        return Gravity.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this)) & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
    }

    public int getDrawerViewAbsoluteGravity(View drawerView) {
        int gravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
        return getDrawerViewAbsoluteGravity(gravity);
    }

    public Setting createSetting() {
        return new Setting();
    }

    public class Setting {
        public boolean fitsSystemWindows = false;
        public float percentage = 1f;
        public int scrimColor = defaultScrimColor;
        public float elevation = 0f;
        public float drawerElevation = defaultDrawerElevation;
        public float radius = 0f;
    }

    private static final String TAG = AdvanceDrawerLayout.class.getSimpleName();
}
/*/===========================================================
  by MehrabSp
//===========================================================*/