package no.uib.inf252.katscan.view.katview.opengl;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import no.uib.inf252.katscan.event.TransferFunctionListener;
import no.uib.inf252.katscan.project.displayable.TransferFunctionNode;
import no.uib.inf252.katscan.util.TransferFunction;

/**
 *
 * @author Marcelo Lima
 */
public class CompositeRenderer extends VolumeRenderer implements TransferFunctionListener {
    
    private final int[] textureLocation = new int[1];
    private boolean transferFunctionDirty;
    
    public CompositeRenderer(TransferFunctionNode displayable) throws GLException {
        super(displayable, "raycaster");
        //TODO Remove listener when done
        displayable.getTransferFunction().addTransferFunctionListener(this);
    }

    @Override
    protected void preDraw(GLAutoDrawable drawable) {
        if (transferFunctionDirty) {
            updateTransferFunction(drawable.getGL().getGL4());
            transferFunctionDirty = false;
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable); 
        
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glGenTextures(1, textureLocation, 0);
        gl4.glBindTexture(GL4.GL_TEXTURE_1D, textureLocation[0]);
        gl4.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
        gl4.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
        gl4.glTexParameteri(GL4.GL_TEXTURE_1D, GL4.GL_TEXTURE_WRAP_R, GL4.GL_CLAMP_TO_BORDER);
        transferFunctionDirty = true;
        
        checkError(gl4, "Create Transfer Function");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        GL4 gl4 = drawable.getGL().getGL4();
        
        gl4.glDeleteTextures(0, textureLocation, 0);
    }
    
    private void updateTransferFunction(GL4 gl4) {
        BufferedImage transferImage = new BufferedImage(TransferFunction.TEXTURE_SIZE, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = (Graphics2D) transferImage.getGraphics();
        g2d.setPaint(getDisplayable().getTransferFunction().getPaint(0f, TransferFunction.TEXTURE_SIZE));
        g2d.drawLine(0, 0, TransferFunction.TEXTURE_SIZE, 0);
        g2d.dispose();
        
        byte[] dataElements = (byte[]) transferImage.getRaster().getDataElements(0, 0, TransferFunction.TEXTURE_SIZE, 1, null);
        gl4.glTexImage1D(GL4.GL_TEXTURE_1D, 0, GL4.GL_RGBA, TransferFunction.TEXTURE_SIZE, 0, GL4.GL_RGBA, GL4.GL_UNSIGNED_INT_8_8_8_8_REV, ByteBuffer.wrap(dataElements));
    }

    private TransferFunctionNode getDisplayable() {
        return (TransferFunctionNode) displayable;
    }

    @Override
    public void pointCountChanged() {
        transferFunctionDirty = true;
        repaint();
    }

    @Override
    public void pointValueChanged() {
        transferFunctionDirty = true;
        repaint();
    }

}