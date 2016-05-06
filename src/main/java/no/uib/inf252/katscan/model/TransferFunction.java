package no.uib.inf252.katscan.model;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.event.EventListenerList;
import no.uib.inf252.katscan.event.TransferFunctionListener;

/**
 *
 * @author Marcelo Lima
 */
public class TransferFunction implements KatModel {
    
    public enum Type {
        SLOPE("Slope", "Make Slope", 'S'),
        BODY("Body", "Make Body", 'B'),
        BRAIN("Brain", "Make Brain", 'R');

        private final String text;
        private final String makeText;
        private final char mnemonic;

        private Type(String text, String makeText, char mnemonic) {
            this.text = text;
            this.makeText = makeText;
            this.mnemonic = mnemonic;
        }

        public String getText() {
            return text;
        }

        public String getMakeText() {
            return makeText;
        }

        public char getMnemonic() {
            return mnemonic;
        }
    }

    public static final int TEXTURE_SIZE = 2048;
    public static final float MIN_STEP = 1f / TEXTURE_SIZE;

    private final ArrayList<TransferFunctionPoint> points;
    private final EventListenerList listenerList;

    transient private boolean dirtyPaint;
    private Color[] colorsLinear;
    private Color[] colorsQuadratic;
    private float[] colorPoints;
    
    public TransferFunction(Type type) {
        points = new ArrayList<>();
        listenerList = new EventListenerList();
        setType(type);
    }
    
    private TransferFunction(TransferFunction transferFunction) {
        points = new ArrayList<>();
        listenerList = new EventListenerList();
        
        for (TransferFunctionPoint point : transferFunction.points) {
            points.add(new TransferFunctionPoint(point, this));
        }
        dirtyPaint = true;
    }
    
    public TransferFunction copy() {
        return new TransferFunction(this);
    }
    
    public final void setType(Type type) {
        if (type == null) {
            throw new NullPointerException("No type was provided.");
        }
        points.clear();
        
        switch(type) {
            case SLOPE:
                points.add(new TransferFunctionPoint(new Color(0, 0, 0, 0), 0f, this, false));
                points.add(new TransferFunctionPoint(new Color(255, 255, 255, 255), 1f, this, false));
                break;
            case BODY:
                points.add(new TransferFunctionPoint(new Color(0, 0, 0, 0), 0f, this, false));
                points.add(new TransferFunctionPoint(new Color(255, 180, 170), MIN_STEP, this));
                points.add(new TransferFunctionPoint(new Color(255, 180, 170), 1010f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(150, 0, 50), 1024f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(255, 150, 150), 1037f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(165, 85, 110), 1050f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(100, 0, 0), 1110f / 4096f, this));                
                points.add(new TransferFunctionPoint(new Color(255, 220, 180), 1220f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(255, 0, 0), 1230f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(255, 0, 0), 1290f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(255, 220, 180), 1300f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(255, 255, 255), 2048f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(255, 255, 255), 1f - MIN_STEP, this));
                points.add(new TransferFunctionPoint(new Color(0, 0, 0, 0), 1f, this, false));
                break;
            case BRAIN:
                points.add(new TransferFunctionPoint(new Color(255, 255, 255, 0), 0f, this, false));
                points.add(new TransferFunctionPoint(new Color(255, 255, 255, 200), 1018f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(0, 150, 100), 1022f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(0, 150, 100), 1068f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(0, 150, 100, 0), 1072f / 4096f, this));
                points.add(new TransferFunctionPoint(new Color(255, 255, 255, 0), 1f, this, false));
                break;
        }
        dirtyPaint = true;
        firePointCountChanged();
    }

    public int getPointCount() {
        return points.size();
    }

    public boolean addPoint(Color color, float point) {
        TransferFunctionPoint newPoint = new TransferFunctionPoint(color, point, this);
        final boolean returnValue = points.add(newPoint);
        dirtyPaint = true;

        firePointCountChanged();
        return returnValue;
    }

    public TransferFunctionPoint getPoint(int index) {
        return points.get(index);
    }

    public boolean removePoint(TransferFunctionPoint point) {
        boolean removed = points.remove(point);
        if (removed) {
            dirtyPaint = true;
            firePointCountChanged();
        }
        return removed;
    }

    public TransferFunctionPoint removePoint(int index) {
        final TransferFunctionPoint point = points.remove(index);
        if (point != null) {
            dirtyPaint = true;
            firePointCountChanged();
        }
        return point;
    }

    private void valueChanged() {
        dirtyPaint = true;
        firePointValueChanged();
    }
    
    public LinearGradientPaint getPaint() {
        return getPaint(0f, TEXTURE_SIZE, false);
    }

    public LinearGradientPaint getPaint(float startX, float endX) {
        return getPaint(startX, endX, false);
    }
    
    public LinearGradientPaint getPaint(float startX, float endX, boolean quadratic) {
        if (dirtyPaint) {
            rebuildPaint();
        }

        return new LinearGradientPaint(startX, 0f, endX, 0f, colorPoints, quadratic ? colorsQuadratic : colorsLinear, MultipleGradientPaint.CycleMethod.NO_CYCLE);
    }

    private void rebuildPaint() {
        Collections.sort(points);

        colorsLinear = new Color[points.size()];
        colorsQuadratic = new Color[points.size()];
        colorPoints = new float[points.size()];
        int index = 0;
        int removedCount = 0;
        for (TransferFunctionPoint point : points) {
            float pointValue = point.getPoint();
            
            if (index > 0 && index < colorPoints.length - 1) {
                if (pointValue <= colorPoints[index - 1]) {
                    pointValue = colorPoints[index - 1] + MIN_STEP;
                    if (pointValue >= 1f) {
                        removedCount++;
                        continue;
                    }
                }
            }
            Color color = point.getColor();
            colorsLinear[index] = color;
            colorsQuadratic[index] = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) ((color.getAlpha() * color.getAlpha()) / 255d));
            colorPoints[index] = pointValue;
            index++;
        }

        if (removedCount > 0) {
            colorsLinear = Arrays.copyOf(colorsLinear, colorsLinear.length - removedCount);
            colorPoints = Arrays.copyOf(colorPoints, colorPoints.length - removedCount);
        }
    }

    private void firePointCountChanged() {
        TransferFunctionListener[] listeners = listenerList.getListeners(TransferFunctionListener.class);

        for (final TransferFunctionListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.pointCountChanged();
                }
            });
        }
    }

    private void firePointValueChanged() {
        TransferFunctionListener[] listeners = listenerList.getListeners(TransferFunctionListener.class);

        for (final TransferFunctionListener listener : listeners) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    listener.pointValueChanged();
                }
            });
        }
    }

    public synchronized void addTransferFunctionListener(TransferFunctionListener listener) {
        if (listener == null) {
            return;
        }

        listenerList.add(TransferFunctionListener.class, listener);
    }

    public synchronized void removeTransferFunctionListener(TransferFunctionListener listener) {
        if (listener == null) {
            return;
        }

        listenerList.remove(TransferFunctionListener.class, listener);
    }

    public class TransferFunctionPoint implements Comparable<TransferFunctionPoint>, Serializable {

        private Color color;
        private float point;
        private TransferFunction owner;
        private boolean movable;

        private TransferFunctionPoint(TransferFunctionPoint other, TransferFunction owner) {
            this.color = other.color;
            this.point = other.point;
            this.owner = owner;
            this.movable = other.movable;
        }
        
        private TransferFunctionPoint(Color color, float point, TransferFunction owner) {
            this(color, point, owner, true);
        }

        private TransferFunctionPoint(Color color, float point, TransferFunction owner, boolean movable) {
            if (owner == null) {
                throw new NullPointerException("Cannot have a " + getClass().getName() + " without owner.");
            }
            
            if (point < 0f || point > 1.0f) {
                throw new IndexOutOfBoundsException("Cannot create a transfer function point at "
                        + point);
            }
            this.color = color;
            this.point = point;
            this.owner = owner;
            this.movable = movable;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
            owner.valueChanged();
        }
        
        public double getAlpha() {
            return color.getAlpha() / 255d;
        }
        
        public void setAlpha(double alpha) {
            if (alpha < 0d) {
                alpha = 0d;
            } else if (alpha > 1d) {
                alpha = 1d;
            }
            
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255));
            owner.valueChanged();
        }
        
        public float getPoint() {
            return point;
        }

        public void setPoint(float point) {
            if (!movable) {
                return;
            }

            if (point < 0f || point > 1.0f) {
                throw new IndexOutOfBoundsException("Cannot create a transfer function point at "
                        + point);
            }
            this.point = point;
            owner.valueChanged();
        }

        public boolean isMovable() {
            return movable;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + Float.floatToIntBits(this.point);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TransferFunctionPoint other = (TransferFunctionPoint) obj;
            if (Float.floatToIntBits(this.point)
                    != Float.floatToIntBits(other.point)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(TransferFunctionPoint o) {
            return Double.compare(point, o.point);
        }

    }
}
