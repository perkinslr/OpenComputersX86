package lperkins2.ocx86.SerialTTY;

import com.jcraft.jcterm.Connection;
import com.jcraft.jcterm.Term;
import li.cil.oc.api.component.TextBuffer;
import lperkins2.ocx86.OpenComputersX86Architecture;

/**
 * Created by perkins on 9/22/15.
 */
public class OpenComputersTerm implements Term {

    public final TextBuffer screen;
    public final OpenComputersX86Architecture machine;

    public OpenComputersTerm(TextBuffer screen, OpenComputersX86Architecture machine){
        this.screen = screen;
        this.machine=machine;
    }
    @Override
    public void start(Connection connection) {

    }

    @Override
    public int getRowCount() {
        return screen.getHeight();
    }

    @Override
    public int getColumnCount() {
        return screen.getWidth();
    }

    @Override
    public int getCharWidth() {
        return 1;
    }

    @Override
    public int getCharHeight() {
        return 1;
    }

    @Override
    public void setCursor(int x, int y) {
        return;
    }

    @Override
    public void clear() {
        screen.fill(0,0,screen.getWidth(),screen.getHeight(),' ');
    }

    @Override
    public void draw_cursor() {

    }

    @Override
    public void redraw(int x, int y, int width, int height) {

    }

    @Override
    public void clear_area(int x1, int y1, int x2, int y2) {
        screen.fill(x1,y1,x2,y2,' ');
    }

    @Override
    public void scroll_area(int x, int y, int w, int h, int dx, int dy) {

    }

    @Override
    public void drawBytes(byte[] buf, int s, int len, int x, int y) {
        char[][] b = new char[1][len];
        for (int i=0;i<len;i++){
            b[0][i]= (char) buf[i];
        }
        screen.rawSetText(x,y,b);
    }

    @Override
    public void drawString(String str, int x, int y) {
        screen.set(x,y,str,false);
    }

    @Override
    public void beep() {
    }

    @Override
    public void setDefaultForeGround(Object foreground) {
        if(foreground instanceof Integer){
            screen.setForegroundColor((Integer) foreground);
        }
    }

    @Override
    public void setDefaultBackGround(Object background) {
        if(background instanceof Integer){
            screen.setBackgroundColor((Integer) background);
        }
    }

    @Override
    public void setForeGround(Object foreground) {
        if(foreground instanceof Integer){
            screen.setForegroundColor((Integer) foreground);
        }
    }

    @Override
    public void setBackGround(Object background) {
        if(background instanceof Integer){
            screen.setBackgroundColor((Integer) background);
        }
    }

    @Override
    public void setBold() {

    }

    @Override
    public void setUnderline() {

    }

    @Override
    public void setReverse() {

    }

    @Override
    public void resetAllAttributes() {

    }

    @Override
    public int getTermWidth() {
        return screen.getWidth();
    }

    @Override
    public int getTermHeight() {
        return screen.renderHeight();
    }

    @Override
    public Object getColor(int index) {
        return null;
    }
}
