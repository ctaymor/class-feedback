package edu.mills.cs180a.classfeedback.test;

import android.content.Context;
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
    public void close() {      
    }
    
    /**
     * Resets the data source to its initial empty condition.
     */
    public void reset() {
        super.delete(null, null);
    }
}
