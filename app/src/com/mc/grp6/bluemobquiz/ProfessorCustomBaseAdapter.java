package com.mc.grp6.bluemobquiz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by prasa on 2017-11-21.
 */

public class ProfessorCustomBaseAdapter extends BaseAdapter {
    private static ArrayList<DisplayAttendance> attendance = new ArrayList<>();
    private LayoutInflater mInflater;
    public ProfessorCustomBaseAdapter(Context context, ArrayList<DisplayAttendance> finalResults) {
        attendance = finalResults;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return attendance.size();
    }

    public Object getItem(int position) {
        return attendance.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        ProfessorCustomBaseAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.professor_listviewstudentnames, null);
            holder = new ProfessorCustomBaseAdapter.ViewHolder();
            holder.studentName = (TextView) convertView.findViewById(R.id.studentName);
            holder.marks = (TextView) convertView.findViewById(R.id.score);
            convertView.setTag(holder);
        } else {
            holder = (ProfessorCustomBaseAdapter.ViewHolder) convertView.getTag();
        }

        holder.studentName.setText(attendance.get(position).getStudentName());
        holder.marks.setText(Integer.toString(attendance.get(position).getMarks()));
        return convertView;
    }

    static class ViewHolder {
        TextView studentName;
        TextView marks;
    }
}
