
package edu.mills.cs180a.classfeedback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContentResolver;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * An {@code Activity} that solicits a {@link Comment} about the specified {@link Person}.
 * The recipient of the comment is specified as an index into {@link Person#everyone}
 * and is communicated via the key {@link #RECIPIENT} in the {@link android.intent.Intent}.
 * 
 * <P>The user is given the choice of saving or canceling the comment. They can also clear
 * the text field.  If saved, it is added to the database, and the result 
 * code {@link Activity#RESULT_OK} is provided to the parent activity.  Otherwise, 
 * the database is not modified, and the result code {@link Activity#RESULT_CANCELED}
 * is provided.
 * 
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 * @author ctaymor@gmail.com (Caroline Taymor)
 */
public class CommentActivity extends Activity {
    public static final String RECIPIENT = "COMMENT_RECIPIENT";
    private int recipient;
    private ContentResolver mContentResolver;
    private static final String TAG = "CommentActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if  (getIntent().getBooleanExtra("DEBUG", false)) {
            mContentResolver = new MockContentResolver();
            CommentContentProvider ccp = new CommentContentProvider();
            RenamingDelegatingContext delegContext = new RenamingDelegatingContext(this, "test.");
            ccp.attachInfo(delegContext, null);
            ((MockContentResolver) mContentResolver).addProvider(CommentContentProvider.AUTHORITY, ccp);
        } else {
            mContentResolver = getContentResolver();
        }      
        
        setContentView(R.layout.activity_comment);

        // Show a picture of the recipient.
        recipient = getIntent().getIntExtra(RECIPIENT, -1);
        assert(recipient >= 0 && recipient < Person.everyone.length);
        Person person = Person.everyone[recipient];
        ImageView icon = (ImageView) findViewById(R.id.commentImageView);
        icon.setImageResource(person.getImageId());
        
        // Show comment if one exists
        EditText commentField =
                (EditText) findViewById(R.id.commentEditText);
        Cursor mCursorOfExistingComment =
                mContentResolver.query(Uri.parse(CommentContentProvider.CONTENT_URI
                + "/" + person.getEmail()), null, null, null, null);
        Comment mExistingComment;
        if (mCursorOfExistingComment.moveToFirst()) {
            mExistingComment = cursorToComment(mCursorOfExistingComment);
            commentField.setText(mExistingComment.getContent());
        }


        // Add listeners.
        Button saveButton = (Button) findViewById(R.id.saveCommentButton);
        saveButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                EditText commentField = (EditText) findViewById(R.id.commentEditText);
                String mRecipientEmail = Person.everyone[recipient].getEmail();
                ContentValues values = new ContentValues();
                values.put("recipient", mRecipientEmail);
                values.put("content", commentField.getText().toString());
                mContentResolver.insert(Uri.parse(CommentContentProvider.CONTENT_URI
                        + "/" + mRecipientEmail), values);
                Intent i = new Intent();
                i.putExtra(MainActivity.SUCCESS_TYPE, "Saved");
                setResult(RESULT_OK, i);
                finish();
            }
        });
        
        Button clearButton = (Button) findViewById(R.id.clearCommentButton);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick (View view) {
                EditText commentField =
                        (EditText) findViewById(R.id.commentEditText);
                commentField.setText(R.string.empty);
            }
        });
        
        Button cancelButton = (Button) findViewById(R.id.cancelCommentButton);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        
         Button deleteButton =
                 (Button) findViewById(R.id.deleteCommentButton);
         deleteButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 deleteComment();
             }
         });
         
         Button mailButton = (Button) findViewById(R.id.mailCommentButton);
         mailButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_SEND);
                 intent.setType("message/rfc822");
                 String [] emails = {Person.everyone[recipient].getEmail()};
                 String mRecipientEmail = Person.everyone[recipient].getEmail();
                 Cursor cursor = mContentResolver.query(Uri.parse(CommentContentProvider.CONTENT_URI
                         + "/" + mRecipientEmail), null, null, null, null);
                 //Expect only one comment returned (because unique comments for recipient
                 if (cursor.moveToFirst()) {
                     Comment mComment = cursorToComment(cursor);
                     intent.putExtra(Intent.EXTRA_EMAIL, emails);
                     intent.putExtra(Intent.EXTRA_SUBJECT, "Comment from class feedback app");
                     intent.putExtra(Intent.EXTRA_TEXT, mComment.getContent());
                     startActivityForResult(Intent.createChooser(intent, "Send Email"), recipient);
                 } else {
                     Toast.makeText(CommentActivity.this,
                             R.string.mailing_null, 
                             Toast.LENGTH_SHORT).show();
                 }
             }
         });
    }
    
    /**
     * Deletes a comment, finishes this activity, and passes
     * an intent extra back indicating the success of deleting the comment.
     * 
     */
    protected void deleteComment() {
        Person mPerson = Person.everyone[recipient];
        int deleteSuccessIndicator = 
                mContentResolver.delete(Uri.parse(CommentContentProvider.CONTENT_URI
                        + "/" + mPerson.getEmail()), null, null);
        if (deleteSuccessIndicator == 1) {
            Intent i =  new Intent();
            i.putExtra(MainActivity.SUCCESS_TYPE, "Deleted");
            setResult(RESULT_OK, i);
            finish();
        } else if (deleteSuccessIndicator == 0) {
            Toast.makeText(CommentActivity.this,
                    R.string.deleting_null, 
                    Toast.LENGTH_SHORT).show();
        } else {
            //This should never happen because comments are unique for each user
            Log.w(TAG, "Multiple comments were deleted for: " + recipient);
        }
    }
    
    /**
     * Translates a cursor from a null projection query into a Comment.
     * This method is designed for use with a null projection query and cannot
     * be used with a cursor which does not expect an id in the first column, a
     * string representing the recipient's email in the second column, and a string
     * representing the content in the third column.
     * @param cursor the result from a query, must already be moved into position
     * @return a Comment containing the data from the cursor, i.e. that comment in the database
     */
    private Comment cursorToComment(Cursor cursor) {
        return new Comment(cursor.getLong(0), cursor.getString(1), 
                cursor.getString(2));
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CommentActivity.this);
        builder.setMessage(R.string.do_you_want_to_delete);
        builder.setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteComment();
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
