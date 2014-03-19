/**
 * 
 */
package edu.mills.cs180a.classfeedback.test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.test.IsolatedContext;
import android.test.RenamingDelegatingContext;
import android.test.UiThreadTest;
import android.test.mock.MockContentResolver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import edu.mills.cs180a.classfeedback.Comment;
import edu.mills.cs180a.classfeedback.CommentActivity;
import edu.mills.cs180a.classfeedback.CommentContentProvider;
import edu.mills.cs180a.classfeedback.MySQLiteOpenHelper;
import edu.mills.cs180a.classfeedback.Person;
import edu.mills.cs180a.classfeedback.R;

public class CommentActivityTest extends ActivityInstrumentationTestCase2<CommentActivity> {
    private static final int RECIPIENT_INDEX = 0;  // Use person 0 in Person.everyone.
    private static final Person RECIPIENT = Person.everyone[RECIPIENT_INDEX];
    private static final String COMMENT_TEXT = "lorem ipsum";
    private CommentActivity mActivity;
    private ImageView mImageView;
    private EditText mCommentField;
    private Button mSaveButton;
    private Button mCancelButton;
    private MockContentResolver mResolver;
    private CommentContentProvider ccp;
    private static final String TAG = "CommentActivityTest";

    public CommentActivityTest() {
        super(CommentActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Intent i = new Intent();
        setActivityInitialTouchMode(true);
        i.putExtra(CommentActivity.RECIPIENT, RECIPIENT_INDEX);
        i.putExtra("DEBUG", true);
        setActivityIntent(i);
        // This must occur after setting the touch mode and intent.
        mActivity = getActivity();
        mResolver = new MockContentResolver();
        ccp = new CommentContentProvider();
        RenamingDelegatingContext delegContext = new RenamingDelegatingContext(mActivity, "test.");
        ccp.attachInfo(delegContext, null);

        mResolver.addProvider(CommentContentProvider.AUTHORITY, ccp);
        
        // Initialize references to views.
        mImageView = (ImageView) mActivity.findViewById(R.id.commentImageView);
        mCommentField = (EditText) mActivity.findViewById(R.id.commentEditText);
        mSaveButton = (Button) mActivity.findViewById(R.id.saveCommentButton);
        mCancelButton = (Button) mActivity.findViewById(R.id.cancelCommentButton);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    // Make sure the imageView contains the picture of the right person.
    public void testImageView() {
        Drawable expectedDrawable = 
                mActivity.getResources().getDrawable(RECIPIENT.getImageId());
        // Drawables cannot be compared directly.   Instead, compare their 
        // constant state, which will be the same for any instances
        // created from the same resource.
        assertEquals(expectedDrawable.getConstantState(), 
                mImageView.getDrawable().getConstantState());
    }
    
    // Make sure that the comment field is initially empty.
    public void testCommentFieldEmpty() {
        assertEquals(0, mCommentField.getText().length());
    }

    private int getNumCommentsForRecipient(Person recipient) {
        Cursor mCursor = mResolver.query(Uri.parse(CommentContentProvider.CONTENT_URI 
                + "/" + recipient.getEmail()), null, null, null, null);
        return mCursor.getCount();
    }

    private void testCommentEntryInternal() {
        assertEquals("Database is not empty at beginning of test.",
                0, getNumCommentsForRecipient(RECIPIENT));
        // Simulate entering a comment.
        mCommentField.setText(COMMENT_TEXT);
        mSaveButton.performClick();
        Cursor mCursor = mResolver.query(Uri.parse(CommentContentProvider.CONTENT_URI 
                + "/" + RECIPIENT.getEmail()), null, null, null, null);
        assertTrue(mCursor.moveToFirst());
        assertEquals(RECIPIENT.getEmail(), mCursor.getString(1));
        assertEquals(COMMENT_TEXT, mCursor.getString(2));
    }
    
    // Test comment entry twice, to make sure that the database has no comments
    // at the beginning of each test.  Note that the order in which tests run
    // is undefined within JUnit, so we cannot assume that testCommentEntry1()
    // runs before testCommentEntry2().
    @UiThreadTest
    public void testCommentEntry1() {
       testCommentEntryInternal();
    }
    
    @UiThreadTest
    public void testCommentEntry2() {
       testCommentEntryInternal();
    }
    
    @UiThreadTest
    public void testCancelButtonWithNoComment() {
        if (getNumCommentsForRecipient(RECIPIENT) != 0) {
       //     mCds.deleteComment(mCds.getCommentForRecipient(RECIPIENT.getEmail()));
        }
        //assertEquals(0, getNumCommentsForRecipient(RECIPIENT));
        checkCancelDoesNotChangeComment(); 
    }
    
   @UiThreadTest
   public void testCancelButtonWithComment() {
    //   mCds.createComment(RECIPIENT.getEmail(), COMMENT_TEXT);
       checkCancelDoesNotChangeComment();
   }
   
   @UiThreadTest
   public void testCancelButtonWithUnsavedText() {
       mCommentField.setText("I am not COMMENT_TEXT");
       checkCancelDoesNotChangeComment();
   }
   
   public void checkCancelDoesNotChangeComment() {
       // Test that comment is unchanged
      // Comment mCommentBeforeCancel = mCds.getCommentForRecipient(RECIPIENT.getEmail());
       mCancelButton.performClick();
      // Comment mCommentAfterCancel = mCds.getCommentForRecipient(RECIPIENT.getEmail());
       //assertEquals(mCommentBeforeCancel, mCommentAfterCancel);
   }
}
