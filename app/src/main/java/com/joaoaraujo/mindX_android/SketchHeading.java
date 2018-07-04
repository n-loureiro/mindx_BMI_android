package com.joaoaraujo.mindX_android;

import processing.core.PApplet;

public class SketchHeading extends PApplet {

    double[] thresholds;

    int textSize = 0;
    int score = 0;
    double atractor_weight;
    double header_radius, airplane_body;
    double arc_length, header_pos;

    float newPos = 0;
    float time;
    boolean showRed = false;
    float target_left, target_right;
    float max_turning_rate;
    float position_new = 0;
    public static boolean isLoading = true;

    public SketchHeading(double[] thresholds,double atractor_weight){
        this.thresholds = thresholds;
        this.atractor_weight = atractor_weight;

    }

    public void settings() {
        size(width, height);
    }

    public void setup() {
        orientation(PORTRAIT);
        stroke(255);
        textSize = width/30;
        textSize(textSize);
        textAlign(CENTER);
        imageMode(CENTER);
        rectMode(CENTER);
        ellipseMode(CENTER);

        header_radius = width/1.2;
        airplane_body = width/10;
        arc_length = PI/4;
        frameRate(60);
        time = millis();
        header_pos = 0;
        background(180,180,180);
        target_left = PI + PI/2;
        target_right = 2*PI - PI/2;
        max_turning_rate = PI/4;

    }

    public void draw()
    {

        // if the task has started show the BMI task UI
        /*if(millis() - time >= 250)
        {
            newPos = (float)(-((-2.5-randomGaussian())/(5))*(thresholds[5]- thresholds[0]) + thresholds[0]);
            //newPos = -((-5)/(5))*(thresholds[5]- thresholds[0]) + thresholds[0];
            time = millis();
        }

        BMI_update(newPos);*/
        BMI_view();

    }

    // Loads BMI task visual components and buttons
    private void BMI_view(){

        float atractor_rate = 0;
            if (header_pos + (header_pos + arc_length) / 2 >= 0.0) //positive position
            {
                if ((header_pos + (header_pos + arc_length)) / 2 < PI / 2 && (header_pos + (header_pos + arc_length)) / 2 > 0) //down right quadrant
                    atractor_rate = -max_turning_rate;
                else if ((header_pos + (header_pos + arc_length)) / 2 < 2 * PI && (header_pos + (header_pos + arc_length)) / 2 > 3 * PI / 2) //up right quadrant
                    atractor_rate = -max_turning_rate;
                else if ((header_pos + (header_pos + arc_length)) / 2 < PI && (header_pos + (header_pos + arc_length)) / 2 > PI / 2) //down left quadrant
                    atractor_rate = max_turning_rate;
                else if ((header_pos + (header_pos + arc_length)) / 2 < 3 * PI / 2 && (header_pos + (header_pos + arc_length)) / 2 > PI) //up left quadrant
                    atractor_rate = max_turning_rate;
            } else { //negative position
                if ((header_pos + (header_pos + arc_length)) / 2 < -3 * PI / 2 && (header_pos + (header_pos + arc_length)) / 2 > -2 * PI) //down right quadrant
                    atractor_rate = -max_turning_rate;
                if ((header_pos + (header_pos + arc_length)) / 2 < 0 && (header_pos + (header_pos + arc_length)) / 2 > -PI / 2) //up right quadrant
                    atractor_rate = -max_turning_rate;
                if ((header_pos + (header_pos + arc_length)) / 2 < -PI && (header_pos + (header_pos + arc_length)) / 2 > -3 * PI / 2) //down left quadrant
                    atractor_rate = max_turning_rate;
                if ((header_pos + (header_pos + arc_length)) / 2 < -PI / 2 && (header_pos + (header_pos + arc_length)) / 2 > -PI) //up left quadrant
                    atractor_rate = max_turning_rate;
            }

            atractor_rate = atractor_rate / 2;

            header_pos = header_pos + (((atractor_weight * atractor_rate) + ((1 - atractor_weight) * PositionToCircle(position_new, thresholds))) * 0.06667); // constant = 1/frameRate * 4
            background(180, 180, 180);
            header_pos = header_pos % (2 * PI);
            //text("Headset: "+newPos,width/2,height/10);

            if (((header_pos + arc_length) >= target_left && (header_pos) <= target_right) || ((header_pos) <= target_right - 2 * PI && (header_pos + arc_length) >= target_left - 2 * PI)) {
                showRed = true;
                if (score < 100) score = score + 1;
                else {
                    RandomizeNewTarget();
                }
            } else
                showRed = false;
            //Circle
            pushStyle();
            stroke(130);
            strokeWeight(width / 50);
            noFill();
            ellipse(width / 2, height / 2, (float) header_radius, (float) header_radius);
            popStyle();

            //Plane view
            pushStyle();
            stroke(130);
            strokeWeight(width / 50);
            line(width / 2, (float) (height / 2 + airplane_body), width / 2, (float) (height / 2 - airplane_body));
            line((float) (width / 2 + airplane_body), (float) (height / 2 - (airplane_body / 2)), (float) (width / 2 - airplane_body), (float) (height / 2 - (airplane_body / 2)));
            line((float) (width / 2 + (airplane_body / 3)), (float) (height / 2 + (airplane_body / 1.1)), (float) (width / 2 - (airplane_body / 3)), (float) (height / 2 + (airplane_body / 1.1)));
            popStyle();

            //heading circular bar
            pushStyle();
            if (showRed) stroke(0, 255, 0);
            else stroke(255);
            strokeWeight(width / 50);
            noFill();
            arc(width / 2, height / 2, (float) header_radius, (float) header_radius, (float) header_pos, (float) (header_pos + arc_length));
            popStyle();

            //Score bar template
            pushStyle();
            stroke(130);
            fill(130);
            rect(width / 2, (float) (height / 2 + header_radius / 2 + width / 5), (float) (width / 1.2), width / 10);
            popStyle();

            //Score bar with score
            pushStyle();
            stroke(0, 255, 0);
            fill(0, 255, 0);
            rect(width / 2, (float) (height / 2 + header_radius / 2 + width / 5), (float) (score * (width / 1.2)) / 100, width / 10);
            popStyle();

            //Area of scoring
            pushStyle();
            stroke(0, 255, 0, 50);
            strokeWeight(width / 50);
            noFill();
            arc(width / 2, height / 2, (float) header_radius, (float) header_radius, target_left, target_right);
            popStyle();
    }

    public void BMI_update(float position)
    {
        this.position_new = position;

    }

    private float PositionToCircle(float newPos, double[] thresholds) {

        double threshold_range = thresholds[5] - thresholds[0];
        return (float)(-((thresholds[5] - newPos)/threshold_range) * (max_turning_rate*2) + max_turning_rate);
    }

    private void RandomizeNewTarget(){
        float randomTarget = random(0, PI);
        //target_right = randomTarget - PI/3;
        //target_left = (randomTarget-PI) + PI/3;
        header_pos = randomTarget;
        score = 0;
    }

}
