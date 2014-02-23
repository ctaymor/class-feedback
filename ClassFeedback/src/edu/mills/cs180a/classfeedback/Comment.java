/**
 * 
 */
package edu.mills.cs180a.classfeedback;

/**
 * A comment meant for a classmate.
 * 
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class Comment {
    private long mId;
    private int mRecipient;
    private String mContent;
    
    Comment(long id, int recipient, String content) {
        mId = id;
        mRecipient = recipient;
        mContent = content;
    }
    
    /**
     * Gets the id of the recipient.
     *
     * @return the id of the recipient
     */
    int getRecipient() {
        return mRecipient;
    }
    
    /**
     * Sets the index of the recipient.
     *
     * @param recipient the index of the recipient
     */
    void setRecipient(int recipient) {
        mRecipient = recipient;
    }
    
    /**
     * Gets the content of the comment.
     *
     * @return the content
     */
    String getContent() {
        return mContent;
    }
    
    /**
     * Sets the content of the comment.
     *
     * @param content the content
     */
    void setContent(String content) {
        mContent = content;
    }
    
    /**
     * Gets the unique id of this comment.
     *
     * @return the id
     */
    long getId() {
        return mId;
    }
}
