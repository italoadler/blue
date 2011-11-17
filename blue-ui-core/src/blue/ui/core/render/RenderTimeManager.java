/*
 * blue - object composition environment for csound
 * Copyright (c) 2000-2006 Steven Yi (stevenyi@gmail.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */
package blue.ui.core.render;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import blue.noteProcessor.TempoMapper;
import blue.settings.PlaybackSettings;
import blue.soundObject.PolyObject;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.core.animation.timing.TimingSource.TickListener;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;

/**
 * 
 * @author Steven Yi
 */
public class RenderTimeManager {

    public static String RENDER_START = "renderStart";
    public static String TIME_POINTER = "timePointer";
    private static RenderTimeManager renderManager;
    private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private ArrayList<RenderTimeManagerListener> renderListeners = new ArrayList<RenderTimeManagerListener>();
    private float renderStart = -1.0f;
    private float timePointer = -1.0f;
    private int timeAdjustCounter = 0;
    private float timeAdjust = Float.NEGATIVE_INFINITY;
    private TempoMapper tempoMapper;
    private PolyObject polyObject;
    TimingSource ts = null;

    private RenderTimeManager() {
    }

    public static RenderTimeManager getInstance() {
        if (renderManager == null) {
            renderManager = new RenderTimeManager();
        }

        return renderManager;
    }

    public void initiateRender(float renderStart) {
        this.setRenderStart(renderStart);

        for (RenderTimeManagerListener listener : renderListeners) {
            listener.renderInitiated();
        }

        if (ts != null) {
            ts.dispose();
            ts = null;
        }

        int fps = PlaybackSettings.getInstance().getPlaybackFPS();
        
        ts = new SwingTimerTimingSource((int)(1000.0f / fps), TimeUnit.MILLISECONDS);
        final long initialTime = System.nanoTime();
        ts.addTickListener(new TickListener() {

            @Override
            public void timingSourceTick(TimingSource source, long nanoTime) {
                float elapsedTime = (nanoTime - initialTime) / 1000000000.0f;
                setTimePointer(elapsedTime);

                if (timeAdjust != Float.NEGATIVE_INFINITY) {
                    float adjustedTime = elapsedTime - timeAdjust;
                    for (RenderTimeManagerListener listener : renderListeners) {
                        listener.renderTimeUpdated(adjustedTime);
                    }
                }
            }
        });
        ts.init();
    }

    public void updateTimePointer(float timePointer) {
        if (timeAdjustCounter == 0) {
            timeAdjust = this.timePointer - timePointer;
            timeAdjustCounter++;

            if (timeAdjustCounter == 10) {
                timeAdjustCounter = 0;
            }
        }
    }

    public void endRender() {
        this.setRenderStart(-1.0f);
        this.setTimePointer(-1.0f);
        timeAdjust = Float.NEGATIVE_INFINITY;
        this.polyObject = null;
        for (RenderTimeManagerListener listener : renderListeners) {
            listener.renderEnded();
        }
        if (ts != null) {
            ts.dispose();
            ts = null;
        }
        timeAdjustCounter = 0;
    }

    private void setRenderStart(float renderStart) {
        float oldVal = this.renderStart;
        this.renderStart = renderStart;

        listeners.firePropertyChange(RENDER_START, new Float(oldVal),
                new Float(renderStart));
    }

    private void setTimePointer(float timePointer) {
        float oldTime = this.timePointer;

        float newVal = timePointer;

        if (this.tempoMapper != null) {
            float renderStartSeconds = tempoMapper.beatsToSeconds(renderStart);
            newVal = tempoMapper.secondsToBeats(newVal + renderStartSeconds);
            newVal -= renderStart;
        }

        this.timePointer = newVal;

//        listeners.firePropertyChange(TIME_POINTER, new Float(oldTime),
//                new Float(this.timePointer));
    }

    public float getRenderStartTime() {
        return renderStart;
    }

    public float getRenderTime() {
        return timePointer - timeAdjust;
    }

    // Property Change Methods
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        listeners.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        listeners.removePropertyChangeListener(pcl);
    }

    public void setTempoMapper(TempoMapper tempoMapper) {
        this.tempoMapper = tempoMapper;
    }

    public TempoMapper getTempoMapper() {
        return this.tempoMapper;
    }

    public void setRootPolyObject(PolyObject polyObject) {
        this.polyObject = polyObject;
    }

    public PolyObject getRootPolyObject() {
        return this.polyObject;
    }

    public void addRenderTimeManagerListener(RenderTimeManagerListener listener) {
        renderListeners.add(listener);
    }

    public void removeRenderTimeManagerListener(RenderTimeManagerListener listener) {
        renderListeners.remove(listener);
    }
}
