package jp.techacademy.shohei.sakiyama.qa_app;

import java.io.Serializable;
import java.util.ArrayList;

public class Favorite implements Serializable{
    private String mFavId;
    private String mQuestionUid;
    private ArrayList<String> mQuestionUidList;

    public Favorite(String favId, String questionUid){
        mFavId = favId;
        mQuestionUid = questionUid;
    }

    public String getFavId(){
        return mFavId;
    }

    public String getQuestionUid(){
        return mQuestionUid;
    }
}
