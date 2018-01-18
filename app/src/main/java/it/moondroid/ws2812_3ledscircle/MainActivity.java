package it.moondroid.ws2812_3ledscircle;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private WS2812B strip;
    private Timer timer;
    private long time = 0;
    private int hue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initStrip();
//        circleSingleColor(Color.valueOf(Color.rgb(0, 128,0)));
//        circleSingleColorWithFade(Color.valueOf(Color.GREEN));
        circleSingleColorWithFade(Color.valueOf(Color.HSVToColor(new float[]{
                0.0f, 1.0f, 0.1f
        })));
    }

    private void initStrip(){
        strip = new WS2812B();
        strip.add(0, Color.valueOf(Color.rgb(0, 0,0)));
        strip.add(1, Color.valueOf(Color.rgb(0, 0,0)));
        strip.add(2, Color.valueOf(Color.rgb(0, 0,0)));
        strip.commit();
    }

    private void circleSingleColor(Color color){

        strip.set(0, color);
        strip.commit();

        // run in loop
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // take last color and move it to first position
                strip.addFirst(strip.pollLast());
                strip.commit();
            }
        }, 1000, 1000);
    }

    private void circleSingleColorWithFade(final Color color){

        float[] hsv = new float[3];
        Color.RGBToHSV((int) (color.red() * 255), (int) (color.green() * 255), (int) (color.blue() *255), hsv);
        final float h = hsv[0];
        final float s = hsv[1];
        final float v = hsv[2];
        hue = (int) h;

        // run in loop
        timer = new Timer();
        final float FADE_TIME = 500; // milliseconds
        final float PERIOD = 4f * FADE_TIME;
        final float omega = (float) (2f * Math.PI / (PERIOD));

        final float angle0 = 0f;
        final float angle1 = (float) (Math.PI / 4f);
        final float angle2 = (float) (Math.PI / 2f);

        final int TIME_STEP = 10; // milliseconds

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                strip.set(0, Color.valueOf(Color.HSVToColor(new float[]{hue, s, (float) (v * Math.max(Math.cos(omega * time + angle0), 0))})));
                strip.set(1, Color.valueOf(Color.HSVToColor(new float[]{hue, s, (float) (v * Math.max(Math.cos(omega * time + angle1), 0))})));
                strip.set(2, Color.valueOf(Color.HSVToColor(new float[]{hue, s, (float) (v * Math.max(Math.cos(omega * time + angle2), 0))})));
                strip.commit();

                time += TIME_STEP;
                if (time == PERIOD){
                    time = 0;
                    hue += 20;
                    if (hue >= 360) hue = 0;
                }

            }
        }, 0, TIME_STEP);
    }

    private void showRandomColors(){
        final Random rnd = new Random();

        // generate random colors for some amount of LEDs.
        for(int i = 0; i < 300; i++) {
            // using HSV palette to generate bright colors.
            strip.add(0, Color.valueOf(Color.HSVToColor(new float[]{
                    rnd.nextFloat() * 360.0f, 1.0f, 0.1f
            })));
        }
        // run in loop
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // take last color and move it to first position
                strip.addFirst(strip.pollLast());
                strip.commit();
            }
        }, 500, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        strip.closeSPI();
    }
}
