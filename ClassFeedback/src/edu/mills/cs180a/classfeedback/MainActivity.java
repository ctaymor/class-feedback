
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
public class MainActivity extends Activity
    implements ClassListFragment.OnCommentClickedListener {
    private static final String TAG = "MainActivity";
    private static final int MIN_MULTIPANE_WIDTH = 700;
    static final String SUCCESS_TYPE = "RESULT_SUCCESS_TYPE";
    private static final String KEY_CUR_RECIP = "CURRENT_RECIPIENT";
    private static final String KEY_IS_COMMENT_VIS = "IS_COMMENT_VIS";
    private FragmentManager fragmentManager;
    private Fragment classListFragment;
    private Fragment commentFragment;
    private boolean multiPane;
    private ContentResolver mContentResolver;
    int mCurrentRecipient = -1; // -1 if no recip
    boolean mComFragVis = false;
    
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
        
        if (savedInstanceState != null) {
            mCurrentRecipient = savedInstanceState.getInt(KEY_CUR_RECIP);
            mComFragVis = savedInstanceState.getBoolean(KEY_IS_COMMENT_VIS, false);
            if (mComFragVis) {
                //assertNotEquals(-1, mCurrentRecipient);
                if (multiPane) {
                    Person person = Person.everyone[mCurrentRecipient];
                    ((CommentFragment) commentFragment).setCommentPane(person,
                            mContentResolver, multiPane);
                    fragmentManager.beginTransaction()
                    .show(commentFragment)
                    .addToBackStack(null)
                    .commit();               
                } else {
                    Person person = Person.everyone[mCurrentRecipient];
                    ((CommentFragment) commentFragment).setCommentPane(person,
                            mContentResolver, multiPane);
                    fragmentManager.beginTransaction()
                        .hide(classListFragment).show(commentFragment)
                        .commit();
                }            
            } else {
                fragmentManager.beginTransaction()
                    .hide(commentFragment).commit();
            }
        } else {
            fragmentManager.beginTransaction()
            .hide(commentFragment).commit();
        }
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(KEY_IS_COMMENT_VIS, mComFragVis);
        savedInstanceState.putInt(KEY_CUR_RECIP, mCurrentRecipient);
    }
    
    @Override
    public void onCommentClicked(int recipient) {
        // Show the current Comment.
        Person person = Person.everyone[recipient];
        ((CommentFragment) commentFragment).setCommentPane(person,
                mContentResolver, multiPane);
        mCurrentRecipient = recipient ;
        mComFragVis = true;
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
