
package edu.mills.cs180a.classfeedback;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

/**
 * An {@code Activity} that displays a list of the names of {@link Person people in CS 180A}.
 * If a name is clicked on, a {@link CommentActivity} is opened, soliciting a
 * comment for the selected person.
 * 
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class MainActivity extends Activity implements ClassListFragment.OnCommentClickedListener {
    private static final String TAG = "MainActivity";
    private static final int MIN_MULTIPANE_WIDTH = 700;
    static final String SUCCESS_TYPE = "RESULT_SUCCESS_TYPE";
    private FragmentManager fragmentManager;
    private Fragment classListFragment;
    private Fragment commentFragment;
    private boolean multiPane;
    private ContentResolver mContentResolver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentResolver = getContentResolver();
        
        fragmentManager = getFragmentManager();
        classListFragment = fragmentManager.findFragmentById(R.id.listFragment);
        commentFragment = fragmentManager.findFragmentById(R.id.commentFragment);
        
        // Determine whether to use single or multiple panes.
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics(); 
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        float screenHeightDp = displayMetrics.heightPixels / displayMetrics.density;
        multiPane = screenWidthDp >= MIN_MULTIPANE_WIDTH;
        
        // TODO: Handle rotation
        fragmentManager.beginTransaction()
        .hide(commentFragment)
        .commit();
        }
    
    // @Override
    public void onCommentClicked(Person person) {
        //Log.d(TAG, "nowtesting: onCommentClicked with recipient " + recipient);
        //assert(recipient >= 0 && recipient < Person.everyone.length);
        // Show the current story.
        ((CommentFragment) commentFragment).setCommentPane(person,
                mContentResolver, multiPane);
        // If we're in multi-pane mode, show the detail pane if it isn't already visible.
        if (multiPane && commentFragment.isHidden()) {
            fragmentManager.beginTransaction()
            .show(commentFragment)
            .addToBackStack(null)
            .commit();
        }
        // If we're in single-pane mode, show the detail panel and hide the overview list.
        else if (!multiPane) {
            fragmentManager.beginTransaction()
            .show(commentFragment)
            .hide(classListFragment)
            .addToBackStack(null)
            .commit();
        }
    }

}