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

public class CustomBaseAdapter extends BaseAdapter {
    private static ArrayList<DisplayResults> results = new ArrayList<>();
    private LayoutInflater mInflater;
    public CustomBaseAdapter(Context context, ArrayList<DisplayResults> finalResults) {
        results = finalResults;
        mInflater = LayoutInflater.from(context);
    }
 
    public int getCount() {
        return results.size();
    }
 
    public Object getItem(int position) {
        return results.get(position);
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
 
        holder.quizName.setText(results.get(position).getQuizName());
        holder.marks.setText(Integer.toString(results.get(position).getMarks()));
        holder.rank.setText(Integer.toString(results.get(position).getRank()));
 
        return convertView;
    }
 
    static class ViewHolder {
        TextView quizName;
        TextView marks;
        TextView rank;
    }
}