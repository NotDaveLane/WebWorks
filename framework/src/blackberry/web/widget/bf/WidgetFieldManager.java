/*
 * WidgetFieldManager.java
 *
 * Research In Motion Limited proprietary and confidential
 * Copyright Research In Motion Limited, 2009-2009
 */

package blackberry.web.widget.bf;

import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.browser.field2.BrowserField;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.traversal.TreeWalker;
import org.w3c.dom.Text;

import blackberry.web.widget.bf.BrowserFieldScreen;
import blackberry.web.widget.bf.WidgetNavigationController;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.XYPoint;
import net.rim.device.api.ui.*;

//import net.rim.device.apps.internal.browser.mango.windowimpl.WindowField;
import net.rim.device.api.util.MathUtilities;

/**
 * 
 */
public class WidgetFieldManager extends VerticalFieldManager {

    /* Creates a new WidgetFieldManager */
    public WidgetFieldManager() {
        this(0);
    }
    
    /* Creates a new WidgetFieldManager with a style */
    public WidgetFieldManager(long style) {
        super(style);
    }

    private BrowserFieldScreen getBrowserFieldScreen() {
        Screen bfScreen = getScreen();

        // Get the screen object
        if(bfScreen instanceof BrowserFieldScreen ) {
            return (BrowserFieldScreen) bfScreen;
        }

        return null;
    }

    /* override */ public boolean navigationMovement(int dx, int dy, int status, int time) {                
        if (getBrowserFieldScreen().getAppNavigationMode()) {
            if (dx == 0 && dy == 0) return true;        
            
            boolean handled = super.navigationMovement( dx, dy, status, time );
            if (handled) {
                return true;
            }

            // Handle the directional event
            int direction = -1;
            if( Math.abs(dx) >= Math.abs(dy)){
                if(dx > 0) {
                    direction = WidgetNavigationController.FOCUS_NAVIGATION_RIGHT;
                } else{
                    direction = WidgetNavigationController.FOCUS_NAVIGATION_LEFT;
                }                
            } else{
                if(dy > 0) {
                    direction = WidgetNavigationController.FOCUS_NAVIGATION_DOWN;
                } else {
                    direction = WidgetNavigationController.FOCUS_NAVIGATION_UP;
                }
            }
            
            try{
                getBrowserFieldScreen().getWidgetNavigationController().handleDirection(direction);
            } catch(Exception e) {
            }
            
            return true;
        }
        
        return super.navigationMovement(dx, dy, status, time);
    }

    protected boolean navigationClick(int status,int time) {
        if (getBrowserFieldScreen().getAppNavigationMode()) {
            if (getBrowserFieldScreen().getWidgetNavigationController().requiresDefaultNavigation()) {
                super.navigationClick(status, time);
            }
    
            try {
                getBrowserFieldScreen().getWidgetNavigationController().handleClick();
            } catch (Exception e) {
            }
            
            return true;
        }
        
        return super.navigationClick(status, time);
    }   
        
    protected boolean navigationUnclick(int status,int time) {
        if (getBrowserFieldScreen().getAppNavigationMode()) {
            if (getBrowserFieldScreen().getWidgetNavigationController().requiresDefaultNavigation()) {
                super.navigationUnclick(status, time);
            }
            
            try {
                getBrowserFieldScreen().getWidgetNavigationController().handleUnclick();
            } catch (Exception e) {
            }
            
            return true;
        }
        
        return super.navigationUnclick(status, time);
    }

    /* override */ public void paint(Graphics graphics) {
        super.paint(graphics);

        // paint current node if exists, is not focused and does not have a hover style
        if (getBrowserFieldScreen().getAppNavigationMode()) {
            if (getBrowserFieldScreen().getWidgetNavigationController().requiresDefaultHover()) {
                Node currentNode = getBrowserFieldScreen().getWidgetNavigationController().getCurrentFocusNode();
                if (currentNode != null) {
                    XYRect position = getPosition(currentNode);
                    if (position != null) {
                        position = scaleRect(position);
                        int oldColor = graphics.getColor();
                        graphics.setColor(0xBBDDFF);
                        graphics.drawRoundRect(position.x-1, position.y-1, position.width+2, position.height+2, 4, 4);
                        graphics.setColor(0x88AAFF);
                        graphics.drawRoundRect(position.x, position.y, position.width, position.height, 4, 4);
                        graphics.setColor(oldColor);
                    }
                }
            }
        }
    }
    
    public void invalidateNode(Node node) {
        if (node == null) return;
        XYRect position = getPosition(node);
        if (position == null) return;
        
        position = scaleRect(position);
        invalidate(position.x-1, position.y-1, position.width+2, position.height+2);
    }
        
    public void scrollToNode(Node node) {
        if (node == null) return;
        XYRect position = getPosition(node);
        if (position == null) return;
        
        position = scaleRect(position);
        scrollToRect(position);
    }

    public void scrollDown() {
        int newVerticalScroll = Math.min( getVerticalScroll() + scaleValue(SAFE_MARGIN), getVirtualHeight() - getHeight() );
        setVerticalScroll( newVerticalScroll );
    }

    public void scrollUp() {
        int newVerticalScroll = Math.max( getVerticalScroll() - scaleValue(SAFE_MARGIN), 0 );
        setVerticalScroll( newVerticalScroll );
    }
    
    public void scrollRight() {
        int newHorizontalScroll = Math.min( getHorizontalScroll() + scaleValue(SAFE_MARGIN), getVirtualWidth() - getWidth() );
        setHorizontalScroll( newHorizontalScroll );
    }

    public void scrollLeft() {
        int newHorizontalScroll = Math.max( getHorizontalScroll() - scaleValue(SAFE_MARGIN), 0 );
        setHorizontalScroll( newHorizontalScroll );
    }    
        
    public static final int SAFE_MARGIN = 30;
    
    private void scrollToRect(XYRect rect) {
        // Check vertical scroll
        int verticalScroll = getVerticalScroll();
        int newVerticalScroll = verticalScroll;

        if (rect.y < verticalScroll) {
            newVerticalScroll = Math.max( rect.y - scaleValue(SAFE_MARGIN), 0 );
        } else if (rect.y + rect.height > verticalScroll + getHeight()) {
            newVerticalScroll = Math.min( rect.y + rect.height - getHeight() + scaleValue(SAFE_MARGIN), getVirtualHeight() - getHeight() );
        }
        
        if (newVerticalScroll - verticalScroll != 0) {
            setVerticalScroll( newVerticalScroll );
        }
        
        // Check horizontal scroll
        int horizontalScroll = getHorizontalScroll();
        int newHorizontalScroll = horizontalScroll;

        if (rect.width >= getWidth()) {
            newHorizontalScroll = Math.max( rect.x, 0 );
        } else if (rect.x < horizontalScroll) {
            newHorizontalScroll = Math.max( rect.x - scaleValue(SAFE_MARGIN), 0 );
        } else if (rect.x + rect.width > horizontalScroll + getWidth()) {
            newHorizontalScroll = Math.min( rect.x + rect.width - getWidth() + scaleValue(SAFE_MARGIN), getVirtualWidth() - getWidth() );
        }
        
        if (newHorizontalScroll - horizontalScroll != 0) {
            setHorizontalScroll( newHorizontalScroll );
        }        
    }
    
    private int scaleValue( int value ) {
        BrowserField bf = getBrowserFieldScreen().getWidgetBrowserField();
        float scale = bf.getZoomScale();        
        return MathUtilities.round( value * scale );
    }

    private XYRect scaleRect( XYRect rect ) {
        return new XYRect(scaleValue(rect.x), scaleValue(rect.y), scaleValue(rect.width), scaleValue(rect.height));
    }

    public int unscaleValue( int value ) {
        BrowserField bf = getBrowserFieldScreen().getWidgetBrowserField();
        float scale = bf.getZoomScale();        
        return MathUtilities.round( value / scale );
    }
    
    public XYRect unscaleRect( XYRect rect ) {
        return new XYRect(unscaleValue(rect.x), unscaleValue(rect.y), unscaleValue(rect.width), unscaleValue(rect.height));
    }
    
    public XYRect getPosition(Node node) {
        BrowserField bf = getBrowserFieldScreen().getWidgetBrowserField();
        XYRect nodeRect = bf.getNodePosition(node);

        return nodeRect;
    }
}

