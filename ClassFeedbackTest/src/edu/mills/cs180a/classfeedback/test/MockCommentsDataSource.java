package edu.mills.cs180a.classfeedback.test;

import java.util.Hashtable;

import android.content.Context;
import android.database.Cursor;
import edu.mills.cs180a.classfeedback.Comment;
import edu.mills.cs180a.classfeedback.CommentsDataSource;

public class MockCommentsDataSource extends CommentsDataSource {
    private static MockCommentsDataSource instance;
    private static Hashtable<String, Comment> mHashtable;
    
    private MockCommentsDataSource(Context context) {
        super(context, null);
        mHashtable = new Hashtable<String, Comment>();
    }
    
    /**
     * Provides a singleton instance of this class.
     * 
     * @param context the context for the underlying database (used only on first call)
     * @return a singleton {@link MockCommentsDataSource}
     */
    public static synchronized MockCommentsDataSource create(Context context) {
        if (instance == null) {
            instance = new MockCommentsDataSource(context);
        }
        return instance;
    }
    
    @Override
    public void open() {  
    }
    
    
    @Override
    public void close() {      
    }
    
    @Override
    public Comment createComment(String recipient, String content) {
        Comment mComment = new Comment(0, recipient, content);
        mHashtable.put(recipient, mComment);
        return mComment;
    }
    
    // TODO Do we actually need this? Not on our list of used methods.
    @Override
    protected void updateComment(Comment comment)
            throws UnsupportedOperationException {
    }
    
    @Override
    protected void deleteComment(Comment comment) {
        mHashtable.remove(comment.getRecipient());
    }
    
    @Override
    protected Comment getCommentForRecipient(String recipient) {
        return mHashtable.get(recipient);
    }
    
    @Override
    public Cursor getCursorForCommentsForRecipient(String recipient,
            String[] projection) throws UnsupportedOperationException {
                return null;
    }
    
    /**
     * Resets the data source to its initial empty condition.
     */
    public void reset() {
        // super.delete(null, null);
        mHashtable.clear();
    }
}
