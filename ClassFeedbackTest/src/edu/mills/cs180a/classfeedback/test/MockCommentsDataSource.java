package edu.mills.cs180a.classfeedback.test;

import android.content.Context;
import android.database.Cursor;
import edu.mills.cs180a.classfeedback.Comment;
import edu.mills.cs180a.classfeedback.CommentsDataSource;

public class MockCommentsDataSource extends CommentsDataSource {
    private static MockCommentsDataSource instance;
    
    private MockCommentsDataSource(Context context) {
        super(context, null);
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
    public void open() throws UnsupportedOperationException {  
    }
    
    
    @Override
    public void close() {      
    }
    
    @Override
    public int delete(String selection, String[] selectionArgs)
            throws UnsupportedOperationException {
        return 0;
    }
    
    @Override
    public Comment createComment(String recipient, String content) 
            throws UnsupportedOperationException {
        return null;
    }
    
    @Override
    protected void updateComment(Comment comment)
            throws UnsupportedOperationException {
    }
    
    @Override
    protected void deleteComment(Comment comment)
            throws UnsupportedOperationException {
    }
    
    @Override
    public Cursor getCursorForCommentsForRecipient(String recipient,
            String[] projection) throws UnsupportedOperationException {
        return null;
    }
    
    @Override
    protected Comment getCommentForRecipient(String recipient)
            throws UnsupportedOperationException {
        return null;
    }
    
    
    /**
     * Resets the data source to its initial empty condition.
     */
    public void reset() {
        super.delete(null, null);
    }
}
