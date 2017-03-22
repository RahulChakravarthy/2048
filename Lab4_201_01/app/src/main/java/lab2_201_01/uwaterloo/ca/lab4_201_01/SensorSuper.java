package lab2_201_01.uwaterloo.ca.lab4_201_01;

import java.util.LinkedList;

import static java.lang.StrictMath.abs;


public class SensorSuper {

    private LinkedList<Float> xcomponent = new LinkedList<>();//xcomponent of acceleration
    private LinkedList<Float> ycomponent = new LinkedList<>();//ycomponent of acceleration
    private LinkedList<Float> zcomponent = new LinkedList<>();//zcomponent of acceleration
    private boolean CreateBlock = false;
    private int Size;
    private StateMachine State = StateMachine.UNKNOWN;

    public SensorSuper(int size) {
        this.Size = size;
    }
    public void removeLast(){
        //this function removes the last value in each component Linked list if the size exceeds max size set
        if (this.GetSize() == Size) {
            xcomponent.removeLast();
            ycomponent.removeLast();
            zcomponent.removeLast();
        }
    }
    public void addFirst(float x, float y, float z, boolean CleanData){
        //This function captures incoming raw values and either applies a filter or simply stores them
        //into their respective component Linked list
        float[] tempData = {x,y,z};
        //If data needs to be cleaned, Clean data and store (can only occur if at least one other point is in the list)
        if (CleanData && xcomponent.size() > 0){
            xcomponent.add(0, CleanDataV1(tempData[0], xcomponent.getFirst()));
            ycomponent.add(0, CleanDataV1(tempData[1], ycomponent.getFirst()));
            zcomponent.add(0, CleanDataV1(tempData[2], zcomponent.getFirst()));
        }
        else {
            //If raw data is being stored or its the first value
            xcomponent.add(0, tempData[0]);
            ycomponent.add(0, tempData[1]);
            zcomponent.add(0, tempData[2]);
        }

    }
    public int GetSize(){
        return xcomponent.size();
    }
    private float CleanDataV1(float newReading, float filteredReading){
        //Trying Constant C = 10
        final int C = 10;
        filteredReading += (newReading - filteredReading) / C;
        return filteredReading;
    }
    //THIS PART IS TO DETERMINE WHAT STATE THE PHONE IS IN
    public String ReturnMovement(){
        /*READ BEFORE

        Return movement is broken into a few parts:
                1) Methods need previous values to determine state so movement doesnt start getting calculated
                   until there are at least 5 values in both x and y component linked lists
                2) In most states, the Determine state method is always called since that method is used to detected any possible
                    movement be it up down left or right
                3) Once Determine state is triggered it either sets the state into a specific movement corresponding to left,
                   right, up  or down, or it doesnt detect any actual movement, in which case it keeps the state as unknown
                4) Once a state is chosen, the next time return movement is called it will enter that specific case to see if
                   the rest of the movements are satisfied (depending on what motion was initialiated i.e left right...)
                5) Once inside a specific case it will stay in that case and check to see if the next movements are satisfied
                6) If the second set of movements are satisfied, the direction is determined and is sent to the textview
                *** IMPORTANT***
                7) The return state method is called after every iteration of Return movement to refresh the state machine values
                   and to break a deadlock if a state is entered but never satisfied towards the end. It works by checking the previous
                   40 values to see if there is any drastic change in slope i.e a movement being started. If nothing happend in the last
                   40 values it resets the state back to Unknown and the process repeats
        */

        String Direction;

        //ONLY AFTER 5 VALUES HAVE BEEN CALCULATED
        if (xcomponent.size() > 5) {

            //Based on the direction output the proper String
            switch (this.State) {
                //DEFAULT CASES SHOULD ALWAYS CHECK FOR CHANGE IN STATE
                case UP:
                    Direction = "UP";
                    break;
                case DOWN:
                    Direction = "DOWN";
                    break;
                case LEFT:
                    Direction = "LEFT";
                    break;
                case RIGHT:
                    Direction = "RIGHT";
                    break;

                //MOVING RIGHT IN X-DIRECTION
                case RISEFIRST_X:
                    //Check the fall state to see if entire motion is completed
                    if (FAllSECOND_X()){
                        //ENTIRE LEFT MOTION COMPLETE
                        this.State = StateMachine.RIGHT;
                        Direction = "RIGHT";
                        this.CreateBlock = true;
                        break;
                    }
                    // If it isn't freeze in this state and call Determine State in case a new motion is taking place
                    Direction = "RIGHT";
                    break;

                //MOVING LEFT in X-DIRECTION
                case FALLFIRST_X:
                    //Check the Rise state to see if entire motion is complete
                    if (RISESECOND_X()){
                        //ENTIRE LEFT MOTION COMPLETE
                        this.State = StateMachine.LEFT;
                        Direction = "LEFT";
                        this.CreateBlock = true;
                        break;
                    }
                    // If it isn't freeze in this state and call Determine State in case a new motion is taking place
                    //DetermineState();
                    Direction = "LEFT";
                    break;

                //MOVING UP in Y-Direction
                case RISEFIRST_Y:
                    if (FALLSECOND_Y()){
                        //ENTIRE UP MOTION COMPLETE
                        Direction = "UP";
                        this.State = StateMachine.UP;
                        this.CreateBlock = true;
                        break;
                    }

                    // If it isn't freeze in this state and call Determine State in case a new motion is taking place
                    Direction = "UP";
                    break;

                //MOVING DOWN in Y-Direction
                case FALLFIRST_Y:
                    if (RISESECOND_Y()){
                        Direction = "DOWN";
                        this.State = StateMachine.DOWN;
                        this.CreateBlock = true;
                        break;
                    }
                    // If it isn't freeze in this state and call Determine State in case a new motion is taking place
                    Direction = "DOWN";
                    break;
                //If the state is ever unknown, always pass the values through DetermineState to find movement change
                case UNKNOWN:
                    DetermineState();
                    Direction = "IDLE";
                    break;

                default:
                    DetermineState();
                    Direction = "IDLE";
                    break;

            }
        }

        else{
            Direction = "IDLE";
        }
        //If nothing has happened recently, then return state to IDLE (starts checking after 36 values
        //This is constantly running to reset Finite State machine (could have used timer here but
        //this is more reliable
        ReturnState();


        return Direction;
    }
    //THE FOLLOWING METHODS ARE FOR THE FINITE STATE MACHINE MOVEMENT PROCESSING
    //method for FallSecond-x
    protected boolean FAllSECOND_X(){
        return (xcomponent.getFirst() - xcomponent.get(5)) / 0.1 < 0;
    }
    //method for RiseSecond-x
    protected  boolean RISESECOND_X(){
        return (xcomponent.getFirst() - xcomponent.get(5)) / 0.1 > 0;
    }
    //method for FallSecond-y
    protected boolean FALLSECOND_Y(){
        return (ycomponent.getFirst() - ycomponent.get(5)) / 0.1 < 0;
    }
    //method for FallSecond-y
    protected boolean RISESECOND_Y(){
        return (ycomponent.getFirst() - ycomponent.get(5)) /0.1 > 0;
    }
    //Method for when state is unknown
    protected void DetermineState(){
        //SENSOR DELAY_GAME RECORDS AN INPUT EVERY 0.02 SECONDS
        //Calculate the slope of the past 5 values of each component to see what direction the phone is moving
        //Generate slope from current value and 5th value before

        //This is a general method that starts the states in the FSM

        float xminSlope = 10f;
        float yminSlope = 10f;

        //Xcomponent
        //Finding the absolute value tangent between both points //THIS MEANS MOVEMENT IS TAKING PLACE
        if ((abs(xcomponent.getFirst()) - abs(xcomponent.get(5)))/0.1 > xminSlope){

            if (xcomponent.getFirst() > xcomponent.get(5)){ // Increasing slope means RISE FIRST
                this.State = StateMachine.RISEFIRST_X;
                return;
            }
            else { // Decreasing slope means going right
                this.State = StateMachine.FALLFIRST_X;
                 return;
            }
        }
        else {
            /* This means  the phone is not moving enough to trigger the state machine*/
        }

        //Ycomponent
        if ((abs(ycomponent.getFirst()) - abs(ycomponent.get(5)))/0.1 > yminSlope){

            if (ycomponent.getFirst() > ycomponent.get(5)){ // Increasing slope means phone is going Down
                this.State = StateMachine.RISEFIRST_Y;

            }
            else { // Decreasing slope means going Up
                this.State = StateMachine.FALLFIRST_Y;
            }
        }
        else {
            /* This means  the phone is not moving enough to trigger the state machine*/
        }
    }
    //Default method that runs to reset state after too many non-active values
    private void ReturnState(){

        //Create final checker to see if any of the values have moved in the past 25 data entries, if not set
        //Textview to Unknown
        //Make sure there are at least 25 values in the component list
        //X component
        if (xcomponent.size() > 8) {
            for (int i = 0; i < 2; i++) {
                //Check to see slope between every 5 points is less then some threshold, if  all of them are lower
                // Switch the textview state to UNKNOWN otherwise return
                float threshold = 6f;
                if (((abs(xcomponent.get(4*i) - xcomponent.get(4*(i+1)))/0.1) > threshold)){
                    return;
                }
            }
        }
        //Y component
        if (ycomponent.size() > 8) {
            for (int i = 0; i < 2; i++) {
                //Check to see slope between every 5 points is less then some threshold, if  all of them are lower
                // Switch the textview state to UNKNOWN otherwise return
                float threshold = 6f;
                if (((abs(ycomponent.get(4*i) - ycomponent.get(4*(i+1)))/0.1) > threshold)){
                    return;
                }
            }
        }

        //If iterator gets to here it means the past 25 values have had not movement
        this.State = StateMachine.UNKNOWN;
    }
    public StateMachine GetterState(){
        return this.State;
    }
    public boolean GetterCreate() {return this.CreateBlock;}
    public void SetterCreate(boolean Set) {this.CreateBlock = Set;}

}

