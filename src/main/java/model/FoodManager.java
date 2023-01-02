package model;

public class FoodManager {
    public static final int foodDose = 60;
    private int totalFood = 0;
    public int putFood(){
        totalFood += foodDose;
        return foodDose;
    }
    public void takeFood(int quantity){
        totalFood -= quantity;
    }

    public boolean stillFood(){
        return totalFood > 0;
    }

    public void reset(){
        totalFood = 0;
    }
}
