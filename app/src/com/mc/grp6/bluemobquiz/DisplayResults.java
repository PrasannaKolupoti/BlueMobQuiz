package com.mc.grp6.bluemobquiz;

/**
 * Created by Owner on 2017-11-19.
 */
public class DisplayResults {
    private String quizName = "";
    private int marks = 0 ;
    private int rank = 0;
    public void setQuizName(String quizName) {
        this.quizName = quizName;
    }
    public String getQuizName() {
        return quizName;
    }
    public void setMarks(int marks) {
        this.marks = marks;
    }
    public int getMarks() {
        return marks;
    }
    public void setRank(int rank) {
        this.rank = rank;
    }
    public int getRank() {
        return rank;
    }
}
