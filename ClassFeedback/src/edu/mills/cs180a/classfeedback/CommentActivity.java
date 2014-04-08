
package edu.mills.cs180a.classfeedback;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
    static final String RECIPIENT = "COMMENT_RECIPIENT";
    private int recipient;
    private static final String TAG = "CommentActivity";
    private EditText mCommentField;
    private ImageView mIcon;
    private Button mSaveButton;
    private Button mClearButton;
    private Button mCancelButton;
    private Button mDeleteButton;
    private Button mMailButton;
    private Person mPerson;
    private Comment mComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        mCommentField = (EditText) findViewById(R.id.commentEditText);
        mIcon = (ImageView) findViewById(R.id.commentImageView);
        mPerson = Person.everyone[recipient];
        mSaveButton = (Button) findViewById(R.id.saveCommentButton);
        mClearButton = (Button) findViewById(R.id.clearCommentButton);
        mCancelButton = (Button) findViewById(R.id.cancelCommentButton);
        mDeleteButton = (Button) findViewById(R.id.deleteCommentButton);
        mMailButton = (Button) findViewById(R.id.mailCommentButton);
        
        // Show a picture of the recipient.
        recipient = getIntent().getIntExtra(RECIPIENT, -1);
        assert(recipient >= 0 && recipient < Person.everyone.length);
        mIcon.setImageResource(mPerson.getImageId());
        
        // Get a connection to the database.
        final CommentsDataSource cds = new CommentsDataSource(this);
        cds.open();
        
        // Show comment
        mComment = cds.getCommentForRecipient(mPerson.getEmail());
        if (mComment != null) {
            mCommentField.setText(mComment.getContent());
        }
            
        // Add listeners.
        mSaveButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                cds.saveComment(mPerson, mCommentField.getText().toString());
                Intent i = new Intent();
                i.putExtra(MainActivity.SUCCESS_TYPE, "Saved");
                setResult(RESULT_OK, i);
                finish();
            }
        });
        
        mClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick (View view) {
                mCommentField.setText(R.string.empty);
            }
        });
        
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        
         mDeleteButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (mComment != null) {
                     cds.deleteComment(mComment);
                     Intent i =  new Intent();
                     i.putExtra(MainActivity.SUCCESS_TYPE, "Deleted");
                     setResult(RESULT_OK, i);
                     finish();
                 } else {
                     Toast.makeText(CommentActivity.this,
                             R.string.deleting_null, 
                             Toast.LENGTH_SHORT).show();
                 }

             }
         });
         
         mMailButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_SEND);
                 intent.setType("message/rfc822");
                 String [] emails = {Person.everyone[recipient].getEmail()};
                 intent.putExtra(Intent.EXTRA_EMAIL, emails);
                 intent.putExtra(Intent.EXTRA_SUBJECT,
                         "Comment from class feedback app");
                 intent.putExtra(Intent.EXTRA_TEXT, mComment.getContent());
                 startActivity(Intent.createChooser(intent, "Send Email"));
             }
         });
    }
}
