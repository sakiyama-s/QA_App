package jp.techacademy.shohei.sakiyama.qa_app;

import java.io.Serializable;
import java.util.ArrayList;

public class Favorite implements Serializable {
    private String mFavId;
    private String mQuestionUid;
    private String mGenre;
    private ArrayList<String> mQuestionUidList;

    public Favorite(String favId, String questionUid, String genre) {
        mFavId = favId;
        mQuestionUid = questionUid;
        mGenre = genre;
    }

    public String getFavId() {
        return mFavId;
    }

    public String getQuestionUid() {
        return mQuestionUid;
    }

    public String getGenre() {
        return mGenre;
    }
}
