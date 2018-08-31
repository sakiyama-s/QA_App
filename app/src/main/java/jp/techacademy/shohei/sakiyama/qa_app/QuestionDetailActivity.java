package jp.techacademy.shohei.sakiyama.qa_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private Favorite mFavorite;
    private Button buttonFav;
    private ProgressDialog mProgress;
    private boolean favFlag = false;

    private DatabaseReference mFavoriteRef;
    private DatabaseReference mAnswerRef;

    private ChildEventListener mFavoritesEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            // すでにお気に入り登録されているかどうか確認
            HashMap map = (HashMap) dataSnapshot.getValue();
            HashMap favMap = (HashMap) map.get("favorites");
            if (favMap != null) {
                for (Object key : favMap.keySet()) {

                    HashMap tmp = (HashMap) favMap.get(key);

                    Log.d("sa-ki", "key.toString  -----> " + key.toString());
                    Log.d("sa-ki", "tmp.get(questionId) ----->" + tmp.get("questionId"));
                    Log.d("sa-ki", "mQuestion.getQuestionUid ------> " + mQuestion.getQuestionUid());
                    if (tmp.get("questionId").equals(mQuestion.getQuestionUid())) {
                        // もうすでにお気に入り登録されている場合
                        favFlag = true;

                        break;
                    } else {
                        // まだお気に入り登録されていない場合
                        favFlag = false;
                    }

                    Log.d("sa-ki", String.valueOf(favFlag));
                }
            }
            if (favFlag) {
                buttonFav.setText(Const.FavoritePositive);
            } else {
                buttonFav.setText(Const.FavoriteNegative);
            }



        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            HashMap favMap = (HashMap) map.get("favorites");
            if (favMap != null) {
                for (Object key : favMap.keySet()) {
                    HashMap tmp = (HashMap) favMap.get(key);
                    if (tmp.get("questionId").equals(mQuestion.getQuestionUid())) {
                        // もうすでにお気に入り登録されている場合
                        favFlag = true;
                        break;
                    } else {
                        // まだお気に入り登録されていない場合
                        favFlag = false;
                    }
                }
            }
            if (favFlag) {
                buttonFav.setText(Const.FavoritePositive);
            } else {
                buttonFav.setText(Const.FavoriteNegative);
            }

            Log.d("sa-ki", "called onChildRemoved!!!__ flag:" + String.valueOf(favFlag));


        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for (Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d("sa-ki", "こっちのonChildが呼ばれたよ");
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");
        Log.d("sa-ki", "called onCreate!!!!");

        Button mButtonFav = (Button) findViewById(R.id.button_fav);

        setTitle(mQuestion.getTitle());

        // ログイン済みのユーザであればお気に入り登録ボタンを表示する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        buttonFav = (Button) findViewById(R.id.button_fav);
        if (user != null) {
            // ログイン済みユーザなのでお気に入りボタンを表示する
            buttonFav.setVisibility(View.VISIBLE);


            DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
            mFavoriteRef = mDatabaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavoritesPATH);
            mFavoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap map = (HashMap) dataSnapshot.getValue();

                    favFlag = false;

                    // ユーザが、いま開いている質問をすでにお気に入り登録しているか確認する。
                    // 登録していなければfirebaseに追加
                    // 登録していればfirebaseから削除
                    if (map != null) {
                        for (Object key : map.keySet()) {

                            HashMap tmp = (HashMap) map.get((String) key);
                            if (tmp.get("questionId").equals(mQuestion.getQuestionUid())) {
                                // もうすでにお気に入り登録されている場合
                                favFlag = true;
                                break;
                            } else {
                                // まだお気に入り登録されていない場合
                                favFlag = false;

                            }

                            Log.d("sa-ki", String.valueOf(favFlag));
                        }
                    }

                    if (favFlag) {
                        buttonFav.setText(Const.FavoritePositive);
                    } else {
                        buttonFav.setText(Const.FavoriteNegative);
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            mFavoriteRef.addChildEventListener(mFavoritesEventListener);


            if (favFlag) {
                //　すでにお気に入り登録されている
                buttonFav.setText(Const.FavoritePositive);
            } else {
                // お気に入り登録されていない
                buttonFav.setText(Const.FavoriteNegative);
            }


        } else {
            // ログインしていないユーザなのでお気に入りボタンを非表示にして質問詳細ListViewを上に詰める
            buttonFav.setVisibility(View.GONE);
        }


        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });


        // お気に入りボタンを押された時の処理
        mButtonFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                mFavoriteRef = dataBaseReference.child(Const.UsersPATH).child(user.getUid()).child(Const.FavoritesPATH);


                mFavoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap map = (HashMap) dataSnapshot.getValue();

                        favFlag = false;

                        String questionId_tmp = "";

                        // ユーザが、いま開いている質問をすでにお気に入り登録しているか確認する。
                        // 登録していなければfirebaseに追加
                        // 登録していればfirebaseから削除
                        if (map != null) {
                            for (Object key : map.keySet()) {

                                HashMap tmp = (HashMap) map.get((String) key);

//                                Log.d("sa-ki", "key.toString  -----> " + key.toString());
//                                Log.d("sa-ki", "tmp.get(questionId) ----->" + tmp.get("questionId"));
//                                Log.d("sa-ki", "mQuestion.getQuestionUid ------> " + mQuestion.getQuestionUid());
                                if (tmp.get("questionId").equals(mQuestion.getQuestionUid())) {
                                    // もうすでにお気に入り登録されている場合
                                    favFlag = true;
                                    questionId_tmp = key.toString();
                                    break;
                                } else {
                                    // まだお気に入り登録されていない場合
                                    favFlag = false;
                                }

                                Log.d("sa-ki", String.valueOf(favFlag));
                            }
                        }

                        if (favFlag) {
                            // すでにお気に入り登録されている場合はfirebaseから削除
                            mFavoriteRef.child(questionId_tmp).removeValue();
                            buttonFav.setText(Const.FavoritePositive);

                            favFlag = false;
                            Log.d("sa-ki", "お気に入り登録されていたので、解除した");
                        } else {
                            // まだお気に入り登録されていないので、firebaseに追加
                            Map<String, String> data = new HashMap<>();
                            data.put("questionId", mQuestion.getQuestionUid());
                            mFavoriteRef.push().setValue(data);
                            buttonFav.setText(Const.FavoriteNegative);

                            favFlag = true;
                            Log.d("sa-ki", "まだお気に入り登録されていなかったので、登録した");
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }
}
