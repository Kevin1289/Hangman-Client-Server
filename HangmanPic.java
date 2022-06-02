package javafinalhangmanprojectclient;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;


public class HangmanPic extends JPanel {
    int errorLeft;
    
    HangmanPic(int error){
        super();
        errorLeft = error;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int xCenter = getWidth()/2;
        int yCenter = getHeight()/2;
        int faceRadius = 100;
        
        //Draw face
        if(errorLeft>=1){
            g.setColor( Color.BLACK );
            g.drawOval( xCenter-(faceRadius/2), yCenter-150, 100, 100 );
        }
        
        //Draw line
        if(errorLeft>=2){
            g.drawLine(xCenter, yCenter-50, xCenter, yCenter+100);
        }
        
        //Draw arms
        if(errorLeft>=3){
            g.drawLine(xCenter, yCenter, xCenter-50, yCenter);
        }
        
        if(errorLeft>=4){
            g.drawLine(xCenter, yCenter, xCenter+50, yCenter);
        }
        
        //Draw legs
        if(errorLeft>=5){
            g.drawLine(xCenter, yCenter+100, xCenter-50, yCenter+150);
        }
        
        if(errorLeft>=6){
            g.drawLine(xCenter, yCenter+100, xCenter+50, yCenter+150);
        }
    }
    
    public void refresh(int newError){
        System.out.println("REFRESH: "+ newError);
        errorLeft = 6-newError;
        repaint();
    }
    
}
