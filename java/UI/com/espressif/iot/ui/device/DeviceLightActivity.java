package com.espressif.iot.ui.device;

import com.espressif.iot.R;
import com.espressif.iot.action.device.longsocket.EspDeviceLongSocketLight;
import com.espressif.iot.action.device.longsocket.IEspDeviceLongSocketLight;
import com.espressif.iot.base.net.longsocket.IEspLongSocket.EspLongSocketDisconnected;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.ui.view.EspColorPicker;
import com.espressif.iot.ui.view.EspColorPicker.OnColorChangedListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class DeviceLightActivity extends DeviceActivityAbs implements OnClickListener, OnSeekBarChangeListener,
     OnColorChangedListener, OnFocusChangeListener, EspLongSocketDisconnected
{
    // range of period is 1000~10000
    private static final int PERIOD_MIN = IEspDeviceLight.PERIOD_MIN;
    private static final int PERIOD_MAX = IEspDeviceLight.PERIOD_MAX;
    
    private int mRGBMax = IEspDeviceLight.RGB_MAX;
    
    private SeekBar mLightPeriodBar;
    private SeekBar mLightCWhiteBar;
    private SeekBar mLightWWhiteBar;
    private SeekBar mLightRedBar;
    private SeekBar mLightGreenBar;
    private SeekBar mLightBlueBar;
    private View mSeekBarContainer;
    
    private TextView mCWhiteText;
    private TextView mWWhiteText;
    private TextView mRedText;
    private TextView mGreenText;
    private TextView mBlueText;
    private TextView mPeriodText;
    
    private ImageView mColorPickerSwap;
    private EspColorPicker mColorPicker;
    
    private View mColorDisplay;
    
    private Button mConfirmBtn;
    private CheckBox mControlChildCB;
    private CheckBox mSwitch;
    
    private IEspDeviceLight mDeviceLight;
    
    protected View mLightLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mDeviceLight = (IEspDeviceLight)mIEspDevice;
        
        boolean compatibility = isDeviceCompatibility();
        checkHelpModeLight(compatibility);
        if (compatibility)
        {
            executeGet();
        }
    }
    
    @Override
    protected View initControlView()
    {
        View view = getLayoutInflater().inflate(R.layout.device_activity_light, null);
        mColorDisplay = view.findViewById(R.id.light_color_display);
        
        mLightPeriodBar = (SeekBar)view.findViewById(R.id.light_period_bar);
        mLightPeriodBar.setMax(PERIOD_MAX - PERIOD_MIN);
        mLightPeriodBar.setOnSeekBarChangeListener(this);
        
        mLightRedBar = (SeekBar)view.findViewById(R.id.light_red_bar);
        mLightRedBar.setMax(mRGBMax);
        mLightRedBar.setOnSeekBarChangeListener(this);
        
        mLightGreenBar = (SeekBar)view.findViewById(R.id.light_green_bar);
        mLightGreenBar.setMax(mRGBMax);
        mLightGreenBar.setOnSeekBarChangeListener(this);
        
        mLightBlueBar = (SeekBar)view.findViewById(R.id.light_blue_bar);
        mLightBlueBar.setMax(mRGBMax);
        mLightBlueBar.setOnSeekBarChangeListener(this);
        
        mLightCWhiteBar = (SeekBar)view.findViewById(R.id.light_cwhite_bar);
        mLightCWhiteBar.setMax(mRGBMax);
        mLightCWhiteBar.setOnSeekBarChangeListener(this);
        
        mLightWWhiteBar = (SeekBar)view.findViewById(R.id.light_wwhite_bar);
        mLightWWhiteBar.setMax(mRGBMax);
        mLightWWhiteBar.setOnSeekBarChangeListener(this);
        
        mCWhiteText = (TextView)view.findViewById(R.id.light_cwhite_text);
        mCWhiteText.setText("0");
        mCWhiteText.setOnFocusChangeListener(this);
        mWWhiteText = (TextView)view.findViewById(R.id.light_wwhite_text);
        mWWhiteText.setText("0");
        mWWhiteText.setOnFocusChangeListener(this);
        mRedText = (TextView)view.findViewById(R.id.light_red_text);
        mRedText.setText("0");
        mRedText.setOnFocusChangeListener(this);
        mGreenText = (TextView)view.findViewById(R.id.light_green_text);
        mGreenText.setText("0");
        mGreenText.setOnFocusChangeListener(this);
        mBlueText = (TextView)view.findViewById(R.id.light_blue_text);
        mBlueText.setText("0");
        mBlueText.setOnFocusChangeListener(this);
        mPeriodText = (TextView)view.findViewById(R.id.light_period_text);
        mPeriodText.setText(PERIOD_MIN + "");
        mPeriodText.setOnFocusChangeListener(this);
        
        if (!mIEspDevice.getDeviceState().isStateLocal() && mIEspDevice.getDeviceState().isStateInternet())
        {
            View wwContainer = view.findViewById(R.id.light_wwhite_container);
            wwContainer.setVisibility(View.GONE);
            TextView cwTitle = (TextView)view.findViewById(R.id.light_cwhite_title);
            cwTitle.setText(R.string.esp_device_light_white);
        }
        
        mConfirmBtn = (Button)view.findViewById(R.id.light_confirm_btn);
        mConfirmBtn.setOnClickListener(this);
        mControlChildCB = (CheckBox)view.findViewById(R.id.control_child_cb);
        mControlChildCB.setVisibility(mIEspDevice.getIsMeshDevice() ? View.VISIBLE : View.GONE);
        mSwitch = (CheckBox)view.findViewById(R.id.light_switch);
        mSwitch.setOnClickListener(this);
        
        mLightLayout = view.findViewById(R.id.light_layout);
        
        mColorPickerSwap = (ImageView)view.findViewById(R.id.light_colorpicker_swap);
        mColorPickerSwap.setOnClickListener(this);
        mSeekBarContainer = view.findViewById(R.id.light_seekbar_container);
        mColorPicker = (EspColorPicker)view.findViewById(R.id.light_color_picker);
        mColorPicker.setOnColorChangeListener(this);
        
        return view;
    }
    
    private void clearEditFocus()
    {
        if (mPeriodText.hasFocus())
        {
            mPeriodText.clearFocus();
        }
        if (mRedText.hasFocus())
        {
            mRedText.clearFocus();
        }
        if (mGreenText.hasFocus())
        {
            mGreenText.clearFocus();
        }
        if (mBlueText.hasFocus())
        {
            mBlueText.clearFocus();
        }
        if (mCWhiteText.hasFocus())
        {
            mCWhiteText.clearFocus();
        }
        if (mWWhiteText.hasFocus())
        {
            mWWhiteText.clearFocus();
        }
    }
    
    @Override
    public void onClick(View view)
    {
        clearEditFocus();
        
        if (view == mConfirmBtn)
        {
            IEspStatusLight status = new EspStatusLight();
            status.setPeriod(getProgressLightPeriod());
            status.setRed(mLightRedBar.getProgress());
            status.setGreen(mLightGreenBar.getProgress());
            status.setBlue(mLightBlueBar.getProgress());
            status.setCWhite(mLightCWhiteBar.getProgress());
            status.setWWhite(mLightWWhiteBar.getProgress());
            
            executeStatus(status);
        }
        else if (view == mSwitch)
        {
            int seekValue = mSwitch.isChecked() ? mRGBMax : 0;
            
            mLightRedBar.setProgress(seekValue);
            mLightGreenBar.setProgress(seekValue);
            mLightBlueBar.setProgress(seekValue);
            
            IEspStatusLight status = new EspStatusLight();
            status.setPeriod(getProgressLightPeriod());
            status.setRed(seekValue);
            status.setGreen(seekValue);
            status.setBlue(seekValue);
            status.setCWhite(mLightCWhiteBar.getProgress());
            status.setWWhite(mLightWWhiteBar.getProgress());
            
            executeStatus(status);
        }
        else if (view == mColorPickerSwap)
        {
            swapColorSelectView();
        }
    }
    
    @Override
    public void onColorChangeStart(View v, int color)
    {
        new Thread()
        {
            public void run()
            {
                IEspDeviceLongSocketLight lightPlayer = EspDeviceLongSocketLight.getInstance();
                if (mDeviceLight.getDeviceState().isStateLocal())
                {
                    lightPlayer.connectLightLocal(mDeviceLight.getInetAddress(), DeviceLightActivity.this);
                }
                else if (mDeviceLight.getDeviceState().isStateInternet())
                {
                    lightPlayer.connectLightInternet(mDeviceLight.getKey(), DeviceLightActivity.this);
                }
            }
        }.start();
        
        mColorDisplay.setBackgroundColor(color);
        
        mLightRedBar.setProgress(parseRGBtoLightValue(Color.red(color)));
        mLightGreenBar.setProgress(parseRGBtoLightValue(Color.green(color)));
        mLightBlueBar.setProgress(parseRGBtoLightValue(Color.blue(color)));
        
        if (mDeviceLight.getDeviceType() == EspDeviceType.LIGHT)
        {
            IEspStatusLight statusLight = new EspStatusLight();
            statusLight.setPeriod(getProgressLightPeriod());
            statusLight.setRed(parseRGBtoLightValue(Color.red(color)));
            statusLight.setGreen(parseRGBtoLightValue(Color.green(color)));
            statusLight.setBlue(parseRGBtoLightValue(Color.blue(color)));
            statusLight.setCWhite(mLightCWhiteBar.getProgress());
            statusLight.setWWhite(mLightWWhiteBar.getProgress());
            
            IEspDeviceLongSocketLight lightPlayer = EspDeviceLongSocketLight.getInstance();
            if (mDeviceLight.getDeviceState().isStateLocal())
            {
                lightPlayer.addLigthtStatusLocal(statusLight);
            }
            else if (mDeviceLight.getDeviceState().isStateInternet())
            {
                lightPlayer.addLigthStatusInternet(statusLight);
            }
        }
    }

    @Override
    public void onColorChanged(View v, int color)
    {
        mColorDisplay.setBackgroundColor(color);
        
        mLightRedBar.setProgress(parseRGBtoLightValue(Color.red(color)));
        mLightGreenBar.setProgress(parseRGBtoLightValue(Color.green(color)));
        mLightBlueBar.setProgress(parseRGBtoLightValue(Color.blue(color)));
        
        IEspStatusLight statusLight = new EspStatusLight();
        statusLight.setPeriod(getProgressLightPeriod());
        statusLight.setRed(parseRGBtoLightValue(Color.red(color)));
        statusLight.setGreen(parseRGBtoLightValue(Color.green(color)));
        statusLight.setBlue(parseRGBtoLightValue(Color.blue(color)));
        statusLight.setCWhite(mLightCWhiteBar.getProgress());
        statusLight.setWWhite(mLightWWhiteBar.getProgress());
        
        IEspDeviceLongSocketLight lightPlayer = EspDeviceLongSocketLight.getInstance();
        if (mDeviceLight.getDeviceState().isStateLocal())
        {
            lightPlayer.addLigthtStatusLocal(statusLight);
        }
        else if (mDeviceLight.getDeviceState().isStateInternet())
        {
            lightPlayer.addLigthStatusInternet(statusLight);
        }
    }
    
    @Override
    public void onColorChangeEnd(View v, int color)
    {
        EspDeviceLongSocketLight.getInstance().finish();
    }
    
    @Override
    public void onEspLongSocketDisconnected()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                new AlertDialog.Builder(DeviceLightActivity.this).setTitle(R.string.esp_device_light_disconnect_msg)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            DeviceLightActivity.this.finish();
                        }
                    })
                    .show();
            }
        });
    }
    
    private void swapColorSelectView()
    {
        int colorPickerVisibility = mSeekBarContainer.getVisibility();
        mSeekBarContainer.setVisibility(colorPickerVisibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        mColorPicker.setVisibility(colorPickerVisibility);
    }
    
    private void executeStatus(IEspStatusLight status)
    {
        if (mIEspDevice.getIsMeshDevice() && mControlChildCB.isChecked())
        {
            executePost(status, true);
        }
        else
        {
            executePost(status);
        }
    }
    
    @Override
    protected void executePrepare()
    {
    }
    
    @Override
    protected void executeFinish(int command, boolean result)
    {
        IEspStatusLight status = mDeviceLight.getStatusLight();
        int period = status.getPeriod();
        int red = status.getRed();
        int green = status.getGreen();
        int blue = status.getBlue();
        int cwhite = status.getCWhite();
        int wwhite = status.getWWhite();
        
        setProgressLightPeriod(period);
        mRGBMax = calRGBMax(getProgressLightPeriod());
        setRGBSeekbarMax(mRGBMax);
        
        mLightRedBar.setProgress(red);
        mLightGreenBar.setProgress(green);
        mLightBlueBar.setProgress(blue);
        mLightCWhiteBar.setProgress(cwhite);
        mLightWWhiteBar.setProgress(wwhite);
        
        checkHelpExecuteFinish(command, result);
    }
    
    private void setRGBSeekbarMax(int max)
    {
        mLightRedBar.setMax(max);
        mLightGreenBar.setMax(max);
        mLightBlueBar.setMax(max);
        mLightCWhiteBar.setMax(max);
        mLightWWhiteBar.setMax(max);
    }
    
    private int calRGBMax(int period)
    {
        return period*1000/45;
    }
    
    private int getProgressLightPeriod()
    {
        int period = mLightPeriodBar.getProgress();
        period += PERIOD_MIN;
        return period;
    }
    
    private void setProgressLightPeriod(int period)
    {
        period -= PERIOD_MIN;
        period = period > 0 ? period : 0;
        mLightPeriodBar.setProgress(period);
    }
    
    private int parseLightValuetoRGB(int value)
    {
        final int rgbMax = 255;
        int lightMax = mRGBMax;
        if (lightMax > rgbMax)
        {
            return rgbMax * value / mRGBMax;
        }
        else
        {
            return value;
        }
    }
    
    private int parseRGBtoLightValue(int rgb)
    {
        final int rgbMax = 255;
        final int lightMax = mRGBMax;
        
        return (rgb * lightMax) / rgbMax;
    }
    
    @Override
    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser)
    {
        int cwhite = mLightCWhiteBar.getProgress();
        mCWhiteText.setText("" + cwhite);
        int wwhite = mLightWWhiteBar.getProgress();
        mWWhiteText.setText("" + wwhite);
        int red = mLightRedBar.getProgress();
        mRedText.setText("" + red);
        int green = mLightGreenBar.getProgress();
        mGreenText.setText("" + green);
        int blue = mLightBlueBar.getProgress();
        mBlueText.setText("" + blue);
        int period = getProgressLightPeriod();
        mPeriodText.setText("" + period);
        
        if (mSeekBarContainer.getVisibility() == View.VISIBLE)
        {
            int color = Color.rgb(parseLightValuetoRGB(red), parseLightValuetoRGB(green), parseLightValuetoRGB(blue));
            mColorDisplay.setBackgroundColor(color);
        }
    }
    
    @Override
    public void onStartTrackingTouch(SeekBar bar)
    {
    }
    
    @Override
    public void onStopTrackingTouch(SeekBar bar)
    {
        if (bar == mLightPeriodBar)
        {
            mRGBMax = calRGBMax(getProgressLightPeriod());
            setRGBSeekbarMax(mRGBMax);
        }
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (v == mPeriodText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mPeriodText, mLightPeriodBar);
                
                mRGBMax = calRGBMax(getProgressLightPeriod());
                setRGBSeekbarMax(mRGBMax);
            }
        }
        else if (v == mRedText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mRedText, mLightRedBar);
            }
        }
        else if (v == mGreenText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mGreenText, mLightGreenBar);
            }
        }
        else if (v == mBlueText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mBlueText, mLightBlueBar);
            }
        }
        else if (v == mCWhiteText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mCWhiteText, mLightCWhiteBar);
            }
        }
        else if (v == mWWhiteText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mWWhiteText, mLightWWhiteBar);
            }
        }
    }
    
    private void onRGBFEdited(TextView textview, SeekBar seekbar)
    {
        CharSequence text = textview.getText();
        if (TextUtils.isEmpty(text))
        {
            return;
        }
        
        int value = Integer.parseInt(text.toString());
        if (seekbar == mLightPeriodBar)
        {
            value -= PERIOD_MIN;
        }
        if (value > seekbar.getMax())
        {
            value = seekbar.getMax();
        }
        seekbar.setProgress(value);
    }
    
    protected void checkHelpModeLight(boolean compatibility)
    {
    }
    
    protected void checkHelpExecuteFinish(int command, boolean result)
    {
    }
}
