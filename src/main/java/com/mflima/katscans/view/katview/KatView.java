package com.mflima.katscans.view.katview;

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mflima.katscans.project.displayable.Displayable;
import com.mflima.katscans.view.katview.opengl.AbsorptionRenderer;
import com.mflima.katscans.view.katview.opengl.AlphaRenderer;
import com.mflima.katscans.view.katview.opengl.CompositeRenderer;
import com.mflima.katscans.view.katview.opengl.MaximumRenderer;
import com.mflima.katscans.view.katview.opengl.SliceNavigator;
import com.mflima.katscans.view.katview.opengl.SurfaceRenderer;

/**
 * @author Marcelo Lima
 */
public interface KatView {

  //TODO Centralize all enums
  //TODO Also remove popup creation from KatNode
  public enum Type {
    SURF("Surface Renderer", 'F', SurfaceRenderer.class),
    COMPOSITE("Composite Renderer", 'C', CompositeRenderer.class),
    ALPHA("Alpha Renderer", 'A', AlphaRenderer.class),
    ABSORPTION("Absorption Renderer", 'B', AbsorptionRenderer.class),
    MAXIMUM("Maximum Renderer", 'M', MaximumRenderer.class),
    SLICE("Slice Navigator", 'S', SliceNavigator.class),
    HISTOGRAM("Histogram", 'H', TransferFunctionEditor.class);

    private final String text;
    private final char mnemonic;
    private final Constructor<? extends Component> constructor;

    private Type(String text, char mnemonic, Class<? extends Component> clazz) {
      this.text = text;
      this.mnemonic = mnemonic;

      Constructor<? extends Component> newConstructor = null;
      try {
        newConstructor = clazz.getConstructor(Displayable.class);
      } catch (NoSuchMethodException | SecurityException ex) {
        Logger.getLogger(Type.class.getName()).log(Level.SEVERE, null, ex);
      }

      constructor = newConstructor;
    }

    public String getText() {
      return text;
    }

    public char getMnemonic() {
      return mnemonic;
    }

    public Constructor<? extends Component> getConstructor() {
      return constructor;
    }

  }

  public Map<String, Object> packProperties();

  public void loadProperties(Map<String, Object> properties);
}
