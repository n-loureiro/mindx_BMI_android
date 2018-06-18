package com.joaoaraujo.mindX_android;

import android.content.Intent;
import android.util.Log;

import processing.core.PApplet;

public class SketchCursor extends PApplet {

    private boolean isStarting = true;
    private float ballHist[] = new float[8];
    private boolean isBaseline = true, isUp = false, isDown = false;
    private boolean reinforce_up = false, reinforce_down = false, goal_up = false, goal_down = false, baseline_reinforce = false;

    int textSize = 0;
    private int score = 0;

    private double[] thresholds;
    private double atractor_weight;

    public SketchCursor(double[] thresholds,double atractor_weight){
        this.thresholds = thresholds;
        this.atractor_weight = atractor_weight;
    }

    public void settings() {
        size(width, height);
    }

    public void setup() {
        orientation(LANDSCAPE);
        background(0,0,255);
        stroke(255);
        textSize = width/50;
        textSize(textSize);
        textAlign(LEFT);
    }

    public void draw()
    {

        if (isStarting){

            for(int i = 0; i < 8; i++)
                ballHist[i] = height/2;
            score = 0;

            isStarting = false;

        }


            // if the task has started show the BMI task UI
            BMI_view();

    }

    // Button functions for device connection and BMI task preferences
    public void mouseReleased()
    {

        // Trial buttons X area
        if(mouseX > width - width / 10 && mouseX < width - width / 10 + width/15){

            if (mouseY > height/2 - width/14 - width/30 && mouseY < height/2 - width/14 + width/30){
                isUp = true;
                isDown = false;
                isBaseline = false;
            }

            else if(mouseY > height/2 - width/30 && mouseY < height/2 + width/30){
                isUp = false;
                isDown = false;
                isBaseline = true;
            }

            else if(mouseY > height/2 + width/14 - width/30 && mouseY < height/2 + width/14 + width/30){
                isUp = false;
                isDown = true;
                isBaseline = false;
            }

        }

    }

    // Loads BMI task visual components and buttons
    private void BMI_view(){

        background(200,200,200);

        pushStyle();
        stroke(50);
        strokeWeight(width/200);
        line(0, PositionToPixels((float)thresholds[1],thresholds),width,PositionToPixels((float)thresholds[1],thresholds));
        line(0, PositionToPixels((float)thresholds[2],thresholds),width,PositionToPixels((float)thresholds[2],thresholds));
        line(0, PositionToPixels((float)thresholds[3],thresholds),width,PositionToPixels((float)thresholds[3],thresholds));
        line(0, PositionToPixels((float)thresholds[4],thresholds),width,PositionToPixels((float)thresholds[4],thresholds));
        popStyle();

        if(isUp)
        {
            pushStyle();
            fill(255,0,0);
            stroke(255,0,0);
            triangle(width - width / 10 - width/15,width/15, width - width / 10 - width/20,width/60,width - width / 10 - width/30,width/15);
            popStyle();

            if(reinforce_up){
                pushStyle();
                fill(255,150,150);
                rect(0, (PositionToPixels((float)thresholds[1],thresholds)),width, (PositionToPixels((float)thresholds[2],thresholds) - PositionToPixels((float)thresholds[1],thresholds)));
                popStyle();
            }

            else if(goal_up){
                pushStyle();
                fill(255,0,0);
                rect(0, (PositionToPixels((float)thresholds[0],thresholds)),width, (PositionToPixels((float)thresholds[1],thresholds) - PositionToPixels((float)thresholds[0],thresholds)));
                popStyle();
            }

        }

        else if(isDown){
            pushStyle();
            fill(0,0,255);
            stroke(0,0,255);
            triangle(width - width / 10 - width/15,height - width/15, width - width / 10 - width/20,height - width/60,width - width / 10 - width/30,height - width/15);
            popStyle();

            if(reinforce_down){
                pushStyle();
                fill(150,150,255);
                rect(0, (PositionToPixels((float)thresholds[3],thresholds)),width, (PositionToPixels((float)thresholds[4],thresholds) - PositionToPixels((float)thresholds[3],thresholds)));
                popStyle();
            }

            if(goal_down){
                pushStyle();
                fill(0,0,255);
                rect(0, (PositionToPixels((float)thresholds[4],thresholds)),width, (PositionToPixels((float)thresholds[5],thresholds) - PositionToPixels((float)thresholds[4],thresholds)));
                popStyle();
            }

        }

        else {

            if(baseline_reinforce){
                pushStyle();
                fill(150,255,100);
                rect(0, (PositionToPixels((float)thresholds[2],thresholds)),width, (PositionToPixels((float)thresholds[3],thresholds) - PositionToPixels((float)thresholds[2],thresholds)));
                popStyle();
            }

        }

        pushStyle();
        stroke(0,0,255);
        strokeWeight(width/120);
        line(0, ballHist[7], (width/(2*7)), ballHist[6]);
        line(width/(2*7), ballHist[6], (2*width/(2*7)), ballHist[5]);
        line(2 * width/(2*7), ballHist[5], (3*width/(2*7)), ballHist[4]);
        line(3 * width/(2*7), ballHist[4], (4*width/(2*7)), ballHist[3]);
        line(4 * width/(2*7), ballHist[3], (5*width/(2*7)), ballHist[2]);
        line(5 * width/(2*7), ballHist[2], (6*width/(2*7)), ballHist[1]);
        line(6 * width/(2*7), ballHist[1], (7*width/(2*7)), ballHist[0]);
        popStyle();

        pushStyle();
        fill(255,0,0);
        stroke(0);
        ellipse(width/2, ballHist[0], width/15, width/15);
        popStyle();

        pushStyle();
        fill(255);
        stroke(255,0,0);
        rect(width - width / 10, height/2 - width/14 - width/30,width/15, width/15);
        fill(255,0,0);
        text("UP", (float)(width - width / 10 + (width - width / 10)/(textSize*2.5)), (float)(height/2 - width/14 - width/30+ textSize*1.5));
        popStyle();

        pushStyle();
        fill(255);
        stroke(50);
        rect(width - width / 10, height/2- width/30,width/15, width/15);
        fill(50);
        text("BL", (float)(width - width / 10 + (width - width / 10)/(textSize*2.5)),(float) (height/2 - width/30 + textSize*1.5));
        popStyle();

        pushStyle();
        fill(255);
        stroke(0,0,255);
        rect(width - width / 10, height/2 + width/14 - width/30,width/15, width/15);
        fill(0,0,255);
        text("DO", (float)(width - width / 10 + (width - width / 10)/(textSize*2.5)),(float) (height/2 + width/14 - width/30 + textSize*1.5));
        popStyle();

        pushStyle();
        fill(0);
        text("Score: "+score, 2* width / 3, height/2);
        popStyle();
    }

    public void BMI_update(float position)
    {
        // circular shift on ball position
        for(int i = 6; i >= 0; i--){
            ballHist[i+1] = ballHist[i];
        }

        reinforce_up = reinforce_down = goal_up = goal_down = baseline_reinforce = false;

        if(PositionToPixels((position),thresholds) < PositionToPixels((float)thresholds[2],thresholds) && isUp)
        reinforce_up = true;
    else if(PositionToPixels((position),thresholds) > PositionToPixels((float)thresholds[3],thresholds) && isDown)
        reinforce_down = true;
        if(PositionToPixels((position),thresholds) < PositionToPixels((float)thresholds[1],thresholds) && isUp)
        {goal_up = true; reinforce_up = false;}
    else if(PositionToPixels((position),thresholds) > PositionToPixels((float)thresholds[4],thresholds) && isDown)
        {goal_down = true; reinforce_down = false;}

        //Atractor code;
        if(isUp) // atract up
            ballHist[0] = (float)((1.0-atractor_weight) * PositionToPixels((position),thresholds) + atractor_weight * PositionToPixels((float)thresholds[1],thresholds));
    else if (isDown) // atract down
        ballHist[0] = (float)((1.0-atractor_weight) * PositionToPixels((position),thresholds) + atractor_weight * PositionToPixels((float)thresholds[4],thresholds));
    else if (isBaseline) // atract middle
        ballHist[0] = (float)((1.0-atractor_weight) * PositionToPixels((position),thresholds) + atractor_weight *
            (PositionToPixels((float)thresholds[2],thresholds) + (PositionToPixels((float)thresholds[3],thresholds))/2));

        //ballHist[0] = PositionToPixels((position),thresholds); // regular ball position with no atractor
        //println(ballHist[0]);

        if(ballHist[0] < PositionToPixels((float)thresholds[2],thresholds) &&
                ballHist[0] > PositionToPixels((float)thresholds[1],thresholds)){

            if(isUp) score += 10;
            else if(isDown) score -= 10;
            else if(isBaseline) score -=10;

        }

        else if(ballHist[0] < PositionToPixels((float)thresholds[1],thresholds)){
            if(isUp) score += 50;
            else if(isDown) score -= 50;
            else if(isBaseline) score -=50;
        }

        else if(ballHist[0] < PositionToPixels((float)thresholds[4],thresholds) &&
                ballHist[0] > PositionToPixels((float)thresholds[3],thresholds)){

            if(isUp) score -= 10;
            else if(isDown) score += 10;
            else if(isBaseline) score -=10;

        }

        else if(ballHist[0] > PositionToPixels((float)thresholds[4],thresholds)){
            if(isUp) score -= 50;
            else if(isDown) score += 50;
            else if(isBaseline) score -=50;
        }

        else if(ballHist[0] < PositionToPixels((float)thresholds[3],thresholds) &&
                ballHist[0] > PositionToPixels((float)thresholds[2],thresholds)){

            if(isBaseline){score +=10;  baseline_reinforce = true;}
        }

    }



    // Function to scale the BT input to the corresponding position in pixels on the screen
    private float PositionToPixels(float newPos, double[] thresholds){

        double threshold_range = (float)thresholds[5] - (float)thresholds[0];
        return (float)(-(((float)thresholds[5] - newPos)/threshold_range) * height + height);


    }



}