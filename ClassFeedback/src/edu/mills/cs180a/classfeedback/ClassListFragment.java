package edu.mills.cs180a.classfeedback;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Fragment to display a list of students with their pictures.  Clicking on
 * a the comment button for a student notifies an
 * {@link OnStorySelectedListener}, which will presumably cause the 
 * {@link DetailFragment} to be displayed.
 * 
 * @author ctaymor@gmail.com (Caroline Taymor)
 * @version 1
 */
public class ClassListFragment extends Fragment {
    private LayoutInflater mInflater;
    private OnCommentClickedListener mComListener;
    
    /**
     * Interface definition to be invoked when a user clicks on the
     * comment button next to a student in the list view.
     */
    protected interface OnCommentClickedListener {
        /**
         * Called when the comment button is clicked
         * @param person the recipient for the comment
         */
        public void onCommentClicked(Person person);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_class_list, container, false);

        // Set up the adapter.
        Activity activity = getActivity();
        
        // Populate a list from Person.everyone.
        ArrayAdapter<Person> adapter = new PersonArrayAdapter(activity);
        ListView listView = (ListView) view.findViewById(R.id.listView1);
        listView.setAdapter(adapter);
        return view;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnCommentClickedListener) {
            mComListener = (OnCommentClickedListener) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implement ClassListFragment.OnCommentClickedListener");
        }
    }
    
    private class ViewHolder {
        ImageView icon;
        TextView name;
        Button commentB;
    }
    
    private class PersonArrayAdapter extends ArrayAdapter<Person> {
        protected static final String TAG = "PERSON_ARRAY_ADAPTER";

        PersonArrayAdapter(Context context) {
            super(context, R.layout.fragment_class_list_row,
                    R.id.rowTextView, Person.everyone);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Handling click events from a row inside a ListView gets very strange.
            // Solution found at "http://stackoverflow.com/questions/1821871".
            ViewHolder holder;
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.fragment_class_list_row, null);
                
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.rowImageView);
                holder.name = (TextView) convertView.findViewById(R.id.rowTextView);
                holder.commentB = (Button) convertView.findViewById(R.id.rowButtonView);
                
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            Person person = getItem(position);
            holder.icon.setImageResource(person.getImageId());
            holder.name.setText(person.getFirstName());

            holder.commentB.setTag(person);
            holder.commentB.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick (View view) {
                    Person person = (Person) view.getTag();
                    mComListener.onCommentClicked(person);
                }
            });
            return convertView;
        }
    }
}
