package application;

import java.util.Timer;
import java.util.TimerTask;

public class gameTimer extends TimerTask{
    static int counter = 0;

    public static void main(String [] args) {
        
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        System.out.println("TimerTask executing counter is: " + counter);
        counter++;
   }

}