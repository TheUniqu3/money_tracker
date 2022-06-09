package com.example.money_tracker;

import java.util.Comparator;

public class TransactionClass {
    public int amount;
    public String date;
    public String message;
    private boolean positive;

    public TransactionClass(int amount,String date, String message, boolean positive) {
        this.amount = amount;
        this.date = date;
        this.message = message;
        this.positive = positive;
    }

    public int getAmount() {
        return amount;
    }
//    public int getAmountInt() {
//        int amountInt = Integer.parseInt(amount);
//        return amountInt;
//    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDate(){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isPositive() {
        return positive;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }

}

class Sortbyroll implements Comparator<TransactionClass>
{
    public int compare(TransactionClass a, TransactionClass b)
    {
        return a.amount - b.amount;
    }
}