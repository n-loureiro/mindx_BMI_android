package com.joaoaraujo.mindX_android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PShape;

import static com.joaoaraujo.mindX_android.Activity_Games.portal;
import static com.joaoaraujo.mindX_android.Activity_Games.portal_sprite;
import static com.joaoaraujo.mindX_android.Activity_Games.portal_success;
import static com.joaoaraujo.mindX_android.Activity_Games.shipSprite;
import static com.joaoaraujo.mindX_android.Activity_Games.spaceBack;

public class SketchSpace extends PApplet {
    float ballHist[] = new float[5];
    double[] thresholds;

    int textSize = 0;
    int score = 0;
    double atractor_weight;

    float shipSize;
    int ringCounter = 0;
    double ringX = 0.0;
    double ringY = 0.0;

    boolean willStop;
    float newPos;
    Context context;

    public SketchSpace(double[] thresholds,double atractor_weight, Context appContext){
        this.thresholds = thresholds;
        this.atractor_weight = atractor_weight;
        context = appContext;
    }

    public void settings() {
        size(width, height);
    }

    public void setup() {
        orientation(LANDSCAPE);
        background(0);
        textSize = width/50;
        textSize(textSize);
        textAlign(LEFT);
        imageMode(CENTER);
        rectMode(CENTER);
        for(int i = 0; i < 5; i++)
            ballHist[i] = height/2;
        score = 0;

        shipSize = 3*width/30;

    }

    public void draw()
    {

        // if the task has started show the BMI task UI
        //newPos = PositionToPixels(random((float)thresholds[5], (float)thresholds[0]), thresholds);
        //BMI_update(newPos);
        BMI_view();

    }

    // Button functions for device connection and BMI task preferences
    public void mouseReleased() {



    }

    // Loads BMI task visual components and buttons
    private void BMI_view(){
        //delay(250);
        image(spaceBack,(float)(width/2.0),(float)(height/2.0),(float)width,(float)height);

        pushStyle();
        stroke(255, 255, 255, 100);
        strokeWeight(width/120);
        line(width, ballHist[4] - shipSize/2, width-(width/(2*7)), ballHist[3]- shipSize/2);
        line(width-width/(2*7), ballHist[3]- shipSize/2, width-(2*width/(2*7)), ballHist[2]- shipSize/2);
        line(width-2 * width/(2*7), ballHist[2]- shipSize/2, width-(3*width/(2*7)), ballHist[1]- shipSize/2);
        line(width-3 * width/(2*7), ballHist[1]- shipSize/2, width-(4*width/(2*7))+shipSize/2, ballHist[0]- shipSize/2);
        line(width, ballHist[4] + shipSize/2, width-(width/(2*7)), ballHist[3]+ shipSize/2);
        line(width-width/(2*7), ballHist[3]+ shipSize/2, width-(2*width/(2*7)), ballHist[2]+ shipSize/2);
        line(width-2 * width/(2*7), ballHist[2]+ shipSize/2, width-(3*width/(2*7)), ballHist[1]+ shipSize/2);
        line(width-3 * width/(2*7), ballHist[1]+ shipSize/2, width-(4*width/(2*7))+shipSize/2, ballHist[0]+ shipSize/2);
        popStyle();

        pushStyle();
        fill(255, 0, 0);
        stroke(255);
        //rect(width-(4*width/(2*7)), ballHist[0], shipSize, shipSize);
        image(shipSprite, width-(4*width/(2*7)), ballHist[0], shipSize, shipSize);
        popStyle();

  /*pushStyle();
  fill(0);
  text("Score: "+score, width / 3, height/2);
  popStyle();*/

        if(ringCounter == 10){
            ringX = 0.0;
            ringY = PositionToPixels(random((float)thresholds[5], (float)thresholds[0]), thresholds);
            ringCounter = 0;
            portal_sprite = portal;
        }

        if(ringY != 0.0){
            pushStyle();
            fill(0, 0, 255);
            stroke(255);
            //rect(ringX,ringY, shipSize, shipSize);
            image(portal_sprite,(float)ringX,(float)ringY, (float)(shipSize * 1.2), (float)(shipSize * 1.2));
            popStyle();

        }

        willStop = checkShipRingCollision((float)ringX,(float)ringY,(float)(width-(4.0*width/(2.0*7.0))),ballHist[0],shipSize);


    }

    public void BMI_update(float position)
    {
        // circular shift on ball position
        for (int i = 3; i >= 0; i--) {
            ballHist[i+1] = ballHist[i];
        }
        ballHist[0] =  (float)((1 - atractor_weight)*position + atractor_weight * ringY);
        //ballHist[0] = PositionToPixels(float(incoming_data),thresholds); // regular ball position with no atractor
        //println(ballHist[0]);

        ringCounter = ringCounter + 1;
        ringX = ringX + width/8;

    }



    // Function to scale the BT input to the corresponding position in pixels on the screen
    private float PositionToPixels(float newPos, double[] thresholds){

        double threshold_range = (float)thresholds[5] - (float)thresholds[0];
        return (float)(-(((float)thresholds[5] - newPos)/threshold_range) * height + height);


    }

    private boolean checkShipRingCollision(float ringX, float ringY, float shipX, float shipY, float shipSize){
        if(ringX + shipSize/2 > shipX - shipSize/2 &&
                ringY + shipSize/2 > shipY - shipSize/2 &&
                ringX - shipSize/2 < shipX + shipSize/2 &&
                ringY - shipSize/2 < shipY + shipSize/2){

            portal_sprite = portal_success;
            image(portal_sprite,ringX,ringY, (float)(shipSize * 1.2), (float)(shipSize * 1.2));
            return true;
        }

        return false;
    }



}
