package com.mc.grp6.bluemobquiz;

/**
 * Created by prasa on 2017-11-21.
 */

public class DisplayAttendance {
    private String studentName = "";
    private int marks = 0;
    private int rank = 0;
    public void setStudentName(String quizName) {
        this.studentName = quizName;
    }

    public String getStudentName() {
        return studentName;
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

