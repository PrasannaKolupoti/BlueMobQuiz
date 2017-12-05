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
//Setting values of multiple textview fields present in listview
public class ProfessorCustomBaseAdapter extends BaseAdapter {
    //Variable declaration and initialization
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
            holder.rank = (TextView) convertView.findViewById(R.id.rank);
            convertView.setTag(holder);
        } else {
            holder = (ProfessorCustomBaseAdapter.ViewHolder) convertView.getTag();
        }
        //setting values of textviews present in listview
        holder.studentName.setText(attendance.get(position).getStudentName());
        holder.marks.setText(Integer.toString(attendance.get(position).getMarks()));
        holder.rank.setText(Integer.toString(attendance.get(position).getRank()));
        return convertView;
    }

    static class ViewHolder {
        //Views in ListView
        TextView studentName;
        TextView marks;
        TextView rank;
    }
}
