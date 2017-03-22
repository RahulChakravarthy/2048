package lab2_201_01.uwaterloo.ca.lab4_201_01;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.LinkedList;
import java.util.TimerTask;
import static lab2_201_01.uwaterloo.ca.lab4_201_01.Lab4_201_01.Values;
import static lab2_201_01.uwaterloo.ca.lab4_201_01.Lab4_201_01.gameTimer;

public class GameLoopTask extends TimerTask {

    //Contexts, threads, and layouts
    private Lab4_201_01 lab4_201_01;
    private RelativeLayout MainLayout;
    private Context context;

    //Block and displayed number variables
    private GameBlock factory;
    private LinkedList<GameBlock> Blocks = new LinkedList<>();
    public static LinkedList<TextView> Numbers = new LinkedList<>();
    private double Blocknumber;

    //Score variables
    public static int TheScore;
    private TextView DisplayScore;


    public GameLoopTask(Lab4_201_01 lab4_201_01, Context context, RelativeLayout MainLayout){
        //Setting up factory block and child block
        this.lab4_201_01 = lab4_201_01;
        factory = new GameBlock(MainLayout, context, 0, 0, null);
        this.MainLayout = MainLayout;
        this.context = context;
        this.Blocks.add(factory.CreateBlock(MainLayout, context, -57, -58)); //Start with one block on the board
        this.Blocknumber = 2* Math.ceil(Math.random()*2);
        UpdateTextViews(String.valueOf((int)Blocknumber));
        SetScore();
    }

    @Override
    public void run() {
        //Method that will execute every time the timer period elapses
            this.lab4_201_01.runOnUiThread(
                    new Runnable() {
                        public void run() {
                            //Append the textview with the new number
                            //If the block is already in motion then do not allow for a change midway
                            if (isLocked(Blocks)) {
                                for (GameBlock i : Blocks) {
                                    i.MoveDetection(Values.GetterState());
                                }
                            } else {
                                //Move the block if a motion was captured
                                for (GameBlock i : Blocks) {
                                    i.Move(Blocks);
                                }
                            }
                            //Clean up any Blocks marked for destruction
                            DeleteBlock(Blocks);
                            //Update the score
                            DisplayScore.setText(String.valueOf(TheScore));
                            //This portion gets exectued when a block needs to be generated and all the other blocks have completed their motion
                            if (Values.GetterCreate() && isLocked(Blocks) && noDirection(Blocks) && Values.GetterState() == StateMachine.UNKNOWN && Blocks.size() < 16) {
                                CreateBlock(Blocks);
                                Blocknumber = 2 * Math.ceil(Math.random() * 2); //Create Textview that will follow on top layer of block
                                UpdateTextViews(String.valueOf((int) Blocknumber));
                                Values.SetterCreate(false);
                                if (winningBlock(Blocks)) { //Check to see if game winning block is attained
                                    GameOutcome(true);
                                    Log.wtf("GAME OVER", "YOU WON");
                                    gameTimer.cancel();

                                } else if (isFull(Blocks) && Blocks.size() == 16) { //Checks to see if the board is full and no more possible moves can be made
                                    GameOutcome(false);
                                    Log.wtf("GAME OVER", "GAME OVER");
                                    gameTimer.cancel();
                                }

                                }

                            }
                        }
                    );
    }

    //Set Game Status TextView
    private void GameOutcome(boolean win){
        String Outcome = (win)? "YOU WON!": "YOU LOSE!";
        int color = (win)? Color.GREEN: Color.RED;
        TextView Result = new TextView(context);
        Result.setTextColor(color);
        Result.setText(Outcome);
        Result.setTextSize(35);
        Result.setX(100);
        Result.setY(1450);
        MainLayout.addView(Result);
    }

    private void SetScore(){
        DisplayScore = new TextView(context);
        DisplayScore.setText(String.valueOf(0));
        DisplayScore.setTextColor(Color.BLACK);
        DisplayScore.setTextSize(48);
        DisplayScore.setX(700);
        DisplayScore.setY(1218);
        MainLayout.addView(DisplayScore);
    }

    //Check to see if any of the blocks are locked
    private boolean isLocked(LinkedList<GameBlock> Blocks){
        for (GameBlock i : Blocks){
            if (i.isLocked()){
                return false;
            }
        }
        return true;
    }

    //check to see if all the blocks have stopped moving
    private boolean noDirection(LinkedList<GameBlock> Blocks){
        for (GameBlock i: Blocks){
            if (i.getState() != StateMachine.UNKNOWN){
                return false;
            }
        }
        return true;
    }

    private void CreateBlock(LinkedList<GameBlock> Blocks){
        factory.CreateBlock(MainLayout, context, Blocks);
    }
    private void DeleteBlock(LinkedList<GameBlock> Blocks){
        //Create a Linked list to store all the indices of the blocks that need to be destroyed
        LinkedList<Integer> indices  = new LinkedList<>();
        for (GameBlock i: Blocks){
            if (i.getDestory()){
                indices.add(Blocks.indexOf(i));
                this.MainLayout.removeView(i);
                this.MainLayout.removeView(i.getNumber());
            }
        }
        for (int i = 0; i < indices.size(); i++){
            int temp = indices.get(i); //Capturing the index value of the block that needs to be deleted
            Blocks.remove(temp-i); //subtract by i since the index reduces each time a block is removed
        }
    }
    //Update the numbers textview
    private void UpdateTextViews(String input){
            Numbers.getLast().setText(input);
            Numbers.getLast().setTextColor(Color.BLACK);
            MainLayout.addView(Numbers.getLast());
    }

    private boolean winningBlock(LinkedList<GameBlock> Blocks){
        //Check to see if the 248 block has been reached
        for (GameBlock i: Blocks){
            if (i.getBlockNumber() == 1){
                return true;
            }
        }
        return false;
    }

    private boolean isFull(LinkedList<GameBlock> Blocks){
        //Check to see if adjacent blocks all have different numbers
        for (GameBlock i: Blocks){
            if (i.checkAdjacentBlocks(Blocks)){
                return false;
            }
        }
        return true;
    }

}
