package lab2_201_01.uwaterloo.ca.lab4_201_01;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.LinkedList;
import static lab2_201_01.uwaterloo.ca.lab4_201_01.GameLoopTask.Numbers;
import static lab2_201_01.uwaterloo.ca.lab4_201_01.GameLoopTask.TheScore;


public class GameBlock extends ImageView {

    private final float IMAGE_SCALE = 0.67f;

    //Create a Textview to store the number and display to screen
    private TextView ChildNumbers;
    private int ChildNumberXoffset = 150;
    private int ChildNumberYoffset = 65;
    private float textviewSize = 65f;

    RelativeLayout MainLayout;
    //Border dimensions
    private final int rightBorder = 748;
    private final int leftBorder = -57;
    private final int topBorder = -58;
    private final int bottemBorder = 748;
    //Movement speeds
    private final float blockAcceleration = 9f;
    private float Velocity = 0;
    private StateMachine blockDirection = StateMachine.UNKNOWN;

    //Create a lock boolean that will lock any movement until a current movement ends
    private boolean lock = false;
    //Create a MergeLock so that blocks can only merge once per movement
    private boolean mergeLock = false;

    //Create block clean up
    private boolean deleteBlock = false;

    public GameBlock(RelativeLayout MainLayout, Context context, int MyCoordX, int MyCoordY, TextView Numbers) {
        super(context);

        //Set background of block
        this.setBackgroundResource(R.drawable.block);

        if (Numbers != null) {

            //Initialize Textview
            this.ChildNumbers = Numbers;
            this.ChildNumbers.setTextSize(this.textviewSize);
            this.ChildNumbers.setX(MyCoordX + this.ChildNumberXoffset);
            this.ChildNumbers.setY(MyCoordY + this.ChildNumberYoffset);
        }
        //Scale position properly
        this.setScaleX(this.IMAGE_SCALE);
        this.setScaleY(this.IMAGE_SCALE);

        //Set position of new block
        this.setX(MyCoordX);
        this.setY(MyCoordY);

        this.blockDirection = StateMachine.UNKNOWN;
        this.lock = false;
        this.mergeLock = false;
        this.MainLayout = MainLayout;
    }

    //Constructor for factory block
    public GameBlock CreateBlock(RelativeLayout MainLayout, Context context, int X, int Y){
        Numbers.add(new TextView(context));
        GameBlock newblock = new GameBlock(MainLayout, context, X, Y, Numbers.getLast());
        MainLayout.addView(newblock);
        MainLayout.bringChildToFront(newblock);
        return newblock;
    }
    //Constructor for appending blocks to the linked list
    public void CreateBlock(RelativeLayout MainLayout, Context context, LinkedList<GameBlock> Blocks){
        int positionX;
        int positionY;
        //Keep generating random location to place block until no other block is present on that spot
        do {
            positionX = (int) Math.floor(Math.random() * 4);
            positionY = (int) Math.floor(Math.random() * 4);
        } while (this.HitDetection(positionX*268+ this.leftBorder, this.bottemBorder - positionY*268, Blocks));

        //Append a textview node to the Textview LinkedList
        Numbers.add(new TextView(context));
        GameBlock newblock = new GameBlock(MainLayout, context, (this.leftBorder) + positionX * 268, (this.bottemBorder)- positionY * 268, Numbers.getLast());
        MainLayout.addView(newblock);
        MainLayout.bringChildToFront(newblock);
        Blocks.add(newblock);
    }

    //Method for hit detection for when blocks are created
    private boolean HitDetection(int x, int y, LinkedList<GameBlock> Blocks){
        //Check location of each block
        for (GameBlock i: Blocks){
            //If this condition is met it means that the block coordinates generated overlap with another block
            if (Math.abs(i.getX() - (x)) < 15 && Math.abs(i.getY() - y) < 15){
                return true;
            }
        }
        //This means that the block created does not overlap on any other pre-existing block
        return false;
    }

    //This returns true if there exists a block adjacent to this one with the same number, else it returns false
    public boolean checkAdjacentBlocks(LinkedList<GameBlock> Blocks){
        for (GameBlock i: Blocks){
            //Checking blocks that are horizontal with the child block to see if they have the same number
            if (Math.abs(i.getX() - this.getX()) <= 300 && Math.abs(i.getY() - this.getY()) < 10 && this != i){
                if (this.ChildNumbers.getText() == i.ChildNumbers.getText()){
                    return true;
                }
            }
            //Checking blocks that are Vertical with the child block to see if they have the same number
            else if (Math.abs(i.getY() - this.getY()) <= 300 && Math.abs(i.getX() - this.getX()) < 10 && this != i){
                if (this.ChildNumbers.getText() == i.ChildNumbers.getText()){
                    return true;
                }
            }
        }
        //This means all the blocks that are adjacent to this block have different numbers
        return false;
    }
    //Checks to see if movement has occured and captures the direction
    public void MoveDetection(StateMachine State){
        //This switch case captures the movement detected by FSM and places the block state into a type of movement
        this.blockDirection = State;
        if (this.blockDirection == StateMachine.LEFT || this.blockDirection == StateMachine.RIGHT ||
                this.blockDirection == StateMachine.UP || this.blockDirection == StateMachine.DOWN) {
            this.mergeLock = false;
            this.lock = true;
        }
    }

    public void Move(LinkedList<GameBlock> Blocks) {
        //This is the logic for movement and boundary checking while translating the object
        float velocityfix = 1.1f; //fixes velocity of the textviews
        switch (this.blockDirection){
            case LEFT:
                //Checks outside boundary condition
                if (this.getX() > this.leftBorder) {
                    if (this.getX() - this.Velocity < this.leftBorder) {
                        //this is the last frame before the block snaps into place
                        float leftBorder = this.leftBorder;
                        this.setX(leftBorder);
                        this.stopBlock();
                        this.ChildNumbers.setX(this.getX() + this.ChildNumberXoffset);//Make the textview the same position as the block now

                    }
                    else if (this.HitDetection(Blocks)){
                        this.stopBlock();
                    }
                    //If non of the above conditions are met then increase block speed
                    else {
                        //Increment velocity based on set acceleration
                        this.setX(this.getX() - this.Velocity);
                        this.ChildNumbers.setX(this.ChildNumbers.getX() - this.Velocity* velocityfix);
                        this.Velocity += this.blockAcceleration;
                    }
                }
                else {
                    //Release the lock if the block is at or past the border
                    this.stopBlock();
                }
                break;
            case RIGHT:
                if (this.getX() < this.rightBorder){
                    if (this.getX() + this.Velocity > this.rightBorder){
                        float rightBorder = this.rightBorder;
                        this.setX(rightBorder);
                        this.stopBlock();
                        this.ChildNumbers.setX(this.getX() + this.ChildNumberXoffset); //Make the textview the same position as the block now

                    }
                    //If the block isn't about to hit a border check to see if it is about to collide with another stationary block
                    else if (this.HitDetection(Blocks)){
                        this.stopBlock();
                    }
                    //If non of these conditions are made then allow the block to continue its trajectory
                    else {
                        this.setX(this.getX() + this.Velocity);
                        this.ChildNumbers.setX(this.ChildNumbers.getX() + this.Velocity* velocityfix);
                        this.Velocity += blockAcceleration;
                    }
                }
                else{
                    this.stopBlock();
                }
                break;
            case UP:
                if (this.getY() > this.topBorder){
                    if (this.getY() - this.Velocity < this.topBorder){
                        float topBorder = this.topBorder;
                        this.setY(topBorder);
                        this.stopBlock();
                        //Make the textview the same position as the block now
                        this.ChildNumbers.setY(this.getY()+ this.ChildNumberYoffset);

                    }
                    else if (this.HitDetection(Blocks)){
                        this.stopBlock();
                    }
                    else {
                        this.setY(this.getY() - this.Velocity);
                        this.ChildNumbers.setY(this.ChildNumbers.getY()- this.Velocity* velocityfix);
                        this.Velocity += this.blockAcceleration;
                    }
                }
                else {
                    this.stopBlock();
                }
                break;
            case DOWN:
                if (this.getY() < this.bottemBorder){
                    if (this.getY() + this.Velocity > this.bottemBorder){
                        float bottemBorder = this.bottemBorder;
                        this.setY(bottemBorder);
                        this.stopBlock();
                        this.ChildNumbers.setY(this.getY()+ this.ChildNumberYoffset); //Make the textview the same position as the block now
                    }
                    else if (this.HitDetection(Blocks)){
                        this.stopBlock();
                    }
                    else {
                        this.setY(this.getY() + this.Velocity);
                        this.ChildNumbers.setY(this.ChildNumbers.getY() + this.Velocity* velocityfix);
                        this.Velocity += this.blockAcceleration;
                    }
                }
                else {
                    this.stopBlock();
                }
                break;
            default:
                //Blocks are finished moving and are now waiting for move direction to trigger another movement
        }
        this.capVelocity();
    }

    //Hitdetection for movement collision has a switch case for each movement type
    private boolean HitDetection(LinkedList<GameBlock> Blocks){
        switch(this.blockDirection){
            case LEFT:
                for (GameBlock i: Blocks){
                    //This condition checks to see if the borders between the current block and any other block are too close
                    if ((Math.abs((i.getX() + 134) - (this.getX() - 134)) <= this.Velocity) && (i.Velocity == 0) && Math.abs(this.getY() - i.getY()) < 10){
                        if (this.MergeBlocks(i)) {return false;} //If a merge needs to occur let the block slide into the merge block and delete it later
                        this.setX(i.getX() + 268);
                        this.setY(i.getY());
                        this.ChildNumbers.setX(this.getX() + this.ChildNumberXoffset);
                        return true;
                    }
                }
                break;
            case RIGHT:
                for (GameBlock i: Blocks){
                    //This condition checks to see if the borders between the current block and any other block are too close
                    if ((Math.abs((i.getX() - 134) - (this.getX() + 134)) <= this.Velocity) && (i.Velocity == 0) && Math.abs(this.getY() - i.getY()) < 10){
                        if (this.MergeBlocks(i)) {return false;}
                        this.setX(i.getX() - 268);
                        this.setY(i.getY());
                        this.ChildNumbers.setX(this.getX() + this.ChildNumberXoffset);
                        return true;
                    }
                }
                break;
            case UP:
                for (GameBlock i: Blocks){
                    //This condition checks to see if the borders between the current block and any other block are too close
                    if (Math.abs((i.getY() + 134) - (this.getY() - 134)) <= this.Velocity && (i.Velocity == 0) && Math.abs(this.getX() - i.getX()) < 10){
                        if (this.MergeBlocks(i)) {return false;}
                        this.setY(i.getY() + 268);
                        this.setX(i.getX());
                        this.ChildNumbers.setY(this.getY() + this.ChildNumberYoffset);
                        return true;
                    }
                }
                break;
            case DOWN:
                for (GameBlock i: Blocks){
                    //This condition checks to see if the borders between the current block and any other block are too close
                    if (Math.abs((i.getY() - 134) - (this.getY() + 134)) <= this.Velocity && (i.Velocity == 0) && Math.abs(this.getX() - i.getX()) < 10){
                        if (this.MergeBlocks(i)) {return false;}
                        this.setY(i.getY() - 268);
                        this.setX(i.getX());
                        this.ChildNumbers.setY(this.getY() + this.ChildNumberYoffset);
                        return true;
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }

    //Method for merging blocks
    private boolean MergeBlocks(GameBlock MergeBlock){
        //check to see if numbers on textview are the same
        int destroyBlockValue = Integer.parseInt(this.ChildNumbers.getText().toString());
        int mergeBlockValue = Integer.parseInt(MergeBlock.ChildNumbers.getText().toString());
        if (destroyBlockValue == mergeBlockValue && !MergeBlock.mergeLock && !this.mergeLock){
            mergeBlockValue *= 2;
            TheScore += mergeBlockValue;
            MergeBlock.ChildNumbers.setText(String.format("%d",mergeBlockValue));
            //Refreshing settings for textview display
            if (mergeBlockValue >= 1024){
                this.RefactorTextview(50, 50, 40, MergeBlock);
            }
            else if (mergeBlockValue >= 128 && mergeBlockValue < 1024){
                this.RefactorTextview(70,70, 50, MergeBlock);
            }
            else if(mergeBlockValue >= 16 && mergeBlockValue < 128){
                this.RefactorTextview(105, 95, 58, MergeBlock);

            }
            //this.setColor(MergeBlock); //Setting new Color of block
            MergeBlock.mergeLock = true;
            this.mergeLock = true; // Prevent block from merging twice in one movement
            this.deleteBlock = true; //Flag for deleting block
            return true;
        }
        return false;
    }

    //Method for Textview refactoring as numbers increase
    private void RefactorTextview(int Xoffset, int Xcurroffset, int textSize, GameBlock MergeBlock){
        MergeBlock.ChildNumberXoffset = Xoffset;
        MergeBlock.ChildNumbers.setX(MergeBlock.getX() + Xcurroffset);
        MergeBlock.textviewSize = textSize;
        MergeBlock.ChildNumbers.setTextSize(MergeBlock.textviewSize);
    }

    //Method for setting colors of blocks
    private void setColor(GameBlock Block){
        switch(Integer.parseInt(Block.ChildNumbers.getText().toString())){
            case 2:
                Block.setBackgroundColor(Color.GREEN);
                break;
            case 4:
                Block.setBackgroundColor(Color.RED);
                break;
            case 8:
                Block.setBackgroundColor(Color.BLUE);
                break;
            case 16:
                Block.setBackgroundColor(Color.GRAY);
                break;
            case 32:
                Block.setBackgroundColor(Color.BLACK);
                break;
            case 64:
                Block.setBackgroundColor(Color.YELLOW);
                break;
            case 128:
                Block.setBackgroundColor(Color.MAGENTA);
                break;
            case 256:
                Block.setBackgroundColor(Color.CYAN);
                break;
            case 512:
                Block.setBackgroundColor(Color.DKGRAY);
                break;
            default:
                Block.setBackgroundColor(Color.TRANSPARENT);
                break;
        }
    }

    //Caps velocity at 20pixels a frame to avoid choppy animation
    private void capVelocity(){ this.Velocity = (this.Velocity > 100)? 100: this.Velocity; }
    private void stopBlock(){
        this.Velocity = 0;
        this.blockDirection = StateMachine.UNKNOWN;
        this.lock = false;
    }

    //GETTERS AND SETTERS
    public boolean isLocked(){
        return this.lock;
    }
    public void setLocked(boolean locked){this.lock = locked;}
    public float getVelocity(){return this.Velocity;}
    public boolean getDestory(){return this.deleteBlock;}
    public TextView getNumber(){return this.ChildNumbers;}
    public StateMachine getState(){return this.blockDirection;}
    public int getBlockNumber(){return Integer.parseInt(String.valueOf(this.ChildNumbers.getText()));}
}
