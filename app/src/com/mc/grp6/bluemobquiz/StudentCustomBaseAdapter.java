package com.mc.grp6.bluemobquiz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Owner on 2017-11-19.
 */
//Setting values of multiple textview fields present in listview
public class StudentCustomBaseAdapter extends BaseAdapter {
    //Variable declaration and initialization
    private static ArrayList<DisplayResults> result = new ArrayList<>();
    private LayoutInflater mInflater;
    public StudentCustomBaseAdapter(Context context, ArrayList<DisplayResults> finalResults) {
        result = finalResults;
        mInflater = LayoutInflater.from(context);
    }
 
    public int getCount() {
        return result.size();
    }
 
    public Object getItem(int position) {
        return result.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.student_listviewattemptedquizzes, null);
            holder = new ViewHolder();
            holder.quizName = (TextView) convertView.findViewById(R.id.answeredQuiz);
            holder.marks = (TextView) convertView.findViewById(R.id.marks);
            holder.rank = (TextView) convertView.findViewById(R.id.rank);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //setting values of textviews present in listview
        holder.quizName.setText(result.get(position).getQuizName());
        holder.marks.setText(Integer.toString(result.get(position).getMarks()));
        holder.rank.setText(Integer.toString(result.get(position).getRank()));
 
        return convertView;
    }
 
    static class ViewHolder {
        //Views in ListView
        TextView quizName;
        TextView marks;
        TextView rank;
    }
}