
package edu.mills.cs180a.classfeedback;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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
    String TAG = "CommentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        
        // Show a picture of the recipient.
        recipient = getIntent().getIntExtra(RECIPIENT, -1);
        assert(recipient >= 0 && recipient < Person.everyone.length);
        Person person = Person.everyone[recipient];
        ImageView icon = (ImageView) findViewById(R.id.commentImageView);
        icon.setImageResource(person.getImageId());
        
        // Get a connection to the database.
        final CommentsDataSource cds = new CommentsDataSource(this);
        cds.open();
        
        // Show comment
        EditText commentField = (EditText) findViewById(R.id.commentEditText);
        Comment comment = cds.getCommentForRecipient(person.getEmail());
        if (comment != null) {
            Log.d(TAG, "The content of the comment for this recipient is now " + comment.getContent());
            commentField.setText(comment.getContent());
        }
            
        // Add listeners.
        Button saveButton = (Button) findViewById(R.id.saveCommentButton);
        saveButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                EditText commentField = (EditText) findViewById(R.id.commentEditText);
                Person mPerson = Person.everyone[recipient];
                Comment mOldComment = cds.getCommentForRecipient(mPerson.getEmail());
                Log.d(TAG, commentField.getText().toString());
                if (mOldComment != null) {
                    Log.d(TAG, "reached if mOldComment is not null");
                    Log.d(TAG, "Now setting content on mOldContent.");
                    mOldComment.setContent(commentField.getText().toString());
                    Log.d(TAG, "mOldComment's content is now " + mOldComment.getContent());
                    cds.updateComment(mOldComment);
                } else {
                    cds.createComment(mPerson.getEmail(), 
                            commentField.getText().toString());
                }
                setResult(RESULT_OK);
                finish();
            }
        });
        
        Button clearButton = (Button) findViewById(R.id.clearCommentButton);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick (View view) {
                EditText commentField = (EditText) findViewById(R.id.commentEditText);
                commentField.setText(R.string.empty);
            }
        });
        
        Button cancelButton = (Button) findViewById(R.id.cancelCommentButton);
        cancelButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
}
