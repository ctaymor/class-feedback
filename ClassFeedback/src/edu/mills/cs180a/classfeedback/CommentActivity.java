
package edu.mills.cs180a.classfeedback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
            commentField.setText(comment.getContent());
        }
            
        // Add listeners.
        Button saveButton = (Button) findViewById(R.id.saveCommentButton);
        saveButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                EditText commentField =
                        (EditText) findViewById(R.id.commentEditText);
                Person mPerson = Person.everyone[recipient];
                Comment mOldComment =
                        cds.getCommentForRecipient(mPerson.getEmail());
                if (mOldComment != null) {
                    mOldComment.setContent(commentField.getText().toString());
                    cds.updateComment(mOldComment);
                } else {
                    cds.createComment(mPerson.getEmail(), 
                            commentField.getText().toString());
                }
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
                 deleteComment(cds);
             }
         });
         
         Button mailButton = (Button) findViewById(R.id.mailCommentButton);
         mailButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_SEND);
                 intent.setType("message/rfc822");
                 String [] emails = {Person.everyone[recipient].getEmail()};
                 Comment mComment =
                         cds.getCommentForRecipient(
                                 Person.everyone[recipient].getEmail());
                 intent.putExtra(Intent.EXTRA_EMAIL, emails);
                 intent.putExtra(Intent.EXTRA_SUBJECT,
                         "Comment from class feedback app");
                 intent.putExtra(Intent.EXTRA_TEXT, mComment.getContent());
                 startActivityForResult(Intent.createChooser(intent, "Send Email"), recipient);
             }
         });
    }
    
    /**
     * Deletes a comment, finishes this activity, and passes
     * an intent extra back indicating the success of deleting the comment.
     * 
     * @param cds this CommentsDataSource
     */
    protected void deleteComment(CommentsDataSource cds) {
        EditText commentField =
                (EditText) findViewById(R.id.commentEditText);
        Person mPerson = Person.everyone[recipient];
        Comment mComment =
                cds.getCommentForRecipient(mPerson.getEmail());
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CommentActivity.this);
        builder.setMessage(R.string.do_you_want_to_delete);
        builder.setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final CommentsDataSource cds = new CommentsDataSource(CommentActivity.this);
                cds.open();
                deleteComment(cds);
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
