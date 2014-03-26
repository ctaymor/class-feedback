package edu.mills.cs180a.classfeedback;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

// TODO documentation
public class CommentFragment extends Fragment {
    private static final String TAG = "COMMENT_FRAGMENT";
    int mRecipient;
    ContentResolver mContentResolver;
    FragmentManager mFragmentManager;
    Fragment classListFragment;
    boolean mMultiPane;
    
    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        return view;
    }
    
    protected void setCommentPane(int recipient, ContentResolver contResolver,
            boolean multiPane) {
        View view = getView();
        mRecipient = recipient;
        mContentResolver = contResolver;
        mFragmentManager = getFragmentManager();
        classListFragment = mFragmentManager.findFragmentById(R.id.listFragment);
        mMultiPane = multiPane;
        
        // Show a picture of the recipient.
        // TODO: This is showing the wrong person.
        assert(mRecipient >= 0 && mRecipient < Person.everyone.length);
        Person person = Person.everyone[recipient];
        ImageView icon = (ImageView) view.findViewById(R.id.commentImageView);
        icon.setImageResource(person.getImageId());
        
        // Show comment if one exists
        EditText commentField =
                (EditText) view.findViewById(R.id.commentEditText);
        Cursor mCursorOfExistingComment =
                mContentResolver.query(Uri.parse(CommentContentProvider.CONTENT_URI
                + "/" + person.getEmail()), null, null, null, null);
        Comment mExistingComment;
        if (mCursorOfExistingComment.moveToFirst()) {
            mExistingComment = cursorToComment(mCursorOfExistingComment);
            commentField.setText(mExistingComment.getContent());
        }
        
        // Add listeners.
        Button saveButton = (Button) view.findViewById(R.id.saveCommentButton);
        saveButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                EditText commentField = (EditText) view.findViewById(R.id.commentEditText);
                String mRecipientEmail = Person.everyone[mRecipient].getEmail();
                ContentValues values = new ContentValues();
                values.put("recipient", mRecipientEmail);
                values.put("content", commentField.getText().toString());
                mContentResolver.insert(Uri.parse(CommentContentProvider.CONTENT_URI
                        + "/" + mRecipientEmail), values);
                if (mMultiPane) {
                    mFragmentManager.beginTransaction()
                    .hide(CommentFragment.this)
                    .addToBackStack(null)
                    .commit();
                }
                // If we're in single-pane mode, show the detail panel and hide the overview list.
                else if (!mMultiPane) {
                    mFragmentManager.beginTransaction()
                    .show(classListFragment)
                    .hide(CommentFragment.this)
                    .addToBackStack(null)
                    .commit();
                }
                Toast.makeText(getActivity(),
                        R.string.comment_added, Toast.LENGTH_SHORT).show();
            }
        });
        
        Button clearButton = (Button) view.findViewById(R.id.clearCommentButton);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick (View view) {
                EditText commentField =
                        (EditText) view.findViewById(R.id.commentEditText);
                commentField.setText(R.string.empty);
            }
        });
        
        Button cancelButton = (Button) view.findViewById(R.id.cancelCommentButton);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMultiPane) {
                    mFragmentManager.beginTransaction()
                    .hide(CommentFragment.this)
                    .addToBackStack(null)
                    .commit();
                }
                // If we're in single-pane mode, show the detail panel and hide the overview list.
                else if (!mMultiPane) {
                    mFragmentManager.beginTransaction()
                    .show(classListFragment)
                    .hide(CommentFragment.this)
                    .addToBackStack(null)
                    .commit();
                }
                Toast.makeText(getActivity(),
                        R.string.comment_canceled, Toast.LENGTH_SHORT).show();
            }
        });
        
         Button deleteButton =
                 (Button) view.findViewById(R.id.deleteCommentButton);
         deleteButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 deleteComment();
             }
         });
         
         Button mailButton = (Button) view.findViewById(R.id.mailCommentButton);
         mailButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_SEND);
                 intent.setType("message/rfc822");
                 String [] emails = {Person.everyone[mRecipient].getEmail()};
                 String mRecipientEmail = Person.everyone[mRecipient].getEmail();
                 Cursor cursor = mContentResolver.query(Uri.parse(CommentContentProvider.CONTENT_URI
                         + "/" + mRecipientEmail), null, null, null, null);
                 //Expect only one comment returned (because unique comments for recipient
                 // TODO I think recipient here maybe is supposed to be the email not the int??
                 if (cursor.moveToFirst()) {
                     Comment mComment = cursorToComment(cursor);
                     intent.putExtra(Intent.EXTRA_EMAIL, emails);
                     intent.putExtra(Intent.EXTRA_SUBJECT, "Comment from class feedback app");
                     intent.putExtra(Intent.EXTRA_TEXT, mComment.getContent());
                     startActivityForResult(Intent.createChooser(intent, "Send Email"), mRecipient);
                 } else {
                     Toast.makeText(getActivity(),
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
        Person mPerson = Person.everyone[mRecipient];
        int deleteSuccessIndicator = 
                mContentResolver.delete(Uri.parse(CommentContentProvider.CONTENT_URI
                        + "/" + mPerson.getEmail()), null, null);
        if (deleteSuccessIndicator == 1) {
            if (mMultiPane) {
                mFragmentManager.beginTransaction()
                .hide(CommentFragment.this)
                .addToBackStack(null)
                .commit();
            }
            // If we're in single-pane mode, show the detail panel and hide the overview list.
            else if (!mMultiPane) {
                mFragmentManager.beginTransaction()
                .show(classListFragment)
                .hide(CommentFragment.this)
                .addToBackStack(null)
                .commit();
            }
            Toast.makeText(getActivity(),
                    R.string.comment_deleted, Toast.LENGTH_SHORT).show();
        } else if (deleteSuccessIndicator == 0) {
            Toast.makeText(getActivity(),
                    R.string.deleting_null, 
                    Toast.LENGTH_SHORT).show();
        } else {
            //This should never happen because comments are unique for each user
            Log.w(TAG, "Multiple comments were deleted for: " + Person.everyone[mRecipient]);
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
}