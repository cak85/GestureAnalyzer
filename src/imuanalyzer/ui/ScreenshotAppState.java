package imuanalyzer.ui;

/** Kopie von com.jme3.app.state.ScreenshotAppState; Modifiziert **/
 
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;
 
public class ScreenshotAppState extends AbstractAppState implements SceneProcessor {
 
    private static final Logger logger = Logger.getLogger(ScreenshotAppState.class.getName());
    private boolean capture = false;
    private Renderer renderer;
    private ByteBuffer outBuf;
    private String screenShotFile;
    private int shotIndex = 0;
    private BufferedImage awtImage;
    ActionListener readyAction;
 
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        List<ViewPort> vps = app.getRenderManager().getPostViews();
        ViewPort last = vps.get(vps.size()-1);
        last.addProcessor(this);
 
    }
 
    public void takeScreenShot() { takeScreenShot(null); }
    public void takeScreenShot(String screenShotFile)
    {
      this.screenShotFile = screenShotFile;
      capture = true;
    }
 
    public void setActionListener(ActionListener readyAction) {this.readyAction = readyAction;}
 
    public Image getLastScreenShot()
    {
      if (awtImage==null) {return null;}
      return awtImage.getScaledInstance(awtImage.getWidth(),awtImage.getHeight(),Image.SCALE_FAST);
    }
 
    public void initialize(RenderManager rm, ViewPort vp) {
        renderer = rm.getRenderer();
        reshape(vp, vp.getCamera().getWidth(), vp.getCamera().getHeight());
    }
 
    @Override
    public boolean isInitialized() {
        return super.isInitialized() && renderer != null;
    }
 
    public void reshape(ViewPort vp, int w, int h) {
        outBuf = BufferUtils.createByteBuffer(w*h*4);
        awtImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
    }
 
  public void preFrame(float tpf) {}
 
  public void postQueue(RenderQueue rq) {}
 
  public void postFrame(FrameBuffer out)
  {
    if (capture)
    {
      capture = false;
      shotIndex++;
 
      renderer.readFrameBuffer(out, outBuf);
      Screenshots.convertScreenShot(outBuf, awtImage);
 
      if (screenShotFile!=null)
      {
        try
        {
          ImageIO.write(awtImage, "png", new File(screenShotFile));
        } catch (IOException ex){ logger.log(Level.SEVERE, "Error while saving screenshot", ex); }
      }
      if (readyAction!=null)
      {
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            readyAction.actionPerformed(null);
          }
        });
      }
    }
  }
}